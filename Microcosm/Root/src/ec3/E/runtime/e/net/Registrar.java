package ec.e.net;

import ec.trace.Trace;
import ec.util.CompletionNoticer;
import ec.util.NestedException;
import ec.e.db.RtEncodingParameters;
import ec.e.db.RtDecodingParameters;
import ec.e.net.RegistrarIDGenerator;
import ec.e.net.ConnectionStatisticsSteward;
import ec.e.run.EEnvironment;
import ec.e.run.MagicPowerMaker;
import ec.e.run.ComparingMagicPowerMaker;

import ec.e.run.Vat;
import ec.e.lang.EString;
import ec.e.file.EStdio;
import ec.e.util.SetCollection;
import ec.security.ECSecureRandom;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Enumeration;
import java.security.KeyPair;

import java.security.PublicKey;
import java.security.PrivateKey;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import ec.e.serialstate.StateOutputStream;
import ec.e.serialstate.StateInputStream;
import java.io.Serializable;

import ec.util.NativeSteward;

/**
 * Registrar objects maintain a registry of local objects whose IDs are made
 * available outside this machine, and a registry of connections to remote
 * machines. A registrar is the root of the comm system: all outbound
 * connections are initiated via the registrar and all inbound connections find
 * their way in through registrar.
 */
public final class Registrar implements CompletionNoticer
        //ABS made this class serialiable/upgradeable
        , Serializable, RtStateUpgradeable
        , RtDoNotEncode 
        {
    static private final Trace tr = new Trace("ec.e.net.Registrar");
    static private Trace timerTrace = new Trace("StartupTimer");

        // Upgrade serialVersionUID
    static final long serialVersionUID = -5762940299309089503L;

    private boolean myOnTheAir;
    private SturdyRefMaker mySturdyRefMaker;
    private SturdyRefImporter mySturdyRefImporter;
    private SturdyRefExporter mySturdyRefExporter;
    private SturdyRefFileImporter mySturdyRefFileImporter;
    private SturdyRefFileExporter mySturdyRefFileExporter;
    private SturdyRefURLImporter mySturdyRefURLImporter;
    private SturdyRefFollower mySturdyRefFollower;
    private RtEncodingParameters myEncodeParameters;
    private RtDecodingParameters myDecodeParameters;
    private ConnectionsManager myConnectionsManager;
    private Hashtable myRegisteredObjects;
    /* package */ KeyPair myKeyPair; // for access by MsgConnection
    /* package */ String myCryptoProtocols;   // Protocols we should try
    /* package */ Hashtable myCryptoProtocolsTable;// In table for quick reference
    /* package */ String myPublicKey;
    /* package */ ECSecureRandom mySecureRandom;
    private String myRegistrarID;
    private String myFlattenedSearchPath;
    private String mySearchPath[];
    private String myListenAddr;
    private SetCollection myProcessLocationServerSet;
    private Vat myVat;
    private RegistrarHelper myRegistrarHelper;
    private CompletionNoticer myHibernationNoticer;

    /**
     * Asks the EEnviroment to summon a RegistrarMaker, and returns
     * the Registrar it conjures up.
     */
    static public Registrar summon(EEnvironment env)
    {
        try {
            return (Registrar)env.magicPower("ec.e.net.RegistrarMaker");
        }
        catch (Exception e) {
                        if (e instanceof RuntimeException) {
                            throw (RuntimeException)e;
                        }
            throw new NestedException("problem summoning a Registrar", e);
        }
    }

    /**
     * Construct a new Registrar. To do this successfully you must possess a
     * reference to an EEnvironment -- this is how we control access to this
     * class.  You probably just want to summon it as a magic power.
     *
     * @param env The EEnvironment for which we are to be the registrar.
     */

    public Registrar(EEnvironment env) {
        if (env == null) {
            throw new SecurityException();
        }
        KeyPair keys = RegistrarIDGenerator.generateRegistrarKeyPair(env);
        init(env, keys);
    }

    private void init(EEnvironment env, KeyPair keys) {
        long stime = 0;
        if (timerTrace.debug && Trace.ON) {
            timerTrace.debugm("!!Initializing Registrar...");
            stime = NativeSteward.queryTimer();
        }
        if (env == null) {
            throw new SecurityException();
        }
        myOnTheAir = false;
        myVat = env.vat();
        new ConnectionStatisticsSteward(myVat, this);   // Make link to statistics
        int activeConnectionLimit = Integer.parseInt(env.getProperty("ActiveConnectionLimit", "20"));
        boolean killerhack = "true".equals(env.getProperty("killerhack", "false"));
        myConnectionsManager = new ConnectionsManager(myVat, this, activeConnectionLimit, killerhack);
        myRegisteredObjects = new Hashtable();
        mySecureRandom = ECSecureRandom.summon(env);
        myKeyPair = keys;
        byte[] derkey = myKeyPair.getPublic().getEncoded();
        myPublicKey = new BigInteger(derkey).toString(36);
        myRegistrarID = RegistrarIDGenerator.calculateRegistrarID(myKeyPair.getPublic());

        // XXX change to debug when people stop coming to me claiming
        // they're connecting to themselves...
        if (tr.error) tr.errorm("myRegistrarID is " + myRegistrarID);

        myFlattenedSearchPath = env.getProperty("SearchPath");
        if (myFlattenedSearchPath == null || myFlattenedSearchPath.equals("")) {
            throw new RuntimeException("You must specify the SearchPath property to create a Registrar");
        }
        if (tr.debug && Trace.ON) tr.debugm("myFlattenedSearchPath is " + myFlattenedSearchPath);
        mySearchPath = EARL.parseSearchPath(myFlattenedSearchPath);
        if (tr.debug && Trace.ON) tr.debugm("done parsing myFlattenedSearchPath");
        mySturdyRefMaker    = new SturdyRefMaker(this);
        mySturdyRefImporter = new SturdyRefImporter(this);
        mySturdyRefExporter = new SturdyRefExporter(this);
        mySturdyRefFileImporter = new SturdyRefFileImporter(this, env);
        mySturdyRefFileExporter = new SturdyRefFileExporter(this, env);
        mySturdyRefURLImporter = new SturdyRefURLImporter(this, env);
        mySturdyRefFollower = new SturdyRefFollower(this);
        myEncodeParameters = new RtEncodingParameters();
        myDecodeParameters = new RtDecodingParameters();

        myEncodeParameters.put(mySturdyRefFollower, "follower");
        myDecodeParameters.put("follower", mySturdyRefFollower);

        if (tr.debug && Trace.ON) tr.debugm("summoning StandardRepository");
        try {
             String echomePath = env.getProperty("ECHome");
             if (echomePath == null) {
                 if (tr.debug && Trace.ON)  {
                    tr.debugm(
                        "Environment variable 'ECHome' is undefined so " +
                        "StandardRepository will not be available");
                }
             } else {
                 Object myStandardRepository = env.magicPower("ec.e.rep.ParimeterizedRepositoryMaker");
                 myEncodeParameters.put(myStandardRepository, "standardRepository");
                 myDecodeParameters.put("standardRepository", myStandardRepository);
             }
        } catch (Exception e) {
            noticeProblem(e);
        }

        boolean shutdownConnections = env.getProperty("ShutdownPLSConnections", "true").equals("true");
        myRegistrarHelper = new RegistrarHelper(this, myRegistrarID, shutdownConnections);

        // Get the crypto protocols to use.  They are the ones the user
        // asked for that we support.  MsgConnection.theCryptoProtocols has
        // the list currently supported.

        String requestedProtocols = env.getProperty("EncryptedLinks");
        if (null == requestedProtocols || "true".equals(requestedProtocols)) {
            myCryptoProtocols = MsgConnection.theCryptoProtocols;
        } else if ("false".equals(requestedProtocols)) {
            myCryptoProtocols = EConnection.CRYPTO_NONE;
        } else {                // User is specifing which ones

            //  First make MsgConnection.theCryptoProtocols easy to use:
            String protocols = MsgConnection.theCryptoProtocols;
            Hashtable ourProtocols = new Hashtable(9);
            while ("" != protocols) {
                int i = protocols.indexOf(',');
                String protocol;
                if (i <= 0) {
                    if (0 == i) continue;   // Skip empty elements
                    protocol = protocols;
                    protocols = "";
                } else {
                    protocol = protocols.substring(0,i);
                    protocols = protocols.substring(i+1);
                }
                ourProtocols.put(protocol.toUpperCase(), protocol);
            }

            // Now process the ones the user is requesting
            myCryptoProtocols = null;
            while ("" != requestedProtocols) {
                int i = requestedProtocols.indexOf(',');
                String protocol;
                if (i < 0) {
                    protocol = requestedProtocols;
                    requestedProtocols = "";
                } else {
                    protocol = requestedProtocols.substring(0,i);
                    requestedProtocols = requestedProtocols.substring(i+1);
                }
                String p = (String)ourProtocols.get(protocol.toUpperCase());
                if (null == p) {
                    tr.warningm("EncryptedLinks protocol "+protocol
                                +" not supported, it is being ignored");
                } else {
                    if (null == myCryptoProtocols) {
                        myCryptoProtocols = p;
                    } else {
                        myCryptoProtocols += ("," + p);
                    }
                }
            }
        }

        // Set a last ditch default if necessary
        if (null == myCryptoProtocols || "".equals(myCryptoProtocols)) {
            myCryptoProtocols = MsgConnection.theCryptoProtocols;
        }
        // Set the protocols in a hash table for quick reference
        String protocols = myCryptoProtocols;
        myCryptoProtocolsTable = new Hashtable(9);
        while ("" != protocols) {
            int i = protocols.indexOf(',');
            String protocol;
            if (i <= 0) {
                if (0 == i) continue;   // Skip empty elements
                protocol = protocols;
                protocols = "";
            } else {
                protocol = protocols.substring(0,i);
                protocols = protocols.substring(i+1);
            }
            myCryptoProtocolsTable.put(protocol.toUpperCase(), protocol);
        }
        if (tr.debug && Trace.ON) tr.debugm("CryptoProtocols are: "+myCryptoProtocols);

        myProcessLocationServerSet = new SetCollection();
        boolean urls = false;
        String registerWith = env.getProperty("RegisterWithURLs");
        if (registerWith != null && registerWith.length() > 0) {
            urls = true;
        }
        else {
            registerWith = env.getProperty("RegisterWith");
        }

        if (registerWith != null && registerWith.length() > 0) {
            try {
                setProcessLocationServerSet(registerWith, urls);
            }
            catch (Exception e) {
                EStdio.reportException(e);
                throw new NestedException("problem setting PLSset, RegisterWith=" + registerWith + ", urls=" + urls, e);
            }
        }
        if (timerTrace.debug && Trace.ON) {
            timerTrace.debugm("   !!Done initializing Registrar: "+
                              NativeSteward.deltaTimerMSec(stime) + " mSec");
        }
    }

// ABS Serialization support

    // Next two methods from the Externalizable interface
    private void writeObject(java.io.ObjectOutputStream out)
                     throws IOException {
        // StateOutputStream is final class that validates the output stream
                if (out instanceof StateOutputStream) {
            StateOutputStream o = (StateOutputStream)out;
          o.writeObject(myKeyPair.getPublic());
            o.writeObject(myKeyPair.getPrivate());      
                } else {
                    throw new IOException("Tried to externalize to stream other than StateOutput.");
                }
    }

        private void readObject(java.io.ObjectInputStream in)
                    throws IOException, ClassNotFoundException {
    
                if (in instanceof StateInputStream) {                   
            StateInputStream i = (StateInputStream)in;
            EEnvironment env = i.getEEnvironment(this);
            PublicKey pub = (PublicKey)i.readObject();
            PrivateKey priv = (PrivateKey)i.readObject();

            byte[] derkey = pub.getEncoded();
          myPublicKey = new BigInteger(derkey).toString(36);

                    if (env != null) {
                        Registrar environmentRegistrar;
                        try {
                            EStdio.out().println("[Registrar]Checking for existing Registars.");
                            environmentRegistrar = 
                                (Registrar)env.establishMagicPower("ec.e.net.RegistrarMaker", this);
                        } catch (Exception ex) {
                            // There is already a valid and different registrar here
                            throw new IOException("There is already a different registar here!"+ex);
                        }

                        if (environmentRegistrar == this) {
                            EStdio.out().println("      No existing registrar, this is the good one.");
                            // This is a registrar loaded in a brand new system
                    init(env, new KeyPair(pub, priv));
                        } else {
                            EStdio.out().println("      Existing registrar, will default to that.");
                            // The stateInputStream will take care to do the proper
                            // replacement of this doohickey for all further references.
                        }
                        return;
                    } else {
                        throw new IOException("No environment provided.");
                    }
                }
                throw new IOException("Read in from stream other than StateInput.");
    }

        protected String getPublicKey() {
            return myPublicKey;
        }


// ABS End Serialization support

    /**
       Begin allowing incoming and outgoing connections.  This version
       selects a port to listen at for you.
    */
    public void onTheAir() throws RegistrarException {
        onTheAir(null);
    }

    /**
       Begin allowing incoming and outgoing connections, listening at
       a specified port.  Calling this will result in messages being
       sent to all of the Process Location Servers in your
       ProcessLocationServerSet (initialized from either the
       RegisterWith or the RegisterWithURLs properties).  Those PLS's
       will then know the address that you're actually listening at,
       and will redirect requests for this registrar to your real
       location.

       @param listenAddr nullOk the port to listen at, specified as
       "localhost:<port>".  The system will choose a port for you if
       you specify port 0, or null.
    */
    public void onTheAir(String listenAddr) throws RegistrarException {
        if (listenAddr == null) {
            listenAddr = "localhost:0" ;
        }
        myConnectionsManager.listenForConnections(listenAddr);
        myOnTheAir = true;
    }

    /**
       Shutdown all existing connections, and disallow new ones.
       Unregisters with PLS's in ProcessLocationServerSet.
    */
    // XXX add a CompletionNoticer ??? -emm
    public void offTheAir() throws RegistrarException {
        myConnectionsManager.stopListeningForConnections();
        myRegistrarHelper <- unRegisterWith(myProcessLocationServerSet, this, Boolean.FALSE);
        myRegistrarHelper <- setListenAddr(null);
    }

    /**
       Suspend all currently active connections, and begin rejecting
       incoming messages.  On completion, the Registrar should be in
       an ideal state to hibernate.  After revival, you should arrange
       to call reviveFromHibernation.

       @param noticer who to notify when preparations are complete.
    */
    public void prepareToHibernate(CompletionNoticer noticer) {
        if (myOnTheAir) {
            myHibernationNoticer = noticer;
            myRegistrarHelper <- unRegisterWith(myProcessLocationServerSet, this, Boolean.TRUE);
        }
        else {
            noticer.noticeCompletion(null);
        }
    }

    public void checkHibernation() {
        myRegistrarHelper <- checkShutdown();
    }

    /**
       Unblock existing connections, allowing them to be resumed when
       a message is sent into them.
    */
    public void reviveFromHibernation() {
        if (myOnTheAir) {
            // don't registerWith(myProcessLocationServerSet), wait for listening()
            myConnectionsManager.reviveFromHibernation();
        }
    }

    /**
       Shouldn't be public.
    */
    public void noticeCompletion(Object hibernation) {
        if (((Boolean)hibernation).booleanValue()) {
            myConnectionsManager.prepareToHibernate(myHibernationNoticer);
            myHibernationNoticer = null;
        }
        else {
            myOnTheAir = false;
            myListenAddr = null;
        }
    }

    /**
       Return a copy of the set of SturdyRef's to
       Process Location Servers that we're registered with.
    */
    public SetCollection getProcessLocationServerSet() {
        return myProcessLocationServerSet.union(new SetCollection());
    }

    /**
       Change the set of Process Location Servers that we're
       registered with.  Unregisters with any that have been removed
       from the set, and registers with any new members.

       @param set the new ProcessLocationServer set.
    */
    public void setProcessLocationServerSet(SetCollection set) {
        if (set == null) {
            set = new SetCollection();
        }
        if (myListenAddr != null) {
            myRegistrarHelper <- unRegisterWith(myProcessLocationServerSet.difference(set), null, null);
            myRegistrarHelper <- registerWith(set.difference(myProcessLocationServerSet));
        }
        myProcessLocationServerSet = set;
    }

    /**
       Change the set of Process Location Servers that we're
       registered with.

       @param set semi-colon seperated string of set elements.  These
       are either filenames, with exported SturdyRef's in them, or
       URL's to files with exported SturdyRef's in them.

       @param urls if true, specifies that the set contains URLs,
       otherwise it contains filenames.
    */
    public void setProcessLocationServerSet(String set, boolean urls) throws IOException, InvalidURLException {
        String fname;
        int semi;
        SturdyRef ref;
        SetCollection newSet = new SetCollection();
        int attemptCount = 0 ;
        int failCount = 0 ;

        if (tr.debug && Trace.ON) tr.debugm("registering with " + set + " urls=" + urls);
        while ((semi=set.indexOf(';')) >= 0) {
            fname = set.substring(0, semi);
            set = set.substring(semi + 1);
            attemptCount++;
            try {
                if (urls) {
                    ref = mySturdyRefURLImporter.importRef(fname);
                }
                else {
                    ref = mySturdyRefFileImporter.importRef(fname);
                }
                newSet.addElement(ref);
            }
            catch (Exception e) {
                if (tr.warning && Trace.ON) tr.warningm("problem adding " + fname + " to ProcessLocationServerSet", e);
                failCount++;
            }
        }
        attemptCount++;
        try {
            if (urls) {
                ref = mySturdyRefURLImporter.importRef(set);
            }
            else {
                ref = mySturdyRefFileImporter.importRef(set);
            }
            newSet.addElement(ref);
        }
        catch (Exception e) {
            if (tr.warning && Trace.ON) tr.warningm("problem adding " + set + " to ProcessLocationServerSet", e);
            failCount++;
        }
        if (failCount == attemptCount) {
            tr.errorm("All elements of RegisterWith* property failed to be resolved.  No ProcessLocationServers will be contacted.");
            // XXX an alert box would be nice about here
        }
        setProcessLocationServerSet(newSet);
    }

    /**
       Reregister with the current set of Process Location Servers.
       This must be done periodically, as such registrations time out.
       This will be called for you, so you probably don't have to
       worry about it.
    */
    public void refreshProcessLocationServerSet() {
        myRegistrarHelper <- registerWith(myProcessLocationServerSet);
    }

    public SturdyRefMaker getSturdyRefMaker() {
        return mySturdyRefMaker;
    }

    public SturdyRefImporter getSturdyRefImporter() {
        return mySturdyRefImporter;
    }

    public SturdyRefExporter getSturdyRefExporter() {
        return mySturdyRefExporter;
    }

    public SturdyRefFileImporter getSturdyRefFileImporter(EEnvironment env) {
        if (env == null) {
            throw new SecurityException("need real EEnvironment to getSturdyRefFileImporter");
        }
        return mySturdyRefFileImporter;
    }

    public SturdyRefFileExporter getSturdyRefFileExporter(EEnvironment env) {
        if (env == null) {
            throw new SecurityException("need real EEnvironment to getSturdyRefFileExporter");
        }
        return mySturdyRefFileExporter;
    }

    public SturdyRefURLImporter getSturdyRefURLImporter(EEnvironment env) {
        if (env == null) {
            throw new SecurityException("need real EEnvironment to getSturdyRefURLImporter");
        }
        return mySturdyRefURLImporter;
    }

    public String statistics() {
        return myConnectionsManager.statistics();
    }

    SturdyRefFollower getSturdyRefFollower() {
        return mySturdyRefFollower;
    }

    RtEncodingParameters getEncodeParameters() {
        return myEncodeParameters;
    }

    RtDecodingParameters getDecodeParameters() {
        return myDecodeParameters;
    }

    /**
     * Register an EObject for external access.
     *
     * @param obj The EObject being registered
     * @returns the objectID issued to this EObject
     */
         // ABS updated to use exportable
    String register(Exportable obj) {
        return register(obj, null);
    }

    /**
     * Register an Exportable for external access with an expiration date.
     *
     * @param obj The Exportable being registered
     * @param expiration The Date after which this registration will no longer
     *  be valid.
     * @returns the objectID issued to this Exportable
     */
         // ABS updated to use exportable
    String register(Exportable obj, Date expiration) {
        /* XXX we should be doing something with the expiration date */
        Exportable exportable = (Exportable)obj;
        String objectID =
            Long.toString(obj.getIdentity(), 36);
        myRegisteredObjects.put(objectID, obj);
        return objectID;
    }

    /**
     * Return the connections manager for this registrar.
     */
    ConnectionsManager connectionsManager() {
        return myConnectionsManager;
    }

    /**
     * Lookup a locally registered object. NOTE: This method is designed to be
     * called from E code and so indicates errors via ethrow.
     *
     * @param objectID The object ID of the object desired.
     * @param resultDist A distributor which will be forwarded to the
     *  registered object, if such an object exists.
     */
    void lookupObjectID(String objectID, EResult resultDist) {
        if (!myOnTheAir) {
            ethrow new ConnectionDeadEException("Not on the air.", null);
        }

        RtTether result = (RtTether) myRegisteredObjects.get(objectID);
        if (tr.debug && Trace.ON) tr.debugm("lookupObjectID(" + objectID + "), found " + result);

        if (result == null) {
            ethrow new RegistrarLookupEException("object not registered");
        } else {
            resultDist <- forward(result);
        }
    }

    /**
     * Lookup a locally registered object. NOTE: This method is designed to be
     * called from E code and so indicates errors via ethrow.
     * XXX PESSIMISM Not needed if channels are fully optimistic.
     *
     * @param objectID The object ID of the object desired.
     * @param env The envelop to deliver to said object desired.
     */
    void sendToObjectID(String objectID, RtEnvelope env) {
        if (!myOnTheAir) {
            ethrow new ConnectionDeadEException("Not on the air.", null);
        }
                //ABS Changed EObject to RtTether for compatibility
        RtTether result = (RtTether) myRegisteredObjects.get(objectID);
        if (tr.debug && Trace.ON) tr.debugm("lookupObjectID(" + objectID + "), found " + result);
        if (result == null) {
            ethrow new RegistrarLookupEException("object not registered");
        } else {
            env.sendTo(result);
        }
    }

    /**
     * Lookup an EObject referenced by a URL. NOTE: This method is designed to
     * be called from E code and so indicates errors via ethrow.
     *
     * @param url The URL of the object desired.
     * @param resultDist A distributor which will be forwarded to the object,
     *  if such an object is found.
     */
    void lookupURL(String searchPath[], String registrarID, String objectID, EResult resultDist) {
        if (!myOnTheAir) {
            ethrow new RegistrarLookupEException("Not on the air.");
        }

        if (tr.debug && Trace.ON) tr.debugm("looking up URL: " +  registrarID + "/" + objectID);
        if (myRegistrarID.equals(registrarID)) {
            lookupObjectID(objectID, resultDist);
        } else {
            EConnection connection =
                myConnectionsManager.connection(registrarID, searchPath);
            SturdyRefFollower remoteSturdyRefFollower =
                connection.remoteSturdyRefFollower();
            remoteSturdyRefFollower <- lookupObjectID(objectID, resultDist);
        }
    }

    /**
     * Send an envelope to an EObject referenced by a URL. NOTE: This method is designed
     * to be called from E code and so indicates errors via ethrow.
     * XXX PESSIMISM This method is not needed if channels are fully optimistic.
     *
     * @param url The URL of the object desired.
     * @param env The envelope to deliver to that object.
     */
    void sendToURL(String searchPath[], String registrarID, String objectID, RtEnvelope env) {
        if (!myOnTheAir) {
            ethrow new RegistrarLookupEException("Not on the air.");
        }

        if (tr.debug && Trace.ON) tr.debugm("looking up URL: " +  registrarID + "/" + objectID);
        if (myRegistrarID.equals(registrarID)) {
            sendToObjectID(objectID, env);
        } else {
            EConnection connection =
                myConnectionsManager.connection(registrarID, searchPath);
            SturdyRefFollower remoteSturdyRefFollower =
                connection.remoteSturdyRefFollower();
            remoteSturdyRefFollower <- sendToObjectID(objectID, env);
        }
    }

    /**
     * Return the registrarID of this registrar. This functions as the process
     * ID of the process whose registrar this is.
     */
    String registrarID() {
        return myRegistrarID;
    }

    /**
     * Return the search path of this registrar, i.e., the preferred path for
     * others to find it.  Returns array of strings;
     */
    String[] searchPath() {
        return mySearchPath;
    }

    /**
     * Return the search path of this registrar, i.e., the preferred path for
     * others to find it.  Returns ; seperated single string.
     */
    String flattenedSearchPath() {
        return myFlattenedSearchPath;
    }

    /**
       Notice the address we're listening at.  Tells the
       RegistrarHelper to notify the ProcessLocationServers.
    */
    void listening(String listenAddr) {
        myListenAddr = listenAddr;
        myRegistrarHelper <- setListenAddr(myListenAddr);
        myRegistrarHelper <- registerWith(myProcessLocationServerSet);
        if (tr.debug && Trace.ON) tr.debugm("listening at " + listenAddr);
    }

    /**
     * Return our vat.
     */
    Vat vat() {
        return myVat;
    }

    void noticeProblem(Throwable t) {
        tr.errorReportException(t, "Registrar.noticeProblem");
    }
}

public class RegistrarMaker 
                implements MagicPowerMaker, ComparingMagicPowerMaker {

    public Object make(EEnvironment env) {
                return new Registrar(env);
    }

    public boolean areEquivalent(Object environmentPower, Object providedPower) {
            Registrar environmentRegistrar = (Registrar)environmentPower;
            Registrar providedRegistrar = (Registrar)providedPower;

            if(environmentRegistrar.getPublicKey().equals(
                    providedRegistrar.getPublicKey())) {
                return true;
            }else  {
                return false;
            }
    }


    public boolean isValidPower(Object providedPower) {
            Registrar providedRegistrar = (Registrar)providedPower;
        if (null != providedRegistrar.getPublicKey()) {
            return true;
        }else  {
            return false;
        }
    }
}

/**
 * All purpose exception for problems in the Registrar.
 */
public class RegistrarException extends Exception {
    public RegistrarException(String message) { super(message); }
}
