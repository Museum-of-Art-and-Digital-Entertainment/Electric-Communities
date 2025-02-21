package ec.ez.syntax;

import ec.ez.runtime.*;
import ec.ez.collect.*;
import ec.ez.prim.EZStaticWrapper;
import ec.ez.ezvm.*;
import java.io.*;
import java.util.Vector;
import java.math.BigInteger;


/**
 * XXX there should plobably be an overview of the parser's structure
 *  & modus operandi here.
 */
public class Parser {

    static private final Expr SELF_NODE = new NounExpr("self");

    static private final Expr[] NO_ARGS = {};
    static private final String[] NO_PARAMS = {};


    /**********************************************************************
     *
     * some useful constant parse trees, to help some of the parser bits
     *
     **********************************************************************/

    static private final Expr
        EZ_TRUE_THUNK = Parser.thunkify(new LiteralExpr("true", Boolean.TRUE));
    static private final Expr
        EZ_FALSE_THUNK = Parser.thunkify(new LiteralExpr("false", Boolean.FALSE));
    static private final Expr
        EZ_NULL_THUNK = Parser.thunkify(new LiteralExpr("null", null));

    static private final Class
        TUPLE_IMPL_CLASS = TupleImpl.run().getClass();
    static private final Expr
        TUPLE_EXPR = new LiteralExpr("Tuple", new EZStaticWrapper(TUPLE_IMPL_CLASS));

    static private final Expr
        PROMISE_EXPR = new LiteralExpr("Promise", null/*XXX*/);
    static private final Expr
        NEW_PROMISE = new CallExpr(PROMISE_EXPR, "new");

    static private final Expr
        ZERO_EXPR = new LiteralExpr("0", BigInteger.valueOf(0));
    static private final Expr
        ONE_EXPR = new LiteralExpr("1", BigInteger.valueOf(1));


    /**********************************************************************
     *
     * The foundations of the parser, the current token, token handling
     * methods, contructors etc.
     *
     **********************************************************************/

    /** contains all the tokens after myToken */
    private Lexer myLexer;

    /** the current token */
    private Token myToken;

    /** generated temp variable count */
    private int myTempCount = 0;

    private PrintStream myDebuggingSpamStream = null;

    public Parser(Lexer lexer) throws IOException, SyntaxException {
        myLexer = lexer;
        nextToken();
    }

    public Parser(Lexer l, PrintStream debuggingSpam)
            throws IOException, SyntaxException {
        this(l);
        myDebuggingSpamStream = debuggingSpam;
    }

    public Parser(String sourceURL, String sourceCode)
         throws IOException, SyntaxException {
        this(new Lexer(sourceURL,
                       new DataInputStream(
                                   new StringBufferInputStream(sourceCode))));
    }

    public Parser(String sourceURL, String sourceCode,
            PrintStream debugStream) throws IOException, SyntaxException {
        this(new Lexer(sourceURL,
                       new DataInputStream(
                                   new StringBufferInputStream(sourceCode))));
        myDebuggingSpamStream = debugStream;
    }


    private void debugMethodPrint(String s) {
        if (myDebuggingSpamStream != null) {
            myDebuggingSpamStream.println(s);
        }
    }

    private void debugPrint(String s) {
        if (myDebuggingSpamStream != null) {
            myDebuggingSpamStream.println(s);
        }
    }



    /**********************************************************************
     *
     * Stuff to handle debugging. extra constructors & methods. etc
     *
     **********************************************************************/


    /* throws syntaxEx if an unrecognised character is found while
     * building a token. */
    private void nextToken() throws IOException, SyntaxException {

        debugPrint("Done with token ("+myToken+")");

        myToken = myLexer.nextToken();
        String s = myToken.token();
        debugPrint("  Now considering ("+myToken
            +")" +((s.length() > 0) ? " Token[0] = "+((int)(s.charAt(0))) : "") );
    }



    /**********************************************************************
     *
     * Utility function etc to deal with the occasions where the parser
     * needs temporary variables. I think this relies on the users not
     * trying te create matching variable names, but i guess that's a note
     * on style of programming rather than anything else.
     *
     * they are numbered, n, and named "temp$n"
     *
     * these are used in, amongst other places, tuple assignment.
     *
     **********************************************************************/


    /* generate 'unique' temporary variable names for transformations
     */
    private NounExpr newTemp() {
        return new NounExpr("temp$" + myTempCount++);
    }




    /**********************************************************************
     *
     * parseCommand is the bit that deals with the syntactic sugar notion
     * of commands being something different so that they may be verb first.
     * Otherwise they reduce to statements. <p>
     * <pre>
     *      command ::= request          => "self" request
     *               |  statement
     * </pre>
     **********************************************************************/

    public Expr parseCommand() throws IOException, SyntaxException {
        debugMethodPrint("parseCommand");
        if (myToken.tokenType() == Token.DOT) {
            RequestNode request = parseRequest();
            debugMethodPrint("  -- end Command");
            return new CallExpr(SELF_NODE, request);
        } else {
            Expr result = parseExpr(); // was parseStatement();
            debugMethodPrint("  -- end Command");
            return result;
        }
    }




    /**********************************************************************
     *
     * Here is the bits which deal with expressions.
     *
     * operator precedence is handled in this collection of methods, there
     * is not a distinction between kernel expressions and sugary expressions
     * in fact the only kernel expressions are sequencing, messaging (in its
     * many forms), generating nouns, and any kernel statements. everything
     * else (ie. most of this expression parsing stuff) collapses into
     * message sends of appropriate predefined verbs, and fragments of
     * handcrafted parse trees to perform the other operations.
     *
     **********************************************************************/

    /**
     * this just starts parsing at the top of the chain.<p>
     * <pre>
     *      expr ::= expr17L
     * </pre>
     */
    private Expr parseExpr() throws IOException, SyntaxException {
        debugMethodPrint("parseExpr");
        return parseExpr17L();
    }

    /**
     * If we have an IDENT here, then it is a noun. It is not a verb a la
     * 'verb first' stuff, since that is caught by 'command's. <p>
     * <pre>
     *      expr2L ::= noun
     *              | statement
     *              | expr2L "." noun       => expr2L "get" literal-name-of-noun
     *   such that
     *      x[y].z
     *   expands to
     *      (x get y) get "z"
     * </pre>
     */
    private Expr parseExpr2L() throws IOException, SyntaxException {
        debugMethodPrint("parseExpr2L");

        String idString;
        if (myToken.tokenType() == Token.IDENT) {
            idString = parseIdentifier();
        }
        else // Jay's experiment at using dot to mean self.
         if (myToken.tokenType() == Token.DOT) {
            nextToken();
            return SELF_NODE;
         }
          else {
              return parseStatement();;
        }

        if(myToken.tokenType() != Token.DOT) {
            return new NounExpr(idString);
        } else
           while (eatIf(Token.DOT)) {
            String ident = expectIdentifier("identifier must follow period");
            idString = idString + "." + ident;
            //the quotes trick works only because identifier do not contain characters
            //that need to be escaped
        }

        return new PathExpr(idString);
    }

