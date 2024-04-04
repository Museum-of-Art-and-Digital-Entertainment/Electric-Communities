/* A Bison parser, made by GNU Bison 3.8.2.  */

/* Bison implementation for Yacc-like parsers in C

   Copyright (C) 1984, 1989-1990, 2000-2015, 2018-2021 Free Software Foundation,
   Inc.

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <https://www.gnu.org/licenses/>.  */

/* As a special exception, you may create a larger work that contains
   part or all of the Bison parser skeleton and distribute that work
   under terms of your choice, so long as that work isn't itself a
   parser generator using the skeleton or a modified version thereof
   as a parser skeleton.  Alternatively, if you modify or redistribute
   the parser skeleton itself, you may (at your option) remove this
   special exception, which will cause the skeleton and the resulting
   Bison output files to be licensed under the GNU General Public
   License without this special exception.

   This special exception was added by the Free Software Foundation in
   version 2.2 of Bison.  */

/* C LALR(1) parser skeleton written by Richard Stallman, by
   simplifying the original so-called "semantic" parser.  */

/* DO NOT RELY ON FEATURES THAT ARE NOT DOCUMENTED in the manual,
   especially those whose name start with YY_ or yy_.  They are
   private implementation details that can be changed or removed.  */

/* All symbols defined below should begin with yy or YY, to avoid
   infringing on user name space.  This should be done even for local
   variables, as they might otherwise be expanded by user macros.
   There are some unavoidable exceptions within include files to
   define necessary library symbols; they are noted "INFRINGES ON
   USER NAME SPACE" below.  */

/* Identify Bison output, and Bison version.  */
#define YYBISON 30802

/* Bison version string.  */
#define YYBISON_VERSION "3.8.2"

/* Skeleton name.  */
#define YYSKELETON_NAME "yacc.c"

/* Pure parsers.  */
#define YYPURE 0

/* Push parsers.  */
#define YYPUSH 0

/* Pull parsers.  */
#define YYPULL 1




/* First part of user prologue.  */
#line 15 "pluribus.y"


#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "pl.h"

#define YYDEBUG 1

void yyerror(char *s);

#line 83 "y.tab.c"

# ifndef YY_CAST
#  ifdef __cplusplus
#   define YY_CAST(Type, Val) static_cast<Type> (Val)
#   define YY_REINTERPRET_CAST(Type, Val) reinterpret_cast<Type> (Val)
#  else
#   define YY_CAST(Type, Val) ((Type) (Val))
#   define YY_REINTERPRET_CAST(Type, Val) ((Type) (Val))
#  endif
# endif
# ifndef YY_NULLPTR
#  if defined __cplusplus
#   if 201103L <= __cplusplus
#    define YY_NULLPTR nullptr
#   else
#    define YY_NULLPTR 0
#   endif
#  else
#   define YY_NULLPTR ((void*)0)
#  endif
# endif

/* Use api.header.include to #include this header
   instead of duplicating it here.  */
#ifndef YY_YY_Y_TAB_H_INCLUDED
# define YY_YY_Y_TAB_H_INCLUDED
/* Debug traces.  */
#ifndef YYDEBUG
# define YYDEBUG 0
#endif
#if YYDEBUG
extern int yydebug;
#endif

/* Token kinds.  */
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
  enum yytokentype
  {
    YYEMPTY = -2,
    YYEOF = 0,                     /* "end of file"  */
    YYerror = 256,                 /* error  */
    YYUNDEF = 257,                 /* "invalid token"  */
    ABSTRACT = 258,                /* ABSTRACT  */
    ATTRIBUTE = 259,               /* ATTRIBUTE  */
    BOOLEAN = 260,                 /* BOOLEAN  */
    BYTE = 261,                    /* BYTE  */
    CASE = 262,                    /* CASE  */
    CHAR = 263,                    /* CHAR  */
    CLASS = 264,                   /* CLASS  */
    DATA = 265,                    /* DATA  */
    DEFAULT = 266,                 /* DEFAULT  */
    DELIVER = 267,                 /* DELIVER  */
    DOUBLE = 268,                  /* DOUBLE  */
    ECLASS = 269,                  /* ECLASS  */
    EINTERFACE = 270,              /* EINTERFACE  */
    ELEVATE = 271,                 /* ELEVATE  */
    ENUM = 272,                    /* ENUM  */
    EXPORT = 273,                  /* EXPORT  */
    EXTENDS = 274,                 /* EXTENDS  */
    FACET = 275,                   /* FACET  */
    FALSEX = 276,                  /* FALSEX  */
    FILL = 277,                    /* FILL  */
    FINAL = 278,                   /* FINAL  */
    FLOAT = 279,                   /* FLOAT  */
    FUNCTION = 280,                /* FUNCTION  */
    IMPLEMENTS = 281,              /* IMPLEMENTS  */
    IMPORT = 282,                  /* IMPORT  */
    IMPL = 283,                    /* IMPL  */
    INGREDIENT = 284,              /* INGREDIENT  */
    INIT = 285,                    /* INIT  */
    INT = 286,                     /* INT  */
    INTERFACE = 287,               /* INTERFACE  */
    KIND = 288,                    /* KIND  */
    LONG = 289,                    /* LONG  */
    MAKE = 290,                    /* MAKE  */
    MAKES = 291,                   /* MAKES  */
    MAP = 292,                     /* MAP  */
    METHOD = 293,                  /* METHOD  */
    NEIGHBOR = 294,                /* NEIGHBOR  */
    NONE = 295,                    /* NONE  */
    PACKAGE = 296,                 /* PACKAGE  */
    PNULL = 297,                   /* PNULL  */
    PRESENCE = 298,                /* PRESENCE  */
    PRESENCEBEHAVIOR = 299,        /* PRESENCEBEHAVIOR  */
    PRIME = 300,                   /* PRIME  */
    PRIVATE = 301,                 /* PRIVATE  */
    PROTECTED = 302,               /* PROTECTED  */
    PUBLIC = 303,                  /* PUBLIC  */
    PUBLISH = 304,                 /* PUBLISH  */
    REMOTE = 305,                  /* REMOTE  */
    REQUIRE = 306,                 /* REQUIRE  */
    ROLE = 307,                    /* ROLE  */
    SEQUENCE = 308,                /* SEQUENCE  */
    SHORT = 309,                   /* SHORT  */
    STATE = 310,                   /* STATE  */
    STATIC = 311,                  /* STATIC  */
    STRING = 312,                  /* STRING  */
    STRUCT = 313,                  /* STRUCT  */
    STRUCTURE = 314,               /* STRUCTURE  */
    SWITCH = 315,                  /* SWITCH  */
    TEMPLATE = 316,                /* TEMPLATE  */
    THROWS = 317,                  /* THROWS  */
    TO = 318,                      /* TO  */
    TRUEX = 319,                   /* TRUEX  */
    TYPEDEF = 320,                 /* TYPEDEF  */
    UNION = 321,                   /* UNION  */
    UNIT = 322,                    /* UNIT  */
    UNUM = 323,                    /* UNUM  */
    Character = 324,               /* Character  */
    CodeBlock = 325,               /* CodeBlock  */
    HexBlock = 326,                /* HexBlock  */
    Initialization = 327,          /* Initialization  */
    Number = 328,                  /* Number  */
    String = 329,                  /* String  */
    Symbol = 330,                  /* Symbol  */
    TagType = 331,                 /* TagType  */
    Or = 332,                      /* Or  */
    And = 333,                     /* And  */
    Neq = 334,                     /* Neq  */
    Eq = 335,                      /* Eq  */
    Geq = 336,                     /* Geq  */
    Leq = 337,                     /* Leq  */
    Lsl = 338,                     /* Lsl  */
    Lsr = 339,                     /* Lsr  */
    Asr = 340,                     /* Asr  */
    UMinus = 341,                  /* UMinus  */
    UPlus = 342                    /* UPlus  */
  };
  typedef enum yytokentype yytoken_kind_t;
#endif
/* Token kinds.  */
#define YYEMPTY -2
#define YYEOF 0
#define YYerror 256
#define YYUNDEF 257
#define ABSTRACT 258
#define ATTRIBUTE 259
#define BOOLEAN 260
#define BYTE 261
#define CASE 262
#define CHAR 263
#define CLASS 264
#define DATA 265
#define DEFAULT 266
#define DELIVER 267
#define DOUBLE 268
#define ECLASS 269
#define EINTERFACE 270
#define ELEVATE 271
#define ENUM 272
#define EXPORT 273
#define EXTENDS 274
#define FACET 275
#define FALSEX 276
#define FILL 277
#define FINAL 278
#define FLOAT 279
#define FUNCTION 280
#define IMPLEMENTS 281
#define IMPORT 282
#define IMPL 283
#define INGREDIENT 284
#define INIT 285
#define INT 286
#define INTERFACE 287
#define KIND 288
#define LONG 289
#define MAKE 290
#define MAKES 291
#define MAP 292
#define METHOD 293
#define NEIGHBOR 294
#define NONE 295
#define PACKAGE 296
#define PNULL 297
#define PRESENCE 298
#define PRESENCEBEHAVIOR 299
#define PRIME 300
#define PRIVATE 301
#define PROTECTED 302
#define PUBLIC 303
#define PUBLISH 304
#define REMOTE 305
#define REQUIRE 306
#define ROLE 307
#define SEQUENCE 308
#define SHORT 309
#define STATE 310
#define STATIC 311
#define STRING 312
#define STRUCT 313
#define STRUCTURE 314
#define SWITCH 315
#define TEMPLATE 316
#define THROWS 317
#define TO 318
#define TRUEX 319
#define TYPEDEF 320
#define UNION 321
#define UNIT 322
#define UNUM 323
#define Character 324
#define CodeBlock 325
#define HexBlock 326
#define Initialization 327
#define Number 328
#define String 329
#define Symbol 330
#define TagType 331
#define Or 332
#define And 333
#define Neq 334
#define Eq 335
#define Geq 336
#define Leq 337
#define Lsl 338
#define Lsr 339
#define Asr 340
#define UMinus 341
#define UPlus 342

/* Value type.  */
#if ! defined YYSTYPE && ! defined YYSTYPE_IS_DECLARED
typedef int YYSTYPE;
# define YYSTYPE_IS_TRIVIAL 1
# define YYSTYPE_IS_DECLARED 1
#endif


extern YYSTYPE yylval;


int yyparse (void);


#endif /* !YY_YY_Y_TAB_H_INCLUDED  */
/* Symbol kind.  */
enum yysymbol_kind_t
{
  YYSYMBOL_YYEMPTY = -2,
  YYSYMBOL_YYEOF = 0,                      /* "end of file"  */
  YYSYMBOL_YYerror = 1,                    /* error  */
  YYSYMBOL_YYUNDEF = 2,                    /* "invalid token"  */
  YYSYMBOL_ABSTRACT = 3,                   /* ABSTRACT  */
  YYSYMBOL_ATTRIBUTE = 4,                  /* ATTRIBUTE  */
  YYSYMBOL_BOOLEAN = 5,                    /* BOOLEAN  */
  YYSYMBOL_BYTE = 6,                       /* BYTE  */
  YYSYMBOL_CASE = 7,                       /* CASE  */
  YYSYMBOL_CHAR = 8,                       /* CHAR  */
  YYSYMBOL_CLASS = 9,                      /* CLASS  */
  YYSYMBOL_DATA = 10,                      /* DATA  */
  YYSYMBOL_DEFAULT = 11,                   /* DEFAULT  */
  YYSYMBOL_DELIVER = 12,                   /* DELIVER  */
  YYSYMBOL_DOUBLE = 13,                    /* DOUBLE  */
  YYSYMBOL_ECLASS = 14,                    /* ECLASS  */
  YYSYMBOL_EINTERFACE = 15,                /* EINTERFACE  */
  YYSYMBOL_ELEVATE = 16,                   /* ELEVATE  */
  YYSYMBOL_ENUM = 17,                      /* ENUM  */
  YYSYMBOL_EXPORT = 18,                    /* EXPORT  */
  YYSYMBOL_EXTENDS = 19,                   /* EXTENDS  */
  YYSYMBOL_FACET = 20,                     /* FACET  */
  YYSYMBOL_FALSEX = 21,                    /* FALSEX  */
  YYSYMBOL_FILL = 22,                      /* FILL  */
  YYSYMBOL_FINAL = 23,                     /* FINAL  */
  YYSYMBOL_FLOAT = 24,                     /* FLOAT  */
  YYSYMBOL_FUNCTION = 25,                  /* FUNCTION  */
  YYSYMBOL_IMPLEMENTS = 26,                /* IMPLEMENTS  */
  YYSYMBOL_IMPORT = 27,                    /* IMPORT  */
  YYSYMBOL_IMPL = 28,                      /* IMPL  */
  YYSYMBOL_INGREDIENT = 29,                /* INGREDIENT  */
  YYSYMBOL_INIT = 30,                      /* INIT  */
  YYSYMBOL_INT = 31,                       /* INT  */
  YYSYMBOL_INTERFACE = 32,                 /* INTERFACE  */
  YYSYMBOL_KIND = 33,                      /* KIND  */
  YYSYMBOL_LONG = 34,                      /* LONG  */
  YYSYMBOL_MAKE = 35,                      /* MAKE  */
  YYSYMBOL_MAKES = 36,                     /* MAKES  */
  YYSYMBOL_MAP = 37,                       /* MAP  */
  YYSYMBOL_METHOD = 38,                    /* METHOD  */
  YYSYMBOL_NEIGHBOR = 39,                  /* NEIGHBOR  */
  YYSYMBOL_NONE = 40,                      /* NONE  */
  YYSYMBOL_PACKAGE = 41,                   /* PACKAGE  */
  YYSYMBOL_PNULL = 42,                     /* PNULL  */
  YYSYMBOL_PRESENCE = 43,                  /* PRESENCE  */
  YYSYMBOL_PRESENCEBEHAVIOR = 44,          /* PRESENCEBEHAVIOR  */
  YYSYMBOL_PRIME = 45,                     /* PRIME  */
  YYSYMBOL_PRIVATE = 46,                   /* PRIVATE  */
  YYSYMBOL_PROTECTED = 47,                 /* PROTECTED  */
  YYSYMBOL_PUBLIC = 48,                    /* PUBLIC  */
  YYSYMBOL_PUBLISH = 49,                   /* PUBLISH  */
  YYSYMBOL_REMOTE = 50,                    /* REMOTE  */
  YYSYMBOL_REQUIRE = 51,                   /* REQUIRE  */
  YYSYMBOL_ROLE = 52,                      /* ROLE  */
  YYSYMBOL_SEQUENCE = 53,                  /* SEQUENCE  */
  YYSYMBOL_SHORT = 54,                     /* SHORT  */
  YYSYMBOL_STATE = 55,                     /* STATE  */
  YYSYMBOL_STATIC = 56,                    /* STATIC  */
  YYSYMBOL_STRING = 57,                    /* STRING  */
  YYSYMBOL_STRUCT = 58,                    /* STRUCT  */
  YYSYMBOL_STRUCTURE = 59,                 /* STRUCTURE  */
  YYSYMBOL_SWITCH = 60,                    /* SWITCH  */
  YYSYMBOL_TEMPLATE = 61,                  /* TEMPLATE  */
  YYSYMBOL_THROWS = 62,                    /* THROWS  */
  YYSYMBOL_TO = 63,                        /* TO  */
  YYSYMBOL_TRUEX = 64,                     /* TRUEX  */
  YYSYMBOL_TYPEDEF = 65,                   /* TYPEDEF  */
  YYSYMBOL_UNION = 66,                     /* UNION  */
  YYSYMBOL_UNIT = 67,                      /* UNIT  */
  YYSYMBOL_UNUM = 68,                      /* UNUM  */
  YYSYMBOL_Character = 69,                 /* Character  */
  YYSYMBOL_CodeBlock = 70,                 /* CodeBlock  */
  YYSYMBOL_HexBlock = 71,                  /* HexBlock  */
  YYSYMBOL_Initialization = 72,            /* Initialization  */
  YYSYMBOL_Number = 73,                    /* Number  */
  YYSYMBOL_String = 74,                    /* String  */
  YYSYMBOL_Symbol = 75,                    /* Symbol  */
  YYSYMBOL_TagType = 76,                   /* TagType  */
  YYSYMBOL_77_ = 77,                       /* '?'  */
  YYSYMBOL_78_ = 78,                       /* ':'  */
  YYSYMBOL_Or = 79,                        /* Or  */
  YYSYMBOL_And = 80,                       /* And  */
  YYSYMBOL_81_ = 81,                       /* '|'  */
  YYSYMBOL_82_ = 82,                       /* '^'  */
  YYSYMBOL_83_ = 83,                       /* '&'  */
  YYSYMBOL_Neq = 84,                       /* Neq  */
  YYSYMBOL_Eq = 85,                        /* Eq  */
  YYSYMBOL_86_ = 86,                       /* '>'  */
  YYSYMBOL_87_ = 87,                       /* '<'  */
  YYSYMBOL_Geq = 88,                       /* Geq  */
  YYSYMBOL_Leq = 89,                       /* Leq  */
  YYSYMBOL_Lsl = 90,                       /* Lsl  */
  YYSYMBOL_Lsr = 91,                       /* Lsr  */
  YYSYMBOL_Asr = 92,                       /* Asr  */
  YYSYMBOL_93_ = 93,                       /* '+'  */
  YYSYMBOL_94_ = 94,                       /* '-'  */
  YYSYMBOL_95_ = 95,                       /* '*'  */
  YYSYMBOL_96_ = 96,                       /* '/'  */
  YYSYMBOL_97_ = 97,                       /* '%'  */
  YYSYMBOL_98_ = 98,                       /* '!'  */
  YYSYMBOL_99_ = 99,                       /* '~'  */
  YYSYMBOL_UMinus = 100,                   /* UMinus  */
  YYSYMBOL_UPlus = 101,                    /* UPlus  */
  YYSYMBOL_102_ = 102,                     /* '{'  */
  YYSYMBOL_103_ = 103,                     /* '}'  */
  YYSYMBOL_104_ = 104,                     /* ';'  */
  YYSYMBOL_105_ = 105,                     /* '='  */
  YYSYMBOL_106_ = 106,                     /* ','  */
  YYSYMBOL_107_ = 107,                     /* '('  */
  YYSYMBOL_108_ = 108,                     /* ')'  */
  YYSYMBOL_109_ = 109,                     /* '['  */
  YYSYMBOL_110_ = 110,                     /* ']'  */
  YYSYMBOL_111_ = 111,                     /* '.'  */
  YYSYMBOL_YYACCEPT = 112,                 /* $accept  */
  YYSYMBOL_pluribusModule = 113,           /* pluribusModule  */
  YYSYMBOL_unitDef = 114,                  /* unitDef  */
  YYSYMBOL_unitUse = 115,                  /* unitUse  */
  YYSYMBOL_unitElems = 116,                /* unitElems  */
  YYSYMBOL_unitElemList = 117,             /* unitElemList  */
  YYSYMBOL_unitElem = 118,                 /* unitElem  */
  YYSYMBOL_definitionStatement = 119,      /* definitionStatement  */
  YYSYMBOL_attributeType = 120,            /* attributeType  */
  YYSYMBOL_attributeDef = 121,             /* attributeDef  */
  YYSYMBOL_assignment = 122,               /* assignment  */
  YYSYMBOL_requireAtt = 123,               /* requireAtt  */
  YYSYMBOL_codeAtt = 124,                  /* codeAtt  */
  YYSYMBOL_codeInherit = 125,              /* codeInherit  */
  YYSYMBOL_codeInheritList = 126,          /* codeInheritList  */
  YYSYMBOL_codeInherits = 127,             /* codeInherits  */
  YYSYMBOL_codeModifier = 128,             /* codeModifier  */
  YYSYMBOL_codeModifierList = 129,         /* codeModifierList  */
  YYSYMBOL_codeModifiers = 130,            /* codeModifiers  */
  YYSYMBOL_codeType = 131,                 /* codeType  */
  YYSYMBOL_mangledSymbolElem = 132,        /* mangledSymbolElem  */
  YYSYMBOL_mangledSymbolList = 133,        /* mangledSymbolList  */
  YYSYMBOL_prototypeDecl = 134,            /* prototypeDecl  */
  YYSYMBOL_parameterDeclList = 135,        /* parameterDeclList  */
  YYSYMBOL_arrayMarker = 136,              /* arrayMarker  */
  YYSYMBOL_arrayMarkers = 137,             /* arrayMarkers  */
  YYSYMBOL_parameterDecl = 138,            /* parameterDecl  */
  YYSYMBOL_kindDef = 139,                  /* kindDef  */
  YYSYMBOL_commaNameList = 140,            /* commaNameList  */
  YYSYMBOL_commaScopedRefList = 141,       /* commaScopedRefList  */
  YYSYMBOL_kindUse = 142,                  /* kindUse  */
  YYSYMBOL_kindUseNoSemi = 143,            /* kindUseNoSemi  */
  YYSYMBOL_kindElems = 144,                /* kindElems  */
  YYSYMBOL_kindElemList = 145,             /* kindElemList  */
  YYSYMBOL_kindElem = 146,                 /* kindElem  */
  YYSYMBOL_ingredientImplDef = 147,        /* ingredientImplDef  */
  YYSYMBOL_ingredientImplUse = 148,        /* ingredientImplUse  */
  YYSYMBOL_ingredientImplElems = 149,      /* ingredientImplElems  */
  YYSYMBOL_ingredientImplElemList = 150,   /* ingredientImplElemList  */
  YYSYMBOL_ingredientImplElem = 151,       /* ingredientImplElem  */
  YYSYMBOL_neighborAtt = 152,              /* neighborAtt  */
  YYSYMBOL_stateBundleAtt = 153,           /* stateBundleAtt  */
  YYSYMBOL_valueOrNot = 154,               /* valueOrNot  */
  YYSYMBOL_155_1 = 155,                    /* $@1  */
  YYSYMBOL_variableDecl = 156,             /* variableDecl  */
  YYSYMBOL_throwsList = 157,               /* throwsList  */
  YYSYMBOL_functionAtt = 158,              /* functionAtt  */
  YYSYMBOL_modifiersOrNot = 159,           /* modifiersOrNot  */
  YYSYMBOL_modifiers = 160,                /* modifiers  */
  YYSYMBOL_modifierList = 161,             /* modifierList  */
  YYSYMBOL_modifier = 162,                 /* modifier  */
  YYSYMBOL_initBlockAtt = 163,             /* initBlockAtt  */
  YYSYMBOL_methodAtt = 164,                /* methodAtt  */
  YYSYMBOL_methodCode = 165,               /* methodCode  */
  YYSYMBOL_166_2 = 166,                    /* $@2  */
  YYSYMBOL_dataAtt = 167,                  /* dataAtt  */
  YYSYMBOL_168_3 = 168,                    /* $@3  */
  YYSYMBOL_packageAtt = 169,               /* packageAtt  */
  YYSYMBOL_importAtt = 170,                /* importAtt  */
  YYSYMBOL_implementsAtt = 171,            /* implementsAtt  */
  YYSYMBOL_presenceStructureDef = 172,     /* presenceStructureDef  */
  YYSYMBOL_presenceStructureUse = 173,     /* presenceStructureUse  */
  YYSYMBOL_presenceStructureElems = 174,   /* presenceStructureElems  */
  YYSYMBOL_presenceStructureElemList = 175, /* presenceStructureElemList  */
  YYSYMBOL_presenceStructureElem = 176,    /* presenceStructureElem  */
  YYSYMBOL_ingredientAtt = 177,            /* ingredientAtt  */
  YYSYMBOL_ingredientAttElems = 178,       /* ingredientAttElems  */
  YYSYMBOL_ingredientAttElemList = 179,    /* ingredientAttElemList  */
  YYSYMBOL_ingredientAttElem = 180,        /* ingredientAttElem  */
  YYSYMBOL_deliverSym = 181,               /* deliverSym  */
  YYSYMBOL_deliverAtt = 182,               /* deliverAtt  */
  YYSYMBOL_scope = 183,                    /* scope  */
  YYSYMBOL_presenceImplDef = 184,          /* presenceImplDef  */
  YYSYMBOL_presenceImplUse = 185,          /* presenceImplUse  */
  YYSYMBOL_presenceImplElems = 186,        /* presenceImplElems  */
  YYSYMBOL_presenceImplElemList = 187,     /* presenceImplElemList  */
  YYSYMBOL_presenceImplElem = 188,         /* presenceImplElem  */
  YYSYMBOL_presenceBehavior = 189,         /* presenceBehavior  */
  YYSYMBOL_templateAtt = 190,              /* templateAtt  */
  YYSYMBOL_makeAtt = 191,                  /* makeAtt  */
  YYSYMBOL_templateDef = 192,              /* templateDef  */
  YYSYMBOL_templateElems = 193,            /* templateElems  */
  YYSYMBOL_templateElemList = 194,         /* templateElemList  */
  YYSYMBOL_templateElem = 195,             /* templateElem  */
  YYSYMBOL_mapScope = 196,                 /* mapScope  */
  YYSYMBOL_mapAtt = 197,                   /* mapAtt  */
  YYSYMBOL_exprList = 198,                 /* exprList  */
  YYSYMBOL_unumStructureDef = 199,         /* unumStructureDef  */
  YYSYMBOL_unumStructureUse = 200,         /* unumStructureUse  */
  YYSYMBOL_unumStructureElems = 201,       /* unumStructureElems  */
  YYSYMBOL_unumStructureElemList = 202,    /* unumStructureElemList  */
  YYSYMBOL_unumStructureElem = 203,        /* unumStructureElem  */
  YYSYMBOL_primeAtt = 204,                 /* primeAtt  */
  YYSYMBOL_presenceAtt = 205,              /* presenceAtt  */
  YYSYMBOL_presenceConds = 206,            /* presenceConds  */
  YYSYMBOL_presenceCondList = 207,         /* presenceCondList  */
  YYSYMBOL_presenceCond = 208,             /* presenceCond  */
  YYSYMBOL_makes = 209,                    /* makes  */
  YYSYMBOL_plurality = 210,                /* plurality  */
  YYSYMBOL_unumImplDef = 211,              /* unumImplDef  */
  YYSYMBOL_unumImplUse = 212,              /* unumImplUse  */
  YYSYMBOL_unumImplElems = 213,            /* unumImplElems  */
  YYSYMBOL_unumImplElemList = 214,         /* unumImplElemList  */
  YYSYMBOL_unumImplElem = 215,             /* unumImplElem  */
  YYSYMBOL_presenceImplAtt = 216,          /* presenceImplAtt  */
  YYSYMBOL_expr = 217,                     /* expr  */
  YYSYMBOL_exprA = 218,                    /* exprA  */
  YYSYMBOL_expr9 = 219,                    /* expr9  */
  YYSYMBOL_expr8 = 220,                    /* expr8  */
  YYSYMBOL_expr7 = 221,                    /* expr7  */
  YYSYMBOL_expr6 = 222,                    /* expr6  */
  YYSYMBOL_expr5 = 223,                    /* expr5  */
  YYSYMBOL_expr4 = 224,                    /* expr4  */
  YYSYMBOL_expr3 = 225,                    /* expr3  */
  YYSYMBOL_expr2 = 226,                    /* expr2  */
  YYSYMBOL_expr1 = 227,                    /* expr1  */
  YYSYMBOL_term = 228,                     /* term  */
  YYSYMBOL_prim = 229,                     /* prim  */
  YYSYMBOL_type = 230,                     /* type  */
  YYSYMBOL_remoteDef = 231,                /* remoteDef  */
  YYSYMBOL_publishDef = 232,               /* publishDef  */
  YYSYMBOL_defSymbol = 233,                /* defSymbol  */
  YYSYMBOL_directRef = 234,                /* directRef  */
  YYSYMBOL_scopedRef = 235,                /* scopedRef  */
  YYSYMBOL_outerScope = 236,               /* outerScope  */
  YYSYMBOL_typeDef = 237,                  /* typeDef  */
  YYSYMBOL_typeDeclarator = 238,           /* typeDeclarator  */
  YYSYMBOL_typeSpec = 239,                 /* typeSpec  */
  YYSYMBOL_simpleTypeSpec = 240,           /* simpleTypeSpec  */
  YYSYMBOL_baseTypeSpec = 241,             /* baseTypeSpec  */
  YYSYMBOL_templateTypeSpec = 242,         /* templateTypeSpec  */
  YYSYMBOL_constrTypeSpec = 243,           /* constrTypeSpec  */
  YYSYMBOL_declarators = 244,              /* declarators  */
  YYSYMBOL_declarator = 245,               /* declarator  */
  YYSYMBOL_simpleDeclarator = 246,         /* simpleDeclarator  */
  YYSYMBOL_complexDeclarator = 247,        /* complexDeclarator  */
  YYSYMBOL_floatingPtType = 248,           /* floatingPtType  */
  YYSYMBOL_integralType = 249,             /* integralType  */
  YYSYMBOL_charType = 250,                 /* charType  */
  YYSYMBOL_booleanType = 251,              /* booleanType  */
  YYSYMBOL_structType = 252,               /* structType  */
  YYSYMBOL_memberList = 253,               /* memberList  */
  YYSYMBOL_member = 254,                   /* member  */
  YYSYMBOL_unionType = 255,                /* unionType  */
  YYSYMBOL_switchTypeSpec = 256,           /* switchTypeSpec  */
  YYSYMBOL_switchBody = 257,               /* switchBody  */
  YYSYMBOL_case = 258,                     /* case  */
  YYSYMBOL_caseLabels = 259,               /* caseLabels  */
  YYSYMBOL_caseLabel = 260,                /* caseLabel  */
  YYSYMBOL_elementSpec = 261,              /* elementSpec  */
  YYSYMBOL_enumType = 262,                 /* enumType  */
  YYSYMBOL_sequenceType = 263,             /* sequenceType  */
  YYSYMBOL_stringType = 264,               /* stringType  */
  YYSYMBOL_arrayDeclarator = 265,          /* arrayDeclarator  */
  YYSYMBOL_fixedArraySizes = 266,          /* fixedArraySizes  */
  YYSYMBOL_fixedArraySize = 267            /* fixedArraySize  */
};
typedef enum yysymbol_kind_t yysymbol_kind_t;




