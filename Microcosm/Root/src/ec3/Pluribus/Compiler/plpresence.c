/*
  plpresence.c -- Presence output for Pluribus.

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

  void
generatePresenceImpl(YT(presenceImpl) *presImpl)
{
    YT(ingredientRoleList) *roles = presImpl->roles;
    char prefix[MSGLEN];
    char templateName[MSGLEN];
    int numRoles = 0;

    sprintf(prefix, "presenceImpl %s: ", SDNAME(presImpl));
    //KSSHack Done in checkUnumImpl? checkPresenceImplLinks(prefix, presImpl);

    /* Templates */
    if (roles) {
        while (roles) {
            YT(ingredientRole) *role = roles->ingredientRole;
            numRoles++;
            sprintf(templateName, "%s%s_$template_%d_", SDNAME(presImpl),
                    role->ingredients->symbol->name, numRoles);
            generateTemplate(templateName, role->template);
            roles = roles->next;
        }
    }

    P("public class %s", /* KSSHack need plmangle.c routine? */
      PNAME(presImpl));
    P("extends pl$_presenceImpl");
    P("{");
    P("    static pl$_presenceImpl self;");
    P("    static {");
    
    /* Attributes */
    generateAttributes(presImpl->attributes);
    
    /* Roles */
    if (roles) {
        numRoles = 0;
        P("        pl$_ingredientRole roles[] = {");
        while (roles) {
            YT(ingredientRole) *role = roles->ingredientRole;
            YT(symbolList) *ingrs = role->ingredients;
            numRoles++;
            sprintf(templateName, "%s%s_$template_%d_", SDNAME(presImpl),
                    ingrs->symbol->name, numRoles);
            while (ingrs) {
                P("            new pl$_ingredientRole(%s,",
                  namestr(ingrs->symbol));
                P("                (pl$_template)Plu.getSelf(\"%s\"))%s",
                  templateName, comma(ingrs->next ? roles : roles->next));
                ingrs = ingrs->next;
            }
            roles = roles->next;
        }
        P("        };");
    } else {
        P("        pl$_ingredientRole roles[] = null;");
    }
    P("        self = new pl$_presenceImpl(");
    P("            attributes,");
    P("            (pl$_presenceStructure)Plu.getSelf(\"%s\"),",
      /* KSSHack need plmangle.c routine? */ PNAME(presImpl->structure));
    P("            roles);");
    P("    }");
    P("}");
    P("");
}

  void
generateTemplate(char *name, YT(template) *template)
{
    YT(mapAttList) *mapAtts = template->mapAtts;

    P("public class %s", name);
    P("extends pl$_template");
    P("{");

    P("    static pl$_template self;");
    P("    static {");

    /* MapAtts */
    if (mapAtts) {
        P("        pl$_mapAtt mapAtts[] = {");
        while (mapAtts) {
            YT(mapAtt) *mapAtt = mapAtts->mapAtt;
            if (YTAG_TEST(mapAtt,mapAtt)) {
                if (mapAtt->mapTo) {
                    P("            new pl$_mapAtt(%s, %s, %s)%s",
                      boolstr(mapAtt->scope == NEIGHBOR),
                      namestr(mapAtt->mapTo),
                      namestr(mapAtt->mapFrom),
                      comma(mapAtts->next));
                }
            }
            mapAtts = mapAtts->next;
        }
        P("        };");
    } else {
        P("        pl$_mapAtt mapAtts[] = null;");
    }

    /* StateInitAtts */
    /* TODO */

    P("        self = new pl$_template(");
    P("            (pl$_ingredientImpl)Plu.getSelf(\"%s\"),",
      template->ingredientImpl ? iiCodeName(SDNAME(template->ingredientImpl)) :
                                 "MISSING TEMPLATE IMPL");
    P("            mapAtts);");
    P("    }");

    P("}");
    P("");
}

   void
generatePresenceStructure(YT(presenceStructure) *struc)
{
    YT(ingredientList) *ingredients = struc->ingredients;
    YT(ingredient) *ingredient;
    YT(deliverAttList) *deliverAtts;
    YT(deliverAtt) *deliverAtt;
    char prefix[MSGLEN];
    int numDeliveries = 0;

    sprintf(prefix, "presenceStructure %s: ", SDNAME(struc));

    P("public class %s", /* KSSHack need plmangle.c routine? */ PNAME(struc));
    P("extends pl$_presenceStructure");
    P("{");

    P("    static pl$_presenceStructure self;");
    P("    static {");

    /* Attributes */
    generateAttributes(struc->attributes);

    /* PresenceKind */
    P("        pl$_kind presenceKind = (pl$_kind)Plu.getSelf(\"%s\");",
      kindClassName(SDNAME(struc->kind)));

    /* Ingredients */
    if (ingredients) {
        P("        pl$_ingredient ingredients[] = {");
        while (ingredients) {
           P("            new pl$_ingredient((pl$_kind)Plu.getSelf(\"%s\"),",
              kindClassName(SDNAME(ingredients->ingredient->kind)));
           P("                               %s)%s",
              namestr(ingredients->ingredient->name),
              comma(ingredients->next));
            ingredients = ingredients->next;
        }
        P("        };");
    } else {
        P("        pl$_ingredient ingredients[] = null;");
    }

    /* DeliverAtts */
    ingredients = struc->ingredients;
    if (ingredients) {
        while (ingredients) {
            ingredient = ingredients->ingredient;
            deliverAtts = ingredient->deliverAtts;
            if (deliverAtts) {
                numDeliveries++;
                if (numDeliveries == 1)
                    P("        pl$_deliverAtt deliverAtts[] = {");
                else
                    PP(",");
                while (deliverAtts) {
                    deliverAtt = deliverAtts->deliverAtt;
                    if (YTAG_TEST(deliverAtt,deliverAtt)) {
                        P("            new pl$_deliverAtt(%s, %s, %s, %s)%s",
                          boolstr(deliverAtt->scope == PRESENCE),
                          namestr(deliverAtt->source),
                          namestr(ingredient->name),
                          namestr(deliverAtt->target),
                          comma(deliverAtts->next));
                    }
                    deliverAtts = deliverAtts->next;
                }
            }
            ingredients = ingredients->next;
        }
    }
    if (numDeliveries > 0)
        P("        };");
    else
        P("        pl$_deliverAtt deliverAtts[] = null;");


    P("        self = new pl$_presenceStructure(attributes, presenceKind, ingredients,");
    P("            deliverAtts);");
    P("    }");

    P("}");
    P("");
}
