/*
 * @(#)Field11SubWriter.java    1.4 98/03/18
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
public class Field11SubWriter extends FieldSubWriter {

    final Class11Writer writer11;

    Field11SubWriter(Class11Writer writer11) {
        super(writer11);
    this.writer11 = writer11;
    }

    protected void printFieldBall(FieldDoc field, boolean small) {
        if (field.isStatic()) {
            writer11.printBall("blue-ball", "Blue Ball", small);
        } else {
            writer11.printBall("magenta-ball", "Magenta Ball", small);
        }
    }

    public void printSummaryLabel(ClassDoc cd) {
        writer11.printBanner("variable-index", "doclet.Variable_Index", 207, 38);
    }

    protected void printSummaryLink(ClassDoc cd, ProgramElementDoc member) {
        String name = member.name();
        writer.printClassLink(cd, name, name);
    }

    protected void printSummaryType(ProgramElementDoc member) {
        printFieldBall((FieldDoc)member, true);
    }

    protected void printHeader(ClassDoc cd) {
        writer.anchor("variables");
        writer11.printBanner("variables", "doclet.Fields", 153, 38);
    }

    protected void printFooter(ClassDoc cd) {
    }

    protected void printMember(ClassDoc cd, ProgramElementDoc member) {
        FieldDoc field = (FieldDoc)member;
        writer.aName(field.name());
        printFieldBall(field, false);
        writer.aEnd();
        writer.bold(field.name());

        printSignature(field);
        printFullComment(member);
    }
}


