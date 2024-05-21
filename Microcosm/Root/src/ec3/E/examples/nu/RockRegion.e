/* 
        RockRegion.e
        v 0.1

        Jay Fenton 
        Proprietary and Confidential
        Copyright 1997 Electric Communities.  All rights reserved worldwide.
*/

package ec.tests.nu;
import ec.e.net.ListenerInterest;
import ec.e.net.Registrar;
import ec.e.net.RegistrarLookupEException;
import ec.e.net.RegistrarException;
import ec.e.net.SturdyRef;
import ec.e.net.SturdyRefMaker;

import ec.e.file.EStdio;
import ec.e.lang.EString;

import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;

import ec.tests.capabilities;
import java.util.Random;


/**
    UniqueUnumRegistry is used to maintain a directory of root-level una
    so that we can preserve identity after a quake takes place. Since regions
    track their members themselves, we only resort to this global directory
    to register the regions themselves.

    An improved comm system IMHO should preserve identity across quakes, perhaps
    by keeping a weak index similar to this one.

    This class is a cludge and no security claims are made for it.
*/
class UniqueUnumRegistry {
    static Random testRandom = new Random();    // for assigning random ID codes.
    static public Hashtable registeredUna = new Hashtable();    // for tracking registrations.

    // generate a new random ID number and return it as a boxed number.
    public static Long genUnumID() {    
        Long theID = new Long(testRandom.nextLong());
        return(theID);
    }

    // generate a random ID number and register the given una under it.
    public static Long genRegisterUnumID(Object una) {
        Long theID = new Long(testRandom.nextLong());
        EStdio.out().println("registering una: " + una + " under: " + theID);
        registeredUna.put(theID, una);
        return(theID);
    }

    // issue a random bitstring.
    public static long randomLong() {
        return(testRandom.nextLong());
    }

    // return the object reference registered under the ID given.
    // if a registration already exists, then return the pre-existing reference.
    // if no previous registration exists, then generate a new ID number and register
    // the given una under it (and return the given una reference as the result).
    public static Object getUnaForID(Object una, Object underKey) {
        EStdio.out().println("looking up: " + una + " under: " + underKey);
        
        if(registeredUna.containsKey(underKey)) {
            EStdio.out().println("Already there: " + registeredUna.get(underKey));
            return(registeredUna.get(underKey));
        }
        registeredUna.put(underKey, una);
        return(una);
    }
}


/**
* An EObjectWrapper is used to send an arbitrary object over a channel.
*/
eclass EObjectWrapper {
    Object theValue;

    EObjectWrapper(Object val) {
        theValue = val;
    }

    Object value() {
        return(theValue);
    }
}

/**
    an interface defining messages sent by quake recovery logic to cause
    connections to be reestablished.
*/
einterface ReviveConnectionsInterface {
    reviveConnections();
}

/**
 A RegionServerReceiver object is created for each participant in a multicast group (client).
 It fields messages sent by the client, generally forwarding them to the RegionServer proper.
 This forwarding includes mention of a capability object which serves to authorize this
 RegionServerReceiver to submit the request. This mechanism is used because stone-casting
 is not yet available.
    
*/
eclass RegionServerReceiver {
    RegionClientReceiver forClient; // Client Receiver (which recieves traffic on behalf of this client).
    RegionServer target;    // RegionServer I represent.
    ECapability changeOK;   // Capability issued by RegionServer authorizing me to forward requests.

    // Constructor used by RegionServer to generate intances of this class.
    RegionServerReceiver(ECapability pokeCapability, RegionClientReceiver theClient, RegionServer theTarget) {
        changeOK = pokeCapability;
        target = theTarget;
        forClient = theClient;
    }

    // Used by client to register a swiss number representing the multicast capability
    // for a given unum.
    // Since multicast capabilities are issued only once, there is no need to protect this
    // interface.
    emethod sRegisterMulticastSwissNumber(EObject toUnum, long multiCastSwissNumber) {
        target <- suRegisterMulticastSwissNumber(toUnum, multiCastSwissNumber);
    }

    // forward a request to multicast an message to the RegionServer for distribution.
    emethod sMulticast(long multiCastSwissNumber, EObject toUnum, RtEnvelope theEnvelope) {
        EStdio.out().println("sMulticast RegionServerReceiver");
        target <- suMulticast(changeOK, multiCastSwissNumber, toUnum, theEnvelope);
    }

    // request a state fill on behalf of this client. This is used to cause a state refresh
    // on the event of initial entry or after a quake.
    emethod sFillClient() {
        target <- suFillClient(changeOK, forClient);
    }

    // Notify the RegionServer of this clients desire to withdraw from the multicast group.
    emethod sDropClient() {
        target <- suDropClient(changeOK, forClient);
    }

    // Withdraw an unum from the multicast group. This is only necessary for two reasons:
    // 1) Tracking membership of the set so we know which state to send fills on.
    // 2) We currently do manual identity registration so that we preserve identity
    //    across quakes. If we instead use a weak table for this, then this need goes away.
    emethod sDropUna(EObject theUnum) {
        target <- suDropUna(changeOK, theUnum);
    }

    // Used by a client to notify the RegionServer of the SturdyRef under which
    // the RegionClientReceiver is registered.
    emethod noteClientRegistration(SturdyRef theRef) {
        target <- suNoteClientRegistration(changeOK, theRef, forClient);
    }
}

