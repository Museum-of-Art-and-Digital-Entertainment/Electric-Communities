/*
 * hackdepends.java
 * a hack by RobJ
 */

// package ec.misc.build.HackDepends;

import java.lang.*;
import java.util.*;
import java.io.*;


// class denoting a group of files which need to be date-checked and
// perhaps compiled
// subclasses define the rules for dependency checking and compiling
// (this class defines the most common rules: the ones for E/Java/ecomp)
class EFileGroup {
    // the base files to be checked/compiled
    protected Vector myBaseFiles = new Vector();
    // the subset of baseFiles which is out of date
    protected Vector myOutOfDateFiles = new Vector();
    // the "touch names" for the outOfDateFiles
    // (i.e. the names of their corresponding *t files)
    // this gets used to write all those files when compile's done
    protected Vector myOutOfDateTouches = new Vector();
    // the hashtable of properties from the command line
    protected Hashtable myProperties;
    // are we spamming?
    // staticSpammy is strictly a hack so we don't have to pass spammy in EVERYWHERE
    protected boolean spammy;
    protected boolean pureJava;
    private static boolean staticSpammy;
    private static boolean staticPureJava;
    
    public EFileGroup (Hashtable properties, Vector baseFileNames) {
        myBaseFiles = baseFileNames;
        myProperties = properties;
        spammy = staticSpammy;
        pureJava = staticPureJava;
    }
    
    public EFileGroup (Hashtable properties, Vector baseFileNames, boolean spam, boolean pureJava) {
        myBaseFiles = baseFileNames;
        myProperties = properties;
        staticSpammy = spam;
        staticPureJava = pureJava;
        spammy = spam;
    }
    

    
    //
    // SUBCLASS ROUTINES
    // expected to be redefined
    //


