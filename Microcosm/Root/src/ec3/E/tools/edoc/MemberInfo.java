/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;

/** This class stores all the pertinent information about a class member,
 *  ie. a field or method (or emethod etc etc)
 */
abstract class MemberInfo extends Info {

    /** Constructor to create a new MemberInfo.
     *  @param name, nullFatal; Name of this item.
     *  @param comment, nullOK; The comment for this item.
     *  @param modifiers; Base modifiers for this item, can be manipulated with
     *   addModifier, maskModifier
     *  @see #addModifier
     *  @see #maskModifier
     *
     *******
     *
     *  @param type, nullFatal; Type, or returnTYpe of this item. NB.
     *   should be a fully qualified type eg. java.lang.String[], which can
     *   be created with TypeTable.
     *  @see ec.edoc.TypeTable
     *  @param containingClass, nullFatal; the class that this member
     *   belongs to.
     */
    MemberInfo(String name, Comment comment, int modifiers,
            String type, ClassInterfaceInfo containingClass) {
        super(name, comment, modifiers);
        myContainingClass = containingClass;
        myType = type;
    }

    /** Constructor used purely when building from trees
     *  @see ec.edoc.Info#Info */
    protected MemberInfo(ClassInterfaceInfo containingClass)
            throws MalformedASTException {
        super();
        myContainingClass = containingClass;
    }

    /** Used to store the type / return type of this member */
    protected String myType = null;

    /** Accessor method for the type of this Info
     *  @return nullFatal; the name of this Info */
    /* is nullFatal, because the constructor must not be passed null. */
    String type() {
        return myType;
    }

    /** Accessor method for the type, represented as used internally by the
     *  java compiler, of this Info
     *  @return nullFatal; the internal rep. of this info's type.
     *  @see ec.edoc.TypeTable#getInternalName
     */
    /* this is a default implementation - methods will need to override this*/
    String internalType() {
        return TypeTable.getInternalName(myType);
    }

    /** myContainingClass is used to store a reference to the class in which
     *  this member belongs */
    protected ClassInterfaceInfo myContainingClass = null;

    /** Accessor method to retrieve the class this belongs to
     *  @return nullFatal; the class this belongs to */
    ClassInterfaceInfo containingClass() {
        return myContainingClass;
    }

    /** Used to modify the containing class of a MemberInfo. Typically used
     *  along with .clone() to create a copy of this member in a different
     *  class.
     *  @param c, nullFatal; the new containing class.
     *  @see java.lang.Object.html#clone
     */
    void containingClass(ClassInterfaceInfo c) {
        myContainingClass = c;
    }

}
