/*
  pluribus.y -- Grammar for Pluribus, out of which come una.

  Programming Language for Unum Realization,
  Ingredient Building, and Universe Specification

  Chip Morningstar
  Electric Communities
  21-August-1996

  Copyright 1996 Electric Communities, all rights reserved.

*/

%{

#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "pl.h"

#define YYDEBUG 1

void yyerror(char *s);
%}

%token ABSTRACT ATTRIBUTE BOOLEAN BYTE CASE CHAR CLASS DATA DEFAULT DELIVER
%token DOUBLE ECLASS EINTERFACE ELEVATE ENUM EXPORT EXTENDS FACET FALSEX FILL FINAL
%token FLOAT FUNCTION IMPLEMENTS IMPORT IMPL INGREDIENT INIT INT
%token INTERFACE KIND LONG MAKE MAKES MAP METHOD NEIGHBOR NONE PACKAGE PNULL
%token PRESENCE PRESENCEBEHAVIOR PRIME PRIVATE PROTECTED
%token PUBLIC PUBLISH REMOTE REQUIRE ROLE SEQUENCE SHORT STATE STATIC STRING
%token STRUCT STRUCTURE SWITCH TEMPLATE THROWS TO TRUEX TYPEDEF UNION UNIT UNUM

%token Character CodeBlock HexBlock Initialization Number String Symbol TagType

%nonassoc '?' ':'
%left     Or
%left     And
%left     '|'
%left     '^'
%left     '&'
%nonassoc Neq Eq
%nonassoc '>' '<' Geq Leq
%left     Lsl Lsr Asr
%left     '+' '-'
%left     '*' '/' '%'
%right    '!' '~' UMinus UPlus

%%

pluribusModule:
        ':' EXPORT unitDef
{
    setExport(YC(genericDef,$3));
    YRESULT($3);
}
 |      ':'
{
    YRESULT(NULL);
}
 ;


unitDef:
        UNIT defSymbol '{' unitElems '}'
{
    $$ = YH_BUILD(unitDef)(info(), YC(symbolDef,$2), YC(elemList,$4));
}
 ;

unitUse:
        UNIT scopedRef ';'
{
    $$ = YH_BUILD(unitRef)(YC(symbolRef,$2), NONE);
}
 |      EXPORT UNIT scopedRef ';'
{
    $$ = YH_BUILD(unitRef)(YC(symbolRef,$3), EXPORT);
}
 |      ELEVATE UNIT scopedRef ';'
{
    $$ = YH_BUILD(unitRef)(YC(symbolRef,$3), ELEVATE);
}
 |      UNIT '{' unitElems '}'
{
    $$ = YH_BUILD(unitDef)(genInfo(), defSymbol(gensym()), YC(elemList,$3));
}
 |      EXPORT UNIT '{' unitElems '}'
{
    $$ = YH_BUILD(unitDef)(genInfo(), defSymbol(gensym()), YC(elemList,$4));
    setExport(YC(genericDef,$$));
}
 ;

unitElems:
        unitElemList
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

unitElemList:
        unitElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$1), NULL);
}
 |      unitElemList unitElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$2), YC(elemList,$1));
}
 ;

unitElem:
        definitionStatement
{
    $$ = $1;
}
 |      EXPORT definitionStatement
{
    setExport(YC(genericDef,$2));
    $$ = $2;
}
 |      codeAtt
{
    $$ = $1;
}
 |      importAtt
{
    $$ = $1;
}
 |      packageAtt
{
    $$ = $1;
}
 |      unitUse
{
    $$ = $1;
}
 |      unumImplUse
{
    $$ = $1;
}
 ;

definitionStatement:
        attributeDef
{
    $$ = $1;
}
 |      ingredientImplDef
{
    $$ = $1;
}
 |      kindDef
{
    $$ = $1;
}
 |      presenceImplDef
{
    $$ = $1;
}
 |      presenceStructureDef
{
    $$ = $1;
}
 |      publishDef
{
    $$ = $1;
}
 |      remoteDef
{
    $$ = $1;
}
 |      typeDef
{
    $$ = $1;
}
 |      unitDef
{
    $$ = $1;
}
 |      unumImplDef
{
    $$ = $1;
}
 |      unumStructureDef
{
    $$ = $1;
}
 ;


attributeType:
        booleanType
{
    $$ = $1;
}
 |      charType
{
    $$ = $1;
}
 |         LONG
{
    $$ = YH_BUILD(primType)(LONG);
}
 |      stringType
{
    $$ = $1;
}
 ;

attributeDef:
        ATTRIBUTE defSymbol attributeType ';'
{
    $$ = YH_BUILD(attributeDef)(info(), YC(symbolDef,$2), YC(typeSpec,$3));
}
 |      ATTRIBUTE defSymbol      ';'
{
    $$ = YH_BUILD(attributeDef)(info(), YC(symbolDef,$2), NULL);
}
 ;

assignment:
        scopedRef '=' expr             ';'
{
    $$ = YH_BUILD(attributeRef)(YC(symbolRef,$1), YC(expr,$3));
}
 |      scopedRef                       ';'
{
    $$ = YH_BUILD(attributeRef)(YC(symbolRef,$1), NULL);
}
 |      unitUse
{
    $$ = $1;
}
 ;

requireAtt:
        REQUIRE expr ';'
{
    $$ = YH_BUILD(requireAtt)(YC(expr,$2), NULL);
}
 ;

codeAtt:
        codeModifiers codeType defSymbol codeInherits methodCode
{
    $$ = YH_BUILD(codeDef)(info(), YC(symbolDef,$3), YC(codeModifierList,$1),
               $2, YC(codeInheritList,$4), YC(string,$5));
} ;

