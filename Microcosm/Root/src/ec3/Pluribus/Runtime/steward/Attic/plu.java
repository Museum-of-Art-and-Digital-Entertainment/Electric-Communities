// BUG--remove this file (or replace it with the contents of plunew.java)
// when the 1.1 transition is complete
package ec.pl.runtime;

import java.lang.Cloneable;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import ec.e.net.ProxyInterest;
import ec.e.net.ProxyDeathHandler;

public class Plu {
    static {
        System.loadLibrary("plrun");
    }

    static public Object getSelf(String name) {
        return(getSelfForClass(forName(name)));
    }

    static public native Object getSelfForClass(Class theClass);

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

public eclass Ingredient {
    protected PresenceEnvironment environment;
    
    public Ingredient(PresenceEnvironment environment) {
        this.environment = environment;
    }
    
    protected UnumInterest registerInterestInUnum(EObject unum, Object data) {
        // This cast will be done asynchronously later, let's make sure
        // it works now, so we choke as early as possible
        UnumDeathHandler handler = (UnumDeathHandler)this;
        return new UnumInterest(unum, handler, data);
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
    static private UnumInterestHelper TheUnumInterestHelper = new EUnumInterestHelper();
     
    public UnumInterest (EObject unum, UnumDeathHandler handler, Object data) {
        myHandler = handler;
        myData = data;
        if (unum instanceof Unum)  {
            registerInterestInUnum((Unum)unum);
        }
        else  {
            TheUnumInterestHelper.turnWaterIntoWine(unum, this);
        }
    }
    
    void noteUnumDeath (Unum unum) {
        myHandler <- noteUnumDeath(unum, myData);
    }
    
    public void unregisterInterestInUnum () {
        if (myUnum != null)  {
            myUnum <- BaseUnum.unregisterInterestInUnum(this);
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
                e.printStackTrace();                        
            }
        }
    }   
    
    private void registerInterestInUnum (Unum unum)  {
        myUnum = unum;
        myUnum <- BaseUnum.registerInterestInUnum(this);
    }   
}

class UnumSecurityViolation extends RuntimeException
{
    UnumSecurityViolation(String msg) {
        super(msg);
    }
}

class SecretKey
{
    static SecretKey theSecretKey = new SecretKey();
    private SecretKey() {
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
}

public einterface Unum {
    registerInterestInUnum(UnumInterest interest);
    unregisterInterestInUnum(UnumInterest interest);
}

public eclass BaseUnum implements Unum, SecretUnum {        
    // XXX - Needs to be WEAK!!!
    private Vector /* UnumInterest */ unumInterests;
    private boolean unumIsDead = false;
    
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
        if (secret != SecretKey.theSecretKey) {
            throw new UnumSecurityViolation("Attempt to invoke indicateUnumDeath");
        }
        unumIsDead = true;
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
}

public einterface PresenceDelegate {
    unumDeathNotification ();
}

public eclass BasePresence implements Presence, PresenceInterface, ProxyDeathHandler {
    public static Trace tr = new Trace(false, "[Presence]");
    protected PresenceEnvironment environment;
    protected PresenceDelegate delegate;
    private Vector /* ClientPresenceTracker */ trackers;
    
    // Implemented for the compiler's benefit
    protected void init(Unum thisUnum)  {
        throw new RtRuntimeException("BasePresence init(Unum) method not overriden by " + this);
    }
    
    // Subclass init must call this, and then set flags and set Ingredients 
    local void init(Unum thisUnum, boolean isPrimePresence) {
        if (isPrimePresence)  {
           trackers = new Vector();
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
        ////System.out.println("ForwardUnum called, I am " + this + ", distributor is " + dist);
        dist <- forward(environment.unum);
    }   
    
    private static long HackyFakeSwissNumber = 0; // Use a real Swiss # !!!
    local void encodeOtherPresence(RtEncoder encoder) {
        if ((environment.flags & PresenceEnvironment.Encodeable) == 0) {
            throw new RuntimeException("Exception: Attempt to encode Presence which isn't encodeable");
        }
        // If we're not the host, encode the actual host
        // If we are the host, encode ourselves                
        try {
            // XXX - If hostPresence == null, report error? Would break things
            // more than letting the other side figure it out and silently
            // fail ...
            if ((environment.flags & PresenceEnvironment.IsClientPresence) != 0) {
                ////System.out.println("Encoding indicator I'm client Presence");
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
            System.err.println("Exception encoding presence");
            e.printStackTrace();
            throw new RtRuntimeException(e.getMessage());
        }
    }
    
    final protected void decodeAndInitialize(Unum unum, RtDecoder decoder) {
        long swissNumber;
        this.init(unum);
        environment.hostPresence = (Presence) decoder.decodeObject();        
        try {
            swissNumber = decoder.readLong();
        } catch (Exception e) {
            System.err.println("Exception decoding presence");
            e.printStackTrace();
            throw new RtRuntimeException(e.getMessage());
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
        if (environment.otherPresences == null)  {
            if (tr.tracing) tr.$("NewOtherPresence called when otherPresences null with Presence "
                + otherPresence + ", swissNumber is " + swissNumber);
            return;
        }
        
        try {
            if (tr.tracing) tr.$("Called for " + this + " with " + otherPresence);
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
                if (tr.tracing) tr.$("NewOtherPresence replacing " + tracker + " with " + otherPresence);
                environment.otherPresences.addElement(otherPresence);
                registerInterestInProxy(otherPresence);
                environment.otherPresences.removeElement(tracker.getChannel());
                tracker.getDistributor() <- forward(otherPresence);
            }
        } catch (Exception e) {
            System.err.println("Exception in newOtherPresence");
            e.printStackTrace();
            throw new RtRuntimeException(e.getMessage());
        }
    }
    
    local void noteProxyDeath (Object proxy, Object data) {
        if (tr.tracing) {
            if (proxy == null) {
                tr.$("Called with null proxy!");
                System.err.println(tr.getStackTrace());
            }
            else tr.$("Proxy " + proxy + " died");
        }   
        int vectorSize = 0;
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
                    e.printStackTrace();
                }
            }
        }
    }  
}

