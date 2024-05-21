package ec.cert;

import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.PublicKey;
import java.security.PrivateKey;

class Signature {
  private byte[] myBytes; //Raw Data
  private PrivateKey myPrivateKey;
  private PublicKey myPublicKey;

  void Signature () {}

  static Signature getInstance(String signatureType)
    throws NoSuchAlgorithmException {
    return new Signature();
  }


  public final void initSign(PrivateKey key)
    throws InvalidKeyException {
      myPrivateKey = key;
  }

  public final void initVerify(PublicKey key) 
    throws InvalidKeyException {
      myPublicKey = key;
  }

  public final void update(byte[] b) 
    throws SignatureException {
      myBytes = b;
  }

  public final byte[] sign()
    throws SignatureException {
      byte[] keyBytes = myPrivateKey.getEncoded();
      byte[] newBytes = new byte[myBytes.length+keyBytes.length];
      System.arraycopy(keyBytes, 0, newBytes, 0, keyBytes.length);
      System.arraycopy(myBytes, 0, newBytes, keyBytes.length, myBytes.length);
      return newBytes;
  }

  public final boolean verify(byte[] signedData) 
    throws SignatureException {
      byte[] keyBytes = myPublicKey.getEncoded();
      if (myBytes.length != (signedData.length - keyBytes[0])) return false;
      for (int i=0; i < myBytes.length; i++) {
        if (signedData[i+keyBytes[0]] != myBytes[i]) return false;
      }
      return true;
  }
}
