package ec.tables;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.lang.reflect.Array;


/**
 * 
 */
/*package*/ class IntColumn extends Column {

    private int[] myArray;

    /*package*/ IntColumn(int numSlots) {
        this(new int[numSlots]);
    }

    private IntColumn(int[] array) {
        super();
        myArray = array;
    }

    /*package*/ int numSlots() {
        return myArray.length;
    }

    /*package*/ Object get(int slot) {
        return new Integer(myArray[slot]);
    }

    /*package*/ int getInt(int slot) {
        return myArray[slot];
    }

    /*package*/ void put(int slot, Object value) {
        myArray[slot] = ((Integer)value).intValue();
    }

    /*package*/ void putInt(int slot, int value) {
        myArray[slot] = value;
    }

    /*package*/ void vacate(int slot) {
        //don't need to do anything
    }

    /*package*/ Class memberType() {
        return Integer.TYPE;
    }

    protected Object clone() {
        int[] array = new int[myArray.length];
        System.arraycopy(myArray, 0, array, 0, myArray.length);
        return new IntColumn(array);
    }

    /*package*/ Column newVacant(int numSlots) {
        return new IntColumn(numSlots);
    }
}
