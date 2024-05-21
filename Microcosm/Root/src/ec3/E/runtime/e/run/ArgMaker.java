package ec.e.run;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

/**
 * This is a small utility program which makes the N special cases of
 * enqueue et al.
 */
public class ArgMaker
{
    static public void main(String[] args) {
        if (args.length != 1) {
            System.err.println("usage: java ec.temp.ArgMaker input-file");
            System.exit(1);
        }

        new ArgMaker().doit(args[0]);
    }

    /** the file name to read from */
    private String myFileName;

    /** the reader to read from */
    private BufferedReader myReader;

    /** have we hit EOF? */
    private boolean myEOF = false;

    /** current line number */
    private int myLine = 0;

    /** current pattern buffer */
    private String myPattern;

    /** current set of variables */
    private Hashtable myVars = new Hashtable();

    /**
     * Read a line from the input file. Strip off any trailing
     * cr/nls.
     */
    private String readLine() {
        if (myEOF) {
            return null;
        }

        String result;

        try {
            result = myReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            result = null;
        }

        if (result == null) {
            myEOF = true;
            return null;
        }

        for (;;) {
            int len = result.length();
            if (len == 0) {
                break;
            }
            char c = result.charAt(len - 1);
            if ((c != '\n') && (c != '\r')) {
                break;
            }
            result = result.substring(0, len - 1);
        }

        myLine++;
        return result;
    }

    /**
     * Return true if the given line is a directive (or null, an implicit
     * "eof" directive.
     */
    private boolean isDirective(String line) {
        if (line == null) {
            return true;
        }
        if ((line.length() > 0) && (line.charAt(0) == '#')) {
            return true;
        }
        return false;
    }

    /**
     * Print an error and exit.
     */
    private void error(String msg) {
        System.err.println(myFileName + "(" + myLine + "): " + msg);
        System.exit(1);
    }
    
    /**
     * Process the given file.
     */
    private void doit(String fileName) {
        myFileName = fileName;
        try {
            myReader = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        String nextDirective = null;
        for (;;) {
            nextDirective = processDirective(nextDirective);
            if (nextDirective == null) {
                error("Unexpected end of file");
            }
            if (nextDirective.equals("#END")) {
                break;
            }
        }
    }

    /**
     * Process a single directive.
     */
    private String processDirective(String directive) {
        for (;;) {
            if (directive == null) {
                if (myEOF) {
                    return null;
                }
                directive = readLine();
            }
            if ((directive.length() < 2) || directive.charAt(0) != '#') {
                error("Malformed directive \"" + directive + "\"");
            }
            if (directive.charAt(1) == ' ') {
                // deal with comments
                directive = null;
                continue;
            }
            break;
        }
        if (directive.equals("#LITERAL")) {
            directive = processLiteral();
        } else if (directive.equals("#PATTERN")) {
            directive = processPattern();
        } else if (directive.startsWith("#VARIABLE ")) {
            directive = processVariable(directive.substring(10));
        } else if (directive.startsWith("#CASE ")) {
            processCase(directive.substring(6));
            directive = readLine();
        } else if (directive.equals("#END")) {
            // do nothing
        } else {
            error("Unknown directive \"" + directive + "\"");
        }
        return directive;
    }

    private String processLiteral() {
        String line;
        for (;;) {
            line = readLine();
            if (isDirective(line)) {
                break;
            }
            System.out.println(replaceVars(line));
        }
        return line;
    }

    private String processPattern() {
        StringBuffer pat = new StringBuffer();
        String line;
        for (;;) {
            line = readLine();
            if (isDirective(line)) {
                break;
            }
            pat.append(replaceVars(line));
            pat.append('\n');
        }
        myPattern = pat.toString();
        return line;
    }

    private String processVariable(String varLine) {
        varLine = varLine.intern();
        StringBuffer val = new StringBuffer();
        String line;
        boolean first = true;
        for (;;) {
            line = readLine();
            if (isDirective(line)) {
                break;
            }
            if (first) {
                first = false;
            } else {
                val.append('\n');
            }
            val.append(replaceVars(line));
        }
        myVars.put(varLine.intern(), val.toString());
        return line;
    }

    private void processCase(String caseLine) {
        String countStr;
        String declStr;
        String useStr;
        String arrayStr;
        String assignStr;
        if (caseLine.equals("array")) {
            countStr = "array";
            declStr = ", Object[] a";
            useStr = ", a";
            arrayStr = "a";
            assignStr = "/* no assignment needed */";
        } else {
            int count = Integer.parseInt(caseLine);
            if (count < 0) {
                error("Unexpected case \"" + caseLine + "\"");
            }
            countStr = "" + count;
            declStr = "";
            useStr = "";
            if (count == 0) {
                arrayStr = "ZeroLengthArray";
                assignStr = "/* no assignment needed */";
            } else {
                arrayStr = "new Object[] {";
                assignStr = "";
                for (int i = 0; i < count; i++) {
                    String arg = "a" + i;
                    declStr += ", Object " + arg;
                    useStr += ", " + arg;
                    if (i != 0) {
                        arrayStr += ", ";
                    }
                    arrayStr += arg;
                    assignStr += "a[" + i + "] = " + arg + "; ";
                }
                arrayStr += "}";
            }
        }
        String output = myPattern;
        output = doReplace(output, "#COUNT#", countStr);
        output = doReplace(output, "#DECL#", declStr);
        output = doReplace(output, "#USE#", useStr);
        output = doReplace(output, "#ARRAY#", arrayStr);
        output = doReplace(output, "#ASSIGN#", assignStr);
        System.out.print(output);
    }

    private String replaceVars(String line) {
        int pos = line.indexOf('#');
        int len = line.length();
        if (pos == -1) {
            return line;
        }
        int pos2 = line.lastIndexOf('#');
        if (pos2 == pos) {
            return line;
        }
        for (int i = 0; i < pos; i++) {
            if (line.charAt(i) != ' ') {
                return line;
            }
        }
        for (int i = pos2 + 1; i < len; i++) {
            if (line.charAt(i) != ' ') {
                return line;
            }
        }
        String varName = line.substring(pos + 1, pos2).intern();
        String replacement = (String) myVars.get(varName);
        if (replacement == null) {
            return line;
        }
        return replacement;
    }
        

    static private String doReplace(String src, String var, String val) {
        for (;;) {
            int pos = src.indexOf(var);
            if (pos == -1) {
                break;
            }
            src = src.substring(0, pos) +
                val + src.substring(pos + var.length());
        }
        return src;
    }
}
