/*
  yh_lex.c -- Useful lexical analysis routines for YaccHelper

  Copyright 1993,1994 by Chip Morningstar
  Permission granted to use freely (including commercial use), provided this
    copyright notice remains attached.
  For further information contact chip@netcom.com

*/

#include "generic.h"
#include "yh.h"
#include <string.h>
#include <ctype.h>
#include "y.tab.h"

extern YYSTYPE yylval;
YYSTYPE yh_result;
int yyparse(void);

static FILE *yh_Input = NULL;

#define MAX_INCLUDE_DEPTH       20
static struct {
    FILE *input;
    char *filename;
    int lineNumber;
} yh_IncludeStack[MAX_INCLUDE_DEPTH];
static int yh_IncludeDepth = 0;

#define BUF(ch) if (buf && buf < bufLimit) *buf++ = ch

  int
yh_getc(void)
{
    int c = getc(yh_Input);

    if (c == EOF && !yh_popInput())
        c = yh_getc();
    return(c);
}

  int
yh_ungetc(int c)
{
    ungetc(c, yh_Input);
    return(c);
}

  bool
yh_include(char *filename)
{
    FILE *newInput = fopen(filename, "r");
    if (newInput == NULL) {
        yh_syserror("unable to open include file \"%s\"", filename);
        return(TRUE);
    } else
        return(yh_pushInput(filename, newInput));
}

  bool
yh_isCNumberStartChar(char c)
{
    return(isdigit(c));
}

  bool
yh_isCSymbolStartChar(char c)
{
    return(yh_isCSymbolChar(c) && !isdigit(c));
}

  bool
yh_isSymbolStartChar(char c)
{
    return(yh_isSymbolChar(c) && !isdigit(c));
}

bool (*yh_isSymbolChar)(char c) = yh_isCSymbolChar;

  bool
yh_isCSymbolChar(char c)
{
    return(isalpha(c) || isdigit(c) || c == '_');
}

  bool
yh_isWhitespace(char c)
{
    return(isspace(c));
}

  int
yh_lexCCharacterBuf(char c, char *buf)
{
    int  value = 0;
    bool scanning = TRUE;
    char *bufLimit = buf + BUFLEN - 1;

    if (buf)
        *bufLimit = '\0';
    BUF(c);
    do {
        c = yh_getc();
        if (c == '\\') {
            BUF(c);
            c = yh_lexCEscapeSequenceBuf(c, &buf, bufLimit);
        } else if (c == '\'') {
            BUF(c);
            scanning = FALSE;
        } else if (c == EOF) {
            scanning = FALSE;
            yh_error("end of file inside character constant");
        } else {
            BUF(c);
        }
        if (scanning)
            value = value * 256 + c;
    } while (scanning);
    BUF('\0');
    yylval = (YYSTYPE)value;
    return(Character);
}

  int
yh_lexCCharacter(char c)
{
    return(yh_lexCCharacterBuf(c, NULL));
}

  bool
yh_lexCCommentBuf(char c, char *buf)
{
    char *bufLimit = buf + BUFLEN - 1;
    char  prev = 'x';

    if (buf)
        *bufLimit = '\0';
    BUF(c);
    c = yh_getc();
    if (c == '*') {
        BUF(c);
        do {
            prev = c;
            c = yh_getc();
            BUF(c);
            if (c == '\n')
                ++yh_LineNumber;
        } while ((prev != '*' || c != '/') && c != EOF);
        BUF('\0');
        return(TRUE);
    } else if (c == '/') {
        BUF(c);
        do {
            c = yh_getc();
            BUF(c);
        } while (c != '\n' && c != EOF);
        if (c == '\n')
            ++yh_LineNumber;
        BUF('\0');
        return(TRUE);
    } else {
        yh_ungetc(c);
        return(FALSE);
    }
}

  bool
yh_lexCComment(char c)
{
    return(yh_lexCCommentBuf(c, NULL));
}

  char
