/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */
package ec.edoc;

/** This class is used to represent assorted structures found in the constant
 *  pool, along with appropriate mechanisms for managing the pointers and 
 *  streaming the results to a byte array.
 *
 *  XXX this should perhaps be assorted different classes. (?)
 */
class ConstPoolInfo {

    static final int _Class = 7;
    static final int _Field = 9;
    static final int _Method = 10;
    static final int _InterfaceMethod = 11;
    static final int _NameType = 12;
    static final int _String = 1;

    private int tag;
    private ConstPoolInfo name; // is class name for class / member, name for N/T
    private ConstPoolInfo type; // is N/T for member, and type for N/T
    private String string;

    private int index; /* used to store the position in the CPool */

    private ConstPoolInfo() {
    }

    int getTag() {
        return tag;
    }

    int getIndex() {
        return index;
    }

    /* CONSTANT_Utf8 */
    static ConstPoolInfo createString(int i, String s) {
        ConstPoolInfo ret = new ConstPoolInfo();
        ret.index = i;
        ret.tag = _String;
        ret.string = s;
        return ret;
    }

    /* CONSTANT_NameAndType */
    static ConstPoolInfo createNameAndType(int i, ConstPoolInfo name, 
                                           ConstPoolInfo type) {
        if (name.getTag() != name._String || type.getTag() != type._String) {
            System.err.println("Should have had a _String");
            return null;
        }
        ConstPoolInfo ret = new ConstPoolInfo();
        ret.index = i;
        ret.tag = _NameType;
        ret.name = name;
        ret.type = type;
        return ret;
    }

    /* CONSTANT_Class */
    static ConstPoolInfo createClass(int i, ConstPoolInfo name) {
        if (name.getTag() != name._String) {
            System.err.println("Should have had a _String");
            return null;
        }
        ConstPoolInfo ret = new ConstPoolInfo();
        ret.index = i;
        ret.tag = _Class;
        ret.name = name;
        return ret;
    }

    /* CONSTANT_Fieldref / Methodref / InterfaceMethodref */
    static ConstPoolInfo createField(int i, ConstPoolInfo Class, 
                                     ConstPoolInfo NameType) {
        if (Class.getTag() != Class._Class || 
                NameType.getTag() != NameType._NameType) {
            System.err.println("Should have had a _Class and _NameType");
            return null;
        }
        ConstPoolInfo ret = new ConstPoolInfo();
        ret.index = i;
        ret.tag = _Field;
        ret.name = Class;
        ret.type = NameType;
        return ret;
    }
    static ConstPoolInfo createMethod(int i, ConstPoolInfo Class, 
                                     ConstPoolInfo NameType) {
        if (Class.getTag() != Class._Class || 
                NameType.getTag() != NameType._NameType) {
            System.err.println("Should have had a _Class and _NameType");
            return null;
        }
        ConstPoolInfo ret = new ConstPoolInfo();
        ret.index = i;
        ret.tag = _Method;
        ret.name = Class;
        ret.type = NameType;
        return ret;
    }
    static ConstPoolInfo createInterfaceMethod(int i, ConstPoolInfo Class, 
                                               ConstPoolInfo NameType) {
        if (Class.getTag() != Class._Class || 
                NameType.getTag() != NameType._NameType) {
            System.err.println("Should have had a _Class and _NameType");
            return null;
        }
        ConstPoolInfo ret = new ConstPoolInfo();
        ret.index = i;
        ret.tag = _InterfaceMethod;
        ret.name = Class;
        ret.type = NameType;
        return ret;
    }


    void dump(ByteArray ba) {
    
        ba.addu1(this.tag);

        switch (this.tag) {

        case _Class:
            ba.addu2(name.getIndex());
            break;

        case _Field:
        case _Method:
        case _InterfaceMethod:  // valid for all by judicious choice of name, 
        case _NameType:         // type these structures distinguished by tag 
            ba.addu2(name.getIndex());
            ba.addu2(type.getIndex());
            break;

        case _String:      
            // addString adds the required length prefix to the string.
            ba.addString(string);
            break;
        }
    }


    public String toString() {
        if (this.tag == _String)  {
            return "UTF " + this.string;
        } else {
            String ret = "struct ";
            if (this.name != null) {
                ret = ret + "name = " + this.name.toString() + " ";
            }
            if (this.type != null) {
                ret = ret + "type = " + this.type.toString() + " ";
            }
           return ret;
        }
    }    
}
