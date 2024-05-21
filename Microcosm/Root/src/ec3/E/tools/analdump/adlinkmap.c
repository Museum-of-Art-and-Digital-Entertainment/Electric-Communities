/*
  adlinkmap.c -- Read and interpret Windows link map files for Analdump

  Chip Morningstar
  Electric Communities
  18-December-1997

  Copyright 1997 Electric Communities, all rights reserved worldwide.

*/

#include "analdump.h"
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <ctype.h>

static void openAndReadLinkMapFile(char *filename, t_dump *dump,
                                   bool gripeIfMissing);

  static void
internalizeLinkMapDirectory(char *dirname, t_dump *dump)
{
    t_dll *dll = dump->dll;

    while (dll) {
        if (dll->address) {
            char filename[BUFLEN];
            char buf[BUFLEN];
            char *str = strrchr(dll->name, '\\');
            if (!str)
                str = dll->name;
            else
                str = str + 1;
            strcpy(buf, str);
            str = strrchr(buf, '.');
            if (str)
                *str = '\0';
            for (str = buf; *str; ++str)
                *str = tolower(*str);
            sprintf(filename, "%s/%s.map", dirname, buf);
            openAndReadLinkMapFile(filename, dump, FALSE);
        }
        dll = dll->next;
    }
}

  static void
noteMapFileEntry(long funcAddress, char *symbol, char *objfile, t_dll *dll,
                 t_dump *dump)
{
    int i;

    for (i=0; i<dump->mallocCallerCount; ++i) {
        t_mallocCaller *caller = dump->sortedMallocs[i];
        if (caller->filename[0] == '*') {
            long callAddress = caller->line;
            if (dll->vmap->address <= callAddress &&
                    callAddress < dll->vmap->address + dll->vmap->size) {
                t_mapFileInfo *mapFileInfo = caller->mapFileInfo;
                if (mapFileInfo == NULL) {
                    mapFileInfo = TypeAlloc(t_mapFileInfo);
                    caller->mapFileInfo = mapFileInfo;
                    mapFileInfo->funcAddress = 0;
                    mapFileInfo->name[0] = '\0';
                    mapFileInfo->objfile[0] = '\0';
                }
                if (funcAddress <= callAddress &&
                        funcAddress > mapFileInfo->funcAddress) {
                    mapFileInfo->funcAddress = funcAddress;
                    if (*symbol == '_')
                        strcpy(mapFileInfo->name, symbol + 1);
                    else
                        strcpy(mapFileInfo->name, symbol);
                    strcpy(mapFileInfo->objfile, objfile);
                }
            }
        }
    }
}

  static void
internalizeLinkMapFile(FILE *fyle, char *filename, t_dump *dump)
{
    char mapname[80];
    char symbol[256];
    char objfile[256];
    long address;
    long loadAddress;
    int hits;
    t_dll *dll;

    if (vlfscanf(fyle, "%80s", mapname) != 1) {
        fprintf(stderr, "can't find module name in %s\n", filename);
        return;
    }

    skipLine(fyle);
    skipLine(fyle);
    skipLine(fyle);
    if (vlfscanf(fyle, " Preferred load address is %x", &loadAddress) != 1) {
        fprintf(stderr, "can't find load address in %s\n", filename);
        return;
    }
    
    do {
        char temp[10];
        hits = vlfscanf(fyle, "  Address         Publics by %8s", temp);
    } while (hits == 0);
    if (hits < 0) {
        fprintf(stderr, "can't find symbol map in %s\n", filename);
        return;
    }
    skipLine(fyle);
    
    aprintf("reading link map file %s\n", filename);

    dll = findDLLByName(mapname, dump);
    if (!dll) {
        fprintf(stderr, "can't find a DLL to go with map %s\n", mapname);
        return;
    } else if (!dll->vmap) {
        fprintf(stderr, "no VM info for DLL %s\n", pDLLName(dll));
        return;
    }

    while (vlfscanf(fyle, " 0001:%*x %256s %x %*c %256s",
                    symbol, &address, objfile) == 3) {
        noteMapFileEntry(address - loadAddress + dll->address, symbol, objfile,
                         dll, dump);
    }
}

  static void
openAndReadLinkMapFile(char *filename, t_dump *dump, bool gripeIfMissing)
{
    FILE *fyle = fopen(filename, "r");
    if (fyle) {
        internalizeLinkMapFile(fyle, filename, dump);
        fclose(fyle);
    } else if (gripeIfMissing) {
        fprintf(stderr, "unable to open link map file '%s'\n", filename);
    }
}

  void
internalizeLinkMap(char *arg, t_dump *dump)
{
    struct stat statbuf;

    if (arg[0] == '\0') {
        internalizeLinkMapDirectory(".", dump);
    } else if (stat(arg, &statbuf)) {
        fprintf(stderr, "unable to stat file or directory '%s'\n", arg);
#ifdef WIN32
    } else if (statbuf.st_mode & _S_IFDIR) {
#else
    } else if (S_ISDIR(statbuf.st_mode)) {
#endif
        internalizeLinkMapDirectory(arg, dump);
    } else {
        openAndReadLinkMapFile(arg, dump, TRUE);
    }
}
