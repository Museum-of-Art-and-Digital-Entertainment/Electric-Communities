
package ec.examples.lm2000;

import java.util.*;
import java.io.*;
import ec.e.lang.*;

public eclass EClient  {
  lm2000 lm2k;

  EClient(lm2000 l) {
    lm2k = l;

    // I considered putting a few eWhenevers here with the same functionality
    // as the methods found below, but the result objects would have to
    // have different types to decode the message types.

    // I wanted to take advantage of the behind-the-scenes multicasting
    // available with distributors.  What's an elegant way to do this?

    // Arturo said:  "Don't use distributors for multicasting.  Instead,
    // Check out EMulticastVector." 

  }

  emethod processPing(EResult ed) {
    ed<-forward(new EInteger(0));
  }

  emethod processLunchGoers(Vector v /* of String */ ) {
    lm2k.LunchGoerList.clear();
    for (Enumeration e = v.elements(); e.hasMoreElements();) {
        String name = (String)e.nextElement();
        lm2k.LunchGoerList.addItem(name);
    }
    lm2k.LunchGoerList.repaint();
  }

  emethod processAllRestaurants(Vector v /* of Restaurant */ ) {
    lm2k.AllRestaurantList.clear();
    for (Enumeration e = v.elements(); e.hasMoreElements();) {
        Restaurant r = (Restaurant)e.nextElement();
        lm2k.AllRestaurantList.addItem(r.name);
    }
    lm2k.AllRestaurantList.repaint();
    for (Enumeration e = v.elements(); e.hasMoreElements();) {
      Restaurant r = (Restaurant)e.nextElement();
      boolean found = false;
      for (Enumeration en = lm2k.person.tastes.elements(); 
        en.hasMoreElements();) {
        Rating rating = (Rating)en.nextElement();
        if (rating.restaurant.name.equals(r.name)) {
            found = true;
        }
      }
      if (!found) {
        Rating rating = new Rating(r, 5); // new:  medium score
        lm2k.person.tastes.addElement(rating);
        // Do a dialog:  "NEW RESTAURANT: name" XXX
      }
    }
    // alternatively, we need to delete any restaurants which are part of 
    // person.tastes vector and are no longer on the list XXX
  }

  emethod processBestRestaurants(Vector v /* of Restaurant */ ) {
    lm2k.BestRestaurantList.clear();
    for (Enumeration e = v.elements(); e.hasMoreElements();) {
        Restaurant r = (Restaurant)e.nextElement();
        lm2k.BestRestaurantList.addItem(r.name);
    }
    lm2k.BestRestaurantList.repaint();
  }

  emethod processShutdownClient(String reason) {
    lm2k.shutdown(reason);
  }

}
