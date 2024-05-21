/*
  jCapabilityGroup.java -- Capability group management classes

  Copyright 1998 Electric Communities, all rights reserved worldwide.
  Trevor Morris & Arturo Bejar
  Thu Nov 20 11:18:42 1997
*/

package ec.plubar;

/**
 * A capability group is a a set of capabilities which can by accessed by type
 * (i.e. a class or interface). The set cannot be changed via the
 * jCapabilityGroup interface -- it is read-only.
 *
 * @author Trevor Morris
 * @author Arturo Bejar
 * @version 1.0
 */
public interface jCapabilityGroup {
    /**
     * Get the capability of the given type, where type is specified by
     * a type name.
     *
     * @param typeName  Name of a Java interface or class
     * @return capability of given type, or null if the group doesn't contain
     *  a capability of the given type
     * @throws IllegalArgumentException if 'typeName' is not the name of a
     *  Java class or interface
     */
    public Object capabilityOfType(String typeName);
}

