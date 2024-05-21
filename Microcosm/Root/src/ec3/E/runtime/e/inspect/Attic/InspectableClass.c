#include <StubPreamble.h>
#include "ec_e_inspect_InspectableClass.h"
#include <stdio.h>

struct Hjava_lang_String;
struct Hjava_lang_Object;

#define INSPECTORLIBVERSION (4)
#define HArrayOfBoolean HArrayOfInt

struct fieldblock* findField(struct Hjava_lang_Class *theClass,long n)
{
  ClassClass* c;
  struct fieldblock* field;
  int i;
  int count;

  for (; theClass != NULL; ) {
    c = (ClassClass*)unhand(theClass);
    if (c == NULL) break;
    field = c->fields;
    count = c->fields_count;
    for (i = 0; i < count; i++, field++)
      if (((field->access & ACC_STATIC) == 0) && (n-- == 0)) return field;
    theClass = c->superclass;
  }
  return NULL;
}

long ec_e_inspect_InspectableClass_inspectorNativeLibraryVersion(struct Hec_e_inspect_InspectableClass * this)
{
  long result = INSPECTORLIBVERSION;              /* Version of this library - checked at load time */
  return result;
}

long ec_e_inspect_InspectableClass_getNumberFields(struct Hec_e_inspect_InspectableClass * this,
                         struct Hjava_lang_Class * theClass)
{
  ClassClass* c;
  struct fieldblock* field;
  int i;
  int count;
  long result = 0;

  c = (ClassClass*)unhand(theClass);
  if (c == NULL) return 0;
  field = c->fields;
  count = c->fields_count;
  for (i = 0; i < count; i++, field++) if ((field->access & ACC_STATIC) == 0) result++;
  return result;
}

long ec_e_inspect_InspectableClass_getTotalNumberFields(struct Hec_e_inspect_InspectableClass * this,
                         struct Hjava_lang_Class * theClass)
{
  ClassClass* c;
  struct fieldblock* field;
  int i;
  int count;
  long result = 0;

  for (; theClass != NULL; ) {
    c = (ClassClass*)unhand(theClass);
    if (c == NULL) break;
    field = c->fields;
    count = c->fields_count;
    for (i = 0; i < count; i++, field++)
      if ((field->access & ACC_STATIC) == 0) result++;
    theClass = c->superclass;
  }
  return result;
}

long ec_e_inspect_InspectableClass_getFieldModifiers(struct Hec_e_inspect_InspectableClass * this,
                           struct Hjava_lang_Class * theClass,
                           long n)
{
  struct fieldblock* field = findField(theClass,n);

  if (field == NULL) return (long)0;
  return (long)(field->access);
}

struct Hjava_lang_String *ec_e_inspect_InspectableClass_getFieldName(struct Hec_e_inspect_InspectableClass * this,
                                   struct Hjava_lang_Class * theClass,
                                   long n)
{
  struct fieldblock* field = findField(theClass,n);

  if (field == NULL) return NULL;
  return makeJavaString(field->name, strlen(field->name));
}

struct Hjava_lang_String *ec_e_inspect_InspectableClass_getFieldSignature(struct Hec_e_inspect_InspectableClass * this,
                                   struct Hjava_lang_Class * theClass,
                                   long n)
{
  struct fieldblock* field = findField(theClass,n);

  if (field == NULL) return NULL;
  return makeJavaString(field->signature, strlen(field->signature));
}

