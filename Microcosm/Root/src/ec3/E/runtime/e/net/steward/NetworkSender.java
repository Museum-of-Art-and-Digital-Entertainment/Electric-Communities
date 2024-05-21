package ec.e.net.steward;

import java.io.OutputStream;
import java.io.IOException;

public abstract class NetworkSender extends OutputStream {
    public NetworkSender() {
    }

    // XXX this should probably be negotiated at connection setup time
    static final public int MAX_PACKET_LENGTH = 1024 * 1024 ;

    public abstract void enable() throws IOException ;

/**
 *  Change to a new protocol on the link.  The next message sent
 *  will be treated according to the new parameters specified.  This method
 *  is used after successful protocol negotitation.
 *
 *  <p>N.B. This method must only be called while processing a message passed
 *  to the VAT by the receive thread associated with this NetworkSender.
 *
 * @param   isAggragating whether messages are to be combined for compression
 *          encryption etc.
 *
 * @param   macType is the type of MessageAuthenticationCode algorthm to use.  
 *          The only supported values currently are "SHA1" and "None".
 *
 * @param   macKey the key for the MessageAuthenticationCode algorthm selected.  
 *          The length depends on the algorthm.  For SHA1 it is 64.
 *
 * @param   encryptionType is the type of encryption algorthm to use.  The
 *          only supported values currently are "3DES" and "None".
 *
 * @param   encryptionKey the key for the encryption algorthm selected.  The
 *          length depends on the algorthm.  For 3DES it is 24.
 *
 * @param   outIV the initialization vector for outbound messages.  Not used 
 *          if encryptionType is "None".
 *
 * @param   inIV the initialization vector for inbound messages.  Not used 
 *          if encryptionType is "None".
 *
 * @param   isCompressing true if ZIP compression is to be used for the messages.
 *
 * @param   useSmallZip true is ZIP checksums are not to be generated.
 */
    public abstract void changeProtocol(
                boolean isAggragating,
                String macType,
                byte[] macKey, 
                String encryptionType,
                byte[] encryptionKey,
                byte[] outIV, 
                byte[] inIV,
                boolean isCompressing,
                boolean useSmallZip) throws IOException;
}
