#include <StubPreamble.h>
#include "ec_e_openers_VarOpener.h"
#include "ec_e_openers_LongOpener.h"
#include "ec_e_openers_FloatOpener.h"
#include "ec_e_openers_DoubleOpener.h"
#include "ec_e_openers_RefOpener.h"
#include "ec_e_openers_Surgeon.h"

#ifdef cbFieldsCount
 #define THIS_IS_JAVA1_1
#else
 #undef THIS_IS_JAVA1_1
#endif

#define OPENERLIBVERSION (300)

#define HVarOpener       struct Hec_e_openers_VarOpener
#define HLongOpener      struct Hec_e_openers_LongOpener
#define HFloatOpener     struct Hec_e_openers_FloatOpener
#define HDoubleOpener    struct Hec_e_openers_DoubleOpener
#define HRefOpener       struct Hec_e_openers_RefOpener
#define HSurgeon         struct Hec_e_openers_Surgeon

#define libraryVersion       ec_e_openers_VarOpener_libraryVersion

#define peekField32          ec_e_openers_VarOpener_peekField32
#define pokeField32          ec_e_openers_VarOpener_pokeField32

#define peekFieldLow         ec_e_openers_LongOpener_peekFieldLow
#define peekFieldHigh        ec_e_openers_LongOpener_peekFieldHigh
#define pokeFieldLow         ec_e_openers_LongOpener_pokeFieldLow
#define pokeFieldHigh        ec_e_openers_LongOpener_pokeFieldHigh

#define peekFieldFloat       ec_e_openers_FloatOpener_peekFieldFloat
#define pokeFieldFloat       ec_e_openers_FloatOpener_pokeFieldFloat
#define peekFieldDouble      ec_e_openers_DoubleOpener_peekFieldDouble
#define pokeFieldDouble      ec_e_openers_DoubleOpener_pokeFieldDouble

#define peekFieldRef         ec_e_openers_RefOpener_peekFieldRef
#define pokeFieldRef         ec_e_openers_RefOpener_pokeFieldRef

#define rawInstanceOf        ec_e_openers_Surgeon_rawInstanceOf
#define getNumDeclaredFields ec_e_openers_Surgeon_getNumDeclaredFields
#define getFieldModifiers    ec_e_openers_Surgeon_getFieldModifiers
#define getFieldSignature    ec_e_openers_Surgeon_getFieldSignature
#define getFieldName         ec_e_openers_Surgeon_getFieldName
#define getFieldOffset       ec_e_openers_Surgeon_getFieldOffset


/** 
 * Internal error checking routines.  openers.c relies on the Java
 * code in ec.e.openers for its correctness.  Should openers.c detect
 * an internal error, this indicates a bug in ec.e.openers,
 * independent of what the rest of the Java program is doing. <p>
 *
 * Operations that need to be high speed suppress their own checking
 * when 'CheckOpenersFlag' is FALSE.  Operation that need not be high
 * speed check regardless.
 */
static /*boolean*/int CheckOpenersFlag = TRUE;

/**
 * Signals an error and returns TRUE when 'index' is a bad field index
 * of 'cb'.  Otherwise returns FALSE.
 */
static /*boolean*/int fieldIndexBad(Classjava_lang_Class* cb, jint index) {
    if (cb == NULL) {
        SignalError(0, JAVAPKG "InternalError", "class must not be null");
        return TRUE;
    }
    if (index < 0 || index >= cb->fields_count) {
        SignalError(0, JAVAPKG "InternalError", "invalid field index");
        return TRUE;
    }
    return FALSE;
}


/**
 * If 'byteOffset' is a valid offset of an instance variable field of
 * class 'cb', return the fieldblock*.  Otherwise return NULL.
 */
struct fieldblock* fieldAtOffset(Classjava_lang_Class* cb, jint byteOffset) {
    for (; cb != NULL; cb = unhand(cb->superclass)) {
        int i;
        for (i = 0; i < cb->fields_count; i++) {
            struct fieldblock* fb = &cb->fields[i];
            if ((fb->access & ACC_STATIC) == 0 
                && fb->u.offset == byteOffset) {
                
                return fb;
            }
        }
    }
    
    return NULL;
}
     

/**
 * Signals an error and returns NULL if byteOffset isn't an offset of
 * a field of object 'base'.  Otherwise returns the field's signature. 
 */
