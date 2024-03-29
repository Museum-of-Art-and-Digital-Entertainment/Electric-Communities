/*
  yh_dump.h -- Structure dump function declaration includer for YaccHelper
  
  Copyright 1993,1994 by Chip Morningstar
  Permission granted to use freely (including commercial use), provided this
    copyright notice remains attached.
  For further information contact chip@netcom.com

*/

#ifndef __YHH_DUMP__

#include "yh_struct.h"

#define YHH_DUMP_PROTO
#include "yh_def.h"
#include "master_yh.h"
#include "yh_undef.h"
#undef  YHH_DUMP_PROTO

void YH_dump_symbol(YT(symbol) *arg, int tabLevel);
void YH_dump_string(YT(string) *arg, int tabLevel);
void YH_dump_charp(YT(charp) *arg, int tabLevel);
void YH_dump_long(YT(long) *arg, int tabLevel);
void YH_dump_float(YT(float) *arg, int tabLevel);
void YH_dump_bool(YT(bool) *arg, int tabLevel);
void YH_dump_char(YT(char) *arg, int tabLevel);

#define __YHH_DUMP__
#endif
