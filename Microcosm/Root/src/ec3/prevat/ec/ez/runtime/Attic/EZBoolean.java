package ec.ez.runtime;

import java.lang.Boolean;

/**
 * EZBoolean - a basic boolean class for EZ
 *
 */
public class EZBoolean extends EZBaseClass {
    boolean myValue;

    public static final EZBoolean EZ_TRUE = new EZBoolean(true);
    public static final EZBoolean EZ_FALSE = new EZBoolean(false);

    public static final EZBoolean convert(boolean value) {
        if(value) return(EZ_TRUE); else return(EZ_FALSE);
    }

    EZBoolean(boolean value) {
        myValue = value;
    }

    public EZObject print() {
        if(this == EZ_TRUE)
            System.out.print("true");
          else
             System.out.print("false");
        return(this);
    }

    public EZObject and(boolean arg) {
        if((this == EZ_TRUE) && arg) return(EZ_TRUE);
            else return(EZ_FALSE);
    }

    public EZObject not() {
        if(this == EZ_TRUE) return(EZ_FALSE);
            else return(EZ_TRUE);
    }

    public EZObject or(boolean arg) {
        if(this == EZ_TRUE) return(EZ_TRUE);
        if(arg) return(EZ_TRUE);
           else return(EZ_FALSE);
    }

    public EZObject xor(boolean arg) {
        if((this == EZ_TRUE) ^ arg) return(EZ_TRUE);
           else return(EZ_FALSE);
    }

    public EZObject equals(boolean arg) {
        if((this == EZ_TRUE) && arg) return(EZ_TRUE);
            else return(EZ_FALSE);
    }

    public EZObject pick(EZObject trueBlock, EZObject falseBlock) {
        if(this == EZ_TRUE)
            return(trueBlock);
          else
            return(falseBlock);
    }

    public Object coerceTo(Class targetType) throws Exception {
        if(targetType == java.lang.Boolean.TYPE)
            return(new Boolean(this == EZ_TRUE));

        if(targetType == java.lang.Character.TYPE) {
            if(this == EZ_TRUE)
                return(new Character((char) 'T'));
              else
                return(new Character((char) 'F'));
        }

        if(targetType == java.lang.Short.TYPE) {
             if(this == EZ_TRUE)
                return(new Short((short) 1));
              else
                return(new Short((short) 0));
        }

         if(targetType == java.lang.Long.TYPE) {
              if(this == EZ_TRUE)
                return(new Long(1));
              else
                return(new Long(0));
        }

         if(targetType == java.lang.Float.TYPE) {
              if(this == EZ_TRUE)
                return(new Float(1.0));
              else
                return(new Float(0.0));
        }

         if(targetType == java.lang.Double.TYPE) {
              if(this == EZ_TRUE)
                return(new Double(1.0));
              else
                return(new Double(0.0));
        }

       return(this);
    }

}

