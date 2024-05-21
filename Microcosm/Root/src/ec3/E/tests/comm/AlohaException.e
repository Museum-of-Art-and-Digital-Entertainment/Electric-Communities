/* 
   AlohaException.e -- Version 0.3 -- Simple comm test

   Arturo Bejar & Chip Morningstar
   Electric Communities
   19-February-1996
   
   Copyright 1997 Electric Communities, all rights reserved worldwide.

   Changed to new comm system:  Oct 1996, Eric Messick
   Ported to pre-Vat April 1997, Chip Morningstar


   HOW TO RUN USING A PROCESS LOCATION SERVER:

   In your ~/.eprops/default file (or on the command line) specify a
   matching RegisterWith/SearchPath pair specifing the PLS to use.
   For example:

   RegisterWithURLs=ftp://www.communities.com/pub/pls-location/gracie.communities.com_1666
   SearchPath=gracie.communities.com:1666


   first, start the listener:
       java ec.e.start.EBoot ec.tests.ae.AlohaException reg=<file_for_SturdyRef>

   then, start the sender:
       java ec.e.start.EBoot ec.tests.ae.AlohaException lookup=<file_for_SturdyRef> checkpoint=<file_for_Checkpoint>

   it will checkpoint, then halt.

   then, restart from the checkpoint file:

       java ec.e.quake.Revive <file_for_Checkpoint>.evat


   HOW TO RUN STANDALONE:

   Pick two port numbers that are not currently in use on the machines
   that you're running on.  Construct <listener_address> as
   <listener_hostname>:<listener_port> and <sender_address> as
   <sender_hostname>:<sender_port>.

   first, start the listener:
       java ec.e.start.EBoot ec.tests.ae.AlohaException reg=<file_for_SturdyRef> listenAddr=<listener_address>

   then, start the sender:
       java ec.e.start.EBoot ec.tests.ae.AlohaException lookup=<file_for_SturdyRef> checkpoint=<file_for_Checkpoint> listenAddr=<sender_address>

   it will checkpoint, then halt.

   then, restart from the checkpoint file:

       java ec.e.quake.Revive <file_for_Checkpoint>.evat
       
   
*/

package ec.tests.ae;

import ec.util.NestedError;
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

public eclass AlohaException implements ELaunchable
{
    emethod go(EEnvironment env) {
        String reg = env.getProperty("reg");
        String lookup = env.getProperty("lookup");
        Registrar registrar = Registrar.summon(env);
        String listenAddr = env.getProperty("ListenAddr");

        if (listenAddr != null) {
            env.setProperty("SearchPath", listenAddr);
        }
        
        try {
            registrar.onTheAir(listenAddr);
        }
        catch (RegistrarException e) {
            throw new NestedError("fatal registrar problem going on the air", e);
        }

        if (reg != null) { // listener
            SturdyRefMaker refMaker = registrar.getSturdyRefMaker();
            SturdyRefFileExporter refExporter = registrar.getSturdyRefFileExporter(env);
            AlohaReceiver receiver = new AlohaReceiver(registrar, refMaker, refExporter, reg);
        }
        else if (lookup != null) { // sender
            SturdyRefFileImporter importer = registrar.getSturdyRefFileImporter(env);
            SturdyRef ref;
            try {
                ref = importer.importRef(lookup);
            }
            catch (Exception e) {
                throw new NestedError("problem importing reference " + lookup, e);
            }
            AlohaSender sender = new AlohaSender();
            sender <- sendAloha(ref, env);
        }
        else {
            EStdio.err().println("supply reg=<relative_path_name> for server or lookup=<same_file> for client");
        }
    }
}

eclass AlohaSender 
{
    private AlohaReceiver myOtherGuy;
    private EEnvironment myEnv;
    private SturdyRef myRef;
    
    emethod sendAloha(SturdyRef ref, EEnvironment env) {
        myEnv = env;
        myRef = ref;
        
        lookupDude();
        etry {
            try {
                EStdio.out().println("Sender: sending aloha()");
                myOtherGuy <- aloha(this, new RuntimeException("Excepcion, amigo!"));
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
            myOtherGuy <- alohaDude(new RuntimeException("Exception, dude!"));
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
        catch (Exception e) {
            throw new NestedError("couldn't make a TimeMachine", e);
        }

        if (myEnv.getProperty("Passphrase") != null) {
            tm <- setPassphrase(myEnv.getProperty("Passphrase"));
        }
        
        AlohaSender chan;
            
        Seismologist willSend = new AlohaSeismologist(this, &chan);

        tm <- hibernate(willSend, 0);
            
        chan <- sendAlohaDude();
    }
    
}

eclass AlohaReceiver {
    Registrar myRegistrar;
    
    AlohaReceiver(Registrar registrar, SturdyRefMaker refMaker, SturdyRefFileExporter refExporter, String reg) {
        try {
            myRegistrar = registrar;
            SturdyRef ref = refMaker.makeSturdyRef(this);
            refExporter.exportRef(ref, reg);
            EStdio.out().println("Receiver: run   java ec.e.start.EBoot ec.tests.ae.AlohaException lookup=" + reg);
        } catch (Throwable t) {
            EStdio.err().println("Receiver: caught exception in startup " +
                                 t.getMessage());
            EStdio.reportException(t);
        }
    }

    emethod aloha(AlohaSender sender, Throwable t) {
        EStdio.out().println("Receiver: Aloha! " + t);
        t.printStackTrace();
        sender <- aloha();
    }

    emethod alohaDude(Throwable th) {
        EStdio.out().println("Receiver: Aloha Duuude (was: Hola Mundo): " + th);
        th.printStackTrace();
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
