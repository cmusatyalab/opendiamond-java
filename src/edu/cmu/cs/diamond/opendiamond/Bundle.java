/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 6
 *
 *  Copyright (c) 2011 Carnegie Mellon University
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import edu.cmu.cs.diamond.opendiamond.bundle.*;

public class Bundle {
    private static class Manifest {
        private static final JAXBContext jaxbContext;

        private static final Schema schema;

        static {
            JAXBContext ctx = null;
            Schema s = null;
            try {
                ctx = JAXBContext.newInstance(SearchSpec.class);
                SchemaFactory sf = SchemaFactory.newInstance(
                        XMLConstants.W3C_XML_SCHEMA_NS_URI);
                s = sf.newSchema(Bundle.class.getClassLoader().
                        getResource("resources/bundle.xsd"));
            } catch (JAXBException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
            // commit
            jaxbContext = ctx;
            schema = s;
        }


        private final SearchSpec spec;

        private final boolean isCodec;

        public Manifest(InputStream in) throws BundleFormatException {
            try {
                Unmarshaller u = jaxbContext.createUnmarshaller();
                u.setSchema(schema);
                StreamSource source = new StreamSource(in);
                JAXBElement<SearchSpec> element = u.unmarshal(source,
                        SearchSpec.class);
                this.spec = element.getValue();
                this.isCodec = element.getName().getLocalPart()
                        .equals("codec");
            } catch (JAXBException e) {
                String msg = e.getMessage();
                Throwable linked = e.getLinkedException();
                if (linked != null) {
                    msg = linked.getMessage();
                }
                throw new BundleFormatException(msg);
            }
        }

        public SearchSpec getSpec() {
            return spec;
        }

        public boolean isCodec() {
            return isCodec;
        }
    }

    private static abstract class FileLoader {
        protected static final String MANIFEST_NAME = "opendiamond-search.xml";

        public abstract PreparedFileLoader getPreparedLoader()
                throws IOException;

        public abstract Manifest getManifest() throws IOException;
    }

    private static class PendingFileLoader extends FileLoader {
        private final File bundleFile;

        private final List<File> memberDirs;

        public PendingFileLoader(File bundleFile, List<File> memberDirs) {
            this.bundleFile = bundleFile;
            this.memberDirs = memberDirs;
        }

        @Override
        public PreparedFileLoader getPreparedLoader() throws IOException {
            return new PreparedFileLoader(new FileInputStream(bundleFile),
                    memberDirs);
        }

