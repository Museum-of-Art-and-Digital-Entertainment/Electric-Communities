/*
  adparse.c -- Dump file parsing for the Analyze Dump program.

  Chip Morningstar
  Electric Communities
  6-January-1998

  Copyright 1998 Electric Communities, all rights reserved worldwide.

*/

#include "analdump.h"
#include <ctype.h>

static t_dump *internalizeObject(char *descriptor, t_dump *dump);
static int internalizeFields(longword **fieldsptr, char **typesptr);
static t_dump *internalizeMalloc(char *descriptor, t_dump *dump);
static t_root *internalizeRoot(char *rootLine, t_root *roots);
static t_dump *internalizeSummary(char *summaryLine, t_dump *dump);
static t_dump *internalizeVMap(char *vmapLine, t_dump *dump);
static t_dump *internalizeVMStat(char *vmLine, t_dump *dump);
static t_dump *internalizeWsInfo(char *wsLine, t_dump *dump);
static bool testKeyword(char *buf, char *keyword);

static t_fieldList *availFields = NULL;

static bool DumpHasFuncnames = FALSE;
int HeapOverhead = 0;
int StatOverhead = 0;

  static t_fieldList *
allocField()
{
    t_fieldList *result;

    if (availFields) {
        result = availFields;
        availFields = availFields->next;
    } else {
        result = TypeAlloc(t_fieldList);
    }
    return(result);
}

  static t_dump *
createDump()
{
    int i;
    t_dump *result = TypeAlloc(t_dump);
    result->descriptiveHeader = "";
    result->classTable = TypeAllocMulti(t_class *, HASH_TABLE_SIZE);
    result->classCount = 0;
    result->sortedClasses = NULL;
    result->objectTable = TypeAllocMulti(t_object *, HASH_TABLE_SIZE);
    result->objectCount = 0;
    result->byteCount = 0;
    result->sortedObjects = NULL;
    result->mallocTable = TypeAllocMulti(t_mallocCaller *, HASH_TABLE_SIZE);
    result->mallocCallerCount = 0;
    result->mallocCount = 0;
    result->mallocBytes = 0;
    result->sortedMallocs = NULL;
    for (i=0; i<HASH_TABLE_SIZE; ++i) {
        result->classTable[i] = NULL;
        result->objectTable[i] = NULL;
        result->mallocTable[i] = NULL;
    }
    result->summaryInfo = NULL;
    result->summaryTail = NULL;
    result->wsInfo = NULL;
    result->wsTail = NULL;
    result->wsCurrent = NULL;
    result->wsCount = 0;
    result->vmStats = NULL;
    result->vmStatsTail = NULL;
    result->vmap = NULL;
    result->vmapTail = NULL;
    result->heapZones = NULL;
    result->heapZoneCount = 0;
    result->dll = NULL;
    result->dllTail = NULL;
    result->map = NULL;
    return(result);
}

  static char *
demangleClassname(char *mangled, bool expectPrefix)
{
    static char result[BUFLEN];
    char *baseType;
    char *resultptr;
    int arity = 0;

    while (*mangled == '[') {
        ++arity;
        ++mangled;
    }
    if (arity || expectPrefix) {
        switch (*mangled) {
            case 'Z': baseType = "boolean"; break;
            case 'B': baseType = "byte";    break;
            case 'C': baseType = "char";    break;
            case 'D': baseType = "double";  break;
            case 'F': baseType = "float";   break;
            case 'I': baseType = "int";     break;
            case 'J': baseType = "long";    break;
            case 'S': baseType = "short";   break;
            case 'L': baseType = ++mangled; break;
        }
    } else {
        baseType = mangled;
    }
    resultptr = result;
    while (*baseType != ';' && *baseType != '\0') {
        if (*baseType == '/') {
            *resultptr++ = '.';
            ++baseType;
        } else {
            *resultptr++ = *baseType++;
        }
    }
    while (arity--) {
        *resultptr++ = '[';
        *resultptr++ = ']';
    }
    *resultptr = '\0';
    return(result);
}

  static t_dump *
describeDump(char *descriptiveHeader, t_dump *dump)
{
    dump->descriptiveHeader = STRDUP(descriptiveHeader);
    return(dump);
}

  static void
freeField(t_fieldList *field)
{
    field->next = availFields;
    availFields = field;
}

  static t_dump *
