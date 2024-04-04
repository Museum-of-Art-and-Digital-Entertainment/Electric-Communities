/*
  Plgrind.c -- Turn Pluribus input into Pluribus internal data structures

  Chip Morningstar
  Electric Communities
  2-August-1996

  Copyright 1996 Electric Communities, all rights reserved.

*/

#include<stdlib.h>
#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "yh_dump.h"
#include "yh_walk.h"
#include "y.tab.h"
#include "pl.h"
#include "plutil.h"

//KSS Globals declared here because gdb can't find them inside of PCASEs
YT(attributeType) *GLBLattribute = NULL;
YT(initBlockAtt) *GLBLinitBlockAtt = NULL;
YT(codeAtt) *GLBLcodeAtt = NULL;
YT(method) *GLBLmethod = NULL;
YT(nestedElem) *GLBLnested = NULL;
YT(parameterDecl) *GLBLparameter = NULL;
YT(presenceAtt) *GLBLpresenceAtt = NULL;
YT(presence) *GLBLpresence = NULL;
YT(primeAtt) *GLBLprimeAtt = NULL;
YT(symbol) *GLBLsymbol = NULL;
YT(typedValue) *GLBLvalue = NULL, *GLBLvalue2 = NULL;

typedef struct {
    YT(attributeList) *attributes;
    YT(deliverAttList) *deliverAtts;
    YT(methodList) *facetInitBlocks;
    YT(importAttList) *imports;
    YT(ingredientImpl) *ingredientImpl;
    YT(implementsAttList) *implements;
    YT(kind) *kind;
    YT(kindList) *kinds;
    YT(methodList) *initBlocks;
    YT(makeAtt) *makeAtt;
    YT(memberList) *members;
    YT(methodList) *primeInitBlocks;
    YT(presenceImpl) *presenceImpl;
    YT(presenceStructure) *presenceStructure;
    YT(protoDefList) *protos;
    YT(stateBundle) *stateBundle;
    YT(variableList) *vars;
    YT(template) *template;
    YT(type) *type;
    YT(unumImpl) *unumImpl;
    YT(unumStructure) *unumStructure;
    bool inKind;
    bool inPresenceImpl;
    long modifiers;
    char *currentElement;
} t_pl_grindEnv;

#define ENV_DUMP(env) \
    YH_DUMP(attributeList, env->attributes); \
    YH_DUMP(deliverAttList, env->deliverAtts); \
    YH_DUMP(methodList, env->facetInitBlocks); \
    YH_DUMP(importAttList, env->imports); \
    YH_DUMP(ingredientImpl, env->ingredientImpl); \
    YH_DUMP(implementsAttList, env->implements); \
    YH_DUMP(kind, env->kind); \
    YH_DUMP(kindList, env->kinds); \
    YH_DUMP(makeAtt, env->makeAtt); \
    YH_DUMP(memberList, env->members); \
    YH_DUMP(methodList, env->initBlocks); \
    YH_DUMP(methodList, env->primeInitBlocks); \
    YH_DUMP(presenceImpl, env->presenceImpl); \
    YH_DUMP(presenceStructure, env->presenceStructure); \
    YH_DUMP(protoDefList, env->protos); \
    YH_DUMP(stateBundle, env->stateBundle); \
    YH_DUMP(template, env->template); \
    YH_DUMP(type, env->type); \
    YH_DUMP(unumImpl, env->unumImpl); \
    YH_DUMP(unumStructure, env->unumStructure); \
    printf( \
        "inKind=%s, inPresenceImpl=%s", \
    (env->inKind?"true":"false"),\
        (env->inPresenceImpl?"true":"false")); \
    printf( \
        "modifiers=%d, currentElement=%s", \
         env->modifiers, env->currentElement);

typedef struct {
    YT(attributeList) *knownAttributes;
    YT(methodList) *inits;
    YT(ingredientRoleList) *roles;
    YT(scopedRef) *scopedRef;
    exprTypeEnum exprType;
    bool reportErrors;
    bool anyAttributes;
    bool anyOperators;
    char *prefix;
} t_pl_evalEnv;

