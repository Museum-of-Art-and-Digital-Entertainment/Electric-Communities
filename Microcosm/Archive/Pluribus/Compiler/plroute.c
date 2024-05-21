/*
  plroute.c -- Message router class output for Pluribus.
  
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
#include "plroute.h"

  YT(scopedRefList) *
buildScopedVarList(char *prefix, YT(exprList) *inits)
{
    YT(scopedRefList) *ingVars = NULL, *tempVars = NULL;
    
    while (inits) {
        tempVars = varsFromExpr(prefix, inits->expr, FALSE);
        while (tempVars) {
            if (!inScopedRefList(tempVars->scopedRef, ingVars))
                ingVars = YBUILD(scopedRefList) (tempVars->scopedRef,
                                                 ingVars);
            tempVars = tempVars->next;
        }
        inits = inits->next;
    }
    return ingVars;
}   

  YT(presence) *
findPresence(YT(unumStructure) *struc, YT(symbol) *roleName)
{
    YT(presence) *presence = NULL;
    YT(presenceList) *presences = struc->presences;
    
    while (presences) {
        presence = presences->presence;
        if (presence->name == roleName)
            return(presence);
        presences = presences->next;
    }
    return(NULL);
}

  YT(template) *
findTemplate(YT(presenceImpl) *presImpl, YT(symbol) *roleName)
{
    YT(ingredientRoleList) *roles = presImpl->roles;
    
    while (roles) {
        YT(symbolList) *ingredients = roles->ingredientRole->ingredients;
        while (ingredients) {
            if (ingredients->symbol == roleName)
                return(roles->ingredientRole->template);
            else
                ingredients = ingredients->next;
        }
        roles = roles->next;
    }
    return(NULL);
}

  bool
inScopedRefList(YT(scopedRef) *ref, YT(scopedRefList) *refs)
{
    while (refs) {
        if (ref == refs->scopedRef)
            return TRUE;
        refs = refs->next;
    }
    return FALSE;
}

  bool
matchMethodInKind(YT(kind) *kind, YT(symbol) *message)
{
    if (kind) {
        YT(kindList) *kinds = kind->kinds;
        YT(protoDefList) *protos = kind->protos;
        
        while (protos) {
            if (message == protos->protoDef->name)
                return(TRUE);
            protos = protos->next;
        }
        while (kinds) {
            if (matchMethodInKind(kinds->kind, message))
                return(TRUE);
            kinds = kinds->next;
        }
    }
    return(FALSE);
}

//  KSSHack  What about the comparison if (message == target)--what if
//KSSHack the message is named the same thing as the kind?
//KSSHack  Bad move, I think...
 bool
matchMethodOrKindName(char *prefix, YT(symbol) *target, YT(symbol) *message,
                      YT(scope) *contextScope)
{
    YT(scope) *saveScope = NULL;
    bool result = FALSE;
    
    if (message == target) {
        return(TRUE);
    } else {
        saveScope = setCurrentScope(contextScope);
        result = matchMethodInKind(
                                   lookupKindRef(prefix, refSymbol(target), FALSE),
                                   message);
        setCurrentScope(saveScope);
        return(result);
    }
}

  static char *
pRouterName(char *tag, YT(unumImpl) *impl, YT(symbol) *roleName)
{
    static char buf[BUFLEN];
    
    sprintf(buf, "%s_%s$%s", pBaseJavaName(YC(anyBinding,impl)),
            roleName->name, tag);
    return(buf);
}

  char *
pPrName(YT(unumImpl) *impl, YT(symbol) *roleName)
{
    return(pRouterName("pr", impl, roleName));
}

  char *
pUrName(YT(unumImpl) *impl, YT(symbol) *roleName)
{
    return(pRouterName("ur", impl, roleName));
}
