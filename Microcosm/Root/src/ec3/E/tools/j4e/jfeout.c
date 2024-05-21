/*
  jfeout.c -- Output generation for the E-to-pure Java translator

  Chip Morningstar
  Electric Communities
  11-January-1998

  Copyright 1998 Electric Communities, all rights reserved worldwide.

*/

#include "generic.h"
#include "yh.h"
#include "yh_struct.h"
#include "yh_build.h"
#include "yh_walk.h"
#include "y.tab.h"
#include "jfe.h"

static int GenSymCounter = 0;   /* For uniqifying symbols */

/* This is the big hairy environment struct which is carried around as we
   walk the parse tree generating output */
typedef struct {
    YT(compilationUnit) *currentUnit; /* Compilation unit being processed    */
    YT(distContext) *distContext; /* Symbol context for seeing distributors  */
    YT(identifier) *implicitDistParam; /* If found                           */
    YT(stringStack) *preTokenStrings; /* Text to follow whitespace           */
    YT(type) *currentType;      /* Most recent variable declarator type      */
    bool doingSend;             /* Method invocation is <- operator          */
    bool findImplicitDistributor; /* Look for implicit distributor parameter */
    bool haveThrown;            /* True if exception generated in cur. block */
    bool openExceptionContext;  /* Block is top of exception context         */
    bool openResultContext;     /* Block is top of result context            */
    bool outputHere;            /* Outputting to current working directory   */
    bool outputConfigured;      /* Have the output file all set up           */
    bool suppressWhitespace;    /* Don't output captured whitespace          */
    char *outputBaseDirName;    /* Where the output is rooted                */
    int exceptionEnv;           /* # of exception catching var               */
} t_e_env;

YA_FUNC_DEF(eWalker);

/* Following macros are syntactic sugar for output generation */

#define EWALKA(what)  \
    YH_WALK(statement, YC(statement,what), YA_FUNC(eWalker), env)
#define EWALKAL(what) \
    YH_WALK(statementList, YC(statementList,what), YA_FUNC(eWalker), env)

#define EWALK(what)     EWALKA(arg->what)
#define EWALKL(what)    EWALKAL(arg->what)
#define ES(s)           eputs(s)
#define EP              eprintf
#define PRESTRING(s)    pushString(env, s)
#define EW(ws)          emitWhitespace(env, arg->ws);
#define EWS(ws, s)      if (arg->ws) { EW(ws); ES(s); }
#define EKEY(w)         EWS(w##WS, #w)
#define EFLD(w)         EWS(w##WS, arg->w);
#define ETOKEN(t)       EWS(t##WS, etoken(arg->t));
#define E_SEMICOLON     EWS(semicolonWS, ";")
#define E_COLON         EWS(colonWS, ":")
#define E_AMPER         EWS(amperWS, "&")
#define E_DOT           EWS(dotWS, ".")
#define E_COMMA         EWS(commaWS, ",")
#define E_STAR          EWS(starWS, "*")
#define E_EQUALS        EWS(equalsWS, "=")
#define E_QMARK         EWS(qmarkWS, "?")
#define E_OPENPAREN     EWS(openParenWS, "(")
#define E_CLOSEPAREN    EWS(closeParenWS, ")")
#define E_OPENBRACE     EWS(openBraceWS, "{")
#define E_CLOSEBRACE    EWS(closeBraceWS, "}")
#define E_OPENBRACKET   EWS(openBracketWS, "[")
#define E_CLOSEBRACKET  EWS(closeBracketWS, "]")
#define ESYM(sym)       if (arg->sym) { EW(sym##WS); ES(arg->sym->name); }
#define ECASE(type, block) YA_CASE(type, { block; YA_RETURN(arg); } )
#define ESCOPED(sym)    if (arg->sym) {                         \
                            EW(sym##WS);                        \
                            outputScopedSymbol(env, arg->sym);  \
                        }
#define THIS_CANT_HAPPEN yh_error("<internal error> dummy type in parse tree!")

#define PUSH_ENV(var,val,block) {       \
    t_e_env saveEnv;                    \
    saveEnv.var = env->var;             \
    env->var = val;                     \
    block                               \
    env->var = saveEnv.var;             \
}

#define PUSH_ENV2(var1,val1,var2,val2,block) { \
    t_e_env saveEnv;                    \
    saveEnv.var1 = env->var1;           \
    saveEnv.var2 = env->var2;           \
    env->var1 = val1;                   \
    env->var2 = val2;                   \
    block                               \
    env->var1 = saveEnv.var1;           \
    env->var2 = saveEnv.var2;           \
}