codeInherit:
        EXTENDS mangledSymbolList
{
    $$ = YH_BUILD(codeInherit)(EXTENDS, YC(pluribusTypeList,$2));
}
 |      IMPLEMENTS mangledSymbolList
{
    $$ = YH_BUILD(codeInherit)(IMPLEMENTS, YC(pluribusTypeList,$2));
}
 ;

codeInheritList:
        codeInherit
{
    $$ = YH_BUILD(codeInheritList)(YC(codeInherit,$1), NULL);
}
 |      codeInheritList codeInherit
{
    $$ = YH_BUILD(codeInheritList)(YC(codeInherit,$2), YC(codeInheritList,$1));
}
 ;


codeInherits:
        codeInheritList
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

codeModifier:
        ABSTRACT
{
    $$ = YH_BUILD(codeModifier)(ABSTRACT);
}
 |      FINAL
{
    $$ = YH_BUILD(codeModifier)(FINAL);
}
 |      PUBLIC
{
    $$ = YH_BUILD(codeModifier)(PUBLIC);
}
 ;

codeModifierList:
        codeModifier
{
    $$ = YH_BUILD(codeModifierList)(YC(codeModifier,$1), NULL);
}
 |      codeModifierList codeModifier
{
    $$ = YH_BUILD(codeModifierList)(YC(codeModifier,$2),
                    YC(codeModifierList,$1));
}
;

codeModifiers:
       codeModifierList
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

codeType:
        CLASS
{
    $$ = CLASS;
}
 |      INTERFACE
{
    $$ = INTERFACE;
}
 |      ECLASS
{
    $$ = ECLASS;
}
 |      EINTERFACE
{
    $$ = EINTERFACE;
}
 ;

mangledSymbolElem:
        Symbol
{
    $$ = YH_BUILD(pluribusType)
             (YC(symbolRef,YH_BUILD(symbolRef) (YC(symbol,$1), NULL)),
          FALSE);
}
 |      KIND Symbol
{
    $$ = YH_BUILD(pluribusType)
             (YC(symbolRef,YH_BUILD(symbolRef)(YC(symbol,$2), NULL)),
          KIND);
}
 ;

mangledSymbolList:
        mangledSymbolElem
{
    $$ = YH_BUILD(pluribusTypeList)(YC(pluribusType,$1), NULL);
}
 |      mangledSymbolList ',' mangledSymbolElem
{
    $$ = YH_BUILD(pluribusTypeList)(YC(pluribusType,$3),
                    YC(pluribusTypeList,$1));
}
 ;

prototypeDecl:
        Symbol '(' parameterDeclList ')' throwsList ';'
{
    $$ = YH_BUILD(protoDef)(YC(symbol,$1), YC(parameterDeclList,$3),
                            YC(scopedRefList,$5));
}
 |      Symbol '('                   ')' throwsList ';'
{
    $$ = YH_BUILD(protoDef)(YC(symbol,$1), NULL, YC(scopedRefList,$4));
}
 |      INIT '(' parameterDeclList ')' throwsList ';'
{
    $$ = YH_BUILD(protoDef)(initSym, YC(parameterDeclList,$3),
                            YC(scopedRefList,$5));
}
 |      INIT '('                   ')' throwsList ';'
{
    $$ = YH_BUILD(protoDef)(initSym, NULL, YC(scopedRefList,$5));
}
 ;

parameterDeclList:
        parameterDecl
{
    $$ = YH_BUILD(parameterDeclList)(YC(parameterDecl,$1), NULL);
}
 |      parameterDeclList ',' parameterDecl
{
    $$ = YH_BUILD(parameterDeclList)(YC(parameterDecl,$3),
                                     YC(parameterDeclList,$1));
}
 ;

arrayMarker:
      '[' ']'
;

arrayMarkers:
        arrayMarker
{
    $$ = 1;
}
 |      arrayMarkers arrayMarker
{
    $$ = $1 + 1;
}
 ;

parameterDecl:
        type arrayMarkers Symbol
{
    $$ = YH_BUILD(parameterDecl)(YC(typeSpec,$1), YC(symbol,$3), $2);
}
 |      type Symbol arrayMarkers
{
    $$ = YH_BUILD(parameterDecl)(YC(typeSpec,$1), YC(symbol,$2), $2);
}
 |      type Symbol
{
    $$ = YH_BUILD(parameterDecl)(YC(typeSpec,$1), YC(symbol,$2), 0);
}
 |      type
{
    $$ = YH_BUILD(parameterDecl)(YC(typeSpec,$1), NULL, 0);
}
 ;


kindDef:
        KIND defSymbol '{' kindElems '}'
{
    $$ = YH_BUILD(kindDef)(info(), YC(symbolDef,$2), YC(elemList,$4));
}
 ;

commaNameList:
        Symbol
{
    $$ = YH_BUILD(symbolList)(YC(symbol,$1), NULL);
}
 |      commaNameList ',' Symbol
{
    $$ = YH_BUILD(symbolList)(YC(symbol,$3), YC(symbolList,$1));
}
 ;

commaScopedRefList:
        scopedRef
{
    $$ = YH_BUILD(scopedRefList)(YC(scopedRef,$1), NULL);
}
 |      commaScopedRefList ',' scopedRef
{
    $$ = YH_BUILD(scopedRefList)(YC(scopedRef,$3), YC(scopedRefList,$1));
}
 ;

kindUse:
        KIND scopedRef ';'
{
    $$ = YH_BUILD(kindRef)(YC(symbolRef,$2));
}
 |      KIND '{' kindElems '}'
{
    YT(symbol) *anon = gensym();
    YT(elem) *elem1 = YC(elem,YBUILD(kindDef)(
        genInfo(), defSymbol(anon), YC(elemList,$3)));
    YT(elem) *elem2 = YC(elem,YBUILD(kindRef)(refSymbol(anon)));
    YT(elemList) *list = YBUILD(elemList)(elem1, NULL);
                  list = YBUILD(elemList)(elem2, list);
    $$ = YH_BUILD(nestedElem)(list);
}
 ;

