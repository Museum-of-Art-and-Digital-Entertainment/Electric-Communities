/*
  A YACC Grammar for EZ.

  by Chip Morningstar
  based on a non-YACC grammar by Mark Miller
  modified by others (see cvs)

  This grammar is for the non-kernel version of EZ.

  Copyright 1997 Electric Communities. All rights reserved worldwide.
*/

%{
package ec.ez.syntax;

import ec.ez.runtime.AlreadyDefinedException;
import ec.ez.runtime.MethodNode;
import ec.ez.collect.*;
import ec.ez.prim.EZStaticWrapper;
import ec.ez.ezvm.*;
import java.lang.reflect.Array;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.math.BigInteger;
%}

/* Categorical terminals */
%token Identifier
%token Literal
%token QuasiString
%token BlockMacro
%token DispatchMacro

/* Keywords */
%token CATCH DEFINE DISPATCH ELSE ENTER_POV
%token ESCAPE FINALLY FOR IF IN LET LOOP
%token MATCH OBJECT POV SWITCH THROW TO TRY WHILE

/* Reserved Keywords */
%token ABSTRACT AN AS BEHALF BELIEF
%token BELIEVE BELIEVES BIND CASE CLASS CONST DEFAULT
%token DEFMACRO DELEGATE DEPRECATED DO ENCAPSULATE ENCAPSULATED
%token ENCAPSULATES ENSURE ENUM EVENTUAL EVENTUALLY EXISTS
%token EXPORT EXTENDS FINAL FORALL GIVEN HIDDEN
%token HIDE HIDES IMPLEMENTS IMPORT INTERFACE IS
%token ISA KNOW KNOWS METHODS NATIVE
%token ON PACKAGE PRIVATE PROTECTED PUBLIC REQUIRE
%token SAKE STATIC STRUCT SUCHTHAT SYNCHRONIZED
%token THIS THROWS TRANSIENT TYPEDEF VIRTUAL VOID
%token VOLATILE



/* The magical end-of-line token, not considered whitespace */
%token EOL

/* Multi-Character Operators */
%token OpFlrDiv         /* _/ */
%token OpEq             /* == */
%token OpGeq            /* >= */
%token OpLAnd           /* && */
%token OpLOr            /* || */
%token OpLeq            /* <= */
%token OpThru           /* .. */
%token OpTill           /* ..! */
%token OpAsl            /* << */
%token OpAsr            /* >> */
%token OpMod            /* %% */
%token OpNeq            /* != */
%token OpPow            /* ** */

%token OpAss            /* :=  */
%token OpAssAdd         /* +=  */
%token OpAssAnd         /* &=  */
%token OpAssAprxDiv     /* /=  */
%token OpAssFlrDiv      /* _/= */
%token OpAssAsl         /* <<= */
%token OpAssAsr         /* >>= */
%token OpAssRemdr       /* %=  */
%token OpAssMod         /* %%= */
%token OpAssMul         /* *=  */
%token OpAssOr          /* |=  */
%token OpAssPow         /* **= */
%token OpAssSub         /* -=  */
%token OpAssXor         /* ^=  */

/* Other funky tokens */
%token Send             /* <- */
%token MapsTo           /* => */
%token MatchBind        /* =~ */
%token MisMatch         /* !~ */


/* Grammar follows */
%%

/* XXX JAY - added optEOLs to start production so we can accept
 * "comment-only" programs. */
start:
        optEOLs
 |      expr                    { myResult = (Expr)$1; }
 ;

/*
********************************************
**               Expressions
********************************************
*/


expr:
        optEOLs seqs optEOLs    { $$ = $2; }
 ;


/*
 * Don't-care associative.
 *
 * First evaluates seqs, then evaluates to the evaluation of seq.
 */
seqs:
        seq
 |      seqs EOLs seq           { $$ = sequence($1, $3); }
 ;


/*
 * Don't-care associative
 */
seq:
        assign
 |      assign ';'
 |      assign ';' seq          { $$ = sequence($1, $3); }
 ;


/*
 * Right associative.  The invalid cases of the left hand side are
 * caught after parsing.  See documentation for transformations.
 */
assign:
        cond
 |      cond OpAss    assign    { $$ = assign($1,     $3); }
 |      cond assignop assign    { $$ = update($1, $2, $3); }
 |      cond OpAssAsr assign    { $$ = assAsr($1,     $3); }

 |      DEFINE pattern OpAss assign     { $$ = define($2, $4); }
 ;


/*
 * Left associative
 */
cond:
        condAnd
 |      cond OpLOr condAnd      { $$ = condOr($1, $3); }
 ;


/*
 * Left associative
 */
condAnd:
        comp
 |      condAnd OpLAnd comp     { $$ = condAnd($1, $3); }
 ;


/*
 * Non associative
 */
comp:
        order
 |      order OpEq  order       { $$ = same($1, $3); }
 |      order OpNeq order       { $$ = not(same($1, $3)); }
 |      order '&'   order       { $$ = call($1, "and", list($3)); }
 |      order '|'   order       { $$ = call($1, "or", list($3)); }
 |      order '^'   order       { $$ = call($1, "xor", list($3)); }

 |      order MatchBind pattern { $$ = matchBind($1, $3); }
 |      order MisMatch  pattern { $$ = not(matchBind($1, $3)); }
 ;


/*
 * Non associative
 */
