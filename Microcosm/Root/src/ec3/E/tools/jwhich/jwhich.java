package ec.jwhich;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

/**
 *
 */
public abstract class jwhich {

    static public void main(String[] args) throws IOException {

        boolean cont = false;
        boolean listPath = false;
        String className = null;

        for (int i=0; i<args.length; i++) {
            if ("-all".equals(args[i])){
                cont = true;
            } else if ("-verbose".equals(args[i])){
                listPath = true;
            } else {
                className = args[i];
            }
        }
        if (null == className) {
            System.err.println("Usage: java ec.jwhich.jwhich [-all] [-verbose] "
                               + "<fully qualified name>");
            System.exit(1);
        }
        report(className, cont, listPath);
    }

    /**
     * Reports on where 'name' is found
     *
     * @param name is a fully qualified name of the a class of package
     */
    static private void report(String name, boolean cont, boolean listPath) 
         throws IOException
    {     
        FileInputStream is;
        boolean found = false;

        Enumeration iter = LocatedFQName.classPathVector().elements();
        while (iter.hasMoreElements()) {
            LocatedFQName lfqn = (LocatedFQName)iter.nextElement();
            lfqn = lfqn.get(name);
            try {
                if (lfqn.existence() != LocatedFQName.DOESNT_EXIST) {
                    System.out.println("Found " + lfqn);
                    if (cont) {
                        found = true;
                    } else {
                        return;
                    }    
                } else if (listPath) {
                    System.out.println("... " + lfqn);
                }

            } catch (IOException e) {
                System.err.println("IOException in " + lfqn);
                e.printStackTrace();
            }
        }    
        if (! found) {
            System.err.println(name + " not found");
        }
    }
}

