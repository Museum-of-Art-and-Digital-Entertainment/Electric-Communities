package ec.ez.runtime;

import java.lang.Double;

/**
 * EZDouble - a basic numeric class for EZ
 *
 */
public class EZDouble extends EZBaseClass {
    private double myValue;

    public EZDouble(double value) {
        myValue = value;
    }

    public double getValue() {
        return(myValue);
    }

    public EZObject print() {
        System.out.println(myValue);
        return(this);
    }

    public EZObject plus(double arg) {
        return( new EZDouble(myValue + arg) );
    }

    public EZObject minus(double arg) {
        return( new EZDouble(myValue - arg) );
    }

    public EZObject times(double arg) {
        return( new EZDouble(myValue * arg) );
    }

    public EZObject dividedBy(double arg) {
        return( new EZDouble(myValue / arg) );
    }

    public EZObject intDividedBy(double arg) {
        return( new EZLong( (long) (myValue / arg)) );
    }

    public EZObject negated() {
         return( new EZDouble(-myValue) );
    }


    public EZObject modulo(double arg) {
        return( new EZDouble(myValue % arg) );
    }

    public EZObject raisedTo(double arg) {
        return( new EZDouble(Math.pow(myValue, arg)) );
    }

    public boolean lessThanOrEqualTo(double arg) {
        return( myValue <= arg );
    }

    public boolean equals(double arg) {
        return( myValue == arg );
    }

    public EZObject thru(double limit) {
        if(myValue <= limit)
            return new EZDouble_Iterator(myValue, limit, (double) 1.0, false);
         else
            return new EZDouble_Iterator(myValue, limit, (double) -1.0, false);
    }

    public EZObject thru(double limit, double increment) {
       return new EZDouble_Iterator(myValue, limit, (double) increment, false);
    }

    public EZObject till(double limit) {
        if(myValue <= limit)
            return new EZDouble_Iterator(myValue, limit, (double) 1.0, true);
         else
            return new EZDouble_Iterator(myValue, limit, (double) -1.0, true);
    }

    public EZObject till(double limit, double increment) {
       return new EZDouble_Iterator(myValue, limit, (double) increment, true);
    }

    public Object coerceTo(Class targetType) throws Exception {
        if(targetType == java.lang.Boolean.TYPE)
            return(new Boolean(myValue != 0.0));

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

}

