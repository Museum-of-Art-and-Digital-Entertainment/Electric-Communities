package ec.e.hold;

import ec.util.NestedException;
import ec.e.run.MagicPowerMaker;
import ec.e.run.EEnvironment;
import ec.e.rep.ParimeterizedRepository;

/**

 * Given an Environment capability, return one canonical
 * NullFulfiller object.  A NullFulfiller is simply a fulfiller with a
 * NULL hints vector, implying that the objects must live in the
 * Repository in the receiver. This in turn implies it's part of the
 * EC Distribution.<p>

 * This maker can be called from both CREW and GUEST environments.<p>

 * While fulfillers can be created cheaply and easily without added
 * capabilities besides the required ParimeterizedRepository it is
 * more efficient to share one single fulfiller as much as possible
 * since it will cut down on network traffic.

 */

public class NullFulfillerMaker implements MagicPowerMaker {
    public Object make(EEnvironment env) {
        try {
            ParimeterizedRepository repository =
              (ParimeterizedRepository)
              (env.magicPower("ec.e.rep.ParimeterizedRepositoryMaker"));
            return new Fulfiller(repository,null);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new NestedException("NullFulfillerMaker.make() caught ", t);
        }
    }
}
