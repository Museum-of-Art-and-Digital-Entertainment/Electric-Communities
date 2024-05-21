/*
  yh_error.c -- Error message handling routines for YaccHelper

  Copyright 1993,1994 by Chip Morningstar
  Permission granted to use freely (including commercial use), provided this
    copyright notice remains attached.
  For further information contact chip@netcom.com

*/

#include "generic.h"
#include "yh.h"
#include <stdarg.h>
#include <errno.h>

static void yh_message(bool syserr, char *format, va_list ap);

int yh_LineNumber = 1;
char *yh_FileName = "";
int yh_ErrorCount = 0;
int yh_WarningCount = 0;

  void
yh_error(char *format, ...)
{
    va_list ap;

    ++yh_ErrorCount;
    va_start(ap, format);
    yh_message(FALSE, format, ap);
    va_end(ap);
}

  static void
yh_message(bool syserr, char *format, va_list ap)
{
    if (yh_LineNumber > 0)
        fprintf(stderr, "%s:%d: ", yh_FileName, yh_LineNumber);
    vfprintf(stderr, format, ap);
    if (syserr) {
        fprintf(stderr, ": ");
        perror(NULL);
    } else
        fprintf(stderr, "\n");
}

  void
yh_syserror(char *format, ...)
{
    va_list ap;

    ++yh_ErrorCount;
    va_start(ap, format);
    yh_message(TRUE, format, ap);
    va_end(ap);
}

  void
yh_warning(char *format, ...)
{
    va_list ap;

    ++yh_WarningCount;
    va_start(ap, format);
    yh_message(FALSE, format, ap);
    va_end(ap);
}
