package ec.pl.runtime;

import java.lang.Cloneable;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import ec.e.net.RtConnection;

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
}

public einterface Unum {
}

public eclass BaseUnum implements Unum {        
    protected Object value () {
        return this;
    }    
}

public class PresenceEnvironment {
    public Presence presence;
    public Unum unum;
    
    public int flags;
    // Behavioral flags
    public static final int DieWhenNoOtherPresences = 0x00000001;
    public static final int TrackOtherPresences     = 0x00000002;
    public static final int IsClientPresence        = 0x00000004;
    public static final int IsHostPresence          = 0x00000008;
    public static final int Encodeable              = 0x00000010;
    public static final int EncodeOtherPresences    = 0x00000020;

    // Internal use only flags (XXX - Possibly in another int?)
    final static int PresenceIsDetached    = 0x08000000; 
    
    public String classNameToEncode;
    
    public Vector /* Presence */ otherPresences;
    public Presence hostPresence;
}
 
public einterface Presence {
}

public interface PresenceInterface {
    void init(Unum thisUnum);
    void encodeOtherPresence(RtEncoder encoder);
}

public einterface PresenceDelegate {
    unumDeathNotification ();
}

final class GlobalUniqueIdentifier implements RtCodeable
{
    Long id1;
    Long id2;

    // XXX - Seed this with something more random?
    // Don't want to have the Netscape bug where this
    // is based on TOD or something else obvious ...
    private static Random generator = new Random();
    
    GlobalUniqueIdentifier() {
        id1 = new Long(generator.nextLong());
        id2 = new Long(generator.nextLong());
    }
    
    public int hashCode () {
        return (id1.intValue() ^ id2.intValue());
    }
    
    public boolean equals (Object other) {
        if (other == null) return false;
        if (other instanceof GlobalUniqueIdentifier) {
            GlobalUniqueIdentifier otherIdentifier = (GlobalUniqueIdentifier)other;
            return ((id1.longValue() == otherIdentifier.id1.longValue()) && 
                    (id2.longValue() == otherIdentifier.id2.longValue()));
        }
        else {
            return false;
        }
    }
    
    public String classNameToEncode (RtEncoder encoder) {
        return null;
    }
    
    public void encode (RtEncoder encoder) {
        try {
            encoder.writeLong(id1.longValue());
            encoder.writeLong(id2.longValue());
        } catch (Exception e) {
            System.out.println("Error encoding GlobalUniqueIdentifier");
            e.printStackTrace();
        }
    }
    
    public Object decode (RtDecoder decoder) {
        long l1 = 0;
        long l2 = 0;
        try {
            l1 = decoder.readLong();
            l2 = decoder.readLong();
        } catch (Exception e) {
            System.out.println("Error encoding GlobalUniqueIdentifier");
            e.printStackTrace();
        }
        id1 = new Long(l1);
        id2 = new Long(l2);
        return this;
    }       
}

// XXX - Move this into the comm system!
public einterface ProxyDeathHandler
{
    noteProxyDeath (Object proxy);
}

