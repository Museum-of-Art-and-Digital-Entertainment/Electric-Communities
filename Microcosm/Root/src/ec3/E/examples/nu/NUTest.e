/* 
        New Unum Model Test
        v 0.1

        Jay Fenton 
        Proprietary and Confidential
        Copyright 1997 Electric Communities.  All rights reserved worldwide.
*/

package ec.tests.nu;

import ec.e.start.ELaunchable;    // Needed for Agency (calls go())
import ec.e.start.EEnvironment;   // Needed for Agency

import ec.e.net.ListenerInterest;
import ec.e.net.Registrar;
import ec.e.net.RegistrarLookupEException;
import ec.e.net.RegistrarException;
import ec.e.net.SturdyRef;
import ec.e.net.SturdyRefMaker;
import ec.e.net.SturdyRefFileExporter;
import ec.e.net.SturdyRefFileImporter;
import ec.e.net.SturdyRefImporter;

import ec.e.file.EStdio;
import ec.e.lang.EString;

import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;

import ec.tests.capabilities;


import ec.e.start.Seismologist;
import ec.e.quake.TimeMachine;
import ec.e.start.TimeQuake;

import ec.e.run.ETimer;
import java.util.Hashtable;

public eclass NUTest implements ELaunchable
{
    Registrar registrar;
    SturdyRefMaker refMaker;
    EObject PostQuake;
    NUSeismologist seismo = new NUSeismologist(&PostQuake);
    TimeMachine tm;
    RtTimer timer = new RtTimer();
        RegionServer r1;
        RegionClientUnum rc1;
        RockUnum rock1;


    emethod go(EEnvironment env) {
        String reg = env.getProperty("reg");
        String lookup = env.getProperty("lookup");

        registrar = Registrar.summon(env);
        refMaker = registrar.getSturdyRefMaker();

        try {
            EStdio.initialize(env.vat());
        } catch (IOException e) {
            throw new Error("fatal EStdio initialization problem " + e);
        }

        try {
            registrar.onTheAir();
        }
        catch (RegistrarException e) {
            throw new Error("fatal registrar problem going on the air: " + e);
        }

        try {
            tm = TimeMachine.summon(env);
            tm <- nextQuake(seismo);    // Arrange to be notified of the next quake.
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("couldn't make a TimeMachine: " + e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("couldn't make a TimeMachine: " + e);
        }
        catch (InstantiationException e) {
            throw new RuntimeException("couldn't make a TimeMachine: " + e);
        }
 
        if (reg != null) {
            SturdyRefFileExporter refExporter = registrar.getSturdyRefFileExporter(env);
            launchServer(refExporter, reg);
        }
        else if (lookup != null) {
             SturdyRefFileImporter importer = registrar.getSturdyRefFileImporter(env);
             SturdyRef ref;
            try {
                importer.importRef(lookup, &ref);
                launchClient(ref);
            }
            catch (IOException e) {
                throw new Error("problem importing reference " + lookup + ": " + e);
            }
        }
        else {
            EStdio.err().println("supply reg=<relative_path_name> for server or lookup=<same_file> for client");
        }
    }

    emethod launchServer(SturdyRefFileExporter refExporter, String reg) {
        try {
            SturdyRef ref;
            refMaker <- makeSturdyRef(this, &ref);
            refExporter <- exportRef(ref, reg);
            
            
            r1 = new RegionServer(refMaker);
            rc1 = new RegionClientUnum(r1);
            rock1 = new RockUnum(refMaker, rc1,"Kitty");

            ((SeismoAddDrop) seismo).addToRootList(r1);
            serverTest();
            EStdio.out().println("Receiver: java ec.e.start.EBoot ec.tests.nu.NUTest lookup=" + reg);
        } catch (Throwable t) {
            EStdio.err().println("Receiver: caught exception in startup " +
                                 t.getMessage());
            EStdio.reportException(t);
        }

    }

    emethod serverTest() {
        EObject TimerTrip;

        RtTimer timer1 = new RtTimer();
        timer1.setTimeout(15000, &TimerTrip);
        int i = 0;

        ewhen TimerTrip (boolean flag1) {
            EStdio.out().println("Server hibernation.");
            tm <- hibernate(seismo, 0);

            ewhen PostQuake (boolean flag) {
                EStdio.out().println("PostQuake event fired.");
                rock1 <- setNewName("server t1");
                
                EObject Trip2;
                RtTimer timer2 = new RtTimer();
                timer2.setTimeout(10000, &Trip2);
                ewhenever Trip2 (boolean flag3) {
                    i++;
                    rock1 <-setNewName("server set: " + i);
                    timer2.setTimeout(5000, &Trip2);
                }
            }
        }
    }

    emethod clientTest() {
        EObject TimerTrip;

        RtTimer timer1 = new RtTimer();
        timer1.setTimeout(2000, &TimerTrip);

        ewhen TimerTrip (boolean flag1) {
//          EStdio.out().println("Client hibernation.");
//          tm <- hibernate(seismo, 0);
            int i = 0;
//          ewhen PostQuake (boolean flag) {
                EStdio.out().println("PostQuake server timer fired: " + rock1);
//              rock1 <- setNewName("PostQuake");
                
                EObject Trip2;
                RtTimer timer2 = new RtTimer();
                timer2.setTimeout(10000, &Trip2);
                ewhenever Trip2 (boolean flag3) {
                    i++;
                    rock1 <-setNewName("client set: " + i);
                    timer2.setTimeout(5000, &Trip2);
                }
//          }
        }
    }

    emethod launchClient(SturdyRef serverRef) {
        NUTest myServer;
        
        etry {
            connectToServer(serverRef, &myServer);
        } ecatch (Exception ex) {
            EStdio.out().println("connectToServer prob: "
                +   ex.getMessage());
        }

        RegionClientUnum myClient;
        EObjectWrapper  theRock;

        myServer <- clientConnect(&myClient, &theRock);
        ewhen myClient (RegionClientUnum client) {
            ((ClientRegInterface) client).sendClientReceiverRegistration(refMaker);
            ((SeismoAddDrop) seismo).addToRootList(client);
            RockUnum localRock = new RockUnum(refMaker, client, "ClientRock");
            localRock <- setNewName("KittyCat");
            this <- clientTest();
        }
        ewhen theRock (RockUnum aRock) {
            rock1 = aRock;
            EStdio.out().println("other guys rock: " + rock1);
            rock1 <- setNewName("Purry");
        }
    }

    emethod connectToServer(SturdyRef serverRef, EResult server) {
        etry {
            try {
                serverRef <- followRef(server);
                EStdio.out().println("Client connecting to server");
             } catch (Throwable t) {
                EStdio.err().println("Sender: caught exception in lookup " +
                                     t.getMessage());
                EStdio.reportException(t);
            }
        } ecatch (RegistrarLookupEException e) {
            EStdio.err().println("Sender: caught E exception in lookup "
                                 + e.getMessage());
        }
    }

    emethod clientConnect(EResult clientRegion, EResult aRock) {
        EStdio.out().println("Server: Connection established.");
//        ((ClientRegInterface) testUnum).sendClientReceiverRegistration(refMaker);
//      ((SeismoAddDrop) seismo).addToRootList(testUnum);
        clientRegion <- forward(new EObjectWrapper(rc1));
        aRock <- forward(new EObjectWrapper(rock1) );
        rock1 <- setNewName("Kitty");
     }
}

interface SeismoAddDrop {
    void addToRootList(EObject theObj);
    void dropFromRootList(EObject theObj);
}

eclass NUSeismologist implements Seismologist, SeismoAddDrop {
    Vector myRootObjects = new Vector();
    EResult notifyAfterQuake;

    // XXX CLUDGE
    Hashtable rootUna;
    // XXX CLUDGE

    NUSeismologist(EResult wakeUpAfter) {
        // XXX CLUDGE
        rootUna = UniqueUnumRegistry.registeredUna;
        // XXX CLUDGE

        notifyAfterQuake = wakeUpAfter;
    }

    local void addToRootList(EObject theObj) {
        myRootObjects.addElement(theObj);
    }

    local void dropFromRootList(EObject theObj) {
        myRootObjects.removeElement(theObj);
    }

    emethod noticeQuake(TimeQuake q) {
        EStdio.out().println("quaking in my EBoots: " + q);

        // XXX CLUDGE
        UniqueUnumRegistry.registeredUna = rootUna;
        // XXX CLUDGE
 
        Enumeration en = myRootObjects.elements();
        while (en.hasMoreElements()) {
         ReviveConnectionsInterface thisRoot = (ReviveConnectionsInterface) en.nextElement();
         etry {
            thisRoot <- reviveConnections();
         } ecatch (Exception ex) { // remove bad references from the list.
                EStdio.out().println("Revive failure: " + thisRoot);
            }
        }
    
//      if(notifyAfterQuake != null)
//          notifyAfterQuake <- forward(etrue);
    }

    emethod noticeCommit() {
        RtTimer timer = new RtTimer();
        EStdio.out().println("Commit taking place.");
        if(notifyAfterQuake != null)
            timer.setTimeout(5000, notifyAfterQuake);
    }

}

