/*
  adwoutput.c -- Analdump output generation for Windows-related stuff

  Chip Morningstar
  Electric Communities
  6-January-1998

  Copyright 1998 Electric Communities, all rights reserved worldwide.

*/

#include "analdump.h"
#include <string.h>

  static char
cProt(int prot)
{
    switch (prot) {
       case VM_PROT_READ:             return('r');
       case VM_PROT_READWRITE:        return('w');
       case VM_PROT_WRITECOPY:        return('W');
       case VM_PROT_EXECUTE:          return('x');
       case VM_PROT_EXECUTEREAD:      return('X');
       case VM_PROT_EXECUTEREADWRITE: return('c');
       case VM_PROT_EXECUTEWRITECOPY: return('C');
       case VM_PROT_GUARD:            return('g');
       case VM_PROT_NOACCESS:         return('n');
       case VM_PROT_NOCACHE:          return('a');
       case VM_PROT_UNKNOWNPROT:      return('u');
       case VM_PROT_UNKNOWN:          return('u');
    }
    return('?');
}

  static char
cState(int state)
{
    switch (state) {
        case VM_STATE_COMMIT:  return('C');
        case VM_STATE_FREE:    return('F');
        case VM_STATE_RESERVE: return('R');
        case VM_STATE_UNKNOWN: return('U');
    }
    return('?');
}

  static char *
pProt(int prot)
{
    switch (prot) {
       case VM_PROT_READ:             return("rd      ");
       case VM_PROT_READWRITE:        return("rd/wr   ");
       case VM_PROT_WRITECOPY:        return("wc      ");
       case VM_PROT_EXECUTE:          return("ex      ");
       case VM_PROT_EXECUTEREAD:      return("ex/rd   ");
       case VM_PROT_EXECUTEREADWRITE: return("ex/rd/wr");
       case VM_PROT_EXECUTEWRITECOPY: return("ex/wc   ");
       case VM_PROT_GUARD:            return("guard   ");
       case VM_PROT_NOACCESS:         return("no-acc  ");
       case VM_PROT_NOCACHE:          return("no-cache");
       case VM_PROT_UNKNOWNPROT:      return("unknown ");
       case VM_PROT_UNKNOWN:          return("unknown ");
    }
    return("this can't happen ");
}

  static char *
pState(int state)
{
    switch (state) {
        case VM_STATE_COMMIT:  return("commit ");
        case VM_STATE_FREE:    return("free   ");
        case VM_STATE_RESERVE: return("reserve");
        case VM_STATE_UNKNOWN: return("unknown");
    }
    return("huh????");
}

  char *
pDLL(long address, t_dump *dump)
{
    t_dll *dll = dump->dll;

    if (address) {
        while (dll) {
            t_vmap *vmap = dll->vmap;
            if (vmap && vmap->address <= address &&
                address < vmap->address + vmap->size)
                return(pDLLName(dll));
            dll = dll->next;
        }
    }
    return("unknown");
}

  char *
pDLLName(t_dll *dll)
{
    char *pname = strrchr(dll->name, '\\');
    if (pname && !VerboseMode)
        return(pname + 1);
    else
        return(dll->name);
}

#ifdef WIN32
  int
strcasecmp(char *s1, char *s2)
{
    while (*s1 && *s2) {
        int test = tolower(*s1++) - tolower(*s2++);
        if (test)
            return(test);
    }
    if (*s1)
        return(1);
    else if (*s2)
        return(-1);
    else
        return(0);
}
#endif

  static void
