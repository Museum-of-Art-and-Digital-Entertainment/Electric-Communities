/*
  plmain.c -- Main for Pluribus compiler

  Chip Morningstar
  Electric Communities
  2-August-1996

  Copyright 1996 Electric Communities, all rights reserved.

*/

#include "generic.h"
#include "yh.h"
#include "yh_struct.h"
#include "yh_dump.h"
#include "yh_build.h"
#include <string.h>
#include "pl.h"
#include "platform.h"

extern int yydebug;
extern bool yh_CaseSensitiveSymbols;

static bool BackAck = FALSE;
static bool OutputParseTree = FALSE;
bool DumpIntermediateStructures = FALSE;
bool WarnAboutUndefinedTypes = FALSE;
bool Verbose = FALSE;

YT(stringList) *UnitPath = NULL;
YT(symbolRef) *UnitPackage = NULL;
YT(symbol) *initSym;

static char *InputFileName     = NULL;
static char *OutputBaseDirName = ".";
static char *GenFileNameList = NULL;

static void badUsage(void);
static void initializeEverything(int argc, char *argv[]);
static void parseCommandLineArgs(int argc, char *argv[]);
static void printHelp(void);
static void printUsage(void);

/*
  addToUnitPath -- Add directories to the unit path.
*/
  static void
addToUnitPath(char *unitPathString)
{
    char *saveIt = NULL;
    char *delimiter = NULL;

    if (unitPathString)
        saveIt = strdup(unitPathString);

    while (saveIt) {
        delimiter = strchr(saveIt, PLDELIMITER);
        if (delimiter) {
            *delimiter = '\0';
            UnitPath = YBUILD(stringList)(STRDUP(saveIt), UnitPath);
            saveIt = delimiter + 1;
        } else {
            UnitPath = YBUILD(stringList)(STRDUP(saveIt), UnitPath);
            saveIt = NULL;
        }
    }
}

/*
  badUsage -- Print the standard usage message for bad command line flags and
              then exit.
*/
  static void
badUsage(void)
{
    printUsage();
    exit(1);
}

/*
  initializeEverything -- Just what the name says.
*/
  static void
initializeEverything(int argc, char *argv[])
{
    NO_ERROR_CONTEXT();
    yh_CaseSensitiveSymbols = TRUE;
    yh_initializeSymbolTable();
    initSym = yh_handleSymbol("init");
    parseCommandLineArgs(argc, argv);
    addToUnitPath(getenv("PLUNITPATH"));
    if (UnitPath == NULL)
        addToUnitPath(".");
    if (BackAck) {
        backack();
        exit(0);
    }
}

/*
  parseCommandLineArgs -- Command line args are noted in globals.
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
                    case 'Y': yydebug = TRUE;                   break;
                    case 'd': OutputBaseDirName = strdup(argv[++i]);
                        UnitPath = YBUILD(stringList)
                                         (STRDUP(OutputBaseDirName), UnitPath);
                              break;
                    case 'u': addToUnitPath(argv[++i]);
                        break;
                    case 'h': printHelp();                      break;
                    case 'p': BackAck = TRUE;                   break;
                    case 's': GenFileNameList = argv[++i]; break;
                    case 'v': Verbose = TRUE;                   break;
                    case 'w': WarnAboutUndefinedTypes = TRUE;   break;
                    case 'P': OutputParseTree = TRUE;           break;
                    case 'D': DumpIntermediateStructures = TRUE;break;
#ifdef DEBUG_ALLOC
                    case 'A': DebugAlloc = TRUE;                break;
#endif
                    default:
                        yh_error("bad command line flag \"-%c\"", *--argptr);
                        badUsage();
                }
            }
        } else {
            if (InputFileName) {
                yh_error("more than one input file given on command line");
                badUsage();
            } else
                InputFileName = &argv[i][0];
        }
    }
}

/*
  printHelp -- Print a nice message describing the command line switches.
*/
  void
printHelp(void)
{
    printUsage();
    printf("  -d dirname  Set the output base directory to dirname\n");
    printf("  -h          Print this helpful help message\n");
    printf("  -p          Print the acronym expansion of \"Pluribus\"\n");
    printf("  -u path     Add path to the unit path\n");
    printf("  -v          Be verbose\n");
    printf("  -w          Warn about undefined types\n");
#ifdef DEBUG_ALLOC
    printf("  -A          Dump storage allocator debug trace (big!)\n");
#endif
    printf("  -D          Dump intermediate structures\n");
    printf("  -P          Dump parse tree\n");
    printf("  -Y          Print YACC debug trace\n");
    exit(0);
}

/*
  printUsage -- Print standard Unix-style usage message.
*/
  void
printUsage(void)
{
#ifdef DEBUG_ALLOC
#define EXTRA_FLAGS     "A"
#else
#define EXTRA_FLAGS     ""
#endif
    printf("usage: pl [-hpvwDPY%s] [-d dir] [-u path] [file]\n", EXTRA_FLAGS);
}

/*
  main -- Parser loop is driven from here.
*/
  int
main(int argc, char *argv[])
{
    YT(unitDef) *unitDef;

#ifdef PLRTEXENV
    fprintf(stderr, "\n  Running with new RtExceptionEnv code\n\n");
#endif
    initializeEverything(argc, argv);
    unitDef = yh_parse(InputFileName, ':', "pluribus");
    if (OutputParseTree)
        YH_DUMP(unitDef, unitDef);
    if (unitDef) {
        YT(unit) *result;
        pushScope(NULL);
        result = grindInput(unitDef, InputFileName);
        if (result) {
            generateOutput(result, InputFileName, OutputBaseDirName,
                           GenFileNameList);
        }
    }
    if (yh_ErrorCount > 0) {
      exit(1); 
    } else {
      exit(0); 
    }
    return(0);
}