internalizeArray(longword address, char *classname, int size, int dimension,
                 char *stringValue, t_dump *dump)
{
    t_class *theClass =
        handleClassReference(demangleClassname(classname, FALSE), -1, dump);

    if (theClass) {
        theClass->instanceCount++;
        theClass->totalInstanceSize += size;
        if (strcmp(classname, "[C")) {
            t_array *array = TypeAlloc(t_array);
            array->theClass = theClass;
            array->address = address;
            array->dimension = dimension;
            array->size = size;
            array->fieldCount = internalizeFields(&array->fields.v,
                                                  &array->typeCodes);
            array->referencers = NULL;
            array->flags = OBJ_ARRAY;
            if (classname[1] == 'L')
                array->flags |= OBJ_OBJ_ARRAY;
            if (array->fieldCount < 0)
                return(NULL);
            return(addObject((t_object *)array, dump));
        } else {
            t_charArray *charArray = TypeAlloc(t_charArray);
            charArray->theClass = theClass;
            charArray->address = address;
            charArray->dimension = dimension;
            charArray->size = size;
            charArray->fields = NULL;
            charArray->fieldCount = 0;
            charArray->referencers = NULL;
            charArray->flags = OBJ_ARRAY | OBJ_CHAR_ARRAY;
            charArray->typeCodes = NULL;
            charArray->stringValue = STRDUP(stringValue);
            return(addObject((t_object *)charArray, dump));
        }
    } else {
        return(NULL);
    }
}

  static t_dump *
internalizeClass(char *descriptor, t_dump *dump)
{
    longword address;
    longword superAddress;
    longword methodTableAddress;
    longword constantPoolAddress;
    char classname[BUFLEN];
    char buf[BUFLEN];
    int count = 0;
    int i;
    t_fieldDescriptor *fieldList = NULL;
    t_class *theClass;
    int hits;

    hits = sscanf(descriptor, "class %s %x %x %x %x", classname, &address,
                  &superAddress, &methodTableAddress, &constantPoolAddress);
    if (hits == 2) {
        superAddress = 0;
    } else if (hits == 3) {
        methodTableAddress = 0;
        constantPoolAddress = 0;
    } else if (hits != 5) {
        return(NULL);
    }

    theClass =
        handleClassReference(demangleClassname(classname, FALSE), -1, dump);
    if (theClass == NULL)
        return(NULL);

    while (getLine(buf)) {
        if (buf[0] == ' ') {
            t_fieldDescriptor *newField = TypeAlloc(t_fieldDescriptor);
            char nameBuf[BUFLEN];
            char typeBuf[BUFLEN];
            if (sscanf(buf, " %s %s %c", nameBuf, typeBuf,
                       &newField->scopeCode) != 3)
                return(NULL);
            if (newField->scopeCode != SCOPE_CLASS &&
                    newField->scopeCode != SCOPE_INSTANCE)
                return(NULL);
            newField->name = STRDUP(nameBuf);
            newField->type = STRDUP(demangleClassname(typeBuf, TRUE));
            newField->next = fieldList;
            fieldList = newField;
            ++count;
        } else {
            ungetLine(buf);
            break;
        }
    }
    theClass->address = address;
    theClass->superAddress = superAddress;
    theClass->methodTableAddress = methodTableAddress;
    theClass->methodTableMalloc = NULL;
    theClass->constantPoolAddress = constantPoolAddress;
    theClass->constantPoolMalloc = NULL;
    theClass->fieldCount = count;
    theClass->fields = TypeAllocMulti(t_fieldDescriptor *, count);
    i = count;
    while (i--) {
        theClass->fields[i] = fieldList;
        fieldList = fieldList->next;
    }
    return(dump);
}

  static t_dump *
internalizeDLL(char *dllLine, t_dump *dump)
{
    longword address;
    char dllName[BUFLEN];
    int hits;
    t_dll *dll;

    hits = sscanf(dllLine, "vmdll %x %s", &address, dllName);
    dll = TypeAlloc(t_dll);
    dll->address = address;
    dll->name = STRDUP(dllName);
    dll->next = NULL;

    if (dump->dllTail)
        dump->dllTail->next = dll;
    else
        dump->dll = dll;
    dump->dllTail = dll;
    return(dump);
}

  static t_dump *
