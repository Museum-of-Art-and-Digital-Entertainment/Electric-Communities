/*
  plscope.c -- Symbol and scope management for Pluribus.

  Chip Morningstar
  Electric Communities
  2-August-1996

  Copyright 1996 Electric Communities, all rights reserved.

*/

#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "y.tab.h"
#include "pl.h"

static YT(scope) *CurrentScope = NULL;
static YT(scope) *CurrentNamedScope = NULL;
static YT(nameSpace) *acquireNameSpaceInScope(int bindingType,
    YT(scope) *scope);
static YT(anyBinding) *bindSymbolDef(YT(symbolDef) *name, int bindingType,
    YT(anyBinding) *def, long modifiers, bool redefine);
static void dumpScope(YT(scope) *scope);
static YT(nameSpace) *lookupNameSpaceInScope(char *prefix, int bindingType,
    YT(scope) *scope);
static YT(anyBinding) *lookupRefInScope(char *prefix, YT(symbolRef) *ref,
    int bindingType, YT(scope) *scope, bool isImportedScope);
static YT(binding) *lookupRefBindingInScope(char *prefix, YT(symbolRef) *ref,
    int bindingType, YT(scope) *scope, bool isImportedScope);
static YT(binding) *lookupSymbolInNameSpace(char *prefix, YT(symbol) *name,
    YT(nameSpace) *nameSpace, bool isImportedScope);
static YT(binding) *lookupSymbolInScope(char *prefix, YT(symbol) *name,
    int bindingType, YT(scope) *scope, bool isImportedScope);
static bool matchSymbolDefToSymbolDef(YT(symbolDef) *name, YT(symbolDef) *def);
static bool matchSymbolToSymbolDef(YT(symbol) *name, YT(symbolDef) *def);

  static YT(nameSpace) *
acquireNameSpaceInScope(int bindingType, YT(scope) *scope)
{
    YT(nameSpace) *result;
    char prefix[MSGLEN];

    sprintf(prefix, "KSSZ acquireNameSpaceInScope: ");
    result = lookupNameSpaceInScope(prefix, bindingType, scope);
    if (result)
        return(result);
    result = YBUILD(nameSpace)(bindingType, NULL);
    scope->nameSpaces = YBUILD(nameSpaceList)(result, scope->nameSpaces);
    return(result);
}

  static YT(anyBinding) *
bindSymbolDef(YT(symbolDef) *name, int bindingType, YT(anyBinding) *def,
              long modifiers, bool redefine)
{
    YT(scope) *searchScope;

    if (modifiers & MOD_INTERNAL)
        searchScope = CurrentNamedScope;
    else
        searchScope = CurrentScope;

    if (searchScope) {
        YT(nameSpace) *nameSpace = acquireNameSpaceInScope(bindingType,
                                                           searchScope);
        YT(bindingList) *bindings = nameSpace->bindings;
        YT(binding) *newBinding;

        while (bindings) {
            if (matchSymbolDefToSymbolDef(name, bindings->binding->name)) {
                if (redefine) {
                    bindings->binding->value = def;
                    break;
                } else {
                    yh_error("multiply defined symbol %s",
                             pSymbolRef(YC(symbolRef,name)));
                    return(NULL);
                }
            }
            bindings = bindings->next;
        }
        if (!bindings) {
            bool isExport = (modifiers & MOD_EXPORT) != 0;
            newBinding = YBUILD(binding)(name, def, isExport);
            nameSpace->bindings =
                YBUILD(bindingList)(newBinding, nameSpace->bindings);
        }
        def->name = name;
        def->bindingType = bindingType;
        return(def);
    } else {
        return(NULL);
    }
}

  static void
dumpScope(YT(scope) *scope)
{
    YH_DUMP(scope,scope);
}

  static YT(nameSpace) *
lookupNameSpaceInScope(char *prefix, int bindingType, YT(scope) *scope)
{
    YT(nameSpaceList) *nameSpaces = scope->nameSpaces;

    while (nameSpaces) {
        if (nameSpaces->nameSpace->bindingType == bindingType)
            return(nameSpaces->nameSpace);
        nameSpaces = nameSpaces->next;
    }
    return(NULL);
}

  static YT(anyBinding) *
lookupRefInScope(char *prefix, YT(symbolRef) *ref, int bindingType,
                 YT(scope) *scope, bool isImportedScope)
{
    YT(binding) *binding = lookupRefBindingInScope(prefix, ref, bindingType,
                                                   scope, isImportedScope);
    if (binding)
        return(binding->value);
    else
        return(NULL);
}

  static YT(binding) *