    // get the touchname for a file
    // potentially redefinable in subclasses
    public String touchName (String target) {
        String targett = getProperty("FULLPATH") + target + "t";
        return targett;
    }
    
    
    // mark a file as out of date
    // (base class does nothing special, plu redefines to launch pl)
    // potentially redefinable in subclasses
    public boolean markOutOfDate (String fileName, String fileNamet) {
        // OK, add it
        myOutOfDateFiles.addElement(fileName);
        myOutOfDateTouches.addElement(fileNamet);
        
        return true;
    }
    
    
    // build base compile string (at end of which filenames get appended)
    // build it as a vector of individual strings so we can pass to exec conveniently
    // potentially redefinable in subclasses
    public Vector makeBaseCompileString () {
        // base class is for E/Java
        Vector ret = new Vector();
        ret.addElement(getProperty("EC"));
//        ret.addElement("-resultfile");
//        ret.addElement("C:\\ECDev\\ecomp_result");
        ret.addElement("-clearclasspath");
        ret.addElement("-esystem");
        ret.addElement("-classpath");
        ret.addElement(getProperty("JAVA_CLASSPATH"));
        ret.addElement("-d");
        ret.addElement(getProperty("BUILD_DRIVE") + getProperty("BUILD") + getProperty("VAT_TARGET") + "/classes");
        ret.addElement(getProperty("ECFLAGS"));
        
        return ret;
    }    
    
    
    // check whether compile succeeded
    // for this class, checks the output file
    // for others, doesn't
    // NOTE THAT THIS IS ACTUALLY NOT NEEDED since exec seems to get the right
    // failure code!  dunno who wasn't doing the right thing in sh-land, but
    // anyway, me happy.
    public boolean checkCompileSuccess () {
        try {
            RandomAccessFile file = new RandomAccessFile("C:\\ECDev\\ecomp_result", "r");
            String line = file.readLine();
            if (line.equals("0")) {
                file.close();
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            System.err.println("HackDepends Couldn't check ecomp_result, assuming ecomp failure.");
            return false;
        }
    }



    //
    // UTILITY ROUTINES
    // not expected to be redefined
    //
    
    // ack, but OK
    public Vector outOfDateFiles () {
        return myOutOfDateFiles;
    }
    public Vector outOfDateTouches () {
        return myOutOfDateTouches;
    }
    
    
    // merge other's myOutOfDateFiles and myOutOfDateTouches with this's
    // this is to let the plu filegroup merge with the e/java filegroup
    // so all gets compiled at once
    public void mergeOutOfDateFiles (EFileGroup other) {
        for (int i = 0; i < other.outOfDateFiles().size(); i++)
            myOutOfDateFiles.addElement(other.outOfDateFiles().elementAt(i));
        for (int i = 0; i < other.outOfDateTouches().size(); i++)
            myOutOfDateTouches.addElement(other.outOfDateTouches().elementAt(i));
    }


    // get a given key from the properties
    public String getProperty (String key) {
        return ((String)myProperties.get(key));
    }
    
    
    // time test these files, building up myOutOfDateFiles and myOutOfDateTouches
    public boolean doDependencies () {
        if (spammy)
            System.out.println("HackDepends Doing dependencies for "+this+", myBaseFiles "+myBaseFiles+".");
            
        // are we out of date?
        for (int i = 0; i < myBaseFiles.size(); i++) {
            String fileName = (String)myBaseFiles.elementAt(i);
            String fileNamet = touchName(fileName);
            if (outOfDate(fileName, fileNamet)) {
                if (spammy) 
                    System.out.println("HackDepends File '"+fileName+"' is out of date, appending it to "+myOutOfDateFiles
                          +" and '"+fileNamet+"' to "+myOutOfDateTouches);

                // do anything special we need to do
                boolean ret = markOutOfDate(fileName, fileNamet);
                if (!ret) {
                    // bail out!
                    return false;
                }
            }
        }
        
        return true;
    }
    
    
    // compare file date of target and targett; return true if out of date
    public boolean outOfDate (String target, String targett) {
        // compare these two dates against each other...?!
        File targetFile = new File(target);
        File targettFile = new File(targett);
        
        if (!targetFile.exists()) {
            System.err.println("HackDepends AAACK! target file '"+target+"' not in cwd!  terminating make.");
            System.exit(1);
//            return false;
        }
        
        if (!targettFile.exists()) {

            if (spammy)
                System.out.println("HackDepends Target file '"+targett+"' doesn't exist, therefore out of date.");
                
            return true;
        } else if (targettFile.lastModified() < targetFile.lastModified()) {
            
            if (spammy)
                System.out.println("HackDepends File '"+targett+"' (mod "+targettFile.lastModified()
                  +") out of date compared to '"+target+"' (mod "+targetFile.lastModified()+").");

            return true;
        } else {

            if (spammy)
                System.out.println("HackDepends File '"+targett+"' (mod "+targettFile.lastModified()
                  +") NOT out of date compared to '"+target+"' (mod "+targetFile.lastModified()+").");

            return false;
        }
    }            



    // compile all myOutOfDateFiles
    public boolean doCompile () {
        if (spammy)
            System.out.println("HackDepends Doing compile for "+this+", myOutOfDateFiles is "+myOutOfDateFiles
                +", myOutOfDateTouches is "+myOutOfDateTouches+".");
    
        // only if we have anything out of date!
        if (myOutOfDateFiles.size() > 0) {
            Vector compileVec = makeBaseCompileString();       
            
            // OK, shell out to do it??!!
            // first make the array??!!!
            // see classlib reference, p. 1160
            String[] args = new String[compileVec.size() + myOutOfDateFiles.size()];
            for (int i = 0; i < compileVec.size(); i++) {
                args[i] = (String)compileVec.elementAt(i);
            }
            for (int i = 0; i < myOutOfDateFiles.size(); i++) {
                args[i + compileVec.size()] = (String)myOutOfDateFiles.elementAt(i);
            }

            // Create info for braun / braunie / foo2java
            // 'if (System.getenv("PUREJAVA").equals("true"))' doesn't 
            // work, so (HACK) I'm passing PureJava around...
            if (staticPureJava) {
              String fileList = "";
              for (int i = 0 ; i < myOutOfDateFiles.size(); i++) {
                fileList += myOutOfDateFiles.elementAt(i) + " ";
              }
              // AARGH! -- can't launch braunie from HackDepends.
              // Hack two:  Send file list to braunie in a file.
              String fileName = getProperty("BUILD_DRIVE") + getProperty("BUILD") + "/.BraunieFileList";
              try {
                FileOutputStream fos = new FileOutputStream(fileName);
                PrintStream ps = new PrintStream(fos);
                ps.print(fileList);
                fos.close();
              } catch (IOException e) {
                System.out.println("Error writing to file " + fileName);
                System.exit(-1);
              }
            }
                
            boolean succeeded = exec(args, true);
            if (succeeded) {
                if (spammy) 
                    System.out.println("HackDepends Compile worked, now touching touchfiles.");
                // Now touch all our touchfiles.
                touchOutOfDateTouches();
            } else {
                System.out.println("HackDepends COMPILE FAILED, NOT TOUCHING.");
            }
            
            return succeeded;

        }
        
        return true;
    }        
    
    
    
    // exec a program given a string array of arguments
    // returns true if it succeeded, false if not
    // if spamStdout, output from the process is routed through to stdout
    public boolean exec (String[] args, boolean spamStdout) {
        // OK, God help us, here we go.
        
        // Because we want to befriend our callers, tell them what the hell
        // we're doing.
        for (int i = 0; i < args.length; i++) {
            if (spamStdout || spammy)
                System.out.print(args[i] + " ");
        }
        if (spamStdout || spammy)
            System.out.println("");

        String[] env = new String[1];
        env[0] = "CLASSPATH=" + getProperty("CLASSPATH");

/*
        if (spammy) {
            System.out.print("  Environment: ");
            for (int i = 0; i < env.length; i++) {
                System.out.print(env[i] + " ");
            }
            System.out.println("");
        }
*/
            
        try {
            // get the classpath, the real one, and include as an envprops
            Process child = Runtime.getRuntime().exec(args, env);
            
            // print output of command (see p. 1082-1083, Java Class Libs. ref manual)
            InputStream in = child.getInputStream();
            int c;
            while ((c = in.read()) != -1) {
                if (spamStdout)
                    System.out.print((char)c);
            }
            in.close();
            
            InputStream err = child.getErrorStream();
            while ((c = err.read()) != -1) {
                System.out.print((char)c);
            }
            err.close();
            
            int exitStatus;
            try {
                exitStatus = child.waitFor();
            } catch (InterruptedException e) {
                System.err.println("HackDepends ERROR, child process interrupted...");
                e.printStackTrace();
                return false;
            }
            
            if (exitStatus == 0) {
                // as far as compiler's concerned, we won...
                // do subclass-specific success-checking
                return true; //checkCompileSuccess();
            } else {
                System.err.println("HackDepends ERROR, child process failed, ending make.");
                return false;
            }
        } catch (IOException e) {
            System.err.println("HackDepends FAILED TO DO COMPILE, exception "+e);
            return false;
        }
    }        
    
    
    // make a pathname to a file.  (i.e. convert "foo/bar" to "foo/")
    public String parentPath (String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("/"));
    }
    
    
    // make a directory to contain a file, if it doesn't exist
    public boolean makeDirectoryFor (String fileName) {
        String pn = parentPath(fileName);
        File p = new File(pn);

        if (!p.exists()) { 
            String[] args = new String[3];
            args[0] = "mkdir";
            args[1] = "-p";
            args[2] = pn;
            return exec(args, false);
        } else {
            return true;
        }
    }
          

