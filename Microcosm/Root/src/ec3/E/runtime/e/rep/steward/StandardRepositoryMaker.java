package ec.e.rep.steward;

import ec.util.NestedException;
import ec.e.start.MagicPowerMaker;
import ec.e.start.EEnvironment;
import ec.e.rep.StandardRepository;

/**

 * The make() method can now be called from both CREW and GUEST
 * environments. The first one to call this, creates the
 * StandardRepository.

 */

public class StandardRepositoryMaker implements MagicPowerMaker {
    public Object make(EEnvironment env) {
        try {
            return new StandardRepository(env);
        } catch (Throwable t) {
            throw new NestedException("StandardRepositoryMaker.make() caught ", t);
        }
    }
}
