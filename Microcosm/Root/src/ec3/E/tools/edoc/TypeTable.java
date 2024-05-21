/*
 *  Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 *  Rob Kinninmont, April 97
 */
 
package ec.edoc;

import java.util.Hashtable;

/** This class defines a hashtable which stores a mapping from simple types
 *  to fully qualified types.  
 *
 *  XXX (RAK) This will need to be exteded to deal with import package; 
 *  statements - it currently only works with import package.class;
 */
class TypeTable {
  
    private Hashtable myTable;

    // we _really_ don't want to build this for everyfile we load...
    static private Hashtable JavaLangTable;

    static {
        JavaLangTable = new Hashtable(211); // loading_limit = 0.75
    
        JavaLangTable.put("boolean", "boolean");
        JavaLangTable.put("char", "char");
        JavaLangTable.put("byte", "byte");
        JavaLangTable.put("short", "short");
        JavaLangTable.put("int", "int");
        JavaLangTable.put("long", "long");
        JavaLangTable.put("float", "float");
        JavaLangTable.put("double", "double");

        JavaLangTable.put("Object", "java.lang.Object");
        JavaLangTable.put("Boolean", "java.lang.Boolean");
        JavaLangTable.put("Character", "java.lang.Character");
        JavaLangTable.put("Class", "java.lang.Class");
        JavaLangTable.put("ClassLoader", "java.lang.ClassLoader");
        JavaLangTable.put("Compiler", "java.lang.Compiler");
        JavaLangTable.put("Math", "java.lang.Math");
        JavaLangTable.put("Number", "java.lang.Number");
        JavaLangTable.put("Double", "java.lang.Double");
        JavaLangTable.put("Float", "java.lang.Float");
        JavaLangTable.put("Integer", "java.lang.Integer");
        JavaLangTable.put("Long", "java.lang.Long");
        JavaLangTable.put("Process", "java.lang.Process");
        JavaLangTable.put("Runtime", "java.lang.Runtime");
        JavaLangTable.put("SecurityManager", "java.lang.Runtime");
        JavaLangTable.put("String", "java.lang.String");
        JavaLangTable.put("StringBuffer", "java.lang.StringBuffer");
        JavaLangTable.put("System", "java.lang.System");
        JavaLangTable.put("Thread", "java.lang.Thread");
        JavaLangTable.put("ThreadGroup", "java.lang.ThreadGroup");
        JavaLangTable.put("Throwable", "java.lang.Throwable");
        JavaLangTable.put("Error", "java.lang.Error");
        JavaLangTable.put("Runnable", "java.lang.Runnable");
        JavaLangTable.put("Cloneable", "java.lang.Cloneable");
        JavaLangTable.put("ClassCircularityError", 
                          "java.lang.ClassCircularityError");
        JavaLangTable.put("ClassFormatError", "java.lang.ClassFormatError");
        JavaLangTable.put("IncompatibleClassChangeError", 
                          "java.lang.IncompatibleClassChangeError");
        JavaLangTable.put("AbstractMethodError", 
                          "java.lang.AbstractMethodError");
        JavaLangTable.put("IllegalAccessError", "java.lang.IllegalAccessError");
        JavaLangTable.put("InstantiationError", "java.lang.InstantiationError");
        JavaLangTable.put("NoSuchFileError", "java.lang.NoSuchFileError");
        JavaLangTable.put("NoSuchMethodError", "java.lang.NoSuchMethodError");
        JavaLangTable.put("NoClassDefFoundError", 
                          "java.lang.NoClassDefFoundError");
        JavaLangTable.put("UnsatifiedLinkError", 
                          "java.lang.UnsatifiedLinkError");
        JavaLangTable.put("VerifyError", "java.lang.VerifyError");
        JavaLangTable.put("ThreadDeath", "java.lang.ThreadDeath");
        JavaLangTable.put("VirtualMachineError", 
                          "java.lang.VirtualMachineError");
        JavaLangTable.put("InternalError", "java.lang.InternalError");
        JavaLangTable.put("OutOfMemoryError", "java.lang.OutOfMemoryError");
        JavaLangTable.put("StackOverflowError", "java.lang.StackOverflowError");
        JavaLangTable.put("UnknownError", "java.lang.UnknownError");
        JavaLangTable.put("Exception", "java.lang.Exception");
        JavaLangTable.put("ClassNotFoundException", 
                          "java.lang.ClassNotFoundException");
        JavaLangTable.put("CloneNotSupportedException", 
                          "java.lang.CloneNotSupportedException");
        JavaLangTable.put("IllegalAccessException", 
                          "java.lang.IllegalAccessException");
        JavaLangTable.put("InstantiationException", 
                          "java.lang.InstantiationException");
        JavaLangTable.put("InterruptedException", 
                          "java.lang.InterruptedException");
        JavaLangTable.put("NoSuchMethodException", 
                          "java.lang.NoSuchMethodException");
        JavaLangTable.put("RuntimeException", "java.lang.RuntimeException");
        JavaLangTable.put("ArithmeticException", 
                          "java.lang.ArithmeticException");
        JavaLangTable.put("ArrayStoreException", 
                          "java.lang.ArrayStoreException");
        JavaLangTable.put("ClassCastException", "java.lang.ClassCastException");
        JavaLangTable.put("IllegalArgumentException", 
                          "java.lang.IllegalArgumentException");
        JavaLangTable.put("IllegalThreadStateException", 
                          "java.lang.IllegalThreadStateException");
        JavaLangTable.put("NumberFormatException", 
                          "java.lang.NumberFormatException");
        JavaLangTable.put("IllegalMonitorStateException", 
                          "java.lang.IllegalMonitorStateException");
        JavaLangTable.put("IndexOutOfBoundsException", 
                          "java.lang.IndexOutOfBoundsException");
        JavaLangTable.put("ArrayIndexOutOfBoundsException", 
                          "java.lang.ArrayIndexOutOfBoundsException");
        JavaLangTable.put("StringIndexOutOfBoundsException", 
                          "java.lang.StringIndexOutOfBoundsException");
        JavaLangTable.put("NegativeArraySizeException", 
                          "java.lang.NegativeArraySizeException");
        JavaLangTable.put("NullPointerException", 
                          "java.lang.NullPointerException");
        JavaLangTable.put("SecurityException", "java.lang.SecurityException");
        
        JavaLangTable.put("EString", "ec.e.lang.EString");
        JavaLangTable.put("EInteger", "ec.e.lang.EInteger");
        JavaLangTable.put("ELong", "ec.e.lang.ELong");
        JavaLangTable.put("EFloat", "ec.e.lang.EFloat");
        JavaLangTable.put("EDouble", "ec.e.lang.EDouble");
        
        JavaLangTable.put("EBoolean", "ec.e.run.EBoolean");
        JavaLangTable.put("EChannel", "ec.e.run.EChannel");
        JavaLangTable.put("EDistributor", "ec.e.run.EDistributor");
        JavaLangTable.put("EFalse", "ec.e.run.EFalse");
        JavaLangTable.put("ENull", "ec.e.run.ENull");
        JavaLangTable.put("EObject", "ec.e.run.EObject");
        JavaLangTable.put("EPrintStream", "ec.e.run.EPrintStream");
        JavaLangTable.put("ESealer", "ec.e.run.ESealer");
        JavaLangTable.put("ETrue", "ec.e.run.ETrue");
        JavaLangTable.put("EUnsealer", "ec.e.run.EUnsealer");
        JavaLangTable.put("IBoolean", "ec.e.run.IBoolean");
        JavaLangTable.put("RtClock", "ec.e.run.RtClock");
        JavaLangTable.put("RtClockTerminator", "ec.e.run.RtClockTerminator");
        JavaLangTable.put("RtEException", "ec.e.run.RtEException");
        JavaLangTable.put("RtEnvelope", "ec.e.run.RtEnvelope");
        JavaLangTable.put("RtTimer", "ec.e.run.RtTimer");
        JavaLangTable.put("RtDecoder", "ec.e.run.RtDecoder");
        JavaLangTable.put("RtEncoder", "ec.e.run.RtEncoder");
        JavaLangTable.put("RtTickHandling", "ec.e.run.RtTickHandling");
        JavaLangTable.put("RtTimeoutHandling", "ec.e.run.RtTimeoutHandling");
        
    }


