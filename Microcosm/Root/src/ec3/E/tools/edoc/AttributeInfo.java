/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */
package ec.edoc;

/** This class is a struct for attributes which end up in the class file 
 *  as of the moment we don't need any of these, but we may use them in the 
 *  future
 */
abstract class AttributeInfo {

    /*u2*/ ConstPoolInfo myName; /* pointer to UTF for name of attrib */
    /*u4*/ int myLength;         /* length excluding first 6 bytes */

    abstract void dump(ByteArray ba);

}


        
