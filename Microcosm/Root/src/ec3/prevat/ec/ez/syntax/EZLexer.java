package ec.ez.syntax;

import ec.ez.runtime.AlreadyDefinedException;
import ec.ez.runtime.SourceSpan;
import java.io.DataInput;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.io.BufferedReader;
import java.io.InputStreamReader;


/**
 *
 */
public class EZLexer {

    static public final int EOF = -1;

    /** what string represents the source text? */
    private String mySourceURL;

    /** contains everything after the current line */
    private BufferedReader myDataInput;

    /** the current line, or null at end-of-file */
    private String myLine;

    /** number of current line */
    private int myLineNo;

    /**
     * Total characters before current line, assuming a newline is
     * one character.
     */
    private int myPrev;

    /** position in current line of candidate character */
    private int myPos;

    /** the candidate character, or EOF for end-of-file. */
    private int myChar;

    /** 
     * Total characters before the current token, or -1 if no
     * current token 
     */
    private int myStartPos = -1;

    /** 
     * Line number where current token starts, or -1 if no current
     * token 
     */
    private int myStartLineNo = -1;

    /**
     * Accumulates all text of the current token from lines before the
     * current line, or null if no current token or if the current
     * token starts on the current line.  The EOL token itself is not
     * considered to be a line spanning token.
     */
    private StringBuffer myOptStartText = null;

    private void startToken() {
        if (myStartLineNo != -1) {
            throw new Error("internal: token already started");
        }
        myStartPos = myPrev + myPos;
        myStartLineNo = myLineNo;
    }

    private SourceSpan endToken() {
        return endToken(false);
    }

    private SourceSpan endToken(boolean withText) {
        if (myStartLineNo == -1) {
            throw new Error("internal: no current token");
        }
        String text = null;
        if (withText) {
            if (myStartLineNo == myLineNo) {
                text = myLine.substring(myStartPos - myPrev, myPos);
            } else {
                String subtext = myLine.substring(0, myPos);
                text = myOptStartText.append(subtext).toString();
            }
        }
        SourceSpan result = new SourceSpan(mySourceURL,
                                           myStartPos, myPrev + myPos,
                                           myStartLineNo,
                                           text);
        myStartPos = -1;
        myStartLineNo = -1;
        myOptStartText = null;
        return result;
    }

    /**
     *
     */
    public EZLexer(String sourceURL, 
                   InputStream dataInput) throws IOException
    {
        this(sourceURL, 0, 0, dataInput);
    }

    /**
     *
     */
    public EZLexer(String sourceURL, 
                   int prevChars,
                   int prevLines,
                   InputStream dataInput) throws IOException
    {
        mySourceURL = sourceURL;
        myDataInput = new BufferedReader(new InputStreamReader(dataInput));
        myPrev = prevChars;
        myPos = -1;
        myLineNo = prevLines + 1; //line numbers are 1-index-origin
        prompt();
        myLine = myDataInput.readLine();
        nextChar();
    }

    /** pretty self explanatory */
    public boolean isEndOfFile() {
        return myLine == null;
    }

    private void nextChar() throws IOException {
        while (true) {
            if (myLine == null) {
                myChar = EOF;
                return;
            }
            myPos++;
            int len = myLine.length();
            if (myPos < len) {
                myChar = myLine.charAt(myPos);
                return;
            } else if (myPos == len) {
                myChar = '\n';
                return;
            } else {
                nextLine();
            }
        }
    }

    private int peekChar() {
        if (myChar == EOF || myChar == '\n') {
            throw new Error("internal: can't peek here");
        }
        int last = myLine.length() -1;

        if (myPos < last) {
            return myLine.charAt(myPos + 1);

        } else if (myPos == last) {
            return '\n';
        } else {
            throw new Error("internal: boy am I confused");
        }
    }

    private void nextLine() throws IOException {
        if (myLine == null) {
            myChar = EOF;
            return;
        }
        if (myStartLineNo != -1) {
            if (myStartLineNo == myLineNo) {
                myOptStartText = new StringBuffer();
                myOptStartText.append(myLine.substring(myStartPos - myPrev));
            } else {
                myOptStartText.append(myLine);
            }
            myOptStartText.append("\n");
        }
        myLineNo++;
        myPrev += myLine.length() +1; //for the newline
        myPos = -1;
        prompt();
        myLine = myDataInput.readLine();
        myChar = '\n';
    }