#ifdef short
# undef short
#endif

/* On compilers that do not define __PTRDIFF_MAX__ etc., make sure
   <limits.h> and (if available) <stdint.h> are included
   so that the code can choose integer types of a good width.  */

#ifndef __PTRDIFF_MAX__
# include <limits.h> /* INFRINGES ON USER NAME SPACE */
# if defined __STDC_VERSION__ && 199901 <= __STDC_VERSION__
#  include <stdint.h> /* INFRINGES ON USER NAME SPACE */
#  define YY_STDINT_H
# endif
#endif

/* Narrow types that promote to a signed type and that can represent a
   signed or unsigned integer of at least N bits.  In tables they can
   save space and decrease cache pressure.  Promoting to a signed type
   helps avoid bugs in integer arithmetic.  */

#ifdef __INT_LEAST8_MAX__
typedef __INT_LEAST8_TYPE__ yytype_int8;
#elif defined YY_STDINT_H
typedef int_least8_t yytype_int8;
#else
typedef signed char yytype_int8;
#endif

#ifdef __INT_LEAST16_MAX__
typedef __INT_LEAST16_TYPE__ yytype_int16;
#elif defined YY_STDINT_H
typedef int_least16_t yytype_int16;
#else
typedef short yytype_int16;
#endif

/* Work around bug in HP-UX 11.23, which defines these macros
   incorrectly for preprocessor constants.  This workaround can likely
   be removed in 2023, as HPE has promised support for HP-UX 11.23
   (aka HP-UX 11i v2) only through the end of 2022; see Table 2 of
   <https://h20195.www2.hpe.com/V2/getpdf.aspx/4AA4-7673ENW.pdf>.  */
#ifdef __hpux
# undef UINT_LEAST8_MAX
# undef UINT_LEAST16_MAX
# define UINT_LEAST8_MAX 255
# define UINT_LEAST16_MAX 65535
#endif

#if defined __UINT_LEAST8_MAX__ && __UINT_LEAST8_MAX__ <= __INT_MAX__
typedef __UINT_LEAST8_TYPE__ yytype_uint8;
#elif (!defined __UINT_LEAST8_MAX__ && defined YY_STDINT_H \
       && UINT_LEAST8_MAX <= INT_MAX)
typedef uint_least8_t yytype_uint8;
#elif !defined __UINT_LEAST8_MAX__ && UCHAR_MAX <= INT_MAX
typedef unsigned char yytype_uint8;
#else
typedef short yytype_uint8;
#endif

#if defined __UINT_LEAST16_MAX__ && __UINT_LEAST16_MAX__ <= __INT_MAX__
typedef __UINT_LEAST16_TYPE__ yytype_uint16;
#elif (!defined __UINT_LEAST16_MAX__ && defined YY_STDINT_H \
       && UINT_LEAST16_MAX <= INT_MAX)
typedef uint_least16_t yytype_uint16;
#elif !defined __UINT_LEAST16_MAX__ && USHRT_MAX <= INT_MAX
typedef unsigned short yytype_uint16;
#else
typedef int yytype_uint16;
#endif

#ifndef YYPTRDIFF_T
# if defined __PTRDIFF_TYPE__ && defined __PTRDIFF_MAX__
#  define YYPTRDIFF_T __PTRDIFF_TYPE__
#  define YYPTRDIFF_MAXIMUM __PTRDIFF_MAX__
# elif defined PTRDIFF_MAX
#  ifndef ptrdiff_t
#   include <stddef.h> /* INFRINGES ON USER NAME SPACE */
#  endif
#  define YYPTRDIFF_T ptrdiff_t
#  define YYPTRDIFF_MAXIMUM PTRDIFF_MAX
# else
#  define YYPTRDIFF_T long
#  define YYPTRDIFF_MAXIMUM LONG_MAX
# endif
#endif

#ifndef YYSIZE_T
# ifdef __SIZE_TYPE__
#  define YYSIZE_T __SIZE_TYPE__
# elif defined size_t
#  define YYSIZE_T size_t
# elif defined __STDC_VERSION__ && 199901 <= __STDC_VERSION__
#  include <stddef.h> /* INFRINGES ON USER NAME SPACE */
#  define YYSIZE_T size_t
# else
#  define YYSIZE_T unsigned
# endif
#endif

#define YYSIZE_MAXIMUM                                  \
  YY_CAST (YYPTRDIFF_T,                                 \
           (YYPTRDIFF_MAXIMUM < YY_CAST (YYSIZE_T, -1)  \
            ? YYPTRDIFF_MAXIMUM                         \
            : YY_CAST (YYSIZE_T, -1)))

#define YYSIZEOF(X) YY_CAST (YYPTRDIFF_T, sizeof (X))


/* Stored state numbers (used for stacks). */
typedef yytype_int16 yy_state_t;

/* State numbers in computations.  */
typedef int yy_state_fast_t;

#ifndef YY_
# if defined YYENABLE_NLS && YYENABLE_NLS
#  if ENABLE_NLS
#   include <libintl.h> /* INFRINGES ON USER NAME SPACE */
#   define YY_(Msgid) dgettext ("bison-runtime", Msgid)
#  endif
# endif
# ifndef YY_
#  define YY_(Msgid) Msgid
# endif
#endif


#ifndef YY_ATTRIBUTE_PURE
# if defined __GNUC__ && 2 < __GNUC__ + (96 <= __GNUC_MINOR__)
#  define YY_ATTRIBUTE_PURE __attribute__ ((__pure__))
# else
#  define YY_ATTRIBUTE_PURE
# endif
#endif

#ifndef YY_ATTRIBUTE_UNUSED
# if defined __GNUC__ && 2 < __GNUC__ + (7 <= __GNUC_MINOR__)
#  define YY_ATTRIBUTE_UNUSED __attribute__ ((__unused__))
# else
#  define YY_ATTRIBUTE_UNUSED
# endif
#endif

/* Suppress unused-variable warnings by "using" E.  */
#if ! defined lint || defined __GNUC__
# define YY_USE(E) ((void) (E))
#else
# define YY_USE(E) /* empty */
#endif

/* Suppress an incorrect diagnostic about yylval being uninitialized.  */
#if defined __GNUC__ && ! defined __ICC && 406 <= __GNUC__ * 100 + __GNUC_MINOR__
# if __GNUC__ * 100 + __GNUC_MINOR__ < 407
#  define YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN                           \
    _Pragma ("GCC diagnostic push")                                     \
    _Pragma ("GCC diagnostic ignored \"-Wuninitialized\"")
# else
#  define YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN                           \
    _Pragma ("GCC diagnostic push")                                     \
    _Pragma ("GCC diagnostic ignored \"-Wuninitialized\"")              \
    _Pragma ("GCC diagnostic ignored \"-Wmaybe-uninitialized\"")
# endif
# define YY_IGNORE_MAYBE_UNINITIALIZED_END      \
    _Pragma ("GCC diagnostic pop")
#else
# define YY_INITIAL_VALUE(Value) Value
#endif
#ifndef YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
# define YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
# define YY_IGNORE_MAYBE_UNINITIALIZED_END
#endif
#ifndef YY_INITIAL_VALUE
# define YY_INITIAL_VALUE(Value) /* Nothing. */
#endif

#if defined __cplusplus && defined __GNUC__ && ! defined __ICC && 6 <= __GNUC__
# define YY_IGNORE_USELESS_CAST_BEGIN                          \
    _Pragma ("GCC diagnostic push")                            \
    _Pragma ("GCC diagnostic ignored \"-Wuseless-cast\"")
# define YY_IGNORE_USELESS_CAST_END            \
    _Pragma ("GCC diagnostic pop")
#endif
#ifndef YY_IGNORE_USELESS_CAST_BEGIN
# define YY_IGNORE_USELESS_CAST_BEGIN
# define YY_IGNORE_USELESS_CAST_END
#endif


#define YY_ASSERT(E) ((void) (0 && (E)))

#if !defined yyoverflow

/* The parser invokes alloca or malloc; define the necessary symbols.  */

# ifdef YYSTACK_USE_ALLOCA
#  if YYSTACK_USE_ALLOCA
#   ifdef __GNUC__
#    define YYSTACK_ALLOC __builtin_alloca
#   elif defined __BUILTIN_VA_ARG_INCR
#    include <alloca.h> /* INFRINGES ON USER NAME SPACE */
#   elif defined _AIX
#    define YYSTACK_ALLOC __alloca
#   elif defined _MSC_VER
#    include <malloc.h> /* INFRINGES ON USER NAME SPACE */
#    define alloca _alloca
#   else
#    define YYSTACK_ALLOC alloca
#    if ! defined _ALLOCA_H && ! defined EXIT_SUCCESS
#     include <stdlib.h> /* INFRINGES ON USER NAME SPACE */
      /* Use EXIT_SUCCESS as a witness for stdlib.h.  */
#     ifndef EXIT_SUCCESS
#      define EXIT_SUCCESS 0
#     endif
#    endif
#   endif
#  endif
# endif

# ifdef YYSTACK_ALLOC
   /* Pacify GCC's 'empty if-body' warning.  */
#  define YYSTACK_FREE(Ptr) do { /* empty */; } while (0)
#  ifndef YYSTACK_ALLOC_MAXIMUM
    /* The OS might guarantee only one guard page at the bottom of the stack,
       and a page size can be as small as 4096 bytes.  So we cannot safely
       invoke alloca (N) if N exceeds 4096.  Use a slightly smaller number
       to allow for a few compiler-allocated temporary stack slots.  */
#   define YYSTACK_ALLOC_MAXIMUM 4032 /* reasonable circa 2006 */
#  endif
# else
#  define YYSTACK_ALLOC YYMALLOC
#  define YYSTACK_FREE YYFREE
#  ifndef YYSTACK_ALLOC_MAXIMUM
#   define YYSTACK_ALLOC_MAXIMUM YYSIZE_MAXIMUM
#  endif
#  if (defined __cplusplus && ! defined EXIT_SUCCESS \
       && ! ((defined YYMALLOC || defined malloc) \
             && (defined YYFREE || defined free)))
#   include <stdlib.h> /* INFRINGES ON USER NAME SPACE */
#   ifndef EXIT_SUCCESS
#    define EXIT_SUCCESS 0
#   endif
#  endif
#  ifndef YYMALLOC
#   define YYMALLOC malloc
#   if ! defined malloc && ! defined EXIT_SUCCESS
void *malloc (YYSIZE_T); /* INFRINGES ON USER NAME SPACE */
#   endif
#  endif
#  ifndef YYFREE
#   define YYFREE free
#   if ! defined free && ! defined EXIT_SUCCESS
void free (void *); /* INFRINGES ON USER NAME SPACE */
#   endif
#  endif
# endif
#endif /* !defined yyoverflow */

#if (! defined yyoverflow \
     && (! defined __cplusplus \
         || (defined YYSTYPE_IS_TRIVIAL && YYSTYPE_IS_TRIVIAL)))

/* A type that is properly aligned for any stack member.  */
union yyalloc
{
  yy_state_t yyss_alloc;
  YYSTYPE yyvs_alloc;
};

/* The size of the maximum gap between one aligned stack and the next.  */
# define YYSTACK_GAP_MAXIMUM (YYSIZEOF (union yyalloc) - 1)

/* The size of an array large to enough to hold all stacks, each with
   N elements.  */
# define YYSTACK_BYTES(N) \
     ((N) * (YYSIZEOF (yy_state_t) + YYSIZEOF (YYSTYPE)) \
      + YYSTACK_GAP_MAXIMUM)

# define YYCOPY_NEEDED 1

/* Relocate STACK from its old location to the new one.  The
   local variables YYSIZE and YYSTACKSIZE give the old and new number of
   elements in the stack, and YYPTR gives the new location of the
   stack.  Advance YYPTR to a properly aligned location for the next
   stack.  */
# define YYSTACK_RELOCATE(Stack_alloc, Stack)                           \
    do                                                                  \
      {                                                                 \
        YYPTRDIFF_T yynewbytes;                                         \
        YYCOPY (&yyptr->Stack_alloc, Stack, yysize);                    \
        Stack = &yyptr->Stack_alloc;                                    \
        yynewbytes = yystacksize * YYSIZEOF (*Stack) + YYSTACK_GAP_MAXIMUM; \
        yyptr += yynewbytes / YYSIZEOF (*yyptr);                        \
      }                                                                 \
    while (0)

#endif

#if defined YYCOPY_NEEDED && YYCOPY_NEEDED
/* Copy COUNT objects from SRC to DST.  The source and destination do
   not overlap.  */
# ifndef YYCOPY
#  if defined __GNUC__ && 1 < __GNUC__
#   define YYCOPY(Dst, Src, Count) \
      __builtin_memcpy (Dst, Src, YY_CAST (YYSIZE_T, (Count)) * sizeof (*(Src)))
#  else
#   define YYCOPY(Dst, Src, Count)              \
      do                                        \
        {                                       \
          YYPTRDIFF_T yyi;                      \
          for (yyi = 0; yyi < (Count); yyi++)   \
            (Dst)[yyi] = (Src)[yyi];            \
        }                                       \
      while (0)
#  endif
# endif
#endif /* !YYCOPY_NEEDED */

/* YYFINAL -- State number of the termination state.  */
#define YYFINAL  4
/* YYLAST -- Last index in YYTABLE.  */
#define YYLAST   1056

/* YYNTOKENS -- Number of terminals.  */
#define YYNTOKENS  112
/* YYNNTS -- Number of nonterminals.  */
#define YYNNTS  156
/* YYNRULES -- Number of rules.  */
#define YYNRULES  364
/* YYNSTATES -- Number of states.  */
#define YYNSTATES  661

/* YYMAXUTOK -- Last valid token kind.  */
#define YYMAXUTOK   342


/* YYTRANSLATE(TOKEN-NUM) -- Symbol number corresponding to TOKEN-NUM
   as returned by yylex, with out-of-bounds checking.  */
#define YYTRANSLATE(YYX)                                \
  (0 <= (YYX) && (YYX) <= YYMAXUTOK                     \
   ? YY_CAST (yysymbol_kind_t, yytranslate[YYX])        \
   : YYSYMBOL_YYUNDEF)

/* YYTRANSLATE[TOKEN-NUM] -- Symbol number corresponding to TOKEN-NUM
   as returned by yylex.  */
static const yytype_int8 yytranslate[] =
{
       0,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,    98,     2,     2,     2,    97,    83,     2,
     107,   108,    95,    93,   106,    94,   111,    96,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,    78,   104,
      87,   105,    86,    77,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,   109,     2,   110,    82,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,   102,    81,   103,    99,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     1,     2,     3,     4,
       5,     6,     7,     8,     9,    10,    11,    12,    13,    14,
      15,    16,    17,    18,    19,    20,    21,    22,    23,    24,
      25,    26,    27,    28,    29,    30,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,    41,    42,    43,    44,
      45,    46,    47,    48,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    58,    59,    60,    61,    62,    63,    64,
      65,    66,    67,    68,    69,    70,    71,    72,    73,    74,
      75,    76,    79,    80,    84,    85,    88,    89,    90,    91,
      92,   100,   101
};

#if YYDEBUG
/* YYRLINE[YYN] -- Source line where rule number YYN was defined.  */
static const yytype_int16 yyrline[] =
{
       0,    53,    53,    58,    66,    73,    77,    81,    85,    89,
      97,   102,   108,   112,   119,   123,   128,   132,   136,   140,
     144,   151,   155,   159,   163,   167,   171,   175,   179,   183,
     187,   191,   199,   203,   207,   211,   218,   222,   229,   233,
     237,   244,   251,   258,   262,   269,   273,   281,   286,   292,
     296,   300,   307,   311,   319,   324,   330,   334,   338,   342,
     349,   355,   364,   368,   376,   381,   385,   390,   397,   401,
     409,   413,   417,   424,   428,   432,   436,   444,   451,   455,
     462,   466,   473,   477,   490,   494,   507,   512,   518,   522,
     529,   533,   537,   541,   549,   557,   561,   574,   579,   585,
     589,   596,   600,   604,   608,   612,   616,   620,   624,   628,
     632,   636,   643,   647,   654,   662,   662,   667,   673,   678,
     683,   688,   693,   698,   706,   711,   718,   725,   732,   737,
     743,   750,   754,   761,   765,   769,   773,   777,   784,   789,
     794,   799,   804,   809,   814,   822,   827,   835,   835,   842,
     842,   846,   853,   860,   864,   871,   878,   886,   890,   903,
     908,   914,   918,   925,   929,   933,   937,   944,   951,   956,
     962,   966,   973,   977,   984,   988,   994,   998,  1005,  1009,
    1017,  1024,  1028,  1041,  1046,  1052,  1056,  1063,  1067,  1071,
    1075,  1079,  1083,  1087,  1094,  1101,  1121,  1125,  1132,  1139,
    1144,  1150,  1154,  1161,  1165,  1172,  1176,  1183,  1188,  1192,
    1199,  1203,  1211,  1218,  1222,  1235,  1240,  1246,  1250,  1257,
    1261,  1265,  1269,  1273,  1280,  1287,  1292,  1300,  1305,  1311,
    1315,  1323,  1330,  1334,  1339,  1345,  1350,  1357,  1364,  1368,
    1381,  1386,  1392,  1396,  1403,  1407,  1411,  1418,  1425,  1429,
    1436,  1440,  1447,  1451,  1458,  1462,  1469,  1473,  1480,  1484,
    1491,  1495,  1499,  1506,  1510,  1514,  1518,  1522,  1529,  1533,
    1537,  1541,  1548,  1552,  1556,  1563,  1567,  1571,  1575,  1582,
    1586,  1590,  1594,  1598,  1605,  1609,  1613,  1617,  1621,  1625,
    1629,  1633,  1640,  1647,  1654,  1661,  1668,  1672,  1676,  1683,
    1687,  1697,  1704,  1708,  1718,  1722,  1726,  1730,  1737,  1744,
    1748,  1755,  1759,  1763,  1767,  1774,  1778,  1782,  1786,  1793,
    1797,  1804,  1808,  1812,  1819,  1823,  1830,  1834,  1841,  1848,
    1855,  1859,  1866,  1871,  1875,  1879,  1886,  1893,  1900,  1908,
    1912,  1919,  1926,  1934,  1938,  1942,  1946,  1950,  1957,  1961,
    1969,  1977,  1981,  1989,  1993,  2000,  2007,  2014,  2018,  2025,
    2029,  2036,  2043,  2047,  2054
};
#endif