    // touch a file
    public boolean touchFile (String fileName) {
        String[] args = new String[2];
        args[0] = "touch";
        args[1] = fileName;
        return exec(args, false);
    }


public boolean fakeJavapOut() { return true; }
    
    
    // touch a touchFile, done after compiling
    public void touch (String fileName) {

        // GAAAACK... OK, here we go, if the file path isn't present then
        // mkdir it.
        // OK, so we have to make the pathname ourselves??!!

        // Files with drive specs are probably generated files, so
        // don't try to make a touchfile for them.  It would fail
        // anyway because the path would look like .../c:/...
        // HOWEVER, Eric's test was BROKEN.  So now we make sure we
        // only skip the touch if:
        // - there is a colon
        // - it is either not the second character, or there's another after
        //   the second character
//        System.out.println("fileName '"+fileName+"', indexOf(':') "+fileName.indexOf(':')+", indexOf(':', 2) "+fileName.indexOf(':', 2));
        if (fileName.indexOf(':') != -1
            && (fileName.indexOf(':') != 1 || fileName.indexOf(':', 2) != -1)) {
            return;
        }
        
        if (!makeDirectoryFor(fileName)) {
            System.err.println("HackDepends ERROR: couldn't make directory for touchfile "+fileName+", continuing...");       
            return;
        }

//              touchFile(fileName);

        // XXX DANGER!  The Win32 java doesn't seem to notice BASH mounts.  So
        // trying to open "/ECDev/whatever" through java fails, if BASH has /ECDev
        // mounted on a drive other than the one java is running on.
                RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(fileName, "rw");
        } catch (IOException e) {
            System.err.println("HackDepends ACK!  IOException "+e+" thrown from trying to open as rw, forget about it!");
        }
            
