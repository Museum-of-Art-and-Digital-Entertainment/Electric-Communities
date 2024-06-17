/*
  jfelex.c -- Lexer for E-to-pure Java translator

  Chip Morningstar
  Electric Communities
  9-January-1998

  Copyright 1998 Electric Communities, all rights reserved worldwide.

*/

#include "generic.h"
#include "yh.h"
#include "yh_struct.h"
#include "yh_build.h"
#include "jfe.h"
#include "y.tab.h"
#include "string.h"

static char *scanNumber(char *buf, int buflen, bool *hitFloat);

extern YYSTYPE yylval;

t_yh_keywordTable ReservedWords[] = {
    { "_DEBUG_",          EDEBUG },
    { "abstract",         ABSTRACT },
    { "boolean",          BOOLEAN },
    { "break",            BREAK },
    { "byte",             BYTE },
    { "case",             CASE },
    { "catch",            CATCH },
    { "char",             CHAR },
    { "class",            CLASS },
    { "continue",         CONTINUE },
    { "default",          DEFAULT },
    { "do",               DO },
    { "double",           DOUBLE },
    { "ecatch",           ECATCH },
    { "eclass",           ECLASS },
    { "edebug",           EDEBUG },
    { "efalse",           EFALSE },
    { "eif",              EIF },
    { "einterface",       EINTERFACE },
    { "ekeep",            EKEEP },
    { "else",             ELSE },
    { "emethod",          EMETHOD },
    { "enull",            ENULL },
    { "eorif",            EORIF },
    { "eorwhen",          EORWHEN },
    { "ethrow",           ETHROW },
    { "etrue",            ETRUE },
    { "etry",             ETRY },
    { "ewhen",            EWHEN },
    { "ewhenever",        EWHENEVER },
    { "extends",          EXTENDS },
    { "false",            JFALSE },
    { "final",            FINAL },
    { "finally",          FINALLY },
    { "float",            FLOAT },
    { "for",              FOR },
    { "if",               IF },
    { "implements",       IMPLEMENTS },
    { "import",           IMPORT },
    { "instanceof",       INSTANCEOF },
    { "int",              INT },
    { "interface",        INTERFACE },
    { "local",            LOCAL },
    { "long",             LONG },
    { "native",           NATIVE },
    { "new",              NEW },
    { "null",             JNULL },
    { "package",          PACKAGE },
    { "private",          PRIVATE },
    { "protected",        PROTECTED },
    { "public",           PUBLIC },
    { "return",           RETURN },
    { "short",            SHORT },
    { "static",           STATIC },
    { "super",            SUPER },
    { "switch",           SWITCH },
    { "synchronized",     SYNCHRONIZED },
    { "this",             THIS },
    { "throw",            THROW },
    { "throws",           THROWS },
    { "transient",        TRANSIENT },
    { "true",             JTRUE },
    { "try",              TRY },
    { "void",             VOID },
    { "volatile",         VOLATILE },
    { "while",            WHILE },
    { NULL,               0 }
};

static char WhitespaceBuf[BUFLEN];
static int  WhitespaceBufptr = 0;

/**
 * saveWhitespaceChar -- Remember a character of whitespace.
 *
 * @param c  The whitespace (actually it could be a comment) character
 */
  static void
saveWhitespaceChar(char c)
{
    if (WhitespaceBufptr < BUFLEN - 1)
        WhitespaceBuf[WhitespaceBufptr++] = c;
}

/**
 * saveWhitespaceBuf -- Remember a string of whitespace
 *
 * @param buf  The whitespace string
 */
  static void
saveWhitespaceBuf(char *buf)
{
    while (*buf)
        saveWhitespaceChar(*buf++);
}

/**
 * flushWhitespace -- Empty the whitespace buffer and return its contents
 *
 * @returns  An (alloc'd) string containing the current contents of the
 *      whitespace buffer
 */
  char *
flushWhitespace(void)
{
    char *result;

    WhitespaceBuf[WhitespaceBufptr++] = '\0';
    result = STRDUP(WhitespaceBuf);
    WhitespaceBufptr = 0;
    return(result);
}

/**
 * isSymbolChar -- Test if a given character is allowed in a symbol.
 *
 * @param c  The character to be tested
 * @returns TRUE iff the given character is a valid symbol character
 */
  static bool
isSymbolChar(char c)
{
    return(yh_isCSymbolChar(c) || c == '$');
}

/**
 * lexCharacter -- Scan input for a character literal token.
 *
 * @param c  The first character of the token
 * @returns  The token found (Character). The token value ('yylval') will be a
 *      characterLiteral struct for the literal
 */
  static int
