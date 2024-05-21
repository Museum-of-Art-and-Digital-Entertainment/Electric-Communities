/*
  pl.h -- Types and function protos for Pluribus.

  Chip Morningstar
  Electric Communities
  21-August-1996

  Copyright 1996 Electric Communities, all rights reserved.

*/

#include "yh_struct.h"

extern bool ExpectCodeBlock;
extern bool ExpectHexBlock;
extern bool ExpectInitialization;

extern bool DumpIntermediateStructures;
extern bool WarnAboutUndefinedTypes;
extern bool Verbose;
extern YT(stringList) *UnitPath;
extern char *VersionString;
extern YT(symbolRef) *UnitPackage;
extern YT(symbol) *initSym;

extern char *UnumEinterfaceName;
extern char *PresenceEinterfaceName;
extern char *PresenceHostEinterfaceName;

enum typeOfExpressionEnum {
    ATTR_EXP, REQ_EXP, INIT_EXP, MAKE_EXP, COND_EXP
};

typedef enum typeOfExpressionEnum exprTypeEnum;

enum formOfExpressionEnum {
    PREFIX, INFIX, POSTFIX
};

typedef enum formOfExpressionEnum exprFormEnum;

/* plbackack.c */
void backack(void);

/* plcode.c */
void generateCode(char *source, YT(ingredientRoleList) *roles);
void generateJavaCode(YT(codeAtt) *codeAtt);
void generateThrowsList(YT(scopedRefList) *throws);

/* plcheck.c */
bool checkIngredientImpl(char *prefix, YT(ingredientImpl) *impl,
                         YT(kind) *kind);
bool checkIngredientImplRequirements(char *prefix, YT(ingredientImpl) *impl,
                            YT(kind) *kind);
bool checkExtendRequirements(char *prefix, YT(kindList) *kinds,
                             YT(attributeList) *attrs);
bool checkPresenceImpl(char *prefix, YT(presenceImpl) *presImpl,
                       YT(presenceStructure) *presStruc);
bool checkPresenceImplLinks(char *prefix, YT(presenceImpl) *impl);
bool checkPresenceStructure(char *prefix, YT(presenceStructure) *presStruc,
                            YT(kind) *presKind);
bool checkParameterList(char *prefix, YT(parameterDeclList) *params);
bool checkTemplate(char *prefix, YT(template) *template,
                   YT(ingredientImpl) *impl);
bool checkUnumImpl(char *prefix, YT(unumImpl) *unumImpl,
                   YT(unumStructure) *unumStruc);
bool checkUnumStructure(char *prefix, YT(unumStructure) *unumStruc,
                        YT(kind) *unumKind);
bool compareExprsToParams(char *prefix, char *firstName, YT(exprList) *exprs,
              char *secondName, YT(parameterDeclList) *params,
              exprTypeEnum exprType, YT(presenceImpl) *presImpl,
              bool reportErrors);
bool compareParameters(char *prefix, YT(symbol) *firstName,
                       YT(parameterDeclList) *first, YT(symbol) *secondName,
                       YT(parameterDeclList) *second, bool report);
bool validateAttributes(char *prefix, YT(attributeList) *attributes);
bool validateRequirements(char *prefix, YT(attributeList) *requirements);

/* plexpr.c */
YT(parameterDecl) *findParameter(YT(symbol) *name,
    YT(parameterDeclList) *initParams);
YT(parameterDecl) *findParameterInMethods(YT(symbol) *name,
                      YT(methodList) *methods);
YT(expr) *scanExpr(char **line);
char *stringFromExpr(char *prefix, exprFormEnum exprForm,
    exprTypeEnum exprType, YT(expr) *expr, YT(presenceImpl) *impl,
    bool specialUnaries, bool reportErrors);
YT(scopedRefList) *varsFromExpr(char *prefix, YT(expr) *expr,
    bool reportErrors);

/* plfileio.c */
bool popOutput(void);
bool pushOutput(char *filename, char *saveFile);
void pprintf(char *format, ...);
void pprintln(char *format, ...);
void pputs(char *str);
void pputch(char c);

/* plfind.c */
YT(attribute) *findAttributeByName(YT(symbol) *name,
                                   YT(attributeList) *attributes);
YT(deliverAtt) *findDeliver(YT(symbol) *source, long scope,
                            YT(ingredientList) *ingredients,
                            YT(ingredient) **ingredient);
YT(function) *findFunctionInImpl(YT(symbol) *name, YT(ingredientImpl) *impl);
YT(implementsAtt) *findImplements(char *implName,
                                  YT(implementsAttList) *implList);
YT(implementsAtt) *findImplementsAtt(YT(implementsAtt) *impl,
                                     YT(implementsAttList) *implList);
