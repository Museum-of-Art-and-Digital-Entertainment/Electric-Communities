/*
  plunithab.c -- Unit descriptor output for the Haberdashery-based Pluribus.

  Chip Morningstar
  Electric Communities
  3-March-1997

  Copyright 1996, 1997 Electric Communities, all rights reserved.

*/

#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "yh_walk.h"
#include "y.tab.h"
#include "pl.h"
#include <time.h>

static void generateProtoDescriptor(YT(symbol) *name,
                                    YT(parameterDeclList) *params,
                    bool includeNames);
static void generateTemplateDescriptor(char *prefix, YT(template) *template,
                                    YT(presenceImpl) *impl);

  static void
generateAttributeDescriptors(YT(attributeList) *attributes)
{
    if (attributes) {
        P("@a %d", YCOUNT(attributes));
        while (attributes) {
            if (YTAG_TEST(attributes->attribute, attribute)) {
                YT(attribute) *attribute = attributes->attribute;
                P(" %s", PBASENAME(attribute->type));
                P(" %s", typedValueToString(*(attribute->worth)));
            } else {
                YT(requireAtt) *require=(YT(requireAtt)*)attributes->attribute;
                //KSS printf("KSS: in plunit.c:generateAttributeDescriptors()\n"); 
                P(" %s",
                  "//KSS Put 'require' code in generateAttributeDescriptors()");
            }
            attributes = attributes->next;
        }
    }
}

  static void
generateAttributeTypeDescriptor(YT(attributeType) *attribute)
{
    PP("@t ");
    generateTypeSignature(attribute->type, FALSE);
    P("");
}

  static bool
generateEntityDescriptorPrefix(YT(binding) *binding)
{
    bool isImported = FALSE;

    if (YTAG_TEST(binding->value,unit))
        isImported = YC(unit,binding->value)->isImported;
    if (YTAG_TEST(binding->value->name,symbolRef)) {
        P("%c%c%s %s", isImported ? '>' : ':', binding->isExport ? '+' : '-',
          bindingTypeToTag(binding->value->bindingType), PBASENAME(binding));
        return(TRUE);
    } else {
        return(FALSE);
    }
}

  static void
generateIngredientImplDescriptor(YT(ingredientImpl) *impl)
{
    YT(functionList) *functions = impl->functions;
    YT(implementsAttList) *implements = impl->implements;
    YT(methodList) *initBlocks = impl->initBlocks;
    YT(method) *forall = impl->forall;
    YT(methodList) *methods = impl->methods;
    YT(neighborList) *neighbors = impl->neighbors;
    YT(stateBundleList) *stateBundles = impl->stateBundles;
    YT(variable) *var = NULL;
    YT(variableList) *vars = impl->vars;
    int count = 0;

    P("@k %s", PBASENAME(impl->kind));

    /* Attributes */
    generateAttributeDescriptors(impl->attributes);

    /* Neighbors */
    if (neighbors) {
        P("@n %d", YCOUNT(neighbors));
        while (neighbors) {
            YT(neighbor) *neighbor = neighbors->neighbor;
            P(" %s %s %d %d", SNAME(neighbor), PBASENAME(neighbor->kind),
              neighbor->isPlural, neighbor->isPresence);
            neighbors = neighbors->next;
        }
    }

    /* State bundles */
    if (stateBundles) {
        P("@s %d", YCOUNT(stateBundles));
        while (stateBundles) {
            if (stateBundles->stateBundle->name)
                P(" %s", SNAME(stateBundles->stateBundle));
            else
                P(" *");
            stateBundles = stateBundles->next;
        }
    }

    /* Instance variables */
    if (vars) {
        while (vars) {
            if (vars->variable->isExport)
                count++;
            vars = vars->next;
        }
        if (count > 0) {
            P("@v %d", count);
            vars = impl->vars;
            while (vars) {
                var = vars->variable;
                if (var->isExport) {
                    PP(" ");
                    generateTypeSignature(YC(type,var->type), var->isArray);
                    P(" %s", SNAME(var));
                }
                vars = vars->next;
            }
        }
    }

    /* functions */
    if (functions) {
        P("@f %d", YCOUNT(functions));
        while (functions) {
//KSSHack what about the MOD and TYP portions
            YT(function) *function = functions->function;
            generateProtoDescriptor(function->name, function->params, FALSE);
            functions = functions->next;
        }
    }

    /* initBlocks */
    if (initBlocks) {
    P("@I %d", YCOUNT(initBlocks));
    while (initBlocks) {
        generateProtoDescriptor(initSym, initBlocks->method->params,
                    FALSE);
        initBlocks = initBlocks->next;
    }
    }

    /* forall */
    if (forall) {
        P("@F 1");
        generateProtoDescriptor(NULL, forall->params, FALSE);
    }

    /* methods */
    if (methods) {
        P("@m %d", YCOUNT(methods));
        while (methods) {
            YT(method) *method = methods->method;
            generateProtoDescriptor(method->name, method->params, FALSE);
            methods = methods->next;
        }
    }

    /* implements */
    if (implements) {
        P("@z %d", YCOUNT(implements));
        while (implements) {
            YT(implementsAtt) *implement = implements->implementsAtt;
            P(" %s", SDNAME(implement));
            implements = implements->next;
        }
    }
}

  static void
