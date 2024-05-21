/*
 * @(#)PackageIndexFrameWriter.java 1.3 98/03/18
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
public class PackageIndexFrameWriter extends AbstractPackageIndexWriter {

    /**
     * Constructor.
     */
    public PackageIndexFrameWriter(String filename,
                                   Root root) throws IOException {
        super(filename, root);
    }

    /**
     * Generate the package index.
     *
     * @param root the root of the doc tree.
     */
    public static void generate(Root root) throws DocletAbortException {
        PackageIndexFrameWriter packgen;
        String filename = "packages-frame.html";
        try {
            packgen = new PackageIndexFrameWriter(filename, root);
            packgen.generatePackageIndexFile();
            packgen.close();
        } catch (IOException exc) {
            error("doclet.exception_encountered", exc.toString(), filename);
            throw new DocletAbortException();
        }
    }

    protected void printIndexRow(PackageDoc packagedoc) {
        String name = packagedoc.name();
        printTargetHyperLink("package-frame-" + name + ".html",
                             "packageFrame", name);
        br();
    }

    protected void printIndexHeader() {
    }

    protected void printIndexFooter() {
        tableEnd();
        trEnd();
        tdEnd();
    }

    protected void printTableMenuFirstRow(boolean mustBeJava) {
    }

    protected void printHeaderOrTitle() {
        if (configuration.header != null) {
            tablePackageFrame();
            tr();
            td();
            font("+1");
            //bold(configuration.header);
            print(configuration.header);
            fontEnd();
            printIndexFooter();
        }
        tablePackageFrame();
        tr();
        td();
    }
}