lookupRefBindingInScope(char *prefix, YT(symbolRef) *ref, int bindingType,
                        YT(scope) *scope, bool isImportedScope)
{
    if (scope == NULL || ref == NULL)
        return(NULL);

    if (YTAG_TEST(ref,symbolRef)) {
        return(lookupSymbolInScope(prefix, ref->name, bindingType, scope,
                                   isImportedScope));
    } else if (YTAG_TEST(ref,scopedRef)) {
        YT(scopedRef) *scopedRef = YC(scopedRef,ref);
        YT(unit) *unit =
            YC(unit,lookupRefInScope(prefix, YC(symbolRef,scopedRef->scope),
                                     BIND_UNIT, scope, isImportedScope));
        if (unit)
            return(lookupRefBindingInScope(prefix, scopedRef->ref, bindingType,
                                           unit->scope, TRUE));
        return(NULL);
    } else if (YTAG_TEST(ref,outerRef)) {
        YT(outerRef) *outerRef = YC(outerRef,ref);
        int level = outerRef->level;
        if (isImportedScope) {
            yh_error("can't use outer scope of imported unit");
            return(NULL);
        }
        while (level && scope) {
            scope = scope->outer;
            --level;
        }
        return(lookupRefBindingInScope(prefix, outerRef->ref, bindingType,
                                       scope, isImportedScope));
    } else {
        yh_error("invalid symbol ref in lookupRefInScope");
        return(NULL);
    }
}

  static YT(binding) *
lookupSymbolInNameSpace(char *prefix, YT(symbol) *name,
                        YT(nameSpace) *nameSpace, bool isImportedScope)
{
    YT(bindingList) *bindings = nameSpace->bindings;

    while (bindings) {
        if (!isImportedScope || bindings->binding->isExport)
            if (matchSymbolToSymbolDef(name, bindings->binding->name))
                return(bindings->binding);
        bindings = bindings->next;
    }
    return(NULL);
}

  static YT(binding) *
lookupSymbolInScope(char *prefix, YT(symbol) *name, int bindingType,
                    YT(scope) *scope, bool isImportedScope)
{
    while (scope) {
        YT(scopeList) *imported = scope->imported;
        YT(nameSpace) *nameSpace = lookupNameSpaceInScope(prefix, bindingType,
                                                          scope);
        YT(binding) *result;

        if (nameSpace) {
            result = lookupSymbolInNameSpace(prefix, name, nameSpace,
                                             isImportedScope);
            if (result)
                return(result);
        }
        while (imported) {
            nameSpace = lookupNameSpaceInScope(prefix, bindingType,
                                               imported->scope);
            if (nameSpace) {
                result = lookupSymbolInNameSpace(prefix, name, nameSpace,
                                                 TRUE);
                if (result)
                    return(result);
            }
            imported = imported->next;
        }

        if (isImportedScope)
            return(NULL);
        scope = scope->outer;
    }
    return(NULL);
}

  static bool
matchSymbolDefToSymbolDef(YT(symbolDef) *name, YT(symbolDef) *def)
{
    if (YTAG_TEST(name,symbolRef))
        return(matchSymbolToSymbolDef(YC(symbolRef,name)->name, def));
    yh_error("bad symbol def %s", pSymbolRef(YC(symbolRef,name)));
    return(FALSE);
}

  static bool
matchSymbolToSymbolDef(YT(symbol) *name, YT(symbolDef) *def)
{
    if (YTAG_TEST(def,symbolRef))
        return(name == YC(symbolRef,def)->name);
    yh_error("bad symbol def %s", pSymbolRef(YC(symbolRef,def)));
    return(FALSE);
}

  YT(anyBinding) *
bindSymbol(YT(symbol) *name, int bindingType, YT(anyBinding) *def,
           long modifiers)
{
    return(bindSymbolDef(defSymbol(name), bindingType, def, modifiers, FALSE));
}

  YT(attributeType) *
defineAttributeType(YT(symbolDef) *name, YT(type) *type, long modifiers)
{
    YT(attributeType) *attType = YBUILD(attributeType)(NULL, NULL, type);
    return(YC(attributeType,bindSymbolDef(name, BIND_ATTRIBUTE,
        YC(anyBinding,attType), modifiers, FALSE)));
}

  YT(codeAtt) *
defineCodeAtt(YT(symbolDef) *name, YT(codeAtt) *codeAtt, long modifiers)
{
    long binding = 0;

    switch (codeAtt->type) {
    case CLASS: binding = BIND_CLASS; break;
    case INTERFACE: binding = BIND_INTERFACE; break;
    case ECLASS: binding = BIND_ECLASS; break;
    case EINTERFACE: binding = BIND_EINTERFACE; break;
    default: yh_error("Unknown code type %d'", codeAtt->type);
    }

    return(YC(codeAtt,
          bindSymbolDef(name, binding, YC(anyBinding,codeAtt), modifiers,
                FALSE)));
}

  YT(ingredientImpl) *
