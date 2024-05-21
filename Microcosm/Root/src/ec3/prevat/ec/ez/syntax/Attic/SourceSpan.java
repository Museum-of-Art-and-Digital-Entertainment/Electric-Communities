package ec.ez.syntax;

import java.io.PrintStream;
import java.io.IOException;

/**
 *
 */
public class SourceSpan {
    private String myUrl = null;
    private int myStart = 0;
    private int myBound = -1;
    private int myLineNo = -1;

    /**
     *
     */
    public SourceSpan(String url,
                      int start, int bound,
                      int lineNo) {
        myUrl = url;
        myStart = start;
        myBound = bound;
        myLineNo = lineNo;
    }

    /**
     * Where is the source file?
     */
    public String url() { return myUrl; }

    /**
     * Position of first source character of the construct that parsed
     * into this node.
     */
    public int start() { return myStart; }

    /**
     * Position after last source character of the construct that parsed
     * into this node.
     */
    public int bound() { return myBound; }

    /**
     * Line number of the first line of this source construct
     */
    public int lineNo() { return myLineNo; }

    /**
     * Returns a new SourceSpan that covers the original two.  Either input
     * may be null, as may the output.  If either input is null, the result
     * is the other one.  If the two don't have the same Url, the result is
     * null.  Finally, the result describes the minimal span that includes
     * both the originals.
     */
    static public SourceSpan optCover(SourceSpan optA, SourceSpan optB) {
        if (optA == null) {
            return optB;
        } else if (optB == null) {
            return optA;
        } else if ( ! optA.myUrl.equals(optB.myUrl)) {
            return null;
        }
        return new SourceSpan(optA.myUrl,
                              Math.min(optA.myStart, optB.myStart),
                              Math.max(optA.myBound, optB.myBound),
                              Math.min(optA.myLineNo, optB.myLineNo));
    }


}
