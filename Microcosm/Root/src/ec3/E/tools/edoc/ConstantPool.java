/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;
import java.util.Enumeration;

/** This class is used to reprasent the constant pool of a class file. It 
 *  provides a mechanism to generate new entries in itself, to simplify the
 *  main application code.
 *  It then provides a method for writing out the entire pool at once, once
 *  it has been populated.
 */
class ConstantPool {

    private Vector pool;
    private int index; /* purely an optimisation - avoid pool.size() calls */

    ConstantPool() {
        pool = new Vector();
        index = 1;
        /* NB The constant pool is defined to not have an entry 0.
         * ie. 0 is reserved, but is _not_ present in the final byte stream
         * if we number from 1, but output from 0, ie. output the actual 
         * contents of pool, then we should be all right */
    }

    void dump(ByteArray ba) {
        /* index points beyond the pool, but count includes the missing 
         * nought-th element */
        ba.addu2(index);  
    
        for (Enumeration e = pool.elements(); e.hasMoreElements(); ) {
            ((ConstPoolInfo) e.nextElement()).dump(ba);
        }
    }

    ConstPoolInfo createString(String s) {
        ConstPoolInfo ret = ConstPoolInfo.createString(index, s);
        pool.addElement(ret);
        index++;
        return ret;
    }

    ConstPoolInfo createClass(ClassInterfaceInfo ci) {
        return this.createClass(ci.name());
    }

    ConstPoolInfo createClass(String name) {
        return this.createClass(this.createString(name));
    }

    ConstPoolInfo createClass(ConstPoolInfo name) {
        if (name.getTag() != name._String) {
            System.err.println("Should have had a _String");
            return null;
        }
        ConstPoolInfo ret = ConstPoolInfo.createClass(index, name);
        pool.addElement(ret);
        index++;
        return ret;
    }

    ConstPoolInfo createNameAndType(String nam, String typ) {
        ConstPoolInfo name = this.createString(nam);
        ConstPoolInfo type = this.createString(typ);
        ConstPoolInfo ret = ConstPoolInfo.createNameAndType(index, name, type);
        pool.addElement(ret);
        index++;
        return ret;
    }

    ConstPoolInfo createNameAndType(ConstPoolInfo name, ConstPoolInfo type) {
        if (name.getTag() != name._String || type.getTag() != type._String) {
            System.err.println("Should have had a _String");
            return null;
        }
        ConstPoolInfo ret = ConstPoolInfo.createNameAndType(index, name, type);
        pool.addElement(ret);
        index++;
        return ret;
    }

    ConstPoolInfo createField(ConstPoolInfo Class, 
                              ConstPoolInfo name, ConstPoolInfo type) {
        if (Class.getTag() != Class._Class || 
                name.getTag() != name._String || type.getTag() != type._String) {
          System.err.println("Should have had a _Class & _String");
          return null;
        }
        ConstPoolInfo nt = this.createNameAndType(name, type);
        ConstPoolInfo ret = ConstPoolInfo.createField(index, Class, nt);
        pool.addElement(ret);
        index++;
        return ret;
    }
    
    ConstPoolInfo createField(ConstPoolInfo Class, String name, String type) {
        return this.createField(Class, 
                                this.createString(name), 
                                this.createString(type));
    }

    ConstPoolInfo createMethod(ConstPoolInfo Class, 
                               ConstPoolInfo name, ConstPoolInfo type) {
        if (Class.getTag() != Class._Class || 
                name.getTag() != name._String || type.getTag() != type._String) {
            System.err.println("Should have had a _Class & _String");
            return null;
        }
        ConstPoolInfo nt = this.createNameAndType(name, type);
        ConstPoolInfo ret = ConstPoolInfo.createMethod(index, Class, nt);
        pool.addElement(ret);
        index++;
        return ret;
    }
    
    ConstPoolInfo createMethod(ConstPoolInfo Class, String name, String type) {
        return this.createMethod(Class, 
                                 this.createString(name), 
                                 this.createString(type));
    }

    ConstPoolInfo createInterfaceMethod(ConstPoolInfo Class, 
                                        ConstPoolInfo name, 
                                        ConstPoolInfo type) {
        if (Class.getTag() != Class._Class || 
                name.getTag() != name._String || type.getTag() != type._String) {
            System.err.println("Should have had a _Class & _String");
            return null;
        }
        ConstPoolInfo nt = this.createNameAndType(name, type);
        ConstPoolInfo ret = ConstPoolInfo.createInterfaceMethod(index, Class, nt);
        pool.addElement(ret);
        index++;
        return ret;
    }
    
    ConstPoolInfo createInterfaceMethod(ConstPoolInfo Class, 
                                        String name, String type) {
        return this.createInterfaceMethod(Class, 
                                         this.createString(name), 
                                         this.createString(type));
    }


    /*ConstPoolInfo createMember(ClassMemberInfo cmi) {

        switch (cmi.type) {

        case cmi.FIELD:
            return this.createField(this.createClass(cmi.getParent().name),
                                    cmi.name,
                                    TypeTable.getInternalName(cmi.returnType));

        case cmi.METHOD:
        case cmi.CONSTRUCTOR:
        case cmi.INTERFACEMETHOD:

            StringBuffer sb = new StringBuffer();
            sb.append('(');
            if (cmi.parameterTypes != null) {
                java.util.Enumeration et = cmi.parameterTypes.elements();
                while (et.hasMoreElements()) {
                    sb.append(TypeTable.getInternalName((String)et.nextElement()));
                }
            }
            sb.append(')');
            sb.append(TypeTable.getInternalName(cmi.returnType));
      
            if (cmi.type == cmi.INTERFACEMETHOD) { 
                return this.createInterfaceMethod(
                    this.createClass(cmi.getParent().name),
                    cmi.name, sb.toString());
            } else if (cmi.type == cmi.METHOD) { 
                return this.createMethod(this.createClass(cmi.getParent().name),
                                         cmi.name, sb.toString());
            } else if (cmi.type == cmi.STATICINITIALISER) { 
                return this.createMethod(this.createClass(cmi.getParent().name),
                                         cmi.name, sb.toString());
            } else { 
                // constructor 
                return this.createMethod(this.createClass(cmi.getParent().name),
                                         "<init>", sb.toString());
            }

        default:
            throw new RuntimeException("Can't deal with E methods yet");
        }
    }*/
}
