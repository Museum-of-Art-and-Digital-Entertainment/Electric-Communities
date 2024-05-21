/*
 * @(#)ClassWriter.java 1.20 98/03/18
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
 * Generate the Class Information Page.
     * @see sun.tools.javadoc.ClassDoc
     * @see java.util.Collections
     * @see java.util.List
     * @see java.util.ArrayList
     * @see java.util.HashMap
 *
 * @author Atul M Dambalkar
 * @author Robert Field
 */
public class ClassWriter extends SubWriterHolderWriter {

    protected ClassDoc classdoc;

    protected ClassTree classtree;

    protected String prev;

    protected String next;

    protected String path;  //new

    public ClassWriter(String filename, ClassDoc classdoc,
                       String prev, String next,
                       ClassTree classtree,
                       String sourcepath) throws IOException { //new
        super(filename);
        this.classdoc = classdoc;
        this.classtree = classtree;
        this.prev = prev;
        this.next = next;

    if(sourcepath != null){ //new
           String classname = classdoc.toString(); //new
           classname = classname.replace('.', '/');  //new
           classname = classname.concat(".java");  //new
           path = sourcepath.concat("/" + classname);  //new
    } else { //new
          path = null; //new
    } //new
    }

    public void printClassLink(ClassDoc cd, String where, String tag) {
        if (cd == classdoc) {
            if (where == null) {
                where = "_top_";
            }
            printHyperLink("", where, tag);
        } else {
            super.printClassLink(cd, where, tag);
        }
    }

    /**
     * Generate a class page.
     *
     * @param prev the previous class to generated, or null if no previous.
     * @param classdoc the class to generate.
     * @param next the next class to be generated, or null if no next.
     */
    public static void generate(ClassDoc classdoc, String prev,
                                String next, ClassTree classtree,
                                String sourcepath) //new
        throws DocletAbortException {
            ClassWriter clsgen;
            String filename = classdoc + ".html";
            try {
                clsgen = new ClassWriter(filename, classdoc,
                                         prev, next, classtree, sourcepath);
                clsgen.generateClassFile();
                clsgen.close();
            } catch (IOException exc) {
                error("doclet.exception_encountered", exc.toString(), filename);
                throw new DocletAbortException();
            }
    }

    /**
     * Print this package link
     */
    protected void navLinkPackage() {
        navLinkPackage(classdoc.containingPackage());
    }

    /**
     * Print class/interface hierarchy link
     */
    protected void navLinkTree() {
        navLinkTree(classdoc.containingPackage());
    }

    /**
     * Print class page indicator
     */
    protected void navLinkClass() {
        boldText("doclet.Class");
    }

    /**
     * Print previous item link
     */
    protected void navLinkPrevious() {
        navLinkPrevious(prev);
    }

    /**
     * Print next item link
     */
    protected void navLinkNext() {
        navLinkNext(next);
    }

    public void generateClassFile() {
        String label = getText(classdoc.isInterface()?
                               "doclet.Interface" :
                               "doclet.Class") + " " +
                               classdoc.qualifiedName();
        printHeader(label);
        navLinks(true);
        hr();
        h1(label);

        // if this is a class (not an interface) then generate
        // the super class tree.
        if (!classdoc.isInterface()) {
            pre();
            printTreeForClass(classdoc);
            preEnd();
        }

        printSubClassInterfaceInfo();

        if (classdoc.isInterface()) {
            printImplementingClasses();
        }

        hr();
        printClassDescription();
        printDeprecated();
        // generate documentation for the class.
        String comment = classdoc.commentText();
        if (comment.length() > 0) {
            print(comment);
            p();
        }
        // Print Information about all the tags here
        generateTagInfo(classdoc, path);
        hr();
        p();

        anchor("index");

    printAllMembers();

        hr();
        navLinks(false);
        printBottom();
        printFooter();
    }

