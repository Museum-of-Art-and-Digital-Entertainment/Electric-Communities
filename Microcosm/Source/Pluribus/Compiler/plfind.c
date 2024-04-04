/*
  plfind.c -- Element location routines for Pluribus.

  Karl Schumaker
  Electric Communities
  5-November-1996

  Copyright 1996 Electric Communities, all rights reserved.

*/

#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "pl.h"
#include "y.tab.h"
#include "plroute.h"
#include "plutil.h"

  YT(attribute) *
findAttributeByName(YT(symbol) *name, YT(attributeList) *attributes) {
    while (attributes) {
        if (name == attributes->attribute->type->name->name)
            return attributes->attribute;
        attributes = attributes->next;
    }
    return NULL;
}

  YT(deliverAtt) *
findDeliver(YT(symbol) *source, long scope, YT(ingredientList) *ingredients,
            YT(ingredient) **ingredient)
     /* Find 'source' in a list of delivery statements. */
{
    YT(deliverAtt) *deliver = NULL;
    YT(deliverAttList) *deliveries = NULL;

    while (ingredients) {
        *ingredient = ingredients->ingredient;
        deliveries = (*ingredient)->deliverAtts;
        while (deliveries) {
            deliver = deliveries->deliverAtt;
            if (YTAG_TEST(deliver, deliverAtt))
                if (deliver->scope == scope)
                    if (source == deliver->source)
                        return deliver;
            deliveries = deliveries->next;
        }
        ingredients = ingredients->next;
    }
    *ingredient = NULL;
    return NULL;
}

  YT(function) *
findFunctionInImpl(YT(symbol) *name, YT(ingredientImpl) *impl)
{
    YT(functionList) *functions = impl->functions;

    while (functions) {
        if (name == functions->function->name)
            return functions->function;
        functions = functions->next;
    }
    return NULL;
}

  YT(implementsAtt) *
findImplements(char *implName, YT(implementsAttList) *implList)
{
    while (implList) {
        if (strcmp(implName, pSymbolRef(implList->implementsAtt->name)) == 0) {
            return implList->implementsAtt;
        }
        implList = implList->next;
    }
    return NULL;
}

  YT(implementsAtt) *
findImplementsAtt(YT(implementsAtt) *impl, YT(implementsAttList) *implList)
{
    while (implList) {
        if (strcmp(pSymbolRef(impl->name),
                   pSymbolRef(implList->implementsAtt->name)) == 0) {
            return implList->implementsAtt;
        }
        implList = implList->next;
    }
    return NULL;
}

  YT(ingredient) *
findIngredient(YT(symbol) *roleName, YT(ingredientList) *ingredients)
     /* Find the 'roleName' in a list of ingredients. */
{
    while (ingredients) {
        if (roleName == ingredients->ingredient->name)
            return ingredients->ingredient;
        ingredients = ingredients->next;
    }
    return NULL;
}

  YT(ingredientRole) *
findIngredientRole(YT(symbol) *roleName, YT(ingredientRoleList) *roles)
     /* Find the 'roleName' in a list of ingredient roles. */
{
    YT(symbolList) *names = NULL;

    while (roles) {
    names = roles->ingredientRole->ingredients;
    while (names) {
        if (roleName == names->symbol)
        return roles->ingredientRole;
        names = names->next;
    }
        roles = roles->next;
    }
    return NULL;
}

  YT(method) *
findInit(YT(method) *init, YT(presenceImpl) *impl)
     /* Find an init in 'impl'->initBlocks that matches the parameter signature
    of 'init' */
{
    YT(method) *check = NULL;
    YT(methodList) *inits = impl->initBlocks;

    while (inits) {
        check = inits->method;
    if (compareParameters("", init->name, init->params,
                  check->name, check->params, FALSE))
            return check;
        inits = inits->next;
    }
    return NULL;
}

  YT(kind) *
findKindInKind(YT(symbol) *name, YT(kind) *kind)
{
    YT(kind) *found = kind;
    YT(kindList) *extends = kind->kinds;

    found = lookupKindRef("", refSymbol(name), FALSE);
    if (found) {
    if (name == kind->name->name)
        return found;
    while (extends) {
        found = findKindInKind(name, extends->kind);
        if (found)
        return found;
        extends = extends->next;
    }
    }
    return NULL;
}

  YT(symbol) *
findMappedNeighbor(YT(template) *template, YT(neighbor) *neighbor)
{
    YT(mapAttList) *mapAtts = template->mapAtts;
    while (mapAtts) {
        if (neighbor->name == mapAtts->mapAtt->mapFrom) {
            if (!mapAtts->mapAtt->mapTo)
                return NULL;
            else
                return(mapAtts->mapAtt->mapTo);
        }
        mapAtts = mapAtts->next;
    }
    return(neighbor->name);
}

  YT(mapAtt) *
findMapping(long mapType, YT(symbol) *name, YT(template) *template)
     /* Find a mapping in 'template' whose mapFrom matches 'name' */
{
    YT(mapAttList) *mappings = template->mapAtts;
    YT(mapAtt) *mapping;
    long scope;

    while (mappings) {
        mapping = mappings->mapAtt;
        scope = mapping->scope;
        if ((name == mapping->mapFrom) &&
            ((scope == mapType) || (scope == DEFAULT)))
            return mappings->mapAtt;
        mappings = mappings->next;
    }
    return NULL;
}

  YT(ingredient)*
