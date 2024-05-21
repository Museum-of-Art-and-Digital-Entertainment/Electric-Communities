/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;

/** This class provides a collection of utility functions for extracting 
 *  information from the Abstract Syntax Tree. 
 */
class TreeHelper {

    /** This method examines the children of node n, from number 'childIndex' 
     *  onwards. For each ASTModifier found, this Info is modified accordingly
     *  it returns once there are no more ASTModifiers, returning the number
     *  of the first non ASTModifier found.
     *  @exception ec.edoc.MalformedASTException; thrown if we are 
     *   unable to retrieve information from the ASTModifier.
     *  @param n, nullFatal; the root of the tree to be examined.
     *  @param childIndex, nullFatal; a PointerToInteger pointing to 
     *   the first child to be considered, typically 0. This is left pointing
     *   at the first non-ASTModifier child;
     *  @return; the modifiers found.
     */
    static int modifiersFromFirstChildren
            (SimpleNode n, PointerToInteger childIndex) 
            throws MalformedASTException {
       
        int totalChildren = n.jjtGetNumChildren();
       
        if (childIndex.datum >= totalChildren) {
            /* then we're supposed to look off the end ! */
            throw new ArrayIndexOutOfBoundsException();
        }
        
        int modifiers = 0;
        Node child;
        while ((child = n.jjtGetChild(childIndex.datum)) 
                instanceof ASTModifier) {
            /* can throw MalformedASTException; */
            modifiers |= ((ASTModifier)child).intValue();
            childIndex.datum++;
        }
        
        return modifiers;
    }
    
    /** This method examines the child of node n, at childIndex.
     *  Assuming an ASTIdentifier is found, the name is set accordingly,
     *  otherwise MalformedASTException is thrown.
     *
     *  this method return just the identifier. It may be that case that this
     *  return value needs to be validated by a TypeTable
     *  @see ec.edoc.TypeTable
     *
     *  @exception ec.edoc.MalformedASTException; thrown if we are 
     *   unable to retrieve information from the ASTIdentifier, or
     *   if child number childIndex isn't an ASTIdentifier
     *  @param validator, nullFatal; the TypeTable which this Identifier
     *   should be validated in - equivalent to namespace / scope ?
     *  @param n, nullFatal; the root of the tree to be examined.
     *  @param childIndex, nullFatal; the child to be considered, typically 0,
     *   is advanced te point to the next child if an Identifier is found.
     *  @return, nullFatal; the string retrieved from the tree. not null,
     *   since an exception is thrown if there are any problems.
     */
    static String nameFromASTIdentifier(SimpleNode n, 
            PointerToInteger childIndex) 
            throws MalformedASTException {
                
        int totalChildren = n.jjtGetNumChildren();
       
        if (childIndex.datum >= totalChildren) {
            /* then we're supposed to look off the end ! */
            throw new ArrayIndexOutOfBoundsException();
        }
        
        Node child = n.jjtGetChild(childIndex.datum);
        if (child instanceof ASTIdentifier) {
            /* must put FQ type name here, so we call validate
             * which also ensures that this class goes into TTable */
            childIndex.datum++;
            return ((ASTIdentifier)child).getName();
        } else {
            throw new MalformedASTException();
        }
    }
    
    /** This method examines the child of node n, at childIndex
     *  assuming an ASTIdentifier is found, it's name is returned,
     *  otherwise MalformedASTException is thrown.
     *  @exception ec.edoc.MalformedASTException; thrown if we are 
     *   unable to retrieve information from the AST, or
     *   if child number childIndex isn't an ASTType or ASTResultType
     *  @param validator, nullFatal; the TypeTable which this Identifier
     *   should be validated in - equivalent to namespace / scope ?
     *  @param n, nullFatal; the root of the tree to be examined.
     *  @param childIndex, nullFatal; the child to be considered, typically 0,
     *   is advanced te point to the next child if a Type is found.
     *  @return nullFatal; the string retrieved from the tree. not null,
     *   since an exception is thromn if there are any problems.
     */
    static String typeFromASTType(SimpleNode n, 
            PointerToInteger childIndex, TypeTable validator) 
            throws MalformedASTException {
                
        if (childIndex.datum >= n.jjtGetNumChildren()) {
            /* then we're supposed to look off the end ! */
            throw new ArrayIndexOutOfBoundsException();
        }
        
        Node child = n.jjtGetChild(childIndex.datum);
        if (child instanceof ASTResultType) {
        
            try {
                ASTResultType rt = (ASTResultType)child;
                if (rt.getInfo() == null) {
                    /* then we have a type */
                    childIndex.datum++;
                    return validator.validate(
                        ((ASTType)(rt.jjtGetChild(0))).getName());
                } else {
                    /* we don't have a type - ie void */
                    childIndex.datum++;
                    return "void";
                }
            } catch (ClassCastException e) {
                throw new MalformedASTException();
            }
        } else if (child instanceof ASTType) {
        
            childIndex.datum++;
            return validator.validate(((ASTType)child).getName());
            
        } else {
            throw new MalformedASTException();
        }
    }
    