/** Accessing symbol of state STATE.  */
#define YY_ACCESSING_SYMBOL(State) YY_CAST (yysymbol_kind_t, yystos[State])

#if YYDEBUG || 0
/* The user-facing name of the symbol whose (internal) number is
   YYSYMBOL.  No bounds checking.  */
static const char *yysymbol_name (yysymbol_kind_t yysymbol) YY_ATTRIBUTE_UNUSED;

/* YYTNAME[SYMBOL-NUM] -- String name of the symbol SYMBOL-NUM.
   First, the terminals, then, starting at YYNTOKENS, nonterminals.  */
static const char *const yytname[] =
{
  "\"end of file\"", "error", "\"invalid token\"", "ABSTRACT",
  "ATTRIBUTE", "BOOLEAN", "BYTE", "CASE", "CHAR", "CLASS", "DATA",
  "DEFAULT", "DELIVER", "DOUBLE", "ECLASS", "EINTERFACE", "ELEVATE",
  "ENUM", "EXPORT", "EXTENDS", "FACET", "FALSEX", "FILL", "FINAL", "FLOAT",
  "FUNCTION", "IMPLEMENTS", "IMPORT", "IMPL", "INGREDIENT", "INIT", "INT",
  "INTERFACE", "KIND", "LONG", "MAKE", "MAKES", "MAP", "METHOD",
  "NEIGHBOR", "NONE", "PACKAGE", "PNULL", "PRESENCE", "PRESENCEBEHAVIOR",
  "PRIME", "PRIVATE", "PROTECTED", "PUBLIC", "PUBLISH", "REMOTE",
  "REQUIRE", "ROLE", "SEQUENCE", "SHORT", "STATE", "STATIC", "STRING",
  "STRUCT", "STRUCTURE", "SWITCH", "TEMPLATE", "THROWS", "TO", "TRUEX",
  "TYPEDEF", "UNION", "UNIT", "UNUM", "Character", "CodeBlock", "HexBlock",
  "Initialization", "Number", "String", "Symbol", "TagType", "'?'", "':'",
  "Or", "And", "'|'", "'^'", "'&'", "Neq", "Eq", "'>'", "'<'", "Geq",
  "Leq", "Lsl", "Lsr", "Asr", "'+'", "'-'", "'*'", "'/'", "'%'", "'!'",
  "'~'", "UMinus", "UPlus", "'{'", "'}'", "';'", "'='", "','", "'('",
  "')'", "'['", "']'", "'.'", "$accept", "pluribusModule", "unitDef",
  "unitUse", "unitElems", "unitElemList", "unitElem",
  "definitionStatement", "attributeType", "attributeDef", "assignment",
  "requireAtt", "codeAtt", "codeInherit", "codeInheritList",
  "codeInherits", "codeModifier", "codeModifierList", "codeModifiers",
  "codeType", "mangledSymbolElem", "mangledSymbolList", "prototypeDecl",
  "parameterDeclList", "arrayMarker", "arrayMarkers", "parameterDecl",
  "kindDef", "commaNameList", "commaScopedRefList", "kindUse",
  "kindUseNoSemi", "kindElems", "kindElemList", "kindElem",
  "ingredientImplDef", "ingredientImplUse", "ingredientImplElems",
  "ingredientImplElemList", "ingredientImplElem", "neighborAtt",
  "stateBundleAtt", "valueOrNot", "$@1", "variableDecl", "throwsList",
  "functionAtt", "modifiersOrNot", "modifiers", "modifierList", "modifier",
  "initBlockAtt", "methodAtt", "methodCode", "$@2", "dataAtt", "$@3",
  "packageAtt", "importAtt", "implementsAtt", "presenceStructureDef",
  "presenceStructureUse", "presenceStructureElems",
  "presenceStructureElemList", "presenceStructureElem", "ingredientAtt",
  "ingredientAttElems", "ingredientAttElemList", "ingredientAttElem",
  "deliverSym", "deliverAtt", "scope", "presenceImplDef",
  "presenceImplUse", "presenceImplElems", "presenceImplElemList",
  "presenceImplElem", "presenceBehavior", "templateAtt", "makeAtt",
  "templateDef", "templateElems", "templateElemList", "templateElem",
  "mapScope", "mapAtt", "exprList", "unumStructureDef", "unumStructureUse",
  "unumStructureElems", "unumStructureElemList", "unumStructureElem",
  "primeAtt", "presenceAtt", "presenceConds", "presenceCondList",
  "presenceCond", "makes", "plurality", "unumImplDef", "unumImplUse",
  "unumImplElems", "unumImplElemList", "unumImplElem", "presenceImplAtt",
  "expr", "exprA", "expr9", "expr8", "expr7", "expr6", "expr5", "expr4",
  "expr3", "expr2", "expr1", "term", "prim", "type", "remoteDef",
  "publishDef", "defSymbol", "directRef", "scopedRef", "outerScope",
  "typeDef", "typeDeclarator", "typeSpec", "simpleTypeSpec",
  "baseTypeSpec", "templateTypeSpec", "constrTypeSpec", "declarators",
  "declarator", "simpleDeclarator", "complexDeclarator", "floatingPtType",
  "integralType", "charType", "booleanType", "structType", "memberList",
  "member", "unionType", "switchTypeSpec", "switchBody", "case",
  "caseLabels", "caseLabel", "elementSpec", "enumType", "sequenceType",
  "stringType", "arrayDeclarator", "fixedArraySizes", "fixedArraySize", YY_NULLPTR
};

static const char *
yysymbol_name (yysymbol_kind_t yysymbol)
{
  return yytname[yysymbol];
}
#endif

#define YYPACT_NINF (-399)

#define yypact_value_is_default(Yyn) \
  ((Yyn) == YYPACT_NINF)

#define YYTABLE_NINF (-296)

#define yytable_value_is_error(Yyn) \
  0

/* YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing
   STATE-NUM.  */
static const yytype_int16 yypact[] =
{
     -41,    37,   120,    56,  -399,   151,  -399,   105,   132,  -399,
     643,   655,  -399,  -399,  -399,  -399,  -399,  -399,  -399,   726,
     726,   726,   726,   726,  -399,   131,   221,   181,   231,   295,
     244,   296,   256,   400,   157,   259,   298,  -399,  -399,  -399,
     274,    39,  -399,   151,   289,   314,   988,  -399,   -33,   175,
     386,   151,   -33,     3,  -399,   345,   363,   396,   893,   403,
     183,   102,  -399,  -399,   380,   711,  -399,  -399,  -399,  -399,
    -399,    93,   389,  -399,  -399,  -399,  -399,  -399,  -399,  -399,
    -399,  -399,  -399,  -399,  -399,  -399,  -399,  -399,  -399,  -399,
    -399,  -399,   115,   726,  -399,   726,   726,   726,   726,   726,
     726,   726,   726,   726,   726,   726,   726,   726,   726,   726,
     726,   726,   726,   726,   726,   151,  -399,   151,    43,   -33,
     393,   198,  -399,   199,   399,   214,   151,   413,   223,   151,
     151,   397,   431,   422,  -399,  -399,  -399,  -399,  -399,  -399,
     -33,  -399,   440,  -399,   443,   274,   433,   468,  -399,  -399,
    -399,  -399,  -399,  -399,  -399,  -399,  -399,  -399,  -399,  -399,
    -399,   484,   655,   444,   269,   151,   151,  -399,  -399,  -399,
    -399,  -399,  -399,  -399,   151,  -399,   221,   372,   231,   295,
     244,   296,   256,   400,   400,   157,   157,   157,   157,   259,
     259,   259,   298,   298,  -399,  -399,  -399,  -399,  -399,  -399,
    -399,   445,  -399,  -399,  -399,   302,   470,   655,   305,  -399,
     167,   483,   470,   207,   183,  -399,  -399,  -399,   448,   399,
    -399,  -399,   202,  -399,   450,    89,  -399,   453,   456,  -399,
     449,   893,   274,   932,   487,  -399,   454,   458,  -399,  -399,
    -399,  -399,   460,   465,  -399,   467,   469,   245,   726,  -399,
    -399,  -399,    96,   472,  -399,   466,   198,     1,   958,   318,
    -399,  -399,  -399,   726,   821,   -33,   471,   219,   726,   475,
    -399,  -399,  -399,   476,    89,  -399,  -399,   953,   171,  -399,
     468,   740,  -399,   166,   486,   513,   454,  -399,   468,   285,
    -399,   399,   958,    -1,    -1,  -399,   245,   489,   181,  -399,
     518,  -399,  -399,   247,  -399,   519,   520,  -399,  -399,  -399,
     485,   958,  -399,  -399,  -399,  -399,   101,   133,   567,  -399,
     222,    -5,   219,   526,   168,   574,  -399,  -399,  -399,   -33,
    -399,  -399,  -399,   503,   821,  -399,  -399,  -399,  -399,  -399,
     893,   222,  -399,  -399,  -399,  -399,  -399,  -399,    28,   202,
    -399,   319,   330,    89,   328,   111,   394,  -399,  -399,   470,
     533,   470,   273,  -399,  -399,  -399,  -399,   506,   953,  -399,
    -399,  -399,  -399,   539,  -399,  -399,  -399,   512,   171,  -399,
    -399,   191,  -399,  -399,  -399,   544,  -399,   509,  -399,  -399,
    -399,  -399,  -399,  -399,   514,  -399,   517,   521,   546,  -399,
    -399,   524,   524,  -399,  -399,  -399,  -399,   953,   329,   590,
     522,  -399,  -399,  -399,   525,  -399,   527,   893,   222,   -33,
     451,   489,   328,   528,   563,   564,   534,   -31,  -399,  -399,
      54,  -399,   -55,   530,  -399,    58,  -399,   580,   209,  -399,
      67,   540,  -399,  -399,   580,   359,    -2,    30,   304,   171,
     332,  -399,  -399,   542,  -399,  -399,  -399,   560,  -399,   547,
    -399,  -399,  -399,    -1,   583,   552,  -399,   275,   614,  -399,
    -399,   585,   508,   582,   556,   274,   580,   364,  -399,   523,
     630,   557,   579,   562,   -55,    85,  -399,   -55,   561,  -399,
     562,  -399,   566,   893,   580,   557,    97,  -399,   570,   580,
     573,  -399,  -399,   373,  -399,   565,  -399,   137,  -399,   272,
    -399,  -399,  -399,    89,   274,    16,   575,  -399,   580,   388,
     578,   -33,   489,   580,   580,   391,  -399,  -399,   630,   580,
     392,   576,   -55,   577,   562,   605,   586,  -399,   587,  -399,
    -399,   588,   557,  -399,  -399,   589,    15,  -399,  -399,   160,
    -399,   591,   137,  -399,  -399,   726,   608,    23,  -399,   878,
    -399,   592,  -399,  -399,   726,  -399,   489,   580,   594,   274,
    -399,   489,   489,   580,  -399,   489,   580,  -399,   593,  -399,
     595,  -399,  -399,  -399,  -399,  -399,   276,     6,  -399,   597,
      15,  -399,  -399,  -399,  -399,    38,  -399,  -399,   377,  -399,
    -399,  -399,   468,  -399,   602,  -399,   598,   726,  -399,   441,
    -399,   489,   580,   404,  -399,  -399,   489,  -399,   489,  -399,
    -399,   821,   342,  -399,  -399,   633,  -399,  -399,  -399,  -399,
      35,  -399,  -399,  -399,  -399,  -399,   634,  -399,   489,   580,
    -399,  -399,   607,  -399,    48,    38,  -399,   620,  -399,   489,
    -399,   106,  -399,   622,  -399,  -399,   626,   627,  -399,  -399,
    -399
};

/* YYDEFACT[STATE-NUM] -- Default reduction number in state STATE-NUM.
   Performed when YYTABLE does not specify something else to do.  Zero
   means the default is an error.  */
static const yytype_int16 yydefact[] =
{
       0,     3,     0,     0,     1,     0,     2,   296,     0,   295,
       0,    55,   290,   291,   285,   284,   287,   286,   302,     0,
       0,     0,     0,     0,   297,     0,   210,   248,   250,   252,
     254,   256,   258,   260,   263,   268,   272,   275,   279,   299,
     289,     0,    49,     0,     0,     0,     0,    50,     0,     0,
       0,     0,     0,     0,    51,     0,     0,     0,     0,     0,
       0,     0,    29,    19,     0,    55,    12,    14,    21,    16,
      52,    54,     0,    23,    22,    18,    17,    25,    24,    31,
      30,    20,    27,    26,    28,   305,   306,   307,   280,   281,
     282,   283,     0,     0,   298,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   303,     0,     0,     0,
       0,     0,    15,     0,   241,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   337,   332,   336,   331,   330,   334,
       0,   335,     0,   333,   360,   313,     0,     0,   309,   311,
     312,   310,   315,   316,   317,   318,   321,   322,   323,   319,
     320,     0,    55,   299,     0,     0,     0,     4,    13,    53,
      56,    58,    59,    57,     0,   288,   211,     0,   251,   253,
     255,   257,   259,   262,   261,   266,   264,   267,   265,   269,
     270,   271,   273,   274,   276,   277,   278,   300,   301,    34,
      37,     0,    33,    32,    35,     0,     0,    55,     0,   153,
       0,     0,     0,     0,     0,    40,   244,   245,     0,   240,
     242,   246,     0,   238,     0,    87,   152,     0,     0,   294,
       0,     0,   314,     0,     0,   304,   328,   308,   324,   326,
     327,   329,     0,     0,     5,     0,     0,    48,     0,    36,
       7,    78,     0,     0,     6,     0,     0,     0,   216,     0,
     239,   243,    39,     0,    98,     0,     0,     0,     0,     0,
      91,    90,    92,     0,    86,    88,    93,   184,   160,   293,
       0,     0,   339,     0,     0,     0,   361,   362,     0,     0,
       8,   241,   216,     0,     0,    45,    47,     0,   249,   356,
       0,     9,   154,     0,   247,     0,     0,   219,   220,   221,
       0,   215,   217,   222,   223,   213,     0,     0,     0,   137,
     129,   125,     0,     0,     0,     0,   134,   135,   133,     0,
     136,   101,   102,     0,    97,    99,   103,   104,   105,   106,
       0,   130,   131,   108,   107,   109,   110,   111,     0,   313,
     292,     0,     0,    87,     0,     0,     0,    77,    89,     0,
       0,     0,     0,   187,   189,   193,   190,     0,   183,   185,
     188,   191,   192,     0,   163,   164,   165,     0,   159,   161,
     166,     0,   338,   340,   358,     0,   359,     0,   363,   325,
     347,   343,   344,   345,     0,   346,     0,     0,     0,    60,
      62,    43,    44,    46,   147,    42,    79,   184,     0,     0,
       0,   214,   218,    38,     0,   149,     0,     0,   128,     0,
       0,     0,   314,     0,     0,     0,     0,     0,    94,   100,
       0,   132,   117,     0,    71,     0,   155,   125,     0,    68,
      76,     0,    82,    41,   125,     0,     0,     0,     0,   160,
       0,   180,   186,     0,   156,   162,   341,     0,   364,     0,
     237,   212,    61,     0,     0,     0,   181,     0,   234,   224,
     151,     0,     0,     0,   124,    80,   125,     0,   144,     0,
       0,   236,     0,   117,   117,     0,   115,   117,     0,    70,
     117,    72,     0,     0,   125,    75,     0,    83,     0,   125,
       0,   195,   196,     0,   194,     0,   157,   169,   357,     0,
      63,   148,   182,    87,    84,     0,     0,   150,   125,     0,
       0,     0,     0,   125,   125,     0,   112,   235,     0,   125,
       0,     0,   117,     0,   117,     0,     0,   123,     0,    67,
      69,     0,    74,    73,    65,     0,   200,   197,   158,     0,
     172,     0,   168,   170,   173,     0,     0,     0,   348,     0,
     351,     0,   233,   232,   228,   225,     0,   125,     0,    81,
     143,     0,     0,   125,   113,     0,   125,   114,     0,   120,
       0,   116,   121,   122,    66,    64,     0,     0,   203,     0,
     199,   201,   204,   178,   179,     0,   167,   171,     0,   354,
     342,   349,     0,   352,     0,    85,     0,   227,   229,     0,
     141,     0,   125,     0,   142,   146,     0,   139,     0,   118,
     119,    98,     0,   205,   206,     0,   198,   202,   175,   174,
       0,   353,   355,   350,   226,   230,     0,   140,     0,   125,
     145,   138,     0,    95,     0,     0,   177,     0,   127,     0,
      96,     0,   209,     0,   231,   126,     0,     0,   176,   207,
     208
};

/* YYPGOTO[NTERM-NUM].  */
static const yytype_int16 yypgoto[] =
{
    -399,  -399,   686,    29,   -93,  -399,   646,   687,  -399,  -399,
    -251,  -183,  -399,   436,  -399,  -399,   664,  -399,  -399,  -399,
     280,   455,  -399,  -336,  -388,  -394,   262,  -399,  -206,  -399,
    -253,  -399,  -329,  -399,   482,  -399,  -399,   141,  -399,   429,
    -399,  -399,  -352,  -399,  -399,  -132,  -399,  -399,  -399,   446,
    -308,  -262,  -399,  -398,  -399,  -399,  -399,  -399,  -247,  -256,
    -399,  -399,   316,  -399,   390,  -399,  -399,  -399,   215,   125,
    -399,  -399,  -399,  -399,   365,  -399,   407,  -399,  -399,  -399,
    -399,  -399,  -399,   190,  -399,  -399,  -399,  -399,  -399,   490,
    -399,   473,  -399,  -399,  -399,  -399,   174,  -399,  -399,  -399,
    -399,   492,  -399,   568,  -399,    -7,   537,   690,   691,   693,
     697,   689,   421,   387,   354,   424,    45,  -399,  -255,  -399,
    -399,   158,   216,   -48,  -399,  -399,  -399,   -56,   559,  -399,
    -399,  -399,   529,  -274,  -399,  -399,  -399,   515,   -99,   -96,
      17,  -399,   531,    24,  -399,  -399,   246,  -399,   248,  -399,
       7,  -399,   692,  -399,  -399,   532
};

/* YYDEFGOTO[NTERM-NUM].  */
static const yytype_int16 yydefgoto[] =
{
       0,     2,    62,   215,    64,    65,    66,    67,   201,    68,
     216,   270,    69,   295,   296,   297,    70,    71,    72,   174,
     400,   401,   271,   438,   434,   435,   439,    73,   252,   474,
     272,   468,   273,   274,   275,    74,   588,   333,   334,   335,
     336,   337,   488,   535,   338,   421,   339,   417,   340,   341,
     342,   343,   344,   405,   464,   345,   471,    75,    76,   276,
      77,   366,   377,   378,   379,   380,   551,   552,   553,   630,
     554,   595,    78,   304,   367,   368,   369,   370,   371,   372,
     501,   589,   590,   591,   625,   592,    25,    79,   217,   310,
     311,   312,   313,   314,   606,   607,   608,   516,   528,    80,
      81,   218,   219,   220,   221,   609,    27,    28,    29,    30,
      31,    32,    33,    34,    35,    36,    37,    38,   440,    82,
      83,     8,    39,    40,    41,    84,   146,   350,   148,   149,
     150,   151,   237,   238,   239,   240,   152,   153,   154,   155,
     156,   281,   282,   157,   394,   557,   558,   559,   560,   604,
     158,   159,   160,   241,   286,   287
};

/* YYTABLE[YYPACT[STATE-NUM]] -- What to do in state STATE-NUM.  If
   positive, shift that token.  If negative, reduce the rule whose
   number is the opposite.  If YYTABLE_NINF, syntax error.  */
static const yytype_int16 yytable[] =
{
     123,   125,   147,    26,   128,   309,   257,   307,   347,   348,
     145,   332,   164,   331,   389,   364,    92,   346,    87,   202,
     445,   365,   203,   478,   441,   376,   363,   374,    85,   303,
     555,   129,   398,   431,   556,    86,   485,     1,   487,   309,
      63,   307,     7,   586,   483,   623,   496,   491,   134,    18,
     486,   136,   587,    87,   433,     3,   562,   419,   309,   500,
     307,   624,   130,    85,    88,    89,    90,    91,   628,   243,
      86,   205,    87,   208,   399,   308,   222,   199,   347,   348,
     115,   332,    85,   331,   477,   430,   176,   346,   177,    86,
     532,   563,   232,   527,    63,   375,    42,   491,   645,   491,
     144,   542,   420,   432,   300,   251,   364,   300,   491,   308,
     431,   651,   365,   629,   253,   265,    47,   363,   564,   266,
       4,   116,   267,     5,   570,   376,   600,   374,   308,   484,
     165,   531,   533,   490,   502,   536,   519,   433,   538,   646,
     268,    54,   495,   525,   491,   364,   530,   200,   656,   549,
     117,   365,   652,   446,   491,   448,   363,   194,   195,   196,
     534,   166,   473,   433,   269,   259,   164,   433,   610,    87,
     267,   222,   543,   614,   615,   280,   433,   617,    95,    85,
     578,   657,   580,   145,   561,   145,    86,    44,    95,   211,
     392,    63,    95,   393,   433,   375,   376,   424,   374,   299,
     373,   118,   300,   593,   267,   413,   433,   414,   208,   127,
     222,   425,    10,   637,    87,   443,   349,   351,   640,   354,
     641,     9,   268,   175,    85,   280,     7,   526,   594,   222,
     222,    86,   613,   145,    11,   415,    63,    93,   214,    94,
     648,   503,     7,   222,   222,   319,     7,   107,   108,   109,
       7,   655,   384,    18,   550,   408,   316,    18,     7,     9,
      96,   355,   255,   222,   293,    18,   375,     9,   326,   327,
     328,   294,   385,     7,   422,   574,   163,   124,   330,   555,
      18,   427,     7,   556,   224,   162,   349,   227,   228,    18,
     134,   135,   145,   136,     7,   456,   395,   288,    95,   550,
     207,    18,    45,   209,   145,   492,   262,   263,   145,   258,
     210,    97,   498,   115,   450,   493,   139,   494,   223,   141,
     222,   353,     7,   245,   246,   115,    99,   226,   632,    18,
     222,   197,   247,   198,   115,   134,   135,   163,   136,   143,
     101,   102,     9,   137,   522,     9,     9,    45,     7,   407,
       7,     7,   110,   111,   138,    18,   119,    18,    18,   222,
     390,   139,   541,   140,   141,   347,   348,   545,   332,   145,
     331,   475,   145,   244,   346,   449,    98,   513,   621,   100,
     115,     9,     9,   142,   143,   115,   566,   144,    57,   120,
       9,   571,   572,   112,   113,   114,    59,   575,   170,   134,
     135,   222,   136,   171,   172,     7,   250,   137,   504,   254,
     300,    45,    18,   115,   126,    44,   115,   211,   138,   514,
     131,   173,   315,   436,   145,   139,   197,   140,   141,   115,
     115,   145,   442,   466,   145,   611,   506,   132,   437,   115,
     115,   616,   212,   115,   618,   145,   643,   142,   143,    95,
     248,   144,    57,   115,    95,   631,   134,   135,   213,   136,
      59,   189,   190,   191,   137,   493,   214,   499,    45,     7,
     493,   133,   523,   569,     7,   138,    18,   547,   161,   300,
     638,    18,   139,   167,   140,   141,   103,   104,   105,   106,
     185,   186,   187,   188,   493,   206,   567,   493,   493,   573,
     576,   229,   444,   602,   142,   143,   230,   649,   144,    57,
     493,   145,   639,   134,   135,   225,   136,    59,    95,   636,
     145,   137,   183,   184,   231,    45,     7,   233,   134,   135,
     234,   136,   138,    18,   192,   193,   137,   235,   622,   139,
      45,   140,   141,   236,   242,   251,  -295,   138,   598,   249,
     256,   260,   264,   279,   139,   277,   140,   141,   278,   476,
     284,   142,   143,   285,   288,   144,    57,   289,   290,   291,
     302,   292,   386,   349,    59,   301,   142,   143,   352,   357,
     144,    57,   356,     7,   134,   135,   387,   136,   411,    59,
      18,   404,   137,   406,   409,   410,    45,   416,     7,   134,
     135,   423,   136,   138,   426,    18,   428,   137,   447,   451,
     139,    45,   140,   141,   453,   454,   518,   457,   138,   458,
     460,   462,   459,   467,   461,   139,   469,   140,   141,   470,
     463,   524,   142,   143,   472,   479,   144,    57,   480,   481,
     489,   482,   419,   497,   507,    59,   508,   142,   143,   509,
     515,   144,    57,   511,     7,   512,   517,   520,    42,    43,
      59,    18,   521,   267,    12,   537,   433,   486,   548,     7,
     539,    44,    45,    46,   544,   546,    18,   581,    47,   565,
     577,   579,    48,    49,    50,   568,   599,   529,    51,     6,
     582,   583,   584,   585,   596,   605,    52,   619,    53,   620,
     626,   634,   612,    54,    55,    56,   633,    13,   644,   647,
     650,   168,    14,    57,    42,    43,    15,    16,     7,    17,
      58,    59,    60,    61,   654,    18,   658,    44,    45,    46,
     659,   660,   403,   122,    47,   169,    19,    20,    48,    49,
      50,    21,    22,   510,    51,   134,   135,    12,   136,   402,
      23,    24,    52,   137,    53,   540,   358,    45,   -11,    54,
      55,    56,   642,   429,   138,   505,   418,   597,   455,    57,
     653,   139,   465,   140,   141,   452,    58,    59,    60,    61,
     627,   635,   397,   396,   412,   298,   178,   261,   179,   182,
      13,   180,   283,   142,   143,    14,   181,   144,    57,    15,
      16,     7,    17,   601,   391,     0,    59,   603,    18,   381,
     204,     0,   383,     0,   -10,     7,     0,     0,   388,    19,
      20,     0,    18,     0,    21,    22,   134,   135,     0,   136,
       0,   317,     0,    23,   137,     0,     0,    44,    45,   211,
       0,   318,     0,   382,   319,   138,   320,   265,    48,     0,
       0,   321,   139,     0,   322,   141,     0,     0,     0,   323,
     324,     0,     0,     0,     0,     0,   325,   326,   327,   328,
       0,     0,     0,     0,   142,   143,   329,   330,   144,    57,
       0,     0,     0,   134,   135,   555,   136,    59,   214,   556,
       0,   137,     0,     0,     0,    45,     7,     0,   134,   135,
       0,   136,   138,    18,     0,     0,   137,     0,     0,   139,
      45,   140,   141,     0,     0,     0,     0,   138,     0,     0,
       0,     0,     0,     0,   139,     0,   140,   141,     0,     0,
       0,   142,   143,     0,     0,   144,    57,   134,   135,     0,
     136,     0,     0,     0,    59,   137,   142,   143,     0,     0,
     144,    57,     0,     7,     0,     0,   138,     0,     0,    59,
      18,     0,     0,   139,     0,   140,   141,     0,     7,    44,
       0,   211,     0,   318,    44,    18,   211,     0,     0,   265,
       0,     0,   359,   321,     0,   142,   143,     0,   360,   144,
       0,   267,    43,     0,     0,     0,     0,   361,   325,     0,
       0,   305,     0,   306,     0,    45,     0,     7,     0,   268,
       0,     0,   362,     0,    18,     0,     0,    50,     0,     0,
     214,    51,     0,     0,     0,   214,     0,     0,     7,     0,
       0,    53,     0,     7,     0,    18,     0,    55,    56,     0,
      18,     0,     0,     0,     0,     0,    57,     0,     0,     0,
       0,     0,     0,    58,    59,   121,    61
};

