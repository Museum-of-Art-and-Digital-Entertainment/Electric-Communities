#ifdef UNIX

#endif

#ifdef WIN32
#include <windows.h>
#endif

#ifdef USEJNI
#define JAVA11 1
#else
#define JAVA102 1
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

JNIEXPORT jint JNICALL package(Native_getPhysicalMemorySizeNative)(
    JAVA_PARAMS(Native))
{
#ifdef UNIX
    return ((jint) 0);
#endif

#ifdef WIN32
    MEMORYSTATUS MemoryStatus;
 
    memset( &MemoryStatus, sizeof(MEMORYSTATUS), 0 );
    MemoryStatus.dwLength = sizeof(MEMORYSTATUS);
 
    GlobalMemoryStatus( &MemoryStatus );
 
    return((jint) (MemoryStatus.dwTotalPhys));
#endif
}