    TypeTable() {
        myTable = (Hashtable) JavaLangTable.clone();
    }

    void putAnImport(String FQname) {
        //System.out.println("Adding import " + FQname);
        /* XXX might want to include import package; syntax here */
        int index;
        index = FQname.lastIndexOf('.');
        if (index >= 0) {
            myTable.put(FQname.substring(index + 1, FQname.length()), FQname);
            //System.out.println(" +-> As " + FQname.substring(index + 1, 
            //  FQname.length()) + " maps to " + FQname);
        } else {
            System.err.println("Warning: Ignoring invalid import statement;\n"+
                              FQname);
        }
    }

    String get(String type) {
        return (String) myTable.get(type);
    }

    private String currentPackage = null;
  
    void setCurrentPackage(String p) {
        if (p == null) {
          currentPackage = "";
        } else {
            currentPackage = p;
        }
    }
  

    /** This method is used to confirm that a type exists in the table, and can 
     *  be used as a sanity check, or to add new types to the table.
     *  It returns the fully qualified name of the type.
     *  It ignores arrays for the purposes of creating new type entries in the 
     *  table, but accounts for them again in the return value.
     */
    String validate(String typeName) {

        //System.out.println("Called to validate : " + typeName);
  
        if (typeName.indexOf('.') >= 0) {
            return typeName;
        }
  
        int brackets = 0;
        while (typeName.endsWith("[]")) {
            brackets++;
            typeName = typeName.substring(0, typeName.length() - 2);
        }

        String ret = (String) myTable.get(typeName);
        if (ret == null) {
            //System.out.println("Failed to find "+typeName+" in TypeTable");
            if (currentPackage == null) {
                System.err.println("Warning; No package has been declared");
                currentPackage = "";
            }
            if (currentPackage.equals("")) {
                ret = typeName;
            } else {
                ret = currentPackage + "." + typeName;
            }
            myTable.put(typeName, ret);
        }

        if (brackets > 0) {
            StringBuffer sb = new StringBuffer(ret);
            while (brackets > 0) {
                sb.append("[]");
                brackets--;
            }
            ret = sb.toString();
        }

        return ret;
    }

