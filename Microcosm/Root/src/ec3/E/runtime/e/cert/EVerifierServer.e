package ec.cert;

import ec.e.run.*;
import ec.e.net.Registrar;
import ec.e.net.RegistrarLookupEException;
import ec.e.net.RegistrarException;
import ec.e.net.SturdyRef;
import ec.e.net.SturdyRefMaker;
//import ec.e.net.SturdyRefFileImporter;
import ec.e.net.RtForwardingSturdyRef;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.KeyPair;
import java.util.Vector;
import java.util.Hashtable;
import java.io.Serializable;
import java.io.IOException;
import ec.e.serialstate.StateInputStream;
import ec.e.serialstate.StateOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/**
 *
 */
einterface EPublicVerifierServerInterface {
  emethod getDescription(EResult whoWantsToKnow);
  emethod getVerifier(EResult whoWantsToKnow);
  emethod reCertify(Certificate aCertificate, 
                    EResult whoWantsToKnow);
  emethod getCertificate(CryptoHash objectInQuestion, 
                         EResult whoWantsToKnow);
  emethod getRevoked(EResult whoWantsToKnow);
  emethod getCertifier(EResult whoWantsToKnow);
  emethod submitForCertification(CertSubmissionBundle submissionBundle);
}


/**
 * Safe facet to a verifier server object. No certification state can be 
 * altered or secreat information exposed through this interface.
 */
eclass EPublicVerifierServer implements EPublicVerifierServerInterface {
  private EVerifierServer myRealServer;

  public EPublicVerifierServer (EVerifierServer theServer) {
    myRealServer = theServer;
  }

  emethod getDescription(EResult whoWantsToKnow) {
    myRealServer <- getDescription(whoWantsToKnow);
  }

  emethod getVerifier(EResult whoWantsToKnow) {
    myRealServer <- getVerifier(whoWantsToKnow);
  }

  emethod getCertifier(EResult whoWantsToKnow) {
    whoWantsToKnow <- forward(enull);
  }

  emethod reCertify(Certificate aCertificate, 
                    EResult whoWantsToKnow) {
    myRealServer <- reCertify(aCertificate, whoWantsToKnow);
  }

  emethod getCertificate(CryptoHash objectInQuestion, 
                         EResult whoWantsToKnow) {
    myRealServer <- getCertificate(objectInQuestion, whoWantsToKnow);
  }

  emethod getRevoked(EResult whoWantsToKnow) {
    myRealServer <- getRevoked(whoWantsToKnow);
  }

  emethod submitForCertification(CertSubmissionBundle certSubmissionBundle) {
    myRealServer <- submitForCertification(certSubmissionBundle);
  }

}  

/**
 * All the saveable state for the VerifierServer objects should go here.
 */

class VerifierServerState implements Serializable {
  protected VerifierDescription description;
  protected PublicKey pubKey; 
  protected PrivateKey privKey; 
  protected Vector shitList = new Vector();
  protected Hashtable certificateList = new Hashtable(500);  // XXX Tune me
  protected RtForwardingSturdyRef safeFSRef;
  protected Vector certSubmissionQueue;
}

