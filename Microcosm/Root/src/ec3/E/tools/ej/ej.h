/*
  ej.h -- Types, macros, globals & function protos for E-to-Java translator

  Chip Morningstar
  Electric Communities
  17-July-1997

  Copyright 1997 Electric Communities, all rights reserved worldwide.

*/

#ifdef WIN32
#define WRONG_ENDIAN
#define PATH_SEPARATOR_CHAR     ';'
#else
#define PATH_SEPARATOR_CHAR     ':'
#endif

#define ERROR_CONTEXT(ptr) yh_FileName = ptr->filename; \
yh_LineNumber = ptr->lineNumber
#define NO_ERROR_CONTEXT() yh_FileName = NULL; yh_LineNumber = 0

extern bool EDebug;
extern bool LegibleOutput;
extern bool OutputToStdio;
extern bool Verbose;
extern YT(stringList) *ClassPath;
extern YT(unitInfo) *CurrentUnitInfo;

#define SYNTH_ID(str) YBUILD(identifier)(" ", yh_handleSymbol(str))
#define SYNTH_NAME(prefix,str) YBUILD(name)(prefix, (prefix==NULL) ? NULL : "", SYNTH_ID(str))

extern YT(string) *DEFINED_HERE;
extern YT(stringList) *UNPROCESSED;

typedef enum {
    CLASS_CHANNEL,
    CLASS_DEFLECTOR,
    CLASS_IMPL,
    CLASS_INTF,
    CLASS_PLAIN,
    CLASS_PROXY,
    CLASS_SEALER,
    CLASS_JAVA,
    CLASS_NONE
} t_classFormat;

typedef enum {
    HEADER_ENQUEUE_METHOD,
    HEADER_INVOCATION_METHOD,
    HEADER_PLAIN,
    HEADER_PLAIN_ASYNC,
    HEADER_SEALER_DECL,
    HEADER_SEALER_INVOKE_CASE,
    HEADER_SEALER_TABLE_ASSIGN,
    HEADER_NONE
} t_headerFormat;

typedef enum {
    METHOD_DONT_CALL,
    METHOD_ENQUEUE_METHOD,
    METHOD_IMPLEMENTATION,
    METHOD_INVOCATION_METHOD,
    METHOD_SEALER_DECL,
    METHOD_SEALER_INVOKE_CASE,
    METHOD_SEALER_TABLE_ASSIGN,
    METHOD_STUB,
    METHOD_STUB_ASYNC,
    METHOD_NONE
} t_methodFormat;

typedef enum {
    MODIFIER_NORMAL,
    MODIFIER_WHITESPACE
} t_modifierFormat;

typedef enum {
    NAME_CHANNEL,       /* <name>_$_Channel   */
    NAME_DEFLECTOR,     /* <name>_$_Deflector */
    NAME_IMPL,          /* <name>_$_Impl      */
    NAME_INTF,          /* <name>_$_Intf      */
    NAME_PLAIN,         /* <name>             */
    NAME_PROXY,         /* <name>_$_Proxy     */
    NAME_SEALER,        /* <name>_$_Sealer    */
    NAME_JAVA,
    NAME_NONE,
    NAME_ERROR
} t_nameFormat;

typedef enum {
    PARAM_ASSIGN_ARG_VAR,
    PARAM_CALL,
    PARAM_CALL_WITH_ARG_VAR,
    PARAM_CAPTURE,      /* {[, ](Object) <boxeddeclarator>}     */
    PARAM_DECL,         /* {[, ]<typename>_$_Intf <declarator>} */
    PARAM_INVOKE_ARG_VAR,
    PARAM_MANGLE,       /* {$<mangledtypename>}                 */
    PARAM_PROTO,        /* {[, ]<typename>_$_Intf}              */
    PARAM_WHEN_VAR,
    PARAM_NONE
} t_paramFormat;

typedef enum {
    VARIABLE_PLAIN,
    VARIABLE_INTF,
    VARIABLE_INIT,
    VARIABLE_NONE
} t_variableFormat;

#define HASH_TABLE_SIZE 64
#define HASH_TABLE_MASK 0x3F

/* ejclassfile.c */
char *getMethodDescriptor(YT(classFile) *cf, int index);
char *getMethodName(YT(classFile) *cf, int index);
YT(classFile) *readClassFile(FILE *fyle);
void dumpClassFile(YT(classFile) *cf);
void freeClassFile(YT(classFile) *cf);

/* ejclasstable.c */
void attachDeclaration(YT(class) *theClass, YT(typeDeclaration) *declaration);
void defaultImports(void);
YT(class) *defineClass(YT(unitInfo) *unitInfo, YT(identifier) *id,
    bool isEClass, bool isInterface);
void dumpClasses(YT(unitInfo) *unitInfo);
YT(class) *findClass(YT(unitInfo) *unitInfo, YT(name) *name);
YT(name) *getFQN(YT(unitInfo) *unitInfo, YT(name) *name);
void handleClassImport(YT(classTable) *table, YT(name) *className,
    bool isEClass, YT(string) *location);
bool isEClass(YT(unitInfo) *unitInfo, YT(name) *name);
bool isEClassType(YT(unitInfo) *unitInfo, YT(type) *type);
YT(unitInfo) *newUnitInfo(YT(name) *packageName);

/* ejdefine.c */
void internalizeClass(YT(unitInfo) *unitInfo, YT(class) *theClass);
bool isFirstEMethodDefinition(YT(unitInfo) *unitInfo, YT(class) *theClass,
    YT(emethodHeader) *header);

/* ejfileio.c */
void convertNameToPath(YT(name) *name, char *pathResult);
void convertNameToMangle(YT(name) *name, char *mangleResult);
void convertNameToString(YT(name) *name, char *result);
void eprintf(char *format, ...);
void eprintln(char *format, ...);
void eputs(char *str);
bool fileExists(char *pathname);
bool popOutput(void);
bool prepareOutputDirectory(YT(packageDeclaration) *package, char *rootDirname,
    bool outputHere);
bool prepareClassOutputFile(char *className);
bool pushClassOutputFile(char *targetName);

/* ejimport.c */
void performImportClass(YT(name) *className, YT(unitInfo) *unitInfo);
void performImportPackage(YT(name) *packageName, YT(classTable) *table);
bool stripTail(char *str, char *pattern);
bool strTailMatch(char *str, char *pat);

/* ejlex.c */
char *flushWhitespace(void);

/* ejout.c */
void outputE(YT(compilationUnit) *unit, char *inputFileName,
    char *outputBaseDirName, bool outputHere);
