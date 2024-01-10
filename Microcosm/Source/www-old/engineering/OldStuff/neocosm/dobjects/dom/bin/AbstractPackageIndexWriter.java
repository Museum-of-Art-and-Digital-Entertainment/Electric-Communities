/*
 * @(#)AbstractPackageIndexWriter.java  1.5 98/03/18
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
 * Abstract class to generate the package index. The package index needs to be
 * generated in 1.1 compliant, Frame and Non-Frame format.
 *
 * @author Atul M Dambalkar
 */
public abstract class AbstractPackageIndexWriter extends HtmlDocWriter {

    /**
     * Array of Packages.
     */
    protected PackageDoc[] packages;

    /**
     * Constructor.
     */
    public AbstractPackageIndexWriter(String filename,
                                      Root root) throws IOException {
        super(filename);
        packages = root.specifiedPackages();
        Arrays.sort(packages);
    }

    protected abstract void printTableMenuFirstRow(boolean mustBeJava);

    protected abstract void printIndexRow(PackageDoc packagedoc);

    /**
     * Generate code for package index.
     * If parameter passed is true print only 'java.*' packages,
     * else if parameter passed is false print everything but 'java.*'
     * packages.
     *
     * @param mustBeJava boolean.
     */
    protected void printIndex(boolean mustBeJava) {
        boolean first = true;
        for(int i = 0; i < packages.length; i++) {
            PackageDoc packagedoc = packages[i];
            boolean inJava = packagedoc.name().startsWith("java.");
            if ((mustBeJava && inJava) || (!mustBeJava && !inJava)) {
                if (first) {
                    if (mustBeJava) {
                        print(configuration.javaPackagesHeader);
                    } else {
                        print(configuration.otherPackagesHeader);
                    }
                    printTableMenuFirstRow(first);
                    first = false;
                }
                printIndexRow(packagedoc);
            }
        }
        if (!first) {
            printIndexEnd();
        }
    }

    protected void printOneOneHeader() {
    }

    protected void printOverview() throws IOException {
    }

    /**
     * Generate the contants in the package index file. Call appropriate
     * methods from the sub-class in order to generate 1.1 or Frame or Non
     * Frame format.
     */
    protected void generatePackageIndexFile() throws IOException {
        printHeader(getText("doclet.Package_Summary"));
        printIndexHeader();
        printHeaderOrTitle();
        printOneOneHeader();
        if(packages.length > 0) {
            printIndex(true);
            printIndex(false);
        }
        printOverview();
        printIndexFooter();
        printFooter();
    }

    /**
     * Print the header for Non-Frame frmat output generation. Over-ridden in
     * few sub-classes.
     */
    protected void printIndexHeader() {
        navLinks(true);
    hr();
    }

    /**
     * Print the footer for Non-Frame frmat output generation. Over-ridden in
     * few sub-classes.
     */
    protected void printIndexFooter() {
        hr();
        navLinks(false);
        printBottom();
    }

    /**
     * Print the header or title.  Over-ridden in few sub-classes.
     */
    protected void printHeaderOrTitle() {
        if (configuration.title.length() > 0) {
            center();
            h1();
            print(configuration.title);
            h1End();
            centerEnd();
        }
    }

    /**
     * Print part of the index footer.
     */
    protected void printIndexEnd() {
        printIndexSpecificEnd();
        p();
        space();
    }

    /**
     * Print the specific index footer. The method is over-ridden in few
     * sub-classes.
     */
    protected void printIndexSpecificEnd() {
        menuEnd();
    }

    /**
     * Print the first row of the index table.
     */
    protected void printFirstRow(String label) {
        tableHeaderStart();
        bold(label);
        tableHeaderEnd();
    }

    /**
     * Print packages contents link
     */
    protected void navLinkContents() {
        boldText("doclet.Contents");
    }
}