    private void skipLine() {
        if (myLine != null) {
            myPos = myLine.length();
        }
    }

    /**
     * Does nothing.  Exists for subclasses to override.  Called right
     * before waiting for a new line of input.
     */
    protected void prompt() {}


    private void skipWhiteSpace() throws IOException {
        while (true) {
            if (myChar == EOF || myChar == '\n') {
                return;
            }
            if (! Character.isWhitespace((char)myChar)) {                
                return;
            }
            nextChar();
        }
    }

    public EZToken nextToken() throws IOException, SyntaxException {
        EZToken result;
        try {
            result = getNextToken();
        } catch (SyntaxException ex) {
            //XXX report place in source
            throw ex;
        }
        return result;
    }

    private EZToken getNextToken() throws IOException, SyntaxException {
        skipWhiteSpace();
        startToken();

        switch(myChar) {
            case EOF: {
                return new EZToken(endToken(), EZParser.EOF);
            } case '\n': {
                nextChar();
                return new EZToken(endToken(), EZParser.EOL);
            }
            case '(':
            case ')':
            case '{':
            case '}':
            case ';':
            case ',':
            case '[':
            case ']':
            case '~': {
                char c = (char)myChar;
                nextChar();
                return new EZToken(endToken(), c);
            }
            case '.': {
                nextChar();
                if (myChar == '.') {
                    nextChar();
                    if (myChar == '!') {
                        nextChar();
                        return new EZToken(endToken(), EZParser.OpTill);
                    }
                    return new EZToken(endToken(), EZParser.OpThru);
                } else {
                    return new EZToken(endToken(), '.');
                }
            }
            case '^': {
                nextChar();
                if (myChar == '=') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.OpAssXor);
                } else {
                    return new EZToken(endToken(), '^');
                }
            }
            case '+': {
                nextChar();
                if (myChar == '=') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.OpAssAdd);
                } else {
                    return new EZToken(endToken(), '+');
                }
            }
            case '-': {
                nextChar();
                if (myChar == '=') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.OpAssSub);
                } else {
                    return new EZToken(endToken(), '-');
                }
            }

            case ':': {
                nextChar();
                if (myChar == '=') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.OpAss);
                } else {
                    return new EZToken(endToken(), ':');
                }
            } case '<': {
                nextChar();
                if (myChar == '-') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.Send);
                } else if (myChar == '=') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.OpLeq);
                } else if (myChar == '<') {
                    nextChar();
                    if (myChar == '=') {
                        nextChar();
                        return new EZToken(endToken(), EZParser.OpAssAsl);
                    }
                    return new EZToken(endToken(), EZParser.OpAsl);
                } else {
                    return new EZToken(endToken(), '<');
                }
            } case '>': {
                nextChar();
                if (myChar == '=') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.OpGeq);
                } else if (myChar == '>') {
                    nextChar();
                    if (myChar == '=') {
                        nextChar();
                        return new EZToken(endToken(), EZParser.OpAssAsr);
                    }
                    return new EZToken(endToken(), EZParser.OpAsr);
                } else {
                    return new EZToken(endToken(), '>');
                }
            } case '*': {
                nextChar();
                if (myChar == '*') {
                    nextChar();
                    if(myChar == '=') {
                        nextChar();
                        return new EZToken(endToken(), EZParser.OpAssPow);
                    }
                    return new EZToken(endToken(), EZParser.OpPow);
                } else {
                    if(myChar == '=') {
                        nextChar();
                        return new EZToken(endToken(), EZParser.OpAssMul);
                    }
                   return new EZToken(endToken(), '*');
                }
            } case '/': {
                nextChar();
                if (myChar == '=') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.OpAssAprxDiv);
                }
                if (myChar != '/') {
                    return new EZToken(endToken(), '/');
                }
                // Skip comment to end of line
                skipLine();
                nextChar();
                return new EZToken(endToken(), EZParser.EOL);
            } case '#': {
                // Skip comment to end of line (as in "//" case above).
                skipLine();
                nextChar();
                return new EZToken(endToken(), EZParser.EOL);
            } case '_': {
                nextChar();
                if (myChar == '/') {
                    nextChar();
                    if (myChar == '=') {
                        nextChar();
                        return new EZToken(endToken(), EZParser.OpAssFlrDiv);
                    }
                    return new EZToken(endToken(), EZParser.OpFlrDiv);
                } else {
                    return new EZToken(endToken(), '_');
                }
            } case '%': {
                nextChar();
                if (myChar == '%') {
                    nextChar();
                    if(myChar == '=') { // check for "%%="
                        nextChar();
                        return new EZToken(endToken(), EZParser.OpAssMod);
                    } else
                        return new EZToken(endToken(), EZParser.OpMod);
                } else if(myChar == '=') { // check for "%="
                     nextChar();
                     return new EZToken(endToken(), EZParser.OpAssRemdr);
                } else {
                    return new EZToken(endToken(), '%');
                }
            } case '!': {
                nextChar();
                if (myChar == '=') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.OpNeq);
                } else if (myChar == '~') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.MisMatch);
                } else {
                    return new EZToken(endToken(), '!');
                }
            } case '=': {
                nextChar();
                if (myChar == '=') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.OpEq);
                } else if (myChar == '>') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.MapsTo);
                } else if (myChar == '~') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.MatchBind);
                } else {
                    throw new SyntaxException
                    ("use ':=' for assignment, or '==' for equality");
                }
            } case '&': {
                nextChar();
                if (myChar == '&') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.OpLAnd);
                } else if (myChar == '=') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.OpAssAnd);
                } else {
                    return new EZToken(endToken(), '&');
                }
            } case '|': {
                nextChar();
                if (myChar == '|') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.OpLOr);
                } else if (myChar == '=') {
                    nextChar();
                    return new EZToken(endToken(), EZParser.OpAssOr);
                } else {
                    return new EZToken(endToken(), '|');
                }
            } case '\'': {
                return charLiteral();
            } case '"': {
                return stringLiteral();
            } case '`': {
                return quasiString();
            } case '$': {
                return quasiValue();
            } case '@': {
                return quasiParam();
            }
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9': {
                return numberLiteral();

            } default: {
                if (Character.isJavaIdentifierStart((char)myChar)) {
                    return identifier();
                } else {
                    throw new SyntaxException("unrecognized character");
                }
            }
        }
    }

    private char charConstant() throws IOException, SyntaxException {
        if (myChar == '\\') {
            nextChar();
            switch(myChar) {
                case 'b': return '\b';
                case 't': return '\t';
                case 'n': return '\n';
                case 'f': return '\f';
                case 'r': return '\r';
                case '"': return '"';
                case '\'': return '\'';
                case '\\': return '\\';
                case '$': return '$';
                case '@': return '@';
                case '\n': {
                    nextChar();
                    return charConstant();
                }
                case EOF: {
                    new SyntaxException("End of file in middle of literal");
                } 
                default: {
                    if (Character.isDigit((char)myChar)) {
                        throw new SyntaxException
                            ("escaped char codes not yet implemented");
                    } else {
                        throw new SyntaxException
                            ("Unrecognized escaped character");
                    }
                }
            }
        } else if (myChar == EOF) {
            throw new SyntaxException("End of file in middle of literal");
        } else {
            return (char)myChar;
        }
    }

    private EZToken charLiteral() throws IOException, SyntaxException {
        nextChar();
        char value = charConstant();
        nextChar();
        if (myChar != '\'') {
            throw new SyntaxException("char constant must end in \"'\"");
        }
        nextChar();
        return new EZLiteral(endToken(true), new Character(value));
    }

    private EZToken stringLiteral() throws IOException, SyntaxException {
        nextChar();
        StringBuffer value = new StringBuffer();
        while (myChar != '"') {
            value.append(charConstant());
            nextChar();
        }
        nextChar();
        return new EZLiteral(endToken(true), value.toString());
    }

    private EZToken quasiString() throws IOException, SyntaxException {
        nextChar();
        StringBuffer value = new StringBuffer();
        while (true) {
            while (myChar != '`') {
                value.append((char)myChar);
                nextChar();
            }
            nextChar();
            if (myChar == '`') {
                value.append((char)myChar);
                nextChar();
            } else {
                break;
            }
        }
        return new EZQuasiString(endToken(true), value.toString());
    }

    private EZToken quasiValue() throws IOException, SyntaxException {
        throw new Error("XXX $n not yet implemented");
    }

    private EZToken quasiParam() throws IOException, SyntaxException {
        throw new Error("XXX @n not yet implemented");
    }

    private void getDecimalInteger()  throws IOException, SyntaxException {
        if (! Character.isDigit((char)myChar)) {
            throw new SyntaxException("digit expected");
        }
        do {
            nextChar();
        } while (myChar != EOF
                 && Character.isDigit((char)myChar));
    }

    private EZToken numberLiteral()
         throws IOException, SyntaxException 
    {
        // Now handles floating point numbers as well as integers
        boolean floating = false;
        getDecimalInteger();
        // If we have a decimal point go for the fractional part
        if (myChar == '.' && peekChar() != '.') {
            nextChar();
            floating = true;
            getDecimalInteger();
        }

        if ((myChar == 'E') || (myChar == 'e')) {
             nextChar();
             floating = true;
             if (myChar == '-') {
                nextChar();
             }
             getDecimalInteger();
        }

        SourceSpan source = endToken(true);
        String text = source.text();
        if (floating) {
            return new EZLiteral(source, Double.valueOf(text));
        } else {
            //XXX should eventually accomodate radixes besides 10
            return new EZLiteral(source, new BigInteger(text, 10));
        }
    }

    private EZToken identifier() throws IOException, SyntaxException {
        do {
            nextChar();
        } while (myChar != EOF
                 && Character.isJavaIdentifierPart((char)myChar));
        return EZIdentifier.make(endToken(true));
    }

    /**
     * Report the exception in association with the current position, and
     * skip the rest of the line.
     */
    public void diagnostic(Throwable ex, PrintStream errs) {
        //XXX must make this switchable
        ex.printStackTrace(errs);

        errs.println(ex.getMessage());
        if (mySourceURL != null && mySourceURL.length() > 0) {
            errs.print(mySourceURL + ", " + myLineNo + ": ");
        }
        errs.println(ex);
        if (myLine != null) {
            errs.println(myLine);
            String space = "";
            for (int i = 0; i < myPos -1; i++) {
                space += " ";
            }
            errs.println(space + "^");
        }
        myStartPos = -1;
        myStartLineNo = -1;
        myOptStartText = null;
        skipLine();
        try {
            nextChar();
        } catch (IOException ex2) {
            throw new RuntimeException("while skipping line " + ex2);
        }
    }

    static public void main(String[] args)
         throws IOException, SyntaxException {

        if (args.length != 1) {
            throw new RuntimeException
                ("usage: java ec.ez.syntax.EZLexer file");
        }
        InputStream ins = new FileInputStream(args[0]);
        ins = new BufferedInputStream(ins); //XXX bad if interactive
        EZLexer lex = new EZLexer(args[0], new DataInputStream(ins));
        while (true) {
            try {
                EZToken t;
                do {
                    t = lex.nextToken();
                    System.out.println(t + " ");
                } while (t.tokenType() != EZParser.EOF);
                return;
            } catch (SyntaxException ex) {
                lex.diagnostic(ex, System.err);
            }
        }
    }

    /** 
     * trialParse does a simple count of brackets, etc.
     * to see if the input given is balanced and therefore
     * ready for submission to the parser.
     */
    public boolean trialParse() {
        EZToken t = null;
        int braceCount = 0;
        int theTokenType = 0;
        do {
            try {
                try {
                    t = nextToken();
                } catch (SyntaxException e) {
                    nextChar();
                }
            } catch (Exception e) {
                return(false);
            }
            if (t != null) {
                theTokenType = t.tokenType();
                if (theTokenType == '{') {
                    braceCount++;
                } else if (theTokenType == '}') {
                    braceCount--;
                }
            } else { // t is null
                theTokenType = 0;
            }
        } while (theTokenType != EZParser.EOF);
        return braceCount == 0;
    }
}

