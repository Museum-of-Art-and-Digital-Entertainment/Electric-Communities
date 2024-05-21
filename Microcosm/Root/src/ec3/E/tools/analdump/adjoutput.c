/*
  adjoutput.c -- Analdump output generation for Java-related stuff

  Chip Morningstar
  Electric Communities
  13-January-1998

  Copyright 1998 Electric Communities, all rights reserved worldwide.

*/

#include "analdump.h"
#include <string.h>

  static char *
pAddress(t_object *object)
{
    static char result[9];
    if (object == NULL) {
        return("00000000");
    } else {
        sprintf(result, "%08x", object->address);
        return(result);
    }
}

  static char *
pClassname(t_object *object, t_dump *dump)
{
    static char result[BUFLEN];

    if (isClassClass(object->theClass)) {
        t_class *theClass = lookupClassByAddress(object->address, dump);
        if (theClass) {
            sprintf(result," %s [%s]", object->theClass->name, theClass->name);
            return(result);
        }
    }
    return(object->theClass->name);
}

  static char *
pMarks(int flag)
{
    static char result[7];

    result[0] = (flag & ROOT_STACK)  ? 's' : '-';
    result[1] = (flag & ROOT_CSTACK) ? 'S' : '-';
    result[2] = (flag & ROOT_GLOBAL) ? 'g' : '-';
    result[3] = (flag & ROOT_STATIC) ? 'c' : '-';
    result[4] = (flag & ROOT_INTERN) ? 'i' : '-';
    result[5] = (flag & MARK_HEAP)   ? 'h' : '-';
    result[6] = '\0';
    return(result);
}

  static int
classOverhead(t_class *aClass)
{
    int result = 0;
    if (aClass->constantPoolMalloc)
        result += aClass->constantPoolMalloc->size;
    if (aClass->methodTableMalloc)
        result += aClass->methodTableMalloc->size;
    return(result);
}

  static int
cmpClassBytes(const void *p1, const void *p2)
{
    t_class **cl1 = (t_class **) p1;
    t_class **cl2 = (t_class **) p2;
    int test = (*cl1)->totalInstanceSize - (*cl2)->totalInstanceSize;
    if (test)
        return(test);
    else
        return(cmpClassNames(p1, p2));
}

  static int
cmpClassBytesReverse(const void *p1, const void *p2)
{
    t_class **cl1 = (t_class **) p1;
    t_class **cl2 = (t_class **) p2;
    int test = (*cl2)->totalInstanceSize - (*cl1)->totalInstanceSize;
    if (test)
        return(test);
    else
        return(cmpClassNames(p1, p2));
}

  static int
cmpClassCounts(const void *p1, const void *p2)
{
    t_class **cl1 = (t_class **) p1;
    t_class **cl2 = (t_class **) p2;
    int test = (*cl1)->instanceCount - (*cl2)->instanceCount;
    if (test)
        return(test);
    else
        return(cmpClassNames(p1, p2));
}

  static int
cmpClassCountsReverse(const void *p1, const void *p2)
{
    t_class **cl1 = (t_class **) p1;
    t_class **cl2 = (t_class **) p2;
    int test = (*cl2)->instanceCount - (*cl1)->instanceCount;
    if (test)
        return(test);
    else
        return(cmpClassNames(p1, p2));
}

  static int
countInstances(t_class *theClass, t_dump *dump)
{
    int count = 0;

    if (theClass) {
        int i;

        for (i=0; i<dump->objectCount; ++i) {
            t_object *refObject = dump->sortedObjects[i];
            if (refObject->theClass == theClass) {
                ++count;
            }
        }
    }
    return(count);
}

  static void
