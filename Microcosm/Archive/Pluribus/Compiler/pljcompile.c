#include "ec_plcompile_PluribusCompiler.h"
#include <setjmp.h>

static jmp_buf ExitCatcher;
static long    ExitResult;

  long
ec_plcompile_PluribusCompiler_runNativeMain(
  struct Hec_plcompile_PluribusCompiler *this_h,
  HArrayOfString *javaArgv_h)
{
    ClassArrayOfString *javaArgv;
    char **argv;
    int argc;
    int i;

    if (javaArgv_h == NULL) {
        SignalError(EE(), "java/lang/NullPointerException", "null arg list");
        return 1;
    }

    javaArgv = unhand(javaArgv_h);
    argc = obj_length(javaArgv_h) + 1;
    argv = (char **) malloc(argc * sizeof(char *));
    argv[0] = "pl";
    for (i=1; i<argc; ++i)
        argv[i] = makeCString(javaArgv->body[i-1]);
    if (!setjmp(ExitCatcher))
        ExitResult = wrappedMain(argc, argv);
    free(argv);
    return(ExitResult);
}

  void
wrappedExit(int resultCode)
{
    ExitResult = resultCode;
    longjmp(ExitCatcher, 1);
}
