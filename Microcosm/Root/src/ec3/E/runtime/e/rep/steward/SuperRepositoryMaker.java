package ec.e.rep.steward;

import java.util.Properties;

import ec.e.rep.steward.SuperRepository;
import ec.e.start.MagicPowerMaker;
import ec.e.start.RepositoryMaker;
import ec.e.start.EEnvironment;

import ec.util.NestedException;

/**

 * Restrict the Environment capability to a SuperRepository
 * capability.  This can be called from both CREW and GUEST
 * environments.<p>

 * Note that while this capability is meant to be given (only
 * indirectly) to Guest objects, it is nevertheless fragile. Guests
 * should really be given StandardRepository or
 * ParimeterizedRepository capabilities that restrict this
 * SuperRepository further to only provide Read-Only
 * access. StandardRepositories and ParimeterizedRepositories are also
 * self-repairing after a quake. <p>

 */

public class SuperRepositoryMaker implements MagicPowerMaker, RepositoryMaker {
    public Object make(EEnvironment env) {
        try {
            return SuperRepository.makeSuperRepository(env.props());
        } catch (Throwable t) {
            t.printStackTrace();
            throw new NestedException("SuperRepositoryMaker.make() caught ", t);
        }
    }

    public Object make(Properties props) {
        try {
            return SuperRepository.makeSuperRepository(props);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new NestedException("SuperRepositoryMaker.make() caught ", t);
        }
    }
}