/**

 A RegionServer administers a multicast group. Multiple stations can then subscribe to this
 group. This works as follows:
 
 By some means, a client causes a RegionClientUnum to be transmitted to it. The
 RegionClientUnum contains within it a reference to the appropriate RegionServer
 and attempts to connect to it. It furnishes a RegionClientReceiver to which multicast
 notifications are to be sent.
 
 The RegionServer responds to this request by creating a RegionServerReceiver, with
 the appropriate capabilities to relay appropriate messages over to the RegionServer
 for action or distribution.

 The RegionClientReceiver registers itself with the Registrar and tells the RegionServerReceiver
 about it so that connections can be reestablished in the event of a server quake recovery.

 The RegionServer also tracks the RegionClientReceiver in its multicast list so it can
 multicast state change announcements.

 During practical operation, a client furnishes a large random number
 which represents the capability to transmit multicast
 notifications involving a given unum.
 A client then sends (through the RegionServerReceiver) a request to the RegionServer
 which then relays it to all the RegionClientReceivers, which then send it to their associated
 presences.

*/
eclass RegionServer implements ReviveConnectionsInterface {
    CapabilityFactory myFactory;    // Used to create capabilities valid for this RegionServer.
    Multicaster multiCaster = null; // For multicasting to subscribers.
    SturdyRef myRegistration;       // Registration of this region
    Vector unaList = new Vector();  // XXX use a hashtable someday for effeciency
    Hashtable sturdyRefToClient = new Hashtable();  // indexes for tracking the SturdyRefs of
    Hashtable clientToSturdyRef = new Hashtable();  // the RegionClientReceivers
    Hashtable multicastSwissNumberTable = new Hashtable();  // Used to track issued multicast capabilities

// Create RegionServer and register it with the SturdyRefMaker provided.
    RegionServer(SturdyRefMaker theMaker) {
         myFactory = new CapabilityFactory(this);
         multiCaster = new Multicaster();

         SturdyRef ref;
         myRegistration = ref;
         theMaker <- makeSturdyRef(this, &ref); 
    }

// a new client has been created and calls into here to register itself.
    emethod registerClient(RegionClientReceiver theClient, EResult receiver, EResult serverReg) {
        // Create a RegionServerReceiver object to field requests from this new client.
        RegionServerReceiver thisReceiver = new RegionServerReceiver(myFactory.issueRevokableCapability("clientReceiver"), theClient, this);
        EStdio.out().println("begin registerClient.");
        receiver <- forward(thisReceiver);      // Tell the client about its receiver.
        serverReg <- forward(myRegistration);   // and about the registration the server is known under.
        ((MulticasterLocalInterface)multiCaster).addClientPresence(theClient);  // Add to multicast list.
        EStdio.out().println("clientPresence added.");
    }

// Register a Swiss number capability allowing its holder to transmit
// multicast notifications to presences of a given unum.
    emethod suRegisterMulticastSwissNumber(EObject toUnum, long multiCastSwissNumber) {
        if(!unaList.contains(toUnum)) { // Issue it only once
            Long multiCastCode = new Long(multiCastSwissNumber);
            multicastSwissNumberTable.put(toUnum, multiCastCode);
        }
    }

// Transmit a multicast envelope to all the subscribers to this region
// provided that the appropriate Swiss number capability is presented.
    emethod suMulticast(ECapability changeCapability, long multiCastSwissNumber, EObject toUnum, RtEnvelope theEnvelope) {
        if( (((verifyInterface)changeCapability).verify(this, "clientReceiver") ==  myFactory)) { // make sure from a valid receiver.
          Long capability = (Long) multicastSwissNumberTable.get(toUnum);
          if(capability == null) { // First time through -- just accept this one as valid then.
                Long multiCastCode = new Long(multiCastSwissNumber);
                multicastSwissNumberTable.put(toUnum, multiCastCode);
            } else // better check for valid...
            if(capability.longValue() == multiCastSwissNumber) {
          // OK - if need be, add it to my tracking list. 
            if(!unaList.contains(toUnum)) 
                unaList.addElement(toUnum);
            EStdio.out().println("suMulticast RegionServer");
            // Reseal the message in a distribution envelope for the multicaster to rebroadcast.
            RtEnvelope env;
            env <- (RegionClientReceiver).multicastNotify(toUnum, theEnvelope);
            // Have the multicaster send it to everyone.
            ((MulticasterLocalInterface)multiCaster).sendToAll(env);
            } else EStdio.out().println("Swiss Number Capability not valid.");
        } else EStdio.out().println("suMulticast - Capability not valid.");
    }

// Send the state of all una which are registered with this region to the given client to "fill them in".
    emethod suFillClient(ECapability changeCapability, RegionClientReceiver forClient) {
        if( ((verifyInterface)changeCapability).verify(this, "clientReceiver") ==  myFactory) {
            Enumeration en = unaList.elements();    // Iterate through all registered.
            while (en.hasMoreElements()) {
                EObject thisUnum = (EObject) en.nextElement();
                etry {
                    forClient <- crFill(thisUnum);  // send a "Fill" message - which should transmit it over.
                } ecatch (Exception ex) { // remove bad references from the list.
                    unaList.removeElement(thisUnum);
                    EStdio.out().println("Removing from unaList: " + thisUnum);
                }
            }
        } else EStdio.out().println("suFillClient -Capability not valid.");
    }

// Disconnect a client from the multicast group.
    emethod suDropClient(ECapability changeCapability, RegionClientReceiver theClient) {
        if( ((verifyInterface)changeCapability).verify(this, "clientReceiver") ==  myFactory) {
            ((MulticasterLocalInterface)multiCaster).dropClientPresence(theClient); // Drop from list.
            SturdyRef dropRef = (SturdyRef) clientToSturdyRef.get(theClient);   // remove SturdyRef registrations.
            sturdyRefToClient.remove(dropRef);
            clientToSturdyRef.remove(theClient);
            EStdio.out().println("Dropping Client: " + theClient);
        } else EStdio.out().println("suDropClient - Capability not valid.");
    }

// Remove an Unum from the tracking list - it will no longer be available for fill-in or identity preservation.
    emethod suDropUna(ECapability changeCapability, EObject theUnum) {
        if( ((verifyInterface)changeCapability).verify(this, "clientReceiver") ==  myFactory) {
            
            unaList.removeElement(theUnum); // Drop from master list.
            multicastSwissNumberTable.remove(theUnum); // Revoke its multicast capability
            // Tell all RegionClients to drop the given unum.
            RtEnvelope env;
            env <- (RegionClientReceiver).crDropLocalPresence(theUnum);
            ((MulticasterLocalInterface)multiCaster).sendToAll(env);

            EStdio.out().println("Dropping Una: " + theUnum);
        } else EStdio.out().println("suDropUna - Capability not valid.");
    }

// A new RegionClientReceiver has been created on the "other side" - this method handles notification on the server
// side that this has taken place.
    emethod suNoteClientRegistration(ECapability changeCapability, SturdyRef theRef, RegionClientReceiver forReceiver) {
        if( ((verifyInterface)changeCapability).verify(this, "clientReceiver") ==  myFactory) {
            clientToSturdyRef.put(forReceiver, theRef); // Track the SturdyRef and associated object references in indexes.
            sturdyRefToClient.put(theRef, forReceiver);
            EStdio.out().println("Note suClient Registration: " + forReceiver + " " + theRef);
        } else EStdio.out().println("suNoteClientRegistration - Capability not valid.");
    }

// A quake has occured for the vat hosting the RegionServer - we must then reestablish valid connections with all the
// RegionClientReceivers for the clients.
    emethod reviveConnections() {
        EStdio.out().println("reviveConnections for Region");

        // Working from the old list of client SturdyRefs, build a new list indicating those we were able to contact.
        clientToSturdyRef = new Hashtable();
        Enumeration refList = sturdyRefToClient.keys();
        sturdyRefToClient = new Hashtable();
        // For each old SturdyRef, attempt to reconnect and reregister.
        while (refList.hasMoreElements()) {
            RegionClientReceiver theReceiver;
            SturdyRef theRef = (SturdyRef) refList.nextElement();
            theRef <- followRef(&theReceiver);  // Follow it to attempt reconnection.
            ewhen theReceiver (RegionClientReceiver receiverObj) { // Successful reconnection accomplished.
                EStdio.out().println("reviveConnection: " + receiverObj);
                ((MulticasterLocalInterface)multiCaster).addClientPresence(receiverObj);    // Add to multicaster
                clientToSturdyRef.put(receiverObj, theRef); // and to indexes.
                sturdyRefToClient.put(theRef, receiverObj);
                // Issue a new RegionServerReceiver for this client to direct requests to.
                receiverObj <- crReestablish(new RegionServerReceiver(myFactory.issueRevokableCapability("clientReceiver"), receiverObj, this));
            }
        }
    }

// Called when a client revives from a quake and wishes to reconnect to the server.
    emethod reRegisterClient(RegionClientReceiver theClient, EResult receiver) {
        // Produce a new RegionServerReceiver for the revived connection.
        RegionServerReceiver thisReceiver = new RegionServerReceiver(myFactory.issueRevokableCapability("clientReceiver"), theClient, this);
        EStdio.out().println("begin reRegisterClient: " + theClient);
        receiver <- forward(thisReceiver);  // and tell the client about it.
        ((MulticasterLocalInterface)multiCaster).addClientPresence(theClient);  // reinstall the client into the multicast list.
        EStdio.out().println("clientPresence reRegistered.");
    }

    local Object value() {
        return(this);   // Used to pull self out of a channel to a RegionServer
    }
}

