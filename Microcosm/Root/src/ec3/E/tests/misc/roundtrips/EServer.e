import java.io.IOException;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;
import java.lang.ClassNotFoundException;

import ec.e.start.ELaunchable;
import ec.e.lang.EString;
import ec.e.start.EEnvironment;
import ec.e.start.Vat;
import ec.e.file.EStdio;
import ec.e.file.EEditableDirectory;
import ec.e.file.EDirectoryRootMaker;
import ec.e.file.EEditableFile;
import ec.e.net.SturdyRef;
import ec.e.net.SturdyRefMaker;
import ec.e.net.SturdyRefExporter;
import ec.e.net.SturdyRefImporter;
import ec.e.net.SturdyRefFileImporter;
import ec.e.net.SturdyRefFileExporter;
import ec.e.net.Registrar;
import ec.e.net.RegistrarException;
import ec.e.net.RegistrarLookupEException;
import ec.e.net.InvalidURLException;

import ec.util.NestedError;

import ec.cosm.objects.ukAvatarReceptionist$kind;
import ec.cosm.objects.eeBadTeleportPadException;
import ec.cosm.objects.eeException;
import ec.pl.runtime.Unum;

public eclass EServer implements ELaunchable
{
    EEnvironment myEnv;
    EClient myClient = null;

    EServer value() {
      return this;
    }

    emethod go(EEnvironment env) {

        Registrar registrar = null;
        EDirectoryRootMaker directoryMaker = null;
        myEnv = env;

        try {
          registrar = Registrar.summon(env);
          directoryMaker = EDirectoryRootMaker.summon(env);
        } catch (Exception any) {
          throw new Error("Couldn't summon helpers. Aborting. Exception:" + any);
        }

        try {
            registrar.onTheAir();
        }
        catch (RegistrarException e) {
            throw new Error("fatal registrar problem going on the air: " + e);
        }

        Properties recProps = new Properties(); // receptionist props
        EEditableDirectory currentDir =
            directoryMaker.makeDirectoryRoot(".");
        String recEARL = null;


        EEditableFile recFile = null;
        String myRefName = "EServer.ref";
        try {
          recFile = currentDir.lookupFile(myRefName);
        } catch (IOException e) {
          try {          
            recFile = currentDir.mkfile(myRefName);
          }
          catch (IOException e2) {
            EStdio.out().println("Couldn't lookup or create " + myRefName);
            env.vat().exit(9); 
          }
        }

        try { 
            SturdyRefFileExporter exporter = registrar.getSturdyRefFileExporter(myEnv);
            SturdyRefMaker refMaker = registrar.getSturdyRefMaker();
            SturdyRef myRef = refMaker.makeSturdyRef(this);
            Exception lastException = null;
    
            try { // snarf the receptionist sturdyref
                exporter.exportRef(myRef, recFile);
            }
            catch (Exception e) {
                throw new NestedError("Problem exporting receptionist from: " +
                                      myRefName, e);
            }
    
        } catch (Throwable t) {
          EStdio.out().println("Couldn't export to " + myRefName);
          env.vat().exit(9); 
        }
        EStdio.out().println("EServer running...");
    }

    emethod recvClient(EClient client) {
        EStdio.out().println("Recv'd EClient");
        myClient = client;
    }

    emethod recv(Message  m) {
       System.out.println("Server bouncing message: " + m.myNumber);
       myClient <- recv(m);
    }

    emethod recv(EClient client, Message  m) {
       System.out.println("Server bouncing message: " + m.myNumber);
       client <- recv(m);
    }
}    
