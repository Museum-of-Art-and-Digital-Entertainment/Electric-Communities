/*
  plfile.c -- System-specific file I/O for Pluribus (SunOS Unix version)

  Chip Morningstar
  Electric Communities
  18-July-1996

  Copyright 1996 Electric Communities, all rights reserved.

*/

#include "generic.h"
#include "yh.h"
#include <string.h>
#include <stdarg.h>
#include <errno.h>
#include <sys/stat.h>
#include "pl.h"

#define MAX_OUTPUT_DEPTH        5
static struct {
    FILE *output;
    char *filename;
} OutputStack[MAX_OUTPUT_DEPTH];
static int OutputDepth = -1;
static FILE *CurrentOutput = NULL;

static char *CurrentOutputFilename = "<stdout>";

  bool
popOutput(void)
{
    if (OutputDepth < 0) {
        yh_error("internal error: output stack underflow");
        return(FALSE);
    } else {
        if (CurrentOutput != stdout)
            fclose(CurrentOutput);
        CurrentOutput = OutputStack[OutputDepth].output;
        CurrentOutputFilename = OutputStack[OutputDepth].filename;
        --OutputDepth;
        return(TRUE);
    }
}

  static bool
mkdirIfAbsent(char *name)
{
    /*KSSX errno = 89 means 'Operation not applicable' which seems to apply
           to the software-mounted user directories... KSSX*/
    if (mkdir(name, 0755) < 0 && (errno != EEXIST && errno != 89)) {
        yh_error("unable to create directory %s", name);
        return(FALSE);
    }
    return(TRUE);
}

  static FILE *
fopenAnywhere(char *filename)
{
    char *slashptr = filename;

    while (slashptr = strchr(slashptr + 1, '/')) {
        *slashptr = '\0';
        if (!mkdirIfAbsent(filename))
            break;
        *slashptr = '/';
    }
    if (!slashptr)
        return(fopen(filename, "w"));
    else
        return(NULL);
}

  bool
pushOutput(char *filename, char *saveFile)
{
    ++OutputDepth;
    if (OutputDepth >= MAX_OUTPUT_DEPTH) {
        yh_error("internal error: output files nested too deep");
        return(FALSE);
    } else {
        FILE *output = fopenAnywhere(filename);
        FILE *save;
        if (output == NULL) {
            yh_error("unable to open output file %s", filename);
            return(FALSE);
        }
        if (saveFile != NULL) {
            if (!(save = fopen(saveFile, "a"))) {
                yh_error("unable to open file %s for generated file names",
                         saveFile);
                return(FALSE);
            }
            fprintf(save, "%s\n", filename);
            fclose(save);
        }
        OutputStack[OutputDepth].output = CurrentOutput;
        CurrentOutput = output;
        OutputStack[OutputDepth].filename = CurrentOutputFilename;
        CurrentOutputFilename = STRDUP(filename);
        return(TRUE);
    }
}

  void
pprintf(char *format, ...)
{
    va_list ap;

    va_start(ap, format);
    vfprintf(CurrentOutput, format, ap);
    va_end(ap);
}

  void
pprintln(char *format, ...)
{
    va_list ap;

    va_start(ap, format);
    if (CurrentOutput == NULL)
        CurrentOutput = stdout;
    vfprintf(CurrentOutput, format, ap);
    va_end(ap);
    pputs("\n");
}

  void
pputs(char *str)
{
    if (CurrentOutput == NULL)
        CurrentOutput = stdout;
    fputs(str, CurrentOutput);
}

  void
pputch(char c)
{
    if (CurrentOutput == NULL)
        CurrentOutput = stdout;
    fputc(c, CurrentOutput);
}
