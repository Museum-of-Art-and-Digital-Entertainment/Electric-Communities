/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;


/** Used to store information about classes */
abstract class ClassInfo extends ClassInterfaceInfo {

    /** Constructor for ClassInfo matches Info
     *  @see ec.edoc.Info#Info */
    ClassInfo(String name, Comment comment, int modifiers) {
        super(name, comment, modifiers);
        myImplements = new Vector();
        myMethods = new Vector();
        myConstructors = new Vector();
        myFields = new Vector();
    }

    /** Constructor for ClassInfo matches
     *  ClassInterfaceInfo.deferredConstructor
     *  @see ec.edoc.ClassInterfaceInfo#deferredConstructor  */
    protected ClassInfo(SimpleNode n, TypeTable t)
            throws MalformedASTException {
        super();
        myImplements = new Vector();
        myMethods = new Vector();
        myConstructors = new Vector();
        myFields = new Vector();
        deferredConstructor(n, t);
    }

    /** Used to store the superclass of this class */
    protected String myExtends = null;
    /** Set the superclass of this class.
     *  @param name, nullFatal; the (fully qualified) type of the superclass.
     *  @see ec.edoc.TypeTable
     */
    void addExtends(String name) {
        myExtends = name;
    }
    /** Get the supertype of this class
     *  @return; the supertype, in fully qualified form. */
    String getExtends() {

        if (myExtends == null) {
            /* then it hasn't been set */
            return "java.lang.Object";
        }

        return myExtends;
    }

    /** Used to store the list of interfaces this class implements */
    protected Vector myImplements = null;
    /** Add an interface to the list of what this class implements.
     *  @param name, nullFatal; the (fully qualified) type of the interface.
     *  @see ec.edoc.TypeTable  */
    void addImplements(String name) {
        myImplements.addElement(name);
    }
    /** Get the list of interfaces this class implements.
     *  @return nullFatal; is Vector(String), the FQ types of all interfaces,
     *   possibly empty */
    Vector getImplements() {
        return myImplements;
    }

    /** Add a member to this class
     *  This handler overrides ClassInterfaceInfo.addMember()
     *  and handles class specific members.
     *  @param i; a MemberInfo representing the field.
     *  @exception ec.edoc.MemberNotSupportedException if the class or
     *   interface being added to cannot contain the member being added.
     *   for example adding an emethod to a java class
     *  @see ec.edoc.ClassInterfaceInfo#addMember
     *  @see ec.edoc.MemberInfo
     */
    void addMember(MemberInfo i) throws MemberNotSupportedException {

        if (i instanceof FieldInfo) {
            this.addMember((FieldInfo)i);
        } else if (i instanceof ConstructorInfo) {
            this.addMember((ConstructorInfo)i);
        } else if (i instanceof JavaMethodInfo) {
            this.addMember((JavaMethodInfo)i);
        } else if (i instanceof StaticInitialiserInfo) {
            this.addMember((StaticInitialiserInfo)i);
        } else {
            super.addMember(i);
        }
    }

    /** Used to store information about any contructors */
    protected Vector myConstructors = null;
    /** Add a Constructor to this class
     *  @param i; a ConstructorInfo representing the Constructor.
     *  @see ec.edoc.ConstructorInfo */
    protected void addMember(ConstructorInfo i) {
        myConstructors.addElement(i);
    }
    /** Accessor method used to get a list of constructors
     *  @return nullFatal; return Vector(ConstructorInfo), possibly empty
     */
    Vector constructors() {
        return myConstructors;
    }


    /** Used to store whether or not this class has any static initialisers*/
    protected boolean myHaveStaticInitialiser = false;
    /** 'Add' a Static InitialiserMethod to this class
     *  in actual fact this is simply stating that one exists; they hold
     *  no interesting type information, and since we do not produce code,
     *  their code is irrelevant.
     *  @param i; a StaticInitialiser representing the method.
     *  @see ec.edoc.StaticInitialiserInfo
     */
    protected void addMember(StaticInitialiserInfo i) {
        myHaveStaticInitialiser = true;
    }
    /** Accessor method used to check if this class has any static
     *  initialisers
     *  @return; return Vector(FieldInfo), which might be null
     *   if there are no fields */
    boolean hasStaticInitialiser() {
        return myHaveStaticInitialiser;
    }

    /** Used to store list of fields */
    protected Vector myFields = null;
    /** Add a field (data member) to this class / interface
     *  @param i; a FieldInfo representing the field.
     *  @see ec.edoc.FieldInfo
     */
    protected void addMember(FieldInfo i) {
        myFields.addElement(i);
    }
    /** Accessor method used to get a list of fields
     *  @return nullFatal; return Vector(FieldInfo), wpossibly empty
     */
    Vector fields() {
        return myFields;
    }

    /* used to store list of methods */
    protected Vector myMethods = null;
    /** Add a Method (member function) to this class / interface
     *  @param i; a JavaMethodInfo representing the method.
     *  @see ec.edoc.JavaMethodInfo
     */
    protected void addMember(JavaMethodInfo i) {
        myMethods.addElement(i);
    }
    /** Accessor method used to get a list of methods
     *  @return nullFatal; return Vector(JavaMethodInfo), possibly empty
     */
    Vector methods() {
        return myMethods;
    }

}


