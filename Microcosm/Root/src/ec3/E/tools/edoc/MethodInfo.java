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
abstract class MethodInfo extends MemberInfo {

    /** myTypes is used to store the types of any parameters */
    protected Vector myTypes = null;
    /** myNames is used to store the names of any parameters */
    protected Vector myNames = null;
    /** myThrows is used to store a list of exceptions */
    protected Vector myThrows = null;

    /** Constructor to create a new MemberInfo.
     *  @param name, nullFatal; Name of this item.
     *  @param type, nullFatal; Type, or returnTYpe of this item. NB.
     *   should be a fully qualified type eg. java.lang.String[], which can
     *   be created with TypeTable.
     *  @see ec.edoc.TypeTable.html
     *  @param comment, nullOK; The comment for this item.
     *  @param modifiers; Base modifiers for this item, can be manipulated with
     *   addModifier, maskModifier
     *  @see #addModifier
     *  @see #maskModifier
     *  @param containingClass, nullFatal; the class that this member
     *   belongs to.
     */
    MethodInfo(String name, Comment comment, int modifiers,
            String type, ClassInterfaceInfo containingClass) {
        super(name, comment, modifiers, type, containingClass);
        if (type == null) {
            throw new RuntimeException();
        }
        myTypes = new Vector();
        myNames = new Vector();
        myThrows = new Vector();
    }

    /** Constructor used purely when building from trees
     *  @see ec.edoc.Info#Info */
    protected MethodInfo(ClassInterfaceInfo containingClass)
            throws MalformedASTException {
        super(containingClass);
        myTypes = new Vector();
        myNames = new Vector();
        myThrows = new Vector();
    }


    /** Add a formal my to the lists
     *  @param type, nullFatal; the type of the parameter.
     *   assumed to be fully qualified; use TypeTable
     *  @param name, nullFatal; the name of the parameter.
     *  @see ec.edoc.TypeTable
     */
    void addParameter(String type, String name) {
        myTypes.addElement(type);
        myNames.addElement(name);
    }

    /** Retrieve the list of the types of parameters.
     *  @return nullFatal; the list of types. possibly empty.
     *  @see #parameterNames
     */
    Vector parameterTypes() {
        return myTypes;
    }
    /** Retrieve the list of the names of parameters.
     *  @return nullFatal; the list of names. possibly empty.
     *  @see #parameterTypes
     */
    Vector parameterNames() {
        return myNames;
    }

    /** Add an exception to the list thrown by this method.
     *  @param name, nullFatal; the (fully qualified) type of the exception.
     *  @see ec.edoc.TypeTable
     */
    void addThrows(String name) {
        //System.out.println("MethodInfo.addThrows("+name+")");
        myThrows.addElement(name);
    }
    /** Get the list of interfaces this class implements.
     *  @return nullFatal; is Vector(String), the FQ types of all interfaces,
     *   possibly empty */
    Vector getThrows() {
        return myThrows;
    }

    /** Accessor method for the type, represented as used internally by the
     *  java compiler, of this MethodInfo
     *  @return nullFatal; the internal rep. of this info's type.
     *  @see ec.edoc.TypeTable#getInternalName
     */
    String internalType() {

        StringBuffer sb = new StringBuffer();
        sb.append('(');
        if (myTypes != null) {
            java.util.Enumeration et = myTypes.elements();
            while (et.hasMoreElements()) {
                sb.append(TypeTable.getInternalName((String)et.nextElement()));
            }
        }
        sb.append(')');
        sb.append(TypeTable.getInternalName(myType));

        return sb.toString();
    }


}
