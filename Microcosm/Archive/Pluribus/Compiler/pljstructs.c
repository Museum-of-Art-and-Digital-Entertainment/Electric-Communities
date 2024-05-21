/*
  pljstructs.c -- Java native code to translate back and forth between the
                  world of Java objects stored in the Haberdashery and the
                  world of C structs in the compiler's memory.

  Chip Morningstar
  Electric Communities
  7-March-1997

  Copyright 1997 Electric Communities. All rights reserved worldwide.

*/

#include "generic.h"
#include "plj.h"

#include "ec_plcompile_ElemKind.h"
#include "ec_e_hab_Haberdashery.h"
#include "ec_e_hab_DirectDesignator.h"

#define clHABER(clas)   "ec/e/hab/" #clas
#define clPLU(clas)     "ec/plcompile/" #clas

#define sHABER(clas)    "L" clHABER(clas) ";"
#define sSTRING         "Ljava/lang/String;"
#define sOBJECT         "Ljava/lang/Object;"
#define sARRAY(type)    "[" type
#define sARGS(args)     "(" args ")"
#define sRETURN(res)    res
#define sBOOLEAN        "B"

#define hHABER(clas)    Hec_e_hab_##clas

#define HDirectDesignator hHABER(DirectDesignator)
#define HString         Hjava_lang_String

static HObject *TheHaberdashery = NULL;

/**
* Close the Haberdashery (if it's open).
*/
  void
closeHaberdashery()
{
    if (TheHaberdashery)
        javaCall(TheHaberdashery, "close", "()V");
}

/**
* Open the Haberdashery.
*
* @param filename The name of the Haberdashery's Repository file.
*/
  void
openHaberdashery(char *filename)
{
    HString *jFilename;

    jFilename = makeJavaString(filename, strlen(filename));
    TheHaberdashery = javaConstruct(clHABER(Haberdashery),
                                    sARGS(sSTRING),
                                    jFilename);
}

/**
* Generate a Java String from a symbol name.
*/
 HString *
convertSymbolToJavaString(YT(symbol) *symbol)
{
    return(makeJavaString(SNAME(symbol),
                          strlen(SNAME(symbol))));
}

/**
* Put a Java object into the Haberdashery.
*
* @param obj The object to put.
* @return The DirectDesignator of that resulted.
*/
  HDirectDesignator *
putHaberdashery(HObject *obj)
{
    return((HDirectDesignator *) javaCall(TheHaberdashery, "put",
        sARGS(sHABER(HaberdasheryObject))  sRETURN(sHABER(DirectDesignator)),
        obj));
}

/**
* Put an attributeType struct into the Haberdashery as an ElemAttributeType
*       object.
*/
  HDirectDesignator *
putElemAttributeType(YT(unit) *unit, YT(attributeType) *attributeType)
{
    HObject *newElem;
    HDirectDesignator *result;
    char buf[BUFLEN];
    HString *attributeTypeName;
    HString *typeName;

    if (attributeType->key)
        return(attributeType->key);

    strcat(buf, PNAME(attributeType));
    attributeTypeName = makeJavaString(buf, strlen(buf));
    printf("putElemAttributeType sez name: '%s'\n", buf);/* XXX diagnostic */
    pTypeSignature(attributeType->type, buf);
    typeName = makeJavaString(buf, strlen(buf));
    printf("putElemAttributeType sez type: '%s'\n", buf);/* XXX diagnostic */
    newElem = javaConstruct(clPLU(ElemAttributeType),
                            sARGS(sSTRING sSTRING),
                            attributeTypeName, typeName);
    result = putHaberdashery(newElem);
    attributeType->key = result;
    return(result);
}

/**
* Produce a DescAttribute object from an attribute struct (for
* convertListToJavaArray).
*/
  static HObject *
extractAttributeDesc(void *elem)
{
    YT(attribute) *attribute = YC(attribute,elem);
    HObject *result;
    HDirectDesignator *type;
    HObject *value;

    type = putElemAttributeType(attribute->type);
    value = convertValueToJavaObject(attribute->worth);
    result = javaConstruct(clPLU(DescAttribute),
                           sARGS(sHABER(DirectDesignator) sOBJECT),
                           type, value);
    return(result);
}

/**
* Pull the key from a kind struct (for convertListToJavaArray).
*/
  static HObject *
extractKindKey(void *elem)
{
    return((HObject *)YC(kind,elem)->key);
}

/**
* Put a kind struct into the Haberdashery as an ElemKind object.
*/
  HDirectDesignator *
