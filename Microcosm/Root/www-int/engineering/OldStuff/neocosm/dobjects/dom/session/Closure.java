/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/Closure.java $
    $Revision: 1 $
    $Date: 1/8/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import java.lang.reflect.*;
import java.io.*;

/**
 * Class to represent an arbitrary closure, or code block.  Uses CRAPI
 * to do things like a) get a String name for a given class; b) getting a
 * Class object for a given name; c) given an array of Objects that
 * represent the parameter values for the Closure, get the types of those
 * objects; d) look for a given Method (with given Class, String methodName,
 * and array of Object parameters), on a target object (provided at execution
 * time).
 *
 * TODO:  Doesn't currently search inheritance hierarchy to execute a given
 * Closure...Let's see if that's necessary.
 *
 * @author Scott Lewis
 */
public class Closure implements Serializable {

    private static Class nullArgClasses[] = new Class[0];
    private static Object nullParams[] = new Object[0];

    protected String myClassName = null;
    protected String myMethodName = null;
    protected Object myArgs[] = null;

    /* Static utility methods */

    /**
     * Utility method for Closure class to get the Class object for a given
     * fully-qualified classname (e.g. java.util.Vector)
     *
     * @param name the class name to get
     * @return Class the class found corresponding to this name.  Will use whatever
     * classloader was used to load this Closure class.  Returns null if name is null.
     * @exception ClassNotFoundException thrown if class not found
     */
    public static Class getClassForName(String name) throws ClassNotFoundException
    {
        if (name == null) return null;
        return Class.forName(name);
    }

    /**
     * Utility method for Closure class to get the fully qualified class name
     * for a given Class object.
     *
     * @param clazz the Class object to get the name for
     * @return String the fully qualified name for the class
     */
    public static String getNameForClass(Class clazz)
    {
        return clazz.getName();
    }

    /**
     * Utility methods to get an array of Class representing the object types
     * for our arguments.
     *
     * @param args an array of objects that are the arguments.
     * @return Class[] the array of class object that represent the arg *types* for
     * the provided args.
     */
    public static Class[] getArgTypesFromArgs(Object args[])
    {
        Class argTypes[] = null;
        if (args == null || args.length == 0) argTypes = nullArgClasses;
        else {
            argTypes = new Class[args.length];
            for(int i=0; i < args.length; i++) {
                if (args[i] == null) argTypes[i] = null;
                else argTypes[i] = args[i].getClass();
            }
        }
        return argTypes;
    }

    /**
     * Utility to find a method on this object with a given method name and
     * set of argument types.  Doesn't currently do search on superclasses,
     * this will be added if required.
     *
     * @param theClass the class to look on
     * @param methName a String representation of a method name (case sensitive)
     * @param args an Object array containing the object types for the parameters of
     * the desired method
     * @return Method the Method object representing the method found.  Returns null
     * if no matching method found on this object.
     */
    public static Method findMethodOnClass(Class theClass, String methName, Class argTypes[])
    {
        Method targetMethod = null;
        try {
            // Use CRAPI to get the Method object on the given Class
            targetMethod = searchForTarget(theClass.getDeclaredMethods(), methName, argTypes);
        } catch (SecurityException e1) {
            // The local security policy should dictate that this will not
            // occur, but if it does, we act as if the method doesn't
            // exist and return null
            // The next line is for debugging purposes...eventually this should be
            // removed
            e1.printStackTrace();
            return null;
        }
        return targetMethod;
    }
    
    /**
     * Given a starting class (first parameter), search the entire superclass chain
     * for a method with the given name and with the same arity as the argTypes array
     * parameter.
     *
     * @param theClass the leaf class to start searching from
     * @param methName the method name of the method to find
     * @param argTypes the Class [] to use to find a method (only uses arity) to do search
     */
    public static Method findMethodOnClassRecursive(Class theClass, String methName, Class argTypes[])
    {
        Method aMethod = findMethodOnClass(theClass, methName, argTypes);
        if (aMethod == null) {
            Class superClass = theClass.getSuperclass();
            if (superClass != null) {
                return findMethodOnClassRecursive(superClass, methName, argTypes);
            } else {
                return null;
            }
        } else {
            return aMethod;
        }
    }

    /**
     * Search given array of Method objects (first param) for the first one with
     * the (second param) given method name and the same *arity* as the (third param)
     * given array of types.
     *
     * @param arr a Method array to search 
     * @param methName the String method name of the method to search for
     * @parma argTypes the Class [] of to use to look for a method name of the same
     * arity
     * @return Method the first method instance found in arr that meets the given
     * search criteria.  Returns null if no matching method found.
     */
    public static Method searchForTarget(Method arr[], String methName, Class argTypes[])
    {
        // Find it from among the given set of Method objects
        for (int i=0; i < arr.length; i++) {
            Method testMeth = arr[i];
            // First check for method name match
            if (testMeth.getName().equals(methName)) {
                // Then check on the arity of arguments.  This could be
                // expanded to do a full check on the argument types...but
                // may not be appropriate...we'll see
                if (testMeth.getParameterTypes().length == argTypes.length) return testMeth;
            }
        }
        return null;
    }

