/*
  pllex.c -- Lexer for Pluribus.

  Chip Morningstar
  Electric Communities
  1-August-1996

  Copyright 1996 Electric Communities, all rights reserved.

*/

#include "generic.h"
#include "yh.h"
#include "y.tab.h"
#include "pl.h"
#include <string.h>

extern YYSTYPE yylval;

static int lexCodeBlock(char c);
static int lexHexBlock(char c);
static int lexInitialization(char c);

bool ExpectCodeBlock = FALSE;
bool ExpectHexBlock = FALSE;
bool ExpectInitialization = FALSE;

t_yh_keywordTable keywordTable[] = {
    "Impl",                     IMPL,
    "Kind",                     KIND,
    "String",                   STRING,
    "Structure",                STRUCTURE,
    "abstract",                 ABSTRACT,
    "attribute",                ATTRIBUTE,
    "boolean",                  BOOLEAN,
    "byte",                     BYTE,
    "case",                     CASE,
    "char",                     CHAR,
    "class",                    CLASS,
    "data",                     DATA,
    "default",                  DEFAULT,
    "deliver",                  DELIVER,
    "double",                   DOUBLE,
    "eclass",                   ECLASS,
    "einterface",               EINTERFACE,
    "elevate",                  ELEVATE,
    "enum",                     ENUM,
    "export",                   EXPORT,
    "extends",                  EXTENDS,
    "facet",                    FACET,
    "false",                    FALSEX,
    "fill",                     FILL,
    "final",                    FINAL,
    "float",                    FLOAT,
    "function",                 FUNCTION,
    "impl",                     IMPL,
    "implements",               IMPLEMENTS,
    "import",                   IMPORT,
    "ingredient",               INGREDIENT,
    "init",                     INIT,
    "int",                      INT,
    "interface",                INTERFACE,
    "kind",                     KIND,
    "long",                     LONG,
    "make",                     MAKE,
    "makes",                    MAKES,
    "map",                      MAP,
    "method",                   METHOD,
    "neighbor",                 NEIGHBOR,
    "none",                     NONE,
    "null",                     PNULL,
    "package",                  PACKAGE,
    "presence",                 PRESENCE,
    "presenceBehavior",         PRESENCEBEHAVIOR,
    "presencebehavior",         PRESENCEBEHAVIOR,
    "prime",                    PRIME,
    "private",                  PRIVATE,
    "protected",                PROTECTED,
    "public",                   PUBLIC,
    "publish",                  PUBLISH,
    "remote",                   REMOTE,
    "require",                  REQUIRE,
    "role",                     ROLE,
    "sequence",                 SEQUENCE,
    "short",                    SHORT,
    "state",                    STATE,
    "static",                   STATIC,
    "string",                   STRING,
    "struct",                   STRUCT,
    "structure",                STRUCTURE,
    "switch",                   SWITCH,
    "template",                 TEMPLATE,
    "throws",                   THROWS,
    "to",                       TO,
    "true",                     TRUEX,
    "typedef",                  TYPEDEF,
    "union",                    UNION,
    "unit",                     UNIT,
    "unum",                     UNUM,
    NULL,                       0,
};

#define BIG_BUF_LEN             1000000
#define BUF_MARGIN                  3
#define LAST_CHAR               bufptr[-1]
#define NEXT_TO_LAST_CHAR       bufptr[-2]

#define NEXT_CODE_CHAR                                          \
    *bufptr++ = yh_getc();                                      \
    if (LAST_CHAR == EOF) {                                     \
        yh_error("end-of-file inside code block\n");            \
        return(0);                                              \
    } else if (bufptr - buf > BIG_BUF_LEN - BUF_MARGIN) {       \
        yh_error("code block too long");                        \
        return(0);                                              \
    } else if (LAST_CHAR == '\n') {                             \
        ++yh_LineNumber;                                        \
    }

/*
  lexCodeBlock -- Lexical analysis routine to capture embedded E or other
                  code.  The only language assumption is that code contains
                  balanced braces and uses C/C++/Java lexical conventions for
                  strings, character constants, and comments.
*/
  static int
