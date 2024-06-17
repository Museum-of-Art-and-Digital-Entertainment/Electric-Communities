/*
  yh_def.h -- Variant macro definitions for YaccHelper

  Copyright 1993,1994 by Chip Morningstar
  Permission granted to use freely (including commercial use), provided this
    copyright notice remains attached.
  For further information contact chip@netcom.com

*/

#if defined(YHH_TYPEDEF)

#define YH_LIST_DEF(type) typedef struct struct_YH_##type##List YT(type##List);
#define YH_DEF_START(type)        typedef struct struct_YH_##type YT(type);
#define YH_DEF_START_TAGGED(type) YH_DEF_START(type)
#define YH_TAG_FIELD(type)
#define YH_FLD_ENUM_START(name)
#define YH_FLD_ENUM_CASE(en)
#define YH_FLD_ENUM_END(name)
#define YH_FLD_PRIM(name,type)
#define YH_FLD_PRIMA(name,type)
#define YH_FLDI_PRIM(name,type)
#define YH_ARG_PRIM(name,type)
#define YH_FLD_STR(name,dim)
#define YH_FLDI_STR(name,dim)
#define YH_ARG_STR(name,dim)
#define YH_FLD_CSTR(name)
#define YH_FLDI_CSTR(name)
#define YH_ARG_CSTR(name)
#define YH_FLD_PTR(name,type)
#define YH_FLD_PTRA(name,type)
#define YH_FLD_PTRN(name,type)
#define YH_FLD_PTRF(name,type,func)
#define YH_FLDI_PTR(name,type)
#define YH_ARG_PTR(name,type)
#define YH_FLD_OBJ(name)
#define YH_FLD_VAR_START(selector,item)
#define YH_VAR_CASE(acase,type,item)
#define YH_FLD_VAR_END(selector,item)
#define YH_BUILD_CODE(code,type)
#define YH_BUILD_CODE_TAGGED(code,type)
#define YH_DEF_END(type)

#else
#if defined(YHH_STRUCT)

#define YH_LIST_DEF(type)       struct struct_YH_##type##List { \
                                    YT(type) *type;             \
                                    YT(type##List) *next;       \
                                };
#define YH_DEF_START(type)      struct struct_YH_##type {
#define YH_DEF_START_TAGGED(type) YH_DEF_START(type)
#define YH_TAG_FIELD(type)          long _yh_tag;
#define YH_FLD_ENUM_START(name)     word name;
#define YH_FLD_ENUM_CASE(en)
#define YH_FLD_ENUM_END(name)
#define YH_FLD_PRIM(name,type)      type name;
#define YH_FLD_PRIMA(name,type)     type *name;
#define YH_FLDI_PRIM(name,type)     type name;
#define YH_ARG_PRIM(name,type)
#define YH_FLD_STR(name,dim)        char name[dim];
#define YH_FLDI_STR(name,dim)       char name[dim];
#define YH_ARG_STR(name, dim)
#define YH_FLD_CSTR(name)           char *name;
#define YH_FLDI_CSTR(name)          char *name;
#define YH_ARG_CSTR(name)
#define YH_FLD_PTR(name,type)       YT(type) *name;
#define YH_FLD_PTRA(name,type)      YT(type) **name;
#define YH_FLD_PTRN(name,type)      YT(type) *name;
#define YH_FLD_PTRF(name,type,func) YT(type) *name;
#define YH_FLDI_PTR(name,type)      YT(type) *name;
#define YH_ARG_PTR(name, type)
#define YH_FLD_OBJ(name)            void *name;
#define YH_FLD_VAR_START(selector,item) word selector; \
                                        YUT(item) *item;
#define YH_VAR_CASE(acase,type,item)
#define YH_FLD_VAR_END(selector,item)
#define YH_BUILD_CODE(code,type)
#define YH_BUILD_CODE_TAGGED(code,type)
#define YH_DEF_END(type)        };

#else
#if defined(YHH_ENUM)

#define YH_LIST_DEF(type)
#define YH_DEF_START(type)
#define YH_DEF_START_TAGGED(type)
#define YH_TAG_FIELD(type)
#define YH_FLD_ENUM_START(name)         enum e_##name {
#define YH_FLD_ENUM_CASE(en)                en,
#define YH_FLD_ENUM_END(name)           __ANSI_hack##name };
#define YH_FLD_PRIM(name,type)
#define YH_FLD_PRIMA(name,type)
#define YH_FLDI_PRIM(name,type)
#define YH_ARG_PRIM(name,type)
#define YH_FLD_STR(name,dim)
#define YH_FLDI_STR(name,dim)
#define YH_ARG_STR(name, dim)
#define YH_FLD_CSTR(name)
#define YH_FLDI_CSTR(name)
#define YH_ARG_CSTR(name)
#define YH_FLD_PTR(name,type)
#define YH_FLD_PTRA(name,type)
#define YH_FLD_PTRN(name,type)
#define YH_FLD_PTRF(name,type,func)
#define YH_FLDI_PTR(name,type)
#define YH_ARG_PTR(name, type)
#define YH_FLD_OBJ(name)
#define YH_FLD_VAR_START(selector,item) typedef enum {
#define YH_VAR_CASE(acase,type,item)        acase,
#define YH_FLD_VAR_END(selector,item)   __ANSI_hack##selector } e_##selector;
#define YH_BUILD_CODE(code,type)
#define YH_BUILD_CODE_TAGGED(code,type)
#define YH_DEF_END(type)