internalizeDump(int dumpNumber)
{
    char buf[BUFLEN];
    t_dump *dump = createDump();
    t_root *roots = NULL;
    bool started = FALSE;
    int skipCount = dumpNumber - 1;

    printf("reading...\n");
    while (!started) {
        if (getLine(buf)) {
            if (skipCount > 0) {
                if (testKeyword(buf, DUMPEND_KEY))
                    --skipCount;
            } else {
                if (testKeyword(buf, DUMPSTART_KEY)) {
                    char *descptr = buf + strlen(DUMPSTART_KEY);
                    while (*descptr == ' ')
                        ++descptr;
                    dump = describeDump(descptr, dump);
                    started = TRUE;
                } else if (testKeyword(buf, ROOT_KEY)) {
                    roots = internalizeRoot(buf, roots);
                } else if (testKeyword(buf, WSFAIL_KEY)) {
                    dump = internalizeSummary(buf, dump);
                } else if (testKeyword(buf, WORKINGSET_KEY)) {
                    dump = internalizeWsInfo(buf, dump);
                }
            }
        } else {
            if (dumpNumber == 1)
                fprintf(stderr, "no dump in dump file\n");
            else
                fprintf(stderr, "no dump #%d in dump file\n", dumpNumber);
            return(NULL);
        }
    }

    while (dump && getLine(buf)) {
        if (testKeyword(buf, DUMPEND_KEY))
            break;
        else if (testKeyword(buf, ROOT_KEY))
            roots = internalizeRoot(buf, roots);
        else if (testKeyword(buf, VM_KEY))
            dump = internalizeVMStat(buf, dump);
        else if (testKeyword(buf, CLASS_KEY))
            dump = internalizeClass(buf, dump);
        else if (testKeyword(buf, MALLOC_KEY))
            dump = internalizeMalloc(buf, dump);
        else if (testKeyword(buf, SUMMARY_KEY))
            dump = internalizeSummary(buf, dump);
        else if (testKeyword(buf, WSFAIL_KEY))
            dump = internalizeSummary(buf, dump);
        else if (testKeyword(buf, WORKINGSET_KEY))
            dump = internalizeWsInfo(buf, dump);
        else if (testKeyword(buf, VMAP_KEY))
            dump = internalizeVMap(buf, dump);
        else if (testKeyword(buf, VMDLL_KEY))
            dump = internalizeDLL(buf, dump);
        else
            dump = internalizeObject(buf, dump);
    }
    if (dump) {
        dump = sortDump(dump);
        if (DumpHasFuncnames) {
            StatOverhead = 28; /* empirical */
            HeapOverhead = 8;  /* empirical */
        } else {
            StatOverhead = 32; /* empirical */
            HeapOverhead = 0;  /* empirical */
        }
        printf("converting addresses...\n");
        convertReferenceAddresses(dump);
        convertSuperclassAddresses(dump);
        printf("noting roots...\n");
        noteRoots(dump, roots);
        printf("computing backreferences...\n");
        computeBackReferences(dump);
        printf("marking internal references...\n");
        markInternalReferences(dump);
        printf("mapping malloc heap zones ..\n");
        computeHeapZones(dump);
        printf("mapping DLLs...\n");
        computeDLLZones(dump);
        printf("noting class allocation overhead...\n");
        findDefaultClassOverhead(dump);
    }
    return(dump);
}

  static int
internalizeFields(longword **fieldsptr, char **typesptr)
{
    char buf[BUFLEN];
    t_fieldList *fieldList = NULL;
    int count = 0;
    int i;

    while (getLine(buf)) {
        if (buf[0] == ' ') {
            t_fieldList *newField = allocField();
            if (sscanf(buf, " %c %x", &newField->typeCode,
                       &newField->address) != 2)
                return(-1);
            if (newField->typeCode != TYPE_REFERENCE &&
                    newField->typeCode != TYPE_INVALID &&
                    newField->typeCode != TYPE_SCALAR)
                return(-1);
            newField->next = fieldList;
            fieldList = newField;
            ++count;
        } else {
            ungetLine(buf);
            break;
        }
    }
    *fieldsptr = TypeAllocMulti(longword, count);
    *typesptr = TypeAllocMulti(char, count);
    i = count;
    while (i--) {
        t_fieldList *field = fieldList;
        fieldList = fieldList->next;
        (*fieldsptr)[i] = field->address;
        (*typesptr)[i] = field->typeCode;
        freeField(field);
    }
    return(count);
}

  static t_dump *
internalizeMalloc(char *descriptor, t_dump *dump)
{
    int hits;
    longword address;
    int size;
    char filename[BUFLEN];
    int line;
    char funcname[BUFLEN];
    t_mallocCaller *caller;
    t_malloc *aMalloc;

    hits = sscanf(descriptor, "malloc %x %d %s %d %s", &address, &size,
                  filename, &line, funcname);
    if (hits == 4) {
        caller = handleMallocCaller(filename, line, NULL, dump);
    } else if (hits == 5) {
        caller = handleMallocCaller(filename, line, funcname, dump);
        DumpHasFuncnames = TRUE;
    } else {
        return(NULL);
    }
    aMalloc = TypeAlloc(t_malloc);
    aMalloc->address = address;
    aMalloc->size = size;
    aMalloc->next = caller->mallocs;
    caller->mallocs = aMalloc;
    caller->count++;
    caller->bytes += size;
    dump->mallocCount++;
    dump->mallocBytes += size;
    return(dump);
}

  static t_dump *
