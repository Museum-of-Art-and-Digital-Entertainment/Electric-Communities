/*
  adoutput.c -- Output generation for the Analyze Dump program.

  Chip Morningstar
  Electric Communities
  6-January-1998

  Copyright 1998 Electric Communities, all rights reserved worldwide.

*/

#include "analdump.h"
#include <string.h>

extern char *VersionString;

  void
printBigMap(t_dump *dump)
{
    printVMStatsForBigMap(dump);
    printVMapForBigMap(dump);
    printDLLs(dump, TRUE);
    printHeapZones(dump, TRUE);
    printWorkingSetForBigMap(dump);
}

  void
printDescriptiveHeader(t_dump *dump)
{
    if (dump->descriptiveHeader)
        aprintf("%s", dump->descriptiveHeader);
}

  void
printDumpStats(t_dump *dump, bool printSummary)
{
    t_summary *summ = dump->summaryInfo;

    aprintf("analyzing: ");
    printDescriptiveHeader(dump);
    aprintf("%d Java objects, %d classes, %d bytes\n",
           dump->objectCount, dump->classCount, dump->byteCount);
    aprintf("%d non-Java objects, %d creators, %d bytes\n",
            dump->mallocCount, dump->mallocCallerCount, dump->mallocBytes);
    if (dump->wsCount)
        aprintf("%d working sets\n", dump->wsCount);
    if (printSummary) {
        aprintf("summary info:\n");
        while (summ) {
            aprintf("  %s", summ->info);
            summ = summ->next;
        }
    }
}

  void
printVersion(void)
{
    aprintf("%s\n", VersionString);
}

  void
printZoneTag(void)
{
    if (!allzone())
        aprintf("(zone %x-%x)\n", ZoneLo, ZoneHi);
}
