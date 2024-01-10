/*
 * @(#)FieldSubWriter.java  1.6 98/03/18
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
public class FieldSubWriter extends AbstractSubWriter {

    FieldSubWriter(SubWriterHolderWriter writer) {
        super(writer);
    }

    public ProgramElementDoc[] members(ClassDoc cd) {
        return cd.fields();
    }

    public void printSummaryLabel(ClassDoc cd) {
        writer.boldText("doclet.Field_Summary");
    }

    public void printInheritedSummaryLabel(ClassDoc cd) {
        writer.bold();
        writer.printText("doclet.Inherited_Fields_From_Class");
        writer.print(' ');
    writer.printPreQualifiedClassLink(cd);
        writer.boldEnd();
    }

    void printSignature(MemberDoc member) {
        FieldDoc field = (FieldDoc)member;
    writer.pre();
        printModifiers(field);
        printTypeLink(field.type());
        print(' ');
        bold(field.name());
    writer.preEnd();
    }

    protected void printSummaryLink(ClassDoc cd, ProgramElementDoc member) {
        String name = member.name();
        writer.bold();
        writer.printClassLink(cd, name, name);
        writer.boldEnd();
    }

    protected void printInheritedSummaryLink(ClassDoc cd,
                                             ProgramElementDoc member) {
        printSummaryLink(cd, member);
    }

    protected void printSummaryType(ProgramElementDoc member) {
        FieldDoc field = (FieldDoc)member;
        printStaticAndType(field.isStatic(), field.type());
    }

    protected void printHeader(ClassDoc cd) {
        writer.anchor("variables");
        //writer.printIndexHeading(writer.getText("doclet.Fields"));
        writer.printTableHeadingBackground(writer.getText("doclet.Fields"));
    }

    protected void printFooter(ClassDoc cd) {
        //writer.hr();
    }

    protected void printMember(ClassDoc cd, ProgramElementDoc member) {
        FieldDoc field = (FieldDoc)member;
        writer.anchor(field.name());

        printHead(field);
        printSignature(field);
        printFullComment(field);
    }

    protected void printDeprecatedLink(ProgramElementDoc member) {
        writer.printClassLink(member.containingClass(), member.name(),
                              ((FieldDoc)member).qualifiedName());
    }
}


