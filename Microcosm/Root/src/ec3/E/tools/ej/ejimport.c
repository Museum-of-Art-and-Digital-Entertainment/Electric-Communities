/*
  ejimport.c -- Class importation for the E-to-Java translator

  Chip Morningstar
  Electric Communities
  18-July-1997

  Copyright 1997 Electric Communities, all rights reserved worldwide.

*/

#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "ejdirent.h"
#include <errno.h>
#include "ej.h"
#include <string.h>

static void notePackageImport(YT(name) *packageName,
     YT(fileImportList) *imports, YT(classTable) *table, YT(string) *location);
static YT(fileImportList) *notePossibleClassImport(YT(fileImportList) *imports,
    char *filename);

/**
 * demangleClassFileName -- Given the name of a class file, find the name of
 *      the Java or E class that produced to it.
 *
 * @param nameIn  The name of the class file
 * @param nameOut  A buffer into which will be placed the unmanged name
 * @returns  The name format of the class file
 */
  static t_nameFormat
demangleClassFileName(char *nameIn, char *nameOut)
{
    strcpy(nameOut, nameIn);
    if (stripTail(nameOut, "_$_Intf.class"))
        return(NAME_INTF);
    else if (stripTail(nameOut, "_$_Impl.class"))
        return(NAME_IMPL);
    else if (stripTail(nameOut, "_$_Channel.class"))
        return(NAME_CHANNEL);
    else if (stripTail(nameOut, "_$_Deflector.class"))
        return(NAME_DEFLECTOR);
    else if (stripTail(nameOut, "_$_Proxy.class"))
        return(NAME_PROXY);
    else if (stripTail(nameOut, "_$_Sealer.class"))
        return(NAME_SEALER);
    else if (stripTail(nameOut, ".class"))
        return(NAME_PLAIN);
    else
        return(NAME_ERROR);
}

/**
 * importPackageDirectory -- Take note of all the classes in a directory
 *      corresponding to some Java package.
 *
 * @param packageName  A name struct naming the package in question
 * @param dirname  The directory name string
 * @param table  The class table in which the package's classes are to be noted
 * @returns  Success flag, TRUE->success
 */
  static bool
importPackageDirectory(YT(name) *packageName, char *dirname,
                       YT(classTable) *table)
{
    DIR *packageDyr;
    struct dirent *entry;
    YT(fileImportList) *fileImports = NULL;

    packageDyr = opendir(dirname);
    if (packageDyr) {
        while (entry = readdir(packageDyr)) {
            if (strTailMatch(entry->d_name, ".class"))
                fileImports =
                    notePossibleClassImport(fileImports, entry->d_name);
        }
        closedir(packageDyr);
        notePackageImport(packageName, fileImports, table, dirname);
        return(TRUE);
    } else if (errno != ENOENT) {
        yh_error("unable to open package directory %s", dirname);
        return(FALSE);
    }
    return(FALSE);
}

/**
 * notePackageImport -- Having scanned a package directory to find out what
 *      classes are in it, actually import the classes.
 *
 * @param packageName  A name struct naming the package in question
 * @param imports  A list of the .class files imported
 * @param table  The class table in which the classes are to be noted
 * @param location  The directory name string
 */
  static void
notePackageImport(YT(name) *packageName, YT(fileImportList) *imports,
                  YT(classTable) *table, YT(string) *location)
{
    if (imports)
        location = STRDUP(location);
    while (imports) {
        YT(fileImport) *import = imports->fileImport;
        YT(name) *className =
            YBUILD(name)(
                packageName,
                "",
                YBUILD(identifier)("", yh_handleSymbol(import->className)));
        handleClassImport(table, className, import->isEClass, location);
        imports = imports->next;
    }
}

/**
 * notePossibleClassImport -- Given a filename, check if it should be imported
 *      and add it to an ongoing list if it should.  We only import plain
 *      Java classes and the _$_Intf version of E classes.
 *
 * @param imports  A fileImportList under construction
 * @param filename  The name of the file that maybe should be imported
 * @returns  'imports', possibly augmented with 'filename'
 */
  static YT(fileImportList) *
