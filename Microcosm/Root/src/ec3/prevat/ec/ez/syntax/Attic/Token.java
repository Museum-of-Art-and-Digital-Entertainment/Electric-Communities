package ec.ez.syntax;

import ec.ez.runtime.AlreadyDefinedException;
import ec.ez.runtime.SourceSpan;
import java.util.Hashtable;


/**
 *
 */
public class Token {

    /* token types, kernel language first */
    static public final int IDENT               = 0;
    static public final int LITERAL_STRING      = 1;
    static public final int OTHER_LITERAL       = 2;
    static public final int EOF                 = 3;
    static public final int OPEN_PAREN          = 4;
    static public final int CLOSE_PAREN         = 5;
    static public final int OPEN_CURLY          = 6;
    static public final int CLOSE_CURLY         = 7;
    static public final int ASSIGN              = 8;
    static public final int COLON               = 9;
    static public final int ESEND               = 10;
    static public final int SEMI_COLON          = 11;
    static public final int COMMA               = 12;
    static public final int NEWLINE             = 13;

    /* kernel language keywords */
    static public final int DEFINE              = 14;
    static public final int ESCAPE              = 15;
    static public final int LOOP                = 16;
    static public final int TRY                 = 17;
    static public final int CATCH               = 18;
    static public final int FINALLY             = 19;
    static public final int THROW               = 20;
    static public final int POV                 = 21;
    static public final int ENTER_POV           = 22;
    static public final int DISPATCH            = 23;
    static public final int TO                  = 24;
    static public final int FOR_REQUEST         = 25;

    /* sugar */

    /*
     * MarkM didn't make the <binop>"=" form into a single token, but
     * neither did he make "=" into a token, so he had no way to
     * recognize the <binop>"=" forms.  If you tried to deal with this
     * by making "=" into a separate token, you'd no longer be able to
     * recognize the grammar with only one token of lookahead, so
     * instead I'm making each valid <binop>"=" form into a single
     * token.
     *
     * To ease their handling, I'm grouping all such binop tokens
     * and their corresponding assignment tokens together, so we can
     * do a subrange check to see that we've got one, and calculate
     * its corresponding token by adding or subtracting one.
     *
     * Rather unbelievable that Java doesn't have "enum", isn't it?
     */

    static public final int OPEN_SQUARE         = 26;
    static public final int CLOSE_SQUARE        = 27;
    static public final int OPEN_ANGLE          = 28;
    static public final int CLOSE_ANGLE         = 29;
    static public final int COMPLEMENT          = 30;
    static public final int NOT                 = 31;
    static public final int LEQ                 = 32;
    static public final int GEQ                 = 33;
    static public final int EQUALS              = 34;
    static public final int NOT_EQUALS          = 35;
    static public final int COND_AND            = 36;
    static public final int COND_OR             = 37;
    static public final int DOT                 = 38;

    /* assignables */
    static public final int MINUS               = 39;
    static public final int MINUS_ASN           = 39 + 1;
    static public final int PLUS                = 41;
    static public final int PLUS_ASN            = 41 + 1;
    static public final int TIMES               = 43;
    static public final int TIMES_ASN           = 43 + 1;
    static public final int APRX_DIVIDE         = 45;
    static public final int APRX_DIVIDE_ASN     = 45 + 1;
    static public final int REMAINDER           = 47;
    static public final int REMAINDER_ASN       = 47 + 1;
    static public final int FLOOR_DIVIDE        = 49;
    static public final int FLOOR_DIVIDE_ASN    = 49 + 1;
    static public final int MODULO              = 51;
    static public final int MODULO_ASN          = 51 + 1;
    static public final int POW                 = 53;
    static public final int POW_ASN             = 53 + 1;
    static public final int LEFT_SHIFT          = 55;
    static public final int LEFT_SHIFT_ASN      = 55 + 1;
    static public final int RIGHT_SHIFT         = 57;
    static public final int RIGHT_SHIFT_ASN     = 57 + 1;
    static public final int BIT_AND             = 59;
    static public final int BIT_AND_ASN         = 59 + 1;
    static public final int BIT_XOR             = 61;
    static public final int BIT_XOR_ASN         = 61 + 1;
    static public final int BIT_OR              = 63;
    static public final int BIT_OR_ASN          = 63 + 1;

    static private final int FIRST_ASNABLE      = MINUS;
    static private final int LAST_ASNABLE       = BIT_OR_ASN;

    /* EZ is a typeless language whose integer arithmetic is
     * eventually supposed to be unlimited in precision.  In this
     * case, ">>>" makes no sense. Hence, it's gone
     */

    /* sugar keywords */

    static public final int OBJECT              = 65;
    static public final int IF                  = 66;
    static public final int ELSE                = 67;
    static public final int WHILE               = 68;
    static public final int FOR                 = 69;
    static public final int IN                  = 70;
    static public final int DO                  = 71;

    static public final String[] TheTokens = {
        null, null, null, "",
        "(", ")", "{", "}", ":=", ":", "<-", ";", ",", "\n",
        "define", "escape", "loop", "try", "catch", "finally",
        "throw", "pov", "enterPov", "dispatch", "to", "forRequest",

        "[", "]", "<", ">", "~", "!", "<=", ">=", "==", "!=",
        "&&", "||", ".",

        "-", "-=", "+", "+=", "*", "*=", "/", "/=", "%", "%=",
        "//", "//=", "%%", "%%=", "**", "**=",
        "<<", "<<=", ">>", ">>=",
        "&", "&=", "^", "^=", "|", "|=",

        "object", "if", "else", "while", "for", "in", "do"
    };

    static private Hashtable tokenTable(String[] tokens) {
        Hashtable result = new Hashtable();
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] != null
                && result.put(tokens[i], new Integer(i)) != null) {
                throw new IllegalArgumentException
                     ("conflicting token definitions");
            }
        }
        return result;
    }

    static public final Hashtable TheTokenTable = tokenTable(TheTokens);

    private SourceSpan myOptSource;
    private int myTokenType;

    public Token(int tokenType) {
        myOptSource = null;
        myTokenType = tokenType;
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

    public int tokenType() { return myTokenType; }

    public String token() {
        String result = TheTokens[myTokenType];
        if (result == null) {
            throw new RuntimeException
                ("variable tokens should override this method");
        }
        return result;
    }

    public String toString() {
        return tokenType() + ": \"" + token() + "\"";
    }

    /* encapsulate the binop arithmetic calculations */

    /**
     * A <binop> such that <binop>"=" is valid
     */
    public boolean isAssignable() {
        return (myTokenType >= FIRST_ASNABLE
                && myTokenType <= LAST_ASNABLE
                && ((myTokenType - FIRST_ASNABLE) & 1) == 0);
    }

    /**
     * A <binop>"=" such that <binop> is valid
     */
    public boolean isAssignOp() {
        return (myTokenType >= FIRST_ASNABLE
                && myTokenType <= LAST_ASNABLE
                && ((myTokenType - FIRST_ASNABLE) & 1) == 1);
    }

    /**
     * The <binop>"=" from the <binop>
     */
    public Token asAssignOp() {
        if (!isAssignable()) {
            throw new Error(token() + " is not assignable");
        }
        return new Token(myTokenType + 1);
    }

    /**
     * The <binop> from the <binop>"="
     */
    public Token asAssignable() {
        if (!isAssignOp()) {
            throw new Error(token() + " is not a binary assignment op");
        }
        return new Token(myTokenType - 1);
    }
}