/**
* Interface to local methods of a RegionClientUnum.
*/
interface ClientRegInterface {
    void sendClientReceiverRegistration(SturdyRefMaker refMaker); // Register receiver with server
    void noteReestablished(RegionServerReceiver theReceiver);   // notice connection reestablished  
    Object presenceToRegisterWithCommSystem(Object thisPresence, Object theKey); // determine ID to register
}

// A RegionClientReceiver receives messages from the RegionServer and passes them on to its
// RegionClientUnum. Since this object is known only to the RegionClientUnum and the RegionServer
// There is no special need to use a specific capability to protect the interface.
eclass RegionClientReceiver {
    RegionServer serverPresence;    // Server I represent.
    RegionClientUnum clientUnum;    // ClientUnum I forward to.

// Create a new receiver and register with the server.
    RegionClientReceiver(RegionServer theServer, RegionClientUnum theClient, EResult serverReceiver, EResult serverRefChannel) {
        EString distantState;
        serverPresence = theServer;
        clientUnum = theClient;
        serverPresence <- registerClient(this, serverReceiver, serverRefChannel);
    }

// Receive and forward a multicast notification from the server.
    emethod multicastNotify(EObject toUnum, RtEnvelope theEnvelope) {
        EStdio.out().println("in multicastNotify for: " + toUnum);
        toUnum <- theEnvelope;
    }

// Relay a fill request through to the unum.
    emethod crFill(EObject fillUna) {
        clientUnum <- cFill(fillUna);
    }

// Notify the unum that the server has come back up.
    emethod crReestablish(RegionServerReceiver theReceiver) {
        ((ClientRegInterface) clientUnum).noteReestablished(theReceiver);
    }

// Relay a request to drop an unum from the tracking collection. 
    emethod crDropLocalPresence(EObject forUnum) {
        clientUnum <- cDropLocalPresence(forUnum);
    }

    local Object value() {
        return(this);
    }
}

