
package ec.examples.lm2000;

import ec.e.db.*;              // for encode() and decode()
import ec.e.comm.*;            // for encode() and decode()

import java.util.*;
import java.io.*;

public class Common {

  public static byte[] encode(Object o) {
    TypeTable encodeTT = new TypeTable();
    RtStandardEncoder encoder = new RtStandardEncoder(null, encodeTT, true);
    encoder.encodeObject(o);
    return encoder.getBytes();
  }

  public static Object decode(byte[] encoded_object) {
    TypeTable decodeTT = new TypeTable();
    RtStandardDecoder decoder = 
      new RtStandardDecoder(null, decodeTT, encoded_object);
    return decoder.decodeRootObject();
  }

  public static Vector /* of Restaurant */ loadRestaurants() {
    Vector restaurants = new Vector();
    try {
      String outFile = System.getProperty("user.home", "") +
        File.separator + "Restaurants";
      FileInputStream fis = new FileInputStream(outFile);
      byte[] b = new byte[fis.available()];
      fis.read(b);
      restaurants = (Vector)decode(b);
      fis.close();
    } catch (IOException e) { // no restaurant info yet
    }
    return restaurants;
  }

  public static void savePerson(Person person) {
    String outFile = System.getProperty("user.home", "") +
      File.separator + "Person";
    try {
      FileOutputStream fos = new FileOutputStream(outFile);
      byte[] b = encode(person);
      fos.write(b);
      fos.close();
    } catch (IOException e) {
      System.err.println("Can't write to output file.  Breaking...");
      System.exit(1);
    } catch (Exception e) {
      System.err.println("Exception thrown in Client.configure():");
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static Person loadPerson() {
    Person person = new Person("Your Name", new Vector(), 120, 10);
    try {
      String outFile = System.getProperty("user.home", "") +
        File.separator + "Person";
      FileInputStream fis = new FileInputStream(outFile);
      byte[] b = new byte[fis.available()];
      fis.read(b);
      person = (Person)decode(b);
      fis.close();
    } catch (IOException e) { // no personal info
      // call personal info input screen XXX
    } catch (Exception e) {
      System.err.println("Exception Thrown:");
      e.printStackTrace();
    }
    return person;
  }

  public static void saveRestaurants(Vector restaurants /* of Restaurant */ ) {
    String outFile = System.getProperty("user.home", "") +
      File.separator + "Restaurants";
    try {
      FileOutputStream fos = new FileOutputStream(outFile);
      byte[] b = encode(restaurants); 
      fos.write(b);
      fos.close();
    } catch (IOException e) {
      System.exit(1);
    }
  }

  public static Vector /* of Restaurant */ bestFit(
     Vector /* of ClientInfo */ clients) {

    // get restaurant ratings, time, and dollar budgets from
    // each client,
    // Sum restaurant scores (by restaurant name) in Hashtable,
    // sort resulting hashtable by score, excluding those
    // restaurants which are over budget
    // return this list of restaurants as a Vector

    Vector persons = new Vector();
    Hashtable restaurant_scores = new Hashtable();
    int min_minutes = 9999;
    double min_budget = 999.99;
    for (Enumeration en = clients.elements();  en.hasMoreElements();) {
        ClientInfo ci = (ClientInfo)en.nextElement();
        Person p = ci.person;
        persons.addElement(p);
        if (p.minutes < min_minutes) {
            min_minutes = p.minutes;
        }
        if (p.budget < min_budget) {
            min_budget = p.budget;
        }
        for (Enumeration e = p.tastes.elements();  e.hasMoreElements();) {
            Rating r = (Rating)e.nextElement();
            Rating rs = (Rating) restaurant_scores.get(r.restaurant.name);
            if (rs == null) {
                restaurant_scores.put(r.restaurant.name, r);
            } else {
                rs.score += r.score;
            }
        }
    }

    int i = 0;
    for (Enumeration en = restaurant_scores.elements(); en.hasMoreElements();) {
        Rating r = (Rating)en.nextElement();
        if (r.restaurant.cost <= min_budget && 
          r.restaurant.minutes <= min_minutes) {
            i++;
        }
     }
    Rating ra[] = new Rating[i];
    i = 0;
    for (Enumeration en = restaurant_scores.elements(); en.hasMoreElements();) {
        Rating r = (Rating)en.nextElement();
        if (r.restaurant.cost <= min_budget && 
          r.restaurant.minutes <= min_minutes) {
            ra[i++] = r;
        }
    }

    RatingSorter rs = new RatingSorter();
    try {
        rs.sort(ra);
    } catch (Exception e) {
      System.err.println("Exception Thrown:");
      e.printStackTrace();
    }

    Vector v_result = new Vector();
    for (int j = 0; j < i; j++) {
        v_result.addElement(ra[j].restaurant);
    }
    return v_result;
  }
}