defineIngredientImpl(YT(symbolDef) *name, YT(ingredientImpl) *impl,
                     long modifiers)
{
    char prefix[MSGLEN];

    if (impl && impl->kind) {
    sprintf(prefix, "kind %s vs ingredient impl %s: ",
        SDNAME(impl->kind), SNAME(name));
    checkIngredientImpl(prefix, impl, impl->kind);
    return(YC(ingredientImpl,bindSymbolDef(name, BIND_INGREDIENT_IMPL,
            YC(anyBinding,impl), modifiers, FALSE)));
    } else
    return NULL;
}

  YT(kind) *
defineKind(YT(symbolDef) *name, YT(attributeList) *attributes,
           YT(protoDefList) *protos,
           YT(implementsAttList) *implements, YT(kindList) *extends,
           long modifiers, int what)
{
    YT(kind) *kind = YBUILD(kind)(name, NULL, attributes, protos,
                                  implements, extends);
    char prefix[MSGLEN];

    sprintf(prefix, "kind %s: ", SNAME(name));
    validateRequirements(prefix, kind->attributes);
    return(YC(kind,bindSymbolDef(name, what,
                                 YC(anyBinding,kind), modifiers, FALSE)));
}

  YT(presenceImpl) *
definePresenceImpl(YT(symbolDef) *name, YT(presenceImpl) *impl, long modifiers)
{
    char prefix[MSGLEN];

    sprintf(prefix, "presence impl %s: ", SNAME(name));
    checkPresenceImpl(prefix, impl, impl->structure);
    return(YC(presenceImpl,bindSymbolDef(name, BIND_PRESENCE_IMPL,
        YC(anyBinding,impl), modifiers, FALSE)));
}

  YT(presenceStructure) *
definePresenceStructure(YT(symbolDef) *name, YT(presenceStructure) *struc,
                        long modifiers)
{
    char prefix[MSGLEN];

    sprintf(prefix, "presenceStructure %s: ", SNAME(name));
    checkPresenceStructure(prefix, struc, struc->kind);
    return(YC(presenceStructure,bindSymbolDef(name, BIND_PRESENCE_STRUCTURE,
        YC(anyBinding,struc), modifiers, FALSE)));
}

  YT(defType) *
defineType(YT(symbol) *name, YT(arraySizeList) *dimensions, YT(type) *type,
           long modifiers)
{
    if (type) {
        YT(defType) *newType = YBUILD(defType)(NULL, NULL, dimensions, type);
        return(YC(defType,bindSymbol(name, BIND_TYPE, YC(anyBinding,newType),
                                     modifiers)));
    } else {
        return(NULL);
    }
}

  YT(unit) *
defineUnit(YT(symbolDef) *name, char *filePath, YT(scope) *scope,
    long export, bool isImported, YT(importAttList) *imports, bool redefine)
{
    bool isExport = (export != NONE);

    YT(unit) *unit = YBUILD(unit)(NULL, NULL, filePath, scope, export,
                                  isImported, imports);
    return(YC(unit,bindSymbolDef(name, BIND_UNIT, YC(anyBinding,unit),
                                 isExport ? MOD_EXPORT : MOD_NONE, redefine)));
}

  YT(unumImpl) *
defineUnumImpl(YT(symbolDef) *name, YT(unumImpl) *impl, long modifiers)
{
    char prefix[MSGLEN];

    sprintf(prefix, "unumImpl %s: ", SNAME(name));
    checkUnumImpl(prefix, impl, impl->structure);
    return(YC(unumImpl,bindSymbolDef(name, BIND_UNUM_IMPL, YC(anyBinding,impl),
                                     modifiers, FALSE)));
}

  YT(unumStructure) *
defineUnumStructure(YT(symbolDef) *name, YT(unumStructure) *struc,
                    long modifiers)
{
    char prefix[MSGLEN];

    sprintf(prefix, "unumStructure %s: ", SNAME(name));
    checkUnumStructure(prefix, struc, struc->kind);
    return(YC(unumStructure,bindSymbolDef(name, BIND_UNUM_STRUCTURE,
        YC(anyBinding,struc), modifiers, FALSE)));
}

  void
dumpCurrentScope()
{
    dumpScope(CurrentScope);
}

  bool
addImportedUnitToCurrentScope(YT(unit) *unit)
{
    if (unit->scope) {
        CurrentScope->imported = YBUILD(scopeList)(unit->scope,
                                                   CurrentScope->imported);
        return(FALSE);
    } else {
        return(TRUE);
    }
}

  YT(attributeType) *
