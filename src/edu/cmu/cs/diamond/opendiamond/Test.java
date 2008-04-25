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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String scopeName = null;
        if (args.length >= 1) {
            scopeName = args[0];
        }

        // get scopes
        Scope scope = null;
        List<Scope> scopes = ScopeSource.getPredefinedScopeList();
        for (Scope s : scopes) {
            System.out.println(s);
            if (s.getName().equals(scopeName)) {
                scope = s;
            }
        }

        if (scope == null) {
            System.out.println("Cannot find scope \"" + scopeName
                    + "\" from command line");
            System.exit(1);
        }

        // set up the rgb filter
        Filter rgb = null;
        try {
            FilterCode c = new FilterCode(new FileInputStream(
                    "/opt/diamond/lib/fil_rgb.so"));
            rgb = new Filter("RGB", c, "f_eval_img2rgb", "f_init_img2rgb",
                    "f_fini_img2rgb", 1, new String[0], new String[0], 400);
            System.out.println(rgb);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // init diamond
        Search search = Search.getSharedInstance();
        search.setScope(scope);

        // make a new searchlet
        Searchlet searchlet = new Searchlet();
        searchlet.addFilter(rgb);
        searchlet.setApplicationDependencies(new String[] { "RGB" });
        search.setSearchlet(searchlet);

        Result r;

        DoubleComposer sum = new DoubleComposer() {
            public double compose(String key, double a, double b) {
                // ignore key
                return a + b;
            }
        };

        for (int ii = 0; ii < 1; ii++) {
            // begin search
            search.start();

            Map<String, Double> map = new HashMap<String, Double>();
            map.put("Hi", 42.0);
            map.put("Oops", 23842938.0);

            search.mergeSessionVariables(map, sum);

            // read some results
            int count = 0;
            try {
                while ((r = search.getNextResult()) != null && count < 10) {
                    processResult(r);

                    System.out.println(search.mergeSessionVariables(map, sum));

                    count++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (Test.class) {
                try {
                    Test.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            search.stop();
        }
    }

    private static void processResult(Result r) {
        System.out.println(r);

        byte data[] = r.getData();

        BufferedImage img;
        // try the uncompressed data
        data = r.getValue("_rgb_image.rgbimage");
        byte tmp[] = r.getValue("_cols.int");
        int w = Util.extractInt(tmp);
        tmp = r.getValue("_rows.int");
        int h = Util.extractInt(tmp);

        System.out.println(w + "x" + h);

        img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = (y * w + x) * 4;
                // System.out.println(x);
                // System.out.println(y);
                int val = (data[i] & 0xFF) << 16 | (data[i + 1] & 0xFF) << 8
                        | (data[i + 2] & 0xFF);
                img.setRGB(x, y, val);
            }
        }

        // JFrame j = new JFrame();
        // j.setLocationByPlatform(true);
        // j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // j.getContentPane().add(new JButton(new ImageIcon(img)));
        // j.pack();
        // j.setVisible(true);
    }
}
