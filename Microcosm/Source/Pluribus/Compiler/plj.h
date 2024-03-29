/*
  plj.h -- Types and function protos for Pluribus compiler native code.

  Chip Morningstar
  Electric Communities
  5-March-1997

  Copyright 1997 Electric Communities, all rights reserved.

*/

#include <native.h>
#include "yh.h"
#include "pl.h"

/* In pljnative.c */
HArrayOfObject *convertListToJavaArray(YT(genericList) *list,
    HObject *(*extractFunc)(void *elem));
long javaCall(HObject *obj, char *methodName, char *signature, ...);
long javaCallStatic(char *className, char *methodName, char *signature, ...);
HObject *javaConstruct(char *className, char *signature, ...);
