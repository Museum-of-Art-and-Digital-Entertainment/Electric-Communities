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


/*
 findclass [-path <directories/zipFiles>] <simpleClassName>

 Prints the CLASSPATH element and the file in that path for all files which
 match simpleClassName.

 simpleClassName is a class name without any periods.
 */
public class FindFile {

    static public void main(String[] args) throws IOException {

        String path = null;
        String fileName = null;

        for (int i=0; i<args.length; i++) {
            if ("-path".equals(args[i])){
                i++;
                if (args.length == i) {
                    System.err.println("Usage: java ec.upgrade.FindFile "
                            +"[-path <classPath>] fileName");
                    System.exit(1);
                }
                path = args[i];;
            } else {
                fileName = args[i];
            }
        }
        if (null == fileName) {
            System.err.println("Usage: java ec.upgrade.FindFile "
                    +"[-path <classPath>] fileName");
            System.exit(1);
        }
        if (null == path) {
            path = System.getProperty("java.class.path");
        }
        Path test = new Path(path);
        Enumeration en = test.fileElementsNoStream();
        while (en.hasMoreElements()) {
            PathElement el = (PathElement)en.nextElement();
            String simpleName = cannonizeClassName(el.file);
            int i = simpleName.lastIndexOf('/');
            if (i >= 0) {
                simpleName=simpleName.substring(i+1);
            }
            if (simpleName.equals(fileName)) {
                System.out.println(el.toString());
            }
        }
    }

    static private String cannonizeClassName(String className) {
        String ret = className.replace(File.separatorChar,'/');
        return ret.replace('\\','/');
    }
}