kindUseNoSemi:
        KIND scopedRef
{
    $$ = YH_BUILD(kindRef)(YC(symbolRef,$2));
}
 |      KIND '{' kindElems '}'
{
    YT(symbol) *anon = gensym();
    YT(elem) *elem1 = YC(elem,YBUILD(kindDef)(
        genInfo(), defSymbol(anon), YC(elemList,$3)));
    YT(elem) *elem2 = YC(elem,YBUILD(kindRef)(refSymbol(anon)));
    YT(elemList) *list = YBUILD(elemList)(elem1, NULL);
                  list = YBUILD(elemList)(elem2, list);
    $$ = YH_BUILD(nestedElem)(list);
}
 ;

kindElems:
        kindElemList
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

kindElemList:
        kindElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$1), NULL);
}
 |      kindElemList kindElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$2), YC(elemList,$1));
}
 ;

kindElem:
        prototypeDecl
{
    $$ = $1;
}
 |      requireAtt
{
    $$ = $1;
}
 |      kindUse
{
    $$ = $1;
}
 |      implementsAtt
{
    $$ = $1;
}
 ;


ingredientImplDef:
        INGREDIENT IMPL defSymbol '{' ingredientImplElems '}'
{
    $$ = YH_BUILD(ingredientImplDef)(info(), YC(symbolDef,$3),
                                     YC(elemList,$5));
}
 ;

ingredientImplUse:
        IMPL scopedRef ';'
{
    $$ = YH_BUILD(ingredientImplRef)(YC(symbolRef,$2));
}
 |      IMPL '{' ingredientImplElems '}'
{
    YT(symbol) *anon = gensym();
    YT(elem) *elem1 = YC(elem,YBUILD(ingredientImplDef)(
        genInfo(), defSymbol(anon), YC(elemList,$3)));
    YT(elem) *elem2 = YC(elem,YBUILD(ingredientImplRef)(refSymbol(anon)));
    YT(elemList) *list = YBUILD(elemList)(elem1, NULL);
                  list = YBUILD(elemList)(elem2, list);
    $$ = YH_BUILD(nestedElem)(list);
}
 ;

ingredientImplElems:
        ingredientImplElemList
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

ingredientImplElemList:
        ingredientImplElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$1), NULL);
}
 |      ingredientImplElemList ingredientImplElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$2), YC(elemList,$1));
}
 ;

ingredientImplElem:
        assignment
{
    $$ = $1;
}
 |      kindUse
{
    $$ = $1;
}
 |      neighborAtt
{
    $$ = $1;
}
 |      stateBundleAtt
{
    $$ = $1;
}
 |      variableDecl
{
    $$ = $1;
}
 |      functionAtt
{
    $$ = $1;
}
 |      methodAtt
{
    $$ = $1;
}
 |      initBlockAtt
{
    $$ = $1;
}
 |      dataAtt
{
    $$ = $1;
}
 |      importAtt
{
    $$ = $1;
}
 |      implementsAtt
{
    $$ = $1;
}
 ;

neighborAtt:
        NEIGHBOR INGREDIENT Symbol kindUse
{
    $$ = YH_BUILD(neighborAtt)(YC(symbol,$3), FALSE, FALSE, YC(genericRef,$4));
}
 |      NEIGHBOR PRESENCE Symbol plurality kindUse
{
    $$ = YH_BUILD(neighborAtt)(YC(symbol,$3), $4, TRUE, YC(genericRef,$5));
}
 ;

stateBundleAtt:
        STATE scopedRef Symbol valueOrNot ';'
{
    $$ = YH_BUILD(stateBundleDef)(YC(scopedRef,$2), YC(symbol,$3),
                  YC(string,$4));
}
 ;

valueOrNot:
        '=' { ExpectInitialization = TRUE; } Initialization
{
    $$ = $3;
}
 |
{
    $$ = 0;
}
 ;

variableDecl:
        modifiers type Symbol arrayMarkers valueOrNot ';'
{
    $$ = YH_BUILD(variableDecl)
            (YC(typeSpec,$2), $1, YC(symbol,$3), $4, YC(string,$5));
}
 |      modifiers type arrayMarkers Symbol valueOrNot ';'
{
    $$ = YH_BUILD(variableDecl)
             (YC(typeSpec,$2), $1, YC(symbol,$4), $3, YC(string,$5));
}
 |      modifiers type Symbol valueOrNot ';'
{
    $$ = YH_BUILD(variableDecl)
             (YC(typeSpec,$2), $1, YC(symbol,$3), 0, YC(string,$4));
}
 |      type Symbol arrayMarkers valueOrNot ';'
{
    $$ = YH_BUILD(variableDecl)
            (YC(typeSpec,$1), 0, YC(symbol,$2), $3, YC(string,$4));
}
 |      type arrayMarkers Symbol valueOrNot ';'
{
    $$ = YH_BUILD(variableDecl)
             (YC(typeSpec,$1), 0, YC(symbol,$3), $2, YC(string,$4));
}
 |      type Symbol valueOrNot ';'
{
    $$ = YH_BUILD(variableDecl)
             (YC(typeSpec,$1), 0, YC(symbol,$2), 0, YC(string,$3));
}
 ;

throwsList:
        THROWS commaScopedRefList
{
    $$ = $2;
}
 |
{
    $$ = 0;
}
 ;


functionAtt:
        FUNCTION modifiersOrNot type Symbol '(' parameterDeclList ')' throwsList
        methodCode
{
    $$ = YH_BUILD(functionAtt)($2, YC(typeSpec,$3), YC(symbol,$4),
                   YC(parameterDeclList,$6), YC(scopedRefList,$8),
                   YC(string,$9));
}
 |      FUNCTION modifiersOrNot type Symbol '('       ')' throwsList methodCode
{
    $$ = YH_BUILD(functionAtt)($2, YC(typeSpec,$3), YC(symbol,$4),
                   NULL, YC(scopedRefList,$7), YC(string,$8));
}

