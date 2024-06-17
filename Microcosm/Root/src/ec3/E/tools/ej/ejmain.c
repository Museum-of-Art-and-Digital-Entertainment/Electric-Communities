/*
  ejmain.c -- Main for the E-to-Java translator

  Chip Morningstar
  Electric Communities
  16-July-1997

  Copyright 1997 Electric Communities, all rights reserved worldwide.

*/

#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "yh_struct.h"
#include "yh_dump.h"
#include "ej.h"
#include <string.h>

extern int yydebug;
extern bool yh_CaseSensitiveSymbols;

static YT(stringList) *InputFileNames = NULL;
static bool  OutputParseTree   = FALSE;
static bool  OutputHere        = FALSE;
static bool  OutputJava        = TRUE;
static bool  VerboseDumps      = FALSE;
static char *OutputBaseDirName = ".";
static char *ClassToDump       = NULL;

bool EDebug = FALSE;
bool LegibleOutput = FALSE;
bool Verbose = FALSE;
bool OutputToStdio = FALSE;
YT(stringList) *ClassPath = NULL;

static void addToClassPath(char *classPathString);
static void badUsage(void);
static void initializeEverything(int argc, char *argv[]);
static void parseCommandLineArgs(int argc, char *argv[]);
static void printHelp(void);
static void printUsage(void);

/**
 * addToClassPath -- Add directories to the class path.
 *
 * @param classPathString  A class path string
 */
  static void
addToClassPath(char *classPathString)
{
    while (classPathString) {
        char *delimiter = strchr(classPathString, PATH_SEPARATOR_CHAR);
        if (delimiter) {
            *delimiter = '\0';
            ClassPath = YBUILD(stringList)(STRDUP(classPathString), ClassPath);
            classPathString = delimiter + 1;
        } else {
            ClassPath = YBUILD(stringList)(STRDUP(classPathString), ClassPath);
            classPathString = NULL;
        }
    }
}

/**
 * badUsage -- Print the standard usage message for bad command line flags and
 *      then exit.
 */
  static void
badUsage(void)
{
    printUsage();
    exit(1);
}

/**
 * initializeEverything -- Just what the name says.
 *
 * @param argc  C standard arg count
 * @param argv  C standard arg array
 */
  static void
initializeEverything(int argc, char *argv[])
{
    NO_ERROR_CONTEXT();
    yh_CaseSensitiveSymbols = TRUE;
    parseCommandLineArgs(argc, argv);
    addToClassPath(getenv("CLASSPATH"));
    defaultImports();
}

/**
 * parseCommandLineArgs -- Command line args are noted in globals.
 *
 * @param argc  C standard arg count
 * @param argv  C standard arg array
 */
  static void
parseCommandLineArgs(int argc, char *argv[])
{
    int i;

    for (i = 1; i < argc; i++) {
        if (argv[i][0] == '-') {
            char *argptr =  &argv[i][1];
            while (*argptr) {
                switch (*argptr++) {
                    case 'd': {
                        char *dirName = argv[++i];
                        if (strcmp(dirName, "-") == 0) {
                            OutputToStdio = TRUE;
                        } else {
                            OutputBaseDirName = dirName;
                            addToClassPath(dirName);
                        }
                        break;
                    }
                    case 'c': addToClassPath(argv[++i]);        break;
                    case 'C': ClassToDump = argv[++i];          break;
                    case 'g': EDebug = TRUE;                    break;
                    case 'j': OutputJava = FALSE;               break;
                    case 'l': LegibleOutput = TRUE;             break;
                    case 'p': OutputHere = TRUE;                break;
                    case 'P': OutputParseTree = TRUE;           break;
                    case 'v': Verbose = TRUE;                   break;
                    case 'V': VerboseDumps = TRUE;              break;
                    case 'Y': yydebug = TRUE;                   break;
                    case 'h': printHelp();                      break;
#ifdef DEBUG_ALLOC
                    case 'A': DebugAlloc = TRUE;                break;
#endif
                    default:
                        yh_error("bad command line flag \"-%c\"", *--argptr);
                        badUsage();
                }
            }
        } else {
            InputFileNames = YBUILD(stringList)(&argv[i][0], InputFileNames);
        }
    }
}

/**
 * printHelp -- Print a nice message describing the command line switches.
 */
  void
printHelp(void)
{
    printUsage();
    printf("  -d dirname  Set the output base directory to dirname\n");
    printf("  -c path     Add path to the class path\n");
    printf("  -g          Output _DEBUG_ blocks\n");
    printf("  -h          Print this helpful help message\n");
    printf("  -j          Suppress final Java output\n");
    printf("  -l          Legible output instead of line number correspondence\n");
    printf("  -p          Put output files in current working directory\n");
    printf("  -v          Compile verbosely\n");
#ifdef DEBUG_ALLOC
    printf("  -A          Dump storage allocator debug trace (big!)\n");
#endif
    printf("  -P          Dump parse tree\n");
    printf("  -V          Verbose dumps\n");
    printf("  -Y          Print Yacc debug trace\n");
    exit(0);
}

/**
 * printUsage -- Print standard Unix-style usage message.
 */
  void
printUsage(void)
{
#ifdef DEBUG_ALLOC
    printf("usage: ej [-ghjlpvPVYA] [-d dir] [-c classpath] file ...\n");
#else
    printf("usage: ej [-ghjlpvPVY] [-d dir] [-c classpath] file ...\n");
#endif
}

/**
 * main -- Main for the E to Java translator
 */
  int
main(int argc, char *argv[])
{
    YT(compilationUnitList) *units = NULL;
    YT(stringList) *inputChaser;
    YT(compilationUnitList) *unitChaser;

    initializeEverything(argc, argv);
    if (ClassToDump) {
        dumpClassFile(readClassFile(fopen(ClassToDump, "r")));
        exit(0);
    }

    inputChaser = InputFileNames;
    while (inputChaser) {
        units = YBUILD(compilationUnitList)(
            yh_parse(inputChaser->string, '.', "e"), units);
        inputChaser = inputChaser->next;
    }
    if (OutputParseTree) {
        unitChaser = units;
        while (unitChaser) {
            YH_DUMP(compilationUnit, unitChaser->compilationUnit);
            unitChaser = unitChaser->next;
        }
    }
    if (OutputJava) {
        unitChaser = units;
        inputChaser = InputFileNames;
        while (unitChaser) {
            outputE(unitChaser->compilationUnit, inputChaser->string,
                    OutputBaseDirName, OutputHere);
            unitChaser = unitChaser->next;
            inputChaser = inputChaser->next;
        }
    }
    exit((yh_ErrorCount > 0) ? 1 : 0);
}
