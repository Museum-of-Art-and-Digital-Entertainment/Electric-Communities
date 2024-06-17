/*
  egrammar.y -- Yacc grammar and parser for E

  Chip Morningstar
  Electric Communities
  9-January-1998

  Copyright 1998 Electric Communities, all rights reserved worldwide.

*/

%{

#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "jfe.h"

int yylex(void);
void yyerror(char *s);

void pushDistContext(void);
void popDistContext(void);

static YT(distContext) *CurrentDistContext = NULL;

#define YYDEBUG 1

/* Syntactic sugar for capturing token whitespace */
#define WS(arg) YC(string,arg)

%}

/* Keywords */
%token ABSTRACT BOOLEAN BREAK BYTE CASE CATCH CHAR CLASS CONTINUE DEFAULT DO
%token DOUBLE ECATCH ECLASS EDEBUG EFALSE EIF EINTERFACE EKEEP ELSE EMETHOD
%token ENULL EORIF EORWHEN ETHROW ETRY ETRUE EWHEN EWHENEVER EXTENDS FINAL
%token FINALLY FLOAT FOR IF IMPLEMENTS IMPORT INSTANCEOF INT INTERFACE JFALSE
%token JNULL JTRUE LOCAL LONG NATIVE NEW PACKAGE PRIVATE PROTECTED PUBLIC
%token RETURN SHORT STATIC SUPER SWITCH SYNCHRONIZED THIS THROW THROWS
%token TRANSIENT TRY VOID VOLATILE WHILE

/* Operators and other funny multi-character symbols */
%token AssAdd AssAnd AssAsr AssDiv AssLsl AssLsr AssMod AssMul AssOr AssSub
%token AssXor OpAsr OpDec OpEq OpGeq OpInc OpLAnd OpLOr OpLeq OpLsl OpLsr OpNeq
%token Send

/* Compound terminals */
%token Character Identifier Number String

/* This is for the benefit of YaccHelper -- we don't actually use it here */
%token Symbol

%%

eCompile:
        '.' {pushDistContext();} compilationUnit
{
    YC(compilationUnit,$3)->finalWS = flushWhitespace();
    YRESULT($3);
}
 ;

compilationUnit:
        optPackageDeclaration optImportDeclarations optTypeDeclarations
{
    $$ = YH_BUILD(compilationUnit)(YC(packageDeclaration,$1),
                                   YC(importDeclarationList,$2),
                                   YC(typeDeclarationList,$3),
                                   NULL);
}
 ;

optPackageDeclaration:
        PACKAGE name ';'
{
    $$ = YH_BUILD(packageDeclaration)(WS($1), YC(name,$2), WS($3));
}
 |
{
    $$ = 0;
}
 ;

optImportDeclarations:
        importDeclarations
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

importDeclarations:
        importDeclaration
{
    $$ = YH_BUILD(importDeclarationList)(YC(importDeclaration,$1), NULL);
}
 |      importDeclarations importDeclaration
{
    $$ = YH_BUILD(importDeclarationList)(YC(importDeclaration,$2),
                                         YC(importDeclarationList,$1));
}
 ;

importDeclaration:
        IMPORT name ';'
{
    $$ = YH_BUILD(importDeclaration)(WS($1), YC(name,$2), NULL, NULL, WS($3));
}
 |      IMPORT name '.' '*' ';'
{
    $$ = YH_BUILD(importDeclaration)(WS($1), YC(name,$2), WS($3), WS($4),
                                     WS($5));
}
 ;

optTypeDeclarations:
        typeDeclarations
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

typeDeclarations:
        typeDeclaration
{
    $$ = YH_BUILD(typeDeclarationList)(YC(typeDeclaration,$1), NULL);
}
 |      typeDeclarations typeDeclaration
{
    $$ = YH_BUILD(typeDeclarationList)(YC(typeDeclaration,$2),
                                       YC(typeDeclarationList,$1));
}
 ;

typeDeclaration:
        classDeclaration
{
    $$ = $1;
}
 |      eclassDeclaration
{
    $$ = $1;
}
 |      interfaceDeclaration
{
    $$ = $1;
}
 |      einterfaceDeclaration
{
    $$ = $1;
}
 |      ';'
{
    $$ = YH_BUILD(nullDeclaration)(WS($1));
}
 |      error typeDeclaration
{
    $$ = $2;
}
 ;

classDeclaration:
        optModifiers CLASS Identifier optSuper optInterfaces classBody
{
    $$ = YH_BUILD(classDeclaration)(YC(modifierList,$1), WS($2),
        YC(identifier,$3), YC(extends,$4), YC(implements,$5),
        YC(classBody,$6));
}
 ;

eclassDeclaration:
        optModifiers ECLASS Identifier optSuper optInterfaces eclassBody
{
    $$ = YH_BUILD(eclassDeclaration)(YC(modifierList,$1), WS($2),
        YC(identifier,$3), YC(extends,$4), YC(implements,$5),
        YC(classBody,$6));
}
 ;

optModifiers:
        modifiers
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

modifiers:
        modifier
{
    $$ = YH_BUILD(modifierList)(YC(modifier,$1), NULL);
}
 |      modifiers modifier
{
    $$ = YH_BUILD(modifierList)(YC(modifier,$2), YC(modifierList,$1));
}
 ;

modifier:
        ABSTRACT
{
    $$ = YH_BUILD(modifier)(ABSTRACT, WS($1));
}
 |      FINAL
{
    $$ = YH_BUILD(modifier)(FINAL, WS($1));
}
 |      LOCAL
{
    $$ = YH_BUILD(modifier)(LOCAL, WS($1));
}
 |      NATIVE
{
    $$ = YH_BUILD(modifier)(NATIVE, WS($1));
}
 |      PRIVATE
{
    $$ = YH_BUILD(modifier)(PRIVATE, WS($1));
}
 |      PROTECTED
{
    $$ = YH_BUILD(modifier)(PROTECTED, WS($1));
}
 |      PUBLIC
{
    $$ = YH_BUILD(modifier)(PUBLIC, WS($1));
}
 |      STATIC
{
    $$ = YH_BUILD(modifier)(STATIC, WS($1));
}
 |      SYNCHRONIZED
{
    $$ = YH_BUILD(modifier)(SYNCHRONIZED, WS($1));
}
 |      TRANSIENT
{
    $$ = YH_BUILD(modifier)(TRANSIENT, WS($1));
}
 |      VOLATILE
{
    $$ = YH_BUILD(modifier)(VOLATILE, WS($1));
}
 ;