Hjava_lang_Object * constructor_InspectableObject(char *valuep, char signatureChar) {
  long result;
  ClassClass *cb;
  char sigbuf[] = "( )Ljava/lang/Object;";      /* Returns an object */

  sigbuf[1] = signatureChar;                /* Create a signature for the methods arguments */
  cb = FindClass(0, "ec/e/inspect/InspectableClass", TRUE );  /* Get the classblock for InspectableClass */
  if (cb == NULL) {
    fprintf(stderr,"Could not find constructor with signature %s to wrap value %lx\n", sigbuf, *(long*)valuep);
    return NULL;
  }

  result = execute_java_static_method(0,        /* Use current environment */
                      cb,       /* Class block for class method lives in */
                    "getInspectableObject", /* Method name */
                    sigbuf,     /* Signature of method */
                   *(long*)valuep);     /* Argument  to constructor */
  if (result == 0L)
    fprintf(stderr,"Wrapping object with signature char '%c', and value %lx using method with signature %s returned a NULL!\n",
                  signatureChar,*(long*)valuep,sigbuf);
  return (Hjava_lang_Object *)result;
}

Hjava_lang_Object * constructor_InspectableObjectFloat(float value, char signatureChar) {
  long result;
  ClassClass *cb;
  char sigbuf[] = "( )Ljava/lang/Object;";      /* Returns an object */

  sigbuf[1] = signatureChar;                /* Create a signature for the methods arguments */
  cb = FindClass(0, "ec/e/inspect/InspectableClass", TRUE );  /* Get the classblock for InspectableClass */
  if (cb == NULL) {
    fprintf(stderr,"Could not find constructor with signature %s to wrap float value %f\n", sigbuf, value);
    return NULL;
  }

  result = execute_java_static_method(0,        /* Use current environment */
                      cb,       /* Class block for class method lives in */
                      "getInspectableObject", /* Method name */
                      sigbuf,       /* Signature of method */
                      value);       /* Argument  to constructor */
  return (Hjava_lang_Object *)result;
}

Hjava_lang_Object * constructor_InspectableObjectInt64t(char *valuep, char signatureChar) {
  long result;
  ClassClass *cb;
  char sigbuf[] = "( )Ljava/lang/Object;";      /* Returns an object */

  sigbuf[1] = signatureChar;                /* Create a signature for the methods arguments */
  cb = FindClass(0, "ec/e/inspect/InspectableClass", TRUE );  /* Get the classblock for InspectableClass */
  if (cb == NULL) {
    fprintf(stderr,"Could not find constructor with signature %s to wrap 64-bit value at address %lx\n",
        sigbuf, (long)valuep);
    return NULL;
  }

  result = execute_java_static_method(0,        /* Use current environment */
                      cb,       /* Class block for class method lives in */
                    "getInspectableObject", /* Method name */
                    sigbuf,     /* Signature of method */
                   *((int64_t*)valuep));    /* Argument  to constructor */
  return (Hjava_lang_Object *)result;
}

struct Hjava_lang_Object
*ec_e_inspect_InspectableClass_getFieldValue(struct Hec_e_inspect_InspectableClass * this,
                       struct Hjava_lang_Class * theClass,
                       struct Hjava_lang_Object * theObject,
                       long n) {
  struct fieldblock* field = findField(theClass,n);
  char *p;
  Hjava_lang_Object *result = NULL;

  if (field == NULL) return NULL;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */

  if (field->access & ACC_STATIC) {
    result = NULL;
    /*    result = (Hjava_lang_Object *) field->u.static_value; */
  } else {
    switch (field->signature[0]) {
    case SIGNATURE_ARRAY:
    case SIGNATURE_CLASS: result = *(Hjava_lang_Object **) p; break; /* Already a reference type, just return it */

    case SIGNATURE_LONG:
    case SIGNATURE_DOUBLE:
      /*      fprintf(stderr,"Wrapping long/double with signature %s and value (as bits) %lx\n",
          field->signature,*(long*)p); /* */
      result = constructor_InspectableObjectInt64t(p,field->signature[0]); break; /* Wrap 8-byte primitive value in Object */
    case SIGNATURE_FLOAT:   /* I need to treat this case specially. Don't know why, which bothers me. */
      /*      fprintf(stderr,"Wrapping float with signature %s and value %f\n",
          field->signature,*(float*)p); /* */
      result = constructor_InspectableObjectFloat(*((float*)p),field->signature[0]); /* Wrap 4-byte float value in Java object */
      break;
    default:
      /* fprintf(stderr,"Wrapping primitive data with signature %s and value %lx\n",field->signature,*(long*)p); /* */
      result = constructor_InspectableObject(p,field->signature[0]); /* Wrap 4-byte primitive values in Java object */
    }
  }
  return result;
}

