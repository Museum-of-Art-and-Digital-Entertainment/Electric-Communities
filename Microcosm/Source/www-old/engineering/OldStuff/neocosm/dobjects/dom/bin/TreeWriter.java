/*
 * @(#)TreeWriter.java  1.15 98/03/18
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
import java.io.*;
import java.lang.*;
import java.util.*;

/**
 * Generate Class Hierarchy page for all the Classes.  Use ClassTree for
 * getting the Tree.
 *
 * @see java.util.HashMap
 * @see java.util.List
 * @see sun.tools.javadoc.Type
 * @see sun.tools.javadoc.ClassDoc
 * @author Atul M Dambalkar
 */
public class TreeWriter extends AbstractTreeWriter {

    private PackageDoc[] packages;

    /**
     * Constructor.
     *
     * @param file String filename
     */
    public TreeWriter(String filename, ClassTree classtree, Root root)
                                               throws IOException {
        super(filename, classtree);
        packages = root.specifiedPackages();
        Arrays.sort(packages);
    }

    /**
     * Static method to be called by the Standard doclet.
     */
    public static void generate(ClassTree classtree, Root root)
                                throws DocletAbortException {
        TreeWriter treegen;
        String filename = "tree.html";
        try {
            treegen = new TreeWriter(filename, classtree, root);
            treegen.generateTreeFile();
            treegen.close();
        } catch (IOException exc) {
            error("doclet.exception_encountered", exc.toString(), filename);
            throw new DocletAbortException();
        }
    }

    /**
     * Generate the Tree File Contents.
     */
    public void generateTreeFile() throws IOException {
        printHeader(getText("doclet.Class_Hierarchy"));
        printTreeHeader();

        printPageHeading();

        printPackageTreeLinks();

        generateTree(classtree.baseclasses(), "doclet.Class_Hierarchy");
        generateTree(classtree.baseinterfaces(), "doclet.Interface_Hierarchy");

        printTreeFooter();
    }

    protected void printPackageTreeLinks() {
        dl();
        dt();
        boldText("doclet.Package_Hierarchies");
        dd();
        for (int i = 0; i < packages.length; i++) {
            String pname = packages[i].name();
            printHyperLink("package-tree-" + pname + ".html", null, pname);
            if (i < packages.length - 1) {
                print(", ");
            }
        }
        dlEnd();
        hr();
    }

    protected void printTreeHeader() {
        navLinks(true);
        hr();
    }

    protected void printTreeFooter() {
        hr();
        navLinks(false);
        printBottom();
        printFooter();
    }

    protected void printPageHeading() {
        center();
        h1();
        print(getText("doclet.Hierarchy_For_All_Packages"));
        h1End();
        centerEnd();
    }
}