#else
#if defined(YHH_ENUM_TAGS)

#define YH_LIST_DEF(type)
#define YH_DEF_START(type)
#define YH_DEF_START_TAGGED(type)
#define YH_TAG_FIELD(type)
#define YH_FLD_ENUM_START(name)
#define YH_FLD_ENUM_CASE(en)
#define YH_FLD_ENUM_END(name)
#define YH_FLD_PRIM(name,type)
#define YH_FLD_PRIMA(name,type)
#define YH_FLDI_PRIM(name,type)
#define YH_ARG_PRIM(name,type)
#define YH_FLD_STR(name,dim)
#define YH_FLDI_STR(name,dim)
#define YH_ARG_STR(name, dim)
#define YH_FLD_CSTR(name)
#define YH_FLDI_CSTR(name)
#define YH_ARG_CSTR(name)
#define YH_FLD_PTR(name,type)
#define YH_FLD_PTRA(name,type)
#define YH_FLD_PTRN(name,type)
#define YH_FLD_PTRF(name,type,func)
#define YH_FLDI_PTR(name,type)
#define YH_ARG_PTR(name, type)
#define YH_FLD_OBJ(name)
#define YH_FLD_VAR_START(selector,item)
#define YH_VAR_CASE(acase,type,item)
#define YH_FLD_VAR_END(selector,item)
#define YH_BUILD_CODE(code,type)
#define YH_BUILD_CODE_TAGGED(code,type) YTAG(type),
#define YH_DEF_END(type)

#else
#if defined(YHH_DUMP_TAG_FUNC)

#define YH_LIST_DEF(type)
#define YH_DEF_START(type)
#define YH_DEF_START_TAGGED(type) case YTAG(type):                            \
                                      YH_dump_##type(YC(type,arg), tabLevel); \
                                      break;
#define YH_TAG_FIELD(type)
#define YH_FLD_ENUM_START(name)
#define YH_FLD_ENUM_CASE(en)
#define YH_FLD_ENUM_END(name)
#define YH_FLD_PRIM(name,type)
#define YH_FLD_PRIMA(name,type)
#define YH_FLDI_PRIM(name,type)
#define YH_ARG_PRIM(name,type)
#define YH_FLD_STR(name,dim)
#define YH_FLDI_STR(name,dim)
#define YH_ARG_STR(name, dim)
#define YH_FLD_CSTR(name)
#define YH_FLDI_CSTR(name)
#define YH_ARG_CSTR(name)
#define YH_FLD_PTR(name,type)
#define YH_FLD_PTRA(name,type)
#define YH_FLD_PTRN(name,type)
#define YH_FLD_PTRF(name,type,func)
#define YH_FLDI_PTR(name,type)
#define YH_ARG_PTR(name, type)
#define YH_FLD_OBJ(name)
#define YH_FLD_VAR_START(selector,item)
#define YH_VAR_CASE(acase,type,item)
#define YH_FLD_VAR_END(selector,item)
#define YH_BUILD_CODE(code,type)
#define YH_BUILD_CODE_TAGGED(code,type)
#define YH_DEF_END(type)

#else
#if defined(YHH_ENUM_STRINGS)

#define YH_LIST_DEF(type)
#define YH_DEF_START(type)
#define YH_DEF_START_TAGGED(type)
#define YH_TAG_FIELD(type)
#define YH_FLD_ENUM_START(name)         char *ESTR(name)[] = {
#define YH_FLD_ENUM_CASE(en)                #en,
#define YH_FLD_ENUM_END(name)           NULL };
#define YH_FLD_PRIM(name,type)
#define YH_FLD_PRIMA(name,type)
#define YH_FLDI_PRIM(name,type)
#define YH_ARG_PRIM(name,type)
#define YH_FLD_STR(name,dim)
#define YH_FLDI_STR(name,dim)
#define YH_ARG_STR(name, dim)
#define YH_FLD_CSTR(name)
#define YH_FLDI_CSTR(name)
#define YH_ARG_CSTR(name)
#define YH_FLD_PTR(name,type)
#define YH_FLD_PTRA(name,type)
#define YH_FLD_PTRN(name,type)
#define YH_FLD_PTRF(name,type,func)
#define YH_FLDI_PTR(name,type)
#define YH_ARG_PTR(name, type)
#define YH_FLD_OBJ(name)
#define YH_FLD_VAR_START(selector,item) char *ESTR(selector)[] = {
#define YH_VAR_CASE(acase,type,item)        #acase,
#define YH_FLD_VAR_END(selector,item)   NULL };
#define YH_BUILD_CODE(code,type)
#define YH_BUILD_CODE_TAGGED(code,type)
#define YH_DEF_END(type)