    /**
     * <pre>
     *      expr3X ::= "-" expr2L       => expr "negate"
     *              |  "~" expr2L       => expr "complement"
     *              |  "!" expr2L       => expr "not"
     *              |  "&" expr2L       => (define temp$n := (Promise new);
     *                                      expr := temp$n[0];
     *                                      temp$n[1])
     *              |  expr2L
     * </pre>
     */
    private Expr parseExpr3X() throws IOException, SyntaxException {
        debugMethodPrint("parseExpr3X");
        switch(myToken.tokenType()) {
            case Token.MINUS:
            case Token.COMPLEMENT:
            case Token.NOT: {
                break;
            }
            case Token.BIT_AND: {
                nextToken(); /* & */
                /* This is a good example of lValue handling.
                 *
                 * &lvalue => (define temp$n := (Promise new);
                 *                   lValue := temp$n[0];
                 *                   temp$n[1])
                 */
                Expr lValue = require(parseExpr2L(), "missing lValue");
                NounExpr temp = newTemp();
                DefineExpr defPromise = new DefineExpr(temp.name(), NEW_PROMISE);
                Expr assign = doAssign(false, lValue,
                                       new CallExpr(temp, "get", ZERO_EXPR));
                Expr result = new CallExpr(temp, "get", ONE_EXPR);
                return new SequenceExpr(new SequenceExpr(defPromise,
                                                         assign),
                                        result);
            }
            default: {
                return parseExpr2L();
            }
        }
        String verb = expectOp(1);
        Expr arg = require(parseExpr2L(), "nothing to " + verb);
        return new CallExpr(arg, verb);
    }

    /**
     * <pre>
     *      expr4L ::= expr3X
     *              |  expr4L "[" expr "]"      => expr4L "get" expr
     * </pre>
     */
    private Expr parseExpr4L() throws IOException, SyntaxException {
        //debugMethodPrint("parseExpr4L");
        Expr result = parseExpr3X();
        if (result == null) {
            return result;
        }
        while (myToken.tokenType() == Token.OPEN_SQUARE) {
            String verb = expectOp(2);
            Expr index = require(parseExpr(), "missing index");
            expect(Token.CLOSE_SQUARE, "Must have a matching ']'");
            result = new CallExpr(result, verb, index);
        }
        return result;
    }

    /**
     * <pre>
     *      expr5L ::= expr4L
     *              |  expr5L "**" expr4L           => expr5L "pow" expr4L
     *              |  expr5L "*" expr4L            => expr5L "multiply" expr4L
     *              |  expr5L "/" expr4L            => expr5L "approxDivide" expr4L
     *              |  expr5L "/_" expr4L           => expr5L "floorDivide" expr4L
     *              |  expr5L "%" expr4L            => expr5L "remainder" expr4L
     *              |  expr5L "%%" expr4L           => expr5L "mod" expr4L
     *              |  (base ** exp) "%%" expr4L    => base "modPow" exp "," expr4L
     * </pre>
     */
    private Expr parseExpr5L() throws IOException, SyntaxException {
        //debugMethodPrint("parseExpr5L");

        // XXX Need to deal with modular arithmetic stuff */

        Expr result = parseExpr4L();
        if (result == null) {
            return null;
        }
        while (true) {
            switch(myToken.tokenType()) {
                case Token.POW: //really should be in expr4andAHalfX()
                case Token.TIMES:
                case Token.APRX_DIVIDE:
                case Token.FLOOR_DIVIDE:
                case Token.REMAINDER: {
                    break;
                }
                case Token.MODULO: {
                    if (result instanceof CallExpr) {
                        CallExpr ce = (CallExpr)result;
                        RequestNode req = ce.request();
                        Expr[] args = req.args();
                        if (req.verb().equals("pow") && args.length == 1) {
                            nextToken();
                            Expr modulus = require(parseExpr4L(),
                                                   "missing modulus");
                            Expr base = ce.recipient();
                            Expr exp = args[0];
                            result = new CallExpr(base, "modPow", exp, modulus);
                            continue;
                        }
                    }
                    break;
                }
                default: {
                    return result;
                }
            }
            String verb = expectOp(2);
            Expr arg = require(parseExpr4L(), "nothing to " + verb);
            result = new CallExpr(result, verb, arg);
        }
    }

    /**
     * <pre>
     *      expr6L ::= expr5L
     *              |  expr6L "+" expr5L        =>  expr6L "add" expr5L
     *              |  expr6L "-" expr5L        =>  expr6L "subtract" expr5L
     * </pre>
     */
    private Expr parseExpr6L() throws IOException, SyntaxException {
        //debugMethodPrint("parseExpr6L");
        Expr result = parseExpr5L();
        if (result == null) {
            return null;
        }
        while (true) {
            switch(myToken.tokenType()) {
                case Token.PLUS:
                case Token.MINUS: {
                    String verb = expectOp(2);
                    Expr arg = require(parseExpr5L(), "nothing to " + verb);
                    result = new CallExpr(result, verb, arg);
                    break;
                }
                default: {
                    return result;
                }
            }
        }
    }

    /**
     * <pre>
     *      expr7L ::= expr6L
     *              |  expr7L "<<" expr6L        =>  expr6L "shiftLeft" expr5L
     *              |  expr7L ">>" expr6L        =>  expr6L "shiftLeft" (expr5L "negate")
     * </pre>
     */
    private Expr parseExpr7L() throws IOException, SyntaxException {
        //debugMethodPrint("parseExpr7L");
        Expr result = parseExpr6L();
        if (result == null) {
            return null;
        }
        while (true) {
            switch(myToken.tokenType()) {
                /* LOGICAL_RIGHT is dead! Long live Big Nums! */
                case Token.LEFT_SHIFT: {
                    String verb = expectOp(2);
                    Expr arg = require(parseExpr6L(), "no amount to " + verb);
                    result = new CallExpr(result, verb, arg);
                    break;
                }
                case Token.RIGHT_SHIFT: {
                    nextToken();
                    Expr arg = require(parseExpr6L(), "no amount to right shift");
                    Expr neg = new CallExpr(arg, "negate");
                    result = new CallExpr(result, "shiftLeft", neg);
                    break;
                }
                default: {
                    return result;
                }
            }
        }
    }

    /**
     * <pre>
     *      expr8X ::= expr7L
     *              |  expr7L "<=" expr7L       =>  x "lessThanOrEqualTo" y
     *              |  expr7L "<" expr7L        =>  (define temp$n := x; !(y <= temp$n))
     *              |  expr7L ">=" expr7L       =>  (define temp$n := x; y <= temp$n)
     *              |  expr7L ">" expr7L        =>  !(x <= y)
     * </pre>
     */
    public Expr comparison(Expr x, int tokenType, Expr y) {
        switch(tokenType) {
            case Token.LEQ: {
                return new CallExpr(x, "lessThanOrEqualTo", y);
            }
            case Token.OPEN_ANGLE: {
                NounExpr temp = newTemp();
                return new SequenceExpr(new DefineExpr(temp.name(), x),
                                        comparison(y, Token.CLOSE_ANGLE, temp));
            }
            case Token.GEQ: {
                NounExpr temp = newTemp();
                return new SequenceExpr(new DefineExpr(temp.name(), x),
                                        comparison(y, Token.LEQ, temp));
            }
            case Token.CLOSE_ANGLE: {
                return new CallExpr(comparison(x, Token.LEQ, y), "not");
            }
            default: {
                throw new Error("internal: not a comparison operator "
                                + tokenType);
            }
        }
    }

