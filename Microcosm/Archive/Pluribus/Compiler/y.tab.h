/* A Bison parser, made by GNU Bison 3.8.2.  */

/* Bison interface for Yacc-like parsers in C

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

/* DO NOT RELY ON FEATURES THAT ARE NOT DOCUMENTED in the manual,
   especially those whose name start with YY_ or yy_.  They are
   private implementation details that can be changed or removed.  */

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
