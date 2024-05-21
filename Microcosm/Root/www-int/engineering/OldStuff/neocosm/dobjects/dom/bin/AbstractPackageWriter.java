/*
 * @(#)AbstractPackageWriter.java   1.5 98/03/18
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
 * Abstract class to generate file for each package contents. Sub-classed to
 * generate specific formats for 1.1 compliant, Frame and Non-Frame Output.
 *
 * @author Atul M Dambalkar
 */
public abstract class AbstractPackageWriter extends HtmlDocWriter {

    /**
     * The package information from Doclet API.
     */
    PackageDoc packagedoc;

    /**
     * Constructor.
     */
    public AbstractPackageWriter(String filename,
                                 PackageDoc packagedoc) throws IOException {
        super(filename);
        this.packagedoc = packagedoc;
    }

    protected abstract void generateClassListing();

    protected abstract void printPackageDescription() throws IOException;

    protected abstract void printPackageHeader(String head);

    protected abstract void printPackageFooter();

    /**
     * Generate Individual Package File with Class/Interface/Exceptions and
     * Error Listing with the appropriate links. File names will be e.g.
     * "package-java.io.applet.html". Calls the methods from the sub-classes.
     */
    protected void generatePackageFile() throws IOException {
        String heading1 = getText("doclet.Package") + " ";
        String heading2 = packagedoc.toString();
        printHeader(heading1 + heading2);
        printPackageHeader(heading2);

        generateClassListing();
        printPackageDescription();

        printPackageFooter();
        printFooter();
    }

    /**
     * Print class/interface hierarchy link
     */
    protected void navLinkTree() {
        navLinkTree(packagedoc);
    }

    /**
     * Print this package link
     */
    protected void navLinkPackage() {
        boldText("doclet.Package");
    }
}



