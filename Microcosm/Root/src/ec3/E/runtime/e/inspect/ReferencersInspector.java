package ec.e.inspect;
import java.util.*;
import ec.e.openers.*;

/* Copyright 1997 Electric Communities. All rights reserved.
 * By KJD 971027
 */

/**

 * A ReferencersInspector knows how to inspect Referencers lists. We display
 * a list of objects that have references to another object, the target object,
 * as an arry of objects <p>

 */

public class ReferencersInspector extends Inspector {
    protected int numberSlots;  // Number of entries in Vector
    private ObjOpener myOpener;
    private Object[] myObjects = null;
    private String[] myKinds = null;
    private static Object theDummy = new Integer(530220);

    /**
     * Main constructor.

     * @param object notNull suspect - The Referencers array

     * @param objectName notNull trusted - A name for the target object

     * @param opener notNull trusted - An opener, currently always a
     * Surgeon but we hope to limit the inspecting capabilities to
     * subsets of a user's inspectable objects (or parts of the
     * objects) by using other openers later. This is passed to
     * inspectors created to inspect the values in the Vector.

     */

    ReferencersInspector(TraceInfo[] referencers, String objectName, ObjOpener opener) {
        super(theDummy,objectName);
        myOpener = opener;
        Object tempObjects[] = new Object[referencers.length]; // Overestimate first
        numberSlots = 0;

        // Filter the results down to heap objects only

        for (int i = 0; i < referencers.length; i++) {
            if (referencers[i].refKind == Tracer.REF_HEAP) {
                Object o = Inspector.objectify(referencers[i].address);

                // Do further filtering - avoid inspector itself as much as possible

                //                if (o instanceof ObjectInspector) continue;
                //                if (o instanceof ReferencersInspector) continue;
                //                if (o instanceof ArrayInspectorInspector) continue;
                if (o instanceof Inspector) continue;

                // Accept this object.

                tempObjects[numberSlots++] = o;
            }
        }

        myObjects = new Object[numberSlots];
        for (int i =0; i < numberSlots; i++) myObjects[i] = tempObjects[i];
        inspectedObject = myObjects;
        myClazz = inspectedObject.getClass();
    }

    /**

     * Return the number of items in Vector.

     */

    public int getNumberFields() {
        return numberSlots;
    }

    /**

     * Trivial version of the overridden method in ObjectInspector().

     */

    public int[] getInheritedNumberFields() {
        return null;
    }

    /**

     * Return the class name for the nth superclass.
     * Trivial version of the overridden method in ObjectInspector().

     * @param superlevel - The level of superclass we wat the name
     * for. 0 returns the name of the current class, 1 that of its
     * immediate superclass (if any) etc.
     */

    public String getInheritedClassName(int superLevel) {
        return null;
    }

    /**

     * Return the variable name of a field. We construct it on the fly
     * from the <fieldIndex>

     * @param fieldIndex - an index used to identify the slot

     */

    public String getName(int fieldIndex) throws ArrayIndexOutOfBoundsException {
        return Inspector.objectName(get(fieldIndex));
    }

    /**

     * Return the declared signatures for a field.  Trivial version of
     * the overridden method in ObjectInspector().

     */

    public String getDeclaredSignature(int fieldIndex)
         throws ArrayIndexOutOfBoundsException {
             return getAssignedSignature(fieldIndex);
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
             Object value = get(fieldIndex); // We haved a BonaFideObject. Get its value
             if (value == null) return "N"; // Object was null
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
             return myObjects[fieldIndex];
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
             // A dummy
    }

    public void preRunOne(RtQ theQ, Object vatLock, RtRun runnable) {
    }
}
