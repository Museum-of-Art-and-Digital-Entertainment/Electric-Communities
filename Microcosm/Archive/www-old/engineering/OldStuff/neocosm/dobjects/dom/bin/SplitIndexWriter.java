/*
 * @(#)SplitIndexWriter.java    1.3 98/03/18
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
 * Generate Files for all the Member Names with Indexing in
 * Alphabetical Order.
 *
 * @author Atul M Dambalkar
 */
public class SplitIndexWriter extends AbstractIndexWriter {

    public SplitIndexWriter(String filename,
                             IndexBuilder indexbuilder) throws IOException {
        super(filename, indexbuilder);
    }

    /**
     * Generate a single index file, listing all the members.
     *
     */
    public static void generate(IndexBuilder indexbuilder)
                                throws DocletAbortException {
        SplitIndexWriter indexgen;
        String filename = "";
        try {
            for (int i = 0; i < indexbuilder.elements.length; i++) {
                filename = i + 1 + "-index.html";
                indexgen = new SplitIndexWriter(filename, indexbuilder);
                indexgen.generateIndexFile((Character)indexbuilder.elements[i]);
                indexgen.close();
            }
        } catch (IOException exc) {
            error("doclet.exception_encountered", exc.toString(), filename);
            throw new DocletAbortException();
        }
    }

    /**
     * Generate the contents of each index file, with Header, Footer,
     * Member Field, Method and Constructor Description.
     *
     * @param str String referring to the alphabet for the index.
     */
    protected void generateIndexFile(Character unicode) throws IOException {
        printHeader(unicode + "-" + getText("doclet.Index"));

        navLinks(true);
        printLinksForIndexes();

        hr();

        generateContents(unicode, indexbuilder.getMemberList(unicode));

        printLinksForIndexes();
        navLinks(false);

        printBottom();
        printFooter();
    }

    /**
     * Print Links for all the Index Files per alphabet.
     *
     */
    protected void printLinksForIndexes() {
        for (int i = 0; i < indexbuilder.elements.length; i++) {
            printHyperLink(i + 1 + "-index.html",
                           indexbuilder.elements[i].toString());
            print(' ');
        }
    }
}