        @Override
        public Manifest getManifest() throws IOException {
            // Read the search manifest from the bundle and return it without
            // loading the entire bundle into memory
            FileInputStream in = new FileInputStream(bundleFile);
            try {
                ZipInputStream zip = new ZipInputStream(in);
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (entry.getName().equals(MANIFEST_NAME)) {
                        return new Manifest(zip);
                    } else {
                        zip.closeEntry();
                    }
                }
                throw new BundleFormatException("Bundle manifest not found");
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static class PreparedFileLoader extends FileLoader {
        private final Map<String, byte[]> bundleContents;

        private final List<File> memberDirs;

        public PreparedFileLoader(InputStream in, List<File> memberDirs)
                throws IOException {
            this.bundleContents = Util.readZipFile(in);
            this.memberDirs = memberDirs;
        }

        @Override
        public PreparedFileLoader getPreparedLoader() throws IOException {
            return this;
        }

        @Override
        public Manifest getManifest() throws IOException {
            byte[] manifest = bundleContents.get(MANIFEST_NAME);
            if (manifest == null) {
                throw new BundleFormatException("Bundle manifest not found");
            }
            return new Manifest(new ByteArrayInputStream(manifest));
        }

        public FilterCode getCode(String name) throws IOException {
            return new FilterCode(getBlob(name));
        }

        public byte[] getBlob(String name) throws IOException {
            byte[] data = bundleContents.get(name);
            if (data != null) {
                return data;
            }
            for (File dir : memberDirs) {
                File file = new File(dir, name);
                if (file.exists()) {
                    return Util.readFully(new FileInputStream(file));
                }
            }
            throw new IOException("File not found: " + name);
        }
    }

    private static class PendingFilter {
        private final String name;

        private final String label;

        private final FilterCode code;

        private final byte[] blob;

        private final double minScore;

        private final double maxScore;

        private final List<String> dependencies = new ArrayList<String>();

        private final List<String> dependencyLabels = new ArrayList<String>();

        private final List<String> arguments = new ArrayList<String>();

        public PendingFilter(PreparedFileLoader loader,
                Map<String, String> optionMap, List<BufferedImage> examples,
                FilterSpec f) throws IOException {
            // load basic metadata
            label = f.getLabel();

            // load code
            code = loader.getCode(f.getCode());

            // load blob
            FilterBlobArgumentSpec blobSpec = f.getBlob();
            if (blobSpec != null) {
                FilterBlobExampleSpec exampleSpec = blobSpec.getExamples();
                if (blobSpec.getMembers().size() > 0 || exampleSpec != null) {
                    // Construct a Zip file containing individual members.
                    // First add the explicit members
                    Map<String, byte[]> zipMap = new HashMap<String, byte[]>();
                    for (FilterBlobMemberSpec member : blobSpec.getMembers()) {
                        byte[] data = getBlobData(loader, optionMap,
                                member.getOption(), member.getData());
                        zipMap.put(member.getFilename(), data);
                    }
                    // Now add the examples
                    if (exampleSpec != null) {
                        if (examples == null) {
                            throw new BundleFormatException(
                                    "Missing example specification");
                        }
                        // Add examples directory
                        zipMap.put("examples/", new byte[0]);
                        int i = 0;
                        for (BufferedImage example : examples) {
                            zipMap.put(String.format("examples/%07d.png", i++),
                                     encodePNG(example));
                        }
                    }
                    blob = Util.encodeZipFile(zipMap);
                } else {
                    // Blob is specified directly
                    blob = getBlobData(loader, optionMap,
                            blobSpec.getOption(), blobSpec.getData());
                }
            } else {
                blob = new byte[0];
            }

            // load thresholds
            minScore = getThreshold(optionMap, f.getMinScore(),
                    Double.NEGATIVE_INFINITY);
            maxScore = getThreshold(optionMap, f.getMaxScore(),
                    Double.POSITIVE_INFINITY);

            // load fixed dependencies and pending labels
            FilterDependencyList depList = f.getDependencyList();
            if (depList != null) {
                for (FilterDependencySpec dep : depList.getDependencies()) {
                    String depLabel = dep.getLabel();
                    String depName = dep.getFixedName();
                    if (depLabel != null) {
                        dependencyLabels.add(depLabel);
                    } else if (depName != null) {
                        dependencies.add(depName);
                    } else {
                        throw new BundleFormatException(
                                "Missing dependency specification");
                    }
                }
            }

            // load arguments
            FilterArgumentList argList = f.getArgumentList();
            if (argList != null) {
                for (FilterArgumentSpec arg : argList.getArguments()) {
                    String option = arg.getOption();
                    String value;
                    if (option != null) {
                        value = getOptionValue(optionMap, option);
                    } else {
                        value = arg.getValue();
                        if (value == null) {
                            throw new BundleFormatException(
                                    "Missing argument specification");
                        }
                    }
                    arguments.add(value);
                }
            }

            // set filter name
            String name = f.getFixedName();
            if (name == null) {
                try {
                    MessageDigest m = MessageDigest.getInstance("SHA-256");
                    m.update(code.getBytes());
                    for (String arg : arguments) {
                        m.update(arg.getBytes());
                        m.update((byte) 0);
                    }
                    m.update(blob);
                    byte[] digest = m.digest();
                    Formatter ff = new Formatter();
                    for (byte b : digest) {
                        ff.format("%02x", b & 0xFF);
                    }
                    name = "z" + ff.toString();
                } catch (NoSuchAlgorithmException e) {
                    // can't happen on java 6?
                    e.printStackTrace();
                    name = "";
                }
            }
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getLabel() {
            return label;
        }

        public Filter getFilter(Map<String, String> labelMap)
                throws BundleFormatException {
            HashSet<String> deps = new HashSet<String>(dependencies);
            for (String label : dependencyLabels) {
                String dep = labelMap.get(label);
                if (dep == null) {
                    throw new BundleFormatException(
                            "Missing filter label \"" + label + "\"");
                }
                deps.add(dep);
            }
            return new Filter(name, code, minScore, maxScore, deps,
                    arguments, blob);
        }

        private static String getOptionValue(Map<String, String> optionMap,
                String option) throws BundleFormatException {
            String value = optionMap.get(option);
            if (value == null) {
                throw new BundleFormatException(
                        "Missing option \"" + option + "\"");
            }
            return value;
        }

        private static double getThreshold(Map<String, String> optionMap,
                FilterThresholdSpec threshold, double defaultValue)
                throws BundleFormatException {
            if (threshold == null) {
                return defaultValue;
            }
            String option = threshold.getOption();
            if (option != null) {
                String value = getOptionValue(optionMap, option);
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new BundleFormatException("Couldn't parse " +
                            "option \"" + option + "\" for threshold");
                }
            } else {
                Double d = threshold.getValue();
                if (d == null) {
                    throw new BundleFormatException(
                            "Missing threshold specification");
                }
                return d.doubleValue();
            }
        }

        // option is the name of an option containing the blob filename,
        // data is the blob filename
        private static byte[] getBlobData(PreparedFileLoader loader,
                Map<String, String> optionMap, String option, String data)
                throws IOException {
            String filename;
            if (option != null) {
                filename = getOptionValue(optionMap, option);
            } else if (data != null) {
                filename = data;
            } else {
                throw new BundleFormatException(
                        "Missing blob data specification");
            }
            // Validation rule from the Filename XSD type
            if (!filename.matches("^[A-Za-z0-9_.-]+$")) {
                throw new BundleFormatException(
                        "Invalid filename for blob data");
            }
            return loader.getBlob(filename);
        }

        private static byte[] encodePNG(BufferedImage image)
                throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (!ImageIO.write(image, "PNG", baos)) {
                throw new IOException("Couldn't write PNG");
            }
            return baos.toByteArray();
        }
    }


