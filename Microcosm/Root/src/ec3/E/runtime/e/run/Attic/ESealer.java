package ec.e.run;

import java.io.*;

public interface ESealer extends EObject {
}

public abstract class ESealer_Proxy extends EProxy_Impl implements ESealer {
}

public abstract class ESealer_Channel extends ec.e.run.EChannel.EChannel_Impl implements ESealer{
}

public abstract class ESealer_Impl extends EObject_Impl implements ESealer, RtCodeable {
    private boolean hasUnsealer = false;
    protected int myIndex = -1;
    static final RtSealerKey sealerKey = new RtSealerKey();
	
    /** Interlock with unsealer creation. */
    synchronized public boolean attachUnsealer() {
        if (hasUnsealer) {
            return(false);
        } else {
            hasUnsealer = true;
            return(true);
        }
    }

    /** Abstract method for Sealers to return their distinguished instance. */
    abstract public ESealer otherSealer(RtSealerKey key, int msg);

    /** Deliver the envelope to an eobject; use my key to give us quick
        dispatch to the object. */
    public void sendMeTo(EObject_Impl obj, RtEnvelope envelope)
            throws RtBadSealerException {
        obj.deliver(this, envelope);
    }

    abstract public RtEnvelope streamInArgs(DBInputStream stream)
        throws IOException;

    public void deliver(ESealer key, RtEnvelope envelope)
            throws RtBadSealerException {
        throw new RtBadSealerException("send to ESealer not allowed");
    }
        
	public final void encode (RtEncoder coder) {
		try {
			coder.writeInt(myIndex);
	    } catch (Exception e) { 
	    	e.printStackTrace(); 
	    }
	}
	
	public final Object decode (RtDecoder coder) {
		Object theSealer = null;
		try {
		    int sealerNum = coder.readInt();		
		    theSealer = otherSealer(sealerKey, sealerNum);
		    if (theSealer == null) System.out.println("The Sealer is null, Chip!!");
	    	return theSealer;
	    } catch (Exception e) { 
	    	e.printStackTrace(); 
	    	return null; 
	    }
	}
}
