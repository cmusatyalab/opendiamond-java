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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;

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

    final private ConnectionSet cs;

    private boolean closed;

    public void close() {
        closed = true;
        cs.close();
    }

    public void start() throws InterruptedException, IOException {
        checkClosed();
        CompletionService<?> replies = cs
                .runOnAllServers(new ConnectionFunction<Object>() {
                    @Override
                    public Callable<Object> createCallable(final Connection c) {
                        return new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                c.sendStart();
                                return null;
                            }
                        };
                    }
                });

        try {
            Util.checkResultsForIOException(cs.size(), replies);
        } catch (InterruptedException e) {
            close();
            throw e;
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    private void checkClosed() {
        if (closed) {
            throw new SearchClosedException();
        }
    }

    public Result getNextResult() throws InterruptedException, IOException {
        checkClosed();

        BlastChannelObject bco = cs.getNextBlastChannelObject();

        // check for exception
        IOException e = bco.getException();
        if (e != null) {
            close();

            // make sure there is at least one more sentinel, in case of
            // other waiting threads
            cs.addNoMoreResultsToBlastQueue();

            throw e;
        }

        if (bco == BlastChannelObject.NO_MORE_RESULTS) {
            close();
            cs.addNoMoreResultsToBlastQueue();
            return null;
        }

        return new JResult(bco.getObj().getAttributes(), bco.getHostname());
    }

    public Map<String, ServerStatistics> getStatistics() throws IOException,
            InterruptedException {
        checkClosed();

        Map<String, ServerStatistics> result = new HashMap<String, ServerStatistics>();

        // request_stats = 15
        CompletionService<MiniRPCReply> results = cs.sendToAllControlChannels(
                15, new byte[0]);
        try {
            for (int i = 0; i < cs.size(); i++) {
                try {
                    MiniRPCReply reply = results.take().get();

                    reply.checkStatus();

                    String host = reply.getHostname();
                    MiniRPCMessage msg = reply.getMessage();
                    XDR_dev_stats stats = new XDR_dev_stats(msg.getData());

                    // add
                    result.put(host, new ServerStatistics(stats.getObjsTotal(),
                            stats.getObjsProcessed(), stats.getObjsDropped()));
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof IOException) {
                        IOException e2 = (IOException) cause;
                        throw e2;
                    }
                }
            }
        } catch (IOException e) {
            close();
            throw e;
        }

        return result;
    }

    public Map<String, Double> mergeSessionVariables(
            Map<String, Double> globalValues, DoubleComposer composer) {
        checkClosed();

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

    Search2(ConnectionSet connectionSet) {
        this.cs = connectionSet;
    }
}
