// ArchivingStack.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.util;

/** This private class is used by the Archiver and Unarchiver to maintain
  * the stack of states associated with archiving and unarchiving a graph
  * of objects. The ArchivingStack tries to be efficient in space and time
  * so some of the stuff here is gross, but at least it's private. The two
  * hacks which are important to know about are:
  *
  * 1. The parts of the Archiver/Unarchiver state which are Objects are packed
  * into one array, and the parts which are ints are all packed into another.
  * Don't mess up the multiples and offsets!
  *
  * 2. Because this has to reach into both Archivers and Unarchivers there
  * are separate push and pop methods for each to satisfy the type system.
  * Do not mix and match!
  */
final class ArchivingStack {
    int depth;
    int maxDepth;
    Object objArray[];
    int intArray[];

    ArchivingStack() {
        super();
    }

    private void growArrays() {
        Object newObjArray[];
        int newIntArray[];

        if (maxDepth == 0)
            maxDepth = 8;
        else
            maxDepth = 2 * maxDepth;

        newObjArray = new Object[2 * maxDepth];
        newIntArray = new int[4 * maxDepth];

        if (objArray != null && intArray != null) {
            System.arraycopy(objArray, 0, newObjArray, 0, objArray.length);
            System.arraycopy(intArray, 0, newIntArray, 0, intArray.length);
        }

        objArray = newObjArray;
        intArray = newIntArray;
    }

    void pushArchiver(Archiver archiver) {
        int i;

        depth++;
        i = depth;

        if (depth >= maxDepth)
            growArrays();

        objArray[2 * i + 0] = archiver.currentObject;
        objArray[2 * i + 1] = archiver.currentTable;

        intArray[4 * i + 0] = archiver.currentId;
        intArray[4 * i + 1] = archiver.currentColumnCount;
        intArray[4 * i + 2] = archiver.currentRow;
        intArray[4 * i + 3] = archiver.currentColumn;
    }

    void pushUnarchiver(Unarchiver unarchiver) {
        int i;

        depth++;
        i = depth;

        if (depth >= maxDepth)
            growArrays();

        objArray[2 * i + 0] = unarchiver.currentObject;
        objArray[2 * i + 1] = unarchiver.currentTable;

        intArray[4 * i + 0] = unarchiver.currentId;
        intArray[4 * i + 1] = unarchiver.currentColumnCount;
        intArray[4 * i + 2] = unarchiver.currentRow;
        intArray[4 * i + 3] = unarchiver.currentColumn;
    }

    void popArchiver(Archiver archiver) {
        int i = depth;

        archiver.currentObject = objArray[2 * i + 0];
        objArray[2 * i + 0] = null;
        archiver.currentTable  = (ClassTable)objArray[2 * i + 1];
        objArray[2 * i + 1] = null;

        archiver.currentId          = intArray[4 * i + 0];
        archiver.currentColumnCount = intArray[4 * i + 1];
        archiver.currentRow         = intArray[4 * i + 2];
        archiver.currentColumn      = intArray[4 * i + 3];

        depth--;
    }

    void popUnarchiver(Unarchiver unarchiver) {
        int i = depth;

        unarchiver.currentObject = objArray[2 * i + 0];
        objArray[2 * i + 0] = null;
        unarchiver.currentTable  = (ClassTable)objArray[2 * i + 1];
        objArray[2 * i + 1] = null;

        unarchiver.currentId          = intArray[4 * i + 0];
        unarchiver.currentColumnCount = intArray[4 * i + 1];
        unarchiver.currentRow         = intArray[4 * i + 2];
        unarchiver.currentColumn      = intArray[4 * i + 3];

        depth--;
    }
}
