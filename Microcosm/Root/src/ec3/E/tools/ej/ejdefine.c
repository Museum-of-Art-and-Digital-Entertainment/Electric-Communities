/*
  ejdefine.c -- Class & method definition handling for the E-to-Java translator

  Chip Morningstar
  Electric Communities
  30-July-1997

  Copyright 1997 Electric Communities, all rights reserved worldwide.

*/

#include "generic.h"
#include "yh.h"
#include "yh_struct.h"
#include "yh_build.h"
#include "ej.h"
#include "y.tab.h"
#include "string.h"

YT(stringList) *UNPROCESSED = YC(stringList, "<UNPROCESSED>");

static void computeTypeSignature(YT(unitInfo) *unitInfo, YT(type) *type,
    char *signature);
static YT(class) *findMethodUpHierarchy(YT(unitInfo) *unitInfo,
    YT(class) *theClass, char *signature);
static YT(emethodStub) *internalizeEMethodStub(char *name, char *descriptor);
static YT(formalParameterList) *internalizeFormalParameters(char *descriptor);
static YT(nameSequence) *internalizeInterfaces(YT(classFile) *classFile);
static YT(name) *internalizeSuperclass(YT(classFile) *classFile);
static YT(type) *internalizeTypeDescriptor(char **descriptorptr);
static bool isMethodOfClass(YT(class) *theClass, char *signature);
static YT(variableDeclarator) *synthesizeParamDeclarator(int index);

/**
 * computeMethodSignature -- compute a signature string for a method, suitable
 *      for equality comparison (e.g., to determine if a method implemented in
 *      a class matches a method declared in an interface).  The signature
 *      consists of the message name followed by the standard Java method type
 *      signature sans return type.  For example "foo(ILjava/lang/Object)"
 *      would be the signature for "emethod foo(int i, Object o) {...".
 *
 * @param unitInfo  Unit info for current compilation unit
 * @param declarator  A methodDeclarator struct for the method
 * @param signature  A buffer into which the result will be placed
 */
  static void
computeMethodSignature(YT(unitInfo) *unitInfo,
                       YT(methodDeclarator) *declarator, char *signature)
{
    YT(formalParameterList) *chaser = declarator->formalParameters;
    sprintf(signature, "%s(", declarator->id->symbol->name);
    while (chaser) {
        computeTypeSignature(unitInfo, chaser->formalParameter->type,
                             signature);
        chaser = chaser->next;
    }
    strcat(signature, ")");
}

/**
 * computeTypeSignature -- compute a standard Java type signature string given
 *      a type struct.
 *
 * @param unitInfo  Unit info for current compilation unit
 * @param type  Type struct for the type whose signature is desired
 * @param signature  A buffer into which the result will be placed
 */
  static void
computeTypeSignature(YT(unitInfo) *unitInfo, YT(type) *type, char *signature)
{
    switch (YTAG_OF(type)) {
        case YTAG(name): {
            char path[BUFLEN];
            convertNameToPath(getFQN(unitInfo, YC(name,type)), path);
            strcat(signature, "L");
            strcat(signature, path);
            strcat(signature, ";");
            break;
        }
        case YTAG(arrayType): {
            YT(bracketsList) *dimensions = YC(arrayType,type)->dimensions;
            while (dimensions) {
                strcat(signature, "[");
                dimensions = dimensions->next;
            }
            computeTypeSignature(unitInfo, YC(arrayType,type)->baseType,
                                 signature);
            break;
        }
        case YTAG(primType): {
            char *tag;
            switch (YC(primType,type)->type) {
                case BOOLEAN: tag = "Z"; break;
                case BYTE:    tag = "B"; break;
                case CHAR:    tag = "C"; break;
                case DOUBLE:  tag = "D"; break;
                case FLOAT:   tag = "F"; break;
                case INT:     tag = "I"; break;
                case LONG:    tag = "J"; break;
                case SHORT:   tag = "S"; break;
                default:
                    yh_error("invalid primType %d for signature",
                             YC(primType,type)->type);
                    tag = "?";
            }
            strcat(signature, tag);
            break;
        }
        default:
            yh_error("invalid type %d for signature", YTAG_OF(type));
    }
}

