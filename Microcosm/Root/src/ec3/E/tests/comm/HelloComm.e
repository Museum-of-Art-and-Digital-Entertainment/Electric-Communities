/* 
   HelloComm.e -- Version 0.3 -- Simple comm test

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
       java ec.e.start.EBoot ec.tests.comm.HelloComm reg=<file_for_SturdyRef>

   then, start the sender:
       java ec.e.start.EBoot ec.tests.comm.HelloComm lookup=<file_for_SturdyRef>


   HOW TO RUN STANDALONE:

   Pick two port numbers that are not currently in use on the machines
   that you're running on.  Construct <listener_address> as
   <listener_hostname>:<listener_port> and <sender_address> as
   <sender_hostname>:<sender_port>.

   first, start the listener:
       java ec.e.start.EBoot ec.tests.comm.HelloComm reg=<file_for_SturdyRef> listenAddr=<listener_address>

   then, start the sender:
       java ec.e.start.EBoot ec.tests.comm.HelloComm lookup=<file_for_SturdyRef> listenAddr=<sender_address>
       
*/

package ec.tests.comm;

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
import ec.e.lang.EString;
import java.io.IOException;

public eclass HelloComm implements ELaunchable
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
        
        if (reg != null) {
            SturdyRefMaker refMaker = registrar.getSturdyRefMaker();
            SturdyRefFileExporter refExporter = registrar.getSturdyRefFileExporter(env);
            HelloReceiver receiver = new HelloReceiver(registrar, refMaker, refExporter, reg);
        }
        else if (lookup != null) {
            SturdyRefFileImporter importer = registrar.getSturdyRefFileImporter(env);
            SturdyRef ref;
            try {
                ref = importer.importRef(lookup);
            }
            catch (Exception e) {
                throw new NestedError("problem importing reference " + lookup, e);
            }
            HelloSender sender = new HelloSender();
            sender <- sendHello(ref);
        }
        else {
            EStdio.err().println("supply reg=<relative_path_name> for server or lookup=<same_file> for client");
        }
    }
}

eclass HelloSender 
{
    emethod sendHello(SturdyRef ref) {
        etry {
            try {
                HelloReceiver otherGuy;
                ref.followRef(&otherGuy);
                EStdio.out().println("Sender: sending hello()");
                otherGuy <- hello();
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
}

eclass HelloReceiver {
    Registrar myRegistrar;
    
    HelloReceiver(Registrar registrar, SturdyRefMaker refMaker, SturdyRefFileExporter refExporter, String reg) {
        try {
            myRegistrar = registrar;
            SturdyRef ref;
            ref = refMaker.makeSturdyRef(this);
            refExporter.exportRef(ref, reg);
            EStdio.out().println("Receiver: run   java ec.e.start.EBoot ec.tests.comm.HelloComm lookup=" + reg);
        } catch (Throwable t) {
            EStdio.err().println("Receiver: caught exception in startup " +
                                 t.getMessage());
            EStdio.reportException(t);
        }
    }

    emethod hello() {
        EStdio.out().println("Receiver: Hola Mundo");
        try {
            myRegistrar.offTheAir();
        } catch (Throwable t) {
            EStdio.err().println("Receiver: caught exception in hello " +
                                 t.getMessage());
            EStdio.reportException(t);
        }
    }
}


