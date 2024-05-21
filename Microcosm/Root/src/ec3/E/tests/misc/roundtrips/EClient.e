import java.io.IOException;
import java.util.Properties;

import java.util.Hashtable;
import java.util.Random;

import java.text.DecimalFormat;

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
import ec.util.Native;

public eclass EClient implements ELaunchable
{
    EEnvironment myEnv;
    long totalBytes =0;
    long totalRTTime = 0;
    double bps = 0.0;

    DecimalFormat myBPSFormat = new DecimalFormat("000.000");
    DecimalFormat myBFormat = new DecimalFormat("00000");
    DecimalFormat myTimeFormat = new DecimalFormat("000000");
    int BUFFER_SIZE = (64 * 1024);
    EServer myServer = null;
    Random myRandom = new Random(12345678);
    int numMessages = 100;
    int numRcvd = 0;
    int sendNum = 0;


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
        EEditableFile tFile = null;
        String myRefName = "EClient.ref";
        String targetName = "EServer.ref";
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
          tFile = currentDir.lookupFile(targetName);
        } catch (IOException e) {
          EStdio.out().println("Couldn't lookup: " + targetName);
          env.vat().exit(9); 
        }


        EServer server;
        long start = 0;
        try { 
            SturdyRefFileExporter exporter = registrar.getSturdyRefFileExporter(myEnv);
            SturdyRefFileImporter importer = registrar.getSturdyRefFileImporter(myEnv);
            SturdyRefMaker refMaker = registrar.getSturdyRefMaker();
            SturdyRef myRef = refMaker.makeSturdyRef(this);
            Exception lastException = null;
            SturdyRef targetRef;
   
            EStdio.out().println("Resolving Servers's SturdyRef..."); 
            try { // snarf the receptionist sturdyref
                exporter.exportRef(myRef, recFile);
                targetRef = importer.importRef(tFile.asReadableFile());
                start = Native.queryTimer();
                targetRef.followRef(&server);
            }
            catch (Exception e) {
               EStdio.out().println("Bad thing");
            }
    
        } catch (Throwable t) {
          EStdio.out().println("Couldn't export to " + myRefName);
          env.vat().exit(9); 
        }

        ewhen server (EServer s) {
          long delta = Native.deltaTimerUSec(start);
          EStdio.out().println("Got it (took " + delta + " uSecs). Starting.");
          s <- recvClient (this);
          myServer = s;
          sendMessage();
        }
    }

    void sendMessage() {
        int numBytes = (int)(myRandom.nextFloat() * BUFFER_SIZE);
        totalBytes += numBytes;
        myServer <- recv(this, new Message(numBytes, sendNum));
        sendNum++;
    }

    emethod recv(Message msg) {
        long now = Native.queryTimer();
        long deltaRT = 0;

        //System.out.println("Client recvd: " + numRcvd + ", size: " + msg.mySize);        
        numRcvd++;

        deltaRT = now - msg.startTime;
        bps = (double) msg.mySize/ (double) (deltaRT);
        EStdio.out().println("RT: " + msg.myNumber + ": " + (deltaRT) + 
                             " (" + myBPSFormat.format(bps) + ")" );
        totalRTTime += deltaRT;

        if (numRcvd >= numMessages) {
            EStdio.out().println("Num Messages                    : " + numRcvd);
            EStdio.out().println("Total Bytes (wo/hashtable) Sent : " + totalBytes);
            EStdio.out().println("Total Round Trip Time           : " + totalRTTime);
            EStdio.out().println("Ave RT uSecs/msg                : " + 
                                  ((double)totalRTTime) / (double) numRcvd);
            EStdio.out().println("Ave RT msgs/sec                : " + 
                     myBPSFormat.format(1000.0 * ((double)numRcvd) / (double) totalRTTime));
//            System.exit(1);
       } else {
            sendMessage();
       }
   }
}    