findMessageDestination(YT(presenceStructure) *presStruc, YT(protoDef) *proto,
                       int scope, YT(symbol) **ingrRole, YT(symbol) **message)
{
    YT(ingredientList) *ingredients = presStruc->ingredients;
    YT(deliverAttList) *deliverAtts;
    YT(deliverAtt) *deliverAtt = NULL;
    char prefix[MSGLEN];
    
    sprintf(prefix, "presenceStructure %s: ", SDNAME(presStruc));
    *ingrRole = NULL;
    *message = NULL;
    
    /* XXX This should be smarter about looking at the ingredient kind to
       check for the presence of the indicated message (or look at it to find
       the default ingredient to deliver to in some cases). */
    while (ingredients) {
        deliverAtts = ingredients->ingredient->deliverAtts;
        while (deliverAtts) {
            deliverAtt = deliverAtts->deliverAtt;
            if (YTAG_TEST(deliverAtt,deliverAtt)) {
                if (deliverAtt->scope == scope &&
                    matchMethodOrKindName(prefix, deliverAtt->source,
                                          proto->name,
                                          deliverAtt->contextScope)) {
                    *ingrRole = ingredients->ingredient->name;
                    if (deliverAtt->target)
                        *message = deliverAtt->target;
                    else
                        *message = proto->name;
                    return ingredients->ingredient;
                }
            }
            deliverAtts = deliverAtts->next;
        }
        ingredients = ingredients->next;
    }
    *ingrRole = NULL;
    *message = NULL;
    //KSSHack This should be found in plcheck.c:checkPresenceImpls()
      //yh_error("Unable to find message destination for %s message %s in %s",
                 //         scope == UNUM ? "unum" : "presence", SNAME(proto),
                 //         SDNAME(presStruc));
    return NULL;
}

  YT(method) *
findMethodInImpl(YT(symbol) *name, YT(ingredientImpl) *impl)
{
    YT(methodList) *methods = impl->methods;

    while (methods) {
        if (name == methods->method->name)
            return methods->method;
        methods = methods->next;
    }
    return NULL;
}

  YT(parameterDecl) *
findParameter(YT(symbol) *name, YT(parameterDeclList) *params)
{
    while (params) {
    if (name == params->parameterDecl->name)
        return params->parameterDecl;
    params = params->next;
    }
    return NULL;
}

  YT(parameterDecl) *
findParameterInMethods(YT(symbol) *name, YT(methodList) *methods)
{
    YT(parameterDecl) *param = NULL;

    while (methods) {
    param = findParameter(name, methods->method->params);
    if (param)
        return param;
    methods = methods->next;
    }
    return NULL;
}

  YT(presenceRole) *
findPresenceRole(YT(symbol) *presence, YT(unumImpl) *impl)
     /* Find role 'presence' in 'impl'. */
{
    YT(presenceRoleList) *roles = impl->roles;

    while (roles) {
        if (findSymbol(presence, roles->presenceRole->presences))
            return roles->presenceRole;
        roles = roles->next;
    }
    return NULL;
}

  YT(presence) *
findPrimePresence(YT(unumStructure) *struc)
{
    YT(presenceList) *presences = struc->presences;

    while (presences) {
        if (presences->presence->name == struc->prime)
            return(presences->presence);
        presences = presences->next;
    }
    return(NULL);
}

  YT(protoDef) *
findProtoInKind(YT(symbol) *name, YT(kind) *kind)
{
    YT(kindList) *extends = kind->kinds;
    YT(protoDef) *proto = NULL;
    YT(protoDefList) *protos = kind->protos;

    while (protos) {
        proto = protos->protoDef;
        if (name == proto->name)
            return proto;
        protos = protos->next;
    }
    while (extends) {
        proto = findProtoInKind(name, extends->kind);
        if (proto)
            return proto;
        extends = extends->next;
    }
    return NULL;
}

  YT(function) *
findPublicFunction(YT(symbol) *name, YT(functionList) *functions)
{
    YT(function) *function = NULL;

    while (functions) {
        function = functions->function;
        if (name == function->name && function->modifiers & MOD_PUBLIC) {
            return functions->function;
        }
        functions = functions->next;
    }
    return NULL;
}

  YT(symbol) *
findSymbol(YT(symbol) *symbol, YT(symbolList) *symbols)
{
    while (symbols) {
        if (symbol == symbols->symbol)
            return symbols->symbol;
        symbols = symbols->next;
    }
    return NULL;
}

  YT(variable) *
findVariable(YT(symbol) *name, YT(variableList) *vars)
{
    while (vars) {
    if (name == vars->variable->name)
        return vars->variable;
    vars = vars->next;
    }
    return NULL;
}

  YT(protoDef) *
locateProto(YT(symbol) *name, YT(ingredientList) *ingredients)
     /* Locate the prototype 'name' in the Kinds from the list of
        'ingredients'. */
{
    YT(protoDef) *proto = NULL;

    while (ingredients) {
        proto = findProtoInKind(name, ingredients->ingredient->kind);
        if (proto)
            return proto;
        ingredients = ingredients->next;
    }
    return NULL;
}
