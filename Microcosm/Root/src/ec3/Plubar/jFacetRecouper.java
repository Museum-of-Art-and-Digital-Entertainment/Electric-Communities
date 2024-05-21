/*
  jFacetRecouper.java -- Facet capability recouper classes

  Copyright 1998 Electric Communities, all rights reserved worldwide.
  Arturo Bejar
  Sunday December 7 1997
*/

package ec.plubar;

import ec.e.run.RtDeflector;
import ec.e.run.RtStateUpgradeable;
import java.io.IOException;
import java.io.Serializable;

/**
 * Example recouper for facets.
 *
 * @author Trevor Morris
 * @author Arturo Bejar
 * @version 1.0
 */
public class jFacetRecouper
implements jiCapabilityRecouper, Serializable, RtStateUpgradeable {

    String myCapabilityName;

    transient EObject myCapability;
    transient EUniDistributor myDistributor;

    public jFacetRecouper(String capabilityName) {
        myCapabilityName = capabilityName;
    }

    /**
     * @return the real capability, or a UniChannel to the capability
     *   that will be set at a later time throught setCapablity.
     */
    public EObject getCapability() {
        return myCapability;
    }

    /**
     * Sets this recouper's capability either by doing it the first
     * time around, or by forwarding a UniDistributor to the UniChannel
     * that is returned as the capability.
     * Called from jEditableCapabilityGroup.
     */
    protected void setCapability(RtTether realCapability) {
        /* Will need to figure out the best shortcutting strategy but for now
           layering tethers is very painless. */

        if (myDistributor != null) {
//KSSHack            myDistributor <- forward(realCapability);
            myDistributor = null;
        } else {
            if (myCapability == null) {
                myCapability = (EObject)realCapability;
            } else {
                throw new RuntimeException("Can't set capability twice");
            }
        }
    }

    /**
     * Serialization support
     */
    private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
        out.writeObject(myCapabilityName);
    }

    /**
     * Deserialization support, will create a UniChannel at decode so that the
     * capability is accessible as the world is getting recreated.
     *
     * @exception IOException from readObject()
     * @exception ClassNotFoundException from readObject()
     */
    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        myCapabilityName = (String)in.readObject();
        EUniChannel temp = new EUniChannel();
        myDistributor = temp.getDistributor();
        myCapability = (EObject) (RtTether)
            RtDeflector.construct(myCapabilityName, temp, temp);
    }
}
