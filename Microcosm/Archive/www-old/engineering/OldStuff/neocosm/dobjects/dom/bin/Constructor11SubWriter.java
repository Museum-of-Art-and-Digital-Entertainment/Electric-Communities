/*
 * @(#)Constructor11SubWriter.java  1.4 98/03/18
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
public class Constructor11SubWriter extends ConstructorSubWriter {

    final Class11Writer writer11;

    Constructor11SubWriter(Class11Writer writer11) {
        super(writer11);
    this.writer11 = writer11;
    }

    protected void printConstructorBall(ConstructorDoc cons, boolean small) {
        writer11.printBall("yellow-ball", "Yellow Ball", small);
    }

    public void printSummaryLabel(ClassDoc cd) {
        writer11.printBanner("constructor-index", "doclet.Constructor_Index", 275, 38);
    }

    protected void printHeader(ClassDoc cd) {
        writer.anchor("constructors");
        writer11.printBanner("constructors", "doclet.Constructors", 151, 38);
    }

    protected void printFooter(ClassDoc cd) {
    }

    protected void printSummaryType(ProgramElementDoc member) {
        printConstructorBall((ConstructorDoc)member, true);
    }

    protected void printSummaryLink(ClassDoc cd, ProgramElementDoc member) {
        ConstructorDoc cons = (ConstructorDoc)member;
        String name = cons.name();
        writer.printMemberHyperLink("#" + name + cons.signature(), name);
        writer.println(cons.flatSignature());
    }

    protected void printMember(ClassDoc cd, ProgramElementDoc member) {
        ConstructorDoc cons = (ConstructorDoc)member;
        String name = cons.name();
        writer.aName(name + cons.signature());
        printConstructorBall(cons, false);
        writer.aEnd();

        writer.bold(name);

        // Get the signature,
        // generate link for each parameter if it is a class.
        printSignature(cons);
        printFullComment(cons);
    }
}


