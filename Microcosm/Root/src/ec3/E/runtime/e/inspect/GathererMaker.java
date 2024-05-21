package ec.e.inspect;

import ec.util.NestedException;
import ec.e.run.MagicPowerMaker;
import ec.e.run.EEnvironment;

/**

 * The make() method can now be called from both CREW and GUEST
 * environments. The first one to call this, creates the
 * Gatherer.

 */

public class GathererMaker implements MagicPowerMaker {
    public Object make(EEnvironment env) {
        try {
            return new Gatherer(env);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new NestedException("GathererMaker.make() caught ", t);
        }
    }
}
