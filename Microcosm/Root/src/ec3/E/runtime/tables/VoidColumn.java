package ec.tables;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.lang.reflect.Array;


/**
 * 
 */
public class VoidColumn extends Column {

    private int myNumSlots;

    /*package*/ VoidColumn(int numSlots) {
        super();
        myNumSlots = numSlots;
    }

    /*package*/ int numSlots() {
        return myNumSlots;
    }

    /*package*/ Object get(int slot) {
        return null;
    }

    /*package*/ void put(int slot, Object value) {
        if (value != null) {
            throw new ArrayStoreException("only nulls allowed");
        }
    }

    /*package*/ void vacate(int slot) {
        //don't need to do anything
    }

    /*package*/ Class memberType() {
        return Void.TYPE;
    }

    protected Object clone() {
        return new VoidColumn(myNumSlots);
    }

    /*package*/ Column newVacant(int numSlots) {
        return new VoidColumn(numSlots);
    }
}
