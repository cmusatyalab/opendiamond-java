package edu.cmu.cs.diamond.opendiamond;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

public class SearchFactory {
    private final ExecutorService executor = new ThreadPoolExecutor(0,
            Integer.MAX_VALUE, 2, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    public static final int MAX_ATTRIBUTE_NAME = 256;

    final private Searchlet searchlet;

    final private Set<String> pushAttributes;

    private Map<String, Cookie> cookieMap;

    public SearchFactory(Searchlet searchlet, Set<String> pushAttributes,
            Map<String, Cookie> cookieMap) {
        this.searchlet = searchlet;

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

    public Search2 createSearch() throws IOException, InterruptedException {
        // start making all the connections

        ArrayList<Future<Connection>> futures = new ArrayList<Future<Connection>>();
        for (Map.Entry<String, Cookie> e : cookieMap.entrySet()) {
            final String hostname = e.getKey();
            final Cookie cookie = e.getValue();

            futures.add(executor.submit(new Callable<Connection>() {
                @Override
                public Connection call() throws Exception {
                    return Connection.createConnection(hostname, cookie);
                }
            }));
        }

        // with the connectionCreator, we want to close everything if
        // anything failed: we'll need to catch and cleanup for exceptions
        InterruptedException ie = null;
        IOException ioe = null;

        Set<Connection> connections = new HashSet<Connection>();
        try {
            for (Future<Connection> f : futures) {
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

        return new Search2(searchlet, cs, pushAttributes);
    }

    private void cleanup(ArrayList<Future<Connection>> futures)
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

        Connection conn = Connection.createConnection(host, c);

        // prestart
        byte[] spec = searchlet.toString().getBytes();
        XDR_sig_and_data fspec = new XDR_sig_and_data(XDR_sig_val
                .createSignature(spec), spec);
        List<Filter> filters = searchlet.getFilters();
        conn.sendPreStart(null, fspec, filters);

        // send eval
        ByteBuffer reexec = new XDR_reexecute(objID, attributes).encode();
        MiniRPCReply reply = new RPC(conn, conn.getHostname(), 21, reexec)
                .doRPC();

        // read reply
        reply.checkStatus();
        Map<String, byte[]> resultAttributes = new XDR_attr_list(reply
                .getMessage().getData()).createMap();

        // create result
        JResult newResult = new JResult(resultAttributes, host, -1);

        // close
        conn.close();

        return newResult;
    }
}