modifiersOrNot:
        modifierList
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

modifiers:
        modifierList
{
    $$ = $1;
}
 ;

modifierList:
        modifier
{
    $$ = $1;
}
 |      modifierList modifier
{
    $$ = $1 | $2;
}
 ;

modifier:
        PUBLIC
{
    $$ = MOD_PUBLIC;
}
 |      PRIVATE
{
    $$ = MOD_PRIVATE;
}
 |      PROTECTED
{
    $$ = MOD_PROTECTED;
}
 |      STATIC
{
    $$ = MOD_STATIC;
}
 |      FINAL
{
    $$ = MOD_FINAL;
}
 ;

initBlockAtt:
        PRIME INIT '(' parameterDeclList ')' throwsList methodCode
{
    $$ = YH_BUILD(initBlockAtt(PRIME, YC(parameterDeclList,$4),
                   YC(scopedRefList,$6), YC(string,$7)));
}
 |      PRIME INIT '('                   ')' throwsList methodCode
{
    $$ = YH_BUILD(initBlockAtt(PRIME, NULL, YC(scopedRefList,$5),
                               YC(string,$6)));
}
 |      FACET INIT '(' parameterDeclList ')' throwsList methodCode
{
    $$ = YH_BUILD(initBlockAtt(FACET, YC(parameterDeclList,$4),
                   YC(scopedRefList,$6), YC(string,$7)));
}
 |      FACET INIT '('                   ')' throwsList methodCode
{
    $$ = YH_BUILD(initBlockAtt(FACET, NULL, YC(scopedRefList,$5),
                               YC(string,$6)));
}
 |            INIT '(' parameterDeclList ')' throwsList methodCode
{
    $$ = YH_BUILD(initBlockAtt(INIT, YC(parameterDeclList,$3),
                               YC(scopedRefList,$5), YC(string,$6)));
}
 |            INIT '('                   ')' throwsList methodCode
{
    $$ = YH_BUILD(initBlockAtt(INIT, NULL, YC(scopedRefList,$4),
                               YC(string,$5)));
}
 |            INIT                           throwsList methodCode
{
    $$ = YH_BUILD(initBlockAtt(INIT, NULL, YC(scopedRefList,$2),
                               YC(string,$3)));
}
 ;

methodAtt:
        METHOD Symbol '(' parameterDeclList ')' throwsList methodCode
{
    $$ = YH_BUILD(emethodAtt)(YC(symbol,$2), YC(parameterDeclList,$4),
                  YC(scopedRefList,$6), YC(string,$7));
}
 |      METHOD Symbol '('                   ')' throwsList methodCode
{
    $$ = YH_BUILD(emethodAtt)(YC(symbol,$2), NULL, YC(scopedRefList,$5),
                  YC(string,$6));
}
 ;

methodCode:
        '{' { ExpectCodeBlock = TRUE; } CodeBlock
{
    $$ = $3;
}
 ;

dataAtt:
        DATA '{' { ExpectHexBlock = TRUE; } HexBlock
{
    $$ = YH_BUILD(dataAtt)(YC(string,$4));
}
 |      DATA String ';'
{
    $$ = YH_BUILD(dataAtt)(YC(string,$2));
}
 ;

packageAtt:
        PACKAGE scopedRef ';'
{
    $$ = YH_BUILD(packageAtt)(YC(symbolRef,$2));
}
 ;

importAtt:
        IMPORT scopedRef ';'
{
    $$ = YH_BUILD(importAtt)(YC(symbolRef,$2), FALSE);
}
 |      IMPORT scopedRef '.' '*' ';'
{
    $$ = YH_BUILD(importAtt)(YC(symbolRef,$2), TRUE);
}
 ;

implementsAtt:
        IMPLEMENTS scopedRef ';'
{
    $$ = YH_BUILD(implementsAtt)(YC(symbolRef,$2));
}
 ;

presenceStructureDef:
        PRESENCE STRUCTURE defSymbol '{' presenceStructureElems '}'
{
    $$ = YH_BUILD(presenceStructureDef)(info(), YC(symbolDef,$3),
                                        YC(elemList,$5));
}
 ;

presenceStructureUse:
        STRUCTURE scopedRef ';'
{
    $$ = YH_BUILD(presenceStructureRef)(YC(symbolRef,$2));
}
 |      STRUCTURE '{' presenceStructureElems '}'
{
    YT(symbol) *anon = gensym();
    YT(elem) *elem1 = YC(elem,YBUILD(presenceStructureDef)(
        genInfo(), defSymbol(anon), YC(elemList,$3)));
    YT(elem) *elem2 = YC(elem,YBUILD(presenceStructureRef)(refSymbol(anon)));
    YT(elemList) *list = YBUILD(elemList)(elem1, NULL);
                  list = YBUILD(elemList)(elem2, list);
    $$ = YH_BUILD(nestedElem)(list);
}
 ;

presenceStructureElems:
        presenceStructureElemList
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

presenceStructureElemList:
        presenceStructureElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$1), NULL);
}
 |      presenceStructureElemList presenceStructureElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$2), YC(elemList,$1));
}
 ;

presenceStructureElem:
        assignment
{
    $$ = $1;
}
 |      requireAtt
{
    $$ = $1;
}
 |      kindUse
{
    $$ = $1;
}
 |      ingredientAtt
{
    $$ = $1;
}
 ;

ingredientAtt:
        INGREDIENT Symbol '{' ingredientAttElems '}'
{
    $$ = YH_BUILD(ingredientAtt)(YC(symbol,$2), YC(elemList,$4));
}
 ;

ingredientAttElems:
        ingredientAttElemList
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

ingredientAttElemList:
        ingredientAttElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$1), NULL);
}
 |      ingredientAttElemList ingredientAttElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$2), YC(elemList,$1));
}
 ;

