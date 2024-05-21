package ec.e.quake.crew;

import ec.e.quake.StateTimeMachine;
import ec.e.start.EEnvironment;
import ec.e.quake.TimeMachine;
import ec.e.run.OnceOnlyException;
import ec.util.NestedException;

import java.io.IOException;

/**
 * Class for creating StateTimeMachines.
    */
    
public class StateTimeMachineMaker extends TimeMachineMaker  {
  
    public Object make(EEnvironment eEnv) {
        try {
            return new StateTimeMachine(eEnv);
        } catch (OnceOnlyException ex) {
            throw new NestedException("not again", ex);
        } catch (IOException ex) {
            throw new NestedException("can't persist", ex);
        }
    }
  
}
