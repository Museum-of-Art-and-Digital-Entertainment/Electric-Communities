/*
 * @(#)FrameOutputWriter.java   1.6 98/03/18
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
 * Generate the Html output required for the Frames.
 *
 * @see sun.tools.javadoc.doclets.HtmlDocWriter
 * @author Atul M Dambalkar
 */
public class FrameOutputWriter extends HtmlDocWriter {

    String firstpackage;

    /**
     * Constructor.
     */
    public FrameOutputWriter(String filename, Root root)
                   throws IOException, UnsupportedOperationException {
        super(filename);
        PackageDoc[] packages = root.specifiedPackages();
        if (packages.length > 0) {
            Arrays.sort(packages);
            firstpackage = packages[0].name();
        } else {
            throw new UnsupportedOperationException("Cannot handle no packages");
        }
    }

    /**
     * Generate the frame as well as the option file.
     *
     */
    public static void generate(Root root) throws DocletAbortException {
        FrameOutputWriter framegen;
        String filename = "";
        try {
            filename = "frame.html";
            framegen = new FrameOutputWriter(filename, root);
            framegen.generateFrameFile();
            framegen.close();
        } catch (UnsupportedOperationException exc) {
            // quietly ignore - don't generate these files for now.
        } catch (IOException exc) {
            error("doclet.exception_encountered", exc.toString(), filename);
            throw new DocletAbortException();
        }
    }

    /**
     * Print the frame file contents.
     */
    protected void generateFrameFile() {
        printPartialHeader(getText("doclet.Frame_Output"));
        printFrameDetails();
        printFrameWarning();
        printFooter();
    }

    /**
     * Generate the code for issueing the warning for a non-frame capable web
     * client.
     */
    protected void printFrameWarning() {
        h1();
        println("Frame Alert");
        h1End();
        p();
        print("This document is designed to be viewed using the ");
        print("frames feature. If you see this message, you are ");
        println("using a non-frame-capable web client.");
    }

    /**
     * Print the frame sizes and their contents.
     */
    protected void printFrameDetails() {
        frameSet("cols=\"20%,80%\"");
        frameSet("rows=\"30%,70%\"");
        frame("src=\"packages-frame.html\" name=\"packageListFrame\"");
        frame("src=\"package-frame-" + firstpackage + ".html"
               + "\" name=\"packageFrame\"");
        frameSetEnd();
        frame("src=\"packages.html\" name=\"classFrame\"");
        frameSetEnd();
    }
}



