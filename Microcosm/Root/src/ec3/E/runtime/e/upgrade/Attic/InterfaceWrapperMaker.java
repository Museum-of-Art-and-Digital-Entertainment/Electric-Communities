package ec.e.upgrade;

/**
 * InterfaceWrapperMaker is an UpgradeWrapperMaker interface that wrappers
 * for Objects that are Proxied over the wire by reference must implement.
 * When an Object is about to be Proxied and an InterfaceWrapperMaker is 
 * present for it, the InterfaceWrapper is asked to return an Object
 * wrapping the outgoing Object. This wrapper Object is what is actually
 * proxied over the wire.
 *
 * Conversely, when a Proxy is received over the wire and its class
 * maps to an InterfaceWrapperMaker, the InterfaceWrapperMaker is asked
 * to wrap the incoming Object in another Object that can be returned
 * from decoding.
 *
 * @see ec.e.upgrade.UpgradeWrapperMaker
 */
public interface InterfaceWrapperMaker extends UpgradeWrapperMaker
{
    /**
     * Return an Object to encode over the wire.
     */
    Object wrapOut (Object object);
    
    /**
     * Return an Object to wrap a prior version of the
     * Object with.
     */ 
    Object wrapIn (Object object);
}
        
