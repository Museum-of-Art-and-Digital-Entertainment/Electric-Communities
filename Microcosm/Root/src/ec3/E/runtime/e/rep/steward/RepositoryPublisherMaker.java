package ec.e.rep.steward;

import ec.util.NestedException;
import ec.e.start.MagicPowerMaker;
import ec.e.start.EEnvironment;
import ec.e.rep.RepositoryPublisher;

/**

 * The make() method can now be called from both CREW and GUEST
 * environments. The first one to call this, creates the
 * RepositoryPublisher.

 */

public class RepositoryPublisherMaker implements MagicPowerMaker {
    public Object make(EEnvironment env) {
        try {
            return new RepositoryPublisher(env);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new NestedException("RepositoryPublisherMaker.make() caught ", t);
        }
    }
}
