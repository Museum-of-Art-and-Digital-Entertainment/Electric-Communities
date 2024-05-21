/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */
package ec.edoc;

class VoidCodeAttributeInfo extends AttributeInfo {

    VoidCodeAttributeInfo(ConstantPool cp) {
        
        myName = cp.createString("Code");
    }
    
    void dump(ByteArray ba) {
        
        ba.addu2(myName.getIndex());
        ba.addu4(13);
        
        ba.addu2(0); /* max_stacks */
        ba.addu2(2); /* max_locals */
        
        ba.addu4(1); /* code length */
        ba.addu1(177); /* code array - single instruction == "return" */
        
        ba.addu2(0); /* num exceptions */
        
        /*  0   1   0   10  0   0   0   6   0   1   0   0   0   4   */
        ba.addu2(0); /* attribute count */
    }
}
