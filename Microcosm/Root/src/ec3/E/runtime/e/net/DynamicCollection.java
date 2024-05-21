package ec.e.net;

import java.util.Enumeration;
import java.util.Vector;

/**
 * A collection of objects which can be iterated through while being added to.
 * This collection is like a set in that a given object can only be in the
 * collection once. It is unlike a set in that is ordered.
 */
public class DynamicCollection {
    private Vector myElems;
    private int myCurrentLength;

    /**
     * Create a new dynamic collection.
     *
     * @param elems The objects which are to be the initial members of the
     *  collection.
     */
    public DynamicCollection(Object elems[]) {
        myCurrentLength = 0;
        myElems  = new Vector();
        addElems(elems);
    }

    /**
     * Add new objects to the collection.
     *
     * @param elems The objects which are to be added to the collection.
     */
    public void addElems(Object elems[]) {
        addElemsAt(elems, 0);
    }

    /**
     * Add new objects to the collection at a specific location.
     *
     * @param elems The objects which are to be added to the collection.
     */
    /*package*/void addElemsAt(Object elems[], int loc) {
        int newMaxLength = myElems.size() + elems.length;
        for (int i=0; i<elems.length; ++i) {
            Object newElem = elems[i];
            if (!alreadyHaveIt(newElem)) {
                myElems.insertElementAt(newElem, loc++);
            }
        }
    }

    /**
     * Return an Enumeration of the elements of the collection. This
     * Enumeration will include any elements that get added to the collection
     * before the user of the Enumeration enumerates all the elements.
     */
    public DynamicCollectionEnumeration elems() {
        return new DynamicCollectionEnumeration(this);
    }

    /**
     * Return the current size of the collection.
     */
    public int length() {
        return myElems.size();
    }

    /**
     * Return the nth member of the collection.
     */
    public Object nthElem(int n) {
        return myElems.elementAt(n);
    }

    /**
     * Return true is the element is already in the collection
     */

    private boolean alreadyHaveIt(Object obj) {
        for (int j=0; j<myElems.size(); ++j) {
            if (myElems.elementAt(j).equals(obj)) {
                return true;
            }
        }
        return false;
    }
}

/**
 * An Enumeration of the elements of a DynamicCollection object.
 */
public class DynamicCollectionEnumeration implements Enumeration {
    private DynamicCollection myCollection;
    private int myCurrentPosition;

    /**
     * Construct a new Enumeration based on a given DynamicCollection.
     */
    public DynamicCollectionEnumeration(DynamicCollection collection) {
        myCollection = collection;
        myCurrentPosition = 0;
    }

    /**
     * Return true if there are any more elements remaining to be enumerated.
     */
    public boolean hasMoreElements() {
        return myCurrentPosition < myCollection.length();
    }

    /**
     * Return the next element in the collection.
     */
    public Object nextElement() {
        return myCollection.nthElem(myCurrentPosition++);
    }

    /**
     * Add elements at the current position.
     */
    public void addElems(Object elems[]) {
        myCollection.addElemsAt(elems, myCurrentPosition);
    }
}