#define PUSH_ENV3(var1,val1,var2,val2,var3,val3,block) { \
    t_e_env saveEnv;                    \
    saveEnv.var1 = env->var1;           \
    saveEnv.var2 = env->var2;           \
    saveEnv.var3 = env->var3;           \
    env->var1 = val1;                   \
    env->var2 = val2;                   \
    env->var3 = val3;                   \
    block                               \
    env->var1 = saveEnv.var1;           \
    env->var2 = saveEnv.var2;           \
    env->var3 = saveEnv.var3;           \
}

/* Table mapping between Java primitive types and corresponding box classes */
static struct {
    int type;                /* Our lexer token for the primitive type      */
    char *typeName;          /* Name of box class                           */
    char *unboxFuncName;     /* Name of method on box object to unbox value */
} BoxTable[] = {
    BOOLEAN,        "Boolean",      "booleanValue",
    BYTE,           "Integer",      "intValue",
    CHAR,           "Character",    "charValue",
    DOUBLE,         "Double",       "doubleValue",
    FLOAT,          "Float",        "floatValue",
    INT,            "Integer",      "intValue",
    LONG,           "Long",         "longValue",
    SHORT,          "Integer",      "intValue",
    -1,             NULL,           NULL
};

/**
 * boxType -- Given a primitive type, return the name of the corresponding box
 *      class.
 *
 * @param prim  A Java primitive type, described by a primType struct
 * @returns  Box class name, as a string
 */
  static char *
boxType(YT(primType) *prim)
{
    int i;

    for (i=0; BoxTable[i].type > 0; ++i)
        if (BoxTable[i].type == prim->type)
            return(BoxTable[i].typeName);
    return("<???>");
}

/**
 * emitWhitespace -- Output whitespace. If whitespace suppression is currently
 *      on, just output a single space character so as to provide token
 *      separation without messing up the line number. After outputting the
 *      whitespace, output any pre-token strings that have been pushed.
 *
 * @param env  Current environment
 * @param whitespaceString  The whitespace to be output
 */
  static void
emitWhitespace(t_e_env *env, char *whitespaceString)
{
    if (!env->suppressWhitespace && whitespaceString)
        eputs(whitespaceString);
    if (env->suppressWhitespace)
        eputs(" ");
    while (env->preTokenStrings) {
        eputs(env->preTokenStrings->string);
        env->preTokenStrings = env->preTokenStrings->stack;
    }
}

/**
 * etoken -- Return the string to output for a given lexer token.
 *
 * @param token  The lexer token of interest.
 * @returns  A string containing the proper output representation of 'token'.
 */
  static char *
etoken(int token)
{
    static struct {
        int token;
        char *string;
    } TokenTable[] = {
        '!',            "!",
        '%',            "%",
        '&',            "&",
        '*',            "*",
        '+',            "+",
        ',',            ",",
        '-',            "-",
        '.',            ".",
        '/',            "/",
        '<',            "<",
        '=',            "=",
        '>',            ">",
        '^',            "^",
        '|',            "|",
        '~',            "~",
        AssAdd,         "+=",
        AssAnd,         "&=",
        AssAsr,         ">>>=",
        AssDiv,         "/=",
        AssLsl,         "<<=",
        AssLsr,         ">>=",
        AssMod,         "%=",
        AssMul,         "*=",
        AssOr,          "|=",
        AssSub,         "-=",
        AssXor,         "^=",
        OpAsr,          ">>>",
        OpDec,          "--",
        OpEq,           "==",
        OpGeq,          ">=",
        OpInc,          "++",
        OpLAnd,         "&&",
        OpLOr,          "||",
        OpLeq,          "<=",
        OpLsl,          "<<",
        OpLsr,          ">>",
        OpNeq,          "!=",
        Send,           "<-",
        ABSTRACT,       "abstract",
        BOOLEAN,        "boolean",
        BYTE,           "byte",
        CHAR,           "char",
        DOUBLE,         "double",
        EFALSE,         "efalse",
        ENULL,          "enull",
        ETRUE,          "etrue",
        FINAL,          "final",
        FLOAT,          "float",
        INSTANCEOF,     "instanceof",
        INT,            "int",
        EMETHOD,        "emethod",
        JFALSE,         "false",
        JNULL,          "null",
        JTRUE,          "true",
        LOCAL,          "public",
        LONG,           "long",
        NATIVE,         "native",
        PRIVATE,        "private",
        PROTECTED,      "protected",
        PUBLIC,         "public",
        SHORT,          "short",
        STATIC,         "static",
        SUPER,          "super",
        SYNCHRONIZED,   "synchronized",
        THIS,           "this",
        TRANSIENT,      "transient",
        VOID,           "void",
        -1,             NULL
    };
    int i;

    for (i=0; TokenTable[i].string; ++i)
        if (TokenTable[i].token == token)
            return(TokenTable[i].string);
    return("<<???>>");
}

  static YT(expression) *
