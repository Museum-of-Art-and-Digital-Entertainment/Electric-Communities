/*
 * @(#)PackageWriter.java   1.16 98/03/18
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
 * Class to generate file for each package contents.
 *
 * @see sun.tools.javadoc.PackageDoc
 * @see sun.tools.javadoc.doclets.HtmlDocWriter
 * @author Atul M Dambalkar
 */
public class PackageWriter extends AbstractPackageWriter {

    String prev;

    String next;

    /**
     * Constructor.
     */
    public PackageWriter(String filename, PackageDoc packagedoc,
                         String prev, String next) throws IOException {
        super(filename, packagedoc);
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
        PackageWriter packgen;
        String filename = "package-" + pkg.toString() + ".html";
        try {
            packgen = new PackageWriter(filename, pkg, prev, next);
            packgen.generatePackageFile();
            packgen.close();
        } catch (IOException exc) {
            error("doclet.exception_encountered", exc.toString(), filename);
            throw new DocletAbortException();
        }
    }

    protected void generateClassListing() {
        generateClassKindListing(packagedoc.interfaces(),
                                 getText("doclet.Interface_Summary"));
        generateClassKindListing(packagedoc.ordinaryClasses(),
                                 getText("doclet.Class_Summary"));
        generateClassKindListing(packagedoc.exceptions(),
                                 getText("doclet.Exception_Summary"));
        generateClassKindListing(packagedoc.errors(),
                                 getText("doclet.Error_Summary"));
    }

    /**
     * This method is used by genIndPackageFiles method to generate
     * the Class/Interface... Listing.
     */
    protected void generateClassKindListing(ClassDoc[] arr, String label) {
        if(arr.length > 0) {
            Arrays.sort(arr, new ClassComparator());
            tableIndexSummary();
            printFirstRow(label);
            for (int i = 0; i < arr.length; i++) {
                trBgcolor("#FFFFCC");
                summaryRow(15);
                bold();
                printClassLink(arr[i]);
                boldEnd();
                summaryRowEnd();
                summaryRow(0);
                String comment = firstSentence(arr[i].commentText());
                if (comment == "") {
                    comment = "&nbsp;";
                }
                println(comment);
                if (arr[i].tags("deprecated").length > 0) {
                    print(arr[i].tags("deprecated")[0].text());
                }
                summaryRowEnd();
                trEnd();
            }
            tableEnd();
            println("&nbsp;");
            p();
        }
    }

    protected void printFirstRow(String label) {
        tableHeaderStart();
        bold(label);
        tableHeaderEnd();
    }

    protected void printPackageComment() {
        String text = packagedoc.commentText();
        if (!text.equals("")) {
            h2(getText("doclet.Package_Description"));
            p();
            print(text);
            p();
        }
    }

    protected void printPackageDescription() throws IOException {
        printPackageComment();
        generateTagInfo(packagedoc, null);  //new
    }

    protected void printPackageHeader(String heading) {
        navLinks(true);
        hr();
        h2(getText("doclet.Package") + " " + heading);
    }

    protected void printPackageFooter() {
        hr();
        navLinks(false);
        printBottom();
    }

    /**
     * Print previous item link
     */
    protected void navLinkPrevious() {
        if (prev == null) {
            navLinkPrevious(null);
        } else {
            navLinkPrevious("package-" + prev + ".html");
        }
    }

    /**
     * Print next item link
     */
    protected void navLinkNext() {
        if (next == null) {
            navLinkNext(null);
        } else {
            navLinkNext("package-" + next + ".html");
        }
    }
}



