/*
  plout.c -- Output for Pluribus.

  Chip Morningstar
  Electric Communities
  15-August-1996

  Copyright 1996 Electric Communities, all rights reserved.

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

  char *
comma(void *next)
{
    if (next)
        return(", ");
    else
        return("");
}

  char *
boolstr(bool flag)
{
    if (flag)
        return("true");
    else
        return("false");
}

  void
ekeepClose()
{
#ifdef PLRTEXENV
    P("    }}");
#endif
}

  void
ekeepOpen()
{
#ifdef PLRTEXENV
    P("    {  ekeep (ee_$_) {");
#endif
}

  char *
namestr(YT(symbol) *name) {
    if (name == NULL) {
        return("null");
    } else {
        char buf[BUFLEN];
        sprintf(buf, "\"%s\"", name->name);
        return(STRDUP(buf));
    }
}

  char *
pBaseJavaName(YT(anyBinding) *binding)
{
    YT(symbolDef) *name = binding->name;

    if (YTAG_TEST(name,symbolRef))
        return(YC(symbolRef,name)->name->name);
    else
        return("<invalid pBaseJavaName source>");
}

  char *
pJavaName(YT(anyBinding) *binding)
{
    static char buf[BUFLEN];
    YT(symbolDef) *name = NULL;

    if (binding && binding->name) {
    name = binding->name;
    if (YTAG_TEST(name,symbolRef)) {
        sprintf(buf, "%s$_%s_", bindingTypeToTag(binding->bindingType),
            YC(symbolRef,name)->name->name);
    } else {
        strcpy(buf, "<invalid pJavaName source>");
    }
    }
    return(buf);
}

  char *
rtExceptionEnvParam(bool withType)
{
#ifdef PLRTEXENV
    if (withType) {
        return ("RtExceptionEnv ee_$_");
    } else {
        return ("ee_$_");
    }
#else
    return NULL;
#endif
}

  void
generateTypeSignature(YT(type) *type, bool isArray)
{
    YT(pluribusType) *plType = NULL;
    if (isArray) {
        PP("(");
    }
    switch (YTAG_OF(type)) {
        case YTAG(defType):
            PP("L%s;", PNAME(type));
            break;
        case YTAG(enumType):
            yh_error("enum not allowed in type signature");
            break;
        case YTAG(pluribusType):
            plType = YC(pluribusType,type);
            if (plType->mangle == KIND)
                PP("K%s;", pSymbolRef(plType->type));
            else
                PP("L%s;", pSymbolRef(plType->type));
            break;
        case YTAG(primType):
            switch (YC(primType,type)->type) {
                case BOOLEAN: PP("Z"); break;
                case CHAR: PP("C"); break;
                case DOUBLE: PP("D"); break;
                case FLOAT: PP("F"); break;
                case INT: PP("I"); break;
                case LONG: PP("J"); break;
                case BYTE: PP("B"); break;
                case SHORT: PP("S"); break;
                case TagType: PP("T"); break; //KSS?
                default:
                    yh_error("invalid primType code %d in generateTypeSignature()",
                             YC(primType,type)->type);
            }
            break;
        case YTAG(sequenceType):{
            YT(sequenceType) *seq = YC(sequenceType,type);
            if (seq->dimension == -1)
                PP("[");
            else
                PP("[%d", seq->dimension);
            generateTypeSignature(seq->type, FALSE);
            break;
            }
        case YTAG(stringType):
            PP("LString;");
            break;
        case YTAG(structType):
            yh_error("struct not allowed in type signature>");
            break;
        case YTAG(undefinedType):
            PP("L%s;", pSymbolRef(YC(undefinedType,type)->type));
            break;
        case YTAG(unionType):
            yh_error("union not in type signature");
            break;
        case YTAG(symbolRef):
                PP("L%s;", pSymbolRef(YC(symbolRef,type)));
            break;
        default:
           yh_error("invalid type code %d in generateTypeSignature()",
                    YTAG_OF(type));
    }
}

  void
generateTypeSpec(YT(type) *type)
{
    YT(pluribusType) *plType = NULL;
    switch (YTAG_OF(type)) {
        case YTAG(defType):
            PP("%s", PNAME(type));
            break;
        case YTAG(enumType):
            PP("<enum not allowed as typeSpec>");
            break;
        case YTAG(pluribusType):
            plType = YC(pluribusType,type);
        PP("%s", mangleName(SNAME(plType->type), plType->mangle));
            break;
        case YTAG(primType):
            switch (YC(primType,type)->type) {
                case BOOLEAN: PP("boolean"); break;
                case CHAR: PP("char"); break;
                case DOUBLE: PP("double"); break;
                case FLOAT: PP("float"); break;
                case INT: PP("int"); break;
                case LONG: PP("long"); break;
                case BYTE: PP("byte"); break;
                case SHORT: PP("short"); break;
                default:
                    yh_error("invalid primType code %d in generateTypeSpec",
                             YC(primType,type)->type);
            }
            break;
        case YTAG(sequenceType):
            generateTypeSpec(YC(sequenceType,type)->type);
            PP("[]");
            break;
        case YTAG(stringType):
            PP("String");
            break;
        case YTAG(structType):
            PP("<struct not allowed as typeSpec>");
            break;
        case YTAG(undefinedType):
            PP("%s", pSymbolRef(YC(undefinedType,type)->type));
            break;
        case YTAG(unionType):
            PP("<union not allowed as typeSpec>");
            break;
        case YTAG(symbolRef): /* XXX hack */
            PP("%s", pSymbolRef(YC(symbolRef,type)));
            break;
        default:
           yh_error("invalid type code %d in generateTypeSpec", YTAG_OF(type));
    }
}

  void