order:
        interval
 |      interval OpLeq interval { $$ = leq($1, $3); }
 |      interval '>'   interval { $$ = not(leq($1, $3)); }
 |      interval OpGeq interval { $$ = geq($1, $3); }
 |      interval '<'   interval { $$ = not(geq($1, $3)); }
 ;


/*
 * Non associative
 */
interval:
        shift
 |      shift OpThru shift      { $$ = call($1, "thru", list($3)); }
 |      shift OpTill shift      { $$ = call($1, "till", list($3)); }
 ;


/*
 * Left associative
 */
shift:
        add
 |      shift OpAsl add         { $$ = call($1, "shiftLeft", list($3)); }
 |      shift OpAsr add         { $$ = call($1, "shiftLeft",
                                            list(call($3, "negate", list())));
                                }
 ;


/*
 * Left associative
 */
add:
        mult
 |      add '+' mult            { $$ = call($1, "add", list($3)); }
 |      add '-' mult            { $$ = call($1, "subtract", list($3)); }
 ;


/*
 * Left associative
 */
mult:
        pow
 |      mult '*'      pow       { $$ = call($1, "multiply", list($3)); }
 |      mult '/'      pow       { $$ = call($1, "approxDivide", list($3)); }
 |      mult OpFlrDiv pow       { $$ = call($1, "floorDivide", list($3)); }
 |      mult '%'      pow       { $$ = call($1, "remainder", list($3)); }
 |      mult OpMod    pow       { $$ = mod($1, $3); }
 ;


/*
 * Non-associative
 */
pow:
        call
 |      call OpPow call         { $$ = call($1, "pow", list($3)); }
 ;


/*
 * Left associative
 */
call:
        unary
 |      call verb       '(' argList ')' { $$ = call($1, $2, $4); }
 |      call verb                       { $$ = call($1, $2, list()); }
 |      call Send verb  '(' argList ')' { $$ = send($1, $3, $5); }
 |      call Send verb                  { $$ = send($1, $3, list()); }
/*
 |      call Send       '.' ident       { $$ = send($1, "get",
                                                    list(name($4))); }
*/
 |      call Send       '[' expr ']'    { $$ = send($1, "get", list($4)); }

 |      call Send       '(' argList ')' { $$ = send($1, "run", $4); }
 |      call Send unaryPattOp '(' ')'   { $$ = send($1, $3, list()); }
 |      call Send '-'         '(' ')'   { $$ = send($1, "negate", list()); }
 |      call Send binaryOp '(' expr ')' { $$ = send($1, $3, list($5)); }
 |      call Send '-'      '(' expr ')' { $$ = send($1, $3, list($5)); }
 |      call Send OpPow    '(' expr ')' { $$ = send($1, "pow", list($5)); }
 |      call Send OpPow OpMod '(' expr ',' expr ')'
                                        { $$ = send($1, "modPow",
                                                    list($5, $7)); }
 ;


/*
 * Non-associative
 */
unary:
        postfix
 |      unaryPattOp postfix             { $$ = call($2, $1, list()); }
 |      '-'         postfix             { $$ = call($2, "negate", list()); }
 |      '&'         postfix             { yyerror("reserved: unary &"); }
 ;


/*
 * Left associative
 */
postfix:
        prim
 |      postfix '(' argList ')'         { $$ = call($1, "run", $3); }
/*  |   postfix '.' ident               { $$ = path($1, $3); } */
 |      postfix '[' expr ']'            { $$ = call($1, "get", list($3)); }
 |      postfix QuasiString             { $$ = quasiExpr($1, $2); }
 ;


/*
 * A no-parse-ambiguity-possible expression.
 */
prim:
        noun
 |      Literal                         { $$ = literal($1); }
 |      QuasiString                     { $$ = quasiExpr(noun("sprintf"), $1);}
 |      parenExpr
 |      POV                             { $$ = new PovExpr(); }
 |      '[' assocList ']'               { $$ = mapping($2); }
 |      ESCAPE pattern body             { $$ = escape($2, $3); }
 |      LET body                        { $$ = let($2); }
 |      LOOP         body               { $$ = loop($2); }
 |      LOOP pattern body               { $$ = escape($2, loop($3)); }
 |      THROW parenExpr                 { $$ = throwx($2); }
 |      ENTER_POV parenExpr body        { $$ = enterPov($2, $3); }
 |      DISPATCH vtable                 { $$ = $2; }
 |      method                          { $$ = dispatch(list($1), null); }
 |      matcher                         { $$ = dispatch(list(), $1); }
 |      ifExpr
 |      tryExpr
 |      OBJECT vtable                   { $$ = object($2); }
 |      SWITCH parenExpr mtable         { $$ = switchx($2, $3); }
 |      WHILE parenExpr body            { $$ = whilex($2, $3); }
 |      FOR assocPattern IN parenExpr forVerb body
                                        { $$ = forx($2, $4, $5, $6); }
 |      macro
 ;


/*
 * Evaluates to the binding of this name in the current pov
 */
noun:
        ident                   { $$ = noun($1); }
 |      compoundName            { $$ = compoundNoun($1); }
 ;


/* 
 * Body represents the potential to evaluate expr in its own scope
 * (which is always a child of the current lexical scope, except with
 * an enterPov expression). 
 */
body:
        '{' expr '}'                    { $$ = $2; }
 |      '{'      '}'                    { $$ = nullExpr(); }
 ;


/*
 * As an expression, this defines an object whose only non-default
 * behavior is this method.  Ie,
 *
 *      method-as-expr  ->  dispatch { method }
 *
 * As a method, this will match the corresponding message and evaluate
 * its body in the current lexical scope extended by binding the
 * msgPatt's patterns to the message's args.
 */
