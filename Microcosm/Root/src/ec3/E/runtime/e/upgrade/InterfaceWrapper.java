package ec.e.upgrade;

/**
 * InterfaceWrapper is an abstract class that wraps another Object for
 * purposes of method conversion for messages that aren't implemented
 * by the target object. Although this documentation refers to 
 * subclasses of InterfaceWrapper, this class should not be directly
 * subclassed. Concrete implementations must subclass one of ObjectWrapper
 * of ProxyWrapper, both of which subclass InterfaceWrapper (both of which
 * are abstract). 
 * <p>
 * An InterfaceWrapper wraps either an incoming Proxy or an outgoing
 * Object referenced via the comm system. For exported Objects to another
 * process, the Wrapper side's class is newer than the importing side, and 
 * hence incoming messages from the importing side might be for messages that
 * no longer exist or are no longer implemented by the new Class. For
 * imported Proxies from another process the other process has an older
 * version of the Object's class than the wrapping side, hence messages being 
 * sent to the Proxy might not be implemented on the receiving side. In both
 * cases, the interface wrapper converts the Sealer and Args to something
 * the target understands, and forwards on the new message.
 * <p>
 * InterfaceWrapper implements the methods invoke() and invokeNow().
 * Subclasses of InterfaceWrappers must implement the methods
 * sealerForSealer() and argsForSealer() in order to properly convert
 * the Sealer and Args for an incoming message into a message the wrapped
 * Object understands.
 * <p>
 * Invocations are handled by forwarding to the target Object. If the message
 * is no longer implemented by the target Object, the Sealer is converted
 * to a Sealer the Object recognizes. Optionally, the args are converted
 * to a new array of args to go along with the message. After this conversion,
 * the invocation is forwarded on to the target.
 * <p>
 * If the subclass of InterfaceWrapper cannot properly convert a message
 * into one that makes sense, it should throw a MessageConversionException.
 * This can be done in either the sealerForSealer or argsForSealer method.
 * <p>
 * @see ec.e.run.RtTether
 * @see ec.e.run.UnknownSealer
 * @see ec.e.upgrade.ObjectWrapper
 * @see ec.e.upgrade.ProxyWrapper
 * @see ec.e.upgrade.UpgradeTable
 */
public abstract class InterfaceWrapper implements RtTether
{
    RtTether target;
    
    /**
     * Constructs an InterfaceWrapper.
     * <p>
     * @param target The target Object being wrapped. Messages sent
     * to the InterfaceWrapper are forwarded to the target after any possible
     * conversion to a message and args the target understands.
     */     
    /* package */ InterfaceWrapper () {
    }
    
    protected final void init (RtTether target) {
        if (target == null) {
            throw new RtRuntimeException("InterfaceWrapper initialized with null target");
        }
        this.target = target;
    }
    
    /**
     * Forwards invocation to target, after any possible conversion to a message
     * and args the target understands.
     *
     * @param sealer The Sealer for the message being sent
     * @param args The args sent in the message
     * @param ee The Exception Environment for the message
     */ 
    public final void invoke (RtSealer sealer, Object[] args, RtExceptionEnv ee) {
        invoke(sealer, args, ee, false);
    }
    
    /**
     * Forwards invocation to target, after any possible conversion to a message
     * and args the target understands.
     *
     * @param sealer The Sealer for the message being sent
     * @param args The args sent in the message
     * @param ee The Exception Environment for the message
     */ 
    public final void invokeNow (RtSealer sealer, Object[] args, RtExceptionEnv ee) {
        invoke(sealer, args, ee, true);
    }
    
    /**
     * Abstract method subclasses must implement to convert the Sealer, Args, and
     * Exception Environment for an incoming message into an appropriate Envelope
     * to send on to the target.
     * <p>
     * @param oldSealer The Sealer for the message.
     * @param args The array of Args passed in to the message
     * @param ee The Exception Environment in the original envelope
     * @return An RtEnvelope to forward to the target.
     */ 
    public abstract MessageWrapper createMessageWrapper (RtSealer oldSealer, Object args[], RtExceptionEnv ee);  
      
    /**
     * If perchance a Deflector targets me, encode the Deflector
     */ 
    public boolean encodeMeForDeflector() {
        return false;
    }   
      
    /**
     * Private implementation of invoke/invokeNow to use same code path for common
     * code. Asks subclass (via abstract methods) for the proper RtSealer and args array,
     * and then forwards the message.
     * <p>
     * @param sealer The original Sealer sent in the message.
     * @param args The array of arguments originally sent in the message.
     * @param ee The exception environment sent in the message.
     * @param now Indicates whether or not the message should be forwarded
     * via invokeNow (true) or invoke (false).
     *
     */  
    private void invoke (RtSealer oldSealer, Object[] args, RtExceptionEnv ee, boolean now) {
        MessageWrapper wrapper = createMessageWrapper(oldSealer, args, ee);
        if (now) {
            target.invokeNow(wrapper.sealer, wrapper.args, wrapper.ee);
        }
        else {
            target.invoke(wrapper.sealer, wrapper.args, wrapper.ee);
        }
    }   
}   
