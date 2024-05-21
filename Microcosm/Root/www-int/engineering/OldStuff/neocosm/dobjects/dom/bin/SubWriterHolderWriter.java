/*
 * @(#)SubWriterHolderWriter.java   1.6 98/03/18
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
 * This abstract class exists to provide functionality needed in the
 * the formatting of member information.  Since AbstractSubWriter and its
 * subclasses control this, they would be the logical place to put this.
 * However, because each member type has its own subclass, subclassing
 * can not be used effectively to change formatting.  The concrete
 * class subclass of this class can be subclassed to change formatting.
 *
 * @see AbstractSubWriter
 * @see ClassWriter
 *
 * @author Robert Field
 */
public abstract class SubWriterHolderWriter extends HtmlDocWriter {

    public SubWriterHolderWriter(String filename) throws IOException {
        super(filename);
    }

    public void printTypeSummaryHeader() {
        tdIndex();
        font("-1");
    }

    public void printTypeSummaryFooter() {
        fontEnd();
        tdEnd();
    }

    public void printSummaryHeader(AbstractSubWriter mw, ClassDoc cd) {
        tableIndexSummary();
        tableHeaderStart();
        mw.printSummaryLabel(cd);
        tableHeaderEnd();
    }

    public void printTableHeadingBackground(String str) {
        tableIndexDetail();
        tableHeaderStart("#CCFFCC", 1);
        bold(str);
        tableHeaderEnd();
        tableEnd();
    }

    public void printInheritedSummaryHeader(AbstractSubWriter mw, ClassDoc cd) {
        tableIndexSummary();
        tableInheritedHeaderStart();
        mw.printInheritedSummaryLabel(cd);
        tableHeaderEnd();
        trBgcolor("#FFFFCC");
        summaryRow(0);
        space();
    }

    public void printSummaryFooter(AbstractSubWriter mw, ClassDoc cd) {
        tableEnd();
        space();
        //p();
    }

    public void printInheritedSummaryFooter(AbstractSubWriter mw, ClassDoc cd) {
        summaryRowEnd();
        trEnd();
        tableEnd();
        space();
        //p();
    }

    protected void printCommentDef(Doc member) {
        dd();
        printIndexComment(member);
    }

    protected void printIndexComment(Doc member) {
        Tag[] deprs = member.tags("deprecated");
        String comment = firstSentence(member.commentText());
        if (comment == "") {
            comment = "&nbsp;";
        }
        println(comment);
        if (deprs.length > 0) {
            boldText("doclet.Deprecated");
        }
    }

    public void printSummaryMember(AbstractSubWriter mw, ClassDoc cd,
                                   ProgramElementDoc member) {
        trBgcolor("#FFFFCC");
        mw.printSummaryType(member);
        summaryRow(0);
        print("&nbsp;");
        mw.printSummaryLink(cd, member);
        br();
        printCommentDef(member);
        summaryRowEnd();
        trEnd();
    }

    public void printInheritedSummaryMember(AbstractSubWriter mw, ClassDoc cd,
                                            ProgramElementDoc member) {
        mw.printInheritedSummaryLink(cd, member);
    }

    public void printMemberHeader() {
        //hr(1,50);
        hr();
    }

    public void printMemberFooter() {
    }
}




