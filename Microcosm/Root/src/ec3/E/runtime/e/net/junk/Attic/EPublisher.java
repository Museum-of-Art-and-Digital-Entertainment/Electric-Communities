/*
  EPublisher implements a capability to contact public remote E objects.
  
  To retrieve the capability from an EEnvironment env:
  EPublisher pub = (EPublisher)env.get("publisher.root");

  What you can do with the capability:

    EPublication publish(String name, EObject obj)
      throws EPublicationException;
    void unPublish(String name)
      throws EPublicationException;
    void lookupURL(String url, EDistributor result)
      throws EInvalidUrlException;

  future: Enumeration Catalog(Rrid);

  For conveinence, EPublisher.lookupURL and ERegistrar.lookupURL both
  provide the same functionality; they can be used interchangably.
  
  To publish an object so that it can be found by any outside process
  call publish(name, object).  publish() returns an EPublication which
  can be used to unPublish() and to retrieve the URL string representing
  the reference via getURL().
  
  To remove a name->object binding from the catalog, call
  unPublish(name).  unPublish()ing does not revoke any capabilities to
  the object that have already been obtained from the catalog.
  
  Once you have a published URL string, you can contact the referenced
  object with lookupURL(url, result).  result will be forwarded to the
  referenced object on the remote system.
*/

package ec.e.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.InternalError;
import java.util.Hashtable;

final public class EPublisher {
    static Trace tr = new Trace(false, "[EPublisher]");
    
    private RtPublicationServer myPublicationServer;
    
    /* create unrestricted RtPublications only callable in package called from
       RtRootCapabilities */
    EPublisher(RtRegistrarServer reg) {
        if (tr.tracing)
            tr.$("Creating root EPublisher capability");
        
        myPublicationServer = reg.getPublicationServer();
    }
    
    /* below here is public interface */
    
    public EPublication publish(String name, EObject obj)
            throws EPublicationException {
        return(myPublicationServer.publish(name, obj));
    }
    
    public void unPublish(String name)
            throws EPublicationException {
        myPublicationServer.unPublish(name);
    }
    
    public void lookupURL(String url, EDistributor result)
            throws EInvalidUrlException {
        myPublicationServer.lookupURL(url, result);
    }
}

final class RtPublicationServer {
    static Trace tr = new Trace(false, "[RtPublicationServer]");
    
    private Hashtable myPublishedObjects = new Hashtable();
    private String myRegistrarId;
    private String myPublisherId;
    private EDirectoryServer myDirectoryServer;
    private RtRegistrarServer myRegistrarServer;
    
    /* called from RtPublications */
    RtPublicationServer(RtRegistrarServer registrarServer) {
        if (tr.tracing)
            tr.$("Creating new RtPublicationServer");
        myRegistrarServer = registrarServer;
        myRegistrarId = myRegistrarServer.getRegistrarId();
        myPublisherId = myRegistrarServer.getPublisherId();
    }
    
    void setEDirectoryServer(EDirectoryServer directoryServer) {
        myDirectoryServer = directoryServer;
    }
    
    /* called from RtPublications */
    EPublication publish(String name, EObject object)
            throws EPublicationException {
        if (myPublishedObjects.containsKey((Object) name)) {
            tr.$("publication of '" + name + "' in " + myPublisherId +
                 " fails");
            throw new EPublicationException("publish: duplicate of '" + name +
                                            "'");
        } else {
            tr.$("publishing '" + name + "' in " + myPublisherId);
            myPublishedObjects.put((Object) name, (Object) object);
            return(new EPublication(this,
                new RtEARL(myRegistrarServer.getSearchPath(), myPublisherId,
                           name, null), name));
        }
    }
    
    /* called from RtPublications */
    void unPublish(String name) throws EPublicationException {
        if (myPublishedObjects.containsKey((Object) name)) {
            myPublishedObjects.remove((Object) name);
        } else {
            throw new EPublicationException("unpublish: no object '" + name +
                                            "' in " + myPublisherId);
        }
    }
    
    /* called from RtPublications */
    void lookupURL(String url, EDistributor result)
            throws EInvalidUrlException {
        RtEARL earl = new RtEARL(url);
        String Rrid = earl.getRegistrarId();
        String objId = earl.getObjectId();
        RtConnection connection;
        EDirectoryServer directoryServer;
        
        if (Rrid.equals(myRegistrarId) || Rrid.equals(myPublisherId)) {
            directoryServer = myDirectoryServer;
        } else {
            connection = myRegistrarServer.getConnectionToRegistrar(Rrid, null,
                earl.getSearchPath(), false);
            directoryServer = connection.getDirectoryServerProxy();
        }
        directoryServer <- lookupName(objId, result);
    }
    
    /* called from EDirectoryServer */
    void lookupName(String name, EDistributor result)
            throws EPublicationException {
        tr.$("lookupName '" + name + "' in " + myPublisherId);
        if (myPublishedObjects.containsKey((Object) name)) {
            tr.$("found an object");
            EObject resultObject =
                (EObject) myPublishedObjects.get((Object) name);
            result <- forward(resultObject);
        } else {
            tr.$("couldn't find an object");
            throw new EPublicationException("lookupName: no object '" + name +
                                            "'");
        }
    }
}

public class EPublication {
    private RtPublicationServer myLocation;
    private RtEARL myEarl;
    private String myName;
    
    EPublication(RtPublicationServer location, RtEARL earl, String name) {
        myLocation = location;
        myEarl = earl;
        myName = name;
    }
    
    public void unPublish() throws EPublicationException {
        myLocation.unPublish(myName);
    }
    
    public String getURL() {
        return(myEarl.getURL());
    }
    
    public RtEARL getEARL() {
        return(myEarl);
    }
}

public class EPublicationException extends Exception {
    public EPublicationException(String message) {
        super(message);
    }
}
