/*
 * @(#)DeprecatedListWriter.java    1.10 98/03/18
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
 * Generate File to list all the deprecated classes and class members with the
 * appropriate links.
 *
 * @see java.util.List
 * @author Atul M Dambalkar
 */
public class DeprecatedListWriter extends SubWriterHolderWriter {

    /**
     * Constructor.
     *
     * @param filename the file to be generated.
     */
    public DeprecatedListWriter(String filename) throws IOException {
        super(filename);
    }

    /**
     * Get list of all the deprecated classes and members in all the Packages
     * specified on the Command Line.
     * Then instantiate DeprecatedListWriter and generate File.
     *
     * @param root Root of the Document
     */
    public static void generate(Root root) throws DocletAbortException {
        String filename = "deprecatedlist.html";
        try {
            DeprecatedListWriter depr = new DeprecatedListWriter(filename);
            depr.generateDeprecatedListFile(new DeprecatedAPIListBuilder(root));
            depr.close();
        } catch (IOException exc) {
            error("doclet.exception_encountered", exc.toString(), filename);
            throw new DocletAbortException();
        }
    }

    /**
     * Print the deprecated API list.
     * @param deprapi list of deprecated API built already.
     */
    protected void generateDeprecatedListFile(DeprecatedAPIListBuilder deprapi)
                                                        throws IOException {
        ClassSubWriter classW = new ClassSubWriter(this);
        FieldSubWriter fieldW = new FieldSubWriter(this);
        MethodSubWriter methodW = new MethodSubWriter(this);
        ConstructorSubWriter consW = new ConstructorSubWriter(this);
        printDeprecatedHeader();

        classW.printDeprecatedAPI(deprapi.getDeprecatedClasses(),
                                  "doclet.Deprecated_Classes");
        classW.printDeprecatedAPI(deprapi.getDeprecatedInterfaces(),
                                  "doclet.Deprecated_Interfaces");
        classW.printDeprecatedAPI(deprapi.getDeprecatedExceptions(),
                                  "doclet.Deprecated_Exceptions");
        classW.printDeprecatedAPI(deprapi.getDeprecatedErrors(),
                                  "doclet.Deprecated_Errors");
        fieldW.printDeprecatedAPI(deprapi.getDeprecatedFields(),
                                  "doclet.Deprecated_Fields");
        methodW.printDeprecatedAPI(deprapi.getDeprecatedMethods(),
                                   "doclet.Deprecated_Methods");
        consW.printDeprecatedAPI(deprapi.getDeprecatedConstructors(),
                                 "doclet.Deprecated_Constructors");

        printDeprecatedFooter();
    }

    /**
     * Print the header for the deprecated API Listing.
     */
    protected void printDeprecatedHeader() {
        printHeader(getText("doclet.Deprecated_List"));
        navLinks(true);
        hr();
        center();
        h1();
        boldText("doclet.Deprecated_API");
        h1End();
        centerEnd();
    }

    /**
     * Print the footer for the deprecated API Listing.
     */
    protected void printDeprecatedFooter() {
        hr();
        navLinks(false);
        printBottom();
        printFooter();
    }

    /**
     * Print deprecated API link
     */
    protected void navLinkDeprecated() {
        boldText("doclet.Deprecated");
    }
}
