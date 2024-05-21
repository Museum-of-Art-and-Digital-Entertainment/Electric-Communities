/*
  ejdirent.h -- Include dirent.h or emulate it for Windoze

  Chip Morningstar
  Electric Communities
  15-August-1997

  Copyright 1997 Electric Communities, all rights reserved worldwide.

*/

#ifndef WIN32
#include <dirent.h>
#else

/* We are only emulating as much of the POSIX directory API as we actually
   need, which is not very much! */

typedef struct openDirStruct DIR;

struct dirent {
    char *d_name;
};

int closedir(DIR *dirp);
DIR *opendir(char *dirname);
struct dirent *readdir(DIR *dirp);

#endif
