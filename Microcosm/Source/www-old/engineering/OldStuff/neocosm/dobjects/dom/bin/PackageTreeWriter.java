/*
 * @(#)PackageTreeWriter.java   1.4 98/03/18
 *
 * Copyright 1998 by Sun Microsystems, Inc.,
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
 * Class to generate file for each package contents.
 *
 * @see sun.tools.javadoc.PackageDoc
 * @see sun.tools.javadoc.doclets.HtmlDocWriter
 * @author Atul M Dambalkar
 */
public class PackageTreeWriter extends AbstractTreeWriter {

    private PackageDoc packagedoc;

    private String prev;

    private String next;

    /**
     * Constructor.
     */
    public PackageTreeWriter(String filename, PackageDoc packagedoc,
                             String prev, String next) throws IOException {
        super(filename, new ClassTree(packagedoc.allClasses()));
        this.packagedoc = packagedoc;
        this.prev = prev;
        this.next = next;
    }

    /**
     * Generate a package page.
     *
     * @param package the package to generate.
     */
    public static void generate(PackageDoc pkg, String prev,
                                String next) throws DocletAbortException {
        PackageTreeWriter packgen;
        String filename = "package-tree-" + pkg.toString() + ".html";
        try {
            packgen = new PackageTreeWriter(filename, pkg, prev, next);
            packgen.generatePackageTreeFile();
            packgen.close();
        } catch (IOException exc) {
            error("doclet.exception_encountered", exc.toString(), filename);
            throw new DocletAbortException();
        }
    }

    protected void generatePackageTreeFile() throws IOException {
        printHeader(packagedoc.name() + " "
                    + getText("doclet.Class_Hierarchy"));
        printPackageTreeHeader();

        printLinkToMainTree();

        generateTree(classtree.baseclasses(), "doclet.Class_Hierarchy");
        generateTree(classtree.baseinterfaces(), "doclet.Interface_Hierarchy");

        printPackageTreeFooter();
        printBottom();
        printFooter();
    }

    protected void printPackageTreeHeader() {
        navLinks(true);
        hr();
        center();
        h1(getText("doclet.Hierarchy_For_Package") + ' ' + packagedoc.name());
        centerEnd();
    }

    protected void printLinkToMainTree() {
        dl();
        dt();
        boldText("doclet.Package_Hierarchies");
        dd();
        navLinkMainTree(getText("doclet.All_Packages"));
        dlEnd();
        hr();
    }

    protected void printPackageTreeFooter() {
        hr();
        navLinks(false);
    }

    /**
     * Print this package link
     */
    protected void navLinkPackage() {
        navLinkPackage(packagedoc);
    }

    protected void navLinkPrevious() {
        if (prev == null) {
            navLinkPrevious(null);
        } else {
            navLinkPrevious("package-tree-" + prev + ".html");
        }
    }

    protected void navLinkNext() {
        if (next == null) {
            navLinkNext(null);
        } else {
            navLinkNext("package-tree-" + next + ".html");
        }
    }
}



