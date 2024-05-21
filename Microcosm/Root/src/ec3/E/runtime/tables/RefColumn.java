package ec.tables;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.lang.reflect.Array;


/**
 * 
 */
/*package*/ class RefColumn extends Column {

    private Object[] myArray;
    private boolean myIsWeak;

    /*package*/ RefColumn(Class memberType, 
                          int numSlots,
                          boolean isWeak) {
        this((Object[])Array.newInstance(memberType, numSlots), isWeak);
    }

    private RefColumn(Object[] array, boolean isWeak) {
        super();
        myArray = array;
        myIsWeak = isWeak;
    }

    /*package*/ int numSlots() {
        return myArray.length;
    }

    /*package*/ boolean isWeak() {
        return myIsWeak;
    }

    /*package*/ Object get(int slot) {
        return myArray[slot];
    }

    /*package*/ void put(int slot, Object value) {
        myArray[slot] = value;
    }

    /*package*/ void vacate(int slot) {
        myArray[slot] = null;
    }

    /*package*/ Class memberType() {
        return myArray.getClass().getComponentType();
    }

    protected Object clone() {
        Object[] array = (Object[])Array.newInstance(memberType(), 
                                                     myArray.length);
        System.arraycopy(myArray, 0, array, 0, myArray.length);
        return new RefColumn(array, myIsWeak);
    }

    /*package*/ Column newVacant(int numSlots) {
        return new RefColumn(memberType(), numSlots, myIsWeak);
    }
}