doReferenceRecursion(t_object *object, t_dump *dump, int level)
{
    int i;
    t_referencer *ref = object->referencers;

    while (ref) {
        t_object *refObject = ref->object;
        for (i=0; i<level; ++i)
            aprintf("  ");
        aprintf("  %s%08x %s\n", isMarked(refObject, MARK_MARK) ? "*" : "",
                refObject->address, pClassname(refObject, dump));
        if (!isMarked(refObject, MARK_MARK)) {
            markObject(refObject, MARK_MARK);
            doReferenceRecursion(refObject, dump, level + 1);
        }
        ref = ref->next;
    }
}

  static t_class *
getClass(char *name, t_dump *dump)
{
    t_class *theClass = lookupClass(name, dump);

    if (!theClass)
        fprintf(stderr, "don't know any class '%s'\n", name);
    return(theClass);
}

  static t_object *
getObject(char *addressString, t_dump *dump)
{
    longword address;

    if (sscanf(addressString, "%x", &address) != 1) {
        fprintf(stderr, "'%s' doesn't look like an address to me\n",
               addressString);
        return(NULL);
    } else {
        t_object *object = lookupObject(address, dump);
        if (!object)
            fprintf(stderr, "don't know any object at %08x\n", address);
        return(object);
    }
}

  static void
markUniquely(t_object *object, t_dump *dump)
{
    int i;

    unmarkAllObjects(dump, MARK_MARK | MARK_AUX);
    for (i=0; i<dump->objectCount; ++i) {
        t_object *target = dump->sortedObjects[i];
        if (target == object) {
            markObjectAndChildren(target, MARK_MARK, NULL);
        } else if (isRoot(target)) {
            markObjectAndChildren(target, MARK_AUX, object);
        }
    }
}

  static void
printClassReachabilityInfo(t_object *object, t_dump *dump)
{
    int i;
    int classReachCount, classReachSize;
    int classUniqueCount, classUniqueSize;

    markUniquely(object, dump);
    classReachCount = classUniqueCount = 0;
    classReachSize = classUniqueSize = 0;
    for (i=0; i<dump->classCount; ++i) {
        t_class *target = dump->sortedClasses[i];
        if (isMarkedClass(target, MARK_MARK)) {
            ++classReachCount;
            classReachSize += classOverhead(target);
            if (!isMarkedClass(target, MARK_AUX)) {
                ++classUniqueCount;
                classUniqueSize += classOverhead(target);
            }
        }
    }
    aprintf("%08x %6d %8d %6d %8d %s\n", object->address,
            classUniqueCount, classUniqueSize,
            classReachCount - classUniqueCount,
            classReachSize - classUniqueSize,
            pClassname(object, dump));
    for (i=0; i<dump->classCount; ++i) {
        t_class *target = dump->sortedClasses[i];
        if (isMarkedClass(target, MARK_MARK) &&
                !isMarkedClass(target, MARK_AUX)) {
            aprintf(" %6d %s\n", classOverhead(target), target->name);
        }
    }
}

  static int
printClassStruct(t_class *aClass, bool expanded, bool printColumnHeaders,
                 t_dump *dump)
{
    int overhead = classOverhead(aClass);
    char *tag;

    if (aClass->constantPoolMalloc) {
        tag = wsTag(dump->wsCurrent, aClass->constantPoolMalloc->address,
                    aClass->constantPoolMalloc->size);
    } else {
        tag = " ";
    }
    if (printColumnHeaders) {
        aprintf("  address isize clsize classname\n");
        aprintf("--------- ----- ------ ---------\n");
    }
    if (aClass->size != -1)
        aprintf("%s%08x %5d %6d %s", tag, aClass->address, aClass->size,
                overhead, aClass->name);
    else
        aprintf("%s%08x ----- %6d %s", tag, aClass->address, overhead,
                aClass->name);
    
    if (aClass->superclass)
        aprintf(" extends %s", aClass->superclass->name);
    aprintf("\n");
    if (expanded) {
        int i;
        if (aClass->constantPoolMalloc) {
            aprintf("  constantpool at %08x (%d bytes)\n",
                    aClass->constantPoolMalloc->address,
                    aClass->constantPoolMalloc->size);
        } else {
            aprintf("  constantpool at %08x (not in malloc heap)\n",
                    aClass->constantPoolAddress);
        }
        if (aClass->methodTableMalloc) {
            aprintf("  methodtable at %08x (%d bytes)\n",
                    aClass->methodTableMalloc->address,
                    aClass->methodTableMalloc->size);
        } else {
            aprintf("  methodTable at %08x (not in malloc heap)\n",
                    aClass->methodTableAddress);
        }
        for (i=0; i<aClass->fieldCount; ++i) {
            t_fieldDescriptor *field = aClass->fields[i];
            aprintf("  %s%s %s;\n",
                   field->scopeCode == SCOPE_CLASS ? "static " : "",
                   field->type,
                   field->name);
        }
    }
    return(overhead);
}

  static void