generateKindDescriptor(YT(kind) *kind)
{
    YT(stateBundleList) *stateBundles = kind->stateBundles;
    YT(protoDefList) *protos = kind->protos;
    YT(kindList) *extends = kind->kinds;

    /* Protos */
    if (protos) {
        P("@p %d", YCOUNT(protos));
        while (protos) {
            generateProtoDescriptor(protos->protoDef->name,
                                    protos->protoDef->params, FALSE);
            protos = protos->next;
        }
    }
    /* State bundles */
    if (stateBundles) {
        P("@s %d", YCOUNT(stateBundles));
        while (stateBundles) {
            if (stateBundles->stateBundle->name)
                P(" %s", SNAME(stateBundles->stateBundle));
            else
                P(" *");
            stateBundles = stateBundles->next;
        }
    }

    /* Extends */
    if (extends) {
        P("@e %d", YCOUNT(extends));
        while (extends) {
            P(" %s", PBASENAME(extends->kind));
            extends = extends->next;
        }
    }
    generateAttributeDescriptors(kind->attributes);
}

  static void
generateParameterListDescriptor(YT(parameterDeclList) *params,
                bool includeNames)
{
    YT(parameterDecl) *param = NULL;
    while (params) {
        param = params->parameterDecl;
        generateTypeSignature(YC(type,param->type), param->isArray);
        if (includeNames) {
            PP("%s%s", SNAME(param), params->next ? "," : "");
        }
        params = params->next;
    }
}

   static void
generatePresenceImplDescriptor(YT(presenceImpl) *presImpl)
{
    YT(exprList) *inits = NULL;
    YT(ingredientRoleList) *roles = presImpl->roles;
    YT(ingredientRole) *role = NULL;
    YT(methodList) *initBlocks = NULL;
    YT(symbolList) *ingrs = NULL;
    char prefix[MSGLEN];

    P("@s %s", PBASENAME(presImpl->structure));

    /* Attributes */
    generateAttributeDescriptors(presImpl->attributes);

    /* primeInitBlocks */
    initBlocks = presImpl->primeInitBlocks;
    if (initBlocks) {
        P("@P %d", YCOUNT(initBlocks));
        while (initBlocks) {
        generateProtoDescriptor(initSym, initBlocks->method->params, TRUE);
        P(" %d", numCharIn('\n', initBlocks->method->methodCode)+1);
        P(" %s", initBlocks->method->methodCode);
        initBlocks = initBlocks->next;
    }
    }

    /* initBlocks */
    initBlocks = presImpl->initBlocks;
    if (initBlocks) {
        P("@I %d", YCOUNT(initBlocks));
        while (initBlocks) {
        generateProtoDescriptor(initSym, initBlocks->method->params, TRUE);
        P(" %d", numCharIn('\n', initBlocks->method->methodCode)+1);
        P(" %s", initBlocks->method->methodCode);
        initBlocks = initBlocks->next;
    }
    }

    /* Roles */
    if (roles) {
        P("@r %d", YCOUNT(roles));
        while (roles) {
            role = roles->ingredientRole;
            ingrs = role->ingredients;
        sprintf(prefix, "presence impl %s: ingredient %s: ",
            SDNAME(presImpl), ingrs->symbol->name);
            PP(" %d", YCOUNT(ingrs));
            while (ingrs) {
                PP(" %s", ingrs->symbol->name);
                ingrs = ingrs->next;
            }
            generateTemplateDescriptor(prefix, role->template, presImpl);
            roles = roles->next;
        }
    }

    /* makeAtt */
    if (presImpl->makeAtt) {
    sprintf(prefix, "presence impl %s: make %s: ",
        SDNAME(presImpl), SNAME(presImpl->makeAtt));
        P("@m");
    P(" %s %d",SNAME(presImpl->makeAtt), YCOUNT(presImpl->makeAtt->inits));
        inits = presImpl->makeAtt->inits;
        while (inits) {
            P(" %s", stringFromExpr(prefix, PREFIX, MAKE_EXP, inits->expr,
                    presImpl, TRUE, FALSE));
            inits = inits->next;
        }
    }
}

   static void
