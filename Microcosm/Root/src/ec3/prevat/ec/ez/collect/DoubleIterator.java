package ec.ez.collect;
import java.util.Enumeration;
import java.math.BigInteger;

/**
 * DoubleIterator - iterates through a range of double-precision values
 *
 */
public class DoubleIterator implements AssociationEnumeration {
    double myValue;
    double myBound;
    double myDelta;
    boolean myOpenFlag;
    long    index = -1;

    public DoubleIterator(double first, double bound, double delta, boolean open) {
        myValue = first;
        myBound = bound;
        myDelta = delta;
        myOpenFlag = open;
    }

    public boolean hasMoreElements() {
        if (myValue < myBound) {
            //we're below, so we're still going iff we're going up
            return myDelta > 0;
        } else if (myValue > myBound) {
            //we're above, so we're still going iff we're going down
            return myDelta < 0;
        } else {
            //we coincide, so we're still going if we're inclusive
            return ! myOpenFlag;
        }
    }

    public Object nextElement() {
        Double result = new Double(myValue);
        myValue += myDelta;
        index++;
        return result;
    }

    public Object currentKey() {
        return BigInteger.valueOf(index);
    }

}