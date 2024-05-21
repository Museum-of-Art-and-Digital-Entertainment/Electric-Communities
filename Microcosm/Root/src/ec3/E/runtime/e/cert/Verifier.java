package ec.cert;

import java.security.Signature;
import java.security.PublicKey;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import ec.e.net.SturdyRef;

import java.io.Serializable;
/**
 * Abstract representation of a public key.
 * sigType should arguably be accessible from PublicKey, but Java1.1 doesn't 
 * put it there.
 */
// needs to decode uniquely so (coder.getVerifier() == myVerifier)
//  in Certificate.decode works properly.  Hence comment about interning above.
//  Note:  the comm system will have to provide this facility.
public final class Verifier 
implements Serializable
{

  public static final byte PASS = 4;  // Result types
  public static final byte FAIL = 1;
  public static final byte EXPIRED = 2;
  public static final byte REVOKED = 3;

  private SturdyRef myServerSturdyRef;  // For when we need service
  private PublicKey myPubKey;  // Should we be using java.security.Identity?
  private String mySigType;
  private VerifierDescription myDescription;
  /**
   * Create a new Verifier to represent a PublicKey that uses a
   * particular signature type.
   * @param pubKey the public key.
   * @param sigType the name of a signature algorithm that can
   * accept this public key as input (e.g. "DSA")
   * @param name A string identity to give to the Verifier.
   */
  public Verifier(PublicKey pubKey, 
                  String sigType, 
                  VerifierDescription description) {
      myPubKey = pubKey;
      mySigType = sigType;
      myDescription = description;
      // should place itself in the interning table.
    }

  /**
   * Create a new Verifier to represent a PublicKey that uses a
   * particular signature type.
   * @param pubKey the public key.
   * @param name A string identity to give to the Verifier.
   */
  public Verifier(PublicKey pubKey, VerifierDescription description) {
     this(pubKey, pubKey.getAlgorithm(), description);
    }

  public void setServerRef(SturdyRef serverRef) {
    //    myServerSturdyRef  = serverRef;
  }

  /**
   * Returns true if the public keys match byte for byte.
   */
  public boolean equals(Object o) {
    if (!(o instanceof Verifier)) return false;
    Verifier aVerifier = (Verifier)o;
    PublicKey keyToCheck = aVerifier.myPubKey;    
    byte[] checkBytes =  keyToCheck.getEncoded();
    byte[] myBytes =  myPubKey.getEncoded();
    if (checkBytes.length != myBytes.length) return false;
    for (int i=0; i < checkBytes.length; i++) {
      if (checkBytes[i] != myBytes[i]) return false;
    }
    return true;
  }

  /**
   * Return the name given to the certificate.
   */
//   public String getStringID() {
//     return myStringID;
//   }

  /**
   * Returns a hash code for the verifier.
   */
  public int hashCode() {
    CryptoHash ch = new CryptoHash(myPubKey.getEncoded());
    return ch.hashCode();
  }


  /**
   * Returns the cryptohash for the verifiers public key.
   */
  public CryptoHash getCryptoHash() {
    CryptoHash ch = new CryptoHash(myPubKey.getEncoded());
    return ch;
  }


  /**
   * Every Verifier has a "server" that is made when the Verification
   * type is created and every verifier caries with it a SturdyRef
   * to that server. This is a accessor method to that reference.
   */
  public SturdyRef getReferenceToServer() {
    return myServerSturdyRef;
  }


  /**
   * createChannelToServer returns a channel to the server for this Verifier 
   * type.
   */
  public EVerifierServer createChannelToServer() {
    EVerifierServer anEChannelToServer = (EVerifierServer) EUniChannel.construct(EVerifierServer.class);
    EUniDistributor anEChannelToServer_dist = EUniChannel.getDistributor(anEChannelToServer);
    myServerSturdyRef.followRef(anEChannelToServer_dist);
    return anEChannelToServer;
  }

  public VerifierDescription getDescription() {
    return myDescription;
  }


  /**
   * Verifies that a signature is valid for a blob.
   * @param blobToCheck The blob in question. 
   * @param signedBlob The bytes that make up the signed blob 
   *                   (i.e. a privately signed crypto-hash)
   */
  public byte verify(byte[] blobToCheck, Certificate certificate) {
    try {
      byte[] signedBlob = certificate.getSignedBlob();
      long expirationDate = certificate.getExpirationDate();

      // First do an expiration check.
      int tzOffset = java.util.TimeZone.getDefault().getOffset(0,0,0,0,0,0);
      long currentDate = System.currentTimeMillis();
      if (expirationDate < (currentDate-tzOffset)) { 
        // The Cert has expired. This date can be hacked inside the certificate,
        // but then the verify step bellow will fail.
        /* To be uncommented when server is up and running
        EVerifierServer channelToServer = createChannelToServer();
        EBoolean result;
        channelToServer <- reCertiry(certificate, result);
        ewhen result (Boolean realResult) {
        // Return PASS or REVOKED depending upon result
        }
        */
        return EXPIRED;
      }

      //Instantiate a specific signature object
      Signature sig = Signature.getInstance(mySigType);
      sig.initVerify(myPubKey);
      sig.update(blobToCheck);
      //Append the expiration date to the block
      byte dateByte;
      for (int s = 0; s<64; s += 8) {
        dateByte = (byte)((expirationDate >>> s) & 0x00000000000000FF);
        sig.update(dateByte);
      }
      if (sig.verify(signedBlob)) {
        return PASS;
      }
      return FAIL;
    } catch (NoSuchAlgorithmException nsae) {
      System.out.println("Verifier: No Such Algorithm");
    } catch (InvalidKeyException ike) {
      System.out.println("Verifier: Invalid Key");
    } catch (SignatureException se) {
      System.out.println("Verifier: Signature Exception");
    }
    return FAIL;
  }

  public String toString () {
    String s = myDescription.getName() + "(" + 
      getCryptoHash() + ", "+ mySigType + ")";
    return s;
  }
}


