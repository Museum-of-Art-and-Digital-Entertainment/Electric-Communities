/*
  istBase.java -- Base class of all state bundles.

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.plubar;

import java.io.Serializable;

/**
 * Base class of all state bundles.
 *
 * @author Arturo Bejar
 * @version 1.0
 */
public abstract class istBase implements Serializable, Cloneable {
    /* Every state bundle has a capability group which in turn is held onto by
       the SoulState */
    transient private jEditableCapabilityGroup myCapabilityGroup;

    /**
     * Return the state bundle's capability group.
     *
     * Called from the SoulState add code establishing the unum's
     * capability group.
     */ 
    public jEditableCapabilityGroup getCapabilityGroup() {
        return myCapabilityGroup;
    }

    /**
     * Set the capability group.
     *
     * Called from the SoulState add code establishing the unum's
     * capability group.
     *
     * XXX CLEANUP: why isn't this in the constructor?
     */ 
    protected void setCapabilityGroup(
            jEditableCapabilityGroup capabilityGroup) {
        if (myCapabilityGroup == null) {
            myCapabilityGroup = capabilityGroup;
        } else {
            throw new RuntimeException("Cannot set capabilityGroup twice");
        }
    }
}

