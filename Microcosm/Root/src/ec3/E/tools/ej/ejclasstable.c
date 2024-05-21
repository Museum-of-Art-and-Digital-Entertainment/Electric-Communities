/*
  ejclasstable.c -- Symbol-table-like lookup for classes.

  Chip Morningstar
  Electric Communities
  29-July-1997

  Copyright 1997 Electric Communities, all rights reserved worldwide.

*/

#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "ej.h"

YT(string) *DEFINED_HERE = "<internal>";

static YT(classTable) *GlobalClasses = NULL;

static void dumpMethodDeclarator(YT(methodDeclarator) *method);
static YT(class) *lookupClassFQN(YT(unitInfo) *info, YT(name) *name);
static YT(class) *lookupClassFQNInTable(YT(classTable) *table, YT(name) *name);
static YT(class) *lookupClassUnqual(YT(unitInfo) *into, YT(name) *name,
    YT(class) **duplicate);
static YT(class) *lookupClassUnqualInTable(YT(classTable) *table,
    YT(name) *name, YT(class) **duplicate);
static YT(class) *newClass(YT(classTable) *table, YT(name) *name,
    bool isEClass, bool isInterface, bool isImport, YT(string) *location);

/**
 * cmpIds -- Test if the ids of two names are the same.
 *
 * @param name1  First name
 * @param name2  Second name
 * @returns  A number less than, equal to, or greater than 0 depending on
 *      whether the id referred to by 'name1' is lexicographically less than,
 *      equal to, or greater than the one referred to by 'name2'
 */
  static int
cmpIds(YT(name) *name1, YT(name) *name2)
{
    if (name1 == name2) {
        return(0);
    } else if (name1 == NULL) {
        return(-1);
    } else if (name2 == NULL) {
        return(1);
    } else {
        return(strcmp(name1->id->symbol->name, name2->id->symbol->name));
    }
}

/**
 * cmpNames -- Test if two names are the same.
 *
 * @param name1  First name
 * @param name2  Second name
 * @returns  A number less than, equal to, or greater than 0 depending on
 *      whether 'name1' is lexicographically less than, equal to, or greater
 *      than 'name2'
 */
  static int
cmpNames(YT(name) *name1, YT(name) *name2)
{
    if (name1 == name2) {
        return(0);
    } else if (name1 == NULL) {
        return(-1);
    } else if (name2 == NULL) {
        return(1);
    } else {
        int cmp = cmpNames(name1->prefix, name2->prefix);
        if (cmp == 0)
            return(strcmp(name1->id->symbol->name, name2->id->symbol->name));
        else
            return(cmp);
    }
}

/**
 * dumpClass -- Output (on stderr) a diagnostic dump of a class struct.
 *
 * @param theClass  The class struct to be dumped.
 */
  static void
dumpClass(YT(class) *theClass)
{
    char nameString[BUFLEN];
    YT(classBodyDeclarationList) *chaser;

    convertNameToString(theClass->className, nameString);
    fprintf(stderr, "%s%s %s %s\n",
            theClass->isEClass ? "e" : "",
            theClass->isInterface ? "interface" : "class",
            nameString,
            theClass->isImport ? "import" : "define");
    if (theClass->declaration == NULL)
        chaser = NULL;
    else if (YTAG_TEST(theClass->declaration, eclassDeclaration))
        chaser = YC(eclassDeclaration,theClass->declaration)->body->fields;
    else if (YTAG_TEST(theClass->declaration, einterfaceDeclaration))
        chaser = YC(einterfaceDeclaration,theClass->declaration)->body->fields;
    else
        chaser = NULL;
    while (chaser) {
        if (YTAG_TEST(chaser->classBodyDeclaration, emethodDeclaration))
            dumpMethodDeclarator(YC(emethodDeclaration,chaser->classBodyDeclaration)->header->declarator);
        else if (YTAG_TEST(chaser->classBodyDeclaration, emethodStub))
            dumpMethodDeclarator(YC(emethodStub,chaser->classBodyDeclaration)->header->declarator);
        chaser = chaser->next;
    }    
}

/**
 * dumpClassTable -- Output (on stderr) a diagnostic dump of a classTable
 *      struct.
 *
 * @param table  The classTable struct to be dumped.
 */
  static void
