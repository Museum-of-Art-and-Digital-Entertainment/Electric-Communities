package ec.tables;

import java.util.Enumeration;
import java.util.NoSuchElementException;


/**
 * 
 */
public class KeyedCollection {

    /*package*/ KeyColumn myKeys;

    /*package*/ KeyedCollection(KeyColumn keys) {
        myKeys = keys;
        myKeys.registerInterest(this);
    }

    /**
     * The number of keys in the collection
     */
    public int size() {
        return myKeys.size();
    }

    /**
     * Does the collection contain any keys?
     */
    public boolean isEmpty() {
        return myKeys.size() <= 0;
    }

    /**
     * Return an enumeration of the keys in the collection.  May include a
     * null. 
     *
     * @return the enumeration
     */
    public Enumeration keys() {
        return new ColumnEnumeration(myKeys, myKeys);
    }

    /**
     * Returns true if the specified object is a key in the
     * collection, as defined by the equality function of the collection.
     *
     * @param key the object to look for
     * @return true if the key is in the collection
     */
    public boolean containsKey(Object key) {
        return myKeys.isSlotTaken(myKeys.findSlot(key));
    }
    
    /**
     * Removes the given key (or its equivalent, according to the
     * equal function) from the collection. This does nothing if the
     * object is not actually a key in the collection.  Unlike
     * Dictionary, this does not return the old value.  If you want
     * this for a Table, use 'get' first.
     *
     * @param key the key to remove
     */
    public void remove(Object key) {
        int slot = myKeys.findSlot(key);
        vacate(slot);
    }

    /**
     * 
     */
    /*package*/ void vacate(int slot) {        
        myKeys.vacate(slot);
    }

    /**
     * All keys in this collection must be of this type
     */
    public Class keyType() {
        return myKeys.memberType();
    }

    /**
     *
     */
    /*package*/ void growTo(int size) {
        //XXX
    }
}
