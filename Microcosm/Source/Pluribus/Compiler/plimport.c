/*
  plimport.c -- External unit importation routines for Pluribus

  Chip Morningstar
  Electric Communities
  21-August-1996

  Copyright 1996 Electric Communities, all rights reserved.

*/

#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "y.tab.h"
#include "pl.h"
#include <ctype.h>

static FILE *findUnitFile(char *result, char *filePath);
static YT(attributeList) *internAttributes(char *prefix);
static int internAttributeType(char *unitName, YT(symbolDef) *name,
    long modifiers);
static bool internBoolean();
static char internChar();
static YT(deliverAttList) *internDeliverAtts(char *prefix, int count);
void internFunctionOrMethodDescriptor(char *prefix, bool withNames,
    bool withCode, bool isFunction, char *bufptr, YT(symbol) **name,
    YT(parameterDeclList) **params, YT(scopedRefList) **throws, long *modifiers,
    YT(type) **resultType, char **code);
static YT(functionList) *internFunctions(char *prefix);
static YT(implementsAtt) *internImplementsAtt(char *prefix);
static YT(implementsAttList) *internImplementsAttList(char *prefix);
static int internIngredientImpl(char *unitName, YT(symbolDef) *name,
    long modifiers);
static YT(ingredientRoleList) *internIngredientRoles(char *prefix);
static YT(ingredientList) *internIngredients(char *prefix);
static int internKind(char *unitName, YT(symbolDef) *name, long modifiers,
    int bindingType);
static YT(kindList) *internKindList(char *prefix, int bindingType);
static YT(mapAttList) *internMapAtts(char *prefix, int count);
static YT(method) *internMethod(char *prefix, bool withNames, bool withCode);
static YT(methodList) *internMethods(char *prefix, bool withNames,
    bool withCode);
static YT(neighborList) *internNeighbors(char *prefix);
static int internNumber();
static int internOneDef(char *unitName);
static int internPresenceImpl(char *unitName, YT(symbolDef) *name,
    long modifiers);
static YT(presenceRoleList) *internPresenceRoles(char *prefix);
static YT(presenceList) *internPresences(char *prefix,
    YT(presence) **primeptr);
static int internPresenceStructure(char *unitName, YT(symbolDef) *name,
   long modifiers);
static YT(protoDef) *internProtoDef(char *prefix);
static YT(protoDefList) *internProtoDefList(char *prefix);
static YT(stateBundle) *internStateBundle(char *prefix);
static char *internString();
static YT(symbolRef) *internSymbolRef();
static bool internTag();
static YT(template) *internTemplate(char *prefix, char *implName, int numMaps,
                                    int init);
static YT(unit) *internUnit(char *unitName, FILE *fyle, char *filename,
    char *filePath, YT(symbolRef) *ref, long export);
static int internUnitImport(char *unitName, YT(symbol) *name, long modifiers);
static int internUnumImpl(char *unitName, YT(symbolDef) *name, long modifiers);
static int internUnumStructure(char *unitName, YT(symbolDef) *name,
   long modifiers);
static YT(variable) *internVariable(char *prefix);
static YT(variableList) *internVariables(char *prefix);
static void nextAttribute();
static void nextUnitLine();
static YT(type) *parseTypeSignature(char *prefix, char **bufptr,
                                    int *dimensions);
static bool recursiveSymbolRefPathname(char *path, char *unit,
                       YT(symbolRef) *ref);
static YT(symbolRef) *scanSymbolRef(char *ref);
static YT(symbolDef) *symbolDefFromSymbolRef(YT(symbolRef) *ref);
static bool symbolRefPathname(char *path, char *unit, YT(symbolRef) *ref);


static FILE *UnitFyle = NULL;
static char *UnitFilename = NULL;
static char LineBuf[BUFLEN];
static char LineTag = '\0';
static char AttBuf[BUFLEN];
static char AttTag = '\0';
static int LineNumber = 0;

enum {
    INTERN_OK = 0,
    INTERR_BIND_FAILURE,
    INTERR_FORMAT,
    INTERR_IMPORT,
    INTERR_INTERNAL
};

  YT(anyBinding) *
activateUnitImport(YT(anyBinding) *binding, long export)
{
    char filename[BUFLEN];
    YT(importBinding) *importBinding = YC(importBinding,binding);
    FILE *fyle;

    if (Verbose)
        printf("<activating %s>\n", importBinding->filePath);
    if (fyle = findUnitFile(filename, importBinding->filePath))
        return(YC(anyBinding,internUnit(SDNAME(importBinding), fyle, filename,
            importBinding->filePath, YC(symbolRef,importBinding->name),
            export)));
    else
        return(NULL);
}

  static FILE *
findUnitFile(char *result, char *filePath)
{
    YT(stringList) *unitPath = UnitPath;
    FILE *fyle;

    if (Verbose)
        printf("<import of %s>\n", filePath);
    while (unitPath) {
        sprintf(result, "%s/%s.unitd", unitPath->string, filePath);
        if (fyle = fopen(result, "r")) {
            if (Verbose)
                printf("<import finds file %s>\n", result);
            return(fyle);
        }
        unitPath = unitPath->next;
    }
    return(NULL);
}

  YT(unit) *
importExternalUnit(YT(symbolRef) *ref, long export)
{
    char path[BUFLEN], unit[BUFLEN];
    char filename[BUFLEN];
    FILE *fyle;

    if (symbolRefPathname(path, unit, ref)) {
        fyle = findUnitFile(filename, path);
        if (fyle)
            return(internUnit(unit, fyle, filename, path, ref, export));
    }
    return(NULL);
}

  static YT(attributeList) *
