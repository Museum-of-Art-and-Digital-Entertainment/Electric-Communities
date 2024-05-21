/*
 * @(#)PackageIndex11Writer.java    1.3 98/03/18
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
 * Write out the package index.
 *
 * @see sun.tools.javadoc.PackageDoc
 * @see sun.tools.javadoc.doclets.HtmlDocWriter
 * @author Atul M Dambalkar
 */
public class PackageIndex11Writer extends AbstractPackageIndexWriter {

    /**
     * Constructor.
     */
    public PackageIndex11Writer(String filename,
                                Root root) throws IOException {
        super(filename, root);
    }

    /**
     * Generate the package index.
     *
     * @param root the root of the doc tree.
     */
    public static void generate(Root root) throws DocletAbortException {
        PackageIndex11Writer packgen;
        String filename = "packages.html";
        try {
            packgen = new PackageIndex11Writer(filename, root);
            packgen.generatePackageIndexFile();
            packgen.close();
        } catch (IOException exc) {
            error("doclet.exception_encountered", exc.toString(), filename);
            throw new DocletAbortException();
        }
    }

    protected void printIndexRow(PackageDoc packagedoc) {
        li();
        print(' ');
        printText("doclet.package");
        print(' ');
        bold();
        printPackageLink(packagedoc);
        boldEnd();
    }

    protected void printTableMenuFirstRow(boolean mustBeJava) {
        menu();
    }

    protected void printOneOneHeader() {
        h1();
        printImage("package-index", getText("doclet.Package_Summary"),
                   238, 37);
        h1End();
    }
}