generatePresenceStructureDescriptor(YT(presenceStructure) *struc)
{
    YT(ingredientList) *ingredients = struc->ingredients;
    YT(deliverAttList) *deliverAtts;

    /* Attributes */
    generateAttributeDescriptors(struc->attributes);

    /* Presence Kind */
    P("@k %s", PBASENAME(struc->kind));

    /* Ingredients */
    if (ingredients) {
        P("@i %d", YCOUNT(ingredients));
        while (ingredients) {
            deliverAtts = ingredients->ingredient->deliverAtts;
            P(" %s %s %d", SNAME(ingredients->ingredient),
              PBASENAME(ingredients->ingredient->kind),
              deliverAtts ? YCOUNT(deliverAtts) : 0);

            /* DeliverAtts */
            if (deliverAtts) {
                while (deliverAtts) {
                    YT(deliverAtt) *deliverAtt = deliverAtts->deliverAtt;
                    if (YTAG_TEST(deliverAtt,deliverDefaultAtt)) {
                        YT(deliverDefaultAtt) *deliverDefaultAtt =
                            YC(deliverDefaultAtt,deliverAtt);
                        P(" %d * *", deliverDefaultAtt->scope == PRESENCE);
                    } else {
                        P(" %d %s %s",
                          deliverAtt->scope == PRESENCE,
                          deliverAtt->source->name,
                          deliverAtt->target ? deliverAtt->target->name : "*");
                    }
                    deliverAtts = deliverAtts->next;
                }
            }
            ingredients = ingredients->next;
        }
    }

}

  static void
generateProtoDescriptor(YT(symbol) *name, YT(parameterDeclList) *params,
            bool includeNames)
{
    PP(" %s(", name ? name->name : "*");
    generateParameterListDescriptor(params, includeNames);
    P(")");
}

  static void
generateScopeName(YT(scope) *scope)
{
    char buf[BUFLEN];

    expandScopeName(scope, buf, ".");
    PP("%s", buf);
}

  static void
generateTemplateDescriptor(char *prefix, YT(template) *template,
               YT(presenceImpl) *impl)
{
    YT(exprList) *exprs = NULL;
    YT(mapAttList) *mapAtts = template->mapAtts;
    YT(mapAtt) *mapAtt;

    P(" %s %d",
      template->ingredientImpl ? PBASENAME(template->ingredientImpl) :
                                 "#MissingTemplateImpl#",
      mapAtts ? YCOUNT(mapAtts) : 0);

    /* MapAtts */
    if (mapAtts) {
        while (mapAtts) {
            mapAtt = mapAtts->mapAtt;
            if (YTAG_TEST(mapAtt,mapAtt)) {
                if (mapAtt->mapTo) {
                    P(" %d %s %s", mapAtt->scope == NEIGHBOR,
                      mapAtt->mapFrom->name, mapAtt->mapTo->name);
                }
                else {
                    P(" %d %s null", mapAtt->scope == NEIGHBOR,
                      mapAtt->mapFrom->name);
                }
            } else {
                YT(mapDefaultAtt) *mapDefaultAtt = YC(mapDefaultAtt,mapAtt);
                P(" %d * *", mapDefaultAtt->scope == NEIGHBOR);
            }
            mapAtts = mapAtts->next;
        }
    }

    /* StateInitAtts */
    /* TODO */
}

  static void
generateUnitImportDescriptor(YT(unit) *unit)
{
    P("@f %s", unit->filePath);
}

  static void
generateUnumImplDescriptor(YT(unumImpl) *impl)
{
    YT(presenceRoleList) *roles = impl->roles;
    YT(presenceImpl) *primePresence = NULL;

    P("@s %s", PBASENAME(impl->structure));

    /* Attributes */
    generateAttributeDescriptors(impl->attributes);

    /* Roles */
    if (roles) {
        int count = 0;
        while (roles) {
            YT(symbolList) *presences = roles->presenceRole->presences;
            while (presences) {
                ++count;
                presences = presences->next;
            }
            roles = roles->next;
        }
        P("@r %d", count);
        roles = impl->roles;
        while (roles) {
            YT(presenceRole) *role = roles->presenceRole;
            YT(symbolList) *presences = role->presences;
            if (!primePresence)
                primePresence = role->impl;
            while (presences) {
                P(" %s %s", presences->symbol->name, PBASENAME(role->impl));
                presences = presences->next;
            }
            roles = roles->next;
        }
    }
}

  static void
