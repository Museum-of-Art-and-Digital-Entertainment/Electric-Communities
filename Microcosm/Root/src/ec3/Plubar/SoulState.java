/*
  SoulState.java -- The inner heart of the unum.

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

/**                                                          
 * SoulState.java
 * Table that holds all of the state bundles for any una 
 * @author Arturo Bejar
 *
 * Copyright 1997, Electric Communities, all rights reseved 
 * Propietary and confidential                              
 */

package ec.plubar;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputValidation;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Hashtable;

import ec.e.run.EObject;
import ec.e.run.RtStateUpgradeable;
import ec.e.run.RtDoNotEncode;
import ec.e.serialstate.StateOutputStream;
import ec.vcache.ClassCache;

/**
 * Class to hold onto the state bundles of an unum.
 *
 * @author Arturo Bejar
 * @version 1.0
 */
public class SoulState implements Serializable, RtStateUpgradeable,
    RtDoNotEncode, ObjectInputValidation {

    /** Maximum number of state bundles to keep hashtables small */
    private static final int MAX_BUNDLES = 15;
    
    /** Method name used for CRAPI lookup for instantiation */
    private static final String CREATE_METHOD_NAME = "createUnum";
    
    /** Parameter used for CRAPI lookup for instantiation */
    private static Class[] SOUL_PARAMETER;
    
    /** Name of the class of unum to instantiate */
    private String myUnumClassName = null;
    
    /** Table for bundles indexed by class name string */
    /* XXX CLEANUP: Identifying state bundles by their class is arguably bogus
       and certainly a "per" error. Ideally they should be identified by
       ingredient role name in the brave new world of Plubar. */
    private Hashtable myStateBundles;
    
    /** Capability group for this unum */
    private jEditableCapabilityGroup myCapabilityGroup;
    
    /** Controls whether this unum is instantiated on decode or not */
    /* Use with extreme caution. Put in for the realm. The problem with doing
       this is that the init will not publish any other capabilities that other
       una will be expecting; in the case of the realm it will be OK since we
       will force an instantiation before continuing to read the stream. */
    private boolean myInstantiateOnReadObject = true;
    
    transient private UnumSoul myUnumSoul;
    private Object myUnumKey;
    
    /**
     * Construct a new soul state with no class and an initially empty bundle
     * table.
     */
    public SoulState() {
        myStateBundles = new Hashtable(MAX_BUNDLES);        
    }

    /**
     * Return the state bundle associated with a particular class.
     *
     * @param bundleClassName  The name of the class of state bundle you want
     * @return state bundle of that class or null if there isn't one.
     */
    public istBase get(String bundleClassName) {
        return (istBase) myStateBundles.get(bundleClassName);        
    }

    /**
     * Get a capability from the unum's capability group.
     *
     * @param capabilityName  Name of Java type of capability to look up.
     * @return  Capability of the given type, or null if the group doesn't
     *  contain a capability of that type.
     */
    public EObject getCapability(String capabilityName) {
        return (EObject) myCapabilityGroup.capabilityOfType(capabilityName);
    }
    
    /**
     * Return the soul's capability group.
     */ 
    public jEditableCapabilityGroup getCapabilityGroup() {
        return myCapabilityGroup;
    }
    
    /**
     * Control whether the unum from this SoulState will be instantiated
     * on readObject.
     *
     * @param value  Setting for the instantiate-on-read-object flag
     */
    public void instantiateOnReadObject(boolean value) {
        myInstantiateOnReadObject = value;
    }

    /**
     * Instantiate unum using state bundles.
     */
    private void makeUnum() {
        if (myUnumClassName != null) {
            if (SOUL_PARAMETER == null) {
                SOUL_PARAMETER = new Class[1];
                SOUL_PARAMETER[0] = this.getClass();
            }
            try {
                Class unumClass = ClassCache.forName(myUnumClassName);
                Method unumCreateMethod = 
                    unumClass.getDeclaredMethod(CREATE_METHOD_NAME,
                                                SOUL_PARAMETER);
                Object param[] = { this };
                unumCreateMethod.invoke(null, param);               
            } catch (Throwable ex) {
                // XXX Another bad printStackTrace() call
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Add an element to the state bundle table.
     *
     * @param stateBundle  A state bundle object to add to the table
     *
     * Since the table is indexed by class name, you can only put one of each
     * class of state bundle into the SoulState.
     */
    public void put(istBase stateBundle) {
        stateBundle.setCapabilityGroup(myCapabilityGroup);
        myStateBundles.put(stateBundle.getClass().getName(), stateBundle);
    }
    
    /**
     * Add an element to the state bundle table based on role name.
     *
     * @param stateBundle  A state bundle object to add to the table
     *
     * Since the table is indexed by class name, you can only put one of each
     * class of state bundle into the SoulState.
     */
    public void put(String ingredientRole, istBase stateBundle) {
        stateBundle.setCapabilityGroup(myCapabilityGroup);
        myStateBundles.put(ingredientRole, stateBundle);
    }
    
    /**
     * Read method for Java serialization.
     *
     * Reads in the unum's name and state bundles. Registers validation for
     * unum instantiation.
     *
     * @exception IOException from readObject() or readBoolean()
     * @exception ClassNotFoundException from readObject() or readBoolean()
     */ 
    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        myUnumClassName = (String)in.readObject();
        myStateBundles = (Hashtable)in.readObject();
        myInstantiateOnReadObject = in.readBoolean();
        
        in.registerValidation(this, StateOutputStream.UNUM_VALIDATE);
    }
    
    /**
     * Set the soul's capability group.
     *
     * @param capabilityGroup  What to set it to
     */ 
    public void setCapabilityGroup(jEditableCapabilityGroup capabilityGroup) {
        if (myCapabilityGroup == null) {
            myCapabilityGroup = capabilityGroup;
            myCapabilityGroup.setSoulState(this);
        } else {
            throw new RuntimeException("Cannot set capabilityGroup twice");
        }
    }
    
    /**
     * Pair up a SoulState with an UnumSoul and class.
     *
     * @param soul  The UnumSoul to pair up with
     * @param unumClass  The class of the unum whose soul we are to be
     */
    public void setUnumSoul(UnumSoul soul, Class unumClass) {
        myUnumSoul = soul;
        String presenceClassName = unumClass.getName();
        /* XXX What is the magic number 15 doing?  We're obviously doing some
           name mangling but what is it that is supposed to be lopped off? */
        myUnumClassName =
            presenceClassName.substring(0, presenceClassName.length() - 15) +
            "$ui";
        myUnumSoul.setSoulState(this);      
    }
    
    /**
     * Store the UnumKey (just an Object) of the unum that this
     * SoulState belongs to.
     *
     * @param unumKey The unum's key.
    */
    public void setUnumKey(Object unumKey) {
      myUnumKey = unumKey;
    }

    /**
     * Validate the object by making the unum if appropriate. Part of the
     * Java serialization interface.
     *
     * Called after all of the souls have been loaded, all the channels
     * for capabilities have been set up now.
     */
    public void validateObject() throws InvalidObjectException {
        if (myInstantiateOnReadObject) {
            makeUnum();
        }
    }
    
    /**
     * Write method for Java serialization.
     *
     * Writes the unum's class name and the state bundle hashtable.
     */ 
    private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
        out.writeObject(myUnumClassName);
        out.writeObject(myStateBundles);
        out.writeBoolean(myInstantiateOnReadObject);
    }
}
