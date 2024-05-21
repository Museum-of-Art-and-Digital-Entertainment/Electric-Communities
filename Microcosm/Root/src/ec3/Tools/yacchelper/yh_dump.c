/*
  yh_dump.c -- Structure dump function generator for YaccHelper
  
  Copyright 1993,1994 by Chip Morningstar
  Permission granted to use freely (including commercial use), provided this
    copyright notice remains attached.
  For further information contact chip@netcom.com

*/

#include "generic.h"
#include "yh.h"
#include "yh_struct.h"
#include "yh_dump.h"

#define YHH_ENUM_STRINGS
#include "yh_def.h"
#include "master_yh.h"
#include "yh_undef.h"
#undef  YHH_ENUM_STRINGS

#define YHH_DUMP_TAG_FUNC
#include "yh_def.h"
void YH_dump_tag_dispatch(void *arg, int tabLevel) {
    yh_genericTagged *taggedArg = (yh_genericTagged *)arg;

    switch (taggedArg->_yh_tag) {
#include "master_yh.h"
        default:
            printf("Unknown tag %ld\n", taggedArg->_yh_tag);
            break;
    }
}
#include "yh_undef.h"
#undef YHH_DUMP_TAG_FUNC

#define YHH_DUMP_FUNC
#include "yh_def.h"
#include "master_yh.h"
#undef  YHH_DUMP_FUNC

  void
yh_tab(int count)
{
    while (count-- > 0)
        printf("  ");
}

  void
YH_dump_string(YT(string) *arg, int tabLevel)
{
    if (arg)
	printf("\"%s\"\n", arg);
    else
	printf("NULL STRING\n");
}

  void
YH_dump_charp(YT(charp) *arg, int tabLevel)
{
    if (arg)
	printf("\"%s\"\n", *arg);
    else
	printf("NULL STRING\n");
}

  void
YH_dump_long(YT(long) *arg, int tabLevel)
{
    if (arg)
	printf("%ld\n", *arg);
    else
	printf("NULL LONG PTR\n");
}

  void
YH_dump_float(YT(float) *arg, int tabLevel)
{
    if (arg)
	printf("%f\n", *arg);
    else
	printf("NULL FLOAT PTR\n");
}

  void
YH_dump_bool(YT(bool) *arg, int tabLevel)
{
    if (arg)
	printf("%c\n", *arg ? 'T' : 'F');
    else
	printf("NULL BOOL PTR\n");
}

  void
YH_dump_char(YT(char) *arg, int tabLevel)
{
    if (arg)
	printf("'%c' (0x%02x)\n", *arg, *arg);
    else
	printf("NULL CHAR PTR\n");
}