eclass EVerifierServer 
implements EPublicVerifierServerInterface,  Serializable {

  private static Vector classPublicFacets = new Vector();
  private EPublicVerifierServer myPublicFacet;
  private CertAgencyServerInt myCertServer;
  private VerifierServerState myState = new VerifierServerState();

  private static Trace myTrace = new Trace("VerifierServer");

  public EVerifierServer(VerifierDescription aDescription,
                         KeyPair aKeyPair, 
                         CertAgencyServerInt certServer,
                         Registrar theRegistrar) {
    myState.description = aDescription;
    myState.pubKey = aKeyPair.getPublic();
    myState.privKey = aKeyPair.getPrivate();
    myPublicFacet = new EPublicVerifierServer(this);
    myCertServer = certServer;

    // to get around some e BS
    classPublicFacets.addElement(myPublicFacet);

    // register a sturdy ref for this server
    SturdyRefMaker aSturdyRefMaker = theRegistrar.getSturdyRefMaker();
    myState.safeFSRef = 
      aSturdyRefMaker.makeForwardingSturdyRef(myPublicFacet);
  }

  public static Vector getPublicFacets() {
    return classPublicFacets;
  }

  private void readObject(ObjectInputStream s) 
  throws IOException {
    if (myTrace.debug && Trace.ON) {
      myTrace.debugm ("Reading Verifier State");
    }
    try {
      myState = (VerifierServerState)s.readObject();
    } catch (ClassNotFoundException cnfe) {
    } catch (IOException ioe) {
    }

    myPublicFacet = new EPublicVerifierServer(this);
    classPublicFacets.addElement(myPublicFacet);

    try {
      myState.safeFSRef.setTarget(myPublicFacet);
    } catch (Exception e) {
      // XXX - It would be good if I didn't have to catch a generic Exception
      // but that's what setTarget throws.
      throw new RuntimeException(e.getMessage());
    }
  }

  private void writeObject(ObjectOutputStream s) 
  throws IOException {
    if (myTrace.debug && Trace.ON) {
      myTrace.debugm ("Writing Verifier State");
    }
      try {
        s.writeObject(myState);
        s.flush();
      } catch (IOException ioe) {
        String em = "Could not save state: ";
        myTrace.errorm(em + ioe + ": " +ioe.getMessage());
      }
  }

  emethod setCertAgencyServer(CertAgencyServerInt certServer) {
    myCertServer = certServer;
  }

  emethod addCertificate(CryptoHash dataKey, 
                         Certificate aCertificate, 
                         int renewal) {
    if (myTrace.debug && Trace.ON) {
      myTrace.debugm("Trying to Add certificate for ("+dataKey+
                     ") to certificate list");
    }
    if ((aCertificate == null) || (dataKey == null)) {
      if (myTrace.debug && Trace.ON) {
        myTrace.debugm("certificate or date key is null");
      }
      return;
    }
    if (myState.certificateList.containsKey(dataKey)) {
      CertListBundle oldCert = 
        (CertListBundle)myState.certificateList.get(dataKey);
      if (oldCert.certificate.getExpirationDate() < 
          aCertificate.getExpirationDate()) {
        CertListBundle clb = new CertListBundle(aCertificate, renewal);
        myState.certificateList.put(dataKey, clb);
        myCertServer <- saveNeeded();
        if (myTrace.debug && Trace.ON) {
            myTrace.debugm("Replaced certificate ("+dataKey+
                           ") in certificate list");
        }
      } else {
        if (myTrace.debug && Trace.ON) {
            myTrace.debugm("Preexisting certificate ("+dataKey+
                           ") w/ same or later expiration date - ignoring");
        }
      }
    } else {
      CertListBundle clb = new CertListBundle(aCertificate, renewal);
      myState.certificateList.put(dataKey, clb);
      myCertServer <- saveNeeded();
      if (myTrace.debug && Trace.ON) {
        myTrace.debugm("Added certificate for ("+dataKey+
                       ") to certificate list");
      }
    }
    if (myTrace.debug && Trace.ON) {
      myTrace.debugm("List contains "+myState.certificateList.size()+
                     " certificates");
    }
  }    

  emethod revokeCertificate(CryptoHash aCryptoHash) {
    if (!myState.shitList.contains(aCryptoHash)) {
      myState.shitList.addElement(aCryptoHash);
    }
    myCertServer <- saveNeeded();

    if (myTrace.debug && Trace.ON) {
      myTrace.debugm("Added certificate ("+aCryptoHash+
                     ") to revocation list");
    }
  }

  emethod getDescription(EResult whoWantsToKnow) {
    whoWantsToKnow <- forward(new EJavaObjectWrapper(myState.description));
  }

  emethod getVerifier(EResult whoWantsToKnow) {
    // xxx clone myVerifier??
    Verifier aVerifier = new Verifier(myState.pubKey, 
                                      myState.description);
    aVerifier.setServerRef(myState.safeFSRef.getSturdyRef());
    whoWantsToKnow <- forward(new EJavaObjectWrapper(aVerifier));
  }


  emethod reCertify(Certificate aCertificate, 
                    EResult whoWantsToKnow) {
    // xxx do an actual check here
    Certificate newCertificate = null;
    whoWantsToKnow <- forward(new EJavaObjectWrapper(newCertificate));
  }

  emethod getCertificate(CryptoHash objectInQuestion, 
                         EResult whoWantsToKnow) {

    if (myTrace.debug && Trace.ON) {
      myTrace.debugm("Handling getCertificate request");
    }
    Certificate foundCertificate = null;
    if (myState.certificateList.containsKey(objectInQuestion)) {
      if (myTrace.debug && Trace.ON) {
        myTrace.debugm("**Certificate found");
      }
      CertListBundle aCertBundle = 
        (CertListBundle)myState.certificateList.get(objectInQuestion);
      // Check for expiration, may need to auto recertify
      int tzOffset = java.util.TimeZone.getDefault().getOffset(0,0,0,0,0,0);
      long currentDate = System.currentTimeMillis() - tzOffset;
      if (aCertBundle.certificate.getExpirationDate() <
          (currentDate)) {
        if (myTrace.debug && Trace.ON) {
          myTrace.debugm("**Certificate expired");
        }
          // Extend the expiration date buy the renewal period (which is in
          // days) if not on the shitlist
        if (!myState.shitList.contains(objectInQuestion)) {// Check the shitlist.
          if (myTrace.debug && Trace.ON) {
            myTrace.debugm("**Recertifing");
          }
          long delta = 24 * 3600000 * aCertBundle.renewalPeriod;
          long newExpirationDate = currentDate + delta;
          // Make a new certificate and replace the old one
          aCertBundle.certificate = 
            getCertifier().certify(objectInQuestion.getCopyOfHashBytes(),
                                   newExpirationDate);
          // Do a checkpoint
          foundCertificate = aCertBundle.certificate;
        } else {
          myState.certificateList.remove(objectInQuestion);
          if (myTrace.debug && Trace.ON) {
            myTrace.debugm("**Certificate revoked - "+
                           "removing from certificate list");
          }
        }
        myCertServer <- saveNeeded();
        myCertServer <- saveState();
      } else {
        foundCertificate = aCertBundle.certificate;
      }
    } 
    whoWantsToKnow <- forward(new EJavaObjectWrapper(foundCertificate));
  }

  emethod getRevoked(EResult whoWantsToKnow) {
    whoWantsToKnow <- forward(new EJavaObjectWrapper(myState.shitList));
  }

  emethod setSturdyRef(RtForwardingSturdyRef aSturdyRef) {
    myState.safeFSRef = aSturdyRef;
  }

  emethod test(String aString) {
    System.out.println(aString);
  }

  emethod getCertifier(EResult whoWantsToKnow) {
    whoWantsToKnow <- forward(new EJavaObjectWrapper(getCertifier()));
  }

  Certifier getCertifier() {
    Certifier aCertifier = 
      new Certifier(this, new KeyPair(myState.pubKey, myState.privKey),
                    new CryptoHash(myState.pubKey.getEncoded()));
    return aCertifier;
  }

  emethod submitForCertification(CertSubmissionBundle certSubmissionBundle) {
    if (myState.description.acceptsRequests()) {
      myState.certSubmissionQueue.addElement(certSubmissionBundle);
    }
  }


  emethod getSubmissionQueue(EResult whoWantsToKnow) {
    whoWantsToKnow <- forward(new EJavaObjectWrapper(myState.certSubmissionQueue));
  }

  emethod removeFromSubmissionQueue(CertSubmissionBundle certSubmissionBundle) {
    myState.certSubmissionQueue.removeElement(certSubmissionBundle); 
  }
}

