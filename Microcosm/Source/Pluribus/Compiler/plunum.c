/*
  plunum.c -- Unum output for Pluribus.

  Chip Morningstar
  Electric Communities
  18-July-1996

  Copyright 1996 Electric Communities, all rights reserved.

*/

#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "y.tab.h"
#include "pl.h"

char *UnumEinterfaceName = "Unum",
     *PresenceEinterfaceName = "Presence",
     *PresenceHostEinterfaceName = "PresenceHost";


void generateRoutingTable(YT(kind) *kind, YT(presenceStructure) *presStruc,
                          int scope, int *counter);
void generateSetNeighbors(YT(presenceImpl) *presImpl, YT(presenceStructure) *presStruc);

  int
countMatches(char *name, YT(symbolList) *names) 
{
    int result = 0, i, count = YCOUNT(names);
    char *compare = NULL;

    while (names)
    {
        compare = names->symbol->name;
        if (name && compare && (strcmp(name, compare) == 0))
        {
            result++;
        }
        names = names->next;
    }
    return result;
}

  void
generateClientIngredientVariables(YT(makeAtt) *makeAtt)
{
    int numIngrs = 0;
    YT(symbolList) *ingrs = NULL;
    
    P ("    /* Save Client Ingredients */");
    if (makeAtt) {
        ingrs = makeAtt->inits;
        while (ingrs) {
            P("    pState.clientIngredients[%d] = %s;", numIngrs++,
              ingrs->symbol->name);
            ingrs = ingrs->next;
        }
    }
}

  void
generateGetSoulStateCalls(YT(ingredientRoleList) *ingrRoles)
{
    int numIngrs = 0, numMatches = 0, numRoles = YCOUNT(ingrRoles);
    YT(ingredientRole) *ingrRole = NULL;
    YT(stateBundle) *stateBundle = NULL;
    YT(symbolList) *bundleNames = NULL;
    
    if (ingrRoles) {
        while (ingrRoles) {
            ingrRole = ingrRoles->ingredientRole;
            if (ingrRole && ingrRole->template &&
                ingrRole->template->ingredientImpl) {
                stateBundle = ingrRole->template->ingredientImpl->stateBundle;
                if (stateBundle) {
                    numMatches = countMatches(SNAME(stateBundle), bundleNames);
                    bundleNames = YBUILD(symbolList)(stateBundle->typename, bundleNames);
                    P("    (%s)(soulState.get(\"%s.%s\"))%s",
                      stateBundle->typename->name,
                      stateBundle->packagename->name,
                      stateBundle->typename->name,
                      comma(ingrRoles->next));
                }
            }
            ingrRoles = ingrRoles->next;
        }
    }
}


  void
generateIngredientVariables(YT(ingredientRoleList) *ingrRoles)
{
    int numIngrs = 0;
    YT(ingredientRole) *ingrRole = NULL;
    YT(ingredientImpl) *ingrImpl = NULL;
    YT(symbolList) *ingredients = NULL;
    
    P ("    /* Initialize Ingredients */");
    while (ingrRoles) {
        ingrRole = ingrRoles->ingredientRole;
        if (ingrRole && ingrRole->template) {
            ingrImpl = ingrRole->template->ingredientImpl;
            ingredients = ingrRole->ingredients;
            if (ingrImpl) {
                while (ingredients) {
                    P("    %s %s = new %s(env);",
                      iiCodeName(SDNAME(ingrImpl)), ingredients->symbol->name,
                      iiCodeName(SDNAME(ingrImpl)));
                    P("    pState.ingredients[%d] = %s;", numIngrs++,
                      ingredients->symbol->name);
                    ingredients = ingredients->next;
                }
            }
        }
        ingrRoles = ingrRoles->next;
    }
}

   void
