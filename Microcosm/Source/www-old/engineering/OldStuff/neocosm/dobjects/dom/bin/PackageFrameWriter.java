/*
 * @(#)PackageFrameWriter.java  1.5 98/03/18
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
public class PackageFrameWriter extends AbstractPackageWriter {

    /**
     * Constructor.
     */
    public PackageFrameWriter(String filename, PackageDoc packagedoc)
                         throws IOException {
        super(filename, packagedoc);
    }

    /**
     * Generate a package page.
     *
     * @param package the package to generate.
     */
    public static void generate(PackageDoc pkg) throws DocletAbortException {
        PackageFrameWriter packgen;
        String filename = "package-frame-" + pkg.toString() + ".html";
        try {
            packgen = new PackageFrameWriter(filename, pkg);
            packgen.generatePackageFile();
            packgen.close();
        } catch (IOException exc) {
            error("doclet.exception_encountered", exc.toString(), filename);
            throw new DocletAbortException();
        }
    }

    protected void generateClassListing() {
        generateClassKindListing(packagedoc.interfaces(),
                                 getText("doclet.Interfaces"));
        generateClassKindListing(packagedoc.ordinaryClasses(),
                                 getText("doclet.Classes"));
        generateClassKindListing(packagedoc.exceptions(),
                                 getText("doclet.Exceptions"));
        generateClassKindListing(packagedoc.errors(),
                                 getText("doclet.Errors"));
    }

    /**
     * This method is used by genIndPackageFiles method to generate
     * the Class/Interface... Listing.
     */
    protected void generateClassKindListing(ClassDoc[] arr, String label) {
        if(arr.length > 0) {
            Arrays.sort(arr, new ClassComparator());
            printPackageTableHeader();
            font("+1");
            print(label);
            fontEnd();
            println("&nbsp");
            for (int i = 0; i < arr.length; i++) {
                br();
                printTargetClassLink(arr[i], "classFrame");
            }
            printPackageTableFooter();
            p();
        }
    }

    protected void printPackageHeader(String heading) {
        font("+1");
        print(heading);
        fontEnd();
    }

    protected void printPackageTableHeader() {
        tablePackageFrame();
        tr();
        td();
    }

    protected void printPackageTableFooter() {
        tableEnd();
        trEnd();
        tdEnd();
    }

    protected void printPackageFooter() {

    }

    protected void printPackageDescription() throws IOException {
    }
}



