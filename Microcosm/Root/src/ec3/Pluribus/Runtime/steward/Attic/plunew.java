package ec.pl.runtime;

import ec.util.EThreadGroup;
import ec.util.NestedException;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.io.IOException;

import ec.e.net.ProxyInterest;
import ec.e.net.ProxyDeathHandler;
import ec.e.net.EConnection;

import ec.e.openers.RootClassRecipe;
import ec.e.openers.OpenerRecipe;
import ec.e.openers.FieldKnife;
import ec.e.openers.Surgeon;

public class Plu {
    static public Object getSelf(String name) {
        return(getSelfForClass(forName(name)));
    }

    static public Object getSelfForClass(Class theClass) {
        try {
            Field f = theClass.getField("self");
            return f.get(null);
        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    static public Class forName(String className) {
        try {
            return(Class.forName(className));
        } catch (ClassNotFoundException ex) {
            return(null);
        }
    }
}

public class pl$_deliverAtt {
    boolean presenceScope;
    String from;
    String toIngredient;
    String toMethod;

    public pl$_deliverAtt(boolean presenceScope, String from, String toIngredient,
                   String toMethod) {
        this.presenceScope = presenceScope;
        this.from = from;
        this.toIngredient = toIngredient;
        this.toMethod = toMethod;
    };
}

public class pl$_ingredient {
    pl$_kind kind;
    String name;

    public pl$_ingredient(pl$_kind kind, String name) {
        this.kind = kind;
        this.name = name;
    }
}

public class pl$_ingredientImpl {
    Hashtable attributes;
    pl$_kind ingredientKind;
    pl$_neighbor neighbors[];
    String stateBundles[];
    Class code;

    public pl$_ingredientImpl(Hashtable attributes,
                       pl$_kind ingredientKind,
                       pl$_neighbor neighbors[],
                       String stateBundles[],
                       Class code) {
        this.attributes = attributes;
        this.ingredientKind = ingredientKind;
        this.neighbors = neighbors;
        this.stateBundles = stateBundles;
        this.code = code;
    }

    public pl$_ingredientImpl() { }
}

public class pl$_ingredientRole {
    String name;
    pl$_template template;

    public pl$_ingredientRole(String name, pl$_template template) {
        this.name = name;
        this.template = template;
    }
}

public class pl$_kind {
    Hashtable attributes;
    String stateBundles[]; //KSS
    Class kindInterface;

  public pl$_kind(Hashtable attributes, String stateBundles[], //KSS
            Class kindInterface) {
        this.attributes = attributes;
        this.stateBundles = stateBundles; //KSS
        this.kindInterface = kindInterface;
    }

    public pl$_kind() { }
}

public class pl$_mapAtt {
    boolean neighborScope;
    String from;
    String to;

    public pl$_mapAtt(boolean neighborScope, String from, String to) {
        this.neighborScope = neighborScope;
        this.from = from;
        this.to = to;
    }
}

public class pl$_neighbor {
    String name;
    pl$_kind neighborKind;
    boolean isPlural;
    boolean isPresence;

    public pl$_neighbor(String name, pl$_kind neighborKind, boolean isPlural,
                 boolean isPresence) {
        this.name = name;
        this.neighborKind = neighborKind;
        this.isPlural = isPlural;
        this.isPresence = isPresence;
    }
}

public class pl$_presence {
    String name;
    pl$_presenceStructure structure;
    boolean isPrime;

    public pl$_presence(String name, pl$_presenceStructure structure,
            boolean isPrime) {
        this.name = name;
        this.structure = structure;
        this.isPrime = isPrime;
    }
}

public class pl$_presenceImpl {
    Hashtable attributes;
    pl$_presenceStructure structure;
    pl$_ingredientRole[] roles;

    public pl$_presenceImpl(Hashtable attributes, pl$_presenceStructure structure,
                     pl$_ingredientRole[] roles) {
        this.attributes = attributes;
        this.structure = structure;
        this.roles = roles;
    }

    public pl$_presenceImpl() { }
}

public class pl$_presenceRole {
    String name;
    pl$_presenceImpl presenceImpl;

    public pl$_presenceRole(String name, pl$_presenceImpl presenceImpl) {
        this.name = name;
        this.presenceImpl = presenceImpl;
    }
}

public class pl$_presenceStructure {
    Hashtable attributes;
    pl$_kind presenceKind;
    pl$_ingredient ingredients[];
    pl$_deliverAtt deliverAtts[];

    public pl$_presenceStructure(Hashtable attributes,
                          pl$_kind presenceKind,
                          pl$_ingredient ingredients[],
                          pl$_deliverAtt deliverAtts[]) {
        this.attributes = attributes;
        this.presenceKind = presenceKind;
        this.ingredients = ingredients;
        this.deliverAtts = deliverAtts;
    }

    public pl$_presenceStructure() { }
}

public class pl$_template {
    pl$_ingredientImpl ingredientImpl;
    pl$_mapAtt[] mapAtts;

    public pl$_template(pl$_ingredientImpl ingredientImpl,
                 pl$_mapAtt[] mapAtts) {
        this.ingredientImpl = ingredientImpl;
        this.mapAtts = mapAtts;
    }

    public pl$_template() { }
}

public class pl$_unumImpl {
    Hashtable attributes;
    pl$_unumStructure structure;
    pl$_presenceRole[] roles;

    public pl$_unumImpl(Hashtable attributes, pl$_unumStructure structure,
                 pl$_presenceRole[] roles) {
        this.attributes = attributes;
        this.structure = structure;
        this.roles = roles;
    }

    public pl$_unumImpl() { }
}

public class pl$_unumStructure {
    Hashtable attributes;
    pl$_kind unumKind;
    pl$_presence presences[];

    public pl$_unumStructure(Hashtable attributes, pl$_kind unumKind,
                      pl$_presence presences[]) {
        this.attributes = attributes;
        this.unumKind = unumKind;
        this.presences = presences;
    }

    public pl$_unumStructure() { }
}

/** 
 * Ingredient Base Class
 *
 */
public eclass Ingredient {
    protected PresenceEnvironment environment;
    
    public Ingredient(PresenceEnvironment environment) {
        this.environment = environment;
    }
    
    protected final UnumInterest registerInterestInUnum(EObject unum, Object data) {
        // This cast will be done asynchronously later, let's make sure
        // it works now, so we choke as early as possible
        UnumDeathHandler handler = (UnumDeathHandler)this;
        return new UnumInterest(unum, handler, data);
    }
    
    protected final Unum invalidateAndMakeNewUnum ()  {
        return ((PresenceInterface)environment.presence).invalidateAndMakeNewUnum(SecretKey.theSecretKey);
    }
    
    protected final void invalidate ()  {
        ((PresenceInterface)environment.presence).invalidate(SecretKey.theSecretKey);
    }
}

public einterface UnumDeathHandler {
    noteUnumDeath(Object unum, Object data);
}

interface UnumInterestHelper
{
    void turnWaterIntoWine (EObject water, UnumInterest interest);
}   

eclass EUnumInterestHelper implements UnumInterestHelper
{
    EUnumInterestHelper()  {
    }
    
    local void turnWaterIntoWine (EObject water, UnumInterest interest)  {
        ewhen water (Object wine) {
            ////System.out.println("EUnumInterestHelper got " + wine + " from " + water);
            interest.registerInterestInObject(wine);
        }
    }   
} 
  
public class UnumInterest
{
    private Unum myUnum;
    private UnumDeathHandler myHandler;
    private Object myData;
    private boolean unregistered = false;
    static private UnumInterestHelper TheUnumInterestHelper;
     
    public UnumInterest (EObject unum, UnumDeathHandler handler, Object data) {
        myHandler = handler;
        myData = data;
        if (unum instanceof Unum)  {
            registerInterestInUnum((Unum)unum);
        }
        else  {
            if (TheUnumInterestHelper == null)  {
                TheUnumInterestHelper = new EUnumInterestHelper();
            }
            TheUnumInterestHelper.turnWaterIntoWine(unum, this);
        }
    }
    
    void noteUnumDeath (Unum unum) {
        if (unregistered == false)  {
            myHandler <- noteUnumDeath(unum, myData);
        }
    }
    
    public void unregisterInterestInUnum () {
        if (myUnum != null)  {
            myUnum <- unregisterInterestInUnum(this);
        }
        myUnum = null;
        unregistered = true;
    }
    
    void registerInterestInObject (Object unum)  {
        if (unregistered == false) {
            try {
                registerInterestInUnum((Unum)unum);
            } catch (Exception e) {
                System.err.println("Error registering interest in Unum:\nUnum " +
                                   unum + "\nFor handler " + myHandler + "\nWith data " + myData);
                EThreadGroup.reportException(e);
            }
        }
    }   
    
    private void registerInterestInUnum (Unum unum)  {
        myUnum = unum;
        myUnum <- registerInterestInUnum(this);
    }   
}

class UnumSecurityViolationException extends RuntimeException
{
    UnumSecurityViolationException(String msg) {
        super(msg);
    }
}

class Farbulator
{
    private static OpenerRecipe openerRecipe;

    static void invalidateObject (Object obj)  {
        // After Quake this will be null too
        if (openerRecipe == null)  {
            openerRecipe = new OpenerRecipe(null, RootClassRecipe.THE_ONE);
        }
        
        Surgeon surgeon = (Surgeon)openerRecipe.forObject(obj);
        if (surgeon == null)  {
            // XXX - Throw? Just complain?
        }
        int size = surgeon.numInstVars();
        for (int i = 0; i < size; i++) {
            FieldKnife knife = surgeon.instVarOpener(i);
            String signature = knife.signature();
            // Only clear if it is Object type (we actually only really
            // care about Ingredients, but this is quicker and harmless) 
            switch (signature.charAt(0)) {
                case 'L':
                case '[':
                    knife.set(obj, null);
            }
        }
    }   
    
    static Vector instanceObjectsForObject (Object obj)  {
        // After Quake this will be null too
        if (openerRecipe == null)  {
            openerRecipe = new OpenerRecipe(null, RootClassRecipe.THE_ONE);
        }
        
        Surgeon surgeon = (Surgeon)openerRecipe.forObject(obj);
        if (surgeon == null)  {
            return null;
            // XXX - Throw? Just complain?
        }
        int size = surgeon.numInstVars();
        if (size == 0)  {
            return null;
        }
        Vector objects = new Vector(size);
        for (int i = 0; i < size; i++) {
            FieldKnife knife = surgeon.instVarOpener(i);
            String signature = knife.signature();
            // Only add if it is Object (non Array) type (we actually only really
            // care about Ingredients, but this is quicker and harmless) 
            switch (signature.charAt(0)) {
                case 'L':
                    objects.addElement(knife.get(obj));
            }
        }
        return objects;
    }   
}

// XXX - Need Recipe registered with Checkpoint system for this
// so that on revival it reassigns the value to the new static!
class SecretKey
{
    static SecretKey theSecretKey = new SecretKey();
    private SecretKey() {
    }

    static void validateSecretKey (SecretKey secret, String errorMessage)  {
        if (!(secret instanceof SecretKey)) {
            throw new UnumSecurityViolationException(errorMessage);
        }
    }       
}

public class ClientPresenceTracker
{
    private long mySwissNumber;
    private EObject myChannel = null;
    private EDistributor myDistributor = null;
    
    private ClientPresenceTracker()  {
    }

    public ClientPresenceTracker (long swissNumber, EObject channel, EDistributor distributor)  {
        mySwissNumber = swissNumber;
        myChannel = channel;
        myDistributor = distributor;
    }

    public long getSwissNumber()  {
        return mySwissNumber;
    }

    public EObject getChannel()  {
        return myChannel;
    }

    public EDistributor getDistributor()  {
        return myDistributor;
    }
        
    public String toString ()  {
        return super.toString() + ": Swiss Number " + mySwissNumber
            + ", myChannel " + myChannel + ", myDistributor " + myDistributor;
    }   
}   

public interface SecretUnum  {
    ClientPresenceTracker makeClientPresenceTracker (long swissNumber);
    Unum invalidateAndMakeNewUnum(SecretKey secret);
    void invalidate(SecretKey secret);
    void killUnum(SecretKey secret);
    void setNewPresence (Presence presence, SecretKey secret);
    UnumSoul getUnumSoul (SecretKey secret);
    void createUnumSoul (SecretKey secret);
}

public einterface Unum {
    registerInterestInUnum(UnumInterest interest);
    unregisterInterestInUnum(UnumInterest interest);
}

public class UnumSoul implements RtDoNotEncode {
    private SecretUnum myUnum;
    
    UnumSoul (SecretUnum unum)  {
        myUnum = unum;
    }
    
    public void setUnum (SecretUnum unum, SecretKey secret)  {
        SecretKey.validateSecretKey(secret, "Attempt to call UnumSoul setUnum");
        myUnum = unum;
    }   

    public Unum getUnum ()  {
        return (Unum)myUnum;
    }       
    
    public void killUnum ()  {
        myUnum.killUnum(SecretKey.theSecretKey);
    }   
}

public interface UnumKillHandler
{
    void noteUnumKilled();
}   

public eclass BaseUnum implements Unum, SecretUnum {        
    // XXX - Needs to be WEAK!!!
    private Vector /* UnumInterest */ unumInterests;
    private boolean unumIsDead = false;
    private UnumSoul soul;
    
    emethod registerInterestInUnum(UnumInterest interest) {
        if (unumIsDead) {
            interest.noteUnumDeath(this);
        }
        else {
            if (unumInterests == null) {
                unumInterests = new Vector(10);
            }
            unumInterests.addElement(interest);
        }
    }
    
    emethod unregisterInterestInUnum(UnumInterest interest) {
        if (unumInterests != null) {
            unumInterests.removeElement(interest);
        }
    }
    
    emethod indicateUnumDeath (SecretKey secret) {
        SecretKey.validateSecretKey(secret, "Attempt to call indicateUnumDeath");
        internalIndicateUnumDeath();
        unumIsDead = true;   
    }
    
    private void internalIndicateUnumDeath () {
        // For a Client, Unum Death is the real Death
        PresenceInterface presence = (PresenceInterface)getPresence();
        if (presence == null)  {
            System.err.println("Warning: Attempt to indicate Unum death for Unum that is already invalid");
            return;
        }      
        if (presence.isClient())  {
            notifyIngredientsOfDeath();
        }
        if (unumInterests != null) {
            int size = unumInterests.size();
            for (int i = 0; i < size; i++) {
                UnumInterest interest = (UnumInterest)unumInterests.elementAt(i);
                if (interest != null) {
                    interest.noteUnumDeath(this);
                }
            }
        }
        unumInterests = null; // Early cleanup for GC
    }
   
    local ClientPresenceTracker makeClientPresenceTracker (long swissNumber)  {
        throw new RuntimeException("Method makeClientPresenceTracker not implemented in Unum");
    }
 
    local Unum invalidateAndMakeNewUnum (SecretKey secret)  {
        SecretKey.validateSecretKey(secret, "Attempt to call invalidateAndMakeNewUnum");
        internalIndicateUnumDeath();
        Unum newUnum = null;
        try {
            newUnum = (Unum)clone();    
        } catch (CloneNotSupportedException e) {
            System.err.println("Error! Can't clone Unum Router for " + this);
            // XXX - Never happen
        }
        if (soul != null)  {
            soul.setUnum((SecretUnum)newUnum, secret);
        }
        invalidateInternalState();
        return newUnum;
    } 
 
    local void killUnum (SecretKey secret)  {
        SecretKey.validateSecretKey(secret, "Attempt to call killUnum");
        PresenceInterface presence = (PresenceInterface)getPresence();
        if (presence == null)  {
            System.err.println("Warning: Attempt to kill Unum that is already invalid");
            return;
        }      
        notifyIngredientsOfDeath();
        presence.invalidate(secret);        
    } 
    
    local void invalidate (SecretKey secret)  {
        SecretKey.validateSecretKey(secret, "Attempt to call internalInvalidate");
        internalIndicateUnumDeath();
        invalidateInternalState();
    } 
    
    private void invalidateInternalState()  {
        unumIsDead = true;   
        soul = null;
        Farbulator.invalidateObject(this);
    }   
    
    private void notifyIngredientsOfDeath ()  {
        Vector instanceObjects = Farbulator.instanceObjectsForObject(this);
        if (instanceObjects != null) {
            int size = instanceObjects.size();
            for (int i = 0; i < size; i++) {
                Object object = instanceObjects.elementAt(i);
                if (object instanceof UnumKillHandler) {
                    ((UnumKillHandler)object).noteUnumKilled();
                }
            }
        }
        
    }   
    
    protected Presence getPresence ()  {        
        throw new RuntimeException("Method getPresence not implemented in Unum");    
    } 
    
    protected void setNewPresence (Presence presence)  {        
        throw new RuntimeException("Method setNewPresence not implemented in Unum");    
    } 
    
    local void setNewPresence (Presence presence, SecretKey secret)  {
        SecretKey.validateSecretKey(secret, "Attempt to call setNewPresence");
        setNewPresence(presence);
    }   

    local UnumSoul getUnumSoul (SecretKey secret)  {
        SecretKey.validateSecretKey(secret, "Attempt to call getUnumSoul");
        return soul;
    }
  
    local void createUnumSoul (SecretKey secret)  {
        SecretKey.validateSecretKey(secret, "Attempt to call createUnumSoul");
        soul = new UnumSoul((SecretUnum)this);
    }   
      
    Object value () {
        return this;
    }    
}

public class PresenceEnvironment {
    public Presence presence;
    public Unum unum;
    
    public int flags;
    // Behavioral flags
    public static final int DieWhenNoOtherPresences = 0x00000001;
    public static final int IsClientPresence        = 0x00000004;
    public static final int IsHostPresence          = 0x00000008;
    public static final int Encodeable              = 0x00000010;
    public static final int Invalidated             = 0x08000000;

    // Harmnless old constants that much code still needs to compile
    public static final int TrackOtherPresences     = 0x00000000;
    
    public Vector /* Presence */ otherPresences;
    public Presence hostPresence;
}
 
public einterface Presence {
}

public interface PresenceInterface {
    void init(Unum thisUnum, boolean isPrimePresence);
    void encodeOtherPresence(RtEncoder encoder);
    Unum invalidateAndMakeNewUnum(SecretKey secret);
    void invalidate(SecretKey secret);
    boolean isClient();
}

public einterface PresenceDelegate {
    unumDeathNotification ();
}

public eclass BasePresence implements Presence, PresenceInterface, ProxyDeathHandler {
    public static Trace tr = new Trace("ec.pl.runtime.BasePresence");
    protected PresenceEnvironment environment;
    protected PresenceDelegate delegate;
    private Vector /* ClientPresenceTracker */ trackers;
    
    // Implemented for the compiler's benefit
    protected void init(Unum thisUnum)  {
        throw new RtRuntimeException("BasePresence init(Unum) method not overriden by " + this);
    }
    
    // Subclass init must call this, and then set flags and set Ingredients 
    local void init(Unum thisUnum, boolean isPrimePresence) {
        if (environment != null)  {
            throw new UnumSecurityViolationException("Presence init called more than once!");
        }
        if (isPrimePresence)  {
           trackers = new Vector();
           ((SecretUnum)thisUnum).createUnumSoul(SecretKey.theSecretKey);
        }
        environment = new PresenceEnvironment();
        environment.presence = this;
        environment.unum = thisUnum;
    }

    final protected void setPresenceDelegate (PresenceDelegate delegate) {
        this.delegate = delegate;
    }
    
    /**
     * Called by a Client that was encoded by another Client
     */ 
    emethod forwardUnum (EDistributor dist) {
        if (environment == null)  {
            // XXX - We've been invalidated. How to tell other side!?
            if (tr.tracing) tr.$("ForwardUnum called on Invalidated Presence: " + this);
            return;
        }
        dist <- forward(environment.unum);
    }   
    

    /**
     * Kill the Presence of the Unum on this machine. If this
     * is a Host Presence it will render all of the Client
     * Presences invalid as well. If this is a Client Presence, it
     * will only kill itself, but cause the Host to remove
     * it from its list of other Presences.
     *
     * This method works for both Host and Client Presences
     */ 
    final local void invalidate (SecretKey secret)  {   
        Vector savedTrackers = null;
     
        SecretKey.validateSecretKey(secret, "Attempt to invoke Presence invalidate");
        ((SecretUnum)environment.unum).invalidate(SecretKey.theSecretKey);     

        if ((environment == null) || (environment.flags & PresenceEnvironment.Invalidated) != 0)  {
            System.err.println("Warning: Attempt to invalidate already invalid Presence"); 
            return;
        }
        
        long identity = ((EObject_$_Impl)this).getIdentity();
        EProxy_$_Impl proxy;
        if (trackers != null) {
            int size = environment.otherPresences.size();
            for (int i = 0; i < size; i++) {
                Object other = environment.otherPresences.elementAt(i);
                if (other instanceof EProxy_$_Impl)  {
                    proxy = (EProxy_$_Impl)other;
                    invalidateOtherPresence(proxy, identity, environment.unum);
                }
                else {
                    // It's a Channel for an as yet unreported client
                    // We'll clean it up later when it reports in.
                    System.out.println("Unum invalidate: Will keep tracking Channel for " + this);
                    savedTrackers = trackers;
                }
            }
            if (environment.hostPresence != null)  {
                proxy = (EProxy_$_Impl)environment.hostPresence;
                invalidateOtherPresence(proxy, identity, environment.unum);
            }
            if (environment.otherPresences != null)  {
                environment.otherPresences.removeAllElements();
            }
        }
        environment.presence = null;
        // Invalidate all of our routing information
        Farbulator.invalidateObject(this);
        if (savedTrackers != null)  {
            environment.flags |= PresenceEnvironment.Invalidated;
        } else  {
            environment = null;
            trackers = null;
        }
    }   
    
    /**
     * Invalidate the Unum and Presence Routers, creating new
     * ones in their place. This rewires all of the internal
     * Unum schematic to reflect the new routers. This can only
     * be called on a Host Presence. It causes all of the Client
     * Presences of the Unum to die.
     */  
    final local Unum invalidateAndMakeNewUnum (SecretKey secret)  {
        Vector savedTrackers = null;
   
        SecretKey.validateSecretKey(secret, "Attempt to invoke Presence invalidate");
   
        if ((environment == null) || (environment.flags & PresenceEnvironment.Invalidated) != 0)  {
            System.err.println("Warning: Attempt to invalidate already invalid Presence"); 
            return null;
        }
        
        if ((environment.flags & PresenceEnvironment.IsClientPresence) != 0)  {
            // XXX - Should eventually be exception?
            System.err.println("Warning: Attempt to invalidate Client Presence"); 
            return null;
        }
        
        Unum oldUnum = environment.unum;        
        Unum newUnum = ((SecretUnum)environment.unum).invalidateAndMakeNewUnum(SecretKey.theSecretKey);     
        Presence newPresence = null;
        try {
            newPresence = (Presence)clone();
        } catch (CloneNotSupportedException e) {
            System.err.println("Error! Can't clone Presence Router for " + this);
        }
        environment.unum = newUnum;
        environment.presence = newPresence;
        ((SecretUnum)newUnum).setNewPresence(newPresence, SecretKey.theSecretKey);
        
        if (trackers != null) {
            long identity = ((EObject_$_Impl)this).getIdentity();
            int size = environment.otherPresences.size();
            for (int i = 0; i < size; i++) {
                EProxy_$_Impl proxy;
                Object other = environment.otherPresences.elementAt(i);
                if (other instanceof EProxy_$_Impl)  {
                    proxy = (EProxy_$_Impl)other;
                    invalidateOtherPresence(proxy, identity, oldUnum);
                }
                else {
                    // It's a Channel for an as yet unreported client
                    // We'll clean it up later when it reports in.
                    System.out.println("Unum invalidate: Will keep tracking Channel for " + this);
                    savedTrackers = trackers;
                }
            }
            environment.otherPresences.removeAllElements();
        }
        // Invalidate all of our routing information
        Farbulator.invalidateObject(this);
        if (savedTrackers != null)  {
            environment = new PresenceEnvironment();
            environment.flags |= PresenceEnvironment.Invalidated;
            environment.unum = oldUnum;
        } else {
            environment = null;
            trackers = null;
        }
        return newUnum;
    }   
        
    final local Unum NEWinvalidateAndMakeNewUnum (SecretKey secret)  {      
        SecretKey.validateSecretKey(secret, "Attempt to invoke Presence invalidate");
        if ((environment.flags & PresenceEnvironment.IsClientPresence) != 0)  {
            // XXX - Should eventually be exception?
            System.err.println("Warning: Attempt to invalidate Client Presence"); 
            return null;
        }

        Unum oldUnum = environment.unum;        
        Unum newUnum = ((SecretUnum)environment.unum).invalidateAndMakeNewUnum(SecretKey.theSecretKey);     
        environment.unum = newUnum;
        // XXX - Remove setNewPresence from BaseUnum and pl gen
            
        // XXX - add EConnection.removeExport which goes through each connection, and
        // compares connection's exported ID to Object's current ID (which we just set
        // to 0) and if it doesn't match, removes from export table and sends msg over
        // to the other side.   
        //((EObject_$_Impl)this).clearIdentity(); // Protected method on EObject_$_Impl
        // to set identity to 0 so it will revoke export...     
        //EConnection.removeExport(this);
        //oldUnum.revokeExport(); // Possibly this calls EConnection removeUniquExport
        //EConnection.removeUniqueExport(oldUnum); // XXX - Add this too, but how to make safe?
        // XXX - Probably have to add API to RtUniquelyCodeable boolean revoked() which conn
        // can call when we tell it to removeUniqueExport
        
        // Can get rid of invalidated flag in environment, and newOtherPresence doesn't need
        // code to check, it can just bail when the loser from the other side races, although
        // the revoked client will probably lose before it gets the message over here ...
        
        // Could be race for 1)we encode to client, 2) we revoke, 3) client presence made but
        // it regisers interest in unexported proxy that already died - in this case comm system
        // has to catch this and immediately send the notification of proxy death to it. Code
        // added but not tested.
        // Other races?
            
        if (environment.otherPresences != null) {
            environment.otherPresences.removeAllElements();
        }
        if (trackers != null) {
            trackers.removeAllElements();       
        }
        return newUnum;
    }   
        
    private void invalidateOtherPresence (EProxy_$_Impl proxy, long identity, Unum oldUnum)  {
        // XXX - Note this stuff is obscenely insecure!!!
        EConnection connection = ((EProxyConnection)proxy).getConnection();
        connection.unregisterImportedObject(proxy);
        connection.unregisterExportedObject(identity);
        connection.unregisterUniquelyExportedObject(oldUnum);       
    }   
    
    private static long HackyFakeSwissNumber = 0; // XXX - Use a real Swiss # !!!
    
    // XXX - Security hole!!! Need to plug this up somehow...   
    local void encodeOtherPresence(RtEncoder encoder) {
        if ((environment.flags & PresenceEnvironment.Encodeable) == 0) {
            throw new RuntimeException("Exception: Attempt to encode Presence which isn't encodeable");
        }
        // If we're not the host, encode the actual host
        // If we are the host, encode ourselves                
        try {
            if ((environment.flags & PresenceEnvironment.IsClientPresence) != 0) {
                if (tr.tracing) tr.$("Encoding indicator I'm client Presence");
                encoder.writeBoolean(false);
                encoder.encodeObject(environment.hostPresence);
            }
            else {
                long swissNumber = 0;
                encoder.writeBoolean(true);
                encoder.encodeObject(this);
                if (environment.otherPresences != null)  {
                    swissNumber  = ++HackyFakeSwissNumber;
                    ClientPresenceTracker tracker = 
                        ((SecretUnum)environment.unum).makeClientPresenceTracker(swissNumber);
                    environment.otherPresences.addElement(tracker.getChannel());
                    trackers.addElement(tracker);
                }
                encoder.writeLong(swissNumber);
            } 
        } catch (Exception e) {
            throw new NestedException("Exception encoding presence", e);
        }
    }
    
    final protected void decodeAndInitialize(Unum unum, RtDecoder decoder) {
        long swissNumber;
        this.init(unum);
        try {
            environment.hostPresence = (Presence) decoder.decodeObject();  
            swissNumber = decoder.readLong();
        } catch (Exception e) {
            throw new NestedException("Exception decoding presence", e);
        }
        registerInterestInProxy(environment.hostPresence);
        if (environment.hostPresence != null)  {
            if (swissNumber != 0) {
                environment.hostPresence <- BasePresence.newOtherPresence(this, swissNumber);
            }
        }
        else {
            System.err.println("Client presence decoded without host: " + this);
            throw new RtRuntimeException("Client presence decoded without host");
        }
    }
            
    emethod newOtherPresence (Presence otherPresence, long swissNumber) {
        if (trackers == null)  {
            System.err.println("NewOtherPresence called when Trackers null with Presence "
                + otherPresence + ", swissNumber is " + swissNumber);
            return;
        }        
        try {
            if (tr.tracing) tr.$("newOtherPresence called for " + this + " with " + otherPresence);
            ClientPresenceTracker tracker = null;
            int i;
            int size = trackers.size();
            for (i = 0; i < size; i++) {
                ClientPresenceTracker temp = (ClientPresenceTracker)trackers.elementAt(i);
                if (temp.getSwissNumber() == swissNumber)  {
                    trackers.removeElementAt(i);
                    tracker = temp;
                    break;
                }
            }
            if (tracker == null)  {
                // Couldn't find entry, liar!
                System.err.println("NewOtherPresence can't find Presence swiss Number " +
                    swissNumber + " for Presence " + otherPresence);
            }
            else {
                if ((environment.flags & PresenceEnvironment.Invalidated) == 0)  {
                    // Presence is still alive, add the Client
                    if (environment.otherPresences == null)  {
                        if (tr.tracing) tr.$("NewOtherPresence called when otherPresences null with Presence "
                            + otherPresence + ", swissNumber is " + swissNumber);
                        return;
                    }
                    if (tr.tracing) tr.$("NewOtherPresence replacing " + tracker + " with " + otherPresence);
                    environment.otherPresences.addElement(otherPresence);
                    registerInterestInProxy(otherPresence);
                    environment.otherPresences.removeElement(tracker.getChannel());
                    tracker.getDistributor() <- forward(otherPresence);
                }
                else {
                    // Presence has been invalidated, tell the client
                    long identity = ((EObject_$_Impl)this).getIdentity(); 
                    if (tr.tracing) tr.$("NewOtherPresence invalidating " + tracker + " for " + otherPresence);
                    invalidateOtherPresence((EProxy_$_Impl)otherPresence, identity, environment.unum);
                    if (trackers.size() == 0)  {
                        trackers = null;
                        environment = null;
                    }
                }
            }
        } catch (Exception e) {
            throw new NestedException("Exception in newOtherPresence", e);
        }
    }
    
    local final boolean isClient ()  {
        return ((environment.flags & PresenceEnvironment.IsClientPresence) != 0);
    }   
    
    local final void noteProxyDeath (Object proxy, Object data) {
        if (tr.tracing) {
            if (proxy == null) {
                tr.$("Called with null proxy!");
                System.err.println(tr.getStackTrace());
            }
            else tr.$("Proxy " + proxy + " died");
        }   
        int vectorSize = 0;
        if (environment == null)  {
            // We're invalidated, we don't care about other Presence death
            return;
        }
        if ((environment.otherPresences != null) && ((environment.flags & PresenceEnvironment.IsHostPresence) != 0)) {
            if (tr.tracing) tr.$("Removing presence " + proxy + " from otherPresences");   
            vectorSize = BasePresence.removePresenceFromVector((Presence)proxy, environment.otherPresences);
        }
        else {
            if (environment.hostPresence == proxy) {
                if (tr.tracing) tr.$("Clearing host presence " + proxy);  
                environment.hostPresence = null;
            }
        }
        checkForUnumDeath(vectorSize);
    }
        
    private void checkForUnumDeath (int vectorSize) {
        if ((vectorSize == 0) && (environment.hostPresence == null)) {
            if (tr.tracing) tr.$("Null vector and no host presence");
            if ((environment.flags & PresenceEnvironment.DieWhenNoOtherPresences) != 0) {
                if (tr.tracing) tr.$("DieWhenNoOtherPresences, notifying Agency");
                if (delegate != null) delegate <- unumDeathNotification();
                ((BaseUnum)environment.unum) <- indicateUnumDeath(SecretKey.theSecretKey);
            }
        }
    }
        
    final protected void addOtherPresenceToVector (Object otherPresenceObject, Vector vector) {
        if (vector == null) return;
        synchronized (vector) {
            if (vector.contains(otherPresenceObject)) {
                System.err.println("addOtherPresenceToVector: error! Adding presence which is already in Vector");
                System.err.println(Trace.getStackTrace());
            }
            else {
                vector.addElement(otherPresenceObject);
                registerInterestInProxy(otherPresenceObject);
            }
        }
    }
    
    final ProxyInterest registerInterestInProxy (Object presence) {
        EProxy_$_Impl proxy = (EProxy_$_Impl)presence;
        return proxy.registerInterestInProxy(this, null);
    }
            
    public static int removePresenceFromVector (Presence presence, Vector vector) {
        // Makes simple assumption that item is only in vector once
        if (vector == null) return 0;
        int size = 0;
        boolean removed = false;
        synchronized(vector) {
            int i;
            size = vector.size();
            if (size <= 0) return 0;
            for (i = 0; i < size; i++) {
                Object obj = vector.elementAt(i);
                if (obj == (Object)presence) {
                    removed = true;
                    if (tr.tracing) tr.$("Removing presence " + obj);   
                    vector.removeElementAt(i);
                    break;
                }
            }
        }
        if (removed) {
            return (size - 1);
        }
        else {
            if (tr.tracing) tr.$("Couldn't find presence in vector " + presence);
            return size;
        }
    }

    public static void sendEnvelopeToOthers (Vector vector, RtEnvelope envelope) {
        if (tr.tracing) tr.$("Called with " + envelope);
        if (vector == null) return;
        if (tr.tracing) tr.$("Vector size is " + vector.size() + ", " + vector);
        ////etry {      
            synchronized(vector) {
                int i;
                int size = vector.size();
                for (i = 0; i < size; i++) {
                    try {
                        EObject obj = (EObject)vector.elementAt(i);
                        obj <- envelope;
                    } catch (Exception e) { // XXX - Should be more specific exception
                        // We might be racing against a notification that
                        // the connection went away, so we'll let this
                        // particular exception get by without major incident.
                        EThreadGroup.reportException(e);
                    }
                }
            }
        ////} ecatch(Throwable t) {
        ////    EThreadGroup.reportException(t);
        ////}
    }  
}