#else
#if defined(YHH_UNION)

#define YH_LIST_DEF(type)
#define YH_DEF_START(type)
#define YH_DEF_START_TAGGED(type)
#define YH_TAG_FIELD(type)
#define YH_FLD_ENUM_START(name)
#define YH_FLD_ENUM_CASE(en)
#define YH_FLD_ENUM_END(name)
#define YH_FLD_PRIM(name,type)
#define YH_FLD_PRIMA(name,type)
#define YH_FLDI_PRIM(name,type)
#define YH_ARG_PRIM(name,type)
#define YH_FLD_STR(name,dim)
#define YH_FLDI_STR(name,dim)
#define YH_ARG_STR(name, dim)
#define YH_FLD_CSTR(name)
#define YH_FLDI_CSTR(name)
#define YH_ARG_CSTR(name)
#define YH_FLD_PTR(name,type)
#define YH_FLD_PTRA(name,type)
#define YH_FLD_PTRN(name,type)
#define YH_FLD_PTRF(name,type,func)
#define YH_FLDI_PTR(name,type)
#define YH_ARG_PTR(name, type)
#define YH_FLD_OBJ(name)
#define YH_FLD_VAR_START(selector,item) union union_YH_##item {
#define YH_VAR_CASE(acase,type,item)        YT(type) eu_##acase;
#define YH_FLD_VAR_END(selector,item)   };
#define YH_BUILD_CODE(code,type)
#define YH_BUILD_CODE_TAGGED(code,type)
#define YH_DEF_END(type)

#else
#if defined(YHH_UNIONDEF)

#define YH_LIST_DEF(type)
#define YH_DEF_START(type)
#define YH_DEF_START_TAGGED(type)
#define YH_TAG_FIELD(type)
#define YH_FLD_ENUM_START(name)
#define YH_FLD_ENUM_CASE(en)
#define YH_FLD_ENUM_END(name)
#define YH_FLD_PRIM(name,type)
#define YH_FLD_PRIMA(name,type)
#define YH_FLDI_PRIM(name,type)
#define YH_ARG_PRIM(name,type)
#define YH_FLD_STR(name,dim)
#define YH_FLDI_STR(name,dim)
#define YH_ARG_STR(name, dim)
#define YH_FLD_CSTR(name)
#define YH_FLDI_CSTR(name)
#define YH_ARG_CSTR(name)
#define YH_FLD_PTR(name,type)
#define YH_FLD_PTRA(name,type)
#define YH_FLD_PTRN(name,type)
#define YH_FLD_PTRF(name,type,func)
#define YH_FLDI_PTR(name,type)
#define YH_ARG_PTR(name, type)
#define YH_FLD_OBJ(name)
#define YH_FLD_VAR_START(selector,item) typedef union union_YH_##item YUT(item);
#define YH_VAR_CASE(acase,type,item)
#define YH_FLD_VAR_END(selector,item)
#define YH_BUILD_CODE(code,type)
#define YH_BUILD_CODE_TAGGED(code,type)
#define YH_DEF_END(type)

#else
#if defined(YHH_BUILD_PROTO)

