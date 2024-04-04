/*
  yh.h -- Master header file for YaccHelper

  Copyright 1993,1994 by Chip Morningstar
  Permission granted to use freely (including commercial use), provided this
    copyright notice remains attached.
  For further information contact chip@netcom.com

*/

#ifndef YYSTYPE
#define YYSTYPE long
#endif

#define E(en)                   en,
#define YT(type)                t_YH_##type
#define YUT(type)               u_YH_##type
#define YTAG(type)              tag_YH_##type
#define YC(type,ptr)            ((YT(type) *)ptr)
#define YUC(type,ptr)           ((YUT(type) *)ptr)
#define YUE(fld,elem)           fld->eu_##elem
#define YUV(val,type)           &(val.YUE(value,type))
#define YUVP(val,type)          &(val->YUE(value,type))
#define ESTR(type)              estr_YH_##type
#define YH_BUILD(type)          (YYSTYPE)YH_build_##type
#define YBUILD(type)            YH_build_##type
#define YBUILDA(type,len)       TypeAllocMulti(YT(type), len)
#define YH_APPEND(type)         (YYSTYPE)YH_append_##type
#define YAPPEND(type)           YH_append_##type
#define YH_FREE(type,arg)       YH_free_##type(arg)
#define YH_DUMP(type,arg)       YH_dump_##type(arg, 1)
#define YH_WALK(type,arg,func,env) YH_walk_##type(arg,func,(void *)env)
#define YH_WALKI(type,arg)      YH_WALK(type,arg,yh_walk_func,yh_walk_env)
#define YH_ARG_FIELD(name)      result_->name = name;
#define YTAG_OF(ptr)            ((ptr)->_yh_tag)
#define YTAG_TEST(ptr,type)     (YTAG_OF(ptr) == YTAG(type))

#define YH_DEF(type,fields,init) \
YH_DEF_START(type)               \
  fields                         \
  YH_BUILD_CODE(init,type)       \
YH_DEF_END(type)

#define YH_DEF_TAGGED(type,fields,init) \
YH_DEF_START_TAGGED(type)         \
  YH_TAG_FIELD(type)              \
  fields                          \
  YH_BUILD_CODE_TAGGED(init,type) \
YH_DEF_END(type)

#define YH_FLD_ENUM(field,cases) \
YH_FLD_ENUM_START(field)         \
  cases                          \
YH_FLD_ENUM_END(field)

#define YH_FLD_VAR(selector,field,cases)  \
YH_FLD_VAR_START(selector,field)          \
  cases                                   \
YH_FLD_VAR_END(selector,field)

#define YH_FLD_LONG(name)  YH_FLD_PRIM(name,long)
#define YH_FLDI_LONG(name) YH_FLDI_PRIM(name,long)
#define YH_ARG_LONG(name)  YH_ARG_PRIM(name,long)
#define YH_FLD_WORD(name)  YH_FLD_PRIM(name,word)
#define YH_FLDI_WORD(name) YH_FLDI_PRIM(name,word
#define YH_ARG_WORD(name)  YH_ARG_PRIM(name,word)
#define YH_FLD_BYTE(name)  YH_FLD_PRIM(name,byte)
#define YH_FLDI_BYTE(name) YH_FLDI_PRIM(name,byte)
#define YH_ARG_BYTE(name)  YH_ARG_PRIM(name,byte)
#define YH_FLD_BOOL(name)  YH_FLD_PRIM(name,bool)
#define YH_FLDI_BOOL(name) YH_FLDI_PRIM(name,bool)
#define YH_ARG_BOOL(name)  YH_ARG_PRIM(name,bool)

#define YA_FUNC(name)           f_YA_##name
#define YA_FUNC_DEF(name)                                               \
    bool YA_FUNC(name)(void **_ya_arg, void *_ya_env)
