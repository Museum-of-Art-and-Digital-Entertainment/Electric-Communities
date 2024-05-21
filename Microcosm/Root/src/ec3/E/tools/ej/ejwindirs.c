#ifdef WIN32

#include "generic.h"
#include "ejdirent.h"
#include <wtypes.h>
#include <winbase.h>
#include <errno.h>

struct openDirStruct {
    HANDLE windowsDirHandle;
    WIN32_FIND_DATA findData;
    bool readFirst;
    bool noFiles;
};

  int
closedir(DIR *dirp)
{
    BOOL result = FindClose(dirp->windowsDirHandle);
    dirp->windowsDirHandle = NULL;
    if (result)
        return(0);
    else
        return(-1);
}

  DIR *
opendir(char *dirname)
{
    static DIR theDir;
    char namebuf[BUFLEN];

    sprintf(namebuf, "%s/*.*", dirname);
    theDir.windowsDirHandle = FindFirstFile(namebuf, &theDir.findData);
    theDir.readFirst = FALSE;
    theDir.noFiles = FALSE;
    if (theDir.windowsDirHandle == INVALID_HANDLE_VALUE) {
        if (GetLastError() == ERROR_NO_MORE_FILES) {
            theDir.noFiles = TRUE;
            return(&theDir);
        } else {
            if(GetLastError() == ERROR_PATH_NOT_FOUND)
                errno = ENOENT;
            return(NULL);
        }
    } else {
        return(&theDir);
    }
}

  struct dirent *
readdir(DIR *dirp)
{
    static struct dirent theDirent;

    if (dirp->windowsDirHandle) {
        if (dirp->noFiles) {
            return(NULL);
        }
        if (dirp->readFirst) {
            if (!FindNextFile(dirp->windowsDirHandle, &dirp->findData))
                return(NULL);
        } else {
            dirp->readFirst = TRUE;
        }
        theDirent.d_name = dirp->findData.cFileName;
        return(&theDirent);
    }
    return(NULL);
}

#endif