    /**
     * <pre>
     *      expr8X ::= expr7L
     *              |  expr7L "<=" expr7L       =>  x "lessThanOrEqualTo" y
     *              |  expr7L "<" expr7L        =>  (define temp$n := x; !(y <= temp$n))
     *              |  expr7L ">=" expr7L       =>  (define temp$n := x; y <= temp$n)
     *              |  expr7L ">" expr7L        =>  !(x <= y)
     * </pre>
     */
    private Expr parseExpr8X() throws IOException, SyntaxException {
        debugMethodPrint("parseExpr8X");
        Expr result = parseExpr7L();
        if (result == null)  {
            return null;
        }
        int tt = myToken.tokenType();
        switch (tt) {
            case Token.LEQ:
            case Token.OPEN_ANGLE:
            case Token.GEQ:
            case Token.CLOSE_ANGLE: {
                nextToken();
                Expr arg = require(parseExpr7L(), "missing expr on right of binop");
                return comparison(result, tt, arg);
            }
            default: {
                /* this corresponds to x having been a valid expr7L, but not
                 * having found an inequality */
                return result;
            }
        }
    }

    /**
     * <pre>
     *      expr9X ::= expr8X
     *              |  expr8X "==" expr8X       => x "equals" y
     *              |  expr8X "!=" expr8X       => !(x == y)
     * </pre>
     */
    private Expr parseExpr9X() throws IOException, SyntaxException {
        debugMethodPrint("parseExpr9X");
        Expr result = parseExpr8X();
        if (result == null)  {
            return null;
        }
        switch (myToken.tokenType()) {
            case Token.EQUALS: {
                String verb = expectOp(2);
                Expr arg = require(parseExpr8X(), "missing expr on right of binop");
                return new CallExpr(result, verb, arg);
            }
            case Token.NOT_EQUALS: {
                nextToken(); /* != */
                /* this is x != y, and is expanded to !(y == x) */
                Expr arg = require(parseExpr8X(), "missing expr on right of binop");
                return new CallExpr(new CallExpr(result, "equals", arg),
                                    "not");
            }
            default: {
                /* this corresponds to x having been a valid expr7L, but not
                 * having found an inequality */
                return result;
            }
        }
    }

    /**
     * <pre>
     *      expr10L ::= expr9X
     *               |  expr10L "&" expr9X      =>  x "and" y
     * </pre>
     */
    private Expr parseExpr10L() throws IOException, SyntaxException {
        debugMethodPrint("parseExpr10L");
        Expr result = parseExpr9X();
        if (result == null) {
            return null;
        }
        while (myToken.tokenType() == Token.BIT_AND) {
            String verb = expectOp(2);
            Expr arg = require(parseExpr9X(),
                               "missing expr on right of binop");
            result = new CallExpr(result, verb, arg);
        }
        return result;
    }

    /**
     * <pre>
     *      expr11L ::= expr10L
     *               |  expr11L "^" expr10L     => x "xor" y
     * </pre>
     */
    private Expr parseExpr11L() throws IOException, SyntaxException {
        //debugMethodPrint("parseExpr11L");
        Expr result = parseExpr10L();
        if (result == null) {
            return null;
        }
        while (myToken.tokenType() == Token.BIT_XOR) {
            String verb = expectOp(2);
            Expr arg = require(parseExpr10L(),
                               "missing expr on right of binop");
            result = new CallExpr(result, verb, arg);
        }
        return result;
    }

    /**
     * <pre>
     *      expr12L ::= expr11L
     *               |  expr12L "|" expr11L     => x "or" y
     * </pre>
     */
    private Expr parseExpr12L() throws IOException, SyntaxException {
        //debugMethodPrint("parseExpr12L");
        Expr result = parseExpr11L();
        if (result == null) {
            return null;
        }
        while (myToken.tokenType() == Token.BIT_OR) {
            String verb = expectOp(2);
            Expr arg = require(parseExpr11L(),
                               "missing expr on right of binop");
            result = new CallExpr(result, verb, arg);
        }
        return result;
    }

    /**
     * <pre>
     *      expr13L ::= expr12L
     *               |  expr13L "&&" expr12L     => (x "pick" "{" y "}" "{" false "}") ":"
     * </pre>
     */
    private Expr parseExpr13L() throws IOException, SyntaxException {
        //debugMethodPrint("parseExpr13L");
        Expr result = parseExpr12L();
        if (result == null) {
            return null;
        }
        while (eatIf(Token.COND_AND)) {
            Expr arg = require(parseExpr12L(),
                               "missing expr on right of binop");
            result = new CallExpr(new CallExpr(result, "pick",
                                               thunkify(arg),
                                               EZ_FALSE_THUNK),
                                  "run");
        }
        return result;
    }

    /**
     * <pre>
     *      expr14L ::= expr13L
     *               |  expr14L "||" expr13L     => (x "pick" "{" true "}" "{" y "}") ":"
     * </pre>
     */
    private Expr parseExpr14L() throws IOException, SyntaxException {
        //debugMethodPrint("parseExpr14L");
        Expr result = parseExpr13L();
        if (result == null) {
            return result;
        }
        while (eatIf(Token.COND_OR)) {
            Expr arg = require(parseExpr13L(),
                               "missing expr on right of binop");
            result = new CallExpr(new CallExpr(result, "pick",
                                               EZ_TRUE_THUNK,
                                               thunkify(arg)),
                                  "run");
        }
        return result;
    }

    /**
     * <pre>
     *      expr15X ::= expr14L
     *               |  expr14L request
     *               |  expr14L ":" optExprs        =>  x "run" optExprs
     *               |  expr14L "<-" request
     * </pre>
     */
    private Expr parseExpr15X() throws IOException, SyntaxException {
        //debugMethodPrint("parseExpr15X");
        Expr result = parseExpr14L();
        if (result == null)  {
            return result;
        }
        switch (myToken.tokenType()) {
            case Token.IDENT: {
                RequestNode req = parseRequest();
                if (req == null) {
                    throw new SyntaxException("error reading request "+
                                              "in 'someobject request'");
                }
                return new CallExpr(result, req);
            }

            case Token.COLON: {
                nextToken(); /* : */
                Expr[] args = parseOptExprs();
                return new CallExpr(result, "run", args);
            }

            // XXX JAY - what the heck - we can have dot in both places (I think)
 //          case Token.DOT: {
 //             nextToken(); /* . */
 //             RequestNode req = parseRequest();
 //             if (req == null) {
 //                 throw new SyntaxException("error reading request "+
 //                                            "in 'someobject . request'");
 //             }
 //             return new CallExpr(result, req);
 //           }
            case Token.ESEND: {
                nextToken(); /* <- */
                RequestNode req = parseRequest();
                if (req == null) {
                    throw new SyntaxException("error reading request "+
                                              "in 'someobject <- request'");
                }
                return new SendExpr(result, req);
            }
            default: {
                /* this corresponds to result having been a valid expr15X, but not
                 * being a send of any description */
                return result;
            }
        }
    }