optSuper:
        EXTENDS name
{
    $$ = YH_BUILD(extends)(WS($1), YC(name,$2));
}
 |
{
    $$ = 0;
}
 ;

optInterfaces:
        IMPLEMENTS nameList
{
    $$ = YH_BUILD(implements)(WS($1), YC(nameSequence,$2));
}
 |
{
    $$ = 0;
}
 ;

nameList:
        name
{
    $$ = YH_BUILD(nameSequence)(NULL, NULL, YC(name,$1));
}
 |      nameList ',' name
{
    $$ = YH_BUILD(nameSequence)(YC(nameSequence,$1), WS($2), YC(name,$3));
}
 ;

name:
        Identifier
{
    $$ = YH_BUILD(name)(NULL, NULL, YC(identifier,$1));
}
 |      name '.' Identifier
{
    $$ = YH_BUILD(name)(YC(name,$1), WS($2), YC(identifier,$3));
}
 ;

interfaceDeclaration:
        optModifiers INTERFACE Identifier optExtendsInterfaces interfaceBody
{
    $$ = YH_BUILD(interfaceDeclaration)(YC(modifierList,$1), WS($2),
        YC(identifier,$3), YC(interfaceExtends,$4), YC(classBody,$5));
}
 ;

einterfaceDeclaration:
        optModifiers EINTERFACE Identifier optExtendsInterfaces einterfaceBody
{
    $$ = YH_BUILD(einterfaceDeclaration)(YC(modifierList,$1), WS($2),
        YC(identifier,$3), YC(interfaceExtends,$4), YC(classBody,$5));
}
 ;

optExtendsInterfaces:
        EXTENDS nameList
{
    $$ = YH_BUILD(interfaceExtends)(WS($1), YC(nameSequence,$2));
}
 |
{
    $$ = 0;
}
 ;

classBody:
        '{' classBodyDeclarations '}'
{
    $$ = YH_BUILD(classBody)(WS($1), YC(classBodyDeclarationList,$2), WS($3));
}
 |      '{'                       '}'
{
    $$ = YH_BUILD(classBody)(WS($1), NULL, WS($2));
}
 ;

eclassBody:
        '{' eclassBodyDeclarations '}'
{
    $$ = YH_BUILD(classBody)(WS($1), YC(classBodyDeclarationList,$2), WS($3));
}
 |      '{'                       '}'
{
    $$ = YH_BUILD(classBody)(WS($1), NULL, WS($2));
}
 ;

classBodyDeclarations:
        classBodyDeclaration
{
    $$ = YH_BUILD(classBodyDeclarationList)(YC(classBodyDeclaration,$1), NULL);
}
 |      classBodyDeclarations classBodyDeclaration
{
    $$ = YH_BUILD(classBodyDeclarationList)(YC(classBodyDeclaration,$2),
                                            YC(classBodyDeclarationList,$1));
}
 ;

eclassBodyDeclarations:
        eclassBodyDeclaration
{
    $$ = YH_BUILD(classBodyDeclarationList)(YC(classBodyDeclaration,$1), NULL);
}
 |      eclassBodyDeclarations eclassBodyDeclaration
{
    $$ = YH_BUILD(classBodyDeclarationList)(YC(classBodyDeclaration,$2),
                                            YC(classBodyDeclarationList,$1));
}
 ;

classBodyDeclaration:
        fieldDeclaration
{
    $$ = $1;
}
 |      methodDeclaration
{
    $$ = $1;
}
 |      constructorDeclaration
{
    $$ = $1;
}
 |      STATIC block
{
    $$ = YH_BUILD(staticInitializer)(WS($1), YC(block,$2));
}
 |      block
{
    $$ = $1;
}
 |      typeDeclaration
{
    $$ = $1;
}
 ;

eclassBodyDeclaration:
        classBodyDeclaration
{
    $$ = $1;
}
 |      emethodDeclaration
{
    $$ = $1;
}
 ;

fieldDeclaration:
        optModifiers type variableDeclarators ';'
{
    $$ = YH_BUILD(fieldDeclaration)(YC(modifierList,$1), YC(type,$2),
        YC(variableDeclaratorList,$3), WS($4));
}
 ;

variableDeclarators:
        variableDeclarator
{
    $$ = YH_BUILD(variableDeclaratorList)(YC(variableDeclarator,$1), NULL);
}
 |      variableDeclarators ',' variableDeclarator
{
    YC(variableDeclarator,$3)->commaWS = WS($2);
    $$ = YH_BUILD(variableDeclaratorList)(YC(variableDeclarator,$3),
                                          YC(variableDeclaratorList,$1));
}
 ;

variableDeclarator:
        variableDeclaratorId
{
    $$ = $1;
}
 |      variableDeclaratorId '=' variableInitializer
{
    YC(variableDeclarator,$1)->equalsWS = WS($2);
    YC(variableDeclarator,$1)->initializer = YC(variableInitializer,$3);
    $$ = $1;
}
 ;

variableDeclaratorId:
        Identifier
{
    $$ = YH_BUILD(variableDeclarator)(NULL, YC(identifier,$1), NULL, NULL,
                                      NULL);
}
 |      Identifier bracketsList
{
    $$ = YH_BUILD(variableDeclarator)(NULL, YC(identifier,$1),
                                      YC(bracketsList,$2), NULL, NULL);
}
 ;

bracketsList:
        '[' ']'
{
    $$ = YH_BUILD(bracketsList)(YBUILD(brackets)(WS($1),WS($2)), NULL);
}
 |      bracketsList '[' ']'
{
    $$ = YH_BUILD(bracketsList)(YBUILD(brackets)(WS($2),WS($3)),
                                YC(bracketsList,$1));
}
 ;

variableInitializer:
        expression
{
    $$ = $1;
}
 |      arrayInitializer
{
    $$ = $1;
}
 ;