static const yytype_int16 yycheck[] =
{
      48,    49,    58,    10,    52,   258,   212,   258,   264,   264,
      58,   264,    60,   264,   288,   277,    23,   264,    11,   118,
     356,   277,   118,   421,   353,   278,   277,   278,    11,    28,
       7,    28,    33,   341,    11,    11,   430,    78,   432,   292,
      11,   292,    75,    28,    75,    39,   440,   435,     5,    82,
     105,     8,    37,    46,   109,    18,    40,    62,   311,    61,
     311,    55,    59,    46,    19,    20,    21,    22,    30,   162,
      46,   119,    65,   121,    75,   258,   124,    34,   334,   334,
     111,   334,    65,   334,   420,   340,    93,   334,    95,    65,
     484,    75,   140,   481,    65,   278,     3,   485,    63,   487,
      57,   495,   107,    75,   106,    75,   368,   106,   496,   292,
     418,    63,   368,    75,   207,    26,    23,   368,   102,    30,
       0,    82,    33,    67,   522,   378,   103,   378,   311,    75,
      28,   483,   484,    75,   104,   487,   472,   109,   490,   104,
      51,    48,    75,   479,   532,   407,   482,   104,    42,    12,
     111,   407,   104,   359,   542,   361,   407,   112,   113,   114,
      75,    59,   417,   109,    75,   213,   214,   109,   566,   162,
      33,   219,    75,   571,   572,   231,   109,   575,    77,   162,
     532,    75,   534,   231,   513,   233,   162,    16,    77,    18,
     289,   162,    77,   289,   109,   378,   449,    29,   449,   103,
      29,    43,   106,    43,    33,   104,   109,    74,   256,    51,
     258,    43,   107,   611,   207,   104,   264,   265,   616,   267,
     618,     5,    51,   108,   207,   281,    75,   480,    68,   277,
     278,   207,   568,   281,   102,   102,   207,   106,    67,   108,
     638,   447,    75,   291,   292,    23,    75,    90,    91,    92,
      75,   649,    86,    82,   507,   303,   263,    82,    75,    43,
      79,   268,    95,   311,    19,    82,   449,    51,    46,    47,
      48,    26,   106,    75,   322,   528,    60,   102,    56,     7,
      82,   329,    75,    11,   126,   102,   334,   129,   130,    82,
       5,     6,   340,     8,    75,   104,   289,   106,    77,   552,
     102,    82,    17,   104,   352,   437,   104,   105,   356,   102,
     111,    80,   444,   111,   362,   106,    31,   108,   104,    34,
     368,   102,    75,   165,   166,   111,    82,   104,   602,    82,
     378,   115,   174,   117,   111,     5,     6,   121,     8,    54,
      84,    85,   126,    13,   476,   129,   130,    17,    75,   102,
      75,    75,    93,    94,    24,    82,    67,    82,    82,   407,
      75,    31,   494,    33,    34,   621,   621,   499,   621,   417,
     621,   419,   420,   104,   621,   102,    81,   102,   102,    83,
     111,   165,   166,    53,    54,   111,   518,    57,    58,    75,
     174,   523,   524,    95,    96,    97,    66,   529,     9,     5,
       6,   449,     8,    14,    15,    75,   104,    13,   104,   104,
     106,    17,    82,   111,    28,    16,   111,    18,    24,   467,
      75,    32,   104,   104,   472,    31,   210,    33,    34,   111,
     111,   479,   104,   104,   482,   567,   104,    74,   108,   111,
     111,   573,    43,   111,   576,   493,   104,    53,    54,    77,
      78,    57,    58,   111,    77,    78,     5,     6,    59,     8,
      66,   107,   108,   109,    13,   106,    67,   108,    17,    75,
     106,    75,   108,   521,    75,    24,    82,   104,    75,   106,
     612,    82,    31,   103,    33,    34,    86,    87,    88,    89,
     103,   104,   105,   106,   106,   102,   108,   106,   106,   108,
     108,   104,   108,   559,    53,    54,    75,   639,    57,    58,
     106,   559,   108,     5,     6,   102,     8,    66,    77,    78,
     568,    13,   101,   102,   102,    17,    75,    87,     5,     6,
      87,     8,    24,    82,   110,   111,    13,   104,   586,    31,
      17,    33,    34,    75,    60,    75,   102,    24,   555,   104,
      67,   103,   102,   104,    31,   102,    33,    34,   102,   108,
      73,    53,    54,   109,   106,    57,    58,   107,   103,   102,
     104,   102,    86,   621,    66,   103,    53,    54,   107,   103,
      57,    58,   107,    75,     5,     6,    73,     8,   103,    66,
      82,   102,    13,    75,    75,    75,    17,    30,    75,     5,
       6,    75,     8,    24,    30,    82,   103,    13,    75,   103,
      31,    17,    33,    34,    75,   103,   108,    73,    24,   110,
     103,    75,   108,    33,   103,    31,   104,    33,    34,   104,
     106,   108,    53,    54,   107,   107,    57,    58,    75,    75,
     110,   107,    62,   103,   102,    66,    86,    53,    54,   102,
      36,    57,    58,    70,    75,   103,    71,    75,     3,     4,
      66,    82,   106,    33,    21,   104,   109,   105,   103,    75,
     104,    16,    17,    18,   104,   102,    82,    72,    23,   104,
     104,   104,    27,    28,    29,   107,    78,   108,    33,     3,
     104,   104,   104,   104,   103,   103,    41,   104,    43,   104,
     103,   103,   108,    48,    49,    50,   104,    64,    75,    75,
     103,    65,    69,    58,     3,     4,    73,    74,    75,    76,
      65,    66,    67,    68,   104,    82,   104,    16,    17,    18,
     104,   104,   296,    46,    23,    71,    93,    94,    27,    28,
      29,    98,    99,   463,    33,     5,     6,    21,     8,   294,
     107,   108,    41,    13,    43,   493,   274,    17,   103,    48,
      49,    50,   621,   334,    24,   449,   320,   552,   378,    58,
     645,    31,   407,    33,    34,   368,    65,    66,    67,    68,
     590,   607,   292,   291,   311,   248,    96,   219,    97,   100,
      64,    98,   233,    53,    54,    69,    99,    57,    58,    73,
      74,    75,    76,   557,   289,    -1,    66,   559,    82,   280,
     118,    -1,   281,    -1,   103,    75,    -1,    -1,   286,    93,
      94,    -1,    82,    -1,    98,    99,     5,     6,    -1,     8,
      -1,    10,    -1,   107,    13,    -1,    -1,    16,    17,    18,
      -1,    20,    -1,   103,    23,    24,    25,    26,    27,    -1,
      -1,    30,    31,    -1,    33,    34,    -1,    -1,    -1,    38,
      39,    -1,    -1,    -1,    -1,    -1,    45,    46,    47,    48,
      -1,    -1,    -1,    -1,    53,    54,    55,    56,    57,    58,
      -1,    -1,    -1,     5,     6,     7,     8,    66,    67,    11,
      -1,    13,    -1,    -1,    -1,    17,    75,    -1,     5,     6,
      -1,     8,    24,    82,    -1,    -1,    13,    -1,    -1,    31,
      17,    33,    34,    -1,    -1,    -1,    -1,    24,    -1,    -1,
      -1,    -1,    -1,    -1,    31,    -1,    33,    34,    -1,    -1,
      -1,    53,    54,    -1,    -1,    57,    58,     5,     6,    -1,
       8,    -1,    -1,    -1,    66,    13,    53,    54,    -1,    -1,
      57,    58,    -1,    75,    -1,    -1,    24,    -1,    -1,    66,
      82,    -1,    -1,    31,    -1,    33,    34,    -1,    75,    16,
      -1,    18,    -1,    20,    16,    82,    18,    -1,    -1,    26,
      -1,    -1,    29,    30,    -1,    53,    54,    -1,    35,    57,
      -1,    33,     4,    -1,    -1,    -1,    -1,    44,    45,    -1,
      -1,    43,    -1,    45,    -1,    17,    -1,    75,    -1,    51,
      -1,    -1,    59,    -1,    82,    -1,    -1,    29,    -1,    -1,
      67,    33,    -1,    -1,    -1,    67,    -1,    -1,    75,    -1,
      -1,    43,    -1,    75,    -1,    82,    -1,    49,    50,    -1,
      82,    -1,    -1,    -1,    -1,    -1,    58,    -1,    -1,    -1,
      -1,    -1,    -1,    65,    66,    67,    68
};

/* YYSTOS[STATE-NUM] -- The symbol kind of the accessing symbol of
   state STATE-NUM.  */
static const yytype_int16 yystos[] =
{
       0,    78,   113,    18,     0,    67,   114,    75,   233,   234,
     107,   102,    21,    64,    69,    73,    74,    76,    82,    93,
      94,    98,    99,   107,   108,   198,   217,   218,   219,   220,
     221,   222,   223,   224,   225,   226,   227,   228,   229,   234,
     235,   236,     3,     4,    16,    17,    18,    23,    27,    28,
      29,    33,    41,    43,    48,    49,    50,    58,    65,    66,
      67,    68,   114,   115,   116,   117,   118,   119,   121,   124,
     128,   129,   130,   139,   147,   169,   170,   172,   184,   199,
     211,   212,   231,   232,   237,   252,   255,   262,   228,   228,
     228,   228,   217,   106,   108,    77,    79,    80,    81,    82,
      83,    84,    85,    86,    87,    88,    89,    90,    91,    92,
      93,    94,    95,    96,    97,   111,    82,   111,   233,    67,
      75,    67,   119,   235,   102,   235,    28,   233,   235,    28,
      59,    75,    74,    75,     5,     6,     8,    13,    24,    31,
      33,    34,    53,    54,    57,   235,   238,   239,   240,   241,
     242,   243,   248,   249,   250,   251,   252,   255,   262,   263,
     264,    75,   102,   234,   235,    28,    59,   103,   118,   128,
       9,    14,    15,    32,   131,   108,   217,   217,   219,   220,
     221,   222,   223,   224,   224,   225,   225,   225,   225,   226,
     226,   226,   227,   227,   228,   228,   228,   234,   234,    34,
     104,   120,   250,   251,   264,   235,   102,   102,   235,   104,
     111,    18,    43,    59,    67,   115,   122,   200,   213,   214,
     215,   216,   235,   104,   233,   102,   104,   233,   233,   104,
      75,   102,   235,    87,    87,   104,    75,   244,   245,   246,
     247,   265,    60,   116,   104,   233,   233,   233,    78,   104,
     104,    75,   140,   116,   104,    95,    67,   140,   102,   235,
     103,   215,   104,   105,   102,    26,    30,    33,    51,    75,
     123,   134,   142,   144,   145,   146,   171,   102,   102,   104,
     239,   253,   254,   240,    73,   109,   266,   267,   106,   107,
     103,   102,   102,    19,    26,   125,   126,   127,   218,   103,
     106,   103,   104,    28,   185,    43,    45,   122,   123,   142,
     201,   202,   203,   204,   205,   104,   217,    10,    20,    23,
      25,    30,    33,    38,    39,    45,    46,    47,    48,    55,
      56,   122,   142,   149,   150,   151,   152,   153,   156,   158,
     160,   161,   162,   163,   164,   167,   170,   171,   230,   235,
     239,   235,   107,   102,   235,   217,   107,   103,   146,    29,
      35,    44,    59,   122,   163,   171,   173,   186,   187,   188,
     189,   190,   191,    29,   122,   123,   142,   174,   175,   176,
     177,   244,   103,   254,    86,   106,    86,    73,   267,   245,
      75,   249,   250,   251,   256,   262,   213,   201,    33,    75,
     132,   133,   133,   125,   102,   165,    75,   102,   235,    75,
      75,   103,   203,   104,    74,   102,    30,   159,   161,    62,
     107,   157,   235,    75,    29,    43,    30,   235,   103,   151,
     230,   162,    75,   109,   136,   137,   104,   108,   135,   138,
     230,   144,   104,   104,   108,   135,   140,    75,   140,   102,
     235,   103,   188,    75,   103,   176,   104,    73,   110,   108,
     103,   103,    75,   106,   166,   186,   104,    33,   143,   104,
     104,   168,   107,   230,   141,   235,   108,   135,   165,   107,
      75,    75,   107,    75,    75,   137,   105,   137,   154,   110,
      75,   136,   157,   106,   108,    75,   137,   103,   157,   108,
      61,   192,   104,   140,   104,   174,   104,   102,    86,   102,
     132,    70,   103,   102,   235,    36,   209,    71,   108,   135,
      75,   106,   157,   108,   108,   135,   142,   136,   210,   108,
     135,   154,   137,   154,    75,   155,   154,   104,   154,   104,
     138,   157,   137,    75,   104,   157,   102,   104,   103,    12,
     142,   178,   179,   180,   182,     7,    11,   257,   258,   259,
     260,   144,    40,    75,   102,   104,   157,   108,   107,   235,
     165,   157,   157,   108,   142,   157,   108,   104,   154,   104,
     154,    72,   104,   104,   104,   104,    28,    37,   148,   193,
     194,   195,   197,    43,    68,   183,   103,   180,   217,    78,
     103,   258,   239,   260,   261,   103,   206,   207,   208,   217,
     165,   157,   108,   135,   165,   165,   157,   165,   157,   104,
     104,   102,   235,    39,    55,   196,   103,   195,    30,    75,
     181,    78,   245,   104,   103,   208,    78,   165,   157,   108,
     165,   165,   149,   104,    75,    63,   104,    75,   165,   157,
     103,    63,   104,   181,   104,   165,    42,    75,   104,   104,
     104
};

/* YYR1[RULE-NUM] -- Symbol kind of the left-hand side of rule RULE-NUM.  */
static const yytype_int16 yyr1[] =
{
       0,   112,   113,   113,   114,   115,   115,   115,   115,   115,
     116,   116,   117,   117,   118,   118,   118,   118,   118,   118,
     118,   119,   119,   119,   119,   119,   119,   119,   119,   119,
     119,   119,   120,   120,   120,   120,   121,   121,   122,   122,
     122,   123,   124,   125,   125,   126,   126,   127,   127,   128,
     128,   128,   129,   129,   130,   130,   131,   131,   131,   131,
     132,   132,   133,   133,   134,   134,   134,   134,   135,   135,
     136,   137,   137,   138,   138,   138,   138,   139,   140,   140,
     141,   141,   142,   142,   143,   143,   144,   144,   145,   145,
     146,   146,   146,   146,   147,   148,   148,   149,   149,   150,
     150,   151,   151,   151,   151,   151,   151,   151,   151,   151,
     151,   151,   152,   152,   153,   155,   154,   154,   156,   156,
     156,   156,   156,   156,   157,   157,   158,   158,   159,   159,
     160,   161,   161,   162,   162,   162,   162,   162,   163,   163,
     163,   163,   163,   163,   163,   164,   164,   166,   165,   168,
     167,   167,   169,   170,   170,   171,   172,   173,   173,   174,
     174,   175,   175,   176,   176,   176,   176,   177,   178,   178,
     179,   179,   180,   180,   181,   181,   182,   182,   183,   183,
     184,   185,   185,   186,   186,   187,   187,   188,   188,   188,
     188,   188,   188,   188,   189,   190,   191,   191,   192,   193,
     193,   194,   194,   195,   195,   196,   196,   197,   197,   197,
     198,   198,   199,   200,   200,   201,   201,   202,   202,   203,
     203,   203,   203,   203,   204,   205,   205,   206,   206,   207,
     207,   208,   209,   209,   209,   210,   210,   211,   212,   212,
     213,   213,   214,   214,   215,   215,   215,   216,   217,   217,
     218,   218,   219,   219,   220,   220,   221,   221,   222,   222,
     223,   223,   223,   224,   224,   224,   224,   224,   225,   225,
     225,   225,   226,   226,   226,   227,   227,   227,   227,   228,
     228,   228,   228,   228,   229,   229,   229,   229,   229,   229,
     229,   229,   230,   231,   232,   233,   234,   234,   234,   235,
     235,   235,   236,   236,   237,   237,   237,   237,   238,   239,
     239,   240,   240,   240,   240,   241,   241,   241,   241,   242,
     242,   243,   243,   243,   244,   244,   245,   245,   246,   247,
     248,   248,   249,   249,   249,   249,   250,   251,   252,   253,
     253,   254,   255,   256,   256,   256,   256,   256,   257,   257,
     258,   259,   259,   260,   260,   261,   262,   263,   263,   264,
     264,   265,   266,   266,   267
};

/* YYR2[RULE-NUM] -- Number of symbols on the right-hand side of rule RULE-NUM.  */
static const yytype_int8 yyr2[] =
{
       0,     2,     3,     1,     5,     3,     4,     4,     4,     5,
       1,     0,     1,     2,     1,     2,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     4,     3,     4,     2,
       1,     3,     5,     2,     2,     1,     2,     1,     0,     1,
       1,     1,     1,     2,     1,     0,     1,     1,     1,     1,
       1,     2,     1,     3,     6,     5,     6,     5,     1,     3,
       2,     1,     2,     3,     3,     2,     1,     5,     1,     3,
       1,     3,     3,     4,     2,     4,     1,     0,     1,     2,
       1,     1,     1,     1,     6,     3,     4,     1,     0,     1,
       2,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     4,     5,     5,     0,     3,     0,     6,     6,
       5,     5,     5,     4,     2,     0,     9,     8,     1,     0,
       1,     1,     2,     1,     1,     1,     1,     1,     7,     6,
       7,     6,     6,     5,     3,     7,     6,     0,     3,     0,
       4,     3,     3,     3,     5,     3,     6,     3,     4,     1,
       0,     1,     2,     1,     1,     1,     1,     5,     1,     0,
       1,     2,     1,     1,     1,     1,     6,     4,     1,     1,
       6,     3,     4,     1,     0,     1,     2,     1,     1,     1,
       1,     1,     1,     1,     3,     3,     3,     4,     4,     1,
       0,     1,     2,     1,     1,     1,     1,     6,     6,     4,
       1,     3,     6,     3,     4,     1,     0,     1,     2,     1,
       1,     1,     1,     1,     3,     5,     7,     1,     0,     1,
       2,     4,     2,     2,     0,     1,     0,     6,     3,     4,
       1,     0,     1,     2,     1,     1,     1,     3,     1,     5,
       1,     3,     1,     3,     1,     3,     1,     3,     1,     3,
       1,     3,     3,     1,     3,     3,     3,     3,     1,     3,
       3,     3,     1,     3,     3,     1,     3,     3,     3,     1,
       2,     2,     2,     2,     1,     1,     1,     1,     3,     1,
       1,     1,     1,     4,     3,     1,     1,     3,     4,     1,
       3,     3,     1,     2,     3,     1,     1,     1,     2,     1,
       1,     1,     1,     1,     2,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     3,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     5,     1,
       2,     3,     9,     1,     1,     1,     1,     1,     1,     2,
       3,     1,     2,     3,     2,     2,     5,     6,     4,     4,
       1,     2,     1,     2,     3
};


enum { YYENOMEM = -2 };

#define yyerrok         (yyerrstatus = 0)
#define yyclearin       (yychar = YYEMPTY)

#define YYACCEPT        goto yyacceptlab
#define YYABORT         goto yyabortlab
#define YYERROR         goto yyerrorlab
#define YYNOMEM         goto yyexhaustedlab


#define YYRECOVERING()  (!!yyerrstatus)

#define YYBACKUP(Token, Value)                                    \
  do                                                              \
    if (yychar == YYEMPTY)                                        \
      {                                                           \
        yychar = (Token);                                         \
        yylval = (Value);                                         \
        YYPOPSTACK (yylen);                                       \
        yystate = *yyssp;                                         \
        goto yybackup;                                            \
      }                                                           \
    else                                                          \
      {                                                           \
        yyerror (YY_("syntax error: cannot back up")); \
        YYERROR;                                                  \
      }                                                           \
  while (0)

/* Backward compatibility with an undocumented macro.
   Use YYerror or YYUNDEF. */
#define YYERRCODE YYUNDEF


/* Enable debugging if requested.  */
#if YYDEBUG

# ifndef YYFPRINTF
#  include <stdio.h> /* INFRINGES ON USER NAME SPACE */
#  define YYFPRINTF fprintf
# endif

# define YYDPRINTF(Args)                        \
do {                                            \
  if (yydebug)                                  \
    YYFPRINTF Args;                             \
} while (0)




