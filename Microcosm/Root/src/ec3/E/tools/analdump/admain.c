/*
  admain.c -- Main for the Analyze Dump program.

  Chip Morningstar
  Electric Communities
  3-December-1997

  Copyright 1997 Electric Communities, all rights reserved worldwide.

*/

#include "analdump.h"

  static void
usage()
{
    fprintf(stderr, "usage: analdump dumpfilename [dumpnumber] \n");
    exit(1);
}

  void
main(int argc, char *argv[])
{
    t_dump *dump;
    int dumpNumber;

    if (argc != 2 && argc != 3)
        usage();
    if (argc == 3)
        dumpNumber = atoi(argv[2]);
    else
        dumpNumber = 1;

    if (dumpNumber < 1) {
        fprintf(stderr, "dump number must be >=1\n");
        exit(1);
    }

    initializeOutput();
    dump = parseDumpFile(argv[1], dumpNumber);
    if (dump) {
        printVersion();
        printDumpStats(dump, FALSE);
        processCommands(dump, stdin);
    } else {
        fprintf(stderr, "couldn't parse dump file\n");
        exit(1);
    }
    exit(0);
}
