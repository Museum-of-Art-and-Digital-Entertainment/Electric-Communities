package ec.e.upgrade;

/**
 * InterfaceConverter is an UpgradeConverter interface that wrappers
 * for Objects that are Proxied over the wire by reference must implement.
 * When an Object is about to be Proxied and an InterfaceConverter is 
 * present for it, the InterfaceWrapper is asked to return an Object
 * wrapping the outgoing Object. This wrapper Object is what is actually
 * proxied over the wire.
 *
 * Conversely, when a Proxy is received over the wire and its class
 * maps to an InterfaceConverter, the InterfaceConverter is asked
 * to wrap the incoming Object in another Object that can be returned
 * from decoding.
 *
 * @see ec.e.upgrade.UpgradeConverter
 */
public interface InterfaceConverter extends UpgradeConverter
{
    /**
     * Return an Object wrapping new version of the Object to export over the wire.
     */
    Object wrapOut (Object object);
    
    /**
     * Return an Object to wrap a prior version of the
     * Object with.
     */ 
    Object wrapIn (Object object);
}
        