putElemKind(YT(unit) *unit, YT(kind) *kind)
{
    HObject *newElem;
    HDirectDesignator *result;
    HArrayOfObject *attributes = NULL;
    HArrayOfObject *kinds = NULL;
    char buf[BUFLEN];
    HString *interfaceName;

    if (kind->key)
        return(kind->key);

    attributes = convertListToJavaArray(YC(genericList,kind->attributes),
                                        extractAttributeDesc);
    kinds = convertListToJavaArray(YC(genericList,kind->kinds),
                                   extractKindKey);

    strcpy(buf, UnitPackage ? pSymbolRef(UnitPackage) : "ec.pl.gencode");
    strcat(buf, ".");
    strcat(buf, PNAME(kind));
    strcat(buf, "if_");
    interfaceName = makeJavaString(buf, strlen(buf));
    printf("putElemKind sez: '%s'\n", buf); /* XXX diagnostic */

    newElem = javaConstruct(clPLU(ElemKind),
                            sARGS(sARRAY(sHABER(Designator))
                                  sARRAY(sHABER(Designator))
                                  sSTRING),
                            attributes, kinds, interfaceName);
    result = putHaberdashery(newElem);
    kind->key = result;
    return(result);
}

/**
* Produce a DescPresenceStructureDeliver object from a deliverAtt struct.
*/
  static HObject *
extractDeliverDesc(void *elem)
{
    YT(deliverAtt) *deliverAtt = YC(deliverAtt,elem);
    HObject *result;
    bool presenceScope;
    HString *source;
    HString *target;

    if (YTAG_TEST(deliverAtt,deliverDefaultAtt)) {
        presenceScope = (YC(deliverDefaultAtt,deliverAtt)->scope == PRESENCE);
        source = NULL;
        target = NULL;
    } else {
        presenceScope = (deliverAtt->scope == PRESENCE);
        source = convertSymbolToJavaString(deliverAtt->source);
        target = convertSymbolToJavaString(deliverAtt->target);
    }

    result = javaConstruct(clPLU(DescPresenceStructureDeliver),
                           sARGS(sBOOLEAN sSTRING sSTRING),
                           presenceScope, source, target);
    return(result);
}

/**
* Produce a DescPresenceStructureIngredient object from a presence structure
* ingredient (for convertListToJavaArray).
*/
  static HObject *
extractIngredientDesc(void *elem)
{
    YT(ingredient) *ingredient = YC(ingredient,elem);
    HObject *result;
    HString *name;
    HObject *kind;
    HArrayOfObject *delivers;

    name = convertSymbolToJavaString(ingredient->name);
    kind = putElemKind(unit, ingredient->kind);
    delivers =
        convertListToJavaArray(YC(genericList,ingredient->deliverAtts),
                               extractDeliverDesc);
    result = javaConstruct(clPLU(DescPresenceStructureIngredient),
                           sARGS(sSTRING
                                 sHABER(Designator)
                                 sARRAY(sPLU(DescPresenceStructureDeliver))),
                           name, kind, delivers);
    return(result);
}

/**
* Put a presenceStructure struct into the Haberdashery as an
* ElemPresenceStructure object.
*/
  HDirectDesignator *
putElemPresenceStructure(YT(unit) *unit, YT(presenceStructure) *struc)
{
    HObject *newElem;
    HDirectDesignator *result;
    HArrayOfObject *attributes;
    HObject *kind;
    HArrayOfObject *ingredients;

    if (struc->key)
        return(struc->key);

    attributes = convertListToJavaArray(YC(genericList,struc->attributes),
                                        extractAttributeDesc);
    kind = putElemKind(struc->kind);
    ingredients = convertListToJavaArray(YC(genericList,struc->ingredients),
                                         extractIngredientDesc);

    newElem = javaConstruct(clPLU(ElemPresenceStructure),
                           sARGS(sARRAY(sPLU(DescAttribute))
                                 sHABER(Designator)
                                 sARRAY(sPLU(DescPresenceStructureIngredient)))
                                 sBOOLEAN),
                           attributes, kind, ingredients, FALSE);
    result = putHaberdashery(newElem);
    struc->key = result;
    return(result);
}

/**
* Produce a DescPresenceCond object from a presenceCond struct.
*/
  static HObject *
extractPresenceCond(void *elem)
{
    YT(presenceCond) *presenceCond = YC(presenceCond,elem);
    HObject *result;
    HString *makes;

    makes = convertSymbolToJavaString(presenceCond->makes);
    /* We don't know what to do with expressions yet, so for now just pass
       NULL as a placeholder. XXX */
    result = javaConstruct(clPLU(DescPresenceCond),
                           sARGS(sSTRING sPLU(DescExpr)),
                           NULL, makes);
    return(result);
}

/**
* Produce a DescUnumStructurePresence object from an unum structure presence
* (for convertListToJavaArray).
*/
  static HObject *
extractPresenceDesc(void *elem)
{
    YT(presence) *presence = YC(presence,elem);
    HObject *result;
    HString *name;
    HObject *kind;
    HArrayOfObject *conditionals;

    name = convertSymbolToJavaString(presence->name);
    kind = putElemKind(unit, presence->kind);
    conditionals =
        convertListToJavaArray(YC(genericList,presence->conditionals),
                               extractPresenceCondDesc);
    result = javaConstruct(clPLU(DescUnumStructurePresence),
                           sARGS(sSTRING
                                 sHABER(Designator)
                                 sBOOLEAN
                                 sARRAY(sPLU(DescPresenceCond))),
                           name, kind, presence->isPrime, conditionals);
    return(result);
}

