/*
  plcheck.c -- Element validation routines for Pluribus.

  Karl Schumaker
  Electric Communities
  21-October-1996

  Copyright 1996 Electric Communities, all rights reserved.

*/

#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "pl.h"
#include "y.tab.h"

enum typeOfDeliverEnum {
    DEL_UNKNOWN, DEL_KIND, DEL_PROTO
};

typedef enum typeOfDeliverEnum deliverTypeEnum;

bool checkDeliverStatements(YT(presenceStructure) *struc, YT(unumImpl) *impl);
bool checkDeliverTarget(char *prefix, YT(ingredient) *ingredient,
                        YT(deliverAtt) *deliver, YT(presenceStructure) *struc,
                        deliverTypeEnum deliverType);
bool checkKindDeliveries(char *prefix, long scope, YT(kind) *kind,
                         YT(presenceStructure) *struc);
bool checkProtoDelivery(char *prefix, long scope, YT(protoDef) *proto,
                        YT(presenceStructure) *struc);
bool checkProtosInImpl(char *prefix, YT(ingredientImpl) *impl,
                       YT(kind) *kind);
bool checkStateBundle(char *name, YT(stateBundle) *stateBundle);
bool checkTemplate(char *prefix, YT(template) *template,
                   YT(ingredientImpl) *impl);
bool checkTemplates(char *prefix, YT(presenceImpl) *impl,
                    YT(presenceStructure) *pStruc, YT(unumImpl) *uImpl);
bool compareParameters(char *prefix, YT(symbol) *firstName,
                       YT(parameterDeclList) *first, YT(symbol) *secondName,
                       YT(parameterDeclList) *second, bool report);
bool compareProtosInKinds(char *prefix, YT(kind) *kind1, YT(kind) *kind2);
bool matchAllProtosInKind(char *prefix, YT(kind) *source,
                          YT(ingredient) *ingredient);
char *neighborErrMessage(char *templateName, YT(symbolList) *ingredients);

/**
 *  Check the deliver statements in a presence structure to see if they are
 * correct.  Then check to make sure that all of the necessary kinds are
 * specifically, or that each of their protos are delivered individually.
 * Called from checkPresenceImplLinks().
 */
  bool
checkDeliveries(YT(presenceStructure) *struc, YT(unumImpl) *impl)
{
    bool allsWell = TRUE;
    char prefix[MSGLEN];
    
    /*  Check the source and target for all deliver statements */
    if (!checkDeliverStatements(struc, impl))
        allsWell = FALSE;
    sprintf(prefix,
            "presence structure %s: deliver kind %s from unum impl %s: ",
            SDNAME(struc), SDNAME(impl->structure->kind),
            SDNAME(impl->structure));
    /*  Check for kind deliveries for the unum kind and presence
     * kind for this presence structure.
     */
    if (!checkKindDeliveries(prefix, UNUM, impl->structure->kind, struc))
        allsWell = FALSE;
    sprintf(prefix, "presence structure %s: deliver kind %s: ",
            SDNAME(struc), SDNAME(struc->kind));
    if (!checkKindDeliveries(prefix, PRESENCE, struc->kind, struc))
        allsWell = FALSE;
    
    return allsWell;
}

/*  Make sure the deliver statement source matches a kind or specific
 * prototype in the unum or presence kind.  Only deliverAtts should get sent
 * here, NOT deliverDefaultAtts, so there will always be source.  Returns true
 * if the deliver source exists, false otherwise.  The deliverType parameter
 * is set to the type of deliver--KIND, PROTO or UNKNOWN if the source does
 * not exist.
 */
  bool
checkDeliverSource(char *prefix, YT(deliverAtt) *deliver,
                   YT(presenceStructure) *struc, YT(unumImpl) *impl,
                   deliverTypeEnum *deliverType)
{
    YT(kind) *fromKind = NULL;
    YT(protoDef) *fromProto = NULL;
    bool allsWell = TRUE;

    /*
     *  Check if the source is a kind.
     */
    *deliverType = DEL_KIND;
    if (deliver->scope == UNUM)
        fromKind = findKindInKind(deliver->source, impl->structure->kind);
    else if (deliver->scope == PRESENCE)
        fromKind = findKindInKind(deliver->source, struc->kind);

    /*
     *  Check if the source is a prototype.
     */
    if (!fromKind) {
        *deliverType = DEL_PROTO;
        if (deliver->scope == UNUM)
            fromProto = findProtoInKind(deliver->source,
                                        impl->structure->kind);
        else if (deliver->scope == PRESENCE)
            fromProto = findProtoInKind(deliver->source, struc->kind);
        if (!fromProto) {
            if (deliver->source != struc->kind->name->name &&
                deliver->source != impl->structure->kind->name->name) {
                yh_error("%s\n  no kind or method %s defined in %s to deliver",
                         prefix, deliver->source->name,
                         deliver->scope == UNUM ?
                         SDNAME(impl->structure->kind) :
                         SDNAME(struc->kind));
                *deliverType = DEL_UNKNOWN;
                allsWell = FALSE;
            }
        }
    }
    return allsWell;
}

/**
 *  Check the source and target for each deliver statement in every ingredient
 * in a presence structure.  NOTE that if a kind is being delivered,
 * checkKindDelivers() will check to see that each of it's methods has a
 * target method in the target ingredient.
 */
  bool
checkDeliverStatements(YT(presenceStructure) *struc, YT(unumImpl) *impl)
{
    YT(deliverAtt) *deliver = NULL;
    YT(deliverAttList) *deliveries = NULL;
    YT(ingredient) *ingredient;
    YT(ingredientList) *ingredients = struc->ingredients;
    bool allsWell = TRUE;
    deliverTypeEnum deliverType = DEL_UNKNOWN;
    char prefix[MSGLEN];
    
    while (ingredients) {
        ingredient = ingredients->ingredient;
        deliveries = ingredient->deliverAtts;
        sprintf(prefix, "presence structure %s: ingredient %s: ",
                SDNAME(struc), SNAME(ingredient));
        while (deliveries) {
            deliver = deliveries->deliverAtt;
            if (checkDeliverSource(prefix, deliver, struc, impl,
                                   &deliverType)) {
                if (!checkDeliverTarget(prefix, ingredient, deliver, struc,
                                        deliverType)) {
                    allsWell = FALSE;
                } else {
                    allsWell = FALSE;
                }
            } else {
                allsWell = FALSE;
            }
            deliveries = deliveries->next;
        }
        ingredients = ingredients->next;
    }
    return allsWell;
}

/*  Make sure the deliver statement target matches a prototype in the
 * ingredient kind being delivered to.
 */
  bool
