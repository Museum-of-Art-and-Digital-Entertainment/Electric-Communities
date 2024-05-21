/*
  jRecoupableCapability.java -- Facet capability recouper classes

  Copyright 1998 Electric Communities, all rights reserved worldwide.
  Arturo Bejar
  Sunday December 7 1997
*/

package ec.plubar;

import ec.e.run.EChannel;
import ec.e.run.EObject;
import ec.e.run.RtDelegatingSerializable;
import ec.e.serialstate.StateOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * This class is what you put into your state bundle to be able to
 * recoup a colocated capability at deserialization time.
 *
 * @author Arturo Bejar
 * @version 1.0
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
