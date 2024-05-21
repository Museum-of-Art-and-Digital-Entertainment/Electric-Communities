/*
  admoutput.c -- Analdump output generation for C malloc-related stuff

  Chip Morningstar
  Electric Communities
  6-January-1998

  Copyright 1998 Electric Communities, all rights reserved worldwide.

*/

#include "analdump.h"
#include <string.h>

static char *pFile(char *filename, bool verboseMode);
static t_malloc *findMalloc(t_mallocCaller *caller, longword address);
static t_mallocCaller *getMallocCaller(char *callerString, t_dump *dump);

#define DEFAULT_CALLER_CONSTANT_POOL     "classloader.c:476"
#define DEFAULT_CALLER_METHOD_TABLE      "classresolver.c:310"

  static char *
pCall(t_mallocCaller *caller, t_dump *dump)
{
    static char result[BUFLEN];
    char *pname = pFile(caller->filename, VerboseMode);
    if (pname[0] == '*') {
        if (caller->funcname)
            sprintf(result, "%s:%08x (%s:%s)", pname, caller->line,
                    pDLL(caller->line, dump), caller->funcname);
        else
            sprintf(result, "%s:%08x (%s)", pname, caller->line,
                    pDLL(caller->line, dump));
        if (caller->mapFileInfo)
            sprintf(result + strlen(result), " %s (%s)",
                    caller->mapFileInfo->name, caller->mapFileInfo->objfile);
            
    } else {
        if (caller->funcname)
            sprintf(result, "%s:%d (%s)", pname, caller->line,
                    caller->funcname);
        else
            sprintf(result, "%s:%d", pname, caller->line);
    }
    return(result);
}

  static char *
pFile(char *filename, bool verboseMode)
{
    if (!verboseMode) {
        char *result = strrchr(filename, '/');
        if (result)
            return(result + 1);
        result = strrchr(filename, '\\');
        if (result)
            return(result + 1);
    }
    return(filename);
}

  static int
cmpMallocBytes(const void *p1, const void *p2)
{
    t_mallocCaller **m1 = (t_mallocCaller **)p1;
    t_mallocCaller **m2 = (t_mallocCaller **)p2;
    int test = (*m1)->bytes - (*m2)->bytes;
    if (test)
        return(test);
    else
        return(cmpMallocCallers(p1, p2));
}

  static int
cmpMallocBytesReverse(const void *p1, const void *p2)
{
    t_mallocCaller **m1 = (t_mallocCaller **)p1;
    t_mallocCaller **m2 = (t_mallocCaller **)p2;
    int test = (*m2)->bytes - (*m1)->bytes;
    if (test)
        return(test);
    else
        return(cmpMallocCallers(p1, p2));
}

  static int
cmpMallocCounts(const void *p1, const void *p2)
{
    t_mallocCaller **m1 = (t_mallocCaller **)p1;
    t_mallocCaller **m2 = (t_mallocCaller **)p2;
    int test = (*m1)->count - (*m2)->count;
    if (test)
        return(test);
    else
        return(cmpMallocCallers(p1, p2));
}

  static int
cmpMallocCountsReverse(const void *p1, const void *p2)
{
    t_mallocCaller **m1 = (t_mallocCaller **)p1;
    t_mallocCaller **m2 = (t_mallocCaller **)p2;
    int test = (*m2)->count - (*m1)->count;
    if (test)
        return(test);
    else
        return(cmpMallocCallers(p1, p2));
}

#define ALLOC_GRAIN     4
#define ALLOC_BITS      2
#define ROUND_TO_GRAIN(n)  ((((n) - 1) & ~(ALLOC_GRAIN - 1)) + ALLOC_GRAIN)

#define MAPWORD_BITS    5
#define MAPWORD_SIZE    (1 << MAPWORD_BITS)
#define MAP_MASK        (MAPWORD_SIZE - 1)

#define MAP_INDEX(addr) \
    (((addr) - dump->minAddress) >> (ALLOC_BITS + MAPWORD_BITS))
#define MAP_ELEM(addr) \
    ((((addr) - dump->minAddress) >> ALLOC_BITS) & MAP_MASK)
#define MAP_BIT(addr)   (1 << MAP_ELEM(addr))
#define SET_BIT(map, addr)  (map[MAP_INDEX(addr)] |= MAP_BIT(addr))
#define TST_BIT(map, addr)  (map[MAP_INDEX(addr)] &  MAP_BIT(addr))

#define ADDR(index, bitn) \
    (((((index) << MAPWORD_BITS) + (bitn)) << ALLOC_BITS) + dump->minAddress)
#define TST_ADDR(map, index, bitn) (map[index] & (1 << (bitn)))

  static void
