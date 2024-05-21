/*
  yh_walk.h -- Structure walk function declaration includer for YaccHelper
  
  Copyright 1995 by Chip Morningstar
  Permission granted to use freely (including commercial use), provided this
    copyright notice remains attached.
  For further information contact chip@netcom.com

*/

#ifndef __YHH_WALK__

#include "yh_struct.h"

#define YHH_WALK_PROTO
#include "yh_def.h"
#include "master_yh.h"
#include "yh_undef.h"
#undef  YHH_WALK_PROTO

YT(symbol) *YH_walk_symbol(YT(symbol) *arg,
                           bool yh_walk_func(void **arg, void *env),
                           void *yh_walk_env);
YT(string) *YH_walk_string(YT(string) *arg,
                           bool yh_walk_func(void **arg, void *env),
                           void *yh_walk_env);
YT(charp) *YH_walk_charp(YT(charp) *arg,
                           bool yh_walk_func(void **arg, void *env),
                           void *yh_walk_env);
YT(long)   *YH_walk_long(YT(long) *arg,
                         bool yh_walk_func(void **arg, void *env),
                         void *yh_walk_env);
YT(float)   *YH_walk_float(YT(float) *arg,
                         bool yh_walk_func(void **arg, void *env),
                         void *yh_walk_env);
YT(bool)   *YH_walk_bool(YT(bool) *arg,
                         bool yh_walk_func(void **arg, void *env),
                         void *yh_walk_env);
YT(char)   *YH_walk_char(YT(char) *arg,
                         bool yh_walk_func(void **arg, void *env),
                         void *yh_walk_env);

#define __YHH_WALK__
#endif
