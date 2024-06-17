/*
  jfe.h -- E-to-pure Java translator types, macros, globals & function protos 

  Chip Morningstar
  Electric Communities
  10-January-1998

  Copyright 1998 Electric Communities, all rights reserved worldwide.

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
extern bool OutputToStdio;
extern bool SplitFiles;
extern bool Verbose;
extern YT(stringList) *ClassPath;

extern bool XlateEClass;
extern bool XlateELiterals;
extern bool XlateEWhen;
extern bool XlateImplicitChannels;
extern bool XlatePromises;
extern bool XlateSend;

#define HASH_TABLE_SIZE 64
#define HASH_TABLE_MASK 0x3F

/* jfefileio.c */
void convertNameToPath(YT(name) *name, char *pathResult);
void convertNameToString(YT(name) *name, char *result);
void eprintf(char *format, ...);
void eprintln(char *format, ...);
void eputs(char *str);
bool popOutput(void);
bool prepareOutputDirectory(YT(packageDeclaration) *package, char *rootDirname,
    bool outputHere);
bool prepareClassOutputFile(char *className);
bool pushClassOutputFile(char *targetName);

/* jfelex.c */
char *flushWhitespace(void);

/* jfeout.c */
void outputE(YT(compilationUnit) *unit, char *inputFileName,
    char *outputBaseDirName, bool outputHere);
