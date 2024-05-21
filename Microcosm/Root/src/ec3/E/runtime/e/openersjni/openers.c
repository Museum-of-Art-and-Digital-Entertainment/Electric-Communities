#include <jni.h>
#include "ec_e_openers_VarOpener.h"
#include "ec_e_openers_BooleanOpener.h"
#include "ec_e_openers_ByteOpener.h"
#include "ec_e_openers_CharOpener.h"
#include "ec_e_openers_ShortOpener.h"
#include "ec_e_openers_IntOpener.h"
#include "ec_e_openers_LongOpener.h"
#include "ec_e_openers_FloatOpener.h"
#include "ec_e_openers_DoubleOpener.h"
#include "ec_e_openers_RefOpener.h"
#include "ec_e_openers_Surgeon.h"

#define OPENERLIBVERSION (400)

#define libraryVersion       Java_ec_e_openers_VarOpener_libraryVersion

#define peekFieldBoolean     Java_ec_e_openers_BooleanOpener_peekFieldBoolean
#define pokeFieldBoolean     Java_ec_e_openers_BooleanOpener_pokeFieldBoolean
#define peekFieldByte        Java_ec_e_openers_ByteOpener_peekFieldByte
#define pokeFieldByte        Java_ec_e_openers_ByteOpener_pokeFieldByte
#define peekFieldChar        Java_ec_e_openers_CharOpener_peekFieldChar
#define pokeFieldChar        Java_ec_e_openers_CharOpener_pokeFieldChar
#define peekFieldShort       Java_ec_e_openers_ShortOpener_peekFieldShort
#define pokeFieldShort       Java_ec_e_openers_ShortOpener_pokeFieldShort
#define peekFieldInt         Java_ec_e_openers_IntOpener_peekFieldInt
#define pokeFieldInt         Java_ec_e_openers_IntOpener_pokeFieldInt
#define peekFieldLong        Java_ec_e_openers_LongOpener_peekFieldLong
#define pokeFieldLong        Java_ec_e_openers_LongOpener_pokeFieldLong
#define peekFieldFloat       Java_ec_e_openers_FloatOpener_peekFieldFloat
#define pokeFieldFloat       Java_ec_e_openers_FloatOpener_pokeFieldFloat
#define peekFieldDouble      Java_ec_e_openers_DoubleOpener_peekFieldDouble
#define pokeFieldDouble      Java_ec_e_openers_DoubleOpener_pokeFieldDouble

#define peekFieldRef         Java_ec_e_openers_RefOpener_peekFieldRef
#define testNullRet         Java_ec_e_openers_RefOpener_testNullRet
#define pokeFieldRef         Java_ec_e_openers_RefOpener_pokeFieldRef

#define rawInstanceOf        Java_ec_e_openers_Surgeon_rawInstanceOf
//#define getFieldID           Java_ec_e_openers_Surgeon_getFieldID

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
static /*boolean*/int CheckOpenersFlag = JNI_TRUE;

/**
 * XXX Temporary error signaler used during debugging
 * this is supposed to be replaced before this file is checked in
 */
static void signalerror(JNIEnv* env, const char* msg) {
    jclass errclass;
    jthrowable errobj = (*env)->ExceptionOccurred(env);
    fprintf(stderr, "native method signaling error: %s\n", msg);
    if (errobj == NULL) {
        errclass = (*env)->FindClass(env, "java/lang/InternalError");
        (*env)->ThrowNew(env, errclass, msg);
    }
}

static jfieldID myGetFieldID(JNIEnv* env, jobject base, jstring fieldName, const char* csig) {
    const char* cname;
    jclass clazz;
    jfieldID result;

    if ((base == NULL) || (fieldName == NULL)) {
        /* signal error */
        signalerror(env, "base and fieldName must not be NULL");
        return NULL;
    }
    clazz = (*env)->GetObjectClass(env, base);
    cname = (*env)->GetStringUTFChars(env, fieldName, NULL);
    if (cname == NULL) {
        /* signal error */
        signalerror(env, "Error converting string");
        return NULL;
    }
    result = ((*env)->GetFieldID(env, clazz, cname, csig));
    if (result == NULL) {
        /* signal error */
        signalerror(env, "Could not get fieldID");
        return NULL;
    }
    (*env)->ReleaseStringUTFChars(env, fieldName, cname);
    return result;
}


/*********** Report the version number of this library *************/

JNIEXPORT jint JNICALL
libraryVersion(JNIEnv* env, jobject ignored)
{
  jint result = OPENERLIBVERSION; /* Version of this library - checked at load time */
  return result;
}

/************************ Native Routines By Class *****************/


/**
 * class BooleanOpener
 */

JNIEXPORT jboolean JNICALL
peekFieldBoolean(JNIEnv* env, jobject me, jobject base, jstring fieldID) {
    jfieldID myFieldID;
    jboolean result = JNI_FALSE;
    myFieldID = myGetFieldID(env, base, fieldID, "Z");
    if (myFieldID != NULL) {
        result = (*env)->GetBooleanField(env, base, myFieldID);
    }
    return result;
}

