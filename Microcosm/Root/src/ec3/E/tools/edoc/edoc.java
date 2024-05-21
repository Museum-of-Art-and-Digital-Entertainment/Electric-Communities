/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;
import java.util.Enumeration;

import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.EOFException;

/** This class is the main class of the edoc application.  it pulls out the
 *  command line options and pulls together all the rest of the program.
 *  Nothing really ground breaking here.
 */
public class edoc {

    static boolean debugging = false;

    static void usage() {
        System.err.println(
            "Produces HTML documentation from java & E source."+
            "\nusage java ec.edoc.edoc [options] file1 [file2 ...]"+
            "\n options;"+
            "\n  -out <directory> : html / index output directory"+
            "\n  -in  <directory> : path prepended to input filenames"+
            "\n  -indexdir <dir>  : separate directory to read / write .index files to"+
            "\n                     this overrides -in / -out which are used otherwise"+
            "\n"+
            "\n  -ptree           : print out parse tree"+
            "\n"+
            "\n  -noimages        : produce html files without image bullets"+
            "\n  -index           : process .index files to generate indices, rather"+
            "\n                     than .java & .e into html and .index files"
            );
    }

    private static void dprint(String s) {
        if (debugging) {
            System.out.print(s);
            System.err.flush();
        }
    }

    public static void main(String args[]) {

        EDocParser parser;

        boolean indexMode = false;
        boolean imageMode = true;
        boolean debugtree = false;
        String outDir = "";
        String inDir = "";
        String indexDir = null;
        Vector fileNames = null;


        if (args.length == 0) {
            System.err.println("No arguments specified");
            usage();
            return;
        }
        for (int i = 0; i < args.length; i++) {

            if (args[i].equals("-out")) {
                i++;
                if (args.length < i) {
                    System.err.println(
                        "No argument to -out option, or no filename");
                    usage();
                    return;
                }
                outDir = args[i] + File.separator;
            } else if (args[i].equals("-in")) {
                i++;
                if (args.length < i) {
                    System.err.println(
                        "No argument to -in option, or no filename");
                    usage();
                    return;
                }
                inDir = args[i] + File.separator;
            } else if (args[i].equals("-indexdir")) {
                i++;
                if (args.length < i) {
                    System.err.println(
                        "No argument to -indexdir option, or no filename");
                    usage();
                    return;
                }
                indexDir = args[i] + File.separator;
            } else if (args[i].equals("-ptree")) {
                debugtree = true;
            } else if (args[i].equals("-debug")) {
                debugging = true;
            } else if (args[i].equals("-noimages")) {
                imageMode = false;
            } else if (args[i].equals("-index")) {
                indexMode = true;
            } else {
                if (fileNames == null) {
                    fileNames = new Vector();
                }
                fileNames.addElement(args[i]);
                if ((indexMode  && !args[i].endsWith(".index")) ||
                    (!indexMode && !args[i].endsWith(".e") &&
                        !args[i].endsWith(".java"))) {
                    System.err.println("Warning: filename ("+args[i]+
                        ") does not end in appropriate .e .java or .index");
                }
            }
        }

        if (fileNames == null) {
            System.err.println("Must specify files to process...");
            return;
        }

        if (debugging) {
            System.out.println("\nType \"go\" and press return to continue...");
            System.out.println("(It might be an idea to start the debugger "+
                "now if you want to...)");
            try {
                while (!(System.in.read() == 'g' && System.in.read() == 'o')) {
                }
            } catch (IOException e) {
                /* do nothing */
            }
        }

        /* either we're building indexes, or html files */
        if (indexMode) {
            doIndex(fileNames.elements(), imageMode,
                    (indexDir != null) ? indexDir : inDir,
                    outDir);
        } else {
            Vector classes = parseFiles(fileNames.elements(), inDir, debugtree);
            doHTML(classes, imageMode, outDir,
                   (indexDir != null) ? indexDir : outDir);
        }

        return;
    }

    static void doHTML(Vector classes, boolean imageMode, String outDir,
            String indexDir) {
        HTMLGenerator generator = new HTMLGenerator();

        generator.images(imageMode);

        for (Enumeration e = classes.elements(); e.hasMoreElements();) {

            ClassInterfaceInfo cii =
                (ClassInterfaceInfo) (e.nextElement());

            //System.out.println("Working on Class " + ci.name);

            FileOutputStream outputFile = null;
            FileOutputStream indexFile = null;
            try {
                outputFile = new FileOutputStream(outDir+cii.name() + ".html");
                indexFile = new FileOutputStream(indexDir+cii.name() + ".index");
            } catch (IOException ex) {
                System.err.println("edoc: unable to open "
                    + outDir + cii.name() +
                    ".html for writing documentation, or "
                    + indexDir + cii.name() + ".index for index info");
            }

            if (outputFile != null && indexFile != null) {
                PrintStream p = new PrintStream(outputFile);
                //System.out.println("generating files");
                generator.outputClass(p, indexFile, cii);
                try {
                    outputFile.close();
                    indexFile.close();
                } catch (IOException ex) {
                    System.err.println("edoc: unable to close "
                        + outDir + cii.name() +
                        ".html after writing documentation, or "
                        + indexDir + cii.name() + ".index for index info");
                }
            }
        }  // for loop over classes
    }


