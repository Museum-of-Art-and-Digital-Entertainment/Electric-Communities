/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */
package ec.edoc;

/** This class is used to hold structures for the field and methods pools
 *  of the .class file. */
class MemberPoolInfo {

    /*u2*/ int access_flags;
    /*u2*/ ConstPoolInfo name;
    /*u2*/ ConstPoolInfo type;
    /*u2*/ int attribute_count;
    AttributeInfo[] attributes;

    void dump(ByteArray ba) {
        ba.addu2(this.access_flags);
        ba.addu2(this.name.getIndex());
        ba.addu2(this.type.getIndex());
        ba.addu2(this.attribute_count);
    
        for (int i = 0; i < attribute_count; i++) {
            attributes[i].dump(ba);
        }
    }    
}