printWorkingSet(t_wsInfo *ws, bool bigMapMode)
{
    int i;
    longword testAddress = 0xFFFFFFFF;
    longword expectAddress = testAddress;
    longword blockAddress;
    int totalPages = 0;
    int pageCount = 0;
    
    if (!bigMapMode) {
        aprintf("working set %s (%d pages)\n", ws->tag, ws->count);
        aprintf("address  pages\n");
        aprintf("-------- -----\n");
    }    
    for (i=0; i<ws->count; ++i) {
        testAddress = wsNextAddress(ws, testAddress);
        if (testAddress != expectAddress) {
            if (pageCount) {
                if (bigMapMode) {
                    aprintf("%08x ws\n", blockAddress);
                    aprintf("%08x we\n",
                            blockAddress + (pageCount * PAGE_SIZE) - 1);
                } else {
                    aprintf("%08x %5d\n", blockAddress, pageCount);
                }
            }
            totalPages += pageCount;
            expectAddress = blockAddress = testAddress;
            pageCount = 0;
        }
        expectAddress += PAGE_SIZE;
        ++pageCount;
    }
    totalPages += pageCount;
    if (!bigMapMode) {
        aprintf("%08x %5d\n", blockAddress, pageCount);
        aprintf("-------- -----\n");
        aprintf("         %5d total pages\n", totalPages);
    }
}

  t_dll *
findDLLByName(char *shortname, t_dump *dump)
{
    t_dll *dll = dump->dll;
    char buf[BUFLEN];

    while (dll) {
        if (dll->address) {
            char *testname = strrchr(dll->name, '\\');
            if (testname)
                testname++;
            else
                testname = dll->name;
            sprintf(buf, "%s.dll", shortname);
            if (strcasecmp(buf, testname) == 0)
                return(dll);
            sprintf(buf, "%s.exe", shortname);
            if (strcasecmp(buf, testname) == 0)
                return(dll);
        }
        dll = dll->next;
    }
    return(NULL);
}

  void
printDetailedVMap(t_dump *dump)
{
    t_vmap *vmap = dump->vmap;
    if (vmap) {
        printZoneTag();
        aprintf(" address      size allcprot prot     state   type\n");
        aprintf(" -------- -------- -------- -------- ------- ------\n");
        while (vmap) {
            if (inzone(vmap->address, vmap->size)) {
                char *tag;
                if (vmap->state == VM_STATE_COMMIT)
                    tag = wsTag(dump->wsCurrent, vmap->address, vmap->size);
                else
                    tag = " ";
                aprintf("%s%8x %8x", tag, vmap->address, vmap->size);
                aprintf(" %s", pProt(vmap->allocProtection));
                aprintf(" %s", pProt(vmap->protection));
                aprintf(" %s", pState(vmap->state));
                switch (vmap->type) {
                    case VM_TYPE_IMAGE:   aprintf(" image\n");   break;
                    case VM_TYPE_MAPPED:  aprintf(" mapped\n");  break;
                    case VM_TYPE_PRIVATE: aprintf(" private\n"); break;
                    case VM_TYPE_UNKNOWN: aprintf(" image\n");   break;
                }
            }
            vmap = vmap->next;
        }
    } else {
        aprintf("no virtual address map info in dump\n");
    }
}

  void
printDLLs(t_dump *dump, bool bigMapMode)
{
    t_dll *dll = dump->dll;

    if (dll) {
        if (!bigMapMode) {
            printZoneTag();
            aprintf(" address filename\n");
            aprintf("-------- --------\n");
        }
        while (dll) {
            if (inzone(dll->address, 1)) {
                if (bigMapMode) {
                    aprintf("%08x dl %s\n", dll->address, pDLLName(dll));
                } else {
                    aprintf("%8x %s\n", dll->address, dll->name);
                }
            }
            dll = dll->next;
        }
    } else {
        aprintf("no DLL info in dump\n");
    }
}

  void