extractForward(YT(expression) *recv, YT(methodInvocation) *call, t_e_env *env)
{
    if (YTAG_TEST(recv, name)) {
        YT(name) *recvName = YC(name, recv);
        if (recvName->prefix == NULL && env->implicitDistParam &&
                recvName->id->symbol == env->implicitDistParam->symbol) {
            if (YTAG_TEST(call->method, name)) {
                YT(name) *callName = YC(name, call->method);
                if (callName->prefix == NULL &&
                        strcmp(callName->id->symbol->name, "forward") == 0) {
                    return(call->arguments->tail);
                }
            }
        }
    }
    return(NULL);
}

  static bool
hasDistributor(YT(identifier) *id, YT(distContext) *context)
{
    if (context) {
        YT(nameList) *names = context->names;
        YT(distContext) *lower = context->lowerContext;
        while (names) {
            if (names->name->id == id)
                return(TRUE);
            else
                names = names->next;
        }
        while (lower) {
            if (hasDistributor(id, lower))
                return(TRUE);
            else
                lower = lower->nextContext;
        }
    }
    return(FALSE);
}

  static bool
isDistributorType(YT(type) *type)
{
    if (YTAG_TEST(type, name)) {
        YT(name) *typeName = YC(name, type);
        if (typeName->prefix == NULL &&
                (strcmp(typeName->id->symbol->name, "EDistributor") == 0 ||
                 strcmp(typeName->id->symbol->name, "EResult") == 0))
            return(TRUE);
    }
    return(FALSE);
}

  static int
countDistributorParameters(YT(formalParameterList) *params)
{
    int result = 0;
    while (params) {
        if (isDistributorType(params->formalParameter->type))
            ++result;
        params = params->next;
    }
    return(result);
}

  static void
outputClassDecl(YT(typeDeclaration) *decl, YT(identifier) *id, t_e_env *env)
{
    char targetFileName[BUFLEN];
    bool saveOutputConfigured = env->outputConfigured;

    sprintf(targetFileName, "%s.java", id->symbol->name);
    if (!OutputToStdio)
        pushClassOutputFile(targetFileName);
    env->outputConfigured = TRUE;
    EWALKA(env->currentUnit->package);
    EWALKAL(env->currentUnit->imports);
    EWALKA(decl);
    emitWhitespace(env, env->currentUnit->finalWS);
    env->outputConfigured = saveOutputConfigured;
    if (!OutputToStdio)
        popOutput();
}

  static char *
pTag(int num)
{
    static char buf[20];
    if (num) {
        sprintf(buf, "%d", num);
        return(buf);
    } else {
        return("");
    }
}

/**
 * pushString -- Remember a string to be output after the next batch of
 *      whitespace but before the next output token.
 *
 * @param env  The current environment
 * @param string  The string to be output
 */
  static void
pushString(t_e_env *env, char *string)
{
    env->preTokenStrings = YBUILD(stringStack)(env->preTokenStrings, string);
}

/**
 * unboxFunc -- Given a primitive type, return the name of the corresponding
 *      unboxing method on its box class.
 *
 * @param prim  A Java primitive type, described by a primType struct
 * @returns  Unboxing function name, as a string
 */
  static char *
unboxFunc(YT(primType) *prim)
{
    int i;

    for (i=0; BoxTable[i].type > 0; ++i)
        if (BoxTable[i].type == prim->type)
            return(BoxTable[i].unboxFuncName);
    return("<???>");
}

/**
 * outputE -- Generated the output for an E compilation unit.
 *
 * @param unit  The compilation unit to be output
 * @param inputFileName  The name of the file this all came from
 * @param outputBaseDirName  Where the output files should go
 * @param outputHere  TRUE->all output goes into output directory,
 *      FALSE->output files located according to package hierarchy rooted in
 *      output directory
 */
  void