    /**
     * Get an empty object array to use for closures with no arguments
     *
     * @return Object[] with no elements
     */
    public static Object[] getObjectArrayForNoParams()
    {
        return nullParams;
    }

    /**
     * Get an object array from a given Object.  This is a utility method
     * to aid in converting parameters to Object arrays for CRAPI.
     *
     * @param obj the Object to put into array at position 0
     * @return Object[] containing one element in array
     */
    public static Object[] getObjectArrayFromParam(Object obj)
    {
        Object arr[] = new Object[1];
        arr[0] = obj;
        return arr;
    }

    /**
     * Get an object array from a given Objects.  This is a utility method
     * to aid in converting parameters to an Object array for CRAPI.
     *
     * @param Object to put into array at position 0
     * @param Object to put into array at position 1
     * @return Object[] containing two elements
     */
    public static Object[] getObjectArrayFromParams(Object obj0, Object obj1)
    {
        Object arr[] = new Object[2];
        arr[0] = obj0; arr[1] = obj1;
        return arr;
    }

    /**
     * Get an object array from a given Objects.  This is a utility method
     * to aid in converting parameters to an Object array for CRAPI.
     *
     * @param obj0 the Object to put into array at position 0
     * @param obj1 the Object to put into array at position 1
     * @param obj2 the Object to put into array at position 2
     * @return Object[] containing three elements
     */
    public static Object[] getObjectArrayFromParams(Object obj0,
                                                    Object obj1,
                                                    Object obj2)
    {
        Object arr[] = new Object[3];
        arr[0] = obj0; arr[1] = obj1; arr[2] = obj2;
        return arr;
    }

    /**
     * Get an object array from a given Objects.  This is a utility method
     * to aid in converting parameters to an Object array for CRAPI.
     *
     * @param obj0 the Object to put into array at position 0
     * @param obj1 the Object to put into array at position 1
     * @param obj2 the Object to put into array at position 2
     * @param obj3 the Object to put into array at position 3
     * @return Object[] containing three elements
     */
    public static Object[] getObjectArrayFromParams(Object obj0,
                                                    Object obj1,
                                                    Object obj2,
                                                    Object obj3)
    {
        Object arr[] = new Object[4];
        arr[0] = obj0; arr[1] = obj1; arr[2] = obj2; arr[3] = obj3;
        return arr;
    }

    /**
     * Check the given closure to verify that the arguments are all
     * all Serializable.  If not, this will throw a NotSerializableException
     *
     * @param closure the Closure to check out
     * @exception NotSerializableException thrown if any one of the parameters
     * of the given Closure do not implement Serializable
     */
    public static void checkParamsForSerializable(Closure closure)
        throws NotSerializableException
    {
        Object arr[] = closure.myArgs;
        if (arr != null) {
            for(int i=0; i < arr.length; i++) {
                if (!(arr[i] instanceof Serializable) && arr[i]!=null)
                    throw new NotSerializableException("Param "+i+" not Serializable");
            }
        }
    }

    /** Instance constructors **/

    /**
     * Null constructor.  Should only be used if the values for this class are
     * set via setClassName(), setMethodName(), setArgs().
     */
    public Closure()
    {
        myClassName = null;
        myMethodName = null;
        myArgs = null;
    }

    /**
     * Normal constructor
     *
     * @param aClass the class that has the code block to execute
     * @param methodName the method name to execute
     * @param args the array of object arguments for this closure
     */
    public Closure(Class aClass, String methodName, Object args[])
    {
        this((aClass == null)?((String) null):getNameForClass(aClass), methodName, args);
    }

    /**
     * Alternative constructor.  Takes class name as string.
     *
     * @param className the fully qualified name of the class (e.g. java.util.Vector)
     * @param methodName the methodName to invoke on the desired class
     * @param args the array of Object arguments to pass to the block on
     * invokation
     */
    public Closure(String className, String methodName, Object args[])
    {
        myClassName = className;
        myMethodName = methodName;
        myArgs = args;
    }

    /**
     * Get arg types for our arguments.
     *
     * @return Class[] the array of class objects that represent the arg *types* for
     * this object's args.
     */
    protected Class[] getArgTypes()
    {
        return getArgTypesFromArgs(myArgs);
    }

    /**
     * Find the method object corresponding to our closure.  Calls
     * getDeclaredMethod on class corresponding to the class name provided
     * for this closure.
     *
     * @param aClass the Class of the target object.
     * @return Method the method object found (if any) using CRAPI
     * @exception Exception thrown if class not found, or desired method not found
     */
    protected Method findMethodForClosure(Class aClass) throws Exception
    {
        return findMethodOnClass(aClass, getMethodName(), getArgTypes());
    }