printVMap(t_dump *dump)
{
    t_vmap *vmap = dump->vmap;
    int reserveBytes = 0;
    int reserveCount = 0;
    int commitBytes = 0;
    int commitCount = 0;
    int freeBytes = 0;
    int freeCount = 0;
    int unknownBytes = 0;
    int unknownCount = 0;

    if (vmap) {
        long baseAddress;
        int cumSize = 0;
        char *tag;

        printZoneTag();
        aprintf(" address      size prot     state\n");
        aprintf(" -------- -------- -------- -------\n");
        while (vmap) {
            if (inzone(vmap->address, vmap->size)) {
                switch (vmap->state) {
                    case VM_STATE_COMMIT:
                        commitBytes += vmap->size;
                        ++commitCount;
                        break;
                    case VM_STATE_RESERVE:
                        reserveBytes += vmap->size;
                        ++reserveCount;
                        break;
                    case VM_STATE_FREE:
                        freeBytes += vmap->size;
                        ++freeCount;
                        break;
                    case VM_STATE_UNKNOWN:
                        unknownBytes += vmap->size;
                        ++unknownCount;
                        break;
                }
                if (cumSize == 0)
                    baseAddress = vmap->address;
                cumSize += vmap->size;
                if (vmap->state == VM_STATE_COMMIT)
                    tag = wsTag(dump->wsCurrent, baseAddress, cumSize);
                else
                    tag = " ";
                if (VerboseMode) {
                    if (!vmap->next || vmap->state != vmap->next->state ||
                        vmap->protection != vmap->next->protection) {
                        aprintf("%s%8x %8x %s %s\n", tag, baseAddress, cumSize,
                                pProt(vmap->protection), pState(vmap->state));
                        cumSize = 0;
                    }
                } else {
                    int testProt;
                    int nextTestProt;
                    testProt = (vmap->protection == VM_PROT_NOACCESS);
                    if (vmap->next)
                        nextTestProt =
                            (vmap->next->protection == VM_PROT_NOACCESS);
                    if (!vmap->next || vmap->state != vmap->next->state ||
                            testProt != nextTestProt) {
                        aprintf("%s%8x %8x %s %s\n", tag, baseAddress, cumSize,
                                testProt ? "no-acc  " : "acc     ",
                                pState(vmap->state));
                        cumSize = 0;
                    }
                }
            }
            vmap = vmap->next;
        }
        aprintf("\nVirtual memory summary:\n");
        printZoneTag();
        aprintf("state      bytes blocks\n");
        aprintf("------- -------- ------\n");
        aprintf("commit  %8x %6d\n", commitBytes, commitCount);
        aprintf("reserve %8x %6d\n", reserveBytes, reserveCount);
        aprintf("free    %8x %6d\n", freeBytes, freeCount);
        aprintf("unknown %8x %6d\n", unknownBytes, unknownCount);
        aprintf("------- -------- ------\n");
        aprintf("  total %8x %6d\n",
                commitBytes + reserveBytes + freeBytes + unknownBytes,
                commitCount + reserveCount + freeCount + unknownCount);
    } else {
        aprintf("no virtual address map info in dump\n");
    }
}

  void
printVMapForBigMap(t_dump *dump)
{
    t_vmap *vmap = dump->vmap;
    if (vmap) {
        long baseAddress;
        int cumSize = 0;

        while (vmap) {
            if (cumSize == 0)
                baseAddress = vmap->address;
            cumSize += vmap->size;
            if (!vmap->next || vmap->state != vmap->next->state ||
                vmap->protection != vmap->next->protection) {
                if (inzone(vmap->address, vmap->size)) {
                    aprintf("%08x vs %x %c %c %c\n", baseAddress, cumSize,
                            cProt(vmap->allocProtection),
                            cProt(vmap->protection), cState(vmap->state));
                    aprintf("%08x ve\n", baseAddress + cumSize - 1);
                    cumSize = 0;
                }
            }
            vmap = vmap->next;
        }
    }
}

  void
printVMStats(t_dump *dump)
{
    t_vmStat *chaser = dump->vmStats;
    aprintf("Virtual memory operations:\n");
    printZoneTag();
    aprintf("      op address  size\n");
    aprintf("-------- -------- ------\n");
    while (chaser) {
        if (inzone(chaser->address, chaser->size))
            aprintf("%8s %8x 0x%x\n", chaser->mode, chaser->address,
                    chaser->size);
        chaser = chaser->next;
    }
}

  void
printVMStatsForBigMap(t_dump *dump)
{
    t_vmStat *chaser = dump->vmStats;
    while (chaser) {
        if (inzone(chaser->address, chaser->size)) {
            aprintf("%08x Vs %x %c\n", chaser->address, chaser->size,
                    toupper(chaser->mode[0]));
            aprintf("%08x Ve\n", chaser->address + chaser->size - 1);
        }
        chaser = chaser->next;
    }
}

  void
