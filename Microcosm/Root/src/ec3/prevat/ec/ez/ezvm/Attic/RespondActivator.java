package ec.ez.ezvm;
import ec.e.run.*;
import ec.ez.runtime.Ejection;

public class RespondActivator extends InternalEWhenClosure {

      EZImpl script;

      public static EWhenClosure_$_Impl makeActivator(EZImpl theScript) {
          RespondActivator theActivator = new RespondActivator(theScript);
          return new EWhenClosure_$_Impl(theActivator);
      }

      RespondActivator(EZImpl theScript) {
            script = theScript;
      }

      public void doit(Object value) {
      Object args[] = { value };
         if(use()) {
           try {
            try {
                script.perform("doit", args);
            } catch (Ejection e) { }
           } catch (Exception e) { // XXX JAY Need to send back an E-Exception!
            e.printStackTrace();
           }
         }
      }
}




