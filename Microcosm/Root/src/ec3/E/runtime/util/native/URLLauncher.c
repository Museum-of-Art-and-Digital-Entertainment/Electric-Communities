//#include "interpreter.h"
//#include "winuser.h"

#ifdef WIN32
//#include <string.h>
//#include <winuser.h>
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

#define MAX_URL_LEN 1024

char *ProtocolStr[] = {
    "http:",
    "https:",
    "file:",
    "mailto:",
    "news:",
    "snews:"
};

#define MAX_PROTOCOLS 6


JNIEXPORT jint JNICALL package(Native_openURL) (
    JAVA_PARAMS(URLLauncher),
    jstring url) {

#ifdef WIN32
    int i;
    HINSTANCE returnVal;
#endif

    char * theURL = (char *)GET_UTFChars(url);

#ifdef WIN32
    for (i=0; i<MAX_PROTOCOLS; i++) {
        /*printf("checking protocol %s\n", ProtocolStr[i]);*/
        if ( ! _strnicmp(ProtocolStr[i], theURL, strlen(ProtocolStr[i]))) {
            returnVal = ShellExecute(NULL, 
                                     "open", 
                                     theURL, 
                                     NULL, 
                                     NULL, 
                                     SW_SHOWMINIMIZED);
            if ( (long) returnVal < 32) {
                /* return val spec'ed in the VC++ "ShellExecute" docs */
                return (jint) returnVal;
            } else {
                return (jint)0;
            }
        }
    }
    return (jint)-1; /* url was not a valid protocol */
#endif

#ifdef SPARC
    printf("nativeOpenURL on %s\n", theURL);
    return (jint)0;
#endif
} 
