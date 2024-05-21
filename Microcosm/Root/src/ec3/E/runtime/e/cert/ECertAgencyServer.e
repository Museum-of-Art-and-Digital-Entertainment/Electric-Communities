package ec.app.cert;

import ec.trace.Trace;
import ec.e.lang.*;
import ec.e.run.*;
import ec.e.net.Registrar;
import ec.e.net.RegistrarLookupEException;
import ec.e.net.RegistrarException;
import ec.e.net.SturdyRef;
import ec.e.net.SturdyRefMaker;
import ec.e.net.SturdyRefFileExporter;
import ec.e.net.RtForwardingSturdyRef;
import java.security.PublicKey;
import java.security.KeyPair;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import ec.e.quake.TimeMachine;
import ec.e.timer.Timer;
import ec.util.NestedException;
import ec.cert.VerifierDescription;
import ec.cert.EJavaObjectWrapper;
import ec.cert.EVerifierServer;
import ec.cert.CertAgencyServerInt;
import ec.e.util.PropUtil;
import java.io.Serializable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import ec.e.serialstate.StateInputStream;
import ec.e.serialstate.StateOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Properties;

eclass CertAgencyServer implements ELaunchable {
  emethod go (EEnvironment env) {
    // First get file name for state bundle
    String saveFileName = env.getProperty("SaveFile");

    // Build the server
    ECertAgencyServer aCertAgencyServer = 
      new ECertAgencyServer(env, saveFileName);

    aCertAgencyServer <- go();
  }

}


/**
 * This is the most public facet to the Certification Agency Server. No 
 * agency data can be altered with these methods.
 */

eclass SafeFacetToCertAgencyServer
implements CertAgencyServerInt {
  protected ECertAgencyServer myRealServer;

  public SafeFacetToCertAgencyServer(ECertAgencyServer theServer) {
    myRealServer = theServer;
  }

  emethod getVerifierServers(EResult whoWantsToKnow) {
    myRealServer <- 
      getPublicVerifierServers(whoWantsToKnow);
  }

  emethod createNewCertificateType(KeyPair aKeyPair, 
                                   VerifierDescription description) {
  }

  emethod ping(EResult whoWantsToKnow) {
    myRealServer <- ping(whoWantsToKnow);
  }

  emethod saveNeeded() {
  }

  emethod saveState() {
  }
}

/**
 * This is a semi-public facet to the Certification Agency Server. References
 * to this facet should only be given out to entities trusted to alter cert
 * agency data. 
 */
eclass TrustedFacetToCertAgencyServer 
extends SafeFacetToCertAgencyServer {

  public TrustedFacetToCertAgencyServer(ECertAgencyServer theServer) {
    super(theServer);
  }

  emethod getVerifierServers(EResult whoWantsToKnow) {
    myRealServer <- 
      getVerifierServers(whoWantsToKnow);
  }

  emethod createNewCertificateType(KeyPair aKeyPair, 
                                   VerifierDescription description) {
    myRealServer <- createNewCertificateType(aKeyPair, description);
  }

  emethod ping(EResult whoWantsToKnow) {
    myRealServer <- ping(whoWantsToKnow);
  }

  emethod saveNeeded() {
    myRealServer <- saveNeeded();
  }

  emethod saveState() {
    myRealServer <- saveState();
  }
}

/**
 * This class wraps all Certificate Agency server state that we want to save.
 */
class CertAgencyState implements Serializable {
  protected Registrar registrar;  // need to save this to validate SRefs
  protected RtForwardingSturdyRef trustedFSRef;
  protected RtForwardingSturdyRef safeFSRef;
  protected Vector verifierServers = new Vector();
}  