# define YY_SYMBOL_PRINT(Title, Kind, Value, Location)                    \
do {                                                                      \
  if (yydebug)                                                            \
    {                                                                     \
      YYFPRINTF (stderr, "%s ", Title);                                   \
      yy_symbol_print (stderr,                                            \
                  Kind, Value); \
      YYFPRINTF (stderr, "\n");                                           \
    }                                                                     \
} while (0)


/*-----------------------------------.
| Print this symbol's value on YYO.  |
`-----------------------------------*/

static void
yy_symbol_value_print (FILE *yyo,
                       yysymbol_kind_t yykind, YYSTYPE const * const yyvaluep)
{
  FILE *yyoutput = yyo;
  YY_USE (yyoutput);
  if (!yyvaluep)
    return;
  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  YY_USE (yykind);
  YY_IGNORE_MAYBE_UNINITIALIZED_END
}


/*---------------------------.
| Print this symbol on YYO.  |
`---------------------------*/

static void
yy_symbol_print (FILE *yyo,
                 yysymbol_kind_t yykind, YYSTYPE const * const yyvaluep)
{
  YYFPRINTF (yyo, "%s %s (",
             yykind < YYNTOKENS ? "token" : "nterm", yysymbol_name (yykind));

  yy_symbol_value_print (yyo, yykind, yyvaluep);
  YYFPRINTF (yyo, ")");
}

/*------------------------------------------------------------------.
| yy_stack_print -- Print the state stack from its BOTTOM up to its |
| TOP (included).                                                   |
`------------------------------------------------------------------*/

static void
yy_stack_print (yy_state_t *yybottom, yy_state_t *yytop)
{
  YYFPRINTF (stderr, "Stack now");
  for (; yybottom <= yytop; yybottom++)
    {
      int yybot = *yybottom;
      YYFPRINTF (stderr, " %d", yybot);
    }
  YYFPRINTF (stderr, "\n");
}

# define YY_STACK_PRINT(Bottom, Top)                            \
do {                                                            \
  if (yydebug)                                                  \
    yy_stack_print ((Bottom), (Top));                           \
} while (0)


/*------------------------------------------------.
| Report that the YYRULE is going to be reduced.  |
`------------------------------------------------*/

static void
yy_reduce_print (yy_state_t *yyssp, YYSTYPE *yyvsp,
                 int yyrule)
{
  int yylno = yyrline[yyrule];
  int yynrhs = yyr2[yyrule];
  int yyi;
  YYFPRINTF (stderr, "Reducing stack by rule %d (line %d):\n",
             yyrule - 1, yylno);
  /* The symbols being reduced.  */
  for (yyi = 0; yyi < yynrhs; yyi++)
    {
      YYFPRINTF (stderr, "   $%d = ", yyi + 1);
      yy_symbol_print (stderr,
                       YY_ACCESSING_SYMBOL (+yyssp[yyi + 1 - yynrhs]),
                       &yyvsp[(yyi + 1) - (yynrhs)]);
      YYFPRINTF (stderr, "\n");
    }
}

# define YY_REDUCE_PRINT(Rule)          \
do {                                    \
  if (yydebug)                          \
    yy_reduce_print (yyssp, yyvsp, Rule); \
} while (0)

/* Nonzero means print parse trace.  It is left uninitialized so that
   multiple parsers can coexist.  */
int yydebug;
#else /* !YYDEBUG */
# define YYDPRINTF(Args) ((void) 0)
# define YY_SYMBOL_PRINT(Title, Kind, Value, Location)
# define YY_STACK_PRINT(Bottom, Top)
# define YY_REDUCE_PRINT(Rule)
#endif /* !YYDEBUG */


/* YYINITDEPTH -- initial size of the parser's stacks.  */
#ifndef YYINITDEPTH
# define YYINITDEPTH 200
#endif

/* YYMAXDEPTH -- maximum size the stacks can grow to (effective only
   if the built-in stack extension method is used).

   Do not make this value too large; the results are undefined if
   YYSTACK_ALLOC_MAXIMUM < YYSTACK_BYTES (YYMAXDEPTH)
   evaluated with infinite-precision integer arithmetic.  */

#ifndef YYMAXDEPTH
# define YYMAXDEPTH 10000
#endif






/*-----------------------------------------------.
| Release the memory associated to this symbol.  |
`-----------------------------------------------*/

static void
yydestruct (const char *yymsg,
            yysymbol_kind_t yykind, YYSTYPE *yyvaluep)
{
  YY_USE (yyvaluep);
  if (!yymsg)
    yymsg = "Deleting";
  YY_SYMBOL_PRINT (yymsg, yykind, yyvaluep, yylocationp);

  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  YY_USE (yykind);
  YY_IGNORE_MAYBE_UNINITIALIZED_END
}


/* Lookahead token kind.  */
int yychar;

/* The semantic value of the lookahead symbol.  */
YYSTYPE yylval;
/* Number of syntax errors so far.  */
int yynerrs;




/*----------.
| yyparse.  |
`----------*/

int
yyparse (void)
{
    yy_state_fast_t yystate = 0;
    /* Number of tokens to shift before error messages enabled.  */
    int yyerrstatus = 0;

    /* Refer to the stacks through separate pointers, to allow yyoverflow
       to reallocate them elsewhere.  */

    /* Their size.  */
    YYPTRDIFF_T yystacksize = YYINITDEPTH;

    /* The state stack: array, bottom, top.  */
    yy_state_t yyssa[YYINITDEPTH];
    yy_state_t *yyss = yyssa;
    yy_state_t *yyssp = yyss;

    /* The semantic value stack: array, bottom, top.  */
    YYSTYPE yyvsa[YYINITDEPTH];
    YYSTYPE *yyvs = yyvsa;
    YYSTYPE *yyvsp = yyvs;

  int yyn;
  /* The return value of yyparse.  */
  int yyresult;
  /* Lookahead symbol kind.  */
  yysymbol_kind_t yytoken = YYSYMBOL_YYEMPTY;
  /* The variables used to return semantic value and location from the
     action routines.  */
  YYSTYPE yyval;



#define YYPOPSTACK(N)   (yyvsp -= (N), yyssp -= (N))

  /* The number of symbols on the RHS of the reduced rule.
     Keep to zero when no symbol should be popped.  */
  int yylen = 0;

  YYDPRINTF ((stderr, "Starting parse\n"));

  yychar = YYEMPTY; /* Cause a token to be read.  */

  goto yysetstate;


/*------------------------------------------------------------.
| yynewstate -- push a new state, which is found in yystate.  |
`------------------------------------------------------------*/
yynewstate:
  /* In all cases, when you get here, the value and location stacks
     have just been pushed.  So pushing a state here evens the stacks.  */
  yyssp++;


/*--------------------------------------------------------------------.
| yysetstate -- set current state (the top of the stack) to yystate.  |
`--------------------------------------------------------------------*/
yysetstate:
  YYDPRINTF ((stderr, "Entering state %d\n", yystate));
  YY_ASSERT (0 <= yystate && yystate < YYNSTATES);
  YY_IGNORE_USELESS_CAST_BEGIN
  *yyssp = YY_CAST (yy_state_t, yystate);
  YY_IGNORE_USELESS_CAST_END
  YY_STACK_PRINT (yyss, yyssp);

  if (yyss + yystacksize - 1 <= yyssp)
#if !defined yyoverflow && !defined YYSTACK_RELOCATE
    YYNOMEM;
#else
    {
      /* Get the current used size of the three stacks, in elements.  */
      YYPTRDIFF_T yysize = yyssp - yyss + 1;

# if defined yyoverflow
      {
        /* Give user a chance to reallocate the stack.  Use copies of
           these so that the &'s don't force the real ones into
           memory.  */
        yy_state_t *yyss1 = yyss;
        YYSTYPE *yyvs1 = yyvs;

        /* Each stack pointer address is followed by the size of the
           data in use in that stack, in bytes.  This used to be a
           conditional around just the two extra args, but that might
           be undefined if yyoverflow is a macro.  */
        yyoverflow (YY_("memory exhausted"),
                    &yyss1, yysize * YYSIZEOF (*yyssp),
                    &yyvs1, yysize * YYSIZEOF (*yyvsp),
                    &yystacksize);
        yyss = yyss1;
        yyvs = yyvs1;
      }
# else /* defined YYSTACK_RELOCATE */
      /* Extend the stack our own way.  */
      if (YYMAXDEPTH <= yystacksize)
        YYNOMEM;
      yystacksize *= 2;
      if (YYMAXDEPTH < yystacksize)
        yystacksize = YYMAXDEPTH;

      {
        yy_state_t *yyss1 = yyss;
        union yyalloc *yyptr =
          YY_CAST (union yyalloc *,
                   YYSTACK_ALLOC (YY_CAST (YYSIZE_T, YYSTACK_BYTES (yystacksize))));
        if (! yyptr)
          YYNOMEM;
        YYSTACK_RELOCATE (yyss_alloc, yyss);
        YYSTACK_RELOCATE (yyvs_alloc, yyvs);
#  undef YYSTACK_RELOCATE
        if (yyss1 != yyssa)
          YYSTACK_FREE (yyss1);
      }
# endif

      yyssp = yyss + yysize - 1;
      yyvsp = yyvs + yysize - 1;

      YY_IGNORE_USELESS_CAST_BEGIN
      YYDPRINTF ((stderr, "Stack size increased to %ld\n",
                  YY_CAST (long, yystacksize)));
      YY_IGNORE_USELESS_CAST_END

      if (yyss + yystacksize - 1 <= yyssp)
        YYABORT;
    }
#endif /* !defined yyoverflow && !defined YYSTACK_RELOCATE */


  if (yystate == YYFINAL)
    YYACCEPT;

  goto yybackup;


/*-----------.
| yybackup.  |
`-----------*/
yybackup:
  /* Do appropriate processing given the current state.  Read a
     lookahead token if we need one and don't already have one.  */

  /* First try to decide what to do without reference to lookahead token.  */
  yyn = yypact[yystate];
  if (yypact_value_is_default (yyn))
    goto yydefault;

  /* Not known => get a lookahead token if don't already have one.  */

  /* YYCHAR is either empty, or end-of-input, or a valid lookahead.  */
  if (yychar == YYEMPTY)
    {
      YYDPRINTF ((stderr, "Reading a token\n"));
      yychar = yylex ();
    }

  if (yychar <= YYEOF)
    {
      yychar = YYEOF;
      yytoken = YYSYMBOL_YYEOF;
      YYDPRINTF ((stderr, "Now at end of input.\n"));
    }
  else if (yychar == YYerror)
    {
      /* The scanner already issued an error message, process directly
         to error recovery.  But do not keep the error token as
         lookahead, it is too special and may lead us to an endless
         loop in error recovery. */
      yychar = YYUNDEF;
      yytoken = YYSYMBOL_YYerror;
      goto yyerrlab1;
    }
  else
    {
      yytoken = YYTRANSLATE (yychar);
      YY_SYMBOL_PRINT ("Next token is", yytoken, &yylval, &yylloc);
    }

  /* If the proper action on seeing token YYTOKEN is to reduce or to
     detect an error, take that action.  */
  yyn += yytoken;
  if (yyn < 0 || YYLAST < yyn || yycheck[yyn] != yytoken)
    goto yydefault;
  yyn = yytable[yyn];
  if (yyn <= 0)
    {
      if (yytable_value_is_error (yyn))
        goto yyerrlab;
      yyn = -yyn;
      goto yyreduce;
    }

  /* Count tokens shifted since error; after three, turn off error
     status.  */
  if (yyerrstatus)
    yyerrstatus--;

  /* Shift the lookahead token.  */
  YY_SYMBOL_PRINT ("Shifting", yytoken, &yylval, &yylloc);
  yystate = yyn;
  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  *++yyvsp = yylval;
  YY_IGNORE_MAYBE_UNINITIALIZED_END

  /* Discard the shifted token.  */
  yychar = YYEMPTY;
  goto yynewstate;


/*-----------------------------------------------------------.
| yydefault -- do the default action for the current state.  |
`-----------------------------------------------------------*/
yydefault:
  yyn = yydefact[yystate];
  if (yyn == 0)
    goto yyerrlab;
  goto yyreduce;


/*-----------------------------.
| yyreduce -- do a reduction.  |
`-----------------------------*/
yyreduce:
  /* yyn is the number of a rule to reduce with.  */
  yylen = yyr2[yyn];

  /* If YYLEN is nonzero, implement the default value of the action:
     '$$ = $1'.

     Otherwise, the following line sets YYVAL to garbage.
     This behavior is undocumented and Bison
     users should not rely upon it.  Assigning to YYVAL
     unconditionally makes the parser a bit smaller, and it avoids a
     GCC warning that YYVAL may be used uninitialized.  */
  yyval = yyvsp[1-yylen];


  YY_REDUCE_PRINT (yyn);
  switch (yyn)
    {
  case 2: /* pluribusModule: ':' EXPORT unitDef  */
#line 54 "pluribus.y"
{
    setExport(YC(genericDef,yyvsp[0]));
    YRESULT(yyvsp[0]);
}
#line 2145 "y.tab.c"
    break;

  case 3: /* pluribusModule: ':'  */
#line 59 "pluribus.y"
{
    YRESULT(NULL);
}
#line 2153 "y.tab.c"
    break;

  case 4: /* unitDef: UNIT defSymbol '{' unitElems '}'  */
#line 67 "pluribus.y"
{
    yyval = YH_BUILD(unitDef)(info(), YC(symbolDef,yyvsp[-3]), YC(elemList,yyvsp[-1]));
}
#line 2161 "y.tab.c"
    break;

  case 5: /* unitUse: UNIT scopedRef ';'  */
#line 74 "pluribus.y"
{
    yyval = YH_BUILD(unitRef)(YC(symbolRef,yyvsp[-1]), NONE);
}
#line 2169 "y.tab.c"
    break;

  case 6: /* unitUse: EXPORT UNIT scopedRef ';'  */
#line 78 "pluribus.y"
{
    yyval = YH_BUILD(unitRef)(YC(symbolRef,yyvsp[-1]), EXPORT);
}
#line 2177 "y.tab.c"
    break;

  case 7: /* unitUse: ELEVATE UNIT scopedRef ';'  */
#line 82 "pluribus.y"
{
    yyval = YH_BUILD(unitRef)(YC(symbolRef,yyvsp[-1]), ELEVATE);
}
#line 2185 "y.tab.c"
    break;

  case 8: /* unitUse: UNIT '{' unitElems '}'  */
#line 86 "pluribus.y"
{
    yyval = YH_BUILD(unitDef)(genInfo(), defSymbol(gensym()), YC(elemList,yyvsp[-1]));
}
#line 2193 "y.tab.c"
    break;

  case 9: /* unitUse: EXPORT UNIT '{' unitElems '}'  */
#line 90 "pluribus.y"
{
    yyval = YH_BUILD(unitDef)(genInfo(), defSymbol(gensym()), YC(elemList,yyvsp[-1]));
    setExport(YC(genericDef,yyval));
}
#line 2202 "y.tab.c"
    break;

  case 10: /* unitElems: unitElemList  */
#line 98 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2210 "y.tab.c"
    break;

  case 11: /* unitElems: %empty  */
#line 102 "pluribus.y"
{
    yyval = NULL;
}
#line 2218 "y.tab.c"
    break;

  case 12: /* unitElemList: unitElem  */
#line 109 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), NULL);
}
#line 2226 "y.tab.c"
    break;

  case 13: /* unitElemList: unitElemList unitElem  */
#line 113 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), YC(elemList,yyvsp[-1]));
}
#line 2234 "y.tab.c"
    break;

  case 14: /* unitElem: definitionStatement  */
#line 120 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2242 "y.tab.c"
    break;

  case 15: /* unitElem: EXPORT definitionStatement  */
#line 124 "pluribus.y"
{
    setExport(YC(genericDef,yyvsp[0]));
    yyval = yyvsp[0];
}
#line 2251 "y.tab.c"
    break;

  case 16: /* unitElem: codeAtt  */
#line 129 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2259 "y.tab.c"
    break;

  case 17: /* unitElem: importAtt  */
#line 133 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2267 "y.tab.c"
    break;

  case 18: /* unitElem: packageAtt  */
#line 137 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2275 "y.tab.c"
    break;

  case 19: /* unitElem: unitUse  */
#line 141 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2283 "y.tab.c"
    break;

  case 20: /* unitElem: unumImplUse  */
#line 145 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2291 "y.tab.c"
    break;

  case 21: /* definitionStatement: attributeDef  */
#line 152 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2299 "y.tab.c"
    break;

  case 22: /* definitionStatement: ingredientImplDef  */
#line 156 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2307 "y.tab.c"
    break;

  case 23: /* definitionStatement: kindDef  */
#line 160 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2315 "y.tab.c"
    break;

  case 24: /* definitionStatement: presenceImplDef  */
#line 164 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2323 "y.tab.c"
    break;

  case 25: /* definitionStatement: presenceStructureDef  */
#line 168 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2331 "y.tab.c"
    break;

  case 26: /* definitionStatement: publishDef  */
#line 172 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2339 "y.tab.c"
    break;

  case 27: /* definitionStatement: remoteDef  */
#line 176 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2347 "y.tab.c"
    break;

  case 28: /* definitionStatement: typeDef  */
#line 180 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2355 "y.tab.c"
    break;

  case 29: /* definitionStatement: unitDef  */
#line 184 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2363 "y.tab.c"
    break;

  case 30: /* definitionStatement: unumImplDef  */
#line 188 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2371 "y.tab.c"
    break;

  case 31: /* definitionStatement: unumStructureDef  */
#line 192 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2379 "y.tab.c"
    break;

  case 32: /* attributeType: booleanType  */
#line 200 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2387 "y.tab.c"
    break;

  case 33: /* attributeType: charType  */
#line 204 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2395 "y.tab.c"
    break;

  case 34: /* attributeType: LONG  */
#line 208 "pluribus.y"
{
    yyval = YH_BUILD(primType)(LONG);
}
#line 2403 "y.tab.c"
    break;

  case 35: /* attributeType: stringType  */
#line 212 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2411 "y.tab.c"
    break;

  case 36: /* attributeDef: ATTRIBUTE defSymbol attributeType ';'  */
#line 219 "pluribus.y"
{
    yyval = YH_BUILD(attributeDef)(info(), YC(symbolDef,yyvsp[-2]), YC(typeSpec,yyvsp[-1]));
}
#line 2419 "y.tab.c"
    break;

  case 37: /* attributeDef: ATTRIBUTE defSymbol ';'  */
#line 223 "pluribus.y"
{
    yyval = YH_BUILD(attributeDef)(info(), YC(symbolDef,yyvsp[-1]), NULL);
}
#line 2427 "y.tab.c"
    break;

  case 38: /* assignment: scopedRef '=' expr ';'  */
#line 230 "pluribus.y"
{
    yyval = YH_BUILD(attributeRef)(YC(symbolRef,yyvsp[-3]), YC(expr,yyvsp[-1]));
}
#line 2435 "y.tab.c"
    break;

  case 39: /* assignment: scopedRef ';'  */
#line 234 "pluribus.y"
{
    yyval = YH_BUILD(attributeRef)(YC(symbolRef,yyvsp[-1]), NULL);
}
#line 2443 "y.tab.c"
    break;

  case 40: /* assignment: unitUse  */
#line 238 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2451 "y.tab.c"
    break;

  case 41: /* requireAtt: REQUIRE expr ';'  */
#line 245 "pluribus.y"
{
    yyval = YH_BUILD(requireAtt)(YC(expr,yyvsp[-1]), NULL);
}
#line 2459 "y.tab.c"
    break;

  case 42: /* codeAtt: codeModifiers codeType defSymbol codeInherits methodCode  */
#line 252 "pluribus.y"
{
    yyval = YH_BUILD(codeDef)(info(), YC(symbolDef,yyvsp[-2]), YC(codeModifierList,yyvsp[-4]),
               yyvsp[-3], YC(codeInheritList,yyvsp[-1]), YC(string,yyvsp[0]));
}
#line 2468 "y.tab.c"
    break;

  case 43: /* codeInherit: EXTENDS mangledSymbolList  */
#line 259 "pluribus.y"
{
    yyval = YH_BUILD(codeInherit)(EXTENDS, YC(pluribusTypeList,yyvsp[0]));
}
#line 2476 "y.tab.c"
    break;

  case 44: /* codeInherit: IMPLEMENTS mangledSymbolList  */
#line 263 "pluribus.y"
{
    yyval = YH_BUILD(codeInherit)(IMPLEMENTS, YC(pluribusTypeList,yyvsp[0]));
}
#line 2484 "y.tab.c"
    break;

  case 45: /* codeInheritList: codeInherit  */
#line 270 "pluribus.y"
{
    yyval = YH_BUILD(codeInheritList)(YC(codeInherit,yyvsp[0]), NULL);
}
#line 2492 "y.tab.c"
    break;

  case 46: /* codeInheritList: codeInheritList codeInherit  */
#line 274 "pluribus.y"
{
    yyval = YH_BUILD(codeInheritList)(YC(codeInherit,yyvsp[0]), YC(codeInheritList,yyvsp[-1]));
}
#line 2500 "y.tab.c"
    break;

  case 47: /* codeInherits: codeInheritList  */
#line 282 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2508 "y.tab.c"
    break;

  case 48: /* codeInherits: %empty  */
#line 286 "pluribus.y"
{
    yyval = NULL;
}
#line 2516 "y.tab.c"
    break;

  case 49: /* codeModifier: ABSTRACT  */
#line 293 "pluribus.y"
{
    yyval = YH_BUILD(codeModifier)(ABSTRACT);
}
#line 2524 "y.tab.c"
    break;

  case 50: /* codeModifier: FINAL  */
#line 297 "pluribus.y"
{
    yyval = YH_BUILD(codeModifier)(FINAL);
}
#line 2532 "y.tab.c"
    break;

  case 51: /* codeModifier: PUBLIC  */
#line 301 "pluribus.y"
{
    yyval = YH_BUILD(codeModifier)(PUBLIC);
}
#line 2540 "y.tab.c"
    break;

  case 52: /* codeModifierList: codeModifier  */
#line 308 "pluribus.y"
{
    yyval = YH_BUILD(codeModifierList)(YC(codeModifier,yyvsp[0]), NULL);
}
#line 2548 "y.tab.c"
    break;

  case 53: /* codeModifierList: codeModifierList codeModifier  */
#line 312 "pluribus.y"
{
    yyval = YH_BUILD(codeModifierList)(YC(codeModifier,yyvsp[0]),
                    YC(codeModifierList,yyvsp[-1]));
}
#line 2557 "y.tab.c"
    break;

  case 54: /* codeModifiers: codeModifierList  */
#line 320 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2565 "y.tab.c"
    break;

  case 55: /* codeModifiers: %empty  */
#line 324 "pluribus.y"
{
    yyval = NULL;
}
#line 2573 "y.tab.c"
    break;

  case 56: /* codeType: CLASS  */
#line 331 "pluribus.y"
{
    yyval = CLASS;
}
#line 2581 "y.tab.c"
    break;

  case 57: /* codeType: INTERFACE  */
#line 335 "pluribus.y"
{
    yyval = INTERFACE;
}
#line 2589 "y.tab.c"
    break;

  case 58: /* codeType: ECLASS  */
#line 339 "pluribus.y"
{
    yyval = ECLASS;
}
#line 2597 "y.tab.c"
    break;

  case 59: /* codeType: EINTERFACE  */
#line 343 "pluribus.y"
{
    yyval = EINTERFACE;
}
#line 2605 "y.tab.c"
    break;

  case 60: /* mangledSymbolElem: Symbol  */
#line 350 "pluribus.y"
{
    yyval = YH_BUILD(pluribusType)
             (YC(symbolRef,YH_BUILD(symbolRef) (YC(symbol,yyvsp[0]), NULL)),
          FALSE);
}
#line 2615 "y.tab.c"
    break;

  case 61: /* mangledSymbolElem: KIND Symbol  */
#line 356 "pluribus.y"
{
    yyval = YH_BUILD(pluribusType)
             (YC(symbolRef,YH_BUILD(symbolRef)(YC(symbol,yyvsp[0]), NULL)),
          KIND);
}
#line 2625 "y.tab.c"
    break;

  case 62: /* mangledSymbolList: mangledSymbolElem  */
