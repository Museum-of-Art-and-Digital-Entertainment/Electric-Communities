/*
  plunit.c -- Unit descriptor output for Pluribus.

  Chip Morningstar
  Electric Communities
  21-August-1996

  Copyright 1996 Electric Communities, all rights reserved.

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
                                    bool includeNames,
                                    YT(scopedRefList) *throws,
                                    long modifiers, YT(type) *resultType);
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
    generateTypeSignature(attribute->type, 0);
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
    YT(implementsAttList) *implements = impl->implements;
    YT(functionList) *functions = impl->functions;
    YT(methodList) *initBlocks = impl->initBlocks;
    YT(methodList) *methods = impl->methods;
    YT(neighborList) *neighbors = impl->neighbors;
    YT(stateBundle) *stateBundle = impl->stateBundle;
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
    if (stateBundle) {
        P("@s %s %s %s",
          stateBundle->packagename? stateBundle->packagename->name : "*",
          stateBundle->typename->name, SNAME(stateBundle));
    }

    /* functions */
    if (functions) {
        P("@f %d", YCOUNT(functions));
        while (functions) {
            YT(function) *function = functions->function;
            generateProtoDescriptor(function->name, function->params, FALSE,
                    NULL, function->modifiers,
                    function->resultType);
            functions = functions->next;
        }
    }

    /* initBlocks */
    if (initBlocks) {
        P("@I %d", YCOUNT(initBlocks));
        while (initBlocks) {
            generateProtoDescriptor(initSym, initBlocks->method->params, FALSE,
                                    initBlocks->method->throws, 0, NULL);
            initBlocks = initBlocks->next;
        }
    }

    /* methods */
    if (methods) {
        P("@m %d", YCOUNT(methods));
        while (methods) {
            YT(method) *method = methods->method;
            generateProtoDescriptor(method->name, method->params, FALSE,
                                    method->throws, 0, NULL);
            methods = methods->next;
        }
    }

    /* implements */
    if (implements) {
        P("@z %d", YCOUNT(implements));
        while (implements) {
            YT(implementsAtt) *implement = implements->implementsAtt;
            P(" %s", pSymbolRef(implement->name));
            implements = implements->next;
        }
    }
}

  static void