struct HArrayOfObject
*ec_e_inspect_InspectableClass_getFieldArrayValue(struct Hec_e_inspect_InspectableClass * this,
                        struct Hjava_lang_Class * theClass,
                        struct Hjava_lang_Object * theObject,
                        long n)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  HArrayOfObject *result = NULL;

  if (field == NULL) return NULL;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */

  if (field->access & ACC_STATIC) {
    result = NULL;
    /*    result = (HArrayOfObject *) field->u.static_value; */
  } else {
    switch (field->signature[0]) {
    case SIGNATURE_ARRAY:
    case SIGNATURE_CLASS:
      result = *(HArrayOfObject **) p;          /* Data is already a reference type - just return it */
      break;

    default:   ;
    }
  }
  return result;
}

struct HArrayOfBoolean *
ec_e_inspect_InspectableClass_getFieldBooleanArrayValue(struct Hec_e_inspect_InspectableClass * this,
                               struct Hjava_lang_Class * theClass,
                               struct Hjava_lang_Object * theObject,
                               long n)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  HArrayOfBoolean *result = NULL;

  if (field == NULL) return NULL;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */

  if (field->access & ACC_STATIC) {
    result = NULL;
    /*    result = (HArrayOfBoolean *) field->u.static_value; */
  } else {
    switch (field->signature[0]) {
    case SIGNATURE_ARRAY:
    case SIGNATURE_CLASS:
      result = *(HArrayOfBoolean **) (void*) p;         /* Data is already a reference type - just return it */
      break;

    default:   ;
    }
  }
  return result;
}

struct HArrayOfByte *ec_e_inspect_InspectableClass_getFieldByteArrayValue(struct Hec_e_inspect_InspectableClass * this,
                                   struct Hjava_lang_Class * theClass,
                                    struct Hjava_lang_Object * theObject,
                                   long n)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  HArrayOfByte *result = NULL;

  if (field == NULL) return NULL;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */

  if (field->access & ACC_STATIC) {
    result = NULL;
    /*    result = (HArrayOfByte *) field->u.static_value; */
  } else {
    switch (field->signature[0]) {
    case SIGNATURE_ARRAY:
    case SIGNATURE_CLASS:
      result = *(HArrayOfByte **) (void*) p;            /* Data is already a reference type - just return it */
      break;
      
    default:   ;
    }
  }
  return result;
}

struct HArrayOfChar *ec_e_inspect_InspectableClass_getFieldCharArrayValue(struct Hec_e_inspect_InspectableClass * this,
                                   struct Hjava_lang_Class * theClass,
                                    struct Hjava_lang_Object * theObject,
                                   long n)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  HArrayOfChar *result = NULL;

  if (field == NULL) return NULL;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */

  if (field->access & ACC_STATIC) {
    result = NULL;
    /*    result = (HArrayOfChar *) field->u.static_value; */
  } else {
    switch (field->signature[0]) {
    case SIGNATURE_ARRAY:
    case SIGNATURE_CLASS:
      result = *(HArrayOfChar **) (void*) p;            /* Data is already a reference type - just return it */
      break;

    default:   ;
    }
  }
  return result;
}

struct HArrayOfShort *ec_e_inspect_InspectableClass_getFieldShortArrayValue(struct Hec_e_inspect_InspectableClass * this,
                                   struct Hjava_lang_Class * theClass,
                                    struct Hjava_lang_Object * theObject,
                                   long n)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  HArrayOfShort *result = NULL;

  if (field == NULL) return NULL;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */

  if (field->access & ACC_STATIC) {
    result = NULL;
    /*    result = (HArrayOfShort *) field->u.static_value; */
  } else {
    switch (field->signature[0]) {
    case SIGNATURE_ARRAY:
    case SIGNATURE_CLASS:
      result = *(HArrayOfShort **) (void*) p;           /* Data is already a reference type - just return it */
      break;
      
    default:   ;
    }
  }
  return result;
}