internAttributes(char *prefix)
{
    char c;
    int count = atoi(AttBuf);
    YT(attributeList) *result = NULL;

    while (count--) {
        YT(symbolRef) *ref = internSymbolRef();
        YT(tagLit) *thisTag;
        YT(boolLit) *thisBool;
        YT(charLit) *thisChar;
        YT(numLit) *value;
        YT(stringLit) *thisString;
        YT(attributeType) *attributeType = lookupAttributeTypeRef("", ref);

        if (attributeType) {
            switch (typeOf(attributeType->type)) {
            case TagType:
              thisTag = YBUILD(tagLit)(internTag());
              result = YBUILD(attributeList)(
                YBUILD(attribute)(attributeType, YC(expr,thisTag),
                                  YBUILD(typedValue)
                                        (TV_TAG,YUC(value,thisTag->value))),
                                  result);
              break;
            case BOOLEAN:
              thisBool = YBUILD(boolLit)(internBoolean());
              result = YBUILD(attributeList)(
                YBUILD(attribute)(attributeType, YC(expr,thisBool),
                                  YBUILD(typedValue)
                                        (TV_BOOL,YUC(value,thisBool->value))),
                                  result);
              break;
            case CHAR:
        c = internChar();
        thisChar = YBUILD(charLit)(c);
        result = YBUILD(attributeList)(
                    YBUILD(attribute)(attributeType, YC(expr,thisChar),
                      YBUILD(typedValue)
                      (TV_CHAR,YUC(value,thisChar->value))),
            result);
        break;
            case LONG:
              value = YBUILD(numLit)(internNumber());
              result = YBUILD(attributeList)(
                YBUILD(attribute)(attributeType, YC(expr,value),
                                  YBUILD(typedValue)
                                        (TV_LONG,YUC(value,value->value))),
                                  result);
              break;
            case YTAG(stringType):
              thisString = YBUILD(stringLit)(internString());
              result = YBUILD(attributeList)(
                YBUILD(attribute)(attributeType, YC(expr,thisString),
                                  YBUILD(typedValue)
                                        (TV_STRING,YUC(value,
                                                     thisString->value))),
                                  result);
              break;
            default:
              yh_warning("%sUnknown attribute type %d for %s\n", prefix,
                         attributeType->type->dummy,
                         SDNAME(attributeType));
              break;
            }
        } else
        yh_error("%s: undefined attribute %s", prefix, ref);
    }
    return(result);
}

  static int
internAttributeType(char *unitName, YT(symbolDef) *name, long modifiers)
{
    YT(type) *type = NULL;
    int dummy;
    char prefix[MSGLEN];

    sprintf(prefix, "import unit %s: attribute %s: ", unitName, SNAME(name));
    nextAttribute();
    nextUnitLine();
    if (AttTag == 't') {
        char *buf = AttBuf;
        type = parseTypeSignature(prefix, &buf, &dummy);
        if (defineAttributeType(name, type, modifiers))
            return(INTERN_OK);
        else
            return(INTERR_BIND_FAILURE);
    }
    return(INTERR_FORMAT);
}

  static YT(presenceBehavior) *
internBehavior(char *prefix)
{
    YT(presenceBehavior) *result = NULL;
    YT(symbol) *behave = NULL;
    YT(symbolList) *behaviors = NULL;
    int count = atoi(AttBuf);

    while (count--) {
        nextUnitLine();
        behave = yh_handleSymbol(LineBuf);
        if (behave)
            behaviors = YBUILD(symbolList)(behave, behaviors);
    }
    if (behaviors) {
    result = YBUILD(presenceBehavior)(behaviors);
    }
    return(result);
}

  static bool
internBoolean()
{
    nextUnitLine();
    return(LineBuf[0] == 'T');
}

  static char
internChar()
{
    nextUnitLine();
    return(LineBuf[0]);
}

  static YT(deliverAttList) *
internDeliverAtts(char *prefix, int count)
{
    YT(deliverAttList) *result = NULL;
    YT(deliverAtt) *deliver;
    int scope;
    char from[BUFLEN];
    char toMethod[BUFLEN];

    while (count--) {
        nextUnitLine();
        sscanf(LineBuf, "%d %s %s", &scope, from, toMethod);
        scope = scope ? PRESENCE : UNUM;
    deliver = YBUILD(deliverAtt)(scope, yh_handleSymbol(from),
                toMethod[0] == '*' ? NULL : yh_handleSymbol(toMethod),
                getCurrentScope());
        result = YBUILD(deliverAttList)(deliver, result);
    }
    return(result);
}

  static YT(function) *
internFunction(char *prefix, bool withNames, bool withCode)
{
    YT(parameterDeclList) *params = NULL;
    YT(symbol) *name;
    YT(scopedRefList) *throws = NULL;
    YT(type) *resultType = NULL;
    bool isFunction = TRUE;
    char *bufptr, *code;
    long modifiers = 0;

    nextUnitLine();
    bufptr = LineBuf;
    internFunctionOrMethodDescriptor(prefix, withNames, withCode, isFunction,
                     bufptr, &name, &params, &throws,
                     &modifiers, &resultType, &code);
    return(YBUILD(function)(modifiers, resultType, name, params, throws,
                code));
}

  void
internFunctionOrMethodDescriptor(char *prefix, bool withNames,
                 bool withCode, bool isFunction, char *bufptr,
                 YT(symbol) **name,
                 YT(parameterDeclList) **params,
                 YT(scopedRefList) **throws, long *modifiers,
                 YT(type) **resultType, char **code)
{
    YT(typeSpec) *type;
    YT(parameterDecl) *decl;
    YT(symbol) *parmName;
    bool isArray = FALSE;
    char *allocSpaceForMethodCode;
    char methodCode[BUFLEN], pName[MSGLEN], throwName[MSGLEN];
    char newPrefix[MSGLEN];
    int numLines = 0, numThrows = 0, i = 0;

    while (*bufptr != '(') { ++bufptr; }
    *bufptr++ = '\0';
    *name = yh_handleSymbol(LineBuf);
    sprintf(newPrefix, "%s%s:\n  ", prefix, (*name)->name);
    while (*bufptr != ')') {
        type = YC(typeSpec,parseTypeSignature(newPrefix, &bufptr, &isArray));
        if (withNames) {
            i = 0;
            while (*bufptr != ',' && *bufptr != ')') {
                pName[i++] = *bufptr++;
            }
            pName[i] = '\0';
            parmName = yh_handleSymbol(pName);
            decl = YBUILD(parameterDecl)(type, parmName, isArray);
            if (*bufptr == ',') {
                bufptr++;
            }
        } else {
            decl = YBUILD(parameterDecl)(type, gensym(), isArray);
        }
        *params = YBUILD(parameterDeclList)(decl, *params);
    }
    bufptr += 2;
    sscanf(bufptr, " %d", &numThrows);
    if (numThrows) {
        while (*bufptr != ' ') { ++bufptr; }
        while (numThrows--) {
            sscanf(bufptr, "%s", throwName);
            *throws = YBUILD(scopedRefList)
                ((YT(scopedRef *))(yh_handleSymbol(throwName)), *throws);
            bufptr += strlen(throwName)+1;
        }
    }
    if (isFunction) {
        /* Move past the '0' if there were no throws */
        if (!numThrows) {
            while (*bufptr != ' ')
                ++bufptr;
        }
        sscanf(bufptr, " %d", modifiers);
        /* Move past the ' %d ' */
        while (*bufptr == ' ') { ++bufptr; }
        while (*bufptr != ' ') { ++bufptr; }
        while (*bufptr == ' ') { ++bufptr; }
        *resultType = parseTypeSignature(newPrefix, &bufptr, &isArray);
    }
    methodCode[0] = '\0';
    bufptr = methodCode;
    if (withCode) {
        nextUnitLine();
        sscanf(LineBuf, "%d", &numLines);
        for (i = 0; i < numLines; i++) {
            nextUnitLine();
            strcat(bufptr, LineBuf);
        }
    }
    if (strlen(bufptr)) {
        allocSpaceForMethodCode = STRDUP(bufptr);
        bufptr = allocSpaceForMethodCode;
    }
    *code = bufptr;
}

  static YT(functionList) *