arrayInitializer:
        '{' variableInitializers ',' '}'
{
    $$ = YH_BUILD(arrayInitializer)(WS($1), YC(variableInitializers,$2),
                                    WS($3), WS($4));
}
 |      '{' variableInitializers     '}'
{
    $$ = YH_BUILD(arrayInitializer)(WS($1), YC(variableInitializers,$2),
                                    NULL, WS($3));
}
 |      '{'                      ',' '}'
{
    $$ = YH_BUILD(arrayInitializer)(WS($1), NULL, WS($2), WS($3));
}
 |      '{'                          '}'
{
    $$ = YH_BUILD(arrayInitializer)(WS($1), NULL, NULL, WS($2));
}
 ;

variableInitializers:
        variableInitializer
{
    $$ = YH_BUILD(variableInitializers)(NULL, NULL,
                                        YC(variableInitializer,$1));
}
 |      variableInitializers ',' variableInitializer
{
    $$ = YH_BUILD(variableInitializers)(YC(variableInitializers,$1), WS($2),
                                        YC(variableInitializer,$3));
}
 ;

methodDeclaration:
        methodHeader block
{
    $$ = YH_BUILD(methodDeclaration)(YC(methodHeader,$1), YC(block,$2));
}
 |      methodHeader ';'
{
    $$ = YH_BUILD(methodStub)(YC(methodHeader,$1), WS($2));
}
 ;

emethodDeclaration:
        emethodHeader block
{
    $$ = YH_BUILD(emethodDeclaration)(YC(emethodHeader,$1), YC(block,$2));
}
 ;

methodHeader:
        optModifiers type methodDeclarator optThrows
{
    $$ = YH_BUILD(methodHeaderTyped)(YC(modifierList,$1), YC(type,$2),
                                     YC(methodDeclarator,$3), YC(throws,$4));
}
 |      optModifiers VOID methodDeclarator optThrows
{
    $$ = YH_BUILD(methodHeaderVoid)(YC(modifierList,$1), WS($2),
                                    YC(methodDeclarator,$3), YC(throws,$4));
}
 ;

emethodHeader:
        optModifiers EMETHOD emethodDeclarator optThrows
{
    $$ = YH_BUILD(emethodHeader)(YC(modifierList,$1), WS($2),
                                 YC(methodDeclarator,$3), YC(throws,$4));
}
 ;

methodDeclarator:
        methodProto
{
    $$ = $1;
}
 |      methodProto bracketsList
{
    YC(methodDeclarator,$1)->brackets = YC(bracketsList,$2);
    $$ = $1;
}
 ;

methodProto:
        Identifier '(' formalParameterList ')'
{
    $$ = YH_BUILD(methodDeclarator)(YC(identifier,$1), WS($2),
                                    YC(formalParameterList,$3), WS($4), NULL);
}
 |      Identifier '('                     ')'
{
    $$ = YH_BUILD(methodDeclarator)(YC(identifier,$1), WS($2),
                                    NULL, WS($3), NULL);
}
 ;

emethodDeclarator:
        methodProto
{
    $$ = $1;
}
 ;

formalParameterList:
        formalParameter
{
    $$ = YH_BUILD(formalParameterList)(YC(formalParameter,$1), NULL);
}
 |      formalParameterList ',' formalParameter
{
    YC(formalParameter,$3)->commaWS = WS($2);
    $$ = YH_BUILD(formalParameterList)(YC(formalParameter,$3),
                                       YC(formalParameterList,$1));
}
 ;

formalParameter:
        type variableDeclaratorId
{
    $$ = YH_BUILD(formalParameter)(NULL, YC(type,$1),
                                   YC(variableDeclarator,$2));
}
 ;

optThrows:
        THROWS nameList
{
    $$ = YH_BUILD(throws)(WS($1), YC(nameSequence,$2));
}
 |
{
    $$ = 0;
}
 ;

constructorDeclaration:
        optModifiers methodProto optThrows block/*constructorBody*/
{
    $$ = YH_BUILD(constructorDeclaration)(YC(modifierList,$1),
        YC(methodDeclarator,$2), YC(throws,$3), YC(constructorBody,$4));
}
 ;

constructorInvocation:
        THIS '(' optArgumentList ')'
{
    $$ = YH_BUILD(constructorInvocation)(THIS, WS($1), WS($2),
        YC(expressionSequence,$3), WS($4));
}
 |      SUPER '(' optArgumentList ')'
{
    $$ = YH_BUILD(constructorInvocation)(SUPER, WS($1), WS($2),
        YC(expressionSequence,$3), WS($4));
}
 ;

interfaceBody:
        '{' interfaceMemberDeclarations '}'
{
    $$ = YH_BUILD(classBody)(WS($1), YC(classBodyDeclarationList,$2), WS($3));
}
 |      '{'                             '}'
{
    $$ = YH_BUILD(classBody)(WS($1), NULL, WS($2));
}
 ;

einterfaceBody:
        '{' einterfaceMemberDeclarations '}'
{
    $$ = YH_BUILD(classBody)(WS($1), YC(classBodyDeclarationList,$2), WS($3));
}
 |      '{'                             '}'
{
    $$ = YH_BUILD(classBody)(WS($1), NULL, WS($2));
}
 ;

interfaceMemberDeclarations:
        interfaceMemberDeclaration
{
    $$ = YH_BUILD(classBodyDeclarationList)(YC(classBodyDeclaration,$1), NULL);
}
 |      interfaceMemberDeclarations interfaceMemberDeclaration
{
    $$ = YH_BUILD(classBodyDeclarationList)(YC(classBodyDeclaration,$2),
                                            YC(classBodyDeclarationList,$1));
}
 ;

einterfaceMemberDeclarations:
        einterfaceMemberDeclaration
{
    $$ = YH_BUILD(classBodyDeclarationList)(YC(classBodyDeclaration,$1), NULL);
}
 |      einterfaceMemberDeclarations einterfaceMemberDeclaration
{
    $$ = YH_BUILD(classBodyDeclarationList)(YC(classBodyDeclaration,$2),
                                            YC(classBodyDeclarationList,$1));
}
 ;

interfaceMemberDeclaration:
        fieldDeclaration
{
    $$ = $1;
}
 |      methodHeader ';'
{
    $$ = YH_BUILD(methodStub)(YC(methodHeader,$1), WS($2));
}
 ;

einterfaceMemberDeclaration:
        EMETHOD emethodInterfaceHeader ';'
{
    $$ = YH_BUILD(emethodStub)(WS($1), YC(emethodHeader,$2), WS($3));
}
 |              emethodInterfaceHeader ';'
{
    $$ = YH_BUILD(emethodStub)(NULL, YC(emethodHeader,$1), WS($2));
}
 ;