checkDeliverTarget(char *prefix, YT(ingredient) *ingredient,
                   YT(deliverAtt) *deliver, YT(presenceStructure) *struc,
                   deliverTypeEnum deliverType)
{
    YT(kind) *kind = NULL;
    YT(protoDef) *proto = NULL;
    YT(symbol) *target = NULL;
    bool allsWell = TRUE;

    /*  If the deliver does not have a target, the target is same as the
     * source.
     */
    if (deliver->target) {
        target = deliver->target;
    } else {
        target = deliver->source;
    }

    if (target) {
        /*  If we're delivering a prototype, make sure that the target proto
         * exists in the ingredient kind we're delivering to.
         */
        if (deliverType == DEL_PROTO) {
            proto = findProtoInKind(target, ingredient->kind);
            if (!proto) {
                yh_error("%s\n  no prototype %s in %s to deliver to",
                         prefix, target->name,
                         SDNAME(ingredient->kind));
                allsWell = FALSE;
            }
        } else if (deliverType == DEL_KIND) {
            /*  If we're delivering a kind, we better not have a target
             * *method* to deliver to...
             */
            if (deliver->target) {
               yh_error("%s\n  cannot deliver kind %s to a specific method %s",
                         prefix, deliver->source->name, deliver->target->name);
                proto = findProtoInKind(target, ingredient->kind);
                if (!proto) {
                    yh_error(
                       "%s\n  no prototype %s in %s to deliver to anyway",
                       prefix, target->name, SDNAME(ingredient->kind));
                }
                allsWell = FALSE;
            }
        }
    }
    return allsWell;
}

/**
 * Make sure there are no duplicate deliver statements in a list of
 * ingredients from a presence structure.
 */
  bool
checkDuplicateDelivers(char *prefix, YT(ingredientList) *ingredients)
{
    YT(deliverAtt) *deliverAtt = NULL;
    YT(deliverAttList) *deliveries = NULL;
    YT(symbolList) *unumDelivers = NULL;
    YT(symbolList) *presenceDelivers = NULL;
    bool allsWell = TRUE;
    char newPrefix[MSGLEN];

    while (ingredients) {
        sprintf(newPrefix, "%singredient %s: ", prefix,
                SNAME(ingredients->ingredient));
        deliveries = ingredients->ingredient->deliverAtts;
        while (deliveries) {
            deliverAtt = deliveries->deliverAtt;
            if (deliverAtt->scope == UNUM) {
                /*
                 * If it's an unum deliver statement, see if it's in the list
                 * of unum delivers found so far.
                 */
                if (findSymbol(deliverAtt->source, unumDelivers)) {
                    yh_error("%s%s already delivered", newPrefix,
                             deliverAtt->source->name);
                    allsWell = FALSE;
                } else {
                    /*
                     * Add this one to the unum delivers list.
                     */
                    unumDelivers = YBUILD(symbolList)(deliverAtt->source,
                                                      unumDelivers);
                }
            } else if (deliverAtt->scope == PRESENCE) {
                /*
                 * If it's a presence deliver statement, see if it's in the
                 * list of presence delivers found so far.
                 */
                if (findSymbol(deliverAtt->source, presenceDelivers)) {
                    yh_error("%s%s already delivered", newPrefix,
                             deliverAtt->source->name);
                    allsWell = FALSE;
                } else {
                    /*
                     * Add this one to the presence delivers list.
                     */
                    presenceDelivers = YBUILD(symbolList)(deliverAtt->source,
                                                          presenceDelivers);
                }
            } else {
                yh_error("%sunknown delivery scope %d", newPrefix,
                         deliverAtt->scope);
                allsWell = FALSE;
            }
            deliveries = deliveries->next;
        }
        ingredients = ingredients->next;
    }
    return allsWell;
}

/**
 *  Get the list of ingredient impls from the ingredient role names, then
 * make sure that the ingredients in the presence impl to make are the same
 * in type and number.
 */
  bool
checkInitIngredients(char *prefix, YT(symbolList) *ingrNames,
       YT(ingredientRoleList) *ingrs1, YT(ingredientRoleList) *ingrs2)
{
    YT(ingredientRole) *ingrRole = NULL;
    YT(symbol) *roleName = NULL;
    bool allsWell = TRUE;
    int numRoles = 0;
    
    while (ingrNames && ingrs2) {
        numRoles++;
        roleName = ingrNames->symbol;
        ingrRole = findIngredientRole(roleName, ingrs1);
        if (ingrRole) {
/*KSSHack Don't check until we come up with a scheme for host/client diffs
//KSSHack What about making jiGetCLientState() return the actual ist<type>?
//KSSHack Then we can check the type of the host jiGetCLientState() against
//KSSHack the type of the init(ist<type>) argument...
            if (ingrRole->template && ingrs2->ingredientRole &&
                ingrs2->ingredientRole->template) {
//KSSHack shouldn't this compare the *spirit* of the impls rather than ptr equality
                if (ingrRole->template->ingredientImpl->name !=
                    ingrs2->ingredientRole->template->ingredientImpl->name) {
                    yh_error("%sIngredient role %s does not have same impl as role %d in target presence",
                             prefix, roleName->name, numRoles);
                    allsWell = FALSE;
                }
            } else {
                allsWell = FALSE;
            }
KKSHack*/
        } else {
            yh_error("%sNo ingredient role %s in making presence impl", prefix,
                     roleName->name);
            allsWell = FALSE;
        }
        ingrNames = ingrNames->next;
        ingrs2 = ingrs2->next;
    }
    if (ingrNames) {
        yh_error("%sToo many ingredient names in make statement", prefix);
        allsWell = FALSE;
    }
    if (ingrs2) {
        yh_error("%sToo few ingredient names in make statement", prefix);
        allsWell = FALSE;
    }
    
    return allsWell;
}

/**
 * Go through kinds (a list of extends from an an impl's kind's
 * extend list) and call checkRequirements() on on all of the extended
 * Kind's attributes (and *their* extends list, etc.)
 */
  bool
checkExtendRequirements(char *prefix, YT(kindList) *kinds,
                        YT(attributeList) *attrs)
{
    YT(kind) *kind;
    bool allsWell = TRUE;
    char newPrefix[MSGLEN];

    while (kinds) {
        kind = kinds->kind;
        sprintf(newPrefix, "%s\n  kind %s: ", prefix, SDNAME(kind));
        if (!checkExtendRequirements(newPrefix, kind->kinds, attrs))
            allsWell = FALSE;
        if (!checkRequirements(prefix, kind->attributes, attrs))
            allsWell = FALSE;
        kinds = kinds->next;
    }
    return allsWell;
}