internFunctions(char *prefix)
{
    YT(function) *function = NULL;
    YT(functionList) *result = NULL;
    int count = atoi(AttBuf);

    while (count--) {
        function = internFunction(prefix, FALSE, FALSE);
        if (function)
            result = YBUILD(functionList)(function, result);
    }
    return(result);
}

  static YT(implementsAtt) *
internImplementsAtt(char *prefix)
{
    YT(symbolRef) *name = internSymbolRef();
    return(YBUILD(implementsAtt)(name));
}

  static YT(implementsAttList) *
internImplementsAttList(char *prefix)
{
    int count = atoi(AttBuf);
    YT(implementsAttList) *result = NULL;
    while (count--) {
        YT(implementsAtt) *implements = internImplementsAtt(prefix);
        if (implements)
            result = YBUILD(implementsAttList)(implements, result);
    }
    return(result);
}

  static int
internIngredientImpl(char *unitName, YT(symbolDef) *name, long modifiers)
{
    YT(attributeList) *attributes = NULL;
    YT(functionList) *functions = NULL;
    YT(ingredientImpl) *impl;
    YT(kind) *kind = NULL;
    YT(methodList) *initBlocks = NULL;
    YT(methodList) *methods = NULL;
    YT(neighborList) *neighbors = NULL;
    YT(stateBundle) *stateBundle = NULL;
    YT(variableList) *vars = NULL;
    char prefix[MSGLEN];

    sprintf(prefix, "import unit %s: ingredientImpl %s: ", unitName,
            SNAME(name));

    for (;;) {
        nextAttribute();
        switch (AttTag) {
            case 'a':
                attributes = internAttributes(prefix);
                break;
            case 'k':
                kind = lookupKindRef(prefix, scanSymbolRef(AttBuf), FALSE);
        if (!kind)
            yh_error("%s: kind %s not defined", prefix, AttBuf);
                break;
            case 'n':
                neighbors = internNeighbors(prefix);
                break;
            case 's':
                stateBundle = internStateBundle(prefix);
                break;
            case 'v':
                vars = internVariables(prefix);
                break;
            case 'f':
                functions = internFunctions(prefix);
                break;
            case 'I':
                initBlocks = internMethods(prefix, FALSE, FALSE);
                break;
            case 'm':
                methods = internMethods(prefix, FALSE, FALSE);
                break;
            case 'z':
                internImplementsAttList(prefix);
                break;
            case '\0':
                impl = YBUILD(ingredientImpl)(name, NULL, attributes, kind,
                    neighbors, stateBundle, vars, functions, initBlocks,
                    methods, NULL, NULL, NULL);
                if (defineIngredientImpl(name, impl, modifiers)) {
                    return(INTERN_OK);
                }
                else {
                    return(INTERR_BIND_FAILURE);
                }
            default:
                return(INTERR_FORMAT);
        }
    }
}

  static YT(ingredientRole) *
internIngredientRole(char *prefix)
{
    YT(ingredientRole) *result = NULL;
    YT(template) *template = NULL;
    YT(symbolList) *roleSymbols = NULL;
    char *bufptr;
    char newPrefix[MSGLEN];
    char roleName[BUFLEN];
    char implName[BUFLEN];
    int numRoles = 0, numMaps = 0, init = 0;

    nextUnitLine();
    bufptr = LineBuf;
    sscanf(bufptr, "%d", &numRoles);
    bufptr++;
    while (*bufptr != ' ') bufptr++;
    while (numRoles--) {
        sscanf(bufptr, "%s", roleName);
        roleSymbols = YBUILD(symbolList)(yh_handleSymbol(roleName),
                                         roleSymbols);
        bufptr += strlen(roleName)+1;
    }
    sprintf(newPrefix,"%s\n  role %s: ", prefix, roleName); 
    sscanf(bufptr, "%s %d %d", implName, &numMaps, &init);
    template = internTemplate(newPrefix, implName, numMaps, init);
    result = YBUILD(ingredientRole)(roleSymbols, template);
    return(result);
}

  static YT(ingredientRoleList) *
internIngredientRoles(char *prefix)
{
    int count = atoi(AttBuf);
    YT(ingredientRoleList) *result = NULL;
    while (count--) {
        YT(ingredientRole) *role = internIngredientRole(prefix);
        if (role)
            result = YBUILD(ingredientRoleList)(role, result);
    }
    return(result);
}

  static YT(ingredient) *
