package ec.e.net;

import ec.eload.ClassInfo;
import ec.eload.ClassManager;
import ec.eload.RtClassInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Vector;

class RtRemoteClassManager implements ClassManager {
/*  static RtRemoteClassManager theClassManager = new RtRemoteClassManager();*/
    static String directoryRoot = null;
    static Trace tr = new Trace(false, "[RtRemoteClassManager]");;
    static Hashtable cache = new Hashtable();
    
    static {
        String home = System.getProperty("user.home");
        if (home == null)
            home = ".";
        directoryRoot = home + File.separatorChar + ".ec" +
            File.separatorChar + "classes";
    }
    
    public RtClassInfo doLoadClass(String className) {
        /*if (tr.tracing) tr.$("Calling getClassBytes for " + className); */
        /*    return(getClassBytes(className)); */
        return(null);
    }
    
    public RtClassInfo doLoadClass(ClassInfo classInfo) {
        /*if (tr.tracing) tr.$("Calling getClassBytes for " + classInfo); */
        /*    return(getClassBytes(classInfo)); */
        return(null);
    }
    
    /* *** Yay! We don't have to be a class loader anymore ...
    public synchronized Class loadClass(String name, boolean resolve) {
        Class c;
        byte bytes[] = null;
        
        try {
            if (tr.tracing)
                tr.$("Trying forName for class " + name);
            c = Class.forName(name);
            if (c != null) {
                if (tr.tracing)
                    tr.$("Found class " + name + " using forName");
                return(c);
            }
        } catch (Exception e) {
            if (tr.tracing)
                tr.$("Exception getting class using forName");
        }
        c = (Class) cache.get(name);
        if (c == null) {
            if (tr.tracing)
                tr.$("Trying to get info for class " + name);
            RtClassInfo info = getClassBytes(name);
            if (info == null) {
                if (tr.tracing)
                    tr.$("No info for class " + name);
                return(null);
            }
            if (tr.tracing)
                tr.$("Putting class " + name + " into loader cache in memory");
            bytes = info.getClassBytes();
            if (tr.tracing)
                tr.$("Bytes at " + bytes + " length " + bytes.length);
            cache.put(name, c = defineClass(bytes, 0, bytes.length));
        }
        if (resolve == true)
            resolveClass(c); 
        return(c);
    }
    */
    
    public RtClassInfo checkForClass(String className) {
        ClassInfo info = new ClassInfo(className, null);
        return(checkForClass(info));
    }
    
    public RtClassInfo checkForClass(ClassInfo classInfo) {
        String baseName = fileNameFromPackageName(classInfo.getName());
        String hashName = baseName + ".hash";
        String className = baseName + ".class";
        File classFile = new File(className);
        byte hash[] = null;
        
        if (tr.tracing)
            tr.$("Called for " + className);
        if (tr.tracing)
            tr.printStackTrace();   
        if (classFile.canRead()) {
            if (tr.tracing)
                tr.$("Found class file for " + className);
            if (classInfo.getHash() != null) {
                if (tr.tracing)
                    tr.$("Looking for hash for " + className);
                try {
                    File file = new File(hashName);
                    FileInputStream stream = new FileInputStream(file);
                    int length = (int)file.length();
                    hash = new byte[length];
                    stream.read(hash);
                } catch (Exception e) {
                    /* XXX bad exception usage -- fix */
                    if (tr.tracing)
                        tr.$("Error reading hash for class " +
                             classInfo.getName());
                    e.printStackTrace();
                    return(null);
                }
                if (hash.equals(classInfo.getHash())) {
                    if (tr.tracing)
                        tr.$("Hash matches for " + className);
                    return(new RtClassInfo(classInfo));
                } else {
                    if (tr.tracing)
                        tr.$("Hash doesn't match for " + className);
                    return(null);
                }
            }
            if (tr.tracing)
                tr.$("Match on class " + className);
            return(new RtClassInfo(classInfo));
        }
        return(null);
    }
    
    public RtClassInfo getClassBytes(String className) {
        ClassInfo info = new ClassInfo(className, null);
        return(getClassBytes(info));
    }
    
    public RtClassInfo getClassBytes(ClassInfo classInfo) {
        if (checkForClass(classInfo) == null)
            return(null);
        String className =
            fileNameFromPackageName(classInfo.getName()) + ".class";
        if (tr.tracing)
            tr.$("Called for " + className);
        /* delay creation of RtClassInfo until all info is avail. ctg */
        RtClassInfo info = null;
        try {
            File file = new File(className);
            FileInputStream stream = new FileInputStream(file);
            int length = (int)file.length();
            byte classBytes[] = new byte [length];
            stream.read(classBytes);
            info = new RtClassInfo(classInfo, classBytes);
        } catch (Exception e) {
            /* XXX bad exception usage -- fix */
            if (tr.tracing)
                tr.$("Error reading bytes for class " + classInfo.getName());
            e.printStackTrace();
            return(null);
        }
        return(info);
    }
    
    void cacheClasses(Vector /* RtClassInfo */ classInfos) {
        if (tr.tracing)
            tr.$("Handed array of " + classInfos.size() + " classInfos");
        for (int i = 0, int size = classInfos.size(); i < size; i++) {
            try {
                RtClassInfo info = (RtClassInfo)classInfos.elementAt(i);
                if (tr.tracing)
                    tr.$("Trying to cache " + info.getName());
                String name = fileNameFromPackageName(info.getName());
                if (ensurePathForFile(name) == false) {
                    if (tr.tracing)
                        tr.$("Couldn't get full path for " + name);
                    continue;
                }
                FileOutputStream stream =
                    new FileOutputStream(name + ".class");
                stream.write(info.getClassBytes());
                stream.close();
                if (info.getHash() != null) {
                    if (tr.tracing)
                        tr.$("Trying to cache hash for " + info.getName());
                    stream = new FileOutputStream(name + ".hash");
                    stream.write(info.getHash());
                    stream.close();
                }
            } catch (Exception e) {
                /* XXX bad exception usage -- fix */
                System.out.println("Error occured writing class file");
                e.printStackTrace();
            }
        }
    }   
    
    private boolean ensurePathForFile(String path) {
        try {
            /* XXX - Strip off the file name, don't need a directory for
               that!!! (Which we make erroneously) */
            File dir = new File(path);
            if (dir.isDirectory() == true)
                return(true);
            if (dir.mkdirs() == true)
                return(true);
        } catch (Exception e) {
            /* XXX bad exception usage -- fix */
            System.out.println("Error trying to make directory " + path);
            e.printStackTrace();
        }
        return(false);
    }
    
    private String fileNameFromPackageName(String packageName) {
        String fileName = packageName.replace('.', File.separatorChar);
        return(directoryRoot + File.separatorChar + fileName);
    }
}
