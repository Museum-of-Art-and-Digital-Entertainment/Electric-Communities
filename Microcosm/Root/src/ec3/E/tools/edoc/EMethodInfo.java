/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;

/** EMethodInfo provides a holder for emethods */
class EMethodInfo extends MethodInfo {
    /** Constructor for EMethodInfo matches MethodInfo, except that it
     *  does not accept a type, since emethods do not return
     *  @see ec.edoc.MethodInfo#MethodInfo */
    EMethodInfo(String name, Comment comment, int modifiers,
            ClassInterfaceInfo containingClass) {
        super(name, comment, modifiers, "void", containingClass);
    }

    /** This constructor is used to get information from the Syntax Tree
     *  (AST) for emethod declarations (or I_InterfaceMemberDeclaration).
     *
     *  @param n, nullFatal; is expected to be emethod decl from the syntax
     *   tree.
     *  @param validator, nullFatal; is a TypeTable which will be used to
     *   validate names, ie. a namespace.
     *  @param containingClass, nullFatal; the class which this method belongs
     *   to.
     *  @see ec.edoc.TypeTable
     */
    EMethodInfo(SimpleNode n, TypeTable validator,
            ClassInterfaceInfo containingClass) throws MalformedASTException {

        super(containingClass);

        PointerToInteger childIndex = new PointerToInteger(0);

        /* get my name */
        myName = TreeHelper.nameFromASTIdentifier(n, childIndex);

        myTypes = new Vector();
        myNames = new Vector();

        TreeHelper.parametersFromASTFormalParameters(n, childIndex, validator,
                                                    myTypes, myNames);
    }

}