JNIEXPORT void JNICALL
pokeFieldBoolean(JNIEnv* env, jobject me, jobject base, jstring fieldID, jboolean newValue) {
    jfieldID myFieldID;
    myFieldID = myGetFieldID(env, base, fieldID, "Z");
    if (myFieldID != NULL) {
        (*env)->SetBooleanField(env, base, myFieldID, newValue);
    }
    return;
}

/**
 * class ByteOpener
 */

JNIEXPORT jbyte JNICALL
peekFieldByte(JNIEnv* env, jobject me, jobject base, jstring fieldID) {
    jfieldID myFieldID;
    jbyte result = 0;
    myFieldID = myGetFieldID(env, base, fieldID, "B");
    if (myFieldID != NULL) {
        result = (*env)->GetByteField(env, base, myFieldID);
    }
    return result;
}

JNIEXPORT void JNICALL
pokeFieldByte(JNIEnv* env, jobject me, jobject base, jstring fieldID, jbyte newValue) {
    jfieldID myFieldID;
    myFieldID = myGetFieldID(env, base, fieldID, "B");
    if (myFieldID != NULL) {
        (*env)->SetByteField(env, base, myFieldID, newValue);
    }
    return;
}

/**
 * class CharOpener
 */

JNIEXPORT jchar JNICALL
peekFieldChar(JNIEnv* env, jobject me, jobject base, jstring fieldID) {
    jfieldID myFieldID;
    jchar result = 0;
    myFieldID = myGetFieldID(env, base, fieldID, "C");
    if (myFieldID != NULL) {
        result = (*env)->GetCharField(env, base, myFieldID);
    }
    return result;
}

JNIEXPORT void JNICALL
pokeFieldChar(JNIEnv* env, jobject me, jobject base, jstring fieldID, jchar newValue) {
    jfieldID myFieldID;
    myFieldID = myGetFieldID(env, base, fieldID, "C");
    if (myFieldID != NULL) {
        (*env)->SetCharField(env, base, myFieldID, newValue);
    }
    return;
}

/**
 * class ShortOpener
 */

JNIEXPORT jshort JNICALL
peekFieldShort(JNIEnv* env, jobject me, jobject base, jstring fieldID) {
    jfieldID myFieldID;
    jshort result = 0;
    myFieldID = myGetFieldID(env, base, fieldID, "S");
    if (myFieldID != NULL) {
        result = (*env)->GetShortField(env, base, myFieldID);
    }
    return result;
}

JNIEXPORT void JNICALL
pokeFieldShort(JNIEnv* env, jobject me, jobject base, jstring fieldID, jshort newValue) {
    jfieldID myFieldID;
    myFieldID = myGetFieldID(env, base, fieldID, "S");
    if (myFieldID != NULL) {
        (*env)->SetShortField(env, base, myFieldID, newValue);
    }
    return;
}

/**
 * class IntOpener
 */

JNIEXPORT jint JNICALL
peekFieldInt(JNIEnv* env, jobject me, jobject base, jstring fieldID) {
    jfieldID myFieldID;
    jint result = 0;
    myFieldID = myGetFieldID(env, base, fieldID, "I");
    if (myFieldID != NULL) {
        result = (*env)->GetIntField(env, base, myFieldID);
    }
    return result;
}

JNIEXPORT void JNICALL
pokeFieldInt(JNIEnv* env, jobject me, jobject base, jstring fieldID, jint newValue) {
    jfieldID myFieldID;
    myFieldID = myGetFieldID(env, base, fieldID, "I");
    if (myFieldID != NULL) {
        (*env)->SetIntField(env, base, myFieldID, newValue);
    }
    return;
}

/**
 * class LongOpener
 */

JNIEXPORT jlong JNICALL
peekFieldLong(JNIEnv* env, jobject me, jobject base, jstring fieldID) {
    jfieldID myFieldID;
    jlong result = 0;
    myFieldID = myGetFieldID(env, base, fieldID, "J");
    if (myFieldID != NULL) {
        result = (*env)->GetLongField(env, base, myFieldID);
    }
    return result;
}

JNIEXPORT void JNICALL
pokeFieldLong(JNIEnv* env, jobject me, jobject base, jstring fieldID, jlong newValue) {
    jfieldID myFieldID;
    myFieldID = myGetFieldID(env, base, fieldID, "J");
    if (myFieldID != NULL) {
        (*env)->SetLongField(env, base, myFieldID, newValue);
    }
    return;
}

/**
 * class FloatOpener
 */

JNIEXPORT jfloat JNICALL
peekFieldFloat(JNIEnv* env, jobject me, jobject base, jstring fieldID) {
    jfieldID myFieldID;
    jfloat result = 0.0;
    myFieldID = myGetFieldID(env, base, fieldID, "F");
    if (myFieldID != NULL) {
        result = (*env)->GetFloatField(env, base, myFieldID);
    }
    return result;
}

