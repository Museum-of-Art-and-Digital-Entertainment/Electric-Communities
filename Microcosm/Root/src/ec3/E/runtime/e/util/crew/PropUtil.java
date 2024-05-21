package ec.e.util.crew;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;

/**
 * Just a collection of static methods useful for handling Properties.
 */
public class PropUtil {
    
    /**
     * Waive the miranda constructor
     */
    private PropUtil() {}

    /**
     * Enhance 'props' from 'inArgs' according to the following
     * rules. Returns an array of the args remaining after the
     * property-specifying ones are stripped out. 
     * <pre>
     *  <em>key</em>=<em>value</em>
     *      The 'key' string is associated with the 'value' string
     *  -ECNoDefaults
     *      suppress reading "/usr/local/lib/eprops/systemdefault"
     *      and "~/.eprops/default" for further property definitions
     *  -ECproperties <em>filename</em>
     *      file is read for further property definitions
     *  <em>anything else</em>
     *      added to the returned args array
     * </pre>
     * 
     * @exception IOException when a file from which one is supposed
     * to read further properties does not exist or cannot be read.
     * However, if "-ECNoDefaults" is not provided, but any of the
     * default properties files does not exist, they are silently
     * skipped rather than throwing an exception.  But if they do
     * exist and aren't readable, we throw an exception as for any
     * other properties file. <p>
     *
     * @see ec.e.start.EBoot
     * @see ec.e.quake.Revive
     */
    static public String[] argsAndProps(String inArgs[], Properties props)
         throws IOException
    {
        boolean loadDefaults = true;
        
        // args we find on command line
        Vector args = new Vector(inArgs.length);
        // properties we find on command line
        Vector cmdProps = new Vector(inArgs.length);
        // property files we find on command line
        Vector propFiles = new Vector(inArgs.length);
        
        for (int i = 0; i < inArgs.length; i++) {
            String s = inArgs[i];
            if (s.equals("-ECproperties")) {
                if (++i < inArgs.length) {
                    propFiles.addElement(inArgs[i]);
                } else {
                    throw new IllegalArgumentException
                        ("-ECproperties must be followed by filename");
                }
            } else if (s.equals("-ECNoDefaults")) {
                loadDefaults = false;
            } else if (s.indexOf('=') > 0) {
                cmdProps.addElement(s);
            } else {
                args.addElement(s);
            }
        }
        
        if (loadDefaults) {
            try {
                loadPropsFile("/usr/local/lib/eprops/systemdefault", props);
            } catch (FileNotFoundException ex) {
                /*
                 * It is actually part of the contract of this method
                 * that if it's loading these by default (as opposed
                 * to by being told to), and the files don't exist, to
                 * just ignore them.  Therefore, we can silently
                 * ignore the error.
                 */
            }
            try {
                loadPropsFile(System.getProperty("user.home", "") + 
                              "/.eprops/default", props);
            } catch (FileNotFoundException ex) {
                //see comment in above catch clause
            }
            try {
                loadPropsFile("/eprops.txt", props);
            } catch (FileNotFoundException ex) {
                //see comment in above catch clause
            }
        }
        for (Enumeration names = propFiles.elements();
             names.hasMoreElements(); ) {

            loadPropsFile((String)names.nextElement(), props);
        }
        
        for (Enumeration assocs = cmdProps.elements();
             assocs.hasMoreElements(); ) {

            String s = (String)assocs.nextElement();
            int i = s.indexOf('=') ;
            String key = s.substring(0, i);
            String value = s.substring(i+1);
            props.put(key, value);
        }
        
        String[] result = new String[args.size()];
        args.copyInto(result);
        return result;
    }
    
    /** 
     * If the file named 'name' exists and is readable, it is read as
     * a properties-defining file, and these definitions are added to
     * 'props'.
     * 
     * @param filename name of properties file
     * @param props Properties to be added to
     * @exception FileNotFoundException if the file doesn't exist
     * @exception IOException if there was a problem reading from it
     */
    static public void loadPropsFile(String filename, Properties props)
         throws FileNotFoundException, IOException
    {
        filename = new File(filename).getAbsolutePath();
        FileInputStream instream;
        try {
            instream = new FileInputStream(filename);

        } catch (FileNotFoundException ex) {
            //make diagnostic more informative
            throw new FileNotFoundException("Error opening " + 
                                            filename + ": " + ex);
        }
        try {
            props.load(instream);

        } catch (IOException ex) {
            //make diagnostic more informative
            throw new IOException("Error reading " + 
                                  filename + ": " + ex);
        } finally {
            instream.close();
        }
    }
}
