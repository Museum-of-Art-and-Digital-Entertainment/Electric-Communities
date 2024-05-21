/*
 * @(#)AbstractSubWriter.java   1.6 98/03/18
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
import java.util.*;
import java.lang.reflect.Modifier;

/**
 *
 * @author Robert Field
 */
public abstract class AbstractSubWriter {

    protected final SubWriterHolderWriter writer;

    /**
     * temp var.
     *
     * track how long the displayed (non-html) contents are.
     */
    protected int displayLength;

    AbstractSubWriter(SubWriterHolderWriter writer) {
        this.writer = writer;
    }

    /*** abstracts ***/

    public abstract ProgramElementDoc[] members(ClassDoc cd);

    public abstract void printSummaryLabel(ClassDoc cd);

    public abstract void printInheritedSummaryLabel(ClassDoc cd);

    protected abstract void printSummaryType(ProgramElementDoc member);

    protected abstract void printSummaryLink(ClassDoc cd, ProgramElementDoc member);

    protected abstract void printInheritedSummaryLink(ClassDoc cd, ProgramElementDoc member);

    protected abstract void printHeader(ClassDoc cd);

    protected abstract void printFooter(ClassDoc cd);

    protected abstract void printMember(ClassDoc cd, ProgramElementDoc elem);

    protected abstract void printDeprecatedLink(ProgramElementDoc member);

    /***  ***/

    protected void print(String str) {
        writer.print(str);
        displayLength += str.length();
    }

    protected void print(char ch) {
        writer.print(ch);
        displayLength++;
    }

    protected void bold(String str) {
        writer.bold(str);
        displayLength += str.length();
    }

    protected void printClassLink(ClassDoc cd) {
        writer.printClassLink(cd);
        displayLength += cd.name().length();
    }

    protected void printTypeLinkNoDimension(Type type) {
        ClassDoc cd = type.asClassDoc();
    if (cd == null) {
        print(type.name());
    } else {
        printClassLink(cd);
    }
    }

    protected void printTypeLink(Type type) {
        printTypeLinkNoDimension(type);
        print(type.dimension());
    }

    /**
     * Return a string describing the access modifier flags.
     * Don't include native or synchronized.
     *
     * The modifier names are returned in canonical order, as
     * specified by <em>The Java Language Specification</em>.
     */
    protected String modifierString(MemberDoc member) {
        int ms = member.modifierSpecifier();
        int no = Modifier.NATIVE | Modifier.SYNCHRONIZED;
    return Modifier.toString(ms & ~no);
    }

    protected void printModifiers(MemberDoc member) {
        String mod;
        if (writer.configuration.oneOne) {
            mod = member.modifiers();  // includes 'native', 'synchronized'
        } else {
            mod = modifierString(member);
        }
        if(mod.length() > 0) {
            print(mod);
            print(' ');
        }
    }

    protected void printTypedName(Type type, String name) {
        if (type != null) {
            if (writer.configuration.oneOne) {
                printTypeLinkNoDimension(type);
            } else {
                printTypeLink(type);
            }
        }
        if(name.length() > 0) {
            writer.print(' ');
            writer.print(name);
        }
        if (writer.configuration.oneOne && type != null) {
            writer.print(type.dimension());
        }
    }

    protected String makeSpace(int len) {
        StringBuffer sb = new StringBuffer(len);
        for(int i = 0; i < len; i++) {
            sb.append(' ');
    }
        return sb.toString();
    }

    /**
     * Print 'static' if static and type link.
     */
    protected void printStaticAndType(boolean isStatic, Type type) {
        writer.printTypeSummaryHeader();
        if (isStatic) {
            print("static&nbsp;");
        }
        if (type != null) {
            printTypeLink(type);
        }
        writer.printTypeSummaryFooter();
    }

    protected void printComment(ProgramElementDoc member) {
        String comment = member.commentText();
        if (comment.length() > 0) {
            writer.dd();
            print(comment);
            // writer.p();
        }
    }

    protected void printTags(ProgramElementDoc member) {
        if (member.seeTags().length > 0) {
            writer.dd();
            writer.dl();
            writer.printSeeTags(member);
            writer.dlEnd();
            writer.ddEnd();
        }
    }