/**
 * convertPathToName -- compute a name struct given a class file path string,
 *      e.g., as found in a Java type signature. The path string may be
 *      terminated either by a nul or by a semicolon.
 *
 * @param path  A pointer to the path string. On exit will point to the
 *      character after the terminator.
 * @returns  A name struct representing the given path
 */
  static YT(name) *
convertPathToName(char **path)
{
    char *end;
    char *start = *path;
    char terminator = '/';
    YT(name) *result = NULL;

    while (terminator != '\0' && terminator != ';') {
        for (end=start; *end != '\0' && *end != '/' && *end != ';'; ++end)
            ;
        terminator = *end;
        *end = '\0';
        result = SYNTH_NAME(result, start);
        *end = terminator;
        start = end + 1;
    }
    *path = start;
    return(result);
}

/**
 * findMethodInClass -- Determine if a given class directly or indirectly
 *      declares or implements a method.
 *
 * @param unitInfo  Unit info for current compilation unit
 * @param className  The name of the class being scrutinized
 * @param signature  The signature of the desired method
 * @returns  If the method is found, the class struct for the class it was
 *      found in (which actually may be a superclass or superinterface of
 *      'className'). If the method is not found, NULL.
 */
  static YT(class) *
findMethodInClass(YT(unitInfo) *unitInfo, YT(name) *className, char *signature)
{
    YT(class) *theClass = findClass(unitInfo, className);
    if (theClass) {
        internalizeClass(unitInfo, theClass);
        if (isMethodOfClass(theClass, signature))
            return(theClass);
        else
            return(findMethodUpHierarchy(unitInfo, theClass, signature));
    } else {
        return(NULL);
    }
}

/**
 * findMethodUpHierarchy -- Determine if a method is implemented or declared
 *      somewhere in a class's inheritance tree.
 *
 * @param unitInfo  Unit info for current compilation unit
 * @param theClass  Class struct of the class being scrutinized
 * @param signature  The signature of the desired method
 * @returns  If the method is found, the class struct for the class it was
 *      found in (which actually may be a superclass or superinterface of
 *      'theClass'). If the method is not found, NULL.
 */
  static YT(class) *
findMethodUpHierarchy(YT(unitInfo) *unitInfo, YT(class) *theClass,
                      char *signature)
{
    if (theClass && theClass->declaration) {
        YT(class) *hit;
        switch (YTAG_OF(theClass->declaration)) {
            case YTAG(classDeclaration):
            case YTAG(eclassDeclaration): {
                YT(classDeclaration) *decl =
                    YC(classDeclaration,theClass->declaration);
                if (decl->extends) {
                    hit = findMethodInClass(unitInfo,
                                            decl->extends->extendTypeName,
                                            signature);
                    if (hit)
                        return(hit);
                }
                if (decl->implements) {
                    YT(nameSequence) *chaser =
                        decl->implements->implementTypes;
                    while (chaser) {
                        hit = findMethodInClass(unitInfo,
                                                chaser->tail,
                                                signature);
                        if (hit)
                            return(hit);
                        chaser = chaser->head;
                    }
                }
                return(NULL);
            }
            case YTAG(interfaceDeclaration):
            case YTAG(einterfaceDeclaration): {
                YT(interfaceDeclaration) *decl =
                    YC(interfaceDeclaration,theClass->declaration);
                if (decl->extends) {
                    YT(nameSequence) *chaser = decl->extends->extendTypes;
                    while (chaser) {
                        hit = findMethodInClass(unitInfo,
                                                chaser->tail,
                                                signature);
                        if (hit)
                            return(hit);
                        chaser = chaser->head;
                    }
                }
                return(NULL);
            }
            default:
                yh_error("<internal> bad class decl type %d in findMethod",
                         YTAG_OF(theClass->declaration));
                return(NULL);
        }
    } else {
        return(NULL);
    }
}

/**
 * internalizeClassFile -- Fill in the information about a class given the
 *      parsed .class file information.
 *
 * @param unitInfo  Unit info for current compilation unit
 * @param theClass  A class struct for the class being internalized
 * @param classFile  A classFile struct for the classfile describing the class
 */
  static void
