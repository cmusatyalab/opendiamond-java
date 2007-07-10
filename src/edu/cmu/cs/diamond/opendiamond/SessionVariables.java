package edu.cmu.cs.diamond.opendiamond;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class SessionVariables {
    final private InetAddress address;
    final private Map<String, Double> map = new HashMap<String, Double>();
    
    public SessionVariables(InetAddress address, String names[], double values[]) {
        this.address = address;
        
        for (int i = 0; i < names.length; i++) {
            map.put(names[i], values[i]);
        }
    }

    public InetAddress getAddress() {
        return address;
    }

    public Map<String, Double> getVariables() {
        Map<String, Double> result = new HashMap<String, Double>();
        
        map.putAll(result);
        
        return result;
    }
    
    @Override
    public String toString() {
        return address.getCanonicalHostName() + ": " + map.toString();
    }
}
