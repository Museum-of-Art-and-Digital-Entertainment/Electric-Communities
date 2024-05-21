/*
  analdump.h -- Types and function protos for the Analyze Dump program.

  Chip Morningstar
  Electric Communities
  13-January-1998

  Copyright 1998 Electric Communities, all rights reserved worldwide.

*/

#include "generic.h"

extern bool VerboseMode;
extern longword ZoneLo;
extern longword ZoneHi;
extern int HeapOverhead;
extern int StatOverhead;

#define ZONE_MIN        0x00000000
#define ZONE_MAX        0xFFFFFFFF

#define CLASS_KEY       "class"
#define DUMPEND_KEY     "dumpend"
#define DUMPSTART_KEY   "dumpstart"
#define MALLOC_KEY      "malloc"
#define ROOT_KEY        "root"
#define SUMMARY_KEY     "summary"
#define VM_KEY          "vm"
#define VMDLL_KEY       "vmdll"
#define VMAP_KEY        "vmap"
#define WORKINGSET_KEY  "workingset"
#define WSFAIL_KEY      "wsfail"

#define VM_PROT_READ                    0
#define VM_PROT_READWRITE               1
#define VM_PROT_WRITECOPY               2
#define VM_PROT_EXECUTE                 3
#define VM_PROT_EXECUTEREAD             4
#define VM_PROT_EXECUTEREADWRITE        5
#define VM_PROT_EXECUTEWRITECOPY        6
#define VM_PROT_GUARD                   7
#define VM_PROT_NOACCESS                8
#define VM_PROT_NOCACHE                 9
#define VM_PROT_UNKNOWNPROT            10
#define VM_PROT_UNKNOWN                11

#define VM_STATE_COMMIT                 0
#define VM_STATE_FREE                   1
#define VM_STATE_RESERVE                2
#define VM_STATE_UNKNOWN                3

#define VM_TYPE_IMAGE                   0
#define VM_TYPE_MAPPED                  1
#define VM_TYPE_PRIVATE                 2
#define VM_TYPE_UNKNOWN                 3

#define SCOPE_CLASS     'c'
#define SCOPE_INSTANCE  'i'

#define TYPE_REFERENCE  'r'
#define TYPE_INVALID    'i'
#define TYPE_SCALAR     's'

#define ROOT_CODE_STACK         's'
#define ROOT_CODE_CSTACK        'S'
#define ROOT_CODE_GLOBAL        'g'
#define ROOT_CODE_STATIC        'c'
#define ROOT_CODE_INTERN        'i'

#define ROOT_STACK      0x001
#define ROOT_CSTACK     0x002
#define ROOT_GLOBAL     0x004
#define ROOT_STATIC     0x008
#define ROOT_INTERN     0x010
#define ROOT_MASK       0x01F

#define MARK_HEAP       0x020
#define MARK_MARK       0x040
#define MARK_AUX        0x080

#define OBJ_ARRAY       0x100
#define OBJ_CHAR_ARRAY  0x200
#define OBJ_OBJ_ARRAY   0x400
#define OBJ_NORMAL      0x000

#define HASH_TABLE_SIZE 512
#define HASH_TABLE_MASK 0x1FF

enum {
    FIND_CONSTANT_POOL,
    FIND_METHOD_TABLE
};

typedef struct vmapStruct {
    long address;
    int size;
    int allocProtection;
    int protection;
    int state;
    int type;
    struct vmapStruct *next;
} t_vmap;

typedef struct dllStruct {
    long address;
    char *name;
    t_vmap *vmap;
    struct dllStruct *next;
} t_dll;

typedef struct heapStruct {
    long address;
    int size;
    int blocks;
    int usedBytes;
    int freeBytes;
    t_vmap *vmap;
    struct heapStruct *next;
} t_heap;

typedef struct vmStatStruct {
    char *mode;
    long address;
    long size;
    struct vmStatStruct *next;
} t_vmStat;

typedef struct summaryStruct {
    char *info;
    struct summaryStruct *next;
} t_summary;

typedef struct mallocStruct {
    longword address;
    int size;
    struct mallocStruct *next;
} t_malloc;

typedef struct {
    long funcAddress;
    char name[256];
    char objfile[256];
} t_mapFileInfo;

typedef struct mallocCallerStruct {
    char *filename;
    int line;
    char *funcname;
    t_mapFileInfo *mapFileInfo;
    t_malloc *mallocs;
    int count;
    int bytes;
    struct mallocCallerStruct *next;
} t_mallocCaller;