    /**
     * <pre>
     *      expr16R ::= expr15X
     *               |  expr15X ":=" expr16R
     *               |  expr15X op"=" expr16R       => x ":=" (x "op" y)
     * </pre>
     */
    private Expr parseExpr16R() throws IOException, SyntaxException {
        //debugMethodPrint("parseExpr16R");

        /* OK, new style lvalue etc is to have an expression as a valid
         * lvalue, but we're going to catch some parse tree constructs and
         * reform them to mean something different before we hand them out.
         *
         * This is an interesting approach because it means that what we parse
         * might not be what we mean, depending on the _Context_ of what
         * follows it.  However, <touch wood> none of the invalid parse
         * tree fragments should ever make it outside the parser.
         */

        /* XXX There's still a question here about how & where we deal with
         * the 'define' form of the assignments. ie. whether it's an
         * assignment problem, or whether the define statement form needs
         * to be much more elaborate to handle the different types of Lvalue
         *
         * (for instance when we define a collection, do we need to define
         *  a. just the collection itself
         *      { define x ~blah~ ; x[0] = foo ; x[1] = bar }
         *  b. each element in the collection
         *      { define x[0] = foo ; define x[1] = bar }
         *  c. both a & b  )
         */

        Expr lValue = parseExpr15X();
        if (lValue == null) {
            return null;
        }

        if (eatIf(Token.ASSIGN)) {
            Expr rValue = require(parseExpr16R(), "missing rValue");
            return doAssign(false, lValue, rValue);

        } else if (myToken.isAssignOp()) {
            Token binop = myToken.asAssignable();
            nextToken(); /* assignment operator as above */
            /* XXX Oops, need to avoid doubled side effects
             * Que? Rob says where did this comment come from &
             * what does it mean?
             */
            Expr arg = require(parseExpr16R(), "missing rValue arg");
            Expr rValue = new CallExpr(lValue,
                                       new RequestNode(msgForOp(binop, 2),
                                                       arg));
            return doAssign(false, lValue, rValue);
        } else {
            return lValue;
        }
    }

    /**
     * <pre>
     *      expr17L ::= expr16R
     *               |  expr17L ";" expr16R
     */
    private Expr parseExpr17L() throws IOException, SyntaxException {
        debugMethodPrint("parseExpr17L");

        Expr result = parseExpr16R();
        if (result == null) {
            return null;
        }
        while (eatIf(Token.SEMI_COLON)) {
            Expr arg = require(parseExpr16R(), "expr needed after semi");
            result = new SequenceExpr(result, arg);
        }
        return result;
    }

    /**
     * XXX Although there was some talk of an assign(rval) method on Expr types,
     * that's not the case as expr cannot be assigned to. In fact what happens
     * is that we modify an existing parse tree which is wrong (eg. x get y
     * deriving from x[y]) into a valid assignment parse tree (eg. x put y, z)
     * hence it's a purely parser issue and the expressions do not need to
     * have assign methods, simply provide access to the pertinent information
     * so that the new tree can be generated.
     *
     * this should be done recursively in the case of tuples, so all the
     * pertinent info should be in locals.
     *
     * the lval expression is expected to be a valid L-value, ie. it should
     * be possible to assign to it. the rval is the value to be assigned,
     * and will be assigned to a temporary, which will then be assigned to
     * any other lvals in the case of a tuple.
     *
     * the parse tree which replaces the lvalue and presumably the rval is
     * returned to the caller.
     *
     * XXX What does (Tuple : x, y, z) actually mean?
     * It looks nice & intuitive, but really it's "send
     * x, y & z to the noun 'Tuple'. does this make sense, or
     * is it just a cute way of denoting something which
     * should never make it as far as the interpreter ?
     *
     * either way, temp here is defining a new referenc to an
     * already existing collection. (whatever collections are)
     * I think this is so that 'rval' is only evaluated once, though
     * i'm not sure that's an intuitively sensible thing to do;
     * because the values of the contents of the collection temp
     * points to can be changing. whatever.
     *
     * the other thing to note is that there is no checking of bounds
     * or sizes etc when we do this. - that's done lazily at
     * evaluation time. this might lead to exceptions etc when people
     * don't expect them & could be a debugging nightmare
     *
     * what I'm not sure of is exactly how lazy EZ is;
     * if one assigns a (temp get blah) into a var, does it get
     * the value at assign time, or (what i expect) at evaluation
     * time. hence multiple evaluations of the new copy of a tuple
     * member value will reflect any changes in the original source
     * this might makes things delicate
     */
    private Expr doAssign(boolean defining, Expr lval, Expr rval) throws SyntaxException {
        debugMethodPrint("doAssign");

        /*
         *  x ":=" z
         *  "define" x ":=" z
         */
        if (lval instanceof NounExpr) {
            /* the base case */
            String name = ((NounExpr)lval).name();
            if (defining) {
                return new DefineExpr(name, rval);
            } else {
                return new AssignExpr(name, rval);
            }

        } else if (lval instanceof CallExpr) {
            CallExpr ce = (CallExpr)lval;
            Expr recip = ce.recipient();
            RequestNode req = ce.request();
            String verb = req.verb();
            Expr[] args = req.args();

            /*
             *  (x "get" y) ":=" z              =>  x "put" y "," z
             *  (x "get" y1 "," y2) ":=" z      =>  x "put" y1 "," y2 "," z
             *  "define" (x "get" y) ":=" z     =>  x "introduce" y "," z
             */
            if (verb.equals("get")) {
                /* then we had x[y] or x.y - are equivalent by this time */
                Expr[] newArgs = new Expr[args.length +1];
                System.arraycopy(args, 0, newArgs, 0, args.length);
                newArgs[args.length] = rval;
                String newVerb = defining ? "introduce" : "put";
                return new CallExpr(recip, newVerb, newArgs);

            /*
             * Notice that we are NOT creating or assigning a Tuple with
             * these forms.  We are using the Tuple syntax as an lValue
             * in order to take apart a Tuple rValue and put the components
             * somewhere.
             *
             *  ("Tuple" "run" x "," y) ":=" z
             * =>
             *  ("define" temp$n ":=" z ";"
             *   x ":=" temp$n "get" "0" ";"
             *   y ":=" temp$n "get" "1" ";"
             *   temp$n)
             *
             *  "define" ("Tuple" "run" x "," y) ":=" z
             * =>
             *  ("define" temp$n ":=" z ";"
             *   "define" x ":=" temp$n "get" "0" ";"
             *   "define" y ":=" temp$n "get" "1" ";"
             *   temp$n)
             */
            //XXX need to implement all the ParseNode.equals() methods for
            //this test to work on explicitly written Tuple expressions
            } else if (TUPLE_EXPR.equals(recip) && verb.equals("run")) {

                /* create a new temp var */
                NounExpr temp = newTemp();
                Expr result = new DefineExpr(temp.name(), rval);

                /* loop over the array assigning pairwise
                 * we return a sequence of these assignments */
                for (int i = 0; i < args.length; i++) {
                    Expr inti = new LiteralExpr("" + i, BigInteger.valueOf(i));
                    Expr expri = new CallExpr(temp, "get", inti);
                    result = new SequenceExpr(result, doAssign(defining, args[i], expri));
                }
                return new SequenceExpr(result, temp);
            }
        }
        throw new SyntaxException("Assignment can only be done to "+
            "nouns, collection elements, and tuples of same");
    }