ingredientAttElem:
        kindUse
{
    $$ = $1;
}
 |      deliverAtt
{
    $$ = $1;
}
 ;

deliverSym:
       Symbol
{
    $$ = $1;
}
 |     INIT
{
    $$ = (long)initSym; /* KSSHack */
}

deliverAtt:
        DELIVER scope deliverSym TO deliverSym        ';'
{
    $$ = YH_BUILD(deliverAtt)($2, YC(symbol,$3), YC(symbol,$5), NULL);
}
 |      DELIVER scope deliverSym                  ';'
{
    $$ = YH_BUILD(deliverAtt)($2, YC(symbol,$3), NULL, NULL);
}
 ;

scope:
        PRESENCE
{
    $$ = PRESENCE;
}
 |      UNUM
{
    $$ = UNUM;
}
 ;


presenceImplDef:
        PRESENCE IMPL defSymbol '{' presenceImplElems '}'
{
    $$ = YH_BUILD(presenceImplDef)(info(), YC(symbolDef,$3), YC(elemList,$5));
}
 ;

presenceImplUse:
        IMPL scopedRef ';'
{
    $$ = YH_BUILD(presenceImplRef)(YC(symbolRef,$2));
}
 |      IMPL '{' presenceImplElems '}'
{
    YT(symbol) *anon = gensym();
    YT(elem) *elem1 = YC(elem,YBUILD(presenceImplDef)(
        genInfo(), defSymbol(anon), YC(elemList,$3)));
    YT(elem) *elem2 = YC(elem,YBUILD(presenceImplRef)(refSymbol(anon)));
    YT(elemList) *list = YBUILD(elemList)(elem1, NULL);
                  list = YBUILD(elemList)(elem2, list);
    $$ = YH_BUILD(nestedElem)(list);
}
 ;

presenceImplElems:
        presenceImplElemList
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

presenceImplElemList:
        presenceImplElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$1), NULL);
}
 |      presenceImplElemList presenceImplElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$2), YC(elemList,$1));
}
 ;

presenceImplElem:
        assignment
{
    $$ = $1;
}
 |      presenceBehavior
{
    $$ = $1;
}
 |      initBlockAtt
{
    $$ = $1;
}
 |      presenceStructureUse
{
    $$ = $1;
}
 |      templateAtt
{
    $$ = $1;
}
 |      makeAtt
{
    $$ = $1;
}
 |      implementsAtt
{
    $$ = $1;
}
 ;

presenceBehavior:
        PRESENCEBEHAVIOR commaNameList ';'
{
    $$ = YH_BUILD(presenceBehavior)(YC(symbolList,$2));
}
 ;

templateAtt:
        INGREDIENT commaNameList templateDef
{
    $$ = YH_BUILD(templateAtt)(YC(symbolList,$2), YC(templateDef,$3));
}
 ;

/*KSSHack
makeAtt:
        MAKE Symbol ';'
{
    $$ = YH_BUILD(makeAtt)(YC(symbol,$2), NULL);
}
 |      MAKE Symbol exprList ';'
{
    $$ = YH_BUILD(makeAtt)(YC(symbol,$2), YC(exprList,$3));
}
 ;
KSSHack*/

makeAtt:
        MAKE Symbol ';'
{
    $$ = YH_BUILD(makeAtt)(YC(symbol,$2), NULL);
}
 |      MAKE Symbol commaNameList ';'
{
    $$ = YH_BUILD(makeAtt)(YC(symbol,$2), YC(symbolList,$3));
}
 ;

templateDef:
        TEMPLATE '{' templateElems '}'
{
    $$ = YH_BUILD(templateDef)(info(), YC(elemList,$3));
}
 ;

templateElems:
        templateElemList
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

templateElemList:
        templateElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$1), NULL);
}
 |      templateElemList templateElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$2), YC(elemList,$1));
}
 ;

templateElem:
        ingredientImplUse
{
    $$ = $1;
}
 |      mapAtt
{
    $$ = $1;
}
 ;

mapScope:
        NEIGHBOR
{
    $$ = NEIGHBOR;
}
 |      STATE
{
    $$ = STATE;
}
 ;

mapAtt:
        MAP mapScope Symbol TO PNULL ';'
{
    $$ = YH_BUILD(mapAtt)($2, YC(symbol,$3), NULL);
}
|
        MAP mapScope Symbol TO Symbol ';'
{
    $$ = YH_BUILD(mapAtt)($2, YC(symbol,$3), YC(symbol,$5));
}
 |      MAP mapScope Symbol           ';'
{
    $$ = YH_BUILD(mapAtt)($2, NULL, YC(symbol,$3));
}
 ;

exprList:
        expr
{
    $$ = YH_BUILD(exprList)(YC(expr,$1), NULL);
}
 |      exprList ',' expr
{
    $$ = YH_BUILD(exprList)(YC(expr,$3), YC(exprList,$1));
}
 ;


unumStructureDef:
        UNUM STRUCTURE defSymbol '{' unumStructureElems '}'
{
    $$ = YH_BUILD(unumStructureDef)(info(), YC(symbolDef,$3), YC(elemList,$5));
}
 ;

unumStructureUse:
        STRUCTURE scopedRef ';'
{
    $$ = YH_BUILD(unumStructureRef)(YC(symbolRef,$2));
}
 |      STRUCTURE '{' unumStructureElems '}'
{
    YT(symbol) *anon = gensym();
    YT(elem) *elem1 = YC(elem,YBUILD(unumStructureDef)(
        genInfo(), defSymbol(anon), YC(elemList,$3)));
    YT(elem) *elem2 = YC(elem,YBUILD(unumStructureRef)(refSymbol(anon)));
    YT(elemList) *list = YBUILD(elemList)(elem1, NULL);
                  list = YBUILD(elemList)(elem2, list);
    $$ = YH_BUILD(nestedElem)(list);
}
 ;

