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

/** This class is the main class of the estub application.  it pulls out the
 *  command line options and pulls together all the rest of the program.
 *  Nothing really ground breaking here.
 *
 *  estub is what used to be the part of edoc related to .class file generation
 *  as a step towards a solution for circular dependencies for e.
 */
public class estub {

    static boolean debugging = false;

    static void usage() {
        System.err.println(
            "usage java ec.edoc.estub [options] file1 [file2 ...]"+
            "\n options;"+
            "\n  -in  <directory> : path prepended to input filenames"+
            "\n  -out <directory> : top of the output classpath directory"+
            "\n  -ptree           : print out parse tree"
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

        boolean debugtree = false;
        String outDir = null;
        String inDir = null;
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
            } else if (args[i].equals("-ptree")) {
                debugtree = true;
            } else if (args[i].equals("-debug")) {
                debugging = true;
            } else {
                if (fileNames == null) {
                    fileNames = new Vector();
                }
                fileNames.addElement(args[i]);
                if (!args[i].endsWith(".e") && !args[i].endsWith(".java")) {
                    System.err.println("Warning: filename ("+args[i]+
                        ") does not end in .e .java");
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


        if (outDir == null) {
            System.err.println(
                "Warning: Using current directory as output path");
            outDir = "." + File.separator;
        }

        File destClassPath = new File(outDir);
        if (!destClassPath.isDirectory()) {
            System.err.println("edoc: Cannot write class files into " +
                outDir + "; it is not a directory");
            return;
        }
        outDir = destClassPath.getAbsolutePath();

        Vector classes = edoc.parseFiles(fileNames.elements(), inDir, debugtree);

        int i = 0;

        TypeGuesser typeGuesser = new TypeGuesser();

        /* Before we loop over the classes exploding the eclasses,
         * we first make a note of which are eclasses. This lets us
         * reliably handle type guessing for all the classes in
         * the files we're considering. */
        while (i < classes.size()) {

            ClassInterfaceInfo cii =
                (ClassInterfaceInfo)classes.elementAt(i);

            if (cii instanceof EClassInfo ||
                cii instanceof EInterfaceInfo) {
                typeGuesser.remember(cii.name());
            }
            i++;
        }


        /* First we loop over the classes exploding any eclasses and
         * einterfaces we find there. When we explode a class it
         * disappears from the vector. The rest of the vector's
         * contents move down. hence we only need to increment i
         * when we don't explode a class. */
        i = 0;
        while (i < classes.size()) {

            ClassInterfaceInfo cii =
                (ClassInterfaceInfo)classes.elementAt(i);

            if (cii instanceof EClassInfo) {
                ((EClassInfo)cii).explode(classes, typeGuesser);
            } else if (cii instanceof EInterfaceInfo) {
                ((EInterfaceInfo)cii).explode(classes, typeGuesser);
            } else {
                i++;
            }
        }

        /* having exploded each eclass etc we now have just java classes.
         * we dump those now. */
        for (Enumeration e = classes.elements(); e.hasMoreElements();) {
            ClassInterfaceInfo cii =
                (ClassInterfaceInfo)e.nextElement();
            dumpClass(cii, outDir);
        }

    }  // main()


    /** This method takes an arbitrary ClassInfo, and a path root, and spits
     *  a .class file stub into the appropriate subdirectory of that path.
     */
    static void dumpClass(ClassInterfaceInfo cii, String path) {

        if (cii instanceof EClassInfo || cii instanceof EInterfaceInfo) {
            System.err.println("Cannot write class files for eclasses or" +
                " einterfaces");
            return;
        }

        boolean ciiIsInterface = false;
        if (cii instanceof JavaInterfaceInfo) {
            ciiIsInterface = true;
        } /* else class */

        ConstantPool Cpool = new ConstantPool();

        String fname = path + File.separatorChar +
            (cii.name()).replace('.', File.separatorChar) + ".class";

        FileOutputStream outputFile = null;
        try {
            outputFile = new FileOutputStream(fname);
            // XXX need to check for overwrites etc etc.
        } catch (IOException ex) {
            System.err.println("edoc: unable to open " + fname +
                " to write .class file");
            return;
        }

        /* Spool class info to the file */

        /* Generate a handle to this class */
        ConstPoolInfo thisClassName =
            Cpool.createString(cii.name().replace('.','/'));

        ConstPoolInfo thisClass = Cpool.createClass(thisClassName);

        /* Any object not declared to 'extend' extends java.lang.Object
         * All interfaces _extend_ Object, and _implement_ superinterfaces
         * Latter information not in the vm spec (thanks, dan) */
        String superClassName = TypeTable.getInternalName(cii.getExtends());
        superClassName = superClassName.substring(1, superClassName.length()-1);

        ConstPoolInfo superClass = Cpool.createClass(superClassName);

        Vector implementsList = cii.getImplements();
        int interfaceCount = implementsList.size();
        /* We only need pointers to _Classes for interfaces */
        int[] interfacePtrs = new int[interfaceCount];
        for (int i = 0; i < interfaceCount; i++) {
            String s = TypeTable.getInternalName(
                (String) implementsList.elementAt(i));
            interfacePtrs[i] =
                Cpool.createClass(s.substring(1, s.length() - 1)).getIndex();
        }

        /* Now we need the field pool & method pool */


        int numFields = 0;
        MemberPoolInfo[] fieldPool = null;
        MemberPoolInfo f;

        Vector fields = null;
        Vector methods = null;
        Vector constructors = null;
        boolean staticinitialiser = false;

        if (ciiIsInterface) {
            fields = ((JavaInterfaceInfo)cii).fields();
            methods = ((JavaInterfaceInfo)cii).methods();
        } else {
            /* instanceof JavaClassInfo */
            fields = ((JavaClassInfo)cii).fields();
            methods = ((JavaClassInfo)cii).methods();
            constructors = ((JavaClassInfo)cii).constructors();
            staticinitialiser = ((JavaClassInfo)cii).hasStaticInitialiser();
        }


        if(fields != null) {

            numFields = fields.size();
            fieldPool = new MemberPoolInfo[numFields];

            for (int i = 0; i < numFields; i++)  {

                FieldInfo fi = (FieldInfo) fields.elementAt(i);
                f = new MemberPoolInfo();
                fieldPool[i] = f;

                if (ciiIsInterface) {
                    f.access_flags = 0x19; /* interface access is implicit */
                } else {
                    f.access_flags = fi.modifiers();
                    // XXX possible sanity check
                }
                f.name = Cpool.createString(fi.name());
                f.type = Cpool.createString(fi.internalType());
                f.attribute_count = 0;
                f.attributes = null;
            }
        } else {
            numFields = 0;
            fieldPool = new MemberPoolInfo[0];
        }

        int numMethods = 0;
        int numConstructors = 0;
        int numStaticInitialisers = 0; /* either 0 or 1 */
        MemberPoolInfo[] methodPool = null;

        ConstPoolInfo initName = Cpool.createString("<init>");
        ConstPoolInfo initType = Cpool.createString("()V");

        if (methods != null)  {
            numMethods = methods.size();
        }

        if (constructors != null)  {
            numConstructors = constructors.size();
        }
        if (staticinitialiser) {
            numStaticInitialisers = 1;
        }

        dprint("methods=" + numMethods +
               "\nconstructors=" + numConstructors +
               "\nstaticinis=" +  numStaticInitialisers + "\n");

        /* if it's an interface we don't want any constructors */
        if (numConstructors == 0 && !ciiIsInterface)  {
            dprint("default constructor\n");
            methodPool = new MemberPoolInfo[1 + numMethods + numStaticInitialisers];
            /* one for default init */
            MemberPoolInfo m = new MemberPoolInfo();
            methodPool[0] = m;
            m.access_flags = 0; // XXX init modifiers
            m.name = initName;
            m.type = initType;
            m.attribute_count = 0;
            m.attributes = null;
        } else {
             methodPool = new MemberPoolInfo[numConstructors
                                          + numMethods
                                          + numStaticInitialisers];
        }


        /* we've now accounted for any default contructors. do the real methods */

        if (numConstructors != 0) {
            for (int i = 0; i < numConstructors; i++)  {
                //System.out.println("Constructor\n");
                ConstructorInfo ci =
                    (ConstructorInfo)constructors.elementAt(i);
                MemberPoolInfo m = new MemberPoolInfo();
                methodPool[i] = m;

                /* should really have only one of &0x1 xor &0x2 xor &0x4 set*/
                m.access_flags = (ci.modifiers() & 0x7);
                /* name it <init>, need a full type descriptor */
                m.name = initName;
                m.type = Cpool.createString(ci.internalType());
                if (ci.getThrows() == null || ci.getThrows().size() == 0) {
                    m.attribute_count = 0;
                    m.attributes = null;
                } else {
                    m.attribute_count = 1;
                    m.attributes = new AttributeInfo[1];
                    m.attributes[0] = new ExceptionsAttributeInfo(
                        Cpool, ci.getThrows());
                }
            }
        } else {
            /* if we have numConstructors == 0, then we either have an
             * interface, or we had a default constructor earlier.
             * we now account for that constructor if need be */
            if (!ciiIsInterface) {
                numConstructors = 1;
            }
        }

        /* class static initialiser */
        /* this lives in methodPool[] after the constuctors */
        if (numStaticInitialisers == 1) {
            //System.out.println("Static Initialiser\n");
            /* think for i = 1 + numConstructors;
                         i < 1 + numConstructors + numStaticInitialisers */
            methodPool[numConstructors] = new MemberPoolInfo();
            methodPool[numConstructors].access_flags = 0;
            /* clinit modifiers ignored by vm */
            methodPool[numConstructors].name = Cpool.createString("<clinit>");
            methodPool[numConstructors].type = initType;
            methodPool[numConstructors].attribute_count = 0;
            methodPool[numConstructors].attributes = null;
        }

        for (int i = numConstructors + numStaticInitialisers;
                i < numConstructors + numStaticInitialisers + numMethods;
                i++)  {
            //System.out.println("Method\n");
            MethodInfo mi = (MethodInfo) methods.elementAt(
                i - numConstructors - numStaticInitialisers);

            MemberPoolInfo m = new MemberPoolInfo();
            methodPool[i] = m;

            if (ciiIsInterface) {
                m.access_flags = 0x401;
            } else {
                m.access_flags = (mi.modifiers() & 0x53F);
            }

            /* get name of method & a full type descriptor */
            m.name = Cpool.createString(mi.name());
            m.type = Cpool.createString(mi.internalType());

            //System.out.println("Throws;" + mi.getThrows());

            if (mi.getThrows() == null || mi.getThrows().size() == 0) {
                if (ciiIsInterface) {
                    m.attribute_count = 0;
                    m.attributes = null;
                } else {
                    m.attribute_count = 1;
                    m.attributes = new AttributeInfo[1];
                    m.attributes[0] = new VoidCodeAttributeInfo(Cpool);
                }
            } else {
                if (ciiIsInterface) {
                    m.attribute_count = 1;
                    m.attributes = new AttributeInfo[1];
                    m.attributes[0] = new ExceptionsAttributeInfo(
                        Cpool, mi.getThrows());
                } else {
                    m.attribute_count = 2;
                    m.attributes = new AttributeInfo[2];
                    m.attributes[0] = new ExceptionsAttributeInfo(
                        Cpool, mi.getThrows());
                    m.attributes[1] = new VoidCodeAttributeInfo(Cpool);
                }
            }

        }



        ByteArray Bytes = new ByteArray(debugging);

        // Class consists of;
        // magic Oxcafebabe
        Bytes.addu4(0xcafebabe);
        // minor / major version 3/45
        Bytes.addu2(3);
        Bytes.addu2(45);

        // constant pool
        Cpool.dump(Bytes);

        // access modifiers
        //XXX Sanity Check on modifiers class / interface etc. for this class
        /* Modifiers; must set ACC_SUPER == 0x0020 */
        /* class can only set 0x1, 0x10, 0x20,        0x400  = 0x431 */
        /* intfc can only set 0x1,       0x20, 0x200, 0x400  = 0x621*/
        if ((cii.modifiers() & 0x410) == 0x410) {
            /* then someone set final AND ABSTRACT */
            System.err.println("Error! Cannot have an abstract final class;\n" +
                " It has been made non-final");
            cii.maskModifiers(~0x10);
        }
        if (ciiIsInterface) {
            Bytes.addu2((cii.modifiers() & 0x431) | 0x620);
        } else {
            Bytes.addu2((cii.modifiers() & 0x621) | 0x20);
        }

        // this class
        Bytes.addu2(thisClass.getIndex());

        // super class
        Bytes.addu2(superClass.getIndex());

        // interfaces
        //X Bytes.addu2(InterfaceVector.size());
        Bytes.addu2(interfaceCount);
        for (int i = 0; i < interfaceCount; i++) {
            Bytes.addu2(interfacePtrs[i]);
        }

        // field pool
        Bytes.addu2(fieldPool.length);
        //System.out.println("fields\n");
        for (int i = 0; i < fieldPool.length; i++) {
            //System.out.println(i + ":" + fields[i] + "\n");
            fieldPool[i].dump(Bytes);
        }

        // method pool
        /* I'm going to temporarily remove <clinit>, miss out [0] + do 1 less */
        Bytes.addu2(methodPool.length);
        //System.out.println("methodPool\n");
        for (int i = 0 ; i < methodPool.length; i++) {
            //System.out.println(i + ":" + methodPool[i] + "\n");
            methodPool[i].dump(Bytes);
        }

        // attributes
        //X Bytes.addu2(InterfaceVector.size());
        //X for () Bytes.addu2()
        Bytes.addu2(0); // no attributess (?)
        // hence put out none here....

        try {
            outputFile.write(Bytes.getContents());
            outputFile.close();
        } catch (IOException e) {
            System.err.println("edoc: error trying to write to file " + fname);
        }
    }

}