internIngredient(char *prefix)
{
    YT(symbolRef) *kindRef;
    YT(deliverAttList) *deliverAtts = NULL;
    YT(kind) *kind = NULL;
    YT(ingredient) *result = NULL;
    char roleName[BUFLEN];
    char kindName[BUFLEN];
    char newPrefix[MSGLEN];
    int count = 0;

    nextUnitLine();
    sscanf(LineBuf, "%s %s %d", roleName, kindName, &count);
    kindRef = scanSymbolRef(kindName);
    sprintf(newPrefix,"%s\n  role %s: ", prefix, roleName); 
    kind = lookupKindRef(newPrefix, kindRef, FALSE);
    if (!kind)
    yh_error("%s: kind %s not defined", newPrefix, kindName);
    deliverAtts = internDeliverAtts(prefix, count);
    if (kind) {
        result = YBUILD(ingredient)(yh_handleSymbol(roleName), kind,
                                    deliverAtts);
    }
    return(result);
}

  static YT(ingredientList) *
internIngredients(char *prefix)
{
    int count = atoi(AttBuf);
    YT(ingredientList) *result = NULL;
    YT(ingredient) *ingredient;

    while (count--) {
        ingredient = internIngredient(prefix);
        if (ingredient) {
            result = YBUILD(ingredientList)(ingredient, result);
        }
    }
    return(result);
}

  static int
internKind(char *unitName, YT(symbolDef) *name, long modifiers,
           int bindingType)
{
    YT(attributeList) *attributes = NULL;
    YT(protoDefList) *protos = NULL;
    YT(implementsAttList) *implements = NULL;
    YT(kindList) *extends = NULL;
    YT(kind) *kind = NULL;
    char prefix[MSGLEN];

    sprintf(prefix, "import unit %s: kind %s: ", unitName, SNAME(name));

    for (;;) {
        nextAttribute();
        switch (AttTag) {
            case 'p':
                protos = internProtoDefList(prefix);
                break;
            case 'e':
                extends = internKindList(prefix, bindingType);
                break;
            case 'i':
                implements = internImplementsAttList(prefix);
                break;
            case 'a':
                attributes = internAttributes(prefix);
                break;
            case '\0':
                switch (bindingType) {
                case BIND_KIND:
                    kind = defineKind(name, attributes, protos,
                                      implements, extends,
                                      modifiers, bindingType);
                    break;
                }
                if (kind)
                    return(INTERN_OK);
                else
                    return(INTERR_BIND_FAILURE);
            default:
                return(INTERR_FORMAT);
        }
    }
}

  static YT(kindList) *
internKindList(char *prefix, int bindingType)
{
    int count = atoi(AttBuf);
    YT(kindList) *result = NULL;

    while (count--) {
        YT(symbolRef) *ref = internSymbolRef();
        YT(kind) *kind;

        switch (bindingType) {
            case BIND_KIND:
                kind = lookupKindRef(prefix, ref, FALSE);
        if (!kind)
            yh_error("%s: extending kind %s not defined", prefix,
                 ref->name->name);
                break;
            default:
                yh_error("illegal binding type %d in kind list for %s",
                         bindingType, SNAME(ref));
                break;
        }
        if (kind)
            result = YBUILD(kindList)(kind, result);
    }
    return(result);
}

  static YT(makeAtt) *
internMakeAtt(char *prefix)
{
    YT(symbolList) *roleSymbols = NULL;
    char *bufptr;
    char roleName[BUFLEN], ingrName[BUFLEN], dummy[BUFLEN];
    int count = 0;

    sscanf(LineBuf, "%s %d %s\n", dummy, &count, roleName);
    nextUnitLine();
    bufptr = LineBuf;
    while (count--) {
        sscanf(bufptr, "%s", ingrName);
        roleSymbols = YBUILD(symbolList)(yh_handleSymbol(ingrName),
                                         roleSymbols);
        bufptr += strlen(roleName)+1;
    }
    return(YBUILD(makeAtt)(yh_handleSymbol(roleName), roleSymbols));
}

  static YT(mapAttList) *
internMapAtts(char *prefix, int count)
{
    int scope;
    char from[BUFLEN];
    char to[BUFLEN];
    YT(mapAtt) *att;
    YT(mapAttList) *result = NULL;

    while (count--) {
        nextUnitLine();
        sscanf(LineBuf, "%d %s %s", &scope, from, to);
        scope = scope ? NEIGHBOR : PRESENCE;
        if (strcmp(to, "null") == 0)
            att = YBUILD(mapAtt)(scope, yh_handleSymbol(from), NULL);
        else
            att = YBUILD(mapAtt)(scope, yh_handleSymbol(from),
                                 yh_handleSymbol(to));
        result = YBUILD(mapAttList)(att, result);
    }
    return(result);
}

  static YT(method) *
internMethod(char *prefix, bool withNames, bool withCode)
{
    YT(parameterDeclList) *params = NULL;
    YT(symbol) *name = NULL;
    YT(scopedRefList) *throws = NULL;
    YT(type) *resultType = NULL;
    bool isFunction = FALSE;
    char *bufptr, *code;
    long modifiers = 0;

    nextUnitLine();
    bufptr = LineBuf;
    internFunctionOrMethodDescriptor(prefix, withNames, withCode, isFunction,
                     bufptr, &name, &params, &throws,
                     &modifiers, &resultType, &code);
    return(YBUILD(method)(name, params, throws, code));
}

  static YT(methodList) *
internMethods(char *prefix, bool withNames, bool withCode)
{
    YT(method) *method = NULL;
    YT(methodList) *result = NULL;
    int count = atoi(AttBuf);

    while (count--) {
        method = internMethod(prefix, withNames, withCode);
        if (method)
            result = YBUILD(methodList)(method, result);
    }
    return(result);
}

  static YT(neighborList) *
internNeighbors(char *prefix)
{
    int count = atoi(AttBuf);
    YT(neighborList) *result = NULL;
    char neighborName[BUFLEN];
    char kindName[BUFLEN];
    char newPrefix[MSGLEN];
    YT(kind) *kind;
    int plural;
    int presence;

    while (count--) {
        YT(neighbor) *neighbor;
        nextUnitLine();
        sscanf(LineBuf, "%s %s %d %d", neighborName, kindName, &plural,
               &presence);
        sprintf(newPrefix,"%s\n  neighbor %s: ", prefix, neighborName); 
        kind = lookupKindRef(newPrefix, scanSymbolRef(kindName), FALSE);
    if (!kind)
        yh_error("%s: kind %s not defined", newPrefix, kindName);
        neighbor = YBUILD(neighbor)(yh_handleSymbol(neighborName), kind,
                                    plural, presence);
        result = YBUILD(neighborList)(neighbor, result);
    }
    return(result);
}

  static int
