// Copyright 1997 Electric Communities. All rights reserved worldwide.

package ec.tools.upgrade;

import java.io.IOException;
import java.util.Enumeration;

/*
 java ec.toos.upgrade.DuplicateClassesCms [-path <directories/zipFiles>]

 Outputs all the classes which exist in more than one of the
 <directories/zipFiles> in the path list.  If -path is not specified, the 
 current CLASSPATH will be used.
 */
public class DuplicateClassesCmd {

    static public void main(String[] args) throws IOException {

        String path = null;

        for (int i=0; i<args.length; i++) {
            if ("-path".equals(args[i])){
                i++;
                if (args.length == i) {
                    System.err.println("Usage: java ec.upgrade.DuplicateClassesCmd "
                            +"[-path <classPath>]");
                    System.exit(1);
                }
                path = args[i];;
            } else {
                System.err.println("Usage: java ec.upgrade.DuplicateClassesCmd "
                                +"[-path <classPath>]");
                System.exit(1);
            }
        }
        if (null == path) {
            path = System.getProperty("java.class.path");
        }
        DuplicateClasses scanner = new DuplicateClasses(path);

        Enumeration classes = scanner.elements();
        while (classes.hasMoreElements()) {
            Object[] cl = (Object[])classes.nextElement();
            String[] files = (String[])cl[1];
            if (files.length > 1) {
                String name = (String) cl[0];
                System.out.println(name + " in in:");
                for (int i=0; i<files.length; i++) {
                    System.out.println("   " + files[i]);
                }
            }
        }
    }
}