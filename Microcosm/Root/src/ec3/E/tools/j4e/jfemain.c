/*
  jfemain.c -- Main for the E-to-pure Java translator

  Chip Morningstar
  Electric Communities
  9-January-1998

  Copyright 1998 Electric Communities, all rights reserved worldwide.

*/

#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "yh_struct.h"
#include "yh_dump.h"
#include "jfe.h"
#include <string.h>

extern int yydebug;
extern bool yh_CaseSensitiveSymbols;

static YT(stringList) *InputFileNames = NULL;
static bool  OutputParseTree   = FALSE;
static bool  OutputHere        = FALSE;
static bool  OutputJava        = TRUE;
static char *OutputBaseDirName = ".";

bool EDebug = FALSE;
bool Verbose = FALSE;
bool OutputToStdio = FALSE;
bool SplitFiles = FALSE;

bool XlateEClass = FALSE;
bool XlateELiterals = FALSE;
bool XlateEWhen = FALSE;
bool XlateImplicitChannels = FALSE;
bool XlatePromises = FALSE;
bool XlateSend = FALSE;

static void badUsage(void);
static void initializeEverything(int argc, char *argv[]);
static void parseCommandLineArgs(int argc, char *argv[]);
static void printHelp(void);
static void printUsage(void);
static void processEverything(void);

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
                        }
                        break;
                    }
                    case 'g': EDebug = TRUE;                    break;
                    case 'j': OutputJava = FALSE;               break;
                    case 'p': OutputHere = TRUE;                break;
                    case 'P': OutputParseTree = TRUE;           break;
                    case 's': SplitFiles = TRUE;                break;
                    case 'v': Verbose = TRUE;                   break;
                    case 'x': {
                        char *what = argv[++i];
                        if (strcmp(what, "eliterals") == 0) {
                            XlateELiterals = TRUE;
                        } else if (strcmp(what, "eclass") == 0) {
                            XlateEClass = TRUE;
                        } else if (strcmp(what, "ewhen") == 0) {
                            XlateEWhen = TRUE;
                        } else if (strcmp(what, "implicitchannels") == 0) {
                            XlateImplicitChannels = TRUE;
                        } else if (strcmp(what, "promises") == 0) {
                            XlatePromises = TRUE;
                        } else if (strcmp(what, "send") == 0) {
                            XlateSend = TRUE;
                        } else if (strcmp(what, "all") == 0) {
                            XlateEClass = TRUE;
                            XlateELiterals = TRUE;
                            XlateEWhen = TRUE;
                            XlateImplicitChannels = TRUE;
                            XlatePromises = TRUE;
                            XlateSend = TRUE;
                        } else {
                            yh_error("unknown xlate tag %s", what);
                        }
                        break;
                    }
                    case 'Y': yydebug = TRUE;                   break;
                    case 'h': printHelp();                      break;
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
    printf("  -g          Output _DEBUG_ blocks\n");
    printf("  -h          Print this helpful help message\n");
    printf("  -j          Suppress final Java output\n");
    printf("  -p          Put output files in current working directory\n");
    printf("  -s          Split classes into individual output files\n");
    printf("  -v          Compile verbosely\n");
    printf("  -x tag      Do translation specified by tag. Valid tags:\n");
    printf("                eclass\n");
    printf("                eliterals\n");
    printf("                ewhen\n");
    printf("                implicitchannels\n");
    printf("                promises\n");
    printf("                send\n");
    printf("                all\n");
    printf("  -P          Dump parse tree\n");
    printf("  -Y          Print Yacc debug trace\n");
    exit(0);
}

/**
 * printUsage -- Print standard Unix-style usage message.
 */
  void
printUsage(void)
{
    printf("usage: j4e [-ghjpsvPY] [-x tag] [-d dir] file ...\n");
}

/**
 * main -- Main for the E to pure Java translator
 */
  int
main(int argc, char *argv[])
{
    YT(compilationUnitList) *units = NULL;
    YT(stringList) *inputChaser;
    YT(compilationUnitList) *unitChaser;

    initializeEverything(argc, argv);

    inputChaser = InputFileNames;
    while (inputChaser) {
        if (Verbose) {
            fprintf(stderr, "%s\n", inputChaser->string);
        }
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
