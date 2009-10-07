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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import edu.cmu.cs.diamond.opendiamond.glue.*;

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

    public void start() {
        setPushAttributesInternal();

        // prepare searchlet
        if (searchlet != null) {
            File filterspec;
            try {
                filterspec = searchlet.createFilterSpecFile();
                File filters[] = searchlet.createFilterFiles();
                // OpenDiamond.ls_set_searchlet(handle,
                // device_isa_t.DEV_ISA_IA32,
                // filters[0].getAbsolutePath(), filterspec
                // .getAbsolutePath());
                // for (int i = 1; i < filters.length; i++) {
                // OpenDiamond.ls_add_filter_file(handle,
                // device_isa_t.DEV_ISA_IA32, filters[i]
                // .getAbsolutePath());
                // }

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            for (Filter f : searchlet.getFilters()) {
                byte blob[] = f.getBlob();
                // OpenDiamond.ls_set_blob(handle, f.getName(), blob.length,
                // blob);
            }
        }

        // begin
        // OpenDiamond.ls_start_search(handle);

        setIsRunning(true);
    }

    public void stop() {
        // OpenDiamond.ls_terminate_search(handle);
        setIsRunning(false);
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
        return null;
    }

    private String makeObjectID(SWIGTYPE_p_void object) {
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

        // get device list
        SWIGTYPE_p_void devices[] = getDevices();

        SWIGTYPE_p_p_device_session_vars_t varsHandle = OpenDiamond
                .create_session_vars_handle();

        try {
            // for each device, get variables
            for (SWIGTYPE_p_void dev : devices) {
                // OpenDiamond.ls_get_dev_session_variables(handle, dev,
                // varsHandle);
                device_session_vars_t vars = OpenDiamond
                        .deref_session_vars_handle(varsHandle);
                try {
                    SWIGTYPE_p_p_char names = vars.getNames();
                    doubleArray values = vars.getValues();

                    int len = vars.getLen();
                    String namesArray[] = new String[len];
                    double valuesArray[] = new double[len];

                    for (int i = 0; i < len; i++) {
                        namesArray[i] = OpenDiamond
                                .get_string_element(names, i);
                        valuesArray[i] = values.getitem(i);
                    }

                    // String name = OpenDiamond.ls_get_dev_name(handle, dev);
                    // SessionVariables sv = new SessionVariables(name,
                    // namesArray, valuesArray);
                    // result.add(sv);
                } finally {
                    OpenDiamond.delete_session_vars(vars);
                }
            }
        } finally {
            OpenDiamond.delete_session_vars_handle(varsHandle);
        }

        return result.toArray(noResult);
    }

    private void setSessionVariables(Map<String, Double> map) {
        device_session_vars_t vars = OpenDiamond
                .create_session_vars(map.size());
        try {
            SWIGTYPE_p_p_char names = vars.getNames();
            doubleArray values = vars.getValues();

            int i = 0;
            for (Map.Entry<String, Double> e : map.entrySet()) {
                OpenDiamond.set_string_element(names, i, e.getKey());
                values.setitem(i, e.getValue());
                i++;
            }

            SWIGTYPE_p_void[] devices = getDevices();
            for (SWIGTYPE_p_void dev : devices) {
                // OpenDiamond.ls_set_dev_session_variables(handle, dev, vars);
            }
        } finally {
            OpenDiamond.delete_session_vars(vars);
        }
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
        SWIGTYPE_p_p_void newObj = OpenDiamond.create_void_cookie();
        SWIGTYPE_p_p_char attrs = createStringArrayFromSet(attributes);

        try {
            // int err = OpenDiamond.ls_reexecute_filters(handle,
            // r.getObjectID(),
            // attrs, newObj);
            // if (err != 0) {
            // throw new ReexecutionFailedException("code: " + err);
            // }
            SWIGTYPE_p_void obj = OpenDiamond.deref_void_cookie(newObj);
            return new Result(obj, makeObjectID(obj));
        } finally {
            OpenDiamond.delete_string_array(attrs);
            OpenDiamond.delete_void_cookie(newObj);
        }
    }

    private SWIGTYPE_p_p_char createStringArrayFromSet(Set<String> set) {
        SWIGTYPE_p_p_char result = OpenDiamond.create_string_array(set.size());

        int i = 0;
        for (String s : set) {
            OpenDiamond.set_string_element(result, i, s);
            i++;
        }

        return result;
    }

    public void setPushAttributes(Set<String> attributes) {
        this.pushAttributes = new HashSet<String>(attributes);
    }

    private void setPushAttributesInternal() {
        if (pushAttributes != null) {
            SWIGTYPE_p_p_char attrs = createStringArrayFromSet(pushAttributes);
            try {
                // OpenDiamond.ls_set_push_attributes(handle, attrs);
            } finally {
                OpenDiamond.delete_string_array(attrs);
            }
        }
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

    public static void main(String[] args) {
        Search2 s = new Search2();

        try {
            s.defineScope();
            s.defineScope();

            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
