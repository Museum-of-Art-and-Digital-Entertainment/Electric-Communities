/*
  adtables.c -- Class and object table handling for the Analyze Dump program.

  Chip Morningstar
  Electric Communities
  3-January-1998

  Copyright 1998 Electric Communities, all rights reserved worldwide.

*/

#include "analdump.h"
#include <string.h>
#include <ctype.h>

  static int
mallocCallerCmp(t_mallocCaller *m1, t_mallocCaller *m2)
{
    if (m1->filename[0] == '*' && m2->filename[0] == '*') {
        return(m1->line - m2->line);
    } else {
        int test = strcmp(m1->filename, m2->filename);
        if (test)
            return(test);
        else
            return(m1->line - m2->line);
    }
}

  static int
cmpHeaps(const void *p1, const void *p2)
{
    t_heap **h1 = (t_heap **) p1;
    t_heap **h2 = (t_heap **) p2;
    return((int)(*h1)->address - (int)(*h2)->address);
}

  static int
cmpObjects(const void *p1, const void *p2)
{
    t_object **o1 = (t_object **) p1;
    t_object **o2 = (t_object **) p2;
    return((int)(*o1)->address - (int)(*o2)->address);
}

  static longword
hashAddress(longword address)
{
    return((address >> 3) & HASH_TABLE_MASK);
}

  static longword
hashString(char *s)
{
    longword result = 0;
    longword subResult = 0;
    int bitCount = 32;

    while (*s) {
        subResult = (subResult << 1) + tolower(*s++);
        if (!--bitCount) {
            result ^= subResult;
            subResult = 0;
            bitCount = 32;
        }
    }
    result ^= subResult;
    return(result & HASH_TABLE_MASK);
}

  static longword
hashMallocCaller(t_mallocCaller *caller)
{
    return((hashString(caller->filename) + caller->line) & HASH_TABLE_MASK);
}

  static t_class *
newClass(char *name, int size, t_dump *dump)
{
    t_class *theClass;
    
    theClass = TypeAlloc(t_class);
    theClass->name = STRDUP(name);
    theClass->size = size;
    theClass->instanceCount = 0;
    theClass->totalInstanceSize = 0;
    theClass->flags = 0;
    addClass(theClass, dump);
    return(theClass);
}

  static t_mallocCaller *
newMallocCaller(char *filename, int line, char *funcname, t_dump *dump)
{
    t_mallocCaller *caller;

    caller = TypeAlloc(t_mallocCaller);
    caller->filename = STRDUP(filename);
    caller->line = line;
    if (funcname)
        caller->funcname = STRDUP(funcname);
    else
        caller->funcname = NULL;
    caller->mapFileInfo = NULL;
    caller->mallocs = NULL;
    caller->count = 0;
    caller->bytes = 0;
    addMallocCaller(caller, dump);
    return(caller);
}

  void
addClass(t_class *theClass, t_dump *dump)
{
    int test;
    longword hashValue;
    t_class *result;
    t_class *oldResult;
    
    hashValue = hashString(theClass->name);
    result = dump->classTable[hashValue];
    dump->classCount++;
    if (result == NULL) {
        dump->classTable[hashValue] = theClass;
        theClass->next = NULL;
    } else {
        oldResult = NULL;
        while (result) {
            test = strcmp(theClass->name, result->name);
            if (test == 0) {
                fprintf(stderr, "class %s multiply defined\n", theClass->name);
                dump->classCount--;
                return;
            } else if (test > 0) {
                theClass->next = result;
                if (oldResult == NULL)
                    dump->classTable[hashValue] = theClass;
                else
                    oldResult->next = theClass;
                return;
            } else {
                oldResult = result;
                result = result->next;
            }
        }
        if (oldResult)
            oldResult->next = theClass;
        else
            dump->classTable[hashValue] = theClass;
        theClass->next = NULL;
    }
}

  void
addMallocCaller(t_mallocCaller *caller, t_dump *dump)
{
    int test;
    longword hashValue;
    t_mallocCaller *result;
    t_mallocCaller *oldResult;
    
    hashValue = hashMallocCaller(caller);
    result = dump->mallocTable[hashValue];
    dump->mallocCallerCount++;
    if (result == NULL) {
        dump->mallocTable[hashValue] = caller;
        caller->next = NULL;
    } else {
        oldResult = NULL;
        while (result) {
            test = mallocCallerCmp(caller, result);
            if (test == 0) {
                fprintf(stderr, "malloc caller %s/%d multiply defined\n",
                        caller->filename, caller->line);
                dump->mallocCallerCount--;
                return;
            } else if (test > 0) {
                caller->next = result;
                if (oldResult == NULL)
                    dump->mallocTable[hashValue] = caller;
                else
                    oldResult->next = caller;
                return;
            } else {
                oldResult = result;
                result = result->next;
            }
        }
        if (oldResult)
            oldResult->next = caller;
        else
            dump->mallocTable[hashValue] = caller;
        caller->next = NULL;
    }
}

  t_dump *