emethodInterfaceHeader:
        optModifiers emethodDeclarator optThrows
{
    $$ = YH_BUILD(emethodHeader)(YC(modifierList,$1), NULL,
                                 YC(methodDeclarator,$2), YC(throws,$3));
}
 ;

block:
        '{' {pushDistContext();} blockStatements '}'
{
    $$ = YH_BUILD(block)(WS($1), YC(statementList,$3), WS($4),
                         CurrentDistContext);
    popDistContext();
}
 |      '{'                 '}'
{
    $$ = YH_BUILD(block)(WS($1), NULL, WS($2), NULL);
}
 ;

blockStatements:
        blockStatement
{
    $$ = YH_BUILD(statementList)(YC(statement,$1), NULL);
}
 |      blockStatements blockStatement
{
    $$ = YH_BUILD(statementList)(YC(statement,$2), YC(statementList,$1));
}
 ;

blockStatement:
        localVariableDeclaration ';'
{
    $$ = YH_BUILD(variableDeclarationStatement)(
        YC(localVariableDeclaration,$1), WS($2));
}
 |      statement
{
    $$ = $1;
}
 ;

localVariableDeclaration:
        type variableDeclarators
{
    $$ = YH_BUILD(localVariableDeclaration)(YC(type,$1),
                                            YC(variableDeclaratorList,$2));
}
 ;

statement:
        statementWithoutTrailingSubstatement
{
    $$ = $1;
}
 |      Identifier ':' statement
{
    $$ = YH_BUILD(labelledStatement)(YC(identifier,$1), WS($2),
                                     YC(statement,$3));
}
 |      IF '(' expression ')' statement
{
    $$ = YH_BUILD(ifStatement)(WS($1), WS($2), YC(expression,$3), WS($4),
                               YC(statement,$5), NULL, NULL);
}
 |      IF '(' expression ')' statementNoShortIf ELSE statement
{
    $$ = YH_BUILD(ifStatement)(WS($1), WS($2), YC(expression,$3), WS($4),
                               YC(statement,$5), WS($6), YC(statement,$7));
}
 |      WHILE '(' expression ')' statement
{
    $$ = YH_BUILD(whileStatement)(WS($1), WS($2), YC(expression,$3), WS($4),
                                  YC(statement,$5));
}
 |      EKEEP '(' expression ')' statement
{
    $$ = YH_BUILD(ekeepStatement)(WS($1), WS($2), YC(expression,$3), WS($4),
                                  YC(statement,$5));
}
 |      FOR '(' optForInit ';' optExpression ';' optForUpdate ')' statement
{
    $$ = YH_BUILD(forStatement)(WS($1), WS($2), YC(expression,$3), WS($4),
                                YC(expression,$5), WS($6), YC(expression,$7),
                                WS($8), YC(statement,$9));
}
 ;

statementNoShortIf:
        statementWithoutTrailingSubstatement
{
    $$ = $1;
}
 |      Identifier ':' statementNoShortIf
{
    $$ = YH_BUILD(labelledStatement)(YC(identifier,$1), WS($2),
                                     YC(statement,$3));
}
 |      IF '(' expression ')' statementNoShortIf ELSE statementNoShortIf
{
    $$ = YH_BUILD(ifStatement)(WS($1), WS($2), YC(expression,$3), WS($4),
                               YC(statement,$5), WS($6), YC(statement,$7));
}
 |      WHILE '(' expression ')' statementNoShortIf
{
    $$ = YH_BUILD(whileStatement)(WS($1), WS($2), YC(expression,$3), WS($4),
                                  YC(statement,$5));
}
 |      EKEEP '(' expression ')' statementNoShortIf
{
    $$ = YH_BUILD(ekeepStatement)(WS($1), WS($2), YC(expression,$3), WS($4),
                                  YC(statement,$5));
}
 |      FOR '(' optForInit ';' optExpression ';' optForUpdate ')' statementNoShortIf
{
    $$ = YH_BUILD(forStatement)(WS($1), WS($2), YC(expression,$3), WS($4),
                                YC(expression,$5), WS($6), YC(expression,$7),
                                WS($8), YC(statement,$9));
}
 ;

optForInit:
        statementExpressionList
{
    $$ = $1;
}
 |      localVariableDeclaration
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

optExpression:
        expression
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

optForUpdate:
        statementExpressionList
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

statementExpressionList:
        statementExpression
{
    $$ = YH_BUILD(expressionSequence)(NULL, NULL, YC(expression,$1));
}
 |      statementExpressionList ',' statementExpression
{
    $$ = YH_BUILD(expressionSequence)(YC(expressionSequence,$1), WS($2),
                                      YC(expression,$3));
}
 ;

