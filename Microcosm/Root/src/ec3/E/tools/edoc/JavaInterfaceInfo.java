/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;

/** JavaInterfaceInfo is a class used to represent pertinent information about
 *  a normal java interface.
 *  @see ec.edoc.EInterfaceInfo
 *  @see ec.edoc.InterfaceInfo
 */
class JavaInterfaceInfo extends InterfaceInfo {
    /** Constructor for JavaInterfaceInfo matches Info
     *  @see ec.edoc.Info#Info */
    JavaInterfaceInfo(String name, Comment comment, int mods) {
        super(name, comment, mods);
    }

    /** Constructor for JavaInterfaceInfo matches ClassInterfaceInfo
     *  @see ec.edoc.ClassInterfaceInfo#ClassInterfaceInfo */
    JavaInterfaceInfo(SimpleNode n, TypeTable t)
            throws MalformedASTException {
        super(n, t);
    }

    /** Add a member to this interface
     *  This handler overrides ClassInterfaceInfo.addMember()
     *  and handles class specific members.
     *  @param i; a MemberInfo representing the field.
     *  @exception ec.edoc.MemberNotSupportedException
     *  @see ec.edoc.ClassInterfaceInfo.html#addMember
     *  @see ec.edoc.MemberInfo.html
     */
    void addMember(MemberInfo i) throws MemberNotSupportedException {

        if (i instanceof FieldInfo) {
            this.addMember((FieldInfo)i);
        } else if (i instanceof JavaMethodInfo) {
            this.addMember((JavaMethodInfo)i);
        } else {
            super.addMember(i);
        }
    }

    /** Used to store list of fields */
    protected Vector myFields = null;
    /** Add a field (data member) to this class / interface
     *  @param i; a FieldInfo representing the field.
     *  @see ec.edoc.FieldInfo
     */
    protected void addMember(FieldInfo i) {
        if (myFields == null) {
            myFields = new Vector();
        }
        myFields.addElement(i);
    }
    /** Accessor method used to get a list of fields
     *  @return nullOK; return Vector(FieldInfo), which might be null
     *   if there are no fields */
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
        if (myMethods == null) {
            myMethods = new Vector();
        }
        myMethods.addElement(i);
    }
    /** Accessor method used to get a list of methods
     *  @return nullOK; return Vector(JavaMethodInfo), which might be null
     *   if there are no methods */
    Vector methods() {
        return myMethods;
    }

}
