package ec.ez.syntax;

import ec.ez.runtime.SourceSpan;


/**
 *
 */
public class EZToken {

    private SourceSpan mySource;
    private int myTokenType;

    public EZToken(SourceSpan source, int tokenType) {
        if (source == null) {
            //XXX debugging
            throw new RuntimeException("what gives?");
        }
        mySource = source;
        myTokenType = tokenType;
    }

    /**
     * Where is the source code this syntactic construct was parsed from?
     */
    public SourceSpan source() { return mySource; }

    public int tokenType() { return myTokenType; }

    public String token() {
        String result = EZParser.TheTokens[myTokenType];
        if (result == null) {
            throw new Error
                ("internal: variable tokens should override: " + myTokenType);
        }
        return result;
    }

    public String toString() {
        String result = tokenType() + ": \"" + token() + "\"";
        return result + " @ " + mySource;
    }
}
