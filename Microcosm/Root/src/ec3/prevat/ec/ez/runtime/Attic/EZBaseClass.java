package ec.ez.runtime;
import java.lang.reflect.*;

/**
 * A base class for Standard EZ Runtime classes
 */
public class EZBaseClass implements EZObject {

    public Object target() {
        return(this);
    }

   // XXX Cache this guy to make him faster soon!!
    public EZObject apply(String verb, EZObject[] args) throws Exception, Ejection {
            Method meths[] = getClass().getMethods();
            for (int i = 0; i < meths.length; i++) {
               Method thisMeth = meths[i];
               if(thisMeth.getName().equals(verb)) {
                   Class parmTypes[] = thisMeth.getParameterTypes();
                   if(parmTypes.length == args.length) {
                        Object coercedArgs [] = new Object[args.length];
                        for(int j = 0; j < thisMeth.getParameterTypes().length; j++)
                            coercedArgs[j] = args[j].coerceTo(parmTypes[j]);
                        try {
                          try {
                            return(EZWrapper.make(thisMeth.invoke(target(), coercedArgs)));
                           } catch (InvocationTargetException e) {
                              Throwable retoss = (Throwable) e.getTargetException();
                              if(retoss instanceof Ejection)
                                throw ((Ejection) retoss);
                               else throw ((Exception) retoss);
                           }
                        } catch (Exception e) {
                             throw e;
                        }
                   }
               }
            }
        // XXX JAY should throw a verb not defined exception!!!
        throw new NotFoundException(verb + " not found");
    }

    public EZObject applyLater(String verb, EZObject[] args) {
        throw new RuntimeException("NotYetImplemented");
    }

    public Object  coerceTo(Class targetType) throws Exception {
        return(this);
    }

}