addObject(t_object *object, t_dump *dump)
{
    longword hashValue;
    t_object *result;
    t_object *oldResult;
    
    hashValue = hashAddress(object->address);
    result = dump->objectTable[hashValue];
    dump->objectCount++;
    if (result == NULL) {
        dump->objectTable[hashValue] = object;
        object->next = NULL;
    } else {
        oldResult = NULL;
        while (result) {
            if (object->address == result->address) {
                fprintf(stderr, "object %x multiply defined\n",
                        object->address);
                dump->objectCount--;
                return(NULL);
            } else if (object->address > result->address) {
                object->next = result;
                if (oldResult == NULL)
                    dump->objectTable[hashValue] = object;
                else
                    oldResult->next = object;
                return(dump);
            } else {
                oldResult = result;
                result = result->next;
            }
        }
        if (oldResult)
            oldResult->next = object;
        else
            dump->objectTable[hashValue] = object;
        object->next = NULL;
    }
    return(dump);
}

  int
cmpClassNames(const void *p1, const void *p2)
{
    t_class **cl1 = (t_class **) p1;
    t_class **cl2 = (t_class **) p2;
    char *a1 = strchr((*cl1)->name, '[');
    char *a2 = strchr((*cl2)->name, '[');
    if (a1 && !a2)
        return(-1);
    else if (!a1 && a2)
        return(1);
    else
        return(strcmp((*cl1)->name, (*cl2)->name));
}

  int
cmpMallocCallers(const void *p1, const void *p2)
{
    t_mallocCaller **m1 = (t_mallocCaller **)p1;
    t_mallocCaller **m2 = (t_mallocCaller **)p2;
    return(mallocCallerCmp(*m1, *m2));
}

  void
computeBackReferences(t_dump *dump)
{
    int i;

    for (i=0; i<dump->objectCount; ++i) {
        int j;
        t_object *object = dump->sortedObjects[i];
        if (isArray(object) && !isObjectArray(object))
            continue;
        for (j=0; j<object->fieldCount; ++j) {
            if (object->typeCodes[j] == TYPE_REFERENCE) {
                t_object *refObject = object->fields.o[j];
                if (refObject) {
                    t_referencer *ref = TypeAlloc(t_referencer);
                    ref->object = object;
                    ref->next = refObject->referencers;
                    refObject->referencers = ref;
                }
            }
        }
    }
    for (i=0; i<dump->objectCount; ++i) {
        t_object *object = dump->sortedObjects[i];
        t_referencer *prev = NULL;
        t_referencer *chaser = object->referencers;
        while (chaser) {
            t_referencer *temp = chaser->next;
            chaser->next = prev;
            prev = chaser;
            chaser = temp;
        }
        object->referencers = prev;
    }
}

  void
computeDLLZones(t_dump *dump)
{
    t_dll *dll;

    if (!dump->vmap)
        return;
    dll = dump->dll;
    while (dll) {
        t_vmap *vmap = dump->vmap;
        dll->vmap = NULL;
        while (vmap) {
            if (vmap->address <= dll->address &&
                    dll->address < vmap->address + vmap->size) {
                dll->vmap = vmap;
                break;
            }
            vmap = vmap->next;
        }
        dll = dll->next;
    }
}

  void
