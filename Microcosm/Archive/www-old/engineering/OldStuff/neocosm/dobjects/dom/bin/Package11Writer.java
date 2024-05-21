/*
 * @(#)Package11Writer.java 1.4 98/03/18
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
public class Package11Writer extends AbstractPackageWriter {

    /**
     * Constructor.
     */
    public Package11Writer(String filename, PackageDoc packagedoc)
                           throws IOException {
        super(filename, packagedoc);
    }

    /**
     * Generate a package page.
     *
     * @param package the package to generate.
     */
    public static void generate(PackageDoc pkg) throws DocletAbortException {
        Package11Writer packgen;
        String filename = "package-" + pkg.toString() + ".html";
        try {
            packgen = new Package11Writer(filename, pkg);
            packgen.generatePackageFile();
            packgen.close();
        } catch (IOException exc) {
            error("doclet.exception_encountered", exc.toString(), filename);
            throw new DocletAbortException();
        }
    }

    protected void generateClassListing() {
        generateClassKindListing(packagedoc.interfaces(),
                                 "interface-index", 257, 38,
                                 getText("doclet.Interface_Summary"));
        generateClassKindListing(packagedoc.ordinaryClasses(),
                                 "class-index", 216, 37,
                                 getText("doclet.Class_Summary"));
        generateClassKindListing(packagedoc.exceptions(),
                                 "exception-index", 284, 38,
                                 getText("doclet.Exception_Summary"));
        generateClassKindListing(packagedoc.errors(),
                                 "error-index", 174, 38,
                                 getText("doclet.Error_Summary"));
    }

    /**
     * This method is used by genIndPackageFiles method to generate
     * the Class/Interface... Listing.
     */
    protected void generateClassKindListing(ClassDoc[] arr, String gif,
                                            int width, int height,
                                            String label) {
        if(arr.length > 0) {
            Arrays.sort(arr, new ClassComparator());
            h2();
            printImage(gif, label, width, height);
            h2End();
            menu();
            for (int i = 0; i < arr.length; i++) {
                li();
                printClassLink(arr[i]);
                println();
            }
            menuEnd();
        }
    }

    protected void printPackageHeader(String heading) {
        navLinks(true);
        hr();
        h3(getText("doclet.Package") + " " + heading);
    }

    protected void printPackageFooter() {
        hr();
        navLinks(false);
        printBottom();
    }

    protected void printPackageDescription()  throws IOException {
    }

    protected void generateClassKindListing(ClassDoc cd, String nm) {
    }

    protected void printPrevLink() {
    }

    protected void printNextLink() {
    }
}