generateKindDescriptor(YT(kind) *kind)
{
    YT(protoDefList) *protos = kind->protos;
    YT(kindList) *extends = kind->kinds;
    YT(implementsAttList) *implements = kind->implements;

    /* Protos */
    if (protos) {
        P("@p %d", YCOUNT(protos));
        while (protos) {
            generateProtoDescriptor(protos->protoDef->name,
                                    protos->protoDef->params, FALSE,
                                    protos->protoDef->throws, 0, NULL);
            protos = protos->next;
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

    /* Implements */
    if (implements) {
        P("@i %d", YCOUNT(implements));
        while (implements) {
            P(" %s", pSymbolRef(implements->implementsAtt->name));
            implements = implements->next;
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
        generateTypeSignature(YC(type,param->type), param->dimensions);
        if (includeNames)
            PP("%s%s", param->name ? SNAME(param) : "UNNAMED PARAMETER",
               params->next ? "," : "");
        params = params->next;
    }
}

   static void
generatePresenceImplDescriptor(YT(presenceImpl) *presImpl)
{
    YT(symbolList) *inits = NULL;
    YT(ingredientRoleList) *roles = presImpl->roles;
    YT(ingredientRole) *role = NULL;
    YT(methodList) *initBlocks = NULL;
    YT(symbolList) *ingrs = NULL;
    YT(symbolList) *behaviors = NULL;
    char prefix[MSGLEN];

    P("@s %s", PBASENAME(presImpl->structure));

    /* Attributes */
    generateAttributeDescriptors(presImpl->attributes);

    /* primeInitBlocks */
    initBlocks = presImpl->primeInitBlocks;
    if (initBlocks) {
        P("@P %d", YCOUNT(initBlocks));
        while (initBlocks) {
        generateProtoDescriptor(initSym, initBlocks->method->params,
                    TRUE, initBlocks->method->throws, 0, NULL);
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
        generateProtoDescriptor(initSym, initBlocks->method->params,
                    TRUE, initBlocks->method->throws, 0, NULL);
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
        inits = presImpl->makeAtt->inits;
        P("@m %d %s", YCOUNT(inits), SNAME(presImpl->makeAtt));
        while (inits) {
            PP(" %s", inits->symbol->name);
            inits = inits->next;
        }
        P("");
    }

    /* Behaviors */
    if (presImpl->behavior) {
    behaviors = presImpl->behavior->behaviors;
    sprintf(prefix, "presence impl %s: behaviors: ", SDNAME(presImpl));
        P("@b %d", YCOUNT(behaviors));
        while (behaviors) {
            P(" %s", behaviors->symbol->name);
            behaviors = behaviors->next;
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
                    if (YTAG_TEST(deliverAtt,deliverAtt)) {
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
            bool includeNames, YT(scopedRefList) *throws,
            long modifiers, YT(type) *resultType)
{
    char buf[BUFLEN];
    int count = 0;

    buf[0] = '\0';
    if (throws) {
        count = YCOUNT(throws);
    }
    PP(" %s(", name ? name->name : "*");
    generateParameterListDescriptor(params, includeNames);
    PP(") %d", count);
    while (throws) {
        buf[0]='\0';
        symbolRefString(buf, (YT(symbolRef) *)(throws->scopedRef));
        PP(" %s", buf);
        throws = throws->next;
    }
    if (resultType) {
        PP(" %d ", modifiers);
        generateTypeSignature(resultType, 0); //KSSHack Arrays as returns?
    }
    P("");
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
        P(" %s %s %s %d %d", SNAME(presence), PBASENAME(presence->kind),
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

  char *
bindingToString(long type)
{
    switch (type) {
    case BIND_TYPE: return "type";
    case BIND_ATTRIBUTE: return "attribute";
    case BIND_KIND: return "kind";
    case BIND_INGREDIENT_IMPL: return "ingredientImpl";
    case BIND_PRESENCE_STRUCTURE: return "presenceStructure";
    case BIND_UNUM_STRUCTURE: return "unumStructure";
    case BIND_PRESENCE_IMPL: return "presenceImpl";
    case BIND_UNUM_IMPL: return "unumImpl";
    case BIND_UNIT: return "unit";
    case BIND_LIMIT: return "limit";
    default: return "** UNKNOWN BINDING TYPE **";
    }
}

  void
generateElementtDescriptors(YT(unit) *unit, char *inputFileName,
                long bindingType)
{
    YT(binding) *binding = NULL;
    YT(bindingList) *bindings = NULL;
    YT(nameSpaceList) *nameSpaces = unit->scope->nameSpaces;
    char buf[BUFLEN];

    while (nameSpaces) {
    if (bindingType == nameSpaces->nameSpace->bindingType) {
        bindings = nameSpaces->nameSpace->bindings;
        while (bindings) {
        binding = bindings->binding;
        switch (bindingType) {
                case BIND_TYPE:
                    /* TODO */
                    P(";type");
                    break;
                case BIND_ATTRIBUTE:
                    generateEntityDescriptorPrefix(binding);
                    generateAttributeTypeDescriptor(
                        YC(attributeType,binding->value));
                    break;
                case BIND_CLASS:
                case BIND_INTERFACE:
                case BIND_ECLASS:
                case BIND_EINTERFACE:
                    break;
                case BIND_INGREDIENT_IMPL:
                    generateEntityDescriptorPrefix(binding);
                    generateIngredientImplDescriptor(YC(ingredientImpl,
                            binding->value));
                    break;
                case BIND_KIND:
                    generateEntityDescriptorPrefix(binding);
                    generateKindDescriptor(YC(kind,binding->value));
                    break;
                case BIND_PRESENCE_IMPL:
                    generateEntityDescriptorPrefix(binding);
                    generatePresenceImplDescriptor(YC(presenceImpl,
                              binding->value));
                    break;
                case BIND_PRESENCE_STRUCTURE:
                    generateEntityDescriptorPrefix(binding);
                    generatePresenceStructureDescriptor(
                        YC(presenceStructure,binding->value));
                    break;
                case BIND_UNIT:
                    generateEntityDescriptorPrefix(binding);
                    generateUnitImportDescriptor(YC(unit,binding->value));
                    break;
                case BIND_UNUM_IMPL:
                    generateEntityDescriptorPrefix(binding);
                    generateUnumImplDescriptor(YC(unumImpl,binding->value));
                    break;
                case BIND_UNUM_STRUCTURE:
                    generateEntityDescriptorPrefix(binding);
                    generateUnumStructureDescriptor(YC(unumStructure,
                               binding->value));
                    break;
                default:
                    yh_error("illegal binding type %d",
                             nameSpaces->nameSpace->bindingType);
        }
        bindings = bindings->next;
        }
    }
    nameSpaces = nameSpaces->next;
    }
}
  void
generateUnitDescriptor(YT(unit) *unit, char *inputFileName)
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
    generateElementtDescriptors(unit, inputFileName, BIND_UNIT);
    generateElementtDescriptors(unit, inputFileName, BIND_TYPE);
    generateElementtDescriptors(unit, inputFileName, BIND_INTERFACE);
    generateElementtDescriptors(unit, inputFileName, BIND_CLASS);
    generateElementtDescriptors(unit, inputFileName, BIND_EINTERFACE);
    generateElementtDescriptors(unit, inputFileName, BIND_ECLASS);
    generateElementtDescriptors(unit, inputFileName, BIND_ATTRIBUTE);
    generateElementtDescriptors(unit, inputFileName, BIND_KIND);
    generateElementtDescriptors(unit, inputFileName, BIND_INGREDIENT_IMPL);
    generateElementtDescriptors(unit, inputFileName, BIND_PRESENCE_STRUCTURE);
    generateElementtDescriptors(unit, inputFileName, BIND_PRESENCE_IMPL);
    generateElementtDescriptors(unit, inputFileName, BIND_UNUM_STRUCTURE);
    generateElementtDescriptors(unit, inputFileName, BIND_UNUM_IMPL);
}
