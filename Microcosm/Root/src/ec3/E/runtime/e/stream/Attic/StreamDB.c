/*
 * javaDB glue layer
 *
 *
 */

#include "StubPreamble.h"
#include "ec_e_stream_StreamDB.h"
#include <string.h>

typedef struct Hec_e_stream_StreamDB StreamDB;
/*extern HClass* java_lang_Object_getClass(HObject* object);*/

#define doGetClass(obj) \
   ((HClass *) execute_java_dynamic_method(0, obj, "getClass", \
                       "()Ljava/lang/Class;"))

typedef enum {
    Direction_read = 0,
    Direction_write = 1
} Direction;

static int _debug = 0;

/*
 * These two methods are the only place that is endian-aware.  Unfortunately 
 * execute_java_dynamic_method only returns 32-bits of a value so calling 
 * either writeLong, readLong, writeDouble, or readDouble is broken (you only
 * get 32 of the 64 bits back!).  Instead, we break the 64-bit values up into 
 * 2 32-bit values and write them out separately.  
 */
static void writeLong (HObject *stream, unsigned int *value) {
#ifdef LITTLE_ENDIAN
    execute_java_dynamic_method(0, stream, "writeInt", "(I)V", value[1]);
    execute_java_dynamic_method(0, stream, "writeInt", "(I)V", value[0]);
#else
    execute_java_dynamic_method(0, stream, "writeInt", "(I)V", value[0]);
    execute_java_dynamic_method(0, stream, "writeInt", "(I)V", value[1]);
#endif
}

static unsigned int readLong (HObject *stream, unsigned int *value) {
#ifdef LITTLE_ENDIAN
    value[1] = execute_java_dynamic_method(0, stream, "readInt", "()I");
    value[0] = execute_java_dynamic_method(0, stream, "readInt", "()I");
#else
    value[0] = execute_java_dynamic_method(0, stream, "readInt", "()I");
    value[1] = execute_java_dynamic_method(0, stream, "readInt", "()I");
#endif
}

static HObject* 
makeAnInstanceOfClass (HClass* theClass) {
    HObject* object = newobject(unhand(theClass), 0, EE()); 
    /* BUG--need to redo this debugging code at some point */
    /* if (_debug) fprintf(stderr,
                           "Allocated object in makeAnInstanceOfClass: %s\n",
               Object2CString(object)); */
    return object;
}

static bool_t
reallyImplementsInterface (ClassClass* theClass, ClassClass* theInterface) {
    for (; ; theClass = unhand(theClass->superclass)) {
    if (ImplementsInterface(theClass, theInterface, EE())) 
        return TRUE;
    if (theClass->superclass == 0) return FALSE;
    }
}

#define CODEABLE_INTERFACE "ec/e/db/RtCodeable"
static bool_t
isCodeableClass (ClassClass* cb) {
    static ClassClass* cface = NULL;
    bool_t ret;
    
    if (cface == NULL) {
        if (_debug) fprintf(stderr, "Getting RtCodeable interface\n");
        cface = FindClass(0, CODEABLE_INTERFACE, TRUE);
        if (_debug) fprintf(stderr, "Got RtCodeable interface: %s\n",
                cface->name);
    }
    if (_debug) {
    fprintf(stderr, "Checking to see if %s conforms to "
        CODEABLE_INTERFACE "\n", cb->name);
    }
    ret = reallyImplementsInterface(cb, cface);
    if (_debug) {
    fprintf(stderr, "%s %s RtCodeable\n", 
        cb->name, ret ? "implements" : "does not implement");
    }
    return ret;
}

typedef HObject* (*hFuncPtr)(ExecEnv*, HObject*, char*, char*, ...);
typedef HClass* (*cFuncPtr)(ExecEnv*, HObject*, char*, char*, ...);
typedef float (*fFuncPtr)(ExecEnv*, HObject*, char*, char*);
typedef double (*dFuncPtr)(ExecEnv*, HObject*, char*, char*);
typedef Hjava_lang_String* (*sFuncPtr)(ExecEnv*, HObject*, char*, char*);

