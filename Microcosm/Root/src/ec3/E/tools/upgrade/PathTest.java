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

 Outputs all the file names in any of the
 <directories/zipFiles> in the path list.  If -path is not specified, the 
 current CLASSPATH will be used.
 */
public class PathTest {

    static public void main(String[] args) throws IOException {

        String path = null;

        for (int i=0; i<args.length; i++) {
            if ("-path".equals(args[i])){
                i++;
                if (args.length == i) {
                    System.err.println("Usage: java ec.upgrade.PathTest "
                            +"[-path <classPath>]");
                    System.exit(1);
                }
                path = args[i];;
            }
        }
        if (null == path) {
            path = System.getProperty("java.class.path");
        }
        System.out.println("Making path");
        Path test = new Path(path);
        System.out.println("Making enumeration");
        Enumeration en = test.fileElements();
        while (en.hasMoreElements()) {
            PathElement el = (PathElement)en.nextElement();
            System.out.println(el.toString());
            el.data.close();
        }
        System.out.println("Enumeration exhausted");
    }
}