lexCodeBlock(char c)
{
    char buf[BIG_BUF_LEN];
    char *bufptr = buf;
    int  bracketNesting = 1;

    *bufptr++ = '{';
    *bufptr++ = c;
    if (c == '\n')
        ++yh_LineNumber;
    while (1) {
        if (LAST_CHAR == '{' || LAST_CHAR == '[' || LAST_CHAR == '(')
            ++bracketNesting;
        else if (LAST_CHAR == '}' || LAST_CHAR == ']' || LAST_CHAR == ')')
            --bracketNesting;
        if (bracketNesting == 0) {
            *bufptr++ = '\0';
            yylval = (YYSTYPE)STRDUP(buf);
            return(CodeBlock);
        }
        if (LAST_CHAR == '"' || LAST_CHAR == '\'') {
            char start = LAST_CHAR;
            do {
                NEXT_CODE_CHAR;
            } while (LAST_CHAR != start || NEXT_TO_LAST_CHAR == '\\');
        } else if (LAST_CHAR == '/') {
            NEXT_CODE_CHAR;
            if (LAST_CHAR == '/') {
                do {
                    NEXT_CODE_CHAR;
                } while (LAST_CHAR != '\n');
            } else if (LAST_CHAR == '*') {
                do {
                    NEXT_CODE_CHAR;
                } while (LAST_CHAR != '/' || NEXT_TO_LAST_CHAR != '*');
            }
        }
        NEXT_CODE_CHAR;
    }
}


#define NEXT_HEX_CHAR                                           \
    c = yh_getc();                                              \
    if (c == EOF) {                                             \
        yh_error("end-of-file inside hex data block\n");        \
        return(0);                                              \
    } else if (c == '\n') {                                     \
        ++yh_LineNumber;                                        \
    }

/*
  lexHexBlock -- Lexical analysis routine to scan a block of raw hex data.
                 The data is allowed to contain whitespace and comments, and
                 is assumed to be terminated by a close brace ("}").
*/
  static int
lexHexBlock(char c)
{
    byte buf[BIG_BUF_LEN];
    byte *bufptr = buf;
    longword *result;
    int hiNybble;
    int nybble;
    int phase = 0;

    while (1) {
        if ('a' <= c && c <= 'f')
            nybble = c - 'a' + 10;
        else if ('A' <= c && c <= 'F')
            nybble = c - 'A' + 10;
        else if ('0' <= c && c <= '9')
            nybble = c - '0';
        else
            nybble = -1;
        if (nybble >= 0) {
            if (phase == 0) {
                hiNybble = nybble * 16;
                phase = 1;
            } else {
                if (bufptr - buf > BIG_BUF_LEN - BUF_MARGIN) {
                    yh_error("hex data block too long\n");
                    return(0);
                }
                *bufptr++ = hiNybble + nybble;
                hiNybble = 0;
                phase = 0;
            }
        } else {
            if (c == '/') {
                NEXT_HEX_CHAR;
                if (c == '/') {
                    do {
                        NEXT_HEX_CHAR;
                    } while (c != '\n');
                } else if (c == '*') {
                    char prev;
                    c = 'x'; /* can be anything that's not a '*' */
                    do {
                        prev = c;
                        NEXT_HEX_CHAR;
                    } while (c != '/' || prev != '*');
                }
            } else if (c == '\n' || c == ' ' || c == '\t') {
                NEXT_HEX_CHAR;
            } else {
                break;
            }
        }
    }
    if (phase != 0) {
        yh_error("hex data block contains odd number of nybbles\n");
        return(0);
    }
    result = TypeAllocSized(longword, bufptr - buf + sizeof(longword));
    *result = bufptr - buf;
    memcpy((char *)(result + 1), buf, bufptr - buf);
    yylval = (YYSTYPE)result;
    return(HexBlock);
}

#define NEXT_INIT_CHAR                                            \
    c = yh_getc();                                                \
    if (c == EOF) {                                               \
        yh_error("end-of-file inside variable initialization\n"); \
        return(0);                                                \
    } else if (bufptr - buf > BIG_BUF_LEN - BUF_MARGIN) {         \
        yh_error("variable initialization too long");             \
        return(0);                                                \
    } else if (c == '\n') {                                       \
        ++yh_LineNumber;                                          \
    }


