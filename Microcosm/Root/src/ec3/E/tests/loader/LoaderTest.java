
package ec.tests.loader;

import java.util.*;
import java.io.*;

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


public class LoaderTest implements ELaunchable
{
	private static TestClassEnvironment classEnvironment;

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
		// className = "ec.tests.loader.TestLocalClass"; 

		classEnvironment = new TestClassEnvironment();
		classEnvironment.put("java");
        classEnvironment.put("ec.e");
        classEnvironment.put("ec.auth");
        classEnvironment.put("ec.clbless");
        classEnvironment.put("ec.crypt");
        classEnvironment.put("ec.eload");
        classEnvironment.put("ec.util");

		setupClassManager();
		RtClassInfo ci = new RtClassInfo("ec.tests.loader.TestLocalClass",
			(Class)null);
		ci = LoaderManager.loadRtClassInfo(ci);
		if (ci == null) {
		  System.out.println(
   "Class ec.tests.loader.TestLocalClass not found");
		  System.exit(1);
		}
		Class theClass = ci.getClassObj();
		theClass.getInterfaces();
		TestClassManager.theClassManager.getClassBytes(
				 "ec.tests.loader.TestLocalClass");
    }

	static void setupClassManager () {
		try {
			LoaderManager.registerClassManager(TestClassManager.theClassManager);
		} catch (Exception e) {
			System.out.println("Error registering class loader");
			e.printStackTrace();
		}
	}
}
class TestClassEnvironment implements RtClassEnvironment
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

interface TestLocalSuperInterface {
}

class TestLocalSuperSuperClass extends Hashtable {
}

class TestLocalSuperClass extends TestLocalSuperSuperClass implements TestLocalSuperInterface
{
}

interface TestLocalInterface {
}

class TestLocalClass extends TestLocalSuperClass implements TestLocalInterface
{
	String string = "This is the test local class string";

	public void whatever () {
	}
}