method:
        TO msgPatt body                 { $$ = method($2, $3); }
 ;


/* 
 * Within a dispatch clause, this will generically match any message
 * not matched by an earlier method.  The pattern will be bound to a
 * 2-Tuple whose 0-element will be the verb string, and the 1-element
 * will be a Tuple of the argument values.  As an expression, this
 * defines an object whose only non-default behavior is this matcher.
 * Ie,
 *
 *      matcher-as-expr -> dispatch { matcher }
 */
matcher:
        MATCH pattern body                { $$ = matcher($2, $3); }
 ;


/*
 * Control constructs often take their expression in parens, so this
 * case is broken out.
 */
parenExpr:
        '(' expr ')'                    { $$ = $2; }
 ;


/*
 * "if" expressions require curlies around their blocks, but "else if"
 * is accepted directly.
 */
ifExpr:
        IF parenExpr body               { $$ = ifx($2, $3); }
 |      IF parenExpr body ELSE ifExpr   { $$ = ifx($2, $3, $5); }
 |      IF parenExpr body ELSE body     { $$ = ifx($2, $3, $5); }
 ;


tryExpr:
        TRY body CATCH pattern body optFinally { $$ = tryx($2,   $4,  $5, $6);}
 |      TRY body CATCH         body optFinally { $$ = tryx($2, null,  $4, $5);}
 |      TRY body                    optFinally { $$ = tryx($2, null,null, $3);}
 ;


macro:
        BlockMacro    macroArg body   optMacro { $$ = macro($1, $2, $3, $4); }
 |      DispatchMacro macroArg vtable optMacro { $$ = macro($1, $2, $3, $4); }
 ;

/*
********************************************
**               Non-Expressions
********************************************
*/

macroArg:
        /*empty*/                       { $$ = null; }
 |      parenExpr
 |      pattern
 ;

optMacro:
        /*empty*/                       { $$ = null; }
 |      macro
 ;


optEOLs:
        /*empty*/
 |      EOLs
 ;

EOLs:
        EOL
 |      EOLs EOL
 ;


/*
 *
 */
emptyList:
        /*empty*/                       { $$ = list(); }
 ;


/*
 * The 'ident' form below is the only defining lambda occurance of a
 * noun.  
 *
 * Wherever a pattern is needed, a "()" can be used to match anything
 * and suppress binding a name.
 */
pattern:
        primPatt
 |      primPatt ':' prim               { $$ = suchThat($1, $3); }
 ;

primPatt:
        ident                           { $$ = patternName($1); }
 |      '_'                             { $$ = Pattern.ignore(); }
 |      OpEq prim                       { $$ = patternEquals($2); }
 |      '[' patternList ']'             { $$ = patternTuple($2); }
 |      '[' patternList ']' '+' primPatt { $$ = patternTuple($2, $5); }
 |      QuasiString                     { $$ = quasiPattern(noun("sscanf"),
                                                            $1); }
 |      noun QuasiString                { $$ = quasiPattern($1, $2); }
 ;

patternList:
        emptyList
 |      patterns
 ;

patterns:
        pattern                         { $$ = list($1); }
 |      patterns ',' pattern            { $$ = with($1, $3); }
 ;

argList:
        emptyList
 |      args
 ;

args:
        expr                            { $$ = list($1); }
 |      args ',' expr                   { $$ = with($1, $3); }
 ;


assocList:
        emptyList
 |      assocs
 ;

assocs:
        assoc                           { $$ = list($1); }
 |      assocs ',' assoc                { $$ = with($1, $3); }
 ;

assoc:
                    expr                { $$ = new Assoc(null, $1); }
 |      expr MapsTo expr                { $$ = new Assoc($1,   $3); }
 ;

assocPattern:
                       pattern          { $$ = new Assoc(Pattern.ignore(),$1);}
 |      pattern MapsTo pattern          { $$ = new Assoc($1,   $3); }
 ;


/*
 * A message selector
 */
verb:
        ident
 ;


ident:
        Identifier                      { $$ = ((EZToken)$1).token(); }
 |      reserved                        { yyerror("reserved keyword: \""
                                                  + ((EZToken)$1).token()
                                                  + "\""); }
 ;

compoundName:
        ident '.' ident         { $$ = ((String)$1) + "." + ((String)$3); }
|       compoundName '.' ident  { $$ = ((String)$1) + "." + ((String)$3); }
;


/*
 *
 */
forVerb:
        /*empty*/                       { $$ = "each"; }
 |      verb
 ;


/*
 * x op= y  ->  x := x op y
 *
 * Except that side effects with 'x' must only happen once, and before
 * y is evaluated.  (The current Parser is known not to do this
 * correctly.)
 *
 * OpAss and OpAssAsr are handled separately.
 */
assignop:
        OpAssAdd                        { $$ = "add"; }
 |      OpAssAnd                        { $$ = "and"; }
 |      OpAssAprxDiv                    { $$ = "approxDivide"; }
 |      OpAssFlrDiv                     { $$ = "floorDivide"; }
 |      OpAssAsl                        { $$ = "shiftLeft"; }
 |      OpAssRemdr                      { $$ = "remainder"; }
 |      OpAssMod                        { $$ = "mod"; }
 |      OpAssMul                        { $$ = "multiply"; }
 |      OpAssOr                         { $$ = "or"; }
 |      OpAssPow                        { $$ = "pow"; }
 |      OpAssSub                        { $$ = "subtract"; }
 |      OpAssXor                        { $$ = "xor"; }
 ;


