/*
  j_RecoupCapability.java -- Facet capability recouper classes

  Copyright 1998 Electric Communities, all rights reserved worldwide.
  Arturo Bejar
  Sunday December 7 1997
*/

package ec.pl.runtime;

import ec.e.run.RtDeflector;
import ec.e.run.RtStateUpgradeable;
import ec.e.serialstate.StateOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Interface to be implemented by the CapabilityRecouper that is instantiated
 * by the facet when it is serialized.
 */
public interface jiCapabilityRecouper {
    public EObject getCapability();
}

/**
 * Interface implemented by classes that will allow the capability group to set
 * their facet recoupers, in particular applied to whatever we end up calling
 * the unumrouter/deflector for.
 */
interface jiSetFacetRecouper {
    void setFacetRecouper(jFacetRecouper facetRecouper);
}


/**
 * Example recouper for facets.
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
            myDistributor <- forward(realCapability);
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

/**
 * This class is what you put into your state bundle to be able to
 * recoup a colocated capability at deserialization time.
 */
public class jRecoupableCapability implements Serializable  {

    private EObject myCapability;
    /* No need to store the recouper, it is only transient for decode time.
       (being frugal about references) */
    transient private jiCapabilityRecouper myCapabilityRecouper;

    public jRecoupableCapability() {
    }

    public void setCapability(EObject capability) {
        if (null == capability ||
                capability instanceof RtDelegatingSerializable) {
            if (null != capability) {
                Object toSerialize =
                    ((RtDelegatingSerializable)capability).
                        delegateToSerialize();
                if (toSerialize instanceof EChannel) {
                    throw new RuntimeException(
                        "Not RtDelegatingSerializable: " + toSerialize);
                }
            }
            myCapability = capability;
        } else {
            throw new RuntimeException(
                "Not RtDelegatingSerializable: " + capability);
        }
    }

    public EObject getCapability() {
        return myCapability;
    }

    private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
        if (out instanceof StateOutputStream) {
            ((StateOutputStream)out).policyWriteObject(myCapability);
        }
    }

    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        if (null == obj) {
            myCapability = null;
        } else if (obj instanceof jiCapabilityRecouper) {
            myCapability = ((jiCapabilityRecouper)obj).getCapability();
            if (myCapability == null)  {
                throw new IOException(
                    "Not null capability recouper returned null:" + obj);
            }
        } else {
            throw new IOException("Wrong kind for recouper: " + obj);
        }
    }
}