lexCharacter(char c)
{
    char buf[BUFLEN];
    int result = yh_lexCCharacterBuf(c, buf);
    yylval = YH_BUILD(characterLiteral)(flushWhitespace(), STRDUP(buf));
    return(result);
}

/**
 * lexMisc -- Scan input for operators and other funky tokens. This is
 *      guaranteed to find such a token as we only call into here if we have
 *      already seen a valid start-of-token character.
 *
 * @param c  The first character of the token
 * @returns  The token found.  The token value ('yylval') will be the
 *      whitespace that preceded the token.
 */
  static int
lexMisc(char c)
{
    static struct {
        char *string;
        int   token;
    } Tokens[] = {
        { "!",        '!' },
        { "!=",       OpNeq },
        { "%",        '%' },
        { "%=",       AssMod },
        { "&",        '&' },
        { "&&",       OpLAnd },
        { "&=",       AssAnd },
        { "(",        '(' },
        { ")",        ')' },
        { "*",        '*' },
        { "*=",       AssMul },
        { "+",        '+' },
        { "++",       OpInc },
        { "+=",       AssAdd },
        { ",",        ',' },
        { "-",        '-' },
        { "--",       OpDec },
        { "-=",       AssSub },
        { ".",        '.' },
        { "/",        '/' },
        { "/=",       AssDiv },
        { ":",        ':' },
        { ";",        ';' },
        { "<",        '<' },
        { "<-",       Send },
        { "<<",       OpLsl },
        { "<<=",      AssLsl },
        { "<=",       OpLeq },
        { "=",        '=' },
        { "==",       OpEq },
        { ">",        '>' },
        { ">=",       OpGeq },
        { ">>",       OpLsr },
        { ">>=",      AssLsr },
        { ">>>",      OpAsr },
        { ">>>=",     AssAsr },
        { "?",        '?' },
        { "[",        '[' },
        { "]",        ']' },
        { "^",        '^' },
        { "^=",       AssXor },
        { "{",        '{' },
        { "|",        '|' },
        { "|=",       AssOr },
        { "||",       OpLOr },
        { "}",        '}' },
        { "~",        '~' }
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
            yylval = (YYSTYPE) flushWhitespace();
            return(Tokens[base].token);
        }
        for (hi = lo; hi < limit && c == Tokens[hi].string[pos]; ++hi)
            ;
        if (hi == lo + 1) {
            yylval = (YYSTYPE) flushWhitespace();
            return(Tokens[lo].token);
        }
        limit = hi;
        base = lo;
        ++pos;
        c = yh_getc();
    }
}

/**
 * lexNumber -- Scan input for a number token.
 *
 * @param c  The first character of the token
 * @returns  The token found (Number). The token value ('yylval') will be a
 *      numberLiteral struct for the number
 */
  static int
lexNumber(char c)
{
    int result;
    char buf[BUFLEN];
    bool hitFloat;
    char *bufptr = buf;

    yh_ungetc(c);
    bufptr = scanNumber(buf, BUFLEN, &hitFloat);
    if (c == '0' && !hitFloat && bufptr-buf == 1) {
        result = yh_lexCNumberBuf(c, buf);
        if (result == Number) {
            c = yh_getc();
            if (c == 'l')
                strcat(buf, "l");
            else if (c == 'L')
                strcat(buf, "L");
            else
                yh_ungetc(c);
        }
    } else {
        result = Number;
        c = yh_getc();
        if (c == 'f' || c == 'F' || c == 'd' || c == 'D')
            *bufptr++ = c;
        else if (!hitFloat && (c == 'l' || c == 'L'))
            *bufptr++ = c;
        else
            yh_ungetc(c);
        *bufptr = '\0';
    }
    yylval = YH_BUILD(numberLiteral)(flushWhitespace(), STRDUP(buf));
    return(result);
}

/**
 * lexString -- Scan input for a string literal token.
 *
 * @param c  The first character of the token
 * @returns  The token found (String). The token value ('yylval') will be a
 *      stringLiteral struct for the literal
 */
  static int
lexString(char c)
{
    char buf[BUFLEN];
    int result = yh_lexCStringBuf(c, buf);
    yylval = YH_BUILD(stringLiteral)(flushWhitespace(), STRDUP(buf));
    return(result);
}

/**
 * lexSymbol -- Scan input for a symbol or reserved word token.
 *
 * @param c  The first character of the token
 * @param reservedWords  The table of reserved words
 * @returns  The token found. If the token is a symbol, the token value
 *      ('yylval') will be an identifier struct for the token.  If the token is
 *      a reserved word, the token value will be the whitespace that preceded
 *      it.
 */
  static int