//
// Helper to stream in "Class" for array elements,
// when array is an array of arrays or objects
//
static ClassClass* 
makeClassForArrayElements (HObject* stream) {
    static ClassClass* objectClass = NULL;
    sFuncPtr sFunc = (sFuncPtr) execute_java_dynamic_method;
    Hjava_lang_String* string = sFunc(0, stream, "decodeObject",
                      "()Ljava/lang/Object;");
    char* name = makeCString(string);
    // XXX (GJF) claiming Object is superclass probably isn't really right
    if (objectClass == NULL)
        objectClass = FindClassFromClass(0, "java/lang/Object", FALSE, 0);
    return FindClassFromClass(0, name, FALSE, objectClass);
}

//
// Main Array streaming routine
//
static HObject* 
walkArray(HObject* stream, char* signature, Direction direction, 
      HObject* inArray) {
    HObject* theArray = inArray;
    int arraySize, i;
    char* mySignature = signature;
    
    signature++;
    
    /* Code factored out that applies to all array cases: */
    if (direction == Direction_read) {
        arraySize = execute_java_dynamic_method(0, stream, "readInt", "()I");
    } else {
        arraySize = obj_length(theArray);
        execute_java_dynamic_method(0, stream, "writeInt", "(I)V", arraySize);
    }
    
    if (_debug) fprintf(stderr, "Array size is %d\n", arraySize);
    
    switch (*signature) {
    
    case SIGNATURE_BYTE:
    if (direction == Direction_read) {
        long size;
        theArray = ArrayAlloc(T_BYTE, arraySize);
        execute_java_dynamic_method(0, stream, "readFully", "([B)V", 
                    theArray);
    } else {
        execute_java_dynamic_method(0, stream, "write", "([BII)V", 
                    inArray, 0, arraySize);
    }
        break;
        
    case SIGNATURE_BOOLEAN:
        {
            char *aData;
            if (direction == Direction_read) {
                theArray = ArrayAlloc(T_BYTE, arraySize);
        aData = (char*)  unhand(theArray);
                for(i=0;i<arraySize;i++) {
                    *aData++ = execute_java_dynamic_method(0, stream, 
                               "readByte", "()B");
        }
            } else {
                aData = (char*) unhand(theArray);
                for(i=0;i<arraySize;i++) {
                    execute_java_dynamic_method(0, stream, "writeByte", "(I)V",
                        (long) *aData++);
        }
            }
        } /* End read boolean array. */
    break;
    
    case SIGNATURE_CHAR:
        {
            unicode *aData;
            if (direction == Direction_read) {
                theArray = ArrayAlloc(T_CHAR, arraySize);
                aData = (unicode*)  unhand(theArray);
                for(i=0;i<arraySize;i++) {
                    *aData++ = (unicode)
                execute_java_dynamic_method(0, stream, "readChar",
                            "()C"); 
        }
            } else {
                aData = (unicode*) unhand(theArray);
                for(i=0;i<arraySize;i++) {
                    execute_java_dynamic_method(0, stream, "writeChar", "(I)V",
                        (long) *aData++);
        }
            }
        } /* End read char array. */
    break;
    
    case SIGNATURE_SHORT:
        {
            short *aData;
            if (direction == Direction_read) {
                theArray = ArrayAlloc(T_SHORT, arraySize);
                aData = (short*)  unhand(theArray);
                for(i=0;i<arraySize;i++) {
                    *aData++ = (short) 
                     execute_java_dynamic_method(0, stream, 
                         "readShort", "()S");
                }
            } else {
                aData = (short*) unhand(theArray);
                for(i=0;i<arraySize;i++) {
                    execute_java_dynamic_method(0,stream, "writeShort", "(I)V",
                        (long) *aData++);
                 }
            }
        } /* End read short array. */
    break;
    
    case SIGNATURE_INT:
        {
            long *aData;
            if (direction == Direction_read) {
                theArray = ArrayAlloc(T_INT, arraySize);
                aData = (long*)  unhand(theArray);
                for(i=0;i<arraySize;i++) {
                    *aData++ = execute_java_dynamic_method(0, stream, 
                               "readInt", "()I");
        }
            } else {
                aData = (long*) unhand(theArray);
                for(i=0;i<arraySize;i++) {
                    execute_java_dynamic_method(0, stream, "writeInt", "(I)V",
                        *aData++);
        }
            }
        } /* End read int array. */
    break;
    
    case SIGNATURE_FLOAT:
        {
            unsigned int *aData;
            if (direction == Direction_read) {
                theArray = ArrayAlloc(T_FLOAT, arraySize);
                aData = (unsigned int*) unhand(theArray);
                for(i=0;i<arraySize;i++) {
            *aData++ = execute_java_dynamic_method(0, stream,
                               "readInt", "()I");
        }
            } else {
        aData = (unsigned int*) unhand(theArray);
                for(i=0;i<arraySize;i++) {
                    execute_java_dynamic_method(0, stream, "writeInt", "(I)V",
                        *aData++);
                }
            }
        } /* End read float array. */
    break;
    
    case SIGNATURE_DOUBLE:
        {
            unsigned int *aData;
            if (direction == Direction_read) {
                theArray = ArrayAlloc(T_DOUBLE, arraySize);
                aData = (unsigned int*)unhand(theArray);
                for(i=0;i<arraySize;i++) {
            aData = ((unsigned int*)unhand(theArray)) + (i*2);
            readLong (stream, aData);
            if (_debug) {
            double d;
            int* dp = (int*)&d;
            dp[0] = aData[0];
            dp[1] = aData[1];
            fprintf(stderr,
                   "Read double %8.20f (made up of ints: %d %d)\n",
                d, aData[0], aData[1]);
            }
                }
            } else {
                aData = (unsigned int*) unhand(theArray);
                for(i=0;i<arraySize;i++) {
            aData = ((unsigned int*)unhand(theArray)) + (i*2);
            if (_debug) {
            double d;
            int* dp = (int*)&d;
            dp[0] = aData[0];
            dp[1] = aData[1];
            fprintf(stderr,
                  "Wrote double %8.20f (made up of ints: %d %d)\n",
                    d, aData[0], aData[1]);
            }
            writeLong (stream, aData);
                }
            }
        } /* End read double array. */
    break;
    
    case SIGNATURE_LONG:
        {
            unsigned int *aData;         
            if (direction == Direction_read) {
                theArray = ArrayAlloc(T_LONG, arraySize);
                for(i=0;i<arraySize;i++) {
            /* XXX - should make consistent*/
                    aData = ((unsigned int*) unhand(theArray)) + (i*2);
            readLong (stream, aData);
            if (_debug) {
            fprintf(stderr,
                "(%d) Read Long in array as ints: %u, %u\n",
                i, aData[0], aData[1]);
            }
        }
            } else {
                for(i=0;i<arraySize;i++) {
                    aData = ((unsigned int*) unhand(theArray)) + (i*2);
            if (_debug) {
            fprintf(stderr,
                "(%d) Writing Long in array as ints: %u, %u\n",
                i, aData[0], aData[1]);
            }
            writeLong (stream, aData);
                }
            }
        } /* End read long array. */
    break;
    
    case SIGNATURE_CLASS:
        {
            HObject** aData;
            ClassClass** clp;
            ClassClass* cl;
            hFuncPtr hFunc = (hFuncPtr) execute_java_dynamic_method;
            if (direction == Direction_read) {
                theArray = ArrayAlloc(T_CLASS, arraySize);
                for(i=0;i<arraySize;i++) {
                    aData = (HObject**) unhand(theArray) + i;
                    *aData = hFunc(0, stream, "decodeObject", 
                   "()Ljava/lang/Object;");
                }
                clp = (ClassClass**)unhand(theArray) + i;
                *clp = cl = makeClassForArrayElements(stream);
                if (_debug) {
                    fprintf(stderr, "<- Read array of class %s, ", cl->name);
                    if (cl->superclass) {
                        fprintf(stderr, "superclass is %s\n",
                ((ClassClass*)unhand(cl->superclass))->name);
                    } else {
                        fprintf(stderr, "no superclass\n");
                    }
                }
            } else {
                for(i=0;i<arraySize;i++) {
                    aData =  (HObject**) unhand(theArray) + i;
                    execute_java_dynamic_method(0, stream, "encodeObject",
                        "(Ljava/lang/Object;)V", 
                        *aData);
                }
                cl = *((ClassClass**)unhand(theArray) + i);
                execute_java_dynamic_method(0, stream, "encodeObject",
                        "(Ljava/lang/Object;)V",
                        makeJavaString(cl->name, 
                               strlen(cl->name))); 
                if (_debug) {
                    fprintf(stderr, "-> Wrote array of class %s, ", cl->name);
                    if (cl->superclass) {
                        fprintf(stderr, "superclass is %s\n", 
                ((ClassClass*)unhand(cl->superclass))->name);
                    } else {
                        fprintf(stderr, "no superclass\n");
                    }
                }
            }
        } /* End class array. */
    break;
    
    /* Yes, we have an array of arrays, so we have to recurse. */
    case SIGNATURE_ARRAY:
        {
            HObject** aData;
            ClassClass** clp;
            ClassClass* cl;
            hFuncPtr hFunc = (hFuncPtr) execute_java_dynamic_method;
            if (direction == Direction_read) {
                theArray = ArrayAlloc(T_CLASS, arraySize);
                clp = (ClassClass**)unhand(theArray) + arraySize;
                *clp = cl = makeClassForArrayElements(stream);
                if (_debug) {
                    fprintf(stderr, "<- Read (sub)array with signature %s, ", 
                cl->name);
                    if (cl->superclass) {
                        fprintf(stderr, "superclass is %s\n", 
                ((ClassClass*)unhand(cl->superclass))->name);
                    } else {
                        fprintf(stderr, "no superclass\n");
                    }
                }
                for(i=0;i<arraySize;i++) {
                    aData = (HObject**) unhand(theArray) + i;
                    *aData = hFunc(0, stream, "decodeObject",
                   "()Ljava/lang/Object;");
                }
            } else {
                cl = *((ClassClass**)unhand(theArray) + arraySize);
                if (_debug) {
                    fprintf(stderr, "-> Writing a (sub)array of class %s, ", 
                cl->name);
                    if (cl->superclass) {
                        fprintf(stderr, "superclass is %s\n", 
                ((ClassClass*)unhand(cl->superclass))->name);
                    } else {
                        fprintf(stderr, "no superclass\n");
                    }
                }
                execute_java_dynamic_method(0, stream, "encodeObject", 
                        "(Ljava/lang/Object;)V",
                        makeJavaString(cl->name, 
                               strlen(cl->name)));
        
                aData = (HObject**) unhand(theArray);
                for(i=0;i<arraySize;i++) {
                    aData =  (HObject**) unhand(theArray) + i;   
                    execute_java_dynamic_method(0, stream, "encodeObject", 
                        "(Ljava/lang/Object;)V",
                        *aData);
                }
            }
        } /* End array of array. */
    break;
    
    /* We missed one! */
    default:
    fprintf(stderr, "*** Unknown array type to read: %s\n", signature);
    break;
    } /* End switch on array type. */
    
    return(theArray);
}

