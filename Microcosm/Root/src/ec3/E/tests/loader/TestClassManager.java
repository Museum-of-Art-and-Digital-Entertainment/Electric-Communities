
package ec.tests.loader;

import java.util.*;
import java.io.*;

import ec.util.zip.*;
import ec.eload.LoaderManager;
import ec.eload.ClassInfo;
import ec.eload.RtClassInfo;
import ec.eload.ldRequester;
import ec.eload.ECLoader;
import ec.eload.ClassManager;

class TestClassManager implements ClassManager
{
  static TestClassManager theClassManager = new TestClassManager();
  static String PathRoot = "";
	
  public RtClassInfo doLoadClass(String className) {
    //return getClassBytes(className);
    return null;
  }
    
  public RtClassInfo doLoadClass(ClassInfo classInfo) {
    //return getClassBytes(classInfo);
    return null;
  }
	
  public RtClassInfo checkForClass(String className) {
    ClassInfo info = new ClassInfo(className, null);
    return checkForClass(info);
  }

  public RtClassInfo checkForClass(ClassInfo classInfo) {
    System.out.println("CheckForClass: called for " + classInfo.getName());
    try {
      if (RtZipFile.isZipFile(PathRoot)) {
	RtZipFile zipFile = new RtZipFile(PathRoot);
	String className = classInfo.getName();
	String fileName = 
	  fileNameFromClassName(className)  + ".class";
	RtZipElement zipElement = zipFile.find(fileName);
	if (zipElement != null) {
	  System.out.println("Can read class  " + fileName + 
			     " from zip file " + PathRoot);
	  return new RtClassInfo(classInfo);
	} else {
	  System.out.println(
			     "Can *not* read class " + fileName + 
			     " from zip file " + PathRoot );
	  return null;
	}
      } else {
	String baseName = fileNameFromPackageName(classInfo.getName());
	String className = baseName + ".class";
	System.out.println("CheckForClass: looking for " + className);
	File classFile = new File(className);
		
	if (classFile.canRead()) {
	  System.out.println("Can read class " + className);
	  return new RtClassInfo(classInfo);
	}
	else {
	  System.out.println("Can *not* read class " + className);
	  return null;
	}
      }
    } catch (Throwable e) {
      e.printStackTrace();
      return null;
    }
  }

  public RtClassInfo getClassBytes(String className) {
    ClassInfo info = new ClassInfo(className, null);
    return getClassBytes(info);
  }
	
  public RtClassInfo getClassBytes(ClassInfo classInfo) {
    System.out.println("GetClass: called for " + classInfo.getName());
    if (checkForClass(classInfo) == null) return null;
    RtClassInfo info = null;
    try {
      if (RtZipFile.isZipFile(PathRoot)) {
	RtZipFile zipFile = new RtZipFile(PathRoot);
	String className = classInfo.getName();
	String fileName = 
	  fileNameFromClassName(className) + ".class";
	RtZipElement zipElement = zipFile.find(fileName);
	if (zipElement != null) {
	  info = new RtClassInfo(classInfo, new byte [zipElement.getSize()]);
	  zipElement.get(info.getClassBytes());
	} else {
	  System.out.println(
			     "Can *not* get class " + fileName +
			     " from zip file " + PathRoot);
	  return null;
	}
      } else {
	String className = 
	  fileNameFromPackageName(classInfo.getName()) + ".class";
	try {
	  File file = new File(className);
	  FileInputStream stream = new FileInputStream(file);
	  int length = (int)file.length();
	  info = new RtClassInfo(classInfo, new byte [length]);
	  stream.read(info.getClassBytes());
	} catch (Exception e) {
	  System.out.println("Error reading bytes for class " + classInfo.getName());
	  e.printStackTrace();
	  return null;
	}
      }
    } catch (Throwable e) {
      e.printStackTrace();
      return null;
    }
    return info;
  }
	
  private String fileNameFromPackageName (String packageName) {
    String fileName;
    fileName = packageName.replace('.', File.separatorChar);
    return PathRoot + File.separatorChar + fileName;
  }
  private String fileNameFromClassName (String className) {
    String fileName;
    fileName = className.replace('.', File.separatorChar);
    return fileName;
  }
}

