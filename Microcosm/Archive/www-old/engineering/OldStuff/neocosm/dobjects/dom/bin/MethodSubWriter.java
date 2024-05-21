/*
 * @(#)MethodSubWriter.java 1.7 98/03/18
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
public class MethodSubWriter extends ExecutableMemberSubWriter {

    MethodSubWriter(SubWriterHolderWriter writer) {
        super(writer);
    }

    public ProgramElementDoc[] members(ClassDoc cd) {
        return cd.methods();
    }

    public void printSummaryLabel(ClassDoc cd) {
        writer.boldText("doclet.Method_Summary");
    }

    public void printInheritedSummaryLabel(ClassDoc cd) {
        writer.bold();
        if (cd.isClass()) {
            writer.printText("doclet.Inherited_Methods_From_Class");
        } else {
            writer.printText("doclet.Inherited_Methods_From_Interface");
        }
        writer.print(' ');
    writer.printPreQualifiedClassLink(cd);
        writer.boldEnd();
    }

    protected void printSummaryType(ProgramElementDoc member) {
        MethodDoc meth = (MethodDoc)member;
        printStaticAndType(meth.isStatic(), meth.returnType());
    }

    protected void printReturnTag(Tag[] returns) {
        if (returns.length > 0) {
            writer.dt();
            writer.boldText("doclet.Returns");
            writer.dd();
            writer.print(returns[0].text());
        }
    }

    protected void printOverriden(ClassDoc overriden, MethodDoc method) {
        if (overriden != null) {
            String name = method.name();
            writer.dt();
            writer.boldText("doclet.Overrides");
            writer.dd();
            if (overriden.isIncluded()) {
                writer.printHyperLink(overriden.qualifiedName() + ".html",
                                      name + method.signature(), name);
            } else {
                // not in this run
                writer.print(name);
            }
            writer.print(' ');
            writer.printText("doclet.in_class");
            writer.print(' ');
            writer.printClassLink(overriden);
        }
    }

    protected void printTags(ProgramElementDoc member) {
        MethodDoc method = (MethodDoc)member;
        ParamTag[] params = method.paramTags();
        Tag[] returns = method.tags("return");
        ThrowsTag[] thrown = method.throwsTags();
        SeeTag[] sees = method.seeTags();
        ClassDoc[] intfacs = member.containingClass().implementedInterfaces();
        ClassDoc overriden = method.overridenClass();
        if (params.length + returns.length + thrown.length
            + intfacs.length + sees.length > 0 ||
            overriden != null) {
            writer.dd();
            writer.dl();
            printImplementsInfo(method);
            printParamTags(params);
            printReturnTag(returns);
            printThrowsTags(thrown);
            printOverriden(overriden, method);
            writer.printSeeTags(method);
            writer.dlEnd();
            writer.ddEnd();
        }
    }

    protected void printSignature(ExecutableMemberDoc member) {
        displayLength = 0;
    writer.pre();
    printModifiers(member);
    printReturnType((MethodDoc)member);
    bold(member.name());
    printParameters(member);
    printExceptions(member);
    writer.preEnd();
    }

    protected void printReturnType(MethodDoc method) {
        Type type = method.returnType();
        if (type != null) {
            printTypeLink(type);
            print(' ');
        }
    }

    protected void printHeader(ClassDoc cd) {
        writer.anchor("methods");
        //writer.printIndexHeading(writer.getText("doclet.Methods"));
        writer.printTableHeadingBackground(writer.getText("doclet.Methods"));
    }
}


