/* 
   HastaComm.e -- Version 0.3 -- Simple comm test

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
import ec.e.start.Seismologist;
import ec.e.quake.TimeMachine;
import ec.e.start.TimeQuake;
import ec.e.lang.EString;
import java.io.IOException;

public eclass HastaComm implements ELaunchable
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

        if (reg != null) { // listener
            TimeMachine tm;
            try {
                tm = TimeMachine.summon(env);
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

            SturdyRefMaker refMaker = registrar.getSturdyRefMaker();
            SturdyRefFileExporter refExporter = registrar.getSturdyRefFileExporter(env);
            HastaReceiver receiver = new HastaReceiver(registrar, refMaker, refExporter, reg);

            tm <- hibernate(null, 0);
        }
        else if (lookup != null) { // sender
            SturdyRefFileImporter importer = registrar.getSturdyRefFileImporter(env);
            SturdyRef ref;
            try {
                ref = importer.importRef(lookup);
            }
            catch (Exception e) {
                throw new Error("problem importing reference " + lookup + ": " + e);
            }
            HastaSender sender = new HastaSender();
            sender <- sendHasta(ref);
        }
        else {
            EStdio.err().println("supply reg=<relative_path_name> for server or lookup=<same_file> for client");
        }
    }
}

eclass HastaSender 
{
    emethod sendHasta(SturdyRef ref) {
        etry {
            try {
                HastaReceiver otherGuy;
                ref.followRef(&otherGuy);
                EStdio.out().println("Sender: sending hasta()");
                otherGuy <- hasta();
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

eclass HastaReceiver {
    Registrar myRegistrar;
    
    HastaReceiver(Registrar registrar, SturdyRefMaker refMaker, SturdyRefFileExporter refExporter, String reg) {
        try {
            myRegistrar = registrar;
            SturdyRef ref;
            ref = refMaker.makeSturdyRef(this);
            refExporter.exportRef(ref, reg);
            EStdio.out().println("Receiver: run   java ec.e.start.EBoot ec.examples.hc.HastaComm lookup=" + reg);
        } catch (Throwable t) {
            EStdio.err().println("Receiver: caught exception in startup " +
                                 t.getMessage());
            EStdio.reportException(t);
        }
    }

    emethod hasta() {
        EStdio.out().println("Receiver: Hasta la Vishnu (was: Hola Mundo)");
        try {
            myRegistrar.offTheAir();
        } catch (Throwable t) {
            EStdio.err().println("Receiver: caught exception in hasta " +
                                 t.getMessage());
            EStdio.reportException(t);
        }
    }
}