static char* fieldSignature(HObject* base, jint byteOffset) {
    ClassClass* cb;
    struct fieldblock* fb;

    if (base == NULL) {
        SignalError(0, JAVAPKG "InternalError", "base must not be null");
        return NULL;
    }
    if (obj_flags(base) != T_NORMAL_OBJECT) {
        SignalError(0, JAVAPKG "InternalError", "base must be normal");
        return NULL;
    }
    cb = obj_classblock(base);
#ifdef THIS_IS_JAVA1_1
    fb = fieldAtOffset(unhand(cb), byteOffset);
#else
    fb = fieldAtOffset(cb, byteOffset);
#endif
    if (fb == NULL) {
        SignalError(0, JAVAPKG "InternalError", "not at that offset");
        return NULL;
    }
    return fb->signature;
}


/*********** Report the version number of this library *************/

jint libraryVersion(HVarOpener* ignored)
{
  jint result = OPENERLIBVERSION; /* Version of this library - checked at load time */
  return result;
}

/************************ Native Routines By Class *****************/


/**
 * class Opener
 */

jint peekField32(HVarOpener* ignored, HObject* base, jint byteOffset) {
    if (CheckOpenersFlag) {
        char * sig = fieldSignature(base, byteOffset);
        if (sig == NULL || strchr("JFDL[", sig[0]) != NULL) {
            SignalError(0, JAVAPKG "InternalError", "field not integral");
            return 0;
        }
    }            
    return *((jint*)(((char*)unhand(base))+byteOffset));
}

void pokeField32(HVarOpener* ignored, HObject* base, 
                 jint byteOffset, jint newValue) {
    if (CheckOpenersFlag) {
        char * sig = fieldSignature(base, byteOffset);
        if (sig == NULL || strchr("JFDL[", sig[0]) != NULL) {
            SignalError(0, JAVAPKG "InternalError", "field not integral");
            return;
        }
    }            
    *((jint*)(((char*)unhand(base))+byteOffset)) = newValue;
}


/**
 * class LongOpener
 */

static int64_t* longFieldAddr (HObject* base, jint byteOffset) {
    if (CheckOpenersFlag) {
        char * sig = fieldSignature(base, byteOffset);
        if (sig == NULL || sig[0] != 'J') {
            SignalError(0, JAVAPKG "InternalError", "field not long");
            return 0;
        }
    }            
    return (int64_t*)(((char*)unhand(base))+byteOffset);
}

#ifndef LONG_LONGS_WORK
static int endianness = -1 ;

static void figureEndianness() {
    int64_t test = 1;
    long * testP = (long*)&test;
    if (testP[0] == 1 && testP[1] == 0) {
        /* little endian */
      endianness = 0 ;
    } else if (testP[1] == 1 && testP[0] == 0) {
        /* big endian */
      endianness = 1 ;
    }
}
#endif

jint peekFieldLow(HLongOpener* ignored, HObject* base, jint byteOffset) {
    return (jint)*longFieldAddr(base, byteOffset);
}

jint peekFieldHigh(HLongOpener* ignored, HObject* base, jint byteOffset) {
    return *longFieldAddr(base, byteOffset) >> 32;
}

void pokeFieldLow(HLongOpener* ignored, HObject* base, 
                   jint byteOffset, jint newValue) {
    int64_t* p = longFieldAddr(base, byteOffset);
    *p = (*p & ~0xFFFFFFFFL) | newValue;
}

void pokeFieldHigh(HLongOpener* ignored, HObject* base, 
                   jint byteOffset, jint newValue) {
    int64_t* p = longFieldAddr(base, byteOffset);

#ifdef LONG_LONGS_WORK
    /* 
     * The following should be platform independent, but doesn't seem
     * to work on the suns, apparently because of a bug in the gcc?
     * XXX Need to file bug report on gcc.
     */
    int64_t val = newValue;
    *p = (*p & 0xFFFFFFFFL) | (val << 32L);

#else
    long * pseudoP = (long*)p;
    if (endianness < 0) figureEndianness();
    if (endianness >= 0) {
        pseudoP[1-endianness] = newValue;
    } else {
        SignalError(0, JAVAPKG "InternalError", "endian mystery");
    }
#endif
}


/**
 * class FloatOpener
 */

float peekFieldFloat(HFloatOpener* ignored, HObject* base, jint byteOffset) {
    if (CheckOpenersFlag) {
        char* sig = fieldSignature(base, byteOffset);
        if (sig == NULL || sig[0] != 'F') {
            SignalError(0, JAVAPKG "InternalError", "field not float");
            return 0.0;
        }
    }            
    return *((float*)(((char*)unhand(base))+byteOffset));
}