statementWithoutTrailingSubstatement:
        block
{
    $$ = $1;
}
 |      error block
{
    $$ = $2;
}
 |      ';'
{
    $$ = YH_BUILD(nullStatement)(WS($1));
}
 |      error ';'
{
    $$ = YH_BUILD(nullStatement)(WS($2));
}
 |      statementExpression ';'
{
    $$ = YH_BUILD(expressionStatement)(YC(expression,$1), WS($2));
}
 |      SWITCH '(' expression ')' switchBlock
{
    $$ = YH_BUILD(switchStatement)(WS($1), WS($2), YC(expression,$3), WS($4),
                                   YC(switchBlock,$5));
}
 |      DO statement WHILE '(' expression ')' ';'
{
    $$ = YH_BUILD(doStatement)(WS($1), YC(statement,$2), WS($3), WS($4),
                               YC(expression,$5), WS($6), WS($7));
}
 |      BREAK Identifier ';'
{
    $$ = YH_BUILD(breakStatement)(WS($1), YC(identifier,$2), WS($3));
}
 |      BREAK            ';'
{
    $$ = YH_BUILD(breakStatement)(WS($1), NULL, WS($2));
}
 |      CONTINUE Identifier ';'
{
    $$ = YH_BUILD(continueStatement)(WS($1), YC(identifier,$2), WS($3));
}
 |      CONTINUE            ';'
{
    $$ = YH_BUILD(continueStatement)(WS($1), NULL, WS($2));
}
 |      RETURN optExpression ';'
{
    $$ = YH_BUILD(returnStatement)(WS($1), YC(expression,$2), WS($3));
}
 |      SYNCHRONIZED '(' expression ')' block
{
    $$ = YH_BUILD(synchronizedStatement)(WS($1), WS($2), YC(expression,$3),
                                         WS($4), YC(block,$5));
}
 |      THROW expression ';'
{
    $$ = YH_BUILD(throwStatement)(WS($1), YC(expression,$2), WS($3));
}
 |      ETHROW expression ';'
{
    $$ = YH_BUILD(ethrowStatement)(WS($1), YC(expression,$2), WS($3));
}
 |      TRY block catches
{
    $$ = YH_BUILD(tryStatement)(WS($1), YC(block,$2), YC(catchList,$3), NULL);
}
 |      TRY block catches finally
{
    $$ = YH_BUILD(tryStatement)(WS($1), YC(block,$2), YC(catchList,$3),
                                YC(finally,$4));
}
 |      TRY block         finally
{
    $$ = YH_BUILD(tryStatement)(WS($1), YC(block,$2), NULL, YC(finally,$3));
}
 |      ETRY block ecatches
{
    $$ = YH_BUILD(etryStatement)(WS($1), YC(block,$2), YC(ecatchList,$3));
}
 |      EDEBUG block
{
    $$ = YH_BUILD(edebugStatement)(WS($1), YC(block,$2));
}
 |      EWHEN ewhenTargetExpression '(' formalParameter ')' block
{
    $$ = YH_BUILD(ewhenStatement)(WS($1), YC(expression,$2), WS($3),
                                  YC(formalParameter,$4), WS($5), YC(block,$6),
                                  NULL);
}
 |      EWHEN ewhenTargetExpression '(' formalParameter ')' block eorwhens
{
    $$ = YH_BUILD(ewhenStatement)(WS($1), YC(expression,$2), WS($3),
                                  YC(formalParameter,$4), WS($5), YC(block,$6),
                                  YC(eorwhenStatementList,$7));
}
 |      EWHENEVER ewhenTargetExpression '(' formalParameter ')' block
{
    $$ = YH_BUILD(ewheneverStatement)(WS($1), YC(expression,$2), WS($3),
                                      YC(formalParameter,$4), WS($5),
                                      YC(block,$6));
}
 |      EIF '(' expression ')' block
{
    $$ = YH_BUILD(eifStatement)(WS($1), WS($2), YC(expression,$3), WS($4),
                                YC(block,$5), NULL, NULL, NULL);
}
 |      EIF '(' expression ')' block ELSE block
{
    $$ = YH_BUILD(eifStatement)(WS($1), WS($2), YC(expression,$3), WS($4),
                                YC(block,$5), NULL, WS($6), YC(block,$7));
}
 |      EIF '(' expression ')' block eorifs
{
    $$ = YH_BUILD(eifStatement)(WS($1), WS($2), YC(expression,$3), WS($4),
        YC(block,$5), YC(eorifStatementList,$6), NULL, NULL);
}
 |      EIF '(' expression ')' block eorifs ELSE block
{
    $$ = YH_BUILD(eifStatement)(WS($1), WS($2), YC(expression,$3), WS($4),
        YC(block,$5), YC(eorifStatementList,$6), WS($7), YC(block,$8));
}
 ;

ewhenTargetExpression:
        name
{
    $$ = $1;
}
 |      name '[' expression ']'
{
    $$ = YH_BUILD(arrayAccess)(YC(expression,$1), WS($2), YC(expression,$3),
                               WS($4));
}
 |      '(' expression ')'
{
    $$ = YH_BUILD(subexpression)(WS($1), YC(expression,$2), WS($3));
}
 ;

eorwhens:
        eorwhen
{
    $$ = YH_BUILD(eorwhenStatementList)(YC(eorwhenStatement,$1), NULL);
}
 |      eorwhens eorwhen
{
    $$ = YH_BUILD(eorwhenStatementList)(YC(eorwhenStatement,$2),
                                        YC(eorwhenStatementList,$1));
}
 ;

eorwhen:
        EORWHEN ewhenTargetExpression '(' formalParameter ')' block
{
    $$ = YH_BUILD(eorwhenStatement)(WS($1), YC(expression,$2), WS($3),
        YC(formalParameter,$4), WS($5), YC(block,$6));
}
 ;

eorifs:
        eorif
{
    $$ = YH_BUILD(eorifStatementList)(YC(eorifStatement,$1), NULL);
}
 |      eorifs eorif
{
    $$ = YH_BUILD(eorifStatementList)(YC(eorifStatement,$2),
                                      YC(eorifStatementList,$1));
}
 ;

eorif:
        EORIF '(' expression ')' block
{
    $$ = YH_BUILD(eorifStatement)(WS($1), WS($2), YC(expression,$3), WS($4),
                                  YC(block,$5));
}
 ;

switchBlock:
        '{' switchBlockStatementGroups switchLabels '}'
{
    YT(switchGroup) *endGroup =
        YBUILD(switchGroup)(YC(switchLabelList,$3), NULL);
    $$ = YH_BUILD(switchBlock)(
        WS($1),
        YBUILD(switchGroupList)(endGroup, YC(switchGroupList,$2)),
        WS($4));
}
 |      '{' switchBlockStatementGroups              '}'
{
    $$ = YH_BUILD(switchBlock)(WS($1), YC(switchGroupList,$2), WS($3));
}
 |      '{'                            switchLabels '}'
{
    YT(switchGroup) *endGroup =
        YBUILD(switchGroup)(YC(switchLabelList,$2), NULL);
    $$ = YH_BUILD(switchBlock)(
        WS($1),
        YBUILD(switchGroupList)(endGroup, NULL),
        WS($3));
}
 |      '{'                                         '}'
{
    $$ = YH_BUILD(switchBlock)(WS($1), NULL, WS($2));
}
 ;

switchBlockStatementGroups:
        switchBlockStatementGroup
{
    $$ = YH_BUILD(switchGroupList)(YC(switchGroup,$1), NULL);
}
 |      switchBlockStatementGroups switchBlockStatementGroup
{
    $$ = YH_BUILD(switchGroupList)(YC(switchGroup,$2), YC(switchGroupList,$1));
}
 ;

