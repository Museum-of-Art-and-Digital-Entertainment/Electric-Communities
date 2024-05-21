package ec.ez.collect;
import java.util.Enumeration;
import java.math.BigInteger;

public class ArrayIterator implements AssociationEnumeration {

    private Object[] myArray;
    private int myNext;

    /**
     * NOTE: Does not make a copy
     */
    public ArrayIterator(Object[] array) {
        myArray = array;
        myNext = 0;
    }

    public boolean hasMoreElements() {
        return myNext < myArray.length;
    }

    public Object nextElement() {
        return myArray[myNext++];
    }

    public Object currentKey() {
        return BigInteger.valueOf(myNext - 1);
    }
}