/**
 *  Check a list of methods for duplicates (name and type signature).
 */
  bool
checkForDuplicateMethods(char *prefix, YT(methodList) *methods)
{
    YT(methodList) *temps = NULL;
    bool allsWell = TRUE;
    int method = 1, temp = 1;

    while (methods) {
        temps = methods->next;
        temp = 2;
        while (temps) {
            if (methods->method->name == temps->method->name &&
                compareParameters(prefix, methods->method->name,
                                  methods->method->params,
                                  temps->method->name,
                                  temps->method->params, FALSE)) {
                yh_error("%s# %d duplicates # %d", prefix, temp, method);
                allsWell = FALSE;
            }
            temp++;
            temps = temps->next;
        }
        method++;
        methods = methods->next;
    }
    return allsWell;
}

/**
 *  Check implements on ingredient impl against implements on kind.
 */
  bool
checkImplements(char *prefix, YT(ingredientImpl) *impl, YT(kind) *kind)
{
    YT(implementsAttList) *iImplements = impl->implements;
    YT(implementsAttList) *kImplements = kind->implements;
    YT(implementsAtt) *implements;
    YT(kindList) *extends = kind->kinds;
    bool allsWell = TRUE;
    char *implementName;
    char newPrefix[MSGLEN];

    /*
     * Check against any implements in the extended kinds.
     */
    while (extends) {
        sprintf(newPrefix, "%s\n  kind %s: ", prefix, SDNAME(extends->kind));
        if (!checkImplements(newPrefix, impl, extends->kind))
            allsWell = FALSE;
        extends = extends->next;
    }
    while (kImplements) {
        implementName = pSymbolRef(kImplements->implementsAtt->name);
        if (strcmp(implementName, UnumEinterfaceName) != 0 &&
            strcmp(implementName, PresenceEinterfaceName) != 0 &&
            strcmp(implementName, PresenceHostEinterfaceName) != 0
            ) {
            implements = findImplementsAtt(kImplements->implementsAtt,
                                           iImplements);
            if (!implements) {
                yh_error("%s'%s' from %s not implemented",
                         prefix, implementName, SDNAME(kind));
                allsWell = FALSE;
            }
        }
        kImplements = kImplements->next;
    }
    return allsWell;
}

/**
 *  Check the various parts of an ingredient impl.
 */
  bool
checkIngredientImpl(char *prefix, YT(ingredientImpl) *impl, YT(kind) *kind)
{
    YT(kindList) *extends = kind->kinds;
    bool allsWell = TRUE;
    char newPrefix[MSGLEN];

    if (!validateAttributes(prefix, impl->attributes))
        allsWell = FALSE;
    if (!checkExtendRequirements(prefix, kind->kinds, impl->attributes))
        allsWell = FALSE;
    if (!checkRequirements(prefix, kind->attributes, impl->attributes))
        allsWell = FALSE;
    if (!checkStateBundle(SDNAME(impl), impl->stateBundle))
        allsWell = FALSE;
    if (!checkImplements(prefix, impl, kind))
        allsWell = FALSE;
    if (!compareProtosInKinds(prefix, kind, impl->kind))
        allsWell = FALSE;
    if (!checkProtosInImpl(prefix, impl, kind))
        allsWell = FALSE;

    return allsWell;
}

/**
 *  Make sure that no two init()s or prime init()s have the same
 * type signature.
 */
  bool
checkInits(char *prefix, YT(presenceImpl) *presImpl)
{
    bool allsWell = TRUE;
    char newPrefix[MSGLEN];

    if (presImpl) {
        sprintf(newPrefix,"%s prime init ", prefix);
        allsWell = checkForDuplicateMethods(newPrefix,
                                            presImpl->primeInitBlocks);
        sprintf(newPrefix,"%s init ", prefix);
        allsWell = checkForDuplicateMethods(newPrefix, presImpl->initBlocks);
    }
    return allsWell;
}

/**
 *  Check to see if a kind is being delivered, if the kinds it extends are
 * being handled and whether each individual method is covered if the kind
 * itself is not delivered.
 */
  bool
checkKindDeliveries(char *prefix, long scope, YT(kind) *kind,
                    YT(presenceStructure) *struc)
{
    YT(deliverAtt) *deliver = NULL;
    YT(protoDef) *proto = NULL;
    YT(protoDefList) *protos = kind->protos;
    YT(ingredient) *ingredient = NULL;
    YT(kindList) *kinds = kind->kinds;
    bool allsWell = TRUE;
    char newPrefix[MSGLEN];
    int num = 0;

    /*  Find out if this kind is delivered by this presence structure (return
     * the ingredient which did so through the ingredient parameter).
     */
    deliver = findDeliver(kind->name->name, scope, struc->ingredients,
                          &ingredient);
    /*  If this kind is being delivered by one of the presence structure's
     * ingredients, make sure all of its method prototypes exist in the kind of
     * the delivering ingredient (including extended kinds' prototypes).
     */
    if (deliver) {
        if (!matchAllProtosInKind(prefix, kind, ingredient)) {
            allsWell = FALSE;
        }
    } else {
        /*  If this kind is *not* delivered, call checkKindDeliveries() on any
         * kinds which it extends.
         */
        while (kinds) {
            sprintf(newPrefix, "%s\n  kind %s: ", prefix,
                    SDNAME(kinds->kind));
            if (!checkKindDeliveries(newPrefix, scope, kinds->kind, struc)) {
                allsWell = FALSE;
            }
            kinds = kinds->next;
        }
        /*  Again, if this kind was not specifically delivered, make sure that
         * each of its method prototypes were explicitly delivered.
         */
        ingredient = NULL;
        while (protos) {
            proto = protos->protoDef;
            deliver = findDeliver(proto->name, scope, struc->ingredients,
                                  &ingredient);
            if (deliver) {
                sprintf(newPrefix, "%s\n  method %s in ingredient %s: ",
                        prefix, SNAME(proto), SNAME(ingredient));
                if (!checkProtoDelivery(newPrefix, scope, proto, struc)) {
                    allsWell = FALSE;
                }
            } else {
                yh_error("%smethod %s not delivered", prefix, SNAME(proto));
                allsWell = FALSE;
            }
            protos = protos->next;
        }
    }

    return allsWell;
}

/**
 *  Validate the ingredients in the "make pr ingrs1, ingr2..." statement
 * and then match their impl and number against the ingredients in the target
 * presence impl.
 */
  bool