    /**********************************************************************
     * <pre>
     *      statement ::=
     *         "(" expr ")"                             => expr
     *      |  literal
     *      |  "pov"
     *      |  "{" expr "}"                             => ...
     *      |  "[" optExprs "]"                         => ...
     *      |  "define" expr15X ":=" expr16R            => ...
     *      |  "escape" param block
     *      |  "loop" block
     *      |  "loop" param block                       => ...
     *      |  "try" block [ "catch" [ param ] block ] [ "finally" block ]
     *      |  "throw" ":" expr14L
     *      |  "enterPov" expr2L block
     *      |  "dispatch" "{" method* [ forRequest ] "}"
     *      |  "to" ...                                 => ...
     *      |  "forRequest" param "," param block       => ...
     *      |  "if" ...                                 => ...
     *      |  "object" "{" method* [ forRequest ] "}"  => ...
     *      |  "while" expr2L block                     => ...
     *      |  "for" param "in" expr2L block            => ...
     * </pre><p>
     *
     * Now we have the glob of methods which deal with parsing statements.
     *
     * The broad structure here is
     * - parseStatement; which switches to the other parse routines
     *   according to which keyword is present.
     * - kernel statements; which expand to their appropriate parse tree
     *   nodes
     * - syntactic sugar statements; which turn into baroque bits of parse
     *   tree encapsulating the implementation of those ideas.
     *
     **********************************************************************/

    public Expr parseStatement() throws IOException, SyntaxException {
        debugMethodPrint("parseStatement");
        switch (myToken.tokenType()) {

            /* First deal with the case where this statement is really an
             * expression. This is also how nesting of parens gets handled
             */
           case Token.OPEN_PAREN: {
               nextToken(); /* consume '(' */
               Expr result = require(parseExpr(), "expr needed in parens");
                expect(Token.CLOSE_PAREN, "Missing ')'");
                return result;
            }

            /* Literals are handled here as statements. the hard work is done
             * by the tokeniser so there is nothing here to do really */
            case Token.LITERAL_STRING:
            case Token.OTHER_LITERAL: {
                Expr result = new LiteralExpr(myToken.token(), ((Literal)myToken).value());
                nextToken(); /* literal */
                return result;
            }

            /* pov is trivial, so we don't actually parse anything */
            case Token.POV: {
                nextToken(); /* pov */
                return new PovExpr();
            }

            /*
             * an easy way of generating tiny closures
             * to hand around.
             */
            case Token.OPEN_CURLY: {
                return require(parseExpandedThunkBlock());
            }

            case Token.OPEN_SQUARE: {
                return require(parseTuple());
            }
            case Token.DEFINE: {
                return require(parseDefine());
            }
            case Token.ESCAPE: {
                return require(parseEscape());
            }
            case Token.LOOP: {
                return require(parseLoop());
            }
            case Token.TRY: {
                return require(parseTry());
            }
            case Token.THROW: {
                return require(parseThrow());
            }
            case Token.ENTER_POV: {
                return require(parseEnterPov());
            }
            case Token.DISPATCH: {
                return (parseDispatch());
            }

            /* Thus concludes the kernel statements.
             * we now move on to doing the sugary sweet statements */
            case Token.TO: {

                /* XXX XXX XXX
                 * I need to come back to this & think through what the
                 * implications of methods & closures are.
                 *
                 * I think i now see what mark was intending (ie. I've just
                 * seen a much more elegant way of doing things & think that
                 * was the original intention. <sigh>
                 * It will also have scope for consolidation some chunks of
                 * code which can only be good.
                 */

                return require(parseMethodClosure());
                /*return parseToClosure()*/ /* XXX */
            }
            case Token.FOR_REQUEST: {
                return require(parseForRequestClosure());
            }
            case Token.IF: {
                return require(parseIf());
            }
            case Token.OBJECT: {
                return require(parseObject());
            }
            case Token.WHILE: {
                return require(parseWhile());
            }
            case Token.FOR: {
                return require(parseFor());
            }
            default: { // XXX --  Any expression is a statement, too.
    //            Expr result = parseExpr();
                return null;
            }
        }
    }


    /**
     * <pre>
     *      "[" optExprs "]"        =>        "Tuple" "run" optExprs
     * </pre>
     */
    private Expr parseTuple() throws IOException, SyntaxException {
        debugMethodPrint("parseTuple");

        if (! eatIf(Token.OPEN_SQUARE)) {
            return null;
        }
        /* a Tuple with no members is fine */
        Expr[] exprs = parseOptExprs();
        expect(Token.CLOSE_SQUARE, "Expected ']', but found "+myToken);
        return new CallExpr(TUPLE_EXPR, "run", exprs);
    }

    private Expr parseDefine() throws IOException, SyntaxException {
        debugMethodPrint("parseDefine");

        if (! eatIf(Token.DEFINE)) {
            return null;
        }
        Expr lval = require(parseExpr15X(), "need lvalue");
        expect(Token.ASSIGN, "Syntax error, expected ':=' in "+
               "'define lValue := expr'");
        Expr expr = require(parseExpr16R(), "need value to assign");

        return doAssign(true, lval, expr);
    }

    private Expr parseEscape() throws IOException, SyntaxException {
        debugMethodPrint("parseEscape");

        if (! eatIf(Token.ESCAPE)) {
            return null;
        }
        String param = expectIdentifier("need a name for the ejector");
        Expr block = require(parseBlock(), "need escape block");

        return new EscapeExpr(param, block);
    }

    /**
     * <pre>
     *      loopExpr ::= "loop" block           kernel
     *                |  "loop" param block     => "escape" param { loop block }
     */
    private Expr parseLoop() throws IOException, SyntaxException {
        debugMethodPrint("parseLoop");

        if (! eatIf(Token.LOOP)) {
            return null;
        }
        String optParam = parseIdentifier();
        Expr block = require(parseBlock(), "need escape block");
        Expr result = new LoopExpr(block);
        if (optParam == null) {
            return result;
        } else {
            return new EscapeExpr(optParam, result);
        }
    }

    private Expr parseTry() throws IOException, SyntaxException {
        debugMethodPrint("parseTry");

        if (! eatIf(Token.TRY)) {
            return null;
        }
        Expr tryBlock = require(parseBlock(), "need a try block");
        Expr optCatch = null;
        String optParam = null;
        Expr optFinally = null;
        if (eatIf(Token.CATCH)) {
            optParam = parseIdentifier();
            //ok if optParam is still null
            optCatch = require(parseBlock(), "need catch block");
        }
        if (eatIf(Token.FINALLY)) {
            optFinally = require(parseBlock(), "need a finally block");
        }

        return new TryExpr(tryBlock, optParam, optCatch, optFinally);
    }

    private Expr parseThrow() throws IOException, SyntaxException {
        debugMethodPrint("parseThrow");

        if (! eatIf(Token.THROW)) {
            return null;
        }
        expect(Token.COLON, "Syntax Error, throw is always followd by ':'");
        return new ThrowExpr(require(parseExpr14L(), "need something to throw"));

    }

    /* pov is trivial, so doesn't need parsing - see parseStatement() */

    private Expr parseEnterPov() throws IOException, SyntaxException {
        debugMethodPrint("parseEnterPov");

        if (! eatIf(Token.ENTER_POV)) {
            return null;
        }
        Expr expr = require(parseExpr2L(), "need a point of view");
        Expr block = require(parseBlock(), "need something to do there");
        return new EnterPovExpr(expr, block);
    }