generateParameterList(YT(parameterDeclList) *params, char *extraParams)
{
    int i = 0;
    YT(parameterDecl) *param = NULL;
    YT(parameterDeclList) *list = params;

    while (list) {
        param = list->parameterDecl;
        generateTypeSpec(YC(type,param->type));
        for (i = 0; i < param->dimensions; i++) {
            PP("[]");
        }
        PP(" %s%s", (param->name ? param->name->name : gensym()->name),
           comma(list->next));
        list = list->next;
    }
    if (extraParams) {
        if (params) {
            PP(", ");
        }
        PP("%s", extraParams);
    }
}

/** Generate a list of parameter types only, no variable names */
  void
generateParameterTypeList(YT(parameterDeclList) *params, char *extraParams)
{
    int i = 0;
    YT(parameterDecl) *param = NULL;
    YT(parameterDeclList) *list = params;

    while (list) {
        param = list->parameterDecl;
        generateTypeSpec(YC(type,param->type));
        for (i = 0; i < param->dimensions; i++) {
            PP("[]");
        }
        PP("%s", comma(list->next));
        list = list->next;
    }
    if (extraParams) {
        if (params) {
            PP(", ");
        }
        PP("%s", extraParams);
    }
}


  void
generateCallParameters(YT(parameterDeclList) *params, char *extraParams)
{
    YT(parameterDecl) *param = NULL;
    YT(parameterDeclList) *list = params;

    while (list) {
        param = list->parameterDecl;
        PP("%s%s", param->name ? SNAME(param) : "UNNANMED PARAM",
           comma(list->next));
        list = list->next;
    }
    if (extraParams) {
        if (params) {
            PP(", ");
        }
        PP("%s", extraParams);
    }
}

  static void
generateProto(YT(protoDef) *proto)
{
    PP("    %s(", SNAME(proto));
    generateParameterList(proto->params, NULL);
    PP(")");
    generateThrowsList(proto->throws);
    P(";");
}

  void
generateThrowsList(YT(scopedRefList) *throws)
{
    char buf[BUFLEN];

    buf[0]='\0';
    if (throws) {
        PP(" throws ");
        while (throws) {
            symbolRefString(buf, (YT(symbolRef) *)(throws->scopedRef));
            PP("%s", buf);
            PP("%s", throws->next ? ", " : "");
            throws = throws->next;
        }
    }
}

  void
generateAttributeCode(int attrType, YT(typedValue) val, char *name) {
    if (val.typeCode == TV_UND) {
        P("        //\"%s\", is undefined due to a compile error", name);
    } else if (val.typeCode == TV_DEP) {
        P("        //\"%s\", is dependent on another attribute", name);
    } else {
        switch (attrType) {
        case BOOLEAN: case TagType:
            P("        attributes.put(\"%s\", new Boolean(%s));", name,
              YUV(val,TV_BOOL) ? "true":"false");
            break;
        case CHAR:
            P("        attributes.put(\"%s\", new Character('%c'));", name,
              YUV(val,TV_CHAR));
            break;
        case LONG:
            P("        attributes.put(\"%s\", new Integer(%d));", name,
              YUV(val,TV_LONG));
            break;
        case YTAG(stringType):
            P("        attributes.put(\"%s\", new String(\"%s\"));", name,
              YUV(val,TV_STRING));
            break;
        default:
            P("        //\"%s\", is a totally unknown type (%d)", name,
              attrType);
            break;
        }
    }
}

  void
