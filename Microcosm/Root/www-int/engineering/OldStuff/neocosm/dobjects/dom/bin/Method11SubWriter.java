/*
 * @(#)Method11SubWriter.java   1.4 98/03/18
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
 * Write method information in the old JDK 1.1 style.
 *
 * @author Robert Field
 */
public class Method11SubWriter extends MethodSubWriter {

    final Class11Writer writer11;

    Method11SubWriter(Class11Writer writer11) {
        super(writer11);
    this.writer11 = writer11;
    }

    protected void printMethodBall(MethodDoc method, boolean small) {
        if(method.isStatic()) {
            writer11.printBall("green-ball", "Green Ball", small);
        } else {
            writer11.printBall("red-ball", "Red Ball", small);
        }
    }

    public void printSummaryLabel(ClassDoc cd) {
        writer11.printBanner("method-index", "doclet.Method_Index", 207, 38);
    }

    protected void printSummaryType(ProgramElementDoc member) {
        printMethodBall((MethodDoc)member, true);
    }

    protected void printSummaryLink(ClassDoc cd, ProgramElementDoc member) {
        MethodDoc method = (MethodDoc)member;
        String name = method.name();
        writer.printMemberHyperLink("#" + name + method.signature(), name);
        writer.println(method.flatSignature());
    }

    /**
     * Reproduce 1.1 bug.  Remove soon.
     */
    protected void printOverriden(ClassDoc overriden, MethodDoc method) {
        if (overriden != null) {
            String name = method.name();
            writer.dt();
            writer.boldText("doclet.Overrides");
            writer.dd();
        // bug: no test
        writer.printHyperLink(overriden.qualifiedName() + ".html",
                                  name + method.signature(), name);
            writer.print(' ');
            writer.printText("doclet.in_class");
            writer.print(' ');
            writer.printClassLink(overriden);
        }
    }

    protected void printHeader(ClassDoc cd) {
        writer.anchor("methods");
        writer11.printBanner("methods", "doclet.Methods", 151, 38);
    }

    protected void printFooter(ClassDoc cd) {
    }

    protected void printMember(ClassDoc cd, ProgramElementDoc member) {
        MethodDoc method = (MethodDoc)member;
        String name = method.name();
        writer.aName(name + method.signature());
        printMethodBall(method, false);
        writer.aEnd();

        writer.aName(name);
        writer.bold(name);
        writer.aEnd();

        // Get the signature,
        // generate link for each parameter if it is a class.
        printSignature(method);
        printFullComment(method);
    }
}


