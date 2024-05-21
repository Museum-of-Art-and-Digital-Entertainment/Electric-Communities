package ec.ez.runtime;

import java.lang.Double;

/**
 * EZLong - a basic numeric class for EZ
 *
 */
public class EZLong extends EZBaseClass {
    private long myValue;

    public EZLong(long value) {
        myValue = value;
    }

    public long getValue() {
        return(myValue);
    }

    public EZObject print() {
        System.out.println(myValue);
        return(this);
    }

    public EZObject plus(long arg) {
        return( new EZLong(myValue + arg) );
    }

    public EZObject minus(long arg) {
        return( new EZLong(myValue - arg) );
    }

    public EZObject times(long arg) {
        return( new EZLong(myValue * arg) );
    }

    public EZObject dividedBy(long arg) {
        return( new EZLong(myValue / arg) );
    }

    public EZObject intDividedBy(long arg) {
        return( new EZLong(myValue / arg) );
    }

    public EZObject modulo(long arg) {
        return( new EZLong(myValue % arg) );
    }

    public EZObject raisedTo(long arg) {
        return( new EZLong((long) Math.pow(myValue, arg)) );
    }

    public EZObject negated() {
         return( new EZLong(-myValue) );
    }

    public EZObject bitComplement() {
         return( new EZLong(~myValue) );
    }

    public EZObject and(long arg) {
        return( new EZLong(myValue & arg) );
    }

    public EZObject or(long arg) {
        return( new EZLong(myValue | arg) );
    }

    public EZObject xor(long arg) {
        return( new EZLong(myValue ^ arg) );
    }

    public EZObject leftShift(long arg) {
        return( new EZLong(myValue << arg) );
    }

    public EZObject rightShift(long arg) {
        return( new EZLong(myValue >> arg) );
    }

    public boolean lessThanOrEqualTo(long arg) {
        return( myValue <= arg );
    }

    public boolean equals(long arg) {
        return( myValue == arg );
    }

    public Object coerceTo(Class targetType) throws Exception {
        if(targetType == java.lang.Boolean.TYPE)
            return(new Boolean(myValue != 0));

        if(targetType == java.lang.Character.TYPE)
            return(new Character((char) myValue));

        if(targetType == java.lang.Short.TYPE)
            return(new Short((short) myValue));

         if(targetType == java.lang.Long.TYPE)
            return(new Long((long) myValue));

         if(targetType == java.lang.Float.TYPE)
            return(new Float(myValue));

         if(targetType == java.lang.Double.TYPE)
            return(new Double(myValue));

       return(this);
    }

    public EZObject thru(long limit) {
        if(myValue <= limit)
            return new EZLong_Iterator(myValue, limit, (long) 1, false);
         else
            return new EZLong_Iterator(myValue, limit, (long) -1, false);
    }

    public EZObject thru(long limit, long increment) {
       return new EZLong_Iterator(myValue, limit, (long) increment, false);
    }

    public EZObject till(long limit) {
        if(myValue <= limit)
            return new EZLong_Iterator(myValue, limit, (long) 1, true);
         else
            return new EZLong_Iterator(myValue, limit, (long) -1, true);
    }

    public EZObject till(long limit, long increment) {
       return new EZLong_Iterator(myValue, limit, (long) increment, true);
    }

}

