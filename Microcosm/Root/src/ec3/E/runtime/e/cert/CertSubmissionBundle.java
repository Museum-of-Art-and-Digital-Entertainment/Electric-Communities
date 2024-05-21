package ec.cert;

public class CertSubmissionBundle {
  public CryptoHash myVerifierID;        // Just in case
  public CertRequesterData myRequesterData;  // Email address etc.
  public Object myThingToReview;         // Art etc.

  public void setVerifierID(CryptoHash verifierID) {
    myVerifierID = verifierID;
  }

  public CryptoHash getVerifierID() {
    return myVerifierID;
  }

  public void setRequesterData(CertRequesterData requesterData) {
    myRequesterData = requesterData;
  }

  public CertRequesterData getRequesterData() {
    return myRequesterData;
  }

  public void setThingToReview(Object thingToReview) {
    myThingToReview = thingToReview;
  }

  public Object getThingToReview() {
    return myThingToReview;
  }

}


