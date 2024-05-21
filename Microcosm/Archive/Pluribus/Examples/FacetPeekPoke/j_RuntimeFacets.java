/*
  j_RuntimeFacets.java
  Arturo Bejar
  Copyright 1997 Electric Communities, All rights reserved.

*/

package ec.pl.examples.PeekPoke;

import ec.e.net.Registrar;
import ec.e.net.RegistrarLookupEException;
import ec.e.net.RegistrarException;
import ec.e.net.SturdyRef;
import ec.e.net.SturdyRefMaker;
import java.util.*;
import java.lang.RuntimeException;

interface jiRevocable {
  void revoke();
}
  
// This interface is called from a client presence that is trying
// to reanimate itself

einterface eiBundleDispenser {
  giveMeMyBundle(EResult ebundle);
}

// This is called into the host ingredient to sent the client vector

interface jiSetClientVector {
  void setClientVector(Vector v);
}

interface jiGetUnumRouter {
  Object getUnumRouter();
}

interface jiGetURL {
  String getURL();
}

/**
* eUnumRuntimeFacets
* This is the base class for a runtime facet that stops presence
* spread. The special feature is the method called reAnimate which is
* called at: decode, connection loss, quake, it contact the host and
* get the current correct state and the means to keep it consistent.
* 
*/

