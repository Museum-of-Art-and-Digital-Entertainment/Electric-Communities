package ec.ez.collect;
import java.util.Enumeration;
import java.math.BigInteger;

/**
 * Interval - iterates through a range of integer values
 *
 */
public class DoubleInterval implements Tuple {
    double myStart;
    double myBound;
    double myDelta;
    boolean myOpenFlag;
    int mySize;

    public DoubleInterval(double start, double bound) {
        this(start, bound, 1.0, true);
    }

    public DoubleInterval(double start, double bound, double delta, boolean open) {
        myStart = start;
        myBound = bound;
        myDelta = delta;
        myOpenFlag = open;
        double size = (myBound - myStart + myDelta) / myDelta;
        if (open && size == Math.floor(size)) {
            size -= 1.0;
        }
        mySize = Math.max(0, (int)size);
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

    public Object index(int k) throws NotFoundException {
        if (!(k >= 0 && k < mySize)) {
            throw new NotFoundException(k + " is out of range");
        }
        return new Double(myStart + (myDelta * k));
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
        return new DoubleIterator(myStart, myBound, myDelta, myOpenFlag);
    }

    /**
     * Warning: Converts to an explicit representation first.  This may be
     * voluminous and slow.
     */
    public Tuple occlude(Mapping under) {
        return MappingImpl.fromMapping(this).occlude(under);
    }

    public Tuple slice(int start, int bound) throws NotFoundException {
        double startValue = ((Double)get(BigInteger.valueOf(start)))
                                .doubleValue();
        double boundValue;
        if (myOpenFlag && bound == mySize) {
            boundValue = myBound;
        } else {
            boundValue = ((Double)get(BigInteger.valueOf(bound)))
                                .doubleValue();
        }
        return new DoubleInterval(startValue, boundValue, myDelta, true);
    }

    /**
     * Warning: May convert to an explicit representation first.  This may be
     * voluminous and slow.
     */
    public Tuple add(Tuple other) {
        if (other instanceof DoubleInterval) {
            DoubleInterval o = (DoubleInterval)other;
            if (myDelta == o.myDelta
                && myBound == o.myStart
                && myOpenFlag
                && (myBound - myStart) % myDelta == 0.0) {

                return new DoubleInterval(myStart, o.myBound,
                                          myDelta, o.myOpenFlag);
            }
        }
        return TupleImpl.fromTuple(this).add(other);
    }


    public AssociationEnumeration associations() {
         return (AssociationEnumeration) asEnumeration();
    }

    public Object[] asArray() {
        return TupleImpl.fromTuple(this).asArray();
    }
}