checkMake(char *prefix, YT(presenceImpl) *presImpl, YT(unumImpl) *unumImpl)
{
    YT(symbolList) *inits = NULL;
    YT(presence) *presence = NULL;
    YT(presenceRole) *presRole = NULL;
    bool allsWell = TRUE;
    char newPrefix[MSGLEN];

    if (presImpl->makeAtt) {
        inits = presImpl->makeAtt->inits;
        if (presImpl->unumImpl) {
            /*
             *  If there is an unum impl associated with this presence impl,
             * find the presence in the structure of this unum impl that
             * the original presence impls makes.
             */
            presence = findPresence(presImpl->unumImpl->structure,
                                    presImpl->makeAtt->name);
            if (presence) {
                /*
                 * If such a presence exists, find the matching role from the
                 * unum impl.
                 */
                presRole = findPresenceRole(presence->name,
                                            presImpl->unumImpl);
                sprintf(newPrefix, "%smake %s:\n  ", prefix,
                        SNAME(presImpl->makeAtt));
                if (presRole) {
                    if (presRole->impl) {
                        sprintf(newPrefix, "%smake %s vs %s:\n  ", prefix,
                                SNAME(presImpl->makeAtt),
                                SDNAME(presRole->impl));
                        /*
                         * If the presence role also exists, check the original
                         * presence impl's make statement against the ingredients
                         * in the target presence impl.
                         */
                        allsWell = checkInitIngredients(newPrefix, inits,
                                                        presImpl->roles,
                                                        presRole->impl->roles);
                    }
                } else {
                    yh_error("%sno role %s in %s", newPrefix,
                             SNAME(presImpl->makeAtt),
                             SDNAME(presImpl->unumImpl));
                    return FALSE;
                }
            } else {
                yh_error("%sno presence %s in %s", newPrefix,
                         SNAME(presImpl->makeAtt),
                         SDNAME(presImpl->unumImpl->structure));
                return FALSE;
            }
        }
    }
    return allsWell;
}

  bool
checkNeighbors(char *prefix, YT(template) *template,
               YT(presenceImpl) *pImpl, YT(unumImpl) *uImpl)
{
    YT(ingredient) *pIngredient = NULL;
    YT(mapAtt) *mapping = NULL;
    YT(neighbor) *neighbor = NULL;
    YT(neighborList) *neighbors = NULL;
    YT(presence) *presence = NULL;
    YT(presenceRole) *role = NULL;
    YT(presenceStructure) *pStruc = pImpl->structure;
    YT(template) *nTemplate = NULL;
    YT(unumStructure) *uStruc = uImpl->structure;
    bool allsWell = TRUE;
    char newPrefix[MSGLEN];

    if (template && template->ingredientImpl)
        neighbors = template->ingredientImpl->neighbors;
    else
        return FALSE;
    while(neighbors) {
        neighbor = neighbors->neighbor;
        mapping = findMapping(NEIGHBOR, neighbor->name, template);
        if (mapping) {
            if (mapping->mapTo) {
                if (neighbor->isPresence) {
                    presence = findPresence(uStruc, mapping->mapTo);
                    if (presence) {
                        role = findPresenceRole(presence->name, uImpl);
                        if (role)
                            checkPresenceStructure(prefix,
                                                   role->impl->structure,
                                                   presence->kind);
                    } else {
                        yh_error("%sno presence %s for neighbors in %s",
                                 prefix, mapping->mapTo->name, SDNAME(uStruc));
                        allsWell = FALSE;
                    }
                } else {
                    pIngredient = findIngredient(mapping->mapTo,
                                                 pStruc->ingredients);
                    if (pIngredient) {
                        nTemplate = findTemplate(pImpl, pIngredient->name);
                        if (nTemplate && nTemplate->ingredientImpl) {
                            sprintf(newPrefix, "%s\n  mapping neighbor %s to %s\n  kind %s vs ingredient impl %s:  ",
                                    prefix,
                                    mapping->mapFrom->name,
                                    mapping->mapTo->name,
                                    SDNAME(neighbor->kind),
                                    SDNAME(nTemplate->ingredientImpl));
                            checkIngredientImpl(newPrefix,
                                                nTemplate->ingredientImpl,
                                                neighbor->kind);
                        }
                    } else {
                        yh_error("%sno role for %s in %s",
                                 prefix, mapping->mapTo->name, SDNAME(pStruc));
                        allsWell = FALSE;
                    }
                }
            }
        }
        neighbors = neighbors->next;
    }
    return allsWell;
}

  bool
checkPresenceImpl(char *prefix, YT(presenceImpl) *impl,
                  YT(presenceStructure) *struc)
{
    bool allsWell = TRUE;

    if (impl && struc) {
        if (impl->attributes)
            if (!validateAttributes(prefix, impl->attributes))
                allsWell = FALSE;
        if (impl->attributes && struc->attributes)
            if (!checkRequirements(prefix, struc->attributes,
                                   impl->attributes))
                allsWell = FALSE;
/*KSSHack
        if (!checkInits(prefix, impl))
            allsWell = FALSE;
KSSHack*/
        if (!matchPresenceRoles(prefix, struc, impl))
            allsWell = FALSE;
    }

    return allsWell;
}

  bool
checkPresenceImplLinks(char *prefix, YT(presenceImpl) *impl)
{
    bool allsWell = TRUE;

    if (impl) {
        if (impl->unumImpl) {
            if (!checkTemplates(prefix, impl, impl->structure, impl->unumImpl))
                    allsWell = FALSE;
                if (!checkDeliveries(impl->structure, impl->unumImpl))
                    allsWell = FALSE;
            if (!checkMake(prefix, impl, impl->unumImpl))
                allsWell = FALSE;
        }
    } else {
        yh_error("%snull pointer for impl");
        allsWell = FALSE;
    }
    
    return allsWell;
}


  bool
checkPresenceImpls(char *prefix, YT(presenceRoleList) *roles)
{
    YT(presenceRole) *role;
    bool allsWell = TRUE;
    char newPrefix[MSGLEN];

    while (roles) {
        role = roles->presenceRole;
        if (role && role->impl) {
            sprintf(newPrefix, "%s\n  presenceImpl %s: ", prefix,
                    SDNAME(role->impl));
            checkPresenceImplLinks(newPrefix, role->impl);
        }
        roles = roles->next;
    }
    return allsWell;
}

  bool
checkPresenceRoles(char *prefix, YT(unumStructure) *struc)
{
    YT(presenceList) *presences = struc->presences;
    bool allsWell = TRUE;

    while (presences) {
        if (presences->presence->makes) {
            if (!findPresence(struc, presences->presence->makes)) {
                yh_error("%s\n  No presence role %s to make for presence %s",
                         prefix, presences->presence->makes->name,
                         SNAME(presences->presence));
                allsWell = FALSE;
            }
        }
        presences = presences->next;
    }
    return allsWell;
}

  bool
