/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, June 97
 */

package ec.ez.runtime;

import ec.ez.ezvm.Expr;
import ec.ez.ezvm.Pov;

/** This is an object used to reprasent 'null' in the EZ world, both as
 *  a no-op expression in parse trees, and as a null value. */
public class EZNull extends Expr implements EZObject {

    private EZNull() {}

    private static final EZNull TheOne = new EZNull();

    public static EZNull theOne() {
        return TheOne;
    }

    /* There's a possibly interesting issue here about whether EZNull should
     * be both an Expr and the EZObject itself. It should possibly be simply
     * whatever is bound to the noun 'null' in pov. This would allow the
     * meaning of 'null' to be modified for child contexts. */
    public EZObject eval(Pov pov) throws Exception {
        return this;
    }

    public EZObject apply(String verb, EZObject[] args)
            throws Exception, Ejection {
        return this;
    }

    public EZObject applyLater(String verb, EZObject[] args) {
        return this;
    }

    public EZObject run() {
        return(this);
    }

// XXX JAY - in a move sure to inspire a debate - I decided (temporarily
// at least) to make null mean 0 and false.
    public Object  coerceTo(Class targetType) throws Exception {
        if(targetType == java.lang.Boolean.TYPE)
            return(new Boolean(false));

        if(targetType == java.lang.Character.TYPE)
            return(new Character((char) 0));

        if(targetType == java.lang.Short.TYPE)
            return(new Short((short) 0));

         if(targetType == java.lang.Long.TYPE)
            return(new Long(0));

         if(targetType == java.lang.Float.TYPE)
            return(new Float(0.0));

         if(targetType == java.lang.Double.TYPE)
            return(new Double(0.0));

       return(null);
    }
}