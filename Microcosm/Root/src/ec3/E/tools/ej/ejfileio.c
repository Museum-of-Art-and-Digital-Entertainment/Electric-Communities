/*
  ejfileio.c -- Unix-ish version of file I/O for E-to-Java translator.

  Chip Morningstar
  Electric Communities
  18-July-1997

  Copyright 1997 Electric Communities, all rights reserved worldwide.

*/

#include "generic.h"
#include "yh.h"
#include "yh_struct.h"
#include "yh_build.h"
#include <string.h>
#include <stdarg.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <errno.h>
#include "ej.h"

#define MAX_OUTPUT_DEPTH        5
static struct {
    FILE *output;
    char *filename;
} OutputStack[MAX_OUTPUT_DEPTH];
static int OutputDepth = -1;
static FILE *CurrentOutput = NULL;
static char *CurrentOutputFilename = "<stdout>";
static char *OutputBaseDirname = ".";
static char *RootDirname = ".";

static bool mkdirIfAbsent(char *dirname);

/**
 * convertNameToDirectoryPath -- Generalized routine to produce a directory
 *      pathname from a name struct.
 *
 * @param name  The name struct to be converted.
 * @param pathResult  A buffer into which the resulting string will be appended
 * @param doMake  TRUE->actually make the directory in question, FALSE->don't
 * @param separator  String to separate the elements of the generated pathname
 * @returns  Boolean success indicator, TRUE->success
 */
  static bool
convertNameToDirectoryPath(YT(name) *name, char *pathResult, bool doMake,
                           char *separator)
{
    if (name->prefix) {
        if (!convertNameToDirectoryPath(name->prefix, pathResult, doMake,
                                        separator))
            return(FALSE);
        strcat(pathResult, separator);
    }
    strcat(pathResult, name->id->symbol->name);
    if (doMake)
        return(mkdirIfAbsent(pathResult));
    else
        return(TRUE);
}

/**
 * legibleCharacter -- Character conversion based on current LegibleOutput
 *      flag.  Converts '\r' to either '\n' or ' ' depending on flag, leaves
 *      other characters unchanged (for now).
 *
 * @param c  Character to be converted.
 * @returns  Converted character
 */
  static char
legibleCharacter(char c)
{
    if (c == '\r')
        return(LegibleOutput ? '\n' : ' ');
    else
        return(c);
}

/**
 * mkdirIfAbsent -- Analog to UNIX mkdir call, except that it is not an error
 *      for the directory to already exist.
 *
 * @param dirname  Pathname of directory to be created if not already there
 * @returns  Success flag, TRUE->success
 */
  static bool
mkdirIfAbsent(char *dirname)
{
    if (mkdir(dirname, 0755) < 0 && errno != EEXIST) {
        yh_error("unable to create directory %s", dirname);
        return(FALSE);
    }
    return(TRUE);
}

/**
 * pushOutput -- Start outputting to a different file.
 *
 * @param filename  Name of the file to be output to.
 * @returns  Sucess flag, TRUE->success
 * @see popOutput
 */
  static bool
pushOutput(char *filename)
{
    ++OutputDepth;
    if (OutputDepth >= MAX_OUTPUT_DEPTH) {
        yh_error("internal error: output files nested too deep");
        return(FALSE);
    } else {
        FILE *output = fopen(filename, "w");
        if (output == NULL) {
            yh_error("unable to open output file %s", filename);
            return(FALSE);
        }
        OutputStack[OutputDepth].output = CurrentOutput;
        CurrentOutput = output;
        OutputStack[OutputDepth].filename = CurrentOutputFilename;
        CurrentOutputFilename = STRDUP(filename);
        return(TRUE);
    }
}

/**
 * convertNameToPath -- Generate a file pathname for a name struct (i.e., for
 *      the name foo.bar.baz, generate the path "foo/bar/baz").
 *
 * @param name  The name struct to be converted.
 * @param pathResult  A buffer into which the result will be placed.
 */
  void
convertNameToPath(YT(name) *name, char *pathResult)
{
    pathResult[0] = '\0';
    convertNameToDirectoryPath(name, pathResult, FALSE, "/");
}