dumpClassTable(YT(classTable) *table)
{
    if (table) {
        int i;
        for (i=0; i<HASH_TABLE_SIZE; ++i) {
            YT(classBucketEntry) *chaser = table->bucketsFQN[i];
            while (chaser) {
                dumpClass(chaser->class);
                chaser = chaser->nextFQN;
            }
        }
    }
}

/**
 * dumpMethodDeclarator -- Output (on stderr) a diagnostic dump of a
 *      methodDeclarator struct.
 *
 * @param method  The methodDeclarator struct to be dumped.
 */
  static void
dumpMethodDeclarator(YT(methodDeclarator) *method)
{
    fprintf(stderr, "  %s(%s)\n",
            method->id->symbol->name,
            method->formalParameters ? "..." : "");
}

/**
 * handleClassDefine -- Do what needs to be done when a class definition is
 *      encountered.  When defining, any class supercedes what might have
 *      already been imported, so if we find the class already in the table we
 *      replace its definition. And of course if it's not there we put it
 *      there.
 *
 * @param table  The classTable struct for the class table that holds or
 *      should hold this definition.
 * @param className  A name struct for the class's name
 * @param isEClass  TRUE iff this is an eclass or einterface
 * @param isInterface  TRUE iff this an an interface or einterface
 */
  static YT(class) *
handleClassDefine(YT(classTable) *table, YT(name) *className, bool isEClass,
                  bool isInterface)
{
    YT(class) *theClass = lookupClassFQNInTable(table, className);
    if (theClass) {
        theClass->className = className;
        theClass->isEClass = isEClass;
        theClass->isInterface = isInterface;
        theClass->isImport = FALSE;
        theClass->declaration = NULL;
        theClass->location = DEFINED_HERE;
        theClass->methods = UNPROCESSED;
        return(theClass);
    } else {
        return(newClass(table, className, isEClass, isInterface, FALSE,
                        DEFINED_HERE));
    }
}

/**
 * hashId -- Compute a symbol table hash of the id referred to by a name struct
 *
 * @param name  The name to be hashed
 * @returns  An integer hash value
 */
  static longword
hashId(YT(name) *name)
{
    if (name == NULL)
        return(0);
    else
        return(yh_hashString(name->id->symbol->name) & HASH_TABLE_MASK);
}

/**
 * hashName -- Compute a symbol table hash of a name.
 *
 * @param name  A name struct containing the name to be hashed
 * @returns  An integer hash value
 */
  static longword
hashName(YT(name) *name)
{
    if (name == NULL)
        return(0);
    else
        return((hashName(name->prefix) ^ yh_hashString(name->id->symbol->name))
               & HASH_TABLE_MASK);
}

/**
 * installClass -- Put a class into a class table
 *
 * @param table  A class table
 * @param class  A class to put in it
 */
  static void
installClass(YT(classTable) *table, YT(class) *class)
{
    longword hashValue;
    YT(classBucketEntry) *chaser;
    YT(classBucketEntry) *oldChaser;
    YT(classBucketEntry) *newElem = YBUILD(classBucketEntry)(class, NULL,NULL);

#define INSTALL(hashFunc, buckets, cmpFunc, next)                             \
    hashValue = hashFunc(class->className);                                   \
    chaser = table->buckets[hashValue];                                       \
    if (chaser == NULL) {                                                     \
        table->buckets[hashValue] = newElem;                                  \
    } else {                                                                  \
        oldChaser = NULL;                                                     \
        while (chaser) {                                                      \
            int test = cmpFunc(class->className, chaser->class->className);   \
            if (test >= 0) {                                                  \
                newElem->next = chaser;                                       \
                break;                                                        \
            } else {                                                          \
                oldChaser = chaser;                                           \
                chaser = chaser->next;                                        \
            }                                                                 \
        }                                                                     \
        if (oldChaser)                                                        \
            oldChaser->next = newElem;                                        \
        else                                                                  \
            table->buckets[hashValue] = newElem;                              \
    }
    INSTALL(hashName, bucketsFQN,    cmpNames, nextFQN);
    INSTALL(hashId,   bucketsUnqual, cmpIds,   nextUnqual);

    table->count++;
}