    void debug() {
        java.util.Enumeration e = myTable.elements();
        while (e.hasMoreElements()) {
            getInternalName((String) e.nextElement());
        }
    }

    static String getInternalName(String type) {
    
        StringBuffer sb = new StringBuffer();

        while (type.endsWith("[]")) {
            sb.append('[');
            type = type.substring(0, type.length() - 2);
        }

        if (type.equals("byte")) {
            sb.append("B");
        } else if (type.equals("char")) {
            sb.append("C");
        } else if (type.equals("double")) {
            sb.append("D");
        } else if (type.equals("float")) {
            sb.append("F");
        } else if (type.equals("int")) {
            sb.append("I");
        } else if (type.equals("long")) {
            sb.append("J");
        } else if (type.equals("short")) {
            sb.append("S");
        } else if (type.equals("boolean")) {
            sb.append("Z");
        } else if (type.equals("void")) {  //return type only
            sb.append("V");
        } else {  // we have a class
            sb.append("L");
            sb.append(type.replace('.', '/'));
            sb.append(";");
        }

        return sb.toString();
    }
        
    /** This method takes an internal type string, and returns a vector 
     *  containing the return type in [0], and parameter types from [1]
     *  onwards.
     */
    static java.util.Vector getExternalMethodTypes(String type) {
    
        //System.out.println("in " + type);
    
        java.util.Vector ret = new java.util.Vector();
        
        if (!type.startsWith("(")) {
            System.out.println("Wow. Didn't get a method type in get meth type");
            return null;
        }

        /* Return type is everything after the ')' */
        ret.addElement(getExternalType(
            type.substring(type.lastIndexOf(')') + 1)));
        //System.out.println("ret" + ret.elementAt(ret.size() - 1));
        
        /* We'll now iterate over the remainder of the string, taking off
         * a type at a time and adding it to the vector. */
        String subtype = type.substring(1);
        while (subtype.charAt(0) != ')') {
            
            //System.out.println("   " + subtype);
            
            int p = 0;
            while (subtype.charAt(p) == '[') {
                p++;
            }
            
            if (subtype.charAt(p) != 'L') {
                /* then it's a simple type */
                ret.addElement(getExternalType(subtype.substring(p, p+1)));
                //System.out.println("  -" + ret.elementAt(ret.size() - 1));
                subtype = subtype.substring(p+1);
            } else {
                /* it's a class */
                int tmp = subtype.indexOf(';') + 1;
                ret.addElement(getExternalType(subtype.substring(0, tmp)));
                //System.out.println("  -" + ret.elementAt(ret.size() - 1));
                subtype = subtype.substring(tmp);
            }
        }
        return ret;
    }
            
        
    /** This method takes an internal type rep, and returns a string denoting
     *  that type. 
     *
     *  Assumed precondition; 'type' is not a method. if this is the case then
     *  you'll just get an empty string back.
     */
    static String getExternalType(String type) {
    
        int p = 0;
        while ( p < type.length() && type.charAt(p) == '[' ) {
            p++;
        }
        
        StringBuffer sb = new StringBuffer();
    
        char c = type.charAt(p);
        
        switch (c) {
        case 'B':
            sb.append("byte");
            break;
        case 'C':
            sb.append("char");
            break;
        case 'D':
            sb.append("double");
            break;
        case 'F':
            sb.append("float");
            break;
        case 'I':
            sb.append("int");
            break;
        case 'J':
            sb.append("long");
            break;
        case 'S':
            sb.append("short");
            break;
        case 'Z':
            sb.append("boolean");
            break;
        case 'V':      
            sb.append("void");
            break;
        case 'L':
            sb.append(type.substring(p+1, type.length()-1).replace('/', '.'));
            break;
        }
        
        while (p-- > 0) {
            sb.append("[]");
        }           
        
        return sb.toString();
    }


}