lexSymbol(char c, t_yh_keywordTable *reservedWords)
{
    int result;
    char *ws;

    yh_isSymbolChar = isSymbolChar;
    result = yh_lexCSymbol(c, reservedWords);
    ws = flushWhitespace();
    if (result == Symbol) {
        yylval = YH_BUILD(identifier)(ws, YC(symbol,yylval));
        return(Identifier);
    } else {
        yylval = (YYSTYPE) ws;
        return(result);
    }
}

/**
 * scanDecimal -- Scan input for a decimal integer.
 *
 * @param buf  A buffer into which the scanned number will be placed
 * @param bufLimit  A pointer to the end of 'buf'
 * @returns  Pointer to next position in 'buf' after scanned number
 */
  char *
scanDecimal(char *buf, char *bufLimit)
{
#define BUF(ch) if (buf && buf < bufLimit) *buf++ = ch
    for (;;) {
        char c = yh_getc();
        if ('0' <= c && c <= '9') {
            BUF(c);
        } else {
            yh_ungetc(c);
            return(buf);
        }
    }
}

/**
 * scanNumber -- Scan input for a raw decimal number. The syntax is
 *
 *      number:
 *              significand
 *       |      significand efield
 *       ;
 *      significand:  
 *              digits
 *       |      digits '.'
 *       |      '.' digits
 *       |      digits '.' digits
 *       ;
 *      efield:
 *              echar digits
 *       |      echar sign digits
 *       ;
 *      echar:
 *              'e'
 *       |      'E'
 *       ;
 *      sign:
 *              '+'
 *       |      '-'
 *       ;
 *      digits:
 *              digit
 *       |      digits digit
 *       ;
 *      
 *      digit:
 *              '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
 *       ;
 *
 * @param buf  A buffer into which the scanned number string will be placed
 * @param buflen  The length of the buffer
 * @param hitFloat  Return flag: TRUE->floating point format was found,
 *      FALSE->integer format found
 * @returns Pointer to first location in buf after scanned number
 */
  static char *
scanNumber(char *buf, int buflen, bool *hitFloat)
{
    char *bufLimit = buf + buflen;
    char c;

    *hitFloat = FALSE;
    c = yh_getc();
    if (c == '.') {
        BUF('.');
        *hitFloat = TRUE;
        buf = scanDecimal(buf, bufLimit);
    } else if ('0' <= c && c <= '9') {
        yh_ungetc(c);
        buf = scanDecimal(buf, bufLimit);
        c = yh_getc();
        if (c == '.') {
            BUF('.');
            *hitFloat = TRUE;
            buf = scanDecimal(buf, bufLimit);
        } else {
            yh_ungetc(c);
        }
    } else {
        BUF('\0');
        return(FALSE);
    }
    
    c = yh_getc();
    if (c == 'e' || c == 'E') {
        *hitFloat = TRUE;
        BUF(c);
        c = yh_getc();
        if (c == '+' || c == '-') {
            BUF(c);
            c = yh_getc();
        }
        yh_ungetc(c);
        if ('0' <= c && c <= '9') {
            buf = scanDecimal(buf, bufLimit);
        }
    } else {
        yh_ungetc(c);
    }
    return(buf);
}

/**
 * yylex -- The YACC standard lexer
 *
 * @returns The next token in the input, or 0 on end of input. Token value
 *      is returned in the global variable 'yylval'
 */
  int
yylex(void)
{
    char c;

    while ((c = yh_getc()) != EOF) {
        if (yh_isSymbolStartChar(c)) {
            return(lexSymbol(c, ReservedWords));
        } else if (yh_isCNumberStartChar(c)) {
            return(lexNumber(c));
        } else if (c == '\'') {
            return(lexCharacter(c));
        } else if (c == '"') {
            return(lexString(c));
        } else if (c == '/') {
            char buf[BUFLEN];
            if (!yh_lexCCommentBuf(c, buf)) {
                return(lexMisc(c));
            } else {
                saveWhitespaceBuf(buf);
            }
        } else if (yh_lexLiteral(c, ".;*{},()[]=:?|^&<>+-%~!")) {
            return(lexMisc(c));
        } else if (yh_isWhitespace(c)) {
            if (c == '\n')
                ++yh_LineNumber;
            saveWhitespaceChar(c);
        } else {
            yh_error("bogus character '%c' (0x%02x) in input", c, c);
        }
    }
    return(0);
}
