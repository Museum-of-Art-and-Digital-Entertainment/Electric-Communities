/*
  adcommand.c -- Interactive command handling for the Analyze Dump program.

  Chip Morningstar
  Electric Communities
  19-January-1998

  Copyright 1998 Electric Communities, all rights reserved worldwide.

*/

#include "analdump.h"
#include <string.h>
#include <ctype.h>

bool VerboseMode = FALSE;
longword ZoneLo = ZONE_MIN;
longword ZoneHi = ZONE_MAX;

  static char *
arg(char **line)
{
    char *result;

    while (isspace(**line))
        ++*line;
    result = *line;
    while (!isspace(**line) && **line != '\0')
        ++*line;
    if (**line != '\0') {
        **line = '\0';
        ++*line;
    }
    return(result);
}

  static void
printCommandHelp()
{
    aprintf("Available commands:\n");
    aprintf(" a            -* print a map of the virtual address space\n");
    aprintf(" A            -* same, but much more detailed\n");
    aprintf(" B            -* print a BIG map of interesting stuff\n");
    aprintf(" c <name>     -- print info about a class\n");
    aprintf(" C            -- list all classes\n");
    aprintf(" d            -* list loaded DLLs\n");
    aprintf(" e <address>  -- list objects reachable from this object only\n");
    aprintf(" f            -* analyze malloc heap fragmentation\n");
    aprintf(" F            -* list malloc heap fragments\n");
    aprintf(" g <address>  -- analyze reachability graph of an object\n");
    aprintf(" G            -- analyze reachability graph of all objects\n");
    aprintf(" H            -- list heap zones\n");
    aprintf(" i <name>     -- list all instances of a class\n");
    aprintf(" j <address>  -- analyze class reachability of an object\n");
    aprintf(" l [<fdname>] -- read and use Windows link .MAP file(s)\n");
    aprintf(" m <caller>   -* list the malloc blocks created by a caller\n");
    aprintf(" M [<sortby>] -* list malloc callers\n");
    aprintf(" n <name>     -- show number of instances of a class\n");
    aprintf(" o <address>  -- print info about an object\n");
    aprintf(" O            -- list all objects\n");
    aprintf(" p            -- show globally interesting information\n");
    aprintf(" P            -- show analdump program date & version info\n");
    aprintf(" r <address>  -- list all references to an object\n");
    aprintf(" R <address>  -- list all recursive references to an object\n");
    aprintf(" s <name>     -- show space usage info for a class\n");
    aprintf(" S [<sortby>] -- list space usage info for all classes\n");
    aprintf(" v            -* list virtual memory alloc history\n");
    aprintf(" V            -- toggle verbose mode\n");
    aprintf(" w [<set>]    -- set current working set or list sets\n");
    aprintf(" W [<set>]    -- list pages in current working set or <set>\n");
    aprintf(" y <ty> <ca>  -- set class overhead malloc caller\n");
    aprintf(" z <a1> <a2>  -- set address zone range for other operations\n");
    aprintf(" z all        -- set address zone range to include all memory\n");
    aprintf(" h or ?       -- print this helpful message\n");
    aprintf(" < <filename> -- read commands from a file\n");
    aprintf(" & <filename> -- log all output to a file\n");
    aprintf(" q            -- quit\n");
    aprintf("commands marked with * are modified by current zone setting\n");
}

  static void
setZone(char *lo, char *hi)
{
    if (lo[0] == '\0') {
        if (allzone())
            aprintf("zone includes all addresses\n");
        else
            aprintf("zone is %x-%x\n", ZoneLo, ZoneHi);
    } else if (strcmp(lo, "all") == 0) {
        ZoneLo = ZONE_MIN;
        ZoneHi = ZONE_MAX;
        aprintf("zone now includes all addresses\n");
    } else {
        longword newLo, newHi;
        if (strcmp(lo, "lo") == 0) {
            newLo = ZONE_MIN;
        } else if (sscanf(lo, "%x", &newLo) != 1) {
            aprintf("invalid low address %s\n", lo);
            return;
        }
        if (strcmp(hi, "hi") == 0) {
            newHi = ZONE_MAX;
        } else if (sscanf(hi, "%x", &newHi) != 1) {
            aprintf("invalid high address %s\n", hi);
            return;
        }
        if (newHi == newLo) {
            aprintf("you can't have a zero sized zone\n");
            return;
        }
        if (newHi < newLo) {
            ZoneLo = newHi;
            ZoneHi = newLo;
        } else {
            ZoneLo = newLo;
            ZoneHi = newHi;
        }
        aprintf("zone is now %x-%x\n", ZoneLo, ZoneHi);
    }
}

  static void