internNumber()
{
    nextUnitLine();
    return(atoi(LineBuf));
}

  static int
internOneDef(char *unitName)
{
    long modifiers;
    int result = INTERN_OK;
    int bindingType;
    YT(symbol) *name;
    YT(symbolDef) *nameDef;
    char *check;

    if (LineTag != ':' && LineTag != '>')
        return(INTERR_FORMAT);

    if (LineBuf[0] == '+')
        modifiers = MOD_EXPORT;
    else if (LineBuf[0] == '-')
        modifiers = MOD_NONE;
    else
        return(INTERR_FORMAT);

    bindingType = bindingTypeFromTag(&LineBuf[1]);
    if (bindingType < 0)
        return(INTERR_FORMAT);

    if (LineBuf[3] != ' ')
        return(INTERR_FORMAT);

    check = &LineBuf[4];
    while (*check) {
        if (*check == '_' || *check == '$' ||
            ('a' <= *check && *check <= 'z') ||
            ('A' <= *check && *check <= 'Z') || 
            ('0' <= *check && *check <= '9'))
            ++check;
        else
            return(INTERR_FORMAT);
    }
    name = yh_handleSymbol(&LineBuf[4]);
    nameDef = defSymbol(name);

    switch (bindingType) {
        case BIND_TYPE:
            /* TODO */
            break;
        case BIND_ATTRIBUTE:
            result = internAttributeType(unitName, nameDef, modifiers);
            break;
        case BIND_INGREDIENT_IMPL:
            result = internIngredientImpl(unitName, nameDef, modifiers);/**/
            break;
        case BIND_KIND:
            result = internKind(unitName, nameDef, modifiers, bindingType);
            break;
        case BIND_PRESENCE_IMPL:
            result = internPresenceImpl(unitName, nameDef, modifiers);/**/
            break;
        case BIND_PRESENCE_STRUCTURE:
            result = internPresenceStructure(unitName, nameDef, modifiers);
            break;
        case BIND_UNIT:
            result = internUnitImport(unitName, name, modifiers);
            break;
        case BIND_UNUM_IMPL:
            result = internUnumImpl(unitName, nameDef, modifiers);/**/
            break;
        case BIND_UNUM_STRUCTURE:
            result = internUnumStructure(unitName, nameDef, modifiers);
            break;
        default:
            yh_error("illegal binding type %d", bindingType);
            result = INTERR_INTERNAL;
            break;
    }
    return(result);
}

  static YT(presenceCond) *
internPresenceCond(char *prefix)
{
    YT(expr) *expr = NULL;
    YT(presenceCond) *result = NULL;
    char *bufptr;
    char name[BUFLEN];

    nextUnitLine();
    bufptr = LineBuf;
    sscanf(LineBuf, "%s", name);
    bufptr += strlen(name);
    expr = scanExpr(&bufptr);
    result = YBUILD(presenceCond)(expr, yh_handleSymbol(name));

    return(result);
}

  static YT(presenceCondList) *
internPresenceCondList(char *prefix, int count)
{
    YT(presenceCond) *presenceCond = NULL;
    YT(presenceCondList) *result = NULL;

    while (count--) {
    presenceCond = internPresenceCond(prefix);
    result = YBUILD(presenceCondList)(presenceCond, result);
    }
    return(result);
}

  static int
internPresenceImpl(char *unitName, YT(symbolDef) *name, long modifiers)
{
    YT(attributeList) *attributes = NULL;
    YT(implementsAttList) *implements = NULL;
    YT(makeAtt) *makeAtt = NULL;
    YT(methodList) *initBlocks = NULL;
    YT(methodList) *primeInitBlocks = NULL;
    YT(methodList) *facetInitBlocks = NULL;
    YT(presenceBehavior) *behavior = NULL;
    YT(presenceStructure) *struc = NULL;
    YT(ingredientRoleList) *roles = NULL;
    YT(presenceImpl) *impl;
    char prefix[MSGLEN];

    sprintf(prefix, "import unit %s: presenceImpl %s: ", unitName,
            SNAME(name));

    for (;;) {
        nextAttribute();
        switch (AttTag) {
            case 'a':
                attributes = internAttributes(prefix);
                break;
            case 'P':
                primeInitBlocks = internMethods(prefix, TRUE, TRUE);
                break;
            case 'I':
                initBlocks = internMethods(prefix, TRUE, TRUE);
                break;
            case 's':
                struc = lookupPresenceStructureRef(prefix,
                                                   scanSymbolRef(AttBuf));
        if (!struc)
            yh_error("%s: structure %s not defined", prefix, AttBuf);
                break;
            case 'r':
                roles = internIngredientRoles(prefix);
                if (AttTag != '\0')
                    break;
            case 'm':
                makeAtt = internMakeAtt(prefix);
                if (AttTag != '\0')
                    break;
            case 'b':
                behavior = internBehavior(prefix);
                if (AttTag != '\0')
                    break;
            case 'z':
                implements = internImplementsAttList(prefix);
                break;
            case 'f':
                facetInitBlocks = internMethods(prefix, TRUE, TRUE);
                break;
            case '\0':
                impl = YBUILD(presenceImpl)(name, NULL, attributes, struc,
                              roles, makeAtt, initBlocks, primeInitBlocks, behavior,
                              NULL, implements, facetInitBlocks);
                if (definePresenceImpl(name, impl, modifiers))
                    return(INTERN_OK);
                else
                    return(INTERR_BIND_FAILURE);
            default:
                return(INTERR_FORMAT);
        }
    }
}

  static YT(presenceRoleList) *