public eclass BasePresence implements Presence, PresenceInterface, ProxyDeathHandler {

    public static Trace tr = new Trace(false, "[Presence]");
    protected PresenceEnvironment environment;
    protected GlobalUniqueIdentifier globalUniqueIdentifier;
    protected PresenceDelegate delegate;
    
    // XXX - Note knownUnumTable should be WEAK!!!
    private static Hashtable knownUnumTable = new Hashtable();
    
    protected BasePresence() {
        globalUniqueIdentifier = new GlobalUniqueIdentifier();
    }

    // Subclass init must call this, and then set flags and classNameToEncode
    local void init(Unum thisUnum) {
        knownUnumTable.put(globalUniqueIdentifier, thisUnum);
        environment = new PresenceEnvironment();
        environment.unum = thisUnum;
        environment.presence = this;
    }

    final protected void setPresenceDelegate (PresenceDelegate delegate) {
        this.delegate = delegate;
    }
    
    local void encodeOtherPresence(RtEncoder encoder) {
        if ((environment.flags & PresenceEnvironment.Encodeable) == 0) {
            // XXX Squawk!
            return;
        }
        if ((environment.flags & PresenceEnvironment.PresenceIsDetached) != 0) {
        // XXX - Better exception - possibly E Exception?
            throw new RtRuntimeException("Attempt to encode detached presence");
        }

        encoder.encodeObject(globalUniqueIdentifier);
        
        // XXX (GJF) This might be a secutiry hole, it is legacy code from
        // our early valiant attempts to do peer presences, but we probably
        // don't want to give awaya capability directly to our presence by default!
        encoder.encodeObject(this);
        
        boolean encodeHost = false;     
        if ((environment.flags & PresenceEnvironment.IsClientPresence) != 0) {
            encodeHost = true;
        }
        try {
            encoder.writeBoolean(encodeHost);
        } catch (Exception e) {
            // XXX!
        }
        if (encodeHost) {
            encoder.encodeObject(environment.hostPresence);
        }
        
        boolean encodeOtherPresences = false;
        if ((environment.flags & PresenceEnvironment.EncodeOtherPresences) != 0) {
            encodeOtherPresences = true;
        }
        try {
            encoder.writeBoolean(encodeOtherPresences);
        } catch (Exception e) {
            // XXX!
        }
        if (encodeOtherPresences) {
            synchronized(environment.otherPresences) {
                encoder.encodeObject(environment.otherPresences);
            }
        }
    }

    final protected Unum checkForKnownUniqueUnum (RtDecoder decoder) {
        globalUniqueIdentifier = (GlobalUniqueIdentifier) decoder.decodeObject();
        Unum unum = (Unum) knownUnumTable.get(globalUniqueIdentifier);
        return unum;
    }
    
    final protected void decodeAndInitialize(Unum unum, RtDecoder decoder) {
        this.init(unum);
        
        boolean otherPresenceIsHost = false;
        boolean toldOtherPresence = false;
        Presence otherPresence;
        boolean encodedHostPresence = false;
        Presence hostPresence = null;
        boolean encodedOtherPresences = false;
        Vector otherPresences = null;
        
        otherPresence = (Presence) decoder.decodeObject();
        
        try {
            encodedHostPresence = decoder.readBoolean();
        } catch (Exception e) {
            // XXX!
        }
        if (encodedHostPresence) {
            hostPresence = (Presence) decoder.decodeObject();
        }
        try {
            encodedOtherPresences = decoder.readBoolean();
        } catch (Exception e) {
            // XXX!
        }
        if (encodedOtherPresences) {
            otherPresences = (Vector)decoder.decodeObject();
        }
        
        if ((environment.flags & PresenceEnvironment.IsClientPresence) != 0) {
            if (encodedHostPresence) {
                environment.hostPresence = hostPresence;
            }
            else {
                environment.hostPresence = otherPresence;
                otherPresenceIsHost = true;
                toldOtherPresence = true;
            }
            registerInterestInProxy(environment.hostPresence);
            if (environment.hostPresence != null) {
                environment.hostPresence <- BasePresence.newOtherPresence(this);
            }
            else {
                System.err.println("Client presence decoded without host: " + this);
            }
        }
        else {
            environment.hostPresence = null;
        }
        
        if ((environment.flags & PresenceEnvironment.TrackOtherPresences) != 0) {
            environment.otherPresences = otherPresences;
            if ((otherPresences != null) && (otherPresenceIsHost == false)) {
                toldOtherPresence = true;
                environment.otherPresences.addElement(otherPresence);
            }
            registerInterestInProxies(environment.otherPresences, true);
        }
        
        if ((toldOtherPresence == false) && (otherPresence != null)) {
            // XXX (GJF) Probably should only do this when other presence
            // is a server or is tracking other presences. 
            otherPresence <- BasePresence.newOtherPresence(this);
        }        
    }
    
    final protected void decodeKnownUnum(RtDecoder decoder) {        
        Presence otherPresence;
        boolean encodedHostPresence = false;
        Presence hostPresence = null;
        boolean encodedOtherPresences = false;
        Vector otherPresences = null;
        
        otherPresence = (Presence) decoder.decodeObject();
        
        try {
            encodedHostPresence = decoder.readBoolean();
        } catch (Exception e) {
            // XXX!
        }
        if (encodedHostPresence) {
            hostPresence = (Presence) decoder.decodeObject();
        }
        try {
            encodedOtherPresences = decoder.readBoolean();
        } catch (Exception e) {
            // XXX!
        }
        if (encodedOtherPresences) {
            otherPresences = (Vector)decoder.decodeObject();
        }       
    }

    emethod newOtherPresence (Presence otherPresence) {
        try {
            if (tr.tracing) tr.$("Called for " + this + " with " + otherPresence);
            addOtherPresenceToVector(otherPresence, environment.otherPresences);
        } catch (Exception e) {
            System.out.println("Exception in newOtherPresence");
            e.printStackTrace();
        }
    }
    
    emethod noteProxyDeath (Object proxy) {
        if (tr.tracing) {
            if (proxy == null) {
                tr.$("Called with null proxy!");
                tr.printStackTrace();
            }
            else tr.$("Proxy " + proxy + " died");
        }   
        int vectorSize = 0;
        if ((environment.otherPresences != null) &&
            (environment.flags & PresenceEnvironment.TrackOtherPresences) != 0) {
            if (tr.tracing) tr.$("Removing presence " + proxy + " from otherPresences");   
            vectorSize = BasePresence.removePresenceFromVector((Presence)proxy, environment.otherPresences);
        }
        else if ((environment.hostPresence != null) &&
            (environment.flags & PresenceEnvironment.IsClientPresence) != 0) {
            if (environment.hostPresence == proxy) {
                if (tr.tracing) tr.$("Clearing host presence " + proxy);  
                environment.hostPresence = null;
            }
        }
        checkForUnumDeath(vectorSize);
    }
        
    private void indicateUnumDeath () {
        // XXX - This tells all Interests we've died
    }
    
    private void checkForUnumDeath (int vectorSize) {
        if ((vectorSize == 0) && (environment.hostPresence == null)) {
            if (tr.tracing) tr.$("Null vector and no host presence");
            if ((environment.flags & PresenceEnvironment.DieWhenNoOtherPresences) != 0) {
                if (tr.tracing) tr.$("DieWhenNoOtherPresences, notifying Agency");
                indicateUnumDeath();
                if (delegate != null) delegate <- unumDeathNotification();
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
    
    final protected void unregisterInterestInProxy (Object presence) {
    }

    final void registerInterestInProxy (Object presence) {
    }
        
    final protected void registerInterestInProxies (Vector vector, boolean tellOthers) {
        if (vector == null) return;
        synchronized(vector) {
            int i;
            int size = vector.size();
            for (i = 0; i < size; i++) {
                EObject obj = (EObject)vector.elementAt(i);
                registerInterestInProxy(obj);
                if (tellOthers) {
                    obj <- BasePresence.newOtherPresence(this);
                }
            }
        }
    }   
    
    final protected void unregisterInterestInProxies (Vector vector) {
        if (vector == null) return;
        synchronized(vector) {
            int i;
            int size = vector.size();
            for (i = 0; i < size; i++) {
                EObject obj = (EObject)vector.elementAt(i);
                unregisterInterestInProxy(obj);
            }
        }
    }   
    
    public static int removePresenceOnConnectionFromVector (RtConnection connection, Vector vector) {
        // Makes simple assumption that item is only in vector once
        if (vector == null) return 0;
        int size = 0;
        boolean removed = false;
        synchronized(vector) {
            int i;
            size = vector.size();
            if (size <= 0) return 0;
            for (i = 0; i < size; i++) {
                EObject_$_Impl obj = (EObject_$_Impl) vector.elementAt(i);
                if (RtMagic.isProxyOnConnection(obj, connection)) {
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
            if (tr.tracing) tr.$("Couldn't find presence for connection " + connection);
            return size;
        }
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

