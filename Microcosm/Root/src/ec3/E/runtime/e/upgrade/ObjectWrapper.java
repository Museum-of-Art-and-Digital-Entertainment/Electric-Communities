package ec.e.upgrade;

import ec.e.run.Exportable;

/**
 * An ObjectWrapper masquerades as an Object it wraps in order to convert
 * incoming messages which are no longer implemented by the target into
 * sensible messages the target implements. An ObjectWrapper is used
 * to wrap and vend an object over the wire to another process which has
 * an older version of the Object's class, and hence might send it messages
 * which it no longer implements.
 * <p>
 * The method getIdentity() returns the identity of the wrapped (target) Object,
 * since ObjectWrapper is masquerading as that Object for purposes of 
 * exporting it over the wire.
 * <p>
 * When an incoming message is no longer implemented, the Object wrapper converts
 * it to an existing message with a
 * different Sealer. This can happen both in the case where the Sealer is not known
 * by this side of the connection (and hence is converted into an UnknownSealer on 
 * decode), or the Sealer is still known, but is of an Interface or Superclass that
 * the target class no longer implements/extends.
 * <p>
 * @see ec.e.run.Exportable
 * @see ec.e.run.UnknownSealer
 * @see ec.e.upgrade.InterfaceWrapper
 */
public abstract class ObjectWrapper extends InterfaceWrapper implements Exportable
{
    /**
     * Constructs an ObjectWrapper.
     * <p>
     * @param target The Target Exportable Object being wrapped.
     */     
    public ObjectWrapper (Object target) {
        if (target instanceof Exportable) { 
            init((RtTether)target);
        }
        else {
            throw new 
            InterfaceConversionException("ObjectWrapper constructed with non Exportable target:"
                + target);
        }
    }

    /**
     * Method to get unique exportable identity for this Object.
     * <p>
     * @return The target Object's identity, as we are masquerading as
     * that Object for purposes of export.
     */ 
    public final long getIdentity() {
        return ((Exportable)target).getIdentity();
    }
}    