// ---------------------- First level object streamer -------------------- 

static void
walkObject (HClass* theClass, HObject* object, 
        HObject* stream, Direction direction) {
    ClassClass* c = (ClassClass*)unhand(theClass);
    int i;
    char* p = (char*) unhand(object);
    
    if (_debug) {
        fprintf(stderr, 
        "WalkObject() for %s called to %s, field count is %d\n", 
        c->name, (direction == Direction_read) ? "read" : "write", 
        c->fields_count);
    }
    
    if (c->superclass) {
        if (_debug) fprintf(stderr, "Walking superclass for %s\n", c->name);
        walkObject(c->superclass, object, stream, direction);
    }
    
    for (i = 0; i < c->fields_count; i++) {
        struct fieldblock* field = c->fields + i;
        if (_debug) {
        fprintf(stderr, "%s signature %s, offset %d\n", 
            (direction == Direction_read) ? "Reading" : "Writing",
            field->signature, field->u.offset);
    }
    // If this is a transient instance variable, skip it
    if (field->access & (ACC_TRANSIENT | ACC_STATIC)) {
        if (_debug) fprintf(stderr, "Ignoring var with access %d sig %s\n",
                (int) field->access, field->signature);
        continue;
    }
    
        switch (field->signature[0]) {
        
        case SIGNATURE_BYTE:
            if (direction == Direction_read) {
                *((long*)(p + field->u.offset)) = 
                execute_java_dynamic_method(0, stream, "readByte", "()B");
            } else {
                execute_java_dynamic_method(0, stream, "writeByte", "(I)V", 
                        *((long*)(p + field->u.offset)));
        }
            break;
        
        case SIGNATURE_BOOLEAN:
            if (direction == Direction_read) {
                *((long*)(p + field->u.offset)) = 
                execute_java_dynamic_method(0, stream, "readBoolean", "()Z");  
            } else {
                execute_java_dynamic_method(0, stream, "writeBoolean", "(Z)V", 
                        *((long*)(p + field->u.offset)));
        }
            break;
        
        case SIGNATURE_CHAR:
            if (direction == Direction_read) {
                *((long*)(p + field->u.offset)) = 
                execute_java_dynamic_method(0, stream, "readChar", "()C");  
            } else {
                execute_java_dynamic_method(0, stream, "writeChar", "(I)V", 
                        *((long*)(p + field->u.offset)));
        }
            break;
        
        case SIGNATURE_CLASS:
        {
        hFuncPtr hFunc = (hFuncPtr) execute_java_dynamic_method;
        
        if (direction == Direction_read) {
            *((HObject**)(p + field->u.offset)) = 
            hFunc(0, stream, "decodeObject", "()Ljava/lang/Object;"); 
        } else {
            execute_java_dynamic_method(0, stream, "encodeObject", 
                        "(Ljava/lang/Object;)V", 
                     *((HObject**)(p + field->u.offset)));
        }
        }       
        break;
    
        case SIGNATURE_DOUBLE:
        {
        unsigned int *ints;
        if (direction == Direction_read) {
            ints = ((unsigned int*)(p + field->u.offset));
            readLong (stream, ints);
            if (_debug) {
            double d;
            int* dp = (int*)&d;
            dp[0] = ints[0];
            dp[1] = ints[1];
            fprintf(stderr,
                   "Read double %8.20f (made up of ints: %d %d)\n",
                d, ints[0], ints[1]);
            }
        } else {
            unsigned int intArray[2];
            ints = intArray;
            ints[0] = *((int*)(p + field->u.offset));
            ints[1] = *((int*)(p + field->u.offset + 4));
            writeLong (stream, ints);
            if (_debug) {
            double d;
            int* dp = (int*)&d;
            dp[0] = ints[0];
            dp[1] = ints[1];
            fprintf(stderr,
                  "Wrote double %8.20f (made up of ints: %d %d)\n",
                d, ints[0], ints[1]);
            }
        }
        }
        break;
    
        case SIGNATURE_FLOAT:
        {
        if (direction == Direction_read) {
            *((int*)(p + field->u.offset))
                = execute_java_dynamic_method(0, stream,
                              "readInt", "()I");
        } else {
            int value = *((int*)(p + field->u.offset));
                execute_java_dynamic_method(0, stream,
                            "writeInt", "(I)V", value);
        }
        }
        break;
    
        case SIGNATURE_INT:
            if (direction == Direction_read) {
                *((long*)(p + field->u.offset)) = 
                execute_java_dynamic_method(0, stream, "readInt", "()I");   
            } else {
                execute_java_dynamic_method(0, stream, "writeInt", "(I)V", 
                        *((long*)(p + field->u.offset)));
        }
            break;
        
        case SIGNATURE_LONG:
        {
        unsigned int* nump;
        if (direction == Direction_read) {
            nump = ((unsigned int*)(p + field->u.offset));
            readLong (stream, nump);
            if (_debug) {
            fprintf(stderr, "Read Long values as ints: %u, %u\n", 
                nump[0], nump[1]);
            }
        } else {
            unsigned int num[2];
            nump = num;
            nump[0] = *((unsigned int*)(p + field->u.offset));
            nump[1] = *((unsigned int*)(p + field->u.offset + 4));
            writeLong (stream, nump);
            if (_debug) {
            fprintf(stderr, "Wrote Long values as ints: %u, %u\n",
                nump[0], nump[1]);
            }
        }
        }           
        break;
    
        case SIGNATURE_SHORT:
            if (direction == Direction_read) {
                *((long*)(p + field->u.offset)) = 
                execute_java_dynamic_method(0, stream, "readShort", "()S"); 
            } else {
                execute_java_dynamic_method(0, stream, "writeShort", "(I)V", 
                        *((long*)(p + field->u.offset)));
        }
            break;
        
        case SIGNATURE_ARRAY:
        {
        if (direction == Direction_read) {
            hFuncPtr hFunc = (hFuncPtr) execute_java_dynamic_method;
            HObject** aData = (HObject**) (p + field->u.offset);
            *aData = hFunc(0, stream, "decodeObject", 
                   "()Ljava/lang/Object;");
        } else {
            HObject* theArray = *((HObject**)(p + field->u.offset));
            execute_java_dynamic_method(0, stream, "encodeObject", 
                        "(Ljava/lang/Object;)V", 
                        theArray);
        }
        }
        break;
    
        default:
            fprintf(stderr, "*** Unknown type to %s: %s\n", 
            (direction == Direction_read) ? "read" : "write",
            field->signature);
        }
    }
}

