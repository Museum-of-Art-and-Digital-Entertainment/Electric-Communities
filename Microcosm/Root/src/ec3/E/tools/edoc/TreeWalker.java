/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;

/** This class does two things;
 *  (1) It walks over the parse tree to build a list of nodes which should be
 *      documented.
 *  (2) It runs through that list building ClassInfo objects for each one -
 *      these go into a ClassInfo vector for formatting.
 */

class TreeWalker implements EDocParserConstants {

    private Vector myNodeVector = new Vector(100);

    /** walk  recursively builds a vector of all nodes which we actually want
     *  to comment.  This is determined by the parser during parsing.
     */
    /** @param n nullFatal trusted */
    void walk(SimpleNode n) {

        // A node's token field only gets set by the parser if it is a node
        // which cauld be documented.  otherwise token == null, and we don't
        // add it to the documentable node vector.
        if (n.getToken() != null) {
             myNodeVector.addElement(n);
        }

        int num_children = n.jjtGetNumChildren();

        if ( num_children > 0) {
            for (int i = 0; i < num_children; i++) {
                if (n instanceof SimpleNode) {
                    this.walk((SimpleNode) n.jjtGetChild(i));
                }
            }
        }
    }



    /** matchVectors takes the vector of documentable nodes (which is built
     *  during the treewalk) and matches that with the vector of _all_ tokens
     *  While searching through the vectors for matches, it collects all
     *  formal comments, which are then attached to the next documentable node
     *  as a string containing the (commentstripped) concatenation of all the
     *  comments.
     */
    /** @param tokenVector nullFatal trusted */
    void matchVectors(Vector tokenVector) {
        Token tp;
        SimpleNode np;

        String comment = null;

        int tokenIndex = 0;
        for (int nodeIndex = 0; nodeIndex < myNodeVector.size(); nodeIndex++) {
            np = (SimpleNode)(myNodeVector.elementAt(nodeIndex));
            tp = (Token)tokenVector.elementAt(tokenIndex);
            while ( tp != np.getToken()) {
                if (tp.kind == FORMAL_COMMENT) {
                    try {
                        comment = CommentParser.combine(comment, tp.image);
                    } catch (ParseError e) {
                        System.err.println("InternalError; malformed comment");
                        System.err.println(tp.image);
                    }
                }
                tokenIndex++;
                tp = (Token)tokenVector.elementAt(tokenIndex);
            }
            np.setInfo(comment);
            comment = null;
        }
    }