internPresenceRoles(char *prefix)
{
    int count = atoi(AttBuf);
    YT(presenceRoleList) *result = NULL;
    char roleName[BUFLEN];
    char implName[BUFLEN];
    char previousImplName[BUFLEN];
    YT(presenceRole) *role;
    char newPrefix[MSGLEN];

    previousImplName[0] = '\0';
    while (count--) {
        nextUnitLine();
        sscanf(LineBuf, "%s %s", roleName, implName);
        sprintf(newPrefix,"%s\n  role %s: ", prefix, roleName); 
        if (strcmp(implName, previousImplName)) {
            YT(symbolList) *roleSymbols =
                YBUILD(symbolList)(yh_handleSymbol(roleName), NULL);
            YT(presenceImpl) *presImpl =
                lookupPresenceImplRef(newPrefix, scanSymbolRef(implName));
        if (!presImpl)
        yh_error("%s: presence impl %s not defined", newPrefix,
             implName);
            role = YBUILD(presenceRole)(roleSymbols, presImpl);
            result = YBUILD(presenceRoleList)(role, result);
            strcpy(previousImplName, implName);
        } else {
            role->presences = YBUILD(symbolList)(yh_handleSymbol(roleName),
                                                 role->presences);
        }
    }
    return(result);
}

  static YT(presenceList) *
internPresences(char *prefix, YT(presence) **primeptr)
{
    YT(presenceCondList) *conditionals = NULL;
    YT(presenceList) *result = NULL;
    YT(kind) *kind;
    YT(presence) *presence = NULL;
    YT(symbolRef) *kindRef;
    int count = atoi(AttBuf);
    int conds = 0;
    int prime = 0;
    char name[BUFLEN];
    char makes[BUFLEN];
    char kindName[BUFLEN];
    char newPrefix[MSGLEN];

    while (count--) {
        nextUnitLine();
        sscanf(LineBuf, "%s %s %s %d %d", name, kindName, makes, &prime,
               &conds);
        sprintf(newPrefix,"%s\n  role %s: ", prefix, name); 
        kindRef = scanSymbolRef(kindName);
        kind = lookupKindRef(newPrefix, kindRef, FALSE);
        if (kind) {
        if (conds > 0)
        conditionals = internPresenceCondList(prefix, conds);
            presence = YBUILD(presence)(yh_handleSymbol(name),
                makes[0] == '*' ? NULL : yh_handleSymbol(makes),
                conditionals, kind, prime);
            if (prime)
                *primeptr = presence;
        result = YBUILD(presenceList)(presence, result);
        } else
        yh_error("%s: kind %s not defined", newPrefix, kindName);
    }
    return(result);
}

  static int
internPresenceStructure(char *unitName, YT(symbolDef) *name, long modifiers)
{
    YT(attributeList) *attributes = NULL;
    YT(kind) *kind = NULL;
    YT(ingredientList) *ingredients = NULL;
    YT(presenceStructure) *struc;
    char prefix[MSGLEN];

    sprintf(prefix, "import unit %s: presenceStructure %s: ", unitName,
            SNAME(name));

    for (;;) {
        nextAttribute();
        switch (AttTag) {
            case 'a':
                attributes = internAttributes(prefix);
                break;
            case 'k':
                kind = lookupKindRef(prefix, scanSymbolRef(AttBuf), FALSE);
        if (!kind)
            yh_error("%s: kind %s not defined", prefix, AttBuf);
                break;
            case 'i':
                ingredients = internIngredients(prefix);
                break;
            case '\0':
                struc = YBUILD(presenceStructure)(name, NULL, attributes, kind,
                    ingredients);
                if (definePresenceStructure(name, struc, modifiers))
                    return(INTERN_OK);
                else
                    return(INTERR_BIND_FAILURE);
            default:
                return(INTERR_FORMAT);
        }
    }
}

  static YT(protoDef) *
internProtoDef(char *prefix)
{
    bool isFunction = FALSE, withCode = FALSE, withNames = FALSE;
    char *bufptr, *code;
    long modifiers = 0;
    YT(symbol) *name;
    YT(parameterDeclList) *params = NULL;
    YT(scopedRefList) *throws = NULL;
    YT(type) *resultType = NULL;

    nextUnitLine();
    bufptr = LineBuf;
    internFunctionOrMethodDescriptor(prefix, withNames, withCode, isFunction,
                     bufptr, &name, &params, &throws,
                     &modifiers, &resultType, &code);

    /*KSSHack
    bufptr = LineBuf;
    while (*bufptr != '(')
        ++bufptr;
    *bufptr++ = '\0';
    name = yh_handleSymbol(LineBuf);
    sprintf(newPrefix, "%s%s:\n  ", prefix, name->name);
    while (*bufptr != ')') {
        type = YC(typeSpec,parseTypeSignature(newPrefix, &bufptr, &isArray));
        decl = YBUILD(parameterDecl)(type, gensym(), isArray);
        params = YBUILD(parameterDeclList)(decl, params);
    }
    sscanf(bufptr, " %d", &numThrows);
    if (numThrows) {
        while (*bufptr != ' ') { ++bufptr; }
            sscanf(bufptr, "%s", throwName);
            *throws = YBUILD(scopedRefList)
                ((YT(scopedRef *))(yh_handleSymbol(throwName)), *throws);
            bufptr += strlen(throwName)+1;
        }
    }
    KSSHack*/
    return(YBUILD(protoDef)(name, params, throws));
}

  static YT(protoDefList) *
internProtoDefList(char *prefix)
{
    int count = atoi(AttBuf);
    YT(protoDefList) *result = NULL;

    while (count--) {
        YT(protoDef) *proto = internProtoDef(prefix);
        if (proto)
            result = YBUILD(protoDefList)(proto, result);
    }
    return(result);
}

  static YT(stateBundle) *
internStateBundle(char *prefix)
{
    char dummy[BUFLEN], packagename[BUFLEN], typename[BUFLEN], name[BUFLEN];
    YT(stateBundle) *result = NULL;

    sscanf(LineBuf, "%s %s %s %s\n", dummy, packagename, typename, name);
    result = YBUILD(stateBundle)(yh_handleSymbol(packagename),
                                 yh_handleSymbol(typename),
                                 yh_handleSymbol(name), NULL);
    return(result);
}

  static char *
internString() {
    char *string;
    nextUnitLine();
    string = STRDUP(LineBuf);
    return(string);
}

  static YT(symbolRef) *
internSymbolRef()
{
    nextUnitLine();
    return(scanSymbolRef(LineBuf));
}

  static bool
internTag()
{
    nextUnitLine();
    return(LineBuf[0] == 'T');
}

  static YT(template) *