internalizeClassFile(YT(unitInfo) *unitInfo, YT(class) *theClass,
                     YT(classFile) *classFile)
{
    int i;
    YT(stringList) *methodList = NULL;
    YT(classBodyDeclarationList) *fields = NULL;

    for (i=0; i<classFile->methods_count; ++i) {
        char *name = getMethodName(classFile, i);
        if (stripTail(name, "$async")) {
            int j;
            char signature[BUFLEN];
            char *descriptor = getMethodDescriptor(classFile, i);
            for (j=0; descriptor[j]!=')'; ++j)
                ;
            descriptor[j] = '\0';
            sprintf(signature, "%s%s)", name, descriptor);
            methodList = YBUILD(stringList)(STRDUP(signature), methodList);
            fields = YBUILD(classBodyDeclarationList)(
                YC(classBodyDeclaration,
                   internalizeEMethodStub(name, descriptor)),
                fields);
        }
    }
    theClass->methods = methodList;
    if (theClass->isEClass) {
        if (theClass->isInterface) {
            theClass->declaration =
                YC(typeDeclaration,YBUILD(einterfaceDeclaration)(
                    NULL,
                    " ",
                    theClass->className->id,
                    YBUILD(interfaceExtends)("",
                                             internalizeInterfaces(classFile)),
                    YBUILD(classBody)("", fields, ""),
                    theClass));
        } else {
            theClass->declaration =
                YC(typeDeclaration, YBUILD(eclassDeclaration)(
                    NULL,
                    " ",
                    theClass->className->id,
                    YBUILD(extends)("", internalizeSuperclass(classFile)),
                    YBUILD(implements)("", internalizeInterfaces(classFile)),
                    YBUILD(classBody)("", fields, ""),
                    theClass));
        }
    }
}

/**
 * internalizeClassName -- Create a name struct for the a class name, given
 *      a classFile struct and an index into that class file's constant pool.
 *
 * @param classFile  A parsed class file
 * @param refnum  An index into 'classFile's constant pool
 * @returns  A name struct for the class name index by 'refnum'
 */
  static YT(name) *
internalizeClassName(YT(classFile) *classFile, int refnum)
{
    YT(cp_info) *cpInfo = classFile->constant_pool[refnum];
    if (YTAG_TEST(cpInfo, constant_class_info)) {
        YT(constant_class_info) *classInfo = YC(constant_class_info,cpInfo);
        cpInfo = classFile->constant_pool[classInfo->name_index];
        if (YTAG_TEST(cpInfo, constant_utf8_info)) {
            YT(constant_utf8_info) *utf8 = YC(constant_utf8_info,cpInfo);
            char nameBuf[BUFLEN];
            char *nameBufPtr = nameBuf;
            strncpy(nameBuf, utf8->bytes, utf8->length);
            nameBuf[utf8->length] = '\0';
            if (strTailMatch(nameBuf, "_$_Intf"))
                return(NULL);
            else
                return(convertPathToName(&nameBufPtr));
        } else {
            yh_error("<internal> expected constant_utf8_info");
            return(NULL);
        }
    } else {
        yh_error("<internal> expected constant_class_info");
        return(NULL);
    }
}

/**
 * internalizeEMethodStub -- Generate an emethodStub struct given the emethod
 *      name and a Java parameter list signature for its parameters.
 *
 * @param name  The emethod name
 * @param descriptor  A Java parameter list signature for the parameters
 */
  static YT(emethodStub) *
internalizeEMethodStub(char *name, char *descriptor)
{
    YT(identifier) *id = SYNTH_ID(name);
    YT(formalParameterList) *params = internalizeFormalParameters(descriptor);
    YT(methodDeclarator) *decl =
        YBUILD(methodDeclarator)(id, "", params, "", NULL);
    YT(emethodHeader) *header = YBUILD(emethodHeader)(NULL, " ", decl, NULL);
    YT(emethodStub) *result = YBUILD(emethodStub)("", header, "");
    return(result);
}

/**
 * internalizeFormalParameters -- Generate a formalParameterList struct given
 *      a Java type signature for a parameter list.
 *
 * @param descriptor  The Java parameter list signature
 * @returns  A formalParameterList struct for that parameter list
 */
  static YT(formalParameterList) *
