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
class ExceptionsAttributeInfo extends AttributeInfo {

    /*u2*/ ConstPoolInfo myName; /* pointer to UTF for name of attrib */
    /*u4*/ int myLength;         /* length excluding first 6 bytes */

    private int[] myList;
    private int myCount;
    
    ExceptionsAttributeInfo(ConstantPool cp, java.util.Vector throwsList) {
        
        myName = cp.createString("Exceptions");
        
        /* myCount is the number of excptions in the list */
        myCount = throwsList.size();
        
        /* myLength is the number of bytes, of the attribute. */
        myLength = 2 + 2 * myCount; 
        myList = new int[myCount];
        
        for (int i = 0; i < myCount; i++) {
            String name = (String) throwsList.elementAt(i);
            name = TypeTable.getInternalName(name);
            name = name.substring(1, name.length() -1);
            myList[i] = cp.createClass(name).getIndex();
        }
        
        //System.out.println("MY length = " + myLength + " and i should be " 
        //  +throwsList.elementAt(0));
    } 
    
    void dump(ByteArray ba) {
        //System.out.println("dumping " + myName.getIndex()+" "+myLength
        //  +" "+myCount);
        ba.addu2(myName.getIndex());
        ba.addu4(myLength);
        ba.addu2(myCount);
        for (int i = 0; i < myCount; i++) {
            ba.addu2(myList[i]);
        }
    }

}
