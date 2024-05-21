/*
  jEditableCapabilityGroup.java -- Capability group management classes

  Copyright 1998 Electric Communities, all rights reserved worldwide.
  Trevor Morris & Arturo Bejar
  Thu Nov 20 11:18:42 1997
*/

package ec.plubar;

import java.util.Hashtable;
import java.io.Serializable;
import ec.e.run.EObject;
import ec.e.run.EStone;
import ec.e.run.RtDeflector;
import ec.e.run.RtStateUpgradeable;
import ec.e.run.RtTether;
import ec.e.run.Trace;


/**
 * An editable capability group implements the jCapabilityGroup interface, but
 * also allows adding of capabilities.
 * <p>
 * It has a method which returns an read-only jCapabilityGroup reference,
 * which does not allow updating
 *
 * @author Trevor Morris
 * @author Arturo Bejar
 * @version 1.0
 */
public class jEditableCapabilityGroup 
implements jCapabilityGroup, Serializable, RtStateUpgradeable {
    /** Number of entries allocated by hasthable */
    static final int MAX_ENTRIES = 15;
    
    /** Hashtable of capabilities */
    private Hashtable myCapabilities = null;
    
    /** SoulState of unum that this CapabilityGroup belongs to */
    private SoulState mySoulState;
    
    /** Create a new editable capability group. */
    public jEditableCapabilityGroup() {
    }
    
    /**
     * Set the capability group's soul state.
     *
     * Called from the SoulState at setCapabilityGroup time.
     */ 
    protected void setSoulState(SoulState soulState) {
        if (mySoulState == null) {
            mySoulState = soulState;
        } else {
            throw new RuntimeException("Cannot set SoulState twice");
        }
    }
    
    /**
     * Add a capability to the group, under the named type..
     *
     * @param capability  The capability to add
     * @param typeName  The type name of the type which can be used to retrieve
     *  this capability from the table ('capability' must be an instance of the
     *  type).
     * @throws IllegalArgumentException if 'capability' is not an instance of
     *  the named type, or if 'typeName' is not the name of a type.
     */
    public void addCapabilityOfType(Object capability, String typeName) {
        Class capabilityClass;
        try {
            if (capability == null) {
                throw new IllegalArgumentException(
                    "Tried to add null capability: " + typeName);
            }
            
            if (capability instanceof EObject) {
                capabilityClass = Class.forName(typeName + "_$_Intf");
            } else {
                capabilityClass = Class.forName(typeName);
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(typeName +
                " is not a class or interface: " + e);
        }
        
        if (!capabilityClass.isInstance(capability)) {
            throw new IllegalArgumentException(capability +
                " is not of type " + capabilityClass);
        }
        if (myCapabilities == null) {
            myCapabilities = new Hashtable(MAX_ENTRIES);
        }
        
        myCapabilities.put(typeName, capability);
    }
    
    /**
     * Add a capability to the group, under the named type.
     *
     * @param capability  The capability to add
     * @param typeName  The type name of the type which can be used to retreive
     *  this capability from the table ('capability' must be an instance of the
     *  type).
     * @throws IllegalArgumentException if 'capability' is not an instance of
     *  'typeName', or if 'typeName' is not the name of a type.
     */
    public void makeAndAddCapabilityOfType(EObject capability,
                                           String typeName) {
        
        if (capability == null) {
            throw new IllegalArgumentException(
                "Tried to add null capability: " + typeName);
        }
        
        Class capabilityClass;
        try {
            capabilityClass = Class.forName(typeName + "_$_Intf");
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(typeName +
                " is not a class or interface: " + e);
        }
        
        if (!capabilityClass.isInstance(capability)) {
            throw new IllegalArgumentException(capability +
                " is not of type " + capabilityClass);
        }
        if (myCapabilities == null) {
            myCapabilities = new Hashtable(MAX_ENTRIES);
        }
        
        /* Check for existence; if already there then set the target of
           existing facet recouper. */
        jFacetRecouper theRecouper =
            (jFacetRecouper)myCapabilities.get(typeName);
        
//KSSHack        boolean isUnumRouter = capability instanceof Unum;
        boolean isUnumRouter = false;
        
        if (theRecouper == null) {
            /* No existing recouper, make new one. */
            if (!isUnumRouter) {
                theRecouper = new jFacetRecouper(typeName);
            } else {
                
               String unumImplName = ((Object)capability).getClass().getName();
               String unumClassName = 
                   unumImplName.substring(0, unumImplName.length() - 7);
               theRecouper = new jFacetRecouper(unumClassName);
            }
            /*  Put recouper in table */
            myCapabilities.put(typeName, theRecouper);
        }
        
        /* UnumRouter dependent code */
        if (isUnumRouter) {
            theRecouper.setCapability((RtTether)capability);                
        } else {
            /* All other capabilities */
            
            /* Make stonecast of the target to type, setting */
            Object key = new Object();
            
            RtDeflector deflector = 
                RtDeflector.construct(typeName, 
                                      new EStone((RtTether) capability),
                                      key);
            
            RtDeflector.setSerializableDelegate(deflector, key, theRecouper);
            
            /* Make the stonecast the recouper's capability. */
            theRecouper.setCapability(deflector);
        }
    }
    
    /**
     * Get the capability of the given type, where type is specified by
     *   a type name
     *
     * @param typeName  Name of a Java interface or class, used in capability
     *   lookup.
     * @return capability of given type, or null if the group doesn't contain
     *   a capability of the given type
     */
    public Object capabilityOfType(String typeName) {
        // Maintaing generic capability objects for backwards compatibility
        // with Robj use of capability group.
        Object toGet = myCapabilities.get(typeName);
        
        if (null == toGet) {
            Trace.serialstate.debugm("-----------------------------");
            Trace.serialstate.debugm("  Unexpected null in contents: " +
                                     typeName);
            Trace.serialstate.debugm("    "+ myCapabilities);
            throw new NullPointerException("No such capability in group");
        }
        
        if (null != toGet && toGet instanceof jFacetRecouper) {
            jFacetRecouper theRecouper = (jFacetRecouper) toGet;
            return theRecouper.getCapability();
        } else {
            return toGet;
        }
    }
    
    
    /**
     * Return 'this' table "stonecast" to jCapabilityGroup - i.e. a
     * read-only version of ourselves.
     */
    public jCapabilityGroup asCapabilityGroup() {
        return new jStonecastCapabilityGroup(this);
    }
    
    public String toString() {
        return " jEditableCapabilityGroup \n   " + myCapabilities;
    }
}