printObjectStruct(t_object *object, bool expanded, t_dump *dump)
{
    if (isArray(object)) {
        if (isCharArray(object)) {
            t_charArray *charArray = (t_charArray *)object;
            aprintf("%08x %s [C (%d) [%d]\n", charArray->address,
                   pMarks(charArray->flags), charArray->size,
                   charArray->dimension);
            if (expanded)
                aprintf("  chars: \"%s\"\n", charArray->stringValue);
        } else {
            t_array *array = (t_array *)object;
            int i;
            aprintf("%08x %s %s (%d) [%d]\n", array->address,
                   pMarks(array->flags), pClassname(object, dump),
                   array->size, array->dimension);
            if (expanded && isObjectArray(object))
                for (i=0; i<array->dimension; ++i)
                    aprintf("  %d: %s\n", i, pAddress(array->fields.o[i]));
        }
    } else {
        aprintf("%08x %s %s (%d)\n", object->address, pMarks(object->flags),
               pClassname(object, dump), object->theClass->size);
        if (expanded) {
            int i, j;
            char scopeTarget;
            t_class *theClass;

            if (isClassClass(object->theClass)) {
                scopeTarget = SCOPE_CLASS;
                theClass = lookupClassByAddress(object->address, dump);
                if (theClass == NULL) {
                    fprintf(stderr, "can't find class for class object!\n");
                    return;
                }
            } else {
                scopeTarget = SCOPE_INSTANCE;
                theClass = object->theClass;
            }
            j = 0;
            while (theClass) {
                for (i=0; i<theClass->fieldCount; ++i) {
                    if (theClass->fields[i]->scopeCode == scopeTarget) {
                        switch (object->typeCodes[j]) {
                            case TYPE_REFERENCE:
                                aprintf("  %s: %s\n",
                                        theClass->fields[i]->name,
                                        pAddress(object->fields.o[j]));
                                break;
                            case TYPE_INVALID:
                                aprintf("  %s: %s (invalid)\n",
                                        theClass->fields[i]->name,
                                        pAddress(object->fields.o[j]));
                                break;
                            case TYPE_SCALAR:
                                aprintf("  %s: %d\n",
                                        theClass->fields[i]->name,
                                        object->fields.v[j]);
                                break;
                            default:
                                aprintf("  %s: bad type code %d\n",
                                        theClass->fields[i]->name,
                                        object->typeCodes[j]);
                                break;
                        }
                        ++j;
                    }
                }
                theClass = theClass->superclass;
            }
        }
    }
}

  static void
printReachabilityHeader()
{
    aprintf("address  ucount   ubytes ocount   obytes class\n");
    aprintf("-------  ------   ------ ------   ------ -----\n");
}

  static void