unumStructureElems:
        unumStructureElemList
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

unumStructureElemList:
        unumStructureElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$1), NULL);
}
 |      unumStructureElemList unumStructureElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$2), YC(elemList,$1));
}
 ;

unumStructureElem:
        assignment
{
    $$ = $1;
}
 |      requireAtt
{
    $$ = $1;
}
 |      kindUse
{
    $$ = $1;
}
 |      primeAtt
{
    $$ = $1;
}
 |      presenceAtt
{
    $$ = $1;
}
 ;

primeAtt:
        PRIME Symbol ';'
{
    $$ = YH_BUILD(primeAtt)(YC(symbol,$2));
}
 ;

presenceAtt:
        PRESENCE Symbol kindUseNoSemi makes ';'
{
    $$ = YH_BUILD(presenceAtt)(YC(symbol,$2), YC(symbol,$4), NULL,
                               YC(kindRef,$3), FALSE);
}
 |      PRESENCE Symbol kindUseNoSemi MAKES '{' presenceConds '}'
{
    $$ = YH_BUILD(presenceAtt)(YC(symbol,$2), NULL, YC(presenceCondList,$6),
                               YC(kindRef,$3), FALSE);
}
 ;

presenceConds:
        presenceCondList
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

presenceCondList:
        presenceCond
{
    $$ = YH_BUILD(presenceCondList)(YC(presenceCond,$1), NULL);
}
 |      presenceCondList presenceCond
{
    $$ = YH_BUILD(presenceCondList)(YC(presenceCond,$2),
                    YC(presenceCondList,$1));
}
 ;

presenceCond:
        expr ':' Symbol ';'
{
    $$ = YH_BUILD(presenceCond)(YC(expr,$1), YC(symbol,$3));
}
 ;

makes:
        MAKES Symbol
{
    $$ = $2;
}
 |      MAKES NONE
{
    $$ = 0;
}
 |
{
    $$ = 0;
}
 ;

plurality:
        arrayMarker
{
    $$ = TRUE;
}
 |
{
    $$ = FALSE;
}
 ;


unumImplDef:
        UNUM IMPL defSymbol '{' unumImplElems '}'
{
    $$ = YH_BUILD(unumImplDef)(info(), YC(symbolDef,$3), YC(elemList,$5));
}
 ;

unumImplUse:
        IMPL scopedRef ';'
{
    $$ = YH_BUILD(unumImplRef)(YC(symbolRef,$2));
}
 |      IMPL '{' unumImplElems '}'
{
    YT(symbol) *anon = gensym();
    YT(elem) *elem1 = YC(elem,YBUILD(unumImplDef)(
        genInfo(), defSymbol(anon), YC(elemList,$3)));
    YT(elem) *elem2 = YC(elem,YBUILD(unumImplRef)(refSymbol(anon)));
    YT(elemList) *list = YBUILD(elemList)(elem1, NULL);
                  list = YBUILD(elemList)(elem2, list);
    $$ = YH_BUILD(nestedElem)(list);
}
 ;

unumImplElems:
        unumImplElemList
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

unumImplElemList:
        unumImplElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$1), NULL);
}
 |      unumImplElemList unumImplElem
{
    $$ = YH_BUILD(elemList)(YC(elem,$2), YC(elemList,$1));
}
 ;

unumImplElem:
        assignment
{
    $$ = $1;
}
 |      unumStructureUse
{
    $$ = $1;
}
 |      presenceImplAtt
{
    $$ = $1;
}
 ;

presenceImplAtt:
        PRESENCE commaNameList presenceImplUse
{
    $$ = YH_BUILD(presenceImplAtt)(YC(symbolList,$2), YC(presenceImplRef,$3));
}
 ;

expr:
        exprA
{
    $$ = $1;
}
 |      expr '?' expr ':' exprA
{
    $$ = YH_BUILD(condop)(YC(expr,$1), YC(expr,$3), YC(expr,$5));
}
 ;

exprA:
        expr9
{
    $$ = $1;
}
 |      exprA Or expr9
{
    $$ = YH_BUILD(binop)(YC(expr,$1), Or, YC(expr,$3));
}
 ;

expr9:
        expr8
{
    $$ = $1;
}
 |      expr9 And expr8
{
    $$ = YH_BUILD(binop)(YC(expr,$1), And, YC(expr,$3));
}
 ;

expr8:
        expr7
{
    $$ = $1;
}
 |      expr8 '|' expr7
{
    $$ = YH_BUILD(binop)(YC(expr,$1), '|', YC(expr,$3));
}
 ;

expr7:
        expr6
{
    $$ = $1;
}
 |      expr7 '^' expr6
{
    $$ = YH_BUILD(binop)(YC(expr,$1), '^', YC(expr,$3));
}
 ;

expr6:
        expr5
{
    $$ = $1;
}
 |      expr6 '&' expr5
{
    $$ = YH_BUILD(binop)(YC(expr,$1), '&', YC(expr,$3));
}
 ;

expr5:
        expr4
{
    $$ = $1;
}
 |      expr5 Eq expr4
{
    $$ = YH_BUILD(binop)(YC(expr,$1), Eq, YC(expr,$3));
}
 |      expr5 Neq expr4
{
    $$ = YH_BUILD(binop)(YC(expr,$1), Neq, YC(expr,$3));
}
 ;

expr4:
        expr3
{
    $$ = $1;
}
 |      expr4 '<' expr3
{
    $$ = YH_BUILD(binop)(YC(expr,$1), '<', YC(expr,$3));
}
 |      expr4 Leq expr3
{
    $$ = YH_BUILD(binop)(YC(expr,$1), Leq, YC(expr,$3));
}
 |      expr4 '>' expr3
{
    $$ = YH_BUILD(binop)(YC(expr,$1), '>', YC(expr,$3));
}
 |      expr4 Geq expr3
{
    $$ = YH_BUILD(binop)(YC(expr,$1), Geq, YC(expr,$3));
}
 ;

