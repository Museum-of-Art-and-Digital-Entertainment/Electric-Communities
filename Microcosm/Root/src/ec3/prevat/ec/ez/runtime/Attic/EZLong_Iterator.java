package ec.ez.runtime;
import ec.ez.ezvm.EZImpl;

/**
 * EZLong_Iterator - iterates through a range of integer values
 *
 */
public class EZLong_Iterator implements EZObject {
    long value;
    long endingValue;
    long deltaValue;
    boolean openFlag;

    public EZLong_Iterator(long first, long last, long increment, boolean open) {
        value = first;
        endingValue = last;
        deltaValue = increment;
        openFlag = open;
    }

    public EZObject apply(String verb, EZObject[] args) throws Exception, Ejection {
        if(verb.equals("do")) {
          EZImpl doTo = (EZImpl) args[0];
         while (!iterationDone()) {
           EZObject[] val = { new EZLong(value) };
           doTo.apply("run", val);
           value += deltaValue;
          }
         return(Globals.EZ_NULL);
        } else throw new NotFoundException(verb + " not found");
    }

    public boolean iterationDone() {
        if(deltaValue < 0) {
            if(value < endingValue) {
                return(true);
            }
        } else {
            if(value > endingValue) {
                return(true);
            }
        }
        if(value == endingValue) {
            return(openFlag);
        } else {
            return(false);
        }
    }

    public EZObject applyLater(String verb, EZObject[] args) {
          throw new RuntimeException("NotYetImplemented");
    }

    public Object coerceTo(Class targetType) throws Exception {
        return(null);
    }
}