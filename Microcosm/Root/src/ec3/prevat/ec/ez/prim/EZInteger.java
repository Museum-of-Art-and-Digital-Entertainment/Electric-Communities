package ec.ez.prim;
import java.math.BigInteger;
import ec.ez.collect.*;


/**
 * EZInteger - an EZ Sugaring of java.math.BigInteger
 *
 */
public class EZInteger {

    /** work around ecomp's ignornace of '.class' */
    static public final Class TYPE = new EZInteger().getClass();
    private EZInteger() {}
    static public final Class OTHER_TYPE = BigInteger.valueOf(0).getClass();


    /**
     * Always gives back a BigInteger
     * This corresponds to the Java floating-point "/" operator
     * and the EZ "/" operator.
     */
    static public double approxDivide(double rec, double arg) {
        return rec / arg;
    }

    /**
     * Always gives an integer resulting from rounding towards zero,
     * ie, truncating.  This corresponds to the Java integer "/" operator.
     * BigInteger's existing 'remainder' gives the correct remainder from
     * the truncDivide operation.  <p>
     *
     * <pre>
     *      (a truncDivide b)*b + (a remainder b) == a
     *      [ 5, 3]: ( 1* 3) +  2 ==  5
     *      [ 5,-3]: (-1*-3) +  2 ==  5
     *      [-5, 3]: (-1* 3) + -2 == -5
     *      [-5,-3]: ( 1*-3) + -2 == -5
     * </pre><p>
     *
     * Therefore, if the result is non-zero, the sign of the result must
     * be the same as the sign of a.  This corresponds to the Java and
     * EZ "%" operator.
     */
    static public BigInteger truncDivide(BigInteger rec, BigInteger arg) {
        return rec.divide(arg);
    }

    /**
     * Always gives an integer, resulting from rounding towards negative
     * infinity, ie, flooring.  This corresponds to the EZ "//" operator.
     */
    static public BigInteger floorDivide(BigInteger rec, BigInteger arg) {
        if ((rec.signum() < 0) != (arg.signum() < 0)) {
            /*
             * Then the mathematical result is negative, and the two have
             * opposite signs.  Since we don't care when arg is zero (since we'll
             * get an exception), we take it's signum rather than rec's.
             *
             *  (a floorDivide b) == ((a + (b signum) - b) truncDivide b)
             */
            BigInteger fiddle = BigInteger.valueOf(arg.signum());
            rec = rec.add(fiddle).subtract(arg);
        }

        return rec.divide(arg);
   }

    static public Object multiply(BigInteger rec, Object arg) {
        if(arg instanceof Double) {
            return new Double(rec.doubleValue() * ((Double) arg).doubleValue());
        } else if (arg instanceof BigInteger) {
            return rec.multiply((BigInteger) arg);
        } else {
            throw new RuntimeException("invalid argument for multiply");
        }
    }

    /**
     * Remainder from the floorDivide operation.  <p>
     * <pre>
     *     (a floorDivide b)*b + (a modulo b) == a
     *      [ 5, 3]: ( 1* 3) +  2 ==  5
     *      [ 5,-3]: (-2*-3) + -1 ==  5
     *      [-5, 3]: (-2* 3) +  1 == -5
     *      [-5,-3]: ( 1*-3) + -2 == -5
     * </pre><p>
     * Therefore, if the result is non-zero, the sign of the result must be
     * the same as the sign of b, and so the result ranges from 0 inclusive
     * to b exclusive.  This corresponds to the EZ "%%" operator.  When
     * b >= 0, it also corresponds to Java's BigInteger.mod().
     */
    static public BigInteger mod(BigInteger rec, BigInteger arg) {
        if (arg.signum() >= 0) {
            return rec.mod(arg);
        } else {
            return rec.negate().mod(arg.negate());
        }
    }

    static public boolean lessThanOrEqualTo(BigInteger rec, Object arg) {
        if(arg instanceof BigInteger) {
            return rec.compareTo((BigInteger) arg) <= 0;
        } else if(arg instanceof Double) {
            return rec.doubleValue() <= ((Double) arg).doubleValue();
        }

        throw new RuntimeException("invalid operation for comparison");
    }

    static public BigInteger not(BigInteger val) {
        throw new RuntimeException("invalid operation for EZInteger");

//        return val.not();
     }

    static public BigInteger complement(BigInteger val) {
        return val.not();
    }

    static public BigInteger negate(BigInteger val) {
        return val.negate();
    }

    static public BigInteger truncate(BigInteger val) {
        return val;
    }

    static public Double sin(BigInteger val) {
        return new Double(Math.sin(val.doubleValue()));
    }

    static public Double cos(BigInteger val) {
        return new Double(Math.cos(val.doubleValue()));
    }

    static public Double tan(BigInteger val) {
        return new Double(Math.tan(val.doubleValue()));
    }

    static public Double sqrt(BigInteger val) {
        return new Double(Math.sqrt(val.doubleValue()));
    }

    static public Double exp(BigInteger val) {
        return new Double(Math.exp(val.doubleValue()));
    }

    static public Double asin(BigInteger val) {
        return new Double(Math.asin(val.doubleValue()));
    }

    static public Double acos(BigInteger val) {
        return new Double(Math.acos(val.doubleValue()));
    }

    static public Double atan(BigInteger val) {
        return new Double(Math.atan(val.doubleValue()));
    }

    static public Double atan2(BigInteger rec, double arg) {
        return new Double(Math.atan2(rec.doubleValue(), arg));
    }

    // XXX JAY - these are wrong - better to not make an int out of a float here.
    static public BigInteger min(BigInteger rec, Object arg) {
        if(arg instanceof BigInteger) {
            return rec.min((BigInteger) arg);
        } else {
            return rec.min(BigInteger.valueOf( ((Double) arg).longValue()));
        }
    }

    static public BigInteger max(BigInteger rec, Object arg) {
        if(arg instanceof BigInteger) {
            return rec.max((BigInteger) arg);
        } else {
            return rec.max(BigInteger.valueOf( ((Double) arg).longValue()));
        }
    }

    static public BigInteger ceil(BigInteger val) {
        return val;
    }

    static public Double log(BigInteger val) {
        return new Double(Math.log(val.doubleValue()));
    }

    static public Tuple thru(BigInteger rec, BigInteger limit) {
        BigInteger incr = BigInteger.valueOf(limit.compareTo(rec));
        return new Interval(rec, limit.add(incr), incr);
    }

    static public Tuple thru(BigInteger rec, BigInteger limit, BigInteger increment) {
        limit = limit.add(BigInteger.valueOf(increment.signum()));
        return new Interval(rec, limit, increment);
    }

    static public Tuple till(BigInteger rec, BigInteger limit) {
        BigInteger incr = BigInteger.valueOf(limit.compareTo(rec));
        return new Interval(rec, limit, incr);
    }

    static public Tuple till(BigInteger rec, BigInteger limit, BigInteger increment) {
       return new Interval(rec, limit, increment);
    }
}