printReachabilityInfo(t_object *object, t_dump *dump)
{
    int i;
    int reachCount, reachSize;
    int uniqueCount, uniqueSize;

    markUniquely(object, dump);

    reachCount = uniqueCount = 0;
    reachSize = uniqueSize = 0;
    for (i=0; i<dump->objectCount; ++i) {
        t_object *target = dump->sortedObjects[i];
        if (isMarked(target, MARK_MARK)) {
            ++reachCount;
            reachSize += objectSize(target);
            if (!isMarked(target, MARK_AUX)) {
                ++uniqueCount;
                uniqueSize += objectSize(target);
            }
        }
    }
    aprintf("%08x %6d %8d %6d %8d %s\n", object->address,
            uniqueCount, uniqueSize,
            reachCount - uniqueCount, reachSize - uniqueSize,
            pClassname(object, dump));
}

  void
printClass(char *name, t_dump *dump)
{
    t_class *theClass = getClass(name, dump);

    if (theClass) {
        printClassStruct(theClass, TRUE, TRUE, dump);
    }
}

  void
printClassReachability(char *addressString, t_dump *dump)
{
    t_object *object = getObject(addressString, dump);
    if (object) {
        printReachabilityHeader();
        printClassReachabilityInfo(object, dump);
    }
}

  void
printClassTable(t_dump *dump)
{
    int i;
    longword totalOverhead = 0;
    qsort(dump->sortedClasses, dump->classCount, sizeof(t_class *),
          cmpClassNames);
    aprintf("%d classes:\n", dump->classCount);
    for (i=0; i<dump->classCount; ++i) {
        totalOverhead += printClassStruct(dump->sortedClasses[i], FALSE, i==0,
                                          dump);
    }
    aprintf("--------- ----- ------ ---------\n");
    aprintf("               %7d total class overhead\n", totalOverhead);
}

  void
printGlobalReachability(t_dump *dump)
{
    int i;

    printReachabilityHeader();
    for (i=0; i<dump->objectCount; ++i)
        printReachabilityInfo(dump->sortedObjects[i], dump);
}

  void
printInstanceCount(char *name, t_dump *dump)
{
    t_class *theClass = getClass(name, dump);
    if (theClass) {
        aprintf("%d instances of ", countInstances(theClass, dump));
        printClassStruct(theClass, FALSE, FALSE, dump);
    }
}

  void
printInstances(char *name, t_dump *dump)
{
    t_class *theClass = getClass(name, dump);
    if (theClass) {
        int i;

        aprintf("%d instances of ", countInstances(theClass, dump));
        printClassStruct(theClass, FALSE, FALSE, dump);
        for (i=0; i<dump->objectCount; ++i) {
            t_object *refObject = dump->sortedObjects[i];
            if (refObject->theClass == theClass) {
                if (isArray(refObject)) {
                    t_array *refArray = (t_array *) refObject;
                    aprintf("  %08x [%d] (%d)\n", refArray->address,
                           refArray->dimension, refArray->size);
                } else {
                    aprintf("  %08x\n", refObject->address);
                }
            }
        }
    }
}

  void
printObject(char *addressString, t_dump *dump)
{
    t_object *object = getObject(addressString, dump);
    if (object)
        printObjectStruct(object, TRUE, dump);
}

  void
printObjectTable(t_dump *dump)
{
    int i;

    aprintf("%d objects:\n", dump->objectCount);
    for (i=0; i<dump->objectCount; ++i) {
        aprintf(" %c", isRoot(dump->sortedObjects[i]) ? '*' : ' ');
        printObjectStruct(dump->sortedObjects[i], FALSE, dump);
    }
}

  void
printReachability(char *addressString, t_dump *dump)
{
    t_object *object = getObject(addressString, dump);
    if (object) {
        printReachabilityHeader();
        printReachabilityInfo(object, dump);
    }
}

  void