switchBlockStatementGroup:
        switchLabels blockStatements
{
    $$ = YH_BUILD(switchGroup)(YC(switchLabelList,$1), YC(statementList,$2));
}
 ;

switchLabels:
        switchLabel
{
    $$ = YH_BUILD(switchLabelList)(YC(switchLabel,$1), NULL);
}
 |      switchLabels switchLabel
{
    $$ = YH_BUILD(switchLabelList)(YC(switchLabel,$2), YC(switchLabelList,$1));
}
 ;

switchLabel:
        CASE expression ':'
{
    $$ = YH_BUILD(caseLabel)(WS($1), YC(expression,$2), WS($3));
}
 |      DEFAULT ':'
{
    $$ = YH_BUILD(defaultLabel)(WS($1), WS($2));
}
 ;

catches:
        catchClause
{
    $$ = YH_BUILD(catchList)(YC(catch,$1), NULL);
}
 |      catches catchClause
{
    $$ = YH_BUILD(catchList)(YC(catch,$2), YC(catchList,$1));
}
 ;

catchClause:
        CATCH '(' formalParameter ')' block
{
    $$ = YH_BUILD(catch)(WS($1), WS($2), YC(formalParameter,$3), WS($4),
                         YC(block,$5));
}
 ;

ecatches:
        ecatchClause
{
    $$ = YH_BUILD(ecatchList)(YC(ecatch,$1), NULL);
}
 |      ecatches ecatchClause
{
    $$ = YH_BUILD(ecatchList)(YC(ecatch,$2), YC(ecatchList,$1));
}
 ;

ecatchClause:
        ECATCH '(' formalParameter ')' block
{
    $$ = YH_BUILD(ecatch)(WS($1), WS($2), YC(formalParameter,$3), WS($4),
                          YC(block,$5));
}
 ;

finally:
        FINALLY block
{
    $$ = YH_BUILD(finally)(WS($1), YC(block,$2));
}
 ;

statementExpression:
        assignment
{
    $$ = $1;
}
 |      sendExpression
{
    $$ = $1;
}
 |      preCrementExpression
{
    $$ = $1;
}
 |      postCrementExpression
{
    $$ = $1;
}
 |      classInstanceCreationExpression
{
    $$ = $1;
}
 |      methodInvocation
{
    $$ = $1;
}
 |      constructorInvocation
{
    $$ = $1;
}
 ;

assignment:
        leftHandSide assignmentOperator expression
{
    $$ = YH_BUILD(binop)(YC(expression,$1), YC(operator,$2),
                         YC(expression,$3));
}
 ;

leftHandSide:
        name
{
    $$ = $1;
}
 |      fieldAccess
{
    $$ = $1;
}
 |      arrayAccess
{
    $$ = $1;
}
 ;

assignmentOperator:
        '='
{
    $$ = YH_BUILD(operator)('=', WS($1));
}
 |      AssAdd
{
    $$ = YH_BUILD(operator)(AssAdd, WS($1));
}
 |      AssAnd
{
    $$ = YH_BUILD(operator)(AssAnd, WS($1));
}
 |      AssAsr
{
    $$ = YH_BUILD(operator)(AssAsr, WS($1));
}
 |      AssDiv
{
    $$ = YH_BUILD(operator)(AssDiv, WS($1));
}
 |      AssLsl
{
    $$ = YH_BUILD(operator)(AssLsl, WS($1));
}
 |      AssLsr
{
    $$ = YH_BUILD(operator)(AssLsr, WS($1));
}
 |      AssMod
{
    $$ = YH_BUILD(operator)(AssMod, WS($1));
}
 |      AssMul
{
    $$ = YH_BUILD(operator)(AssMul, WS($1));
}
 |      AssOr
{
    $$ = YH_BUILD(operator)(AssOr, WS($1));
}
 |      AssSub
{
    $$ = YH_BUILD(operator)(AssSub, WS($1));
}
 |      AssXor
{
    $$ = YH_BUILD(operator)(AssXor, WS($1));
}
 ;

expression:
        conditionalExpression
{
    $$ = $1;
}
 |      assignment
{
    $$ = $1;
}
 |      sendExpression
{
    $$ = $1;
}
 ;

sendExpression:
        conditionalExpression Send expression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)(Send, WS($2)),
                         YC(expression,$3));
}
 ;

conditionalExpression:
        conditionalOrExpression
{
    $$ = $1;
}
 |      conditionalOrExpression '?' expression ':' conditionalExpression
{
    $$ = YH_BUILD(conditionalExpression)(YC(expression,$1), WS($2),
                                         YC(expression,$3), WS($4),
                                         YC(expression,$5));
}
 ;

conditionalOrExpression:
        conditionalAndExpression
{
    $$ = $1;
}
 |      conditionalOrExpression OpLOr conditionalAndExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)(OpLOr, WS($2)),
                         YC(expression,$3));
}
 ;

conditionalAndExpression:
        inclusiveOrExpression
{
    $$ = $1;
}
 |      conditionalAndExpression OpLAnd inclusiveOrExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)(OpLAnd, WS($2)),
                         YC(expression,$3));
}
 ;

inclusiveOrExpression:
        exclusiveOrExpression
{
    $$ = $1;
}
 |      inclusiveOrExpression '|' exclusiveOrExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)('|', WS($2)),
                         YC(expression,$3));
}
 ;

exclusiveOrExpression:
        andExpression
{
    $$ = $1;
}
 |      exclusiveOrExpression '^' andExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)('^', WS($2)),
                         YC(expression,$3));
}
 ;

andExpression:
        equalityExpression
{
    $$ = $1;
}
 |      andExpression '&' equalityExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)('&', WS($2)),
                         YC(expression,$3));
}
 ;

equalityExpression:
        relationalExpression
{
    $$ = $1;
}
 |      equalityExpression OpEq relationalExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)(OpEq, WS($2)),
                         YC(expression,$3));
}
 |      equalityExpression OpNeq relationalExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)(OpNeq, WS($2)),
                         YC(expression,$3));
}
 ;

