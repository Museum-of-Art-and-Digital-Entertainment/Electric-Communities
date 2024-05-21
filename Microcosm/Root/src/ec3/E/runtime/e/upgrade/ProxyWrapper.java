package ec.e.upgrade;

import java.io.IOException;

/**
 * A ProxyWrapper masquerades as a Proxy it wraps in order to convert
 * outgoing messages which are no longer implemented by the target into
 * sensible messages the target implements. A ProxyWrapper is used
 * to wrap an incoming Proxy over the wire from another process which has
 * an older version of the Object's class, and hence may not have messages
 * existing on the incoming side that might be sent to the Proxy.
 * <p>
 * @see ec.e.db.RtCodeable
 * @see ec.e.upgrade.InterfaceWrapper
 */
 
public abstract class ProxyWrapper extends InterfaceWrapper implements RtDelegatingEncodeable
{       
    /**
     * Constructs a ProxyWrapper.
     * <p>
     * @param target The Target Proxy being wrapped. Messages sent
     * to the ProxyWrapper are forwarded to the target after any possible
     * conversion to a message and args the target understands.
     */     
    public ProxyWrapper (Object target) {
        if (target instanceof EProxy) { 
            init((RtTether)target);
        }
        else {
            throw new 
            InterfaceConversionException("ProxyWrapper constructed with non Proxy target: "
                + target);
        }
    }    

    /**
     * RtDelegatingEncodeable method to encode a different Object
     * in this object's place. Encodes the proxy we refer to as
     * that is ultimately what we are masquerading as.
     */ 
    public Object delegateToEncode() {
        return target;
    }   

    /**
     * Encode me (actually, I'll encode my target!)
     */ 
    public boolean encodeMeForDeflector() {
        return true;
    }       
}
