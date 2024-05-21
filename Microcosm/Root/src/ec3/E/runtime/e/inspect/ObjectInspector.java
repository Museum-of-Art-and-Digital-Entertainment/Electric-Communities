package ec.e.inspect;
import java.util.*;
import ec.e.openers.*;

/* Copyright 1997 Electric Communities. All rights reserved.
 * By KJD 970401
 */

/**

 * An ObjectInspector knows how to inspect Bona Fide Objects.  It
 * *can* be used to inspect Hashtables, Strings, Vectors and other
 * special-cased objects but normally we want to use special
 * inspectors for these - A good example is hashtables where we want
 * to see key-value pairs in an array, and are not interested in
 * seeing empty slots in an oversized array.  <p>

 * Arrays of all kinds are inspected using ArryInspector's - An
 * ObjectInspector cannot inspect Arrays at all.

 */

public class ObjectInspector extends Inspector {

    protected Surgeon  myOpener; // The opener that opens inspectedObject.
    protected String[] slotNames = null; // Array of names of instance variables
    protected int numberSlots = 0; // Number of OK (visible) instance variables
    protected int MAX_SUPERCLASSES = 100; // We lose interest at this superclass depth.

    /**
     * Main constructor.

     * @param object notNull suspect - The object to inspect.

     * @param objectName notNull trusted - A name for the object, typically
     * the name of a variable containing the object.

     * @param opener notNull trusted - An opener, currently always a
     * Surgeon but we hope to limit the inspecting capabilities to
     * subsets of a user's inspectable objects (or parts of the
     * objects) by using other openers later.

     */

    ObjectInspector(Object object, String objectName, ObjOpener opener) {
        super(object,objectName);
        myOpener = (Surgeon)opener;
        slotNames = (String[])opener.commentary();
        if (slotNames != null) numberSlots = slotNames.length;
    }

    /**

     * Methods for indexed data access. For Objects, the index is the
     * index of the instance variable accessible through the current
     * opener. If some variables are unavailable through this opener,
     * then the opener simply pretends they don't exist. <p>

     */

    /**

     * Return the number of fields (instance variables) in the object

     */

    public int getNumberFields() {
        return myOpener.numInstVars();
    }

    /**

     * Return an array of int that enumerates how many instance
     * variables are visible at each level of the class inheritance
     * chain when using this opener. Level 0 (at index 0) is the
     * current class.

     */

    public int[] getInheritedNumberFields() {
        int[] numbers = new int[MAX_SUPERCLASSES];
        int i;
        int j;
        Surgeon ope = myOpener;

        for (i = 0;
             ((ope != null) && (i < MAX_SUPERCLASSES));
             i++, ope = (Surgeon)ope.getSuper()) { // XXX This typecast feels unnatural
            numbers[i] = ope.numInstVars();
        }

        /* Each slot in numbers[], up to (i - 1), now contains the
           number of instance variables from there on up through the
           chain. We want to convert to number of vars at each level
           so we subtract each one form the previous one: */

        numbers[i] = 0;
        int[] result = new int[i];
        i--;                    // Adjust for extra increment in loop

        for (j=0; i >= 0; j++, i--) {
            result[j] = numbers[i] - numbers[i + 1]; // Differentiate
        }
        return result;
    }

    /**

     * Return the class name for the nth superclass.

     * @param superlevel - The level of superclass we wat the name
     * for. 0 returns the name of the current class, 1 that of its
     * immediate superclass (if any) etc.

     */

    public String getInheritedClassName(int superLevel) {
        Class clazz = myClazz;
        while ((clazz != null) && (superLevel-- > 0))
            clazz = clazz.getSuperclass();
        if (clazz != null) return clazz.getName();
        else return null;
    }

    /**

     * Return the variable name of a field. Invent a last-resort
     * answer of style "<n>" in case something is wrong with the
     * commentary.

     * @param fieldIndex - an index used to identify the wanted
     * instance variable slot.

     */

    public String getName(int fieldIndex) throws ArrayIndexOutOfBoundsException {
        if ((slotNames != null) && (fieldIndex < numberSlots)) return slotNames[fieldIndex];
        else return "<" + fieldIndex + ">";
    }

    /** Return the declared signatures for a field */
    public String getDeclaredSignature(int fieldIndex)
         throws ArrayIndexOutOfBoundsException {
             return ((Surgeon)myOpener).instVarOpener(fieldIndex).signature();
    }

    /**

     * Return the signature of the actually assigned data in a field.
     * Note that this may differ from the declared signature in some
     * cases, like if a field is declared a reference to some class of
     * object and it has been assigned an object of some subclass
     * thereof, or if the value is null (which returns the
     * pseudo-signature "N").

     * @param fieldIndex - an index used to identify the wanted
     * instance variable slot.

     */

    public String getAssignedSignature(int fieldIndex)
         throws ArrayIndexOutOfBoundsException {
             String sig = getDeclaredSignature(fieldIndex);
             if (sig.charAt(0) != 'L') return sig; // Non-Object signatures are already correct.
             Object value = get(fieldIndex); // We haved a BonaFideObject. Get its value
             if (value == null) return "N"; // Object was null
             //             System.out.println("value.getclass() is " + value.getClass());
             return JavaUtil.signature(value.getClass().getName()); // Compute signature of value itself
    }

    /**

     * Return the value of a field, given a field index. Returns a
     * reference data type. Primitive data types will be wrapped.

     * @param fieldIndex - The index of the local variable field
     * according to the used opener, or the index of the element if
     * the object being inspected is an array.

     */

    /* XXX No indication is given to show whether a wrapped primitive
     * data type was *originally* wrapped or got wrapped by the access
     * itself.  Is this a problem? Workarounds exist but do we want to
     * do something specific here? */

    public Object get(int fieldIndex)
         throws ArrayIndexOutOfBoundsException {
             return ((Surgeon)myOpener).instVarOpener(fieldIndex).get(inspectedObject);
    }

    /**

     * Set the value of a field, given a field index, a new value.
     * Only works for reference data types - primitive data types
     * should be wrapped.

     * @param fieldIndex - The index of the local variable field
     * according to the used opener, or the index of the element if
     * the object being inspected is an array.

     * @param newValue nullOK, trusted - The new value to assign to
     * the instance variable. Note that this value is marked as
     * "trusted" - The inspector itself is not assuming the value to
     * be trusted but since we are adding the reference to another
     * arbitrary inspected object we are in fact relaying the
     * reference and all capabilities it embodies to the receiving
     * object. Note: Guaranteeing that we can maintain our capability
     * semantics under *inspection* is quite a daunting
     * task. Certainly the matter of trusted/untruste for newValue
     * here is of no consequence until we commonly use non-Surgeon
     * openers.

     */

    public void set(int fieldIndex, Object newValue)
         throws ArrayIndexOutOfBoundsException {
             ((Surgeon)myOpener).instVarOpener(fieldIndex).set(inspectedObject, newValue);
    }


    public void preRunOne(RtQ theQ, Object vatLock, RtRun runnable) {
    }
}