    private final FileLoader loader;

    private final String displayName;

    private final boolean isCodec;

    private Bundle(FileLoader loader) throws IOException {
        this.loader = loader;
        Manifest manifest = loader.getManifest();
        this.displayName = manifest.getSpec().getDisplayName();
        this.isCodec = manifest.isCodec();
    }

    // Return a bundle which loads data from the filesystem on request.
    static Bundle getBundle(File bundleFile, List<File> memberDirs)
            throws IOException {
        return new Bundle(new PendingFileLoader(bundleFile, memberDirs));
    }

    // Return a bundle which caches bundle contents.
    static Bundle getBundle(InputStream in, List<File> memberDirs)
            throws IOException {
        return new Bundle(new PreparedFileLoader(in, memberDirs));
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isCodec() {
        return isCodec;
    }

    public List<OptionGroup> getOptions() throws IOException {
        OptionList l = loader.getManifest().getSpec().getOptionList();
        List<OptionGroup> groups = Collections.emptyList();
        if (l != null) {
            groups = l.getOptionGroups();
            List<Option> options = l.getOptions();
            if (options.size() > 0) {
                // Create a fake option group for the loose options.
                OptionGroup og = new OptionGroup();
                og.getOptions().addAll(options);
                groups.add(og);
            }
        }

        // check that no two options have the same name, and that there is
        // at most one ExampleOption
        HashSet<String> names = new HashSet<String>();
        boolean haveExample = false;
        for (OptionGroup group : groups) {
            for (Option opt : group.getOptions()) {
                String name = opt.getName();
                if (names.contains(name)) {
                    throw new BundleFormatException(
                            "Duplicate option name \"" + name + "\"");
                }
                names.add(name);
                if (opt instanceof ExampleOption) {
                    if (haveExample) {
                        throw new BundleFormatException(
                                "At most one exampleOption can be specified");
                    }
                    haveExample = true;
                }
            }
        }

        return groups;
    }

    public List<Filter> getFilters(Map<String, String> optionMap) throws
            IOException {
        return getFilters(optionMap, null);
    }

    public List<Filter> getFilters(Map<String, String> optionMap,
            List<BufferedImage> examples) throws IOException {
        PreparedFileLoader loader = this.loader.getPreparedLoader();

        ArrayList<PendingFilter> pending = new ArrayList<PendingFilter>();
        HashMap<String, String> labelMap = new HashMap<String, String>();
        List<FilterSpec> specs = loader.getManifest().getSpec()
                .getFilterList().getFilters();
        for (FilterSpec f : specs) {
            PendingFilter pf = new PendingFilter(loader, optionMap, examples,
                    f);
            pending.add(pf);
            String label = pf.getLabel();
            if (label != null) {
                if (labelMap.containsKey(label)) {
                    throw new BundleFormatException(
                            "Duplicate filter label \"" + label + "\"");
                }
                labelMap.put(label, pf.getName());
            }
        }

        ArrayList<Filter> filters = new ArrayList<Filter>();
        for (PendingFilter pf : pending) {
            filters.add(pf.getFilter(labelMap));
        }
        return filters;
    }
}