JNIEXPORT void JNICALL
pokeFieldFloat(JNIEnv* env, jobject me, jobject base, jstring fieldID, jfloat newValue) {
    jfieldID myFieldID;
    myFieldID = myGetFieldID(env, base, fieldID, "F");
    if (myFieldID != NULL) {
        (*env)->SetFloatField(env, base, myFieldID, newValue);
    }
    return;
}

/**
 * class DoubleOpener
 */

JNIEXPORT jdouble JNICALL
peekFieldDouble(JNIEnv* env, jobject me, jobject base, jstring fieldID) {
    jfieldID myFieldID;
    jdouble result = 0.0;
    myFieldID = myGetFieldID(env, base, fieldID, "D");
    if (myFieldID != NULL) {
        result = (*env)->GetDoubleField(env, base, myFieldID);
    }
    return result;
}

JNIEXPORT void JNICALL
pokeFieldDouble(JNIEnv* env, jobject me, jobject base, jstring fieldID, jdouble newValue) {
    jfieldID myFieldID;
    myFieldID = myGetFieldID(env, base, fieldID, "D");
    if (myFieldID != NULL) {
        (*env)->SetDoubleField(env, base, myFieldID, newValue);
    }
    return;
}

/**
 * class RefOpener
 */

JNIEXPORT jobject JNICALL
peekFieldRef(JNIEnv* env, jobject me, jobject base, jstring fieldID, jstring sig) {
    jfieldID myFieldID;
    jobject result = NULL;
    int res1 = 0xDEADBED;
    int res2 = 0xDEADBED;
    const char* csig;
    if ((base == NULL) || (fieldID == NULL) || (sig == NULL)) {
        /* signal error */
        signalerror(env, "Arguments must not be NULL");
        return NULL;
    }
    csig = (*env)->GetStringUTFChars(env, sig, NULL);
    if (csig == NULL) {
        /* signal error */
        signalerror(env, "Error converting string");
        return NULL;
    }
    myFieldID = myGetFieldID(env, base, fieldID, csig);
    if (myFieldID != NULL) {
        (*env)->ReleaseStringUTFChars(env, sig, csig);
        result = (*env)->GetObjectField(env, base, myFieldID);
        if (result != NULL) {
            result = (*env)->NewGlobalRef(env, result);
        }
    }
    return result;
}

JNIEXPORT jobject JNICALL
testNullRet(JNIEnv* env, jobject me) {
    return NULL;
}

JNIEXPORT void JNICALL
pokeFieldRef(JNIEnv* env, jobject me, jobject base, jstring fieldID, jstring sig, jobject newValue) {
    jfieldID myFieldID;
    const char* csig;
    jobject objToStuff;
    if ((base == NULL) || (fieldID == NULL) || (sig == NULL)) {
        /* signal error */
        signalerror(env, "Arguments must not be NULL");
        return;
    }
    csig = (*env)->GetStringUTFChars(env, sig, NULL);
    if (csig == NULL) {
        /* signal error */
        signalerror(env, "Error converting string");
        return;
    }
    myFieldID = myGetFieldID(env, base, fieldID, csig);
    if (myFieldID != NULL) {
        (*env)->ReleaseStringUTFChars(env, sig, csig);
        if (((*env)->ExceptionOccurred(env)) != NULL) return;
        if (newValue == NULL) {
            objToStuff = newValue;
        } else {
            objToStuff = (*env)->NewGlobalRef(env, newValue);
            if (((*env)->ExceptionOccurred(env)) != NULL) return;
        }
        if (((*env)->ExceptionOccurred(env)) != NULL) return;
        (*env)->SetObjectField(env, base, myFieldID, objToStuff);
    }
    return;
}

/**
 * class Surgeon
 */

JNIEXPORT jobject JNICALL
rawInstanceOf(JNIEnv* env, jobject me, jclass clazz) {
    jobject result;
    result = (*env)->AllocObject(env, clazz);
    if (result != NULL) {
        result = (*env)->NewGlobalRef(env, result);
    } else {
        fprintf(stderr, "rawInstanceOf returning NULL. Should be throwing an exception now.\n");
    }
    return result;
}

/*
JNIEXPORT jint JNICALL
getFieldID(JNIEnv* env, jobject me, jclass clazz, jstring fieldName, jstring sig) {
    const char* cname;
    const char* csig;
    jfieldID result;

    if ((clazz == NULL) || (fieldName == NULL) || (sig == NULL)) {
        / * signal error * /
        signalerror(env, "Arguments must not be NULL");
        return 0;
    }
    cname = (*env)->GetStringUTFChars(env, fieldName, NULL);
    csig = (*env)->GetStringUTFChars(env, sig, NULL);
    if ((cname == NULL) || (csig == NULL)) {
        / * signal error * /
        signalerror(env, "Error converting string");
        return 0;
    }
    result = ((*env)->GetFieldID(env, clazz, cname, csig));
    if (result == NULL) {
        / * signal error * /
        signalerror(env, "Could not get fieldID");
        return 0;
    }
    (*env)->ReleaseStringUTFChars(env, fieldName, cname);
    (*env)->ReleaseStringUTFChars(env, sig, csig);
    return ~((jint)result);
}
*/