internalizeFormalParameters(char *descriptor)
{
    YT(formalParameterList) *result = NULL;
    int count = 0;

    if (*descriptor != '(') {
        yh_error("bad method descriptor '%s' in class file", descriptor);
        return(NULL);
    } else {
        ++descriptor;
    }
    while (*descriptor) {
        YT(type) *type = internalizeTypeDescriptor(&descriptor);
        YT(formalParameter) *param =
            YBUILD(formalParameter)(result ? "" : NULL,
                                    type,
                                    synthesizeParamDeclarator(count++));
        result = YBUILD(formalParameterList)(param, result);
    }
    return(result);
}

/**
 * internalizeInterfaces -- Given a class file, generate a list of the names of
 *      all the interfaces that class implements.
 *
 * @param classFile  A classFile struct
 * @returns  A nameSequence struct listing all the interfaces (directly)
 *      implemented by the class in 'classFile'
 */
  static YT(nameSequence) *
internalizeInterfaces(YT(classFile) *classFile)
{
    YT(nameSequence) *result = NULL;
    int i;
    
    for (i=0; i<classFile->interfaces_count; ++i) {
        YT(name) *name = internalizeClassName(classFile,
                                              classFile->interfaces[i]);
        if (name)
            result = YBUILD(nameSequence)(result, result ? "" : NULL, name);
    }
    return(result);
}

/**
 * internalizeMethods -- Generate a list of the signatures of all the methods
 *      declared or implemented by a class body.
 *
 * @param unitInfo  Unit info for current compilation unit
 * @param body  A classBody struct
 * @returns  A list of the signatures of the emethods declared or implemented
 *      in 'body'
 */
  static YT(stringList) *
internalizeMethods(YT(unitInfo) *unitInfo, YT(classBody) *body)
{
    YT(classBodyDeclarationList) *chaser = body->fields;
    YT(stringList) *result = NULL;
    char signature[BUFLEN];

    while (chaser) {
        YT(emethodHeader) *header;
        if (YTAG_TEST(chaser->classBodyDeclaration, emethodDeclaration)) {
            header =
                YC(emethodDeclaration,chaser->classBodyDeclaration)->header;
        } else if (YTAG_TEST(chaser->classBodyDeclaration, emethodStub)) {
            header = YC(emethodStub,chaser->classBodyDeclaration)->header;
        } else {
            header = NULL;
        }
        if (header) {
            computeMethodSignature(unitInfo, header->declarator, signature);
            result = YBUILD(stringList)(STRDUP(signature), result);
        }
        chaser = chaser->next;
    }
    return(result);
}

/**
 * internalizeSuperclass -- Given a classfile, generate a name struct for the
 *      superclass of that class.
 *
 * @param classFile  A classFile struct
 * @returns  A name struct for the superclass of the class in 'classFile'
 */
  static YT(name) *
internalizeSuperclass(YT(classFile) *classFile)
{
    return(internalizeClassName(classFile, classFile->super_class));
}

/**
 * internalizeTypeDescriptor -- Generate a type struct given a Java type
 *      signature.
 *
 * @param descriptorptr  Pointer to the Java signature string. On exit will
 *      point at the next character after the signature.
 * @returns  A type struct for that type.
 */
  static YT(type) *
internalizeTypeDescriptor(char **descriptorptr)
{
    char *descriptor = *descriptorptr;
    YT(bracketsList) *dimensions = NULL;
    YT(type) *result;

    while (*descriptor == '[') {
        ++descriptor;
        dimensions = YBUILD(bracketsList)(YBUILD(brackets)("", ""),
                                          dimensions);
    }
    if (*descriptor == 'L') {
        ++descriptor;
        result = YC(type,convertPathToName(&descriptor));
    } else {
        int prim;
        switch (*descriptor++) {
            case 'Z': prim = BOOLEAN; break;
            case 'B': prim = BYTE;    break;
            case 'C': prim = CHAR;    break;
            case 'D': prim = DOUBLE;  break;
            case 'F': prim = FLOAT;   break;
            case 'I': prim = INT;     break;
            case 'J': prim = LONG;    break;
            case 'S': prim = SHORT;   break;
            default:
                yh_error("invalid prim type signature %c", *--descriptor);
                ++descriptor;
                prim = VOID;
                break;
        }
        result = YC(type,YBUILD(primType)(prim, " "));
    }
    if (dimensions)
        result = YC(type,YBUILD(arrayType)(result, dimensions));
    *descriptorptr = descriptor;
    return(result);
}

