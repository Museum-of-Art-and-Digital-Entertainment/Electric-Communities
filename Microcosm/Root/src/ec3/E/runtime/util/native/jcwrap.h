#ifndef EC_H
#define EC_H

#define STRING(EXP) ""#EXP""

#ifndef USEJNI    // {

/* I don't know why these typedef's don't work, but the compiler doesn't
 * like the "*" tokens

typedef char                      jbyte;
typedef long                      jint;
typedef long                      jsize;
typedef float                     jfloat;
typedef HArrayOfByte*             jbyteArray;
typedef HArrayOfShort*            jshortArray;
typedef HArrayOfInt*              jintArray;
typedef HArrayOfFloat*            jfloatArray;
typedef struct Hjava_lang_String* jstring;
*/

#define jbyte       char
#define jint        long
#define jsize       long
#define jfloat      float
#define jbyteArray  HArrayOfByte*
#define jshortArray HArrayOfShort*
#define jintArray   HArrayOfInt*
#define jfloatArray HArrayOfFloat*
#define jstring     struct Hjava_lang_String*

#define JNIEXPORT
#define JNICALL
#define JAVA_PARAMS(_class) struct h_package(_class) * self

#define GET_arrayLength(a) (obj_length(a))

#define GET_jbyte(o, n)          (unhand(o)->n)
#define GET_jshort(o, n)         (unhand(o)->n)
#define GET_jint(o, n)           (unhand(o)->n)
#define GET_jfloat(o, n)         (unhand(o)->n)
#define GET_jstring(o, n)        (unhand(o)->n)
#define GET_jobjectElement(a, i) (a[i])

#define SET_jbyte(o, n, v)   (unhand(o)->n = v)
#define SET_jshort(o, n, v)  (unhand(o)->n = v)
#define SET_jint(o, n, v)    (unhand(o)->n = v)
#define SET_jfloat(o, n, v)  (unhand(o)->n = v)
#define SET_jstring(o, n, v) (XXX not yet implemented XXX)

#define GET_jbyteArray(o, n)  (unhand(o)->n)
#define GET_jshortArray(o, n) (unhand(o)->n)
#define GET_jintArray(o, n)   (unhand(o)->n)
#define GET_jfloatArray(o, n) (unhand(o)->n)

#define GET_jbyteElements(a)  (unhand(a)->body)
#define GET_jshortElements(a) (unhand(a)->body)
#define GET_jintElements(a)   (unhand(a)->body)
#define GET_jfloatElements(a) (unhand(a)->body)
#define GET_UTFChars(s)       (makeCString(s))

#define RELEASE_jbyteElements(a, e)
#define RELEASE_jshortElements(a, e)
#define RELEASE_jintElements(a, e)
#define RELEASE_jfloatElements(a, e)
#define RELEASE_UTFChars(s, c) /* TODO: shouldn't this actually free something? */

#define THROW_EXCEPTION(e, m) SignalError(0, e, m);

#endif    // } ndef USEJNI

#ifdef USEJNI    // {

#include <jni.h>

#define JAVA_PARAMS(_class) JNIEnv *env, jclass obj
//#define JAVA_PARAMS JNIEnv *env, jobject obj

#ifdef __cplusplus    // {

#define GET_arrayLength(a) (env->GetArrayLength(a))

#define GET_FIELD_ID(o, n, TYPE) (env->GetFieldID(env->GetObjectClass(o), STRING(n), TYPE))

#define GET_jbyte(o, n)   (env->GetByteField  (o, GET_FIELD_ID(o, n, "B")))
#define GET_jshort(o, n)  (env->GetShortField (o, GET_FIELD_ID(o, n, "S")))
#define GET_jint(o, n)    (env->GetIntField   (o, GET_FIELD_ID(o, n, "I")))
#define GET_jfloat(o, n)  (env->GetFloatField (o, GET_FIELD_ID(o, n, "F")))
#define GET_jstring(o, n) ((jstring)(env->GetObjectField(o, GET_FIELD_ID(o, n, "Ljava/lang/String;"))))
//#define GET_jstring(o, n) ((jstring)env->GetObjectField(o, GET_FIELD_ID(o, n, "Ljava/lang/String;")))
#define GET_jobjectField(o, n, t) (env->GetObjectField(o, env->GetFieldID(env->GetObjectClass(o), n, t)))
#define GET_jobjectElement(a, i) (env->GetObjectArrayElement(a, i))

#define SET_jbyte(o, n, v)   (env->SetByteField  (o, GET_FIELD_ID(o, n, "B"), v))
#define SET_jshort(o, n, v)  (env->SetShortField (o, GET_FIELD_ID(o, n, "S"), v))
#define SET_jint(o, n, v)    (env->SetIntField   (o, GET_FIELD_ID(o, n, "I"), v))
#define SET_jfloat(o, n, v)  (env->SetFloatField (o, GET_FIELD_ID(o, n, "F"), v))
#define SET_jstring(o, n, v) (env->SetObjectField(o, GET_FIELD_ID(o, n, "Ljava/lang/String;"), v))

#define GET_jbyteArray(o, n)  ((jbyteArray) env->GetObjectField(o, GET_FIELD_ID(o, n, "[B")))
#define GET_jshortArray(o, n) ((jshortArray)env->GetObjectField(o, GET_FIELD_ID(o, n, "[S")))
#define GET_jintArray(o, n)   ((jintArray)  env->GetObjectField(o, GET_FIELD_ID(o, n, "[I")))
#define GET_jfloatArray(o, n) ((jfloatArray)env->GetObjectField(o, GET_FIELD_ID(o, n, "[F")))

