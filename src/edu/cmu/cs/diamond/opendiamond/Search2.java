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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import edu.cmu.cs.diamond.opendiamond.glue.OpenDiamond;
import edu.cmu.cs.diamond.opendiamond.glue.SWIGTYPE_p_p_char;
import edu.cmu.cs.diamond.opendiamond.glue.SWIGTYPE_p_void;
import edu.cmu.cs.diamond.opendiamond.glue.devHandleArray;

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

    private Searchlet searchlet;

    volatile private boolean isRunning;

    final private AtomicInteger searchID = new AtomicInteger();

    final private Set<SearchEventListener> searchEventListeners = new HashSet<SearchEventListener>();

    private Set<String> pushAttributes;

    final private ConnectionSet cs = new ConnectionSet();

    public void setSearchlet(Searchlet searchlet) {
        this.searchlet = searchlet;
    }

    public void close() {
        cs.clear();
    }

    private XDR_sig_val createSig(byte data[]) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        XDR_sig_val sig = new XDR_sig_val(md.digest(data));

        return sig;
    }

    public void start() throws IOException {
        // prepare searchlet
        if (searchlet != null) {
            byte spec[] = searchlet.toString().getBytes();

            // set the fspec
            XDR_sig_and_data xsf = new XDR_sig_and_data(createSig(spec), spec);
            // device_set_spec = 6
            CompletionService<MiniRPCReply> replies = cs
                    .sendToAllControlChannels(6, xsf.encode());
            checkAllReplies(replies, cs.size());

            // set the codes
            for (Filter f : searchlet.getFilters()) {
                replies = setCodes(f);
                checkAllReplies(replies, cs.size());
            }

            // set the blobs
            for (Filter f : searchlet.getFilters()) {
                replies = setBlobs(f);
                checkAllReplies(replies, cs.size());
            }
        }

        // start search
        ByteBuffer encodedSearchId = ByteBuffer.allocate(4);
        encodedSearchId.putInt(searchID.get()).flip();

        // device_start_search = 1
        CompletionService<MiniRPCReply> replies = cs.sendToAllControlChannels(
                1, encodedSearchId);
        checkAllReplies(replies, cs.size());

        setIsRunning(true);
    }

    private CompletionService<MiniRPCReply> setBlobs(Filter f) {
        CompletionService<MiniRPCReply> replies;
        byte blobData[] = f.getBlob();
        String name = f.getName();

        XDR_sig_val sig = createSig(blobData);

        final ByteBuffer encodedBlobSig = new XDR_blob_sig(name, sig).encode();
        final ByteBuffer encodedBlob = new XDR_blob(name, blobData).encode();

        System.out.println("blob sig: " + encodedBlobSig);

        replies = cs.runOnAllServers(new ConnectionFunction() {
            @Override
            public Callable<MiniRPCReply> createCallable(Connection c) {
                // first, try to set the blob, then send if necessary
                final MiniRPCConnection control = c.getControlConnection();
                final String host = c.getHostname();

                return new Callable<MiniRPCReply>() {
                    @Override
                    public MiniRPCReply call() throws Exception {
                        // device_set_blob_by_signature
                        MiniRPCReply reply1 = new RPC(control, host, 22,
                                encodedBlobSig.duplicate()).call();
                        if (reply1.getMessage().getStatus() != RPC.DIAMOND_FCACHEMISS) {
                            return reply1;
                        }

                        // device_set_blob = 11
                        return new RPC(control, host, 11, encodedBlob
                                .duplicate()).call();
                    }
                };
            }
        });
        return replies;
    }

    private CompletionService<MiniRPCReply> setCodes(Filter f) {
        CompletionService<MiniRPCReply> replies;
        byte code[] = f.getFilterCode().getBytes();
        XDR_sig_val sig = createSig(code);
        XDR_sig_and_data sigAndData = new XDR_sig_and_data(sig, code);

        final ByteBuffer encodedSig = sig.encode();
        final ByteBuffer encodedSigAndData = sigAndData.encode();

        replies = cs.runOnAllServers(new ConnectionFunction() {
            @Override
            public Callable<MiniRPCReply> createCallable(Connection c) {
                // first, try to set the obj, then send if necessary
                final MiniRPCConnection control = c.getControlConnection();
                final String host = c.getHostname();

                return new Callable<MiniRPCReply>() {
                    @Override
                    public MiniRPCReply call() throws Exception {
                        // device_set_obj = 16
                        MiniRPCReply reply1 = new RPC(control, host, 16,
                                encodedSig.duplicate()).call();
                        if (reply1.getMessage().getStatus() != RPC.DIAMOND_FCACHEMISS) {
                            return reply1;
                        }

                        // device_send_obj = 17
                        return new RPC(control, host, 17, encodedSigAndData
                                .duplicate()).call();
                    }
                };
            }
        });
        return replies;
    }

    private static void checkAllReplies(
            CompletionService<MiniRPCReply> replies, int len)
            throws IOException {
        for (int i = 0; i < len; i++) {
            try {
                MiniRPCReply reply = replies.take().get();
                ConnectionSet.checkStatus(reply);
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

        return new JResult(bco.getObj().getAttributes(), bco.getConnection());
    }

    private String makeObjectID(SWIGTYPE_p_void object) {
        // TODO
        SWIGTYPE_p_p_char objectid = OpenDiamond.create_char_cookie();

        try {
            // OpenDiamond.ls_get_objectid(handle, object, objectid);
            return OpenDiamond.get_string_element(objectid, 0);
        } finally {
            OpenDiamond.delete_deref_char_cookie(objectid);
            OpenDiamond.delete_char_cookie(objectid);
        }
    }

    public ServerStatistics[] getStatistics() {
        ServerStatistics noResult[] = new ServerStatistics[0];

        List<ServerStatistics> result = new ArrayList<ServerStatistics>();

        return result.toArray(noResult);
    }

    public boolean isRunning() {
        return isRunning;
    }

    private SWIGTYPE_p_void[] getDevices() {
        int numDevices[] = { 0 };
        devHandleArray devList;
        // while (true) {
        // devList = new devHandleArray(numDevices[0]);
        //
        // TODO
        // int err = OpenDiamond.ls_get_dev_list(handle, devList,
        // numDevices);
        // if (err == 0) {
        // // success
        // break;
        // }
        // }

        // SWIGTYPE_p_void result[] = new SWIGTYPE_p_void[numDevices[0]];
        //
        // for (int i = 0; i < result.length; i++) {
        // result[i] = devList.getitem(i);
        // }

        return null;
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

    public Result reevaluateResult(Result r, Set<String> attributes) {
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
        this.pushAttributes = new HashSet<String>(attributes);
    }

    public void defineScope() throws IOException {
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
