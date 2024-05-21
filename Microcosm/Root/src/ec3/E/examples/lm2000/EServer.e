
package ec.examples.lm2000;

import java.util.*;
import java.io.*;
import ec.e.comm.*;
import ec.e.lang.*;

public eclass EServer implements ELaunchable {

  RtRegistration reg;

  emethod go(RtEEnvironment env) {
    RtNetworkController con = env.startNetworkEnvironment();
    // Here, we should determine whether or not there are other servers
    // running already.  How to do this? XXX
    reg = env.getRegistrar().register("EServer1", this);
  }

  Vector restaurants;
  Vector clients;
  Vector clientsReadyForLunch;

  EServer() {
    clients = new Vector();
    clientsReadyForLunch = new Vector();
    restaurants = Common.loadRestaurants();
  }

  // for testing
  emethod ping() {
    System.out.println("EServer got ping message");
  }

  emethod processNewClient(EClient eClient, Person p) {
    for (Enumeration en = clients.elements(); en.hasMoreElements();) {
      ClientInfo ci = (ClientInfo)en.nextElement();
      if (ci.person.name.equals(p.name)) {

        // ping old client
        EBoolean timeout;
        EInteger result;
        RtTimer timer = new RtTimer();
        int tid;
        tid = timer.setTimeout(2000, &timeout);
        System.out.println("processNewClient: Pinging old client");
        ci.eClient<-processPing(&result);
        
        ewhen result(int rest) {
          System.out.println("processNewClient: old client responded.  Shutting down new client.");
          // old client responded.  Shut down new client.
          eClient<-processShutdownClient("Duplicate client name " + p.name);
        } 
        eorwhen timeout(boolean t) {
          System.out.println("processNewClient: old client did not respond.  Replacing old client with new client");
          // old client did not respond.  Tell it to shut down. 
          ci.eClient<-processShutdownClient("Client didn't respond to ping");
          // replace old client in client list with new client.
          ci.eClient = eClient;
          ci.person = p;
          // make all clients process lunchgoers, in the event that
          // the gone-away client has changed state XXX
        }
      }
    }

    // best-fit a list of restaurants, based on who is ready for lunch
    // send list to the new client
    Vector bestRestaurants = Common.bestFit(clientsReadyForLunch);
    Vector lunchGoers = new Vector();
    for (Enumeration en = clientsReadyForLunch.elements(); 
      en.hasMoreElements();) {
        ClientInfo ci = (ClientInfo)en.nextElement();
        lunchGoers.addElement(ci.person.name);
    }

    eClient<-processLunchGoers(lunchGoers);
    eClient<-processBestRestaurants(bestRestaurants);
    eClient<-processAllRestaurants(restaurants);
    ClientInfo ci = new ClientInfo(eClient, p);
    clients.addElement(ci);
  }

  emethod updateClientInfo(String name, Person p) {
    // (Called when someone updates their personal info)
    // Find client in list of clients
    // update person info for this client
    for (Enumeration en = clients.elements();
        en.hasMoreElements();) {
        ClientInfo ci = (ClientInfo)en.nextElement();
        if (ci.person.name.equals(name)) {
            ci.person = p;
            return;
        }
    }
    // this breaks if someone changes their name.  XXX
  }

  emethod processReadyForLunch(String name_in) {
    // see if client is on the 'I'm ready-to-eat' list
    // if not, add
    for (Enumeration en = clientsReadyForLunch.elements();
        en.hasMoreElements();) {
        ClientInfo ci = (ClientInfo)en.nextElement();
        String name = ci.person.name;
        if (name.equals(name_in)) {
            break;
        }
        if (ci == clientsReadyForLunch.lastElement()) {
          for (Enumeration enum = clients.elements(); enum.hasMoreElements();){
            ClientInfo cli = (ClientInfo)enum.nextElement();
            if (cli.person.name.equals(name_in)) {
              clientsReadyForLunch.addElement(cli);
            }
          }
        }
    }
    if (clientsReadyForLunch.isEmpty()) {
      for (Enumeration en = clients.elements(); en.hasMoreElements();) {
        ClientInfo cli = (ClientInfo)en.nextElement();
        if (cli.person.name.equals(name_in)) {
          clientsReadyForLunch.addElement(cli);
        }
      }
    }
    sendBestFit();
  }

  emethod processNotReadyForLunch(String name_in) {
    ClientInfo ci = null;
    for (Enumeration en = clients.elements(); en.hasMoreElements();) {
      ci = (ClientInfo)en.nextElement();
      if (ci.person.name.equals(name_in)) {
        break;
      }
    }
    if (ci == null) {
      return;
    }
    // see if client is on the 'I'm ready-to-eat' list
    // if so, delete
    if (clientsReadyForLunch.removeElement(ci)) {
        sendBestFit();
    }
  }

  emethod sendBestFit() {
    // best-fit a list of restaurants, based on who's ready for lunch
    // send list to each client
    Vector bestRestaurants = Common.bestFit(clientsReadyForLunch);
    Vector lunchGoers = new Vector();
    for (Enumeration en = clientsReadyForLunch.elements(); 
      en.hasMoreElements();) {
        ClientInfo ci = (ClientInfo)en.nextElement();
        lunchGoers.addElement(ci.person.name);
    }
    for (Enumeration en = clients.elements(); en.hasMoreElements();) {
        ClientInfo ci = (ClientInfo)en.nextElement();
        ci.eClient<-processLunchGoers(lunchGoers);
        ci.eClient<-processBestRestaurants(bestRestaurants);
    }
  }

  emethod processAddRestaurant(Restaurant r) {
    // check for duplicates XXX
    restaurants.addElement(r);
    // save changes to disk
    Common.saveRestaurants(restaurants);
    // send the new vector of restaurants to each client
    // need to do this with a proper distributor XXX
    for (Enumeration en = clients.elements();  en.hasMoreElements();) {
        ClientInfo ci = (ClientInfo)en.nextElement();
        ci.eClient<-processAllRestaurants(restaurants);
    }
  }

  emethod processDeleteRestaurant(String name) {
    for (Enumeration en = restaurants.elements();  en.hasMoreElements();) {
        Restaurant rest = (Restaurant)en.nextElement();
        if (rest.name.equals(name)) {
            restaurants.removeElement(rest);
            break;
        }
    }

    // save changes to disk
    Common.saveRestaurants(restaurants);

    // send the new vector of restaurants to each client
    // need to do this with a proper distributor XXX
    for (Enumeration en = clients.elements();  en.hasMoreElements();) {
        ClientInfo ci = (ClientInfo)en.nextElement();
        ci.eClient<-processAllRestaurants(restaurants);
    }
  }
}
