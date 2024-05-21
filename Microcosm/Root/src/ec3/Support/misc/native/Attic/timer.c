/*
 * Timer.c
 *
 * High resolution native timers for Windows and Unix platforms.
 *
 * java.lang.System.currentTimeMillis() is supposed to have
 * millisecond accuracy.  On Windows however, it only ticks
 * about 18 times/second (I don't know what its accuracy is
 * on Unix platforms).  This means that subsequent calls
 * frequently generate a delta of 0.  The smallest non-zero delta
 * between subsequent calls to System.currentTimeMillis() is 50
 * (with the units being msec).
 *
 * The timers in here have microsecond accuracy.  On a Win95/P133,
 * the smallest delta between subsequent calls to queryTimer() was
 * 6 (with the units being usec).  I never saw a delta of 0.
 *
 * For UNIX, gettimeofday() is used, where the smallest delta
 * between subsequent calls on a Solaris/SS2 is about 4 usec, and
 * 1 usec (occasionally 0) on a Solaris/Ultra.  timer_gettime(),
 * which has nanosecond accuracy, isn't used because it may not
 * be portable across all Unix platforms.  This could be looked
 * into further if the need arises.
 *
 * Calling these routines from Java does incur a hit.  On a
 * Win95/P133, the smallest observed delta was 25 usec.
 */

#ifdef UNIX
#include <sys/time.h>
#include <sys/resource.h>
#endif

#ifdef WIN32
#include <windows.h>
#include <limits.h>
static float ufreq;
static DWORD freq;
static DWORD initial_high, sec_per_high;
#endif

#ifdef USEJNI
#define JAVA11 1
#else
#define JAVA102 1
#endif

#include "../../../../cosm1/ui/gui/scene/jcwrap.h"

// Change these defines to change a package
#ifndef USEJNI
#define package(a) ec_misc_##a
#define h_package(a) Hec_misc_##a
#else
#define package(a) Java_ec_misc_##a
#endif
#define class_package(a) Classec_misc_##a

JNIEXPORT void JNICALL package(Native_initializeTimer)(
    JAVA_PARAMS(Native))
{
#ifdef UNIX
    // nothing to do
#endif
#ifdef WIN32
    LARGE_INTEGER   li;

    if (!freq) {
    QueryPerformanceFrequency(&li);
    freq  = li.LowPart;
    ufreq = ((float)freq)/1000000.0f;
    QueryPerformanceCounter(&li);
    initial_high = li.HighPart;
    sec_per_high = UINT_MAX/freq;
    }
#endif
}

JNIEXPORT jlong JNICALL package(Native_queryTimer)(
    JAVA_PARAMS(Native))
{
#ifdef UNIX
    struct timeval  tp;

    gettimeofday(&tp, 0);
    return ((jlong)tp.tv_sec)*1000000 + (jlong)tp.tv_usec;
#endif

#ifdef WIN32
    LARGE_INTEGER   li;

    QueryPerformanceCounter(&li);
    return 1000000*(jlong)(li.HighPart*sec_per_high + li.LowPart/freq) +
       (jlong)((li.LowPart - (li.LowPart/freq)*freq)/ufreq);
#endif
}
