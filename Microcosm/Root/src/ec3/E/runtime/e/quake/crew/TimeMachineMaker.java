package ec.e.quake.crew;

import java.io.IOException;

import ec.e.start.MagicPowerMaker;
import ec.e.start.EEnvironment;
import ec.e.quake.TimeMachine;
import ec.e.run.OnceOnlyException;
import ec.util.NestedException;


public class TimeMachineMaker implements MagicPowerMaker {

    public Object make(EEnvironment eEnv) {
        try {
            return new TimeMachine(eEnv);
        } catch (OnceOnlyException ex) {
            throw new NestedException("not again", ex);
        } catch (IOException ex) {
            throw new NestedException("can't persist", ex);
        }
    }
}