YT(ingredient) *findIngredient(YT(symbol) *roleName,
                               YT(ingredientList) *ingredients);
YT(ingredientRole) *findIngredientRole(YT(symbol) *roleName,
                       YT(ingredientRoleList) *roles);
YT(method) *findInit(YT(method) *init, YT(presenceImpl) *impl);
YT(kind) *findKindInKind(YT(symbol) *name, YT(kind) *kind);
YT(symbol) *findMappedNeighbor(YT(template) *template, YT(neighbor) *neighbor);
YT(mapAtt) *findMapping(long mapType, YT(symbol) *name,
                        YT(template) *template);
YT(ingredient)*findMessageDestination(YT(presenceStructure) *presStruc,
                                      YT(protoDef) *proto, int scope,
                                      YT(symbol) **ingrRole,
                                      YT(symbol) **message);
YT(method) *findMethodInImpl(YT(symbol) *name, YT(ingredientImpl) *impl);
YT(parameterDecl) *findParameter(YT(symbol) *name,
                 YT(parameterDeclList) *params);
YT(presenceRole) *findPresenceRole(YT(symbol) *presence, YT(unumImpl) *impl);
YT(presence) *findPrimePresence(YT(unumStructure) *struc);
YT(protoDef) *findProtoInKind(YT(symbol) *name, YT(kind) *kind);
YT(function) *findPublicFunction(YT(symbol) *name,
                 YT(functionList) *functions);
YT(symbol) *findSymbol(YT(symbol) *symbol, YT(symbolList) *symbols);
YT(variable) *findVariable(YT(symbol) *name, YT(variableList) *vars);
YT(protoDef) *locateProto(YT(symbol) *name, YT(ingredientList) *ingredients);

/* plgrind.c */
YT(unit) *grindInput(YT(unitDef) *unitDef, char *inputFileName);
YT(typedValue) evalExpr(char *prefix, YT(expr) *expr,
    YT(attributeList) *attributes, YT(methodList) *inits,
    YT(ingredientRoleList) *roles, exprTypeEnum exprType, bool reportErrors,
    bool *anyAttributes, bool *anyOperators);
char *stringExpr(YT(expr) *expr, YT(attributeList) *attributes);

/* plimport.c */
YT(anyBinding) *activateUnitImport(YT(anyBinding) *importBinding, long export);
YT(unit) *importExternalUnit(YT(symbolRef) *ref, long export);

/* plingr.c */
void generateIngredientImpl(YT(ingredientImpl) *impl);

/* pllex.c */
int yylex(void);

/* plmangle.c */
char *iiJavaName(char *name);
char *iiCodeName(char *name);
char *kindClassName(char *name);
char *presenceRouterJavaName(char *name);
char *presenceRouterName(char *name);
char *unumImplName(char *name);
char *unumRouterJavaName(char *name);
char *unumRouterName(char *name);

/* plout.c */
char *boolstr(bool flag);
char *comma(void *thing);
void ekeepClose();
void ekeepOpen();
char *pBaseJavaName(YT(anyBinding) *binding);
char *pJavaName(YT(anyBinding) *binding);
void expandScopeName(YT(scope) *scope, char *buf, char *delimiter);
void generateAttributes(YT(attributeList) *attributes);
void generateOutput(YT(unit) *unit, char *inputFileName,
    char *outputBaseDirName, char *saveFile);
void generateCallParameters(YT(parameterDeclList) *params, char *extraParams);
void generateParameterList(YT(parameterDeclList) *params, char *extraParams);
void generateParameterTypeList(YT(parameterDeclList) *params,
                               char *extraParams);
void generateTypeSpec(YT(type) *type);
void generateTypeSignature(YT(type) *type, int dimensions);
char *namestr(YT(symbol) *symbol);
char *rtExceptionEnvParam(bool withType);

/* plpres.c */
void generateTemplate(char *name, YT(template) *template);
void generatePresenceImpl(YT(presenceImpl) *impl);
void generatePresenceStructure(YT(presenceStructure) *struc);

/* plroute.c */
char *pUrName(YT(unumImpl) *impl, YT(symbol) *roleName);
char *pPrName(YT(unumImpl) *impl, YT(symbol) *roleName);
YT(template) *findTemplate(YT(presenceImpl) *presImpl, YT(symbol) *roleName);
void generatePresenceRouter(YT(unumImpl) *impl, YT(symbol) *roleName,
    YT(presenceImpl) *presImpl);
void generateUnumRouter(YT(unumImpl) *impl, YT(symbol) *roleName,
    YT(presenceImpl) *presImpl);
YT(presence) *findPresence(YT(unumStructure) *struc, YT(symbol) *roleName);

/* plscope.c */
YT(anyBinding) *bindSymbol(YT(symbol) *name, int bindingType,
    YT(anyBinding) *def, long modifiers);
