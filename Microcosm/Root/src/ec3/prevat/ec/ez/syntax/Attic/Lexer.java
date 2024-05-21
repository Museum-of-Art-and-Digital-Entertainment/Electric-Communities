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


/**
 *
 */
public class Lexer {

    static public final int EOF = -1;

    /** what string represents the source text? */
    private String mySourceURL;

    /** contains everything after the current line */
    private DataInput myDataInput;

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

    public Lexer(String sourceURL, DataInput dataInput) throws IOException {
        mySourceURL = sourceURL;
        myDataInput = dataInput;
        myLineNo = 1;
        myPrev = 0;
        myPos = -1;
        prompt();
        myLine = myDataInput.readLine();
        nextChar();
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

    private void nextLine() throws IOException {
        if (myLine == null) {
            myChar = EOF;
            return;
        }
        myLineNo++;
        myPrev += myLine.length() +1; //for the newline
        myPos = -1;
        prompt();
        myLine = myDataInput.readLine();
    }


    /**
     * Does nothing.  Exists for subclasses to override.  Called right
     * before waiting for a new line of input.
     */
    protected void prompt() {}

    private void skipWhiteSpace() throws IOException {
        while(myChar != EOF
              && Character.isSpace((char)myChar)
              && myChar != '\n') {
            nextChar();
        }
        if (myChar == '#') {    //scripting language comment convention
            nextLine();
        }
    }

    public Token nextToken() throws IOException, SyntaxException {
        skipWhiteSpace();
        //Where in file does current token start, assuming a newline is
        //one character?
        int sourceStart = myPrev + myPos;
        int lineNo = myLineNo;
        Token result;
        try {
            result = getNextToken();
        } catch (SyntaxException ex) {
            //XXX report place in source
            throw ex;
        }

        /*
         * Factor out the binop assignment recognition
         */
        if (myChar == '=' && result.isAssignable()) {
            nextChar();
            result = result.asAssignOp();
        }

        try {
            result.defineSource(new SourceSpan(mySourceURL,
                                               sourceStart, myPrev + myPos,
                                               lineNo));
        } catch (AlreadyDefinedException ex) {
            throw new Error("internal error: multiply initialized");
        }
        return result;
    }

    private Token getNextToken() throws IOException, SyntaxException {
    while (myLine != null) {
        switch(myChar) {
        case EOF: {
            return new Token(Token.EOF);
        } case '\n': {
            nextChar();
            return new Token(Token.NEWLINE);
        } case '(': {
            nextChar();
            return new Token(Token.OPEN_PAREN);
        } case ')': {
            nextChar();
            return new Token(Token.CLOSE_PAREN);
        } case '{': {
            nextChar();
            return new Token(Token.OPEN_CURLY);
        } case '}': {
            nextChar();
            return new Token(Token.CLOSE_CURLY);
        } case ':': {
            nextChar();
            if (myChar == '=') {
                nextChar();
                return new Token(Token.ASSIGN);
            } else {
                return new Token(Token.COLON);
            }
        } case '<': {
            nextChar();
            if (myChar == '-') {
                nextChar();
                return new Token(Token.ESEND);
            } else if (myChar == '=') {
                nextChar();
                return new Token(Token.LEQ);
            } else if (myChar == '<') {
                nextChar();
                return new Token(Token.LEFT_SHIFT);
            } else {
                return new Token(Token.OPEN_ANGLE);
            }
        } case ';': {
            nextChar();
            return new Token(Token.SEMI_COLON);
        } case ',': {
            nextChar();
            return new Token(Token.COMMA);
        } case '[': {
            nextChar();
            return new Token(Token.OPEN_SQUARE);
        } case ']': {
            nextChar();
            return new Token(Token.CLOSE_SQUARE);
        } case '>': {
            nextChar();
            if (myChar == '=') {
                nextChar();
                return new Token(Token.GEQ);
            } else if (myChar == '>') {
                nextChar();
                return new Token(Token.RIGHT_SHIFT);
            } else {
                return new Token(Token.CLOSE_ANGLE);
            }
        } case '-': {
            nextChar();
            return new Token(Token.MINUS);
        } case '+': {
            nextChar();
            return new Token(Token.PLUS);
        } case '*': {
            nextChar();
            if (myChar == '*') {
                nextChar();
                return new Token(Token.POW);
            } else {
                return new Token(Token.TIMES);
            }
        } case '/': {
            nextChar();
            if (myChar != '/') {
                return new Token(Token.APRX_DIVIDE);
            }
        // Skip comment to end of line
            nextLine();  // fall into while loop,
            break; // which should seek the token on the next line
        } case '_': {
            nextChar();
            if (myChar == '/') {
                nextChar();
                return new Token(Token.FLOOR_DIVIDE);
            } else {
                throw new SyntaxException("unrecognized character");
            }
        }

          case '%': {
            nextChar();
            if (myChar == '%') {
                nextChar();
                return new Token(Token.MODULO);
            }
            return new Token(Token.REMAINDER);
        } case '~': {
            nextChar();
            return new Token(Token.COMPLEMENT);
        } case '!': {
            nextChar();
            if (myChar == '=') {
                nextChar();
                return new Token(Token.NOT_EQUALS);
            } else {
                return new Token(Token.NOT);
            }
        } case '=': {
            nextChar();
            if (myChar == '=') {
                nextChar();
                return new Token(Token.EQUALS);
            } else {
                throw new SyntaxException("use ':=' or '=='");
            }
        } case '&': {
            nextChar();
            if (myChar == '&') {
                nextChar();
                return new Token(Token.COND_AND);
            } else {
                return new Token(Token.BIT_AND);
            }
        } case '^': {
            nextChar();
            return new Token(Token.BIT_XOR);
        } case '|': {
            nextChar();
            if (myChar == '|') {
                nextChar();
                return new Token(Token.COND_OR);
            } else {
                return new Token(Token.BIT_OR);
            }
        } case '.': {
            nextChar();
            return new Token(Token.DOT);
        } case '\'': {
            return charLiteral();
        } case '"': {
            return stringLiteral();
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
      } // *END* while (myLine != null)
      return new Token(Token.EOF);
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
            case EOF: {
                new SyntaxException("End of file in middle of literal");
            } default: {
                if (Character.isDigit((char)myChar)) {
                    throw new RuntimeException
                    ("escaped char codes not yet implemented");
                } else {
                    throw new SyntaxException
                        ("Unrecognized escaped character");
                }
            }
            }
        } else if (myChar == EOF) {
            throw new SyntaxException("End of file in middle of literal");
        } else if (myChar == '\n') {
            throw new SyntaxException
                ("No newlines inside literals, yet.  Soon.");
        } else {
            return (char)myChar;
        }
    }

    private Token charLiteral() throws IOException, SyntaxException {
        int start = myPos;
        nextChar();
        char value = charConstant();
        nextChar();
        if (myChar != '\'') {
            throw new SyntaxException("char constant must end in \"'\"");
        }
        nextChar();
        return new Literal(myLine.substring(start, myPos),
                           new Character(value));
    }

    private Token stringLiteral() throws IOException, SyntaxException {
        int start = myPos;
        nextChar();
        StringBuffer value = new StringBuffer();
        while (myChar != '"') {
            value.append(charConstant());
            nextChar();
        }
        nextChar();
        return new Literal(myLine.substring(start, myPos),
                           value.toString());
    }

    private void getDecimalInteger()  throws IOException, SyntaxException {
        do {
            nextChar();
        } while (myChar != EOF
                 && Character.isDigit((char)myChar));
    }

    private Token numberLiteral() throws IOException, SyntaxException {
        // Now handles floating point numbers as well as integers
        int start = myPos;
        boolean floating = false;
        getDecimalInteger();
        // If we have a decimal point go for the fractional part
        if (myChar == '.') {
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

        String litstr = myLine.substring(start, myPos);
        if (floating) {
            return new Literal(litstr, Double.valueOf(litstr));
        } else {
            //XXX should eventually accomodate radixes besides 10
            return new Literal(litstr, new BigInteger(litstr, 10));
        }
    }

    private Token identifier() throws IOException, SyntaxException {
        int start = myPos;
        do {
            nextChar();
        } while (myChar != EOF
                 && Character.isJavaIdentifierPart((char)myChar));
        return Identifier.make(myLine.substring(start, myPos));
    }

    /**
     * Report the exception in association with the current position, and
     * skip the rest of the line.
     */
    public void diagnostic(Throwable ex, PrintStream errs) {
 //       ex.printStackTrace(errs);
        errs.println(mySourceURL + ", " + myLineNo + ": " + ex);
        errs.println(myLine);
        String space = "";
        for (int i = 0; i < myPos -1; i++) {
            space += " ";
        }
        errs.println(space + "^");
        try {
            nextLine();
        } catch (IOException ex2) {
            throw new RuntimeException("while skipping line " + ex2);
        }
    }

    static public void main(String[] args)
         throws IOException, SyntaxException {

        if (args.length != 1) {
            throw new RuntimeException("usage: java ec.ez.syntax.Lexer file");
        }
        InputStream ins = new FileInputStream(args[0]);
        ins = new BufferedInputStream(ins); //XXX bad if interactive
        Lexer lex = new Lexer(args[0], new DataInputStream(ins));
        Token t;
        do {
            t = lex.nextToken();
            System.out.println(t + " "); // only for lexer testing.
        } while (t.tokenType() != Token.EOF);
    }

// trialParse does a simple count of brackets, etc.
// to see if the input given is balanced and therefore
// ready for submission to the parser.
    public boolean trialParse() {
        Token t = null;
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
                if (theTokenType == Token.OPEN_CURLY) {
                    braceCount++;
                } else if (theTokenType == Token.CLOSE_CURLY) {
                    braceCount--;
                }
            } else { // t is null
                theTokenType = 0;
            }
        } while (theTokenType != Token.EOF);
        return braceCount == 0;
    }
}

