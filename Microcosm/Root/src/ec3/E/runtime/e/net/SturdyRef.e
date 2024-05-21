package ec.e.net;

import ec.util.NestedException;
import ec.e.lang.EString;
import ec.e.file.EStdio;
import java.io.Serializable;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import ec.e.serialstate.StateInputStream;
import ec.e.serialstate.StateOutputStream;
import ec.e.start.EEnvironment;


/**
   Object reference that survives network partitions.

You've got a SturdyRef.  You decide you want the object, so you call
followRef.  One of two things happens:

1) your result distributor gets forwarded to a proxy to the object.

2) an EException is ethrown.  Ideally there will be only be one type
of eexception you need to worry about here.  Right now things aren't
nearly so unified.  Some of the execptions you might have to deal with
include:

 ethrow new RegistrarLookupEException("Not on the air.");
  if your current process is isolated.

 ethrow new RegistrarLookupEException("object not registered");
  you reached a registrar, but it doesn't know about that object,
  either because you typed the url wrong, or it's been expired.

 ethrow new RegistrarLookupEException("invalid URL: " + url);
  if the url you gave doesn't parse (should only be a problem for
  imported SturdyRef's, but you never know.)

 myConnection.doEThrow(new ConnectionDeadEException("Problem delivering message to " + myConnection + ": " + e));
  the connection to the remote registrar that should know about this
  object was declared dead before your lookup request was sent.

 new ConnectionDeadEException("Failed to connect: " + cause);
  your request initiated a connection attempt (or arrived during a
  connection attempt initiated elsewhere), and that attempt failed.

 new ConnectionDeadEException("This EConnection has been disabled: " + myCause));
  your request was delivered, and you may have even received a
  response, but your exception environment was still exported when this
  connection was declared dead.

Actually, it looks like there are only two eexceptions you need to
worry about.  Perhaps "Not on the air." should be changed to a
ConnectionDeadEException.  

Now, if your followRef failed, you may wish to retry it.  A
ConnectionDeadEException at this point says ``the process hosting this
object is inaccessable at this time.''  Nothing can be said about when
(or if) it will become accessable.  The decision of when to give up
might need to be passed all the way up to the user.

Once you have the proxy to the remote object, any message send into
that proxy could ethrow a ConnectionDeadEException.  Once that
happens, that proxy will no longer function.  You need to get a new
one.  You do that by calling followRef on your SturdyRef.  That
followRef can fail just like the first one...

*/