#line 365 "pluribus.y"
{
    yyval = YH_BUILD(pluribusTypeList)(YC(pluribusType,yyvsp[0]), NULL);
}
#line 2633 "y.tab.c"
    break;

  case 63: /* mangledSymbolList: mangledSymbolList ',' mangledSymbolElem  */
#line 369 "pluribus.y"
{
    yyval = YH_BUILD(pluribusTypeList)(YC(pluribusType,yyvsp[0]),
                    YC(pluribusTypeList,yyvsp[-2]));
}
#line 2642 "y.tab.c"
    break;

  case 64: /* prototypeDecl: Symbol '(' parameterDeclList ')' throwsList ';'  */
#line 377 "pluribus.y"
{
    yyval = YH_BUILD(protoDef)(YC(symbol,yyvsp[-5]), YC(parameterDeclList,yyvsp[-3]),
                            YC(scopedRefList,yyvsp[-1]));
}
#line 2651 "y.tab.c"
    break;

  case 65: /* prototypeDecl: Symbol '(' ')' throwsList ';'  */
#line 382 "pluribus.y"
{
    yyval = YH_BUILD(protoDef)(YC(symbol,yyvsp[-4]), NULL, YC(scopedRefList,yyvsp[-1]));
}
#line 2659 "y.tab.c"
    break;

  case 66: /* prototypeDecl: INIT '(' parameterDeclList ')' throwsList ';'  */
#line 386 "pluribus.y"
{
    yyval = YH_BUILD(protoDef)(initSym, YC(parameterDeclList,yyvsp[-3]),
                            YC(scopedRefList,yyvsp[-1]));
}
#line 2668 "y.tab.c"
    break;

  case 67: /* prototypeDecl: INIT '(' ')' throwsList ';'  */
#line 391 "pluribus.y"
{
    yyval = YH_BUILD(protoDef)(initSym, NULL, YC(scopedRefList,yyvsp[0]));
}
#line 2676 "y.tab.c"
    break;

  case 68: /* parameterDeclList: parameterDecl  */
#line 398 "pluribus.y"
{
    yyval = YH_BUILD(parameterDeclList)(YC(parameterDecl,yyvsp[0]), NULL);
}
#line 2684 "y.tab.c"
    break;

  case 69: /* parameterDeclList: parameterDeclList ',' parameterDecl  */
#line 402 "pluribus.y"
{
    yyval = YH_BUILD(parameterDeclList)(YC(parameterDecl,yyvsp[0]),
                                     YC(parameterDeclList,yyvsp[-2]));
}
#line 2693 "y.tab.c"
    break;

  case 71: /* arrayMarkers: arrayMarker  */
#line 414 "pluribus.y"
{
    yyval = 1;
}
#line 2701 "y.tab.c"
    break;

  case 72: /* arrayMarkers: arrayMarkers arrayMarker  */
#line 418 "pluribus.y"
{
    yyval = yyvsp[-1] + 1;
}
#line 2709 "y.tab.c"
    break;

  case 73: /* parameterDecl: type arrayMarkers Symbol  */
#line 425 "pluribus.y"
{
    yyval = YH_BUILD(parameterDecl)(YC(typeSpec,yyvsp[-2]), YC(symbol,yyvsp[0]), yyvsp[-1]);
}
#line 2717 "y.tab.c"
    break;

  case 74: /* parameterDecl: type Symbol arrayMarkers  */
#line 429 "pluribus.y"
{
    yyval = YH_BUILD(parameterDecl)(YC(typeSpec,yyvsp[-2]), YC(symbol,yyvsp[-1]), yyvsp[-1]);
}
#line 2725 "y.tab.c"
    break;

  case 75: /* parameterDecl: type Symbol  */
#line 433 "pluribus.y"
{
    yyval = YH_BUILD(parameterDecl)(YC(typeSpec,yyvsp[-1]), YC(symbol,yyvsp[0]), 0);
}
#line 2733 "y.tab.c"
    break;

  case 76: /* parameterDecl: type  */
#line 437 "pluribus.y"
{
    yyval = YH_BUILD(parameterDecl)(YC(typeSpec,yyvsp[0]), NULL, 0);
}
#line 2741 "y.tab.c"
    break;

  case 77: /* kindDef: KIND defSymbol '{' kindElems '}'  */
#line 445 "pluribus.y"
{
    yyval = YH_BUILD(kindDef)(info(), YC(symbolDef,yyvsp[-3]), YC(elemList,yyvsp[-1]));
}
#line 2749 "y.tab.c"
    break;

  case 78: /* commaNameList: Symbol  */
#line 452 "pluribus.y"
{
    yyval = YH_BUILD(symbolList)(YC(symbol,yyvsp[0]), NULL);
}
#line 2757 "y.tab.c"
    break;

  case 79: /* commaNameList: commaNameList ',' Symbol  */
#line 456 "pluribus.y"
{
    yyval = YH_BUILD(symbolList)(YC(symbol,yyvsp[0]), YC(symbolList,yyvsp[-2]));
}
#line 2765 "y.tab.c"
    break;

  case 80: /* commaScopedRefList: scopedRef  */
#line 463 "pluribus.y"
{
    yyval = YH_BUILD(scopedRefList)(YC(scopedRef,yyvsp[0]), NULL);
}
#line 2773 "y.tab.c"
    break;

  case 81: /* commaScopedRefList: commaScopedRefList ',' scopedRef  */
#line 467 "pluribus.y"
{
    yyval = YH_BUILD(scopedRefList)(YC(scopedRef,yyvsp[0]), YC(scopedRefList,yyvsp[-2]));
}
#line 2781 "y.tab.c"
    break;

  case 82: /* kindUse: KIND scopedRef ';'  */
#line 474 "pluribus.y"
{
    yyval = YH_BUILD(kindRef)(YC(symbolRef,yyvsp[-1]));
}
#line 2789 "y.tab.c"
    break;

  case 83: /* kindUse: KIND '{' kindElems '}'  */
#line 478 "pluribus.y"
{
    YT(symbol) *anon = gensym();
    YT(elem) *elem1 = YC(elem,YBUILD(kindDef)(
        genInfo(), defSymbol(anon), YC(elemList,yyvsp[-1])));
    YT(elem) *elem2 = YC(elem,YBUILD(kindRef)(refSymbol(anon)));
    YT(elemList) *list = YBUILD(elemList)(elem1, NULL);
                  list = YBUILD(elemList)(elem2, list);
    yyval = YH_BUILD(nestedElem)(list);
}
#line 2803 "y.tab.c"
    break;

  case 84: /* kindUseNoSemi: KIND scopedRef  */
#line 491 "pluribus.y"
{
    yyval = YH_BUILD(kindRef)(YC(symbolRef,yyvsp[0]));
}
#line 2811 "y.tab.c"
    break;

  case 85: /* kindUseNoSemi: KIND '{' kindElems '}'  */
#line 495 "pluribus.y"
{
    YT(symbol) *anon = gensym();
    YT(elem) *elem1 = YC(elem,YBUILD(kindDef)(
        genInfo(), defSymbol(anon), YC(elemList,yyvsp[-1])));
    YT(elem) *elem2 = YC(elem,YBUILD(kindRef)(refSymbol(anon)));
    YT(elemList) *list = YBUILD(elemList)(elem1, NULL);
                  list = YBUILD(elemList)(elem2, list);
    yyval = YH_BUILD(nestedElem)(list);
}
#line 2825 "y.tab.c"
    break;

  case 86: /* kindElems: kindElemList  */
#line 508 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2833 "y.tab.c"
    break;

  case 87: /* kindElems: %empty  */
#line 512 "pluribus.y"
{
    yyval = NULL;
}
#line 2841 "y.tab.c"
    break;

  case 88: /* kindElemList: kindElem  */
#line 519 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), NULL);
}
#line 2849 "y.tab.c"
    break;

  case 89: /* kindElemList: kindElemList kindElem  */
#line 523 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), YC(elemList,yyvsp[-1]));
}
#line 2857 "y.tab.c"
    break;

  case 90: /* kindElem: prototypeDecl  */
#line 530 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2865 "y.tab.c"
    break;

  case 91: /* kindElem: requireAtt  */
#line 534 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2873 "y.tab.c"
    break;

  case 92: /* kindElem: kindUse  */
#line 538 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2881 "y.tab.c"
    break;

  case 93: /* kindElem: implementsAtt  */
#line 542 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2889 "y.tab.c"
    break;

  case 94: /* ingredientImplDef: INGREDIENT IMPL defSymbol '{' ingredientImplElems '}'  */
#line 550 "pluribus.y"
{
    yyval = YH_BUILD(ingredientImplDef)(info(), YC(symbolDef,yyvsp[-3]),
                                     YC(elemList,yyvsp[-1]));
}
#line 2898 "y.tab.c"
    break;

  case 95: /* ingredientImplUse: IMPL scopedRef ';'  */
#line 558 "pluribus.y"
{
    yyval = YH_BUILD(ingredientImplRef)(YC(symbolRef,yyvsp[-1]));
}
#line 2906 "y.tab.c"
    break;

  case 96: /* ingredientImplUse: IMPL '{' ingredientImplElems '}'  */
#line 562 "pluribus.y"
{
    YT(symbol) *anon = gensym();
    YT(elem) *elem1 = YC(elem,YBUILD(ingredientImplDef)(
        genInfo(), defSymbol(anon), YC(elemList,yyvsp[-1])));
    YT(elem) *elem2 = YC(elem,YBUILD(ingredientImplRef)(refSymbol(anon)));
    YT(elemList) *list = YBUILD(elemList)(elem1, NULL);
                  list = YBUILD(elemList)(elem2, list);
    yyval = YH_BUILD(nestedElem)(list);
}
#line 2920 "y.tab.c"
    break;

  case 97: /* ingredientImplElems: ingredientImplElemList  */
#line 575 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2928 "y.tab.c"
    break;

  case 98: /* ingredientImplElems: %empty  */
#line 579 "pluribus.y"
{
    yyval = NULL;
}
#line 2936 "y.tab.c"
    break;

  case 99: /* ingredientImplElemList: ingredientImplElem  */
#line 586 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), NULL);
}
#line 2944 "y.tab.c"
    break;

  case 100: /* ingredientImplElemList: ingredientImplElemList ingredientImplElem  */
#line 590 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), YC(elemList,yyvsp[-1]));
}
#line 2952 "y.tab.c"
    break;

  case 101: /* ingredientImplElem: assignment  */
#line 597 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2960 "y.tab.c"
    break;

  case 102: /* ingredientImplElem: kindUse  */
#line 601 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2968 "y.tab.c"
    break;

  case 103: /* ingredientImplElem: neighborAtt  */
#line 605 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2976 "y.tab.c"
    break;

  case 104: /* ingredientImplElem: stateBundleAtt  */
#line 609 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2984 "y.tab.c"
    break;

  case 105: /* ingredientImplElem: variableDecl  */
#line 613 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 2992 "y.tab.c"
    break;

  case 106: /* ingredientImplElem: functionAtt  */
#line 617 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3000 "y.tab.c"
    break;

  case 107: /* ingredientImplElem: methodAtt  */
#line 621 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3008 "y.tab.c"
    break;

  case 108: /* ingredientImplElem: initBlockAtt  */
#line 625 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3016 "y.tab.c"
    break;

  case 109: /* ingredientImplElem: dataAtt  */
#line 629 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3024 "y.tab.c"
    break;

  case 110: /* ingredientImplElem: importAtt  */
#line 633 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3032 "y.tab.c"
    break;

  case 111: /* ingredientImplElem: implementsAtt  */
#line 637 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3040 "y.tab.c"
    break;

  case 112: /* neighborAtt: NEIGHBOR INGREDIENT Symbol kindUse  */
#line 644 "pluribus.y"
{
    yyval = YH_BUILD(neighborAtt)(YC(symbol,yyvsp[-1]), FALSE, FALSE, YC(genericRef,yyvsp[0]));
}
#line 3048 "y.tab.c"
    break;

  case 113: /* neighborAtt: NEIGHBOR PRESENCE Symbol plurality kindUse  */
#line 648 "pluribus.y"
{
    yyval = YH_BUILD(neighborAtt)(YC(symbol,yyvsp[-2]), yyvsp[-1], TRUE, YC(genericRef,yyvsp[0]));
}
#line 3056 "y.tab.c"
    break;

  case 114: /* stateBundleAtt: STATE scopedRef Symbol valueOrNot ';'  */
#line 655 "pluribus.y"
{
    yyval = YH_BUILD(stateBundleDef)(YC(scopedRef,yyvsp[-3]), YC(symbol,yyvsp[-2]),
                  YC(string,yyvsp[-1]));
}
#line 3065 "y.tab.c"
    break;

  case 115: /* $@1: %empty  */
#line 662 "pluribus.y"
            { ExpectInitialization = TRUE; }
#line 3071 "y.tab.c"
    break;

  case 116: /* valueOrNot: '=' $@1 Initialization  */
#line 663 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3079 "y.tab.c"
    break;

  case 117: /* valueOrNot: %empty  */
#line 667 "pluribus.y"
{
    yyval = NULL;
}
#line 3087 "y.tab.c"
    break;

  case 118: /* variableDecl: modifiers type Symbol arrayMarkers valueOrNot ';'  */
#line 674 "pluribus.y"
{
    yyval = YH_BUILD(variableDecl)
            (YC(typeSpec,yyvsp[-4]), yyvsp[-5], YC(symbol,yyvsp[-3]), yyvsp[-2], YC(string,yyvsp[-1]));
}
#line 3096 "y.tab.c"
    break;

  case 119: /* variableDecl: modifiers type arrayMarkers Symbol valueOrNot ';'  */
#line 679 "pluribus.y"
{
    yyval = YH_BUILD(variableDecl)
             (YC(typeSpec,yyvsp[-4]), yyvsp[-5], YC(symbol,yyvsp[-2]), yyvsp[-3], YC(string,yyvsp[-1]));
}
#line 3105 "y.tab.c"
    break;

  case 120: /* variableDecl: modifiers type Symbol valueOrNot ';'  */
#line 684 "pluribus.y"
{
    yyval = YH_BUILD(variableDecl)
             (YC(typeSpec,yyvsp[-3]), yyvsp[-4], YC(symbol,yyvsp[-2]), 0, YC(string,yyvsp[-1]));
}
#line 3114 "y.tab.c"
    break;

  case 121: /* variableDecl: type Symbol arrayMarkers valueOrNot ';'  */
#line 689 "pluribus.y"
{
    yyval = YH_BUILD(variableDecl)
            (YC(typeSpec,yyvsp[-4]), NULL, YC(symbol,yyvsp[-3]), yyvsp[-2], YC(string,yyvsp[-1]));
}
#line 3123 "y.tab.c"
    break;

  case 122: /* variableDecl: type arrayMarkers Symbol valueOrNot ';'  */
#line 694 "pluribus.y"
{
    yyval = YH_BUILD(variableDecl)
             (YC(typeSpec,yyvsp[-4]), NULL, YC(symbol,yyvsp[-2]), yyvsp[-3], YC(string,yyvsp[-1]));
}
#line 3132 "y.tab.c"
    break;

  case 123: /* variableDecl: type Symbol valueOrNot ';'  */
#line 699 "pluribus.y"
{
    yyval = YH_BUILD(variableDecl)
             (YC(typeSpec,yyvsp[-3]), NULL, YC(symbol,yyvsp[-2]), 0, YC(string,yyvsp[-1]));
}
#line 3141 "y.tab.c"
    break;

  case 124: /* throwsList: THROWS commaScopedRefList  */
#line 707 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3149 "y.tab.c"
    break;

  case 125: /* throwsList: %empty  */
#line 711 "pluribus.y"
{
    yyval = NULL;
}
#line 3157 "y.tab.c"
    break;

  case 126: /* functionAtt: FUNCTION modifiersOrNot type Symbol '(' parameterDeclList ')' throwsList methodCode  */
#line 720 "pluribus.y"
{
    yyval = YH_BUILD(functionAtt)(yyvsp[-7], YC(typeSpec,yyvsp[-6]), YC(symbol,yyvsp[-5]),
                   YC(parameterDeclList,yyvsp[-3]), YC(scopedRefList,yyvsp[-1]),
                   YC(string,yyvsp[0]));
}
#line 3167 "y.tab.c"
    break;

  case 127: /* functionAtt: FUNCTION modifiersOrNot type Symbol '(' ')' throwsList methodCode  */
#line 726 "pluribus.y"
{
    yyval = YH_BUILD(functionAtt)(yyvsp[-6], YC(typeSpec,yyvsp[-5]), YC(symbol,yyvsp[-4]),
                   NULL, YC(scopedRefList,yyvsp[-1]), YC(string,yyvsp[0]));
}
#line 3176 "y.tab.c"
    break;

  case 128: /* modifiersOrNot: modifierList  */
#line 733 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3184 "y.tab.c"
    break;

  case 129: /* modifiersOrNot: %empty  */
#line 737 "pluribus.y"
{
    yyval = 0;
}
#line 3192 "y.tab.c"
    break;

  case 130: /* modifiers: modifierList  */
#line 744 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3200 "y.tab.c"
    break;

  case 131: /* modifierList: modifier  */
#line 751 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3208 "y.tab.c"
    break;

  case 132: /* modifierList: modifierList modifier  */
#line 755 "pluribus.y"
{
    yyval = yyvsp[-1] | yyvsp[0];
}
#line 3216 "y.tab.c"
    break;

  case 133: /* modifier: PUBLIC  */
#line 762 "pluribus.y"
{
    yyval = MOD_PUBLIC;
}
#line 3224 "y.tab.c"
    break;

  case 134: /* modifier: PRIVATE  */
#line 766 "pluribus.y"
{
    yyval = MOD_PRIVATE;
}
#line 3232 "y.tab.c"
    break;

  case 135: /* modifier: PROTECTED  */
#line 770 "pluribus.y"
{
    yyval = MOD_PROTECTED;
}
#line 3240 "y.tab.c"
    break;

  case 136: /* modifier: STATIC  */
#line 774 "pluribus.y"
{
    yyval = MOD_STATIC;
}
#line 3248 "y.tab.c"
    break;

  case 137: /* modifier: FINAL  */
#line 778 "pluribus.y"
{
    yyval = MOD_FINAL;
}
#line 3256 "y.tab.c"
    break;

  case 138: /* initBlockAtt: PRIME INIT '(' parameterDeclList ')' throwsList methodCode  */
#line 785 "pluribus.y"
{
    yyval = YH_BUILD(initBlockAtt(PRIME, YC(parameterDeclList,yyvsp[-3]),
                   YC(scopedRefList,yyvsp[-1]), YC(string,yyvsp[0])));
}
#line 3265 "y.tab.c"
    break;

  case 139: /* initBlockAtt: PRIME INIT '(' ')' throwsList methodCode  */
#line 790 "pluribus.y"
{
    yyval = YH_BUILD(initBlockAtt(PRIME, NULL, YC(scopedRefList,yyvsp[-1]),
                               YC(string,yyvsp[0])));
}
#line 3274 "y.tab.c"
    break;

  case 140: /* initBlockAtt: FACET INIT '(' parameterDeclList ')' throwsList methodCode  */
#line 795 "pluribus.y"
{
    yyval = YH_BUILD(initBlockAtt(FACET, YC(parameterDeclList,yyvsp[-3]),
                   YC(scopedRefList,yyvsp[-1]), YC(string,yyvsp[0])));
}
#line 3283 "y.tab.c"
    break;

  case 141: /* initBlockAtt: FACET INIT '(' ')' throwsList methodCode  */
#line 800 "pluribus.y"
{
    yyval = YH_BUILD(initBlockAtt(FACET, NULL, YC(scopedRefList,yyvsp[-1]),
                               YC(string,yyvsp[0])));
}
#line 3292 "y.tab.c"
    break;

  case 142: /* initBlockAtt: INIT '(' parameterDeclList ')' throwsList methodCode  */
#line 805 "pluribus.y"
{
    yyval = YH_BUILD(initBlockAtt(INIT, YC(parameterDeclList,yyvsp[-3]),
                               YC(scopedRefList,yyvsp[-1]), YC(string,yyvsp[0])));
}
#line 3301 "y.tab.c"
    break;

  case 143: /* initBlockAtt: INIT '(' ')' throwsList methodCode  */
#line 810 "pluribus.y"
{
    yyval = YH_BUILD(initBlockAtt(INIT, NULL, YC(scopedRefList,yyvsp[-1]),
                               YC(string,yyvsp[0])));
}
#line 3310 "y.tab.c"
    break;

  case 144: /* initBlockAtt: INIT throwsList methodCode  */
#line 815 "pluribus.y"
{
    yyval = YH_BUILD(initBlockAtt(INIT, NULL, YC(scopedRefList,yyvsp[-1]),
                               YC(string,yyvsp[0])));
}
#line 3319 "y.tab.c"
    break;

  case 145: /* methodAtt: METHOD Symbol '(' parameterDeclList ')' throwsList methodCode  */
#line 823 "pluribus.y"
{
    yyval = YH_BUILD(emethodAtt)(YC(symbol,yyvsp[-5]), YC(parameterDeclList,yyvsp[-3]),
                  YC(scopedRefList,yyvsp[-1]), YC(string,yyvsp[0]));
}
#line 3328 "y.tab.c"
    break;

  case 146: /* methodAtt: METHOD Symbol '(' ')' throwsList methodCode  */
#line 828 "pluribus.y"
{
    yyval = YH_BUILD(emethodAtt)(YC(symbol,yyvsp[-4]), NULL, YC(scopedRefList,yyvsp[-1]),
                  YC(string,yyvsp[0]));
}
#line 3337 "y.tab.c"
    break;

  case 147: /* $@2: %empty  */
#line 835 "pluribus.y"
            { ExpectCodeBlock = TRUE; }
#line 3343 "y.tab.c"
    break;

  case 148: /* methodCode: '{' $@2 CodeBlock  */
#line 836 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3351 "y.tab.c"
    break;

  case 149: /* $@3: %empty  */
#line 842 "pluribus.y"
                 { ExpectHexBlock = TRUE; }
#line 3357 "y.tab.c"
    break;

  case 150: /* dataAtt: DATA '{' $@3 HexBlock  */
#line 843 "pluribus.y"
{
    yyval = YH_BUILD(dataAtt)(YC(string,yyvsp[0]));
}
#line 3365 "y.tab.c"
    break;

  case 151: /* dataAtt: DATA String ';'  */
#line 847 "pluribus.y"
{
    yyval = YH_BUILD(dataAtt)(YC(string,yyvsp[-1]));
}
#line 3373 "y.tab.c"
    break;

  case 152: /* packageAtt: PACKAGE scopedRef ';'  */
#line 854 "pluribus.y"
{
    yyval = YH_BUILD(packageAtt)(YC(symbolRef,yyvsp[-1]));
}
#line 3381 "y.tab.c"
    break;

  case 153: /* importAtt: IMPORT scopedRef ';'  */
#line 861 "pluribus.y"
{
    yyval = YH_BUILD(importAtt)(YC(symbolRef,yyvsp[-1]), FALSE);
}
#line 3389 "y.tab.c"
    break;

  case 154: /* importAtt: IMPORT scopedRef '.' '*' ';'  */
#line 865 "pluribus.y"
{
    yyval = YH_BUILD(importAtt)(YC(symbolRef,yyvsp[-3]), TRUE);
}
#line 3397 "y.tab.c"
    break;

  case 155: /* implementsAtt: IMPLEMENTS scopedRef ';'  */
#line 872 "pluribus.y"
{
    yyval = YH_BUILD(implementsAtt)(YC(symbolRef,yyvsp[-1]));
}
#line 3405 "y.tab.c"
    break;

  case 156: /* presenceStructureDef: PRESENCE STRUCTURE defSymbol '{' presenceStructureElems '}'  */
#line 879 "pluribus.y"
{
    yyval = YH_BUILD(presenceStructureDef)(info(), YC(symbolDef,yyvsp[-3]),
                                        YC(elemList,yyvsp[-1]));
}
#line 3414 "y.tab.c"
    break;

  case 157: /* presenceStructureUse: STRUCTURE scopedRef ';'  */
