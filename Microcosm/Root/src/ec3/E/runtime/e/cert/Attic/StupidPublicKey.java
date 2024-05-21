package ec.cert;

class StupidPublicKey 
implements java.security.PublicKey {

  byte[] myBytes = new byte[1];

  public StupidPublicKey(byte aByte) {
    myBytes[0] = aByte;
  }

  public byte[] getEncoded() {
    return myBytes;
  }

  public String getFormat() { return null; }
  public String getAlgorithm() { return "Stupid"; }
}
