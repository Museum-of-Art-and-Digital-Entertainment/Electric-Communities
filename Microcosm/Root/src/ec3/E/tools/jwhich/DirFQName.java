package ec.jwhich;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Enumeration;


/**
 *
 */
/*package*/ class DirFQName extends LocatedFQName {
    
    /*package*/ DirFQName(String dir, String fQName) {
        super(dir, fQName);
    }
    
    private String baseName() {
        return join(location(), "/", fullyQualifiedName().replace('.', '/'));
    }
    
    public int existence() throws IOException {
        String baseName = baseName();
        File dir = new File(baseName);
        File classFile = new File(baseName + ".class");
        if (dir.isDirectory()) {
            if (classFile.isFile()) {
                throw new IOException("apparently " + baseName 
                                      + " is a package and a class");
            } else {
                return IS_PACKAGE;
            }
        } else {
            if (classFile.isFile()) {
                return IS_CLASSFILE;
            } else {
                return DOESNT_EXIST;
            }
        }
    }
        
    public Hashtable members() throws IOException {
        File dir = new File(baseName());
        String[] names = dir.list();
        Hashtable result = new Hashtable();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (name.toLowerCase().endsWith(".class")) {
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
        File classFile = new File(baseName() + ".class");
        return new FileInputStream(classFile);
    }
}