lookupAttributeTypeRef(char *prefix, YT(symbolRef) *ref)
{
    YT(attributeType) *result =
        YC(attributeType,lookupRefInScope(prefix, ref, BIND_ATTRIBUTE,
                                          CurrentScope, FALSE));
    if (!result)
        yh_error("%sundefined attribute '%s'", prefix, pSymbolRef(ref));
    return(result);
}

  YT(ingredientImpl) *
lookupIngredientImplRef(char *prefix, YT(symbolRef) *ref)
{
    YT(ingredientImpl) *result =
        YC(ingredientImpl,lookupRefInScope(prefix, ref, BIND_INGREDIENT_IMPL,
                                           CurrentScope, FALSE));
    if (!result)
        yh_error("%sundefined ingredientImpl '%s'", prefix,
                 pSymbolRef(ref));
    return(result);
}

  YT(kind) *
lookupKindRef(char *prefix, YT(symbolRef) *ref, bool report)
{
    YT(kind) *result =
        YC(kind,lookupRefInScope(prefix, ref, BIND_KIND,
                                 CurrentScope, FALSE));
    if (!result && report)
        yh_error("%sundefined kind '%s'", prefix, pSymbolRef(ref));
    return(result);
}

  YT(presenceImpl) *
lookupPresenceImplRef(char *prefix, YT(symbolRef) *ref)
{
    YT(presenceImpl) *result =
        YC(presenceImpl,lookupRefInScope(prefix, ref, BIND_PRESENCE_IMPL,
                                         CurrentScope, FALSE));
    if (!result)
        yh_error("%sundefined presenceImpl '%s'", prefix,
                 pSymbolRef(ref));
    return(result);
}

  YT(presenceStructure) *
lookupPresenceStructureRef(char *prefix, YT(symbolRef) *ref)
{
    YT(presenceStructure) *result =
        YC(presenceStructure,lookupRefInScope(prefix, ref,
                                              BIND_PRESENCE_STRUCTURE,
                                              CurrentScope, FALSE));
    if (!result)
        yh_error("%sundefined presenceStructure '%s'", prefix,
                 pSymbolRef(ref));
    return(result);
}

  YT(type) *
lookupTypeRef(char *prefix, YT(symbolRef) *ref)
{
    YT(type) *result =
        YC(type,lookupRefInScope(prefix, ref, BIND_TYPE, CurrentScope, FALSE));
    if (!result) {
        if (WarnAboutUndefinedTypes)
            yh_warning("%sundefined unit '%s'", prefix, pSymbolRef(ref));
        result =  YC(type,YBUILD(undefinedType)(ref));
    }
    return(result);
}

  YT(unit) *
lookupUnitRef(char *prefix, YT(symbolRef) *ref, long export)
{
    YT(binding) *binding =
        lookupRefBindingInScope(prefix, ref, BIND_UNIT, CurrentScope, FALSE);
    if (binding && YTAG_TEST(binding->value,importBinding))
        binding->value = activateUnitImport(binding->value, export);
    if (binding && binding->value)
        return(YC(unit,binding->value));
    else
        return(NULL);
}

  YT(unumImpl) *
lookupUnumImplRef(char *prefix, YT(symbolRef) *ref)
{
    YT(unumImpl) *result =
        YC(unumImpl,lookupRefInScope(prefix, ref, BIND_UNUM_IMPL, CurrentScope,
                                     FALSE));
    if (!result)
        yh_error("%sundefined unumImpl '%s'", prefix, pSymbolRef(ref));
    return(result);
}

  YT(unumStructure) *
lookupUnumStructureRef(char *prefix, YT(symbolRef) *ref)
{
    YT(unumStructure) *result =
        YC(unumStructure,lookupRefInScope(prefix, ref, BIND_UNUM_STRUCTURE,
                                          CurrentScope, FALSE));
    if (!result)
        yh_error("%sundefined unumStructure '%s'", prefix, pSymbolRef(ref));
    return(result);
}

  YT(scope) *
popScope()
{
    YT(scope) *result = CurrentScope;
    CurrentScope = CurrentScope->outer;
    if (result == CurrentNamedScope) {
        do {
            CurrentNamedScope = CurrentNamedScope->outer;
        } while (CurrentNamedScope && CurrentNamedScope->name == NULL);
    }
    return(result);
}

  YT(scope) *
getCurrentScope()
{
    return(CurrentScope);
}

  YT(scope) *
setCurrentScope(YT(scope) *newScope)
{
    YT(scope) *oldScope = CurrentScope;
    CurrentScope = newScope;
    return(oldScope);
}

  void
pushScope(YT(symbolDef) *name)
{
    YT(scope) *newScope = YBUILD(scope)(CurrentScope, NULL, NULL, name);
    CurrentScope = newScope;
    if (name)
        CurrentNamedScope = newScope;
}