printWorkingSetForBigMap(t_dump *dump)
{
    printWorkingSet(dump->wsCurrent, TRUE);
}

  void
printWorkingSetPages(char *setname, t_dump *dump)
{
    if (setname[0] == '\0') {
        if (dump->wsCurrent)
            printWorkingSet(dump->wsCurrent, FALSE);
        else
            aprintf("current working set is null\n");
    } else {
        t_wsInfo *chaser = dump->wsInfo;
        while (chaser) {
            if (strcmp(setname, chaser->tag) == 0) {
                printWorkingSet(chaser, FALSE);
                return;
            }
            chaser = chaser->next;
        }
        aprintf("no working set named '%s'\n", setname);
    }
}

  void
setCurrentWorkingSet(char *setname, t_dump *dump)
{
    t_wsInfo *chaser = dump->wsInfo;

    if (setname[0] == '\0') {
        aprintf("available working sets:\n");
        while (chaser) {
            aprintf("%s%s %d\n", chaser == dump->wsCurrent ? "*" : " ",
                    chaser->tag, chaser->count);
            chaser = chaser->next;
        }
    } else if (strcmp(setname, "null") == 0) {
        dump->wsCurrent = NULL;
        aprintf("current working set is now null\n");
    } else {
        while (chaser) {
            if (strcmp(setname, chaser->tag) == 0) {
                dump->wsCurrent = chaser;
                aprintf("current working set is now %s\n", chaser->tag);
                return;
            }
            chaser = chaser->next;
        }
        aprintf("no working set named '%s'\n", setname);
    }
}

  void
wsAdd(t_wsInfo *ws, longword address)
{
    int hi;

    address &= PAGE_MASK;
    hi = WS_ADDR_HI(address);
    if (!ws->hiInfo[hi]) {
        int i;

        ws->hiInfo[hi] = TypeAlloc(t_wsHiInfo);
        for (i=0; i<WS_LO_SIZE; ++i)
            ws->hiInfo[hi]->loInfo[i] = 0;
    }
    ws->hiInfo[hi]->loInfo[WS_LO_INDEX(address)] |= WS_LO_BIT(address);
}

  longword
wsNextAddress(t_wsInfo *ws, longword address)
{
    int hi, lo, bit;
    int startHi, startLo, startBit;

    address &= PAGE_MASK;
    address += PAGE_SIZE;
    
    startHi = WS_ADDR_HI(address);
    startLo = WS_LO_INDEX(address);
    startBit = WS_LO_BITNUM(address);
    for (hi = startHi; hi<WS_HI_SIZE; ++hi) {
        t_wsHiInfo *hiInfo = ws->hiInfo[hi];
        if (hiInfo) {
            for (lo=startLo; lo<WS_LO_SIZE; ++lo) {
                longword loWord = hiInfo->loInfo[lo];
                if (loWord) {
                    for (bit=startBit; bit<WS_MAPWORD_SIZE; ++bit) {
                        if (loWord & (1 << bit))
                            return(WS_UNMAP(hi, lo, bit));
                    }
                    startBit = 0;
                }
            }
        }
        startLo = 0;
    }
    return(0);
}

  char *
wsTag(t_wsInfo *ws, longword address, int size)
{
    if (ws) {
        longword startPage = address & PAGE_MASK;
        longword endPage = (address + size - 1) & PAGE_MASK;
        bool hitAll = TRUE;
        bool hitSome = FALSE;
        
        while (startPage <= endPage) {
            if (wsTest(ws, startPage))
                hitSome = TRUE;
            else
                hitAll = FALSE;
            if (hitSome && !hitAll)
                return("~");
            ++startPage;
        }
        if (hitSome)
            return("*");
    }
    return(" ");
}

  bool
wsTest(t_wsInfo *ws, longword address)
{
    int hi;

    if (ws) {
        address &= PAGE_MASK;
        hi = WS_ADDR_HI(address);
        if (!ws->hiInfo[hi])
            return(FALSE);
        else
            return((ws->hiInfo[hi]->loInfo[WS_LO_INDEX(address)] &
                    WS_LO_BIT(address)) != 0);
    } else {
        return(FALSE);
    }
}
