package edu.cmu.cs.diamond.opendiamond;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;

public class SearchFactory {
    private final ExecutorService executor = new ThreadPoolExecutor(0,
            Integer.MAX_VALUE, 500, TimeUnit.MILLISECONDS,
            new SynchronousQueue<Runnable>());

    public static final int MAX_ATTRIBUTE_NAME = 256;

    private final List<Filter> filters;

    private final List<String> applicationDependencies;

    private final Set<String> pushAttributes;

    private Map<String, Cookie> cookieMap;

    public SearchFactory(List<Filter> filters,
            List<String> applicationDependencies, Set<String> pushAttributes,
            Map<String, Cookie> cookieMap) {
        this.filters = new ArrayList<Filter>(filters);

        this.applicationDependencies = new ArrayList<String>(
                applicationDependencies);

        // validate attributes
        Set<String> copyOfAttributes = new HashSet<String>(pushAttributes);
        for (String string : copyOfAttributes) {
            if (string.length() > MAX_ATTRIBUTE_NAME) {
                throw new IllegalArgumentException("\"" + string
                        + "\" length is greater than MAX_ATTRIBUTE_NAME");
            }
        }

        this.pushAttributes = copyOfAttributes;
        this.cookieMap = new HashMap<String, Cookie>(cookieMap);
    }

    private XDR_sig_and_data getFspec() {
        StringBuilder sb = new StringBuilder();
        for (Filter f : filters) {
            sb.append(f.toString());
        }

        if (!applicationDependencies.isEmpty()) {
            sb.append("FILTER APPLICATION\n");
            for (String d : applicationDependencies) {
                sb.append("REQUIRES " + d + "\n");
            }
        }

        byte spec[] = sb.toString().getBytes();
        return new XDR_sig_and_data(XDR_sig_val.createSignature(spec), spec);
    }

    public Search2 createSearch() throws IOException, InterruptedException {
        // make all the connections and prep everything to start
        final XDR_sig_and_data fspec = getFspec();

        List<Future<Connection>> futures = new ArrayList<Future<Connection>>();
        CompletionService<Connection> connectService = new ExecutorCompletionService<Connection>(
                executor);
        for (Map.Entry<String, Cookie> e : cookieMap.entrySet()) {
            final String hostname = e.getKey();
            final Cookie cookie = e.getValue();

            futures.add(connectService.submit(new Callable<Connection>() {
                @Override
                public Connection call() throws Exception {
                    return Connection.createConnection(hostname, cookie,
                            pushAttributes, fspec, filters);
                }
            }));
        }

        // with the connectionCreator, we want to close everything if
        // anything failed: we'll need to catch and cleanup for exceptions
        InterruptedException ie = null;
        IOException ioe = null;

        Set<Connection> connections = new HashSet<Connection>();
        try {
            for (int i = 0; i < futures.size(); i++) {
                Future<Connection> f = connectService.take();
                System.out.println(f);
                connections.add(f.get());
            }
        } catch (ExecutionException e1) {
            Throwable cause = e1.getCause();
            if (cause instanceof IOException) {
                System.out.println("*********");
                IOException e = (IOException) cause;
                ioe = e;
            }
        } catch (InterruptedException e2) {
            ie = e2;
        }

        if (ie != null) {
            cleanup(futures);
            throw ie;
        }
        if (ioe != null) {
            cleanup(futures);
            throw ioe;
        }

        // we're safe
        ConnectionSet cs = new ConnectionSet(executor, connections);

        return new Search2(cs);
    }

    private void cleanup(List<Future<Connection>> futures)
            throws InterruptedException {
        System.out.println("cleanup of " + futures);
        InterruptedException ie = null;
        for (Future<Connection> future : futures) {
            try {
                System.out.println(" cleanup ");
                Connection c = future.get();
                c.close();
            } catch (ExecutionException e) {
                // e.printStackTrace();
            } catch (InterruptedException e) {
                ie = e;
            }
        }
        if (ie != null) {
            throw ie;
        }
    }

    public static Map<String, Cookie> createDefaultCookieMap()
            throws IOException {
        // get newscope file
        File home = new File(System.getProperty("user.home"));
        File diamondDir = new File(home, ".diamond");
        File newscope = new File(diamondDir, "NEWSCOPE");

        InputStream in = new FileInputStream(newscope);
        String megacookie = new String(Util.readFully(in));
        in.close();

        Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();

        // fill map from hostnames to cookies
        List<String> cookies = splitCookies(megacookie);
        for (String s : cookies) {
            Cookie c = new Cookie(s);
            System.out.println(c);

            List<String> servers = c.getServers();
            for (String server : servers) {
                cookieMap.put(server, c);
            }
        }

        return cookieMap;
    }

    private static List<String> splitCookies(String megacookie) {
        List<String> result = new ArrayList<String>();

        String lines[] = megacookie.split("\n");
        boolean inCookie = false;
        StringBuilder sb = null;
        for (String l : lines) {
            if (l.equals(Cookie.BEGIN_COOKIE)) {
                inCookie = true;
                sb = new StringBuilder();
            }

            if (!inCookie) {
                continue;
            }

            sb.append(l);
            sb.append('\n');

            if (!l.equals(Cookie.END_COOKIE)) {
                continue;
            }

            inCookie = false;
            result.add(sb.toString());
        }

        return result;
    }

    public Result reevaluateResult(Result r, Set<String> attributes)
            throws IOException {
        JResult jr = (JResult) r;

        String host = jr.getHostname();
        String objID = jr.getObjectID();
        Cookie c = cookieMap.get(host);

        if (c == null) {
            throw new IOException("No cookie found for host " + host);
        }

        // prestart
        XDR_sig_and_data fspec = getFspec();
        Connection conn = Connection.createConnection(host, c, null, fspec,
                filters);

        // send eval
        byte reexec[] = new XDR_reexecute(objID, attributes).encode();
        MiniRPCReply reply = new RPC(conn, conn.getHostname(), 21, reexec)
                .doRPC();

        // read reply
        reply.checkStatus();
        Map<String, byte[]> resultAttributes = new XDR_attr_list(reply
                .getMessage().getData()).createMap();

        // create result
        JResult newResult = new JResult(resultAttributes, host);

        // close
        conn.close();

        return newResult;
    }
}