    /** This method usess the list of nodes which resulted from the tree walk.
     *  It builds ClassInfo and relavent ClassMemberInfo objects for each item
     *  and deposits them into the vector 'classes'.
     *
     *  @param classes, nullFatal; a vector into which the generated
     *   ClassInterfaceInfos are placed.
     *  @see ClassInterfaceInfo
     */
    void buildClassInfos(Vector classes) {

        TypeTable TTable = new TypeTable();

        ClassInterfaceInfo currentContainer = null;

        SimpleNode node;

        // for each node which could be commented...
        for (int nodeIndex = 0; nodeIndex < myNodeVector.size(); nodeIndex++) {
            node = ((SimpleNode)myNodeVector.elementAt(nodeIndex));

            /* Any comments which accompanied this node were accumulated
             * and stored when the tokens were matched with the nodes.
             * they were stored as a single comment in the 'info' field
             * of the nodes.
             */
            String commentString = (String)(node.getInfo());

            if (commentString == null) {
                /* For simplicity's sake we might as well have an empty
                 * comment rather than worry about nulls */
                commentString = "/** */";
            }

            Comment comment;
            try {
                comment = CommentParser.process(commentString);
            } catch (ParseError e) {
                System.err.println("Internal Error; tried to examine a "+
                    "malformed comment. eg. perhaps wasn't in /** */;");
                System.err.println(commentString);
                /* then we ignore this comment */
                comment = new Comment();
            }


            if (node instanceof ASTPackageDeclaration) {

                ASTPackageDeclaration pd = (ASTPackageDeclaration) node;
                try {
                    TTable.setCurrentPackage(pd.getPackage());
                } catch (MalformedASTException e) {
                    System.err.println("Error in package declaration at"+
                        pd.lineNumberString());
                }

            } else if (node instanceof ASTImportDeclaration) {

                ASTImportDeclaration id = (ASTImportDeclaration) node;
                try {
                    TTable.putAnImport(id.getImport());
                } catch (MalformedASTException e) {
                    System.err.println("Error in import declaration at"+
                        id.lineNumberString());
                }



            } else if (node instanceof ASTClassDeclaration) {

                try {
                    JavaClassInfo jci = new JavaClassInfo(node, TTable);
                    jci.comment(comment);
                    classes.addElement(currentContainer = jci);
                } catch (MalformedASTException e) {
                    System.err.println("Error reading class declaration");
                }

            } else if (node instanceof ASTE_ClassDeclaration) {

                try {
                    EClassInfo eci = new EClassInfo(node, TTable);
                    eci.comment(comment);
                    classes.addElement(currentContainer = eci);
                } catch (MalformedASTException e) {
                    System.err.println("Error reading eclass declaration");
                }

            } else if (node instanceof ASTInterfaceDeclaration) {

                try {
                    JavaInterfaceInfo jii = new JavaInterfaceInfo(node, TTable);
                    jii.comment(comment);
                    classes.addElement(currentContainer = jii);
                } catch (MalformedASTException e) {
                    System.err.println("Error reading interface declaration");
                }

            } else if (node instanceof ASTE_InterfaceDeclaration) {

                try {
                    EInterfaceInfo eii = new EInterfaceInfo(node, TTable);
                    eii.comment(comment);
                    classes.addElement(currentContainer = eii);
                } catch (MalformedASTException e) {
                    System.err.println("Error reading einterface declaration");
                }


            } else if (node instanceof ASTFieldDeclaration) {

                try {
                    FieldInfo fi[] =
                        FieldInfo.construct(node, TTable, currentContainer);
                    for (int i = 0; i < fi.length; i++) {
                        currentContainer.addMember(fi[i]);
                        fi[i].comment(comment);
                    }
                } catch (MalformedASTException e) {
                    System.err.println("Error reading field declaration");
                } catch (MemberNotSupportedException e) {
                    System.err.println("Cannot add field, cannot be part of"+
                        " this class or interface");
                }
            } else if (node instanceof ASTMethodDeclaration) {

                try {
                    JavaMethodInfo jmi =
                        new JavaMethodInfo(node, TTable, currentContainer);
                    jmi.comment(comment);
                    currentContainer.addMember(jmi);
                } catch (MalformedASTException e) {
                    System.err.println("Error reading method declaration");
                } catch (MemberNotSupportedException e) {
                    System.err.println("Cannot add method, cannot be part of"+
                        " this class or interface");
                }
            } else if (node instanceof ASTConstructorDeclaration) {

                try {
                    ConstructorInfo ci =
                        new ConstructorInfo(node, TTable, currentContainer);
                    ci.comment(comment);
                    currentContainer.addMember(ci);
                } catch (MalformedASTException e) {
                    System.err.println("Error reading constructor declaration");
                } catch (MemberNotSupportedException e) {
                    System.err.println("Cannot add constructor, cannot "+
                        "be part of this class or interface");
                }
            } else if (node instanceof ASTE_MethodDeclaration
                    || node instanceof ASTE_InterfaceMemberDeclaration) {

                try {
                    EMethodInfo emi =
                        new EMethodInfo(node, TTable, currentContainer);
                    emi.comment(comment);
                    currentContainer.addMember(emi);
                } catch (MalformedASTException e) {
                    System.err.println("Error reading emethod declaration");
                } catch (MemberNotSupportedException e) {
                    System.err.println("Cannot add emethod, cannot be part of"+
                        " this class or interface");
                }
            } else if (node instanceof ASTStaticInitializer) {

                try {
                    StaticInitialiserInfo si =
                        new StaticInitialiserInfo(currentContainer);
                    si.comment(comment);
                    currentContainer.addMember(si);
                } catch (MemberNotSupportedException e) {
                    System.err.println("Cannot add static initialiser, "+
                        "cannot be part of this class or interface");
                }
            } else {
                throw new Error("Found a commentable parse tree which I "+
                    "don't know how to deal with");
                // it's a class member

            }
        } // end for each (documentable) node

    } // end buildClassInfos
}
