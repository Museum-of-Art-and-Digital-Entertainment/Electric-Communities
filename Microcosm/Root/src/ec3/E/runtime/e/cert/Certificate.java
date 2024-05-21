package ec.cert;

import java.io.Serializable;

/**
   Abstract representation for a signature on a Certifiable.  Opaque
   to the user.  This class doesn't actually do anything; it's just a
   structure for holding the signature data for access by other
   members of the package.
*/
final public class Certificate implements Serializable {
  private byte[] mySignedBlob;      // The meat, baby!
  private CryptoHash myVerifierID;  // Public key of Cert Agency
  private long myExpirationDate;    // Exiration Data in seconds since 
                                    // midnight GMT 1/1/70

  Certificate(byte[] signedBlob, CryptoHash verifierID) { 
    this (signedBlob, verifierID, Long.MAX_VALUE);
  }                             

  Certificate(byte[] signedBlob, CryptoHash verifierID, long expirationData) { 
    mySignedBlob = signedBlob;
    myVerifierID = verifierID;
    myExpirationDate = expirationData;
  }

  /**
   * returns a verifier for the certificate
   */
  public CryptoHash getVerifierID() {
    return myVerifierID;
  }

  /**
   * returns the signing data for the certificate.
   */
  public byte[] getSignedBlob() {
    return mySignedBlob;
  }

  public long getExpirationDate() {
    return myExpirationDate;
  }

}

