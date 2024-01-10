/*
 * @(#)ExecutableMemberSubWriter.java   1.6 98/03/18
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
 * Print method and constructor info.
 *
 * @author Robert Field
 */
public abstract class ExecutableMemberSubWriter extends AbstractSubWriter {

    ExecutableMemberSubWriter(SubWriterHolderWriter writer) {
        super(writer);
    }

    protected void printSignature(ExecutableMemberDoc member) {
        displayLength = 0;
    writer.pre();
    printModifiers(member);
    bold(member.name());
    printParameters(member);
    printExceptions(member);
    writer.preEnd();
    }

    protected void printDeprecatedLink(ProgramElementDoc member) {
        ExecutableMemberDoc emd = (ExecutableMemberDoc)member;
        writer.printClassLink(emd.containingClass(),
                              emd.name() + emd.signature(), emd.qualifiedName());
    }

    protected void printSummaryLink(ClassDoc cd, ProgramElementDoc member) {
        ExecutableMemberDoc emd = (ExecutableMemberDoc)member;
        String name = emd.name();
    writer.bold();
    writer.printClassLink(cd, name + emd.signature(), name);
    writer.boldEnd();
        displayLength = name.length();
    printParameters(emd);
    }

    protected void printInheritedSummaryLink(ClassDoc cd,
                                             ProgramElementDoc member) {
        ExecutableMemberDoc emd = (ExecutableMemberDoc)member;
        String name = emd.name();
    writer.printClassLink(cd, name + emd.signature(), name);
    }

    protected void printParam(Parameter param) {
        printTypedName(param.type(), param.name());
    }

    protected void printParameters(ExecutableMemberDoc member) {
        String indent = "";
        print('(');
        Parameter[] params = member.parameters();
        if (params.length > 0) {
            indent = makeSpace(displayLength);
            printParam(params[0]);
        }

        for (int i = 1; i < params.length; i++) {
            writer.print(',');
            writer.print('\n');
            writer.print(indent);
            printParam(params[i]);
        }

        writer.print(')');
    }

    protected void printExceptions(ExecutableMemberDoc member) {
        ClassDoc[] except = member.thrownExceptions();
        if(except.length > 0) {
            writer.print(' ');
            writer.printText("doclet.throws");
            writer.print(' ');
            printClassLink(except[0]);

            for(int i = 1; i < except.length; i++) {
                writer.print(", ");
                printClassLink(except[i]);
            }
        }
    }

    protected void printImplementsInfo(MethodDoc method) {
        ClassDoc[] implIntfacs = method.containingClass().implementedInterfaces();
        if (implIntfacs.length > 0) {
            ClassDoc intfac = implementsMethodInIntfac(method, implIntfacs);
            if (intfac != null) {
                writer.dt();
                writer.boldText("doclet.Implements");
                writer.dd();
                writer.printHyperLink(intfac.qualifiedName() + ".html",
                               "#" + method.name(),
                               method.name());
                writer.print(' ');
                writer.printText("doclet.in_interface");
                writer.print(' ');
                printClassLink(intfac);
            }
        }
    }

    protected ClassDoc implementsMethodInIntfac(MethodDoc method,
                                                ClassDoc[] intfacs) {
        for (int i = 0; i < intfacs.length; i++) {
            MethodDoc[] methods = intfacs[i].methods();
            if (methods.length > 0) {
                for (int j = 0; j < methods.length; j++) {
                    if (methods[j].name().equals(method.name())) {
                        return intfacs[i];
                    }
                }
            }
        }
        return null;
    }

    protected void printParamTags(ParamTag[] params) {
        if (params.length > 0) {
            writer.dt();
            writer.boldText("doclet.Parameters");
            for (int i = 0; i < params.length; ++i) {
                ParamTag pt = params[i];
                writer.dd();
                writer.code();
                print(pt.parameterName());
                writer.codeEnd();
                print(" - ");
                writer.println(pt.parameterComment());
            }
        }
    }

    protected void printThrowsTags(ThrowsTag[] thrown) {
        if (thrown.length > 0) {
            writer.dt();
            writer.boldText("doclet.Throws");
            for (int i = 0; i < thrown.length; ++i) {
                ThrowsTag tt = thrown[i];
                writer.dd();
                ClassDoc cd = tt.exception();
                if (cd == null) {
                    writer.print(tt.exceptionName());
                } else {
                    printClassLink(cd);
                }
                print(" - ");
                print(tt.exceptionComment());
            }
        }
    }

    protected String name(ProgramElementDoc member) {
        return member.name() + "()";
    }

    protected void printFooter(ClassDoc cd) {
        //writer.hr();
    }

    protected void printMember(ClassDoc cd, ProgramElementDoc member) {
        ExecutableMemberDoc emd = (ExecutableMemberDoc)member;
        String name = emd.name();
        writer.anchor(name);
        writer.anchor(name + emd.signature());

        printHead(emd);
        printSignature(emd);
        printFullComment(emd);
    }
}


