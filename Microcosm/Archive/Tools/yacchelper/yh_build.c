/*
  yh_build.c -- Structure build function generator for YaccHelper
  
  Copyright 1993,1994 by Chip Morningstar
  Permission granted to use freely (including commercial use), provided this
    copyright notice remains attached.
  For further information contact chip@netcom.com

*/

#include "generic.h"
#include "yh.h"
#include "yh_struct.h"
#include "yh_build.h"

#define YHH_BUILD_FUNC
#include "yh_def.h"
#include "master_yh.h"
#undef  YHH_BUILD_FUNC