printReachableObjects(char *addressString, t_dump *dump)
{
    t_object *object = getObject(addressString, dump);
    if (object) {
        int i;

        markUniquely(object, dump);

        aprintf("objects uniquely reachable from %08x (%s):\n",
                object->address, pClassname(object, dump));
        aprintf("address  size class\n");
        aprintf("-------- ---- -----\n");
        for (i=0; i<dump->objectCount; ++i) {
            t_object *target = dump->sortedObjects[i];
            if (isMarked(target, MARK_MARK) && !isMarked(target, MARK_AUX)) {
                aprintf("%08x %4d %s\n", target->address, objectSize(target),
                        pClassname(target, dump));
            }
        }
    }
}

  void
printRecursiveReferences(char *addressString, t_dump *dump)
{
    t_object *object = getObject(addressString, dump);
    unmarkAllObjects(dump, MARK_MARK);
    if (object) {
        aprintf("recursive references to ");
        markObject(object, MARK_MARK);
        printObjectStruct(object, FALSE, dump);
        doReferenceRecursion(object, dump, 0);
    }
}

  void
printReferences(char *addressString, t_dump *dump)
{
    int i;

    t_object *object = getObject(addressString, dump);
    if (object) {
        t_referencer *ref = object->referencers;
        aprintf("references to ");
        printObjectStruct(object, FALSE, dump);
        while (ref) {
            t_object *refObject = ref->object;
            aprintf("  %08x %s\n", refObject->address,
                    pClassname(refObject, dump));
            ref = ref->next;
        }
    }
}

  void
printSizeInfo(char *name, t_dump *dump)
{
    t_class *theClass = getClass(name, dump);
    if (theClass) {
        aprintf("  %s: %d instances, %d bytes\n",
               theClass->name,
               theClass->instanceCount,
               theClass->totalInstanceSize);
    }
}

  void
printSizeSummary(char *sortby, t_dump *dump)
{
    int i;
    int totalCount = 0;
    int totalSize = 0;
    bool printCumulative = TRUE;
    int (*cmpFunc)(const void *p1, const void *p2);

    if (strcmp(sortby, "size") == 0) {
        cmpFunc = cmpClassBytes;
    } else if (strcmp(sortby, "rsize") == 0) {
        cmpFunc = cmpClassBytesReverse;
    } else if (strcmp(sortby, "count") == 0) {
        cmpFunc = cmpClassCounts;
    } else if (strcmp(sortby, "rcount") == 0) {
        cmpFunc = cmpClassCountsReverse;
    } else if (strcmp(sortby, "name") == 0 || sortby[0] == '\0') {
        printCumulative = FALSE;
        cmpFunc = cmpClassNames;
    } else {
        fprintf(stderr, "valid sort keys are 'size', 'rsize', 'count', 'rcount' or 'name'\n");
        return;
    }
    qsort(dump->sortedClasses, dump->classCount, sizeof(t_class *), cmpFunc);

    if (printCumulative) {
        aprintf("count cumct    bytes cumbytes  cumpct class\n");
        aprintf("----- ----- -------- -------- ------- ----------\n");
    } else {
        aprintf("count    bytes class\n");
        aprintf("----- -------- ----------\n");
    }
    for (i=0; i<dump->classCount; ++i) {
        t_class *theClass = dump->sortedClasses[i];
        totalCount += theClass->instanceCount;
        totalSize += theClass->totalInstanceSize;
        if (printCumulative) {
            aprintf("%5d %5d %8d %8d %6.2f%% %s\n", theClass->instanceCount,
                    totalCount, theClass->totalInstanceSize, totalSize,
                    (((double)totalSize)/((double)dump->byteCount)) * 100.0,
                    theClass->name);
        } else {
            aprintf("%5d %8d %s\n", theClass->instanceCount,
                    theClass->totalInstanceSize, theClass->name);
        }
    }
    if (printCumulative) {
        aprintf("----- ----- -------- -------- ------- -------\n");
        aprintf("%5d %5d %8d %8d 100.00%% total\n", totalCount, totalCount,
                totalSize, totalSize);
    } else {
        aprintf("----- -------- ----------\n");
        aprintf("%5d %8d total\n", totalCount, totalSize);
    }
}
