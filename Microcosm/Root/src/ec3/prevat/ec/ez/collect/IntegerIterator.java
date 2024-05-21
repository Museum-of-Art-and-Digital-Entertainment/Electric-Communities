package ec.ez.collect;
import java.util.Enumeration;
import java.math.BigInteger;

/**
 * EZLongIterator - iterates through a range of integer values
 *
 */
public class IntegerIterator implements AssociationEnumeration {
    BigInteger myValue;
    BigInteger myBound;
    BigInteger myDelta;
    long    index = -1;

    public IntegerIterator(BigInteger first, BigInteger bound, BigInteger delta) {
        myValue = first;
        myBound = bound;
        myDelta = delta;
    }

    public boolean hasMoreElements() {
        if (myValue.compareTo(myBound) < 0) {
            //we're below, so we're still going iff we're going up
            return myDelta.signum() > 0;
        } else if (myValue.compareTo(myBound) > 0) {
            //we're above, so we're still going iff we're going down
            return myDelta.signum() < 0;
        } else {
            //we coincide, so we're done
            return false;
        }
    }

    public Object nextElement() {
        BigInteger result = myValue;
        myValue = myValue.add(myDelta);
        index++;
        return result;
    }

    public Object currentKey() {
        return BigInteger.valueOf(index);
    }
}