        if (file != null) {  
                try {
                    file.writeBoolean(true);                            
                    file.close();
                } catch (IOException e) {
                    System.err.println("HackDepends ACK!  IOException "+e+" thrown from trying to write, forget about it!");
                }
                }
    }
    
    // touch all myOutOfDateTouches
    public void touchOutOfDateTouches () {
        for (int i = 0; i < myOutOfDateTouches.size(); i++) {
            touch((String)myOutOfDateTouches.elementAt(i));
        }
    }
}          
    


// the class for javac
class JavacFileGroup extends EFileGroup {    
    public JavacFileGroup (Hashtable properties, Vector baseFileNames) {
        super(properties, baseFileNames);
    }
    

    // get the touchname for a file
    // potentially redefinable in subclasses
    public String touchName (String target) {
        String targett = getProperty("FULLPATH") + target + "ct";
        return targett;
    }
    
    
    // mark a file as out of date
    // (base class does nothing special, plu redefines to launch pl)
    // potentially redefinable in subclasses
    public boolean markOutOfDate (String fileName, String fileNamet) {
        // OK, add it
        myOutOfDateFiles.addElement(fileName);
        myOutOfDateTouches.addElement(fileNamet);
        
        return true;
    }
    
    
    // build base compile string (at end of which filenames get appended)
    // build it as a vector of individual strings so we can pass to exec conveniently
    // potentially redefinable in subclasses
    public Vector makeBaseCompileString () {
        if (spammy)   
            System.out.println("HackDepends In javac makeBaseCompileString for "+this);
            
        // base class is for E/Java
        Vector ret = new Vector();
        String java_compiler = getProperty("ALT_JAVAC");
        if ((java_compiler == null) ||
            (java_compiler.length() < 1)) {
          java_compiler = "javac";
        }
        ret.addElement(java_compiler);
        ret.addElement(getProperty("JAVACFLAGS"));
        ret.addElement("-classpath");
        ret.addElement(getProperty("JAVA_CLASSPATH"));
        ret.addElement("-d");
        ret.addElement(getProperty("BUILD_DRIVE") + getProperty("BUILD") + getProperty("VAT_TARGET") + "/classes");
        
        return ret;
    }


    // check whether compile succeeded
    // for this one, javac is trustworthy
    public boolean checkCompileSuccess () {
        return true;
    }
}
    
    
// the class for spjava
class SpjavaFileGroup extends EFileGroup {    
    public SpjavaFileGroup (Hashtable properties, Vector baseFileNames) {
        super(properties, baseFileNames);
    }
    