checkPresenceStructure(char *prefix, YT(presenceStructure) *struc,
                       YT(kind) *kind)
{
    YT(kindList) *extends = kind->kinds;
    bool allsWell = TRUE;
    char newPrefix[MSGLEN];

    while (extends) {
        sprintf(newPrefix, "%s\n  kind %s: ", prefix, SDNAME(extends->kind));
        if (!checkPresenceStructureKinds(newPrefix, struc, extends->kind))
            allsWell = FALSE;
        extends = extends->next;
    }
    if (!checkPresenceStructureKinds(newPrefix, struc, kind))
    allsWell = FALSE;
    if (!validateAttributes(prefix, struc->attributes))
        allsWell = FALSE;
    if (!validateRequirements(prefix, struc->attributes))
        allsWell = FALSE;
    if (!checkDuplicateDelivers(prefix, struc->ingredients))
        allsWell = FALSE;
    
    return allsWell;
}

  bool
checkPresenceStructureKinds(char *prefix, YT(presenceStructure) *struc,
                YT(kind) *kind)
{
    YT(kindList) *extends = kind->kinds;
    bool allsWell = TRUE;
    char newPrefix[MSGLEN];

    while (extends) {
        sprintf(newPrefix, "%s\n  kind %s: ", prefix, SDNAME(extends->kind));
        if (!checkPresenceStructureKinds(newPrefix, struc, extends->kind))
            allsWell = FALSE;
        extends = extends->next;
    }
    if (!checkExtendRequirements(prefix, kind->kinds, struc->attributes))
        allsWell = FALSE;
    if (!checkRequirements(prefix, kind->attributes, struc->attributes))
        allsWell = FALSE;
    
    return allsWell;
}

  bool
checkProtoDelivery(char *prefix, long scope, YT(protoDef) *proto,
                        YT(presenceStructure) *struc)
{
    YT(deliverAtt) *deliver = NULL;
    YT(ingredient) *ingredient = NULL;
    bool allsWell = TRUE;
    int num = 0;

    deliver = findDeliver(proto->name, scope, struc->ingredients,
                          &ingredient);
    if (deliver) {
        if (!checkProtoVsKind(prefix, proto, deliver->target,
                              ingredient->kind, TRUE))
            return FALSE;
    } else {
        yh_error("%snot delivered", prefix);
        allsWell = FALSE;
    }
    
    return allsWell;
}

  bool
checkParameterList(char *prefix, YT(parameterDeclList) *params)
{
    YT(parameterDecl) *param = NULL;
    bool allsWell = TRUE;
    int num = 0;
    
    while (params) {
    param = params->parameterDecl;
    num++;
    if (param->name) {
        if (findParameter(param->name, params->next)) {
        yh_error("%s%s multiply defined", prefix, SNAME(param));
        allsWell = FALSE;
        }
    } else {
        yh_error("%sunnamed parameter #%d", prefix, num);
        allsWell = FALSE;
    }
    params = params->next;
    }

    return allsWell;
}

  bool
checkProtosInImpl(char *prefix, YT(ingredientImpl) *impl, YT(kind) *kind)
     /* Check each prototype against a similarly named method or function */
{
    YT(kindList) *kinds = kind->kinds;
    YT(protoDefList) *protos = kind->protos;
    YT(function) *function = NULL;
    YT(method) *method = NULL;
    YT(methodList) *inits = NULL;
    bool allsWell = TRUE, found = FALSE;
    char newPrefix[MSGLEN];

    while (kinds) {
        sprintf(newPrefix, "%s\n  kind %s: ", prefix, SDNAME(kinds->kind));
        if (!checkProtosInImpl(newPrefix, impl, kinds->kind)) {
            allsWell = FALSE;
        }
        kinds = kinds->next;
    }
    while (protos) {
        if (protos->protoDef->name == initSym) {
            if (impl->initBlocks) {
                inits = impl->initBlocks;
                found = FALSE;
                while (inits) {
                    method = inits->method;
                    if (compareParameters(prefix, protos->protoDef->name,
                                          protos->protoDef->params,
                                          method->name, method->params,
                                          FALSE)) {
                        inits = NULL;
                        found = TRUE;
                    } else
                        inits = inits->next;
                }
                if (!found) {
                    yh_error("%s\n  no matching %s function found",
                             prefix, SNAME(protos->protoDef));
                    allsWell = FALSE;
                }
            } else {
                yh_error("%s\n  no init functions found", prefix);
                allsWell = FALSE;
            }
        } else {
            method = findMethodInImpl(protos->protoDef->name, impl);
            if (method) {
                if (!compareParameters(prefix, protos->protoDef->name,
                                       protos->protoDef->params,
                                       method->name, method->params, TRUE))
                    allsWell = FALSE;
            } else {
                function = findFunctionInImpl(protos->protoDef->name, impl);
                if (function) {
                    if (!compareParameters(prefix, protos->protoDef->name,
                                           protos->protoDef->params,
                                           function->name, function->params,
                                           TRUE))
                        allsWell = FALSE;
                } else {
                    yh_error("%s\n  no method or function %s found",
                             prefix, SNAME(protos->protoDef));
                    allsWell = FALSE;
                }
            }
        }
        protos = protos->next;
    }
    return allsWell;
}

  bool
checkProtoVsKind(char *prefix, YT(protoDef) *proto,
                 YT(symbol) *target, YT(kind) *kind, bool report)
{
    YT(protoDef) *match = NULL;

    if (target)
        match = findProtoInKind(target, kind);
    else
        match = findProtoInKind(proto->name, kind);

    if (match) {
        return compareParameters(prefix, proto->name, proto->params,
                                          match->name, match->params, TRUE);
    } else {
        if (report)
            yh_error("%sdelivered to nonexistent target %s in %s", prefix,
                     (target ? target->name : ""), SDNAME(kind));
        return FALSE;
    }
}

  bool
