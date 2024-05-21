/*
  generic.h -- Generally useful defs

  Chip Morningstar
  Electric Communities
  22-January-1994

  Copyright 1994 Electric Communities, all rights reserved.

*/
#include <stdio.h>
#include <stdlib.h>

typedef unsigned char   byte;           /*  8-bit number */
typedef unsigned short  word;           /* 16-bit number */
typedef unsigned long   longword;       /* 32-bit number */

typedef int     bool;
#define TRUE    1
#define FALSE   0

#define BUFLEN 10000

#define Case    break; case
#define Orcase  case
#define Default break; default

#ifndef DEBUG_ALLOC
#include <malloc.h>
#ifndef WIN32
char *strdup(const char *s);
#endif
#define FREE(p)    free(p)
#define ALLOC(n)   malloc(n)
#define STRDUP(s)  strdup(s)
#else
extern bool DebugAlloc;
void myfree(void *ptr, char *file, int line);
void *myalloc(long size, char *file, int line);
char *mystrdup(char *s, char *file, int line);
#define FREE(p)    myfree(p, __FILE__, __LINE__)
#define ALLOC(n)   myalloc(n, __FILE__, __LINE__)
#define STRDUP(s)  mystrdup(s, __FILE__, __LINE__)
#endif

#define TypeAlloc(type)         ((type *) ALLOC(sizeof(type)))
#define TypeAllocMulti(type, n) ((type *) ALLOC(sizeof(type) * (n)))
#define TypeAllocSized(type, s) ((type *) ALLOC((s)))

