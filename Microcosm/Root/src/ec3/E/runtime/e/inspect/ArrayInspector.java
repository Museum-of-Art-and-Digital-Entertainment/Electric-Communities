package ec.e.inspect;

import java.lang.reflect.Array;
import java.util.*;
import ec.e.openers.*;


/* Copyright 1997 Electric Communities. All rights reserved.
 * By KJD 970401
 */

/**
 *
 * An ArrayInspector knows how to inspect arrays of all kinds.
 *
 */

public class ArrayInspector extends Inspector {

    protected ArrayOpener myOpener; // The opener that opens inspectedObject.
    
    /**

     * Constructor.
     * @param array untrusted notNull - The array to inspect.
     * @param objectName notNull - The variable name of the array to inspect.
     * @param opener trusted notNull - An ArrayOpener that knows how to open the given
     * array argument.

     */

    ArrayInspector(Object array, String objectName, ArrayOpener opener) {
        super(array,objectName);
        myOpener = opener;
    }

    /** 

     * Methods for indexed data access. For normal (non-Hashtable and
     * non-Vector) Arrays, the index given is just used as the index
     * to the array <p>

     */

    /**

     * Return the number of fields (array elements) in the array

     */
    
    public int getNumberFields() {
        if (inspectedObject == null) return 0;
        return Array.getLength(inspectedObject);
    }

    public int[] getInheritedNumberFields() { return null; } // Arrays are just arrays

    /**

     * Return the label for a slot in the array - created as needed
     * from the index
     * @param fieldIndex - an integer, an index into the inspected array.
     * @return String trivially describing (enumerating) the array slot.

     * @note This version will never throw an
     * ArrayIndexOutOfBoundsException but its counterpart in
     * ObjectInspector will.

     */

    public String getName(int fieldIndex) throws ArrayIndexOutOfBoundsException {
        return myObjectName + " [ " + fieldIndex + " ] ";
    }
  
    /**

     * Return the declared signature for a field We ask the opener to
     * provide it - that way we don't get misled by wrapped primitive
     * data types.

     * @param fieldIndex - an integer, an index into the inspected array.

     */

    public String getDeclaredSignature(int fieldIndex)
         throws ArrayIndexOutOfBoundsException {

             return JavaUtil.signature(myOpener.elementOpener().type().getName());
    }
 
    /**
     * Return the value of an array slot in the inspected array, given
     * a field index. Returns a reference data type. Primitive data
     * types will be wrapped.
     * 
     * @param fieldIndex - The index of the array slot in the
     * inspected array that we want to retrieve the value for.
     *
     * @return Object, nullOK, untrusted - the object designated by
     * fieldIndex in the inspected array. Primitive data types are
     * wrapped into Objects.
     */

    /* 

     * XXX Note: No indication is given here to show whether a wrapped
     * primitive data type was *originally* wrapped or got wrapped by
     * the access itself - is this a problem? We can always find the
     * type of the array itself by other means so maybe it isn't;
     * Getting back an instance of Integer from an array of Objects
     * means the Integer was stored there.

     */

    public Object get(int fieldIndex)
         throws ArrayIndexOutOfBoundsException {
             return Array.get(inspectedObject,fieldIndex);
    }
  
    /**

     * Set the value of an array slot in the inspected array, given a
     * field index, and a new value.  Only works for reference data
     * types - newValues that are primitive data types should be
     * wrapped.

     * @param fieldIndex - The index of slot in the array that we want
     * to store the value in.

     * @param newValue nullOK, trusted - The new value to assign to
     * the array element with the given index. Note that this value is
     * marked as "trusted" - The inspector itself is not assuming the
     * value to be trusted but since we are adding the reference to
     * another arbitrary inspected object we are in fact relaying the
     * reference and all capabilities it embodies to the receiving
     * object. Note: Guaranteeing that we can maintain our capability
     * semantics under *inspection* is quite a daunting
     * task. Certainly the matter of trusted/untruste for newValue
     * here is of no consequence until we commonly use non-Surgeon
     * openers.

     */

    public void set(int fieldIndex, Object newValue) 
         throws ArrayIndexOutOfBoundsException {
             Array.set(inspectedObject,fieldIndex, newValue);
    }

    public void preRunOne(RtQ theQ, Object vatLock, RtRun runnable) {
    }
}