computeMap(t_dump *dump)
{
    int i;
    longword range;
    int duplicateCount = 0;

    if (dump->maxAddress == 0) {
        for (i=0; i<dump->mallocCallerCount; ++i) {
            t_malloc *aMalloc = dump->sortedMallocs[i]->mallocs;
            while (aMalloc) {
                longword size = ROUND_TO_GRAIN(aMalloc->size + StatOverhead);
                longword bottom = (int) aMalloc->address - HeapOverhead;
                longword top    = (int) aMalloc->address + size;
                if (bottom < dump->minAddress)
                    dump->minAddress = bottom;
                if (top > dump->maxAddress)
                    dump->maxAddress = top;
                aMalloc = aMalloc->next;
            }
        }
    }

    range = dump->maxAddress - dump->minAddress;

    dump->mapSize = range / ALLOC_GRAIN; 
    dump->mapSize = dump->mapSize / MAPWORD_SIZE + 1;
    dump->map = TypeAllocMulti(longword, dump->mapSize);
    for (i=0; i<dump->mapSize; ++i)
        dump->map[i] = 0;

    for (i=0; i<dump->mallocCallerCount; ++i) {
        t_malloc *aMalloc = dump->sortedMallocs[i]->mallocs;
        while (aMalloc) {
            int size =
                ROUND_TO_GRAIN(aMalloc->size + StatOverhead) + HeapOverhead;
            int addr = (int)aMalloc->address - HeapOverhead;
            while (size > 0) {
                if (TST_BIT(dump->map, addr)) {
                    if (++duplicateCount < 10)
                        fprintf(stderr, "duplicate malloc hit at %x\n", addr);
                }
                SET_BIT(dump->map, addr);
                addr += ALLOC_GRAIN;
                size -= ALLOC_GRAIN;
            }
            aMalloc = aMalloc->next;
        }
    }
}

  static void
findClassOverhead(int item, char *callerName, t_dump *dump)
{
    t_mallocCaller *caller;
    char buf[BUFLEN];

    strcpy(buf, callerName);
    caller = getMallocCaller(buf, dump);
    if (caller) {
        int misses = 0;
        int finds = 0;
        char *tag;
        int i;

        for (i=0; i<dump->classCount; ++i) {
            longword address;
            t_malloc **target;
            t_class *theClass = dump->sortedClasses[i];
            switch (item) {
                case FIND_CONSTANT_POOL:
                    target = &theClass->constantPoolMalloc;
                    address = theClass->constantPoolAddress;
                    tag = "constantpool";
                    break;
                case FIND_METHOD_TABLE:
                    target = &theClass->methodTableMalloc;
                    address = theClass->methodTableAddress;
                    tag = "methodtable";
                    break;
            }
            if (address == 0) {
                *target = NULL;
            } else {
                *target = findMalloc(caller, address);
                ++finds;
                if (!*target)
                    ++misses;
            }
        }
        if (misses)
            fprintf(stderr, "missing %d/%d %s malloc items from creator at %s\n",
                    misses, finds, tag, callerName);
    }
}

  static t_malloc *
findMalloc(t_mallocCaller *caller, longword address)
{
    t_malloc *chaser = caller->mallocs;
    while (chaser) {
        if (chaser->address + StatOverhead == address)
            return(chaser);
        else
            chaser = chaser->next;
    }
    return(NULL);
}

  static t_mallocCaller *
getMallocCaller(char *callerString, t_dump *dump)
{
    char *colon = strrchr(callerString, ':');
    int line;
    bool shortName;
    bool hit;
    int i;

    if (colon) {
        if (callerString[0] == '*')
            sscanf(colon + 1, "%x", &line);
        else
            line = atoi(colon + 1);
        *colon = '\0';
    } else {
        line = -1;
    }

    shortName = (!strrchr(callerString, '/') && !strrchr(callerString, '\\'));
    for (i=0; i<dump->mallocCallerCount; ++i) {
        t_mallocCaller *caller = dump->sortedMallocs[i];

        if (shortName)
            hit = (strcmp(callerString, pFile(caller->filename, FALSE)) == 0);
        else
            hit = (strcmp(callerString, caller->filename) == 0);
        if (hit) {
            if (line == -1) {
                if (i == dump->mallocCallerCount-1 ||
                        strcmp(caller->filename,
                               dump->sortedMallocs[i+1]->filename)) {
                    return(caller);
                } else {
                    fprintf(stderr, "ambiguous caller reference\n");
                    return(NULL);
                }
            } else if (line == caller->line) {
                return(caller);
            }
        }
    }
    fprintf(stderr, "caller reference not found\n");
    return(NULL);
}

  static void
printMallocBlocksForCaller(t_mallocCaller *caller, t_dump *dump, int *count,
                           int *bytes)
{
    t_malloc *aMalloc = caller->mallocs;
    
    while (aMalloc) {
        if (inzone(aMalloc->address, aMalloc->size)) {
            aprintf(" %s%8x %d\n", wsTag(dump->wsCurrent, aMalloc->address,
                                         aMalloc->size),
                    aMalloc->address, aMalloc->size);
            *count += 1;
            *bytes += aMalloc->size;
        }
        aMalloc = aMalloc->next;
    }
}

  static void
