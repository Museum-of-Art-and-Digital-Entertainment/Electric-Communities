/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;


/** This class stores all the pertinent information about
 *  either a class or a class member,
 *  ie. a field or method (or emethod etc etc)
 */
/* XXX having worked with the class file format, it may have been better to
 * some / much of this using the class file's internal type representation.
 * Having not seen that before, however, I used a straightforward string type.
 */
abstract class Info implements Cloneable {

    protected Comment myComment = null;
    protected String myName = null;
    protected int myModifiers = 0;

    /** Constructor to create a new Info.
     *  @param name, nullFatal; Name of this item.
     *  @param comment, nullOK; The comment for this item.
     *  @param modifiers; Base modifiers for this item, can be manipulated with
     *   addModifier, maskModifier
     *  @see #addModifier
     *  @see #maskModifier
     */
    Info(String name, Comment comment, int modifiers) {
        myName = name;
        if (comment == null) {
            myComment = new Comment();
        } else {
            myComment = comment;
        }
        myModifiers = modifiers;
    }

    /** This is a protected constructor, it should only be called by the
     *  concrete subclasses of Info. It does nothing. */
    protected Info() {
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    /*********************************************************/

    /* Modifiers */
    static final int PUBLIC = 1;        // 0x01
    static final int PRIVATE = 2;       // 0x02
    static final int PROTECTED = 4;     // 0x04
    static final int STATIC = 8;        // 0x08
    static final int FINAL = 16;        // 0x10
    static final int SYNCHRONIZED = 32; // 0x20
    static final int VOLATILE = 64;     // 0x40
    static final int TRANSIENT = 128;   // 0x80
    static final int NATIVE = 256;      // 0x100
    static final int ABSTRACT = 1024;   // 0x400

    /** Accessor method fer modifiers
     *  @return; the int value of the modifiers */
    int modifiers() {
        return myModifiers;
    }

    /** Get a string listing all the modifiers
     *  @return nullFatal; a string containing all modifiers */
    String getModifierString() {
        return getModifierString(myModifiers);
    }


    /*XXX comments here */
    /** Get a string listing all the modifiers
     *  @return nullFatal; a string containing all modifiers */
    static String getModifierString(int mods) {

        StringBuffer sb = new StringBuffer();
        if ((mods & PUBLIC) != 0) {
            sb.append("public ");
        }
        if ((mods & PROTECTED) != 0) {
            sb.append("protected ");
        }
        if ((mods & PRIVATE) != 0) {
            sb.append("private ");
        }
        if ((mods & STATIC) != 0) {
            sb.append("static ");
        }
        if ((mods & ABSTRACT) != 0) {
            sb.append("abstract ");
        }
        if ((mods & FINAL) != 0) {
            sb.append("final ");
        }
        if ((mods & NATIVE) != 0) {
            sb.append("native ");
        }
        if ((mods & SYNCHRONIZED) != 0) {
            sb.append("synchronized ");
        }
        if ((mods & TRANSIENT) != 0) {
            sb.append("transient ");
        }
        if ((mods & VOLATILE) != 0) {
            sb.append("volatile ");
        }
        return sb.toString();
    }

    /** Add modifiers to this info
     *  @param inputFlags; the input flags are bitwise OR'd with the
     *  current modifiers. To replace the existing modifiers use
     *  maskModifier(0)
     *  @see #maskModifier
     */
    void addModifier(int inputFlags) {
        myModifiers |= inputFlags;
    }

    /** Remove modifiers from this info.
     *  @param mask; is bitwise AND'd with the modifiers therfore use
     *  mask(~(FLAG_TO_REMOVE))
     */
    void maskModifiers(int mask) {
        myModifiers &= mask;
    }

    /** Add a modifier to this Info
     *  @param modifierName, nullFatal; The java keyword for the desired
     *  modifier
     */
    void addModifier(String modifierName) {
        if (modifierName.compareTo("public") == 0) {
            this.addModifier(PUBLIC);
        } else if (modifierName.compareTo("private") == 0) {
            this.addModifier(PRIVATE);
        } else if (modifierName.compareTo("protected") == 0) {
            this.addModifier(PROTECTED);
        } else if (modifierName.compareTo("static") == 0) {
            this.addModifier(STATIC);
        } else if (modifierName.compareTo("abstract") == 0) {
            this.addModifier(ABSTRACT);
        } else if (modifierName.compareTo("final") == 0) {
            this.addModifier(FINAL);
        } else if (modifierName.compareTo("native") == 0) {
            this.addModifier(NATIVE);
        } else if (modifierName.compareTo("synchronized") == 0) {
            this.addModifier(SYNCHRONIZED);
        } else if (modifierName.compareTo("volatile") == 0) {
            this.addModifier(VOLATILE);
        } else if (modifierName.compareTo("transient") == 0) {
            this.addModifier(TRANSIENT);
        }
    }


    /*********************************************************/

    /** Accessor method for the name of this Info
     *  @return nullFatal; the name of this Info */
    /* is nullFatal, because the constructor must not be passed null. */
    String name() {
        return myName;
    }

    /** This method allows the comment to be set on an existing info
     *  This is a kludge really because i want to get i tworking.
     *  for elegance, the whole constructor collection which deals
     *  with parse tree fragments fro generating Infos (ie. the whole
     *  Info class hierarchy) should be changed to take the Comment
     *  in the constructor. (XXX)
     */
    /** For the meantime, I'm going to put this back door in.
     *  @param c nullOK; the comment to add. null is ignored
     */
    void comment(Comment c) {
        if (c != null) {
            myComment = c;
        }
    }

    /** Accessor method for the comment of this Info
     *  @return nullFatal; the comment for this Info */
    Comment comment() {
        return myComment;
    }


    /*********************************************************/


}

