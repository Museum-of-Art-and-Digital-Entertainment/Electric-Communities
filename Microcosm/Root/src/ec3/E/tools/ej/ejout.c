/*
  ejout.c -- Output generation for the E-to-Java translator

  Chip Morningstar
  Electric Communities
  17-July-1997

  Copyright 1997 Electric Communities, all rights reserved worldwide.

*/

#include "generic.h"
#include "yh.h"
#include "yh_struct.h"
#include "yh_build.h"
#include "yh_walk.h"
#include "y.tab.h"
#include "ej.h"

static int GenSymCounter = 0;   /* For uniqifying symbols */

/* This is the big hairy environment struct which is carried around as we
   walk the parse tree generating output */
typedef struct {
    YT(class) *currentClass;    /* (e)class currently being generated        */
    YT(identifier) *currentClassID; /* Name of 'currentClass'                */
    YT(compilationUnit) *currentUnit; /* Compilation unit being processed    */
    YT(unitInfo) *unitInfo;     /* Info about 'currentUnit'                  */
    YT(type) *currentType;      /* Most recent variable declarator type      */
    YT(stringStack) *preTokenStrings; /* Text to follow whitespace           */
    bool asyncMethod;           /* Output next method name as "name$async"   */
    bool declaringEVariable;    /* Variable declarator is E-variable         */
    bool eConstructor;          /* Generating constructor for an eclass      */
    bool expandingInterface;    /* Generating (e)interface (not (e)class)    */
    bool haveDefaultConstructor;/* Don't synthesize default constructor      */
    bool onlyIfImplemented;     /* Ignore unimplemented methods in interface */
    bool outputHere;            /* Outputting to current working directory   */
    bool skippingFirst;         /* When ascending superclasses, ignore start */
    bool suppressWhitespace;    /* Don't output captured whitespace          */
    char *outputBaseDirName;    /* Where the output is rooted                */
    int closureNumber;          /* For eorwhens and eorifs                   */
    int emethodCount;           /* Counts the methods in a class             */
    int eorifNumber;            /* Counts the eorif clauses in an eif        */
    int paramCount;             /* Counts the elements of a parameter list   */
    t_classFormat classFormat;
    t_headerFormat headerFormat;
    t_methodFormat methodFormat;
    t_modifierFormat modifierFormat;
    t_nameFormat nameFormat;
    t_paramFormat paramFormat;
    t_variableFormat variableFormat;
} t_e_env;

static void outputClassDecl(YT(typeDeclaration) *decl, t_e_env *env,
    t_classFormat format);

YA_FUNC_DEF(eWalker);
YA_FUNC_DEF(upWalker);

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
#define EID(id, tag)    EWALK(id);                              \
                        ES(classSuffix(tag))
#define ECURCLASS(tag)  EWALKA(env->currentClassID);            \
                        ES(classSuffix(tag))

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
    return("<??\?>");
}

/**
 * classSuffix -- Return the string to append to an eclass name to make it
 *      fit a given name format.
 *
 * @param format  Name format of interest
 * @returns  Suffix string for that name format
 */
  static char *
classSuffix(t_nameFormat format)
{
    switch (format) {
        case NAME_INTF:      return("_$_Intf");
        case NAME_SEALER:    return("_$_Sealer");
        case NAME_DEFLECTOR: return("_$_Deflector");
        case NAME_PROXY:     return("_$_Proxy");
        case NAME_CHANNEL:   return("_$_Channel");
        case NAME_IMPL:      return("_$_Impl");
        case NAME_JAVA:
        case NAME_PLAIN:     return("");
        default:
            yh_error("<internal error> invalid classSuffix nameFormat %d",
                     format);
            return("");
    }
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
        LOCAL,          "local",
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
    return("<<??\?>>");
}

/**
 * generateEClasses -- Output all the various classes needed to expand an
 *      eclass or einterface.
 *
 * @param decl  The type declaration of the eclass or einterface
 * @param env  The current environment
 */
  static void
generateEClasses(YT(typeDeclaration) *decl, t_e_env *env)
{
    outputClassDecl(decl, env, CLASS_INTF);
    outputClassDecl(decl, env, CLASS_PLAIN);
    outputClassDecl(decl, env, CLASS_SEALER);
    outputClassDecl(decl, env, CLASS_DEFLECTOR);
    outputClassDecl(decl, env, CLASS_PROXY);
    outputClassDecl(decl, env, CLASS_CHANNEL);
    outputClassDecl(decl, env, CLASS_IMPL);
}

/**
 * generateJavaClass -- Output a vanilla Java class or interface.
 *
 * @param decl  The type declaration of the class or interface
 * @param env  The current environment
 */
  static void
generateJavaClass(YT(typeDeclaration) *decl, t_e_env *env)
{
    outputClassDecl(decl, env, CLASS_JAVA);
}

/**
 * outputClassDecl -- Generate the output for one of the many Java classes
 *      corresponding to a given input class, eclass, interface or einterface.
 *      All the logic for making sure that each generated class winds up in
 *      its own output file (with proper preamble) is here.
 *
 * @param decl  The type declaration for the (e)class being output
 * @param env  The current environment
 * @param format  Which variant to output
 */
  static void
outputClassDecl(YT(typeDeclaration) *decl, t_e_env *env, t_classFormat format)
{
    int saveClassFormat = env->classFormat;
    char *baseName;
    char targetFileName[BUFLEN];

    switch (YTAG_OF(decl)) {
        case YTAG(classDeclaration):
            baseName = YC(classDeclaration,decl)->id->symbol->name;
            break;
        case YTAG(eclassDeclaration):
            baseName = YC(eclassDeclaration,decl)->id->symbol->name;
            break;
        case YTAG(interfaceDeclaration):
            baseName = YC(interfaceDeclaration,decl)->id->symbol->name;
            break;
        case YTAG(einterfaceDeclaration):
            baseName = YC(einterfaceDeclaration,decl)->id->symbol->name;
            break;
        default:
            yh_error("<internal> invalid decl tag %d in outputClassDecl",
                     YTAG_OF(decl));
            baseName = "bad.bad";
            break;
    }
    sprintf(targetFileName, "%s%s.java", baseName, classSuffix(format));
    if (!OutputToStdio)
        pushClassOutputFile(targetFileName);
    env->classFormat = format;
    EWALKA(env->currentUnit->package);
    EWALKAL(env->currentUnit->imports);
    EWALKA(decl);
    emitWhitespace(env, env->currentUnit->finalWS);
    if (!OutputToStdio)
        popOutput();

    env->classFormat = saveClassFormat;
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
    return("<??\?>");
}