YT(attributeType) *defineAttributeType(YT(symbolDef) *name, YT(type) *type,
    long modifiers);
YT(codeAtt) *defineCodeAtt(YT(symbolDef) *name, YT(codeAtt) *codeAtt,
    long modifiers);
YT(ingredientImpl) *defineIngredientImpl(YT(symbolDef) *name,
    YT(ingredientImpl) *impl, long modifiers);
YT(kind) *defineKind(YT(symbolDef) *name,
    YT(attributeList) *attributes, YT(protoDefList) *protos,
    YT(implementsAttList) *implements,
    YT(kindList) *extends, long modifiers, int what);
YT(presenceImpl) *definePresenceImpl(YT(symbolDef) *name,
    YT(presenceImpl) *impl, long modifiers);
YT(presenceStructure) *definePresenceStructure(YT(symbolDef) *name,
    YT(presenceStructure) *struc, long modifiers);
YT(defType) *defineType(YT(symbol) *name, YT(arraySizeList) *dimensions,
    YT(type) *type, long modifiers);
YT(unit) *defineUnit(YT(symbolDef) *name, char *filePath, YT(scope) *scope,
    long export, bool isImported, YT(importAttList) *imports, bool redefine);
YT(unumImpl) *defineUnumImpl(YT(symbolDef) *name, YT(unumImpl) *impl,
    long modifiers);
YT(unumStructure) *defineUnumStructure(YT(symbolDef) *name,
    YT(unumStructure) *struc, long modifiers);
void dumpCurrentScope();
bool addImportedUnitToCurrentScope(YT(unit) *unit);
YT(scope) *getCurrentScope();
YT(scope) *setCurrentScope(YT(scope) *newScope);
YT(attributeType) *lookupAttributeTypeRef(char *prefix, YT(symbolRef) *ref);
YT(ingredientImpl) *lookupIngredientImplRef(char *prefix, YT(symbolRef) *ref);
YT(kind) *lookupKindRef(char *prefix, YT(symbolRef) *ref, bool report);
YT(presenceImpl) *lookupPresenceImplRef(char *prefix, YT(symbolRef) *ref);
YT(presenceStructure) *lookupPresenceStructureRef(char *prefix,
                                                  YT(symbolRef) *ref);
YT(type) *lookupTypeRef(char *prefix, YT(symbolRef) *ref);
YT(unit) *lookupUnitRef(char *prefix, YT(symbolRef) *ref, long export);
YT(unumImpl) *lookupUnumImplRef(char *prefix, YT(symbolRef) *ref);
YT(unumStructure) *lookupUnumStructureRef(char *prefix, YT(symbolRef) *ref);
YT(scope) *popScope();
void pushScope();

/* plunit.c */
void generateUnitDescriptor(YT(unit) *unit, char *inputFileName);

/* plunum.c */
void generateUnumImpl(YT(unumImpl) *impl);
void generateUnumStructure(YT(unumStructure) *struc);

/* plutil.c */
int bindingTypeFromTag(char *tag);
char *bindingTypeToTag(int bindingType);
int checkTypes(YT(typedValue) *left, int op, YT(typedValue) *right,
           char **msg);
YT(symbolDef) *defSymbol(YT(symbol) *symbol);
bool equal(int typeCode, int attrType);
void generateReadFuncCall(YT(type) *type);
YT(info) *genInfo();
YT(symbol) *gensym();
bool goodTypeCode(int typeCode);
YT(info) *info();
bool isExport(YT(genericDef) *def);
bool isGenerated(char *name);
bool isInternal(YT(genericDef) *def);
bool isOperator(char *token);
bool isUnaryOperator(char *token);
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
char *typedValueToString (YT(typedValue) val);
YT(typeSpec) *typeSpecFromTypeCode(YT(typedValue) *val);
char *typeToString(int attrType);
char *whatKind(int what);
char *writeFuncName(YT(type) *type);

#define ERROR_CONTEXT(ptr) yh_FileName = ptr->filename; \
                           yh_LineNumber = ptr->lineNumber
#define NO_ERROR_CONTEXT() yh_FileName = NULL; yh_LineNumber = 0

#define MOD_NONE        0x0000
#define MOD_PUBLIC      0x0001
#define MOD_PROTECTED   0x0002
#define MOD_PRIVATE     0x0004
#define MOD_STATIC      0x0008
#define MOD_FINAL       0x0010
#define MOD_EXPORT      0x0020
#define MOD_INTERNAL    0x0040

/* Output generation macros */

#define PS(s)           pputs(s)
#define PC(c)           pputch(c)
#define P               pprintln
#define PP              pprintf
#define PNAME(x)        pJavaName(YC(anyBinding,x))
#define PBASENAME(x)    pBaseJavaName(YC(anyBinding,x))