typedef struct rootStruct {
    longword address;
    int rootType;
    struct rootStruct *next;
} t_root;

typedef struct fieldDescriptorStruct {
    char *name;
    char *type;
    char scopeCode;
    struct fieldDescriptorStruct *next;
} t_fieldDescriptor;

typedef struct classStruct {
    char *name;
    int size;
    int fieldCount;
    t_fieldDescriptor **fields;
    longword address;
    longword superAddress;
    longword methodTableAddress;
    longword constantPoolAddress;
    t_malloc *methodTableMalloc;
    t_malloc *constantPoolMalloc;
    struct classStruct *next;
    struct classStruct *superclass;
    int instanceCount;
    int totalInstanceSize;
    int flags;
} t_class;

typedef struct referencerStruct {
    struct objectStruct *object;
    struct referencerStruct *next;
} t_referencer;

typedef struct objectStruct {
    struct objectStruct *next;
    t_class *theClass;
    longword address;
    int fieldCount;
    union {
        longword *v;
        struct objectStruct **o;
    } fields;
    char *typeCodes;
    int flags;
    t_referencer *referencers;
} t_object;

#define WS_MAPWORD_BITS 5
#define WS_MAPWORD_SIZE (1 << WS_MAPWORD_BITS)
#define WS_MASK         (WS_MAPWORD_SIZE - 1)

#define WS_HI_SIZE      1024
#define WS_LO_SIZE      (1024 >> WS_MAPWORD_BITS)

#define WS_ADDR_HI(a)   ((a) >> 22)
#define WS_UNMAP_HI(i)  ((i) << 22)
#define WS_ADDR_LO(a)   (((a) >> 12) & 0x3FF)
#define WS_UNMAP_LO(i)  ((i) << (12 + WS_MAPWORD_BITS))
#define WS_UNMAP_BIT(b) ((b) << 12)
#define WS_UNMAP(h,l,b) (WS_UNMAP_HI(h) | WS_UNMAP_LO(l) | WS_UNMAP_BIT(b))

#define WS_LO_INDEX(a)  (WS_ADDR_LO(a) >> WS_MAPWORD_BITS)
#define WS_LO_BITNUM(a) (WS_ADDR_LO(a) & WS_MASK)
#define WS_LO_BIT(a)    (1 << WS_LO_BITNUM(a))

#define PAGE_SIZE       4096
#define PAGE_MASK       (~(PAGE_SIZE - 1))

typedef struct {
    longword loInfo[WS_LO_SIZE];
} t_wsHiInfo;

typedef struct wsInfoStruct {
    int count;
    char *tag;
    t_wsHiInfo *hiInfo[WS_HI_SIZE];
    struct wsInfoStruct *next;
} t_wsInfo;

typedef struct {
    struct objectStruct *next;
    t_class *theClass;
    longword address;
    int fieldCount;
    union {
        longword *v;
        struct objectStruct **o;
    } fields;
    char *typeCodes;
    int flags;
    t_referencer *referencers;
    int dimension;
    int size;
} t_array;

typedef struct {
    struct objectStruct *next;
    t_class *theClass;
    longword address;
    int fieldCount;     /* not used */
    longword *fields;   /* not used */
    char *typeCodes;    /* not used */
    int flags;
    t_referencer *referencers;
    int dimension;
    int size;
    char *stringValue;
} t_charArray;

typedef struct {
    char *descriptiveHeader;
    t_class **classTable;
    t_class **sortedClasses;
    int classCount;
    t_object **objectTable;
    t_object **sortedObjects;
    int objectCount;
    int byteCount;
    t_mallocCaller **mallocTable;
    t_mallocCaller **sortedMallocs;
    int mallocCallerCount;
    int mallocCount;
    int mallocBytes;
    t_summary *summaryInfo;
    t_summary *summaryTail;
    t_vmStat *vmStats;
    t_vmStat *vmStatsTail;
    t_vmap *vmap;
    t_vmap *vmapTail;
    t_wsInfo *wsInfo;
    t_wsInfo *wsTail;
    t_wsInfo *wsCurrent;
    int wsCount;
    t_dll *dll;
    t_dll *dllTail;
    t_heap **heapZones;
    int heapZoneCount;
    longword minAddress;
    longword maxAddress;
    longword *map;
    int mapSize;
} t_dump;

typedef struct fieldListStruct {
    longword address;
    char typeCode;
    struct fieldListStruct *next;
} t_fieldList;


/* adcommand.c */
bool allzone(void);
bool inzone(longword addr, int len);
bool processCommands(t_dump *dump, FILE *in);
bool touchzone(longword addr, int len);