generateInitPresenceRouterMethods(char *prefix, YT(unumImpl) *uImpl)
{
    int numRoles = 0;
    YT(symbol) *name = NULL;
    YT(symbolList) *names = NULL;
    YT(presenceRole) *presenceRole = NULL;
    YT(presenceRoleList) *presenceRoles = NULL;
    YT(symbol) *primeName = NULL;

    if (uImpl) {
        presenceRoles = uImpl->roles;
        primeName = findPrimePresence(uImpl->structure)->name;
    }
    P("");
    P("  static public void initPresenceRouter(String roleName,");
    P("                                        PresenceRouter router,");
    P("                                        Object unumKey,");
    P("                                        PresenceEnvironment env) {");
    while (presenceRoles) {
        presenceRole = presenceRoles->presenceRole;
        if (presenceRole) {
            names = presenceRole->presences;
            while (names) {
                numRoles++;
                P("    %sif (roleName.equals(\"%s\")) {",
                  numRoles > 1 ? "} else " : "", names->symbol->name);
                P("      router.initialize(unumKey, make%sState(env, %s), env);",
                  SDNAME(presenceRole->impl),
                  primeName == names->symbol ? "true" : "false");
                names = names->next;
            }
        }
        presenceRoles = presenceRoles->next;
    }

    if (numRoles) {
        P("    } else {");
        P("      throw new RtRuntimeException(\"Trying to make invalid presence role\");");
        P("    }");
    }
    P("  }");
}

   void
generateMakeStateMethods(char *prefix, YT(unumImpl) *uImpl)
{
    int numUMethods = 0, numPMethods = 0;
    int numIngredients = 0, numClientIngrs = 0, counter = 0;
    YT(ingredientRoleList) *ingrRoles = NULL;
    YT(kind) *pKind = NULL;
    YT(kind) *uKind = NULL;
    YT(presenceImpl) *presImpl = NULL;
    YT(presenceRole) *presenceRole = NULL;
    YT(presenceRoleList) *presenceRoles = NULL;
    YT(presenceStructure) *pStruc = NULL;
    YT(symbolList) *presImplNames = NULL;

    if (uImpl) {
        presenceRoles = uImpl->roles;
        uKind = uImpl->structure->kind;
        if (!findImplements(UnumEinterfaceName, uKind->implements)) {
            yh_error("%skind %s for unum does not implement %s",
                     prefix, SDNAME(uKind), UnumEinterfaceName);
        }
        numUMethods = numProtosInKind(uKind);
    }
    while (presenceRoles) {
        presenceRole = presenceRoles->presenceRole;
        if (presenceRole) {
            presImpl = presenceRole->impl;
            if (presImpl && presImpl->structure) {
                pStruc = presImpl->structure;
                pKind = presImpl->structure->kind;
                if (!findImplements(PresenceEinterfaceName, pKind->implements)) {
                    yh_error("%skind %s for presence does not implement %s",
                             prefix, SDNAME(pKind), PresenceEinterfaceName);
                }
                numPMethods = numProtosInKind(pKind);
                ingrRoles = presImpl->roles;
                numIngredients = YCOUNT(ingrRoles);
                if (presImpl->makeAtt) {
                    numClientIngrs = YCOUNT(presImpl->makeAtt->inits);
                } else {
                    numClientIngrs = 0;
                }
            }
            if (presImpl && pKind &&
                (countMatches(SDNAME(presenceRole->impl), presImplNames) == 0)) {
                presImplNames = YBUILD(symbolList)(SNAME(presenceRole->impl),
                                                   presImplNames);
                P("");
                P("  static private UnumPresenceState make%sState(PresenceEnvironment env,",
                  SDNAME(presenceRole->impl));
                P("                                                  boolean isHost) {");
                P("    UnumPresenceState state = new UnumPresenceState();");
                P("    PresenceState pState= new PresenceState(%d,%d,%d,%d);",
                  numIngredients, numClientIngrs, numPMethods, numPMethods);
                P("    UnumState uState= new UnumState(%d,%d);", numUMethods, numUMethods);
                P("    state.presence = pState;");
                P("    pState.kindName = \"%s.%s\";", pSymbolRef(UnitPackage),
                    kindClassName(SDNAME(pKind)));
                if (presImpl->makeAtt) {
                    P("    pState.presenceToMakeName = \"%s\";",
                      SNAME(presImpl->makeAtt));
                } else {
                    P("    pState.presenceToMakeName = null;");
                }
                P("    pState.unumImplClassName = \"%s.%s\";", pSymbolRef(UnitPackage),
                    unumImplName(SDNAME(uImpl)));
                P("    pState.isHost = isHost;");
                P("    state.unum = uState;");
                P("    uState.kindName = \"%s.%s\";", pSymbolRef(UnitPackage),
                    kindClassName(SDNAME(uKind)));
                generateIngredientVariables(ingrRoles);
                P ("    /* KSSHack what about init()ing the ingredients here? */");
                generateClientIngredientVariables(presImpl->makeAtt);
                generateSetNeighbors(presImpl, pStruc);
                counter = 0;
                P("    /* Initialize presence routing */");
                generateRoutingTable(pKind, pStruc, PRESENCE, &counter);
                counter = 0;
                P("    /* Initialize unum routing */");
                generateRoutingTable(uKind, pStruc, UNUM, &counter);
                P("    return state;");
                P("  }");
            }
        }
        presenceRoles = presenceRoles->next;
    }
}

  void
