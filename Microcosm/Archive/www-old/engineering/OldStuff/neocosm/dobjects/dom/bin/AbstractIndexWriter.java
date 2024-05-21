/*
 * @(#)AbstractIndexWriter.java 1.4 98/03/18
 *
 * Copyright 1998 by Sun Microsystems, Inc.,
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
 * Generate Files for all the Member Names with Indexing in
 * Alphabetical Order.
 *
 * @author Atul M Dambalkar
 */
public class AbstractIndexWriter extends HtmlDocWriter {

    protected IndexBuilder indexbuilder;

    protected AbstractIndexWriter(String filename,
                             IndexBuilder indexbuilder) throws IOException {
        super(filename);
        this.indexbuilder = indexbuilder;
    }

    /**
     * Print index link - it's me: bold
     */
    protected void navLinkIndex() {
        boldText("doclet.Index");
    }

    protected void generateContents(Character unicode, List memberlist) {
        anchor("_" + unicode + "_");
        h2();
        bold(unicode.toString());
        h2End();
        dl();
        for (int i = 0; i < memberlist.size(); i++) {
            ProgramElementDoc element = (ProgramElementDoc)memberlist.get(i);
            if (element instanceof MemberDoc) {
                printDescription((MemberDoc)element);
            } else if (element instanceof ClassDoc) {
                printDescription((ClassDoc)element);
            }
        }
        dlEnd();
        hr();
    }


    protected void printDescription(ClassDoc cd) {
        dt();
        printClassLink(cd);
        print(' ');
        printClassInfo(cd);
        dd();
        print(firstSentence(cd.commentText()));
    }

    protected void printClassInfo(ClassDoc cd) {
        if (cd.isOrdinaryClass()) {
            printText("doclet.class");
        } else if (cd.isInterface()) {
            printText("doclet.interface");
        } else if (cd.isException()) {
            printText("doclet.exception");
        } else {   // error
            printText("doclet.error");
        }
        print(' ');
        printPreQualifiedClassLink(cd);
        print('.');
    }


    /**
     * Generate Description for Class, Field, Method or Constructor.
     * for Java.* Packages Class Members
     *
     * @param member MemberDoc for the member of the Class Kind.
     * @see sun.tools.javadoc.ClassDoc#classKind
     * @see sun.tools.javadoc.MemberDoc
     */
    protected void printDescription(MemberDoc element) {
        String name = element.name();
        ClassDoc containing = element.containingClass();
        String qualname = containing.qualifiedName();
        String baseClassName = containing.name();
        dt();
        if (element instanceof FieldDoc) {
            printMemberLink((FieldDoc)element, qualname, name);
        } else if (element instanceof ExecutableMemberDoc) {
            printMemberLink((ExecutableMemberDoc)element, qualname, name);
        }
        println('.');
        printMemberDesc(element);
        print(' ');
        printText(containing.isInterface()?
                             "doclet.interface" :
                             "doclet.class");
        print(" ");
        printPreQualifiedClassLink(containing);
        println();
        dd();
        print(firstSentence(element.commentText()));
        println();
    }

    protected void printMemberLink(FieldDoc element, String qualname,
                                   String name)  {
        printMemberHyperLink(qualname +".html" + "#" +  name, name);
    }

    protected void printMemberLink(ExecutableMemberDoc element,
                                   String qualname, String name)  {
        printMemberHyperLink(qualname +".html" + "#" +
                                    name + element.signature(),
                                    name);
        print(element.flatSignature());
    }

    /**
     * Print description about the Static/Method/Constructor for a member.
     *
     * @param member MemberDoc for the member within the Class Kind.
     * @see sun.tools.javadoc.MemberDoc
     */
    protected void printMemberDesc(MemberDoc member) {
        if (member.isField()) {
            if (member.isStatic()) {
                printText("doclet.Static_variable_in");
            } else {
                printText("doclet.Variable_in");
            }
        } else if (member.isConstructor()) {
            printText("doclet.Constructor_for");
        } else if (member.isMethod()) {
            if (member.isStatic()) {
                printText("doclet.Static_method_in");
            } else {
                printText("doclet.Method_in");
            }
        }
    }
}