/*
 * Corresponds directly to a verb, so it can appear in a method
 * definition.
 *
 * Not listed: '-', since it is also binary.
 */
unaryPattOp:
        '!'                             { $$ = "not"; }
 |      '~'                             { $$ = "complement"; }


/*
 * Corresponds directly to a verb, so it can appear in a method
 * definition.
 *
 * Not listed: OpPow, since it's also trinary, OpEq, since it's
 * primitive, '-', since it's also unary, and '[' ']' and '.'
 * (indexing), since their syntax is weird.
 */
binaryPattOp:
        '+'                             { $$ = "add"; }
 |      '&'                             { $$ = "and"; }
 |      '|'                             { $$ = "or"; }
 |      '^'                             { $$ = "xor"; }
 |      '/'                             { $$ = "approxDivide"; }
 |      OpFlrDiv                        { $$ = "floorDivide"; }
 |      OpLeq                           { $$ = "lessThanOrEqualTo"; }
 |      OpThru                          { $$ = "thru"; }
 |      OpTill                          { $$ = "till"; }
 |      OpAsl                           { $$ = "shiftLeft"; }
 |      '%'                             { $$ = "remainder"; }
 |      OpMod                           { $$ = "mod"; }
 |      '*'                             { $$ = "multiply"; }
 ;


/*
 * Can appear after a '<-'.
 *
 * See the 'Not listed:' comment in binaryPattOp.
 */
binaryOp:
        binaryPattOp
 |      '<'     /* x < y   ->  (define left$n := x; !(y <= left$n)) */
 |      OpGeq   /* x >= y  ->  (define left$n := x; (y <= left$n)) */
 |      '>'     /* x > y   ->  !(x <= y) */
 |      OpLAnd  /* x && y  ->  (x pick ({y}, {false})) run */
 |      OpLOr   /* x || y  ->  (x pick ({true}, {y})) run */
 |      OpNeq   /* x != y  ->  !(x == y) */
 |      OpAsr   /* x >> y  ->  x << -y */
 ;


methodList:
        emptyList
 |      methods
 ;

methods:
        method optEOLs                  { $$ = list($1); }
 |      methods method optEOLs          { $$ = with($1, $2); }
 ;

optMatcher:
        /*empty*/                       { $$ = null; }
 |      matcher optEOLs
 ;



/*
 * Provides behavior to 'dispatch' and 'object' forms
 */
vtable:
        '{' optEOLs methodList optMatcher '}'   { $$ = dispatch($3, $4); }
 ;


matcherList:
        emptyList
 |      matchers
 ;

matchers:
        matcher optEOLs                 { $$ = list($1); }
 |      matchers matcher optEOLs        { $$ = with($1, $2); }
 ;

/*
 * Provides behavior to 'switch' form (XXX eventually to be unified
 * with vtable )
 */
mtable:
        '{' optEOLs matcherList '}'     { $$ = $3; }
 ;


/*
 * Last part of a 'try' expression
 */
optFinally:
        /*empty*/                       { $$ = null; }
 |      FINALLY body                    { $$ = $2; }
 ;


/*
 * Appears in method definition
 */
msgPatt:
        /*empty*/                   { $$ = msgPatt("run",      list()); }
 |      verb                        { $$ = msgPatt($1,         list()); }
 |      verb '(' patternList ')'    { $$ = msgPatt($1,         $3); }
 |           '(' patternList ')'    { $$ = msgPatt("run",      $2); }
 |      unaryPattOp                 { $$ = msgPatt($1,         list()); }
 |      '-'                         { $$ = msgPatt("negate",   list()); }
 |      binaryPattOp pattern        { $$ = msgPatt($1,         list($2)); }
 |      '-' pattern                 { $$ = msgPatt("subtract", list($2)); }
 |      '[' pattern ']'             { $$ = msgPatt("get",      list($2)); }
 |      OpPow pattern               { $$ = msgPatt("pow",      list($2)); }
 |      OpPow pattern OpMod pattern { $$ = msgPatt("modPow",   list($2, $4)); }
 ;


/*
 * Reserved Keywords
 */
reserved:
        ABSTRACT | AN | AS | BEHALF | BELIEF
 |      BELIEVE | BELIEVES | BIND | CASE | CLASS | CONST | DEFAULT
 |      DEFMACRO | DELEGATE | DEPRECATED | DO | ENCAPSULATE | ENCAPSULATED
 |      ENCAPSULATES | ENSURE | ENUM | EVENTUAL | EVENTUALLY | EXISTS
 |      EXPORT | EXTENDS | FINAL | FORALL | GIVEN | HIDDEN
 |      HIDE | HIDES | IMPLEMENTS | IMPORT | INTERFACE | IS
 |      ISA | KNOW | KNOWS | METHODS | NATIVE
 |      ON | PACKAGE | PRIVATE | PROTECTED | PUBLIC | REQUIRE
 |      SAKE | STATIC | STRUCT | SUCHTHAT | SYNCHRONIZED
 |      THIS | THROWS | TRANSIENT | TYPEDEF | VIRTUAL | VOID
 |      VOLATILE
 ;


%%


/** contains all the tokens after myToken */
private EZLexer myLexer = null;

/** generated temp variable count */
private int myTempCount = 0;

private Expr myResult = null;

private PrintStream mySpamStream = null;

public yaccpar(EZLexer lexer) {
    this();
    myLexer = lexer;
}

