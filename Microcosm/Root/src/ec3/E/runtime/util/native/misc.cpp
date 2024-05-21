#ifdef WIN32
# include <windows.h>
# include <winreg.h>
# include <process.h>
#else // SOLARIS
#endif

#include "jcwrap.h"
// Change these defines to change a package
#ifndef USEJNI
#define package(a) ec_util_##a
#define h_package(a) Hec_util_##a
#else
#define package(a) Java_ec_util_##a
#endif
#define class_package(a) Classec_util_##a


extern "C" {

/**
 * Register this process' PID in the System Registry (or Unix home directory)
 */
// JNIEXPORT void JNICALL package(Native_dumpThreads)(
//     JAVA_PARAMS(Native))
// {
// #ifdef WIN32
//     extern DumpThreads();
//     DumpThreads(); // in javai.dll
// #endif WIN32
// }

/**
 * Register this process' PID in the System Registry (or Unix home directory)
 */
JNIEXPORT jint JNICALL package(Native_flushWorkingSet)(
    JAVA_PARAMS(Native))
{
    jint result = -1;
#ifdef WIN32
    result = SetProcessWorkingSetSize(GetCurrentProcess(),
                                 0xFFFFffff,  // set it to 0
                                 0xFFFFffff   // set it to 0
                                );
#endif WIN32

    return result;
}


} // extern "C"