struct HArrayOfInt *ec_e_inspect_InspectableClass_getFieldIntArrayValue(struct Hec_e_inspect_InspectableClass * this,
                                   struct Hjava_lang_Class * theClass,
                                    struct Hjava_lang_Object * theObject,
                                   long n)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  HArrayOfInt *result = NULL;

  if (field == NULL) return NULL;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */

  if (field->access & ACC_STATIC) {
    result = NULL;
    /*    result = (HArrayOfInt *) field->u.static_value; */
  } else {
    switch (field->signature[0]) {
    case SIGNATURE_ARRAY:
    case SIGNATURE_CLASS:
      result = *(HArrayOfInt **) (void*) p;         /* Data is already a reference type - just return it */
      break;

    default:   ;
    }
  }
  return result;
}

struct HArrayOfLong *ec_e_inspect_InspectableClass_getFieldLongArrayValue(struct Hec_e_inspect_InspectableClass * this,
                                   struct Hjava_lang_Class * theClass,
                                    struct Hjava_lang_Object * theObject,
                                   long n)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  HArrayOfLong *result = NULL;

  if (field == NULL) return NULL;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */

  if (field->access & ACC_STATIC) {
    result = NULL;
    /*    result = (HArrayOfLong *) field->u.static_value; */
  } else {
    switch (field->signature[0]) {
    case SIGNATURE_ARRAY:
    case SIGNATURE_CLASS:
      result = *(HArrayOfLong **) (void*) p;            /* Data is already a reference type - just return it */
      break;

    default:   ;
    }
  }
  return result;
}

struct HArrayOfFloat *ec_e_inspect_InspectableClass_getFieldFloatArrayValue(struct Hec_e_inspect_InspectableClass * this,
                                   struct Hjava_lang_Class * theClass,
                                    struct Hjava_lang_Object * theObject,
                                   long n)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  HArrayOfFloat *result = NULL;

  if (field == NULL) return NULL;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */

  if (field->access & ACC_STATIC) {
    result = NULL;
    /*    result = (HArrayOfFloat *) field->u.static_value; */
  } else {
    switch (field->signature[0]) {
    case SIGNATURE_ARRAY:
    case SIGNATURE_CLASS:
      result = *(HArrayOfFloat **) (void*) p;           /* Data is already a reference type - just return it */
      break;
      
    default:   ;
    }
  }
  return result;
}

struct HArrayOfDouble *ec_e_inspect_InspectableClass_getFieldDoubleArrayValue(struct Hec_e_inspect_InspectableClass * this,
                                   struct Hjava_lang_Class * theClass,
                                    struct Hjava_lang_Object * theObject,
                                   long n)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  HArrayOfDouble *result = NULL;

  if (field == NULL) return NULL;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */

  if (field->access & ACC_STATIC) {
    result = NULL;
    /*    result = (HArrayOfDouble *) field->u.static_value; */
  } else {
    switch (field->signature[0]) {
    case SIGNATURE_ARRAY:
    case SIGNATURE_CLASS:
      result = *(HArrayOfDouble **) (void*) p;          /* Data is already a reference type - just return it */
      break;

    default:   ;
    }
  }
  return result;
}

void ec_e_inspect_InspectableClass_setFieldValue(struct Hec_e_inspect_InspectableClass * this,
                           struct Hjava_lang_Class * theClass,
                           struct Hjava_lang_Object * theObject,
                           long n,
                           struct Hjava_lang_Object * newValue)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  struct Hjava_lang_Object **dp;

  if (field == NULL) return;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */
  dp = (struct Hjava_lang_Object **) p;

  if (field->access & ACC_STATIC) return; /* Can't set static data, can we? */
  *dp = newValue;
}