#define GET_jbyteElements(a)  (env->GetByteArrayElements (a, NULL))
#define GET_jshortElements(a) (env->GetShortArrayElements(a, NULL))
#define GET_jintElements(a)   (env->GetIntArrayElements  (a, NULL))
#define GET_jfloatElements(a) (env->GetFloatArrayElements(a, NULL))
#define GET_UTFChars(s)       (env->GetStringUTFChars    (s, NULL));

#define RELEASE_jbyteElements(a, e)  (env->ReleaseByteArrayElements (a, e, 0))
#define RELEASE_jshortElements(a, e) (env->ReleaseShortArrayElements(a, e, 0))
#define RELEASE_jintElements(a, e)   (env->ReleaseIntArrayElements  (a, e, 0))
#define RELEASE_jfloatElements(a, e) (env->ReleaseFloatArrayElements(a, e, 0))
#define RELEASE_UTFChars(s, c)       (env->ReleaseStringUTFChars(s, c))

#define THROW_EXCEPTION(e, m) (env->ThrowNew(env->FindClass(e), m))

#define CALL_jintMethod(o, n, t, a) (env->CallIntMethod(o, env->GetMethodID(env->GetObjectClass(o), n, t), a))

#else    // } { __cplusplus

#define GET_arrayLength(a) ((*env)->GetArrayLength(env, a))

#define GET_FIELD_ID(o, n, TYPE) ((*env)->GetFieldID(env, (*env)->GetObjectClass(env, o), STRING(n), TYPE))

#define GET_jbyte(o, n)   ((*env)->GetByteField  (env, o, GET_FIELD_ID(o, n, "B")))
#define GET_jshort(o, n)  ((*env)->GetShortField (env, o, GET_FIELD_ID(o, n, "S")))
#define GET_jint(o, n)    ((*env)->GetIntField   (env, o, GET_FIELD_ID(o, n, "I")))
#define GET_jfloat(o, n)  ((*env)->GetFloatField (env, o, GET_FIELD_ID(o, n, "F")))
#define GET_jstring(o, n) ((*env)->GetObjectField(env, o, GET_FIELD_ID(o, n, "Ljava/lang/String;")))
//#define GET_jstring(o, n) ((jstring)env->GetObjectField(o, GET_FIELD_ID(o, n, "Ljava/lang/String;")))
#define GET_jobjectField(o, n, t) ((*env)->GetObjectField(env, o, (*env)->GetFieldID(env, (*env)->GetObjectClass(env, o), n, t)))
#define GET_jobjectElement(a, i) ((*env)->GetObjectArrayElement(env, a, i))

#define SET_jbyte(o, n, v)   ((*env)->SetByteField  (env, o, GET_FIELD_ID(o, n, "B"), v))
#define SET_jshort(o, n, v)  ((*env)->SetShortField (env, o, GET_FIELD_ID(o, n, "S"), v))
#define SET_jint(o, n, v)    ((*env)->SetIntField   (env, o, GET_FIELD_ID(o, n, "I"), v))
#define SET_jfloat(o, n, v)  ((*env)->SetFloatField (env, o, GET_FIELD_ID(o, n, "F"), v))
#define SET_jstring(o, n, v) ((*env)->SetObjectField(env, o, GET_FIELD_ID(o, n, "Ljava/lang/String;"), v))

#define GET_jbyteArray(o, n)  ((jbyteArray) (*env)->GetObjectField(env, o, GET_FIELD_ID(o, n, "[B")))
#define GET_jshortArray(o, n) ((jshortArray)(*env)->GetObjectField(env, o, GET_FIELD_ID(o, n, "[S")))
#define GET_jintArray(o, n)   ((jintArray)  (*env)->GetObjectField(env, o, GET_FIELD_ID(o, n, "[I")))
#define GET_jfloatArray(o, n) ((jfloatArray)(*env)->GetObjectField(env, o, GET_FIELD_ID(o, n, "[F")))

#define GET_jbyteElements(a)  ((*env)->GetByteArrayElements (env, a, NULL))
#define GET_jshortElements(a) ((*env)->GetShortArrayElements(env, a, NULL))
#define GET_jintElements(a)   ((*env)->GetIntArrayElements  (env, a, NULL))
#define GET_jfloatElements(a) ((*env)->GetFloatArrayElements(env, a, NULL))
#define GET_UTFChars(s)       ((*env)->GetStringUTFChars    (env, s, NULL));

#define RELEASE_jbyteElements(a, e)  ((*env)->ReleaseByteArrayElements (env, a, e, 0))
#define RELEASE_jshortElements(a, e) ((*env)->ReleaseShortArrayElements(env, a, e, 0))
#define RELEASE_jintElements(a, e)   ((*env)->ReleaseIntArrayElements  (env, a, e, 0))
#define RELEASE_jfloatElements(a, e) ((*env)->ReleaseFloatArrayElements(env, a, e, 0))
#define RELEASE_UTFChars(s, c)       ((*env)->ReleaseStringUTFChars    (env, s, c))

#define THROW_EXCEPTION(e, m) ((*env)->ThrowNew((*env)->FindClass(env, e), m))

#define CALL_jintMethod(o, n, t, a) ((*env)->CallIntMethod(env, o, (*env)->GetMethodID(env, (*env)->GetObjectClass(env, o), n, t), a))

#endif    // } __cplusplus

#endif    // } def USEJNI
    
#endif    // EC_H