    /** This method examines the child of node n, at childIndex.
     *  assuming an ASTFormalParameter is found, the name & type are returned 
     *  otherwise MalformedASTException is thrown.
     *  @exception ec.edoc.MalformedASTException; thrown if we are 
     *   unable to retrieve information from the AST, or
     *   if child number childIndex isn't an ASTFormalParameter.
     *  @param validator, nullFatal; the TypeTable which this Identifier
     *   should be validated in - equivalent to namespace / scope ?
     *  @param n, nullFatal; the ASTFormalParameter of the tree to be examined.
     *  @return nullFatal; a NameTypePair representing the name & type.
     */
    protected static NameTypePair parameterFromASTFormalParameter(
            ASTFormalParameter n, TypeTable validator) 
            throws MalformedASTException {
    
        if (n.jjtGetNumChildren() != 2) {
            throw new MalformedASTException();
        }
        
        try {
            String t = validator.validate(
                ((ASTType)(n.jjtGetChild(0))).getName());
            String nm = ((ASTVariableDeclaratorId)
                (n.jjtGetChild(1))).getName();
            return new NameTypePair(nm, t);
        } catch (ClassCastException e) {
            throw new MalformedASTException();
        }
    }
            
    /** This method examines the child of node n, at childIndex
     *  assuming an ASTFormalParameters is found, the two vectors are 
     *  populated with the names & types,
     *  otherwise MalformedASTException is thrown.
     *  @exception ec.edoc.MalformedASTException; thrown if we are 
     *   unable to retrieve information from the AST, or
     *   if child number childIndex isn't an ASTFormalParameters.
     *  @param validator, nullFatal; the TypeTable which this Identifier
     *   should be validated in - equivalent to namespace / scope ?
     *  @param n, nullFatal; the root of the tree to be examined.
     *  @param childIndex; the child to be considered, typically 0,
     *   is advanced te point to the next child if a Type is found.
     *  @param parameterTypes, nullFatal; a vector to put types into.
     *   this is probably empty, but should exist (!= null)
     *  @param parameterNames, nullFatal; a vector to put names into.
     *   this is probably empty, but should exist (!= null)
     */
    static void parametersFromASTFormalParameters(
            SimpleNode n, PointerToInteger childIndex, TypeTable validator,  
            Vector parameterTypes, Vector parameterNames) 
            throws MalformedASTException {
            
        int totalChildren = n.jjtGetNumChildren();
       
        if (childIndex.datum >= totalChildren) {
            /* then we're supposed to look off the end ! */
            throw new ArrayIndexOutOfBoundsException();
        }
        
        Node child = n.jjtGetChild(childIndex.datum);
        
        if (child instanceof ASTFormalParameters) {
        
            ASTFormalParameters fps = (ASTFormalParameters)child;
            int numParameters = fps.jjtGetNumChildren();

            for (int i = 0; i < numParameters; i++) {
                try {
                    NameTypePair nt = parameterFromASTFormalParameter( 
                        (ASTFormalParameter)fps.jjtGetChild(i),
                        validator);
                    parameterTypes.addElement(nt.type());
                    parameterNames.addElement(nt.name());
                } catch (ClassCastException e) {
                    throw new MalformedASTException();
                }
            }
            
            childIndex.datum++;
            
        } else {
            throw new MalformedASTException();
        }
    }
}

    