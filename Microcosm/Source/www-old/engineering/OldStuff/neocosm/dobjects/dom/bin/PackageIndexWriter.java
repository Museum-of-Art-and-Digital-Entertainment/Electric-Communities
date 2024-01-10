/*
 * @(#)PackageIndexWriter.java  1.14 98/03/18
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
 * Write out the package index.
 *
 * @see sun.tools.javadoc.PackageDoc
 * @see sun.tools.javadoc.doclets.HtmlDocWriter
 * @author Atul M Dambalkar
 */
public class PackageIndexWriter extends AbstractPackageIndexWriter {

    private Root root;

    /**
     * Constructor.
     */
    public PackageIndexWriter(String filename, Root root) throws IOException {
        super(filename, root);
        this.root = root;
    }

    /**
     * Generate the package index.
     *
     * @param root the root of the doc tree.
     */
    public static void generate(Root root) throws DocletAbortException {
        PackageIndexWriter packgen;
        String filename = "packages.html";
        try {
            packgen = new PackageIndexWriter(filename, root);
            packgen.generatePackageIndexFile();
            packgen.close();
        } catch (IOException exc) {
            error("doclet.exception_encountered", exc.toString(), filename);
            throw new DocletAbortException();
        }
    }

    protected void printOverviewComment() {
        String text = root.commentText();
        if (!text.equals("")) {
//            h2(getText(???));
            p();
            print(text);
            p();
        }
    }

    protected void printOverview() throws IOException {
        printOverviewComment();
        generateTagInfo(root, null);  //new
    }

    protected void printIndexRow(PackageDoc packagedoc) {
        trBgcolor("#FFFFCC");
        summaryRow(20);
        bold();
        printPackageLink(packagedoc);
        boldEnd();
        summaryRowEnd();
        summaryRow(0);
        String comment = firstSentence(packagedoc.commentText());
        if (comment == "") {
            comment = "&nbsp;";
        }
        println(comment);
        summaryRowEnd();
        trEnd();
    }

    protected void printFirstRow(String label) {
        tableHeaderStart();
        bold(label);
        tableHeaderEnd();
    }

    protected void printTableMenuFirstRow(boolean mustBeJava) {
        tableIndexSummary();
        if (mustBeJava) {
            printFirstRow(getText("doclet.Java_Packages_Summary"));
        } else {
            printFirstRow(getText("doclet.Other_Packages_Summary"));
        }
    }

    protected void printIndexSpecificEnd() {
        tableEnd();
    }
}