internalizeNonArray(longword address, char *classname, int size, t_dump *dump)
{
    t_class *theClass =
        handleClassReference(demangleClassname(classname, FALSE), size, dump);

    if (theClass) {
        t_object *object = TypeAlloc(t_object);
        object->theClass = theClass;
        object->address = address;
        object->fieldCount = internalizeFields(&object->fields.v,
                                               &object->typeCodes);
        object->referencers = NULL;
        object->flags = OBJ_NORMAL;
        theClass->instanceCount++;
        theClass->totalInstanceSize += size;
        if (object->fieldCount < 0)
            return(NULL);
        return(addObject(object, dump));
    } else {
        return(NULL);
    }
}

  static t_dump *
internalizeObject(char *descriptor, t_dump *dump)
{
    longword address;
    char classname[BUFLEN];
    int size;
    int dimension;
    char stringValue[BUFLEN];
    int hits;
    bool isArray;

    hits = sscanf(descriptor, "%x %d %s %d \"%[^\n]",
                  &address, &size, classname, &dimension, stringValue);
    
    if (hits == 3) {
        if (classname[0] == '[')
            dump = NULL;
        else
            isArray = FALSE;
    } else if (hits == 4) {
        if (classname[0] != '[') {
            dump = NULL;
        } else {
            isArray = TRUE;
            stringValue[0] = '\0';
        }
    } else if (hits == 5) {
        if (strcmp(classname, "[C"))
            dump = NULL;
        else
            isArray = TRUE;
    } else {
        dump = NULL;
    }
    if (dump) {
        if (isArray)
            dump = internalizeArray(address, classname, size, dimension,
                                    stringValue, dump);
        else
            dump = internalizeNonArray(address, classname, size, dump);
    }
    if (!dump)
        fprintf(stderr, "bad dump file\n");
    dump->byteCount += size;
    return(dump);
}

  static t_root *
internalizeRoot(char *rootLine, t_root *roots)
{
    longword address;
    char rootCode;
    int rootType;

    if (sscanf(rootLine, "root %x %c", &address, &rootCode) != 2)
        return(roots);
    switch (rootCode) {
        case ROOT_CODE_STACK:  rootType = ROOT_STACK;  break;
        case ROOT_CODE_CSTACK: rootType = ROOT_CSTACK; break;
        case ROOT_CODE_GLOBAL: rootType = ROOT_GLOBAL; break;
        case ROOT_CODE_STATIC: rootType = ROOT_STATIC; break;
        case ROOT_CODE_INTERN: rootType = ROOT_INTERN; break;
        default:               rootType = 0;
    }
    if (rootType) {
        t_root *result = TypeAlloc(t_root);
        result->address = address;
        result->rootType = rootType;
        result->next = roots;
        return(result);
    } else {
        return(roots);
    }
}

  static t_dump *
internalizeSummary(char *summaryLine, t_dump *dump)
{
    t_summary *summ = TypeAlloc(t_summary);
    while (!isspace(*summaryLine))
        ++summaryLine;
    while (isspace(*summaryLine))
        ++summaryLine;
    summ->info = STRDUP(summaryLine);
    summ->next = NULL;
    if (dump->summaryTail)
        dump->summaryTail->next = summ;
    else
        dump->summaryInfo = summ;
    dump->summaryTail = summ;
    return(dump);
}

  static int
decodeProtCode(char *code)
{
    if (strcmp(code, "r") == 0)
        return(VM_PROT_READ);
    else if (strcmp(code, "rw") == 0)
        return(VM_PROT_READWRITE);
    else if (strcmp(code, "wc") == 0)
        return(VM_PROT_WRITECOPY);
    else if (strcmp(code, "x") == 0)
        return(VM_PROT_EXECUTE);
    else if (strcmp(code, "xr") == 0)
        return(VM_PROT_EXECUTEREAD);
    else if (strcmp(code, "xrw") == 0)
        return(VM_PROT_EXECUTEREADWRITE);
    else if (strcmp(code, "xwc") == 0)
        return(VM_PROT_EXECUTEWRITECOPY);
    else if (strcmp(code, "g") == 0)
        return(VM_PROT_GUARD);
    else if (strcmp(code, "na") == 0)
        return(VM_PROT_NOACCESS);
    else if (strcmp(code, "nc") == 0)
        return(VM_PROT_NOCACHE);
    else if (strcmp(code, "?") == 0)
        return(VM_PROT_UNKNOWNPROT);
    else
        return(VM_PROT_UNKNOWN);
}

  static t_dump *