eclass ECertAgencyServer implements CertAgencyServerInt {
  private EEnvironment myEnv;
  private TrustedFacetToCertAgencyServer myTrustedPublicFacet;
  private SafeFacetToCertAgencyServer mySafePublicFacet;
  private boolean iNeedSaving = false;
  private String mySaveFileName = null;
  private static Trace myTrace = new Trace("CertAgency");
  private SturdyRef myTrustedPublicSRef;
  private SturdyRef mySafePublicSRef;

  // This is the holder for my presistant state. Everything else can go
  private CertAgencyState myState = null;

  protected ECertAgencyServer(EEnvironment env, String saveFileName) {
    boolean virginState = false;

    myEnv = env;

    // Make my interfaces
    myTrustedPublicFacet =
      new TrustedFacetToCertAgencyServer(this); //B
    mySafePublicFacet =
      new SafeFacetToCertAgencyServer(this);  //B

    // Deal with the save file and check if I am a virgin
    if (saveFileName != null) {
      mySaveFileName = saveFileName;
      File saveFile = new File(saveFileName);
      // if a save file exists initialize from it
      try {
        if (myTrace.debug && Trace.ON) {
          myTrace.debugm("Cert Agency Server initializing from: "+
                         saveFileName);
        }
        // Unserialize myself
        FileInputStream in = new FileInputStream(mySaveFileName);
        StateInputStream s = new StateInputStream(env, in);
        myState = (CertAgencyState)s.readObject();
        in.close();
        // Cert Kind servers don't remember us so we have to tell them
        // about us.
        Enumeration certKindServers = myState.verifierServers.elements();
        while (certKindServers.hasMoreElements()) {
          EVerifierServer server = (EVerifierServer)certKindServers.nextElement();
          server <- setCertAgencyServer(this);
        }
      } catch (FileNotFoundException fnf) {
        virginState = true;
        if (myTrace.debug && Trace.ON) {
          myTrace.debugm("Starting a new Cert Agency Server");
        }
        myState = new CertAgencyState();
        // Start up a registrar to handle sturdy references to 
        // various services.
        myState.registrar = Registrar.summon(myEnv);
      } catch (Exception e) {
        String em = "Could not recover Certificate Agency Server: ";
        myTrace.errorm(em + e + ": " + e.getMessage());
        e.printStackTrace();
        System.exit(0);
      }
    }


    try {
      myState.registrar.onTheAir();  
    } catch (RegistrarException e) {
      throw new Error("fatal registrar problem going on the air: " + e);
    }


    if (virginState) {
      // Make forwarding sturdyRefs and get real sturdyRefs
      SturdyRefMaker anSRefMaker = 
        myState.registrar.getSturdyRefMaker();
      myState.trustedFSRef =
        anSRefMaker.makeForwardingSturdyRef(myTrustedPublicFacet);
      myState.safeFSRef =
        anSRefMaker.makeForwardingSturdyRef(mySafePublicFacet);
      this <- saveNeeded();
    } else {
      // Use the saved forwarding SRefs and re-target
      try {
        myState.trustedFSRef.setTarget(myTrustedPublicFacet);
        myState.safeFSRef.setTarget(mySafePublicFacet);
      } catch (Exception e) {
        // XXX - It would be good if I didn't have to catch a generic Exception
        // but that's what setTarget throws.
        throw new RuntimeException(e.getMessage());
      }
    }

    // Set these for convenience
    myTrustedPublicSRef = 
      myState.trustedFSRef.getSturdyRef();
    mySafePublicSRef = 
      myState.safeFSRef.getSturdyRef();

    if (virginState) {
      // Export the SRefs
      SturdyRefFileExporter exporter = 
        myState.registrar.getSturdyRefFileExporter(myEnv);
      try {
        exporter.exportRef(myTrustedPublicSRef, 
                           "TrustedCertAgencySRef");
        exporter.exportRef(mySafePublicSRef, 
                           "SafeCertAgencyRef");
      } catch (java.io.IOException e) {
        if (myTrace.debug && Trace.ON) {
          myTrace.debugm("Couldn't export my sturdy refs");
        }
      }
    }
  }

  
  emethod go() {
    this <- checkpoint();
  }


  /*
  emethod tick() {
    EBoolean tock = (EBoolean) EUniChannel.construct(EBoolean.class);
    EUniDistributor tock_dist = EUniChannel.getDistributor(tock);
    myTimer.setTimeout( myTimeOutDuration, etrue, tock_dist);
    if (myTrace.debug && Trace.ON) {
      myTrace.debugm("Checkpointing w/ ("+
                     myState.verifierServerRefs.size()+
                     ") known certificate types...");
    }
    checkpoint();
    if (myTrace.debug && Trace.ON) {
      myTrace.debugm("Done");
    }
    ewhen tock (boolean ingnored) {
      this <- tick();
    }
  }
  */

  emethod checkpoint() {
    myTrace.debugm("!!! In checkpoint");
    if (iNeedSaving) {
      iNeedSaving = false;
      if (myTrace.debug && Trace.ON) {
        myTrace.debugm ("!!! Checkpointing");
      }
      if (mySaveFileName != null) {
        try {
          String tempFileName = "temp."+mySaveFileName;
          String histFileName = mySaveFileName+".000";

          File tempFile = new File(tempFileName);
          File histFile = new File(histFileName);
          File saveFile = new File(mySaveFileName);

          // First write out our state to a temp file
          FileOutputStream out = new FileOutputStream(tempFile);
          StateOutputStream s = new StateOutputStream(out);
          s.writeObject(myState);
          s.flush();
          out.close();

          // Now shuffle files
          // Delete history file if it exists
          if (histFile.exists()) {
            if (myTrace.debug && Trace.ON) {
              myTrace.debugm ("!!! Deleting "+ histFile.getName());
            }
            histFile.delete();
          }

          // If it exists, rename old save file to history file
          if (saveFile.exists()) {
            if (myTrace.debug && Trace.ON) {
              myTrace.debugm ("!!! Renaming "+ saveFile.getName() +
                                " to "+ histFile.getName());
            }
            if (!saveFile.renameTo(histFile)) {
              if (myTrace.debug && Trace.ON) {
                myTrace.debugm ("!!! ...Failed");
              }
            }
            saveFile.delete();
          }
          // Rename temp file to save file
          if (myTrace.debug && Trace.ON) {
            myTrace.debugm ("!!! Renaming "+ tempFile.getName() +
                              " to "+ saveFile.getName());
          }
          if (!tempFile.renameTo(saveFile)){
            if (myTrace.debug && Trace.ON) {
              myTrace.debugm ("!!! ...Failed");
            }
          }
        } catch (IOException ioe) {
          String em = "Could not save state: ";
          myTrace.errorm(em + ioe.getMessage());
        }
      }
      if (myTrace.debug && Trace.ON) {
        myTrace.debugm ("!!! Done Checkpointing");
      }
    }
  }

  emethod createNewCertificateType(KeyPair aKeyPair, 
                                   VerifierDescription description) {
    if (myTrace.debug && Trace.ON) {
      myTrace.debugm("Creating a new certificate: "+
                     System.currentTimeMillis());
    }
    EVerifierServer  newVerifierServer = 
      new EVerifierServer(description, aKeyPair, this, myState.registrar);
    if (myTrace.debug && Trace.ON) {
      myTrace.debugm("  adding server to lists");
    }
    myState.verifierServers.addElement(newVerifierServer);
    if (myTrace.debug && Trace.ON) {
      myTrace.debugm("  done w/ new certicate type");
    }
    saveNeeded();
    this <- saveState();
  }
  
  emethod getVerifierServers(EResult whoWantsToKnow) {
    whoWantsToKnow <- 
      forward(new EJavaObjectWrapper(myState.verifierServers));
  }

  emethod getPublicVerifierServers(EResult whoWantsToKnow) {
    whoWantsToKnow <- 
      forward(new EJavaObjectWrapper(EVerifierServer.getPublicFacets()));
  }

  emethod ping(EResult whoWantsToKnow) {
    whoWantsToKnow <- forward(enull);
  }

  emethod saveNeeded() {
    if (myTrace.debug && Trace.ON) {
      myTrace.debugm ("!!! Got a saveNeeded");
    }
    iNeedSaving = true;
  }

  emethod saveState() {
    this <- checkpoint();
  }
}