/**
 * lookupClassFQN -- Find the class definition associated with a particular
 *      fully qualified name. The function searches all the class tables known
 *      to a given compilation unit.
 *
 * @param info  The unit info for the compilation unit being searched
 * @param name  A name struct for the FQN of the class desired
 * @returns  A class struct for the named class, or NULL if not found
 */
  static YT(class) *
lookupClassFQN(YT(unitInfo) *info, YT(name) *name)
{
    YT(classTable) *table = info->importedClasses;
    while (table) {
        YT(class) *result = lookupClassFQNInTable(table, name);
        if (result)
            return(result);
        else
            table = table->next;
    }
    return(NULL);
}

/**
 * lookupClassFQNInTable -- Find the class definition associated with a
 *      particular fully qualified name. The function searches a particular,
 *      given class table.
 *
 * @param table  The class table to be searched
 * @param name  A name struct for the FQN of the class desired
 * @returns  A class struct for the named class, or NULL if not found
 */
  static YT(class) *
lookupClassFQNInTable(YT(classTable) *table, YT(name) *name)
{
    YT(classBucketEntry) *chaser = table->bucketsFQN[hashName(name)];
    while (chaser) {
        int test = cmpNames(name, chaser->class->className);
        if (test == 0) {
            return(chaser->class);
        } else if (test > 0) {
            break;
        } else {
            chaser = chaser->nextFQN;
        }
    }
    return(NULL);
}

/**
 * lookupClassUnqual -- Find the class definition associated with a particular
 *      unqualified name. The function searches all the class tables known
 *      to a given compilation unit.
 *
 * @param info  The unit info for the compilation unit being searched
 * @param name  A name struct for the unqualified name of the class desired
 * @param duplicated  A class pointer that will point at the first duplicate
 *      name found, if the given unqualified name can ambiguously refer to more
 *      than one class
 * @returns  A class struct for the named class, or NULL if not found
 */
  static YT(class) *
lookupClassUnqual(YT(unitInfo) *info, YT(name) *name, YT(class) **duplicate)
{
    YT(classTable) *table = info->importedClasses;
    YT(class) *oldResult = NULL;

    while (table) {
        YT(class) *result = lookupClassUnqualInTable(table, name, duplicate);
        if (result) {
            if (duplicate) {
                return(result);
            } else if (oldResult) {
                *duplicate = oldResult;
                return(result);
            } else {
                oldResult = result;
            }
        }
        table = table->next;
    }
    return(oldResult);
}

/**
 * lookupClassUnqual -- Find the class definition associated with a particular
 *      unqualified name. The function searches a particular, given class table
 *
 * @param table  The class table to be searched
 * @param name  A name struct for the unqualified name of the class desired
 * @param duplicated  A class pointer that will point at the first duplicate
 *      name found, if the given unqualified name can ambiguously refer to more
 *      than one class
 * @returns  A class struct for the named class, or NULL if not found
 */
  static YT(class) *
lookupClassUnqualInTable(YT(classTable) *table, YT(name) *name,
                         YT(class) **duplicate)
{
    YT(classBucketEntry) *chaser = table->bucketsUnqual[hashId(name)];
    *duplicate = NULL;
    while (chaser) {
        int test = cmpIds(name, chaser->class->className);
        if (test == 0) {
            if (chaser->nextUnqual &&
                    cmpIds(name, chaser->nextUnqual->class->className) == 0)
                *duplicate = chaser->nextUnqual->class;
            return(chaser->class);
        } else if (test > 0) {
            break;
        } else {
            chaser = chaser->nextUnqual;
        }
    }
    return(NULL);
}

/**
 * newClass -- Create a new class struct, initialized according to the given
 *      parameters and entered into the given class table.
 *
 * @param table  The class table into which the new class should be entered
 * @param name  A name struct for the new class's name
 * @param isEClass  TRUE iff this is an eclass or einterface
 * @param isInterface  TRUE iff this an an interface or einterface
 * @param isImport  TRUE if this class is imported, FALSE if defined in input
 * @param location  The pathname of the directory containing the .class file
 * @returns  The newly created class struct
 */
  static YT(class) *
