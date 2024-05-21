package ec.e.rep.steward;

import ec.ifc.app.*;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import ec.e.start.EEnvironment;
import ec.e.start.crew.CrewCapabilities;
import ec.e.start.ELaunchable;
import ec.e.rep.steward.Repository;
import ec.e.rep.steward.RepositoryHandle;
import ec.util.NativeSteward;
import ec.tables.SimTable;

/*

  This tests Repository (retrieval) speed. We assume a Repository file
  exists as .CHome/Dist/Repository .  We time opening that Repository,
  including reading in all the keys.  We then read all objects in the
  Repository without decoding them and time that.  Next we read all
  objects from the Repository and decode them.

  We compute number of objects read per second and number of bytes per
  second, as appropriate, and average byte array size.

  The different tests are each separate methods so that we can change
  global parameters between tests and run them again in a suite. The
  suite is defined in go().

  Note: This test is in the ec.e.rep.steward package to gain access to
  some package variables. This is unusual, all other tests are in
  ec.tests.*

  We need the ECHome property and a ECHome dir and Dist subdir in that
  if our top-level repository contains DataHolders (and most do).

  To run this test, type to a shell:
  java ec.e.start.EBoot ec.e.rep.steward.rept ECHome=ECHome -ECproperties rept.props

    in a directory where you both have an ./ECHome/Dist subdir with a
  Repository and a rept.props file similar to rept.props in
  src/ec3/E/tests/rep/rept.props

  As a convenienence, a file named rept.bad in this dir contains the
  above line so all you really need to do is to cd to
  /src/ec3/e/tests/rep, setup ECHome/Dist/Repository and then type
  "rept"

 */

eclass rept implements ELaunchable {
    emethod go(EEnvironment env) {
        RepositoryTimingTester tester = new RepositoryTimingTester(env);
        tester <- go();
    }
}

eclass RepositoryTimingTester {
    private EEnvironment myEnv;
    private Repository theRep;
    private Hashtable parimeters = new Hashtable(10);

    public RepositoryTimingTester(EEnvironment env) {
        myEnv = env;
    }

    private void dumpKeys(Repository rep) {
        Enumeration e = rep.keys();
        System.out.println("Dump of keys in " + rep.toString());
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            System.out.println("Key: " + key);
        }
    }

    private void timeReadingAllObjects(Enumeration keys, SuperRepository rep) throws IOException {
        long nrKeys = 0;
        long nrBytes = 0;
        long startTime = NativeSteward.queryTimer();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if ("%SymbolTable%".equals(key)) continue; // Ignore a bad case
            nrKeys++;
            Object o = rep.get(key,parimeters);
        }
        long objectReadTime = NativeSteward.deltaTimerMSec(startTime);
        System.out.println("Read " + nrKeys + " decoded objects in " +
                           objectReadTime + " ms -> " +
                           (float)((1000.0 * nrKeys) / objectReadTime) + " obj/s");

    }

    private void timeReadingAllAsBytes(Enumeration keys, Repository rep) throws IOException {
        long nrKeys = 0;
        long nrBytes = 0;

        long startTime = NativeSteward.queryTimer();
        
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if ("%SymbolTable%".equals(key)) continue; // Ignore a bad case
            RepositoryHandle handle = rep.getHandle(key);
            if (handle == null) System.out.println("Key " + key + " returned no handle");
            else {
                byte[] b = handle.getDataBytes(key);
                nrKeys++;
                if (b == null) System.out.println("Key " + key + " returned no data bytes");
                else {
                    nrBytes += b.length; // Accumulate to total length of data
                }
            }
        }
        long byteReadTime = NativeSteward.deltaTimerMSec(startTime);
        System.out.println("Read " + nrKeys + " objects without decoding in " +
                           byteReadTime + " ms - Average length was " + (nrBytes / nrKeys) +
                           " bytes -> " + (float)(nrBytes / (byteReadTime * 1000.0)) + " Mb/s");

    }

    private void dumpRepository(Repository rep) throws IOException {
        System.out.println("Dump of keys and values in " + rep.toString());
        Enumeration e = rep.keys();
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            if ("%SymbolTable%".equals(key)) continue; // Ignore a bad case
            Object value = rep.get(key,parimeters);
            if (value == null) value = "null";
            System.out.println("Key: " + key + " = " + value);
        }
    }

    private void timeSimtableVersusHashtableLookups(Enumeration givenKeys, Repository rep) 
      throws IOException {
        Vector keys = new Vector(4000);
        SimTable sim;
        Hashtable hash;
        
        // Copy enumeration to a vector so we can use it several times

        while (givenKeys.hasMoreElements()) {
            Object key = givenKeys.nextElement();
            if ("%SymbolTable%".equals(key)) continue; // Ignore a bad case
            keys.addElement(key);
        }

        sim = new SimTable(2 * keys.size());
        hash = new Hashtable(2 * keys.size());

        Enumeration e = keys.elements();
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            Object handle = rep.getHandle(key); // This mimics real-life usage
            sim.put(key,handle);
            hash.put(key,handle);
        }
            
        e = keys.elements();
        Object value;

        long startTime = NativeSteward.queryTimer();
        while (e.hasMoreElements()) value = sim.get(e.nextElement());
        long simTime = NativeSteward.deltaTimerUSec(startTime);
        
        e = keys.elements();
        startTime = NativeSteward.queryTimer();
        while (e.hasMoreElements()) value = hash.get(e.nextElement());
        long hashTime = NativeSteward.deltaTimerUSec(startTime);
        
        System.out.println("Table lookup of " + keys.size() + " keys: SimTable: " + 
                           simTime + " us - Hashtable: " + hashTime + " us");
    }


    emethod go() {

        // Turn on some timing flags unconditionally

        Repository.myRepositoryTiming.debug = true;
        //        RepositoryFile.myRepositoryByteCache.debug = true;

        try {
            long startTime = NativeSteward.queryTimer();
            SuperRepository theSuper =
              (SuperRepository)CrewCapabilities.getTheSuperRepository();
            long startupTime = NativeSteward.deltaTimerMSec(startTime);

            System.out.println("SuperRepository started in " + startupTime + " ms");

            // Dump other startup time statistics
            Repository.dumpTimers();

            // We want to get a second handle to the Repository file
            // in ECHome/Dist so we can sneak a peek at the keys in
            // it. We do most actual timing reading through the
            // SuperRepository, but this is hard to do perfectly since
            // the getHandle() method is not available through
            // SuperRepository so we have to use the regular
            // repository for timeReadingAllAsBytes();

            File repFile = new File("ECHome/Dist/Repository");
            theRep = new Repository(repFile,false); // false -> Open it read-only

            parimeters.put("Repository",theSuper); // Create a parimeter table

            timeReadingAllAsBytes(theRep.keys(), theRep);
            timeReadingAllObjects(theRep.keys(), theSuper);
            timeSimtableVersusHashtableLookups(theRep.keys(), theRep);

            Repository.dumpTimers();
            theRep.close();
            System.exit(0);
        } catch (Exception e) {
            System.out.println("[FAILURE] UNXTHROW - Unexpected throw:" + e);
            e.printStackTrace();
        }
    }
}