#line 887 "pluribus.y"
{
    yyval = YH_BUILD(presenceStructureRef)(YC(symbolRef,yyvsp[-1]));
}
#line 3422 "y.tab.c"
    break;

  case 158: /* presenceStructureUse: STRUCTURE '{' presenceStructureElems '}'  */
#line 891 "pluribus.y"
{
    YT(symbol) *anon = gensym();
    YT(elem) *elem1 = YC(elem,YBUILD(presenceStructureDef)(
        genInfo(), defSymbol(anon), YC(elemList,yyvsp[-1])));
    YT(elem) *elem2 = YC(elem,YBUILD(presenceStructureRef)(refSymbol(anon)));
    YT(elemList) *list = YBUILD(elemList)(elem1, NULL);
                  list = YBUILD(elemList)(elem2, list);
    yyval = YH_BUILD(nestedElem)(list);
}
#line 3436 "y.tab.c"
    break;

  case 159: /* presenceStructureElems: presenceStructureElemList  */
#line 904 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3444 "y.tab.c"
    break;

  case 160: /* presenceStructureElems: %empty  */
#line 908 "pluribus.y"
{
    yyval = NULL;
}
#line 3452 "y.tab.c"
    break;

  case 161: /* presenceStructureElemList: presenceStructureElem  */
#line 915 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), NULL);
}
#line 3460 "y.tab.c"
    break;

  case 162: /* presenceStructureElemList: presenceStructureElemList presenceStructureElem  */
#line 919 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), YC(elemList,yyvsp[-1]));
}
#line 3468 "y.tab.c"
    break;

  case 163: /* presenceStructureElem: assignment  */
#line 926 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3476 "y.tab.c"
    break;

  case 164: /* presenceStructureElem: requireAtt  */
#line 930 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3484 "y.tab.c"
    break;

  case 165: /* presenceStructureElem: kindUse  */
#line 934 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3492 "y.tab.c"
    break;

  case 166: /* presenceStructureElem: ingredientAtt  */
#line 938 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3500 "y.tab.c"
    break;

  case 167: /* ingredientAtt: INGREDIENT Symbol '{' ingredientAttElems '}'  */
#line 945 "pluribus.y"
{
    yyval = YH_BUILD(ingredientAtt)(YC(symbol,yyvsp[-3]), YC(elemList,yyvsp[-1]));
}
#line 3508 "y.tab.c"
    break;

  case 168: /* ingredientAttElems: ingredientAttElemList  */
#line 952 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3516 "y.tab.c"
    break;

  case 169: /* ingredientAttElems: %empty  */
#line 956 "pluribus.y"
{
    yyval = NULL;
}
#line 3524 "y.tab.c"
    break;

  case 170: /* ingredientAttElemList: ingredientAttElem  */
#line 963 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), NULL);
}
#line 3532 "y.tab.c"
    break;

  case 171: /* ingredientAttElemList: ingredientAttElemList ingredientAttElem  */
#line 967 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), YC(elemList,yyvsp[-1]));
}
#line 3540 "y.tab.c"
    break;

  case 172: /* ingredientAttElem: kindUse  */
#line 974 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3548 "y.tab.c"
    break;

  case 173: /* ingredientAttElem: deliverAtt  */
#line 978 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3556 "y.tab.c"
    break;

  case 174: /* deliverSym: Symbol  */
#line 985 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3564 "y.tab.c"
    break;

  case 175: /* deliverSym: INIT  */
#line 989 "pluribus.y"
{
    yyval = (long)initSym; /* KSSHack */
}
#line 3572 "y.tab.c"
    break;

  case 176: /* deliverAtt: DELIVER scope deliverSym TO deliverSym ';'  */
#line 995 "pluribus.y"
{
    yyval = YH_BUILD(deliverAtt)(yyvsp[-4], YC(symbol,yyvsp[-3]), YC(symbol,yyvsp[-1]), NULL);
}
#line 3580 "y.tab.c"
    break;

  case 177: /* deliverAtt: DELIVER scope deliverSym ';'  */
#line 999 "pluribus.y"
{
    yyval = YH_BUILD(deliverAtt)(yyvsp[-2], YC(symbol,yyvsp[-1]), NULL, NULL);
}
#line 3588 "y.tab.c"
    break;

  case 178: /* scope: PRESENCE  */
#line 1006 "pluribus.y"
{
    yyval = PRESENCE;
}
#line 3596 "y.tab.c"
    break;

  case 179: /* scope: UNUM  */
#line 1010 "pluribus.y"
{
    yyval = UNUM;
}
#line 3604 "y.tab.c"
    break;

  case 180: /* presenceImplDef: PRESENCE IMPL defSymbol '{' presenceImplElems '}'  */
#line 1018 "pluribus.y"
{
    yyval = YH_BUILD(presenceImplDef)(info(), YC(symbolDef,yyvsp[-3]), YC(elemList,yyvsp[-1]));
}
#line 3612 "y.tab.c"
    break;

  case 181: /* presenceImplUse: IMPL scopedRef ';'  */
#line 1025 "pluribus.y"
{
    yyval = YH_BUILD(presenceImplRef)(YC(symbolRef,yyvsp[-1]));
}
#line 3620 "y.tab.c"
    break;

  case 182: /* presenceImplUse: IMPL '{' presenceImplElems '}'  */
#line 1029 "pluribus.y"
{
    YT(symbol) *anon = gensym();
    YT(elem) *elem1 = YC(elem,YBUILD(presenceImplDef)(
        genInfo(), defSymbol(anon), YC(elemList,yyvsp[-1])));
    YT(elem) *elem2 = YC(elem,YBUILD(presenceImplRef)(refSymbol(anon)));
    YT(elemList) *list = YBUILD(elemList)(elem1, NULL);
                  list = YBUILD(elemList)(elem2, list);
    yyval = YH_BUILD(nestedElem)(list);
}
#line 3634 "y.tab.c"
    break;

  case 183: /* presenceImplElems: presenceImplElemList  */
#line 1042 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3642 "y.tab.c"
    break;

  case 184: /* presenceImplElems: %empty  */
#line 1046 "pluribus.y"
{
    yyval = NULL;
}
#line 3650 "y.tab.c"
    break;

  case 185: /* presenceImplElemList: presenceImplElem  */
#line 1053 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), NULL);
}
#line 3658 "y.tab.c"
    break;

  case 186: /* presenceImplElemList: presenceImplElemList presenceImplElem  */
#line 1057 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), YC(elemList,yyvsp[-1]));
}
#line 3666 "y.tab.c"
    break;

  case 187: /* presenceImplElem: assignment  */
#line 1064 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3674 "y.tab.c"
    break;

  case 188: /* presenceImplElem: presenceBehavior  */
#line 1068 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3682 "y.tab.c"
    break;

  case 189: /* presenceImplElem: initBlockAtt  */
#line 1072 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3690 "y.tab.c"
    break;

  case 190: /* presenceImplElem: presenceStructureUse  */
#line 1076 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3698 "y.tab.c"
    break;

  case 191: /* presenceImplElem: templateAtt  */
#line 1080 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3706 "y.tab.c"
    break;

  case 192: /* presenceImplElem: makeAtt  */
#line 1084 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3714 "y.tab.c"
    break;

  case 193: /* presenceImplElem: implementsAtt  */
#line 1088 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3722 "y.tab.c"
    break;

  case 194: /* presenceBehavior: PRESENCEBEHAVIOR commaNameList ';'  */
#line 1095 "pluribus.y"
{
    yyval = YH_BUILD(presenceBehavior)(YC(symbolList,yyvsp[-1]));
}
#line 3730 "y.tab.c"
    break;

  case 195: /* templateAtt: INGREDIENT commaNameList templateDef  */
#line 1102 "pluribus.y"
{
    yyval = YH_BUILD(templateAtt)(YC(symbolList,yyvsp[-1]), YC(templateDef,yyvsp[0]));
}
#line 3738 "y.tab.c"
    break;

  case 196: /* makeAtt: MAKE Symbol ';'  */
#line 1122 "pluribus.y"
{
    yyval = YH_BUILD(makeAtt)(YC(symbol,yyvsp[-1]), NULL);
}
#line 3746 "y.tab.c"
    break;

  case 197: /* makeAtt: MAKE Symbol commaNameList ';'  */
#line 1126 "pluribus.y"
{
    yyval = YH_BUILD(makeAtt)(YC(symbol,yyvsp[-2]), YC(symbolList,yyvsp[-1]));
}
#line 3754 "y.tab.c"
    break;

  case 198: /* templateDef: TEMPLATE '{' templateElems '}'  */
#line 1133 "pluribus.y"
{
    yyval = YH_BUILD(templateDef)(info(), YC(elemList,yyvsp[-1]));
}
#line 3762 "y.tab.c"
    break;

  case 199: /* templateElems: templateElemList  */
#line 1140 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3770 "y.tab.c"
    break;

  case 200: /* templateElems: %empty  */
#line 1144 "pluribus.y"
{
    yyval = NULL;
}
#line 3778 "y.tab.c"
    break;

  case 201: /* templateElemList: templateElem  */
#line 1151 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), NULL);
}
#line 3786 "y.tab.c"
    break;

  case 202: /* templateElemList: templateElemList templateElem  */
#line 1155 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), YC(elemList,yyvsp[-1]));
}
#line 3794 "y.tab.c"
    break;

  case 203: /* templateElem: ingredientImplUse  */
#line 1162 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3802 "y.tab.c"
    break;

  case 204: /* templateElem: mapAtt  */
#line 1166 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3810 "y.tab.c"
    break;

  case 205: /* mapScope: NEIGHBOR  */
#line 1173 "pluribus.y"
{
    yyval = NEIGHBOR;
}
#line 3818 "y.tab.c"
    break;

  case 206: /* mapScope: STATE  */
#line 1177 "pluribus.y"
{
    yyval = STATE;
}
#line 3826 "y.tab.c"
    break;

  case 207: /* mapAtt: MAP mapScope Symbol TO PNULL ';'  */
#line 1184 "pluribus.y"
{
    yyval = YH_BUILD(mapAtt)(yyvsp[-4], YC(symbol,yyvsp[-3]), NULL);
}
#line 3834 "y.tab.c"
    break;

  case 208: /* mapAtt: MAP mapScope Symbol TO Symbol ';'  */
#line 1189 "pluribus.y"
{
    yyval = YH_BUILD(mapAtt)(yyvsp[-4], YC(symbol,yyvsp[-3]), YC(symbol,yyvsp[-1]));
}
#line 3842 "y.tab.c"
    break;

  case 209: /* mapAtt: MAP mapScope Symbol ';'  */
#line 1193 "pluribus.y"
{
    yyval = YH_BUILD(mapAtt)(yyvsp[-2], NULL, YC(symbol,yyvsp[-1]));
}
#line 3850 "y.tab.c"
    break;

  case 210: /* exprList: expr  */
#line 1200 "pluribus.y"
{
    yyval = YH_BUILD(exprList)(YC(expr,yyvsp[0]), NULL);
}
#line 3858 "y.tab.c"
    break;

  case 211: /* exprList: exprList ',' expr  */
#line 1204 "pluribus.y"
{
    yyval = YH_BUILD(exprList)(YC(expr,yyvsp[0]), YC(exprList,yyvsp[-2]));
}
#line 3866 "y.tab.c"
    break;

  case 212: /* unumStructureDef: UNUM STRUCTURE defSymbol '{' unumStructureElems '}'  */
#line 1212 "pluribus.y"
{
    yyval = YH_BUILD(unumStructureDef)(info(), YC(symbolDef,yyvsp[-3]), YC(elemList,yyvsp[-1]));
}
#line 3874 "y.tab.c"
    break;

  case 213: /* unumStructureUse: STRUCTURE scopedRef ';'  */
#line 1219 "pluribus.y"
{
    yyval = YH_BUILD(unumStructureRef)(YC(symbolRef,yyvsp[-1]));
}
#line 3882 "y.tab.c"
    break;

  case 214: /* unumStructureUse: STRUCTURE '{' unumStructureElems '}'  */
#line 1223 "pluribus.y"
{
    YT(symbol) *anon = gensym();
    YT(elem) *elem1 = YC(elem,YBUILD(unumStructureDef)(
        genInfo(), defSymbol(anon), YC(elemList,yyvsp[-1])));
    YT(elem) *elem2 = YC(elem,YBUILD(unumStructureRef)(refSymbol(anon)));
    YT(elemList) *list = YBUILD(elemList)(elem1, NULL);
                  list = YBUILD(elemList)(elem2, list);
    yyval = YH_BUILD(nestedElem)(list);
}
#line 3896 "y.tab.c"
    break;

  case 215: /* unumStructureElems: unumStructureElemList  */
#line 1236 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3904 "y.tab.c"
    break;

  case 216: /* unumStructureElems: %empty  */
#line 1240 "pluribus.y"
{
    yyval = NULL;
}
#line 3912 "y.tab.c"
    break;

  case 217: /* unumStructureElemList: unumStructureElem  */
#line 1247 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), NULL);
}
#line 3920 "y.tab.c"
    break;

  case 218: /* unumStructureElemList: unumStructureElemList unumStructureElem  */
#line 1251 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), YC(elemList,yyvsp[-1]));
}
#line 3928 "y.tab.c"
    break;

  case 219: /* unumStructureElem: assignment  */
#line 1258 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3936 "y.tab.c"
    break;

  case 220: /* unumStructureElem: requireAtt  */
#line 1262 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3944 "y.tab.c"
    break;

  case 221: /* unumStructureElem: kindUse  */
#line 1266 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3952 "y.tab.c"
    break;

  case 222: /* unumStructureElem: primeAtt  */
#line 1270 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3960 "y.tab.c"
    break;

  case 223: /* unumStructureElem: presenceAtt  */
#line 1274 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 3968 "y.tab.c"
    break;

  case 224: /* primeAtt: PRIME Symbol ';'  */
#line 1281 "pluribus.y"
{
    yyval = YH_BUILD(primeAtt)(YC(symbol,yyvsp[-1]));
}
#line 3976 "y.tab.c"
    break;

  case 225: /* presenceAtt: PRESENCE Symbol kindUseNoSemi makes ';'  */
#line 1288 "pluribus.y"
{
    yyval = YH_BUILD(presenceAtt)(YC(symbol,yyvsp[-3]), YC(symbol,yyvsp[-1]), NULL,
                               YC(kindRef,yyvsp[-2]), FALSE);
}
#line 3985 "y.tab.c"
    break;

  case 226: /* presenceAtt: PRESENCE Symbol kindUseNoSemi MAKES '{' presenceConds '}'  */
#line 1293 "pluribus.y"
{
    yyval = YH_BUILD(presenceAtt)(YC(symbol,yyvsp[-5]), NULL, YC(presenceCondList,yyvsp[-1]),
                               YC(kindRef,yyvsp[-4]), FALSE);
}
#line 3994 "y.tab.c"
    break;

  case 227: /* presenceConds: presenceCondList  */
#line 1301 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4002 "y.tab.c"
    break;

  case 228: /* presenceConds: %empty  */
#line 1305 "pluribus.y"
{
    yyval = NULL;
}
#line 4010 "y.tab.c"
    break;

  case 229: /* presenceCondList: presenceCond  */
#line 1312 "pluribus.y"
{
    yyval = YH_BUILD(presenceCondList)(YC(presenceCond,yyvsp[0]), NULL);
}
#line 4018 "y.tab.c"
    break;

  case 230: /* presenceCondList: presenceCondList presenceCond  */
#line 1316 "pluribus.y"
{
    yyval = YH_BUILD(presenceCondList)(YC(presenceCond,yyvsp[0]),
                    YC(presenceCondList,yyvsp[-1]));
}
#line 4027 "y.tab.c"
    break;

  case 231: /* presenceCond: expr ':' Symbol ';'  */
#line 1324 "pluribus.y"
{
    yyval = YH_BUILD(presenceCond)(YC(expr,yyvsp[-3]), YC(symbol,yyvsp[-1]));
}
#line 4035 "y.tab.c"
    break;

  case 232: /* makes: MAKES Symbol  */
#line 1331 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4043 "y.tab.c"
    break;

  case 233: /* makes: MAKES NONE  */
#line 1335 "pluribus.y"
{
    yyval = NULL;
}
#line 4051 "y.tab.c"
    break;

  case 234: /* makes: %empty  */
#line 1339 "pluribus.y"
{
    yyval = NULL;
}
#line 4059 "y.tab.c"
    break;

  case 235: /* plurality: arrayMarker  */
#line 1346 "pluribus.y"
{
    yyval = TRUE;
}
#line 4067 "y.tab.c"
    break;

  case 236: /* plurality: %empty  */
#line 1350 "pluribus.y"
{
    yyval = FALSE;
}
#line 4075 "y.tab.c"
    break;

  case 237: /* unumImplDef: UNUM IMPL defSymbol '{' unumImplElems '}'  */
#line 1358 "pluribus.y"
{
    yyval = YH_BUILD(unumImplDef)(info(), YC(symbolDef,yyvsp[-3]), YC(elemList,yyvsp[-1]));
}
#line 4083 "y.tab.c"
    break;

  case 238: /* unumImplUse: IMPL scopedRef ';'  */
#line 1365 "pluribus.y"
{
    yyval = YH_BUILD(unumImplRef)(YC(symbolRef,yyvsp[-1]));
}
#line 4091 "y.tab.c"
    break;

  case 239: /* unumImplUse: IMPL '{' unumImplElems '}'  */
#line 1369 "pluribus.y"
{
    YT(symbol) *anon = gensym();
    YT(elem) *elem1 = YC(elem,YBUILD(unumImplDef)(
        genInfo(), defSymbol(anon), YC(elemList,yyvsp[-1])));
    YT(elem) *elem2 = YC(elem,YBUILD(unumImplRef)(refSymbol(anon)));
    YT(elemList) *list = YBUILD(elemList)(elem1, NULL);
                  list = YBUILD(elemList)(elem2, list);
    yyval = YH_BUILD(nestedElem)(list);
}
#line 4105 "y.tab.c"
    break;

  case 240: /* unumImplElems: unumImplElemList  */
#line 1382 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4113 "y.tab.c"
    break;

  case 241: /* unumImplElems: %empty  */
#line 1386 "pluribus.y"
{
    yyval = NULL;
}
#line 4121 "y.tab.c"
    break;

  case 242: /* unumImplElemList: unumImplElem  */
#line 1393 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), NULL);
}
#line 4129 "y.tab.c"
    break;

  case 243: /* unumImplElemList: unumImplElemList unumImplElem  */
#line 1397 "pluribus.y"
{
    yyval = YH_BUILD(elemList)(YC(elem,yyvsp[0]), YC(elemList,yyvsp[-1]));
}
#line 4137 "y.tab.c"
    break;

  case 244: /* unumImplElem: assignment  */
#line 1404 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4145 "y.tab.c"
    break;

  case 245: /* unumImplElem: unumStructureUse  */
#line 1408 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4153 "y.tab.c"
    break;

  case 246: /* unumImplElem: presenceImplAtt  */
#line 1412 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4161 "y.tab.c"
    break;

  case 247: /* presenceImplAtt: PRESENCE commaNameList presenceImplUse  */
#line 1419 "pluribus.y"
{
    yyval = YH_BUILD(presenceImplAtt)(YC(symbolList,yyvsp[-1]), YC(presenceImplRef,yyvsp[0]));
}
#line 4169 "y.tab.c"
    break;

  case 248: /* expr: exprA  */
#line 1426 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4177 "y.tab.c"
    break;

  case 249: /* expr: expr '?' expr ':' exprA  */
#line 1430 "pluribus.y"
{
    yyval = YH_BUILD(condop)(YC(expr,yyvsp[-4]), YC(expr,yyvsp[-2]), YC(expr,yyvsp[0]));
}
#line 4185 "y.tab.c"
    break;

  case 250: /* exprA: expr9  */
#line 1437 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4193 "y.tab.c"
    break;

  case 251: /* exprA: exprA Or expr9  */
#line 1441 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), Or, YC(expr,yyvsp[0]));
}
#line 4201 "y.tab.c"
    break;

  case 252: /* expr9: expr8  */
#line 1448 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4209 "y.tab.c"
    break;

  case 253: /* expr9: expr9 And expr8  */
#line 1452 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), And, YC(expr,yyvsp[0]));
}
#line 4217 "y.tab.c"
    break;

  case 254: /* expr8: expr7  */
#line 1459 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4225 "y.tab.c"
    break;

  case 255: /* expr8: expr8 '|' expr7  */
#line 1463 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), '|', YC(expr,yyvsp[0]));
}
#line 4233 "y.tab.c"
    break;

  case 256: /* expr7: expr6  */
#line 1470 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4241 "y.tab.c"
    break;

  case 257: /* expr7: expr7 '^' expr6  */
#line 1474 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), '^', YC(expr,yyvsp[0]));
}
#line 4249 "y.tab.c"
    break;

  case 258: /* expr6: expr5  */
#line 1481 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4257 "y.tab.c"
    break;

  case 259: /* expr6: expr6 '&' expr5  */
#line 1485 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), '&', YC(expr,yyvsp[0]));
}
#line 4265 "y.tab.c"
    break;

  case 260: /* expr5: expr4  */
#line 1492 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4273 "y.tab.c"
    break;

  case 261: /* expr5: expr5 Eq expr4  */
#line 1496 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), Eq, YC(expr,yyvsp[0]));
}
#line 4281 "y.tab.c"
    break;

  case 262: /* expr5: expr5 Neq expr4  */
#line 1500 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), Neq, YC(expr,yyvsp[0]));
}
#line 4289 "y.tab.c"
    break;

  case 263: /* expr4: expr3  */
#line 1507 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4297 "y.tab.c"
    break;

  case 264: /* expr4: expr4 '<' expr3  */
#line 1511 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), '<', YC(expr,yyvsp[0]));
}
#line 4305 "y.tab.c"
    break;

  case 265: /* expr4: expr4 Leq expr3  */
#line 1515 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), Leq, YC(expr,yyvsp[0]));
}
#line 4313 "y.tab.c"
    break;

  case 266: /* expr4: expr4 '>' expr3  */
#line 1519 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), '>', YC(expr,yyvsp[0]));
}
#line 4321 "y.tab.c"
    break;

  case 267: /* expr4: expr4 Geq expr3  */
#line 1523 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), Geq, YC(expr,yyvsp[0]));
}
#line 4329 "y.tab.c"
    break;

  case 268: /* expr3: expr2  */
#line 1530 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4337 "y.tab.c"
    break;

  case 269: /* expr3: expr3 Lsl expr2  */
#line 1534 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), Lsl, YC(expr,yyvsp[0]));
}
#line 4345 "y.tab.c"
    break;

  case 270: /* expr3: expr3 Lsr expr2  */
#line 1538 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), Lsr , YC(expr,yyvsp[0]));
}
#line 4353 "y.tab.c"
    break;

  case 271: /* expr3: expr3 Asr expr2  */
#line 1542 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), Asr , YC(expr,yyvsp[0]));
}
#line 4361 "y.tab.c"
    break;

  case 272: /* expr2: expr1  */
#line 1549 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4369 "y.tab.c"
    break;

  case 273: /* expr2: expr2 '+' expr1  */
#line 1553 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), '+', YC(expr,yyvsp[0]));
}
#line 4377 "y.tab.c"
    break;

  case 274: /* expr2: expr2 '-' expr1  */
#line 1557 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), '-', YC(expr,yyvsp[0]));
}
#line 4385 "y.tab.c"
    break;

  case 275: /* expr1: term  */
#line 1564 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4393 "y.tab.c"
    break;

  case 276: /* expr1: expr1 '*' term  */
#line 1568 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), '*', YC(expr,yyvsp[0]));
}
#line 4401 "y.tab.c"
    break;

  case 277: /* expr1: expr1 '/' term  */
#line 1572 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), '/', YC(expr,yyvsp[0]));
}
#line 4409 "y.tab.c"
    break;

  case 278: /* expr1: expr1 '%' term  */
#line 1576 "pluribus.y"
{
    yyval = YH_BUILD(binop)(YC(expr,yyvsp[-2]), '%', YC(expr,yyvsp[0]));
}
#line 4417 "y.tab.c"
    break;

  case 279: /* term: prim  */
