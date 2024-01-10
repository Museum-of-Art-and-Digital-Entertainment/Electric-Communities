/*
 * @(#)AbstractTreeWriter.java  1.5 98/03/18
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
 * Abstract class to print the class hierarchy page for all the Classes.
 * Sub-classes to generate the Interface Tree and Class Tree Part.
 *
 * @author Atul M Dambalkar
 */
public class AbstractTreeWriter extends HtmlDocWriter {

    protected final ClassTree classtree;

    /**
     * Constructor.
     *
     * @param file String filename
     */
    protected AbstractTreeWriter(String filename, ClassTree classtree)
                                      throws IOException {
        super(filename);
        this.classtree = classtree;
    }

    /**
     * Generate each line for level info regarding classes.
     * Recurses itself to generate subclasses info.
     * To iterate is human, to recurse is divine - L. Peter Deutsch.
     *
     * @param list list of the sub-classes at this level.
     */
    protected void generateLevelInfo(ClassDoc parent, List list) {
        if (list.size() > 0) {
            ul();
            for (int i = 0; i < list.size(); i++) {
                ClassDoc local = (ClassDoc)list.get(i);
                printPartialInfo(local);
                printExtendsImplements(parent, local);
                generateLevelInfo(local, classtree.subs(local));   // Recurse
            }
            ulEnd();
        }
    }

    /**
     * Generate Tree method, to be called by the TreeWriter. Call appropriate
     * methods from the sub-classes.
     */
    protected void generateTree(List list, String heading) {
        if (list.size() > 0) {
            ClassDoc cd = (ClassDoc)list.get(0);
            printTreeHeading(heading);
            generateLevelInfo(cd.isClass()? (ClassDoc)list.get(0): null, list);
        }
    }

    /**
     * Print the information regarding the classes which this class extends or
     * implements.
     *
     * @param cd The classdoc under consideration.
     */
    protected void printExtendsImplements(ClassDoc parent, ClassDoc cd) {
        ClassDoc[] interfaces = cd.implementedInterfaces();
        if (interfaces.length > (cd.isInterface()? 1 : 0)) {
            Arrays.sort(interfaces);
            if (cd.isInterface()) {
                print("(" + getText("doclet.also") + " extends ");
            } else {
                print("(implements ");
            }
            boolean printcomma = false;
            for (int i = 0; i < interfaces.length; i++) {
                if (parent != interfaces[i]) {
                    if (printcomma) {
                        print(", ");
                    }
                    printPreQualifiedClassLink(interfaces[i]);
                    printcomma = true;
                }
            }
            println(")");
        }
    }

    /**
     * Return true if two classes are same else false.
     */
    protected boolean equalNames(ClassDoc parent, ClassDoc comp) {
        if (parent == null) {
            return true;
        }
        return parent.qualifiedName().equals(comp.qualifiedName());
    }

    /**
     * Print informatioon about the class kind.
     *
     * @param cd classdoc.
     */
    protected void printPartialInfo(ClassDoc cd) {
        boolean isInterface = cd.isInterface();
        li("circle");
        print(isInterface? "interface " : "class ");
        printPreQualifiedClassLink(cd);
    }

    /**
     * Print the heading for the tree depending upon if it's Interface Tree or
     * Class Tree.
     */
    protected void printTreeHeading(String heading) {
        h2();
        println(getText(heading));
        h2End();
    }

    /**
     * Print class/interface hierarchy link
     */
    protected void navLinkTree() {
        boldText("doclet.Tree");
    }
}