    private Expr parseDispatch() throws IOException, SyntaxException {
        debugMethodPrint("parseDispatch");

        if (! eatIf(Token.DISPATCH)) {
            return null;
        }
        return parseDispatchBody();
    }

    /**
     * this is broken off from parseDispatch purely because we want to use
     * exactly the same code within an 'object' parse, but that does not
     * begin with Token.DISPATCH
     * XXX reorganise code to bring parseDispatch / parseObject closer ?
     */
    private Expr parseDispatchBody() throws IOException, SyntaxException {

        expect(Token.OPEN_CURLY, "Syntax Error, dispatch must be followed by "+
               "braces {}, optionally containing methods ");

        NameTableEditor methods = new NameTableEditorImpl();
        swallowAnyNewlines();
        while (myToken.tokenType() == Token.TO) {
            MethodNode meth = parseMethod();
            try {
                methods.introduce(meth.mangle(), meth);
            } catch (AlreadyDefinedException e) {
                throw new SyntaxException("Methods in a 'dispatch' "
                    +"must be uniquely named " + e);
            }
            swallowAnyNewlines();
        }
        ForRequestNode optForRequest = parseForRequest();
        swallowAnyNewlines();
        expect(Token.CLOSE_CURLY, "Syntax Error; only methods "
                        +"and an optional forRequest may be within a dispatch."
                        +" Missing } ?");
        return new DispatchExpr(methods, optForRequest);
    }



    /**********************************************************************
     * <pre>
     *      optExprs ::= [ expr14L ("," expr14L)* ]
     * </pre><p>
     * these are two utility functions used in a number of places, to read
     * an arbitrary length of Expressions or identifiers respectively.
     * can be 0 in length. if there are more than 1, then they must be
     * comma separated.
     *
     *********************************************************************
     */
    private Expr[] parseOptExprs() throws IOException, SyntaxException {
        debugMethodPrint("parseOptExprs");
        Expr expr = parseExpr14L();
        if (expr == null) {
            return NO_ARGS; //return new Expr[0];
        }
        Vector vec = new Vector();
        vec.addElement(expr);
        while (eatIf(Token.COMMA)) {
            expr = require(parseExpr14L(), "expression must follow comma");
            vec.addElement(expr);
        }
        Expr[] result = new Expr[vec.size()];
        vec.copyInto(result);
        return result;
    }

    /**
     * <pre>
     *      optIdents ::= [ ident ("," ident)* ]
     * </pre><p>
     */
    private String[] parseOptIdents() throws IOException, SyntaxException {
        debugMethodPrint("parseOptIdents");
        String ident = parseIdentifier();
        if (ident == null) {
            return /*NO_ARGS*/ new String[0];
        }
        Vector vec = new Vector();
        vec.addElement(ident);
        while (eatIf(Token.COMMA)) {
            ident = expectIdentifier("parameter must follow comma");
            vec.addElement(ident);
        }
        String[] result = new String[vec.size()];
        vec.copyInto(result);
        return result;
    }

    /**
     * Return a block encapsulated by a method object that can be invoked
     * later - this is used for conditional expressions, etc.
     */
/*
    private Expr parseThunkBlock() throws IOException, SyntaxException {
        Expr body = parseBlock();
        if (body == null) {
            return null;
        }
        return(new ThunkExpr(body));
    }
*/

    /**
     * A version that represents the code block directly in the kernal language.
     * Eventually we will replace calls to this method with calls to parseThunkBlock
     * for effeciency purposes.
     */
    private Expr parseExpandedThunkBlock() throws IOException, SyntaxException {
        Expr body = parseBlock();
        if (body == null) {
            return null;
        }
        return thunkify(body);
    }

    /**
     * Transform 'body' into an '{ body }' expression,
     * ie, into 'dispatch { to of { body } }'
     */
    static public Expr thunkify(Expr body) {
        return closurize(new EZMethodNode("run",
                                          NO_PARAMS,
                                          body));
    }

    /**
     * Turn a method into an expression,
     * ie, transform 'method' into 'dispatch { method }'
     */
    static public Expr closurize(MethodNode method) {
        NameTableEditor methods = new NameTableEditorImpl();
        try {
            methods.introduce(method.mangle(), method);

        } catch (AlreadyDefinedException e) {
            throw new Error("Wow. We really shouldn't have got "+
                "this. how can a dispatch with one element have a name clash?");
        }
        return new DispatchExpr(methods, null);
    }

    private Expr parseBlock() throws IOException, SyntaxException {
        debugMethodPrint("parseBlock");

        if (! eatIf(Token.OPEN_CURLY)) {
            return null;
        }
        swallowAnyNewlines();

        Expr result = require(parseCommand(), // was parseCommand();
                              "Must have at least one expr in "+
                              "a block. none found.");
        swallowAnyNewlines();
        Expr second;
        while (null != (second = parseCommand())) { // was parseCommand();
            result = new SequenceExpr(result, second);
            swallowAnyNewlines();
        }
        expect(Token.CLOSE_CURLY, "Syntax Error; We needed a '}' here. "
               +"Is there one missing?");
        return result;
    }

    private MethodNode parseMethod() throws IOException, SyntaxException {
        debugMethodPrint("parseMethod");

        if (! eatIf(Token.TO)) {
            return null;
        }

        Token op = myToken;
        nextToken();
        String[] params = expectParams(op);
        String verb = msgForOp(op, params.length);

        Expr block = require(parseBlock(), "Expected either ', param' "+
                                           "or { body } for this method, but got "+myToken);
        return new EZMethodNode(verb, params, block);
    }

    private Expr parseMethodClosure() throws IOException, SyntaxException {
        debugMethodPrint("parseMethodClosure");

        MethodNode meth = parseMethod();
        if (meth == null) {
            return null;
        } else {
            return closurize(meth);
        }
    }

    private ForRequestNode parseForRequest() throws IOException, SyntaxException {
        debugMethodPrint("parseForRequest");

        if (! eatIf(Token.FOR_REQUEST)) {
            return null;
        }
        String vParam = expectIdentifier("need param for verb");
        expect(Token.COMMA, "Syntax Error; forRequest verb, params { body }");
        String pParam = expectIdentifier("need param for args");

        Expr block = require(parseBlock(), "forRequest must have a { block }");
        return new ForRequestNode(vParam, pParam, block);
    }

    private Expr parseForRequestClosure() throws IOException, SyntaxException {
        debugMethodPrint("parseForRequestClosure");

        ForRequestNode fr = parseForRequest();
        if (fr == null) {
            return null;
        }
        return new DispatchExpr(new NameTableEditorImpl(), fr);
    }

    private RequestNode parseRequest() throws IOException, SyntaxException {
        debugMethodPrint("parseRequest");

        if (myToken.tokenType() != Token.IDENT) {
            return null;
        }
        String verb = expectIdentifier("can't happen");
        Expr[] args = parseOptExprs();

        return new RequestNode(verb, args);
    }



    /**********************************************************************
     *
     * Thus begins the methods which deal with the sugar statements &
     * building the baroque parse trees.
     *
     * rest assured that expressing these
     * in ez(all) => ez(kernel) syntax translations is a hell of a lot neater
     * than into these hand crafted trees.
     *
     **********************************************************************/

