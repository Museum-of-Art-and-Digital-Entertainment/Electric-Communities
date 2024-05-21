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
/* XXX having worked with the class file format, it may have been better to 
 * some / much of this using the class file's internal type representation. 
 * Having not seen that before, however, I used a straightforward string type.
 */
class ClassMemberInfo {

    // Modifiers
    static final int PUBLIC = 1;
    static final int PRIVATE = 2;
    static final int PROTECTED = 4;
    static final int STATIC = 8;
    static final int FINAL = 16;
    static final int SYNCHRONIZED = 32;
    static final int VOLATILE = 64;
    static final int TRANSIENT = 128;
    static final int NATIVE = 256;
    static final int ABSTRACT = 1024;

    //Types of class member
    // These are in Coding standard list order...used in IndexEntry
    // Non should ever be 0; this is used as NULL in IndexEntry
    // They should all be under 100, as >100 is a ClassInfo
    static final int FIELD = 1;
    static final int CONSTRUCTOR = 2;
    static final int METHOD = 3;
    static final int EMETHOD = 4;
    static final int EMESSAGE = 5; // used in einterfaces
    //static final int EFORALL = 6;
    static final int INTERFACEMETHOD = 7;
    static final int STATICINITIALISER = 8;

    String comment = null;
    Vector myCommands;
    String name = null;
    int modifiers = 0;
    int type = 0;

    String returnType = null;

    Vector parameterTypes = null;
    Vector parameterNames = null;

    Vector throwsList = null;
    Vector implementsList = null;
    Vector extendsList = null;

    private ClassInfo parent = null;

    void setParent(ClassInfo ci) {
        parent = ci;
    }
  
    ClassInfo getParent() {
        return parent;
    }

    /** Add a formal parameter to the lists*/
    void addParameter(String type, String name) {
        if ((parameterTypes == null) || (parameterNames == null)) { 
            parameterTypes = new Vector();
            parameterNames = new Vector();
        }
        parameterTypes.addElement(type);
        parameterNames.addElement(name);
    }
  
    void addThrows(String name) {
        if (throwsList == null) {
            throwsList = new Vector();
        }
        throwsList.addElement(name);
    }
  
    void addImplements(String name) {
        if (implementsList == null) {
            implementsList = new Vector();
        }
        implementsList.addElement(name);
    }

    void addExtends(String name) {
        if (extendsList == null) {
            extendsList = new Vector();
        }
        extendsList.addElement(name);
    }

    /** Accepts a vector of words (Strings) and builds a single string from them
     */
    void buildComment(Vector v) {
        StringBuffer sb = new StringBuffer();
        String s;

        for (java.util.Enumeration e = v.elements(); e.hasMoreElements(); ) {
            sb.append(s = (String)e.nextElement());
            if (e.hasMoreElements()) {
                sb.append(" ");
            }
        }
  
        comment = sb.toString();
    }

    /** This takes a collection of Information objects & adds them to this 
     *  class / member 
     *
     *  @param v Vector of Information 
     *  @see ec.edoc.Information
     */
    void addCommentCommands(Vector v) {
        
        /* could do a type check here, but do we really need to? 
         * if only we had polymorphic types... */

        myCommands = v;
    }       
    
    int modifierBuilder(int inputFlags, String modifierName) {
        if (modifierName.compareTo("public") == 0) {
            return (inputFlags | PUBLIC );
        }
        if (modifierName.compareTo("private") == 0) {
            return (inputFlags | PRIVATE );
        }
        if (modifierName.compareTo("protected") == 0) {
            return (inputFlags | PROTECTED );
        }
        if (modifierName.compareTo("static") == 0) {
            return (inputFlags | STATIC );
        }
        if (modifierName.compareTo("abstract") == 0) {
            return (inputFlags | ABSTRACT );
        }
        if (modifierName.compareTo("final") == 0) {
            return (inputFlags | FINAL );
        }
        if (modifierName.compareTo("native") == 0) {
            return (inputFlags | NATIVE );
        }
        if (modifierName.compareTo("synchronized") == 0) {
            return (inputFlags | SYNCHRONIZED );
        }
        if (modifierName.compareTo("volatile") == 0) {
            return (inputFlags | VOLATILE );
        }
        if (modifierName.compareTo("transient") == 0) {
            return (inputFlags | TRANSIENT );
        }
        return inputFlags;
    }
    
}