    /**
     * Find the method object corresponding to our closure.  Calls
     * getDeclaredMethod on class corresponding to the class name provided
     * for this closure.  This version recursively looks on the given class
     * and all of that class's superclasses.
     *
     * @param aClass the Class of the target object.
     * @return Method the method object found (if any) using CRAPI
     * @exception Exception thrown if class not found, or desired method not found
     */
    protected Method findMethodForClosureRecursive(Class aClass) throws Exception
    {
        return findMethodOnClassRecursive(aClass, getMethodName(), getArgTypes());
    }

    /**
     * Coerce the target object for the given method in any way desired.
     *
     * @param target the Object that the Method will be invoked on
     * @param toExecute the Method to invoke
     * @return Object that represents the target object after coersion
     * @exception Exception thrown if any problems coercing the target
     */
    protected Object coerceTarget(Object target, Method toExecute) throws Exception
    {
        // Might want to do some coersion of target object...can't
        // think of any right now.
        return target;
    }

    /**
     * Coerce the arguments for this Closure.
     *
     * @return Object [] that is the coerced version of the arguments for this
     * Closure
     * @exception Exception thrown if some problem doing coersion
     */
    protected Object [] coerceArgs() throws Exception
    {
        // Might again want to do something to coerce args, but can't
        // think of what that might be right now
        return myArgs;
    }

    /**
     * Method to actually execute this closure on the given target Object.  This
     * method is declared final as subclasses should always use this method
     * to execute themselves on a given target object.
     *
     * @param target the Object to execute this Closure on
     * @exception Exception thrown if some problem invoking method on given
     * target.
     * @see java.lang.reflect.Method#invoke
     */
    public final void executeThisClosure(Object target) throws Exception
    {
        // First verify that we have a valid target object.  If not, throw
        if (target == null) throw new NoSuchMethodException();
        // If the class name specified for this Closure is null, it means
        // that the lookup is to be done on the target object's class directly.
        // This lookup is done recursively, checking all of the target's superclasses.
        
        // If the classname associated with this closure is non-null, then the
        // specified class name is used, and the search for the method is limited to
        // the given Class
        Method toExecute = null;
        if (getClassName() == null) {
            toExecute = findMethodForClosureRecursive(target.getClass());
        } else {
            toExecute = findMethodForClosure(getDeclaringClass());
        }
        // If method not found, then throw
        if (toExecute == null) throw new NoSuchMethodException();
        
        // System.out.println("Executing: "+this+" on "+target);
        
        // Else actually invoke method found after coercing target and args (coerceTarget
        // and coerceArgs don't do anything right now, but are just present for generality)
        toExecute.invoke(coerceTarget(target, toExecute), coerceArgs());
    }

    /**
     * Get Class object for this Closure.
     *
     * @return Class the actual local class for this closure
     * @exception ClassNotFoundException thrown if class cannot be found
     */
    public final Class getDeclaringClass() throws ClassNotFoundException
    {
        return getClassForName(myClassName);
    }

    /**
     * Get this Closure's class name.
     *
     * @return String the class name for this Closure
     */
    public final String getClassName()
    {
        return myClassName;
    }

    /**
     * Get this Closure's method name.
     *
     * @return String the method name for this Closure
     */
    public final String getMethodName()
    {
        return myMethodName;
    }

    /**
     * Get this Closure's array of arguments.
     *
     * @return Object[] the objects that are the arguments for this Closure
     */
    public final Object[] getArgs()
    {
        return myArgs;
    }

    /**
     * Set this Closure's class name.
     *
     * @param className the class name to use for this Closure.
     */
    public final void setClassName(String className)
    {
        myClassName = className;
    }

    /**
     * Set this Closure's method name.
     *
     * @param methodName the method name to use for this Closure.
     */
    public final void setMethodName(String methodName)
    {
        myMethodName = methodName;
    }

    /**
     * Set this Closure's arguments array.
     *
     * @param args the Object[] of arguments for this Closure.
     */
    public final void setArgs(Object args[])
    {
        myArgs = args;
    }
    
    /**
     * Utility method to display closure contents
     *
     * @return String representing this Closure's contents
     */
    public String toString()
    {
        String nameStr = (myClassName==null)?("(no classname)"):myClassName;
        nameStr += "."+myMethodName+"(";
        if (myArgs == null) nameStr = nameStr+"):0";
        else {
            for(int i=0; i < myArgs.length; i++) {
                nameStr = nameStr+myArgs[i];
                if (i < myArgs.length-1) nameStr = nameStr+",";
            }
            nameStr = nameStr + "):"+myArgs.length;
        }
        return nameStr;
    }
}