#line 1583 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4425 "y.tab.c"
    break;

  case 280: /* term: '+' term  */
#line 1587 "pluribus.y"
{
    yyval = YH_BUILD(unop)('+', YC(expr,yyvsp[0]));
}
#line 4433 "y.tab.c"
    break;

  case 281: /* term: '-' term  */
#line 1591 "pluribus.y"
{
    yyval = YH_BUILD(unop)('-', YC(expr,yyvsp[0]));
}
#line 4441 "y.tab.c"
    break;

  case 282: /* term: '!' term  */
#line 1595 "pluribus.y"
{
    yyval = YH_BUILD(unop)('!', YC(expr,yyvsp[0]));
}
#line 4449 "y.tab.c"
    break;

  case 283: /* term: '~' term  */
#line 1599 "pluribus.y"
{
    yyval = YH_BUILD(unop)('~', YC(expr,yyvsp[0]));
}
#line 4457 "y.tab.c"
    break;

  case 284: /* prim: Number  */
#line 1606 "pluribus.y"
{
    yyval = YH_BUILD(numLit)(yyvsp[0]);
}
#line 4465 "y.tab.c"
    break;

  case 285: /* prim: Character  */
#line 1610 "pluribus.y"
{
    yyval = YH_BUILD(charLit)(yyvsp[0]);
}
#line 4473 "y.tab.c"
    break;

  case 286: /* prim: TagType  */
#line 1614 "pluribus.y"
{
    yyval = YH_BUILD(tagLit)(yyvsp[0]);
}
#line 4481 "y.tab.c"
    break;

  case 287: /* prim: String  */
#line 1618 "pluribus.y"
{
    yyval = YH_BUILD(stringLit)(YC(string,yyvsp[0]));
}
#line 4489 "y.tab.c"
    break;

  case 288: /* prim: '(' expr ')'  */
#line 1622 "pluribus.y"
{
    yyval = yyvsp[-1];
}
#line 4497 "y.tab.c"
    break;

  case 289: /* prim: scopedRef  */
#line 1626 "pluribus.y"
{
    yyval = YH_BUILD(refTerm)(YC(symbolRef,yyvsp[0]));
}
#line 4505 "y.tab.c"
    break;

  case 290: /* prim: FALSEX  */
#line 1630 "pluribus.y"
{
    yyval = YH_BUILD(boolLit)(FALSE);
}
#line 4513 "y.tab.c"
    break;

  case 291: /* prim: TRUEX  */
#line 1634 "pluribus.y"
{
    yyval = YH_BUILD(boolLit)(TRUE);
}
#line 4521 "y.tab.c"
    break;

  case 292: /* type: typeSpec  */
#line 1641 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4529 "y.tab.c"
    break;

  case 293: /* remoteDef: REMOTE String Symbol ';'  */
#line 1648 "pluribus.y"
{
    yyval = YH_BUILD(remoteDef)(info(), YC(string,yyvsp[-2]), YC(symbol,yyvsp[-1]));
}
#line 4537 "y.tab.c"
    break;

  case 294: /* publishDef: PUBLISH Symbol ';'  */
#line 1655 "pluribus.y"
{
    yyval = YH_BUILD(publishDef)(info(), YC(symbol,yyvsp[-1]));
}
#line 4545 "y.tab.c"
    break;

  case 295: /* defSymbol: directRef  */
#line 1662 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4553 "y.tab.c"
    break;

  case 296: /* directRef: Symbol  */
#line 1669 "pluribus.y"
{
    yyval = YH_BUILD(symbolRef)(YC(symbol,yyvsp[0]), NULL);
}
#line 4561 "y.tab.c"
    break;

  case 297: /* directRef: Symbol '(' ')'  */
#line 1673 "pluribus.y"
{
    yyval = YH_BUILD(symbolRef)(YC(symbol,yyvsp[-2]), YC(exprList,NULL));
}
#line 4569 "y.tab.c"
    break;

  case 298: /* directRef: Symbol '(' exprList ')'  */
#line 1677 "pluribus.y"
{
    yyval = YH_BUILD(symbolRef)(YC(symbol,yyvsp[-3]), YC(exprList,yyvsp[-1]));
}
#line 4577 "y.tab.c"
    break;

  case 299: /* scopedRef: directRef  */
#line 1684 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4585 "y.tab.c"
    break;

  case 300: /* scopedRef: scopedRef '.' directRef  */
#line 1688 "pluribus.y"
{
    if (YTAG_OF(YC(symbolRef,yyvsp[-2])) == YTAG(symbolRef))
    yyval = YH_BUILD(scopedRef)(YC(scopedRef,
                    YH_BUILD(scopedRef)(NULL,
                            YC(symbolRef,yyvsp[-2]))),
                 YC(symbolRef,yyvsp[0]));
    else
    yyval = YH_BUILD(scopedRef)(YC(scopedRef,yyvsp[-2]),YC(symbolRef,yyvsp[0]));
}
#line 4599 "y.tab.c"
    break;

  case 301: /* scopedRef: outerScope '.' directRef  */
#line 1698 "pluribus.y"
{
    yyval = YH_BUILD(outerRef)(yyvsp[-2], YC(symbolRef,yyvsp[0]));
}
#line 4607 "y.tab.c"
    break;

  case 302: /* outerScope: '^'  */
#line 1705 "pluribus.y"
{
    yyval = 1;
}
#line 4615 "y.tab.c"
    break;

  case 303: /* outerScope: outerScope '^'  */
#line 1709 "pluribus.y"
{
    yyval = yyvsp[-1] + 1;
}
#line 4623 "y.tab.c"
    break;

  case 304: /* typeDef: TYPEDEF typeDeclarator ';'  */
#line 1719 "pluribus.y"
{
    yyval = YH_BUILD(typeDef)(info(), YC(typeDeclarator,yyvsp[-1]));
}
#line 4631 "y.tab.c"
    break;

  case 305: /* typeDef: structType  */
#line 1723 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4639 "y.tab.c"
    break;

  case 306: /* typeDef: unionType  */
#line 1727 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4647 "y.tab.c"
    break;

  case 307: /* typeDef: enumType  */
#line 1731 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4655 "y.tab.c"
    break;

  case 308: /* typeDeclarator: typeSpec declarators  */
#line 1738 "pluribus.y"
{
    yyval = YH_BUILD(typeDeclarator)(YC(typeSpec,yyvsp[-1]), YC(declaratorList,yyvsp[0]));
}
#line 4663 "y.tab.c"
    break;

  case 309: /* typeSpec: simpleTypeSpec  */
#line 1745 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4671 "y.tab.c"
    break;

  case 310: /* typeSpec: constrTypeSpec  */
#line 1749 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4679 "y.tab.c"
    break;

  case 311: /* simpleTypeSpec: baseTypeSpec  */
#line 1756 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4687 "y.tab.c"
    break;

  case 312: /* simpleTypeSpec: templateTypeSpec  */
#line 1760 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4695 "y.tab.c"
    break;

  case 313: /* simpleTypeSpec: scopedRef  */
#line 1764 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4703 "y.tab.c"
    break;

  case 314: /* simpleTypeSpec: KIND scopedRef  */
#line 1768 "pluribus.y"
{
    yyval = YH_BUILD(pluribusType)(YC(symbolRef,yyvsp[0]), KIND);
}
#line 4711 "y.tab.c"
    break;

  case 315: /* baseTypeSpec: floatingPtType  */
#line 1775 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4719 "y.tab.c"
    break;

  case 316: /* baseTypeSpec: integralType  */
#line 1779 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4727 "y.tab.c"
    break;

  case 317: /* baseTypeSpec: charType  */
#line 1783 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4735 "y.tab.c"
    break;

  case 318: /* baseTypeSpec: booleanType  */
#line 1787 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4743 "y.tab.c"
    break;

  case 319: /* templateTypeSpec: sequenceType  */
#line 1794 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4751 "y.tab.c"
    break;

  case 320: /* templateTypeSpec: stringType  */
#line 1798 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4759 "y.tab.c"
    break;

  case 321: /* constrTypeSpec: structType  */
#line 1805 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4767 "y.tab.c"
    break;

  case 322: /* constrTypeSpec: unionType  */
#line 1809 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4775 "y.tab.c"
    break;

  case 323: /* constrTypeSpec: enumType  */
#line 1813 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4783 "y.tab.c"
    break;

  case 324: /* declarators: declarator  */
#line 1820 "pluribus.y"
{
    yyval = YH_BUILD(declaratorList)(YC(declarator,yyvsp[0]), NULL);
}
#line 4791 "y.tab.c"
    break;

  case 325: /* declarators: declarators ',' declarator  */
#line 1824 "pluribus.y"
{
    yyval = YH_BUILD(declaratorList)(YC(declarator,yyvsp[0]), YC(declaratorList,yyvsp[0]));
}
#line 4799 "y.tab.c"
    break;

  case 326: /* declarator: simpleDeclarator  */
#line 1831 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4807 "y.tab.c"
    break;

  case 327: /* declarator: complexDeclarator  */
#line 1835 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4815 "y.tab.c"
    break;

  case 328: /* simpleDeclarator: Symbol  */
#line 1842 "pluribus.y"
{
    yyval = YH_BUILD(simpleDeclarator)(YC(symbol,yyvsp[0]));
}
#line 4823 "y.tab.c"
    break;

  case 329: /* complexDeclarator: arrayDeclarator  */
#line 1849 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4831 "y.tab.c"
    break;

  case 330: /* floatingPtType: FLOAT  */
#line 1856 "pluribus.y"
{
    yyval = YH_BUILD(primType)(FLOAT);
}
#line 4839 "y.tab.c"
    break;

  case 331: /* floatingPtType: DOUBLE  */
#line 1860 "pluribus.y"
{
    yyval = YH_BUILD(primType)(DOUBLE);
}
#line 4847 "y.tab.c"
    break;

  case 332: /* integralType: BYTE  */
#line 1867 "pluribus.y"
{
    yyval = YH_BUILD(primType)(BYTE);
}
#line 4855 "y.tab.c"
    break;

  case 333: /* integralType: SHORT  */
#line 1872 "pluribus.y"
{
    yyval = YH_BUILD(primType)(SHORT);
}
#line 4863 "y.tab.c"
    break;

  case 334: /* integralType: INT  */
#line 1876 "pluribus.y"
{
    yyval = YH_BUILD(primType)(INT);
}
#line 4871 "y.tab.c"
    break;

  case 335: /* integralType: LONG  */
#line 1880 "pluribus.y"
{
    yyval = YH_BUILD(primType)(LONG);
}
#line 4879 "y.tab.c"
    break;

  case 336: /* charType: CHAR  */
#line 1887 "pluribus.y"
{
    yyval = YH_BUILD(primType)(CHAR);
}
#line 4887 "y.tab.c"
    break;

  case 337: /* booleanType: BOOLEAN  */
#line 1894 "pluribus.y"
{
    yyval = YH_BUILD(primType)(BOOLEAN);
}
#line 4895 "y.tab.c"
    break;

  case 338: /* structType: STRUCT Symbol '{' memberList '}'  */
#line 1901 "pluribus.y"
{
    yyval = YH_BUILD(structTypeDecl)(info(), YC(symbol,yyvsp[-3]),
                                  YC(memberDeclList,yyvsp[-1]));
}
#line 4904 "y.tab.c"
    break;

  case 339: /* memberList: member  */
#line 1909 "pluribus.y"
{
    yyval = YH_BUILD(memberDeclList)(YC(memberDecl,yyvsp[0]), NULL);
}
#line 4912 "y.tab.c"
    break;

  case 340: /* memberList: memberList member  */
#line 1913 "pluribus.y"
{
    yyval = YH_BUILD(memberDeclList)(YC(memberDecl,yyvsp[0]), YC(memberDeclList,yyvsp[-1]));
}
#line 4920 "y.tab.c"
    break;

  case 341: /* member: typeSpec declarators ';'  */
#line 1920 "pluribus.y"
{
    yyval = YH_BUILD(memberDecl)(YC(typeSpec,yyvsp[-2]), YC(declaratorList,yyvsp[-1]));
}
#line 4928 "y.tab.c"
    break;

  case 342: /* unionType: UNION Symbol SWITCH '(' switchTypeSpec ')' '{' switchBody '}'  */
#line 1927 "pluribus.y"
{
    yyval = YH_BUILD(unionTypeDecl)(info(), YC(symbol,yyvsp[-7]), YC(typeSpec,yyvsp[-4]),
                                 YC(switchCaseDeclList,yyvsp[-1]));
}
#line 4937 "y.tab.c"
    break;

  case 343: /* switchTypeSpec: integralType  */
#line 1935 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4945 "y.tab.c"
    break;

  case 344: /* switchTypeSpec: charType  */
#line 1939 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4953 "y.tab.c"
    break;

  case 345: /* switchTypeSpec: booleanType  */
#line 1943 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4961 "y.tab.c"
    break;

  case 346: /* switchTypeSpec: enumType  */
#line 1947 "pluribus.y"
{
    yyval = yyvsp[0];
}
#line 4969 "y.tab.c"
    break;

  case 347: /* switchTypeSpec: Symbol  */
#line 1951 "pluribus.y"
{
    yyval = YH_BUILD(simpleDeclarator)(YC(symbol,yyvsp[0]));
}
#line 4977 "y.tab.c"
    break;

  case 348: /* switchBody: case  */
#line 1958 "pluribus.y"
{
    yyval = YH_BUILD(switchCaseDeclList)(YC(switchCaseDecl,yyvsp[0]), NULL);
}
#line 4985 "y.tab.c"
    break;

  case 349: /* switchBody: switchBody case  */
#line 1962 "pluribus.y"
{
    yyval = YH_BUILD(switchCaseDeclList)(YC(switchCaseDecl,yyvsp[0]),
                                      YC(switchCaseDeclList,yyvsp[-1]));
}
#line 4994 "y.tab.c"
    break;

  case 350: /* case: caseLabels elementSpec ';'  */
#line 1970 "pluribus.y"
{
    yyval = YH_BUILD(switchCaseDecl)(YC(caseLabelDeclList,yyvsp[-2]),
                                  YC(elementSpec,yyvsp[-1]));
}
#line 5003 "y.tab.c"
    break;

  case 351: /* caseLabels: caseLabel  */
#line 1978 "pluribus.y"
{
    yyval = YH_BUILD(caseLabelDeclList)(YC(caseLabelDecl,yyvsp[0]), NULL);
}
#line 5011 "y.tab.c"
    break;

  case 352: /* caseLabels: caseLabels caseLabel  */
#line 1982 "pluribus.y"
{
    yyval = YH_BUILD(caseLabelDeclList)(YC(caseLabelDecl,yyvsp[0]),
                                     YC(caseLabelDeclList,yyvsp[0]));
}
#line 5020 "y.tab.c"
    break;

  case 353: /* caseLabel: CASE expr ':'  */
#line 1990 "pluribus.y"
{
    yyval = YH_BUILD(caseLabelDecl)(YC(expr,yyvsp[-1]));
}
#line 5028 "y.tab.c"
    break;

  case 354: /* caseLabel: DEFAULT ':'  */
#line 1994 "pluribus.y"
{
    yyval = YH_BUILD(caseLabelDecl)(NULL);
}
#line 5036 "y.tab.c"
    break;

  case 355: /* elementSpec: typeSpec declarator  */
#line 2001 "pluribus.y"
{
    yyval = YH_BUILD(elementSpec)(YC(typeSpec,yyvsp[-1]), YC(declarator,yyvsp[0]));
}
#line 5044 "y.tab.c"
    break;

  case 356: /* enumType: ENUM Symbol '{' commaNameList '}'  */
#line 2008 "pluribus.y"
{
    yyval = YH_BUILD(enumTypeDecl)(info(), YC(symbol,yyvsp[-3]), YC(symbolList,yyvsp[-1]));
}
#line 5052 "y.tab.c"
    break;

  case 357: /* sequenceType: SEQUENCE '<' simpleTypeSpec ',' Number '>'  */
#line 2015 "pluribus.y"
{
    yyval = YH_BUILD(sequenceTypeDecl)(YC(typeSpec,yyvsp[-3]), yyvsp[-1]);
}
#line 5060 "y.tab.c"
    break;

  case 358: /* sequenceType: SEQUENCE '<' simpleTypeSpec '>'  */
#line 2019 "pluribus.y"
{
    yyval = YH_BUILD(sequenceTypeDecl)(YC(typeSpec,yyvsp[-1]), -1);
}
#line 5068 "y.tab.c"
    break;

  case 359: /* stringType: STRING '<' Number '>'  */
#line 2026 "pluribus.y"
{
    yyval = YH_BUILD(stringType)(yyvsp[-1]);
}
#line 5076 "y.tab.c"
    break;

  case 360: /* stringType: STRING  */
#line 2030 "pluribus.y"
{
    yyval = YH_BUILD(stringType)(-1);
}
#line 5084 "y.tab.c"
    break;

  case 361: /* arrayDeclarator: Symbol fixedArraySizes  */
#line 2037 "pluribus.y"
{
    yyval = YH_BUILD(arrayDeclarator)(YC(symbol,yyvsp[-1]), YC(arraySizeList,yyvsp[0]));
}
#line 5092 "y.tab.c"
    break;

  case 362: /* fixedArraySizes: fixedArraySize  */
#line 2044 "pluribus.y"
{
    yyval = YH_BUILD(arraySizeList)(YC(arraySize,yyvsp[0]), NULL);
}
#line 5100 "y.tab.c"
    break;

  case 363: /* fixedArraySizes: fixedArraySizes fixedArraySize  */
#line 2048 "pluribus.y"
{
    yyval = YH_BUILD(arraySizeList)(YC(arraySize,yyvsp[0]), YC(arraySizeList,yyvsp[-1]));
}
#line 5108 "y.tab.c"
    break;

  case 364: /* fixedArraySize: '[' Number ']'  */
#line 2055 "pluribus.y"
{
    yyval = YH_BUILD(arraySize)(yyvsp[-1]);
}
#line 5116 "y.tab.c"
    break;


#line 5120 "y.tab.c"

      default: break;
    }
  /* User semantic actions sometimes alter yychar, and that requires
     that yytoken be updated with the new translation.  We take the
     approach of translating immediately before every use of yytoken.
     One alternative is translating here after every semantic action,
     but that translation would be missed if the semantic action invokes
     YYABORT, YYACCEPT, or YYERROR immediately after altering yychar or
     if it invokes YYBACKUP.  In the case of YYABORT or YYACCEPT, an
     incorrect destructor might then be invoked immediately.  In the
     case of YYERROR or YYBACKUP, subsequent parser actions might lead
     to an incorrect destructor call or verbose syntax error message
     before the lookahead is translated.  */
  YY_SYMBOL_PRINT ("-> $$ =", YY_CAST (yysymbol_kind_t, yyr1[yyn]), &yyval, &yyloc);

  YYPOPSTACK (yylen);
  yylen = 0;

  *++yyvsp = yyval;

  /* Now 'shift' the result of the reduction.  Determine what state
     that goes to, based on the state we popped back to and the rule
     number reduced by.  */
  {
    const int yylhs = yyr1[yyn] - YYNTOKENS;
    const int yyi = yypgoto[yylhs] + *yyssp;
    yystate = (0 <= yyi && yyi <= YYLAST && yycheck[yyi] == *yyssp
               ? yytable[yyi]
               : yydefgoto[yylhs]);
  }

  goto yynewstate;


/*--------------------------------------.
| yyerrlab -- here on detecting error.  |
`--------------------------------------*/
yyerrlab:
  /* Make sure we have latest lookahead translation.  See comments at
     user semantic actions for why this is necessary.  */
  yytoken = yychar == YYEMPTY ? YYSYMBOL_YYEMPTY : YYTRANSLATE (yychar);
  /* If not already recovering from an error, report this error.  */
  if (!yyerrstatus)
    {
      ++yynerrs;
      yyerror (YY_("syntax error"));
    }

  if (yyerrstatus == 3)
    {
      /* If just tried and failed to reuse lookahead token after an
         error, discard it.  */

      if (yychar <= YYEOF)
        {
          /* Return failure if at end of input.  */
          if (yychar == YYEOF)
            YYABORT;
        }
      else
        {
          yydestruct ("Error: discarding",
                      yytoken, &yylval);
          yychar = YYEMPTY;
        }
    }

  /* Else will try to reuse lookahead token after shifting the error
     token.  */
  goto yyerrlab1;


/*---------------------------------------------------.
| yyerrorlab -- error raised explicitly by YYERROR.  |
`---------------------------------------------------*/
yyerrorlab:
  /* Pacify compilers when the user code never invokes YYERROR and the
     label yyerrorlab therefore never appears in user code.  */
  if (0)
    YYERROR;
  ++yynerrs;

  /* Do not reclaim the symbols of the rule whose action triggered
     this YYERROR.  */
  YYPOPSTACK (yylen);
  yylen = 0;
  YY_STACK_PRINT (yyss, yyssp);
  yystate = *yyssp;
  goto yyerrlab1;


/*-------------------------------------------------------------.
| yyerrlab1 -- common code for both syntax error and YYERROR.  |
`-------------------------------------------------------------*/
yyerrlab1:
  yyerrstatus = 3;      /* Each real token shifted decrements this.  */

  /* Pop stack until we find a state that shifts the error token.  */
  for (;;)
    {
      yyn = yypact[yystate];
      if (!yypact_value_is_default (yyn))
        {
          yyn += YYSYMBOL_YYerror;
          if (0 <= yyn && yyn <= YYLAST && yycheck[yyn] == YYSYMBOL_YYerror)
            {
              yyn = yytable[yyn];
              if (0 < yyn)
                break;
            }
        }

      /* Pop the current state because it cannot handle the error token.  */
      if (yyssp == yyss)
        YYABORT;


      yydestruct ("Error: popping",
                  YY_ACCESSING_SYMBOL (yystate), yyvsp);
      YYPOPSTACK (1);
      yystate = *yyssp;
      YY_STACK_PRINT (yyss, yyssp);
    }

  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  *++yyvsp = yylval;
  YY_IGNORE_MAYBE_UNINITIALIZED_END


  /* Shift the error token.  */
  YY_SYMBOL_PRINT ("Shifting", YY_ACCESSING_SYMBOL (yyn), yyvsp, yylsp);

  yystate = yyn;
  goto yynewstate;


/*-------------------------------------.
| yyacceptlab -- YYACCEPT comes here.  |
`-------------------------------------*/
yyacceptlab:
  yyresult = 0;
  goto yyreturnlab;


/*-----------------------------------.
| yyabortlab -- YYABORT comes here.  |
`-----------------------------------*/
yyabortlab:
  yyresult = 1;
  goto yyreturnlab;


/*-----------------------------------------------------------.
| yyexhaustedlab -- YYNOMEM (memory exhaustion) comes here.  |
`-----------------------------------------------------------*/
yyexhaustedlab:
  yyerror (YY_("memory exhausted"));
  yyresult = 2;
  goto yyreturnlab;


/*----------------------------------------------------------.
| yyreturnlab -- parsing is finished, clean up and return.  |
`----------------------------------------------------------*/
yyreturnlab:
  if (yychar != YYEMPTY)
    {
      /* Make sure we have latest lookahead translation.  See comments at
         user semantic actions for why this is necessary.  */
      yytoken = YYTRANSLATE (yychar);
      yydestruct ("Cleanup: discarding lookahead",
                  yytoken, &yylval);
    }
  /* Do not reclaim the symbols of the rule whose action triggered
     this YYABORT or YYACCEPT.  */
  YYPOPSTACK (yylen);
  YY_STACK_PRINT (yyss, yyssp);
  while (yyssp != yyss)
    {
      yydestruct ("Cleanup: popping",
                  YY_ACCESSING_SYMBOL (+*yyssp), yyvsp);
      YYPOPSTACK (1);
    }
#ifndef yyoverflow
  if (yyss != yyssa)
    YYSTACK_FREE (yyss);
#endif

  return yyresult;
}

#line 2061 "pluribus.y"


  void
yyerror(char *s)
{
    yh_error("%s", s);
    YRESULT(NULL);
}
