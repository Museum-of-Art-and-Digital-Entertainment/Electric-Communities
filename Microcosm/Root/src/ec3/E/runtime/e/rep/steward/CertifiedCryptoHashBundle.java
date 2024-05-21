package ec.e.rep.steward;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import ec.e.rep.steward.Repository;
import java.io.IOException;
import ec.cert.*;

public final class CertifiedCryptoHashBundle{
  private Vector myDataCryptoHashes = null;     // this is what is certified
  private Hashtable myBundlesCertificates = new Hashtable(); // keyed my Verifier
  
  public CertifiedCryptoHashBundle(Vector dataCryptoHashes) {
    myDataCryptoHashes = dataCryptoHashes;
  }

  public void addCertificate(Certificate certificate) {
    myBundlesCertificates.put(certificate.getVerifierID(), certificate);
  }

  public Enumeration getDataCryptoHashes() {
    return myDataCryptoHashes.elements();
  }

  public Hashtable getCertificateTable() {
    return myBundlesCertificates;
  }

  public CryptoHash getBundleCryptoHash() throws IOException {
    return Repository.computeCryptoHash(myDataCryptoHashes);
  }

  public boolean claimsToBeCertifiedBy(CryptoHash verifierID) {
    Object certificate =  
      (Object)myBundlesCertificates.get(verifierID);
    boolean hasID = (certificate != null);
    return hasID;
  }

  public byte checkBundle(Verifier[] verifiersToUse) throws IOException {

    if (verifiersToUse == null)  return Verifier.FAIL;
    if (verifiersToUse.length < 1) return Verifier.FAIL;

    // Get a certificate
    Certificate certificate = null;

    //Gets the bundles cryptohash. May throw IOException.
    CryptoHash bundleCryptoHash = Repository.computeCryptoHash(myDataCryptoHashes);
    
    for (int i=0; i < verifiersToUse.length; i++) {
      certificate =  
        (Certificate)myBundlesCertificates.get(verifiersToUse[i].getCryptoHash());
      if (certificate != null) {
        byte vResult = verifiersToUse[i].verify(bundleCryptoHash.getCopyOfHashBytes(), certificate);
        switch (vResult) {
          case Verifier.PASS:
            return Verifier.PASS;
          case Verifier.EXPIRED:
            // Do something here?
            break;
          default:;
        }
      }
    }
    return Verifier.FAIL;

  }
}


  