#define YH_LIST_DEF(type)       YT(type##List) *YH_build_##type##List(  \
                                    YT(type) *type,                     \
                                    YT(type##List) *next);              \
                                YT(type##List) *YH_append_##type##List( \
                                    YT(type##List) *list1,              \
                                    YT(type##List) *list2);
#define YH_DEF_START(type)      YT(type) *YH_build_##type(
#define YH_DEF_START_TAGGED(type) YH_DEF_START(type)
#define YH_TAG_FIELD(type)
#define YH_FLD_ENUM_START(name)     word name,
#define YH_FLD_ENUM_CASE(en)
#define YH_FLD_ENUM_END(name)
#define YH_FLD_PRIM(name,type)      type name,
#define YH_FLD_PRIMA(name,type)     type *name,
#define YH_FLDI_PRIM(name,type)
#define YH_ARG_PRIM(name,type)      type name,
#define YH_FLD_STR(name,dim)        char name[dim],
#define YH_FLDI_STR(name,dim)
#define YH_ARG_STR(name, dim)       char name[dim],
#define YH_FLD_CSTR(name)           char *name,
#define YH_FLDI_CSTR(name)
#define YH_ARG_CSTR(name)           char *name,
#define YH_FLD_PTR(name,type)       YT(type) *name,
#define YH_FLD_PTRA(name,type)      YT(type) **name,
#define YH_FLD_PTRN(name,type)      YT(type) *name,
#define YH_FLD_PTRF(name,type,func) YT(type) *name,
#define YH_FLDI_PTR(name,type)
#define YH_ARG_PTR(name, type)      YT(type) *name,
#define YH_FLD_OBJ(name)            void *name,
#define YH_FLD_VAR_START(selector,item) word selector, \
                                        YUT(item) *item,
#define YH_VAR_CASE(acase,type,item)
#define YH_FLD_VAR_END(selector,item)
#define YH_BUILD_CODE(code,type)
#define YH_BUILD_CODE_TAGGED(code,type)
#define YH_DEF_END(type)        ...);

#else
#if defined(YHH_BUILD_FUNC)

#define YH_LIST_DEF(type)       YT(type##List) *YH_build_##type##List(        \
                                    YT(type) *type,                           \
                                    YT(type##List) *list)                     \
                                {                                             \
                                    typedef struct {                          \
                                        YT(type##List)  elem;                 \
                                        YT(type##List) *last;                 \
                                    } t_head;                                 \
                                    if (list == NULL) {                       \
                                        t_head *result_ = TypeAlloc(t_head);  \
                                        result_->elem.type = type;            \
                                        result_->elem.next = NULL;            \
                                        list = YC(type##List,result_);        \
                                        result_->last = list;                 \
                                    } else {                                  \
                                        t_head *head_ = (t_head *)list;       \
                                        YT(type##List) *result_ =             \
                                            TypeAlloc(YT(type##List));        \
                                        result_->type = type;                 \
                                        result_->next = NULL;                 \
                                        head_->last->next = result_;          \
                                        head_->last = result_;                \
                                    }                                         \
                                    return(list);                             \
                                }                                             \
                                YT(type##List) *YH_append_##type##List(       \
                                    YT(type##List) *list1,                    \
                                    YT(type##List) *list2)                    \
                                {                                             \
                                    typedef struct {                          \
                                        YT(type##List)  elem;                 \
                                        YT(type##List) *last;                 \
                                    } t_head;                                 \
                                    if (list1 == NULL) {                      \
                                        return(list2);                        \
                                    } else if (list2 == NULL) {               \
                                        return(list1);                        \
                                    } else {                                  \
                                        t_head *head1_ = (t_head *)list1;     \
                                        t_head *head2_ = (t_head *)list2;     \
                                        head1_->last->next = list2;           \
                                        head1_->last = head2_->last;          \
                                        return(list1);                        \
                                    }                                         \
                                }
#define YH_DEF_START(type)      YT(type) *YH_build_##type(
#define YH_DEF_START_TAGGED(type) YH_DEF_START(type)
#define YH_TAG_FIELD(type)
#define YH_FLD_ENUM_START(name)     word name,
#define YH_FLD_ENUM_CASE(en)
#define YH_FLD_ENUM_END(name)
#define YH_FLD_PRIM(name,type)      type name,
#define YH_FLD_PRIMA(name,type)     type *name,
#define YH_FLDI_PRIM(name,type)
#define YH_ARG_PRIM(name,type)      type name,
#define YH_FLD_STR(name,dim)        char name[dim],
#define YH_FLDI_STR(name,dim)
#define YH_ARG_STR(name, dim)       char name[dim],
#define YH_FLD_CSTR(name)           char *name,
#define YH_FLDI_CSTR(name)
#define YH_ARG_CSTR(name)           char *name,
#define YH_FLD_PTR(name,type)       YT(type) *name,
#define YH_FLD_PTRA(name,type)      YT(type) **name,
#define YH_FLD_PTRN(name,type)      YT(type) *name,
#define YH_FLD_PTRF(name,type,func) YT(type) *name,
#define YH_FLDI_PTR(name,type)
#define YH_ARG_PTR(name, type)      YT(type) *name,
#define YH_FLD_OBJ(name)            void *name,
#define YH_FLD_VAR_START(selector,item) word selector, \
                                        YUT(item) *item,
#define YH_VAR_CASE(acase,type,item)
#define YH_FLD_VAR_END(selector,item)
#define YH_BUILD_CODE(code,type)    ...) { YT(type) *result_ =               \
                                               TypeAlloc(YT(type)); code
#define YH_BUILD_CODE_TAGGED(code,type)    ...) { YT(type) *result_ =        \
                                               TypeAlloc(YT(type));          \
                                               result_->_yh_tag = YTAG(type);\
                                               code
#define YH_DEF_END(type)        return(result_); }

#else
#if defined(YHH_FREE_PROTO)

#define YH_LIST_DEF(type)       void YH_free_##type##List(      \
                                    YT(type##List) *thing);
#define YH_DEF_START(type)      void YH_free_##type(YT(type) *thing);
#define YH_DEF_START_TAGGED(type) YH_DEF_START(type)
#define YH_TAG_FIELD(type)
#define YH_FLD_ENUM_START(name)
#define YH_FLD_ENUM_CASE(en)
#define YH_FLD_ENUM_END(name)
#define YH_FLD_PRIM(name,type)
#define YH_FLD_PRIMA(name,type)
#define YH_FLDI_PRIM(name,type)
#define YH_ARG_PRIM(name,type) 
#define YH_FLD_STR(name,dim)
#define YH_FLDI_STR(name,dim)
#define YH_ARG_STR(name, dim)
#define YH_FLD_CSTR(name)
#define YH_FLDI_CSTR(name)
#define YH_ARG_CSTR(name)
#define YH_FLD_PTR(name,type)
#define YH_FLD_PTRA(name,type)
#define YH_FLD_PTRN(name,type)
#define YH_FLD_PTRF(name,type,func)
#define YH_FLDI_PTR(name,type)
#define YH_ARG_PTR(name, type)
#define YH_FLD_OBJ(name)
#define YH_FLD_VAR_START(selector,item)
#define YH_VAR_CASE(acase,type,item)
#define YH_FLD_VAR_END(selector,item)
#define YH_BUILD_CODE(code,type)
#define YH_BUILD_CODE_TAGGED(code,type)
#define YH_DEF_END(type)

#else
#if defined(YHH_FREE_FUNC)

#define YH_LIST_DEF(type)       void YH_free_##type##List(              \
                                    YT(type##List) *thing)              \
                                {                                       \
                                    YH_free_##type(thing->type);        \
                                    YH_free_##type##List(thing->next);  \
                                    FREE(thing);                        \
                                }
#define YH_DEF_START(type)      void YH_free_##type(YT(type) *thing) {  \
                                    if (thing) {
#define YH_DEF_START_TAGGED(type) YH_DEF_START(type)
#define YH_TAG_FIELD(type)
#define YH_FLD_ENUM_START(name)
#define YH_FLD_ENUM_CASE(en)
#define YH_FLD_ENUM_END(name)
#define YH_FLD_PRIM(name,type)
#define YH_FLD_PRIMA(name,type) FREE(thing->name);
#define YH_FLDI_PRIM(name,type)
#define YH_ARG_PRIM(name,type) 
#define YH_FLD_STR(name,dim)
#define YH_FLDI_STR(name,dim)
#define YH_ARG_STR(name, dim)
#define YH_FLD_CSTR(name)       FREE(thing->name);
#define YH_FLDI_CSTR(name)      FREE(thing->name);
#define YH_ARG_CSTR(name)
#define YH_FLD_PTR(name,type)   YH_free_##type(thing->name);
#define YH_FLD_PTRA(name,type)  FREE(thing->name);
#define YH_FLD_PTRN(name,type)  YH_FLD_PTR(name,type)
#define YH_FLD_PTRF(name,type,func) YH_FLD_PTR(name,type)
#define YH_FLDI_PTR(name,type)  YH_free_##type(thing->name);
#define YH_ARG_PTR(name, type)
#define YH_FLD_OBJ(name)
#define YH_FLD_VAR_START(selector,item) switch(thing->selector) {
#define YH_VAR_CASE(acase,type,item) case acase:                              \
                                         YH_free_##type(YC(type,thing->item));\
                                         break;
#define YH_FLD_VAR_END(selector,item)   }
#define YH_BUILD_CODE(code,type)
#define YH_BUILD_CODE_TAGGED(code,type)
#define YH_DEF_END(type)        FREE(thing); } }

#else
#if defined(YHH_DUMP_PROTO)

#define YH_LIST_DEF(type)       void YH_dump_##type##List(               \
                                    YT(type##List) *thing, int tabLevel);
#define YH_DEF_START(type)      void YH_dump_##type(YT(type) *thing,     \
                                                    int tabLevel);
#define YH_DEF_START_TAGGED(type) YH_DEF_START(type)
#define YH_TAG_FIELD(type)
#define YH_FLD_ENUM_START(name)
#define YH_FLD_ENUM_CASE(en)
#define YH_FLD_ENUM_END(name)
#define YH_FLD_PRIM(name,type)
#define YH_FLD_PRIMA(name,type)
#define YH_FLDI_PRIM(name,type)
#define YH_ARG_PRIM(name,type) 
#define YH_FLD_STR(name,dim)
#define YH_FLDI_STR(name,dim)
#define YH_ARG_STR(name, dim)
#define YH_FLD_CSTR(name)
#define YH_FLDI_CSTR(name)
#define YH_ARG_CSTR(name)
#define YH_FLD_PTR(name,type)
#define YH_FLD_PTRA(name,type)
#define YH_FLD_PTRN(name,type)
#define YH_FLD_PTRF(name,type,func)
#define YH_FLDI_PTR(name,type)
#define YH_ARG_PTR(name, type)
#define YH_FLD_OBJ(name)
#define YH_FLD_VAR_START(selector,item)
#define YH_VAR_CASE(acase,type,item)
#define YH_FLD_VAR_END(selector,item)
#define YH_BUILD_CODE(code,type)
#define YH_BUILD_CODE_TAGGED(code,type)
#define YH_DEF_END(type) 

#else
#if defined(YHH_DUMP_FUNC)

#if !defined(YH_DEBUG)
#define YH_LIST_DEF(type)       void YH_dump_##type##List(                    \
                                    YT(type##List) *arg, int tabLevel)        \
                                {                                             \
                                    int elemCount = 0;                        \
                                    if (arg) {                                \
                                        printf("YH_" #type "List {\n");       \
                                        while (arg) {                         \
                                            yh_tab(tabLevel);                 \
                                            printf(#type "[%d]: ",            \
                                                   elemCount++);              \
                                            YH_dump_##type(arg->type,         \
                                                           tabLevel+1);       \
                                            arg = arg->next;                  \
                                        }                                     \
                                        yh_tab(tabLevel-1);                   \
                                        printf("}\n");                        \
                                    } else {                                  \
                                        printf("YH_" #type "List { }\n");     \
                                    }                                         \
                                }
#else
#define YH_LIST_DEF(type)       void YH_dump_##type##List(                    \
                                    YT(type##List) *arg, int tabLevel)        \
                                {                                             \
                                    int elemCount = 0;                        \
                                    printf("<%p> YH_" #type "List {\n",arg);\
                                    while (arg) {                             \
                                        yh_tab(tabLevel);                     \
                                        printf("<%p>" #type "[%d]: ",arg,elemCount++);   \
                                        YH_dump_##type(arg->type, tabLevel+1);\
                                        arg = arg->next;                      \
                                    }                                         \
                                    yh_tab(tabLevel-1);                       \
                                    printf("}\n");                            \
                                }
#endif
#if !defined(YH_DEBUG)
#define YH_DEF_START(type)      void YH_dump_##type(YT(type) *arg,            \
                                                    int tabLevel)             \
                                {                                             \
                                    if (arg) {                                \
                                        printf("YH_" #type " {\n");
#define YH_DEF_START_TAGGED(type) void YH_dump_##type(YT(type) *arg,          \
                                                    int tabLevel)             \
                                {                                             \
                                    if (arg) {                                \
                                        if (arg->_yh_tag != YTAG(type)) {     \
                                            YH_dump_tag_dispatch((void *)arg, \
                                                                 tabLevel);   \
                                            return;                           \
                                        }                                     \
                                        printf("YH_" #type " {\n");
#else
#define YH_DEF_START(type)      void YH_dump_##type(YT(type) *arg,            \
                                                    int tabLevel)             \
                                {                                             \
                                    if (arg) {                                \
                                        printf("<%p> YH_" #type " {\n", arg);
#define YH_DEF_START_TAGGED(type) void YH_dump_##type(YT(type) *arg,          \
                                                    int tabLevel)             \
                                {                                             \
                                    if (arg) {                                \
                                        if (arg->_yh_tag != YTAG(type)) {     \
                                            YH_dump_tag_dispatch((void *)arg, \
                                                                 tabLevel);   \
                                            return;                           \
                                        }                                     \
                                        printf("<%p> YH_" #type " {\n", arg);
#endif
#define YH_TAG_FIELD(type)
#define YH_FLD_ENUM_START(name)     yh_tab(tabLevel);                         \
                                    printf(#name ": <%s>\n",                  \
                                           ESTR(name)[arg->name]);
#define YH_FLD_ENUM_CASE(en)
#define YH_FLD_ENUM_END(name)
#define YH_FLD_PRIM(name,type)      yh_tab(tabLevel);                         \
                                    printf(#name ": %ld\n", (long)arg->name);
#define YH_FLD_PRIMA(name,type)     yh_tab(tabLevel);                         \
                                    printf(#name ": " #type "[] <%p>\n",      \
                                           arg->name);
#define YH_FLDI_PRIM(name,type)     yh_tab(tabLevel);                         \
                                    printf(#name ": %ld\n", (long)arg->name);
#define YH_ARG_PRIM(name,type)
#define YH_FLD_STR(name,dim)        yh_tab(tabLevel);                         \
                                    printf(#name ": '%s'\n", arg->name);
#define YH_FLDI_STR(name,dim)       yh_tab(tabLevel);                         \
                                    printf(#name ": '%s'\n", arg->name);
#define YH_ARG_STR(name, dim)
#define YH_FLD_CSTR(name)           yh_tab(tabLevel);                         \
                                    printf(#name ": '%s'\n", arg->name);
#define YH_FLDI_CSTR(name)          yh_tab(tabLevel);                         \
                                    printf(#name ": '%s'\n", arg->name);
#define YH_ARG_CSTR(name)
#define YH_FLD_PTR(name,type)       yh_tab(tabLevel);                         \
                                    printf(#name ": ");                       \
                                    YH_dump_##type(arg->name, tabLevel + 1);
#define YH_FLD_PTRA(name,type)      yh_tab(tabLevel);                         \
                                    printf(#name ": YH_" #type "[] <%p>\n", \
                                           arg->name);
#define YH_FLD_PTRN(name,type)      yh_tab(tabLevel);                         \
                                    printf(#name ": YH_" #type " <%p>\n",   \
                                           arg->name);
#define YH_FLD_PTRF(name,type,func) yh_tab(tabLevel);                         \
                                    printf(#name ": ");                       \
                                    func(arg, arg->name, tabLevel + 1);
#define YH_FLDI_PTR(name,type)      yh_tab(tabLevel);                         \
                                    printf(#name ": ");                       \
                                    YH_dump_##type(arg->name, tabLevel + 1);
#define YH_ARG_PTR(name, type)
#define YH_FLD_OBJ(name)            yh_tab(tabLevel);                         \
                                    printf(#name ": obj <%p>\n",            \
                                           arg->name);
#define YH_FLD_VAR_START(selector,item)                                       \
                                 yh_tab(tabLevel);                            \
                                 printf(#selector "<%s>: ",                   \
                                        ESTR(selector)[arg->selector]);       \
                                 switch(arg->selector) {
#define YH_VAR_CASE(acase,type,item)  case acase:                             \
                                          YH_dump_##type(YC(type,arg->item),  \
                                                         tabLevel + 1);       \
                                          break;
#define YH_FLD_VAR_END(selector,item)                                         \
                                 }
#define YH_BUILD_CODE(code,type)
#define YH_BUILD_CODE_TAGGED(code,type)
#define YH_DEF_END(type)                yh_tab(--tabLevel);                   \
                                        printf("}\n");                        \
                                    } else {                                  \
                                        printf("YH_" #type " (NULL)\n");      \
                                    }                                         \
                                }

#else
#if defined(YHH_WALK_PROTO)

#define YH_LIST_DEF(type)       YT(type##List) *YH_walk_##type##List(         \
                                    YT(type##List) *thing,                    \
                                    bool yh_walk_func(void **arg, void *env), \
                                    void *yh_walk_env);
#define YH_DEF_START(type)      YT(type) *YH_walk_##type(                     \
                                    YT(type) *thing,                          \
                                    bool yh_walk_func(void **arg, void *env), \
                                    void *yh_walk_env);
#define YH_DEF_START_TAGGED(type) YH_DEF_START(type)
#define YH_TAG_FIELD(type)
#define YH_FLD_ENUM_START(name)
#define YH_FLD_ENUM_CASE(en)
#define YH_FLD_ENUM_END(name)
#define YH_FLD_PRIM(name,type)
#define YH_FLD_PRIMA(name,type)
#define YH_FLDI_PRIM(name,type)
#define YH_ARG_PRIM(name,type) 
#define YH_FLD_STR(name,dim)
#define YH_FLDI_STR(name,dim)
#define YH_ARG_STR(name, dim)
#define YH_FLD_CSTR(name)
#define YH_FLDI_CSTR(name)
#define YH_ARG_CSTR(name)
#define YH_FLD_PTR(name,type)
#define YH_FLD_PTRA(name,type)
#define YH_FLD_PTRN(name,type)
#define YH_FLD_PTRF(name,type,func)
#define YH_FLDI_PTR(name,type)
#define YH_ARG_PTR(name, type)
#define YH_FLD_OBJ(name)
#define YH_FLD_VAR_START(selector,item)
#define YH_VAR_CASE(acase,type,item)
#define YH_FLD_VAR_END(selector,item)
#define YH_BUILD_CODE(code,type)
#define YH_BUILD_CODE_TAGGED(code,type)
#define YH_DEF_END(type) 

#else
#if defined(YHH_WALK_TAG_FUNC)

#define YH_LIST_DEF(type)
#define YH_DEF_START(type)
#define YH_DEF_START_TAGGED(type) case YTAG(type):                            \
                                      return((void *)YH_WALKI(type,arg));
#define YH_TAG_FIELD(type)
#define YH_FLD_ENUM_START(name)
#define YH_FLD_ENUM_CASE(en)
#define YH_FLD_ENUM_END(name)
#define YH_FLD_PRIM(name,type)
#define YH_FLD_PRIMA(name,type)
#define YH_FLDI_PRIM(name,type)
#define YH_ARG_PRIM(name,type)
#define YH_FLD_STR(name,dim)
#define YH_FLDI_STR(name,dim)
#define YH_ARG_STR(name, dim)
#define YH_FLD_CSTR(name)
#define YH_FLDI_CSTR(name)
#define YH_ARG_CSTR(name)
#define YH_FLD_PTR(name,type)
#define YH_FLD_PTRA(name,type)
#define YH_FLD_PTRN(name,type)
#define YH_FLD_PTRF(name,type,func)
#define YH_FLDI_PTR(name,type)
#define YH_ARG_PTR(name, type)
#define YH_FLD_OBJ(name)
#define YH_FLD_VAR_START(selector,item)
#define YH_VAR_CASE(acase,type,item)
#define YH_FLD_VAR_END(selector,item)
#define YH_BUILD_CODE(code,type)
#define YH_BUILD_CODE_TAGGED(code,type)
#define YH_DEF_END(type)

#else
#if defined(YHH_WALK_FUNC)

#define YH_LIST_DEF(type)       YT(type##List) *YH_walk_##type##List(         \
                                    YT(type##List) *args,                     \
                                    bool yh_walk_func(void **arg, void *env), \
                                    void *yh_walk_env)                        \
                                {                                             \
                                    YT(type##List) *arg = args;               \
                                     while (arg) {                            \
                                        arg->type = YH_WALKI(type,arg->type); \
                                        arg = arg->next;                      \
                                    }                                         \
                                    return(args);                             \
                                }

#define YH_DEF_START(type)      YT(type) *YH_walk_##type(                     \
                                    YT(type) *arg,                            \
                                    bool yh_walk_func(void **arg, void *env), \
                                    void *yh_walk_env)                        \
                                {                                             \
                                    if (arg) {

#define YH_DEF_START_TAGGED(type) YT(type) *YH_walk_##type(                   \
                                      YT(type) *arg,                          \
                                      bool yh_walk_func(void **arg,void *env),\
                                      void *yh_walk_env)                      \
                                {                                             \
                                    if (arg) {                                \
                                        if (arg->_yh_tag != YTAG(type))       \
                                            return(YC(type,                   \
                                                YH_walk_tag_dispatch(         \
                                                    (void *)arg,              \
                                                    yh_walk_func,             \
                                                    yh_walk_env)));           \
                                        if (!yh_walk_func((void **)&arg,      \
                                                          yh_walk_env))       \
                                            return(arg);

#define YH_TAG_FIELD(type)
#define YH_FLD_ENUM_START(name)
#define YH_FLD_ENUM_CASE(en)
#define YH_FLD_ENUM_END(name)
#define YH_FLD_PRIM(name,type)
#define YH_FLD_PRIMA(name,type)
#define YH_FLDI_PRIM(name,type)
#define YH_ARG_PRIM(name,type)
#define YH_FLD_STR(name,dim)
#define YH_FLDI_STR(name,dim)
#define YH_ARG_STR(name, dim)
#define YH_FLD_CSTR(name)
#define YH_FLDI_CSTR(name)
#define YH_ARG_CSTR(name)
#define YH_FLD_PTR(name,type)       arg->name = YH_WALKI(type,arg->name);
#define YH_FLD_PTRA(name,type)
#define YH_FLD_PTRN(name,type)      arg->name = YH_WALKI(type,arg->name);
#define YH_FLD_PTRF(name,type,func) arg->name = YH_WALKI(type,arg->name);
#define YH_FLDI_PTR(name,type)      arg->name = YH_WALKI(type,arg->name);
#define YH_ARG_PTR(name, type)
#define YH_FLD_OBJ(name)
#define YH_FLD_VAR_START(selector,item)                                       \
                                    switch(arg->selector) {
#define YH_VAR_CASE(acase,type,item)  case acase:                             \
                                        arg->item =                           \
                               YUC(item,YH_WALKI(type,&arg->YUE(item,acase)));\
                                        break;
#define YH_FLD_VAR_END(selector,item)                                         \
                                    }
#define YH_BUILD_CODE(code,type)
#define YH_BUILD_CODE_TAGGED(code,type)
#define YH_DEF_END(type)            }                                         \
                                    return(arg);                              \
                                }

#endif
#endif
#endif
#endif
#endif
#endif
#endif
#endif
#endif
#endif
#endif
#endif
#endif
#endif
#endif
#endif
#endif
