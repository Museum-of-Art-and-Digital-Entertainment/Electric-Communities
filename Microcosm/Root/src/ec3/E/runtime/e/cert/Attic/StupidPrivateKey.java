package ec.cert;

class StupidPrivateKey 
implements java.security.PrivateKey {

  byte[] myBytes = null;

  public StupidPrivateKey(byte[] someBytes) {
    myBytes = someBytes;
  }

  public byte[] getEncoded() {
    return myBytes;
  }

  public String getFormat() { return null; }
  public String getAlgorithm() { return "Stupid"; }
}