generateParameterListFromRoles(YT(ingredientRoleList) *ingrRoles,
                               bool withTypes)
{
    int numIngrs = 0, numMatches = 0, numRoles = YCOUNT(ingrRoles);
    YT(ingredientRole) *ingrRole = NULL;
    YT(stateBundle) *stateBundle = NULL;
    YT(symbolList) *bundleNames = NULL;
    
    if (ingrRoles) {
        while (ingrRoles) {
            ingrRole = ingrRoles->ingredientRole;
            if (ingrRole && ingrRole->template &&
                ingrRole->template->ingredientImpl) {
                stateBundle = ingrRole->template->ingredientImpl->stateBundle;
                if (stateBundle) {
                    numMatches = countMatches(SNAME(stateBundle), bundleNames);
                    bundleNames = YBUILD(symbolList)(stateBundle->typename, bundleNames);
                    P("    %s my%s%d%s",
                      (withTypes ? stateBundle->typename->name : ""),
                      stateBundle->typename->name, numMatches,
                      comma(ingrRoles->next));
                }
            }
            ingrRoles = ingrRoles->next;
        }
    }
}

  void
generateProtoRoutingTable(YT(kind) *kind, YT(protoDef) *proto,
                          YT(presenceStructure) *presStruc, int scope,
                          int *counter)
{
    YT(ingredient) *ingr;
    YT(symbol) *ingrName;
    YT(symbol) *msgName;

    PP("    %sState.sealers[%d] =  sealer (%s <- %s(",
       (scope==UNUM ? "u":"p"), *counter,
       kindClassName(SDNAME(kind)), SNAME(proto));
    generateParameterTypeList(proto->params, NULL);
    P("));");

    ingr = findMessageDestination(presStruc, proto, scope, &ingrName,
                                  &msgName);
    P ("    %sState.targets[%d] =  (RtTether)%s;",
       (scope==UNUM ? "u":"p"), *counter,
       ingrName ? ingrName->name : "UNKNOWN INGREDIENT");
    (*counter)++;
}

  void
generateRoutingTable(YT(kind) *kind, YT(presenceStructure) *presStruc,
                     int scope, int *counter)
{
    YT(protoDefList) *protos = kind->protos;
    YT(kindList) *extends = kind->kinds;
    
    while (protos) {
        generateProtoRoutingTable(kind, protos->protoDef, presStruc, scope,
                                  counter);
        protos = protos->next;
    }
    while (extends) {
        generateRoutingTable(extends->kind, presStruc, scope, counter);
        extends = extends->next;
    }
}

  void
generateSetNeighbors(YT(presenceImpl) *presImpl,
                     YT(presenceStructure) *presStruc)
{
    YT(ingredient) *ingredient = NULL;
    YT(ingredientList) *ingredients = NULL;
    YT(neighbor) *neighbor = NULL;
    YT(neighborList) *neighbors = NULL;
    YT(symbol) *mappedNeighbor = NULL;
    YT(template) *template = NULL;

    P ("    /* Setting neighbors */");
    ingredients = presStruc->ingredients;
    while (ingredients) {
        ingredient = ingredients->ingredient;
        template = findTemplate(presImpl, ingredient->name);
        if (template && template->ingredientImpl) {
            neighbors = template->ingredientImpl->neighbors;
            if (neighbors) {
                PP("    ((%s)%s).setNeighbors(",
                   iiJavaName(SDNAME(template->ingredientImpl)),
                   SNAME(ingredient));
                while (neighbors) {
                    neighbor = neighbors->neighbor;
                    mappedNeighbor = findMappedNeighbor(template,
                                                        neighbor);
                    neighbors = neighbors->next;
                    PP("(%s)%s%s", mangleName(SDNAME(neighbor->kind), KIND),
                       mappedNeighbor ? mappedNeighbor->name : "null",
                       neighbors ? ", " : "");
                }
                P(");");
            }
        }
        ingredients = ingredients->next;
    }
}

   void
