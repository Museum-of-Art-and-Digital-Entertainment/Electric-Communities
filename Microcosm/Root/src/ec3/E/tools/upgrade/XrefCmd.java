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
 XrefCmd [-path <directories/zipFiles>]

 Outputs a cross reference of all the method/field references for all the
 classes defined in the
 <directories/zipFiles> in the path list.  -path must be specified. 
 If the path has more than one copy of a
 class, only the first class in the path will be scanned.
 */
public class XrefCmd {

    static public void main(String[] args) throws IOException {

        String path = null;

        for (int i=0; i<args.length; i++) {
            if ("-path".equals(args[i])){
                i++;
                if (args.length == i) {
                    System.err.println("Usage: java ec.upgrade.XrefCmd "
                            +"-path <classPath>");
                    System.exit(1);
                }
                path = args[i];;
            }
        }
        if (null == path) {
            System.err.println("Usage: java ec.upgrade.XrefCmd "
                                +"-path <classPath>");
            System.exit(1);
        }
        if (null == path) {
            path = System.getProperty("java.class.path");
        }
        Xref scanner = new Xref(path);

        Vector undefined = scanner.undefinedClasses();
        if (null != undefined) {
            System.out.println("\nThe following classes were not found in the path:");
            for (int i=0; i<undefined.size(); i++) {
                String name = (String)undefined.elementAt(i);
                System.out.println("  "+name);
            }
            System.out.println("\n");   // Two blank lines
        }

        scanner.printXref(System.out);
    }
}