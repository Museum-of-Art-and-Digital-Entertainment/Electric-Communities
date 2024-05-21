#ifdef WIN32
# include <windows.h>
# include <winreg.h>
# include <process.h>
#else // SOLARIS
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


extern "C" {

/**
 * Register this process' PID in the System Registry (or Unix home directory)
 */
JNIEXPORT int JNICALL package(Native_registerPID)(
    JAVA_PARAMS(Native))
{
    int retVal = 0;
#ifdef WIN32
    HKEY uCosmKey;

    if (RegOpenKeyEx(HKEY_CLASSES_ROOT,
                     "Microcosm",
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

} // extern "C"