/**
* Put an unumStructure struct into the Haberdashery as an ElemUnumStructure
* object.
*/
  HDirectDesignator *
putElemUnumStructure(YT(unit) *unit, YT(unumStructure) *struc)
{
    HObject *newElem;
    HDirectDesignator *result;
    HArrayOfObject *attributes;
    HObject *kind;
    HArrayOfObject *presences;

    if (struc->key)
        return(struc->key);

    attributes = convertListToJavaArray(YC(genericList,struc->attributes),
                                        extractAttributeDesc);
    kind = putElemKind(struc->kind);
    presences = convertListToJavaArray(YC(genericList,struc->presences),
                                       extractPresenceDesc);

    newElem = javaConstruct(clPLU(ElemUnumStructure),
                            sARGS(sARRAY(sPLU(DescAttribute))
                                  sHABER(Designator)
                                  sARRAY(sPLU(DescUnumStructurePresence))
                                  sBOOLEAN),
                            attributes, kind, presences, FALSE);
    result = putHaberdashery(newElem);
    struc->key = result;
    return(result);
}

/**
* Produce a Java String object from a symbol (for convertListToJavaArray).
*/
  static HObject *
extractSymbolName(void *elem)
{
    return((HObject *)convertSymbolToJavaString(YC(symbol,elem)));
}

/**
* Put an unumImpl struct into the Haberdashery as an ElemUnumImpl object.
*/
  HDirectDesignator *
putElemUnumImpl(YT(unit) *unit, YT(unumImpl) *impl)
{
    HObject *newElem;
    HDirectDesignator *result;
    HArrayOfObject *attributes;
    HObject *structure;
    HArrayOfObject *presences;

    if (impl->key)
        return(impl->key);

    attributes = convertListToJavaArray(YC(genericList,impl->attributes),
                                        extractAttributeDesc);
    kind = putElemUnumStructure(impl->structure);
    presences = convertListToJavaArray(YC(genericList,impl->presences),
                                       extractPresenceRoles);

    newElem = javaConstruct(clPLU(ElemUnumImpl),
                            sARGS(sARRAY(sPLU(DescAttribute))
                                  sHABER(Designator)
                                  sARRAY(sPLU(DescUnumImplPresence))
                                  sBOOLEAN),
                            attributes, structure, presences, FALSE);
    result = putHaberdashery(newElem);
    impl->key = result;
    return(result);
}

/**
* Produce a DescUnumImplPresence object from an unum impl presenceRole struct
* (for convertListToJavaArray).
*/
  static HObject *
extractPresenceRole(void *elem)
{
    YT(presenceRole) *presenceRole = YC(presenceRole,elem);
    HObject *result;
    HArrayOfObject *presences;
    HObject *impl;

    presences =
        convertListToJavaArray(YC(genericList,presenceRole->presences),
                               extractSymbolName);
    impl = putElemPresenceImpl(unit, presenceRole->impl);
    result = javaConstruct(clPLU(DescUnumImplPresence),
                           sARGS(sARRAY(sSTRING)
                                 sHABER(Designator)),
                           presences, impl);
    return(result);
}

/**
* Put a presenceImpl struct into the Haberdashery as an ElemPresenceImpl
* object.
*/
  HDirectDesignator *
putElemPresenceImpl(YT(unit) *unit, YT(presenceImpl) *impl)
{
    HObject *newElem;
    HDirectDesignator *result;
    HArrayOfObject *attributes;
    HObject *structure;
    HArrayOfObject *ingredients;

    if (impl->key)
        return(impl->key);

    attributes = convertListToJavaArray(YC(genericList,impl->attributes),
                                        extractAttributeDesc);
    kind = putElemIngredientstructure(impl->structure);
    ingredients = convertListToJavaArray(YC(genericList,impl->roles),
                                         extractIngredientRoles);

    newElem = javaConstruct(clPLU(ElemPresenceImpl),
                            sARGS(sARRAY(sPLU(DescAttribute))
                                  sHABER(Designator)
                                  sARRAY(sPLU(DescPresenceImplIngredient))
                                  sSTRING
                                  sBOOLEAN),
                            attributes, structure, ingredients, implClassName,
                            FALSE);
    result = putHaberdashery(newElem);
    impl->key = result;
    return(result);
}

/**
* Produce a DescPresenceImplIngredient object from a presence impl
* ingredientRole struct (for convertListToJavaArray).
*/
  static HObject *
extractIngredientRole(void *elem)
{
    YT(ingredientRole) *ingredientRole = YC(ingredientRole,elem);
    HObject *result;
    HArrayOfObject *ingredients;
    HObject *impl;

    ingredients =
        convertListToJavaArray(YC(genericList,ingredientRole->ingredients),
                               extractSymbolName);
    impl = putElemIngredientImpl(unit, ingredientRole->impl);
    result = javaConstruct(clPLU(DescPresenceImplIngredient),
                           sARGS(sARRAY(sSTRING)
                                 sHABER(Designator)),
                           ingredients, impl);
    return(result);
}