    /**
     * <pre>
     *      ifExpr ::=      "if" expr2L block
     *                   => "if" expr2L block "else" "{" null "}"
     *
     *              |       "if" expr2L block "else" ifExpr
     *                   => "if" expr2L block "else" "{" ifExpr "}"
     *
     *              |       "if" expr2L thenBlock "else" elseBlock
     *                   => (expr2L "pick" thenBlock "," elseBlock) "run"
     * </pre><p>
     *
     * the if statement, and correspondingly the && and || boolean forms
     * use the functional (lawbda calculus) idea of booleans being functions
     * from pairs (true, false) to the contents of that pair.
     *
     * this is exhibited in the 'pick' method on EZBoolean which takes two
     * arguments. thus (bool pick e1, e2) evaluates to e1 if bool is true,
     * e2 if bool is false. This action is used in the expansion of if;
     *
     *     if bool { thenBody } else { elseBody }  =>
     *
     *     ( bool pick { thenBody }, { elseBody } ) of
     * and
     *     if bool { thenBody }   =>
     *
     *     ( bool pick { thenBody }, { EZ_NULL } ) of
     */
    private Expr parseIf() throws IOException, SyntaxException {
        debugMethodPrint("parseIf");

        if (! eatIf(Token.IF)) {
            return null;
        }
        Expr condExpr = require(parseExpr2L(),
                                "'if' needs an expr to test");
        Expr thenBlk = require(parseExpandedThunkBlock(),
                               "code in 'if' statements must be "+
                               "enclosed in braces '{}'");

        Expr elseBlk;
        if (eatIf(Token.ELSE)) {
            if (myToken.tokenType() == Token.IF) {
                /* deal with 'else if' case */
                elseBlk = thunkify(parseIf());

            } else {
                /* then we didn't have 'else if' see if we had 'else {...}'*/
                elseBlk = require(parseExpandedThunkBlock(),
                                  "code in 'if' statements must be "+
                                  "enclosed in braces '{}'");
            }
        } else {
            elseBlk = EZ_NULL_THUNK;
        }

        CallExpr picker = new CallExpr(condExpr, "pick", thenBlk, elseBlk);
        return new CallExpr(picker, "run");
    }

    /**
     * This is the expansion of 'object'. It contains some methods &
     * an optional forRequest. in this respect it matches 'dispatch'
     * and in fact uses the same code. That is then wrapped in some
     * stuff which binds 'self' and 'return' to what you'd expect
     * them to be. it corresponds to
     *
     *  object { meths* [forReq] }    expanding to;
     *
     *  { define self := forRequest verb, args {
     *      escape return {
     *         (dispatch {
     *             meths*
     *             [forReq]
     *         } perform verb, args)
     *     }
     *  } of
     */
    private Expr parseObject() throws IOException, SyntaxException {

        if (! eatIf(Token.OBJECT)) {
            return null;
        }
        Expr innerDispatch = require(parseDispatchBody(),
                                     "'object' must be followed by a body"+
                                     " including some methods & an"+
                                     " optional forRequest");
        NounExpr verb = newTemp();
        NounExpr args = newTemp();

        Expr performCall = new CallExpr(innerDispatch, "perform",
                                        verb, args);

        Expr returnEscape = new EscapeExpr("return", performCall);

        ForRequestNode forReqNode = new ForRequestNode(verb.name(),
                                                       args.name(),
                                                       returnEscape);
        Expr forReqClosure = new DispatchExpr(null, forReqNode);
        Expr defSelf = new DefineExpr("self", forReqClosure);

        return new CallExpr(thunkify(defSelf), "run");
    }

    /* This is the while loop.
     * this corresponds to the following expansion.
     *
     *  while (cond) { body }   =>
     *
     *  escape break {
     *      loop {
     *          (cond pick {
     *              escape continue { body }
     *          }, {
     *              break :
     *          }) :
     *      }
     *  }
     */
    private Expr parseWhile() throws IOException, SyntaxException {

        if (! eatIf(Token.WHILE)) {
            return null;
        }
        Expr loopCond = require(parseExpr2L(),
                                "loop must be followed by a conditional"+
                                " expression & a loop body in braces '{}'");
        Expr block = require(parseBlock(),
                             "loop must be followed by a conditional"+
                             " expression & a loop body in braces '{}'");
        EscapeExpr contEscaper = new EscapeExpr("continue", block);
        CallExpr loopBreaker = new CallExpr(new NounExpr("break"), "run");

        CallExpr choose = new CallExpr(loopCond, "pick",
                                        thunkify(contEscaper), thunkify(loopBreaker));
        CallExpr runChoice = new CallExpr(choose, "run");
        return new EscapeExpr("break", new LoopExpr(runChoice));
    }

    /* This is the for loop.
     * this corresponds to the following expansion.
     *
     *  for param in expr { body }   =>
     *
     *  escape break {
     *      ((expr asEnumeration) each to : param {
     *          (escape continue { body })
     *      })
     *  }
     */
    private Expr parseFor() throws IOException, SyntaxException {
        String usage = "usage: for param in expr { body }";

        if (! eatIf(Token.FOR)) {
            return null;
        }
        String param = expectIdentifier(usage);
        expect(Token.IN, usage);
        Expr loopExpr = require(parseExpr2L(), usage);
        Expr block = require(parseBlock(), usage);

        loopExpr = new CallExpr(loopExpr, "asEnumeration");
        EscapeExpr contEscaper = new EscapeExpr("continue", block);

        String[] paramArray = { param };
        MethodNode paramMeth = new EZMethodNode("run",
                                                paramArray,
                                                contEscaper);
        Expr lambdaParam = closurize(paramMeth);
        CallExpr loopBody = new CallExpr(loopExpr, "each", lambdaParam);

        return new EscapeExpr("break", loopBody);
    }




    /**********************************************************************
     *
     * This is a collection of utility functions used in the rest of the parser
     *
     **********************************************************************/

    /**
     * This reads an identifier in from the lexer, and returns the appropriate
     * string. or null for a failure
     */
    private String parseIdentifier() throws IOException, SyntaxException {

        if (myToken.tokenType() != Token.IDENT) {
            return null;
        }
        Identifier i = ((Identifier)myToken);
        nextToken();
        return i.token();
    }

    /**
     * This reads an identifier in from the lexer, and returns the appropriate
     * string. or throws a SyntaxException
     */
    private String expectIdentifier(String errorMessage) throws IOException, SyntaxException {
        String result = parseIdentifier();
        if (result != null) {
            return result;
        }
        throw new SyntaxException(errorMessage);
    }

