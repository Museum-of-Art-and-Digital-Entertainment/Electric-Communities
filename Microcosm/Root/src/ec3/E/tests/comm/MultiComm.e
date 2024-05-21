/* 
   MultiComm.e -- Version 0.3 -- Simple comm test

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

   Specify ActiveConnectionLimit=<num> to change the connection cache size.
   
   first, start as many processes as you like:
       java ec.e.start.EBoot ec.tests.comm.MultiComm reg=<file_for_SturdyRef>

   Once you're running:
   
   enter hibernation at anytime by issuing the "suspend" command (if
   you've specified checkpoint=file.evat).  Revive like this:
       java ec.e.quake.Revive file.evat
   
   then, connect them together using the open command in various processes:
       open <file_for_SturdyRef>

   then, send messages by typing other text.

   HOW TO RUN STANDALONE:

   Pick two port numbers that are not currently in use on the machines
   that you're running on.  Construct <listener_address> as
   <listener_hostname>:<listener_port> and <sender_address> as
   <sender_hostname>:<sender_port>.

   Specify ActiveConnectionLimit=<num> to change the connection cache size.

   first, start as many processes as you like:
       java ec.e.start.EBoot ec.tests.comm.MultiComm reg=<file_for_SturdyRef> listenAddr=<listener_address>

   See Once you're running, above.
       
*/

package ec.tests.comm;

import ec.util.CompletionNoticer;
import ec.util.NestedException;
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
import ec.e.start.TimeQuake;
import ec.e.quake.TimeMachine;
import ec.e.io.EInputHandler;
import ec.e.io.EConsoleMaker;
import java.io.IOException;

public eclass MultiComm implements ELaunchable
{
    emethod go(EEnvironment env) {
        String reg = env.getProperty("reg");
        String listenAddr = env.getProperty("ListenAddr");

        if (listenAddr != null) {
            env.setProperty("SearchPath", listenAddr);
        }
        
        Registrar registrar;
        try {
            registrar = Registrar.summon(env);
            registrar.onTheAir(listenAddr);
        }
        catch (Exception e) {
            throw new NestedException("fatal registrar problem going on the air", e);
        }
        
        if (reg != null) {
            HelloTalker talker = new HelloTalker();
            talker <- startup(env, reg);
        }
        else {
            EStdio.err().println("supply reg=<SturdyRefFile>;  then do open <SturdyRefFile> in another copy");
        }
    }
}

eclass HelloTalker implements EInputHandler, CompletionNoticer, Seismologist
{
    SturdyRef myRef;
    EEnvironment myEnv;
    Registrar myRegistrar;
    
    emethod startup(EEnvironment env, String reg) {
        try {
            myEnv = env;
            myRegistrar = Registrar.summon(myEnv);
            SturdyRef ref;
            SturdyRefMaker refMaker = myRegistrar.getSturdyRefMaker();
            SturdyRefFileExporter refExporter = myRegistrar.getSturdyRefFileExporter(myEnv);
            ref = refMaker.makeSturdyRef(this);
            refExporter.exportRef(ref, reg);
            EConsoleMaker consoleMaker = EConsoleMaker.summon(env);
            consoleMaker.makeConsole(this, EStdio.in(), null);
        }
        catch (Exception e) {
            EStdio.reportException(e);
        }
    }

    emethod handleInput (String line) {
        if (line == null) {
            try {
                myRegistrar.offTheAir();
            }
            catch (Exception e) {
                throw new NestedException("problem going off the air ", e);
            }
            return;
        }
        EStdio.out().println("got input line: " + line);
        if (line.equals("suspend")) {
            EStdio.out().println("preparing to hibernate...");
            try {
                myRegistrar.prepareToHibernate(this);
            }
            catch (Exception e) {
                throw new NestedException("problem preparing to hibernate", e);
            }
            return;
        }
        if (line.equals("check")) {
            EStdio.out().println("checking hibernation status");
            try {
                myRegistrar.checkHibernation();
            }
            catch (Exception e) {
                throw new NestedException("problem checking hibernation status", e);
            }
            return;
        }
        if (line.startsWith("open ")) {
            String toOpen = line.substring(5);
            SturdyRefFileImporter importer = myRegistrar.getSturdyRefFileImporter(myEnv);
            SturdyRef ref;
            try {
                ref = importer.importRef(toOpen);
            }
            catch (Exception e) {
                throw new NestedException("problem importing reference " + toOpen, e);
            }
            myRef = ref;
            return;
        }

        if (myRef == null) {
            EStdio.err().println("You must 'open <SturdyRefFile>' first");
            return;
        }
        
        etry {
            try {
                HelloTalker otherGuy;
                myRef.followRef(&otherGuy);
                EStdio.out().println("sending hello(" + line + ")");
                otherGuy <- hello(line);
            } catch (Throwable t) {
                EStdio.err().println("caught exception in lookup " +
                                     t.getMessage());
                EStdio.reportException(t);
            }
        } ecatch (RegistrarLookupEException e) {
            EStdio.err().println("caught E exception in lookup "
                                 + e.getMessage());
        }
    }

    local void noticeCompletion(Object arg) {
        EStdio.out().println("hibernating...");
        TimeMachine tm;
        try {
            tm = TimeMachine.summon(myEnv);
        }
        catch (Exception e) {
            throw new NestedException("couldn't make a TimeMachine", e);
        }
        tm <- hibernate(this, 0);
    }

    emethod noticeQuake(TimeQuake q) {
    }

    emethod noticeCommit() {
        EStdio.out().println("reviving from hibernation");
        try {
            myRegistrar.reviveFromHibernation();
        }
        catch (Exception e) {
            throw new NestedException("problem reviving from hibernation", e);
        }
    }

    emethod hello(String s) {
        EStdio.out().println("Hola Mundo <<" + s + ">>");
    }
}
