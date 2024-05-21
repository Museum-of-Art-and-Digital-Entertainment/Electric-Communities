/*
 * @(#)ClassSubWriter.java  1.5 98/03/18
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
public class ClassSubWriter extends AbstractSubWriter {

    ClassSubWriter(SubWriterHolderWriter writer) {
        super(writer);
    }

    public ProgramElementDoc[] members(ClassDoc cd) {
        return cd.innerClasses();
    }

    public void printSummaryLabel(ClassDoc cd) {
        writer.boldText("doclet.Inner_Class_Summary");
    }

    public void printInheritedSummaryLabel(ClassDoc cd) {
        writer.bold();
        writer.printText("doclet.Inherited_Inner_Classes_From_Class");
        writer.print(' ');
    writer.printPreQualifiedClassLink(cd);
        writer.boldEnd();
    }

    protected void printSummaryLink(ClassDoc cd, ProgramElementDoc member) {
        writer.bold();
        writer.printClassLink((ClassDoc)member);
        writer.boldEnd();
    }

    protected void printInheritedSummaryLink(ClassDoc cd,
                                             ProgramElementDoc member) {
        printSummaryLink(cd, member);
    }

    protected void printSummaryType(ProgramElementDoc member) {
        ClassDoc cd = (ClassDoc)member;
        printStaticAndType(cd.isStatic(), null);
    }

    protected void printHeader(ClassDoc cd) {
        // N.A.
    }

    protected void printFooter(ClassDoc cd) {
        // N.A.
    }

    protected void printMember(ClassDoc cd, ProgramElementDoc member) {
        // N.A.
    }

    protected void printDeprecatedLink(ProgramElementDoc member) {
        writer.printClassLink((ClassDoc)member);
    }
}


