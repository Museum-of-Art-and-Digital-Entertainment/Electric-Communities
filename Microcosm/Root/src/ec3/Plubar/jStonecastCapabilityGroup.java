/*
  jStonecastCapabilityGroup.java -- Capability group management classes

  Copyright 1998 Electric Communities, all rights reserved worldwide.
  Trevor Morris & Arturo Bejar
  Thu Nov 20 11:18:42 1997
*/

package ec.plubar;

import java.io.Serializable;
import ec.e.run.RtStateUpgradeable;

/**
 * Utility class that wraps an object that implements jCapabilityGroup in
 * such a way that the wrap only exposes the jCapabilityGroup interface
 *
 * @author Trevor Morris
 * @author Arturo Bejar
 * @version 1.0
 */
public class jStonecastCapabilityGroup
implements jCapabilityGroup, Serializable, RtStateUpgradeable {
    
    /** Object being wrapped */
    private jCapabilityGroup myTarget;
    
    /** Constructor */
    public jStonecastCapabilityGroup(jCapabilityGroup target) {
        myTarget = target;
    }
    
    /**
     * Get the capability of the given type, where type is specified by
     *   a type name
     *
     * @param typeName  Name of a Java interface or class, used in capability
     *   lookup.
     * @return capability of given type, or null if the group doesn't contain
     *   a capability of the given type
     * @throws IllegalArgumentException if type name is not the name of a
     *   Java class or interface
     */
    public Object capabilityOfType(String typeName) {
        return myTarget.capabilityOfType(typeName);
    }
}
