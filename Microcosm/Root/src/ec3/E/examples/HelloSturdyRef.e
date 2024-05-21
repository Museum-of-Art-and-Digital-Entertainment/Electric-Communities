/* 
   HelloComm.e -- Version 0.3 -- Simple comm test

   Arturo Bejar & Chip Morningstar
   Electric Communities
   19-February-1996
   
   Copyright 1997 Electric Communities, all rights reserved worldwide.

   Changed to new comm system:  Oct 1996, Eric Messick
   Ported to pre-Vat April 1997, Chip Morningstar
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
import ec.e.lang.EString;
import java.io.IOException;

public eclass HelloSturdyRef implements ELaunchable
{
    emethod go(EEnvironment env) {
        String reg = env.getProperty("reg");
        String lookup = env.getProperty("lookup");
        Registrar registrar = Registrar.summon(env);
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
        
        if (reg != null) {
            SturdyRefMaker refMaker = registrar.getSturdyRefMaker();
            SturdyRefFileExporter refExporter = registrar.getSturdyRefFileExporter(env);
            HelloSturdyRefReceiver receiver = new HelloSturdyRefReceiver(refMaker, refExporter, reg);
        }
        else if (lookup != null) {
            SturdyRefMaker refMaker = registrar.getSturdyRefMaker();
            SturdyRefFileImporter importer = registrar.getSturdyRefFileImporter(env);
            SturdyRef ref;
            SturdyRef me;
            
            try {
                ref = importer.importRef(lookup);
            }
            catch (Exception e) {
                throw new Error("problem importing reference " + lookup + ": " + e);
            }
            HelloSturdyRefSender sender = new HelloSturdyRefSender();
            me = refMaker.makeSturdyRef(sender);
            sender <- sendHello(ref, me);
        }
        else {
            EStdio.err().println("supply reg=<relative_path_name> for server or lookup=<same_file> for client");
        }
    }
}

eclass HelloSturdyRefSender 
{
    emethod sendHello(SturdyRef ref, SturdyRef me) {
        etry {
            try {
                HelloSturdyRefReceiver otherGuy;
                ref.followRef(&otherGuy);
                EStdio.out().println("Sender: sending hello()");
                otherGuy <- hello(me);
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

    emethod hiThere() {
        EStdio.out().println("Sender: Hi There! (was: Hola Mundo)");
    }
}

eclass HelloSturdyRefReceiver {
    HelloSturdyRefReceiver(SturdyRefMaker refMaker, SturdyRefFileExporter refExporter, String reg) {
        try {
            SturdyRef ref;
            ref = refMaker.makeSturdyRef(this);
            refExporter.exportRef(ref, reg);
            EStdio.out().println("Receiver: run   java ec.e.start.EBoot ec.examples.hc.HelloSturdyRef lookup=" + reg);
        } catch (Throwable t) {
            EStdio.err().println("Receiver: caught exception in startup " +
                                 t.getMessage());
            EStdio.reportException(t);
        }
    }

    emethod hello(SturdyRef ref) {
        HelloSturdyRefSender sender;
        EStdio.out().println("Receiver: Hi!");
        ref.followRef(&sender);
        sender <- hiThere();
    }
}


