package ec.ez.collect;

import ec.ez.collect.NotFoundException;
import ec.util.NestedException;
import java.util.Enumeration;
import java.math.BigInteger;


/**
 * A Tuple is a Mapping whose keys are exactly the integers
 * from 0 to size()-1
 */
public class TupleImpl implements Tuple {
    static private Tuple EMPTY_TUPLE = new TupleImpl(new Object[0]);

    private Object[] myArray;

    private TupleImpl(Object[] array) {
        myArray = array;
    }

    static public Tuple make(Object[] array) {
        if (array.length == 0) {
            return EMPTY_TUPLE;
        }
        Object[] newArray = new Object[array.length];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return new TupleImpl(newArray);
    }

    static public Tuple run() {
        return EMPTY_TUPLE;
    }

    static public Tuple run(Object a0) {
        Object[] array = { a0 };
        return new TupleImpl(array);
    }

    static public Tuple run(Object a0, Object a1) {
        Object[] array = { a0, a1 };
        return new TupleImpl(array);
    }

    static public TupleImpl fromTuple(Tuple other) {
        throw new RuntimeException("not yet implemented");
    }


    /**
     * Since Tuples are immutable values, it's actually safe to
     * compare them by contents!
     */
    public boolean equals(Object other) {
        if (! (other instanceof Tuple)) {
            return false;
        }
        Tuple o = (Tuple)other;
        int len = size();
        if (len != o.size()) {
            return false;
        }
        try {
            for (int i = 0; i < len; i++) {
                if (! index(i).equals(o.index(i))) {
                    return false;
                }
            }
        } catch (NotFoundException ex) {
            throw new NestedException("internal:", ex);
        }
        return true;
    }

    public int hashCode() {
        //really sleazy & awful
        return size();
    }        

    public String toString() {
        StringBuffer s = new StringBuffer().append("[");
        if (myArray.length >= 1) {
            int last = myArray.length -1;
            for (int i = 0; i < last; i++) {
                s.append(myArray[i].toString()).append(", ");
            }
            s.append(myArray[last].toString());
        }
        return s.append("]").toString();
    }

    public int size() {
        return myArray.length;
    }

    public boolean containsKey(Object key) {
        Number n;
        try {
            n = (Number)key;
        } catch (ClassCastException ex) {
            //ignored on purpose
            return false;
        }
        int i = n.intValue();
        return 0 <= i && i < myArray.length;
    }

    public Object get(Object key) throws NotFoundException {
        return index(((Number)key).intValue());
    }

    public Object index(int key) throws NotFoundException {
        try {
            return myArray[key];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new NotFoundException("" + key);
        }
    }

    public Mapping with(Object key, Object newValue) {
        int i = ((Number)key).intValue();
        if (0 <= i && i <= myArray.length) {
            Object[] newArray = new Object[Math.max(i+1, myArray.length)];
            System.arraycopy(myArray, 0, newArray, 0, myArray.length);
            newArray[i] = newValue;
            return new TupleImpl(newArray);
        }
        throw new RuntimeException("not yet implemented");
    }

    public Mapping without(Object key) {
        throw new RuntimeException("not yet implemented");
    }

    public Enumeration keys() {
        return new IntegerIterator(BigInteger.valueOf(0),
                                   BigInteger.valueOf(myArray.length),
                                   BigInteger.valueOf(1));
    }

    public Enumeration asEnumeration() {
        return new ArrayIterator(myArray);
    }

    public Tuple occlude(Mapping under) {
        throw new RuntimeException("not yet implemented");
    }

    public Tuple slice(int start, int bound) throws NotFoundException {
        int len = bound - start;
        Object[] newArray = new Object[len];
        System.arraycopy(myArray, start, newArray, 0, len);
        return new TupleImpl(newArray);
    }

    public Tuple add(Tuple other) {
        Object[] newArray = new Object[myArray.length + other.size()];
        System.arraycopy(myArray, 0, newArray, 0, myArray.length);
        if (other instanceof TupleImpl) {
            Object[] otherArray = ((TupleImpl)other).myArray;
            System.arraycopy(otherArray, 0, newArray, myArray.length,
                             otherArray.length);
        } else {
            throw new RuntimeException("not yet implemented");
        }
        return new TupleImpl(newArray);
    }

    public AssociationEnumeration associations() {
         return (AssociationEnumeration) asEnumeration();
    }

    public Object[] asArray() {
        return (Object[])myArray.clone();
    }
}

