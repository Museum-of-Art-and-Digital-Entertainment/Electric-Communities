package ec.tables;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.lang.reflect.Array;


/**
 * 
 */
/*package*/ class ScalarColumn extends Column {

    private Object myArray;

    /*package*/ ScalarColumn(Class memberType, int numSlots) {
        this(Array.newInstance(memberType, numSlots));
    }

    private ScalarColumn(Object array) {
        super();
        myArray = array;
    }

    /*package*/ int numSlots() {
        return Array.getLength(myArray);
    }

    /*package*/ Object get(int slot) {
        return Array.get(myArray, slot);
    }

    /*package*/ void put(int slot, Object value) {
        Array.set(myArray, slot, value);
    }

    /*package*/ void vacate(int slot) {
        //don't need to do anything
    }

    /*package*/ Class memberType() {
        return myArray.getClass().getComponentType();
    }

    protected Object clone() {
        int len = numSlots();
        Object array = Array.newInstance(memberType(), len);
        System.arraycopy(myArray, 0, array, 0, len);
        return new ScalarColumn(array);
    }

    /*package*/ Column newVacant(int numSlots) {
        return new ScalarColumn(memberType(), numSlots);
    }
}
