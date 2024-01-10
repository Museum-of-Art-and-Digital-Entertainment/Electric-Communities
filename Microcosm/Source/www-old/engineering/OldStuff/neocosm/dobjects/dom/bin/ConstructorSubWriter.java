/*
 * @(#)ConstructorSubWriter.java    1.4 98/03/18
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
public class ConstructorSubWriter extends ExecutableMemberSubWriter {

    ConstructorSubWriter(SubWriterHolderWriter writer) {
        super(writer);
    }

    public ProgramElementDoc[] members(ClassDoc cd) {
        return cd.constructors();
    }

    public void printSummaryLabel(ClassDoc cd) {
        writer.boldText("doclet.Constructor_Summary");
    }

    public void printInheritedSummaryLabel(ClassDoc cd) {
        // no such
    }

    protected void printSummaryType(ProgramElementDoc member) {
        // no such
    }

    protected void printTags(ProgramElementDoc member) {
        ParamTag[] params = ((ConstructorDoc)member).paramTags();
        ThrowsTag[] thrown = ((ConstructorDoc)member).throwsTags();
        SeeTag[] sees = member.seeTags();
        if (params.length + thrown.length + sees.length > 0) {
            writer.dd();
            writer.dl();
            printParamTags(params);
            printThrowsTags(thrown);
            writer.printSeeTags(member);
            writer.dlEnd();
            writer.ddEnd();
        }
    }

    protected void printHeader(ClassDoc cd) {
        writer.anchor("constructors");
        //writer.printIndexHeading(writer.getText("doclet.Constructors"));
        writer.printTableHeadingBackground(writer.getText("doclet.Constructors"));
    }
}


