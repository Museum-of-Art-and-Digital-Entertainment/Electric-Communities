/*
  plmangle.c -- Mangling routines for Pluribus generated names.

  Karl Schumaker
  Electric Communities
  May 12, 1997

  Copyright 1997 Electric Communities, all rights reserved.

*/

#include <string.h>
#include <stdlib.h>

  char *
doMangle(char *name, char *mangle)
{
    char *result = (char *)calloc(strlen(name) + strlen(mangle) + 1, 1);
    result = strcat(result, name);
    result = strcat(result, mangle);
    return result;
}

  char *
iiJavaName(char *name)
{
    return doMangle(name, "$iijif");
}

  char *
iiCodeName(char *name)
{
    return doMangle(name, "$iicode");
}

  char *
kindClassName(char *name)
{
    return doMangle(name, "$kind");
}

  char *
presenceRouterJavaName(char *name)
{
    return doMangle(name, "$prjif");
}

  char *
presenceRouterName(char *name)
{
    return doMangle(name, "$pr");
}

  char *
unumImplName(char *name)
{
    return doMangle(name, "$ui");
}

  char *
unumRouterJavaName(char *name)
{
    return doMangle(name, "$urjif");
}

  char *
unumRouterName(char *name)
{
    return doMangle(name, "$ur");
}


