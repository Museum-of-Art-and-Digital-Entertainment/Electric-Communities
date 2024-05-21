
package ec.tests.loader;

import java.util.*;
import java.io.*;

//import ec.e.comm.*;

import ec.eload.LoaderManager;
import ec.eload.ClassInfo;
import ec.eload.RtClassInfo;
import ec.eload.ldRequester;
import ec.eload.ECLoader;
import ec.eload.ClassManager;
import ec.eload.RtClassEnvironment;

import ec.e.start.EBoot;
import ec.e.start.ELaunchable;
import ec.e.cap.EEnvironment;


//import ec.e.comm.*;
//import ec.e.stream.*;
//import ec.e.db.*;
//import ec.eload.*;

public class Starter {
    public void main (String args[]) {
        //ClassLoader cl = ECLoader.getClassLoader();
        //cl.loadClass("ec.tests.loader.LoaderTest2", true);
        //LoaderTest2.main(args);
    }
}

public class LoaderTest2 implements ELaunchable
{
    static ldRequester ecloader;
    static  Vector classInfos = new Vector();
    static Vector certificates = null;
    private static TestClassEnvironment2 classEnvironment;


    public void go(EEnvironment env) {
        if (env != null) {
                String [] args = env.getArgs();
            if (args.length > 0) {
                TestClassManager.PathRoot = args[0];
            } else
                    TestClassManager.PathRoot = "./";

        }
        else {
            TestClassManager.PathRoot = "./";
        }

        String className = null;
        // className = "ec.tests.loader.TestLocalClass2";

        classEnvironment = new TestClassEnvironment2();
        classEnvironment.put("java");
        classEnvironment.put("ec.e");
        classEnvironment.put("ec.auth");
        classEnvironment.put("ec.clbless");
        classEnvironment.put("ec.crypt");
        classEnvironment.put("ec.eload");
        classEnvironment.put("ec.util");

        setupClassManager();
        try {
            //Class tlc = ClassCache.forName(className);
        } catch (Exception e) {
            System.out.println("Couldn't get Class for TestLocalClass2");
            e.printStackTrace();
        }
        try {
                TestLocalClass2 lc = new TestLocalClass2();
                className = lc.getClass().getName();
                System.out.println("Created " + lc);
                System.out.println("And it's class is " + className);
        } catch (Exception e) {
            System.out.println("Couldn't create TestLocalClass2");
            e.printStackTrace();
            System.exit(0);
        }
        classInfos.addElement(new ClassInfo(className, null));
        try {
            Hashtable refTable = ecloader.getAllReferencedClasses(classInfos, classEnvironment);
            classInfos = new Vector(); // Clear it since we'll stuff the hashtable's elements in
            Enumeration en = refTable.elements();
            while (en.hasMoreElements()) {
                classInfos.addElement(en.nextElement());
            }
        } catch (Exception e) {
            System.out.println("Exception getting referenced classes");
            e.printStackTrace();
        }
        classInfos = checkReferencedClasses(classInfos);
        try {
            certificates = ECLoader.getCertificates(classInfos);
        } catch (Exception e) {
            System.out.println("Exception getting certificates");
            e.printStackTrace();
        }
        System.out.println("Got certificates, adding them");
        try {
            ECLoader.addCertificates(certificates);
        } catch (Exception e) {
            System.out.println("Exception adding certificates");
            e.printStackTrace();
        }

        try {
            classInfos = new Vector(1);
            classInfos.addElement(new ClassInfo(className, null));
            classInfos = ecloader.getClasses(classInfos);
        } catch (Exception e) {
            System.out.println("Exception getting classes");
            e.printStackTrace();
        }
        dumpClassInfos(classInfos);
    }

    static void dumpClassInfos (Vector classInfos) {
        int i;
        int size;
        String classString = "Classes:";

        if (classInfos == null) {
            System.out.println("DumpClassInfos: No classes, the info vector was null");
            return;
        }
        if (classInfos.size() == 0) {
            System.out.println("DumpClassInfos: No classes, empty info vector");
            return;
        }
        for (i = 0, size = classInfos.size(); i < size; i++) {
            ClassInfo classInfo = (ClassInfo)classInfos.elementAt(i);
            classString = classString + " " + classInfo.getName();
        }
        System.out.println(classString);
    }

    static Vector checkReferencedClasses (Vector classInfos) {
        int i;
        if (classInfos == null) {
            System.out.println("CheckReferencedClasses: No classes, the info vector was null");
            return classInfos;
        }
        if (classInfos.size() == 0) {
            System.out.println("CheckReferencedClasses: No classes, empty info vector");
            return classInfos;
        }
        for (i = 0; i < classInfos.size(); ) {
            ClassInfo classInfo = (ClassInfo)classInfos.elementAt(i);
            try {
                System.out.println("CheckReferencedClasses for " +
                    classInfo.getName() + ", hash " + classInfo.getHash());
                    if (ecloader.loadClass(classInfo.getName())
                        != null)
                    {
                    // This changes classInfos.size() -- see above
                    System.out.println("Already have " + classInfo.getName());
                    classInfos.removeElementAt(i);
                    continue;
                }
            } catch (Exception e) {
                System.out.println("Exception checking for class " + classInfo.getName());
                e.printStackTrace();
            }
            i++;
        }
        return classInfos;
    }

    static void setupClassManager () {
        ecloader = ECLoader.getRequester();
        try {
            LoaderManager.registerClassManager(TestClassManager.theClassManager);
        } catch (Exception e) {
            System.out.println("Error registering class loader");
            e.printStackTrace();
        }
    }
}

class TestClassEnvironment2 implements RtClassEnvironment
{
    private Hashtable paths = new Hashtable();
    private Hashtable cache = new Hashtable();

    public void put (String path) {
        paths.put(path, path);
    }

    public void remove (String path) {
        paths.remove(path);
        cache.clear();
    }

    public boolean contains (String path) {
        if (cache.contains(path)) {
            return true;
        }
        if (paths.contains(path)) {
            return true;
        }
        String pack;
        int index = 0;
        while ((index = path.indexOf('.', index)) > 0) {
            pack = path.substring(0, index++);
            if (paths.contains(pack)) {
                cache.put(path, path);
                return true;
            }
        }
        return false;
    }
}

interface TestLocalSuperInterface2 {
}

class TestLocalSuperSuperClass2 extends Hashtable {
}

class TestLocalSuperClass2 extends TestLocalSuperSuperClass2 implements TestLocalSuperInterface2
{
}

interface TestLocalInterface2 {
}

class TestLocalClass2 extends TestLocalSuperClass2 implements TestLocalInterface2
{
    String string = "This is the test local class string";

    public void whatever () {
    }
}