notePossibleClassImport(YT(fileImportList) *imports, char *filename)
{
    char demangledName[BUFLEN];
    t_nameFormat format = demangleClassFileName(filename, demangledName);

    if (format == NAME_INTF || format == NAME_PLAIN) {
        YT(fileImportList) *chaser = imports;
        while (chaser) {
            YT(fileImport) *import = chaser->fileImport;
            if (strcmp(import->className, demangledName) == 0) {
                if (format == NAME_INTF)
                    import->isEClass = TRUE;
                return(imports);
            } else {
                chaser = chaser->next;
            }
        }
        return(YBUILD(fileImportList)(
            YBUILD(fileImport)(STRDUP(demangledName), format == NAME_INTF),
            imports));
    }
    return(imports);
}

/**
 * performImportClass -- Import a single class.
 *
 * @param unitInfo  Unit info struct for the current compilation unit
 * @param className  Name struct for the name of the class to import
 */
  void
performImportClass(YT(name) *className, YT(unitInfo) *unitInfo)
{
    char classFilePath[BUFLEN];
    YT(stringList) *pathChaser = ClassPath;
    bool stillSearching = TRUE;

    convertNameToPath(className, classFilePath);

    while (pathChaser && stillSearching) {
        char filename[BUFLEN];

        if (!strTailMatch(pathChaser->string, ".zip")) {
            /* Eventually we need to parse .zip and .jar files */
            snprintf(filename, BUFLEN, "%s/%s_$_Intf.class", pathChaser->string,
                    classFilePath);
            if (fileExists(filename)) {
                handleClassImport(unitInfo->importedClasses, className, TRUE,
                                  pathChaser->string);
                stillSearching = FALSE;
            } else {
                snprintf(filename, BUFLEN, "%s/%s.class", pathChaser->string,
                        classFilePath);
                if (fileExists(filename)) {
                    handleClassImport(unitInfo->importedClasses, className,
                                      FALSE, pathChaser->string);
                    stillSearching = FALSE;
                }
            }
        }
        pathChaser = pathChaser->next;
    }
}

/**
 * performImportPackage -- Import all the classes in a package.
 *
 * @param unitInfo  Unit info struct for the current compilation unit
 * @param packageName  Name struct for the name of the package to import
 */
  void
performImportPackage(YT(name) *packageName, YT(classTable) *table)
{
    char packageDirPath[BUFLEN];
    YT(stringList) *pathChaser = ClassPath;
    bool found = FALSE;

    if (packageName) {
        convertNameToPath(packageName, packageDirPath);
        
        while (pathChaser) {
            char dirname[BUFLEN];
            if (!strTailMatch(pathChaser->string, ".zip")) {
                /* Eventually we need to parse .zip and .jar files */
                snprintf(dirname, BUFLEN, "%s/%s", pathChaser->string, packageDirPath);
                if (importPackageDirectory(packageName, dirname, table))
                    found = TRUE;
            }
            pathChaser = pathChaser->next;
        }
    } else {
        importPackageDirectory(NULL, ".", table);
    }
}

/**
 * stripTail -- Look for a pattern at the end of a string, and remove it if
 *      found.
 *
 * @param str  The string to be tested and possibly stripped
 * @param pattern  The tail substring to look for
 * @returns TRUE iff the pattern was found and stripped
 */
  bool
stripTail(char *str, char *pattern)
{
    if (strTailMatch(str, pattern)) {
        str[strlen(str) - strlen(pattern)] = '\0';
        return(TRUE);
    } else {
        return(FALSE);
    }
}

/**
 * strTailMatch -- Look for a pattern at the end of a string.
 *
 * @param str  The string to be tested
 * @param pat  The tail substring to look for
 * @returns  TRUE iff the pattern was found
 */
  bool
strTailMatch(char *str, char *pat)
{
    int patLen = strlen(pat);
    int strLen = strlen(str);

    bool result = (strLen > patLen && strcmp(str + strLen - patLen, pat) == 0);
    return(result);
}    
