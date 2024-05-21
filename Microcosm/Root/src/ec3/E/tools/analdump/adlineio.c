/*
  adlineio.c -- Line-oriented I/O conveniences for the Analyze Dump program.

  Chip Morningstar
  Electric Communities
  19-January-1998

  Copyright 1998 Electric Communities, all rights reserved worldwide.

*/

#include "analdump.h"
#include <string.h>
#include <ctype.h>
#include <stdarg.h>

#define MODERATE 500

static bool haveLine = FALSE;
static char savedLine[BUFLEN];
static FILE *infyle;
static FILE *outfyle;
static FILE *logfyle = NULL;

static FILE *fyleStack[MODERATE];
static int fyleStackptr = 0;

  static void
pushFyle(FILE *fyle)
{
    fyleStack[fyleStackptr++] = fyle;
}

  static FILE *
popFyle()
{
    return(fyleStack[--fyleStackptr]);
}

  void
aprintf(char *format, ...)
{
    va_list ap;
    va_start(ap, format);
    vfprintf(outfyle, format, ap);
    if (logfyle)
        vfprintf(logfyle, format, ap);
    va_end(ap);
}

  bool
getLine(char *buf)
{
    if (haveLine) {
        strcpy(buf, savedLine);
        haveLine = FALSE;
        return(TRUE);
    } else if (fgets(buf, BUFLEN, infyle)) {
        return(TRUE);
    } else {
        return(FALSE);
    }
}

  bool
handleOutputRedirection(char *line)
{
    char *openMode = "w";
    char *filename;

    pushFyle(outfyle);
    while (*line != '\0' && *line != '>')
        ++line;
    if (*line == '\0')
        return(TRUE);
    *line = '\0';
    ++line;
    if (*line == '>') {
        openMode = "a";
        ++line;
    }
    while (isspace(*line))
        ++line;
    filename = line;
    while (!isspace(*line))
        ++line;
    outfyle = fopen(filename, openMode);
    if (outfyle)
        return(TRUE);
    outfyle = popFyle();
    fprintf(stderr, "unable to open %s\n", filename);
    return(FALSE);
}

  void
initializeLineIO(FILE *fyle)
{
    haveLine = FALSE;
    infyle = fyle;
}

  void
initializeOutput(void)
{
    outfyle = stdout;
}

  void
logOutput(char *filename)
{
    if (logfyle) {
        fclose(logfyle);
        logfyle = NULL;
    }
    if (filename[0]) {
        logfyle = fopen(filename, "a");
        if (logfyle)
            fprintf(stderr, "logging to %s\n", filename);
        else
            fprintf(stderr, "unable to open %s\n", filename);
    } else {
        aprintf("not logging\n");
    }
}

  void
restoreOutput(void)
{
    FILE *oldOutfyle = outfyle;
    outfyle = popFyle();
    if (oldOutfyle != stdout && outfyle != oldOutfyle) {
        fclose(oldOutfyle);
    }
}

  void
skipLine(FILE *fyle)
{
    char buf[BUFLEN];
    fgets(buf, BUFLEN, fyle);
}

  void
ungetLine(char *buf)
{
    strcpy(savedLine, buf);
    haveLine = TRUE;
}

  int
vlfscanf(FILE *fyle, char *format, ...)
{
    va_list ap;
    char *formatTest = format;
    int fieldCount = 0;
    long args[10];
    int i;
    int result;
    char buf[BUFLEN];

    while (*formatTest) {
        if (*formatTest++ == '%') {
            if (*formatTest != '%' && *formatTest != '*')
                ++fieldCount;
            else
                ++formatTest;
        }
    }
    if (!fgets(buf, BUFLEN, fyle))
        return(-1);

    va_start(ap, format);
    if (fieldCount <= 10) {
        for (i=0; i<fieldCount; ++i)
            args[i] = va_arg(ap, long);
        result = sscanf(buf, format, args[0], args[1], args[2], args[3],
                        args[4], args[5], args[6], args[7], args[8], args[9]);
        if (result == -1)
            result = 0;
    } else {
        result = -2;
    }
    va_end(ap);

    return(result);
}
