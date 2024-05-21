package ec.jwhich;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;


/**
 *
 */
/*package*/ class ZipFQName extends LocatedFQName {
    
    /*package*/ ZipFQName(String dir, String fQName) {
        super(dir, fQName);
    }
    
    public int existence() throws IOException {
        File file = new File(location());
        if (! file.exists()) {
            return DOESNT_EXIST;
        }
        ZipFile zipFile = new ZipFile(file);        
        String name = fullyQualifiedName().replace('.', '/');
        if (name.length() == 0) {
            return IS_PACKAGE;
        }
        String dirName = name + "/";
        ZipEntry dirEnt = zipFile.getEntry(dirName);
        String className = name + ".class";
        ZipEntry classEnt = zipFile.getEntry(className);

        if (dirEnt != null && dirEnt.isDirectory()) {
            if (classEnt != null) {
                throw new IOException("apparently both package and class");
            } else {
                return IS_PACKAGE;
            }
        } else {
            if (classEnt != null) {
                return IS_CLASSFILE;
            } else {
                return DOESNT_EXIST;
            }
        }
    }
    
    public Hashtable members() throws IOException {
        Hashtable result = new Hashtable();
        ZipFile zipFile = new ZipFile(location());
        String prefix = fullyQualifiedName().replace('.', '/');
        if (prefix.length() > 0) {
            prefix += "/";
        }
        int prelen = prefix.length();
        for (Enumeration iter = zipFile.entries(); iter.hasMoreElements(); ) {
            ZipEntry ze = (ZipEntry)iter.nextElement();
            String name = ze.getName();
            if (! name.startsWith(prefix)) {
                continue;
            }
            name = name.substring(prelen);
            if (name.endsWith("/")) {
                name = name.substring(0, name.length() -1);
            } else if (name.endsWith(".class")) {
                name = name.substring(0, name.length() - ".class".length());
            }
            if (isJavaIdentifier(name)) {
                LocatedFQName lfqn = get(name);
                if (lfqn.existence() != DOESNT_EXIST) {
                    if (result.put(name, lfqn) != null) {
                        throw new Error("duplicate " + lfqn);
                    }
                }
            }
        }
        return result;
    }    
    
    public InputStream contents() throws IOException {
        ZipFile zipFile = new ZipFile(location());
        String className = fullyQualifiedName().replace('.', '/') + ".class";
        ZipEntry classEnt = zipFile.getEntry(className);
        return zipFile.getInputStream(classEnt);
    }
}

