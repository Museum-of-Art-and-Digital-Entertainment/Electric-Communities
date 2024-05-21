package ec.e.run;

import ec.vcache.ClassCache;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;

import java.io.Serializable;

/**
 * An EEnvironment is a steward passed to the first guest when a Vat
 * is launched.  Any guest that gets an EEnvironment is assumed to be
 * fully trusted, and so is part of the TCB.  Such guests should only
 * hand out individual capabilities from the EEnvironment to any
 * guests that are less than fully trusted. <p>
 *
 * An EEnvironment contains a set of properties and an array of
 * argument strings presumed to reflect the command line of this
 * incarnation of this process.  Copies are retrieved by props() and
 * args() (Copies are made to prevent shared side effects.  Once in
 * the real EVat, the args array will no longer need to be copied.).
 * Individual properties can also be retrieved by name (with optional
 * default values) by the getProperty() functions. <p>
 *
 * The EEnvironment also provides access to the Vat it inhabits. <p>
 *
 * Most importantly, the EEnvironment provides access to magic
 * powers.  A magic power is a capability providing in-vat
 * all-powerful access to some out-of-vat system service, like the
 * file system.  Magic powers themselves should typically not be
 * handed too far.  Rather, magic powers should provide a protocol for
 * deriving lesser powers, such as access to a subdirectory, which are
 * then handed to various parties. <p>
 *
 * Magic powers are generally sturdy, regenerating the out-of-vat
 * service and hooking it back up as needed.  Lesser powers may be
 * sturdy or fragile, on a case by case basis.  Sturdy lesser powers
 * often reconstruct themselves by encapsulating the magic power from
 * which they are derived, and rederiving themselves accordingly.
 */
public class EEnvironment {
    static private boolean ThereCanBeOnlyOne = false;

    private String myArgs[];
    private Properties myProps;
    private Vat myVat;
    private Hashtable myMagicPowers;
    // This is the top-level state bundle object to be serialized by the 
    // StateTimeMachine
    private Serializable mySuperBundle;

    private EEnvironment() {}
    
    /**
     * @param args The command line arguments after properties are
     * stripped off
     * @param props Properties specified by the command line
     * @param vat The Vat we inhabit
     * @param magicLoader The ClassLoader from which we load requested
     * MagicPowerMaker classes.  While in Java 1.0.2, this must always
     * be the system ClassLoader.
     */
    public EEnvironment(String[] args, Properties props, 
                 Vat vat, ClassLoader magicLoader) throws OnceOnlyException {
        if (ThereCanBeOnlyOne) {
            throw new OnceOnlyException("Already initialized");
        }
        ThereCanBeOnlyOne = true;
        if (new Object().getClass().getClassLoader() != magicLoader) {
            throw new RuntimeException("unimplemented: other ClassLoaders");
        }
        myArgs = args;
        myProps = props;
        myVat = vat;
        // The following is set to null, until it is set (on startup), by the 
        // 'setSuperBundle' call below.  This means that until startup is 
        // completed there is nothing to save by default
        mySuperBundle = null;                
        myMagicPowers = new Hashtable();
        myMagicPowers.put("CheckpointTether",
                          new SettableTether(vat, null));
    }

    /**
     * Reset some state to reflect the incarnation we are being
     * revived into.
     *
     * @param args revival command line args (after properties are
     * stripped off)
     * @param props properties specified by the revival command line
     * @param checkpoint the new value for the special magic power
     * named "CheckpointTether", and used by the TimeMachine.  XXX
     * This is typed as 'Object' to avoid having this module depend on
     * ec.e.quake.
     */
    /*package*/ void revive(String[] args,
                            Properties props,
                            Object checkpoint) {
        myArgs = args;
        myProps = props;
        SettableTether checkpointTether
            = (SettableTether)myMagicPowers.get("CheckpointTether");
        checkpointTether.set(checkpoint);
    }

    /**
     * @return The command line arguments after properties are
     * stripped off.  If this process is a revival
     * from an earlier checkpoint, args() will reflect the revival
     * command line.
     */
    public String[] args() {
        String[] result = new String[myArgs.length];
        System.arraycopy(myArgs, 0, result, 0, myArgs.length);
        return result;
    }

    /**
     * @return Properties specified by the command line of the current
     * incarnation of the process.  Ie, if this process is a revival
     * from an earlier checkpoint, props() will reflect the properties
     * specified by the revival command line.
     */
    public Properties props() {
        return (Properties)myProps.clone();
    }

    /**
     * @see java.util.Properties#getProperty
     */
    public String getProperty(String key) {
        return myProps.getProperty(key);
    }

    /**
     * @see java.util.Properties#getProperty
     */
    public String getProperty(String key, String def) {
        return myProps.getProperty(key, def);
    }