newClass(YT(classTable) *table, YT(name) *name, bool isEClass,
         bool isInterface, bool isImport, YT(string) *location)
{
    YT(class) *class;
    
    class = YBUILD(class)(name, isEClass, isInterface, isImport, NULL,
                          location, UNPROCESSED);
    installClass(table, class);
    return(class);
}

/**
 * newClassTable -- Create a new, empty class table.
 *
 * @param next  A class table to follow the new table in searching
 * @returns  The newly created classTable struct
 */
  static YT(classTable) *
newClassTable(YT(classTable) *next)
{
    YT(classBucketEntry) **bucketsFQN =
        TypeAllocMulti(YT(classBucketEntry) *, HASH_TABLE_SIZE);
    YT(classBucketEntry) **bucketsUnqual =
        TypeAllocMulti(YT(classBucketEntry) *, HASH_TABLE_SIZE);
    int i;
    for (i=0; i<HASH_TABLE_SIZE; ++i) {
        bucketsFQN[i] = NULL;
        bucketsUnqual[i] = NULL;
    }
    return(YBUILD(classTable)(bucketsFQN, bucketsUnqual, 0, next));
}

/**
 * packageClassTable -- Obtain (creating if necessary) the class table for
 *      a particular package currently being compiled.
 *
 * @param packageName  A name struct for the name of the package
 * @returns  The class table for that package
 */
  static YT(classTable) *
packageClassTable(YT(name) *packageName)
{
    static YT(activePackageList) *ActivePackages = NULL;

    YT(classTable) *result;
    YT(activePackageList) *chaser = ActivePackages;
    while (chaser) {
        if (cmpNames(packageName, chaser->activePackage->packageName) == 0)
            return(chaser->activePackage->table);
        chaser = chaser->next;
    }
    result = newClassTable(GlobalClasses);
    performImportPackage(packageName, result);
    ActivePackages =
        YBUILD(activePackageList)(
            YBUILD(activePackage)(packageName, result),
            ActivePackages);
    return(result);
}

/**
 * attachDeclaration -- Associate a particular typeDeclaration struct with a
 *      particular class.
 *
 * @param theClass  A class struct for the class being declared
 * @param typeDeclaration  A type declaration to be attached to 'theClass'
 */
  void
attachDeclaration(YT(class) *theClass, YT(typeDeclaration) *declaration)
{
    theClass->declaration = declaration;
}

/**
 * defaultImports -- Inform ourselves about the packages which are imported
 *      automatically by the E/Java compiler by default. For now these are
 *      ec.e.run, ec.e.lang and java.lang.
 */
  void
defaultImports(void)
{
    YT(name) *ec        = SYNTH_NAME(NULL, "ec");
    YT(name) *ec_e      = SYNTH_NAME(ec,   "e");
    YT(name) *ec_e_run  = SYNTH_NAME(ec_e, "run");
    YT(name) *ec_e_lang = SYNTH_NAME(ec_e, "lang");
    YT(name) *java      = SYNTH_NAME(NULL, "java");
    YT(name) *java_lang = SYNTH_NAME(java, "lang");
    GlobalClasses = newClassTable(NULL);
    performImportPackage(ec_e_run,  GlobalClasses);
    performImportPackage(ec_e_lang, GlobalClasses);
    performImportPackage(java_lang, GlobalClasses);
}

/**
 * defineClass -- Handle the class definition of a class which is being defined
 *      here for the first time (i.e., not a class being imported).
 *
 * @param unitInfo  The unit info struct for the current compilation unit
 * @param id  The id of the newly defined class
 * @param isEClass  TRUE iff this is an eclass or einterface
 * @param isInterface  TRUE iff this an an interface or einterface
 * @returns  A class struct for the newly defined class
 */
  YT(class) *
defineClass(YT(unitInfo) *unitInfo, YT(identifier) *id, bool isEClass,
            bool isInterface)
{
    return(handleClassDefine(unitInfo->packageClasses,
                             YBUILD(name)(unitInfo->packageName, "", id),
                             isEClass,
                             isInterface));
}

/**
 * dumpClasses -- Output (on stderr) a diagnostic dump of all the classes
 *      known to a given compilation unit.
 *
 * @param unitInfo  The unitInfo struct of the unit to be dumped.
 */
  void