printMallocZoneFragmentation(t_dump *dump, bool verbose, longword minAddress,
    longword maxAddress, int *gapCount, int *maxGapSize, int *totalGapSize)
{
    int i;
    bool filled;
    longword baseAddress, topAddress;
    int startIndex, endIndex;

    if (maxAddress < ZoneLo || ZoneHi < minAddress)
        return;

    baseAddress = (minAddress < ZoneLo) ? ZoneLo : minAddress;
    startIndex = MAP_INDEX(baseAddress);
    topAddress = (maxAddress > ZoneHi) ? ZoneHi : maxAddress;
    endIndex = MAP_INDEX(topAddress);

    for (i=startIndex; i<=endIndex; ++i) {
        int j;
        int jstart = (i == startIndex) ? MAP_ELEM(baseAddress) : 0;
        int jend = (i == endIndex) ? (MAP_ELEM(topAddress) + 1) :
                                     (MAPWORD_SIZE - 1);
        if (i == startIndex)
            filled = TST_ADDR(dump->map, i, jstart);

        for (j=jstart; j<=jend; ++j) {
            longword newBaseAddress;
            bool test = TST_ADDR(dump->map, i, j);
            if (i == endIndex && j == jend)
                test = (test && filled) || (!test && !filled);

            if (test) {
                if (!filled) {
                    int gapSize;
                    filled = TRUE;
                    newBaseAddress = ADDR(i, j);
                    gapSize = newBaseAddress - baseAddress;
                    if (verbose) {
                        aprintf(" gap: 0x%08x-0x%08x (%d)\n", baseAddress,
                                newBaseAddress - 1, gapSize);
                    }
                    if (gapSize > *maxGapSize)
                        *maxGapSize = gapSize;
                    *totalGapSize += gapSize;
                    *gapCount += 1;
                    baseAddress = newBaseAddress;
                }
            } else {
                if (filled) {
                    filled = FALSE;
                    newBaseAddress = ADDR(i, j);
                    if (verbose) {
                        aprintf("fill: 0x%08x-0x%08x (%d)\n", baseAddress,
                                newBaseAddress - 1,
                                newBaseAddress - baseAddress);
                    }
                    baseAddress = newBaseAddress;
                }
            }
        }
    }
}

  void
findDefaultClassOverhead(t_dump *dump)
{
    findClassOverhead(FIND_CONSTANT_POOL, DEFAULT_CALLER_CONSTANT_POOL, dump);
    findClassOverhead(FIND_METHOD_TABLE, DEFAULT_CALLER_METHOD_TABLE, dump);
}

  void
printHeapZones(t_dump *dump, bool bigMapMode)
{
    int i;
    int totalSize = 0;
    int totalBlocks = 0;
    int totalUsed = 0;
    int totalFree = 0;

    if (!dump->vmap && !bigMapMode) {
        aprintf("no VM info available in dump; can't compute heap zones\n");
        return;
    }
    if (!bigMapMode) {
        aprintf("address               size  blks    used    free   load\n");
        aprintf("-------- --------  ------- ----- ------- ------- ------\n");
    }
    for (i=0; i<dump->heapZoneCount; ++i) {
        t_heap *heap = dump->heapZones[i];
        if (bigMapMode) {
            aprintf("%08x hs %x\n", heap->address, heap->size);
            aprintf("%08x he\n", heap->address + heap->size - 1);
        } else {
            aprintf("%08x %08x  %7x %5d %7x %7x %5.2f%%\n", heap->address,
                    heap->address + heap->size - 1, heap->size, heap->blocks,
                    heap->usedBytes, heap->freeBytes,
                    (((double)heap->usedBytes)/((double)heap->size)) * 100.0);
            totalSize += heap->size;
            totalBlocks += heap->blocks;
            totalUsed += heap->usedBytes;
            totalFree += heap->freeBytes;
        }
    }
    if (!bigMapMode) {
        aprintf("-----------------  ------- ----- ------- ------- ------\n");
        aprintf("          totals:  %7x %5d %7x %7x %5.2f%%\n",
                totalSize, totalBlocks, totalUsed, totalFree,
                (((double)totalUsed)/((double)totalSize)) * 100.0);
        printZoneTag();
    }
}

  void
