package ec.ez.collect;
import java.util.Enumeration;
import java.math.BigInteger;

/**
 * Interval - iterates through a range of integer values
 *
 */
public class Interval implements Tuple {
    BigInteger myStart;
    BigInteger myBound;
    BigInteger myDelta;
    int mySize;

    public Interval(BigInteger start, BigInteger bound) {
        this(start, bound, BigInteger.valueOf(1));
    }

    public Interval(BigInteger start, BigInteger bound, BigInteger delta) {
        myStart = start;
        myBound = bound;
        myDelta = delta;
        // (3 till 3,2) size => (3 - 3 + 2 - 1) / 2 => 0
        // (3 till 4,2) size => (4 - 3 + 2 - 1) / 2 => 1
        // (3 till 5,2) size => (5 - 3 + 2 - 1) / 2 => 1
        // (3 till 6,2) size => (6 - 3 + 2 - 1) / 2 => 2
        // (3 till 7,2) size => (7 - 3 + 2 - 1) / 2 => 2
        // (3 till 8,2) size => (8 - 3 + 2 - 1) / 2 => 3
        mySize = Math.max(0, myBound.subtract(myStart).add(myDelta)
                                .subtract(BigInteger.valueOf(1))
                                .divide(myDelta).intValue());
    }

    public String toString() {
        if (myDelta.equals(BigInteger.valueOf(1))) {
            return "(" + myStart + "..!" + myBound + ")";
        } else {
            return "(" + myStart + " till(" + myBound + ", " + myDelta + "))";
        }
    }

    public int size() {
        return mySize;
    }

    public boolean containsKey(Object key) {
        int k = ((Number)key).intValue();
        return k >= 0 && k < mySize;
    }

    public Object get(Object key) throws NotFoundException {
        return index(((Number)key).intValue());
    }

    public Object index(int key) throws NotFoundException {
        if (!(key >= 0 && key < mySize)) {
            throw new NotFoundException(key + " is out of range");
        }
        return myStart.add(myDelta.multiply(BigInteger.valueOf(key)));
    }

    /**
     * Warning: Converts to an explicit representation first.  This may be
     * voluminous and slow.
     */
    public Mapping with(Object key, Object newValue) {
        return MappingImpl.fromMapping(this).with(key, newValue);
    }

    /**
     * Warning: Converts to an explicit representation first.  This may be
     * voluminous and slow.
     */
    public Mapping without(Object key) {
        return MappingImpl.fromMapping(this).without(key);
    }

    public Enumeration keys() {
        return new IntegerIterator(BigInteger.valueOf(0),
                                   BigInteger.valueOf(mySize),
                                   BigInteger.valueOf(1));
    }

    public Enumeration asEnumeration() {
        return new IntegerIterator(myStart, myBound, myDelta);
    }

    public AssociationEnumeration associations() {
         return (AssociationEnumeration) asEnumeration();
    }

    /**
     * Warning: Converts to an explicit representation first.  This may be
     * voluminous and slow.
     */
    public Tuple occlude(Mapping under) {
        return MappingImpl.fromMapping(this).occlude(under);
    }

    public Tuple slice(int start, int bound) throws NotFoundException {
        BigInteger startValue = (BigInteger)get(BigInteger.valueOf(start));
        BigInteger boundValue;
        if (bound == mySize) {
            boundValue = myBound;
        } else {
            boundValue = (BigInteger)get(BigInteger.valueOf(bound));
        }
        return new Interval(startValue, boundValue, myDelta);
    }

    /**
     * Warning: May convert to an explicit representation first.  This may be
     * voluminous and slow.
     */
    public Tuple add(Tuple other) {
        if (other instanceof Interval) {
            Interval o = (Interval)other;
            if (myDelta.equals(o.myDelta) && myBound.equals(o.myStart)) {
                return new Interval(myStart, o.myBound, myDelta);
            }
        }
        return TupleImpl.fromTuple(this).add(other);
    }

    public Object[] asArray() {
        return TupleImpl.fromTuple(this).asArray();
    }
}
