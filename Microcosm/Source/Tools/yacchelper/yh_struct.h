/*
  yh_struct.h -- Struct declaration includer for YaccHelper
  
  Copyright 1993,1994 by Chip Morningstar
  Permission granted to use freely (including commercial use), provided this
    copyright notice remains attached.
  For further information contact chip@netcom.com

*/

#ifndef __YHH_STRUCT__

#define YHH_TYPEDEF
#include "yh_def.h"
#include "master_yh.h"
#include "yh_undef.h"
#undef  YHH_TYPEDEF

#define YHH_UNIONDEF
#include "yh_def.h"
#include "master_yh.h"
#include "yh_undef.h"
#undef  YHH_UNIONDEF

#define YHH_ENUM
#include "yh_def.h"
#include "master_yh.h"
#include "yh_undef.h"
#undef  YHH_ENUM

#define YHH_ENUM_TAGS
#include "yh_def.h"
typedef enum {
#include "master_yh.h"
    YTAG(LAST)
} yh_tags;
#include "yh_undef.h"
#undef  YHH_ENUM_TAGS

#define YHH_STRUCT
#include "yh_def.h"
#include "master_yh.h"
#include "yh_undef.h"
#undef  YHH_STRUCT

#define YHH_UNION
#include "yh_def.h"
#include "master_yh.h"
#include "yh_undef.h"
#undef  YHH_UNION

#define __YHH_STRUCT__
#endif
