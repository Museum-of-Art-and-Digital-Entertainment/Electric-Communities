package ec.e.rep.steward;

import ec.e.start.Tether;
import ec.e.start.Vat;
import ec.e.start.SmashedException;
import ec.e.start.crew.CrewCapabilities;

public class RepositoryTether extends Tether {

    /**

     * Construct a Tether.
     *
     * @param vat - The vat we want to use the Tether in.

     */

    public RepositoryTether(Vat vat) {
        super(vat,CrewCapabilities.getTheSuperRepository());
    }
    
    /**

     * If held() is called on a smashed Tether, it calls
     * reconstructed().  If reconstructed() returns an object, this
     * becomes the newly held object of the newly non-smashed Tether,
     * and the smashed state is not visible to the Tether's user.
     * Such a Tether is "sturdy".<p>
     *
     * This reconstructed() reconnects the tether to the working
     * Repository in CREW space. It was restarted at revival time.
     * What we have, then, is a self-healing Tether to the
     * SuperRepository that repairs itself after a quake totally
     * transparently to the vat denizens.

     */

    protected Object reconstructed() throws SmashedException {
        myHeld = null; //to free memory
        return CrewCapabilities.getTheSuperRepository();
    }

}
        
        
