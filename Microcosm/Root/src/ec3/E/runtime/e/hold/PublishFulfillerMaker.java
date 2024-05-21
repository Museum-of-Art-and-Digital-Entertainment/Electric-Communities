package ec.e.hold;
import ec.util.NestedException;
import ec.e.run.MagicPowerMaker;
import ec.e.run.EEnvironment;
import ec.e.rep.RepositoryPublisher;
import ec.e.rep.ParimeterizedRepository;
import java.util.Vector;

/**

 * Given an Environment capability, return one canonicaal
 * PublishFulfiller object.  A PublishFulfiller is simply a fulfiller
 * with a hints vector containing our RepositoryPublisher, implying
 * that the objects are published by us.<p>

 * This maker can be called from both CREW and GUEST environments.<p>

 * While fulfillers can be created without added capabilities besides
 * a ParimeterizedRepository it is convenient and efficient to share
 * one single fulfiller as much as possible since this may cut down on
 * network traffic.

 */

public class PublishFulfillerMaker implements MagicPowerMaker {
    public Object make(EEnvironment env) {
        try {
            ParimeterizedRepository repository =
              (ParimeterizedRepository)
              (env.magicPower("ec.e.rep.ParimeterizedRepositoryMaker"));

            RepositoryPublisher publisher = 
              (RepositoryPublisher)
              (env.magicPower("ec.e.rep.RepositoryPublisherMaker"));
            Vector hints = new Vector(1);
            hints.addElement(publisher.getSturdyRef());
            if (Trace.repository.debug && Trace.ON) {
                Trace.repository.debugm("PublishFulfillerMaker - Hints vector is " + hints);
            }
            return new Fulfiller(repository, null, hints);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new NestedException("PublishFulfillerMaker.make() caught ", t);
        }
    }
}
