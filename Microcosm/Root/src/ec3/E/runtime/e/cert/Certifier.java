package ec.cert;


import java.security.KeyPair;
import java.security.Signature;
import java.security.PrivateKey;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.SignatureException;

/**
 * Class to certify objects. 
 * SECURITY NOTE:  Objects of this class MUST be tightly held.
 * Exposing a capability to an object of this class exposes the
 * PRIVATE key.
 */
public final class Certifier {
    private KeyPair myKeyPair;
    private String mySigType;
    private CryptoHash myCertKindID;
    private EVerifierServer myCertKindServer;

    /**
     * Create a new Certifier to represent a KeyPair that uses a
     * particular signature type.
     * @param server the server for this certificate type.
     * @param pair the public/private key pair.
     * @param sigType the name of a signature algorithm that can
     * accept this key pair as input (e.g. "DSA")
     */
    Certifier(EVerifierServer server, 
              KeyPair pair, 
              CryptoHash CertKindID,
              String sigType) {
        myKeyPair = pair;
        mySigType = sigType;
        myCertKindID = CertKindID;
        myCertKindServer = server;
    }


    /**
     * Create a new Certifier to represent a KeyPair that uses a
     * particular signature type. The Signature algorithm is assumed
     * the same as the key algorithm.
     * @param pair the public/private key pair.
     */
    Certifier(EVerifierServer server, 
              KeyPair pair, 
              CryptoHash CertKindID) {
        this(server, pair, CertKindID, pair.getPublic().getAlgorithm());
    }


    /**
     * @return the CryptoHash ID (digest) of the certificate type that 
     * this certifier produces.
     */
    public CryptoHash getCertKindID() {
      return myCertKindID;
    }

    /**
     * Produce a certificate for a frozen representation of a
     * serialized object graph with no expiration date.
     *
     * @param bolb the object grarph data as a byte array. This can be
     * a serialized object or pure data.
     *
     * @return a certificate.
     */
    public Certificate certify(byte[] blob) {
        return certify(blob, Long.MAX_VALUE);
    }

    /**
     * Sign a frozen representation of a serialized object graph.
     * 
     * @param blob the thing to be certified.
     * @param expirationDate when the certificate should expire
     * @param renewalPeriod How long to auto-renew for upon expiration
     */
    public Certificate certify(byte[] blob, 
                               long expirationDate) {
      try {
        Signature sig = Signature.getInstance(mySigType);
        PrivateKey pKey = myKeyPair.getPrivate();
        sig.initSign(pKey);
        sig.update(blob);   
        //Append the expiration date to the block
        byte dateByte;
        for (int s = 0; s<64; s += 8) {
          dateByte = (byte)((expirationDate >>> s) & 0x00000000000000ff);
          sig.update(dateByte);
        }
        byte[] signedData = sig.sign();
        Certificate newCertificate = 
          new Certificate(signedData, myCertKindID, expirationDate);
        return newCertificate;
      } catch (NoSuchAlgorithmException nsae) {
      } catch (InvalidKeyException ike) {
      } catch (SignatureException se) {
      }
      return null;
    }



    /**
     * Add a certificate to its' server.
     *
     * @param dataDigest The finger print of the object to which the
     * certificate applies.
     * @param newCertificate The certificte for the objects.
     * @param renewalPeriod The length of time to auto renew a certificate
     * that has expired.
     */
    public void addCertToCertAgency(CryptoHash dataDigest,
                                    Certificate newCertificate,
                                    int renewalPeriod) {
        myCertKindServer <- addCertificate( dataDigest, 
                                            newCertificate, 
                                            renewalPeriod);
    }


    /**
     * Standard toString
     */
    public String toString () {
      String aString =  "Certifier ("+
        myKeyPair +  ", "+
        mySigType +  ", "+
        myCertKindID +  ", "+
        myCertKindServer +
        ")";
      return aString;
  }
}
