package ec.e.rep.steward;

import ec.util.NestedException;
import ec.e.start.MagicPowerMaker;
import ec.e.start.EEnvironment;
import ec.e.rep.ParimeterizedRepository;

/**

 * The make() method can now be called from both CREW and GUEST
 * environments. The first one to call this, creates the
 * ParimeterizedRepository.

 */

public class ParimeterizedRepositoryMaker implements MagicPowerMaker {
    public Object make(EEnvironment env) {
        try {
            return new ParimeterizedRepository(env);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new NestedException("ParimeterizedRepositoryMaker.make() caught ", t);
        }
    }
}