relationalExpression:
        shiftExpression
{
    $$ = $1;
}
 |      relationalExpression '<' shiftExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)('<', WS($2)),
                         YC(expression,$3));
}
 |      relationalExpression '>' shiftExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)('>', WS($2)),
                         YC(expression,$3));
}
 |      relationalExpression OpLeq shiftExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)(OpLeq, WS($2)),
                         YC(expression,$3));
}
 |      relationalExpression OpGeq shiftExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)(OpGeq, WS($2)),
                         YC(expression,$3));
}
 |      relationalExpression INSTANCEOF referenceType
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)(INSTANCEOF, WS($2)),
                         YC(expression,$3));
}
 ;

shiftExpression:
        additiveExpression
{
    $$ = $1;
}
 |      shiftExpression OpLsl additiveExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)(OpLsl, WS($2)),
                         YC(expression,$3));
}
 |      shiftExpression OpLsr additiveExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)(OpLsr, WS($2)),
                         YC(expression,$3));
}
 |      shiftExpression OpAsr additiveExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)(OpAsr, WS($2)),
                         YC(expression,$3));
}
 ;

additiveExpression:
        multiplicativeExpression
{
    $$ = $1;
}
 |      additiveExpression '+' multiplicativeExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)('+', WS($2)),
                         YC(expression,$3));
}
 |      additiveExpression '-' multiplicativeExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)('-', WS($2)),
                         YC(expression,$3));
}
 ;

multiplicativeExpression:
        unaryExpression
{
    $$ = $1;
}
 |      multiplicativeExpression '*' unaryExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)('*', WS($2)),
                         YC(expression,$3));
}
 |      multiplicativeExpression '/' unaryExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)('/', WS($2)),
                         YC(expression,$3));
}
 |      multiplicativeExpression '%' unaryExpression
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)('%', WS($2)),
                         YC(expression,$3));
}
 ;

unaryExpression:
        preCrementExpression
{
    $$ = $1;
}
 |      '+' unaryExpression
{
    $$ = YH_BUILD(unop)(YBUILD(operator)('+', WS($1)),
                        YC(expression,$2));
}
 |      '-' unaryExpression
{
    $$ = YH_BUILD(unop)(YBUILD(operator)('-', WS($1)),
                        YC(expression,$2));
}
 |      '&' name
{
    CurrentDistContext->names =
        YBUILD(nameList)(YC(name,$2), CurrentDistContext->names);
    $$ = YH_BUILD(distop)(WS($1), YC(name,$2));
}
 |      unaryExpressionNotPlusMinus
{
    $$ = $1;
}
 ;

preCrementExpression:
        OpInc unaryExpression
{
    $$ = YH_BUILD(unop)(YBUILD(operator)(OpInc, WS($1)),
                        YC(expression,$2));
}
 |      OpDec unaryExpression
{
    $$ = YH_BUILD(unop)(YBUILD(operator)(OpDec, WS($1)),
                        YC(expression,$2));
}
 ;

unaryExpressionNotPlusMinus:
        postfixExpression
{
    $$ = $1;
}
 |      '~' unaryExpression
{
    $$ = YH_BUILD(unop)(YBUILD(operator)('~', WS($1)),
                        YC(expression,$2));
}
 |      '!' unaryExpression
{
    $$ = YH_BUILD(unop)(YBUILD(operator)('!', WS($1)),
                        YC(expression,$2));
}
 |      castExpression
{
    $$ = $1;
}
 ;

postfixExpression:
        primary
{
    $$ = $1;
}
 |      name
{
    $$ = $1;
}
 |      postCrementExpression
{
    $$ = $1;
}
 ;

postCrementExpression:
        postfixExpression OpInc
{
    $$ = YH_BUILD(postop)(YC(expression,$1),
                          YBUILD(operator)(OpInc, WS($2)));
}
 |      postfixExpression OpDec
{
    $$ = YH_BUILD(postop)(YC(expression,$1),
                          YBUILD(operator)(OpDec, WS($2)));
}
 ;

castExpression:
        '(' primitiveType optBracketsList ')' unaryExpression
{
    $$ = YH_BUILD(castExpression)(WS($1), YC(type,$2), YC(bracketsList,$3),
                                  WS($4), YC(expression,$5));
}
 |      '(' expression ')' unaryExpressionNotPlusMinus
{
    $$ = YH_BUILD(castExpression)(WS($1), YC(type,$2), NULL, WS($3),
                                  YC(expression,$4));
}
 |      '(' name bracketsList ')' unaryExpressionNotPlusMinus
{
    $$ = YH_BUILD(castExpression)(WS($1), YC(type,$2), YC(bracketsList,$3),
                                  WS($4), YC(expression,$5));
}
 ;

type:
        primitiveType
{
    $$ = $1;
}
 |      referenceType
{
    $$ = $1;
}
 ;

primitiveType:
        BOOLEAN
{
    $$ = YH_BUILD(primType)(BOOLEAN, WS($1));
}
 |      BYTE
{
    $$ = YH_BUILD(primType)(BYTE, WS($1));
}
 |      CHAR
{
    $$ = YH_BUILD(primType)(CHAR, WS($1));
}
 |      DOUBLE
{
    $$ = YH_BUILD(primType)(DOUBLE, WS($1));
}
 |      FLOAT
{
    $$ = YH_BUILD(primType)(FLOAT, WS($1));
}
 |      INT
{
    $$ = YH_BUILD(primType)(INT, WS($1));
}
 |      LONG
{
    $$ = YH_BUILD(primType)(LONG, WS($1));
}
 |      SHORT
{
    $$ = YH_BUILD(primType)(SHORT, WS($1));
}
 ;

referenceType:
        name
{
    $$ = $1;
}
 |      arrayType
{
    $$ = $1;
}
 ;

arrayType:
        primitiveType bracketsList
{
    $$ = YH_BUILD(arrayType)(YC(type,$1), YC(bracketsList,$2));
}
 |      name bracketsList
{
    $$ = YH_BUILD(arrayType)(YC(type,$1), YC(bracketsList,$2));
}
 ;

primary:
        primaryNoNewArray
{
    $$ = $1;
}
 |      arrayCreationExpression
{
    $$ = $1;
}
 ;

