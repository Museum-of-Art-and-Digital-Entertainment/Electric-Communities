// Copyright 1997 Electric Communities. All rights reserved worldwide.

package ec.tools.upgrade;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import ec.transform.classparser.ClassFile;

/*
 inheriters [-path <directories/zipFiles>] <className>

 Outputs all the classes which inherit/implement <className> in any of the
 <directories/zipFiles> in the path list.  If -path is not specified, the 
 current CLASSPATH will be used.  If the path has more than one copy of a
 class, only the first class in the path will be scanned.
 */
public class InheritersCmd {

    static public void main(String[] args) throws IOException {

        String path = null;
        String className = null;

        for (int i=0; i<args.length; i++) {
            if ("-path".equals(args[i])){
                i++;
                if (args.length == i) {
                    System.err.println("Usage: java ec.upgrade.InheritersCmd "
                            +"[-path <classPath>] <className>");
                    System.exit(1);
                }
                path = args[i];;
            } else {
                className = args[i];
            }
        }
        if (null == className) {
            System.err.println("Usage: java ec.upgrade.InheritersCmd "
                                +"[-path <classPath>] <className>");
            System.exit(1);
        }
        if (null == path) {
            path = System.getProperty("java.class.path");
        }
        Inheriters scanner = new Inheriters(path);

        Vector inheritsFrom = scanner.inheriters(className);
        System.out.println(className+" is inherited by:");
        for (int i=0; i<inheritsFrom.size(); i++) {
            String name = (String)inheritsFrom.elementAt(i);
            System.out.println("  "+name);
        }

        Vector undefined = scanner.undefinedClasses();
        if (null != undefined) {
            System.out.println("\nThe following classes were not found in the path:");
            for (int i=0; i<undefined.size(); i++) {
                String name = (String)undefined.elementAt(i);
                System.out.println("  "+name);
            }
        }
    }
}