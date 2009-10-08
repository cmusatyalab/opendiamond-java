/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 3
 *
 *  Copyright (c) 2007 Carnegie Mellon University
 *  All rights reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

package edu.cmu.cs.diamond.opendiamond;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class Search2 {
    private static class SessionVariables {
        final private String hostname;

        final private Map<String, Double> map;

        public SessionVariables(String hostname, String names[],
                double values[]) {
            this.hostname = hostname;

            Map<String, Double> m = new HashMap<String, Double>();

            for (int i = 0; i < names.length; i++) {
                m.put(names[i], values[i]);
            }

            map = Collections.unmodifiableMap(m);
        }

        public String getHostname() {
            return hostname;
        }

        public Map<String, Double> getVariables() {
            return map;
        }

        @Override
        public String toString() {
            return hostname + ": " + map.toString();
        }
    }

    private static final int MAX_ATTRIBUTE_NAME = 256;

    private Searchlet searchlet;

    volatile private boolean isRunning;

    final private AtomicInteger searchID = new AtomicInteger();

    final private Set<SearchEventListener> searchEventListeners = new HashSet<SearchEventListener>();

    private Set<String> pushAttributes;

    final private ConnectionSet cs = new ConnectionSet();

    private HashMap<String, Cookie> cookieMap;

    public void setSearchlet(Searchlet searchlet) {
        this.searchlet = searchlet;
    }

    public void close() {
        cs.clear();
    }

    public void start() throws IOException {
        byte spec[] = searchlet.toString().getBytes();

        final XDR_sig_and_data fspec = new XDR_sig_and_data(XDR_sig_val
                .createSignature(spec), spec);
        final List<Filter> filters = searchlet.getFilters();
        final int searchID = this.searchID.get();

        CompletionService<?> replies = cs
                .runOnAllServers(new ConnectionFunction<Object>() {
                    @Override
                    public Callable<Object> createCallable(final Connection c) {
                        return new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                c.sendPreStart(pushAttributes, fspec, filters);
                                c.sendStart(searchID);
                                return null;
                            }
                        };
                    }
                });

        setIsRunning(true);
        Util.checkResultsForIOException(cs.size(), replies);
    }

    private static void checkAllReplies(
            CompletionService<MiniRPCReply> replies, int len)
            throws IOException {
        for (int i = 0; i < len; i++) {
            try {
                MiniRPCReply reply = replies.take().get();
                reply.checkStatus();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() throws IOException {
        searchID.incrementAndGet();

        setIsRunning(false);

        ByteBuffer stop = new XDR_stop(0, 0, 0, 0, 0).encode();

        CompletionService<MiniRPCReply> replies = cs.sendToAllControlChannels(
                2, stop);
        checkAllReplies(replies, cs.size());
    }

    private void setIsRunning(boolean running) {
        boolean oldRunning = isRunning;

        // XXX make dispatch thread?
        if (oldRunning != running) {
            synchronized (searchEventListeners) {
                isRunning = running;
                for (SearchEventListener s : searchEventListeners) {
                    // SearchEvent e = new SearchEvent(this);
                    // if (isRunning) {
                    // s.searchStarted(e);
                    // } else {
                    // s.searchStopped(e);
                    // }
                }
            }
        }
    }

    public Result getNextResult() throws InterruptedException {
        BlastChannelObject bco;
        do {
            bco = cs.getNextBlastChannelObject();
            System.out.println("current searchID: " + searchID.get());
            System.out.println("obj searchID: " + bco.getObj().getSearchID());
        } while (bco.getObj().getSearchID() != (searchID.get() & 0xFFFFFFFFL));

        return new JResult(bco.getObj().getAttributes(), bco.getHostname());
    }

    public ServerStatistics[] getStatistics() {
        ServerStatistics noResult[] = new ServerStatistics[0];

        List<ServerStatistics> result = new ArrayList<ServerStatistics>();

        return result.toArray(noResult);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public Map<String, Double> mergeSessionVariables(
            Map<String, Double> globalValues, DoubleComposer composer) {
        // collect all the session variables
        SessionVariables[] sv = getSessionVariables();

        // build new state
        composeVariables(globalValues, composer, sv);

        // set it all back
        setSessionVariables(globalValues);

        return globalValues;
    }

    private void composeVariables(Map<String, Double> globalValues,
            DoubleComposer composer, SessionVariables[] sv) {

        // System.out.println("INPUT: " + Arrays.toString(sv));

        // first, gather all possible keys
        for (SessionVariables v : sv) {
            for (String key : v.getVariables().keySet()) {
                if (!globalValues.containsKey(key)) {
                    globalValues.put(key, 0.0);
                }
            }
        }

        // now, compose them
        for (Map.Entry<String, Double> e : globalValues.entrySet()) {
            String key = e.getKey();

            for (SessionVariables v : sv) {
                Map<String, Double> localValues = v.getVariables();

                // compose !
                double global = e.getValue();
                double local = localValues.containsKey(key) ? localValues
                        .get(key) : 0.0;
                double composedValue = composer.compose(key, global, local);
                globalValues.put(key, composedValue);
            }
        }

        // System.out.println("OUTPUT: " + globalValues);
    }

    private SessionVariables[] getSessionVariables() {
        SessionVariables noResult[] = new SessionVariables[0];
        List<SessionVariables> result = new ArrayList<SessionVariables>();

        // TODO

        return result.toArray(noResult);
    }

    private void setSessionVariables(Map<String, Double> map) {
        // TODO
    }

    public void addSearchEventListener(SearchEventListener listener) {
        synchronized (searchEventListeners) {
            searchEventListeners.add(listener);
        }
    }

    public void removeSearchEventListener(SearchEventListener listener) {
        synchronized (searchEventListeners) {
            searchEventListeners.remove(listener);
        }
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

        Connection conn = Connection.createConnection(host);
        conn.sendCookie(c);

        // start

        // TODO
        // SWIGTYPE_p_p_void newObj = OpenDiamond.create_void_cookie();
        // SWIGTYPE_p_p_char attrs = createStringArrayFromSet(attributes);
        //
        // try {
        // // int err = OpenDiamond.ls_reexecute_filters(handle,
        // // r.getObjectID(),
        // // attrs, newObj);
        // // if (err != 0) {
        // // throw new ReexecutionFailedException("code: " + err);
        // // }
        // SWIGTYPE_p_void obj = OpenDiamond.deref_void_cookie(newObj);
        // return new CResult(obj, makeObjectID(obj));
        // } finally {
        // OpenDiamond.delete_string_array(attrs);
        // OpenDiamond.delete_void_cookie(newObj);
        // }

        return null;
    }

    public void setPushAttributes(Set<String> attributes) {
        // validate
        HashSet<String> copyOfAttributes = new HashSet<String>(attributes);
        for (String string : copyOfAttributes) {
            if (string.length() > MAX_ATTRIBUTE_NAME) {
                throw new IllegalArgumentException("\"" + string
                        + "\" length is greater than MAX_ATTRIBUTE_NAME");
            }
        }
        this.pushAttributes = copyOfAttributes;
    }

    public void defineScope() throws IOException {
        // get newscope file
        File home = new File(System.getProperty("user.home"));
        File diamondDir = new File(home, ".diamond");
        File newscope = new File(diamondDir, "NEWSCOPE");

        InputStream in = new FileInputStream(newscope);
        String megacookie = new String(Util.readFully(in));
        in.close();

        cookieMap = new HashMap<String, Cookie>();

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

        // do it
        cs.setConnectionsFromCookies(cookieMap);
    }

    private List<String> splitCookies(String megacookie) {
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
}
