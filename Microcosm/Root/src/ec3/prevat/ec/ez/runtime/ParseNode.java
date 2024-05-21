package ec.ez.runtime;

import java.io.PrintStream;
import java.io.IOException;


/**
 * A ParseNode of a program written in "kernel EZ".  A program written
 * in EZ is immediately expanded to kernel EZ, hopefully passing
 * source position through successfully.
 */
public abstract class ParseNode {
    static private final int INDENT_SPACES = 4;

    private SourceSpan myOptSource;

    /**
     * A bit of a kludge, but we initialize the source after
     * construction to avoid propogating source-tracking logic through
     * all subclasses.
     */
    protected ParseNode() {
        myOptSource = null;
    }

    /**
     * A bit of a kludge, but we initialize the source after
     * construction to avoid propogating source-tracking logic through
     * all subclasses.
     */
    public void defineSource(SourceSpan source)
         throws AlreadyDefinedException {

        if (myOptSource != null) {
            throw new AlreadyDefinedException("source");
        }
        myOptSource = source;
    }

    /**
     * Where is the source code this syntactic construct was parsed from?
     */
    public SourceSpan optSource() { return myOptSource; }

    /**
     * Pretty print this syntactic construct assuming the specified
     * ambient indent level.  The convention is that any leading or trailing
     * whitespace (newlines, indentation, etc...) is handled by my caller.
     * I just print from my first printing character to my last one, indenting
     * as appropriate for internal newlines.
     */
    public abstract void printOn(PrintStream os, int indent) throws IOException;

    /**
     * Onto os, first print a newline, then spaces to the designated indent
     * level, then str.
     */
    static public void lnPrintOn(PrintStream os, int indent, String str) {
        os.println();
        int limit = indent * INDENT_SPACES;
        for(int i = 0; i < limit; i++) {
            os.print(' ');
        }
        os.print(str);
    }

    /**
     * Onto os, first print a newline, then spaces to the designated indent
     * level, then pretty print this parse node.  "printOn" vs "lnPrintOn" is
     * much like the conventional disctinction between "print" and "printLn",
     * except that the newlines come first (hence the weird spelling), and
     * the newline is followed by indentation.
     */
    public void lnPrintOn(PrintStream os, int indent) throws IOException {
        lnPrintOn(os, indent, "");
        printOn(os, indent);
    }


    /**
     * Print the left bracket, then the nodes separated by sep, and
     * then the right bracket
     */
    static public void printListOn(String left,
                                   ParseNode[] nodes,
                                   String sep,
                                   String right,
                                   PrintStream os,
                                   int indent) throws IOException
    {
        os.print(left);
        if (nodes.length >= 1) {
            int last = nodes.length - 1;
            for (int i = 0; i < last; i++) {
                nodes[i].printOn(os, indent);
                os.print(sep);
            }
            nodes[last].printOn(os, indent);
        }
        os.print(right);
    }
}
