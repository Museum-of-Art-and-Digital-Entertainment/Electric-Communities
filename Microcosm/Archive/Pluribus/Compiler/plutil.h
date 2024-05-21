#ifndef PLUTIL_H_
#define PLUTIL_H_

#include "yh_struct.h"

int bindingTypeFromTag(char *tag);
char *bindingTypeToTag(int bindingType);
bool boolFromString(char *token);
int checkTypes(YT(typedValue) *lval, int op, YT(typedValue) *rval, char **msg);
YT(symbolDef) *defSymbol(YT(symbol) *symbol);
bool equal(int typeCode, int attrType);
void generateReadFuncCall(YT(type) *type);
YT(info) *genInfo(void);
YT(symbol) *gensym(void);
bool goodTypeCode(int typeCode);
YT(info) *info(void);
bool isBoolean(char *token);
bool isChar(char *token);
bool isExport(YT(genericDef) *def);
bool isGenerated(char *name);
bool isInternal(YT(genericDef) *def);
bool isNumber(char *token);
bool isOperator(char *token);
bool isScopedVariable(char *token);
bool isString(char *token);
bool isUnaryOperator(char *token);
bool isVariable(char *token);
YT(symbol) *makeSym(char *name);
char *mangleName(char *name, long mangleType);
int numCharIn(char c, char *string);
int numProtosInKind(YT(kind) *kind);
long opFromString(char *token);
char *opToString(int op);
char *pSymbolRef(YT(symbolRef) *ref);
YT(symbolRef) *refSymbol(YT(symbol) *symbol);
YT(scopedRef) *scopedRefFromString(char *token);
void setExport(YT(genericDef) *def);
void setInternal(YT(genericDef) *def);
char *stringFromExprList(char *prefix, YT(exprList) *exprs,
			 YT(ingredientRoleList) *roles);
char *stringFromParameterList(YT(parameterDeclList) *params);
void symbolRefString(char *buf, YT(symbolRef) *ref);
char *typeCodeToString(YT(typedValue) val);
YT(typeSpec) *typeSpecFromTypeCode(YT(typedValue) *val);
bool typeEqual(YT(typedValue) val, YT(parameterDecl) *param);
long typeOf(YT(type) *type);
char *typeSpecToString(YT(typeSpec) *typeSpec);
char *typeToString(int attrType);
char *typedValueToString(YT(typedValue) val);
char *whatKind(int what);
char *writeFuncName(YT(type) *type);

#endif