    private boolean eatIf(int tokenType) throws SyntaxException, IOException {
        if (myToken.tokenType() == tokenType) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Is used to simplify the code in places where a specific token _must_
     * be present. It throws a syntax exception if that token is missing.
     * since the token is necessary, it is also consumed, since is will
     * not be used for any purpose.
     */
    private Token expect(int tokenType, String errorMessage)
         throws IOException, SyntaxException {

        Token result = myToken;

        if (eatIf(tokenType)) {
            return result;
        } else {
            throw new SyntaxException(errorMessage +"\nwhile reading "+result);
        }
    }

    /**
     * Ensures that optExpr isn't null.  If it isn't, it's returned.  If it is null,
     * a SyntaxException is thrown with the errorMessage
     */
    static private Expr require(Expr optExpr, String errorMessage)
         throws SyntaxException {

        if (optExpr != null) {
            return optExpr;
        } else {
            throw new SyntaxException(errorMessage);
        }
    }

    /**
     * Ensures that optExpr isn't null.  If it isn't, it's returned.  If it is null,
     * an indication of an internal error is thrown
     */
    static private Expr require(Expr optExpr)
         throws SyntaxException {

        if (optExpr != null) {
            return optExpr;
        } else {
            throw new Error("internal: shouldn't be null");
        }
    }

    /**
     * helper function mapping op tokens to verb strings
     */
    static public String msgForOp(Token op, int arity) throws SyntaxException {

        //independent of arity
        switch (op.tokenType()) {
            case Token.COLON:               { return "run"; }
            case Token.IDENT:               { return op.token(); }
        }

        switch (arity) {
            case 1: {
                switch (op.tokenType()) {
                    case Token.MINUS:       { return "negate"; }
                    case Token.NOT:         { return "not"; }
                    case Token.COMPLEMENT:  { return "complement"; }
                }
                break;
            }
            case 3: {
                switch (op.tokenType()) {
                    case Token.POW:         { return "modPow"; }
                }
                break;
            }
            case 2: {
                switch (op.tokenType()) {
                    case Token.OPEN_SQUARE: { return "get"; }
                    case Token.PLUS:        { return "add"; }
                    case Token.MINUS:       { return "subtract"; }
                    case Token.TIMES:       { return "multiply"; }
                    case Token.APRX_DIVIDE: { return "approxDivide"; }
                    case Token.FLOOR_DIVIDE:{ return "floorDivide"; }
                    case Token.REMAINDER:   { return "remainder"; }
                    case Token.MODULO:      { return "mod"; } // JAY was modulo
                    case Token.POW:         { return "pow"; }
                    case Token.LEFT_SHIFT:  { return "shiftLeft"; }
                    case Token.RIGHT_SHIFT: { return "shiftRight"; }
                    case Token.EQUALS:      { return "equals"; }
                    case Token.LEQ:         { return "lessThanOrEqualTo"; }
                    case Token.BIT_AND:     { return "and"; }
                    case Token.BIT_XOR:     { return "xor"; }
                    case Token.BIT_OR:      { return "or"; }
                }
                break;
            }
        }
        throw new SyntaxException(op.token() + " not a kernel "
                                  + arity + "-arity operator");
    }

    private String expectOp(int arity) throws IOException, SyntaxException {
        String result = msgForOp(myToken, arity);
        nextToken();
        return result;
    }

    /**
     * Given that op was just consumed, consume and return an array of parameter names.
     */
    private String[] expectParams(Token op) throws IOException, SyntaxException {
        switch (op.tokenType()) {
            case Token.OPEN_SQUARE: {
                String[] result = { expectIdentifier("need index param") };
                expect(Token.CLOSE_SQUARE, "usage: 'to [ident]'");
                return result;
            }
            case Token.POW : {
                String exp = expectIdentifier("need exponent");
                if (eatIf(Token.MODULO)) {
                    String[] result = { exp,
                                        expectIdentifier("need modulus") };
                    return result;
                }
                String[] result = { exp };
                return result;
            }
        }
        return parseOptIdents();
    }


    /* pretty self explanatory */
    public boolean isEndOfFile() {
        return myToken.tokenType() == Token.EOF;
    }

    /* Skips over any new lines found in the token stream. there's a
     * possibility tha this should be done by the caller, by playing
     * with the lexer, but the lexer never holds a token, se it'd be
     * just as klunky. this is reasonably clean.
     *
     * It's also used in the parse block business which needs to be the
     * parser's client for the above purposes.
     *
     * it's also used in a few places where during testing it seemed
     * reasonable to be agnostic about any newlines found. We might want
     * to revisit this issue, but this seems ok at the moment.
     * (the basic issue is that normally languages are not bothered
     * about newline location as it is only white space, but we actually
     * are since newline is used as a distinctive token for command
     * separation.)
     *
     * We return a bool which is whether we found any newlines or not.
     */
    public boolean swallowAnyNewlines() throws IOException, SyntaxException {
        if (myToken.tokenType() != Token.NEWLINE) {
            debugPrint("not swallowing newline");
            return false;
        }
        while (myToken.tokenType() == Token.NEWLINE) {
            debugPrint("swallowing newline");
            nextToken();
        }
        return true;
    }


    /**
     * Report the exception in accociation with the current position, and
     * skip the rest of the line.
     */
    public void diagnostic(Throwable ex, PrintStream errs) {
        myLexer.diagnostic(ex, errs);
     //   try {
     //       nextToken();
     //   } catch (IOException ex2) {
     //       throw new RuntimeException("while skipping line " + ex2);
     //   } catch (SyntaxException ex2) {
     //       throw new RuntimeException("while skipping line " + ex2);
     //   }
    }


    /**********************************************************************
     *
     * Currently only used for testing the parser, this gives an idea of
     * how to use it in whatever finally becomes the read-eval loop parser
     * client.
     *
     **********************************************************************/
    static public void main(String[] args)
         throws IOException, SyntaxException {

        if (args.length < 1 || args.length > 2) {
            throw new RuntimeException(
                "usage: java ec.ez.syntax.Parser [-d] file");
        }

        InputStream ins;
        boolean debugging;

        if (args.length == 2) {
            if (args[0].equals("-d")) {
                debugging = true;
                ins = new FileInputStream(args[1]);
            } else {
                throw new RuntimeException(
                    "usage: java ec.ez.syntax.Parser [-d] file");
            }
        } else if (args.length == 1) {
            debugging = false;
            ins = new FileInputStream(args[0]);
        } else {
            throw new RuntimeException(
                "usage: java ec.ez.syntax.Parser [-d] file");
        }

        ins = new BufferedInputStream(ins); //XXX bad if interactive
        Lexer lex = new Lexer(args[0], new DataInputStream(ins));
        Parser p = debugging ? new Parser(lex, System.out)
                             : new Parser(lex);

        NameTable pv = new NameTableEditorImpl();

        p.swallowAnyNewlines();

        while (!p.isEndOfFile()) {
            p.debugPrint("*** Parsing command ***");
            Expr thisExpr = p.parseCommand(); // was parseCommand();
            p.debugPrint("*** Parsed command ***");

            if (thisExpr == null) {
                System.out.println("Unable to parse a command.");
                System.out.println("Current token is "+p.myToken);
                return;
            }

            try {
                System.out.print("Expression expanded: ");
                thisExpr.printOn(System.out, 2);
                System.out.println("");

            } catch (Exception e) {
                System.out.println("Pretty Print Trouble");
                e.printStackTrace();
                System.out.println("ex: " + e);
            }
            p.debugPrint("*** Running the command...");
            try {
                Object result = thisExpr.eval(pv);
                System.out.println("value: " + result);

            } catch (Throwable e) {
                e.printStackTrace();
                System.out.println("ex: " + e);
            }

            if (!p.swallowAnyNewlines()) {
                System.out.println("! a Missing newline.");
                if (!p.isEndOfFile()) {
                    throw new SyntaxException("Expected end of line or file");
                }
            }
        }
        System.out.println("Done");
    }
}


