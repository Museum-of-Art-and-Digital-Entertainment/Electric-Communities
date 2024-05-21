package ec.e.run;

import java.util.Properties;

/**
 * This interface is really CREW, but has been put into a STEWARD
 * package for the moment to avoid a circular dependency problem.
 * Bletch. <p>
 *
 * Makes an initial root capability steward (to be handed to a guest
 * in the Vat) that provides the all encompasing root authority to
 * some external system service, such as the file system. <p>
 *
 * The EEnvironment looks up MagicPowerMakers by class name, and
 * instantiates the class with newInstance(), so each MagicPowerMaker
 * class should provide a no-argument public constructor.  The
 * MagicPowerMaker should also arrange for the returned magic power
 * object to be sturdy.  Ie, it should be able to transparently
 * reconstruct the equivalent magic power when revived into a new
 * process incarnation.
 */
public interface MagicPowerMaker {

    /**
     * Returns an in-vat steward that provides full authority for
     * this service.
     */
    Object make(EEnvironment env);
}

//XXXABS start Existing Registrar problem
public interface ComparingMagicPowerMaker {

    /**
     * @return true if both the objects provided are equivalent.
     */

    boolean areEquivalent(Object environmentPower, Object providedPower);

    /**
     * @return true if the power provided is valid.
     */

    boolean isValidPower(Object providedPower);
}
//XXXABS end Existing Registrar problem

public interface RepositoryMaker {

    /**
     * Returns an in-vat steward that provides full authority for
     * this service.
     */
    Object make(Properties props);
}
