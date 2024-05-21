/*
  plouthab.c -- Output for the Haberdashery-based Pluribus.

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

void generateRequireCode(YT(requireAtt) *require);
static void generateUnit(YT(unit) *unit, char *inputFileName,
                         char *outputBaseDirName, char *saveFile);

  static void
expandHabScopeName(YT(scope) *scope, char *buf)
{
    strcpy(buf, "pluribus.");
    recursivelyExpandScopeName(scope, buf, ".");
}

  static void /* unch */
generateUnitPackage(YT(unit) *unit)
{
    YT(importAttList) *imports = unit->imports;

    if (UnitPackage)
        P("package %s;", pSymbolRef(UnitPackage));
    else
        P("package ec.pl.gencode;");
    P("import java.util.Vector;");
    P("import java.util.Hashtable;");
    P("import java.io.IOException;");
    P("import ec.e.db.RtUniquelyCodeable;");
    P("import ec.e.cap.EEnvironment;");
    P("import ec.pl.runtime.*;");

    while (imports) {
        YT(importAtt) *import = imports->importAtt;
        P("import %s%s;", pSymbolRef(import->name),
          import->isPackageImport ? ".*" : "");
        imports = imports->next;
    }
}

  static void /* unch */
generateUnitHeader(YT(unit) *unit, char *inputFileName)
{
    long timeNow = time(NULL);
    struct tm *timeInfo;
    char buf[BUFLEN];

    P("/* Produced by %s (Java embedded version)", VersionString);
    strftime(buf, BUFLEN, "%d-%B-%Y %T", localtime(&timeNow));
    P("   from %s on %s", inputFileName, buf);
    P("   This file is machine generated. Don't edit it or you'll be sorry.");
    P("*/");
    P("");
    generateUnitPackage(unit);
    P("");
}

  static void /* unch */
generateSubUnits(YT(unit) *unit, char *inputFileName,
                 char *outputBaseDirName, char *saveFile)
{
    YT(nameSpaceList) *nameSpaces = unit->scope->nameSpaces;
    while (nameSpaces) {
        YT(bindingList) *bindings = nameSpaces->nameSpace->bindings;
        while (bindings) {
            YT(binding) *binding = bindings->binding;
            if (nameSpaces->nameSpace->bindingType == BIND_UNIT)
                generateUnit(YC(unit,binding->value), inputFileName,
                             outputBaseDirName, saveFile);
            bindings = bindings->next;
        }
        nameSpaces = nameSpaces->next;
    }
}

  static void
generateUnitClasses(YT(unit) *unit, char *inputFileName,
                    char *outputBaseDirName)
{
    YT(nameSpaceList) *nameSpaces = unit->scope->nameSpaces;
    while (nameSpaces) {
        YT(bindingList) *bindings = nameSpaces->nameSpace->bindings;
        while (bindings) {
            YT(binding) *binding = bindings->binding;
            switch (nameSpaces->nameSpace->bindingType) {
                case BIND_ATTRIBUTE:
                    break;
                case BIND_CLASS:
                case BIND_INTERFACE:
                case BIND_ECLASS:
                case BIND_EINTERFACE:
                    generateJavaCode(YC(codeAtt,binding->value));
                    break;
                case BIND_INGREDIENT_IMPL:
                    generateIngredientImpl(YC(ingredientImpl,binding->value));
                    break;
                case BIND_KIND:
                    generateKind(YC(kind,binding->value));
#ifdef NON_HABERDASHERY
                    generateKindDescriptorClass(kind);
#endif
                    break;
                case BIND_PRESENCE_IMPL:
                    generatePresenceImpl(YC(presenceImpl,binding->value));
                    break;
                case BIND_PRESENCE_STRUCTURE:
                    /* gone in Haberdashery version */
#ifdef NON_HABERDASHERY
                    generatePresenceStructure(
                        YC(presenceStructure,binding->value));
#endif
                    break;
                case BIND_TYPE:
                    /* gone in Haberdashery version */
#ifdef NON_HABERDASHERY
                    generateType(YC(type,binding->value));
#endif
                    break;
                case BIND_UNIT:
                    break;
                case BIND_UNUM_IMPL:
                    generateUnumImpl(YC(unumImpl,binding->value));
                    break;
                case BIND_UNUM_STRUCTURE:
                    /* gone in Haberdashery version */
#ifdef NON_HABERDASHERY
                    generateUnumStructure(YC(unumStructure,binding->value));
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

  void
generateHaberdasheryOutput(YT(unit) *unit, char *inputFileName,
                           char *outputBaseDirName, char *saveFile)
{
    generateUnit(unit, inputFileName, outputBaseDirName, saveFile);
}

  static void
generateUnit(YT(unit) *unit, char *inputFileName, char *outputBaseDirName,
             char *saveFile)
{
    char scopeName[BUFLEN];
    char habScopeName[BUFLEN];
    char unitName[BUFLEN];
    char fileName[BUFLEN];

    if (!unit->isImported) {
        expandHabScopeName(unit->scope->outer, habScopeName);
        expandScopeName(unit->scope->outer, scopeName, "/");
        sprintf(unitName, "%s%s", scopeName,
                YC(symbolRef,unit->name)->name->name);
        sprintf(fileName, "%s/%s.unit", outputBaseDirName, unitName);
        if (pushOutput(fileName, saveFile)) {
            unit->filePath = STRDUP(unitName);
            generateSubUnits(unit, inputFileName, outputBaseDirName, saveFile);
            generateUnitHeader(unit, inputFileName);
            generateUnitClasses(unit, inputFileName, outputBaseDirName);
            popOutput();
            
            generateUnitDescriptorObjects(unit, inputFileName);
        }
    }
}