/**
* A RegionClientUnum represents the local presence of a multicast group. When a reference to this
* object is transmitted across comm system boundarys, it is specially encoded and decoded. This causes
* a pointer and a SturdyRef to the RegionServer to be sent across. When decoded, the RegionServer
* is contacted with a registration. Future multicasts transmitted through the RegionServer will
* include this presence - until the connection is broken or explicit disconnection takes place.
* This RegionClient can introduce new objects to the multicast group and receive capabilities
* needed for making multicast announcements.
*/
eclass RegionClientUnum implements RtEncodeable, RtDecodeable, RtUniquelyCodeable, ReviveConnectionsInterface, ClientRegInterface {
    RegionServer serverPresence;        // Reference to the RegionServer
    RegionServerReceiver serverReceiver;    // Reference for the Receiver (which relays requests in).
    EObject deferredServerReceiver = null;  // Used to hold traffic pending for reconnection.
    EResult deferredServerReceiverDistributor = null; 


    RegionClientReceiver myReceiver;    // my receiver object (which relays notifications to me).
    SturdyRef serverReference;          // SturdyRef for the server object.
    Hashtable keyToPresence;            // Used to track presence identities    
    Hashtable presenceToKey;    // (to avoid presence aliasing when quakes/partitions occurs).
    Long myIDCode;

    // Use to directly create a  presence of the RegionClientUnum
    RegionClientUnum(RegionServer server) {
        keyToPresence = new Hashtable();    // Build identity index
        presenceToKey = new Hashtable();
        serverPresence = server;
        RegionServerReceiver tempServerReceiver;    // Setup channel to Server's receiver.
        SturdyRef serverRefChannel;
        // Generate my receiver (which registers with the Region proper).
        RegionClientReceiver theReceiver = new RegionClientReceiver(serverPresence, this, &tempServerReceiver, &serverRefChannel);
        serverReceiver = tempServerReceiver;
        myReceiver = theReceiver;
        myIDCode = UniqueUnumRegistry.genRegisterUnumID(this);

        // Shorten the reference to the Server's registration when it becomes possible.
        ewhen serverRefChannel (SturdyRef theRef) {
            serverReference = theRef;
            EStdio.out().println("SturdyRef: " + serverReference);
        }
    }

    // Encode is called the first time this Unum is mentioned on a particular comm system
    // connection. We therefore encode the relevant details needed for the other end to
    // construct a local presence.
    local void encode (RtEncoder encoder) {
        EStdio.out().println("begin encode RegionClientUnum id:" + myIDCode);
        encoder.encodeObject(myIDCode);         // IDCode for avoiding aliases.
        encoder.encodeObject(serverPresence);   // Info needed to contact server/
        encoder.encodeObject(serverReference);
        EStdio.out().println("end encode RegionClientUnum");
    }

    // Used by RtEncodeable to specify the class of object to construct on the receiving end.
    local String classNameToEncode(RtEncoder encoder) {
        return("ec.tests.nu.RegionClientUnum_$_Impl");
    }

    // Use to decode an object the first time it is referred to on a given comm system connection.
    // If the object is already known (say because it was introduced before a quake occured) we
    // will seek to preserve the original identity. Otherwise this is indeed the first time
    // it was used so we construct a local presence and register the presence with the server
    // and with the local comm system. 
    local Object decode (RtDecoder decoder) {
            EStdio.out().println("begin decode RegionClientUnum");
        try {
            keyToPresence = new Hashtable();    // Generate fresh subsidary indexes.
            presenceToKey = new Hashtable();    // If an alias, they will be ignored.
            myIDCode = (Long) decoder.decodeObject();
            Object beKnownAs = UniqueUnumRegistry.getUnaForID(this, myIDCode); // an alias??

            serverPresence = (RegionServer) decoder.decodeObject(); // decode server stuff
            serverReference = (SturdyRef) decoder.decodeObject();
            if(beKnownAs == this) { // first time ever - so send back a registration request.
                RegionServerReceiver tempServerReceiver; // and get a ServerReceiver ref to use.
                EStdio.out().println("server obj: " + serverPresence);
                SturdyRef serverRefChannel;
                RegionClientReceiver theReceiver = new RegionClientReceiver(serverPresence, this, &tempServerReceiver, &serverRefChannel);
                myReceiver = theReceiver;
                serverReceiver = tempServerReceiver;
//              serverReference = serverRefChannel;     // JAY
            } // If already known we will let the existing object's info stand.

            EStdio.out().println("decode Accomplished RegionClientUnum");

            return(beKnownAs);

        } catch (IOException ex) {
            throw new RtRuntimeException("IOException: " + ex.getMessage());
        }
    }

// Relay a request to the ServerReceiver (and then through to the RegionServer)
// to register a capability for originating multicasts for a given unum. Since the
// multicast capability can only be registered once for each unum,
// we should not have to worry about someone
// stealing our object.
    emethod registerMulticastSwissNumber(EObject toUnum, long multiCastSwissNumber) {
        serverReceiver <- sRegisterMulticastSwissNumber(toUnum, multiCastSwissNumber);
    }

// setUpDeferral is called when a comm system problem such as a remote system failure or
// a network partition comes up. This sets up a channel to hold deferred traffic which will
// be sent when the connection is reestablished.
    void setUpDeferral() {
        if(deferredServerReceiver == null) {
            RegionServerReceiver deferredServerReceiverInstance;
            deferredServerReceiverDistributor = &deferredServerReceiverInstance;
            deferredServerReceiver = deferredServerReceiverInstance;
        }
    }

// Send a multicast envelope to the server for retransmission to everyone in the multicast
// group (including yourself).
    emethod transmitMulticast(long multiCastSwissNumber, EObject toUnum, RtEnvelope theEnvelope) {
        etry {
            EStdio.out().println("transmitMulticast RegionClientUnum");
            serverReceiver <- sMulticast(multiCastSwissNumber, toUnum, theEnvelope);    // Relay through ServerReceiver.
        } ecatch (Exception ex) {
            setUpDeferral(); // In case of trouble, save on deferred send channel.
            deferredServerReceiver <-  (RegionServerReceiver).sMulticast(multiCastSwissNumber, toUnum, theEnvelope);

            EStdio.out().println("transmitMulticast transmission deferred for: "
                            + serverReceiver + " " + ex.getMessage());
        }
    }

// Request a state fill for all objects in the multicast group.
    emethod requestFill() {
        etry {
            serverReceiver <- sFillClient();    // Relay request.
        } ecatch (Exception ex) {
            setUpDeferral();    // trouble - queue request.
            deferredServerReceiver <- (RegionServerReceiver).sFillClient();
            EStdio.out().println("Request fill deferred for: "
                            + serverReceiver + " " + ex.getMessage());
        }
    }

// Handle a fill notification - one comes through once for each filled object.
    emethod cFill(EObject fillUna) {
        EStdio.out().println("Fill received for: " + fillUna);
    }

// Remove an unum from the tracking list for this region.
    emethod cDropLocalPresence(EObject forUnum) {
        Object theKey = presenceToKey.get(forUnum); // Is it there already?
        if(theKey != null) {                // if so...
            presenceToKey.remove(forUnum);  // drop it from the index.
            keyToPresence.remove(theKey);
        }
    }

// This vat just came back up from a quake - seek to reestablish a connection
// with the RegionServer.
    emethod reviveConnections() {
        RegionServer newServer;
        etry {
            EStdio.out().println("Attempting to revive: " + this);
            EStdio.out().println("SturdyRef to restore: " + serverReference);

            // Follow the SturdyRef back to the RegionServer.
            serverReference <- followRef(&newServer);
            ewhen newServer (RegionServer serverAddr) { // When accomplished...
                EStdio.out().println("in ewhen of reviveConnections for: " + this);
                serverPresence = serverAddr;    // Request a new ServerReceiver to use.
                RegionServerReceiver tempServerReceiver;
                serverPresence <- reRegisterClient(myReceiver, &tempServerReceiver);
                serverReceiver = tempServerReceiver;
                if(deferredServerReceiver != null) { // If traffic queued, send it on over.
                    EStdio.out().println("Forwarding deferred messages");
                    deferredServerReceiverDistributor <- forward(serverReceiver);
                    deferredServerReceiver = null;
                }
                EStdio.out().println("reregistration: " + this + " for: " + serverPresence);
            }
        } ecatch (Exception ex) {
            EStdio.out().println("Revive failure for: " + this + " " + ex.getMessage());
        }
    }

//  emethod forwardTo(EResult thEResult, EObject theTarget) {
//      thEResult <- forward(theTarget);
//  }

    // Notify the ServerReceiver of the SturdyRef under which the ClientReceiver has
    // been registered under.
    local void sendClientReceiverRegistration(SturdyRefMaker refMaker) {
        SturdyRef ref;
        refMaker <- makeSturdyRef(myReceiver, &ref);
        serverReceiver <- noteClientRegistration(ref);
    }

    // Receive notification from the server that it has come back up and
    // the assigned address for the RegionServerReceiver.
    // (and send any deferred traffic over to it).
    // XXX only allow from RegionClientReceiver someday soon.
    local void noteReestablished(RegionServerReceiver theReceiver) {
        EStdio.out().println("noteReestablished: " + theReceiver + " to: " + this);

        if(deferredServerReceiver != null) {
            EStdio.out().println("noteReestablished: Forwarding deferred messages");
            deferredServerReceiverDistributor <- forward(theReceiver);
            deferredServerReceiver = null;
        }

        serverReceiver = theReceiver;
    }

    // Look up a subject unum in the local region registry. If an existing object is there
    // return it, otherwise return the pointer to thisPresence.
    // This mechanism exists to avoid presence aliasing problems after quakes/partitions.
    local Object presenceToRegisterWithCommSystem(Object thisPresence, Object theKey) {
        EStdio.out().println("Checking existing ref for: " + theKey);
        if(keyToPresence.containsKey(theKey)) { // Alreay there, reuse that one.
            EStdio.out().println("Found an existing ref for: " + theKey + "called: " + keyToPresence.get(theKey));
            return(keyToPresence.get(theKey));
        }
        // First time - register new presence.
        keyToPresence.put(theKey, thisPresence);
        presenceToKey.put(thisPresence, theKey);
        return(thisPresence);
    }

}