/* adjoutput.c */
void printClass(char *name, t_dump *dump);
void printClassReachability(char *addressString, t_dump *dump);
void printClassTable(t_dump *dump);
void printGlobalReachability(t_dump *dump);
void printInstanceCount(char *name, t_dump *dump);
void printInstances(char *name, t_dump *dump);
void printObject(char *addressString, t_dump *dump);
void printObjectTable(t_dump *dump);
void printReachability(char *addressString, t_dump *dump);
void printReachableObjects(char *addressString, t_dump *dump);
void printRecursiveReferences(char *addressString, t_dump *dump);
void printReferences(char *addressString, t_dump *dump);
void printSizeInfo(char *name, t_dump *dump);
void printSizeSummary(char *sortby, t_dump *dump);

/* adlineio.c */
void aprintf(char *format, ...);
bool getLine(char *buf);
bool handleOutputRedirection(char *line);
void initializeLineIO(FILE *fyle);
void initializeOutput(void);
void logOutput(char *filename);
void restoreOutput(void);
void skipLine(FILE *fyle);
void ungetLine(char *buf);
int vlfscanf(FILE *fyle, char *format, ...);

/* adlinkmap.c */
void internalizeLinkMap(char *arg, t_dump *dump);

/* admoutput.c */
void findDefaultClassOverhead(t_dump *dump);
void printHeapZones(t_dump *dump, bool bigMapMode);
void printMallocBlocks(char *caller, t_dump *dump);
void printMallocFragmentation(t_dump *dump, bool verbose);
void printMallocTable(char *sortby, t_dump *dump);
void setClassOverheadSource(char *tag, char *source, t_dump *dump);

/* adoutput.c */
void printBigMap(t_dump *dump);
void printDescriptiveHeader(t_dump *dump);
void printDumpStats(t_dump *dump, bool printSummary);
void printVersion(void);
void printZoneTag(void);

/* adparse.c */
t_dump *parseDumpFile(char *filename, int dumpNumber);

/* adtables.c */
void addClass(t_class *theClass, t_dump *dump);
void addMallocCaller(t_mallocCaller *caller, t_dump *dump);
t_dump *addObject(t_object *object, t_dump *dump);
void computeBackReferences(t_dump *dump);
void computeDLLZones(t_dump *dump);
void computeHeapZones(t_dump *dump);
void convertReferenceAddresses(t_dump *dump);
void convertSuperclassAddresses(t_dump *dump);
int cmpClassNames(const void *p1, const void *p2);
int cmpMallocCallers(const void *p1, const void *p2);
t_class *handleClassReference(char *className, int size, t_dump *dump);
t_mallocCaller *handleMallocCaller(char *filename, int line, char *funcname,
    t_dump *dump);
bool isArray(t_object *object);
bool isClassClass(t_class *aClass);
bool isCharArray(t_object *object);
bool isMarked(t_object *object, int mark);
bool isMarkedClass(t_class *theClass, int mark);
bool isObjectArray(t_object *object);
bool isRoot(t_object *object);
t_class *lookupClass(char *className, t_dump *dump);
t_class *lookupClassByAddress(longword address, t_dump *dump);
t_mallocCaller *lookupMallocCaller(char *filename, int line, t_dump *dump);
t_object *lookupObject(longword address, t_dump *dump);
void markInternalReferences(t_dump *dump);
void markObject(t_object *object, int mark);
void markObjectAndChildren(t_object *object, int mark, t_object *stopObject);
int objectSize(t_object *object);
t_dump *sortDump(t_dump *dump);
void unmarkAllObjects(t_dump *dump, int mark);

/* adwoutput.c */
char *pDLL(long address, t_dump *dump);
char *pDLLName(t_dll *dll);
t_dll *findDLLByName(char *shortname, t_dump *dump);
void printDetailedVMap(t_dump *dump);
void printDLLs(t_dump *dump, bool bigMapMode);
void printVMap(t_dump *dump);
void printVMapForBigMap(t_dump *dump);
void printVMStats(t_dump *dump);
void printVMStatsForBigMap(t_dump *dump);
void printWorkingSetForBigMap(t_dump *dump);
void printWorkingSetPages(char *setname, t_dump *dump);
void setCurrentWorkingSet(char *setname, t_dump *dump);
void wsAdd(t_wsInfo *ws, longword address);
longword wsNextAddress(t_wsInfo *ws, longword address);
char *wsTag(t_wsInfo *ws, longword address, int size);
bool wsTest(t_wsInfo *ws, longword address);
