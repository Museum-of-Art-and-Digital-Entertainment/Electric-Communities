/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;

/** ConstructorInfo provides a holder for constructors */
class ConstructorInfo extends MethodInfo {
    /** Constructor for ConstructorInfo matches MethodInfo
     *  @see ec.edoc.MethodInfo#MethodInfo */
    ConstructorInfo(String name, Comment comment, int modifiers,
            String type, ClassInterfaceInfo containingClass) {
        super(name, comment, modifiers, type, containingClass);
    }

    /** This constructor is used to get information from the Syntax Tree
     *  (AST) for constructor declarations.
     *
     *  @param n, nullFatal; is expected to be a constructor decl from
     *   the syntax tree.
     *  @param validator, nullFatal; is a TypeTable which will be used to
     *   validate names, ie. a namespace.
     *  @param containingClass, nullFatal; the class which this method belongs
     *   to.
     *  @see ec.edoc.TypeTable
     */
    ConstructorInfo(SimpleNode n, TypeTable validator,
            ClassInterfaceInfo containingClass) throws MalformedASTException {

        super(containingClass);

        PointerToInteger childIndex = new PointerToInteger(0);

        /* get modifiers */
        myModifiers = TreeHelper.modifiersFromFirstChildren(n, childIndex);

        /* get my name */
        /* this should perhaps be taken to be implicit ? */
        myName = TreeHelper.nameFromASTIdentifier(n, childIndex);

        myType = "void";

        /* Sanity check */
        String s = myContainingClass.name();
        if (!myName.equals(s.substring(s.lastIndexOf('.') + 1, s.length()))) {
            System.err.println("Error Contructor name does not match class name"
                +"\nMight indicate a missing return type...("+myName+")");
        }

        myTypes = new Vector();
        myNames = new Vector();

        TreeHelper.parametersFromASTFormalParameters(n, childIndex, validator,
                                                    myTypes, myNames);

        if (childIndex.datum < n.jjtGetNumChildren()) {

            /* if we have any throws, we'd better do them */
            Node child = n.jjtGetChild(childIndex.datum++);
            if (child instanceof ASTThrows) {
                try {
                    ASTNameList nl = (ASTNameList)(child.jjtGetChild(0));
                    int numChildren = nl.jjtGetNumChildren();
                    for (int i = 0; i < numChildren; i++ ) {
                        this.addThrows(
                            validator.validate(
                                ((ASTName)(nl.jjtGetChild(i))).getName()));
                    }
                } catch (ClassCastException e) {
                    throw new MalformedASTException();
                }
            }
        }
    }
}