toggleVerboseMode()
{
    VerboseMode = !VerboseMode;
    aprintf("verbose mode is %s\n", VerboseMode ? "on" : "off");
}

  static bool
processCommandFile(char *filename, t_dump *dump)
{
    FILE *fyle = fopen(filename, "r");

    if (fyle) {
        bool result = processCommands(dump, fyle);
        fclose(fyle);
        return(result);
    } else {
        fprintf(stderr, "can't open file %s\n", filename);
        return(FALSE);
    }
}

  static bool
processOneCommand(char *line, t_dump *dump)
{
    char *chaser = line;

    while (isspace(*chaser))
        ++chaser;
    switch (*chaser++) {
        case 'A': printDetailedVMap(dump);                      break;
        case 'a': printVMap(dump);                              break;
        case 'B': printBigMap(dump);                            break;
        case 'C': printClassTable(dump);                        break;
        case 'c': printClass(arg(&chaser), dump);               break;
        case 'd': printDLLs(dump, FALSE);                       break;
        case 'e': printReachableObjects(arg(&chaser), dump);    break;
        case 'f': printMallocFragmentation(dump, FALSE);        break;
        case 'F': printMallocFragmentation(dump, TRUE);         break;
        case 'g': printReachability(arg(&chaser), dump);        break;
        case 'G': printGlobalReachability(dump);                break;
        case 'H': printHeapZones(dump, FALSE);                  break;
        case 'i': printInstances(arg(&chaser), dump);           break;
        case 'j': printClassReachability(arg(&chaser), dump);   break;
        case 'l': internalizeLinkMap(arg(&chaser), dump);       break;
        case 'm': printMallocBlocks(arg(&chaser), dump);        break;
        case 'M': printMallocTable(arg(&chaser), dump);         break;
        case 'n': printInstanceCount(arg(&chaser), dump);       break;
        case 'O': printObjectTable(dump);                       break;
        case 'o': printObject(arg(&chaser), dump);              break;
        case 'P': printVersion();                               break;
        case 'p': printDumpStats(dump, TRUE);                   break;
        case 'r': printReferences(arg(&chaser), dump);          break;
        case 'R': printRecursiveReferences(arg(&chaser), dump); break;
        case 'S': printSizeSummary(arg(&chaser), dump);         break;
        case 's': printSizeInfo(arg(&chaser), dump);            break;
        case 'v': printVMStats(dump);                           break;
        case 'V': toggleVerboseMode();                          break;
        case 'w': setCurrentWorkingSet(arg(&chaser), dump);     break;
        case 'W': printWorkingSetPages(arg(&chaser), dump);     break;
        case 'y': {
            char *arg1 = arg(&chaser);
            char *arg2 = arg(&chaser);
            setClassOverheadSource(arg1, arg2, dump);
            break;
        }
        case 'z': {
            char *arg1 = arg(&chaser);
            char *arg2 = arg(&chaser);
            setZone(arg1, arg2);
            break;
        }
        case 'h': case '?': printCommandHelp();                 break;
        case '&': logOutput(arg(&chaser));                      break;
        case '<': return(processCommandFile(arg(&chaser), dump));
        case 'q': return(TRUE);
        case EOF: return(TRUE);
        case '\0': return(FALSE);
        default: fprintf(stderr, "that is not an analdump command\n");
    }
    return(FALSE);
}

  static void
stripNewline(char *s)
{
    while (*s) {
        if (*s == '\n')
            *s = '\0';
        else
            ++s;
    }
}

  bool
allzone(void)
{
    return(ZoneLo == ZONE_MIN && ZoneHi == ZONE_MAX);
}

  bool
inzone(longword addr, int len)
{
    return(ZoneLo <= addr && addr + len <= ZoneHi);
}

  bool
processCommands(t_dump *dump, FILE *in)
{
    char buf[BUFLEN];

    if (in == stdin) {
        printf("eh? ");
        fflush(stdout);
    }
    while (fgets(buf, BUFLEN, in)) {
        stripNewline(buf);
        if (handleOutputRedirection(buf)) {
            if (processOneCommand(buf, dump))
                return(TRUE);
            restoreOutput();
        }
        if (in == stdin) {
            printf("eh? ");
            fflush(stdout);
        }
    }
    return(FALSE);
}

  bool
touchzone(longword addr, int len)
{
    return(addr <= ZoneHi && addr + len >= ZoneLo);
}