generateUnumImpl(YT(unumImpl) *uImpl)
{
    char prefix[BUFLEN];
    int numIngredients = 0, counter = 0;
    YT(presenceImpl) *primePresence = NULL;
    YT(presenceRole) *role = NULL;
    YT(symbolList) *presences = NULL;
    YT(presenceStructure) *pStruc = NULL;
    YT(presenceRoleList) *roles = uImpl->roles;
    YT(unumStructure) *uStruc = uImpl->structure;
    YT(kind) *uKind = uStruc->kind;
    YT(kind) *pKind = NULL;
    YT(symbol) *prime = uStruc->prime;

    sprintf(prefix, "unum impl %s: ", SDNAME(uImpl));

    P("public class %s {", unumImplName(SDNAME(uImpl)));

    generateMakeStateMethods(prefix, uImpl);
    generateInitPresenceRouterMethods(prefix, uImpl);

    roles = uImpl->roles;
    primePresence = NULL;
    while (roles && !primePresence) {
        role = roles->presenceRole;
        presences = role->presences;
        while (presences && !primePresence) {
            if (prime == presences->symbol) {
                primePresence = role->impl; // TODO poss. import problem
                pStruc = primePresence->structure;
                pKind = pStruc->kind;
            }
            presences = presences->next;
        }
        roles = roles->next;
    }
    
    if (primePresence) {
        numIngredients = YCOUNT(primePresence->roles);

        if (!findImplements(PresenceHostEinterfaceName, pKind->implements)) {
            yh_error("%skind %s for prime presence does not implement %s",
                     prefix, SDNAME(pKind), PresenceHostEinterfaceName);
        }

        P ("");
        P ("  static public %s createUnum(Object unumKey, SoulState soulState) {",
           kindClassName(SDNAME(uKind)));
        P ("    PresenceEnvironment myEnvironment = new PresenceEnvironment();");
        P ("    Object[] myStateBundles = new Object[%d];", numIngredients);
        P ("    /* Create Routers */");
        P ("    PresenceRouter pRouter = new PresenceRouter();");
        P ("    UnumRouter uRouter = new UnumRouter();");
        P ("    /* Store the unumKey in the soulState */");
        P ("    soulState.setUnumKey(unumKey);");
        P ("    /* Initialize Presence Router */");
        P ("    initPresenceRouter(\"%s\", pRouter, unumKey, myEnvironment);",
           prime->name);
        P ("    /* Initialize Unum Router */");
        P ("    pRouter.initUnumRouter(uRouter, unumKey);");
        P ("    /* Intialize state bundles */");
        P ("    myStateBundles = {");
        generateGetSoulStateCalls(primePresence->roles);
        P ("    };");
        P ("    pRouter.finish(uRouter, myStateBundles, soulState, \"%s.%s\");",
           pSymbolRef(UnitPackage), unumImplName(SDNAME(uImpl)));

        P ("    return((%s)(uRouter.getDeflector()));", kindClassName(SDNAME(uKind)));
        P ("  }");
    }

    P("}");
    P("");
    roles = uImpl->roles;
}

  void
generateUnumStructure(YT(unumStructure) *uStruc)
{
    YT(presence) *prime = findPrimePresence(uStruc);
    YT(presence) *presence = NULL;
    YT(presenceList) *presences = uStruc->presences;

    P("public class %s", PNAME(uStruc));
    P("extends pl$_unumStructure");
    P("{");

    P("    static pl$_unumStructure self;");
    P("    static {");

    /* Attributes */
    generateAttributes(uStruc->attributes);

    /* UnumKind */
    P("        pl$_kind kind = (pl$_kind)Plu.getSelf(\"%s\");",
      PNAME(uStruc->kind));

    /* Presences */
    if (prime) {
    P("        pl$_presence presences[] = {");
    while (presences) {
        presence = presences->presence;
        P("            new pl$_presence(%s,", namestr(presence->name));
        P("                (pl$_presenceStructure)Plu.getSelf(\"%s\"),",
          PNAME(presence->kind));
        P("                %s)%s",
          presence->name == prime->name ? "true" : "false",
          comma(presences->next));
        presences = presences->next;
    }
    }
    P("        };");

    P("        self = new pl$_unumStructure(attributes, kind, presences);");
    P("    }");

    P("}");
    P("");
}