    // get the touchname for a file
    // potentially redefinable in subclasses
    // NOTE THIS IS NOT AS IN Rules.gmk BECAUSE IT'S TOO PAINFUL
    public String touchName (String target) {
        String targett = getProperty("FULLPATH") + target + "spt";
        return targett;
    }
    
    
    // mark a file as out of date
    // (base class does nothing special, plu redefines to launch pl)
    // potentially redefinable in subclasses
    public boolean markOutOfDate (String fileName, String fileNamet) {
        // OK, add it
        myOutOfDateFiles.addElement(fileName);
        myOutOfDateTouches.addElement(fileNamet);
        
        return true;
    }
    
    
    // build base compile string (at end of which filenames get appended)
    // build it as a vector of individual strings so we can pass to exec conveniently
    // potentially redefinable in subclasses
    public Vector makeBaseCompileString () {
        if (spammy)   
            System.out.println("HackDepends In spjava makeBaseCompileString for "+this);
            
        // base class is for E/Java
        Vector ret = new Vector();
        ret.addElement(getProperty("EC"));
//        ret.addElement("-resultfile");
//        ret.addElement("C:\\ECDev\\ecomp_result");
        ret.addElement("-clearclasspath");
        ret.addElement("-e2jdone");
        ret.addElement("-classpath");
        ret.addElement(getProperty("JAVA_CLASSPATH"));
        ret.addElement("-d");
        ret.addElement(getProperty("BUILD_DRIVE") + getProperty("BUILD") + getProperty("VAT_TARGET") + "/classes");
        ret.addElement(getProperty("ECFLAGS"));
        
        return ret;
    }
}
    
    


// the class for plu
// NOTE THAT THIS SHOULD BE MERGED WITH THE JAVA FILE GROUP
// BEFORE WE RUN
class PluFileGroup extends EFileGroup {    
    public PluFileGroup (Hashtable properties, Vector baseFileNames) {
        super(properties, baseFileNames);
    }
    

    // get the touchname for a file
    // potentially redefinable in subclasses
    public String touchName (String target) {
        String targett = getProperty("BUILD_DRIVE") + getProperty("OUTPUT") + "/" + target + "t";
        return targett;
    }
    
    
    // mark a file as out of date
    // this is where the plu build actually happens!
    // returns true if mark was OK, false if mark choked
    // if mark choked, then bail out
    public boolean markOutOfDate (String fileName, String fileNamet) {
        // stick 'em in the vectors
        myOutOfDateFiles.addElement(fileName);
        myOutOfDateTouches.addElement(fileNamet);

        // Add call to plprep
        Vector ret = new Vector();
        ret.addElement("cpp -lang-c++ -C -P");
        ret.addElement(fileName);
        ret.addElement(getProperty("BUILD_DRIVE") + getProperty("BUILD") + "/.PlCpp");

        // LAUNCH AWAY!!!! (Part One)

        if (spammy)
            System.out.println("HackDepends Launching cpp with "+ret);

        String[] args = new String[ret.size()];
        for (int i = 0; i < ret.size(); i++) {
            args[i] = (String)ret.elementAt(i);
        }

        boolean plprepStatus = exec(args, true);
        if (!plprepStatus) {
            return false;
        }
        
        // OK, now launch plu to build it.
        // PROBLEM:  this is the ONE REMAINING PLACE where we REALLY NEED the classpath file....
        ret = new Vector();
        ret.addElement("pl");
        ret.addElement(getProperty("PLFLAGS"));
        ret.addElement("-s");
        ret.addElement(getProperty("BUILD_DRIVE") + getProperty("CLASSLIST"));
        ret.addElement("-d");
        ret.addElement(getProperty("BUILD_DRIVE") + getProperty("OUTPUT"));
        ret.addElement("-u");
        ret.addElement(getProperty("BUILD_DRIVE") + getProperty("OUTPUT")
            + getProperty("DIR_SEPARATOR") + getProperty("BUILD_DRIVE") + getProperty("PLUNITDIRS"));
        // ret.addElement(fileName);
        ret.addElement(getProperty("BUILD_DRIVE") + getProperty("BUILD") + "/.PlCpp");
        
        // LAUNCH AWAY!!!! (Part Two)
        
        if (spammy)
            System.out.println("HackDepends Launching pl with "+ret);

        args = new String[ret.size()];
        for (int i = 0; i < ret.size(); i++) {
            args[i] = (String)ret.elementAt(i);
        }
        
        return exec(args, true);
    }
    
    
    // Special routine just for this FileGroup....
    // HACK HACK???
    // The deal is that this FileGroup's "out of date files" start out being .plu files.
    // And the touch files are .plut, no problem there.
    // But once the plu pass is done, then we want to route the "out of date files" in
    // the pl-generated CLASSLIST (i.e. all the .unit files) to the EFileGroup!
    // AND we wnat to put all the touch files in the EFileGroup too (STILL no problem there)!
    // So this routine basically dumps myOutOfDateFiles on the floor and replaces it with
    // data read in from the CLASSLIST file.
    // Then some caller must do "myEFileGroup.mergeOutOfDateFiles(plFileGroup)".
    public void getOutOfDateUnits () {
        // only if we DID anything!
        if (myOutOfDateFiles.size() > 0) {
            try {
                // open up the CLASSLIST
                RandomAccessFile file = new RandomAccessFile(getProperty("BUILD_DRIVE") + getProperty("CLASSLIST"), "r");
                
                // Dump myOutOfDateFiles.
                myOutOfDateFiles = new Vector();
                // Read one line per line!
                String nextLine = file.readLine();
                
                // HACK HACK HACK???!!
                // Don't know why we get funny awful EOL.
                
                if (spammy) {
                    System.out.println("   Read line from plu CLASSLIST, it's '"+nextLine+"'.");
                    System.out.println("   Last character as integer is "+(int)nextLine.charAt(nextLine.length()-1));
                }
                
                while (nextLine != null) {
                    // get rid of horrid EOL if any
                    if (nextLine.length() > 0 && ((int)nextLine.charAt(nextLine.length() - 1)) == 13)
                        nextLine = nextLine.substring(0, nextLine.length() - 1);
                        
                    if (nextLine.length() > 0)
                        myOutOfDateFiles.addElement(nextLine);
                    nextLine = file.readLine();
                }
            } catch (IOException e) {
                System.err.println("HackDepends ACK, IOException in getOutOfDateUnits, exception "+e
                  +", myOutOfDateFiles "+myOutOfDateFiles);
            }
        }
    }

