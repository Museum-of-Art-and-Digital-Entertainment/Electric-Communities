package ec.tools.filtereext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class FilterEext
{
    /** the directory to write output to
     */
    String myOutdir;

    /** the filename for this object
     */
    String myFileName;
    
    /** the reader to read from--it's a .eext file 
     */
    private BufferedReader myReader;
    
    /** whether we have hit EOF on the input yet
     */
    private boolean myEOF = false;

    /** the preface of the file, that is, the package statement
     *  and any imports
     */ 
    String myPreface = null;
    
    /** the package name of this file.
     */
    String myPackage = null;
    
    /** a pushback line, or null
     */
    private String myPushbackLine = null;

    /**
     * Process the files named by the args, except for the first arg,
     * which names the output directory.
     */
    static public void main(String[] args) {
        for (int i = 1; i < args.length; i++) {
            new FilterEext(args[i], args[0]).doit();
        }
    }
    
    /**
     * Just the constructor; merely sets up variables. doit() is
     * the main functionality.
     *
     * @param name the name of the input file
     * @param outdir the name of the output directory
     */
    private FilterEext(String name, String outdir) {
        System.out.println("processing " + name);
        myFileName = name;
        myOutdir = outdir;
        try {
            myReader = new BufferedReader(new FileReader(name));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Process the file represented by this object.
     */
    private void doit() {
        for (;;) {
            readPreface();
            for (;;) {
                ClassDef classDef = readClass();
                if (classDef != null) {
                    classDef.writeYourself();
                } else {
                    break;
                }
            }
            if (myEOF) {
                break;
            }
        }
        try {
            myReader.close();
        } catch (IOException e) {
            // ignore
        }
    }
    
    /**
     * Pushback the given line onto the stream.
     *
     * @param line the line to push back
     */
    private void pushbackLine(String line) {
        myPushbackLine = line;
    }
    
    /**
     * Read a line, but ignore the funky ecomp "#" lines. Also, return a 
     * pushback line if there's one to return. Returns null if at the EOF
     * of input.
     *
     * @return the line read
     */
    private String readLine() {
        String result;
        if (myPushbackLine != null) {
            result = myPushbackLine;
            myPushbackLine = null;
            return result;
        }
        for (;;) {
            if (myEOF) {
                return null;
            }
            try {
                result = myReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                myEOF = true;
                return null;
            }
            if (result == null) {
                myEOF = true;
                return null;
            }
            if (   (result.length() > 0)
                && (result.charAt(0) == '#')) {
                continue;
            }
            break;
        }
        return result + "\n";
    }
    
    /**
     * Read the preface of a file, theoretically the package name and all
     * the imports, but in practice everything up to the first class def
     * line.
     */
    private void readPreface() {
        String result = "";
        for (;;) {
            String line = readLine();
            if (line == null) {
                System.out.println("(note: ate entire file without " +
                    "finding a declaration)");
                return;
            }
            trySettingPackage(line);
            if (getClassName(line) != null) {
                pushbackLine(line);
                break;
            }
            result += line;
        }
        myPreface = result;
    }
    
    /**
     * Read a class def--basically all the lines up to the next class def 
     * line or EOF. It expects there to be a class def line ready to be
     * read.
     *
     * @return a ClassDef for the class that was read in
     */
    private ClassDef readClass() {
        String line = readLine();
        if (line == null) {
            return null;
        }
        if (getPackageName(line) != null) {
            pushbackLine(line);
            return null;
        }
        String className = getClassName(line);
        if (className == null) {
            System.err.println("couldn't find name in: " + line);
            return null;
        }
        StringBuffer buf = new StringBuffer(20000);
        buf.append(line);
        for (;;) {
            line = readLine();
            if (line == null) {
                break;
            }
            if (   (getClassName(line) != null)
                || (getPackageName(line) != null)) {
                pushbackLine(line);
                break;
            }
            buf.append(line);
        }
        return new ClassDef(this, className, buf.toString());
    }
    
    /**
     * Set myPackage based on this line, but only if the line looks like
     * a package line. It's implemented as a small FSA.
     *
     * @param line the line to check
     */
    private void trySettingPackage(String line) {
        String pack = getPackageName(line);
        if (pack != null) {
            myPackage = pack;
        }
    }

    /**
     * Return the package of the line if the line is a "package" statement,
     * or return null if it's not. It's implemented as a small FSA.
     *
     * @param line the line to check
     * @return the package or null
     */
    private String getPackageName(String line) {
        if (line == null) {
            return null;
        }
        int max = line.length();
        int at = 0;
        int packageStart = 0;
        int state = 0;
        while (at < max) {
            char c = line.charAt(at);
            switch (state) {
                case 0: {
                    // initial state; looking for whitespace
                    if (Character.isWhitespace(c)) {
                        at++;
                    } else {
                        state = 1;
                    }
                    break;
                }
                case 1: {
                    // looking for 'package'
                    if (   (c == 'p')
                        && line.regionMatches(at, "package", 0, 7)
                        && Character.isWhitespace(line.charAt(at+7))) {
                        at += 8;
                        state = 2;
                    } else {
                        // failure
                        return null;
                    }
                    break;
                }
                case 2: {
                    // looking for whitespace after 'package'
                    if (Character.isWhitespace(c)) {
                        at++;
                    } else {
                        packageStart = at;
                        state = 3;
                    }
                    break;
                }
                case 3: {
                    // looking for whitespace or semicolon
                    if (   (c == ';') 
                        || Character.isWhitespace(c)) {
                        // success--return package name 
                        return line.substring(packageStart, at);
                    } else {
                        at++;
                    }
                }
            }
        }

        // failure if we fell through
        return null;
    }
    
    /**
     * Figure out the class name for this line and return it, or return
     * null if this line doesn't look like a class (or interface) definition
     * line. It's implemented as a small FSA.
     *
     * @param line the line to scrutinize
     * @return the class name being defined, or null if there is none
     */
    private static String getClassName(String line) {
        if (line == null) {
            return null;
        }
        int max = line.length();
        int at = 0;
        int nameStart = 0;
        int state = 0;
        while (at < max) {
            char c = line.charAt(at);
            switch (state) {
                case 0: {
                    // initial state; looking for whitespace or alpha,
                    // but notice 'class' and 'interface' keywords
                    if (   (c == 'c')
                        && line.regionMatches(at, "class", 0, 5)
                        && Character.isWhitespace(line.charAt(at+5))) {
                        at += 6;
                        state = 1;
                    } else if (   (c == 'i')
                               && line.regionMatches(at, "interface", 0, 9)
                               && Character.isWhitespace(line.charAt(at+9))) {
                        at += 10;
                        state = 1;
                    } else if (   Character.isWhitespace(c)
                               || Character.isLetter(c)) {
                        at++;
                    } else {
                        // failure
                        return null;
                    }
                    break;
                }
                case 1: {
                    // looking for whitespace after 'class' or 'interface'
                    if (Character.isWhitespace(c)) {
                        at++;
                    } else {
                        nameStart = at;
                        state = 2;
                    }
                    break;
                }
                case 2: {
                    // looking for Java identifier until whitespace or 
                    // open brace
                    if (   (c == '{') 
                        || Character.isWhitespace(c)) {
                        // success--return class name
                        return line.substring(nameStart, at);
                    } else if (Character.isJavaIdentifierPart(c)) {
                        at++;
                    } else {
                        // failure
                        return null;
                    }
                }
            }
        }
        // failed
        return null;
    }
}

class ClassDef
{
    private FilterEext myFE;
    private String myName;
    private String myDef;
    
    /**
     * Construct a ClassDef with the given data.
     *
     * @param fe the base FilterEext for ambient info (package, etc.)
     * @param className the name of the class being defined
     * @param classDef the body of the class definition
     */
    public ClassDef(FilterEext fe, String className, String classDef) {
        myFE = fe;
        myName = className;
        myDef = classDef;
    }
    
    public void writeYourself() {
        String fullName = myFE.myPackage + "." + myName;
        String namePath = myFE.myOutdir + File.separator
            + fullName.replace('.', File.separatorChar) + ".java";
        //System.out.println(fullName + " => " + namePath);
        try {
            File f = new File(namePath);
            new File(f.getParent()).mkdirs();
            FileOutputStream fos = new FileOutputStream(f);
            PrintWriter pw = new PrintWriter(fos);
            pw.println("// Generated by FilterEext. DO NOT MODIFY THIS " +
                "FILE.\n// Original source: " + myFE.myFileName + "\n");
            pw.print(myFE.myPreface);
            pw.print(myDef);
            pw.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
