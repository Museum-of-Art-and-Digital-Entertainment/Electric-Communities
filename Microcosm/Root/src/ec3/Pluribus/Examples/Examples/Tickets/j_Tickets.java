
/*
    Tickets.java

    Copyright 1996, Electric Communities, all rights reseved
    Propietary and confidential
*/

package ec.plexamples.Tickets;

import java.util.*;

public class TicketData {
  public long ticketNumber;
  public long eventID;
  public String description;
  public Date expiration;

  public TicketData (long tn, long ei, String desc, Date exp) {
    ticketNumber = tn;
    eventID = ei;
    description = desc;
    expiration = exp;
  }

  public boolean equals(TicketData td) {
    if ((ticketNumber == ticketNumber) &&
        (eventID == eventID) &&
        (description.equals(description)) &&
        (expiration.equals(expiration))) {
      return true;
    }
    return false;
  }
}

public eclass EJavaObjectFuture {
    Object myValue = null;
    public EJavaObjectFuture(Object aValue) {
        myValue = aValue;
    }
    Object value() {
        return myValue;
    }
}
