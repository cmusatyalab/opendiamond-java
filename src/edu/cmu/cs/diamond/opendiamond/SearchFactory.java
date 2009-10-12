package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class SearchFactory {
    private final ExecutorService executor = new ThreadPoolExecutor(0,
            Integer.MAX_VALUE, 500, TimeUnit.MILLISECONDS,
            new SynchronousQueue<Runnable>());

    public static final int MAX_ATTRIBUTE_NAME = 256;

    private final List<Filter> filters;

    private final List<String> applicationDependencies;

    private Map<String, Cookie> cookieMap;

    public SearchFactory(Collection<Filter> filters,
            Collection<String> applicationDependencies,
            Map<String, Cookie> cookieMap) {
        this.filters = new ArrayList<Filter>(filters);

        this.applicationDependencies = new ArrayList<String>(
                applicationDependencies);

        this.cookieMap = new HashMap<String, Cookie>(cookieMap);
    }

    private Set<String> copyAndValidateAttributes(Set<String> attributes) {
        Set<String> copyOfAttributes = new HashSet<String>(attributes);
        for (String string : copyOfAttributes) {
            if (string.length() > MAX_ATTRIBUTE_NAME) {
                throw new IllegalArgumentException("\"" + string
                        + "\" length is greater than MAX_ATTRIBUTE_NAME");
            }
        }
        return copyOfAttributes;
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

    public Search createSearch(Set<String> desiredAttributes)
            throws IOException, InterruptedException {
        final Set<String> pushAttributes;
        if (desiredAttributes == null) {
            // no filtering requested
            pushAttributes = null;
        } else {
            pushAttributes = copyAndValidateAttributes(desiredAttributes);
        }

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
                // System.out.println(f);
                connections.add(f.get());
            }
        } catch (ExecutionException e1) {
            Throwable cause = e1.getCause();
            if (cause instanceof IOException) {
                // System.out.println("*********");
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

        Search search = new Search(cs);
        search.start();
        return search;
    }

    private void cleanup(List<Future<Connection>> futures)
            throws InterruptedException {
        // System.out.println("cleanup of " + futures);
        InterruptedException ie = null;
        for (Future<Connection> future : futures) {
            try {
                // System.out.println(" cleanup ");
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

    public Result reevaluateResult(Result r, Set<String> desiredAttributes)
            throws IOException {
        Set<String> attributes = copyAndValidateAttributes(desiredAttributes);
        String host = r.getHostname();
        String objID = r.getObjectID();
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
        Result newResult = new Result(resultAttributes, host);

        // close
        conn.close();

        return newResult;
    }
}