/*
  lexInitialization -- Lexical analysis routine to scan an initialization
                 string for a variable declaration.  The data is allowed to
                 contain whitespace and strings, and is assumed to be
                 terminated by a semicolon (";").
*/
  static int
lexInitialization(char c)
{
    bool inString = FALSE;
    bool inChar = FALSE;
    byte buf[BIG_BUF_LEN];
    byte *bufptr = buf;

    while (1) {
        if ('"' == c) {
            inString = !inString;
            *bufptr++ = c;
        } else if ('\'' == c) {
            inChar = !inChar;
            *bufptr++ = c;
        } else if (';' == c) {
            if (inString || inChar) {
                *bufptr++ = c;
            } else {
                yh_ungetc(c);
                *bufptr++ = '\0';
                yylval = (YYSTYPE)STRDUP(buf);
                return(Initialization);
            }
        } else {
            *bufptr++ = c;
        }
        NEXT_INIT_CHAR;
    }
}

  static int
lexMisc(char c)
{
    static struct {
        char *string;
        int   token;
    } Tokens[] = {
        "!",        '!',
        "!=",       Neq,
        "%",        '%',
        "&",        '&',
        "&&",       And,
        "(",        '(',
        ")",        ')',
        "*",        '*',
        "+",        '+',
        ",",        ',',
        "-",        '-',
        ".",        '.',
        "/",        '/',
        ":",        ':',
        ";",        ';',
        "<",        '<',
        "<<",       Lsl,
        "<=",       Leq,
        "=",        '=',
        "==",       Eq,
        ">",        '>',
        ">=",       Geq,
        ">>",       Lsr,
        ">>>",      Asr,
        "[",        '[',
        "]",        ']',
        "^",        '^',
        "{",        '{',
        "|",        '|',
        "||",       Or,
        "}",        '}',
        "~",        '~'
    };
    int limit = sizeof(Tokens) / sizeof(Tokens[0]);
    int base = 0;
    int pos = 0;
    int lo;
    int hi;

    while (TRUE) {
        for (lo = base; lo < limit && c != Tokens[lo].string[pos]; ++lo)
            ;
        if (lo == limit) {
            yh_ungetc(c);
            return(Tokens[base].token);
        }
        for (hi = lo; hi < limit && c == Tokens[hi].string[pos]; ++hi)
            ;
        if (hi == lo + 1) {
            return(Tokens[lo].token);
        }
        limit = hi;
        base = lo;
        ++pos;
        c = yh_getc();
    }
}

  static bool
plIsSymbolChar(char c)
{
    return(yh_isCSymbolChar(c) || c == '$');
}

/*
  yylex -- YACC standard lexer, mostly uses YaccHelper routines.
*/
  int
yylex(void)
{
    char c;
    bool internalExpectCodeBlock;
    bool internalExpectHexBlock;
    bool internalExpectInitialization;

    yh_isSymbolChar = plIsSymbolChar;
    internalExpectCodeBlock = ExpectCodeBlock;
    ExpectCodeBlock = FALSE;
    internalExpectHexBlock = ExpectHexBlock;
    ExpectHexBlock = FALSE;
    internalExpectInitialization = ExpectInitialization;
    ExpectInitialization = FALSE;
    while ((c = yh_getc()) != EOF) {
        if (internalExpectCodeBlock) {
            return(lexCodeBlock(c));
        } else if (internalExpectHexBlock) {
            return(lexHexBlock(c));
        } else if (internalExpectInitialization) {
            return(lexInitialization(c));
        } else if (yh_lexLiteral(c, "!;=%{}(),*&+-^.|~:<>[]")) {
            return(lexMisc(c));
        } else if (c == '/') {
            if (!yh_lexCComment(c))
                return('/');
        } else if (yh_isSymbolStartChar(c)) {
            return(yh_lexCSymbol(c, keywordTable));
        } else if (yh_isCNumberStartChar(c)) {
            return(yh_lexCNumber(c));
        } else if (c == '"') {
            return(yh_lexCString(c));
        } else if (c == '\'') {
            return(yh_lexCCharacter(c));
        } else if (yh_isWhitespace(c)) {
            if (c == '\n')
                ++yh_LineNumber;
        } else {
#if 0
            yh_error("bogus character '%c' (0x%02x) in input", c, c);
#endif
        }
    }
    return(0);
}