/**
* An object representing a logical time from the point of view of a given process.
* The clock increments up from zero. Process identities are assigned as random numbers
* and are used primarily to break ties.
* State changes for a shared value include one of these TimeStamps.
* Comparisons of TimeStamps are used to reject earlier, superceded state updates.
*/ 
class TimeStamp {
    long logicalTime;   // incrementing counter.
    long procID;        // random process ID code

// Create an original instance.
    TimeStamp() {
        logicalTime = 0;
        procID = UniqueUnumRegistry.randomLong();
    }

// initialize copy with unique procID
    TimeStamp(long theTime) {   
        logicalTime = theTime;
        procID = UniqueUnumRegistry.randomLong();
    }

// produce an instance with the values supplied
    TimeStamp(long theTime, long theProc) { 
        logicalTime = theTime;
        procID = theProc;
    }

// change this timestamp to be one greater than the one provided.
    TimeStamp(TimeStamp base) { 
        logicalTime = base.getLogicalTime() + 1;
        procID = base.getProcID();
    }

// get current time value.
    public long getLogicalTime() {
        return(logicalTime);
    }

// get assigned procID
    public long getProcID() {
        return(procID);
    }

// update this logical time and return TRUE if this update is later than the current
// time stamp value - which means we can update the state that the Lamport Cell covers
    boolean updateOK(TimeStamp otherTime) {
        long otherTimeValue = otherTime.getLogicalTime();
        if(logicalTime > otherTimeValue)
            // I am already ahead so ignore the message.
            return(false);

        if(logicalTime == otherTimeValue) {
            if(procID < otherTime.getProcID())
            // Times are equal but I loose ties
            return(false);
        }

        logicalTime = otherTimeValue;   // advance my clock.
        return(true);
    }
}