/* WARNING: The following macro uses wacky GCC extensions */
#define PUSH_ENV(var,val) \
    { t_pl_grindEnv save_##var; \
        save_##var.var = env->var; \
        env->var = val; {

#define PUSH_EVAL_ENV(var,val) \
    { t_pl_evalEnv save_##var; \
        save_##var.var = env->var; \
        env->var = val; {

#define POP_ENV(var) \
    } env->var = save_##var.var; } 


YA_FUNC_DEF(plGrind);
YA_FUNC_DEF(plEval);
YA_FUNC_DEF(plString);
YA_FUNC_DEF(plType);

  YT(unit) *
grindInput(YT(unitDef) *unit, char *inputFileName)
{
    t_pl_grindEnv env;
    env.attributes = NULL;
    env.deliverAtts = NULL;
    env.facetInitBlocks = NULL;
    env.imports = NULL;
    env.ingredientImpl = NULL;
    env.implements = NULL;
    env.kind = NULL;
    env.kinds = NULL;
    env.initBlocks = NULL;
    env.makeAtt = NULL;
    env.members = NULL;
    env.primeInitBlocks = NULL;
    env.presenceImpl = NULL;
    env.presenceStructure = NULL;
    env.protos = NULL;
    env.stateBundle = NULL;
    env.template = NULL;
    env.type = NULL;
    env.unumImpl = NULL;
    env.unumStructure = NULL;
    env.inKind = FALSE;
    env.inPresenceImpl = FALSE;
    env.modifiers = MOD_NONE;
    env.currentElement = NULL;
    return(YC(unit,YH_WALK(unitDef, unit, YA_FUNC(plGrind), &env)));
}

#define GR_WALKA(t,what) \
    YC(t,YH_WALK(elem, YC(elem,what), YA_FUNC(plGrind), env))
#define GR_WALKAL(t,what) \
    YC(t,YH_WALK(elemList, YC(elemList,what), YA_FUNC(plGrind), env))
#define GR_WALK(t,what)  GR_WALKA(t,arg->what)
#define GR_WALKL(t,what) GR_WALKAL(t,arg->what)

#define GWALKA(what)    GR_WALKA(elem,what)
#define GWALKAL(what)   GR_WALKAL(elemList,what)
#define GWALK(what)     GWALKA(arg->what)
#define GWALKL(what)    GWALKAL(arg->what)

#define PEVAL(what) \
    ((YT(typedValue)*) YH_WALK(expr, YC(expr,arg->what), YA_FUNC(plEval), env))
#define PSTRING(what) \
    ((char *)YH_WALK(expr, YC(expr,arg->what), YA_FUNC(plString), env))

#define TYPE_WALKA(t,what) \
    YC(t,YH_WALK(type, YC(type,what), YA_FUNC(plType), env))
#define TYPE_WALKAL(t,what) \
    YC(t,YH_WALK(typeList, YC(typeList,what), YA_FUNC(plType), env))
#define TYPE_WALK(t,what) TYPE_WALKA(t,arg->what)
#define TYPE_WALKL(t,what) TYPE_WALKAL(t,arg->what)

#define PCASE(type, block) YA_CASE(type, { block; YA_RETURN(arg); } )


YA_FUNC_START(plGrind, t_pl_grindEnv)
{
    PCASE(unitDef,{
        YT(scope) *scope;
        YT(unit) *unit = YBUILD(unit)(arg->name, NULL, NULL, NULL, NULL, NULL,
                                      NULL);
        char element[MSGLEN];
    if (env->currentElement)
        sprintf(element, "%s: %s", env->currentElement,
            arg->name->name->name);
    else
        sprintf(element, "%s", arg->name->name->name);

        PUSH_ENV(currentElement, element);

        pushScope(arg->name);
        GWALKL(elems);
/*        dumpCurrentScope();*/
        scope = popScope();

        POP_ENV(currentElement);
        unit = defineUnit(arg->name, NULL, scope,
                          isExport(YC(genericDef,arg)) ? EXPORT : NONE,
                          FALSE, env->imports, FALSE);
        addImportedUnitToCurrentScope(unit);
        YA_RETURN(unit);
    });
    PCASE(unitRef,{
        YT(unit) *unit;
        char prefix[MSGLEN];

        sprintf(prefix, "%s: ", env->currentElement);
        unit = lookupUnitRef(prefix, arg->name, arg->export);
        if (!unit)
            unit = importExternalUnit(arg->name, arg->export);
        if (unit)
            addImportedUnitToCurrentScope(unit);
        else
            yh_error("undefined unit %s", pSymbolRef(arg->name));
    });
    PCASE(nestedElem,{
    GLBLnested = arg;
        GWALKL(elems);
    });
    PCASE(attributeDef,{
        YT(type) *type = TYPE_WALK(type, type);
        if (type == NULL)
            type = YC(type,YBUILD(primType)(TagType));
        defineAttributeType(arg->name, type, arg->info->modifiers);
    });
    PCASE(attributeRef,{
        YT(attributeType) *attType;
        bool anyAttributes;
        bool anyOperators;
        char prefix[MSGLEN];

        sprintf(prefix,"%s: ", env->currentElement);
        attType = lookupAttributeTypeRef(prefix, arg->name);
        if (attType) {
            YT(attribute) *newAttribute;
            YT(typedValue) val;
            YT(expr) *expr = arg->expr;
            if (expr == NULL) {
                if (typeOf(attType->type) == BOOLEAN)
                    expr = YC(expr,YBUILD(boolLit)(TRUE));
                else
                    expr = YC(expr,YBUILD(tagLit)(TRUE));
            }
            newAttribute = YBUILD(attribute)(attType, expr, UND_VAR);
            val = evalExpr("", expr, env->attributes, NULL, NULL, ATTR_EXP,
               TRUE, &anyAttributes, &anyOperators);
            *(newAttribute->worth) = val;
            env->attributes = YBUILD(attributeList)(newAttribute,
                                                    env->attributes);
        }
    });
    PCASE(codeDef,{
    GLBLcodeAtt = YBUILD(codeAtt)(arg->name, NULL, arg->modifiers,
                      arg->type, arg->inherits,
                      arg->methodCode);
    defineCodeAtt(arg->name, GLBLcodeAtt, arg->info->modifiers);
    });
    PCASE(requireAtt,{
        env->attributes = YBUILD(attributeList)(YC(attribute,arg),
                                                env->attributes);
    });
    PCASE(protoDef,{
        env->protos = YBUILD(protoDefList)(arg, env->protos);
    });
    PCASE(parameterDecl,{
        YA_RETURN(TYPE_WALK(type, type));
    });
    PCASE(kindDef,{
        YT(kind) *newKind = YBUILD(kind)(arg->name, NULL, NULL, NULL,
                                         NULL, NULL);
        char element[MSGLEN];
        if (env->currentElement)
            sprintf(element, "%s: %s", env->currentElement,
                arg->name->name->name);
        else
            sprintf(element, "%s", arg->name->name->name);

        PUSH_ENV(attributes, NULL);
        PUSH_ENV(protos, NULL);
        PUSH_ENV(implements, NULL);
        PUSH_ENV(inKind, TRUE);
        PUSH_ENV(kinds, NULL);
        PUSH_ENV(currentElement, element);

        pushScope(NULL);
        GWALKL(elems);
        popScope();

        newKind = defineKind(arg->name, env->attributes, env->protos,
                             env->implements, env->kinds,
                             arg->info->modifiers, BIND_KIND);

        POP_ENV(currentElement);
        POP_ENV(kinds);
        POP_ENV(inKind);
        POP_ENV(implements);
        POP_ENV(protos);
        POP_ENV(attributes);
        if (DumpIntermediateStructures)
            YH_DUMP(kind, newKind);
    });
    PCASE(kindRef,{
        YT(kind) *kind;
        char prefix[MSGLEN];

        sprintf(prefix, "%s: ", env->currentElement);
        kind = lookupKindRef(prefix, arg->name, TRUE);
        if (kind) {
            if (env->inKind) {
                env->kinds = YBUILD(kindList)(kind, env->kinds);
            } else {
        if (env->kind)
            yh_error("%s: may have only one Kind (%s and %s)",
                 env->currentElement, SDNAME(env->kind),
                 SDNAME(kind));
        env->kind = kind;
        }
        }
    });
    PCASE(ingredientImplDef,{
        YT(ingredientImpl) *impl = YBUILD(ingredientImpl)(arg->name, NULL,
            NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
        char element[MSGLEN];
    if (env->currentElement)
        sprintf(element, "%s: %s", env->currentElement,
            arg->name->name->name);
    else
        sprintf(element, "%s", arg->name->name->name);

        PUSH_ENV(ingredientImpl, impl);
        PUSH_ENV(attributes, NULL);
        PUSH_ENV(kind, NULL);
        PUSH_ENV(stateBundle, NULL);
        PUSH_ENV(vars, NULL);
        PUSH_ENV(initBlocks, NULL);
        PUSH_ENV(implements, NULL);
        PUSH_ENV(inPresenceImpl, FALSE);
        PUSH_ENV(currentElement, element);

        pushScope(NULL);
        GWALKL(elems);
        popScope();

        if (env->kind == NULL) {
            yh_error("ingredientImpl %s: missing Kind",
                     pSymbolRef(YC(symbolRef,arg->name)));
        } else {
            impl->attributes = env->attributes;
            impl->stateBundle = env->stateBundle;
            impl->vars = env->vars;
            impl->kind = env->kind;
            impl->initBlocks = env->initBlocks;
            defineIngredientImpl(arg->name, impl,
                                 arg->info->modifiers);
        }

        POP_ENV(currentElement);
        POP_ENV(inPresenceImpl);
        POP_ENV(implements);
        POP_ENV(initBlocks);
        POP_ENV(vars);
        POP_ENV(stateBundle);
        POP_ENV(kind);
        POP_ENV(attributes);
        POP_ENV(ingredientImpl);
        if (DumpIntermediateStructures)
            YH_DUMP(ingredientImpl, impl);
    });
    PCASE(ingredientImplRef,{
        YT(ingredientImpl) *impl;
        char prefix[MSGLEN];
        
        sprintf(prefix,"%s: ", env->currentElement);
        impl = lookupIngredientImplRef(prefix, arg->name);
        if (impl) {
            if (env->ingredientImpl)
                yh_error("%s: template may only have one ingredientImpl",
                         env->currentElement);
            env->ingredientImpl = impl;
        }
    });
    PCASE(neighborAtt,{ 
        char element[MSGLEN];
    if (env->currentElement)
        sprintf(element, "%s: %s", env->currentElement,
            arg->name->name);
    else
        sprintf(element, "%s", arg->name->name);

        PUSH_ENV(kind, NULL);
        PUSH_ENV(currentElement, element);

        pushScope(NULL);
        GWALK(kind);
        popScope();
        POP_ENV(currentElement);

        if (env->kind) {
            YT(neighbor) *newNeighbor = YBUILD(neighbor)(
                arg->name,
                env->kind,
                arg->isPlural,
                arg->isPresence);
            env->ingredientImpl->neighbors = YBUILD(neighborList)(
                newNeighbor,
                env->ingredientImpl->neighbors);
        }

        POP_ENV(kind);
    });
    PCASE(stateBundleDef,{
        if (env->stateBundle) {
            yh_error("%s: only one state variable allowed per ingredient",
                     env->currentElement);
        } else {
            char buf[BUFLEN];
            YT(scopedRef) *scopedname = arg->typename;
            YT(symbol) *packagename = NULL;
            YT(symbol) *typename = NULL;

            buf[0] = '\0';
            if (YTAG_TEST(scopedname,scopedRef)) {
                typename = scopedname->ref->name;
                if (scopedname->scope) {
                    symbolRefString(buf, (YT(symbolRef) *)(scopedname->scope));
                }
            } else {
                symbolRefString(buf, UnitPackage);
                typename = YC(symbolRef,arg->typename)->name;
            }
            packagename = yh_handleSymbol(buf);
            env->stateBundle = YBUILD(stateBundle)(packagename, typename,
                                                   arg->name, arg->init);
        }
    });
    PCASE(variableDecl,{
        YT(type) *type = TYPE_WALK(type, type);
        if (type) {
            YT(variable) *newVariable = YBUILD(variable)(type, arg->modifiers,
                             arg->name, arg->dimensions, arg->init);
            env->vars = YBUILD(variableList)(newVariable, env->vars);
        }
    });
    PCASE(functionAtt,{
        YT(type) *resultType = TYPE_WALK(type, resultType);
    char prefix[MSGLEN];
    
    sprintf(prefix, "%s function %s: ",
        env->currentElement ? env->currentElement : "", SNAME(arg));
    checkParameterList(prefix, arg->params);
    
        if (resultType) {
            YT(function) *newFunction = YBUILD(function)(
                arg->modifiers,
                resultType,
                arg->name,
                arg->params,
                arg->throws,
                arg->methodCode);
            env->ingredientImpl->functions = YBUILD(functionList)(
                newFunction,
                env->ingredientImpl->functions);
        }
    });
    PCASE(emethodAtt,{
    YT(method) *newMethod = NULL;
    char prefix[MSGLEN];
    
    sprintf(prefix, "%s emethod %s: ",
        env->currentElement ? env->currentElement : "", SNAME(arg));
    checkParameterList(prefix, arg->params);

    newMethod = YBUILD(method)(arg->name, arg->params, arg->throws,
                               arg->methodCode);
    env->ingredientImpl->methods = YBUILD(methodList)(
                newMethod,
                env->ingredientImpl->methods);
    });
    PCASE(initBlockAtt,{
        char prefix[MSGLEN];

        sprintf(prefix, "%s%s init: ",
                env->currentElement ? env->currentElement : "",
                arg->initType == PRIME ?
                  "prime " :
                  arg->initType == FACET ? "facet " : "");
        checkParameterList(prefix, arg->params);
        
        if (arg->initType == PRIME) {
            if (!env->inPresenceImpl)
                yh_error("%s: prime init not allowed in ingredientImpl",
                         env->currentElement);
            else
                GLBLmethod = YBUILD(method)(initSym, arg->params, arg->throws,
                                            arg->methodCode);
            env->primeInitBlocks = YBUILD(methodList)(GLBLmethod,
                                                      env->primeInitBlocks);
        } else if (arg->initType == FACET) {
            if (!env->inPresenceImpl)
                yh_error("%s: facet init not allowed in ingredientImpl",
                         env->currentElement);
            else
                GLBLmethod = YBUILD(method)(initSym, arg->params, arg->throws,
                                            arg->methodCode);
            env->facetInitBlocks = YBUILD(methodList)(GLBLmethod,
                                                      env->facetInitBlocks);
        } else {
            GLBLmethod = YBUILD(method)(initSym, arg->params, arg->throws,
                                        arg->methodCode);
            env->initBlocks = YBUILD(methodList)(GLBLmethod,
                                                 env->initBlocks);
        }
    });
    PCASE(dataAtt,{
        YT(data) *newData = YBUILD(data)(arg->data);
        env->ingredientImpl->data = YBUILD(dataList)(
            newData,
            env->ingredientImpl->data);
    });
    PCASE(implementsAtt,{
        if (env->ingredientImpl)
            env->ingredientImpl->implements = YBUILD(implementsAttList)(
                                              arg,
                                              env->ingredientImpl->implements);
        else
            env->implements = YBUILD(implementsAttList)(arg, env->implements);
    });
    PCASE(importAtt,{
        env->imports = YBUILD(importAttList)(arg, env->imports);
    });
    PCASE(packageAtt,{
        if (UnitPackage)
            yh_error("%s: only one package declaration allowed per Pluribus file",
                     env->currentElement);
        UnitPackage = arg->name;
    });
    PCASE(presenceStructureDef,{
        YT(presenceStructure) *struc = YBUILD(presenceStructure)(arg->name,
            NULL, NULL, NULL, NULL, NULL, NULL);
        char element[MSGLEN];
    if (env->currentElement)
        sprintf(element, "%s: %s", env->currentElement,
            arg->name->name->name);
    else
        sprintf(element, "%s", arg->name->name->name);

        PUSH_ENV(presenceStructure, struc);
        PUSH_ENV(attributes, NULL);
        PUSH_ENV(kind, NULL);
        PUSH_ENV(currentElement, element);

        pushScope(NULL);
        GWALKL(elems);
        popScope();

        POP_ENV(currentElement);
        if (env->kind == NULL) {
            yh_error("presenceStructure %s: lacks Kind",
                     pSymbolRef(YC(symbolRef,arg->name)));
        } else {
            struc->attributes = env->attributes;
            struc->kind = env->kind;
            definePresenceStructure(arg->name, struc,
                                    arg->info->modifiers);
        }

        POP_ENV(kind);
        POP_ENV(attributes);
        POP_ENV(presenceStructure);
        if (DumpIntermediateStructures)
            YH_DUMP(presenceStructure, struc);
    });
    PCASE(presenceStructureRef,{
        YT(presenceStructure) *struc;
        char prefix[MSGLEN];

        sprintf(prefix, "%s: ", env->currentElement);
        struc = lookupPresenceStructureRef(prefix, arg->name);
        if (struc) {
            if (env->presenceStructure)
                yh_error("presenceImpl %s: may only have one presenceStructure",
                         env->currentElement);
            env->presenceStructure = struc;
        }
    });
    PCASE(ingredientAtt,{
        char element[MSGLEN];
    if (env->currentElement)
        sprintf(element, "%s: %s", env->currentElement,
            arg->name->name);
    else
        sprintf(element, "%s", arg->name->name);

        PUSH_ENV(kind, NULL);
        PUSH_ENV(deliverAtts, NULL);
        PUSH_ENV(currentElement, element);

        pushScope(NULL);
        GWALKL(elems);
        popScope();

        POP_ENV(currentElement);

        if (env->kind) {
            YT(ingredient) *newIngredient = YBUILD(ingredient)(
                arg->name, env->kind, env->deliverAtts);
            env->presenceStructure->ingredients = YBUILD(ingredientList)(
                newIngredient,
                env->presenceStructure->ingredients);
        }

        POP_ENV(deliverAtts);
        POP_ENV(kind);
    });
    PCASE(deliverAtt,{
        arg->contextScope = getCurrentScope();
        env->deliverAtts = YBUILD(deliverAttList)(arg, env->deliverAtts);
    });
    PCASE(presenceImplDef,{
        YT(presenceImpl) *impl = YBUILD(presenceImpl)(arg->name, NULL, NULL,
                                   NULL, NULL, NULL, NULL, NULL, NULL, NULL,
                                   NULL, NULL);
        char element[MSGLEN];
    if (env->currentElement)
        sprintf(element, "%s: %s", env->currentElement,
            arg->name->name->name);
    else
        sprintf(element, "%s", arg->name->name->name);

        PUSH_ENV(presenceImpl, impl);
        PUSH_ENV(attributes, NULL);
        PUSH_ENV(presenceStructure, NULL);
        PUSH_ENV(inPresenceImpl, TRUE);
        PUSH_ENV(makeAtt, NULL);
        PUSH_ENV(initBlocks, NULL);
        PUSH_ENV(primeInitBlocks, NULL);
        PUSH_ENV(implements, NULL);
        PUSH_ENV(facetInitBlocks, NULL);
        PUSH_ENV(currentElement, element);

        pushScope(NULL);
        GWALKL(elems);
        popScope();

        POP_ENV(currentElement);
        if (env->presenceStructure == NULL) {
            yh_error("presenceImpl %s: lacks presenceStructure",
                     pSymbolRef(YC(symbolRef,arg->name)));
        } else {
            impl->attributes = env->attributes;
            impl->structure = env->presenceStructure;
            impl->makeAtt = env->makeAtt;
            impl->initBlocks = env->initBlocks;
            impl->primeInitBlocks = env->primeInitBlocks;
            impl->implements = env->implements;
            impl->facetInitBlocks = env->facetInitBlocks;
            definePresenceImpl(arg->name, impl, arg->info->modifiers);
        }

        POP_ENV(facetInitBlocks);
        POP_ENV(implements);
        POP_ENV(primeInitBlocks);
        POP_ENV(initBlocks);
        POP_ENV(makeAtt);
        POP_ENV(inPresenceImpl);
        POP_ENV(presenceStructure);
        POP_ENV(attributes);
        POP_ENV(presenceImpl);
        if (DumpIntermediateStructures)
            YH_DUMP(presenceImpl, impl);
    });
    PCASE(presenceImplRef,{
        YT(presenceImpl) *impl;
        char prefix[MSGLEN];

        sprintf(prefix, "%s: ", env->currentElement);
        impl = lookupPresenceImplRef(prefix, arg->name);
        if (impl)
            env->presenceImpl = impl;
    });
    PCASE(templateAtt,{
        char element[MSGLEN];
    if (env->currentElement)
        sprintf(element, "%s: %s", env->currentElement,
            arg->ingredients ? arg->ingredients->symbol->name :
                               "template");
    else
        sprintf(element, "%s", arg->ingredients ?
            arg->ingredients->symbol->name : "template");
    
        PUSH_ENV(template, NULL);
    PUSH_ENV(currentElement, element);
    
        pushScope(NULL);
        GWALK(template);
        popScope();
    
    POP_ENV(currentElement);

        if (env->template) {
            YT(ingredientRole) *newRole = YBUILD(ingredientRole)(
                arg->ingredients,
                env->template);
            env->presenceImpl->roles = YBUILD(ingredientRoleList)(
                newRole,
                env->presenceImpl->roles);
        }

        POP_ENV(template);
    });
    PCASE(presenceBehavior,{
        if (env->presenceImpl == NULL) {
            yh_error("%s: can't set behavior outside of a presenceImpl",
                     env->currentElement);
        }
        if (env->presenceImpl->behavior)
            yh_error("presenceimpl %s: may only have one presenceBehavior declaration",
                     env->currentElement);
        env->presenceImpl->behavior = arg;
    });
    PCASE(templateDef,{
        YT(template) *template = YBUILD(template)(NULL, NULL, NULL,
                                                  NULL, NULL, NULL);
        PUSH_ENV(template, template);
        PUSH_ENV(attributes, NULL);
        PUSH_ENV(ingredientImpl, NULL);

        pushScope(NULL);
        GWALKL(elems);
        popScope();

        if (env->ingredientImpl == NULL) {
            yh_error("template: lacks ingredientImpl");
        } else {
            template->ingredientImpl = env->ingredientImpl;
        }

        POP_ENV(ingredientImpl);
        POP_ENV(attributes);
        POP_ENV(template);

        env->template = template;

        if (DumpIntermediateStructures)
            YH_DUMP(template, template);
    });
    PCASE(makeAtt,{
        if (env->makeAtt)
            yh_error("presence impl %s: may only have one make attribute",
                     env->currentElement);
        env->makeAtt = arg;
    });
    PCASE(mapAtt,{
        env->template->mapAtts = YBUILD(mapAttList)(
            arg, env->template->mapAtts);
    });
    PCASE(unumStructureDef,{
        YT(unumStructure) *struc = YBUILD(unumStructure)(arg->name, NULL, NULL,
                                                         NULL, NULL, NULL);
        char element[MSGLEN];
    if (env->currentElement)
        sprintf(element, "%s: %s", env->currentElement,
            arg->name->name->name);
    else
        sprintf(element, "%s", arg->name->name->name);

        PUSH_ENV(unumStructure, struc);
        PUSH_ENV(attributes, NULL);
        PUSH_ENV(kind, NULL);
        PUSH_ENV(currentElement, element);

        pushScope(NULL);
        GWALKL(elems);
        popScope();

        POP_ENV(currentElement);
        if (env->kind == NULL) {
            yh_error("unumStructure %s: lacks kind",
                     pSymbolRef(YC(symbolRef,arg->name)));
        } else if (struc->prime == NULL) {
            yh_error("unumStructure %s: lacks prime presence",
                     pSymbolRef(YC(symbolRef,arg->name)));
        } else {
            struc->attributes = env->attributes;
            struc->kind = env->kind;
            defineUnumStructure(arg->name, struc,
                                arg->info->modifiers);
        }

        POP_ENV(kind);
        POP_ENV(attributes);
        POP_ENV(unumStructure);
        if (DumpIntermediateStructures)
            YH_DUMP(unumStructure, struc);
    });
    PCASE(unumStructureRef,{
        YT(unumStructure) *struc;
        char prefix[MSGLEN];

        sprintf(prefix, "%s: ", env->currentElement);
        struc = lookupUnumStructureRef(prefix, arg->name);
        if (struc) {
            if (env->unumStructure)
                yh_error("unumImpl %s: may only have one unumStructure",
                         env->currentElement);
            env->unumStructure = struc;
        }
    });
    PCASE(presenceAtt,{
    GLBLpresenceAtt = arg;
        PUSH_ENV(kind, NULL);

        pushScope(NULL);
        GWALK(presence);
        popScope();

        if (env->kind) {
            YT(presence) *newPresence = YBUILD(presence)(
                arg->name,
                arg->makes,
                arg->conditionals,
                env->kind,
        FALSE);
        env->unumStructure->presences = YBUILD(presenceList)
            (newPresence, env->unumStructure->presences);
    }

        POP_ENV(kind);
    });
    PCASE(primeAtt,{
    YT(presence) *presence = NULL;
    GLBLprimeAtt = arg;
    if (env->unumStructure) {
        if (env->unumStructure->prime)
        yh_error("unumStructure %s: may only have one prime presence",
             env->currentElement);
        else {
        presence = findPresence(env->unumStructure, arg->name);
        if (presence) {
            env->unumStructure->prime = arg->name;
            presence->isPrime = TRUE;
        } else
            yh_error("unum structure %s: has no presence %s",
                 SDNAME(env->unumStructure), SNAME(arg));
        }
    } else
        yh_error("prime declarations %s not inside unu  structure",
             pSymbolRef(YC(symbolRef,arg->name)));
    });
    PCASE(unumImplDef,{
        YT(unumImpl) *impl = YBUILD(unumImpl)(arg->name, NULL, NULL, NULL,
                                              NULL);
        char element[MSGLEN];
    if (env->currentElement)
        sprintf(element, "%s: %s", env->currentElement,
            arg->name->name->name);
    else
        sprintf(element, "%s", arg->name->name->name);

        PUSH_ENV(unumImpl, impl);
        PUSH_ENV(attributes, NULL);
        PUSH_ENV(unumStructure, NULL);
        PUSH_ENV(currentElement, element);

        pushScope(NULL);
        GWALKL(elems);
        popScope();

        POP_ENV(currentElement);
        if (env->unumStructure == NULL) {
            yh_error("unumImpl %s: lacks unumStructure",
                     pSymbolRef(YC(symbolRef,arg->name)));
        } else {
            impl->attributes = env->attributes;
            impl->structure = env->unumStructure;
            defineUnumImpl(arg->name, impl, arg->info->modifiers);
        }

        POP_ENV(unumStructure);
        POP_ENV(attributes);
        POP_ENV(unumImpl);
        if (DumpIntermediateStructures)
            YH_DUMP(unumImpl, impl);
    });
    PCASE(unumImplRef,{
        YT(unumImpl) *impl;
        char prefix[MSGLEN];

        sprintf(prefix, "%s: ", env->currentElement);
        impl = lookupUnumImplRef(prefix, arg->name);
        if (impl)
            env->unumImpl = impl;
    });
    PCASE(presenceImplAtt,{
        PUSH_ENV(presenceImpl, NULL);

        pushScope(NULL);
        GWALK(presence);
        popScope();

        if (env->presenceImpl) {
            YT(presenceRole) *newRole = YBUILD(presenceRole)(
                arg->names,
                env->presenceImpl);
            env->unumImpl->roles = YBUILD(presenceRoleList)(
                newRole,
                env->unumImpl->roles);
            if (env->presenceImpl->unumImpl) {
                yh_error("unumImpl %s: presenceImpl %s has already been named",
                         SDNAME(env->unumImpl), SDNAME(env->presenceImpl));
            } else {
                env->presenceImpl->unumImpl = env->unumImpl;
            }
        }

        POP_ENV(presenceImpl);
    });
    PCASE(remoteDef,{
        /* TODO */
        yh_error("remote scopes not yet supported");
    });
    PCASE(publishDef,{
        /* TODO */
        yh_error("published scopes not yet supported");
    });
    PCASE(typeDef,{
        env->modifiers = arg->info->modifiers;
        GWALK(decl);
    });
    PCASE(typeDeclarator,{
        env->type = TYPE_WALK(type, type);
        GWALKL(declarators);
    });
    PCASE(simpleDeclarator,{
        defineType(arg->name, NULL, env->type, env->modifiers);
    });
    PCASE(arrayDeclarator,{
        defineType(arg->name, arg->dimensions, env->type,
                   MOD_NONE);
    });
    PCASE(structTypeDecl,{
        defineType(arg->name, NULL, TYPE_WALKA(type, arg),
                   arg->info->modifiers);
    });
    PCASE(unionTypeDecl,{
        defineType(arg->name, NULL, TYPE_WALKA(type, arg),
                   arg->info->modifiers);
    });
    PCASE(enumTypeDecl,{
        defineType(arg->name, NULL, TYPE_WALKA(type, arg),
                   arg->info->modifiers);
    });
}
YA_FUNC_END(plGrind)

YA_FUNC_START(plType, t_pl_grindEnv)
{
    PCASE(primType,{
        YA_RETURN(arg);
    });
    PCASE(structTypeDecl,{
        YT(structType) *result;
        PUSH_ENV(members, NULL);

        TYPE_WALKL(memberList, members);
        result = YBUILD(structType)(env->members);
        POP_ENV(members);
        YA_RETURN(result);
    });
    PCASE(memberDecl,{
        env->type = TYPE_WALK(type, type);
        env->members = YAPPEND(memberList)(env->members,
                                          TYPE_WALKL(memberList, declarators));
    });
    PCASE(simpleDeclarator,{
        YA_RETURN(YBUILD(member)(arg->name, NULL, env->type));
    });
    PCASE(arrayDeclarator,{
        YA_RETURN(YBUILD(member)(arg->name, arg->dimensions, env->type));
    });
    PCASE(unionTypeDecl,{
        YA_RETURN(YBUILD(unionType)(TYPE_WALK(type, switchType),
                                    TYPE_WALKL(switchCaseList, cases)));
    });
    PCASE(switchCaseDecl,{
        YA_RETURN(YBUILD(switchCase)(TYPE_WALKL(caseLabelList, caseLabels),
                                     TYPE_WALK(member, element)));
    });
    PCASE(caseLabelDecl,{
        if (arg->aCase) {
            //KSSX How do we get here?  Make sure this works correctly...
            YA_RETURN(YBUILD(caseLabel)((long)YUVP(PEVAL(aCase),TV_LONG)));
        } else {
            YA_RETURN(NULL);
        }
    });
    PCASE(elementSpec,{
        env->type = TYPE_WALK(type, type);
        YA_RETURN(TYPE_WALK(member, declarator));
    });
    PCASE(enumTypeDecl,{
        YA_RETURN(YBUILD(enumType)(arg->enumerators));
    });
    PCASE(sequenceTypeDecl,{
        YA_RETURN(YBUILD(sequenceType)(TYPE_WALK(type, type), arg->dimension));
    });
    PCASE(stringType,{
        YA_RETURN(arg);
    });
    PCASE(pluribusType,{
        char prefix[MSGLEN];

        sprintf(prefix, "%s: ", env->currentElement);
        YA_RETURN(YBUILD(pluribusType)(arg->type, arg->mangle));
    });
    PCASE(symbolRef,{
        char prefix[MSGLEN];

        sprintf(prefix, "%s: ", env->currentElement);
        YA_RETURN(lookupTypeRef(prefix, arg));
    });
    PCASE(scopedRef,{
        char prefix[MSGLEN];

        sprintf(prefix, "%s: ", env->currentElement);
        YA_RETURN(lookupTypeRef(prefix, YC(symbolRef,arg)));
    });
    PCASE(outerRef,{
        char prefix[MSGLEN];

        sprintf(prefix, "%s: ", env->currentElement);
        YA_RETURN(lookupTypeRef(prefix, YC(symbolRef,arg)));
    });
}
YA_FUNC_END(plType)

//KSSX Move this somewhere appropriate
  YT(typedValue) *
lookupAttributeValue(YT(attributeList) *attrs, char *name)
{
    YT(typedValue) *val = NULL;

    if (attrs) { 
        if (YTAG_TEST(attrs->attribute, attribute) &&
           (strcmp(name, SDNAME(attrs->attribute->type)) == 0)) {
            if (!val ||
                (attrs->attribute->worth->typeCode != TV_UND &&
                 attrs->attribute->worth->typeCode != TV_DEP)) {
                val = attrs->attribute->worth;
            }
       }
        while (attrs->next) {
            attrs = attrs->next;
            if (YTAG_TEST(attrs->attribute, attribute) &&
                (strcmp(name, SDNAME(attrs->attribute->type)) == 0)) {
                if (!val ||
                    (attrs->attribute->worth->typeCode != TV_UND &&
                     attrs->attribute->worth->typeCode != TV_DEP)) {
                    val = attrs->attribute->worth;
                }
            }
        }
    }
    return val;
}

//KSSX Move this somewhere appropriate
  YT(typedValue) *
buildTypedValue(YT(attributeType) *attr)
{
    YT(primType) *prim = NULL;
    YT(type) *type = attr->type;
    switch (YTAG_OF(type)) {
    case YTAG(primType):
        prim = YC(primType,type);
        switch (prim->type) {
        case TagType:
            return(YBUILD(typedValue)(TV_TAG,YUC(value,1)));
        case BOOLEAN:
            return(YBUILD(typedValue)(TV_BOOL,YUC(value,TRUE)));
        case CHAR:
            return(YBUILD(typedValue)(TV_CHAR,YUC(value,'#')));
        case LONG:
            return(YBUILD(typedValue)(TV_LONG,YUC(value,1)));
        default:
            yh_error("%s has invalid primitive type %d", SDNAME(attr),
                     prim->type);
            return(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
        }
    case YTAG(stringType):
        return(YBUILD(typedValue)(TV_STRING,YUC(value,"")));
    default:
        yh_error("%s has invalid type %d", SDNAME(attr), YTAG_OF(type));
        return(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
    }
}

  YT(typedValue) *
checkAttributeSymbolRef(YT(symbol) *name, YT(attributeList) *attrs,
            bool reportErrors, bool *anyAttributes)
{
    YT(typedValue) *val;
    val = lookupAttributeValue(attrs, name->name);
    if (val) {
    *anyAttributes = TRUE;
    if (val->typeCode != TV_UND) {
        if (val->typeCode != TV_DEP) {
        return(val);
        } else {
        if (reportErrors)
            yh_error("attribute '%s' depends on another", name->name);
        return(YBUILD(typedValue)(TV_DEP,YUC(value,-1)));
        }
    } else {
        if (reportErrors)
        yh_error("attribute '%s' previously undefined", name->name);
        return(YBUILD(typedValue)(TV_DEP,YUC(value,-1)));
    }
    } else {
    if (reportErrors)
        yh_error("symbol '%s' used in constant expression", name->name);
    return(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
    }
}

  YT(typedValue) *
checkInitSymbolRef(char *prefix, YT(symbol) *name,
           YT(methodList) *inits, bool reportErrors)
{
    YT(defType) *defType = NULL;
    YT(parameterDecl) *param = NULL;
    YT(pluribusType) *plType = NULL;
    YT(symbolRef) *symbol = NULL;

    param = findParameterInMethods(name, inits);

    if (param) {
    switch (YTAG_OF(param->type)) {
        case YTAG(defType):
        defType = YC(defType, param->type);
        return(YBUILD(typedValue)(TV_OTHER,
                      YUC(value,SNAME(defType))));
        case YTAG(enumType):
        return(YBUILD(typedValue)(TV_OTHER,YUC(value,"enum")));
        case YTAG(pluribusType):
        plType = YC(pluribusType, param->type);
        return(YBUILD(typedValue)(TV_OTHER,
                      YUC(value,SNAME(plType->type))));
        case YTAG(sequenceType):
        return(YBUILD(typedValue)(TV_OTHER,YUC(value,"sequence")));
        case YTAG(structType):
        return(YBUILD(typedValue)(TV_OTHER,YUC(value,"struct")));
        case YTAG(symbolRef):
        symbol = YC(symbolRef, param->type);
        return(YBUILD(typedValue)(TV_OTHER,
                      YUC(value,SNAME(symbol))));
        case YTAG(undefinedType):
        symbol = YC(symbolRef, YC(undefinedType,param->type)->type);
        return(YBUILD(typedValue)(TV_OTHER,
                      YUC(value,SNAME(symbol))));
        case YTAG(unionType):
        return(YBUILD(typedValue)(TV_OTHER,YUC(value,"union")));
        case YTAG(primType):
            switch (YC(primType,param->type)->type) {
        case TagType:
        return(YBUILD(typedValue)(TV_TAG,YUC(value,1)));
        case BOOLEAN:
        return(YBUILD(typedValue)(TV_BOOL,YUC(value,TRUE)));
        case BYTE:
        case CHAR:
        return(YBUILD(typedValue)(TV_CHAR,YUC(value,'#')));
        case DOUBLE:
        case FLOAT: //KSSHack
        return(YBUILD(typedValue)(TV_FLOAT,YUC(value,1)));
        case INT:
        case LONG:
        case SHORT:
        return(YBUILD(typedValue)(TV_LONG,YUC(value,1)));
        default:
        if (reportErrors)
            yh_error("invalid primType code %d in checkInitSymbolRef()",
                 YC(primType,param->type)->type);
        return(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
            }
        case YTAG(stringType):
        return(YBUILD(typedValue)(TV_STRING, YUC(value,"")));
        default:
        if (reportErrors)
        yh_error("invalid type code %d in checkInitSymbolRef()",
             YTAG_OF(param->type));
        return(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
    }
    } else {
    if (reportErrors)
        yh_error("%s%s is not an init() parameter", prefix, name->name);
    return(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
    }
}

  YT(typedValue) *
checkMakeSymbolRef(char *prefix, YT(scopedRef) *scoped,
           YT(ingredientRoleList) *origRoles, bool reportErrors)
{
    YT(defType) *defType = NULL;
    YT(function) *function = NULL;
    YT(ingredientRole) *role = NULL;
    YT(ingredientRoleList) *roles = origRoles;
    YT(pluribusType) *plType = NULL;
    YT(symbol) *name = NULL;
    YT(symbolRef) *symbol = NULL;
    YT(type) *theType = NULL;
    bool foundRole = FALSE;

    if (scoped && scoped->ref && scoped->scope && scoped->scope->ref &&
        scoped->scope->ref->name) {
        name = scoped->ref->name;
    } else {
        return(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
    }
    
    roles = origRoles;
    foundRole = FALSE;
    while (roles && !foundRole && !function) {
        role = roles->ingredientRole;
        
        if (role && role->template && role->template->ingredientImpl &&
            findSymbol(scoped->scope->ref->name, role->ingredients)) {
            foundRole = TRUE;
            function = findPublicFunction(name,
                           role->template->ingredientImpl->functions);
            if (function) {
                theType = function->resultType;
            }
        }   
        roles = roles->next;
    }
    if (theType) {
        switch (YTAG_OF(theType)) {
        case YTAG(defType):
            defType = YC(defType, theType);
            return(YBUILD(typedValue)(TV_OTHER,
                                      YUC(value,SNAME(defType))));
        case YTAG(enumType):
            return(YBUILD(typedValue)(TV_OTHER,YUC(value,"enum")));
        case YTAG(pluribusType):
            plType = YC(pluribusType, theType);
            return(YBUILD(typedValue)(TV_OTHER,
                                      YUC(value,SNAME(plType->type))));
        case YTAG(sequenceType):
            return(YBUILD(typedValue)(TV_OTHER,YUC(value,"sequence")));
        case YTAG(structType):
            return(YBUILD(typedValue)(TV_OTHER,YUC(value,"struct")));
        case YTAG(symbolRef):
            symbol = YC(symbolRef, theType);
            return(YBUILD(typedValue)(TV_OTHER,
                                      YUC(value,SNAME(symbol))));
        case YTAG(undefinedType):
            symbol = YC(symbolRef, YC(undefinedType,theType)->type);
            return(YBUILD(typedValue)(TV_OTHER,
                                      YUC(value,SNAME(symbol))));
        case YTAG(unionType):
            return(YBUILD(typedValue)(TV_OTHER,YUC(value,"union")));
        case YTAG(primType):
            switch (YC(primType,theType)->type) {
            case TagType:
                return(YBUILD(typedValue)(TV_TAG,YUC(value,1)));
            case BOOLEAN:
                return(YBUILD(typedValue)(TV_BOOL,YUC(value,TRUE)));
            case BYTE:
            case CHAR:
                return(YBUILD(typedValue)(TV_CHAR,YUC(value,'#')));
            case DOUBLE:
            case FLOAT: //KSSHack
                          return(YBUILD(typedValue)(TV_FLOAT,YUC(value,1)));
            case INT:
            case LONG:
            case SHORT:
                return(YBUILD(typedValue)(TV_LONG,YUC(value,1)));
            default:
                if (reportErrors)
                    yh_error("%sinvalid primType code %d in checkMakeSymbolRef()",
                             prefix, YC(primType,theType)->type);
                return(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
            }
        case YTAG(stringType):
            return(YBUILD(typedValue)(TV_STRING, YUC(value,"")));
        default:
            if (reportErrors)
                yh_error("%sinvalid type code %d in checkMakeSymbolRef()",
                         prefix, YTAG_OF(theType));
            return(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
        }
    } else {
        if (foundRole) {
            if (reportErrors)
                yh_error("%srole %s: ingredient impl %s:\n  no variable or function %s",
                         prefix, SNAME(scoped->scope->ref),
                         SDNAME(role->template->ingredientImpl), name->name);
            return(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
        } else {
            if (reportErrors)
                yh_error("%sno ingredient role %s for %s.%s", prefix,
                         SNAME(scoped->scope->ref), SNAME(scoped->scope->ref),
                         name->name);
            return(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
        }
    }
}

  YT(typedValue) *
doOperation(char *prefix, bool reportErrors, YT(typedValue) *left, int op,
        YT(typedValue) *right)
{
    bool aBoolean = TRUE;
    char *msg = malloc(120);
    char *leftString;
    char *rightString;
    int exprType = checkTypes(left, op, right, &msg);
    long leftVal, rightVal;

    if (exprType == TV_UND) {
        if (reportErrors) {
            yh_error("%s%s in '%s %s %s'", prefix, msg,
		     typedValueToString(*left), opToString(op),
		     typedValueToString(*right));
        }
        return UND_VAR;
    }
    switch(left->typeCode) {
    case TV_TAG: return UND_VAR;
    case TV_BOOL: case TV_CHAR: case TV_LONG:
        leftVal = (long)YUVP(left,TV_LONG);
        rightVal = (long)YUVP(right,TV_LONG);
        switch (op) {
        case '+': leftVal += rightVal; break;
        case '-': leftVal -= rightVal; break;
        case '*': leftVal *= rightVal; break;
        case '/': leftVal /= rightVal; break;
        case '%': leftVal %= rightVal; break;
        case Lsl: leftVal = leftVal << rightVal; break;
        case Lsr: leftVal = leftVal >> rightVal; break;
        case Asr: leftVal = leftVal >> rightVal; break;
        case '<': leftVal = leftVal < rightVal; break;
        case '>': leftVal = leftVal > rightVal; break;
        case Leq: leftVal = leftVal <= rightVal; break;
        case Geq: leftVal = leftVal >= leftVal; break;
        case  Eq: leftVal = leftVal == rightVal; break;
        case Neq: leftVal = leftVal != rightVal; break;
        case '&': leftVal = leftVal & rightVal; break;
        case '^': leftVal = leftVal ^ rightVal; break;
        case '|': leftVal = leftVal | rightVal; break;
        case And: leftVal = leftVal && rightVal; break;
        case  Or: leftVal = leftVal || rightVal; break;
    default:
        if (reportErrors)
        yh_error("%sinvalid operation %s %s %s", prefix,
             typedValueToString(*left), opToString(op),
             typedValueToString(*right));
        return UND_VAR;
        }
        return YBUILD(typedValue)(exprType,YUC(value,leftVal));
    case TV_FLOAT:
    fprintf(stderr,
        "\nNOTE: floating point types not fully implemented\n\n");
    return YBUILD(typedValue)(TV_BOOL,YUC(value,FALSE));
/*KSSHack next 16 lines for TV_FLOAT
        leftReal = (float)YUVP(left,TV_FLOAT);
        rightReal = (float)YUVP(right,TV_FLOAT);
        switch (op) {
        case '+': leftReal += rightReal; break;
        case '-': leftReal -= rightReal; break;
        case '*': leftReal *= rightReal; break;
        case '/': leftReal /= rightReal; break;
        case '<': leftReal = leftReal < rightReal; break;
        case '>': leftReal = leftReal > rightReal; break;
        case Leq: leftReal = leftReal <= rightReal; break;
        case Geq: leftReal = leftReal >= leftReal; break;
        case  Eq: leftReal = leftReal == rightReal; break;
        case Neq: leftReal = leftReal != rightReal; break;
        }
        return YBUILD(typedValue)(exprType,YUC(value,leftReal));
previous 16 lines for TV_FLOAT KSSHack*/
    case TV_STRING:
        leftString = (char *)YUVP(left,TV_STRING);
        rightString = (char *)YUVP(right,TV_STRING);
        switch (op) {
        case '+':
            msg = (char *)malloc(strlen(leftString) + strlen(rightString));
            msg[0] = '\0';
            strcat(msg, leftString);
            strcat(msg, rightString);
            return YBUILD(typedValue)(exprType,YUC(value,msg));
        case '<': aBoolean = (strcmp(leftString, rightString) < 0); break;
        case '>': aBoolean = (strcmp(leftString, rightString) > 0); break;
        case Leq: aBoolean = (strcmp(leftString, rightString) <= 0); break;
        case Geq: aBoolean = (strcmp(leftString, rightString) >= 0); break;
        case  Eq: aBoolean = (strcmp(leftString, rightString) == 0); break;
        case Neq: aBoolean = (strcmp(leftString, rightString) != 0); break;
    default:
        if (reportErrors)
        yh_error("%sinvalid operation %s %s %s", prefix, leftString,
             opToString(op), rightString);
        return UND_VAR;
        }
        return YBUILD(typedValue)(exprType,YUC(value,aBoolean));
    case TV_OTHER:
        leftString = (char *)YUVP(left,TV_OTHER);
        rightString = (char *)YUVP(right,TV_OTHER);
        switch (op) {
        case '<': case '>': case Leq: case Geq: case  Eq: case Neq: break;
    default:
        if (reportErrors)
        yh_error("%sinvalid operation %s %s %s", prefix, leftString,
             opToString(op), rightString);
        return UND_VAR;
        }
        return YBUILD(typedValue)(exprType,YUC(value,aBoolean));
    default:
    if (reportErrors)
        yh_error("%sinvalid type (%d) of %s in doOperation()", prefix,
             left->typeCode, typedValueToString(*left));
    return UND_VAR;
    }
}

/*  Evaluate an expr and return a typedValue.
   attributes - known attributes for use by ATTR_EXPs that reference
                other attributes
   inits - inits from the presence impl containing parameters for use by
       INIT_EXPRs from templates
   roles - ingredient roles for use by MAKE_EXPs that reference ingredient
           variables
   exprType - the type of expression to evaluate
   reportErrors - should errors be yh_error'ed?
   anyAttributes - were any attributes referenced in the expr?
   anyOperators - were there any operators in the expr?
   prefix - string to prefix any error messages with
 */
  YT(typedValue)
evalExpr(char *prefix, YT(expr) *expr, YT(attributeList) *attributes,
     YT(methodList) *inits, YT(ingredientRoleList) *roles,
     exprTypeEnum exprType, bool reportErrors, bool *anyAttributes,
     bool *anyOperators)
{
    YT(typedValue) *value;
    t_pl_evalEnv env;
    env.knownAttributes = attributes;
    env.inits = inits;
    env.roles = roles;
    env.exprType = exprType;
    env.reportErrors = reportErrors;
    env.prefix = prefix;
    env.anyAttributes = FALSE;
    env.anyOperators = FALSE;
    env.scopedRef = NULL;
    *anyAttributes = (bool)malloc(sizeof(bool));
    *anyOperators = (bool)malloc(sizeof(bool));
    value = ((void*) YH_WALK(expr, expr, YA_FUNC(plEval), &env));
    *anyAttributes = env.anyAttributes;
    *anyOperators = env.anyOperators;
    if (!value)
    value = YBUILD(typedValue)(TV_UND,YUC(value,-1));
    return(*value);
}

YA_FUNC_START(plEval, t_pl_evalEnv)
{
    PCASE(binop,{
        YT(typedValue) *left = PEVAL(left);
        YT(typedValue) *right = PEVAL(right);
        env->anyOperators = TRUE;
    GLBLvalue = left;
    GLBLvalue2 = right;

        if (left->typeCode == TV_UND || left->typeCode == TV_DEP)
            YA_RETURN(left);
        if (right->typeCode == TV_UND || right->typeCode == TV_DEP)
            YA_RETURN(right);
        left = doOperation(env->prefix, env->reportErrors, left, arg->op,
               right);
        YA_RETURN(left);
    });
    PCASE(condop,{
        if (PEVAL(cond))
            YA_RETURN(PEVAL(thenPart))
        else
            YA_RETURN(PEVAL(elsePart));
    });
    PCASE(unop,{
        YT(typedValue) *operand = PEVAL(operand);
        long opval = (long)YUVP(operand,TV_LONG);
        env->anyOperators = TRUE;
        if (operand->typeCode == TV_UND || operand->typeCode == TV_DEP)
            YA_RETURN(operand);
        switch (arg->op) {
            case '+': break;
            case '-': opval = -opval;
                break;
            case '!': opval = !opval;
                break;
            case '~': opval = ~opval;
                break;
        }
        operand = YBUILD(typedValue)(TV_LONG,YUC(value,opval));
        YA_RETURN(operand);
    });
    PCASE(tagLit,{
        YT(typedValue) *val = YBUILD(typedValue)(TV_TAG,YUC(value,TRUE));
        YA_RETURN(val);
    });
    PCASE(numLit,{
        YT(typedValue) *val = YBUILD(typedValue)(TV_LONG,YUC(value,arg->value));
        YA_RETURN(val);
    });
    PCASE(charLit,{
        YT(typedValue) *val = YBUILD(typedValue)(TV_CHAR,
                                                 YUC(value,arg->value));
        YA_RETURN(val);
    });
    PCASE(stringLit,{
        YT(typedValue) *val = YBUILD(typedValue)(TV_STRING,
                                                YUC(value,(char *)arg->value));
        YA_RETURN(val);
    });
    PCASE(boolLit,{
        YT(typedValue) *val = YBUILD(typedValue)(TV_BOOL,
                                                 YUC(value,arg->value));
        YA_RETURN(val);
    });
    PCASE(refTerm,{
        YA_RETURN(PEVAL(value));
    });
    PCASE(symbolRef,{
        if (env->exprType == ATTR_EXP) {
            YA_RETURN(checkAttributeSymbolRef(arg->name,
                                              env->knownAttributes,
                                              env->reportErrors,
                                              &(env->anyAttributes)));
        }  else if (env->exprType == REQ_EXP) {
            /* We're evaluating a requireAtt expr for correctness */
            GLBLattribute = lookupAttributeTypeRef(env->prefix, arg);
            if (GLBLattribute) {
                env->anyAttributes = TRUE;
                GLBLvalue = buildTypedValue(GLBLattribute);
                YA_RETURN(GLBLvalue);
            } else {
                YA_RETURN(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
            }
        } else if (env->exprType == INIT_EXP) {
            YA_RETURN(checkInitSymbolRef(env->prefix, arg->name,
                                         env->inits, env->reportErrors));
        } else if (env->exprType == MAKE_EXP) {
            if (env->scopedRef) {
                GLBLvalue = checkMakeSymbolRef(env->prefix, env->scopedRef,
                                               env->roles, env->reportErrors);
                YA_RETURN(GLBLvalue);
            } else {
                if (env->reportErrors)
                    yh_error("%s%s: vars in make must be <ingredient>.<var_name>",
                             env->prefix, SNAME(arg));
                YA_RETURN(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
            }
        } else {
            if (env->reportErrors)
                yh_error("%sunknown exprType %d in evalExpr()", env->prefix,
                         env->exprType);
            YA_RETURN(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
        }
    });
    PCASE(scopedRef,{
    if (env->exprType == MAKE_EXP) {
        if (arg->scope && arg->scope->scope == NULL) {
        env->scopedRef = arg;
        GLBLvalue = PEVAL(ref);
        env->scopedRef = NULL;
        YA_RETURN(GLBLvalue);
        } else {
        if (env->reportErrors)
            yh_error("%s%s cannot be in nested scope", env->prefix,
                 SNAME(arg->ref));
        YA_RETURN(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
        }
    } else {
        yh_error("%sscopedRef used in constant expression", env->prefix);
        PEVAL(scope);
        PEVAL(ref);
        YA_RETURN(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
    }
    });
    PCASE(outerRef,{
        //KSSX? if (env->reportErrors)
            yh_error("%souterRef used in constant expression", env->prefix);
        PEVAL(ref);
        YA_RETURN(YBUILD(typedValue)(TV_UND,YUC(value,-1)));
    });
}
YA_FUNC_END(plEval)

  char *
stringExpr(YT(expr) *expr, YT(attributeList) *attributes)
{
    char *result;
    t_pl_evalEnv env;
    env.knownAttributes = attributes;
    result = (char *)((void*) YH_WALK(expr, expr, YA_FUNC(plString), &env));
    return(result);
}

YA_FUNC_START(plString, t_pl_grindEnv)
{
    PCASE(binop,{
        char *left = PSTRING(left);
        char *op;
        char *right;
        char *total;
        switch (arg->op) {
            case '+': op = STRDUP(" + "); break;
            case '-': op = STRDUP(" - "); break;
            case '*': op = STRDUP(" * "); break;
            case '/': op = STRDUP(" / "); break;
            case '%': op = STRDUP(" % "); break;
            case Lsl: op = STRDUP(" << "); break;
            case Lsr: op = STRDUP(" >> "); break;
            case Asr: op = STRDUP(" >> "); break;
            case '<': op = STRDUP(" < "); break;
            case '>': op = STRDUP(" > "); break;
            case Leq: op = STRDUP(" <= "); break;
            case Geq: op = STRDUP(" >= "); break;
            case Eq:  op = STRDUP(" == "); break;
            case Neq: op = STRDUP(" != "); break;
            case '&': op = STRDUP(" & "); break;
            case '^': op = STRDUP(" ^ "); break;
            case '|': op = STRDUP(" | "); break;
            case And: op = STRDUP(" && "); break;
            case Or:  op = STRDUP(" || "); break;
        }
        right = PSTRING(right);
        total = malloc(strlen(left) + strlen(op) + strlen(right));
    total[0] = '\0';
        strcat(total,left);
        strcat(total,op);
        strcat(total,right);
        YA_RETURN(total);
    });
    PCASE(condop,{
        if (PSTRING(cond))
            YA_RETURN(PSTRING(thenPart))
        else
            YA_RETURN(PSTRING(elsePart));
    });
    PCASE(unop,{
        char *oper;
        char *symbol;
        char *total;
        switch (arg->op) {
            case '+': symbol = STRDUP("+\0"); break;
            case '-': symbol = STRDUP("-\0"); break;
            case '!': symbol = STRDUP("!\0"); break;
            case '~': symbol = STRDUP("~\0"); break;
        }
        oper = PSTRING(operand);
        total = malloc(strlen(symbol) + strlen(oper));
    total[0] = '\0';
        strcat(total, symbol);
        strcat(total, oper);
        YA_RETURN(total);
    });
    PCASE(tagLit,{
        YA_RETURN("**Tag**");
    });
    PCASE(numLit,{
        char *value = malloc(20);
        sprintf(value, "%d", arg->value);
        YA_RETURN(value);
    });
    PCASE(charLit,{
        char *value = malloc(5);
        sprintf(value, "'%c'", (char)(arg->value));
        YA_RETURN(value);
    });
    PCASE(stringLit,{
        char *value = malloc(strlen((char *)arg->value)+2);
        value[0] = '\0';
        strcat(value, "\"");
        strcat(value, (char *)arg->value);
        strcat(value, "\"");
        YA_RETURN(value);
    });
    PCASE(boolLit,{
        char *value = STRDUP((arg->value ? "true":"false"));
        YA_RETURN(value);
    });
    PCASE(refTerm,{
        YA_RETURN(PSTRING(value));
    });
    PCASE(symbolRef,{
        char *value = STRDUP(SNAME(arg));
        YA_RETURN(value);
    });
    PCASE(scopedRef,{
        yh_error("scopedRef used in constant expression");
        PSTRING(scope);
        PSTRING(ref);
        YA_RETURN("scopedRef used in constant expression");
    });
    PCASE(outerRef,{
        yh_error("outerRef used in constant expression");
        PSTRING(ref);
        YA_RETURN("outerRef used in constant expression");
    });
}
YA_FUNC_END(plString)