    public void setProperty(String key, String value) {
        myProps.put(key, value);
    }

    /**
     * @see java.util.Properties#propertyNames
     */
    public Enumeration propertyNames() {
        return myProps.propertyNames();
    }

    /**
     * @return The Vat we inhabit
     */
    public Vat vat() {
        return myVat;
    }

    /**
     * Set the top-level object for serialization. 
     * @param topObject the Serializable that is the top-level object to
     * serialize on save
     */
    public void setSuperBundle(Serializable topObject)  {
      mySuperBundle = topObject;
    }

    /**
     * Get the top-level object for state bundle serialization.
     * @return Serializable that is the top-level object for serialization
     */
    public Serializable getSuperBundle()  {
      return mySuperBundle;
    }

    /**
     * A guest asks for a magic power by saying the name of the crew
     * class that creates the power.  The EEnvironment will summon
     * that class into existence if needed to create the power.  Many
     * magic power classes, such as EDirectoryRootMaker, provide a
     * static summon() method as a convenient wrapper around
     * magicPower().
     *
     * @see ec.e.file.EDirectoryRootMaker
     * @param powerMakerClassName the name of the crew class that
     * makes the power being requested.  This class must implement
     * MagicPowerMaker, and must be public and have a public
     * no-argument constructor.
     * @return The magic power itself.  This provides
     * all-encompassing (but typically subdividable) authority over
     * some system service.  The MagicPowerMaker should typically
     * arrange for the power to be sturdy.
     * @exception ClassNotFoundException thrown if the powerMaker
     * class could not be found.
     * @exception IllegalAccessException thrown if the powerMaker
     * class isn't public, or if its newInstance() method isn't
     * public.
     * @exception InstantiationException thrown if instantiation fails
     * for some other reason.
     */
    public Object magicPower(String powerMakerClassName)
         throws ClassNotFoundException, IllegalAccessException,
         InstantiationException
    {
        Object result = myMagicPowers.get(powerMakerClassName);
        if (result != null) {
            return result;
        }
        /*
         * Java 1.0.2 has no public ClassLoader.loadClass(), so this
         * doesn't actually hold a ClassLoader.  Rather, it just calls
         * Class.forName().
         */
        Class makerClass = ClassCache.forName(powerMakerClassName);
        MagicPowerMaker maker = (MagicPowerMaker)makerClass.newInstance();
        result = maker.make(this);
        myMagicPowers.put(powerMakerClassName, result);
        myMagicPowers.put(makerClass, result);
        // The line above was originally put in because the 1.1.3 VM was throwing out
        // a class we wanted kept.  The line has been commented out because someone
        // discovered that you could turn off class garbage collection - Harry
        // 971223 KJD Uncommented the line again.
        return result;
    }

//XXXABS start Existing Registrar problem

    /**
     * A guest asks for a magic power by saying the name of the crew
     * class that creates the power.  The EEnvironment will summon
     * that class into existence if needed to create the power.  Many
     * magic power classes, such as EDirectoryRootMaker, provide a
     * static summon() method as a convenient wrapper around
     * magicPower(). 
     * @return the valid magic power which is either the on provided, or the
     *   existing one if the one provided is equivalent to it.
     * @exception ClassNotFoundException thrown if the powerMaker
     *   class could not be found.
     * @exception IllegalAccessException thrown if the powerMaker
     *   class isn't public, or if its newInstance() method isn't
     *   public. 
     * @exception InstantiationException thrown if instantiation fails
     *   for some other reason.
     * @exception 
     */

    public Object establishMagicPower(String powerMakerClassName, 
                                      Object providedPower)
         throws ClassNotFoundException, 
                        IllegalAccessException,
                        InstantiationException {

        Class makerClass = Class.forName(powerMakerClassName);

        ComparingMagicPowerMaker comparingMaker = 
            (ComparingMagicPowerMaker)makerClass.newInstance();

        Object result = myMagicPowers.get(powerMakerClassName);

        if (result != null) {
          // There is a magic power already in the table, so compare if
          // they are equivalent.

          if (comparingMaker.areEquivalent(result, providedPower)) {
            // Return the original power
            return result;
            
          } else {// Comparison failed
            throw new IllegalAccessException(
              "Power already there not equivalent to power provided");
          }
        } else {
            if (comparingMaker.isValidPower(providedPower)) {
            myMagicPowers.put(powerMakerClassName, providedPower);
            return providedPower;
            } else  {
            throw new IllegalAccessException(
              "Invalid power provided");
            }
        }
    }
       
//XXXABS end Existing Registrar problem


    protected void finalize() {
        System.out.println(">>> FINALISING ENVIRONMENT INSTANCE");
    }
}
