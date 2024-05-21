/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;

abstract class InterfaceInfo extends ClassInterfaceInfo {

    /** Constructor for InterfaceInfo matches Info
     *  @see ec.edoc.Info#Info */
    InterfaceInfo(String name, Comment comment, int modifiers) {
        super(name, comment, modifiers);
        myImplements = new Vector();
    }

    /** Constructor for InterfaceInfo matches
     *  ClassInterfaceInfo.deferredConstructor
     *  @see ec.edoc.ClassInterfaceInfo#deferredConstructor  */
    protected InterfaceInfo(SimpleNode n, TypeTable t)
            throws MalformedASTException {
        super();
        myImplements = new Vector();
        deferredConstructor(n, t);
    }

    /** Get the supertype of this class
     *  @return; the supertype, in fully qualified form.*/
    String getExtends() {
        return "java.lang.Object";
    }

    /** Used to store the list of interfaces this inherits from */
    protected Vector myImplements = null;
    /** Add an interface to the list which this inherits from.
     *  @note an interface is declared to _extends_ its superinterfaces,
     *   whereas a class _implements_ its.
     *  @param name, nullFatal; the (fully qualified) type of the
     *   superinterface.
     *  @see ec.edoc.TypeTable
     */
    void addExtends(String name) {
        myImplements.addElement(name);
    }
    /** An interface is declared to _extends_ its superinterfaces,
     *  whereas a class _implements_ its, this is hence a synonym for
     *  addExtends()
     *  @see #addExtends */
    void addImplements(String name) {
        this.addExtends(name);
    }
    /** Get the list of interfaces this class implements.
     *  @return nullFatal; is Vector(String), the FQ types of all interfaces,
     *   possibl[y empty */
    Vector getImplements() {
        return myImplements;
    }

    /** Add a member to this class
     *  This handler overrides ClassInterfaceInfo.addMember()
     *  and handles interface specific members.
     *
     *  The intersection between java interface members and einterface
     *  members is empty; einterfaces can _only_ contain emethods, which
     *  cannot be in java interfaces. Hence we don't actually handle anything
     *  here.
     *
     *  @param i; a MemberInfo representing the field.
     *  @exception ec.edoc.MemberNotSupportedException
     *  @see ec.edoc.ClassInterfaceInfo.html#addMember
     *  @see ec.edoc.MemberInfo.html
     */
    void addMember(MemberInfo i) throws MemberNotSupportedException {
        super.addMember(i);
    }


}

