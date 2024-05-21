/* 
   AlohaComm.e -- Version 0.3 -- Simple comm test

   Arturo Bejar & Chip Morningstar
   Electric Communities
   19-February-1996
   
   Copyright 1997 Electric Communities, all rights reserved worldwide.

   Changed to new comm system:  Oct 1996, Eric Messick
   Ported to pre-Vat April 1997, Chip Morningstar


   HOW TO RUN:

   first, start the listener:
       java ec.e.start.EBoot ec.examples.hc.AlohaComm reg=<file_for_SturdyRef>

   then, start the sender:
       java ec.e.start.EBoot ec.examples.hc.AlohaComm lookup=<file_for_SturdyRef> checkpoint=<file_for_Checkpoint>

   it will checkpoint, then halt.

   then, restart from the checkpoint file:

       java ec.e.quake.Revive <file_for_Checkpoint>.evat

   
*/

package ec.examples.hc;

import ec.e.file.EStdio;
import ec.e.net.ListenerInterest;
import ec.e.net.Registrar;
import ec.e.net.RegistrarLookupEException;
import ec.e.net.RegistrarException;
import ec.e.net.SturdyRef;
import ec.e.net.SturdyRefMaker;
import ec.e.net.SturdyRefFileExporter;
import ec.e.net.SturdyRefFileImporter;
import ec.e.start.ELaunchable;
import ec.e.start.EEnvironment;
import ec.e.start.Seismologist;
import ec.e.quake.TimeMachine;
import ec.e.start.TimeQuake;
import ec.e.lang.EString;
import java.io.IOException;

public eclass AlohaComm implements ELaunchable
{
    Registrar myRegistrar;
    
    emethod go(EEnvironment env) {
        String reg = env.getProperty("reg");
        String lookup = env.getProperty("lookup");
        myRegistrar = Registrar.summon(env);
        try {
            EStdio.initialize(env.vat());
        } catch (IOException e) {
            throw new Error("fatal EStdio initialization problem " + e);
        }
        try {
            myRegistrar.onTheAir();
        }
        catch (RegistrarException e) {
            throw new Error("fatal registrar problem going on the air: " + e);
        }

        if (reg != null) { // listener
            SturdyRefMaker refMaker = myRegistrar.getSturdyRefMaker();
            SturdyRefFileExporter refExporter = myRegistrar.getSturdyRefFileExporter(env);
            AlohaReceiver receiver = new AlohaReceiver(myRegistrar, refMaker, refExporter, reg, this);
        }
        else if (lookup != null) { // sender
            SturdyRefFileImporter importer = myRegistrar.getSturdyRefFileImporter(env);
            SturdyRef ref;
            try {
                ref = importer.importRef(lookup);
            }
            catch (Exception e) {
                throw new Error("problem importing reference " + lookup + ": " + e);
            }
            AlohaSender sender = new AlohaSender();
            sender <- sendAloha(ref, env, this);
        }
        else {
            EStdio.err().println("supply reg=<relative_path_name> for server or lookup=<same_file> for client");
        }
    }

    emethod outAHere() {
        try {
            myRegistrar.offTheAir();
        } catch (Throwable t) {
            EStdio.err().println("Receiver: caught exception going off the air " +
                                 t.getMessage());
            EStdio.reportException(t);
        }
    }
}

eclass AlohaSender 
{
    private AlohaReceiver myOtherGuy;
    private EEnvironment myEnv;
    private SturdyRef myRef;
    private AlohaComm myMain;
    
    emethod sendAloha(SturdyRef ref, EEnvironment env, AlohaComm mainBody) {
        myEnv = env;
        myRef = ref;
        myMain = mainBody;
        
        lookupDude();
        etry {
            try {
                EStdio.out().println("Sender: sending aloha()");
                myOtherGuy <- aloha(this);
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

    private void lookupDude() {
        AlohaReceiver otherGuy;
        myOtherGuy = otherGuy;
        myRef.followRef(&otherGuy);
    }

    emethod sendAlohaDude() {
        etry {
            EStdio.out().println("Sender: sending alohaDude");
            myOtherGuy <- alohaDude(this);
        }
        ecatch (Throwable e) {
            EStdio.err().println("Sender: caught E exception in sendAlohaDude, retrying: " + e);
            lookupDude();
            this <- sendAlohaDude();
        }
    }

    emethod aloha() {
        EStdio.out().println("Sender: Aloha!");

        TimeMachine tm;
        try {
            tm = TimeMachine.summon(myEnv);
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

        AlohaSender chan;
            
        Seismologist willSend = new AlohaSeismologist(this, &chan);

        tm <- hibernate(willSend, 0);
            
        chan <- sendAlohaDude();
    }

    emethod outAHere() {
        myMain <- outAHere();
    }
}

eclass AlohaReceiver {
    Registrar myRegistrar;
    AlohaComm myMain;
    
    AlohaReceiver(Registrar registrar, SturdyRefMaker refMaker, SturdyRefFileExporter refExporter, String reg, AlohaComm mainBody) {
        myMain = mainBody;
        try {
            myRegistrar = registrar;
            SturdyRef ref;
            ref = refMaker.makeSturdyRef(this);
            refExporter.exportRef(ref, reg);
            EStdio.out().println("Receiver: run   java ec.e.start.EBoot ec.examples.hc.AlohaComm lookup=" + reg);
        } catch (Throwable t) {
            EStdio.err().println("Receiver: caught exception in startup " +
                                 t.getMessage());
            EStdio.reportException(t);
        }
    }

    emethod aloha(AlohaSender sender) {
        EStdio.out().println("Receiver: Aloha!");
        sender <- aloha();
    }

    emethod alohaDude(AlohaSender sender) {
        EStdio.out().println("Receiver: Aloha Duuude (was: Hola Mundo)");
        sender <- outAHere();
        myMain <- outAHere();
    }
}

eclass AlohaSeismologist implements Seismologist {
    EObject myObj;
    EResult myResult;
    
    AlohaSeismologist(EObject obj, EResult result) {
        myObj = obj;
        myResult = result;
    }
    
    emethod noticeQuake(TimeQuake q) {
        EStdio.out().println("quaking in my EBoots: " + q);
    }

    emethod noticeCommit() {
        myResult <- forward(myObj);
    }
}