public final class SturdyRef implements RtCodeable, 
        Serializable, RtStateUpgradeable {

    private static final Trace tr = new Trace("ec.e.net.SturdyRef");

        // Upgrade SerialVer
        static final long serialVersionUID = 2852509352910687798L;
    
    /* package */ transient Registrar myRegistrar;
    /* package */ String myRemoteSearchPath[];
    /* package */ String myRemoteRID;
    /* package */ String myRemoteObjectID;

    private SturdyRef() {}
    
    SturdyRef(Registrar registrar, String searchPath[], String remoteRID, String objectID) {
        if (tr.debug && Trace.ON) tr.debugm("new SturdyRef(" + remoteRID + "/" + objectID + ")");
        myRegistrar = registrar;
        myRemoteSearchPath = searchPath;
        myRemoteRID = remoteRID;
        myRemoteObjectID = objectID;
    }

    public String toString () {
        return super.toString() + "[remoteRID "+myRemoteRID+", remoteOID "+myRemoteObjectID+"]";
    }

        //ABS Start serialization support

        /**
         * Writes out all of the default fields of the object.
         */

        private void writeObject(java.io.ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();           
    }

        /**
         * Reads in all of the default fields of the object & gets
         * the environment from an instance of StateInputStream.
         */

        private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
            in.defaultReadObject();
        
            // Get environment from valid instance of StateInputStream
            if (in instanceof StateInputStream) {
                    
                StateInputStream i = (StateInputStream)in;
                EEnvironment env = i.getEEnvironment(this);

                if (env != null) {
                    myRegistrar = Registrar.summon(env);
                    return;
                } else {
                    throw new IOException("No Environment was available.");
                    
                }
            }
            throw new IOException("Read in from stream other than StateInput");
    }

        //ABS End serialization support

    public void followRef(EResult result) {
        if (tr.debug && Trace.ON) tr.debugm("followRef SturdyRef(" + myRemoteRID + "/" + myRemoteObjectID + ")");
        myRegistrar.lookupURL(myRemoteSearchPath, myRemoteRID, myRemoteObjectID, result);
    }

    // XXX PESSIMISM This is not needed if channels are properly optimistic.    
    public void sendToRef(RtEnvelope env) {
        if (tr.debug && Trace.ON) tr.debugm("sendToRef SturdyRef(" + myRemoteRID + "/" + myRemoteObjectID + ") envelope "+env);
        myRegistrar.sendToURL(myRemoteSearchPath, myRemoteRID, myRemoteObjectID, env);
    }

    public boolean equals(Object obj) {
        if (obj != null && obj instanceof SturdyRef) {
            return (((SturdyRef)obj).myRemoteRID.equals( myRemoteRID)) &&
                   (((SturdyRef)obj).myRemoteObjectID.equals(myRemoteObjectID));
        }
        return false;
    }

    public int hashCode() {
        return myRemoteRID.hashCode() + myRemoteObjectID.hashCode();
    }

    /* package */ EConnection getEConnection() {
        return myRegistrar.connectionsManager().connection(myRemoteRID, myRemoteSearchPath);
    }
    
    public String classNameToEncode (RtEncoder encoder) {
        return this.getClass().getName();
    }

    // XXX!!! looks to me like anybody can create an object that
    // implements RtEncoder, thereby extracting the url without
    // needing a registrar.  what prevents this???
    public void encode (RtEncoder encoder) {
        if (tr.debug && Trace.ON) tr.debugm("encode SturdyRef(" + myRemoteRID + "/" + myRemoteObjectID + ")");
        try {
            encoder.encodeObject(myRemoteSearchPath);
            encoder.writeUTF(myRemoteRID);
            encoder.writeUTF(myRemoteObjectID);
            
            // encode follower instead of registrar.
            // SturdyRefFollower is specially encoded as a known proxy
            // id.  Only a member of the ec.e.net package will be able
            // to reconstitute a registrar from this.
            encoder.encodeObject(myRegistrar.getSturdyRefFollower());
        } catch (Exception e) {
            if (tr.error) {
                EStdio.reportException(e);
            }
            throw new NestedException("Exception while encoding SturdyRef", e);
        }
    }

    public Object decode (RtDecoder decoder) {
        Registrar registrar;
        String searchPath[];
        String remoteRID;
        String objectID;
        SturdyRefFollower follower;
        
        try {
            searchPath = (String[])decoder.decodeObject();
            remoteRID = decoder.readUTF();
            objectID = decoder.readUTF();
            follower = (SturdyRefFollower)(decoder.decodeObject());
            
            // reconstitute the registrar from the follower.
            // getRegistrarHolder() is public (it has to be on an
            // eclass), but held() is package scoped.  With eclass
            // package scoping, we could just ask the follower for the
            // registrar.
            registrar = ((SturdyRefFollowerKludge)follower).getRegistrarHolder().held();
        } catch (Exception e) {
            if (tr.error) {
                EStdio.reportException(e);
            }
            throw new NestedException("Exception while decoding SturdyRef", e);
        }
        if (tr.debug && Trace.ON) tr.debugm("decode SturdyRef(" + remoteRID + "/" + objectID + ")");
        return new SturdyRef(registrar, searchPath, remoteRID, objectID);
    }
}

public eclass ESturdyRef {
    private SturdyRef myRef;
    
    public ESturdyRef(SturdyRef ref) {
        myRef = ref;
    }

    protected Object value() {
        return myRef;
    }
}