void 
ec_e_stream_StreamDB_setDebugOn (struct Hec_e_stream_StreamDB* self) {
    _debug = 1;
}

void 
ec_e_stream_StreamDB_setDebugOff (struct Hec_e_stream_StreamDB* self) {
    _debug = 0;
}

HObject*
ec_e_stream_StreamDB_allocate (struct Hec_e_stream_StreamDB* self, 
                   HClass* theClass)
{
    HObject* object;
    char* signature = ((ClassClass*)unhand(theClass))->name;  
    if (signature[0] != SIGNATURE_ARRAY) {
        if (_debug) {
        fprintf(stderr, 
            "StreamDB::allocate(), making instance, %s\n", signature);
    }
    object = makeAnInstanceOfClass(theClass);
    } else {
    if (_debug) {
        fprintf(stderr, 
          "StreamDB::allocate(), error: Can't make array (signature %s)\n",
            signature);
    }
    object = NULL;
    }   
    return object;
}

HObject*
ec_e_stream_StreamDB_preload (struct Hec_e_stream_StreamDB* self,
                  HClass* theClass,
                  struct Hec_e_run_RtDecoder* stream)
{
    HObject* object;
    char* signature = ((ClassClass*)unhand(theClass))->name;  
    if (signature[0] != SIGNATURE_ARRAY) {
        if (_debug) 
        fprintf(stderr, 
            "StreamDB::preload(), making instance, %s\n", signature);
    object = makeAnInstanceOfClass(theClass);
    } else {
    if (_debug) {
        fprintf(stderr, 
            "StreamDB::preload(), array with signature %s\n", 
            signature);
    }
    object = walkArray((HObject*)stream, signature, Direction_read, NULL);
    }   
    return object;
}

