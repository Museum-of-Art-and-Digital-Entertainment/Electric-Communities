package ec.cert;

import java.util.Vector;


public class CertRequesterData {
  public String myEmailAddress;        // Just in case
  public String myDescription;
  public String myComment;

  public void setEmailAddress(String emailAddress) {
    myEmailAddress = emailAddress;
  }

  public String getEmailAddress() {
    return myEmailAddress;
  }

  public void setDescription(String description) {
    myDescription = description;
  }

  public String getDescription() {
    return myDescription;
  }

  public void setComment(String comment) {
    myComment = comment;
  }

  public String getComment() {
    return myComment;
  }

}


