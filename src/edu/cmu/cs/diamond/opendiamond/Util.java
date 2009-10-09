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

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;

import javax.swing.Spring;
import javax.swing.SpringLayout;

import org.jdesktop.swingx.graphics.GraphicsUtilities;

public class Util {
    private Util() {
    }

    // XXX endian specific
    public static int extractInt(byte[] value) {
        return (value[3] & 0xFF) << 24 | (value[2] & 0xFF) << 16
                | (value[1] & 0xFF) << 8 | (value[0] & 0xFF);
    }

    public static long extractLong(byte[] value) {
        return ((long) (value[7] & 0xFF) << 56)
                | ((long) (value[6] & 0xFF) << 48)
                | ((long) (value[5] & 0xFF) << 40)
                | ((long) (value[4] & 0xFF) << 32)
                | ((long) (value[3] & 0xFF) << 24)
                | ((long) (value[2] & 0xFF) << 16)
                | ((long) (value[1] & 0xFF) << 8) | (value[0] & 0xFF);
    }

    public static double extractDouble(byte[] value) {
        return Double.longBitsToDouble(extractLong(value));
    }

    public static double getScaleForResize(int w, int h, int maxW, int maxH) {
        double scale = 1.0;

        double imgAspect = (double) w / h;
        double targetAspect = (double) maxW / maxH;

        if (imgAspect > targetAspect) {
            // more wide
            if (w > maxW) {
                scale = (double) maxW / w;
            }
        } else {
            // more tall
            if (h > maxH) {
                scale = (double) maxH / h;
            }
        }

        return scale;
    }

    public static BufferedImage scaleImage(BufferedImage img, double scale) {
        BufferedImage dest = GraphicsUtilities
                .createCompatibleImage(img, (int) (img.getWidth() * scale),
                        (int) (img.getHeight() * scale));

        return scaleImage(img, dest);
    }

    private static BufferedImage scaleImage(BufferedImage img,
            BufferedImage dest) {

        Graphics2D g = dest.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(img, 0, 0, dest.getWidth(), dest.getHeight(), null);
        g.dispose();

        return dest;
    }

    public static BufferedImage scaleImageFast(BufferedImage img, double scale) {
        BufferedImage dest = GraphicsUtilities
                .createCompatibleImage(img, (int) (img.getWidth() * scale),
                        (int) (img.getHeight() * scale));
        Graphics2D g = dest.createGraphics();
        g.drawImage(img, 0, 0, dest.getWidth(), dest.getHeight(), null);
        g.dispose();

        return dest;
    }

    // http://java.sun.com/docs/books/tutorial/uiswing/examples/layout/SpringGridProject/src/layout/SpringUtilities.java
    /* Used by makeCompactGrid. */
    private static SpringLayout.Constraints getConstraintsForCell(int row,
            int col, Container parent, int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }

    /**
     * Aligns the first <code>rows</code> * <code>cols</code> components of
     * <code>parent</code> in a grid. Each component in a column is as wide as
     * the maximum preferred width of the components in that column; height is
     * similarly determined for each row. The parent is made just big enough to
     * fit them all.
     * 
     * @param parent
     *            container to put grid in
     * @param rows
     *            number of rows
     * @param cols
     *            number of columns
     * @param initialX
     *            x location to start the grid at
     * @param initialY
     *            y location to start the grid at
     * @param xPad
     *            x padding between cells
     * @param yPad
     *            y padding between cells
     */
    public static void makeCompactGrid(Container parent, int rows, int cols,
            int initialX, int initialY, int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout) parent.getLayout();
        } catch (ClassCastException exc) {
            System.err
                    .println("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }

        // Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width, getConstraintsForCell(r, c, parent,
                        cols).getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r,
                        c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        // Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height, getConstraintsForCell(r, c, parent,
                        cols).getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r,
                        c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        // Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }

    public static String extractString(byte[] value) {
        return new String(value, 0, value.length - 1);
    }

    public static void runSimpleSearch(SimpleSearchCallback s)
            throws InterruptedException {
        // init OpenDiamond
        Search search = Search.getSharedInstance();
        search.defineScope();
        search.setSearchlet(s.getSearchlet());

        // begin search
        search.start();

        // process results
        try {
            Result r;
            while ((r = search.getNextResult()) != null) {
                boolean keepGoing = s.processResult(r);
                if (!keepGoing) {
                    break;
                }
            }
        } finally {
            search.stop();
        }
    }

    public static byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte bb[] = new byte[4096];

        int amount;
        while ((amount = in.read(bb)) != -1) {
            out.write(bb, 0, amount);
        }

        return out.toByteArray();
    }

    public static void quickTar1(DataOutputStream out, InputStream in,
            int length, String name) throws IOException {

        // write name length (+1 for zero termination)
        byte nameBytes[] = name.getBytes("UTF-8");
        out.writeInt(nameBytes.length + 1);

        // write size
        out.writeInt(length);

        // write name (zero-terminated)
        out.write(nameBytes);
        out.write(0);

        // write data
        for (int i = 0; i < length; i++) {
            out.write(in.read());
        }
    }

    public static byte[] quickTar(File files[]) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        for (File f : files) {
            if (f.isFile()) {
                String name = f.getName();
                long length = f.length();
                BufferedInputStream in = new BufferedInputStream(
                        new FileInputStream(f));
                try {
                    quickTar1(out, in, (int) length, name);
                } finally {
                    in.close();
                }
            }
        }

        return bos.toByteArray();
    }

    public static byte[] quickTar(File directory) throws IOException {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory + " must be directory");
        }

        return quickTar(directory.listFiles());
    }

    public static void quickTar1(DataOutputStream out, byte[] buf, String name)
            throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        quickTar1(out, in, buf.length, name);
    }

    static void checkResultsForIOException(int size,
            CompletionService<?> connectionCreator) throws IOException,
            InterruptedException {
        for (int i = 0; i < size; i++) {
            try {
                connectionCreator.take().get();
            } catch (ExecutionException e1) {
                Throwable cause = e1.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                e1.printStackTrace();
            }
        }
    }
}
