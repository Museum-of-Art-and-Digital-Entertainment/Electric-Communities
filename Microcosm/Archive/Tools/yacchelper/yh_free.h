/*
  yh_free.h -- Structure deallocation function declaration includer for
               YaccHelper
  
  Copyright 1993,1994 by Chip Morningstar
  Permission granted to use freely (including commercial use), provided this
    copyright notice remains attached.
  For further information contact chip@netcom.com

*/

#ifndef __YHH_FREE__

#include "yh_struct.h"

#define YHH_FREE_PROTO
#include "yh_def.h"
#include "master_yh.h"
#include "yh_undef.h"
#undef  YHH_FREE_PROTO

void YH_free_string(YT(string) *arg);
void YH_free_charp(YT(charp) *arg);
void YH_free_symbol(YT(symbol) *arg);
void YH_free_long(YT(long) *arg);
void YH_free_float(YT(float) *arg);
void YH_free_bool(YT(bool) *arg);
void YH_free_char(YT(char) *arg);

#define __YHH_FREE__
#endif