printMallocBlocks(char *callerString, t_dump *dump)
{
    t_mallocCaller *caller;
    int count = 0;
    int bytes = 0;

    if (strcmp(callerString, "all") == 0) {
        int i;
        aprintf("   address size source\n");
        aprintf("  -------- ---- ------\n");
        for (i=0; i<dump->mallocCallerCount; ++i) {
            caller = dump->sortedMallocs[i];
            if (caller->mallocs)
                printMallocBlocksForCaller(caller, dump, &count, &bytes);
        }
    } else {
        caller = getMallocCaller(callerString, dump);
        if (caller) {
            aprintf("%s %d blocks, %d bytes total\n", pCall(caller, dump),
                    caller->count, caller->bytes);
            aprintf("   address size\n");
            aprintf("  -------- ----\n");
            printMallocBlocksForCaller(caller, dump, &count, &bytes);
        }
    }
    if (!allzone())
        aprintf("(zone %x-%x, %d blocks, %d bytes total)\n",
                ZoneLo, ZoneHi, count, bytes);
}

  void
printMallocFragmentation(t_dump *dump, bool verbose)
{
    int i;
    int maxGapSize = 0;
    int totalGapSize = 0;
    int gapCount = 0;

    if (!dump->map)
        computeMap(dump);

    if (dump->vmap) {
        for (i=0; i<dump->heapZoneCount; ++i) {
            t_heap *heap = dump->heapZones[i];
            printMallocZoneFragmentation(dump, verbose,
                heap->address, heap->address + heap->size - 1, &gapCount,
                &maxGapSize, &totalGapSize);
        }
    } else {
        printMallocZoneFragmentation(dump, verbose,
            dump->minAddress, dump->maxAddress - 1, &gapCount, &maxGapSize,
            &totalGapSize);
    }
    aprintf("%d gaps totalling %d bytes, max gap size %d bytes\n",
            gapCount, totalGapSize, maxGapSize);
}

  void
printMallocTable(char *sortby, t_dump *dump)
{
    int i;
    int totalCount = 0;
    int totalBytes = 0;
    bool printCumulative = TRUE;
    int (*cmpFunc)(const void *p1, const void *p2);

    if (strcmp(sortby, "size") == 0) {
        cmpFunc = cmpMallocBytes;
    } else if (strcmp(sortby, "rsize") == 0) {
        cmpFunc = cmpMallocBytesReverse;
    } else if (strcmp(sortby, "count") == 0) {
        cmpFunc = cmpMallocCounts;
    } else if (strcmp(sortby, "rcount") == 0) {
        cmpFunc = cmpMallocCountsReverse;
    } else if (strcmp(sortby, "name") == 0 || sortby[0] == '\0') {
        printCumulative = FALSE;
        cmpFunc = cmpMallocCallers;
    } else {
        fprintf(stderr, "valid sort keys are 'size', 'rsize', 'count', 'rcount' or 'name'\n");
        return;
    }
    qsort(dump->sortedMallocs, dump->mallocCallerCount,
          sizeof(t_mallocCaller *), cmpFunc);

    if (printCumulative) {
        aprintf("count cumct    bytes cumbytes  cumpct creator\n");
        aprintf("----- ----- -------- -------- ------- -------\n");
    } else {
        aprintf("count    bytes creator\n");
        aprintf("----- -------- -------\n");
    }
    for (i=0; i<dump->mallocCallerCount; ++i) {
        t_mallocCaller *caller = dump->sortedMallocs[i];
        int count, bytes;

        if (allzone()) {
            count = caller->count;
            bytes = caller->bytes;
        } else {
            t_malloc *aMalloc = caller->mallocs;
            count = 0;
            bytes = 0;
            while (aMalloc) {
                if (inzone(aMalloc->address, aMalloc->size)) {
                    ++count;
                    bytes += aMalloc->size;
                }
                aMalloc = aMalloc->next;
            }
        }
        totalCount += count;
        totalBytes += bytes;
        if (printCumulative) {
            aprintf("%5d %5d %8d %8d %6.2f%% %s\n", count, totalCount, bytes,
                    totalBytes,
                    (((double)totalBytes)/((double)dump->mallocBytes)) * 100.0,
                    pCall(caller, dump));
        } else {
            aprintf("%5d %8d %s\n", count, bytes, pCall(caller, dump));
        }
    }
    if (printCumulative) {
        aprintf("----- ----- -------- -------- ------- -------\n");
        aprintf("%5d %5d %8d %8d 100.00%% total\n", totalCount, totalCount,
                totalBytes, totalBytes);
    } else {
        aprintf("----- -------- -------\n");
        aprintf("%5d %8d total\n", totalCount, totalBytes);
    }
    printZoneTag();
}

  void
setClassOverheadSource(char *tag, char *source, t_dump *dump)
{
    if (strcmp(tag, "constantpool") == 0)
        findClassOverhead(FIND_CONSTANT_POOL, source, dump);
    else if (strcmp(tag, "methodtable") == 0)
        findClassOverhead(FIND_METHOD_TABLE, source, dump);
    else
        fprintf(stderr, "valid overhead items are 'constantpool' or 'methodtable'\n");
}