/**
 * isMethodOfClass -- Test if a given class implements or declares a particular
 *      method
 *
 * @param theClass  A class struct for the class in question
 * @param signature  The signature of the method of interest
 * @returns  TRUE iff 'signature' describes a method found in 'theClass'
 */
  static bool
isMethodOfClass(YT(class) *theClass, char *signature)
{
    YT(stringList) *chaser;
    chaser = theClass->methods;
    while (chaser) {
        if (strcmp(chaser->string, signature) == 0)
            return(TRUE);
        else
            chaser = chaser->next;
    }
    return(FALSE);
}

/**
 * synthesizeParamDeclarator -- Produce a fake variableDeclarator struct for
 *      an argument to a synthesized method. Basically, the parameter is named
 *      arg_$_<n>, where n is given.
 *
 * @param index  The parameter number, n
 * @returns  The synthesizes variableDeclarator struct
 */
  static YT(variableDeclarator) *
synthesizeParamDeclarator(int index)
{
    char synthName[BUFLEN];

    sprintf(synthName, "arg_$_%d", index);
    return(YBUILD(variableDeclarator)(
        NULL,
        SYNTH_ID(synthName),
        NULL,
        NULL,
        NULL));
}

/**
 * internalizeClass -- By hook or by crook, make sure that we have the method
 *      list for a given class, regardless of whether the class is defined in
 *      a compilation unit currently being processed or by an imported .class
 *      file. This includes actually reading and parsing the imported .class
 *      file if necessary.
 *
 * @param unitInfo  Unit info for current compilation unit
 * @param theClass  A class struct for the possibly-not-yet-internalized class
 *      in question
 */
  void
internalizeClass(YT(unitInfo) *unitInfo, YT(class) *theClass)
{
    if (theClass->methods == UNPROCESSED) {
        if (theClass->location == DEFINED_HERE) {
            if (theClass->declaration) {
                if (YTAG_TEST(theClass->declaration, classDeclaration) ||
                        YTAG_TEST(theClass->declaration, eclassDeclaration)) {
                    theClass->methods = internalizeMethods(unitInfo,
                        YC(classDeclaration,theClass->declaration)->body);
                } else if (YTAG_TEST(theClass->declaration, interfaceDeclaration) ||
                        YTAG_TEST(theClass->declaration, einterfaceDeclaration)) {
                    theClass->methods = internalizeMethods(unitInfo,
                        YC(interfaceDeclaration,theClass->declaration)->body);
                } else {
                    yh_error("<internal> invalid class decl type %d in internalizeClass",
                             YTAG_OF(theClass->declaration));
                    theClass->methods = NULL;
                }
            } else {
                yh_error("<internal> class has no declaration!");
                theClass->methods = NULL;
            }
        } else {
            char filename[BUFLEN];
            char classSubpath[BUFLEN];
            FILE *fyle;
            
            convertNameToPath(theClass->className, classSubpath);
            sprintf(filename, "%s/%s.class", theClass->location, classSubpath);
            fyle = fopen(filename, "r");
            if (fyle) {
                YT(classFile) *classFile = readClassFile(fyle);
                internalizeClassFile(unitInfo, theClass, classFile);
                freeClassFile(classFile);
                fclose(fyle);
            } else {
                yh_error("unable to open class file %s", filename);
            }
        }
    }
}

/**
 * isFirstEMethodDefinition -- Test if a given emethod is defined for the
 *      first time in a given class (i.e., it is not defined by one of the
 *      class's superclasses or interfaces).
 *
 * @param unitInfo  Unit info for current compilation unit
 * @param theClass  A class struct for the class of interest
 * @param header  An emethodHeader struct describing the method being asked
 *      about
 */
  bool
isFirstEMethodDefinition(YT(unitInfo) *unitInfo, YT(class) *theClass,
                         YT(emethodHeader) *header)
{
    char signature[BUFLEN];

    computeMethodSignature(unitInfo, header->declarator, signature);
    if (findMethodUpHierarchy(unitInfo, theClass, signature))
        return(FALSE);
    else
        return(TRUE);
}