primaryNoNewArray:
        Number
{
    $$ = $1;
}
 |      Character
{
    $$ = $1;
}
 |      String
{
    $$ = $1;
}
 |      ENULL
{
    $$ = YH_BUILD(keywordLiteral)(ENULL, WS($1));
}
 |      ETRUE
{
    $$ = YH_BUILD(keywordLiteral)(ETRUE, WS($1));
}
 |      EFALSE
{
    $$ = YH_BUILD(keywordLiteral)(EFALSE, WS($1));
}
 |      JNULL
{
    $$ = YH_BUILD(keywordLiteral)(JNULL, WS($1));
}
 |      JTRUE
{
    $$ = YH_BUILD(keywordLiteral)(JTRUE, WS($1));
}
 |      JFALSE
{
    $$ = YH_BUILD(keywordLiteral)(JFALSE, WS($1));
}
 |      THIS
{
    $$ = YH_BUILD(keywordLiteral)(THIS, WS($1));
}
 |      '(' expression ')'
{
    $$ = YH_BUILD(subexpression)(WS($1), YC(expression,$2), WS($3));
}
 |      classInstanceCreationExpression
{
    $$ = $1;
}
 |      fieldAccess
{
    $$ = $1;
}
 |      methodInvocation
{
    $$ = $1;
}
 |      arrayAccess
{
    $$ = $1;
}
 ;

arrayCreationExpression:
        NEW primitiveType dimExprs optBracketsList optArrayInitializer
{
    $$ = YH_BUILD(arrayCreationExpression)(WS($1), YC(type,$2),
        YC(dimensionList, $3), YC(bracketsList,$4), YC(arrayInitializer,$5));
}
 |      NEW primitiveType bracketsList arrayInitializer
{
    $$ = YH_BUILD(arrayCreationExpression)(WS($1), YC(type,$2), NULL,
        YC(bracketsList,$3), YC(arrayInitializer,$4));
}
 |      NEW name dimExprs optBracketsList optArrayInitializer
{
    $$ = YH_BUILD(arrayCreationExpression)(WS($1), YC(type,$2),
        YC(dimensionList, $3), YC(bracketsList,$4), YC(arrayInitializer,$5));
}
 |      NEW name bracketsList arrayInitializer
{
    $$ = YH_BUILD(arrayCreationExpression)(WS($1), YC(type,$2), NULL,
        YC(bracketsList,$3), YC(arrayInitializer,$4));
}
 ;

optArrayInitializer:
        arrayInitializer
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

dimExprs:
        '[' expression ']'
{
    $$ = YH_BUILD(dimensionList)(YBUILD(dimension)(WS($1),
                                                   YC(expression,$2),
                                                   WS($3)),
                                 NULL);
}
 |      dimExprs '[' expression ']'
{
    $$ = YH_BUILD(dimensionList)(YBUILD(dimension)(WS($2),
                                                   YC(expression,$3),
                                                   WS($4)),
                                 YC(dimensionList,$1));
}
 ;

classInstanceCreationExpression:
        NEW name '(' optArgumentList ')' optEclassBody
{
    $$ = YH_BUILD(instanceCreationExpression)(WS($1), YC(name,$2), WS($3),
        YC(expressionSequence,$4), WS($5), YC(classBody,$6));
}
 ;

optEclassBody:
        eclassBody
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

fieldAccess:
        primary '.' Identifier
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)('.', WS($2)),
                         YC(expression,$3));
}
 |      SUPER '.' Identifier
{
    $$ = YH_BUILD(binop)(YC(expression,YBUILD(keywordLiteral)(SUPER, WS($1))),
                         YBUILD(operator)('.', WS($2)),
                         YC(expression,$3));
}
 |      name '.' CLASS
{
    $$ = YH_BUILD(classSelection)(YC(expression,$1), WS($2), WS($3));
}
 |      name '.' THIS
{
    $$ = YH_BUILD(binop)(YC(expression,$1),
                         YBUILD(operator)('.', WS($2)),
                         YC(expression,YBUILD(keywordLiteral)(THIS, WS($3))));
}
 ;

methodInvocation:
        name '(' optArgumentList ')'
{
    $$ = YH_BUILD(methodInvocation)(YC(expression,$1), WS($2),
                                    YC(expressionSequence,$3), WS($4));
}
 |      primary '.' Identifier '(' optArgumentList ')'
{
    YT(expression) *method = YC(expression,
        YBUILD(binop)(YC(expression,$1),
                      YBUILD(operator)('.', WS($2)),
                      YC(expression,$3)));
    $$ = YH_BUILD(methodInvocation)(method, WS($4),
                                    YC(expressionSequence,$5), WS($6));
}
 |      SUPER '.' Identifier '(' optArgumentList ')'
{
    YT(expression) *method = YC(expression,
        YBUILD(binop)(YC(expression,YBUILD(keywordLiteral)(SUPER, WS($1))),
                      YBUILD(operator)('.', WS($2)),
                      YC(expression,$3)));
    $$ = YH_BUILD(methodInvocation)(method, WS($4),
                                    YC(expressionSequence,$5), WS($6));
}
 ;

arrayAccess:
        name '[' expression ']'
{
    $$ = YH_BUILD(arrayAccess)(YC(expression,$1), WS($2), YC(expression,$3),
                               WS($4));
}
 |      primaryNoNewArray '[' expression ']'
{
    $$ = YH_BUILD(arrayAccess)(YC(expression,$1), WS($2), YC(expression,$3),
                               WS($4));
}
 ;

optArgumentList:
        argumentList
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

argumentList:
        expression
{
    $$ = YH_BUILD(expressionSequence)(NULL, NULL, YC(expression,$1));
}
 |      argumentList ',' expression
{
    $$ = YH_BUILD(expressionSequence)(YC(expressionSequence,$1), WS($2),
                                      YC(expression,$3));
}
 ;

optBracketsList:
        bracketsList
{
    $$ = $1;
}
 |
{
    $$ = 0;
}
 ;

%%

  void
pushDistContext(void)
{
    CurrentDistContext = YBUILD(distContext)(NULL, NULL, CurrentDistContext);
}

  void
popDistContext(void)
{
    YT(distContext) *newContext = CurrentDistContext;
    if (newContext->names || newContext->lowerContext) {
        CurrentDistContext = newContext->nextContext;
        if (CurrentDistContext) {
            newContext->nextContext = CurrentDistContext->lowerContext;
            CurrentDistContext->lowerContext = newContext;
        }
    }
}

/*
  yyerror -- needed so YACC's generated parser will link.
*/

  void
yyerror(char *s)
{
    yh_error("%s", s);
    YRESULT(0);
}
