package ec.e.rep.steward;

import ec.util.NestedException;
import ec.e.start.MagicPowerMaker;
import ec.e.start.EEnvironment;
import ec.e.rep.PublishRepository;

/**

 * The make() method can now be called from both CREW and GUEST
 * environments. The first one to call this, creates the
 * PublishRepository.

 */

public class PublishRepositoryMaker implements MagicPowerMaker {
    public Object make(EEnvironment env) {
        try {
            return new PublishRepository(env);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new NestedException("PublishRepositoryMaker.make() caught ", t);
        }
    }
}