internTemplate(char *prefix, char *implName, int numMaps, int init)
{
    YT(ingredientImpl) *impl = NULL;
    YT(mapAttList) *maps = NULL;
    YT(template) *template = NULL;

    impl = lookupIngredientImplRef(prefix,
                                   refSymbol(yh_handleSymbol(implName)));
    if (!impl)
    yh_error("%s: ingredient impl %s not defined", prefix, implName);
    maps = internMapAtts(prefix, numMaps);
    template = YBUILD(template)(NULL, impl, maps);

    return(template);
}

  static YT(unit) *
internUnit(char *unitName, FILE *fyle, char *filename, char *filePath,
           YT(symbolRef) *ref, long export)
{
    bool hitEqu = FALSE;
    int err = FALSE;
    int errLineNumber = 0;
    YT(scope) *scope = NULL;
    YT(symbolDef) *importName = symbolDefFromSymbolRef(ref);
    FILE *saveUnitFyle = UnitFyle;
    char *saveUnitFilename = UnitFilename;
    char saveLineTag = LineTag;
    char saveLineBuf[BUFLEN];
    strcpy(saveLineBuf, LineBuf);

    UnitFyle = fyle;
    UnitFilename = filename;

    if (export != ELEVATE)
        pushScope(importName);

    nextUnitLine();
    while (LineTag && !err) {
        errLineNumber = LineNumber;
        switch (LineTag) {
            case '=':
                if (hitEqu) {
                    err = INTERR_FORMAT;
                } else {
                    hitEqu = TRUE;
                    if (LineBuf[0] == '-' && export != NONE) {
                        yh_error("attempt to re-export non-export unit %s",
                                 pSymbolRef(ref));
                        export = NONE;
                    }
                }
                nextUnitLine();
                break;
            case '>':
            case ':':
                err = internOneDef(unitName);
                break;
            default:
                err = INTERR_FORMAT;
                break;
        }
    }
    fclose(fyle);
    UnitFyle = saveUnitFyle;
    UnitFilename = saveUnitFilename;
    LineTag = saveLineTag;
    strcpy(LineBuf, saveLineBuf);

    if (export != ELEVATE)
        scope = popScope();
    else
        scope = getCurrentScope();

    if (err == INTERR_FORMAT) {
        yh_error("corrupt unit file %s: bad import descriptor on line %d",
                 filename, errLineNumber);
        return(NULL);
    } else if (!hitEqu) {
        yh_error("corrupt unit file %s", filename);
        return(NULL);
    }

    return(defineUnit(importName, STRDUP(filePath), scope, export, TRUE, NULL,
                      TRUE));
}

  static int
internUnitImport(char *unitName, YT(symbol) *name, long modifiers)
{
    char prefix[MSGLEN];

    sprintf(prefix, "imported unit %s: ", name->name);
    nextAttribute();
    nextUnitLine();
    if (AttTag == 'f') {
        YT(symbolRef) *refName = refSymbol(name);
        YT(unit) *unit = lookupUnitRef(prefix, refName,
                                       modifiers & MOD_EXPORT);
        if (!unit)
            unit = importExternalUnit(refName,
                (modifiers & MOD_EXPORT) ? EXPORT : NONE);
        if (unit) {
            addImportedUnitToCurrentScope(unit);
            return(INTERN_OK);
        } else {
            return(INTERR_IMPORT);
        }
/*KSSHack        YT(importBinding) *binding =
            YBUILD(importBinding)(NULL, NULL, STRDUP(AttBuf));
        if (bindSymbol(name, BIND_UNIT, YC(anyBinding,binding), modifiers)) {
            if (activateUnitImport(YC(anyBinding,binding), modifiers))
                return(INTERN_OK);
            else
                return(INTERR_IMPORT);
        } else
            return(INTERR_BIND_FAILURE);
KSSHack*/
    }
    return(INTERR_FORMAT);
}

  static int
internUnumImpl(char *unitName, YT(symbolDef) *name, long modifiers)
{
    YT(attributeList) *attributes = NULL;
    YT(unumStructure) *struc = NULL;
    YT(presenceRoleList) *roles = NULL;
    YT(unumImpl) *impl;
    char prefix[MSGLEN];

    sprintf(prefix, "import unit %s: unumImpl %s: ", unitName,
            SNAME(name));
    for (;;) {
        nextAttribute();
        switch (AttTag) {
            case 'a':
                attributes = internAttributes(prefix);
                break;
            case 's':
                struc = lookupUnumStructureRef(prefix, scanSymbolRef(AttBuf));
        if (!struc)
            yh_error("%s: structure %s not defined", prefix, AttBuf);
                break;
            case 'r':
                roles = internPresenceRoles(prefix);
                break;
            case '\0':
                impl = YBUILD(unumImpl)(name, NULL, attributes, struc, roles);
                if (defineUnumImpl(name, impl, modifiers))
                    return(INTERN_OK);
                else
                    return(INTERR_BIND_FAILURE);
            default:
                return(INTERR_FORMAT);
        }
    }
}

  static int
internUnumStructure(char *unitName, YT(symbolDef) *name, long modifiers)
{
    YT(attributeList) *attributes = NULL;
    YT(kind) *kind = NULL;
    YT(presenceList) *presences = NULL;
    YT(presence) *prime = NULL;
    YT(unumStructure) *struc;
    char prefix[MSGLEN];

    sprintf(prefix, "import unit %s: unumStructure %s: ", unitName,
            SNAME(name));
    for (;;) {
        nextAttribute();
        switch (AttTag) {
            case 'a':
                attributes = internAttributes(prefix);
                break;
            case 'k':
                kind = lookupKindRef(prefix, scanSymbolRef(AttBuf), FALSE);
        if (!kind)
            yh_error("%s: kind %s not defined", prefix, AttBuf);
                break;
            case 'p':
                presences = internPresences(prefix, &prime);
                break;
            case '\0':
                struc = YBUILD(unumStructure)(name, NULL, attributes, kind,
                                              presences, prime->name);
                if (defineUnumStructure(name, struc, modifiers))
                    return(INTERN_OK);
                else
                    return(INTERR_BIND_FAILURE);
            default:
                return(INTERR_FORMAT);
        }
    }
}

  static YT(variable) *