generateAttributes(YT(attributeList) *attributes)
{
    YT(attribute) *attribute;
    if (attributes) {
        P("        Hashtable attributes = new Hashtable();");
        while (attributes) {
            if (YTAG_TEST(attributes->attribute, attribute)) {
                attribute = attributes->attribute;
                if (!attribute->worth)
                    attribute->worth = UND_VAR;
                /*KSS Should have been calculated already
                      in plgrind:plGrind() at PCASE(attributeRef) KSS*/
                generateAttributeCode(attribute->type->type->dummy,
                                      *(attribute->worth),
                                      PBASENAME(attribute->type));
            } else {
                generateRequireCode((YT(requireAtt)*)attributes->attribute);
            }
            attributes = attributes->next;
        }
    } else {
        P("        Hashtable attributes = null;");
    }
}

  static void
generateKind(YT(kind) *kind)
{
    YT(protoDefList) *protos = kind->protos;
    YT(kindList) *extends = kind->kinds;
    YT(implementsAttList) *implements = kind->implements;

    PP("public einterface %s", kindClassName(SDNAME(kind)));
    if (extends || implements) {
        PP(" extends ");
    }

    /* Extends */
    while (extends) {
        PP("%s", kindClassName(SDNAME(extends->kind)));
        extends = extends->next;
        if (extends || implements)
            PP(", ");
    }

    /* Implements */
    while (implements) {
        PP("%s", pSymbolRef(implements->implementsAtt->name));
        implements = implements->next;
        if (implements)
            PP(", ");
    }

    P(" {");

    /* Protos */
    while (protos) {
        if (protos->protoDef->name != initSym)
	    generateProto(protos->protoDef);
        protos = protos->next;
    }

    P("}");
    P("");

}

  static void
generateType(YT(type) *type)
{
    /* TODO */
    yh_error("generateType not yet implemented");
}

  static void
recursivelyExpandScopeName(YT(scope) *scope, char *buf, char *delimiter)
{
    if (scope) {
        recursivelyExpandScopeName(scope->outer, buf, delimiter);
        if (scope->name) {
            strcat(buf, YC(symbolRef,scope->name)->name->name);
            strcat(buf, delimiter);
        }
    }
}

  void
generateRequireCode(YT(requireAtt) *require) {
    char *expr;
    //KSS printf("KSS: in plout.c:generateRequireCode()\n");
    expr = stringExpr(require->expr, NULL);
    P("        //KSS require '%s';", expr);
}

  void
expandScopeName(YT(scope) *scope, char *buf, char *delimiter)
{
    buf[0] = '\0';
    recursivelyExpandScopeName(scope, buf, delimiter);
}

  static void
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
    P("import ec.e.run.EEnvironment;");
    P("import ec.pl.runtime.*;");
    P("import ec.e.file.EStdio;");

    while (imports) {
        YT(importAtt) *import = imports->importAtt;
        P("import %s%s;", pSymbolRef(import->name),
          import->isPackageImport ? ".*" : "");
        imports = imports->next;
    }

/*    generateScopeName(unit->scope->outer);
    P("%s;", PBASENAME(unit));*/
}

  static void
generateUnitHeader(YT(unit) *unit, char *inputFileName)
{
    long timeNow = time(NULL);
    char buf[BUFLEN];

    P("/* Produced by %s", VersionString);
    strftime(buf, BUFLEN, "%d-%B-%Y %T", localtime(&timeNow));
    P("   from %s on %s", inputFileName, buf);
    P("   This file is machine generated. Don't edit it or you'll be sorry.");
    P("*/");
    P("");
    generateUnitPackage(unit);
    P("");
}

  static void
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
                    break;
                case BIND_PRESENCE_IMPL:
//KSS removing class bloat                    generatePresenceImpl(YC(presenceImpl,binding->value));
                    break;
                case BIND_PRESENCE_STRUCTURE:
//KSS removing class bloat                    generatePresenceStructure(
//KSS removing class bloat                        YC(presenceStructure,binding->value));
                    break;
                case BIND_TYPE:
                    generateType(YC(type,binding->value));
                    break;
                case BIND_UNIT:
                    break;
                case BIND_UNUM_IMPL:
                    generateUnumImpl(YC(unumImpl,binding->value));
                    break;
                case BIND_UNUM_STRUCTURE:
//KSS removing class bloat                    generateUnumStructure(YC(unumStructure,binding->value));
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
generateOutput(YT(unit) *unit, char *inputFileName, char *outputBaseDirName,
               char *saveFile)
{
    generateUnit(unit, inputFileName, outputBaseDirName, saveFile);
}

  static void
generateUnit(YT(unit) *unit, char *inputFileName, char *outputBaseDirName,
             char *saveFile)
{
    char scopeName[BUFLEN];
    char unitName[BUFLEN];
    char fileName[BUFLEN];

    if (!unit->isImported) {
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

            strcat(fileName, "d");
            if (pushOutput(fileName, NULL)) {
                generateUnitDescriptor(unit, inputFileName);
                popOutput();
            }
        }
    }
}