generateUnumStructureDescriptor(YT(unumStructure) *struc)
{
    YT(presence) *presence = NULL;
    YT(presenceCond) *cond = NULL;
    YT(presenceCondList) *conds = NULL;
    YT(presenceList) *presences = struc->presences;
    YT(symbol) *prime = struc->prime;
    char prefix[MSGLEN];
    int num = 0;

    /* Attributes */
    generateAttributeDescriptors(struc->attributes);

    /* UnumKind */
    P("@k %s", PBASENAME(struc->kind));

    /* Presences */
    P("@p %d", YCOUNT(presences));
    while (presences) {
        presence = presences->presence;
        P(" %s %s %s %d %d", SNAME(presence), PBASENAME(presence->presence),
          presence->makes ? presence->makes->name : "*",
          presence->name == prime, YCOUNT(presence->conditionals));
    conds = presence->conditionals;
    num = 0;
    while (conds) {
        cond = conds->presenceCond;
        num++;
        sprintf(prefix, "unum structure %s: presence %s condition %d:\n  ",
            SDNAME(struc), SNAME(presence), num);
        P(" %s %s", cond->makes->name,
          stringFromExpr(prefix, PREFIX, COND_EXP, cond->expr, NULL, TRUE,
                 FALSE));
        conds = conds->next;
    }
        presences = presences->next;
    }
}

  void
generateUnitDescriptorObjects(YT(unit) *unit, char *inputFileName)
{
    YT(nameSpaceList) *nameSpaces = unit->scope->nameSpaces;
    long timeNow = time(NULL);
    struct tm *timeInfo;
    char buf[BUFLEN];

    P(";Produced by %s", VersionString);
    strftime(buf, BUFLEN, "%d-%B-%Y %T", localtime(&timeNow));
    P(";from %s on %s", inputFileName, buf);
    P(";This file is machine generated. Don't edit it or you'll be sorry.");
    PP("=%cun ", (unit->export == EXPORT) ? '+' : '-');
    generateScopeName(unit->scope->outer);
    P("%s;", PBASENAME(unit));
    while (nameSpaces) {
        YT(bindingList) *bindings = nameSpaces->nameSpace->bindings;
        while (bindings) {
            YT(binding) *binding = bindings->binding;
            switch (nameSpaces->nameSpace->bindingType) {
                case BIND_TYPE:
#if 0
                    /* TODO */
                    P(";type");
#endif
                    break;
                case BIND_ATTRIBUTE:
                    putAttributeTypeDescriptor(unit,
                        YC(attributeType,binding->value));
                    break;
                case BIND_CLASS:
                case BIND_INTERFACE:
                case BIND_ECLASS:
                case BIND_EINTERFACE:
                    break;
                case BIND_INGREDIENT_IMPL:
#if 0
                    generateEntityDescriptorPrefix(binding);
                    generateIngredientImplDescriptor(YC(ingredientImpl,binding->value));
#endif
                    break;
                case BIND_KIND:
                    putKindDescriptor(unit, YC(kind,binding->value));
                    break;
                case BIND_PRESENCE_IMPL:
#if 0
                    generateEntityDescriptorPrefix(binding);
                    generatePresenceImplDescriptor(YC(presenceImpl,binding->value));
#endif
                    break;
                case BIND_PRESENCE_STRUCTURE:
                    putPresenceStructureDescriptor(
                        YC(presenceStructure,binding->value));
                    break;
                case BIND_UNIT:
#if 0
                    generateEntityDescriptorPrefix(binding);
                    generateUnitImportDescriptor(YC(unit,binding->value));
#endif
                    break;
                case BIND_UNUM_IMPL:
#if 0
                    generateEntityDescriptorPrefix(binding);
                    generateUnumImplDescriptor(YC(unumImpl,binding->value));
#endif
                    break;
                case BIND_UNUM_STRUCTURE:
#if 0
                    generateEntityDescriptorPrefix(binding);
                    generateUnumStructureDescriptor(YC(unumStructure,binding->value));
#endif
                    break;
                default:
                    yh_error("illegal binding type %d",
                             nameSpaces->nameSpace->bindingType);
            }
            bindings = bindings->next;
        }
        nameSpaces = nameSpaces->next;
    }
}