expr3:
        expr2
{
    $$ = $1;
}
 |      expr3 Lsl expr2
{
    $$ = YH_BUILD(binop)(YC(expr,$1), Lsl, YC(expr,$3));
}
 |      expr3 Lsr expr2
{
    $$ = YH_BUILD(binop)(YC(expr,$1), Lsr , YC(expr,$3));
}
 |      expr3 Asr expr2
{
    $$ = YH_BUILD(binop)(YC(expr,$1), Asr , YC(expr,$3));
}
 ;

expr2:
        expr1
{
    $$ = $1;
}
 |      expr2 '+' expr1
{
    $$ = YH_BUILD(binop)(YC(expr,$1), '+', YC(expr,$3));
}
 |      expr2 '-' expr1
{
    $$ = YH_BUILD(binop)(YC(expr,$1), '-', YC(expr,$3));
}
 ;

expr1:
        term
{
    $$ = $1;
}
 |      expr1 '*' term
{
    $$ = YH_BUILD(binop)(YC(expr,$1), '*', YC(expr,$3));
}
 |      expr1 '/' term
{
    $$ = YH_BUILD(binop)(YC(expr,$1), '/', YC(expr,$3));
}
 |      expr1 '%' term
{
    $$ = YH_BUILD(binop)(YC(expr,$1), '%', YC(expr,$3));
}
 ;

term:
        prim
{
    $$ = $1;
}
 |      '+' term %prec UPlus
{
    $$ = YH_BUILD(unop)('+', YC(expr,$2));
}
 |      '-' term %prec UMinus
{
    $$ = YH_BUILD(unop)('-', YC(expr,$2));
}
 |      '!' term
{
    $$ = YH_BUILD(unop)('!', YC(expr,$2));
}
 |      '~' term
{
    $$ = YH_BUILD(unop)('~', YC(expr,$2));
}
 ;

prim:
        Number
{
    $$ = YH_BUILD(numLit)($1);
}
 |      Character
{
    $$ = YH_BUILD(charLit)($1);
}
 |      TagType
{
    $$ = YH_BUILD(tagLit)($1);
}
 |      String
{
    $$ = YH_BUILD(stringLit)(YC(string,$1));
}
 |      '(' expr ')'
{
    $$ = $2;
}
 |      scopedRef
{
    $$ = YH_BUILD(refTerm)(YC(symbolRef,$1));
}
 |      FALSEX
{
    $$ = YH_BUILD(boolLit)(FALSE);
}
 |      TRUEX
{
    $$ = YH_BUILD(boolLit)(TRUE);
}
 ;

type:
        typeSpec
{
    $$ = $1;
}
 ;

remoteDef:
        REMOTE String Symbol ';'
{
    $$ = YH_BUILD(remoteDef)(info(), YC(string,$2), YC(symbol,$3));
}
 ;

publishDef:
        PUBLISH Symbol ';'
{
    $$ = YH_BUILD(publishDef)(info(), YC(symbol,$2));
}
 ;

defSymbol:
        directRef /* But all params must be symbols, not exprs */
{
    $$ = $1;
}
 ;

directRef:
        Symbol
{
    $$ = YH_BUILD(symbolRef)(YC(symbol,$1), NULL);
}
 |      Symbol '(' ')'
{
    $$ = YH_BUILD(symbolRef)(YC(symbol,$1), YC(exprList,NULL));
}
 |      Symbol '(' exprList ')'
{
    $$ = YH_BUILD(symbolRef)(YC(symbol,$1), YC(exprList,$3));
}
 ;

scopedRef:
        directRef
{
    $$ = $1;
}
 |      scopedRef '.' directRef
{
    if (YTAG_OF(YC(symbolRef,$1)) == YTAG(symbolRef))
    $$ = YH_BUILD(scopedRef)(YC(scopedRef,
                    YH_BUILD(scopedRef)(NULL,
                            YC(symbolRef,$1))),
                 YC(symbolRef,$3));
    else
    $$ = YH_BUILD(scopedRef)(YC(scopedRef,$1),YC(symbolRef,$3));
}
 |      outerScope '.' directRef
{
    $$ = YH_BUILD(outerRef)($1, YC(symbolRef,$3));
}
 ;

outerScope:
        '^'
{
    $$ = 1;
}
 |      outerScope '^'
{
    $$ = $1 + 1;
}
 ;


/* The following syntax is lifted directly from the CORBA IDL specification */

typeDef:
        TYPEDEF typeDeclarator ';'
{
    $$ = YH_BUILD(typeDef)(info(), YC(typeDeclarator,$2));
}
 |      structType
{
    $$ = $1;
}
 |      unionType
{
    $$ = $1;
}
 |      enumType
{
    $$ = $1;
}
 ;

typeDeclarator:
        typeSpec declarators
{
    $$ = YH_BUILD(typeDeclarator)(YC(typeSpec,$1), YC(declaratorList,$2));
}
 ;

typeSpec:
        simpleTypeSpec
{
    $$ = $1;
}
 |      constrTypeSpec
{
    $$ = $1;
}
 ;

simpleTypeSpec:
        baseTypeSpec
{
    $$ = $1;
}
 |      templateTypeSpec
{
    $$ = $1;
}
 |      scopedRef
{
    $$ = $1;
}
 |      KIND scopedRef
{
    $$ = YH_BUILD(pluribusType)(YC(symbolRef,$2), KIND);
}
 ;

baseTypeSpec:
        floatingPtType
{
    $$ = $1;
}
 |      integralType
{
    $$ = $1;
}
 |      charType
{
    $$ = $1;
}
 |      booleanType
{
    $$ = $1;
}
 ;

templateTypeSpec:
        sequenceType
{
    $$ = $1;
}
 |      stringType
{
    $$ = $1;
}
 ;