/**
 * convertNameToMangle -- Generate a sealer-mangled name for a name struct
 *      (i.e., for the name foo.bar.baz, generate "foo_dot_bar_dot_baz")
 *
 * @param name  The name struct to be converted.
 * @param pathResult  A buffer into which the result will be placed.
 */
  void
convertNameToMangle(YT(name) *name, char *mangleResult)
{
    mangleResult[0] = '\0';
    convertNameToDirectoryPath(name, mangleResult, FALSE, "_dot_");
}

/**
 * convertNameToString -- Generate a printable version of a name struct (i.e.,
 *      for the name foo.bar.baz, generate the string "foo.bar.baz").
 *
 * @param name  The name struct to be converted.
 * @param pathResult  A buffer into which the result will be placed.
 */
  void
convertNameToString(YT(name) *name, char *result)
{
    result[0] = '\0';
    convertNameToDirectoryPath(name, result, FALSE, ".");
}

/**
 * eprintf -- Like printf, except that the format string is subjected to
 *      legibility conversion and the output goes to the current output.
 */
  void
eprintf(char *format, ...)
{
    va_list ap;
    char xformat[BUFLEN];
    char *xformatptr = xformat;

    va_start(ap, format);

    if (CurrentOutput == NULL)
        CurrentOutput = stdout;

    while (*format)
        *xformatptr++ = legibleCharacter(*format++);
    *xformatptr = '\0';

    vfprintf(CurrentOutput, xformat, ap);
    va_end(ap);
}

/**
 * eputs -- Like fputs, except that the string is subjected to legibility
 *      conversion and the output goes to the current output.
 */
  void
eputs(char *str)
{
    if (CurrentOutput == NULL)
        CurrentOutput = stdout;

    while (*str)
        putc(legibleCharacter(*str++), CurrentOutput);
}

/**
 * fileExists -- Test if a given file exists or not.
 *
 * @param pathname  Pathname to be tested
 * @returns  Boolean flag, TRUE->file exists, FALSE->it doesn't
 */
  bool
fileExists(char *pathname)
{
    struct stat statbuf;
    return (!stat(pathname, &statbuf));
}

/**
 * popOutput -- Cease outputting to the current output and resume outputting
 *      to whatever the current output was before the current output was
 *      started.
 *
 * @returns  Success flag, TRUE->success
 * @see pushOutput
 */
  bool
popOutput(void)
{
    if (OutputDepth < 0) {
        yh_error("internal error: output stack underflow");
        return(FALSE);
    } else {
        if (CurrentOutput != stdout)
            fclose(CurrentOutput);
        CurrentOutput = OutputStack[OutputDepth].output;
        CurrentOutputFilename = OutputStack[OutputDepth].filename;
        --OutputDepth;
        return(TRUE);
    }
}

/**
 * prepareOutputDirectory -- Set up to output .java files for a given package,
 *      given a root output directory.
 *
 * @param package  A packageDeclaration struct for the package being output
 * @param rootDirName  The root output directory
 * @param outputHere  TRUE->put all classes in root output directory, FALSE->
 *      generate package-specfic subdirectories as needed.
 * @returns  Success flag, TRUE->success
 */
  bool
prepareOutputDirectory(YT(packageDeclaration) *package, char *rootDirname,
                       bool outputHere)
{
    RootDirname = STRDUP(rootDirname);
    if (outputHere) {
        OutputBaseDirname = RootDirname;
    } else {
        char packagePath[BUFLEN];

        sprintf(packagePath, "%s/", rootDirname);
        if (package)
            if (!convertNameToDirectoryPath(package->packageName, packagePath,
                                            TRUE, "/"))
                return(FALSE);
        OutputBaseDirname = STRDUP(packagePath);
    }
    return(TRUE);
}

/**
 * pushClassOutputFile -- Start outputting to a given file in the prepared
 *      output directory.
 *
 * @param targetName  The base name of the file to output to.
 * @returns  Success flag, TRUE->success
 * @see prepareOutputDirectory
 * @see pushOutput
 */
  bool
pushClassOutputFile(char *targetName)
{
    char path[BUFLEN];

    sprintf(path, "%s/%s", OutputBaseDirname, targetName);

    return(pushOutput(path));
}