    protected String name(ProgramElementDoc member) {
        return member.name();
    }

    protected void printDeprecated(ProgramElementDoc member) {
        Tag[] deprs = member.tags("deprecated");
        if (deprs.length > 0) {
            String text = deprs[0].text();
        writer.dd();
            writer.bold(writer.getText("doclet.Note_0_is_deprecated",  name(member)));
            if (text.length() > 0) {
                writer.italics(text);
            }
            writer.p();
        }
    }

    protected void printHead(MemberDoc member) {
        writer.h3();
        //writer.printTableHeadingBackground(member.name());
        writer.print(member.name());
        writer.h3End();
    }

    protected void printFullComment(ProgramElementDoc member) {
        writer.dl();
        printDeprecated(member);
        printComment(member);
        printTags(member);
        writer.dlEnd();
    }

    /**
     * Forward to containing writer
     */
    public void printSummaryHeader(ClassDoc cd) {
        writer.printSummaryHeader(this, cd);
    }

    /**
     * Forward to containing writer
     */
    public void printInheritedSummaryHeader(ClassDoc cd) {
        writer.printInheritedSummaryHeader(this, cd);
    }

    /**
     * Forward to containing writer
     */
    public void printInheritedSummaryFooter(ClassDoc cd) {
        writer.printInheritedSummaryFooter(this, cd);
    }

    /**
     * Forward to containing writer
     */
    public void printSummaryFooter(ClassDoc cd) {
        writer.printSummaryFooter(this, cd);
    }

    /**
     * Forward to containing writer
     */
    public void printSummaryMember(ClassDoc cd, ProgramElementDoc member) {
        writer.printSummaryMember(this, cd, member);
    }

    /**
     * Forward to containing writer
     */
    public void printInheritedSummaryMember(ClassDoc cd,
                                            ProgramElementDoc member) {
        writer.printInheritedSummaryMember(this, cd, member);
    }

    public void printMembersSummary(ClassDoc cd) {
        ProgramElementDoc[] members = members(cd);
        if (members.length > 0) {
            Arrays.sort(members);
            printSummaryHeader(cd);
            for (int i = 0; i < members.length; ++i) {
                printSummaryMember(cd, members[i]);
            }
            printSummaryFooter(cd);
        }
    }

    public void printInheritedMembersSummary(ClassDoc cd) {
        ClassDoc icd = cd.superclass();
        while (icd != null) {
            if (!cd.isInterface()) {
                ProgramElementDoc[] members = members(icd);
                if (members.length > 0) {
                    Arrays.sort(members);
                    printInheritedSummaryHeader(icd);
                    printInheritedSummaryMember(icd, members[0]);
                    for (int i = 1; i < members.length; ++i) {
                        print(", ");
                        printInheritedSummaryMember(icd, members[i]);
                    }
                    printInheritedSummaryFooter(icd);
                }
            }
            icd = icd.superclass();
        }
    }

    public void printMembers(ClassDoc cd) {
        ProgramElementDoc[] members = members(cd);
        if (members.length > 0) {
            printHeader(cd);
            for (int i = 0; i < members.length; ++i) {
                if (i > 0) {
                    writer.printMemberHeader();
                }
                printMember(cd, members[i]);
                writer.printMemberFooter();
            }
            printFooter(cd);
        }
    }

    /**
     * Generate the code for listing the deprecated APIs. Create the table
     * format for listing the API. Call methods from the sub-class to complete
     * the generation.
     */
    protected void printDeprecatedAPI(List deprmembers, String headingKey) {
        if (deprmembers.size() > 0) {
            writer.tableIndexSummary();
            writer.tableHeaderStart();
            writer.printText(headingKey);
            writer.tableHeaderEnd();
            for (int i = 0; i < deprmembers.size(); i++) {
                ProgramElementDoc member = (ProgramElementDoc)deprmembers.get(i);
                writer.trBgcolor("#FFFFCC");
                writer.summaryRow(0);
                printDeprecatedLink(member);
                writer.summaryRowEnd();
                writer.summaryRow(0);
                writer.print(member.tags("deprecated")[0].text());
                writer.space();
                writer.summaryRowEnd();
                writer.trEnd();
            }
            writer.tableEnd();
            writer.space();
            writer.p();
        }
    }
}