checkRequirements(char *prefix, YT(attributeList) *reqs,
                  YT(attributeList) *attributes)
{
    /* Check reqs ( an Impl's Kind's attribute list) against
       attributes (from an Impl) to see if they're being matched */
    YT(requireAtt) *require;
    YT(attribute) *attribute, *attNeeded;
    YT(typedValue) val;
    bool allsWell = TRUE, anyAttributes, anyOperators;

    while (reqs) {
        if (YTAG_TEST(reqs->attribute, requireAtt)) {
            require = (YT(requireAtt) *)reqs->attribute;
            val = *(require->value);
            if (val.typeCode != TV_UND) { /* plGrind didn't catch it... */
                val = evalExpr(prefix, require->expr, attributes, NULL, NULL,
                   ATTR_EXP, FALSE, &anyAttributes, &anyOperators);
                if (val.typeCode == TV_UND) {
                   yh_error("%smissing required attribute '%s'",
                             prefix, stringExpr(require->expr, NULL));
                   allsWell = FALSE;
                }
                else if ((val.typeCode == TV_BOOL) && (!YUV(val,TV_BOOL))) {
                    yh_error("%sunmet requirement '%s'",
                             prefix, stringExpr(require->expr, NULL));
                    allsWell = FALSE;
                }
            }
        } else if (YTAG_TEST(reqs->attribute, attribute)) {
            attribute = reqs->attribute;
            attNeeded = findAttributeByName(attribute->type->name->name,
                                            attributes);
            if (!attNeeded) {
                yh_error("%sno value given for '%s'",
                   prefix, PBASENAME(attribute->type));
                allsWell = FALSE;
            }
        }
        reqs = reqs->next;
    }
    return allsWell;
}

  bool
checkTemplate(char *prefix, YT(template) *template, YT(ingredientImpl) *impl)
{
    YT(mapAtt) *mapping;
    YT(neighbor) *neighbor;
    YT(neighborList) *neighbors = NULL;
    bool allsWell = TRUE;

    if (impl)
    neighbors = impl->neighbors;
    else
    return FALSE;
    while(neighbors) {
        neighbor = neighbors->neighbor;
        mapping = findMapping(NEIGHBOR, neighbor->name, template);
        if (!mapping) {
            yh_error("%sno map for neighbor %s", prefix, SNAME(neighbor));
            allsWell = FALSE;
        }
        neighbors = neighbors->next;
    }
    return allsWell;
}


  bool
checkStateBundle(char *name, YT(stateBundle) *stateBundle)
{
    bool allsWell = TRUE;
/*KSSHack
    if (!stateBundle) 
    {
        yh_error("Ingredient impl %s: missing state bundle", name);
        return FALSE;
    }
KSSHack*/
    return allsWell;
}

  bool
checkStates(char *prefix, YT(template) *template, YT(kind) *kind)
{
    YT(kindList) *extends = kind->kinds;
    bool allsWell = TRUE;
    char newPrefix[MSGLEN];

    while (extends) {
        sprintf(newPrefix, "%s\n  kind %s: ", prefix, SDNAME(extends->kind));
        if (!checkStates(newPrefix, template, extends->kind))
            allsWell = FALSE;
        extends = extends->next;
    }
    if (Verbose)
        fprintf(stderr, "  KSS: checkStates() not implemented\n");

    return allsWell;
}

  bool
checkTemplates(char *prefix, YT(presenceImpl) *impl,
               YT(presenceStructure) *pStruc, YT(unumImpl) *uImpl)
{
    YT(ingredient) *sIngredient;
    YT(ingredientList) *ingredients = pStruc->ingredients;
    YT(template) *template;
    bool allsWell = TRUE;
    char newPrefix[MSGLEN];

    while (ingredients) {
        sIngredient = ingredients->ingredient;
        template = findTemplate(impl, sIngredient->name);
        if (template) {
        sprintf(newPrefix, "%singredient %s: ", prefix,
            SNAME(sIngredient));
            if (!checkTemplate(newPrefix, template, template->ingredientImpl))
                allsWell = FALSE;
            if (!checkNeighbors(newPrefix, template, impl, uImpl))
                allsWell = FALSE;
            if (!checkStates(newPrefix, template, pStruc->kind))
                allsWell = FALSE;
        }
        ingredients = ingredients->next;
    }
    return allsWell;
}

  bool
checkUnumImpl(char *prefix, YT(unumImpl) *impl, YT(unumStructure) *struc)
{
    bool allsWell = TRUE;

    if (!validateAttributes(prefix, impl->attributes))
        allsWell = FALSE;
    if (!checkRequirements(prefix, struc->attributes, impl->attributes))
        allsWell = FALSE;
    if (!checkPresenceImpls(prefix, impl->roles))
        allsWell = FALSE;
    if (!matchUnumRoles(prefix, struc, impl))
        allsWell = FALSE;

    return allsWell;
}

  bool
checkUnumStructure(char *prefix, YT(unumStructure) *struc, YT(kind) *kind)
{
    YT(kindList) *extends = kind->kinds;
    bool allsWell = TRUE;
    char newPrefix[MSGLEN];

    while (extends) {
        sprintf(newPrefix, "%s\n  kind %s: ", prefix, SDNAME(extends->kind));
        if (!checkUnumStructure(prefix, struc, extends->kind))
            allsWell = FALSE;
        extends = extends->next;
    }
    if (!validateAttributes(prefix, struc->attributes))
        allsWell = FALSE;
    if (!validateRequirements(prefix, struc->attributes))
        allsWell = FALSE;
    if (!checkExtendRequirements(prefix, kind->kinds, struc->attributes))
        allsWell = FALSE;
    if (!checkRequirements(prefix, kind->attributes, struc->attributes))
        allsWell = FALSE;
    if (!checkPresenceRoles(prefix, struc)) 
        allsWell = FALSE;


    return allsWell;
}

  bool
compareExprsToParams(char *prefix, char *firstName, YT(exprList) *exprs,
             char *secondName, YT(parameterDeclList) *params,
             exprTypeEnum exprType, YT(presenceImpl) *presImpl,
             bool reportErrors)
{
    YT(parameterDecl) *param = NULL;
    YT(typedValue) val;
    bool allsWell = TRUE, anyAttributes, anyOperators;
    int num = 0;

    if (YCOUNT(exprs) > YCOUNT(params)) {
    if (reportErrors)
        yh_error("%s\n  too many init parameters in %s for impl %s",
             prefix, firstName, secondName);
    return FALSE;
    } else if (YCOUNT(exprs) < YCOUNT(params)) {
    if (reportErrors)
        yh_error("%s\n  too few init parameters in %s for impl %s",
             prefix, firstName, secondName);
    return FALSE;
    }
    while (exprs) {
    num++;
    param = params->parameterDecl;
    val = evalExpr(prefix, exprs->expr, NULL, presImpl->initBlocks,
               presImpl->roles, exprType, reportErrors,
               &anyAttributes, &anyOperators);
    if (!typeEqual(val, param)) {
        if (reportErrors)
        yh_error("%s\n  %s vs impl %s: type mismatch on parameter %d",
             prefix, firstName, secondName, num);
        allsWell = FALSE;
    }
    if (val.typeCode == TV_UND)
        allsWell = FALSE;
    params = params->next; exprs = exprs->next;
    }
    return allsWell;
}

  bool