outputE(YT(compilationUnit) *unit, char *inputFileName,
        char *outputBaseDirName, bool outputHere)
{
    t_e_env env;

    env.currentType = NULL;
    env.currentUnit = NULL;
    env.distContext = NULL;
    env.doingSend = FALSE;
    env.exceptionEnv = 0;
    env.findImplicitDistributor = FALSE;
    env.haveThrown = FALSE;
    env.implicitDistParam = NULL;
    env.openExceptionContext = FALSE;
    env.openResultContext = FALSE;
    env.outputBaseDirName = outputBaseDirName;
    env.outputConfigured = !SplitFiles;
    env.outputHere = outputHere;
    env.preTokenStrings = NULL;
    env.suppressWhitespace = FALSE;

    YH_WALK(compilationUnit, unit, YA_FUNC(eWalker), &env);
}

/**
 * YA_FUNC(eWalker) -- All-purpose output generation YaccHelper tree-walker.
 */
YA_FUNC_START(eWalker, t_e_env)
{
    ECASE(arrayAccess,{
        EWALK(base);
        E_OPENBRACKET;
        EWALK(index);
        E_CLOSEBRACKET;
    });
    ECASE(arrayCreationExpression,{
        EKEY(new);
        EWALK(baseType);
        EWALKL(allocatedDimensions);
        EWALKL(unallocatedDimensions);
        EWALK(initializer);
    });
    ECASE(arrayInitializer,{
        E_OPENBRACE;
        EWALK(initializers);
        E_COMMA;
        E_CLOSEBRACE;
    });
    ECASE(arrayType,{
        EWALK(baseType);
        EWALKL(dimensions);
    });
    ECASE(binop,{
        if (XlateSend && arg->operator->op == Send) {
            if (YTAG_TEST(arg->rightOpnd, methodInvocation)) {
                YT(expression) *forwardValue =
                    extractForward(arg->leftOpnd,
                                   YC(methodInvocation, arg->rightOpnd),
                                   env);
                if (forwardValue) {
                    ES("\n        result = ");
                    EWALKA(forwardValue);
                } else {
                    int varNum = GenSymCounter++;
                    if (env->exceptionEnv) {
                        EP("\n        Promise promise%s = ", pTag(varNum));
                        PRESTRING("ERun.send(");
                    } else {
                        PRESTRING("ERun.sendOnly(");
                    }
                    EWALK(leftOpnd);
                    ES(",");
                    EW(operator->opWS);
                    PUSH_ENV(doingSend, TRUE, {
                        EWALK(rightOpnd);
                    });
                    if (env->exceptionEnv) {
                        EP(";\n        ERun.sendOnly(promise%s, ",
                           pTag(varNum));
                        EP("\"when\", catcher%s)", pTag(env->exceptionEnv));
                    }
                }
            } else {
                PRESTRING("ERun.deliver(");
                EWALK(leftOpnd);
                ES(",");
                EW(operator->opWS);
                EWALK(rightOpnd);
                ES(")");
            }
        } else {
            EWALK(leftOpnd);
            EWALK(operator);
            EWALK(rightOpnd);
        }
    });
    ECASE(block,{
        bool isExceptionContextBlock = env->openExceptionContext;
        E_OPENBRACE;
        if (isExceptionContextBlock) {
            ES("\n        Throwable problem = null;");
            env->openExceptionContext = FALSE;
        }
        if (env->openResultContext) {
            ES("\n        Object result = null;");
            env->openResultContext = FALSE;
        }
        PUSH_ENV(distContext, arg->distContext,{
            EWALKL(statements);
        });
        if (isExceptionContextBlock && env->haveThrown) {
            ES("\n        if (problem != null) {");
            ES("\n            throw problem;");
            ES("\n        }");
        }
        if (env->implicitDistParam) {
            ES("\n        return result;");
        }
        E_CLOSEBRACE;
    });
    ECASE(brackets,{
        E_OPENBRACKET;
        E_CLOSEBRACKET;
    });
    ECASE(breakStatement,{
        EKEY(break);
        EWALK(label);
        E_SEMICOLON;
    });
    ECASE(caseLabel,{
        EKEY(case);
        EWALK(value);
        E_COLON;
    });
    ECASE(castExpression,{
        E_OPENPAREN;
        EWALK(type);
        EWALKL(dimensions);
        E_CLOSEPAREN;
        EWALK(opnd);
    });
    ECASE(catch,{
        EKEY(catch);
        E_OPENPAREN;
        EWALK(param);
        E_CLOSEPAREN;
        EWALK(body);
    });
    ECASE(characterLiteral,{
        EFLD(character);
    });
    ECASE(classDeclaration,{
        if (env->outputConfigured) {
            EWALKL(modifiers);
            EKEY(class);
            EWALK(id);
            EWALK(extends);
            EWALK(implements);
            EWALK(body);
        } else {
            outputClassDecl(YC(typeDeclaration,arg), arg->id, env);
        }
    });
    ECASE(classBody,{
        E_OPENBRACE;
        EWALKL(fields);
        E_CLOSEBRACE;
    });
    ECASE(classBodyDeclaration,{
        THIS_CANT_HAPPEN;
    });
    ECASE(classSelection,{
        EWALK(base);
        E_DOT;
        EKEY(class);
    });
    ECASE(compilationUnit,{
        if (SplitFiles) {
            PUSH_ENV(currentUnit, arg, {
                if (prepareOutputDirectory(arg->package,
                                           env->outputBaseDirName,
                                           env->outputHere)) {
                    EWALKL(decls);
                }
            });
        } else {
            EWALK(package);
            EWALKL(imports);
            EWALKL(decls);
            EW(finalWS);
        }
    });
    ECASE(conditionalExpression,{
        EWALK(test);
        E_QMARK;
        EWALK(trueValue);
        E_COLON;
        EWALK(falseValue);
    });
    ECASE(constructorDeclaration,{
        EWALKL(modifiers);
        EWALK(declarator);
        EWALK(throws);
        EWALK(body);
    });
    ECASE(constructorBody,{
        E_OPENBRACE;
        EWALK(constructorInvocation);
        EWALKL(statements);
        E_CLOSEBRACE;
    });
    ECASE(constructorInvocation,{
        ETOKEN(which);
        E_OPENPAREN;
        EWALK(arguments);
        E_CLOSEPAREN;
    });
    ECASE(continueStatement,{
        EKEY(continue);
        EWALK(label);
        E_SEMICOLON;
    });
    ECASE(defaultLabel,{
        EKEY(default);
        E_COLON;
    });
    ECASE(dimension,{
        E_OPENBRACKET;
        EWALK(size);
        E_CLOSEBRACKET;
    });
    ECASE(distop,{
        if (XlateImplicitChannels) {
            EW(amperWS);
            EWALK(channel);
            ES("_dist");
        } else {
            E_AMPER;
            EWALK(channel);
        }
    });
    ECASE(doStatement,{
        EKEY(do);
        EWALK(body);
        EKEY(while);
        E_OPENPAREN;
        EWALK(condition);
        E_CLOSEPAREN;
        E_SEMICOLON;
    });
    ECASE(ecatch,{
        EKEY(catch);
        E_OPENPAREN;
        EWALK(param);
        E_CLOSEPAREN;
        EWALK(body);
    });
    ECASE(eclassDeclaration,{
        if (env->outputConfigured) {
            if (XlateEClass) {
                EWALKL(modifiers);
                EW(eclassWS);
                ES("class");
                EWALK(id);
                EWALK(extends);
                EWALK(implements);
                EWALK(body);
            } else {
                EWALKL(modifiers);
                EKEY(eclass);
                EWALK(id);
                EWALK(extends);
                EWALK(implements);
                EWALK(body);
            }
        } else {
            outputClassDecl(YC(typeDeclaration,arg), arg->id, env);
        }
    });
    ECASE(edebugStatement,{
        if (XlateEClass) {
            EW(edebugWS);
            if (!EDebug)
                ES("if (false)");
            EWALK(body);
        } else {
            EKEY(edebug);
            EWALK(body);
        }
    });
    ECASE(eifStatement,{
        if (XlateEWhen) {
            EW(eifWS);
            ES("ERun.SendOnly");
            E_OPENPAREN;
            EWALK(condition);
            ES(", \"when\", newObject() {");
            ES("\n        public void noticeKept(boolean test) {");
            ES("\n            if (test");
            E_CLOSEPAREN;
            EWALK(thenPart);
            if (arg->elsePart) {
                EKEY(else);
                EWALK(elsePart);
            }
            ES("\n        }");
            ES("\n    });");
            EWALKL(eorifs);
        } else {
            EKEY(eif);
            E_OPENPAREN;
            EWALK(condition);
            E_CLOSEPAREN;
            EWALK(thenPart);
            EWALKL(eorifs);
            if (arg->elsePart) {
                EKEY(else);
                EWALK(elsePart);
            }
        }
    });
    ECASE(einterfaceDeclaration,{
        if (env->outputConfigured) {
            if (XlateEClass) {
                EWALKL(modifiers);
                EW(einterfaceWS);
                ES("interface");
                EWALK(id);
                EWALK(extends);
                EWALK(body);
            } else {
                EWALKL(modifiers);
                EKEY(einterface);
                EWALK(id);
                EWALK(extends);
                EWALK(body);
            }
        } else {
            outputClassDecl(YC(typeDeclaration,arg), arg->id, env);
        }
    });
    ECASE(ekeepStatement,{
        if (XlateEWhen) {
            int varNum = GenSymCounter++;
            EW(ekeepWS);
            EP("        Object catcher%s = ", pTag(varNum));
            E_OPENPAREN;
            EWALK(expr);
            E_CLOSEPAREN;
            ES(";");
            PUSH_ENV(exceptionEnv, varNum,{
                EWALK(body);
            });
        } else {
            EKEY(ekeep);
            E_OPENPAREN;
            EWALK(expr);
            E_CLOSEPAREN;
            EWALK(body);
        }
    });
    ECASE(emethodDeclaration,{
        if (XlateEClass) {
            GenSymCounter = 0;
            PUSH_ENV2(findImplicitDistributor, FALSE,
                      implicitDistParam, NULL, {
                EWALK(header);
                PUSH_ENV3(openExceptionContext, TRUE,
                          openResultContext, !!env->implicitDistParam,
                          haveThrown, FALSE, {
                    EWALK(body);
                });
            });
        } else {
            EWALK(header);
            EWALK(body);
        }
    });
    ECASE(emethodHeader,{
        if (XlateEClass) {
            env->findImplicitDistributor =
                (countDistributorParameters(arg->declarator->formalParameters)
                 == 1);
            EWALKL(modifiers);
            EW(emethodWS);
            if (env->findImplicitDistributor) {
                ES("public Object");
            } else {
                ES("public void");
            }
            EWALK(declarator);
            ES(" throws Throwable");
            /* EWALK(throws); /* what about the whitespace? */
        } else {
            EWALKL(modifiers);
            EW(emethodWS);
            EWALK(declarator);
            EWALK(throws);
        }
    });
    ECASE(emethodStub,{
        EW(emethodWS);
        EWALK(header);
        E_SEMICOLON;
    });
    ECASE(eorifStatement,{
        EKEY(eorif);            /* deliberately trigger Java syntax error */
        E_OPENPAREN;
        EWALK(condition);
        E_CLOSEPAREN;
        EWALK(thenPart);
    });
    ECASE(eorwhenStatement,{
        EKEY(eorwhen);          /* deliberately trigger Java syntax error */
        EWALK(target);
        EW(openParenWS);
        EWALK(parameter);
        EW(closeParenWS);
        EWALK(body);
    });
    ECASE(ethrowStatement,{
        if (XlatePromises) {
            EW(ethrowWS);
            ES("problem = ");
            EWALK(exception);
            E_SEMICOLON;
            env->haveThrown = TRUE;
        } else {
            EKEY(ethrow);
            EWALK(exception);
            E_SEMICOLON;
        }
    });
    ECASE(etryStatement,{
        if (XlateEWhen) {
            int varNum = GenSymCounter++;
            EW(etryWS);
            EP("        Object catcher%s = new Object() {", pTag(varNum));
            ES("\n            public void noticeBroken(Throwable e) {");
            ES("\n                try {");
            ES("\n                    throw e;");
            ES("\n                }");
            EWALKL(ecatches);
            ES("\n            }");
            ES("\n        };");
            PUSH_ENV(exceptionEnv, varNum,{
                EWALK(body);
            });
        } else {
            EKEY(etry);
            EWALK(body);
            EWALKL(ecatches);
        }
    });
    ECASE(ewheneverStatement,{
        if (XlateEWhen) {
            EW(ewheneverWS);
            ES("ERun.sendOnly");
            E_OPENPAREN;
            EWALK(target);
            ES(", \"when\", new Object() {");
            ES("\n        public void noticeKept(");
            EWALK(parameter);
            E_CLOSEPAREN;
            EWALK(body);
            ES("\n    });");
        } else {
            EKEY(ewhenever);
            EWALK(target);
            E_OPENPAREN;
            EWALK(parameter);
            E_CLOSEPAREN;
            EWALK(body);
        }
    });
    ECASE(ewhenStatement,{
        if (XlateEWhen) {
            EW(ewhenWS);
            ES("ERun.sendOnly");
            E_OPENPAREN;
            EWALK(target);
            ES(", \"when\", new Object() {");
            ES("\n        public void noticeKept(");
            EWALK(parameter);
            E_CLOSEPAREN;
            EWALK(body);
            ES("\n    });");
            EWALKL(eorwhens);
        } else {
            EKEY(ewhen);
            EWALK(target);
            E_OPENPAREN;
            EWALK(parameter);
            E_CLOSEPAREN;
            EWALK(body);
            EWALKL(eorwhens);
        }
    });
    ECASE(expression,{
        THIS_CANT_HAPPEN;
    });
    ECASE(expressionSequence,{
        EWALK(head);
        E_COMMA;
        EWALK(tail);
    });
    ECASE(expressionStatement,{
        EWALK(expression);
        E_SEMICOLON;
    });
    ECASE(extends,{
        EKEY(extends);
        EWALK(extendTypeName);
    });
    ECASE(fieldDeclaration,{
        EWALKL(modifiers);
        EWALK(type);
        PUSH_ENV(currentType,arg->type,{
            EWALKL(variables);
        });
        E_SEMICOLON;
    });
    ECASE(finally,{
        EKEY(finally);
        EWALK(body);
    });
    ECASE(formalParameter,{
        if (XlatePromises && env->findImplicitDistributor &&
                isDistributorType(arg->type)) {
            env->implicitDistParam = arg->declarator->id;
        } else {
            E_COMMA;
            EWALK(type);
            EWALK(declarator);
        }
    });
    ECASE(forStatement,{
        EKEY(for);
        E_OPENPAREN;
        EWALK(init);
        EWS(semicolon1WS, ";");
        EWALK(test);
        EWS(semicolon2WS, ";");
        EWALK(incr);
        E_CLOSEPAREN;
        EWALK(body);
    });
    ECASE(identifier,{
        ESYM(symbol);
    });
    ECASE(ifStatement,{
        EKEY(if);
        E_OPENPAREN;
        EWALK(condition);
        E_CLOSEPAREN;
        EWALK(thenPart);
        if (arg->elsePart) {
            EKEY(else);
            EWALK(elsePart);
        }
    });
    ECASE(implements,{
        EKEY(implements);
        EWALK(implementTypes);
    });
    ECASE(importDeclaration,{
        EKEY(import);
        EWALK(importName);
        E_DOT;
        E_STAR;
        E_SEMICOLON;
    });
    ECASE(instanceCreationExpression,{
        EKEY(new);
        EWALK(id);
        E_OPENPAREN;
        EWALK(arguments);
        E_CLOSEPAREN;
        EWALK(immediateDef);
    });
    ECASE(interfaceDeclaration,{
        if (env->outputConfigured) {
            EWALKL(modifiers);
            EKEY(interface);
            EWALK(id);
            EWALK(extends);
            EWALK(body);
        } else {
            outputClassDecl(YC(typeDeclaration,arg), arg->id, env);
        }
    });
    ECASE(interfaceExtends,{
        EKEY(extends);
        EWALK(extendTypes);
    });
    ECASE(keywordLiteral,{
        if (XlateELiterals) {
            if (arg->value == ETRUE) {
                EWS(valueWS, "true");
            } else if (arg->value == EFALSE) {
                EWS(valueWS, "false");
            } else if (arg->value == ENULL) {
                EWS(valueWS, "null");
            } else {
                ETOKEN(value);
            }
        } else {
            ETOKEN(value);
        }
    });
    ECASE(labelledStatement,{
        EWALK(label);
        E_COLON;
        EWALK(statement);
    });
    ECASE(localVariableDeclaration,{
        EWALK(type);
        PUSH_ENV(currentType,arg->type,{
            EWALKL(variables);
        });
    });
    ECASE(methodDeclaration,{
        GenSymCounter = 0;
        PUSH_ENV2(findImplicitDistributor, FALSE,
                  implicitDistParam, NULL, {
            EWALK(header);
            EWALK(body);
        });
    });
    ECASE(methodDeclarator,{
        EWALK(id);
        E_OPENPAREN;
        EWALKL(formalParameters);
        E_CLOSEPAREN;
        EWALKL(brackets);
    });
    ECASE(methodHeader,{
        THIS_CANT_HAPPEN;
    });
    ECASE(methodHeaderTyped,{
        EWALKL(modifiers);
        EWALK(type);
        EWALK(declarator);
        EWALK(throws);
    });
    ECASE(methodHeaderVoid,{
        EWALKL(modifiers);
        EKEY(void);
        EWALK(declarator);
        EWALK(throws);
    });
    ECASE(methodInvocation,{
        if (env->doingSend) {
            PRESTRING("\"");
            EWALK(method);
            ES("\"");
            if (arg->arguments)
                ES(",");
            EW(openParenWS);
        } else {
            EWALK(method);
            E_OPENPAREN;
        }
        EWALK(arguments);
        E_CLOSEPAREN;
    });
    ECASE(methodStub,{
        EWALK(header);
        E_SEMICOLON;
    });
    ECASE(modifier,{
        ETOKEN(modifier);
    });
    ECASE(name,{
        EWALK(prefix);
        E_DOT;
        EWALK(id);
    });
    ECASE(nameSequence,{
        EWALK(head);
        E_COMMA;
        EWALK(tail);
    });
    ECASE(nullDeclaration,{
        E_SEMICOLON;
    });
    ECASE(nullStatement,{
        E_SEMICOLON;
    });
    ECASE(numberLiteral,{
        EFLD(number);
    });
    ECASE(operator,{
        ETOKEN(op);
    });
    ECASE(packageDeclaration,{
        EKEY(package);
        EWALK(packageName);
        E_SEMICOLON;
        if (XlateEClass) {
            ES("\nimport ec.ez.prim.ERun;");
            ES("\nimport ec.ez.prim.Promise;");
            ES("\nimport ec.ez.prim.Resolver;");
        }
    });
    ECASE(postop,{
        EWALK(opnd);
        EWALK(operator);
    });
    ECASE(primType,{
        ETOKEN(type);
    });
    ECASE(returnStatement,{
        if (env->haveThrown) {
            ES("\n        if (problem != null) {");
            ES("\n            throw problem;");
            ES("\n        } else {");
        }
        EKEY(return);
        if (arg->result) {
            EWALK(result);
        } else if (env->implicitDistParam) {
            ES(" result");
        }
        E_SEMICOLON;
        if (env->haveThrown) {
            ES("\n        }");
        }
    });
    ECASE(statement,{
        THIS_CANT_HAPPEN;
    });
    ECASE(staticInitializer,{
        EKEY(static);
        EWALK(block);
    });
    ECASE(stringLiteral,{
        EFLD(string);
    });
    ECASE(switchBlock,{
        E_OPENBRACE;
        EWALKL(switchGroups);
        E_CLOSEBRACE;
    });
    ECASE(switchGroup,{
        EWALKL(labels);
        EWALKL(statements);
    });
    ECASE(switchLabel,{
        THIS_CANT_HAPPEN;
    });
    ECASE(switchStatement,{
        EKEY(switch);
        E_OPENPAREN;
        EWALK(condition);
        E_CLOSEPAREN;
        EWALK(body);
    });
    ECASE(subexpression,{
        E_OPENPAREN;
        EWALK(value);
        E_CLOSEPAREN;
    });
    ECASE(synchronizedStatement,{
        EKEY(synchronized);
        E_OPENPAREN;
        EWALK(lock);
        E_CLOSEPAREN;
        EWALK(body);
    });
    ECASE(throws,{
        EKEY(throws);
        EWALK(throwTypes);
    });
    ECASE(throwStatement,{
        EKEY(throw);
        EWALK(exception);
        E_SEMICOLON;
    });
    ECASE(tryStatement,{
        EKEY(try);
        EWALK(body);
        EWALKL(catches);
        EWALK(finally);
    });
    ECASE(type,{
        THIS_CANT_HAPPEN;
    });
    ECASE(typeDeclaration,{
        THIS_CANT_HAPPEN;
    });
    ECASE(unop,{
        EWALK(operator);
        EWALK(opnd);
    });
    ECASE(variableDeclarationStatement,{
        EWALK(declaration);
        E_SEMICOLON;
    });
    ECASE(variableDeclarator,{
        E_COMMA;
        EWALK(id);
        EWALKL(brackets);
        if (arg->initializer) {
            E_EQUALS;
            EWALK(initializer);
        } else if (XlateImplicitChannels &&
                   hasDistributor(arg->id, env->distContext)) {
            PUSH_ENV(suppressWhitespace,TRUE,{
                ES("(");
                EWALKA(env->currentType);
                ES(") EUniChannel.construct(");
                EWALKA(env->currentType);
                ES(".class);");
                ES("\n        EUniDistributor ");
                EWALK(id);
                ES("_dist = EUniChannel.getDistributor(");
                EWALK(id);
                ES(")");
            });
        }
    });
    ECASE(variableInitializer,{
        THIS_CANT_HAPPEN;
    });
    ECASE(variableInitializers,{
        EWALK(head);
        E_COMMA;
        EWALK(tail);
    });
    ECASE(whileStatement,{
        EKEY(while);
        E_OPENPAREN;
        EWALK(condition);
        E_CLOSEPAREN;
        EWALK(body);
    });
}
YA_FUNC_END(eWalker)
