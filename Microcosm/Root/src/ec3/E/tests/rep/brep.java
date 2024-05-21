package ec.tests.rep;

import ec.ifc.app.*;
import ec.e.file.EStdio;
import java.io.IOException;
import java.util.Enumeration;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.rep.steward.Repository;
import java.io.File;

/*

  This tests only the most basic Reppository functionality:
  Open Repository, Write an object, close rep.
  ReOpen rep as read-only, read object, close rep.
  Compare results.
  For simplicity we use string keys, not CryptoHashes.

  To run this test, type to a shell:
  java ec.e.start.EBoot ec.tests.rep.brep 
  If you want an inspector (under windows)
  add Inspector=stop to the end of that line

  */

eclass brep implements ELaunchable {
    emethod go(EEnvironment env) {
        BasicRepositoryTester tester = new BasicRepositoryTester(env);
        tester <- go();
    }
}

eclass BasicRepositoryTester {
    private EEnvironment myEnv;

    public BasicRepositoryTester(EEnvironment env) {
        myEnv = env;
    }

    private static void dumpKeys(Repository rep) {
        Enumeration e = rep.keys();
        System.out.println("Dump of keys in " + rep.toString());
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            System.out.println("Key: " + key);
        }
    }

    private static void dumpRepository(Repository rep) {
        System.out.println("Dump of keys and values in " + rep.toString());
        Enumeration e = rep.keys();
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            Object value = rep.get(key);
            if (value == null) value = "null";
            System.out.println("Key: " + key + " = " + value);
        }
    }

    emethod go() {
        try {
            Object testObject = "Test String";
            File repFile = new File("testRepository");
            Repository rep = new Repository(repFile,true);
            rep.put("test",testObject);
            dumpKeys(rep);
            dumpRepository(rep);
            rep.close();

            File repFile2 = new File("testRepository");
            Repository rep2 = new Repository(repFile2,false);
            System.out.println("After re-opening repository");
            dumpKeys(rep2);
            dumpRepository(rep2);
            Object testResult = rep2.get("test");
            rep2.close();
            
            if (testObject.equals(testResult)) {
                EStdio.err().println("[SUCCESS] OBJREQUL - Objects are equal");
            } else {
                EStdio.err().println("[FAILURE] OBJRDIFF - Objects are different - result is " +
                                   testResult);
            }
        } catch (Exception e) {
            EStdio.err().println("[FAILURE] UNXTHROW - Unexpected throw:" + e);
            e.printStackTrace(EStdio.err());
        }
    }
}
