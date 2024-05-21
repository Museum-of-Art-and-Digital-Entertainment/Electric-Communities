/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;

/** JavaMethodInfo provides a holder for normal java methods */
class JavaMethodInfo extends MethodInfo {
    /** Constructor for JavaMethodInfo matches MethodInfo
     *  @see ec.edoc.MethodInfo#MethodInfo */
    JavaMethodInfo(String name, Comment comment, int modifiers,
            String type, ClassInterfaceInfo containingClass) {
        super(name, comment, modifiers, type, containingClass);
    }

    /** This constructor is used to create a java method from an existing
     *  emethod. This allows the intrenal state ie. parameters, to be cloned
     *  in a simple manner.  It is created with a new name, container and
     *  modifiers.
     *  @param emi, nullFatal; the emethod to be 'copied'
     *  @param name, nullFatal; the name of the new method
     *  @param modifiers; the modifiers for the new method
     *  @param containingClass, nullFatal; the class / interface the new class
     *   belongs toString
     */
    JavaMethodInfo(EMethodInfo emi, TypeGuesser guesser, String name,
            int modifiers, ClassInterfaceInfo containingClass) {
        this(name, emi.myComment, modifiers, "void",
            containingClass);

        myTypes = (Vector) emi.myTypes.clone();
        int size = myTypes.size();
        for (int i = 0; i < size; i++) {
            String s = (String) myTypes.elementAt(i);
            if (guesser.guess(s)) {
                myTypes.setElementAt(s + "_$_Intf", i);
            }
        }
        myNames = emi.myNames;
    }

    /** This constructor is used to get information from the Syntax Tree
     *  (AST) for method declarations.
     *
     *  @param n, nullFatal; is expected to be method decl from the syntax
     *   tree.
     *  @param validator, nullFatal; is a TypeTable which will be used to
     *   validate names, ie. a namespace.
     *  @param containingClass, nullFatal; the class which this method belongs
     *   to.
     *  @see ec.edoc.TypeTable
     */
    JavaMethodInfo(SimpleNode n, TypeTable validator,
            ClassInterfaceInfo containingClass) throws MalformedASTException {

        super(containingClass);

        PointerToInteger childIndex = new PointerToInteger(0);

        /* get modifiers */
        myModifiers = TreeHelper.modifiersFromFirstChildren(n, childIndex);

        /* get my type */
        myType = TreeHelper.typeFromASTType(n, childIndex, validator);

        /* get my name */
        myName = TreeHelper.nameFromASTIdentifier(n, childIndex);

        myTypes = new Vector();
        myNames = new Vector();

        TreeHelper.parametersFromASTFormalParameters(n, childIndex, validator,
                                                    myTypes, myNames);

        //System.out.println(childIndex.datum + ", " + n.jjtGetNumChildren());

        if (childIndex.datum < n.jjtGetNumChildren()) {

            /* New we need to check for the type meth()[][][] syntax */
            Node child = n.jjtGetChild(childIndex.datum++);

            //System.out.println("child = " + child);

            if (child instanceof ASTArrayBrackets) {
                while (child instanceof ASTArrayBrackets) {
                    myType += "[]";
                    if (childIndex.datum >= n.jjtGetNumChildren()) {
                        /* then we have run out of children  before we
                         * hit a node which wasn't brackets */
                        System.err.println("This never gets used?");
                        child = null;
                    } else {
                        child = n.jjtGetChild(childIndex.datum++);
                    }
                }
            }

            /* if we have any throws, we'd better do them */
            if (child instanceof ASTThrows) {
                //System.out.println("Found a throws");
                try {
                    ASTNameList nl = (ASTNameList)(child.jjtGetChild(0));
                    int numChildren = nl.jjtGetNumChildren();
                    for (int i = 0; i < numChildren; i++ ) {
                        this.addThrows(
                            validator.validate(
                                ((ASTName)(nl.jjtGetChild(i))).getName()));
                    }
                } catch (ClassCastException e) {
                    System.out.println("Failed to parse throws, throwing "+
                        "MalformedASTException");
                    throw new MalformedASTException();
                }
                //System.out.println("my throws = "+getThrows());
            }
        }
    }
}