/**
* The Rock Unum illustrates a class that is a member of a multicast region
* It contains a String called myValue that can be changed by those which
* hold the changeSwissNumber. (Since we are doing a simple example, we just
* pass that capability around to all presences - in a real world system
* it would be disclosed only to those with a need to change it).
*/
eclass RockUnum implements RtEncodeable, RtDecodeable, RtUniquelyCodeable {
    TimeStamp valueTime;
    String myValue;
    RegionClientUnum myRegion;
    long changeSwissNumber;
    SturdyRef masterRegistration = null;
    Long myIDCode;

// Create the prime presence of a rock unum, initialized to reside in the given region
// register it with the designated SturdyRefMaker and initialize the state to theValue.
    RockUnum(SturdyRefMaker theMaker, RegionClientUnum theRegion, String theValue) {
        valueTime = new TimeStamp();
        myRegion = theRegion;
        myValue = theValue;
        EObjectWrapper myCap;
        changeSwissNumber = UniqueUnumRegistry.randomLong();    // generate change capability

        myIDCode = UniqueUnumRegistry.genUnumID();  // register as an unique ID with comm system.
        ((ClientRegInterface) myRegion).presenceToRegisterWithCommSystem(this, myIDCode);

        // Register the prime presence so other presences can reconnect after quakes.
        SturdyRef ref;
        masterRegistration = ref;
        theMaker <- makeSturdyRef(this, &ref);
        ewhen ref (SturdyRef theRef) {
            masterRegistration = theRef;
            EStdio.out().println("MasterRef: " + masterRegistration);
        }
    }

// Encode yourself for transmission over a comm system connection.
    local void encode (RtEncoder encoder) {
        EStdio.out().println("begin myRegion Rock");
        encoder.encodeObject(myRegion);             // Region I belong to.
        encoder.encodeObject(myValue);              // Current value of local state.
        encoder.encodeObject(masterRegistration);   // Registration of prime presence.
        encoder.encodeObject(myIDCode);             // ID code for identity anti-aliasing.
        try {
            encoder.writeLong(valueTime.getLogicalTime());  // Send current logical timestamp.
            encoder.writeLong(changeSwissNumber);   // and the change capability swiss number
        }
        catch (IOException ex) {
            throw new RtRuntimeException("IOException: " + ex.getMessage());
        }

        EStdio.out().println("end encode Rock");
    }

// Class to decode as on the other side of the connection.
    local String classNameToEncode(RtEncoder encoder) {
        return("ec.tests.nu.RockUnum_$_Impl");
    }

// Decode state on other side of the connection. Lookup myIDCode in the region's
// registration table to avoid creating an alias unum.
    local Object decode (RtDecoder decoder) {
        EStdio.out().println("begin decode Rock");
        try {
            myRegion = (RegionClientUnum) decoder.decodeObject();
            myValue = (String) decoder.decodeObject();
            masterRegistration = (SturdyRef) decoder.decodeObject();
            myIDCode = (Long) decoder.decodeObject();
            valueTime = new TimeStamp(decoder.readLong());
            changeSwissNumber = decoder.readLong(); // JAY 
            EStdio.out().println("decode Accomplished for Rock");

        } catch (IOException ex) {
            throw new RtRuntimeException("IOException: " + ex.getMessage());
        }

        Object myObj =  ((ClientRegInterface) myRegion).presenceToRegisterWithCommSystem(this, myIDCode);
        EStdio.out().println("decoded Rock: " + myObj);
        return(myObj);
    }

// The following two methods work together to cause a local state change to propogate.
// The first one originates the request while the second one handles notifications
// sent from the server.
// A real object implementing multiple requests would have more pairs like this, a pair
// for each request type. 


// Handle a local request to change the state. We change our local state variable
// as well as originate a multicast through the region this una belongs to.
    emethod setNewName(String theNewName) {
        myValue = theNewName;
        EStdio.out().println("setNewName: " + myValue);
        // Produce an envelope containing the notification. The message name should
        // match the method name of the method which receives the notification requests.
        // (the other method in the pair).
        RtEnvelope env;
        env <- (RockUnum).notifyNameChange(new TimeStamp(valueTime), theNewName);
        // Send the envelope to the region for re-distribution.
        myRegion <- transmitMulticast(changeSwissNumber, this, env);
    }

// This emethod receives the notification from the region that a state change
// has occured. It checks the TimeStamp to verify that this notification is
// chronologically greater than the last one received.
// In this example, all subscribers, including the originator, receive this
// notification.
    emethod notifyNameChange(TimeStamp stamp, String theNewName) {
        if(valueTime.updateOK(stamp)) {
            myValue = theNewName;   
            EStdio.out().println("notifyNameChange: " + myValue);
        } else {
            EStdio.out().println("earlier time stamp: " + myValue + " " + stamp);
        }
    }
}