yh_lexCEscapeSequenceBuf(char c, char **bufptr, char *bufLimit)
{
    char *buf = *bufptr;

#define MIN_CHAR ' '
#define MAX_CHAR '~'
    static char EscapeTable[MAX_CHAR-MIN_CHAR+1] = {
        /*   */ ' ',  /* ! */ '!',  /* " */ '\"', /* # */ '#',
        /* $ */ '$',  /* % */ '%',  /* & */ '&',  /* ' */ '\'',
        /* ( */ '(',  /* ) */ ')',  /* * */ '*',  /* + */ '+',
        /* , */ ',',  /* - */ '-',  /* . */ '.',  /* / */ '/',
        /* 0 */ '0',  /* 1 */ '1',  /* 2 */ '2',  /* 3 */ '3',
        /* 4 */ '4',  /* 5 */ '5',  /* 6 */ '6',  /* 7 */ '7',
        /* 8 */ '8',  /* 9 */ '9',  /* : */ ':',  /* ; */ ';',
        /* < */ '<',  /* = */ '=',  /* > */ '>',  /* ? */ '\?',
        /* @ */ '@',  /* A */ 'A',  /* B */ 'B',  /* C */ 'C',
        /* D */ 'D',  /* E */ 'E',  /* F */ 'F',  /* G */ 'G',
        /* H */ 'H',  /* I */ 'I',  /* J */ 'J',  /* K */ 'K',
        /* L */ 'L',  /* M */ 'M',  /* N */ 'N',  /* O */ 'O',
        /* P */ 'P',  /* Q */ 'Q',  /* R */ 'R',  /* S */ 'S',
        /* T */ 'T',  /* U */ 'U',  /* V */ 'V',  /* W */ 'W',
        /* X */ 'X',  /* Y */ 'Y',  /* Z */ 'Z',  /* [ */ '[',
        /* \ */ '\\', /* ] */ ']',  /* ^ */ '^',  /* _ */ '_',
        /* ` */ '`',  /* a */ 'a' , /* b */ '\b', /* c */ 'c',
        /* d */ 'd',  /* e */ 'e',  /* f */ '\f', /* g */ 'g',
        /* h */ 'h',  /* i */ 'i',  /* j */ 'j',  /* k */ 'k',
        /* l */ 'l',  /* m */ 'm',  /* n */ '\n', /* o */ 'o',
        /* p */ 'p',  /* q */ 'q',  /* r */ '\r', /* s */ 's',
        /* t */ '\t', /* u */ 'u',  /* v */ '\v', /* w */ 'w',
        /* x */ 'x',  /* y */ 'y',  /* z */ 'z',  /* { */ '{',
        /* | */ '|',  /* } */ '}',  /* ~ */ '~'
    };
    char result = 0;

    c = yh_getc();
    if (c == EOF) {
        yh_error("end of file inside character escape sequence");
    } else if ('0' <= c && c <= '7') {
        int len = 0;
        do {
            BUF(c);
            result = result * 8 + c - '0';
            ++len;
            c = yh_getc();
        } while ('0' <= c && c <= '7' && len < 3);
        yh_ungetc(c);
    } else if (c == 'x' || c == 'X') {
        int len = 0;
        bool scanning = TRUE;
        BUF(c);
        do {
            c = yh_getc();
            if (isdigit(c)) {
                BUF(c);
                result = result * 16 + c - '0';
            } else if (isxdigit(c)) {
                BUF(c);
                result = result * 16 + tolower(c) - 'a' + 10;
            } else
                scanning = FALSE;
            ++len;
        } while (scanning && len < 2);
        yh_ungetc(c);
        if (!scanning && len == 1)
            yh_warning("empty hex value escape sequence");
    } else if (MIN_CHAR <= c && c <= MAX_CHAR) {
        BUF(c);
        result = EscapeTable[c - MIN_CHAR];
    } else {
        BUF(c);
        result = c;
    }
    *bufptr = buf;
    return(result);
}

  char
yh_lexCEscapeSequence(char c)
{
    char *buf = NULL;
    return(yh_lexCEscapeSequenceBuf(c, &buf, buf));
}

  int
yh_lexCNumberBuf(char c, char *buf)
{
    int number = 0;
    char *bufLimit = buf + BUFLEN - 1;

    if (buf)
        *bufLimit = '\0';
    BUF(c);
    if (c == '0') {
        c = yh_getc();
        if (c == 'x' || c == 'X') {
            bool scanning = TRUE;
            BUF(c);
            do {
                c = yh_getc();
                if (isdigit(c)) {
                    number = number * 16 + c - '0';
                    BUF(c);
                } else if (isxdigit(c)) {
                    number = number * 16 + tolower(c) - 'a' + 10;
                    BUF(c);
                } else
                    scanning = FALSE;
            } while (scanning);
        } else {
            while ('0' <= c && c <= '7') {
                number = number * 8 + c - '0';
                BUF(c);
                c = yh_getc();
            }
        } 
    } else {
        do {
            number = number * 10 + c - '0';
            BUF(c);
            c = yh_getc();
        } while (isdigit(c));
    }
    yh_ungetc(c);
    BUF('\0');
    yylval = (YYSTYPE)number;
    return(Number);
}

  int
