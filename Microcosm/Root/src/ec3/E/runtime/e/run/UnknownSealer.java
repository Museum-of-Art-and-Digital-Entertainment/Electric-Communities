package ec.e.run;

import java.io.IOException;
import java.util.Hashtable;

/**
 * UnknownSealers are "fake" sealers that exist only to allow nonexistent
 * messages to be sent to Objects. Unknown Sealers are used to maintain
 * coexistence between two different versions of a program communicating
 * over the wire.
 * <p>
 * If an Object is imported from an older version, some messages sent to it may
 * no longer exist. When this occurs, the new side has to convert between
 * an outgoing message call and one that exists on the old side. In this case,
 * the Proxy representing the imported Object is wrapped in a ProxyWrapper.
 * The ProxyWrapper uses an interned UnknownSealer to send the message (as
 * there is no sealer on the sending side to represent the actual message) to
 * the actual Proxy, which will blindly send the message over the wire.
 * <p>
 * The UnknownSealer will encode itself such that the correct RtSealer object
 * is decoded on the other side, and the message will be sent without incident
 * (assuming there are no other errors) to the old version of the exported Object.
 * <p>
 * Conversely, when a newer version of an Object is exported, some of the messages
 * the other side sends may no longer be implemented. Upon receipt of a message
 * that is no longer implemented, the RtSealer decoding will not find an actual
 * RtSealer object. In this case, decode will return an interned UnknownSealer
 * if one has been declared. An Object exported to an older version is wrapped
 * in an ObjectWrapper, and it is the ObjectWrapper that is actually exported,
 * and hence receives incoming messages. If the message contains an UnknownSealer,
 * the ObjectWrapper will convert the sealer and args to an approriate message
 * supported by the new version of the Object. 
 * <p>
 * @see ec.e.run.RtSealer
 * @see ec.e.upgrade.ObjectWrapper
 * @see ec.e.upgrade.ProxyWrapper
 */
public class UnknownSealer extends RtSealer
{
    static final Hashtable internTable = new Hashtable(100);
    // XXX - Boogery knowledge of how Compiler names things
    static final String SealerSuffix = "_$_Sealer";
    
    /** 
     * Name of the Class this Sealer represents without suffix, 
     * i.e. EObject, not EObject_$_Blah
     */
    private String myClassName;

    /**
     * Returns already existing Sealer for class and name, else makes
     * one and sticks it into a unique table so it is returned next
     * time this method is called with the same arguments.
     * @param className The name of the Class this Sealer is for.
     * @param name The String to represent the method and args for the sealer.
     * This is a human readable name 
     * @return The uniquely known already existing or newly
     * created UnknownSealer.
     * @see ec.e.run.RtSealer#humanToSignature
     */ 
    public static UnknownSealer internHuman (String className, String name) {
        String inhumanName = RtSealer.humanToSignature(name);
        return intern(className, inhumanName);
    }
        
    /**
     * Returns already existing Sealer for class and name, else makes
     * one and sticks it into a unique table so it is returned next
     * time this method is called with the same arguments.
     * @param className The name of the Class this Sealer is for.
     * @param name The String to represent the method and args for the sealer.
     * @return The uniquely known already existing or newly
     * created UnknownSealer.
     */ 
    public static UnknownSealer intern (String className, String name) {
        String key = className + name;
        UnknownSealer sealer = (UnknownSealer)internTable.get(key);
        if (sealer == null) {
            sealer = new UnknownSealer(className, name);
            internTable.put(key, sealer);
        }
        return sealer;
    }       
    
    /**
     * Gets uniquely known Sealer for class with name. If it
     * isn't known, returns null.
     *
     * @param className The name of the Class this Sealer is for.
     * @param name The String to represent the method and args for the sealer.
     * @returns The uniquely known already existing UnknownSealer or null if not
     * found.
     */ 
    /* package */ static UnknownSealer get (String className, String name) {
        return (UnknownSealer)internTable.get(className + name);    
    }   
    
    /**
     * Equals comparison.
     */
    public boolean equals(Object other) {
        if ((other == null) || !(other instanceof UnknownSealer)) {
            return false;
        }
        UnknownSealer o = (UnknownSealer) other;
        return (myClassName.equals(o.myClassName)) && (my_$_Name.equals(o.my_$_Name));
    }
    
    /**
     * RtEncodeable method returning type of Object to
     * encode. Claims to be the specific RtSealer subclass
     * the UnknownSealer represents.
     */ 
    public String classNameToEncode (RtEncoder encoder) {
        return myClassName + SealerSuffix;
    }   
    
    /** 
     * RtEncodeable method to encode the UnknownSealer.
     * Note that when an UnknownSealer is encoded, the proper subclass of
     * RtSealer is decoded on the other side.
     */
    public void encode (RtEncoder encoder) {
        try {
            encoder.writeUTF(my_$_Name);
        } catch (Exception e) {
            RtRun.tr.errorReportException(e, "Encoding UnknownSealer");
        }
    }   
    
    /** 
     * RtDecodeable method to decode the UnknownSealer. Since an
     * UnknownSealer encodes itself as a different class, this
     * method should never be called, as an UnknownSealer should
     * never be decoded.
     */
    public Object decode (RtDecoder decoder) {
        throw new RtRuntimeException("UnknownSealer decode should never get called");
    }
    
    /**
     * Invoke method used by the E runtime. Should never be called
     * on an UnknownSealer, as an UnknownSealer is just used as a
     * placeholder for nonexistent methods.
     */ 
    public void invoke(Object target, Object[] args) throws Exception {
        throw new RtRuntimeException("UnknownSealer invoke should never get called");
    }   
    
    /**
     * Private constructor used from static call to intern
     */
    private UnknownSealer (String className, String name) {
        super(0, name);
        myClassName = className;
    }

}   

