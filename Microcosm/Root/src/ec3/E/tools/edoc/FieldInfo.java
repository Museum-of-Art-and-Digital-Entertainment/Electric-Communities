/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;

class FieldInfo extends MemberInfo {
    /** Constructor for FieldInfo matches MemberInfo
     *  @see ec.edoc.MemberInfo#MemberInfo */
    FieldInfo(String name, Comment comment, int modifiers,
            String type, ClassInterfaceInfo containingClass) {
        super(name, comment, modifiers, type, containingClass);
    }

    /** This method is used to get information from the Syntax Tree
     *  (AST) for field declarations. It cannot be a constructor, since
     *  one field declaration might create more than one field.
     *
     *  @param n, nullFatal; is expected to be field decl from the syntax
     *   tree. It will have its life blood
     *   sucked out of it. :-)
     *  @param validator, nullFatal; is a TypeTable which will be used to
     *   validate names, ie. a namespace.
     *  @param containingClass, nullFatal; the class which this field belongs
     *   to.
     *  @return nullFatal; of type FieldInfo[]
     *  @see ec.edoc.TypeTable
     */
    static FieldInfo[] construct(SimpleNode n, TypeTable validator,
                                ClassInterfaceInfo containingClass)
            throws MalformedASTException {

        /* children are (1) modifier[s] (2) type (3) name[s]
         * ignore the type straight away */
        int numFields = n.jjtGetNumChildren() - 1;

        PointerToInteger childIndex = new PointerToInteger(0);

        /* get modifiers */
        int ourModifiers =
            TreeHelper.modifiersFromFirstChildren(n, childIndex);

        /* take off however many modifiers we had. */
        numFields -= childIndex.datum;

        /* get type */
        String ourType = TreeHelper.typeFromASTType(n, childIndex, validator);

        FieldInfo[] ret = new FieldInfo[numFields];
        for (int i = 0; i < numFields; i++) {
            Node child = n.jjtGetChild(childIndex.datum + i);

            String name;
            try {
                name = ((ASTVariableDeclaratorId)
                    ((ASTVariableDeclarator)child).jjtGetChild(0)).getName();
            } catch (ClassCastException e) {
                throw new MalformedASTException();
            }

            NameTypePair nt = new NameTypePair(name, ourType);

            /* create each FI */
            ret[i] = new FieldInfo(nt.name(), null, ourModifiers,
                                    nt.type(), containingClass);
        }

        return ret;
    }
}