void ec_e_inspect_InspectableClass_setBooleanFieldValue(struct Hec_e_inspect_InspectableClass * this,
                           struct Hjava_lang_Class * theClass,
                           struct Hjava_lang_Object * theObject,
                           long n,
                           long newValue)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  long *dp;

  if (field == NULL) return;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */
  dp = (long*) p;

  if (field->access & ACC_STATIC) return; /* Can't modify static data */
  *dp = newValue;
}

void ec_e_inspect_InspectableClass_setByteFieldValue(struct Hec_e_inspect_InspectableClass * this,
                           struct Hjava_lang_Class * theClass,
                           struct Hjava_lang_Object * theObject,
                           long n,
                           char newValue)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;

  if (field == NULL) return;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */

  if (field->access & ACC_STATIC) return; /* Can't modify static data */
  *p = (char)newValue;
}

void ec_e_inspect_InspectableClass_setCharFieldValue(struct Hec_e_inspect_InspectableClass * this,
                           struct Hjava_lang_Class * theClass,
                           struct Hjava_lang_Object * theObject,
                           long n,
                           unicode newValue)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  unicode *dp;

  if (field == NULL) return;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */
  dp = (unicode*) p;

  if (field->access & ACC_STATIC) return; /* Can't modify static data */
  *dp = newValue;
}

void ec_e_inspect_InspectableClass_setShortFieldValue(struct Hec_e_inspect_InspectableClass * this,
                           struct Hjava_lang_Class * theClass,
                           struct Hjava_lang_Object * theObject,
                           long n,
                           short newValue)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  short *dp;

  if (field == NULL) return;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */
  dp = (short*) p;

  if (field->access & ACC_STATIC) return; /* Can't modify static data */
  *dp = newValue;
}

void ec_e_inspect_InspectableClass_setIntFieldValue(struct Hec_e_inspect_InspectableClass * this,
                           struct Hjava_lang_Class * theClass,
                           struct Hjava_lang_Object * theObject,
                           long n,
                           long newValue)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  long *dp;

  if (field == NULL) return;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */
  dp = (long*) p;

  if (field->access & ACC_STATIC) return; /* Can't modify static data */
  *dp = newValue;
}

void ec_e_inspect_InspectableClass_setLongFieldValue(struct Hec_e_inspect_InspectableClass * this,
                           struct Hjava_lang_Class * theClass,
                           struct Hjava_lang_Object * theObject,
                           long n,
                           int64_t newValue)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  unsigned int *dp;

  if (field == NULL) return;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */
  dp = (unsigned int *) p;

  if (field->access & ACC_STATIC) return; /* Can't modify static data */
  *((int64_t*)dp) = newValue;       /* Given newValue is in the same endianness as this assignment */
}

void ec_e_inspect_InspectableClass_setFloatFieldValue(struct Hec_e_inspect_InspectableClass * this,
                           struct Hjava_lang_Class * theClass,
                           struct Hjava_lang_Object * theObject,
                           long n,
                           float newValue)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  float *dp;

  if (field == NULL) return;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */
  dp = (float *) p;

  if (field->access & ACC_STATIC) return; /* Can't modify static data */
  *dp = newValue;
}

void ec_e_inspect_InspectableClass_setDoubleFieldValue(struct Hec_e_inspect_InspectableClass * this,
                           struct Hjava_lang_Class * theClass,
                           struct Hjava_lang_Object * theObject,
                           long n,
                           double newValue)
{
  struct fieldblock* field = findField(theClass,n);
  char *p;
  double *dp;

  if (field == NULL) return;
  p = ((char*) unhand(theObject)) + field->u.offset;    /* Byte pointer to the data */
  dp = (double *) p;

  if (field->access & ACC_STATIC) return; /* Can't modify static data */
  *dp = newValue;
}

