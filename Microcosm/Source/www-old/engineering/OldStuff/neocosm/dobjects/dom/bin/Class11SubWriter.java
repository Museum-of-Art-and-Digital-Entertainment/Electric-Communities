/*
 * @(#)Class11SubWriter.java    1.3 98/03/18
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

/**
 *
 * @author Robert Field
 */
public class Class11SubWriter extends ClassSubWriter {

    final Class11Writer writer11;

    Class11SubWriter(Class11Writer writer11) {
        super(writer11);
    this.writer11 = writer11;
    }

    protected void printInnerClassBall(ClassDoc cd, boolean small) {
        if (cd.isStatic()) {
            writer11.printBall("blue-ball", "Blue Ball", small);
        } else {
            writer11.printBall("magenta-ball", "Magenta Ball", small);
        }
    }

    public void printSummaryLabel(ClassDoc cd) {
            writer11.printBanner("class-index", "doclet.Class_Index", 216, 37);
    }

    protected void printSummaryLink(ClassDoc cd, ProgramElementDoc member) {
            writer.printClassLink((ClassDoc)member);
    }

    protected void printSummaryType(ProgramElementDoc member) {
            printInnerClassBall((ClassDoc)member, true);
    }
}


