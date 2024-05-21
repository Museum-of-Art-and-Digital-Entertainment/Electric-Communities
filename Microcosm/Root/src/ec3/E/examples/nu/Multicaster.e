/* 
        Multicaster.e
        Jay Fenton 
        Proprietary and Confidential
        Copyright 1997 Electric Communities.  All rights reserved worldwide.
*/


package ec.tests.nu;

import java.util.Vector;
import java.util.Enumeration;
import ec.e.file.EStdio;

/**
 This interface describes the local methods that can be invoked on a Multicaster 
@author Jay Fenton
*/
interface MulticasterLocalInterface {
/**
    @param client presence of client unum to add to the multicast set.
*/
    void    addClientPresence(EObject client);
/**
    @param client presence of client unum to remove from the multicast set.
*/
    void    dropClientPresence(EObject client);
/**
    @param msg envelope to send to all presences on the multicast list.
*/
    void    sendToAll(RtEnvelope msg);
}


/**
 The Multicaster object proper.
@author Jay Fenton
*/

eclass Multicaster implements MulticasterLocalInterface {
    Vector clients = new Vector();

    public Multicaster() {

    }

/**
    @see MulticasterLocalInterface.
*/
    local void  addClientPresence(EObject client) {
        if(!clients.contains(client)) // avoid dup targets
            clients.addElement(client);
    }

/**
    @see MulticasterLocalInterface.
*/
    local void  dropClientPresence(EObject client) {
        clients.removeElement(client);
    }

/**
    @see MulticasterLocalInterface.
*/
    local void  sendToAll(RtEnvelope msg) {

        Vector failedSends = null;
        Enumeration en = clients.elements();
        while (en.hasMoreElements()) {
            EObject mt = (EObject) en.nextElement();

            try {
            etry {
                EStdio.out().println("Sending multicast to: " + mt);
                mt <- msg;
            // If the send generated an e-exception, remove the presence from the list.
              } ecatch (Exception ex) { // Later failure
                dropClientPresence(mt);
                EStdio.out().println("Multicast failure for: "
                                    + mt + " " + ex.getMessage());
              }
            } catch (Exception ex2) { // Immediate failure, accumulate list of losers to drop.
                if(failedSends == null)
                    failedSends = new Vector();
                failedSends.addElement(mt);
                EStdio.out().println("Immediate multicast failure for: " 
                                    + mt + " " + ex2.getMessage());
            }
        }

    // If we have one or more presences that no longer work, remove them from the multicast list.
        if(failedSends != null) {
           Enumeration fs = failedSends.elements();
           while (fs.hasMoreElements()) {
               EObject anObj = (EObject) fs.nextElement();
               dropClientPresence(anObj);
            }

        }
    }
}