    protected void printAllMembers() {
        MethodSubWriter methW = new MethodSubWriter(this);
        ConstructorSubWriter consW = new ConstructorSubWriter(this);
        FieldSubWriter fieldW = new FieldSubWriter(this);
        ClassSubWriter innerW = new ClassSubWriter(this);

    innerW.printMembersSummary(classdoc);
    innerW.printInheritedMembersSummary(classdoc);
        p();
    fieldW.printMembersSummary(classdoc);
    fieldW.printInheritedMembersSummary(classdoc);
        p();
    consW.printMembersSummary(classdoc);
        p();
    methW.printMembersSummary(classdoc);
        methW.printInheritedMembersSummary(classdoc);
        p();

    fieldW.printMembers(classdoc);
    consW.printMembers(classdoc);
    methW.printMembers(classdoc);
    }


    /*** cl ***/
    protected void printClassDescription() {
        boolean isInterface = classdoc.isInterface();
        dl();
        dt();

        print(classdoc.modifiers() + " ");

        if (!isInterface) {
            printText("doclet.class");
            print(' ');
        }
        bold(classdoc.name());

        if (!isInterface) {
            ClassDoc superclass = classdoc.superclass();
            if (superclass != null) {
                dt();
                printText("doclet.extends");
                print(' ');
                printClassLink(superclass);
            }
        }

        ClassDoc[] implIntfacs = classdoc.implementedInterfaces();
        if (implIntfacs != null && implIntfacs.length > 0) {
            dt();
            printText(isInterface? "doclet.extends" : "doclet.implements");
            print(' ');
            printClassLink(implIntfacs[0]);
            for (int i = 1; i < implIntfacs.length; i++) {
                print(", ");
                printClassLink(implIntfacs[i]);
            }
        }
        dlEnd();
    }

    protected void printSourceCodeLink(ClassDoc classdoc) {
        dt();
        boldText("doclet.Source_Code");
        dd();
        printHyperLink(classdoc.qualifiedName() + ".java", null,
                       classdoc.qualifiedName() + ".java");
        ddEnd();
    }

    protected void printDeprecated() {
        Tag[] deprs = classdoc.tags("deprecated");
        if (deprs.length > 0) {
            String text = deprs[0].text();
            bold(getText("doclet.Note_0_is_deprecated",  classdoc.name()));
            if (text.length() > 0) {
                italics(text);
            }
            p();
        }
    }

    protected void printStep(int indent) {
        String spc = spaces(8 * indent - 4);
        print(spc);
        println("|");
        print(spc);
        print("+----");
    }

    protected int printTreeForClass(ClassDoc cd) {
        ClassDoc sup = cd.superclass();
        int indent = 0;
        if (sup != null) {
            indent = printTreeForClass(sup);
            printStep(indent);
        }
        if (cd.equals(classdoc)) {
            print(cd.qualifiedName());
        } else {
            printQualifiedClassLink(cd);
        }
        println();
        return indent + 1;
    }

    protected void printSubClassInterfaceInfo() {
        // Before using TreeBuilder.getSubClassList
        // make sure that tree.html is generated prior.
        List subclasses = classtree.subs(classdoc);
        if (subclasses.size() > 0) {
            printSubClassInfoHeader(subclasses);
            if (classdoc.isClass()) {
                bold(getText("doclet.Subclasses"));
            } else { // this is an interface
                bold(getText("doclet.Subinterfaces"));
            }
            printSubClassLinkInfo(subclasses);
        }
    }

    protected void printImplementingClasses() {
        List implcl = classtree.implementingclasses(classdoc);
        if (implcl.size() > 0) {
            printSubClassInfoHeader(implcl);
            bold(getText("doclet.Implementing_Classes"));
            printSubClassLinkInfo(implcl);
        }
    }


    protected void printSubClassInfoHeader(List list) {
        dl();
        dt();
    }

    protected void printSubClassLinkInfo(List list) {
        int i = 0;
        print(' ');
        dd();
        for (; i < list.size() - 1; i++) {
            printClassLink((ClassDoc)(list.get(i)));
            print(", ");
        }
        printClassLink((ClassDoc)(list.get(i)));
        ddEnd();
        dlEnd();
    }

    protected void generateNavBarFile(String prev, String next) {
        String thispack = "Package-" + classdoc.containingPackage().name()
                          + ".html";
        printTargetHyperLink(thispack, "classListFrame",
                             getText("doclet.Package"));
        print(' ');
        printTargetHyperLink(prev, "classFrame", getText("doclet.Previous"));
        print(' ');
        printTargetHyperLink(next, "classFrame", getText("doclet.Next"));
    }
}