computeHeapZones(t_dump *dump)
{
    int i;
    t_heap *heaps = NULL;
    int heapCount = 0;
    int overhead = HeapOverhead + StatOverhead;

    dump->minAddress = 0x7FFFFFFF;
    dump->maxAddress = 0;
    if (!dump->vmap) {
        aprintf("no VM info available in dump; can't compute heap zones\n");
        return;
    }
    for (i=0; i<dump->mallocCallerCount; ++i) {
        t_malloc *aMalloc = dump->sortedMallocs[i]->mallocs;
        while (aMalloc) {
            t_heap *heap = heaps;
            while (heap) {
                if (heap->address <= aMalloc->address &&
                        aMalloc->address < heap->address + heap->size)
                    break;
                heap = heap->next;
            }
            if (!heap) {
                t_vmap *vmap = dump->vmap;
                while (vmap) {
                    if (vmap->address <= aMalloc->address &&
                            aMalloc->address < vmap->address + vmap->size)
                        break;
                    vmap = vmap->next;
                }
                if (vmap) {
                    heap = TypeAlloc(t_heap);
                    heap->address = vmap->address;
                    heap->size = vmap->size;
                    heap->blocks = 0;
                    heap->usedBytes = 0;
                    heap->freeBytes = vmap->size;
                    heap->vmap = vmap;
                    heap->next = heaps;
                    heaps = heap;
                    ++heapCount;
                    if (vmap->address < dump->minAddress)
                        dump->minAddress = vmap->address;
                    if (vmap->address + vmap->size > dump->maxAddress)
                        dump->maxAddress = vmap->address + vmap->size;
                } else {
                    fprintf(stderr, "no VM segment for malloc block at %x\n",
                            aMalloc->address);
                    aMalloc = aMalloc->next;
                    continue;
                }
            }
            heap->blocks++;
            heap->usedBytes += aMalloc->size + overhead;
            heap->freeBytes -= aMalloc->size + overhead;
            aMalloc = aMalloc->next;
        }
    }
    dump->heapZoneCount = heapCount;
    dump->heapZones = TypeAllocMulti(t_heap *, heapCount);
    for (i=0; i<heapCount; ++i) {
        dump->heapZones[i] = heaps;
        heaps = heaps->next;
    }
    qsort(dump->heapZones, heapCount, sizeof(t_heap *), cmpHeaps);
}

  void
convertReferenceAddresses(t_dump *dump)
{
    int i;

    for (i=0; i<dump->objectCount; ++i) {
        int j;
        t_object *refObject = dump->sortedObjects[i];
        if (isArray(refObject) && !isObjectArray(refObject))
            continue;
        for (j=0; j<refObject->fieldCount; ++j) {
            if (refObject->typeCodes[j] == TYPE_REFERENCE) {
                refObject->fields.o[j] =
                    lookupObject(refObject->fields.v[j], dump);
            }
        }
    }
}

  void
convertSuperclassAddresses(t_dump *dump)
{
    int i;

    for (i=0; i<dump->classCount; ++i) {
        t_class *theClass = dump->sortedClasses[i];
        theClass->superclass =
            lookupClassByAddress(theClass->superAddress, dump);
    }
}

  t_class *
handleClassReference(char *className, int size, t_dump *dump)
{
    t_class *theClass = lookupClass(className, dump);
    if (theClass) {
        if (theClass->size == size || size == -1 || theClass->size == -1) {
            if (theClass->size == -1)
                theClass->size = size;
            return(theClass);
        } else {
            return(NULL);
        }
    } else {
        return(newClass(className, size, dump));
    }
}

  t_mallocCaller *
handleMallocCaller(char *filename, int line, char *funcname, t_dump *dump)
{
    t_mallocCaller *caller = lookupMallocCaller(filename, line, dump);
    if (caller)
        return(caller);
    else
        return(newMallocCaller(filename, line, funcname, dump));
}

  bool
isArray(t_object *object)
{
    return(object->flags & OBJ_ARRAY);
}

  bool
isClassClass(t_class *aClass)
{
    return(!strcmp(aClass->name, "java.lang.Class"));
}

  bool
isCharArray(t_object *object)
{
    return(object->flags & OBJ_CHAR_ARRAY);
}

  bool
isMarked(t_object *object, int mark)
{
    return(object->flags & mark);
}

  bool
isMarkedClass(t_class *theClass, int mark)
{
    return(theClass->flags & mark);
}

  bool
isObjectArray(t_object *object)
{
    return(object->flags & OBJ_OBJ_ARRAY);
}

  bool
isRoot(t_object *object)
{
    return(isMarked(object, ROOT_MASK));
}

  t_class *
lookupClass(char *className, t_dump *dump)
{
    t_class *result;
    int test;
    longword hashValue;
    
    hashValue = hashString(className);
    result = dump->classTable[hashValue];
    while (result) {
        test = strcmp(className, result->name);
        if (test == 0)
            break;
        else if (test > 0) {
            result = NULL;
            break;
        } else {
            result = result->next;
        }
    }
    return(result);
}

  t_class *
lookupClassByAddress(longword address, t_dump *dump)
{
    int i;

    if (address == 0)
        return(NULL);
    for (i=0; i<dump->classCount; ++i)
        if (dump->sortedClasses[i]->address == address)
            return(dump->sortedClasses[i]);
    return(NULL);
}

  t_mallocCaller *
