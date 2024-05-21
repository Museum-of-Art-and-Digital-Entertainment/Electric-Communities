/*
  yh_free.c -- Structure deallocation function generator for YaccHelper
  
  Copyright 1993,1994 by Chip Morningstar
  Permission granted to use freely (including commercial use), provided this
    copyright notice remains attached.
  For further information contact chip@netcom.com

*/

#include "generic.h"
#ifndef WIN32
#include <malloc.h>
#endif
#include <string.h>
#include "yh.h"
#include "yh_struct.h"
#include "yh_free.h"

#define YHH_FREE_FUNC
#include "yh_def.h"
#include "master_yh.h"
#undef  YHH_FREE_FUNC

#ifdef DEBUG_ALLOC

  void *
myalloc(long size, char *file, int line)
{
    void *result = malloc(size);
    if (DebugAlloc)
        printf("@@%08x ALLOC(%d) %s %d\n", result, size, file, line);
    return(result);
}

  void
myfree(void *stuff, char *file, int line)
{
return;
    if (DebugAlloc)
        printf("@@%08x FREE %s %d\n", stuff, file, line);
    free(stuff);
}

  char *
mystrdup(char *s, char *file, int line)
{
    char *result = strdup(s);
    if (DebugAlloc)
        printf("@@%08x ALLOC(%d) %s %d\n", result, strlen(s) + 1, file, line);
    return(result);
}

#endif

  void
YH_free_string(YT(string) *arg)
{
    FREE(arg);
}

  void
YH_free_charp(YT(charp) *arg)
{
    FREE(arg);
}

  void
YH_free_long(YT(long) *arg)
{
    /* do nothing -- it's not really a pointer */
}

  void
YH_free_float(YT(float) *arg)
{
    /* do nothing -- it's not really a pointer */
}

  void
YH_free_bool(YT(bool) *arg)
{
    /* do nothing -- it's not really a pointer */
}

  void
YH_free_char(YT(char) *arg)
{
    /* do nothing -- it's not really a pointer */
}