yh_lexCNumber(char c)
{
    return(yh_lexCNumberBuf(c, NULL));
}

  int
yh_lexCSymbol(char c, t_yh_keywordTable *keywordTable)
{
    char  buf[80];
    char *bufptr = buf;
    bool  tooLong = FALSE;
    YT(symbol) *symTabEntry;
    int   test;

    do {
        if (bufptr - buf < sizeof(buf))
            *bufptr++ = c;
        else
            tooLong = TRUE;
        c = yh_getc();
    } while (yh_isSymbolChar(c));
    yh_ungetc(c);
    *bufptr = '\0';
    while (keywordTable->keyword) {
        if ((test = strcmp(buf, keywordTable->keyword)) == 0)
            return(keywordTable->value);
        else if (test < 0)
            break;
        else
            ++keywordTable;
    }
    symTabEntry = yh_handleSymbol(buf);
    if (tooLong && yh_symbolWasNew(symTabEntry))
        yh_warning("identifier '%s' too long, truncated to %d characters",
                    buf, sizeof(buf) - 1);
    yylval = (YYSTYPE)symTabEntry;
    return(Symbol);
}

  int
yh_lexCStringBuf(char c, char *buf)
{
    char  str[1000];
    char *strptr = str;
    bool  tooLong = FALSE;
    bool  scanning = TRUE;
    char *bufLimit = buf + BUFLEN - 1;

    if (buf)
        *bufLimit = '\0';
    BUF(c);

    do {
        c = yh_getc();
        if (c == '\\') {
            BUF(c);
            c = yh_lexCEscapeSequenceBuf(c, &buf, bufLimit);
        } else if (c == '"') {
            BUF(c);
            c = '\0';
            BUF(c);
            scanning = FALSE;
        } else if (c == EOF) {
            c = '\0';
            BUF(c);
            scanning = FALSE;
            yh_error("end of file inside string constant");
        } else {
            BUF(c);
        }
        if (strptr - str < sizeof(str))
            *strptr++ = c;
        else
            tooLong = TRUE;
    } while (scanning);
    if (tooLong)
        yh_warning("string too long, trancated to %d characters",
                    sizeof(str) - 1);
    yylval = (YYSTYPE)STRDUP(str);
    return(String);
}

  int
yh_lexCString(char c)
{
    return(yh_lexCStringBuf(c, NULL));
}

  bool
yh_lexLiteral(char c, char *literals)
{
    while (*literals)
        if (c == *literals++)
            return(TRUE);
    return(FALSE);
}

  void *
yh_parse(char *filename, char mode, char *tag)
{
    if (yh_Input != stdin && yh_Input != NULL) {
        fclose(yh_Input);
    }
    if (filename) {
        yh_Input = fopen(filename, "r");
        if (yh_Input == NULL) {
            yh_syserror("unable to open %s file \"%s\"", tag, filename);
            yh_FileName = "<no input file>";
        } else {
            yh_FileName = filename;
        }
    } else {
        yh_Input = stdin;
        yh_FileName = "<standard input>";
    }
    if (yh_Input) {
        yh_LineNumber = 1;
        yh_ungetc(mode);
        yyparse();
        yh_LineNumber = 0;
        return((void *)yh_result);
    } else {
        yh_LineNumber = 0;
        return(NULL);
    }
}

  bool
yh_popInput(void)
{
    if (yh_IncludeDepth == 0) {
        return(TRUE);
    } else {
        --yh_IncludeDepth;
        yh_Input = yh_IncludeStack[yh_IncludeDepth].input;
        yh_FileName = yh_IncludeStack[yh_IncludeDepth].filename;
        yh_LineNumber = yh_IncludeStack[yh_IncludeDepth].lineNumber;
        return(FALSE);
    }
}

  bool
yh_pushInput(char *filename, FILE *newInput)
{
    if (yh_IncludeDepth >= MAX_INCLUDE_DEPTH) {
        yh_error("include files nested greater than %d deep",
                 MAX_INCLUDE_DEPTH);
        return(TRUE);
    } else {
        yh_IncludeStack[yh_IncludeDepth].input = yh_Input;
        yh_Input = newInput;
        yh_IncludeStack[yh_IncludeDepth].filename = yh_FileName;
        yh_FileName = filename;
        yh_IncludeStack[yh_IncludeDepth].lineNumber = yh_LineNumber;
        yh_LineNumber = 1;
        ++yh_IncludeDepth;
        return(FALSE);
    }
}
