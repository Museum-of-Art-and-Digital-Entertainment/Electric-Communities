package ec.cert;

import java.security.KeyPair;

public class StupidKeyPairGenerator {

  public static KeyPair generateKeyPair() {
    double r = java.lang.Math.random();
    int i = ((int)(r*20)) % 20;
    byte[] pk = new byte[i];
    byte count = (byte)pk.length;
    StupidPublicKey aStupidPublicKey = new StupidPublicKey(count);
    StupidPrivateKey aStupidPrivateKey = new StupidPrivateKey(pk);
    KeyPair newKeyPair = new KeyPair(aStupidPublicKey, aStupidPrivateKey);
    return newKeyPair;
  }
}
