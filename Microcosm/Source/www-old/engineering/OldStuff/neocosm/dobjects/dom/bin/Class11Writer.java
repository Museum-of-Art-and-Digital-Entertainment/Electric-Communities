/*
 * @(#)Class11Writer.java   1.6 98/03/18
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
 * @author Robert Field
 */
public class Class11Writer extends ClassWriter {

    public Class11Writer(String filename, ClassDoc classdoc,
                         String prev, String next,
                         ClassTree classtree, String sourcePath) throws IOException {
            super(filename, classdoc, prev, next, classtree, sourcePath);
    }

    /**
     * Generate a class page.
     *
     * @param prev the previous class to generated, or null if no previous.
     * @param classdoc the class to generate.
     * @param next the next class to be generated, or null if no next.
     */
    public static void generate(ClassDoc classdoc,
                                String prev, String next,
                                ClassTree classtree, String sourcePath)
                                throws DocletAbortException {
            Class11Writer clsgen;
            String filename = classdoc + ".html";
            try {
                clsgen = new Class11Writer(filename, classdoc,
                                           prev, next,
                                           classtree, sourcePath);
                clsgen.generateClassFile();
                clsgen.close();
            } catch (IOException exc) {
                error("doclet.exception_encountered", exc.toString(), filename);
                throw new DocletAbortException();
            }
    }

    protected void printAllMembers() {
        MethodSubWriter methW = new Method11SubWriter(this);
        ConstructorSubWriter consW = new Constructor11SubWriter(this);
        FieldSubWriter fieldW = new Field11SubWriter(this);
        ClassSubWriter innerW = new Class11SubWriter(this);

    innerW.printMembersSummary(classdoc);
    fieldW.printMembersSummary(classdoc);
    consW.printMembersSummary(classdoc);
    methW.printMembersSummary(classdoc);

    fieldW.printMembers(classdoc);
    consW.printMembers(classdoc);
    methW.printMembers(classdoc);
    }

// - the following logically belong in AbstractSubWriter,
// but are here to allow subclassing -

    public void printBall(String ball, String alt, boolean small) {
        int size = 12;
        if (small) {
            size = 6;
            ball = ball + "-small";
        }
        printImage(ball, " o ", size, size);
    }

    public void printBanner(String gif, String nameKey, int width, int height) {
        h2();
        printImage(gif, getText(nameKey), width, height);
        h2End();
    }

    public void printSummaryHeader(AbstractSubWriter mw, ClassDoc cd) {
        mw.printSummaryLabel(cd);  // icon
        dl();
    }

    public void printSummaryFooter(AbstractSubWriter mw, ClassDoc cd) {
        dlEnd();
    }

    public void printInheritedSummaryHeader(AbstractSubWriter mw, ClassDoc cd) {
        // N.A.
    }

    public void printSummaryMember(AbstractSubWriter mw, ClassDoc cd,
                                   ProgramElementDoc member) {
        dt();
        mw.printSummaryType(member);  // ball
        mw.printSummaryLink(cd, member);  // link & sig
        dd();
        printIndexComment(member);
    }

    public void printMemberHeader() {
    }

    public void printMemberFooter() {
    }
}