    public Vector makeBaseCompileString () {
        if (spammy)   
            System.out.println("HackDepends OOP ACK! Shouldn't be in plu makeBaseCompileString");
        return null;
    }
}
    
    


class HackDepends {
    static EFileGroup eFiles;
    static JavacFileGroup javacFiles;
    static SpjavaFileGroup spjavaFiles;
    static PluFileGroup pluFiles;
    static Hashtable properties = new Hashtable();
    static boolean spammy;
    static boolean pureJava;

        public static void main (String[] args) {
        int startOfProps = 0;
        if (args[0].equals("-verbose")) {
            spammy = true;
            startOfProps = 1;
        }
        // XXX disgusting hack for PureJava
        if (args[0].equals("true")) {
            pureJava = true;
            startOfProps = 1;
        }

//System.out.println("!!!!!! HACKED OUTPUT !!!!!!!");

        // OK, start parsing the command line properly.
        int endOfProps = parseProperties(properties, startOfProps, args);
        
        // Now parse each section
        Vector javaVec = new Vector();
        Vector javacVec = new Vector();
        Vector spjavaVec = new Vector();
        Vector pluVec = new Vector();
        int endOfJava = parseFilenames(javaVec, endOfProps, args, "JAVAEND");
        int endOfJavac = parseFilenames(javacVec, endOfJava, args, "JAVACEND");
        int endOfSpjava = parseFilenames(spjavaVec, endOfJavac, args, "SPJAVAEND");
        int endOfE = parseFilenames(javaVec, endOfSpjava, args, "EEND");
        parseFilenames(pluVec, endOfE, args, "PLEND");
        
        eFiles = new EFileGroup(properties, javaVec, spammy, pureJava);
        javacFiles = new JavacFileGroup(properties, javacVec);
        spjavaFiles = new SpjavaFileGroup(properties, spjavaVec);
        pluFiles = new PluFileGroup(properties, pluVec);
        
        // OK, we've got all the stuff we need.
        // ...right?
        
        
        // HERE IS WHERE gmk-like RULES GO.
        
        // Make the fullpath dir.
        // WHY DOESN'T mkdirs() WORK??!!!  AAAAARRRRRRRGGGGGGGHHHHHHH!
        if (!eFiles.makeDirectoryFor(getProperty("FULLPATH"))) {
            System.err.println("HackDepends Couldn't make FULLPATH directory.");
        }
         
        // Get rid of the CLASSLIST file.
        if (!eFiles.makeDirectoryFor(getProperty("BUILD_DRIVE") + getProperty("CLASSLIST"))) {
            System.err.println("HackDepends Couldn't make CLASSLIST directory.");
        }

        File classlist = new File(getProperty("BUILD_DRIVE") + getProperty("CLASSLIST"));
        try {
            classlist.delete();
        } catch (SecurityException e) {
            System.err.println("HackDepends Couldn't delete classlist file "+classlist+", security exception "+e);
        }
        
        // Do dependencies for everything.
        boolean succeeded;
        succeeded = eFiles.doDependencies();

        if (!succeeded)
            System.exit(1);

        succeeded = javacFiles.doDependencies();

        if (!succeeded)
            System.exit(1);

        succeeded = spjavaFiles.doDependencies();

        if (!succeeded)
            System.exit(1);

        succeeded = pluFiles.doDependencies();

        if (!succeeded)
            System.exit(1);
        
        // Do the plu unit get.
        pluFiles.getOutOfDateUnits();
        // Dump its stuff into eFiles.
        eFiles.mergeOutOfDateFiles(pluFiles);
        
        // Compile everything.
        succeeded = javacFiles.doCompile();

        if (!succeeded)
            System.exit(1);

        succeeded = spjavaFiles.doCompile();

        if (!succeeded)
            System.exit(1);

        succeeded = eFiles.doCompile();

        if (!succeeded)
            System.exit(1);
            
        // WE DONE!!!!!     
        
        System.exit(0);
    }
    
    
    // parse all the Properties entries, which are KEY=VALUE strings,
    // ending with the argument PROPSEND
    public static int parseProperties (Hashtable propsTable, int start, String[] args) {
        if (spammy)
            System.out.println("HackDepends In parseProperties, args is "+args);
    
        int i = start;
        while (i < args.length && !args[i].equals("PROPSEND")) {
            if (spammy) 
                System.out.println("HackDepends Processing #"+i+" '"+args[i]+"'.");
            int index = args[i].indexOf('=');
            
            if (index == -1) {
                System.err.println("HackDepends Uh-oh, property '"+args[i]+"' didn't contain =, skipping...");
            } else {
                String key = args[i].substring(0, index);
                String element = args[i].substring(index+1, args[i].length());
                propsTable.put(key, element);
                if (spammy) 
                    System.out.println("HackDepends OK! key '"+key+"', element '"+element+"'");
            }
            
            i++;
        }

        if (spammy) {        
            Enumeration k = propsTable.keys();
            Enumeration e = propsTable.elements();
            for (int j = 0; j < propsTable.size(); j++) {
                System.out.println("HackDepends Property "+j+" key:'"+((String)k.nextElement())
                  +"' element:'"+((String)e.nextElement())+"'");
            }
        }
                
        return i + 1;
    }
    
    
    // parse all arguments up to a delimiter, adding them to a vector
    public static int parseFilenames (Vector ofFiles, int startingIndex, String[] args, String delim) {
        int i = startingIndex;
        while (i < args.length && !args[i].equals(delim)) {
            ofFiles.addElement(args[i]);            
            i++;
        }
        
/*
        System.out.println("HackDepends Created files ending at delimiter '"+delim+"', they're "+ofFiles);
*/
        return i + 1;
    }
    
    
    // get a given key from the properties
    public static String getProperty (String key) {
        return ((String)properties.get(key));
    }
    
    
}
