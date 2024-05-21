package ec.ez.prim;
import ec.ez.collect.*;
import java.math.BigInteger;

/**
 * EZDouble - an EZ Sugaring of java.lang.Double
 */
public class EZDouble {

    /** work around ecomp's ignornace of '.class' */
    static public final Class TYPE = new EZDouble().getClass();
    private EZDouble() {}
    static public final Class OTHER_TYPE = new Double(0.0).getClass();


    static public Double add(double rec, double arg) {
        return new Double(rec + arg);
    }

    static public Double subtract(double rec, double arg) {
        return new Double(rec - arg);
    }

    static public Double negate(double rec) {
         return new Double(-rec);
    }

    static public Double multiply(double rec, double arg) {
        return new Double(rec * arg);
    }

    static public Double abs(double num) {
        if(num > 0.0) return new Double(num);
          else return new Double(-num);
    }


    /**
     * Always gives back a double
     * This corresponds to the Java floating-point "/" operator
     * and the EZ "/" operator.
     */
    static public Double approxDivide(double rec, double arg) {
        return new Double(rec / arg);
    }

    /**
     * Always gives an integer resulting from rounding towards zero,
     * ie, truncating.  This corresponds to the Java integer "/" operator.
     */
    static public long truncDivide(double rec, double arg) {
        return (long)(rec / arg);
    }

    /**
     * Remainder from truncDivide operation.  <p>
     * <pre>
     *      (a truncDivide b)*b + (a remainder b) == a
     *      [ 5, 3]: ( 1* 3) +  2 ==  5
     *      [ 5,-3]: (-1*-3) +  2 ==  5
     *      [-5, 3]: (-1* 3) + -2 == -5
     *      [-5,-3]: ( 1*-3) + -2 == -5
     * </pre><p>
     * Therefore, if the result is non-zero, the sign of the result must
     * be the same as the sign of a.  This corresponds to the Java and
     * EZ "%" operator.
     */
    static public Double remainder(double rec, double arg) {
        return new Double(rec % arg);
    }

    /**
     * Always gives an integer, resulting from rounding towards negative
     * infinity, ie, flooring.  This corresponds to the EZ "//" operator.
     */
    static public BigInteger floorDivide(double rec, double arg) {
        return BigInteger.valueOf((long)Math.floor(rec / arg));
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
    static public Double mod(double rec, double arg) {
        double result = rec % arg;
        if (((arg < 0.0) != (result < 0.0)) && result != 0.0) {
            return new Double(result + arg);
        } else {
            return new Double(result);
        }
    }

    static public Double pow(double rec, double arg) {
        return new Double(Math.pow(rec, arg));
    }

    static public Double modPow(double rec, double exp, double modulus) {
        return mod(Math.pow(rec, exp), modulus);
    }

    static public Double log(double rec) {
        return new Double(Math.log(rec));
    }

    static public boolean lessThanOrEqualTo(double rec, double arg) {
        return rec <= arg;
    }

    static public Tuple thru(double rec, double limit) {
        if(rec <= limit) {
            return new DoubleInterval(rec, limit, 1.0, false);
        } else {
            return new DoubleInterval(rec, limit, -1.0, false);
        }
    }

    static public Tuple thru(double rec, double limit, double increment) {
        return new DoubleInterval(rec, limit, increment, false);
    }

    static public Tuple till(double rec, double limit) {
        if(rec <= limit) {
            return new DoubleInterval(rec, limit, 1.0, true);
        } else {
            return new DoubleInterval(rec, limit, -1.0, true);
        }
    }

    static public Tuple till(double rec, double limit, double increment) {
       return new DoubleInterval(rec, limit, increment, true);
    }


    static public Double sin(double rec) {
        return new Double(Math.sin(rec));
    }

    static public Double cos(double rec) {
        return new Double(Math.cos(rec));
    }

    static public Double tan(double rec) {
        return new Double(Math.tan(rec));
    }

    static public Double sqrt(double rec) {
        return new Double(Math.sqrt(rec));
    }

    static public Double exp(double rec) {
        return new Double(Math.exp(rec));
    }

    static public Double asin(double rec) {
        return new Double(Math.asin(rec));
    }

    static public Double acos(double rec) {
        return new Double(Math.acos(rec));
    }

    static public Double atan(double rec) {
        return new Double(Math.atan(rec));
    }

    static public Double atan2(double rec, double arg) {
        return new Double(Math.atan2(rec, arg));
    }

    static public Double min(double rec, double arg) {
        return new Double(Math.min(rec, arg));
    }

    static public Double max(double rec, double arg) {
        return new Double(Math.max(rec, arg));
    }

    static public BigInteger ceil(double rec) {
        return BigInteger.valueOf((long)Math.ceil(rec));
    }

    static public BigInteger floor(double rec) {
        return BigInteger.valueOf((long)Math.floor(rec));
    }

    static public BigInteger round(double rec) {
        return BigInteger.valueOf((long)Math.round(rec));
    }

    static public BigInteger truncate(double rec) {
        return BigInteger.valueOf((long)rec);
    }

    static public Double random(double rec) {
        return new Double(Math.random() * rec);
    }
}