compareParameters(char *prefix, YT(symbol) *firstName,
                  YT(parameterDeclList) *first,
                  YT(symbol) *secondName, YT(parameterDeclList) *second,
                  bool report)
{
    bool allsWell = TRUE, namesMatch = (firstName == secondName);
    int paramNum = 0;

    if (YCOUNT(first) > YCOUNT(second)) {
    if (report)
        yh_error("%s%s: too many parameters %s%s", prefix,
             firstName->name, namesMatch ? "" : " for ",
             namesMatch ? "" : secondName->name);
    return  FALSE;
    } else if (YCOUNT(first) < YCOUNT(second)) {
        if (report)
            yh_error("%s%s: too few parameters %s%s", prefix, firstName->name,
                     namesMatch ? "" : " for ",
                     namesMatch ? "" : secondName->name);
        return FALSE;
    }
    while (first) {
    paramNum ++;
    if(!compareTypes(YC(type,first->parameterDecl->type),
             YC(type,second->parameterDecl->type))) {
        if (report)
        yh_error("%s%s: parameter %d does not match %s%s",
             prefix, firstName->name, paramNum,
             namesMatch ? "" : secondName->name,
             namesMatch ? "" : "'s");
        allsWell = FALSE;
    }
    first = first->next; second = second->next;
    }
    return allsWell;
}

  bool
compareProtosInKinds(char *prefix, YT(kind) *kind1, YT(kind) *kind2)
{
    YT(kindList) *kinds1 = kind1->kinds;
    YT(protoDef) *proto1 = NULL;
    YT(protoDef) *proto2 = NULL;
    YT(protoDefList) *protos1 = kind1->protos;
    bool allsWell = TRUE;
    char newPrefix[MSGLEN];

    if (kind1->name == kind2->name)
    return TRUE;

    sprintf(newPrefix, "%s\n  kind %s: ", prefix, SDNAME(kind1));

    while (kinds1) {
        if (!compareProtosInKinds(newPrefix, kinds1->kind, kind2))
            allsWell = FALSE;
        kinds1 = kinds1->next;
    }
    while (protos1) {
        proto1 = protos1->protoDef;
        proto2 = findProtoInKind(proto1->name, kind2);
        if (proto2) {
            if (!compareParameters(prefix, proto1->name, proto1->params,
                                   proto2->name, proto2->params, TRUE))
                allsWell = FALSE;
        } else {
        yh_error("%s\n  no prototype %s found in %s",
             prefix, SNAME(proto1), SDNAME(kind2));
        allsWell = FALSE;
        }
        protos1 = protos1->next;
    }

    return allsWell;
}

  bool
compareTypes(YT(type) *type1, YT(type) *type2)
{
    switch (YTAG_OF(type1)) {
        case YTAG(defType):
            return (YTAG_OF(type2) == YTAG(defType) &&
                    compareTypes(YC(defType,type1)->type,
                                 YC(defType,type2)->type));
        case YTAG(enumType):
            yh_error("<enum not allowed as typeSpec>");
            return FALSE;
    case YTAG(pluribusType):
            return (YTAG_OF(type2) == YTAG(pluribusType) &&
                    YC(pluribusType,type1)->mangle ==
                    YC(pluribusType,type2)->mangle &&
                    compareTypes(
                        YC(type,YC(pluribusType,type1)->type),
                        YC(type,YC(pluribusType,type2)->type)));
        case YTAG(primType):
            return (YC(primType,type1)->type == YC(primType,type2)->type);
        case YTAG(sequenceType):
            return (YTAG_OF(type2) == YTAG(sequenceType) &&
                    compareTypes(YC(sequenceType,type1)->type,
                                 YC(sequenceType,type2)->type));
        case YTAG(stringType):
            return (YTAG_OF(type2) == YTAG(stringType));
        case YTAG(structType):
            yh_error("<struct not allowed as typeSpec>");
            return FALSE;
        case YTAG(undefinedType):
            return (YTAG_OF(type2) == YTAG(undefinedType) &&
                    (YC(undefinedType,type1)->type->name ==
                     YC(undefinedType,type2)->type->name));
        case YTAG(unionType):
            yh_error("<union not allowed as typeSpec>");
            return FALSE;
        case YTAG(symbolRef):
            return (YTAG_OF(type2) == YTAG(symbolRef) &&
                    (YC(symbolRef,type1)->name ==
                     YC(symbolRef,type2)->name));
        default:
           yh_error("invalid type code %d in compareTypes()",
                    YTAG_OF(type1));
    }
    return FALSE;
}

  bool
matchAllProtosInKind(char *prefix, YT(kind) *source,
                     YT(ingredient) *ingredient)
{
    YT(kind) *target = ingredient->kind;
    YT(protoDef) *proto = NULL;
    YT(protoDefList) *protos = source->protos;
    YT(kindList) *kinds = source->kinds;
    bool allsWell = TRUE;
    char newPrefix[MSGLEN];
    int num = 0;

    while (kinds) {
        sprintf(newPrefix, "%s\n  kind %s in %s: ", prefix,
                SDNAME(kinds->kind), SNAME(ingredient));
        if (!matchAllProtosInKind(newPrefix, kinds->kind, ingredient))
            allsWell = FALSE;
        kinds = kinds->next;
    }

    while (protos) {
        proto = protos->protoDef;
        sprintf(newPrefix, "%s\n  method %s in %s: ", prefix,
                SNAME(proto), SNAME(ingredient));
        if (!checkProtoVsKind(newPrefix, proto, proto->name, target, TRUE))
            allsWell = FALSE;
        protos = protos->next;
    }

    return allsWell;
}

  bool