eclass eUnumRuntimeFacet  
    implements RtCodeable, RtUniquelyCodeable, jiRevocable {

  // Static method, hack today to get hands on the ERegistrar
  static private SturdyRefMaker STURDYREFMAKER;

  static public void setSturdyRefMaker(SturdyRefMaker refMaker) {
    STURDYREFMAKER = refMaker;
  }

  EObject myUnumQueue; // Generic channel
  EResult myUnumQueueDist;
  Object myUnumRouter; // Overridden in inheritance.

    // How do you unregister sturdyRefs?
  // ERegistration myRegistration;
  // This variable is the important one and only one to
  // get encoded.
  SturdyRef myIdentityRef; // Not necessarily the host

  local void revoke() {
    try {
//      myRegistration.unRegister();          
    } catch (Exception ex) {
//      ex.printStackTrace();
    }
  }
  
  final void register(eBundleDispenser dispenser) {
    System.out.println("[eUnumRuntimeFacet]:register:"+this+":"+dispenser);

    try {
            SturdyRef ref; // Channel instantiation
      myIdentityRef = ref;
      STURDYREFMAKER <- makeSturdyRef(dispenser, &ref);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    System.out.println("[eUnumRuntimeFacet]:register:Registered"+myIdentityRef);
  }

  // To be called after: decode, quake or disconnect.
  // This method tries to revive the unum, can throw a variety
  // of eexceptions depending on what happens when it tries to
  // connect.

  emethod reAnimate() {

    // Set up new queue if there isn't one.
    if (myUnumQueue == null) {
            myUnumQueueDist = setUnumQueue();
            
      myUnumRouter = null;
    }
        
    eRuntimeFacetBundle ebundle;
    eiBundleDispenser toSturdyUnumFacet;

    toSturdyUnumFacet <- giveMeMyBundle(&ebundle);

    // When the bundle gets sent back from eSturdyUnumFacet
    ewhen ebundle(jRuntimeFacetBundle bundle) { 
      // Forward channel to deliver all pending messages
      myUnumQueueDist <- forward((EObject)bundle.getUnumRouter()); 

      // Set unum router for direct delivery
      setUnumRouter(bundle);            

      // Null queue to set up direct delivery
      myUnumQueue = null;
    }

    // Attempt contacting the host facet
//    etry {
      myIdentityRef <- followRef(&toSturdyUnumFacet);
//   } ecatch (Throwable e) {
      // If the lookup failed it was 'cos of hangover or malice?
//      System.out.println("Could not look up runtime facet: " +
//                   e.getMessage());
      // Notify the runtime of total facet death exception
      
//    }
  }

  // These function to be overridden by inherited generated
  // code to make sure that all is of the right kind/interface
  void setUnumRouter(jRuntimeFacetBundle bundle) {
    myUnumRouter = (Object)bundle.getUnumRouter();
  }

    EResult setUnumQueue() {
        return null;
    }

  // Encodes just sturdy reference, for unum handoff.

  local void encode(RtEncoder encoder) {
    try {
            System.out.println("[eUnumRuntimeFacet]:encode:"+this+":"+myIdentityRef);
      encoder.encodeObject(myIdentityRef);
    } catch (Exception e) {
      // XXX Recover gracefully from failiure here
      e.printStackTrace();
    }
  }

  // Decode - decodes sturdy reference ant tries to reAnimate.
  local Object decode(RtDecoder decoder) {
    try {
      myIdentityRef = (SturdyRef)decoder.decodeObject();
      System.out.println("[eUnumRuntimeFacet]:decode:"+myIdentityRef);
    } catch (Exception e) {
      // XXX Recover gracefully from failiure here
      e.printStackTrace();
      return(null);
    }
    reAnimate();
    return this;
  }

  local String classNameToEncode (RtEncoder encoder) {
    return(this.getClass().getName());      
  }

  // Quake behaviour, could not find Seismologist so made up...
  void eekAQuake() {
    reAnimate();
  }

} 

/**
* jRuntimeFacetBundle is the base class of how the graph
* of objects is actually created.
*
* This class should only be encoded from the host to the client
*/

abstract class jRuntimeFacetBundle 
  implements RtCodeable, RtUniquelyCodeable, 
    jiRevocable, jiGetUnumRouter {

  EObject myUnumRouter = null; // Inherited will need right init interface

  // Ingredients
  // kind iskFoo mypinFoo;
  // kind pkHostFoo mypfHostFoo;
  // Vector mycvClientFoo;

  // Encode method for this (Generated in the future):
  // - Encodes unumRouter.
  // And fore each ingredient:
  // - Encodes reference to host.
  // - Adds to client vector.
  // - Gets snapshot of state

  public final void encode(RtEncoder encoder) {
    try {
      // Encode unumRouter
      encoder.encodeObject(getUnumRouter());
      encoder.encodeObject(getHostFacet());
      encoder.encodeObject(getDistToClient());
      encoder.encodeObject(getState());
    } catch (Exception e) {
      // XXX Recover gracefully from failiure here
      e.printStackTrace();
    }
  }

  // Decode
  // - Inits local presence of the unum

  public final Object decode(RtDecoder decoder) {
    try {
      // Decode unumRouter
      myUnumRouter = (EObject)decoder.decodeObject();
      Object hostFacet = decoder.decodeObject();
      Object distToClient = decoder.decodeObject();
      Object state = decoder.decodeObject();
    
      initTarget(myUnumRouter, hostFacet, distToClient, state);
      return this;
    } catch (Exception e) {
      // XXX Recover gracefully from failiure here
      e.printStackTrace();
      return null;
    }
  }
  public String classNameToEncode (RtEncoder encoder) {
    return(this.getClass().getName());      
  }
  
  public Object getUnumRouter() {
    return myUnumRouter;
  }
  Object getHostFacet() {
    return null;
  }
  Object getDistToClient() {
    return null;
  }
  Object getState() {
    return null;
  }
  void initTarget(Object unumRouter,
                  Object hostFacet,
                  Object distToClient,
                  Object state) {
  }
  public void revoke() {
  }
  
}

eclass eRuntimeFacetBundle  {
  jRuntimeFacetBundle myBundle;
  eRuntimeFacetBundle(jRuntimeFacetBundle bundle) {
    myBundle = bundle;
  }
  jRuntimeFacetBundle value() {
    return myBundle;
  }
}



// This class should inherit the cleanup behaviour for the
// client presences vector

eclass eBundleDispenser 
  implements eiBundleDispenser, jiRevocable {

  eRuntimeFacetBundle myEBundle;
  jiRevocable myJBundle;

  public eBundleDispenser(jRuntimeFacetBundle bundle){

    myEBundle = new eRuntimeFacetBundle(bundle);
    myJBundle = (jiRevocable)bundle;
  }

  emethod giveMeMyBundle(EResult ebundleDist) {
    ebundleDist <- forward(myEBundle);
  }

  local void revoke() {
    // Unregister
    myJBundle.revoke();
  }
}


//  jMulticastVector
// This guy will have to implement connection cleanup
// for client presences, the host remains agnostic
// of the list of client presences.

public class jMulticaster {
  Vector myVector;

  jMulticaster(Vector aVector) {
    myVector = aVector;
  }
  
  void sendToClients(RtEnvelope env) {
    EObject thisObj; 
    Enumeration enum = myVector.elements();
    while(enum.hasMoreElements()) {
      thisObj = (EObject)enum.nextElement();
      thisObj <- env;
    }
  }
}

/**
*   Base E exception
*
*/
public class eeException extends RuntimeException {
    public eeException(String exceptionString) {
        super(exceptionString);
    }
}

/**
* Base Java
* 
* The reason it extends RuntimeException is because you don't have to
* declare runtime exceptions in Java method signatures, recommended by
* Chip. (its all his fault anyways...)
* 
*/

public class jeException extends RuntimeException {
    public jeException(String exceptionString) {
        super(exceptionString);
    }
}

/**
* eeOhNoYouDont
* This exception takes place when somebody tried to do something
* that they are not supposed to be doing, it should not happen in the 
* first place. For example if someone tries to have an ingredient send
* a message without the corresponding facet. Due to the nature of
* facets the code has to be there, by the same token due to the nature
* of facets (i.e. if you have the unum facet the message can get sent).
* 
*/

public class eeOhNoYouDont extends eeException {
    public eeOhNoYouDont(String exceptionString) {
        super(exceptionString);
    }  
}

/*
* eeFacetDisconnect
* A facet disconnect exception means that the current presence
* of the unum has become disconnected from the host, the host
* might still be reachable at a later point in time.
*/

public class eeFacetDisconnect extends eeException  {
    public eeFacetDisconnect(String exceptionString) {
        super(exceptionString);
    }
  
}

/*
    Facet death means that there was a succesful connection to the
    machine which hosts the facet but it was not avaiable at lookup,
    this means one of two things:
    - The host facet has been revoked and unregistered.
    - The host facet was put in the table after the last checkpoint
      and therefore it was lost.

        Either way it means that the unum facet is dead and useless,
        with no possibility or revival, such references should be dropped.
    
*/

public class eeFacetDeath extends eeException  {
    public eeFacetDeath(String exceptionString) {
        super(exceptionString);
    }   
}