constrTypeSpec:
        structType
{
    $$ = $1;
}
 |      unionType
{
    $$ = $1;
}
 |      enumType
{
    $$ = $1;
}
 ;

declarators:
        declarator
{
    $$ = YH_BUILD(declaratorList)(YC(declarator,$1), NULL);
}
 |      declarators ',' declarator
{
    $$ = YH_BUILD(declaratorList)(YC(declarator,$3), YC(declaratorList,$3));
}
 ;

declarator:
        simpleDeclarator
{
    $$ = $1;
}
 |      complexDeclarator
{
    $$ = $1;
}
 ;

simpleDeclarator:
        Symbol
{
    $$ = YH_BUILD(simpleDeclarator)(YC(symbol,$1));
}
 ;

complexDeclarator:
        arrayDeclarator
{
    $$ = $1;
}
 ;

floatingPtType:
        FLOAT
{
    $$ = YH_BUILD(primType)(FLOAT);
}
 |      DOUBLE
{
    $$ = YH_BUILD(primType)(DOUBLE);
}
 ;

integralType:
        BYTE
{
    $$ = YH_BUILD(primType)(BYTE);
}
 |
        SHORT
{
    $$ = YH_BUILD(primType)(SHORT);
}
 |         INT
{
    $$ = YH_BUILD(primType)(INT);
}
 |         LONG
{
    $$ = YH_BUILD(primType)(LONG);
}
 ;

charType:
        CHAR
{
    $$ = YH_BUILD(primType)(CHAR);
}
 ;

booleanType:
        BOOLEAN
{
    $$ = YH_BUILD(primType)(BOOLEAN);
}
 ;

structType:
        STRUCT Symbol '{' memberList '}'
{
    $$ = YH_BUILD(structTypeDecl)(info(), YC(symbol,$2),
                                  YC(memberDeclList,$4));
}
 ;

memberList:
        member
{
    $$ = YH_BUILD(memberDeclList)(YC(memberDecl,$1), NULL);
}
 |      memberList member
{
    $$ = YH_BUILD(memberDeclList)(YC(memberDecl,$2), YC(memberDeclList,$1));
}
 ;

member:
        typeSpec declarators ';'
{
    $$ = YH_BUILD(memberDecl)(YC(typeSpec,$1), YC(declaratorList,$2));
}
 ;

unionType:
        UNION Symbol SWITCH '(' switchTypeSpec ')' '{' switchBody '}'
{
    $$ = YH_BUILD(unionTypeDecl)(info(), YC(symbol,$2), YC(typeSpec,$5),
                                 YC(switchCaseDeclList,$8));
}
 ;

switchTypeSpec:
        integralType
{
    $$ = $1;
}
 |      charType
{
    $$ = $1;
}
 |      booleanType
{
    $$ = $1;
}
 |      enumType
{
    $$ = $1;
}
 |      Symbol
{
    $$ = YH_BUILD(simpleDeclarator)(YC(symbol,$1));
}
 ;

switchBody:
        case
{
    $$ = YH_BUILD(switchCaseDeclList)(YC(switchCaseDecl,$1), NULL);
}
 |      switchBody case
{
    $$ = YH_BUILD(switchCaseDeclList)(YC(switchCaseDecl,$2),
                                      YC(switchCaseDeclList,$1));
}
 ;

case:
        caseLabels elementSpec ';'
{
    $$ = YH_BUILD(switchCaseDecl)(YC(caseLabelDeclList,$1),
                                  YC(elementSpec,$2));
}
 ;

caseLabels:
        caseLabel
{
    $$ = YH_BUILD(caseLabelDeclList)(YC(caseLabelDecl,$1), NULL);
}
 |      caseLabels caseLabel
{
    $$ = YH_BUILD(caseLabelDeclList)(YC(caseLabelDecl,$2),
                                     YC(caseLabelDeclList,$2));
}
 ;

caseLabel:
        CASE expr ':'
{
    $$ = YH_BUILD(caseLabelDecl)(YC(expr,$2));
}
 |      DEFAULT ':'
{
    $$ = YH_BUILD(caseLabelDecl)(NULL);
}
 ;

elementSpec:
        typeSpec declarator
{
    $$ = YH_BUILD(elementSpec)(YC(typeSpec,$1), YC(declarator,$2));
}
 ;

enumType:
        ENUM Symbol '{' commaNameList '}'
{
    $$ = YH_BUILD(enumTypeDecl)(info(), YC(symbol,$2), YC(symbolList,$4));
}
 ;

sequenceType:
        SEQUENCE '<' simpleTypeSpec ',' Number '>'
{
    $$ = YH_BUILD(sequenceTypeDecl)(YC(typeSpec,$3), $5);
}
 |      SEQUENCE '<' simpleTypeSpec            '>'
{
    $$ = YH_BUILD(sequenceTypeDecl)(YC(typeSpec,$3), -1);
}
 ;

stringType:
        STRING '<' Number '>'
{
    $$ = YH_BUILD(stringType)($3);
}
 |      STRING
{
    $$ = YH_BUILD(stringType)(-1);
}
 ;

arrayDeclarator:
        Symbol fixedArraySizes
{
    $$ = YH_BUILD(arrayDeclarator)(YC(symbol,$1), YC(arraySizeList,$2));
}
 ;

fixedArraySizes:
        fixedArraySize
{
    $$ = YH_BUILD(arraySizeList)(YC(arraySize,$1), NULL);
}
 |      fixedArraySizes fixedArraySize
{
    $$ = YH_BUILD(arraySizeList)(YC(arraySize,$2), YC(arraySizeList,$1));
}
 ;

fixedArraySize:
        '[' Number ']'
{
    $$ = YH_BUILD(arraySize)($2);
}
 ;


%%

  void
yyerror(char *s)
{
    yh_error("%s", s);
    YRESULT(NULL);
}
