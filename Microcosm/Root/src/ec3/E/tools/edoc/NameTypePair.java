/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;

/** This class simply holds a name and a type. It ensures that, if the name
 *  has any array bracketts ([]) they will be woved onto the type. This is
 *  useful, eg. from a variable declaration where someone might say 
 *  type var[][] instead of type[][] var.  */
class NameTypePair {

    private String myName;
    private String myType;
    
    /** Creates a new NTP
     *  @param name, nullFatal; the name
     *  @param type, nullFatal; the type
     */
    NameTypePair(String name, String type) {
        while (name.endsWith("[]")) {
            name = name.substring(0, name.length() - 2);
            type = type + "[]";
        }
        myName = name;
        myType = type;
    }
    
    /** Accessor method 
     *  @return nullFatal; the name */
    String name() {
        return myName;
    }
    
    /** Accessor method 
     *  @return nullFatal; the type */
    String type() {
        return myType;
    }
    
}