dumpClasses(YT(unitInfo) *unitInfo)
{
    dumpClassTable(GlobalClasses);
    dumpClassTable(unitInfo->packageClasses);
    dumpClassTable(unitInfo->importedClasses);
}

/**
 * findClass -- Find the class definition associated with a particular name.
 *      It is not known a priori whether the given name is an FQN or not. The
 *      function searches all the class tables known to a given compilation
 *      unit. Imports classes as needed. Also generates a user-level error
 *      message in the case of ambiguous name references.
 *
 * @param info  The unit info for the compilation unit being searched
 * @param name  A name struct for the name of the class desired
 * @returns  A class struct for the named class, or NULL if not found
 */
  YT(class) *
findClass(YT(unitInfo) *unitInfo, YT(name) *name)
{
    YT(class) *result = lookupClassFQN(unitInfo, name);
    YT(class) *duplicate;

    if (result) {
        return(result);
    } else if (name->prefix == NULL) {
        result = lookupClassUnqual(unitInfo, name, &duplicate);
        if (duplicate) {
            char nameStr1[BUFLEN];
            char nameStr2[BUFLEN];
            convertNameToString(result->className, nameStr1);
            convertNameToString(duplicate->className, nameStr2);
            yh_error("ambiguous class reference %s: %s or %s?",
                     name->id->symbol->name, nameStr1, nameStr2);
            return(NULL);
        } else {
            return(result);
        }
    } else {
        performImportClass(name, unitInfo);
        return(lookupClassFQN(unitInfo, name));
    }
}

/**
 * getFQN -- Return the fully qualified expansion of a given name.
 *
 * @param info  The unit info for the compilation unit being searched
 * @param name  A name struct for the name whose FQN is desired
 * @returns  A name struct (possibly the same as 'name') which is the desired
 *      FQN, or NULL if the class can't be found.
 */
  YT(name) *
getFQN(YT(unitInfo) *unitInfo, YT(name) *name)
{
    if (name->prefix) {
        return(name);
    } else {
        YT(class) *theClass = findClass(unitInfo, name);
        if (theClass)
            return(theClass->className);
        else
            return(name);
    }
}

/**
 * handleClassImport -- Create a new class as it is imported. When importing,
 *      we are walking the CLASSPATH, so earlier definitions shadow later ones.
 *      Thus, if the class is already in the table we do nothing, but if it's
 *      not there we add it.
 * @param table  The class table to add the new class to
 * @param className  The new class's name
 * @param isEClass  TRUE iff this is an eclass or einterface
 * @param location  The pathname of the directory containing the .class file
 */
  void
handleClassImport(YT(classTable) *table, YT(name) *className, bool isEClass,
                  YT(string) *location)
{
    if (!lookupClassFQNInTable(table, className))
        newClass(table, className, isEClass, FALSE, TRUE, location);
}

/**
 * isEClass -- Test if a named class is an E class (eclass or einterface)
 *
 * @param unitInfo  The unit info struct for the current compilation unit
 * @param name  A name struct for the name of the class of interest
 * @returns TRUE iff the named class is a eclass or einterface.
 */
  bool
isEClass(YT(unitInfo) *unitInfo, YT(name) *name)
{
    YT(class) *theClass = findClass(unitInfo, name);
    if (theClass)
        return(theClass->isEClass);
    else
        return(FALSE);
}

/**
 * isEClassType -- Test if a given type is an E class (eclass or einterface)
 *
 * @param unitInfo  The unit info struct for the current compilation unit
 * @param type  A type struct for the entity of interest
 * @returns TRUE iff the type is a eclass or einterface.
 */
  bool
isEClassType(YT(unitInfo) *unitInfo, YT(type) *type)
{
    if (YTAG_TEST(type, name))
        return(isEClass(unitInfo, YC(name, type)));
    else
        return(FALSE);
}

/**
 * newUnitInfo -- Construct a new unitInfo struct for a compilation unit.
 *
 * @param packageName  The name of the package being compiled
 * @returns  The new unitInfo struct
 */
  YT(unitInfo) *
newUnitInfo(YT(name) *packageName)
{
    YT(classTable) *packageClasses = packageClassTable(packageName);
    return(YBUILD(unitInfo)(packageName,
                            newClassTable(packageClasses),
                            packageClasses,
                            NULL, NULL));
}