#define YA_FUNC_START(name,envType)                                     \
    bool YA_FUNC(name)(void **_ya_arg, void *_ya_env) {                 \
        envType *env = (envType *)_ya_env;                              \
        yh_genericTagged *_ya_tagged = (yh_genericTagged *)*_ya_arg;    \
        switch (_ya_tagged->_yh_tag) {
#define YA_CASE(tag,code)                                               \
            case YTAG(tag): {                                           \
                YT(tag) *arg = YC(tag,*_ya_arg);                        \
                code                                                    \
                break;                                                  \
            }
#define YA_FUNC_END(name)                                               \
        }                                                               \
        return(TRUE);                                                   \
    }
#define YA_RETURN(var)                                                  \
    { *_ya_arg = (void *)(var);                                         \
    return(FALSE); }
#define YA_RETURN_CONT(var)                                             \
    { *_ya_arg = (void *)(var);                                         \
    return(TRUE); }

typedef void YT(any);
typedef char YT(string);
typedef char* YT(charp);
typedef float YT(float);
typedef long YT(long);
typedef bool YT(bool);
typedef char YT(char);

typedef struct {
    long _yh_tag;
} yh_genericTagged;

typedef struct yh_symbolStruct {
    int   refCount;
    char *name;
    struct yh_symbolStruct *next;
    void *binding;
} YT(symbol);

typedef struct {
    char *keyword;
    int   value;
} t_yh_keywordTable;

typedef struct struct_YH_genericList {
    void *elem;
    struct struct_YH_genericList *next;
} YT(genericList);

/* Lexical analysis routines */
int   yh_getc(void);
int   yh_ungetc(int c);
bool  yh_include(char *filename);
bool  yh_isCNumberStartChar(char c);
bool  yh_isCSymbolStartChar(char c);
bool  yh_isCSymbolChar(char c);
bool  yh_isSymbolStartChar(char c);
bool  yh_isWhitespace(char c);
int   yh_lexCCharacter(char c);
int   yh_lexCCharacterBuf(char c, char *buf);
bool  yh_lexCComment(char c);
bool  yh_lexCCommentBuf(char c, char *buf);
char  yh_lexCEscapeSequence(char c);
char  yh_lexCEscapeSequenceBuf(char c, char **bufptr, char *bufLimit);
int   yh_lexCNumber(char c);
int   yh_lexCNumberBuf(char c, char *buf);
int   yh_lexCSymbol(char c, t_yh_keywordTable *keywordTable);
int   yh_lexCString(char c);
int   yh_lexCStringBuf(char c, char *buf);
bool  yh_lexLiteral(char c, char *literals);
void *yh_parse(char *filename, char mode, char *tag);
bool  yh_popInput(void);
bool  yh_pushInput(char *filename, FILE *fyle);

extern bool (*yh_isSymbolChar)(char c);

/* Symbol table managment routines */
void        yh_bindSymbol(YT(symbol) *symbol, void *binding);
void        yh_deleteSymbol(YT(symbol) *symbol);
bool        yh_forEachSymbol(bool (*workfunc)(YT(symbol) *symbol, void *env),
                              void *env);
YT(symbol) *yh_handleSymbol(char *name);
longword    yh_hashString(char *s);
void        yh_initializeSymbolTable(void);
void        yh_installSymbol(YT(symbol) *symbol);
long        yh_listOfSymbols(YT(symbol) ***symbols);
YT(symbol) *yh_lookupSymbol(char *s);
YT(symbol) *yh_newSymbol(char *name, void *binding);
bool        yh_symbolWasNew(YT(symbol) *symbol);

/* Error message display routines */
void yh_error(char *format, ...);
void yh_syserror(char *format, ...);
void yh_warning(char *format, ...);

/* Utility routines */
int yh_countList(YT(genericList *)list);
#define YCOUNT(l) yh_countList(YC(genericList,l))

#define YRESULT(val) (yh_result = (YYSTYPE)(val))

void yh_tab(int count);

extern int yh_LineNumber;
extern YYSTYPE yh_result;
extern char *yh_FileName;
extern int yh_ErrorCount;
extern int yh_WarningCount;