/**
 * A generic wrapper for java objects used by the VerifierServer
 */
public eclass EJavaObjectWrapper {
  Object myObject;

  public EJavaObjectWrapper (Object anObject) {
    myObject = anObject;
  }

  Object value() {
    return myObject;
  }
}

/* deleted during move from ec3 to ec4...
// XXXXXXXXXXXXXX changed from package to public in move from ec3 to ec4 -emm
public final class VerifierServerFactory {
  public static
  Object[] createNewVerifierServer(KeyPair aKeyPair, 
                                    VerifierDescription description,
                                    Registrar theRegistrar) {
    // XXX First check to see if there is already a EVerifierServer
    // for this public key
    EVerifierServer aNewVerifierServer = 
      new EVerifierServer(description, aKeyPair);
    EPublicVerifierServer facitToVerifierServer =
      new EPublicVerifierServer(aNewVerifierServer);
    // XXX register a sturdy ref for this server
    SturdyRefMaker aSturdyRefMaker = theRegistrar.getSturdyRefMaker();
    SturdyRef theSturdyRef = 
      aSturdyRefMaker.makeSturdyRef(facitToVerifierServer);
    aNewVerifierServer <- setSturdyRef(theSturdyRef);
    Object[] serverAndFacit = {facitToVerifierServer, aNewVerifierServer};
    return serverAndFacit;
  }
}
*/

class CertListBundle implements Serializable {
  Certificate certificate;
  int renewalPeriod;

  CertListBundle(Certificate cert, int renewal) {
    certificate = cert;
    renewalPeriod = renewal;
  }
}