    static void doIndex(Enumeration files, boolean imageMode,
            String inDir, String outDir) {
        //System.out.println("Index Mode Selected");
        Vector index = new Vector();
        Vector classes = new Vector();

        /* build up the index from the individual files */
        while (files.hasMoreElements()) {
            String file = inDir + (String) files.nextElement();
            System.err.println("edoc: reading index " +file+ "...");

            try {
                DataInputStream d = new DataInputStream(
                                        new FileInputStream(file));

                /* We want to loop forever, building IndexEntries from this
                 * file, until we get to the end of the file (ie get an
                 * IndexEntry which is Nil */

                IndexEntry i;

                /* Grab the class from the start of the index file */
                i = new IndexEntry(d);
                classes.addElement(i);
                if (!i.isClass()) {
                    System.err.println("Aaaarrrrggh. First entry in "
                        +"an index file should be a class...");
                }
                while ( !( (i = new IndexEntry(d)).isNil() ) ) {
                    if (!i.isClass()) {
                        index.addElement(i);
                    } else {
                        System.err.println("Should only have one class "+
                            "in an index file...");
                    }
                }

            } catch (EOFException e) {
                System.err.println("\nedoc: Premature End of file while "+
                    "reading " + file);
            } catch (FileNotFoundException e) {
                System.err.println("\nedoc: file " + file +
                    " not found.");
            } catch (SecurityException e) {
                System.err.println("\nedoc: unable to read " +file+
                    "; permission denied");
            }  catch (IOException e) {
                System.err.println("\nedoc: Error reading index file");
            }
        }

        System.out.print("Sorting Index items");System.out.flush();
        IndexEntry.sort(index);
        System.out.print("done.\nSorting Classes items");System.out.flush();
        IndexEntry.sort(classes);
        System.out.println("done.");

        //for (Enumeration e = index.elements(); e.hasMoreElements(); ) {
        //    IndexEntry i = (IndexEntry) e.nextElement();
        //  if (i.isNil()) {
        //      System.out.println("t ");
        //  } else {
        //      System.out.println("f " + i.name());
        //  }
        //}

        HTMLGenerator generator = new HTMLGenerator();
        generator.images(imageMode);

        FileOutputStream masterIndexFile = null;
        try {
            System.out.println("Writing Index.html");
            masterIndexFile = new FileOutputStream(outDir + "Index.html");
        } catch (IOException ex) {
            System.err.println("edoc: unable to open Index.html");
        }

        generator.outputIndex(new PrintStream(masterIndexFile), index);

        try {
            masterIndexFile.close();
        } catch (IOException ex) {
            System.err.println("edoc: unable to close Index.html");
        }


        FileOutputStream masterPackagesFile = null;
        try {
            System.out.println("Writing Packages.html");
            masterPackagesFile = new FileOutputStream(outDir + "Packages.html");
        } catch (IOException ex) {
            System.err.println("edoc: unable to open Packages.html");
        }

        generator.outputPackages(new PrintStream(masterPackagesFile),classes);

        try {
            masterPackagesFile.close();
        } catch (IOException ex) {
            System.err.println("edoc: unable to close Packages.html");
        }


        FileOutputStream masterClassesFile = null;
        try {
            System.out.println("Writing Classes.html");
            masterClassesFile = new FileOutputStream(outDir + "Classes.html");
        } catch (IOException ex) {
            System.err.println("edoc: unable to open Classes.html");
        }

        generator.outputClasses(new PrintStream(masterClassesFile),classes);

        try {
            masterClassesFile.close();
        } catch (IOException ex) {
            System.err.println("edoc: unable to close Classes.html");
        }


        FileOutputStream masterTreeFile = null;
        try {
            System.out.println("Writing Tree.html");
            masterTreeFile = new FileOutputStream(outDir + "Tree.html");
        } catch (IOException ex) {
            System.err.println("edoc: unable to open Tree.html");
        }

        generator.outputTree(new PrintStream(masterTreeFile),classes);

        try {
            masterTreeFile.close();
        } catch (IOException ex) {
            System.err.println("edoc: unable to close Tree.html");
        }
        /* If we've done an index, we don't want to bother with the
         * rest of the code, parsing & stuff....*/
        return;
    }

    static Vector parseFiles(Enumeration files, String inDir, boolean pTree) {
        Vector result = new Vector();
        while (files.hasMoreElements()) {

            String file = inDir + (String)files.nextElement();

            System.err.print("edoc: " + file + "...");
            System.err.flush();

            try {

                parseFile(new FileInputStream(file), result, pTree);
                System.err.println("parsed");

            } catch (ParseError e) {
                System.err.println("\nedoc: Failed to parse " + file);
                //System.err.println(e.getMessage());
                //e.printStackTrace();
                System.out.println("Ignoring this class");
                continue;

            } catch (FileNotFoundException e) {
                System.err.println("\nedoc: file " + file
                    + " not found.");

            } catch (SecurityException e) {
                System.err.println("\nedoc: unable to read " +
                    file + "; permission denied");
            }

        }
        return result;
    }

    private static EDocParser parser = null;

    private static void parseFile(FileInputStream f, Vector classes,
            boolean pTree) throws FileNotFoundException, ParseError {
        if (parser == null) {
            parser = new EDocParser(f);
        } else {
            parser.ReInit(f);
        }

        System.gc();
        ASTCompilationUnit n = null;

        n = parser.CompilationUnit();

        if (pTree) {
            n.dump("");
        }

        Vector tv = n.buildTokenVector();


        /* XXX (rak) - this might cause problems for others later...
         * This System.gc() is here for a reason. I lost a godd 30-40 mins
         * when my program suddenly started throwing out of memory errors
         * in bits of code i hadn't changed. I think that the problem
         * was related to heap fragmentation (which would go away if sun
         * built a compacting garbage collector...
         * Anyway, it bombed even if you gave it a 64Mb heap, but works
         * (a least for me, at least at the moment...)
         * if you do a gc _here_...
         */
        System.gc();

        TreeWalker tw = new TreeWalker();
        tw.walk((SimpleNode) n);
        tw.matchVectors(tv);
        tw.buildClassInfos(classes);
    }


}