internVariable(char *prefix)
{
    YT(type) *type = NULL;
    YT(variable) *result = NULL;
    bool isArray = FALSE;
    char *bufptr, typeSig[BUFLEN], name[BUFLEN];
    char newPrefix[MSGLEN];
    long modifiers = 0;

    /* Note that we do not import any assigned values */
    nextUnitLine();
    sscanf(LineBuf, " %s %s %d", typeSig, name, &modifiers);
    sprintf(newPrefix, "%s%s:\n  ", prefix, name);
    bufptr = typeSig;
    type = parseTypeSignature(newPrefix, &bufptr, &isArray);

    result = YBUILD(variable)(type, modifiers, yh_handleSymbol(name), isArray,
                              NULL);

    return(result);
}

  static YT(variableList) *
internVariables(char *prefix)
{
    int count = atoi(AttBuf);
    YT(variable) *var = NULL;
    YT(variableList) *result = NULL;

    while (count--) {
    var = internVariable(prefix);
    result = YBUILD(variableList)(var, result);
    }
    return(result);
}

  static void
nextAttribute()
{
    nextUnitLine();
    if (LineTag == '@') {
        AttTag = LineBuf[0];
        strcpy(AttBuf, LineBuf + 2);
    } else {
        AttTag = '\0';
        AttBuf[0] = '\0';
    }
}

  static void
nextUnitLine()
{
    char rawBuf[BUFLEN];

    while (fgets(rawBuf, BUFLEN, UnitFyle)) {
        int end = strlen(rawBuf) - 1;
        if (rawBuf[end] == '\n')
            rawBuf[end] = '\0';
        ++LineNumber;
        LineTag = rawBuf[0];
        if (LineTag != ';') { /* skip comments */
            strcpy(LineBuf, rawBuf + 1);
            return;
        }
    }
    LineTag = '\0';
}

  static YT(type) *
parseTypeSignature(char *prefix, char **bufptr, bool *isArray)
{
    bool isSubArray = FALSE;
    *isArray = FALSE;
    switch (*(*bufptr)++) {
        case 'B': return(YC(type,YBUILD(primType)(BYTE)));
        case 'C': return(YC(type,YBUILD(primType)(CHAR)));
        case 'D': return(YC(type,YBUILD(primType)(DOUBLE)));
        case 'F': return(YC(type,YBUILD(primType)(FLOAT)));
        case 'I': return(YC(type,YBUILD(primType)(INT)));
        case 'J': return(YC(type,YBUILD(primType)(LONG)));
        case 'S': return(YC(type,YBUILD(primType)(SHORT)));
        case 'T': return(YC(type,YBUILD(primType)(TagType))); //KSS?
        case 'Z': return(YC(type,YBUILD(primType)(BOOLEAN)));
        case '(': {
            *isArray = TRUE;
            return(parseTypeSignature(prefix, bufptr, &isSubArray));
        }
        case '[': {
            int dimension = 0;
            YT(type) *subType;

            if (isdigit(**bufptr)) {
                while (isdigit(**bufptr)) {
                    dimension = dimension * 10 + *(*bufptr)++ - '0';
                }
            } else {
                dimension = -1;
            }
            subType = parseTypeSignature(prefix, bufptr, isArray);
            if (subType) {
                return(YC(type,YBUILD(sequenceType)(subType, dimension)));
            } else {
                return(NULL);
            }
        }
        case 'L': {
            char *name = (*bufptr);
            while (**bufptr != ';')
                ++(*bufptr);
            *(*bufptr)++ = '\0';
            if (strcmp(name, "String") == 0)
                return(YC(type,YBUILD(stringType)(-1)));
            else
                return(YC(type,scanSymbolRef(name)));
        }
        // This is for KIND-ing
        case 'K': {
            char *name = (*bufptr);
            while (**bufptr != ';')
                ++(*bufptr);
            *(*bufptr)++ = '\0';
            return(YC(type,YBUILD(pluribusType)(scanSymbolRef(name),KIND)));
        }
        default:
            yh_error("%sbad type signature '%s'", prefix, --(*bufptr));
            return(NULL);
    }
}

  static bool
recursiveSymbolRefPathname(char *path, char *unit, YT(symbolRef) *ref)
{
    if (ref) {
    if (YTAG_TEST(ref,symbolRef)) {
        strcat(path, SNAME(ref));
        unit[0] = '\0';
        strcat(unit, SNAME(ref));
        return(TRUE);
    } else if (YTAG_TEST(ref,scopedRef)) {
        YT(scopedRef) *scopedRef = YC(scopedRef,ref);
        if (scopedRef->scope) {
        recursiveSymbolRefPathname(path, unit,
                       YC(symbolRef,scopedRef->scope));
        strcat(path, "/");
        }
        return(recursiveSymbolRefPathname(path, unit, scopedRef->ref));
    } else if (YTAG_TEST(ref,outerRef)) {
        YT(outerRef) *outerRef = YC(outerRef,ref);
        int i;
        for (i=0; i<outerRef->level; ++i)
        strcat(path, "../");
        return(recursiveSymbolRefPathname(path, unit, outerRef->ref));
    } else {
        return(FALSE);
    }
    }
    return(FALSE);
}

  static YT(symbolRef) *
scanSymbolRef(char *ref)
{
    return(refSymbol(yh_handleSymbol(ref)));
}

  static YT(symbolDef) *
symbolDefFromSymbolRef(YT(symbolRef) *ref)
{
    if (YTAG_TEST(ref,symbolRef)) {
        return(YC(symbolDef,ref));
    } else if (YTAG_TEST(ref,scopedRef)) {
        return(symbolDefFromSymbolRef(YC(scopedRef,ref)->ref));
    } else if (YTAG_TEST(ref,outerRef)) {
        return(symbolDefFromSymbolRef(YC(outerRef,ref)->ref));
    } else {
        yh_error("invalid symbol ref %s in symbolDefFromSymbolRef",
                 SNAME(ref));
        return(NULL);
    }
}

/* symbolRefPathname -- Fill 'path' with the pathname of the .unit file
                        containing the unit named by 'ref' */
  static bool
symbolRefPathname(char *path, char *unit, YT(symbolRef) *ref)
{
    path[0] = '\0';
    return(recursiveSymbolRefPathname(path, unit, ref));
}
