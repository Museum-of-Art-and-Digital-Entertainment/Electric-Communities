/*
  yh_walk.c -- Structure walk function generator for YaccHelper
  
  Copyright 1995 by Chip Morningstar
  Permission granted to use freely (including commercial use), provided this
    copyright notice remains attached.
  For further information contact chip@netcom.com

*/

#include "generic.h"
#include "yh.h"
#include "yh_struct.h"
#include "yh_walk.h"

#define YHH_WALK_TAG_FUNC
#include "yh_def.h"
void *YH_walk_tag_dispatch(void *arg, bool yh_walk_func(void **arg, void *env),
                           void *yh_walk_env) {
    yh_genericTagged *taggedArg = (yh_genericTagged *)arg;

    switch (taggedArg->_yh_tag) {
#include "master_yh.h"
    }
    return(arg);
}
#include "yh_undef.h"
#undef YHH_WALK_TAG_FUNC

#define YHH_WALK_FUNC
#include "yh_def.h"
#include "master_yh.h"
#undef  YHH_WALK_FUNC

  YT(symbol) *
YH_walk_symbol(YT(symbol) *arg, bool yh_walk_func(void **arg, void *env),
               void *yh_walk_env)
{
    return(arg);
}

  YT(string) *
YH_walk_string(YT(string) *arg, bool yh_walk_func(void **arg, void *env),
               void *yh_walk_env)
{
    return(arg);
}

  YT(charp) *
YH_walk_charp(YT(charp) *arg, bool yh_walk_func(void **arg, void *env),
               void *yh_walk_env)
{
    return(arg);
}

  YT(long)   *
YH_walk_long(YT(long) *arg, bool yh_walk_func(void **arg, void *env),
             void *yh_walk_env)
{
    return(arg);
}

  YT(float)   *
YH_walk_float(YT(float) *arg, bool yh_walk_func(void **arg, void *env),
	      void *yh_walk_env)
{
    return(arg);
}

  YT(bool)   *
YH_walk_bool(YT(bool) *arg, bool yh_walk_func(void **arg, void *env),
             void *yh_walk_env)
{
    return(arg);
}

  YT(char)   *
YH_walk_char(YT(char) *arg, bool yh_walk_func(void **arg, void *env),
             void *yh_walk_env)
{
    return(arg);
}