void pokeFieldFloat(HFloatOpener* ignored, HObject* base, 
                    jint byteOffset, float newValue) {
    if (CheckOpenersFlag) {
        char * sig = fieldSignature(base, byteOffset);
        if (sig == NULL || sig[0] != 'F') {
            SignalError(0, JAVAPKG "InternalError", "field not float");
            return;
        }
    }            
    *((float*)(((char*)unhand(base))+byteOffset)) = newValue;
}


/**
 * class DoubleOpener
 */

/**
 * XXX!!! BUG!!! returning float instead of double
 */
float peekFieldDouble(HDoubleOpener* ignored, HObject* base, jint byteOffset){
    if (CheckOpenersFlag) {
        char * sig = fieldSignature(base, byteOffset);
        if (sig == NULL || sig[0] != 'D') {
            SignalError(0, JAVAPKG "InternalError", "field not double");
            return 0.0;
        }
    }            
    return *((double*)(((char*)unhand(base))+byteOffset));
}

void pokeFieldDouble(HDoubleOpener* ignored, HObject* base, 
                     jint byteOffset, double newValue) {
    if (CheckOpenersFlag) {
        char * sig = fieldSignature(base, byteOffset);
        if (sig == NULL || sig[0] != 'D') {
            SignalError(0, JAVAPKG "InternalError", "field not double");
            return;
        }
    }            
    *((double*)(((char*)unhand(base))+byteOffset)) = newValue;
}


/**
 * class RefOpener
 */

HObject *peekFieldRef(HRefOpener* ignored, HObject* base, 
                      jint byteOffset) {
    if (CheckOpenersFlag) {
        char * sig = fieldSignature(base, byteOffset);
        if (sig == NULL || strchr("L[", sig[0]) == NULL) {
            SignalError(0, JAVAPKG "InternalError", "field not reference");
            return NULL;
        }
    }            
    return *((HObject**)(((char*)unhand(base))+byteOffset));
}

void pokeFieldRef(HRefOpener* ignored, HObject* base, 
                  jint byteOffset, HObject* newValue) {
    if (CheckOpenersFlag) {
        char * sig = fieldSignature(base, byteOffset);
        if (sig == NULL || strchr("L[", sig[0]) == NULL) {
            SignalError(0, JAVAPKG "InternalError", "field not reference");
            return;
        }
    }            
    *((HObject**)(((char*)unhand(base))+byteOffset)) = newValue;
}


/**
 * class Surgeon
 */

HObject* rawInstanceOf(HSurgeon* ignored, Hjava_lang_Class* clazz) {
#ifdef THIS_IS_JAVA1_1
    return newobject(clazz, 0, EE());
#else
    return newobject(unhand(clazz), 0, EE());
#endif
}

jint getNumDeclaredFields(HSurgeon* ignored, Hjava_lang_Class* base) {
    Classjava_lang_Class *cb = unhand(base);
    if (cb == NULL) {
        SignalError(0, JAVAPKG "InternalError", "class must not be null");
        return 0;
    }
    return cb->fields_count;
}

jint getFieldModifiers(HSurgeon* ignored, Hjava_lang_Class* base, jint index) {
    Classjava_lang_Class *cb = unhand(base);
    if (fieldIndexBad(cb, index)) {
        return 0;
    }
    return cb->fields[index].access;
}

HString *getFieldSignature(HSurgeon* ignored, Hjava_lang_Class* base, jint index) {
    Classjava_lang_Class *cb = unhand(base);
    char * signature;

    if (fieldIndexBad(cb, index)) {
        return NULL;
    }
    signature = cb->fields[index].signature;
    return makeJavaString(signature, strlen(signature));
}

HString *getFieldName(HSurgeon* ignored, Hjava_lang_Class* base, jint index) {
    Classjava_lang_Class *cb = unhand(base);
    char * name;

    if (fieldIndexBad(cb, index)) {
        return NULL;
    }
    name = cb->fields[index].name;
    return makeJavaString(name, strlen(name));
}

jint getFieldOffset(HSurgeon* ignored, Hjava_lang_Class* base, jint index) {
    Classjava_lang_Class *cb = unhand(base);
    if (fieldIndexBad(cb, index)) {
        return 0;
    }
    if ((getFieldModifiers(ignored, base, index) & ACC_STATIC) != 0) {
        SignalError(0, JAVAPKG "InternalError", "static vars have no offset");
        return 0;
    }
    return cb->fields[index].u.offset;
}




