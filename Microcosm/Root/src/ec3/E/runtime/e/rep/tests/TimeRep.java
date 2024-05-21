package ec.e.rep;

import ec.cosm.gui.appearance.InvalidTextFileException;
import ec.e.rep.steward.CertifiedCryptoHashBundle;
import ec.e.rep.steward.SimpleRepository;
import ec.e.rep.steward.RepositoryKeyNotFoundException;
import ec.e.rep.ParimeterizedRepository;
import ec.e.rep.steward.SuperRepository;
import ec.e.hold.Fulfiller;
import ec.util.PEHashtable;
import ec.e.file.EStdio;
import ec.e.start.*;
import java.io.*;
import ec.e.rep.steward.Repository;
import ec.cosm.tools.Parser;

import java.util.Enumeration;
import java.util.Hashtable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Vector;
import java.io.FileNotFoundException;
import java.io.IOException;

import ec.e.start.crew.CrewCapabilities;
import ec.e.run.OnceOnlyException;
import ec.e.run.TraceController;
import ec.e.util.crew.PropUtil;

import ec.e.net.Registrar;
import ec.e.net.RegistrarException;
import ec.e.net.SturdyRef;
import ec.e.net.SturdyRefFileImporter;
import ec.e.timer.Timer;

import ec.util.NativeSteward;

eclass TimeRep implements ELaunchable {
    public SuperRepository myRep = null;
    private PEHashtable myPutParimeters = new PEHashtable(40);
    private Hashtable myGetParimeters = new Hashtable(40);

    emethod go(EEnvironment env) {
        try {
            EStdio.out().println("Opening repository...");
            long stime = NativeSteward.queryTimer();
            myRep = (SuperRepository)CrewCapabilities.getTheSuperRepository();
            long time = NativeSteward.queryTimer();
            EStdio.out().println("   Done opening repository: "+((time-stime)/1000) + " ms");
            myGetParimeters.put("Repository",myRep);
            myPutParimeters.put(myRep, "Repository");
        } catch (IOException iox) {
            iox.printStackTrace(EStdio.err());
            System.exit(1);
        }
        doTiming();
        System.exit(0);
    }

    void doTiming() {
        EStdio.out().println("Loading SymbolTable...");
        long stime = NativeSteward.queryTimer();
        Hashtable stable = null;
        try {
            stable = (Hashtable)myRep.get("%SymbolTable%");
        } catch (RepositoryKeyNotFoundException rknfx) {
            EStdio.out().println("Could not get symbol table - key not found - exiting");
            rknfx.printStackTrace();
            System.exit(1);
        } catch (IOException iox) {
            EStdio.out().println("Could not get symbol table - exiting");
            iox.printStackTrace();
            System.exit(1);
        }
        long time = NativeSteward.queryTimer();
        if (stable == null) {
            EStdio.out().println("Could not get symbol table - exiting");
            System.exit(2);
        }
        EStdio.out().println("   Done loading SymbolTable: "+((time-stime)) + " us");
        EStdio.out().println("   Size: "+ stable.size());
        Enumeration keys = stable.keys();
        Vector bmps = new Vector();
        Vector apps2d = new Vector();
        Vector apps3d = new Vector();
        while (keys.hasMoreElements()) {
          String key = (String)keys.nextElement();
          Object o = stable.get(key); 
          if (o != null) {
            if (key.indexOf("bmp")>0) {
              bmps.addElement(o);
            } else if (key.indexOf("appearance2d")>0) {
              apps2d.addElement(o);
            } else if (key.indexOf("appearance3d")>0) {
              apps3d.addElement(o);
            }
          }
        }
        
        ec.e.rep.steward.Repository.dumpTimers
          ("After examining Repository contents and sorting according to type");

        keys = bmps.elements();
        long sz=0;
        EStdio.out().println("Loading BMPs ("+bmps.size()+")...");
        stime = NativeSteward.queryTimer();
        while (keys.hasMoreElements()) {
          try {
            Object o2 = myRep.get(keys.nextElement(), myGetParimeters);
          }
          catch (IOException ioe) {
            EStdio.out().println("IOException");
          }
        }
        time = NativeSteward.queryTimer();
        EStdio.out().println("   Done loading BMPs("+sz+"): "+
                             ((time-stime)/1000) + " mSec, "+
                             ((1000000.0*bmps.size())/(double)(time-stime)) + " obj/sec");

        keys = apps2d.elements();

        ec.e.rep.steward.Repository.dumpTimers("After reading all bitmap objects");

        EStdio.out().println("Loading apps2d Objects("+apps2d.size()+")...");
        stime = NativeSteward.queryTimer();
        while (keys.hasMoreElements()) {
          //          Object o1 = stable.get(keys.nextElement());
          try {Object o2 = myRep.get(keys.nextElement(), myGetParimeters);}
          catch (IOException ioe) {
            EStdio.out().println("IOException");
          }
        }
        time = NativeSteward.queryTimer();
        EStdio.out().println("   Done loading App2ds: "+
                             ((time-stime)/1000) + " mSec, " +
                             (1000000.0*apps2d.size()/(float)(time-stime)) + " obj/sec");
        
        ec.e.rep.steward.Repository.dumpTimers("After reading all App2D objects");
        keys = apps3d.elements();
        EStdio.out().println("Loading apps3d Objects("+apps3d.size()+")...");
        stime = NativeSteward.queryTimer();
        while (keys.hasMoreElements()) {
          //          Object o1 = stable.get(keys.nextElement());
          try {Object o2 = myRep.get(keys.nextElement(), myGetParimeters);}
          catch (IOException ioe) {
            EStdio.out().println("IOException");
          }
        }
        time = NativeSteward.queryTimer();
        EStdio.out().println("   Done loading App3ds: "+
                             ((time-stime)/1000) + " mSec, " +
                             (1000000.0*apps3d.size()/(float)(time-stime)) + " obj/sec");
        ec.e.rep.steward.Repository.dumpTimers("After reading all App3D objects");
    }
}

