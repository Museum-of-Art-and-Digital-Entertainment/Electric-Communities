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

/*******************************************************************
 * NOTE:
 * These two defines *MUST* mirror the ones in 
 *   src\cosm1\remote\Helper\Win32\utils.h
 *******************************************************************/
#define EC_PRODUCT_NAME "HabiSplat"
#define EC_PRODUCT      "SOFTWARE\\Electric Communities\\" EC_PRODUCT_NAME

extern "C" {
/**
 * Register this process' PID in the System Registry (or Unix home directory)
 */
JNIEXPORT jint JNICALL package(Native_registerPID)(
    JAVA_PARAMS(Native))
{
    int retVal = 0;
#ifdef WIN32
    HKEY uCosmKey;

    if (RegOpenKeyEx(HKEY_CURRENT_USER,
                     EC_PRODUCT,
                     0,
                     KEY_ALL_ACCESS, 
                     &uCosmKey) == ERROR_SUCCESS) {

        int pid = _getpid();
        const unsigned char *ptr = (const unsigned char *) & pid;

        retVal = RegSetValueEx(uCosmKey, "LastPID", 0, REG_DWORD,
                               ptr, sizeof(int));

        RegCloseKey(uCosmKey);
    }
#endif WIN32

    return retVal;
}
/**
 * Register the port the AvatarReceptionist is listening on in
 * the System Registry (or Unix home directory)
 */
JNIEXPORT jint JNICALL package(Native_registerPort)(
    JAVA_PARAMS(Native), jint port)
{
    int retVal = 0;
#ifdef WIN32
    HKEY uCosmKey;

    if (RegOpenKeyEx(HKEY_CURRENT_USER,
                     EC_PRODUCT,
                     0,
                     KEY_ALL_ACCESS, 
                     &uCosmKey) == ERROR_SUCCESS) {

        int pid = _getpid();
        const unsigned char *ptr = (const unsigned char *) & port;

        retVal = RegSetValueEx(uCosmKey, "CommandPort", 0, REG_DWORD,
                               ptr, sizeof(int));

        RegCloseKey(uCosmKey);
    }
#endif WIN32

    return retVal;
}

} // extern "C"

