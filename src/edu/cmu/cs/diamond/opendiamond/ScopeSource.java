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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.cmu.cs.diamond.opendiamond.glue.OpenDiamond;
import edu.cmu.cs.diamond.opendiamond.glue.SWIGTYPE_p_p_char;
import edu.cmu.cs.diamond.opendiamond.glue.SWIGTYPE_p_p_void;
import edu.cmu.cs.diamond.opendiamond.glue.groupidArray;

public class ScopeSource {
    static public List<Scope> getPredefinedScopeList() {
        List<Scope> result = new ArrayList<Scope>();

        // malloc!
        SWIGTYPE_p_p_char name = OpenDiamond.create_char_cookie();
        SWIGTYPE_p_p_void cookie = OpenDiamond.create_void_cookie();

        try {
            int val = OpenDiamond.nlkup_first_entry(name, cookie);
            while (val == 0) {
                String nameStr = OpenDiamond.deref_char_cookie(name);

                int arraySize[] = {1024};
                groupidArray gg = new groupidArray(arraySize[0]);

                OpenDiamond.nlkup_lookup_collection(nameStr, arraySize, gg);
                
                result.add(new NameScope(nameStr, gg, arraySize[0]));
                val = OpenDiamond.nlkup_next_entry(name, cookie);
            }
        } finally {
            // do this finally to avoid memory leaks
            OpenDiamond.delete_char_cookie(name);
            OpenDiamond.delete_void_cookie(cookie);
        }

        Collections.reverse(result);
        return result;
    }
}
