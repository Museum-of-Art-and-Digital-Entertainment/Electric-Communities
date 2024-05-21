package ec.cert;

import java.io.Serializable;

public class VerifierDescription 
implements Serializable {  // I don't know what's going to be in here yet.
  public byte[] myLogo;  // Gif or jpg memory image
  public String myName ="";
  public String myVersion = "";
  public String myOrganization = "";
  public String myClaim = "";
  public boolean myAcceptsRequests = true;

  public void setName(String theName) {
    myName = theName;
  }

  public String getName() {
    return myName;
  }

  public void setOrganization(String theOrganization) {
    myOrganization = theOrganization;
  }

  public String getOrganization() {
    return myOrganization;
  }

  public void setVersion(String theVersion) {
    myVersion = theVersion;
  }

  public String getVersion() {
    return myVersion;
  }

  public void setClaim(String theClaim) {
    myClaim = theClaim;
  }

  public String getClaim() {
    return myClaim;
  }

  public void setLogo(byte[] theLogo) {
    myLogo = theLogo;
  }

  public byte[] getLogo() {
    return myLogo;
  }

  public void setAcceptsRequests(boolean value) {
    myAcceptsRequests = value;
  }

  public boolean acceptsRequests() {
    return myAcceptsRequests;
  }

  public String getStringID() {
    String id=myName;
    if ((myVersion != null) &&
        (myVersion.length() > 0)) {
      id += "-"+myVersion;
    }
    return id;
  }

  public int size() {
    int accum = 0;
    if (myName != null) accum += myName.length();
    if (myVersion != null) accum += myVersion.length();
    if (myOrganization != null) accum += myOrganization.length();
    if (myClaim != null) accum += myClaim.length();
    if (myLogo != null) accum += myLogo.length;
    return accum;
  }
}