HObject*
ec_e_stream_StreamDB_load (struct Hec_e_stream_StreamDB* self,
               HObject* object,
               struct Hec_e_run_RtDecoder* stream)
{
    HObject* receiver;
    char* signature;
    HClass* theClass;
    
    if (object == NULL) return NULL;
    
    theClass = doGetClass(object);
    signature = ((ClassClass*)unhand(theClass))->name;  
    
    // Walk the class info and read in each instance var for the object
    if (signature[0] != SIGNATURE_ARRAY) {
        if (_debug) 
        fprintf(stderr, 
            "StreamDB::load(), walking object, %s\n", signature);
        walkObject(theClass, object, (HObject*)stream, Direction_read);
    } else {
        if (_debug) 
        fprintf(stderr, 
            "StreamDB::load(), class is array, already loaded\n");
    }
    
    return object;
}

void 
ec_e_stream_StreamDB_store (struct Hec_e_stream_StreamDB* self,
                HObject* object,
                struct Hec_e_run_RtEncoder* stream)
{
    HClass* theClass = doGetClass(object);
    
    if (obj_flags(object) == T_NORMAL_OBJECT) {
        walkObject(theClass, object, (HObject*)stream, Direction_write);
    } else {
        char* signature = ((ClassClass*)unhand(theClass))->name;
        if (_debug) {
        fprintf(stderr, "Storing array with signature %s\n", signature);
    }
        walkArray((HObject*)stream, signature, Direction_write, object);    
    }
    
}