internalizeVMap(char *vmapLine, t_dump *dump)
{
    longword address;
    int size;
    char allocProt[BUFLEN];
    char prot[BUFLEN];
    char state[BUFLEN];
    char type[BUFLEN];
    t_vmap *vmap;

    if (sscanf(vmapLine, "vmap %x %x %s %s %s %s", &address, &size, allocProt,
               prot, state, type) != 6)
        return(dump);
    vmap = TypeAlloc(t_vmap);
    vmap->address = address;
    vmap->size = size;
    vmap->allocProtection = decodeProtCode(allocProt);
    vmap->protection = decodeProtCode(prot);
    vmap->next = NULL;

    if (strcmp(state, "c") == 0)
        vmap->state = VM_STATE_COMMIT;
    else if (strcmp(state, "f") == 0)
        vmap->state = VM_STATE_FREE;
    else if (strcmp(state, "r") == 0)
        vmap->state = VM_STATE_RESERVE;
    else
        vmap->state = VM_STATE_UNKNOWN;

    if (strcmp(type, "i") == 0)
        vmap->type = VM_TYPE_IMAGE;
    else if (strcmp(type, "m") == 0)
        vmap->type = VM_TYPE_MAPPED;
    else if (strcmp(type, "p") == 0)
        vmap->type = VM_TYPE_PRIVATE;
    else
        vmap->type = VM_TYPE_UNKNOWN;

    if (dump->vmapTail)
        dump->vmapTail->next = vmap;
    else
        dump->vmap = vmap;
    dump->vmapTail = vmap;
    return(dump);
}

  static t_dump *
internalizeVMStat(char *vmLine, t_dump *dump)
{
    char tag[BUFLEN];
    longword address;
    int size;
    t_vmStat *vmStat;

    if (sscanf(vmLine, "vm %s %x %d", tag, &address, &size) != 3)
        return(dump);
    vmStat = TypeAlloc(t_vmStat);
    if (strcmp(tag, "map") == 0)
        vmStat->mode = "map";
    else if (strcmp(tag, "unmap") == 0)
        vmStat->mode = "unmap";
    else if (strcmp(tag, "commit") == 0)
        vmStat->mode = "commit";
    else if (strcmp(tag, "decommit") == 0)
        vmStat->mode = "decommit";
    else
        vmStat->mode = STRDUP(tag);
    vmStat->address = address;
    vmStat->size = size;
    vmStat->next = NULL;
    if (dump->vmStatsTail)
        dump->vmStatsTail->next = vmStat;
    else
        dump->vmStats = vmStat;
    dump->vmStatsTail = vmStat;
    return(dump);
}

  t_wsInfo *
wsNew(int count, char *tag)
{
    int i;
    t_wsInfo *result = TypeAlloc(t_wsInfo);
    result->count = count;
    result->tag = STRDUP(tag);
    for (i=0; i<WS_HI_SIZE; ++i)
        result->hiInfo[i] = NULL;
    result->next = NULL;
    return(result);
}

  static t_dump *
internalizeWsInfo(char *wsLine, t_dump *dump)
{
    int i;
    int count;
    char buf[BUFLEN];
    t_wsInfo *wsInfo;

    if (sscanf(wsLine, "workingset %d %s", &count, buf) != 2)
        return(dump);
    dump->wsCount++;
    wsInfo = wsNew(count, buf);
    for (i=0; i<count; ++i) {
        longword address;
        if (getLine(buf))
            sscanf(buf, " %x", &address);
        else
            return(NULL);
        wsAdd(wsInfo, address);
    }

    if (dump->wsTail)
        dump->wsTail->next = wsInfo;
    else
        dump->wsInfo = wsInfo;
    dump->wsTail = wsInfo;
    if (!dump->wsCurrent)
        dump->wsCurrent = wsInfo;
    return(dump);
}

  static bool
testKeyword(char *buf, char *keyword)
{
    int keywordLen = strlen(keyword);
    return(strncmp(buf, keyword, keywordLen) == 0 &&
           (isspace(buf[keywordLen]) || buf[keywordLen] == '\0'));
}

  t_dump *
parseDumpFile(char *filename, int dumpNumber)
{
    FILE *fyle = fopen(filename, "r");

    if (fyle) {
        initializeLineIO(fyle);
        return(internalizeDump(dumpNumber));
    } else {
        fprintf(stderr, "unable to open '%s'\n", filename);
        return(NULL);
    }
}