lookupMallocCaller(char *filename, int line, t_dump *dump)
{
    t_mallocCaller *result;
    int test;
    longword hashValue;
    t_mallocCaller tester;

    tester.filename = filename;
    tester.line = line;
    hashValue = hashMallocCaller(&tester);
    result = dump->mallocTable[hashValue];
    while (result) {
        test = mallocCallerCmp(&tester, result);
        if (test == 0)
            break;
        else if (test > 0) {
            result = NULL;
            break;
        } else {
            result = result->next;
        }
    }
    return(result);
}

  t_object *
lookupObject(longword address, t_dump *dump)
{
    t_object *result = dump->objectTable[hashAddress(address)];
    while (result) {
        if (address == result->address)
            break;
        else if (address > result->address) {
            result = NULL;
            break;
        } else {
            result = result->next;
        }
    }
    return(result);
}

  void
markInternalReferences(t_dump *dump)
{
    int i;

    for (i=0; i<dump->objectCount; ++i) {
        int j;
        t_object *refObject = dump->sortedObjects[i];
        if (isArray(refObject) && !isObjectArray(refObject))
            continue;
        for (j=0; j<refObject->fieldCount; ++j) {
            if (refObject->typeCodes[j] == TYPE_REFERENCE) {
                t_object *object = refObject->fields.o[j];
                if (object && object != refObject)
                    object->flags |= MARK_HEAP;
            }
        }
    }
}

  void
markObject(t_object *object, int mark)
{
    object->flags |= mark;
    object->theClass->flags |= mark;
}

  void
markObjectAndChildren(t_object *object, int mark, t_object *stopObject)
{
    if (object && object != stopObject && !isMarked(object, mark)) {
        markObject(object, mark);
        if (!isArray(object) || isObjectArray(object)) {
            int j;
            for (j=0; j<object->fieldCount; ++j) {
                if (object->typeCodes[j] == TYPE_REFERENCE) {
                    markObjectAndChildren(object->fields.o[j], mark,
                                          stopObject);
                }
            }
        }
    }
}

  void
noteRoots(t_dump *dump, t_root *roots)
{
    while (roots) {
        t_root *root = roots;
        t_object *object = lookupObject(root->address, dump);
        if (object)
            object->flags |= root->rootType;
        else if (root->rootType != ROOT_CSTACK)
            fprintf(stderr, "warning: missing object %08x declared as root\n",
                    root->address);
        roots = roots->next;
        FREE(root);
    }
}

  int
objectSize(t_object *object)
{
    if (isArray(object))
        return(((t_array *)object)->size);
    else
        return(object->theClass->size);
}

  t_dump *
sortDump(t_dump *dump)
{
    int i, j;

    dump->sortedClasses = TypeAllocMulti(t_class *, dump->classCount);
    j = 0;
    for (i=0; i<HASH_TABLE_SIZE; ++i) {
        t_class *aClass = dump->classTable[i];
        while (aClass) {
            dump->sortedClasses[j++] = aClass;
            aClass = aClass->next;
        }
    }
    qsort(dump->sortedClasses, dump->classCount, sizeof(t_class *),
          cmpClassNames);

    dump->sortedObjects = TypeAllocMulti(t_object *, dump->objectCount);
    j = 0;
    for (i=0; i<HASH_TABLE_SIZE; ++i) {
        t_object *object = dump->objectTable[i];
        while (object) {
            dump->sortedObjects[j++] = object;
            object = object->next;
        }
    }
    qsort(dump->sortedObjects, dump->objectCount, sizeof(t_object *),
          cmpObjects);

    dump->sortedMallocs = TypeAllocMulti(t_mallocCaller *,
                                         dump->mallocCallerCount);
    j = 0;
    for (i=0; i<HASH_TABLE_SIZE; ++i) {
        t_mallocCaller *caller = dump->mallocTable[i];
        while (caller) {
            dump->sortedMallocs[j++] = caller;
            caller = caller->next;
        }
    }
    qsort(dump->sortedMallocs, dump->mallocCallerCount,
          sizeof(t_mallocCaller *), cmpMallocCallers);

    return(dump);
}

  void
unmarkAllObjects(t_dump *dump, int mark)
{
    int i;

    for (i=0; i<dump->objectCount; ++i)
        dump->sortedObjects[i]->flags &= ~mark;
    for (i=0; i<dump->classCount; ++i)
        dump->sortedClasses[i]->flags &= ~mark;
}