public yaccpar(EZLexer lexer, PrintStream spamStream) {
    this(true);
    myLexer = lexer;
    mySpamStream = spamStream;
}


public Expr parseExpr() {
    if (yyparse() != 0) {
        yyerror("couldn't parse expression");
    }
    return myResult;
}


int yylex() {
    EZToken token = null;
    try {
        token = myLexer.nextToken();
    } catch (IOException ex) {
        yyerror("io: " + ex);
    }
    yytext = token.token();
    yylval = token;
    return token.tokenType();
}


void yyprintln(String s) {
    mySpamStream.println(s);
}


void yyerror(String s) {
    throw new SyntaxException(s);
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


/* pretty self explanatory */
public boolean isEndOfFile() {
    return myLexer.isEndOfFile();
}



/*
*************************
* For use by the actions
*************************
*/


Vector with(Object sofar, Object next) {
    Vector result = (Vector)sofar;
    result.addElement(next);
    return result;
}


Vector list() {
    return new Vector();
}

Vector list(Object a) {
    return with(list(), a);
}

Vector list(Object a, Object b) {
    return with(list(a), b);
}


NounExpr noun(Object name) {
    return new NounExpr((String)name);
}

PathExpr compoundNoun(Object longName) {
    return new PathExpr((String)longName);
}


/**
 * generate 'unique' temporary variable names for transformations
 */
private String newTemp(String prefix) {
    return prefix + "$" + myTempCount++;
}

/**
 * Pattern Match stuff
 */

Pattern patternName(Object name) {
    return Pattern.name((String)name);
}

Pattern suchThat(Object pattern, Object condExpr) {
    return Pattern.suchThat((Pattern)pattern, (Expr)condExpr);
}

/**
 *   (== expr)  ->   (specimen : specimen == expr)
 */
Pattern patternEquals(Object expr) {
    String specimen = newTemp("specimen");
    return suchThat(patternName(specimen),
                    same(noun(specimen), (Expr)expr));
}

Pattern patternTuple(Object subPatterns) {
    return patternTuple(subPatterns, null);
}

Pattern patternTuple(Object subs, Object optRestPattern) {
    return Pattern.tuple(patterns(subs), (Pattern)optRestPattern);
}


Expr matchBind(Object specimen, Object pattern) {
    return new MatchBindExpr((Expr)specimen, (Pattern)pattern);
}

Expr define(Object pattern, Object rValue) {
    return new DefineExpr((Pattern)pattern, (Expr)rValue);
}

Expr condAnd(Object left, Object right) {
    return new CondAndExpr((Expr)left, (Expr)right);
}

Expr ifx(Object condExpr, Object thenExpr, Object elseExpr) {
    return new IfExpr((Expr)condExpr, (Expr)thenExpr, (Expr)elseExpr);
}

Expr ifx(Object condExpr, Object thenExpr) {
    return ifx(condExpr, thenExpr, nullExpr());
}

Expr condOr(Object left, Object right) {
    return ifx(left, trueExpr(), right);
}


/**
 *  parser `foo $bar baz $zip zorp`
 * ->
 *  parser valueMaker("foo $0 baz $1 zorp") make(bar, zip)
 */
Expr quasiExpr(Object parser, Object quasiString) {
    EZQuasiString q = (EZQuasiString)quasiString;
    if (q.patterns().size() > 0) {
        throw new SyntaxException("patterns not allowed in quasi-expression");
    }
    Expr exprMaker = call(parser, "valueMaker", list(q.template()));
    return call(exprMaker, "make", q.exprs());
}


/**
 *  parser `foo $bar baz @zip zorp`
 * ->
 *  x : parser matchMaker("foo $0 baz @0 zorp") match(bar, x) =~ [zip]
 */
Pattern quasiPattern(Object parser, Object quasiString) {
    String tempName = newTemp("q");
    EZQuasiString q = (EZQuasiString)quasiString;
    Expr matchMaker = call(parser, "matchMaker", list(q.template()));
    Vector exprs = with((Vector)q.exprs().clone(), noun(tempName));
    Expr matcher = call(matchMaker, "match", exprs);

    Pattern binds = patternTuple(q.patterns());
    Expr mb = new MatchBindExpr(matcher, binds);
    return Pattern.suchThat(Pattern.name(tempName), mb);
}



/**
 *  switch (expr) {
 *      match pattern1 { body1 }
 *      match pattern2 { body2 }
 *  }
 * ->
 *  let {
 *      define temp = expr
 *      if (temp =~ pattern1) {
 *          body1
 *      } else if (temp =~ pattern2) {
 *          body2
 *      } else {
 *          define ():false := temp
 *      }
 *  }
 */
Expr switchx(Object specimen, Object mtable) {
    String tempName = newTemp("specimen");
    Expr temp = noun(tempName);
    Expr defTemp = define(patternName(tempName), specimen);
    Expr result = define(suchThat(Pattern.ignore(), falseExpr()),
                         temp);
    MatchNode[] matchers = (MatchNode[])arrayFromVector((Vector)mtable,
                                                        MatchNode.class);
    for (int i = matchers.length -1; i >= 0; i--) {
        MatchNode m = matchers[i];
        result = ifx(matchBind(temp, m.pattern()),
                     m.body(),
                     result);
    }
    return let(sequence(defTemp, result));
}


Expr let(Object body) {
    return new LetExpr((Expr)body);
}


MsgPatt msgPatt(Object verb, Object parms) {
    return new MsgPatt((String)verb, patterns(parms));
}


private final Expr NULL_EXPR  = noun("null");
private final Expr FALSE_EXPR = noun("false");
private final Expr TRUE_EXPR  = noun("true");
private final Expr TUPLE_EXPR = noun("makeTuple");
private final Expr MAPPING_EXPR = compoundNoun("collect.MappingImpl");

Expr nullExpr()     { return NULL_EXPR; }
Expr falseExpr()    { return FALSE_EXPR; }
Expr trueExpr()     { return TRUE_EXPR; }
Expr tupleExpr()    { return TUPLE_EXPR; }
Expr mappingExpr()  { return MAPPING_EXPR; }


MethodNode method(Object msgPatt, Object bodyExpr) {
    MsgPatt patt = (MsgPatt)msgPatt;
    return new EZMethodNode(patt.verb(),
                            patt.patterns(),
                            (Expr)bodyExpr);
}

MatchNode matcher(Object pattern,
                  Object bodyExpr) {
    return new MatchNode((Pattern)pattern,
                         (Expr)bodyExpr);
}


Expr dispatch(Object methods, Object optMatcher) {
    NameTableEditor meths = new NameTableEditorImpl();
    for (Enumeration iter = ((Vector)methods).elements();
         iter.hasMoreElements(); ) {

        MethodNode meth = (MethodNode)iter.nextElement();
        try {
            meths.introduce(meth.mangle(), meth);
        } catch (AlreadyDefinedException e) {
            yyerror("Methods in a 'dispatch' must be uniquely named " + e);
        }
    }
    return new DispatchExpr(meths, (MatchNode)optMatcher);
}


Expr thunkify(Object bodyExpr) {
    return dispatch(list(method(msgPatt("run", list()),
                                bodyExpr)),
                    null);
}


Expr literal(Object token) {
    EZLiteral tok = (EZLiteral)token;
    return new LiteralExpr(tok.token(), tok.value());
}


/**
 * Caution: the argument must not have any characters that need to be
 * escaped in order to be presented in string literal format.
 */
Expr name(Object ident) {
    String str = (String)ident;
    return new LiteralExpr("\"" + str + "\"", str);
}


Expr path(Object base, Object ident) {
    return call(base, "get", list(name(ident)));
}


/**
 * [x, y, z]       ->  makeTuple(x, y, z)
 *
 * [x, a => y, z]  ->  collect.MappingImpl([0, a, 2], [x, y, z])
 */
Expr mapping(Object assocList) {
    Assoc[] assocs = (Assoc[])arrayFromVector((Vector)assocList, Assoc.class);
    boolean hasKey = false;
    Vector keys   = list();
    Vector values = list();
    for (int i = 0; i < assocs.length; i++) {
        Expr key = (Expr)assocs[i].optKey();
        if (key == null) {
            key = new LiteralExpr(""+i, BigInteger.valueOf(i));
        } else {
            hasKey = true;
        }
        keys = with(keys, key);
        values = with(values, (Expr)assocs[i].optValue());
    }
    Expr vs = call(tupleExpr(), "run", values);
    if (hasKey) {
        Expr ks = call(tupleExpr(), "run", keys);
        return call(mappingExpr(), "run", list(ks, vs));
    } else {
        return vs;
    }
}


Expr escape(Object pattern, Object bodyExpr) {
    return new EscapeExpr((Pattern)pattern, (Expr)bodyExpr);
}

Expr loop(Object bodyExpr) {
    return new LoopExpr((Expr)bodyExpr);
}

Expr tryx(Object expr,
            Object optPattern,
            Object optCatcher,
            Object optFinally) {
    return new TryExpr((Expr)expr,
                       (Pattern)optPattern,
                       (Expr)optCatcher,
                       (Expr)optFinally);
}

Expr throwx(Object problemExpr) {
    return new ThrowExpr((Expr)problemExpr);
}

Expr enterPov(Object povExpr, Object bodyExpr) {
    return new EnterPovExpr((Expr)povExpr, (Expr)bodyExpr);
}

/**
 * This is the expansion of 'object'. It contains some methods &
 * an optional matcher.  That is then wrapped in some
 * stuff which binds 'self' and 'return' to what you'd expect
 * them to be. it corresponds to
 *
 *  let { 
 *      define self := match [verb, args] {
 *          escape return {
 *              dispatch {
 *                  methods* [matcher]
 *              } perform(verb, args)
 *          }
 *      }
 *  }
 */
Expr object(Object vtable) {

    String verb = newTemp("verb");
    String args = newTemp("args");

    Expr performer = call((Expr)vtable, 
                          "perform",
                          list(noun(verb), noun(args)));

    MatchNode matcher = matcher(patternTuple(list(patternName(verb),
                                                patternName(args))),
                                escape(patternName("return"),
                                       performer));

    return let(define(patternName("self"),
                      dispatch(list(), matcher)));
}


/* 
 * The while loop expands as follows:
 *
 *  while (cond) { body }   ->
 *
 *  escape break {
 *      loop {
 *          if (cond) {
 *              escape continue { body }
 *          } else {
 *              break()
 *          }
 *      }
 *  }
 */
Expr whilex(Object condExpr, Object bodyExpr) {

    Expr forever = ifx(condExpr,
                       escape(patternName("continue"), bodyExpr),
                       call(noun("break"), "run", list()));

    return escape(patternName("break"), loop(forever));
}


/* 
 * The for loop has the following expansion:
 *
 *  for [kPattern =>] vPattern in (expr) [verb] { body }   ->
 *
 *  escape break {
 *      expr associations verb(to run(k, v) {
 *          if (k =~ kPattern && v =~ vPattern) {
 *              escape continue { body }
 *          }
 *      })
 *  }
 */
Expr forx(Object assoc,
          Object collExpr,
          Object forVerb,
          Object bodyExpr) {

    String kTemp = newTemp("key");
    String vTemp = newTemp("value");

    Assoc patterns = (Assoc)assoc;
    Pattern key = (Pattern)patterns.optKey();
    Pattern value = (Pattern)patterns.optValue();

    MsgPatt mpatt = msgPatt("run",
                            list(patternName(kTemp), patternName(vTemp)));

    Expr body = ifx(condAnd(matchBind(noun(kTemp), key),
                            matchBind(noun(vTemp), value)),
                    escape(patternName("continue"), bodyExpr));

    Expr closure = dispatch(list(method(mpatt, body)),
                            null);

    return escape(patternName("break"),
                  call(call(collExpr, "associations", list()),
                       (String)forVerb,
                       list(closure)));
}

/**
 *
 */
Expr macro(Object macroName,
           Object optMacroArg,
           Object macroBody,
           Object optNextMacro) {
    throw new Error("XXX Not yet implemented");
}


/**
 * Should be put into a more generic place
 */
static public Object[] arrayFromVector(Vector vec, Class elementType) {
    if (elementType.isPrimitive()) {
        throw new IllegalArgumentException("only reference types");
    }
    Object[] result = (Object[])Array.newInstance(elementType, vec.size());
    vec.copyInto(result);
    return result;
}

Expr[] exprs(Object vec) {
    return (Expr[])arrayFromVector((Vector)vec, Expr.class);
}

Pattern[] patterns(Object vec) {
    return (Pattern[])arrayFromVector((Vector)vec, Pattern.class);
}

Expr call(Object recipientExpr,
          Object verb,
          Object args) {
    return new CallExpr((Expr)recipientExpr,
                        (String)verb,
                        exprs(args));
}


Expr send(Object recipientExpr,
          Object verb,
          Object args) {
    return new SendExpr((Expr)recipientExpr,
                        (String)verb,
                        exprs(args));
}


Expr mod(Object val, Object modulus) {
    if (val instanceof CallExpr) {
        CallExpr ce = (CallExpr)val;
        RequestNode req = ce.request();
        Expr[] args = req.args();
        if (req.verb().equals("pow") && args.length == 1) {
            Expr base = ce.recipient();
            Expr exp = args[0];
            return call(base, "modPow", list(exp, modulus));
        }
    }
    return call(val, "mod", list(modulus));
}


Expr leq(Object x, Object y) {
    return call(x, "lessThanOrEqualTo", list(y));
}


Expr geq(Object x, Object y) {
    String temp = newTemp("left");
    return sequence(define(patternName(temp), (Expr)x),
                    leq(y, noun(temp)));
}

Expr not(Object x) {
    return call(x, "not", list());
}

Expr same(Object x, Object y) {
    return new EqualsExpr((Expr)x, (Expr)y);
}


Expr assign(Object lValue, Object rValue) {

    Expr lval = (Expr)lValue;
    Expr rval = (Expr)rValue;

    /*
     *  x ":=" z
     */
    if (lval instanceof NounExpr) {
        /* the base case */
        String name = ((NounExpr)lval).name();
        return new AssignExpr(name, rval);
    }
    if (lval instanceof CallExpr) {
        CallExpr ce = (CallExpr)lval;
        Expr recip = ce.recipient();
        RequestNode req = ce.request();
        String verb = req.verb();
        Expr[] args = req.args();

        /*
         *  x get(y) := z         ->  x put(y, z)
         *  x get(y1, y2) := z    ->  x put(y1, y2, z)
         *
         * Remember that x[y]     ->  x get(y)
         * and           x.name   ->  x get("name")
         * so both are valid lvalues
         */
        if (verb.equals("get")) {
            Expr[] newArgs = new Expr[args.length +1];
            System.arraycopy(args, 0, newArgs, 0, args.length);
            newArgs[args.length] = rval;
            return new CallExpr(recip, "put", newArgs);
        }
    }
    throw new SyntaxException("Assignment can only be done to nouns "
                              + "and collection elements");
}



Expr update(Object lValue, Object verb, Object rValue) {
    //XXX BUG Must avoid repeating lValue side effects

    return assign(lValue, call(lValue, verb, list(rValue)));
}

Expr assAsr(Object lValue, Object rValue) {

    return update(lValue, "shiftLeft", call(rValue, "negate", list()));
}


Expr sequence(Object x, Object y) {
    return new SequenceExpr((Expr)x, (Expr)y);
}


/*********************************/


static String[] TheTokens = new String[yyname.length];

/* Not provided for us */
public final static short EOF = 0;

static {
    System.arraycopy(yyname, 0, TheTokens, 0, yyname.length);

    /* printrep must not be a token */
    TheTokens[EOF]              = "end-of-file";
    TheTokens[Identifier]       = "non-keyword-identifier";
    TheTokens[Literal]          = "literal-value";
    TheTokens[QuasiString]      = "quasi-string";
    TheTokens[BlockMacro]       = "block-macro";
    TheTokens[DispatchMacro]    = "dispatch-macro";

    /* Keywords */
    TheTokens[CATCH]     = "catch";
    TheTokens[DEFINE]    = "define";
    TheTokens[DISPATCH]  = "dispatch";
    TheTokens[ELSE]      = "else";
    TheTokens[ENTER_POV] = "enterPov";
    TheTokens[FINALLY]   = "finally";
    TheTokens[FOR]       = "for";
    TheTokens[IF]        = "if";
    TheTokens[IN]        = "in";
    TheTokens[LET]       = "let";
    TheTokens[LOOP]      = "loop";
    TheTokens[ESCAPE]    = "escape";
    TheTokens[MATCH]     = "match";
    TheTokens[OBJECT]    = "object";
    TheTokens[POV]       = "pov";
    TheTokens[SWITCH]    = "switch";
    TheTokens[THROW]     = "throw";
    TheTokens[TO]        = "to";
    TheTokens[TRY]       = "try";
    TheTokens[WHILE]     = "while";

    /* reserved keywords */
    TheTokens[ABSTRACT]         = "abstract";
    TheTokens[AN]               = "an";
    TheTokens[AS]               = "as";
    TheTokens[BEHALF]           = "behalf";
    TheTokens[BELIEF]           = "belief";
    TheTokens[BELIEVE]          = "believe";
    TheTokens[BELIEVES]         = "believes";
    TheTokens[BIND]             = "bind";
    TheTokens[CASE]             = "case";
    TheTokens[CLASS]            = "class";
    TheTokens[CONST]            = "const";
    TheTokens[DEFAULT]          = "default";
    TheTokens[DEFMACRO]         = "defmacro";
    TheTokens[DELEGATE]         = "delegate";
    TheTokens[DEPRECATED]       = "deprecated";
    TheTokens[DO]               = "do";
    TheTokens[ENCAPSULATE]      = "encapsulate";
    TheTokens[ENCAPSULATED]     = "encapsulated";
    TheTokens[ENCAPSULATES]     = "encapsulates";
    TheTokens[ENSURE]           = "ensure";
    TheTokens[ENUM]             = "enum";
    TheTokens[EVENTUAL]         = "eventual";
    TheTokens[EVENTUALLY]       = "eventually";
    TheTokens[EXISTS]           = "exists";
    TheTokens[EXPORT]           = "export";
    TheTokens[EXTENDS]          = "extends";
    TheTokens[FINAL]            = "final";
    TheTokens[FORALL]           = "forall";
    TheTokens[GIVEN]            = "given";
    TheTokens[HIDDEN]           = "hidden";
    TheTokens[HIDE]             = "hide";
    TheTokens[HIDES]            = "hides";
    TheTokens[IMPLEMENTS]       = "implements";
    TheTokens[IMPORT]           = "import";
    TheTokens[INTERFACE]        = "interface";
    TheTokens[IS]               = "is";
    TheTokens[ISA]              = "isa";
    TheTokens[KNOW]             = "know";
    TheTokens[KNOWS]            = "knows";
    TheTokens[METHODS]          = "methods";
    TheTokens[NATIVE]           = "native";
    TheTokens[ON]               = "on";
    TheTokens[PACKAGE]          = "package";
    TheTokens[PRIVATE]          = "private";
    TheTokens[PROTECTED]        = "protected";
    TheTokens[PUBLIC]           = "public";
    TheTokens[REQUIRE]          = "require";
    TheTokens[SAKE]             = "sake";
    TheTokens[STATIC]           = "static";
    TheTokens[STRUCT]           = "struct";
    TheTokens[SUCHTHAT]         = "suchthat";
    TheTokens[SYNCHRONIZED]     = "synchronized";
    TheTokens[THIS]             = "this";
    TheTokens[THROWS]           = "throws";
    TheTokens[TRANSIENT]        = "transient";
    TheTokens[TYPEDEF]          = "typedef";
    TheTokens[VIRTUAL]          = "virtual";
    TheTokens[VOID]             = "void";
    TheTokens[VOLATILE]         = "volatile";

    /* The magical end-of-line token, not considered whitespace */
    TheTokens[EOL]      = "\n";

    /* Multi-Character Operators */
    TheTokens[OpFlrDiv] = "_/";
    TheTokens[OpEq]     = "==";
    TheTokens[OpGeq]    = ">=";
    TheTokens[OpLAnd]   = "&&";
    TheTokens[OpLOr]    = "||";
    TheTokens[OpLeq]    = "<=";
    TheTokens[OpThru]   = "..";
    TheTokens[OpTill]   = "..!";
    TheTokens[OpAsl]    = "<<";
    TheTokens[OpAsr]    = ">>";
    TheTokens[OpMod]    = "%%";
    TheTokens[OpNeq]    = "!=";
    TheTokens[OpPow]    = "**";

    TheTokens[OpAss]            = ":=";
    TheTokens[OpAssAdd]         = "+=";
    TheTokens[OpAssAnd]         = "&=";
    TheTokens[OpAssAprxDiv]     = "/=";
    TheTokens[OpAssFlrDiv]      = "_/=";
    TheTokens[OpAssAsl]         = "<<=";
    TheTokens[OpAssAsr]         = ">>=";
    TheTokens[OpAssRemdr]       = "%=";
    TheTokens[OpAssMod]         = "%%=";
    TheTokens[OpAssMul]         = "*=";
    TheTokens[OpAssOr]          = "|=";
    TheTokens[OpAssPow]         = "**=";
    TheTokens[OpAssSub]         = "-=";
    TheTokens[OpAssXor]         = "^=";

    /* Other funky tokens */
    TheTokens[Send]             = "<-";
    TheTokens[MapsTo]           = "=>";
    TheTokens[MatchBind]        = "=~";
    TheTokens[MisMatch]         = "!~";
}

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
