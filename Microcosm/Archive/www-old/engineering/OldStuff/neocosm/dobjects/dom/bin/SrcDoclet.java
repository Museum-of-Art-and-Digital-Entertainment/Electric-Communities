/*
 * @(#)Standard.java    1.17 98/03/18
 *
 * Copyright 1997, 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

import sun.tools.javadoc.*;
import java.util.*;
import java.io.*;

/**
 * The class with "start" method, calls individual Writers.
 *
 * @author Atul M Dambalkar
 * @author Robert Field
 */
public class SrcDoclet extends Doclet {
    public static Configuration configuration;

    /**
     * The "start" method as required by Javadoc.
     *
     * @param Root
     * @see sun.tools.javadoc.Root
     * @return boolean
     */
    public static boolean start(Root root) throws IOException {
        System.out.println("Scott's Super Duper Javadoclet is Running!");
        try {
            configuration().setOptions(root);

            if (configuration().oneOne) {
                (new SrcDoclet()).startGenerationOneOne(root);
            } else {
                (new SrcDoclet()).startGeneration(root);
            }
        } catch (DocletAbortException exc) {
            return false; // message has already been displayed
        }
        return true;
    }

    /**
     * Return the configuration instance. Create if it doesn't exist.
     * Override this method to use a different
     * configuration.
     */
    public static Configuration configuration() {
        if (configuration == null) {
            configuration = new Configuration();
        }
        return configuration;
    }

    /**
     * Start the generation of files. Call generate methods in the individual
     * writers, which will in turn genrate the documentation files. Call the
     * TreeWriter generation first to ensure the Class Hierarchy is built
     * first and then can be used in the later generation.
     *
     * For new format.
     *
     * @see sun.tools.javadoc.Root
     */
    protected void startGeneration(Root root) throws DocletAbortException {
        ClassTree classtree = new ClassTree(root);
        IndexBuilder indexbuilder = new IndexBuilder(root);
        PackageDoc[] packages = root.specifiedPackages();
        Arrays.sort(packages);

        if (configuration().createTree) {
            TreeWriter.generate(classtree, root);
        }

        if (configuration().createIndex) {
            if (configuration.breakIndex) {
                SplitIndexWriter.generate(indexbuilder);
            } else {
                SingleIndexWriter.generate(indexbuilder);
            }
        }

        if (!configuration.noDeprecatedList) {
            DeprecatedListWriter.generate(root);
        }

        if (packages.length > 0) {
            FrameOutputWriter.generate(root);

            PackageIndexWriter.generate(root);
            PackageIndexFrameWriter.generate(root);
        }

        for(int i = 0; i < packages.length; i++) {
            String prev = (i == 0)?
                              null:
                              packages[i-1].name();
            PackageDoc packagedoc = packages[i];
            String next = (i+1 == packages.length)?
                              null:
                              packages[i+1].name();
            PackageWriter.generate(packages[i], prev, next);
            PackageTreeWriter.generate(packages[i], prev, next);
            PackageFrameWriter.generate(packages[i]);
        }
        generateClassFiles(root, classtree);

    }

    /**
     * Start the generation of files. Call generate methods in the individual
     * writers, which will in turn genrate the documentation files. Call the
     * TreeWriter generation first to ensure the Class Hierarchy is built
     * first and then can be used in the later generation.
     *
     * For old 1.1 format.
     *
     * @see sun.tools.javadoc.Root
     */
    protected void startGenerationOneOne(Root root) throws DocletAbortException {
        ClassTree classtree = new ClassTree(root);
        IndexBuilder indexbuilder = new IndexBuilder(root);
        PackageDoc[] packages = root.specifiedPackages();
        Arrays.sort(packages);

        if (configuration().createTree) {
            TreeWriter.generate(classtree, root);
        }

        if (configuration().createIndex) {
            SplitIndexWriter.generate(indexbuilder);
        }

        if (packages.length > 0) {
            PackageIndex11Writer.generate(root);
        }

        for(int i = 0; i < packages.length; i++) {
            Package11Writer.generate(packages[i]);
        }

        generateClassFiles(root, classtree);

    }

    protected void generateClassFiles(Root root, ClassTree classtree)
                                      throws DocletAbortException {
        ClassDoc[] classes = root.specifiedClasses();
        List incl = new ArrayList();
        for (int i = 0; i < classes.length; i++) {
            ClassDoc cd = classes[i];
            if (cd.isIncluded()) {
                incl.add(cd);
            }
        }
        ClassDoc[] inclClasses = new ClassDoc[incl.size()];
        for (int i = 0; i < inclClasses.length; i++) {
            inclClasses[i] = (ClassDoc)incl.get(i);
        }
        generateClassCycle(inclClasses, classtree);
        PackageDoc[] packages = root.specifiedPackages();
        for (int i = 0; i < packages.length; i++) {
            PackageDoc pkg = packages[i];
            generateClassCycle(pkg.interfaces(), classtree);
            generateClassCycle(pkg.ordinaryClasses(), classtree);
            generateClassCycle(pkg.exceptions(), classtree);
            generateClassCycle(pkg.errors(), classtree);
        }
    }

    protected String classFileName(ClassDoc cd) {
        return cd.qualifiedName() + ".html";
    }

    /**
     * Instantiate ClassWriter for each Class within the ClassDoc[]
     * passed to it and generate Documentation for that.
     */
    protected void generateClassCycle(ClassDoc[] arr, ClassTree classtree)
                                      throws DocletAbortException {
        Arrays.sort(arr, new ClassComparator());
        for(int i = 0; i < arr.length; i++) {
            String prev = (i == 0)?
                          null:
                          classFileName(arr[i-1]);
            ClassDoc curr = arr[i];
            String next = (i+1 == arr.length)?
                          null:
                          classFileName(arr[i+1]);

            String sourcepath = null;  //new
            if (configuration.srcRelativePath != null){        //new
          sourcepath = configuration.srcRelativePath; //new
        } else { //new
              System.out.println("No sourcepath information from which to construct links to source files."); //new
        } //new


            if (configuration.oneOne) {
                Class11Writer.generate(curr, prev, next, classtree, null);
            } else {
                ClassWriter.generate(curr, prev, next, classtree,
                     sourcepath);  //new
            }
        }
    }

    /**
     * Check for doclet added options here.
     *
     * @return number of arguments to option. Zero return means
     * option not known.  Negative value means error occurred.
     */
    public static int optionLength(String option) {
        return configuration().optionLength(option);
    }

}