/**
 * walkSuperFields -- When output generation needs to go through the
 *      superclasses and superinterfaces of a class, it starts here.
 *
 * @param env  The current environment
 * @param theClass  The class whose inheritance hierarchy needs to be walked.
 */
  static void
walkSuperFields(t_e_env *env, YT(class) *theClass)
{
    if (theClass) {
        if (!theClass->declaration)
            internalizeClass(env->unitInfo, theClass);
        YH_WALK(typeDeclaration, theClass->declaration, YA_FUNC(upWalker),env);
    }
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

    env.asyncMethod = FALSE;
    env.eConstructor = FALSE;
    env.expandingInterface = FALSE;
    env.classFormat = CLASS_NONE;
    env.currentClass = NULL;
    env.currentClassID = NULL;
    env.currentType = NULL;
    env.currentUnit = NULL;
    env.declaringEVariable = FALSE;
    env.haveDefaultConstructor = FALSE;
    env.headerFormat = HEADER_NONE;
    env.methodFormat = METHOD_NONE;
    env.modifierFormat = MODIFIER_NORMAL;
    env.nameFormat = NAME_PLAIN;
    env.onlyIfImplemented = FALSE;
    env.outputBaseDirName = outputBaseDirName;
    env.outputHere = outputHere;
    env.paramFormat = PARAM_DECL;
    env.preTokenStrings = NULL;
    env.skippingFirst = FALSE;
    env.suppressWhitespace = FALSE;
    env.unitInfo = unit->unitInfo;
    env.variableFormat = VARIABLE_NONE;

    YH_WALK(compilationUnit, unit, YA_FUNC(eWalker), &env);

    if (Verbose)
        dumpClasses(env.unitInfo);
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
        PUSH_ENV(nameFormat, NAME_INTF,{
            EWALK(baseType);
        });
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
        if (arg->operator->op == Send) {
            if (YTAG_TEST(arg->rightOpnd, methodInvocation)) {
                EWALK(leftOpnd);
                EW(operator->opWS);
                ES(".");
                PUSH_ENV(asyncMethod, TRUE,{
                    EWALK(rightOpnd);
                });
            } else {
                EW(operator->opWS);
                PRESTRING("RtRun.enqueue(");
                EWALK(leftOpnd);
                ES(",");
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
        E_OPENBRACE;
        EWALKL(statements);
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
        switch (env->classFormat) {
            case CLASS_NONE:
                    generateJavaClass(YC(typeDeclaration,arg), env);
                    break;
            case CLASS_JAVA:
                PUSH_ENV2(currentClassID, arg->id,
                          currentClass, arg->theClass, {
                        EWALKL(modifiers);
                    EKEY(class);
                    EWALK(id);
                    EWALK(extends);
                    EWALK(implements);
                    EWALK(body);
                });
                break;
            default:
                yh_error("<internal error> invalid class classFormat %d",
                         env->classFormat);
        }
    });
    ECASE(classBody,{
        E_OPENBRACE;
        switch (env->classFormat) {
            case CLASS_JAVA:
                PUSH_ENV(variableFormat, VARIABLE_INIT,{
                    EWALKL(fields);
                });
                break;
            case CLASS_PLAIN:
                if (env->expandingInterface) {
                    PUSH_ENV2(methodFormat, METHOD_STUB_ASYNC,
                              variableFormat, VARIABLE_PLAIN,{
                        EWALKL(fields);
                    });
                } else {
                    PUSH_ENV3(methodFormat, METHOD_DONT_CALL,
                              variableFormat, VARIABLE_PLAIN,
                              haveDefaultConstructor,FALSE,{
                        EWALKL(fields);
                        if (!env->haveDefaultConstructor) {
                            ES("\n    public");
                            ECURCLASS(NAME_PLAIN);
                            ES("() {");
                            ES("\n        super();");
                            ES("\n        throw new RuntimeException(\"CompilerError: e2j ClassDef\");");
                            ES("\n    }");
                        }
                    });
                }
                break;
            case CLASS_INTF:
                PUSH_ENV(methodFormat, METHOD_STUB_ASYNC,{
                    EWALKL(fields);
                });
                break;
            case CLASS_IMPL:
                if (env->expandingInterface) {
                    PUSH_ENV(methodFormat, METHOD_STUB,{
                        EWALKL(fields);
                    });
                } else {
                    PUSH_ENV3(methodFormat, METHOD_IMPLEMENTATION,
                              variableFormat, VARIABLE_INIT,
                              haveDefaultConstructor,FALSE,{
                        EWALKL(fields);
                    });
                    PUSH_ENV(methodFormat, METHOD_ENQUEUE_METHOD,{
                        EWALKL(fields);
                    PUSH_ENV2(skippingFirst, TRUE,
                              onlyIfImplemented, TRUE,{
                            walkSuperFields(env, env->currentClass);
                        });
                    });
                }
                break;
            case CLASS_PROXY:
                PUSH_ENV(methodFormat, METHOD_ENQUEUE_METHOD,{
                    EWALKL(fields);
                    PUSH_ENV2(skippingFirst, TRUE,
                              onlyIfImplemented, !env->expandingInterface,{
                        walkSuperFields(env, env->currentClass);
                    });
                });
                break;
            case CLASS_CHANNEL:
                ES("\n    public ");
                ECURCLASS(NAME_CHANNEL);
                ES("(boolean distflag) {");
                ES("\n        super(distflag);");
                ES("\n    }");
                PUSH_ENV(methodFormat, METHOD_ENQUEUE_METHOD,{
                    EWALKL(fields);
                    PUSH_ENV2(skippingFirst, TRUE,
                              onlyIfImplemented, !env->expandingInterface,{
                        walkSuperFields(env, env->currentClass);
                    });
                });
                break;
            case CLASS_DEFLECTOR:
                ES("\n    public ");
                ECURCLASS(NAME_DEFLECTOR);
                ES("(RtTether target_$_, Object key_$_) {");
                ES("\n        super(target_$_, key_$_);");
                ES("\n    }");
                PUSH_ENV(methodFormat, METHOD_INVOCATION_METHOD,{
                    EWALKL(fields);
                    PUSH_ENV2(skippingFirst, TRUE,
                              onlyIfImplemented, !env->expandingInterface,{
                        walkSuperFields(env, env->currentClass);
                    });
                });
                break;
            case CLASS_SEALER:
                env->emethodCount = 0;
                PUSH_ENV(methodFormat, METHOD_SEALER_DECL,{
                    EWALKL(fields);
                });
                if (env->emethodCount > 0) {
                    ES("\n    private static final RtSealer[] the_$_Sealers = new ");
                    ECURCLASS(NAME_SEALER);
                    EP("[%d];", env->emethodCount);
                    ES("\n    static {");
                    env->emethodCount = 0;
                    PUSH_ENV(methodFormat, METHOD_SEALER_TABLE_ASSIGN,{
                        EWALKL(fields);
                    });
                    ES("\n    }");
                }
                ES("\n    public ");
                ECURCLASS(NAME_SEALER);
                ES("(int my_$_index, String name) {");
                ES("\n        super(my_$_index, name);");
                ES("\n    }");
                ES("\n    public void invoke(Object target_$_, Object[] args_$_) throws Exception {");
                if (env->emethodCount == 0) {
                    ES("\n        badSealer();");
                } else {
                    ES("\n        ");
                    ECURCLASS(NAME_IMPL);
                    ES(" realTarget = null;");
                    ES("\n        try {");
                    ES("\n            realTarget = (");
                    ECURCLASS(NAME_IMPL);
                    ES(")target_$_;");
                    ES("\n        } catch (RuntimeException f_$_) {");
                    ES("\n            badTarget(target_$_);");
                    ES("\n        }");
                    ES("\n        switch (my_$_Index) {");
                    env->emethodCount = 0;
                    PUSH_ENV(methodFormat, METHOD_SEALER_INVOKE_CASE,{
                        EWALKL(fields);
                    });
                    ES("\n        default:");
                    ES("\n            badSealer();");
                    ES("\n        }");
                }
                ES("\n    }");
                ES("\n    protected RtSealer otherSealer(int msg_$_) {");
                if (env->emethodCount == 0)
                    ES("\n        return null;");
                else
                    ES("\n        return the_$_Sealers[msg_$_];");
                ES("\n    }");
                break;
            default:
                yh_error("<internal error> invalid classBody classFormat %d",
                         env->classFormat);
        }
        E_CLOSEBRACE;
    });
    ECASE(classBodyDeclaration,{
        THIS_CANT_HAPPEN;
    });
    ECASE(compilationUnit,{
        PUSH_ENV(currentUnit, arg, {
            if (prepareOutputDirectory(arg->package, env->outputBaseDirName,
                                       env->outputHere)) {
                EWALKL(decls);
            }
        });
    });
    ECASE(conditionalExpression,{
        EWALK(test);
        E_QMARK;
        EWALK(trueValue);
        E_COLON;
        EWALK(falseValue);
    });
    ECASE(constructorDeclaration,{
        if (arg->declarator->formalParameters == NULL)
            env->haveDefaultConstructor = TRUE;
        switch (env->variableFormat) {
            case VARIABLE_INIT:
                EWALKL(modifiers);
                PUSH_ENV(eConstructor, env->currentClass->isEClass,{
                    EWALK(declarator);
                });
                EWALK(throws);
                EWALK(body);
                break;
            case VARIABLE_PLAIN:
                PUSH_ENV(headerFormat, HEADER_PLAIN,{
                    EWALKL(modifiers);
                    EWALK(declarator);
                    EWALK(throws);
                });
                ES(" {");
                ES("\n        super();");
                ES("\n        throw new RuntimeException(\"CompilerError: e2j ClassDef\");");
                ES("\n    }");
                break;
        }
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
        EW(amperWS);
        EWALK(channel);
        ES("_$_dist");
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
        PUSH_ENV2(currentClassID, arg->id,
                  currentClass, arg->theClass, {
            switch (env->classFormat) {
                case CLASS_NONE:
                    generateEClasses(YC(typeDeclaration,arg), env);
                    break;
                case CLASS_INTF:
                    ES("\npublic interface");
                    EID(id, NAME_INTF);
                    if (arg->extends) {
                        PUSH_ENV(nameFormat, NAME_INTF,{
                            EWALK(extends);
                        });
                    } else {
                        ES(" extends EObject_$_Intf");
                    }
                    if (arg->implements) {
                        ES(", ");
                        PUSH_ENV(nameFormat, NAME_INTF,{
                            EWALK(implements->implementTypes);
                        });
                    }
                    EWALK(body);
                    break;
                case CLASS_PLAIN:
                    ES("\npublic class");
                    EID(id, NAME_PLAIN);
                    if (arg->extends) {
                        PUSH_ENV(nameFormat, NAME_PLAIN,{
                            EWALK(extends);
                        });
                    } else {
                        ES(" extends EObject");
                    }
                    if (arg->implements) {
                        PUSH_ENV(nameFormat, NAME_PLAIN,{
                            EWALK(implements);
                        });
                        ES(",");
                    } else {
                        ES(" implements");
                    }
                    EID(id, NAME_INTF);
                    EWALK(body);
                    break;
                case CLASS_PROXY:
                    ES("\npublic class");
                    EID(id, NAME_PROXY);
                    if (arg->extends) {
                        PUSH_ENV(nameFormat, NAME_PROXY,{
                            EWALK(extends);
                        });
                    } else {
                        ES(" extends EObject_$_Proxy");
                    }
                    ES(" implements");
                    EID(id, NAME_INTF);
                    EWALK(body);
                    break;
                case CLASS_CHANNEL:
                    ES("\npublic class");
                    EID(id, NAME_CHANNEL);
                    if (arg->extends) {
                        PUSH_ENV(nameFormat, NAME_CHANNEL,{
                            EWALK(extends);
                        });
                    } else {
                        ES(" extends EObject_$_Channel");
                    }
                    ES(" implements");
                    EID(id, NAME_INTF);
                    EWALK(body);
                    break;
                case CLASS_DEFLECTOR:
                    ES("\npublic class");
                    EID(id, NAME_DEFLECTOR);
                    if (arg->extends) {
                        PUSH_ENV(nameFormat, NAME_DEFLECTOR,{
                            EWALK(extends);
                        });
                    } else {
                        ES(" extends EObject_$_Deflector");
                    }
                    ES(" implements");
                    EID(id, NAME_INTF);
                    EWALK(body);
                    break;
                case CLASS_IMPL:
                    PUSH_ENV(modifierFormat, MODIFIER_WHITESPACE,{
                        EWALKL(modifiers);
                    });
                    EW(eclassWS);
                    ES("public class");
                    EID(id, NAME_IMPL);
                    if (arg->extends) {
                        PUSH_ENV(nameFormat, NAME_IMPL,{
                            EWALK(extends);
                        });
                    } else {
                        ES(" extends EObject_$_Impl");
                    }
                    ES(" implements");
                    EID(id, NAME_INTF);
                    if (arg->implements) {
                        ES(", ");
                        PUSH_ENV(nameFormat, NAME_IMPL,{
                            EWALK(implements->implementTypes);
                        });
                    }
                    EWALK(body);
                    break;
                case CLASS_SEALER:
                    ES("\npublic class");
                    EID(id, NAME_SEALER);
                    ES(" extends RtSealer");
                    EWALK(body);
                    break;
                default:
                    yh_error("<internal error> invalid eclass classFormat %d",
                             env->classFormat);
            }
        });
    });
    ECASE(edebugStatement,{
        EW(edebugWS);
        if (!EDebug)
            ES("if (false)");
        EWALK(body);
    });
    ECASE(eifStatement,{
        PUSH_ENV2(closureNumber, GenSymCounter++,
                  eorifNumber, 1,{
            int eorifCount = YCOUNT(arg->eorifs);
            if (eorifCount)
                EP("\r        InternalEIfClosure eif_$_closure_%d;",
                   env->closureNumber);
            EW(eifWS);
            E_OPENPAREN;
            EWALK(condition);
            E_CLOSEPAREN;
            ES(".when$async(new EWhenClosure_$_Impl(");
            if (eorifCount)
                EP("eif_$_closure_%d = ", env->closureNumber);
            EP("new InternalEIfClosure(%d) {", eorifCount);
            ES("\r        public void doit(Object test_$_in) {");
            ES("\r            if (this.test(test_$_in)) {");
            EWALK(thenPart);
            ES("\r            }");
            ES("\r        }");
            ES("\r        public void doElse() {");
            if (arg->elsePart) {
                EW(elseWS);
                EWALK(elsePart);
            }
            ES("\r        }");
            ES("\r    }));");
            EWALKL(eorifs);
        });
    });
    ECASE(einterfaceDeclaration,{
        PUSH_ENV3(currentClassID, arg->id,
                  currentClass, arg->theClass,
                  expandingInterface, TRUE,{
            switch (env->classFormat) {
                case CLASS_NONE:
                    generateEClasses(YC(typeDeclaration,arg), env);
                    break;
                case CLASS_INTF:
                    ES("\npublic interface");
                    EID(id, NAME_INTF);
                    if (arg->extends)
                        EWALK(extends);
                    else
                        ES(" extends EObject_$_Intf");
                    EWALK(body);
                    break;
                case CLASS_PLAIN:
                    ES("\npublic interface");
                    EID(id, NAME_PLAIN);
                    if (arg->extends)
                        EWALK(extends);
                    else
                        ES(" extends EObject_$_Intf");
                    ES(",");
                    EID(id, NAME_INTF);
                    EWALK(body);
                    break;
                case CLASS_IMPL:
                    PUSH_ENV(modifierFormat, MODIFIER_WHITESPACE,{
                        EWALKL(modifiers);
                    });
                    EW(einterfaceWS);
                    ES("public interface");
                    EID(id, NAME_IMPL);
                    EWALK(extends);
                    EWALK(body);
                    break;
                case CLASS_PROXY:
                    ES("\npublic class");
                    EID(id, NAME_PROXY);
                    ES(" extends EProxy_$_Impl");
                    ES(" implements");
                    EID(id, NAME_INTF);
                    EWALK(body);
                    break;
                case CLASS_CHANNEL:
                    ES("\npublic class");
                    EID(id, NAME_CHANNEL);
                    ES(" extends EChannel_$_Impl");
                    ES(" implements");
                    EID(id, NAME_INTF);
                    EWALK(body);
                    break;
                case CLASS_DEFLECTOR:
                    ES("\npublic class");
                    EID(id, NAME_DEFLECTOR);
                    ES(" extends EObject_$_Deflector");
                    ES(" implements");
                    EID(id, NAME_INTF);
                    EWALK(body);
                    break;
                case CLASS_SEALER:
                    ES("\npublic class");
                    EID(id, NAME_SEALER);
                    ES(" extends RtSealer");
                    EWALK(body);
                    break;
                default:
                    yh_error("<internal error> invalid einterface classFormat %d",
                             env->classFormat);
            }
        });
    });
    ECASE(ekeepStatement,{
        EW(ekeepWS);
        ES("RtRun.pushExceptionEnv");
        E_OPENPAREN;
        EWALK(expr);
        E_CLOSEPAREN;
        ES(";");
        EWALK(body);
        ES("\r        RtRun.popExceptionEnv();");
    });
    ECASE(emethodDeclaration,{
        switch (env->methodFormat) {
            case METHOD_STUB_ASYNC:
                PUSH_ENV(headerFormat, HEADER_PLAIN_ASYNC,{
                    EWALK(header);
                });
                ES(";");
                break;
            case METHOD_DONT_CALL:
                PUSH_ENV(headerFormat, HEADER_PLAIN_ASYNC,{
                    EWALK(header);
                });
                ES(" {");
                ES("\n        throw new RuntimeException(\"CompilerError: e2j ClassDef\");");
                ES("\n    }");
                break;
            case METHOD_ENQUEUE_METHOD:
                if (isFirstEMethodDefinition(env->unitInfo, env->currentClass,
                                             arg->header) &&
                       !env->onlyIfImplemented) {
                    PUSH_ENV(headerFormat, HEADER_PLAIN_ASYNC,{
                        EWALK(header);
                    });
                    PUSH_ENV(headerFormat, HEADER_ENQUEUE_METHOD,{
                        EWALK(header);
                    });
                }
                break;
            case METHOD_INVOCATION_METHOD:
                if (isFirstEMethodDefinition(env->unitInfo, env->currentClass,
                                             arg->header) &&
                       !env->onlyIfImplemented) {
                    PUSH_ENV(headerFormat, HEADER_PLAIN_ASYNC,{
                        EWALK(header);
                    });
                    PUSH_ENV(headerFormat, HEADER_INVOCATION_METHOD,{
                        EWALK(header);
                    });
                }
                break;
            case METHOD_IMPLEMENTATION:
                PUSH_ENV(headerFormat, HEADER_PLAIN,{
                    EWALK(header);
                });
                EWALK(body);
                break;
            case METHOD_SEALER_DECL:
                if (isFirstEMethodDefinition(env->unitInfo, env->currentClass,
                                             arg->header)) {
                    PUSH_ENV(headerFormat, HEADER_SEALER_DECL,{
                        EWALK(header);
                    });
                }
                break;
            case METHOD_SEALER_TABLE_ASSIGN:
                if (isFirstEMethodDefinition(env->unitInfo, env->currentClass,
                                             arg->header)) {
                    PUSH_ENV(headerFormat, HEADER_SEALER_TABLE_ASSIGN,{
                        EWALK(header);
                    });
                }
                break;
            case METHOD_SEALER_INVOKE_CASE:
                if (isFirstEMethodDefinition(env->unitInfo, env->currentClass,
                                             arg->header)) {
                    PUSH_ENV(headerFormat, HEADER_SEALER_INVOKE_CASE,{
                        EWALK(header);
                    });
                }
                break;
            default:
                yh_error("<internal error> invalid emethodDeclaration methodFormat %d",
                         env->methodFormat);
                
        }
    });
    ECASE(emethodHeader,{
        switch (env->headerFormat) {
            case HEADER_PLAIN:
                EWALKL(modifiers);
                EW(emethodWS);
                ES("public void ");
                PUSH_ENV(asyncMethod, FALSE,{
                    EWALK(declarator);
                });
                EWALK(throws);
                break;
            case HEADER_PLAIN_ASYNC:
                PUSH_ENV(suppressWhitespace, !LegibleOutput,{
                    EWALKL(modifiers);
                    ES("\r    ");
                    ES("public void ");
                    PUSH_ENV(asyncMethod, TRUE,{
                        EWALK(declarator);
                    });
                    EWALK(throws);
                });
                break;
            case HEADER_ENQUEUE_METHOD:
                ES(" {\r");
                ES("        RtRun.enqueue(this, new RtEnvelope(");
                ECURCLASS(NAME_SEALER);
                PRESTRING(".sealer_$_");
                PUSH_ENV(paramFormat,PARAM_MANGLE,{
                    EWALK(declarator);
                });
                ES(", new Object[] {");
                PUSH_ENV(paramFormat,PARAM_CAPTURE,{
                    EWALKL(declarator->formalParameters);
                });
                ES("}));\r    }");
                break;
            case HEADER_INVOCATION_METHOD:
                ES(" {\n");
                ES("        target_$_.invoke(");
                ECURCLASS(NAME_SEALER);
                PRESTRING(".sealer_$_");
                PUSH_ENV(paramFormat,PARAM_MANGLE,{
                    EWALK(declarator);
                });
                ES(", new Object[] {");
                PUSH_ENV(paramFormat,PARAM_CAPTURE,{
                    EWALKL(declarator->formalParameters);
                });
                ES("});\n    }");
                break;
            case HEADER_SEALER_DECL:
                ES("\n    public static final ");
                ECURCLASS(NAME_SEALER);
                PRESTRING("sealer_$_");
                PUSH_ENV(paramFormat,PARAM_MANGLE,{
                    EWALK(declarator);
                });
                ES(" = new ");
                ECURCLASS(NAME_SEALER);
                EP("(%d, \"function", env->emethodCount);
                PUSH_ENV2(asyncMethod, TRUE,
                          paramFormat, PARAM_PROTO,{
                    EWALK(declarator);
                });
                ES("\");");
                break;
            case HEADER_SEALER_TABLE_ASSIGN:
                EP("\n        the_$_Sealers[%d] = ", env->emethodCount);
                PRESTRING("sealer_$_");
                PUSH_ENV(paramFormat,PARAM_MANGLE,{
                    EWALK(declarator);
                });
                ES(";");
                break;
            case HEADER_SEALER_INVOKE_CASE:
                EP("\n        case %d:", env->emethodCount);
                ES("\n            {");
                env->paramCount = 0;
                PUSH_ENV(paramFormat, PARAM_INVOKE_ARG_VAR,{
                    EWALKL(declarator->formalParameters);
                });
                ES("\n                try {");
                env->paramCount = 0;
                PUSH_ENV(paramFormat, PARAM_ASSIGN_ARG_VAR,{
                    EWALKL(declarator->formalParameters);
                });
                ES("\n                } catch (RuntimeException f_$_) {");
                ES("\n                    badArgs(target_$_, args_$_);");
                ES("\n                    return;");
                ES("\n                }");
                env->paramCount = 0;
                PUSH_ENV(paramFormat, PARAM_CALL_WITH_ARG_VAR,{
                    EWALK(declarator);
                });
                ES("\n            }");
                ES("\n            break;");
                break;
            default:
                yh_error("<internal error> invalid emethodHeader headerFormat %d",
                         env->headerFormat);
        }
        env->emethodCount++;
    });
    ECASE(emethodStub,{
        switch (env->methodFormat) {
            case METHOD_STUB_ASYNC:
                EW(emethodWS);
                PUSH_ENV(headerFormat, HEADER_PLAIN_ASYNC,{
                    EWALK(header);
                });
                E_SEMICOLON;
                break;
            case METHOD_STUB:
                EW(emethodWS);
                PUSH_ENV(headerFormat, HEADER_PLAIN,{
                    EWALK(header);
                });
                E_SEMICOLON;
                break;
            case METHOD_ENQUEUE_METHOD:
                EW(emethodWS);
                PUSH_ENV(headerFormat, HEADER_PLAIN_ASYNC,{
                    EWALK(header);
                });
                PUSH_ENV(headerFormat, HEADER_ENQUEUE_METHOD,{
                    EWALK(header);
                });
                break;
            case METHOD_INVOCATION_METHOD:
                EW(emethodWS);
                PUSH_ENV(headerFormat, HEADER_PLAIN_ASYNC,{
                    EWALK(header);
                });
                PUSH_ENV(headerFormat, HEADER_INVOCATION_METHOD,{
                    EWALK(header);
                });
                break;
            case METHOD_SEALER_DECL:
                PUSH_ENV(headerFormat, HEADER_SEALER_DECL,{
                    EWALK(header);
                });
                break;
            case METHOD_SEALER_TABLE_ASSIGN:
                PUSH_ENV(headerFormat, HEADER_SEALER_TABLE_ASSIGN,{
                    EWALK(header);
                });
                break;
            case METHOD_SEALER_INVOKE_CASE:
                PUSH_ENV(headerFormat, HEADER_SEALER_INVOKE_CASE,{
                    EWALK(header);
                });
                break;
            default:
                yh_error("<internal error> invalid emethodStub methodFormat %d",
                         env->methodFormat);
        }
    });
    ECASE(eorifStatement,{
        EW(eorifWS);
        E_OPENPAREN;
        EWALK(condition);
        E_CLOSEPAREN;
        EP(".when$async(new EWhenClosure_$_Impl(new InternalEOrIfClosure(ewhen_$_closure_%d, %d) {",
           env->closureNumber, env->eorifNumber++);
        ES("\r        public void doit(Object test_$_in) {");
        ES("\r            if (this.test(test_$_in)) {");
        EWALK(thenPart);
        ES("\r            }");
        ES("\r        }");
        ES("\r    }));");
    });
    ECASE(eorwhenStatement,{
        EW(eorwhenWS);
        EWALK(target);
        EP(".when$async(new EWhenClosure_$_Impl(new InternalEOrWhenClosure(ewhen_$_closure_%d) {",
           env->closureNumber);
        ES("\r        public void doit(Object value_$_in) {");
        ES("\r            if (this.use()) {");
        ES("\r                ");
        EW(openParenWS);
        PUSH_ENV(paramFormat, PARAM_DECL,{
            EWALK(parameter);
        });
        ES(" = ");
        PUSH_ENV(paramFormat, PARAM_WHEN_VAR,{
            EWALK(parameter);
        });
        EW(closeParenWS);
        EWALK(body);
        ES("\r            }");
        ES("\r        }");
        ES("\r    }));");
    });
    ECASE(ethrowStatement,{
        EW(ethrowWS);
        ES("RtRun.exceptionEnv().doEThrow(");
        EWALK(exception);
        ES(")");
        E_SEMICOLON;
    });
    ECASE(etryStatement,{
        int hackVarNum = GenSymCounter++;
        EW(etryWS);
        EP("for (int i_$_%d=0; i_$_%d<2; ++i_$_%d) {", hackVarNum, hackVarNum,
           hackVarNum);
        EP("\r            if (i_$_%d==1) {", hackVarNum);
        EWALK(body);
        ES("\r                RtRun.popExceptionEnv();");
        ES("\r            } else {");
        ES("\r                RtRun.pushExceptionEnv(new InternalECatchClosure() {");
        ES("\r                    public void catchMe(Throwable e) {");
        ES("\r                        try {");
        ES("\r                            throw e;");
        ES("\r                        }");
        EWALKL(ecatches);
        ES("\r                    }");
        ES("\r                });");
        ES("\r            }");
        ES("\r        }");
    });
    ECASE(ewheneverStatement,{
        EW(ewheneverWS);
        EWALK(target);
        ES(".when$async(new EWhenClosure_$_Impl(new InternalEWhenClosure() {");
        ES("\r        public void doit(Object value_$_in) {");
        ES("\r            ");
        EW(openParenWS);
        PUSH_ENV(paramFormat, PARAM_DECL,{
            EWALK(parameter);
        });
        ES(" = ");
        PUSH_ENV(paramFormat, PARAM_WHEN_VAR,{
            EWALK(parameter);
        });
        EW(closeParenWS);
        EWALK(body);
        ES("\r        }");
        ES("\r    }));");
    });
    ECASE(ewhenStatement,{
        PUSH_ENV(closureNumber, GenSymCounter++,{
            if (arg->eorwhens)
                EP("\r        InternalEWhenClosure ewhen_$_closure_%d;",
                   env->closureNumber);
            EW(ewhenWS);
            EWALK(target);
            ES(".when$async(new EWhenClosure_$_Impl(");
            if (arg->eorwhens)
                EP("ewhen_$_closure_%d = ", env->closureNumber);
            ES("new InternalEWhenClosure() {");
            ES("\r        public void doit(Object value_$_in) {");
            ES("\r            if (this.use()) {");
            ES("\r                ");
            EW(openParenWS);
            PUSH_ENV(paramFormat, PARAM_DECL,{
                EWALK(parameter);
            });
            ES(" = ");
            PUSH_ENV(paramFormat, PARAM_WHEN_VAR,{
                EWALK(parameter);
            });
            EW(closeParenWS);
            EWALK(body);
            ES("\r            }");
            ES("\r        }");
            ES("\r    }));");
            EWALKL(eorwhens);
        });
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
        if (env->variableFormat != VARIABLE_NONE) {
            int localNameFormat;
            EWALKL(modifiers);
            if (env->variableFormat == VARIABLE_PLAIN)
                localNameFormat = NAME_PLAIN;
            else
                localNameFormat = NAME_INTF;
            PUSH_ENV(nameFormat, localNameFormat,{
                EWALK(type);
            });
            PUSH_ENV2(currentType,arg->type,
                     declaringEVariable,isEClassType(env->unitInfo,arg->type),{
                EWALKL(variables);
            });
            E_SEMICOLON;
        }
    });
    ECASE(finally,{
        EKEY(finally);
        EWALK(body);
    });
    ECASE(formalParameter,{
        switch (env->paramFormat) {
            case PARAM_MANGLE:
                ES("$");
                EWALK(type);
                break;
            case PARAM_CAPTURE:
                E_COMMA;
                ES("(Object)");
                if (YTAG_TEST(arg->type, primType)) {
                    EP("(new %s(", boxType(YC(primType,arg->type)));
                    EWALK(declarator);
                    ES("))");
                } else {
                    EWALK(declarator);
                }
                break;
            case PARAM_DECL:
                E_COMMA;
                PUSH_ENV(nameFormat, NAME_INTF,{
                    EWALK(type);
                });
                EWALK(declarator);
                break;
            case PARAM_PROTO:
                E_COMMA;
                PUSH_ENV(nameFormat, NAME_PLAIN,{
                    EWALK(type);
                });
                break;
            case PARAM_INVOKE_ARG_VAR:
                ES("\n                ");
                PUSH_ENV(nameFormat, NAME_INTF,{
                    EWALK(type);
                });
                EP(" args_$_%d;", env->paramCount);
                break;
            case PARAM_ASSIGN_ARG_VAR:
                EP("\n                    args_$_%d = (", env->paramCount);
                if (YTAG_TEST(arg->type, primType)) {
                    YT(primType) *prim = YC(primType,arg->type);
                    EP("(%s)args_$_[%d]).%s();", boxType(prim),
                       env->paramCount, unboxFunc(prim));
                } else {
                    PUSH_ENV(nameFormat, NAME_INTF,{
                        EWALK(type);
                    });
                    EP(")args_$_[%d];", env->paramCount);
                }
                break;
            case PARAM_WHEN_VAR:
                if (YTAG_TEST(arg->type, primType)) {
                    YT(primType) *prim = YC(primType,arg->type);
                    EP("((%s) value_$_in).%s();", boxType(prim),
                       unboxFunc(prim));
                } else {
                    ES("(");
                    PUSH_ENV(nameFormat, NAME_INTF,{
                        EWALK(type);
                    });
                    ES(") value_$_in;");
                }
                break;
            case PARAM_CALL_WITH_ARG_VAR:
                E_COMMA;
                EP("args_$_%d", env->paramCount);
                break;
            default:
                yh_error("<internal error> invalid formalParameter paramFormat %d",
                         env->paramFormat);
        }
        env->paramCount++;
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
        PUSH_ENV(suppressWhitespace, TRUE, {
            if (!arg->starWS && isEClass(env->unitInfo, arg->importName)) {
                ES("\rimport "); EWALK(importName); ES("_$_Intf;");
                ES("\rimport "); EWALK(importName); ES("_$_Sealer;");
                ES("\rimport "); EWALK(importName); ES("_$_Channel;");
                ES("\rimport "); EWALK(importName); ES("_$_Impl;");
                ES("\rimport "); EWALK(importName); ES("_$_Proxy;");
                ES("\rimport "); EWALK(importName); ES("_$_Deflector;");
            }
        });
    });
    ECASE(instanceCreationExpression,{
        EKEY(new);
        PUSH_ENV(nameFormat, NAME_IMPL,{
            EWALK(id);
        });
        E_OPENPAREN;
        EWALK(arguments);
        E_CLOSEPAREN;
        EWALK(immediateDef);
    });
    ECASE(interfaceDeclaration,{
        switch (env->classFormat) {
            case CLASS_NONE:
                    generateJavaClass(YC(typeDeclaration,arg), env);
                    break;
            case CLASS_JAVA:
                PUSH_ENV2(currentClassID, arg->id,
                          currentClass, arg->theClass, {
                    EWALKL(modifiers);
                    EKEY(interface);
                    EWALK(id);
                    EWALK(extends);
                    EWALK(body);
                });
                break;
            default:
                yh_error("<internal error> invalid interface classFormat %d",
                         env->classFormat);
        }
    });
    ECASE(interfaceExtends,{
        EKEY(extends);
        PUSH_ENV(nameFormat, env->classFormat,{
            EWALK(extendTypes);
        });
    });
    ECASE(keywordLiteral,{
        if (arg->value == ETRUE) {
            EWS(valueWS, "ETrue_$_Impl.trueValue");
        } else if (arg->value == EFALSE) {
            EWS(valueWS, "EFalse_$_Impl.falseValue");
        } else if (arg->value == ENULL) {
            EWS(valueWS, "ENull_$_Impl.nullValue");
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
        PUSH_ENV(nameFormat, NAME_INTF,{
            EWALK(type);
        });
        PUSH_ENV2(currentType,arg->type,
                  declaringEVariable,isEClassType(env->unitInfo,arg->type),{
            EWALKL(variables);
        });
    });
    ECASE(methodDeclaration,{
        switch (env->variableFormat) {
            case VARIABLE_INIT:
                EWALK(header);
                EWALK(body);
                break;
            case VARIABLE_PLAIN:
                PUSH_ENV(headerFormat, HEADER_PLAIN,{
                    EWALK(header);
                });
                ES(" {");
                ES("\n        throw new RuntimeException(\"CompilerError: e2j ClassDef\");");
                ES("\n    }");
                break;
        }
    });
    ECASE(methodDeclarator,{
        if (env->paramFormat == PARAM_CALL_WITH_ARG_VAR) {
            ES("\n                realTarget.");
        }
        EWALK(id);
        if (env->asyncMethod) {
            ES("$async");
            env->asyncMethod = FALSE;
        } else if (env->eConstructor) {
            ES("_$_Impl");
            env->eConstructor = FALSE;
        }
        if (env->paramFormat == PARAM_MANGLE) {
            if (arg->formalParameters) {
                PUSH_ENV(suppressWhitespace,TRUE,{
                    EWALKL(formalParameters);
                });
            } else {
                ES("$");
            }
        } else {
            E_OPENPAREN;
            EWALKL(formalParameters);
            E_CLOSEPAREN;
            EWALKL(brackets);
        }
        if (env->paramFormat == PARAM_CALL_WITH_ARG_VAR) {
            ES(";");
        }
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
        PUSH_ENV(paramFormat, PARAM_CALL,{
            EWALK(method);
        });
        if (env->asyncMethod) {
            PRESTRING("$async");
            env->asyncMethod = FALSE;
        }
        E_OPENPAREN;
        EWALK(arguments);
        E_CLOSEPAREN;
    });
    ECASE(methodStub,{
        EWALK(header);
        E_SEMICOLON;
    });
    ECASE(modifier,{
        if (env->modifierFormat == MODIFIER_WHITESPACE) {
            EW(modifierWS);
        } else {
            ETOKEN(modifier);
        }
    });
    ECASE(name,{
        if (env->paramFormat == PARAM_MANGLE) {
            char mangleString[BUFLEN];
            convertNameToMangle(getFQN(env->unitInfo, arg), mangleString);
            ES(mangleString);
        } else if (env->paramFormat == PARAM_PROTO) {
            char fqnString[BUFLEN];
            convertNameToString(getFQN(env->unitInfo, arg), fqnString);
            ES(fqnString);
        } else {
            int localNameFormat;
            if (env->paramFormat == PARAM_CALL)
                localNameFormat = NAME_IMPL;
            else
                localNameFormat = NAME_PLAIN;
            PUSH_ENV(nameFormat, localNameFormat,{
                EWALK(prefix);
            });
            E_DOT;
            EWALK(id);
            if (env->nameFormat != NAME_PLAIN &&
                    isEClass(env->unitInfo, arg))
                ES(classSuffix(env->nameFormat));
        }
    });
    ECASE(nameSequence,{
        EWALK(head);
        E_COMMA;
        EWALK(tail);
    });
    ECASE(nullDeclaration,{
        if (env->classFormat != CLASS_NONE) {
            E_SEMICOLON;
        }
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
        ES("\rimport ec.e.run.*;");
        ES("\rimport ec.e.lang.*;");
    });
    ECASE(postop,{
        EWALK(opnd);
        EWALK(operator);
    });
    ECASE(primType,{
        ETOKEN(type);
    });
    ECASE(returnStatement,{
        EKEY(return);
        EWALK(result);
        E_SEMICOLON;
    });
    ECASE(statement,{
        THIS_CANT_HAPPEN;
    });
    ECASE(staticInitializer,{
        if (env->variableFormat == VARIABLE_INIT) {
            EKEY(static);
            EWALK(block);
        }
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
        if (env->variableFormat == VARIABLE_INIT) {
            if (arg->initializer) {
                E_EQUALS;
                EWALK(initializer);
            } else if (env->declaringEVariable) {
                PUSH_ENV(suppressWhitespace,TRUE,{
                    ES(" = (");
                    PUSH_ENV(nameFormat, NAME_INTF,{
                        EWALKA(env->currentType);
                    });
                    ES(") new ");
                    PUSH_ENV(nameFormat, NAME_CHANNEL,{
                        EWALKA(env->currentType);
                    });
                    ES("(false);");
                    ES("\r        EDistributor_$_Intf ");
                    EWALK(id);
                    ES("_$_dist = ((EChannel_$_Impl)");
                    EWALK(id);
                    ES(").distributor()");
                });
            }
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

#define UPWALKA(what)  \
    YH_WALK(statement, YC(statement,what), YA_FUNC(upWalker), env)
#define UPWALKAL(what) \
    YH_WALK(statementList, YC(statementList,what), YA_FUNC(upWalker), env)
#define UPWALK(what)    UPWALKA(arg->what)
#define UPWALKL(what)   UPWALKAL(arg->what)

/**
 * YA_FUNC(upWalker) -- YaccHelper tree walker which traverses up the
 *      superclass/interface hierarchy instead of actually walking the parse
 *      tree.
 */
YA_FUNC_START(upWalker, t_e_env)
{
    ECASE(classBody,{
        if (env->skippingFirst)
            env->skippingFirst = FALSE;
        else
            EWALKL(fields);
    });
    ECASE(eclassDeclaration,{
        PUSH_ENV2(currentClassID, arg->id,
                  currentClass, arg->theClass, {
            UPWALK(body);
            UPWALK(extends);
            UPWALK(implements);
        });
    });
    ECASE(einterfaceDeclaration,{
        PUSH_ENV3(currentClassID, arg->id,
                  currentClass, arg->theClass,
                  expandingInterface, TRUE,{
            UPWALK(body);
            UPWALK(extends);
        });
    });
    ECASE(extends,{
        walkSuperFields(env, findClass(env->unitInfo, arg->extendTypeName));
    });
    ECASE(implements,{
        UPWALK(implementTypes);
    });
    ECASE(interfaceExtends,{
        UPWALK(extendTypes);
    });
    ECASE(nameSequence,{
        UPWALK(head);
        walkSuperFields(env, findClass(env->unitInfo, arg->tail));
    });
}
YA_FUNC_END(upWalker)