matchPresenceRoles(char *prefix, YT(presenceStructure) *struc,
                   YT(presenceImpl) *impl)
{
    YT(ingredientList) *ingredients = struc->ingredients;
    YT(ingredient) *ingredient;
    YT(ingredientRoleList) *roles = impl->roles;
    YT(symbolList) *roleNames;
    YT(symbol) *role;
    YT(template) *template;
    bool allsWell = TRUE;
    char newPrefix[MSGLEN], temp[MSGLEN];

    /*  Check the ingredient roles... */
    while (roles) {
        roleNames = roles->ingredientRole->ingredients;
        /*  Make sure all the roles mentioned in the impl actually appear in
            the struc... */
        while (roleNames) {
            role = roleNames->symbol;
            ingredient = findIngredient(role, struc->ingredients);
            if (!ingredient) {
                yh_error("%sno role %s in %s", prefix, role->name,
                         SDNAME(struc));
                allsWell = FALSE;
            }
            roleNames = roleNames->next;
        }
        roles = roles->next;
    }

    /*  Make sure all the ingredients mentioned in the struc have templates
        in the impl... */
    while(ingredients) {
        ingredient = ingredients->ingredient;
        template = findTemplate(impl, ingredient->name);
        if (template) {
            if (template->ingredientImpl) {
                sprintf(temp, "template for ingredient role %s",
                        SNAME(ingredient));
                sprintf(newPrefix,
                        "%s%s:\n  kind %s vs ingredient impl %s:",
                        prefix, temp, 
                        SDNAME(ingredient->kind),
                        SDNAME(template->ingredientImpl));
                if (!checkIngredientImpl(newPrefix, template->ingredientImpl,
                                         ingredient->kind))
                    allsWell = FALSE;
            }
        } else {
            yh_error("%sno template assigned for %s from %s",
                     prefix, SNAME(ingredient), SDNAME(struc));
            allsWell = FALSE;
        }
        ingredients = ingredients->next;
    }

    return allsWell;
}

  bool
matchUnumRoles(char *prefix, YT(unumStructure) *struc, YT(unumImpl) *impl)
{
    YT(presenceList) *presences = struc->presences;
    YT(presence) *presence = NULL;
    YT(presenceRoleList) *roles = impl->roles;
    YT(presenceRole) *role = NULL, *makeRole = NULL;
    YT(symbolList) *roleNames = NULL;
    bool allsWell = TRUE;

    while (presences) {
        presence = presences->presence;
        role = findPresenceRole(presence->name, impl);
        if (role) {
        if (presence->makes) {
        if (!role->impl->makeAtt) {
            yh_error("%s\n  %s in %s makes, but %s has no make attribute",
                 prefix, SNAME(presence),
                 SDNAME(struc), SDNAME(role->impl));
            allsWell = FALSE;
        }
        }
    } else {
            yh_error("%s%s from %s does not have a role",
                     prefix, SNAME(presence), SDNAME(struc));
            allsWell = FALSE;
        }
        presences = presences->next;
    }

    while (roles) {
        roleNames = roles->presenceRole->presences;
        while (roleNames) {
            presence = findPresence(struc, roleNames->symbol);
            if (!presence) {
                yh_error("%sno %s in %s",
                         prefix, roleNames->symbol->name,
                         SDNAME(struc));
                allsWell = FALSE;
            }
            roleNames = roleNames->next;
        }
        roles = roles->next;
    }
    
    return allsWell;
}

/* Set up for possible error messages */
  char *
neighborErrMessage(char *templateName, YT(symbolList) *ingredients)
{
    char *errPrefix = "for ingredient ";
    char *ingredientName, *errMessage;

    if (isGenerated(templateName)) {
        if (ingredients)
            ingredientName = ingredients->symbol->name;
        else
            ingredientName = strdup("*UNKNOWN*");
        errMessage = (char *)malloc(strlen(errPrefix) +
                                 strlen(ingredientName));
        sprintf(errMessage, "%s%s", errPrefix, ingredientName);
    } else
        errMessage = strdup(templateName);
    return errMessage;
}

  bool
validateAttributes(char *prefix, YT(attributeList) *attributes)
{
    /* Checks that attributes (from Impls and Structures) are defined and
       have expressions if they are not tags */
    YT(attribute) *attr;
    YT(typedValue) val;

    bool allsWell = TRUE;
    while (attributes) {
        attr = attributes->attribute;
        if (YTAG_TEST(attr, attribute)) {
            val = *(attr->worth);
            if (val.typeCode == TV_UND) {
                yh_error("%sundefined attribute '%s' (%s)",
                         prefix, PBASENAME(attr->type),
                         stringExpr(attr->value, NULL));
                allsWell = FALSE;
            } else if (val.typeCode == TV_DEP) {
                yh_error("%sdependent attribute '%s'",
                         prefix, PBASENAME(attr->type));
                allsWell = FALSE;
            } else if ((val.typeCode == TV_TAG) &&
                       (typeOf(attr->type->type) != TagType)) {
                yh_error("%s%s is not a tag and needs a value",
                         prefix, PBASENAME(attr->type));
                attr->worth = UND_VAR;
                allsWell = FALSE;
            } else if (!equal(val.typeCode, typeOf(attr->type->type))) {
                yh_error("%stype mismatch (%s != %s) '%s = %s'",
                         prefix, typeToString(typeOf(attr->type->type)),
                         typeCodeToString(val),
                         PBASENAME(attr->type),
                         stringExpr(attr->value, NULL));
                attr->worth = UND_VAR;
                allsWell = FALSE;
            }
        }
        attributes = attributes->next;
    }
    return allsWell;
}

/* Do the checks on require here; requireAtts are not submitted to evalExpr()
   in plGrind, unlike attributeRefs.  NOTE that we call evalExpr() here,
   which depends on scoping to see if attributes are defined or not; when
   called from plscope.c's defineXXX() routines, which are called from
   plGrind(), the scoping is set up properly--Calling validateRequirements()
   from other places may be a bad idea... */
  bool
validateRequirements(char *prefix, YT(attributeList) *requirements)
{
    YT(requireAtt) *req;
    YT(typedValue) val;
    bool allsWell = TRUE, anyAttributes, anyOperators;

    while (requirements) {
        req = (YT(requireAtt)*)(requirements->attribute);
        if (YTAG_TEST(req, requireAtt)) {
            val = evalExpr(prefix, req->expr, NULL, NULL, NULL, REQ_EXP, TRUE,
               &anyAttributes, &anyOperators);
            req->value = UND_VAR;
            *(req->value) = val;
            if (val.typeCode != TV_UND && !anyAttributes) {
                yh_error("%s'require %s' needs an attribute",
                         prefix, stringExpr(req->expr, NULL));
                allsWell = FALSE;
            }
            else {
                switch (val.typeCode) {
                case TV_UND: 
                case TV_TAG:
                case TV_BOOL: break;
                default:
                    if (anyOperators) {
                        yh_error(
                            "%s'require %s' is not a boolean expression",
                            prefix, stringExpr(req->expr, NULL));
                        allsWell = FALSE;
                    } else {
                        yh_error("%s'require %s' is not a tag or boolean and needs a value",
                                 prefix, stringExpr(req->expr,
                                                    NULL));
                        allsWell = FALSE;
                    }
                }
            }
        }
        requirements = requirements->next;
    }
    return allsWell;
}

