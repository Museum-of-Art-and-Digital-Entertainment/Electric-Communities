/*
  ejclassfile.c -- Class file reader for the E-to-Java translator

  Chip Morningstar
  Electric Communities
  25-July-1997

  Copyright 1997 Electric Communities, all rights reserved worldwide.

*/

#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "ej.h"

#define CONSTANT_Class                   7
#define CONSTANT_Double                  6
#define CONSTANT_Fieldref                9
#define CONSTANT_Float                   4
#define CONSTANT_Integer                 3
#define CONSTANT_InterfaceMethodref     11
#define CONSTANT_Long                    5
#define CONSTANT_Methodref              10
#define CONSTANT_NameAndType            12
#define CONSTANT_String                  8
#define CONSTANT_Utf8                    1

#define ACC_PUBLIC       0x0001
#define ACC_PRIVATE      0x0002
#define ACC_PROTECTED    0x0004
#define ACC_STATIC       0x0008
#define ACC_FINAL        0x0010
#define ACC_SUPER        0x0020
#define ACC_SYNCHRONIZED 0x0020
#define ACC_VOLATILE     0x0040
#define ACC_TRANSIENT    0x0080
#define ACC_NATIVE       0x0100
#define ACC_INTERFACE    0x0200
#define ACC_ABSTRACT     0x0400

YT(attribute_info)  *readAttributeInfo(FILE *fyle);
YT(attribute_info) **readAttributes(FILE *fyle, int count);
YT(classFile)       *readClassFile(FILE *fyle);
byte                 readByte(FILE *fyle);
byte                *readByteArray(FILE *fyle, int length);
YT(cp_info)        **readConstantPool(FILE *fyle, int count);
YT(cp_info)         *readConstantPoolInfo(FILE *fyle);
YT(field_info)      *readFieldInfo(FILE *fyle);
YT(field_info)     **readFields(FILE *fyle, int count);
long                 readLong(FILE *fyle);
YT(method_info)     *readMethodInfo(FILE *fyle);
YT(method_info)    **readMethods(FILE *fyle, int count);
word                 readWord(FILE *fyle);
word                *readWordArray(FILE *fyle, int length);

void freeClassFile(YT(classFile) *cf);
void freeConstantPoolInfo(YT(cp_info) *cp);
void freeFieldInfo(YT(field_info) *fi);
void freeMethodInfo(YT(method_info) *mi);

void dumpAttributeInfo(YT(attribute_info) *ai);
void dumpClassFile(YT(classFile) *cf);
void dumpConstantPoolInfo(YT(cp_info) *cp);
void dumpFieldInfo(YT(field_info) *fi);
void dumpMethodInfo(YT(method_info) *mi);

#ifdef WRONG_ENDIAN
  void
reverseBytes(char *data, int length)
{
    char temp;
    int i;

    for (i=0; i<length/2; ++i) {
        temp = data[i];
        data[i] = data[length-1-i];
        data[length-1-i] = temp;
    }
}
#endif

  char *
getMethodDescriptor(YT(classFile) *cf, int index)
{
    static char result[BUFLEN];

    YT(method_info) *methodInfo = cf->methods[index];
    YT(cp_info) *descriptorInfo =
        cf->constant_pool[methodInfo->descriptor_index];
    if (YTAG_TEST(descriptorInfo, constant_utf8_info)) {
        YT(constant_utf8_info) *utf = YC(constant_utf8_info, descriptorInfo);
        strncpy(result, utf->bytes, utf->length);
        result[utf->length] = '\0';
    } else {
        strcpy(result, "<????>");
    }
    return(result);
}

  char *
getMethodName(YT(classFile) *cf, int index)
{
    static char result[BUFLEN];

    YT(method_info) *methodInfo = cf->methods[index];
    YT(cp_info) *nameInfo = cf->constant_pool[methodInfo->name_index];
    if (YTAG_TEST(nameInfo, constant_utf8_info)) {
        YT(constant_utf8_info) *utf = YC(constant_utf8_info, nameInfo);
        strncpy(result, utf->bytes, utf->length);
        result[utf->length] = '\0';
    } else {
        strcpy(result, "<????>");
    }
    return(result);
}

  YT(attribute_info) *
readAttributeInfo(FILE *fyle)
{
    word attribute_name_index = readWord(fyle);
    long attribute_length = readLong(fyle);
    byte *info = readByteArray(fyle, attribute_length);

    return(YBUILD(attribute_info)(attribute_name_index, attribute_length,
                                  info));
}

  YT(attribute_info) **
readAttributes(FILE *fyle, int count)
{
    YT(attribute_info) **result = TypeAllocMulti(YT(attribute_info) *, count);
    int i;
    for (i=0; i<count; ++i)
        result[i] = readAttributeInfo(fyle);
    return(result);
}

  YT(classFile) *
readClassFile(FILE *fyle)
{
    long magic = readLong(fyle);
    word minor_version = readWord(fyle);
    word major_version = readWord(fyle);
    word constant_pool_count = readWord(fyle);
    YT(cp_info) **constant_pool = readConstantPool(fyle, constant_pool_count);
    word access_flags = readWord(fyle);
    word this_class = readWord(fyle);
    word super_class = readWord(fyle);
    word interfaces_count = readWord(fyle);
    word *interfaces = readWordArray(fyle, interfaces_count);
    word fields_count = readWord(fyle);
    YT(field_info) **fields = readFields(fyle, fields_count);
    word methods_count = readWord(fyle);
    YT(method_info) **methods = readMethods(fyle, methods_count);
    word attributes_count = readWord(fyle);
    YT(attribute_info) **attributes = readAttributes(fyle, attributes_count);

    return(YBUILD(classFile)(magic, minor_version, major_version,
        constant_pool_count, constant_pool, access_flags, this_class,
        super_class, interfaces_count, interfaces, fields_count, fields,
        methods_count, methods, attributes_count, attributes));
}

  byte
readByte(FILE *fyle)
{
    byte result;
    fread((char *)&result, 1, 1, fyle);
    return(result);
}

  byte *
readByteArray(FILE *fyle, int length)
{
    byte *result = TypeAllocMulti(byte, length);
    fread((char *)result, 1, length, fyle);
    return(result);
}

  YT(cp_info) **
readConstantPool(FILE *fyle, int count)
{
    YT(cp_info) **result = TypeAllocMulti(YT(cp_info) *, count);
    int i;
    result[0] = NULL;
    for (i=1; i<count; ++i)
        result[i] = readConstantPoolInfo(fyle);
    return(result);
}

  YT(cp_info) *
readConstantPoolInfo(FILE *fyle)
{
    byte tag = readByte(fyle);
    switch (tag) {
        case CONSTANT_Class:{
            word name_index = readWord(fyle);
            return(YC(cp_info, YBUILD(constant_class_info)(name_index)));
        }
        case CONSTANT_Fieldref:{
            word class_index = readWord(fyle);
            word name_and_type_index = readWord(fyle);
            return(YC(cp_info, YBUILD(constant_fieldref_info)(
                class_index, name_and_type_index)));
        }
        case CONSTANT_Methodref:{
            word class_index = readWord(fyle);
            word name_and_type_index = readWord(fyle);
            return(YC(cp_info, YBUILD(constant_methodref_info)(
                class_index, name_and_type_index)));
        }
        case CONSTANT_InterfaceMethodref:{
            word class_index = readWord(fyle);
            word name_and_type_index = readWord(fyle);
            return(YC(cp_info, YBUILD(constant_interfaceMethodref_info)(
                class_index, name_and_type_index)));
        }
        case CONSTANT_String:{
            word string_index = readWord(fyle);
            return(YC(cp_info, YBUILD(constant_string)(string_index)));
        }
        case CONSTANT_Integer:{
            long bytes = readLong(fyle);
            return(YC(cp_info, YBUILD(constant_integer)(bytes)));
        }
        case CONSTANT_Float:{
            long bytes = readLong(fyle);
            return(YC(cp_info, YBUILD(constant_float)(bytes)));
        }
        case CONSTANT_Long:{
            long high_bytes = readLong(fyle);
            long low_bytes = readLong(fyle);
            return(YC(cp_info, YBUILD(constant_long)(high_bytes, low_bytes)));
        }
        case CONSTANT_Double:{
            long high_bytes = readLong(fyle);
            long low_bytes = readLong(fyle);
            return(YC(cp_info,YBUILD(constant_double)(high_bytes, low_bytes)));
        }
        case CONSTANT_NameAndType:{
            word name_index = readWord(fyle);
            word descriptor_index = readWord(fyle);
            return(YC(cp_info, YBUILD(constant_nameAndType_info)(
                name_index, descriptor_index)));
        }
        case CONSTANT_Utf8:{
            word length = readWord(fyle);
            byte *bytes = readByteArray(fyle, length);
            return(YC(cp_info, YBUILD(constant_utf8_info)(length, bytes)));
        }
        default:
            yh_error("invalid constant pool tag %d", tag);
    }
    return(NULL);
}

  YT(field_info) *
readFieldInfo(FILE *fyle)
{
    word access_flags = readWord(fyle);
    word name_index = readWord(fyle);
    word descriptor_index = readWord(fyle);
    word attributes_count = readWord(fyle);
    YT(attribute_info) **attributes = readAttributes(fyle, attributes_count);

    return(YBUILD(field_info)(access_flags, name_index, descriptor_index,
                              attributes_count, attributes));
}

  YT(field_info) **
readFields(FILE *fyle, int count)
{
    YT(field_info) **result = TypeAllocMulti(YT(field_info) *, count);
    int i;
    for (i=0; i<count; ++i)
        result[i] = readFieldInfo(fyle);
    return(result);
}

  long
readLong(FILE *fyle)
{
    long result;
    fread((char *)&result, 4, 1, fyle);
    return(result);
}

  YT(method_info) *
readMethodInfo(FILE *fyle)
{
    word access_flags = readWord(fyle);
    word name_index = readWord(fyle);
    word descriptor_index = readWord(fyle);
    word attributes_count = readWord(fyle);
    YT(attribute_info) **attributes = readAttributes(fyle, attributes_count);

    return(YBUILD(method_info)(access_flags, name_index, descriptor_index,
                               attributes_count, attributes));
}

  YT(method_info) **
readMethods(FILE *fyle, int count)
{
    YT(method_info) **result = TypeAllocMulti(YT(method_info) *, count);
    int i;
    for (i=0; i<count; ++i)
        result[i] = readMethodInfo(fyle);
    return(result);
}

  word
readWord(FILE *fyle)
{
    word result;
    fread((char *)&result, 2, 1, fyle);
#ifdef WRONG_ENDIAN
    reverseBytes((char *)&result, 2);
#endif
    return(result);
}

  word *
readWordArray(FILE *fyle, int length)
{
    word *result = TypeAllocMulti(word, length);
    fread((char *)result, 2, length, fyle);
#ifdef WRONG_ENDIAN
    {
        int i;
        for (i=0; i<length; ++i)
            reverseBytes((char *)&result[i], 2);
    }
#endif
    return(result);
}

  void
freeClassFile(YT(classFile) *cf)
{
    int i;
    for (i=0; i<cf->constant_pool_count; ++i)
        freeConstantPoolInfo(cf->constant_pool[i]);
    for (i=0; i<cf->fields_count; ++i)
        freeFieldInfo(cf->fields[i]);
    for (i=0; i<cf->methods_count; ++i)
        freeMethodInfo(cf->methods[i]);
    for (i=0; i<cf->attributes_count; ++i)
        YH_FREE(attribute_info, cf->attributes[i]);
    YH_FREE(classFile, cf);
}

  void
freeConstantPoolInfo(YT(cp_info) *cp)
{
    if (cp == NULL)
        return;
    switch (YTAG_OF(cp)) {
        case YTAG(constant_class_info):
            YH_FREE(constant_class_info,cp);
            break;
        case YTAG(constant_fieldref_info):
            YH_FREE(constant_fieldref_info,cp);
            break;
        case YTAG(constant_methodref_info):
            YH_FREE(constant_methodref_info,cp);
            break;
        case YTAG(constant_interfaceMethodref_info):
            YH_FREE(constant_interfaceMethodref_info,cp);
            break;
        case YTAG(constant_string):
            YH_FREE(constant_string,cp);
            break;
        case YTAG(constant_integer):
            YH_FREE(constant_integer,cp);
            break;
        case YTAG(constant_float):
            YH_FREE(constant_float,cp);
            break;
        case YTAG(constant_long):
            YH_FREE(constant_long,cp);
            break;
        case YTAG(constant_double):
            YH_FREE(constant_double,cp);
            break;
        case YTAG(constant_nameAndType_info):
            YH_FREE(constant_nameAndType_info,cp);
            break;
        case YTAG(constant_utf8_info):
            YH_FREE(constant_utf8_info,cp);
            break;
        default:
            yh_error("invalid constant pool tag %d", YTAG_OF(cp));
    }
}

  void
freeFieldInfo(YT(field_info) *fi)
{
    int i;
    for (i=0; i<fi->attributes_count; ++i)
        YH_FREE(attribute_info, fi->attributes[i]);
    YH_FREE(field_info, fi);
}

  void
freeMethodInfo(YT(method_info) *mi)
{
    int i;
    for (i=0; i<mi->attributes_count; ++i)
        YH_FREE(attribute_info, mi->attributes[i]);
    YH_FREE(method_info, mi);
}

  void
dumpAttributeInfo(YT(attribute_info) *ai)
{
    eprintf("name: %d  length: %d\n",
            ai->attribute_name_index,
            ai->attribute_length);
}

  void
dumpClassFile(YT(classFile) *cf)
{
    int i;
    eprintf("magic: %x  version: %d/%d\n",
            cf->magic, cf->minor_version, cf->major_version);
    eprintf("%d constants:\n", cf->constant_pool_count);
    for (i=0; i<cf->constant_pool_count; ++i) {
        eprintf("  [%d]:", i);
        dumpConstantPoolInfo(cf->constant_pool[i]);
    }
    eprintf("flags: %04x  thisClass: %d  superClass: %d\n",
            cf->access_flags, cf->this_class, cf->super_class);
    eprintf("%d interfaces:\n", cf->interfaces_count);
    for (i=0; i<cf->interfaces_count; ++i) 
        eprintf("  [%d]: %d\n", i, cf->interfaces[i]);
    eprintf("%d fields:\n", cf->fields_count);
    for (i=0; i<cf->fields_count; ++i) {
        eprintf("  [%d]:", i);
        dumpFieldInfo(cf->fields[i]);
    }
    eprintf("%d methods:\n", cf->methods_count);
    for (i=0; i<cf->methods_count; ++i) {
        eprintf("  [%d]:", i);
        dumpMethodInfo(cf->methods[i]);
    }
    eprintf("%d attributes:\n", cf->attributes_count);
    for (i=0; i<cf->attributes_count; ++i) {
        eprintf("  [%d]:", i);
        dumpAttributeInfo(cf->attributes[i]);
    }
}

  void
dumpConstantPoolInfo(YT(cp_info) *cp)
{
    if (cp == NULL) {
        eprintf("<null>\n");
        return;
    }
    switch (YTAG_OF(cp)) {
        case YTAG(constant_class_info):{
            YT(constant_class_info) *info = YC(constant_class_info,cp);
            eprintf("class:: name: %d\n", info->name_index);
            break;
        }
        case YTAG(constant_fieldref_info):{
            YT(constant_fieldref_info) *info = YC(constant_fieldref_info,cp);
            eprintf("fieldref:: class: %d  nameAndType: %d\n",
                    info->class_index, info->name_and_type_index);
            break;
        }
        case YTAG(constant_methodref_info):{
            YT(constant_methodref_info) *info = YC(constant_methodref_info,cp);
            eprintf("methodref:: class: %d  nameAndType: %d\n",
                    info->class_index, info->name_and_type_index);
            break;
        }
        case YTAG(constant_interfaceMethodref_info):{
            YT(constant_interfaceMethodref_info) *info =
                YC(constant_interfaceMethodref_info,cp);
            eprintf("interfaceMethodref:: class: %d  nameAndType: %d\n",
                    info->class_index, info->name_and_type_index);
            break;
        }
        case YTAG(constant_string):{
            YT(constant_string) *info = YC(constant_string,cp);
            eprintf("string:: index: %d\n", info->string_index);
            break;
        }
        case YTAG(constant_integer):{
            YT(constant_integer) *info = YC(constant_integer,cp);
            eprintf("integer:: bytes: %08x (%d)\n", info->bytes, info->bytes);
            break;
        }
        case YTAG(constant_float):{
            YT(constant_float) *info = YC(constant_float,cp);
            eprintf("float:: bytes: %08x (%f)\n",
                    info->bytes, (double) *(float *)&info->bytes);
            break;
        }
        case YTAG(constant_long):{
            YT(constant_long) *info = YC(constant_long,cp);
            eprintf("long:: bytes: %08x%08x\n",
                    info->high_bytes, info->low_bytes);
            break;
        }
        case YTAG(constant_double):{
            YT(constant_double) *info = YC(constant_double,cp);
            eprintf("double:: bytes: %08x%08x (%f)\n",
                    info->high_bytes, info->low_bytes,
                    *(double *)&info->high_bytes);
            break;
        }
        case YTAG(constant_nameAndType_info):{
            YT(constant_nameAndType_info) *info =
                YC(constant_nameAndType_info,cp);
            eprintf("nameAndType:: name: %d  descriptor: %d\n",
                    info->name_index, info->descriptor_index);
            break;
        }
        case YTAG(constant_utf8_info):{
            YT(constant_utf8_info) *info = YC(constant_utf8_info,cp);
            int i;
            eprintf("utf8:: '");
            for (i=0; i<info->length; ++i)
                eprintf("%c", info->bytes[i]);
            eprintf("'\n");
            break;
        }
        default:
            yh_error("invalid constant pool tag %d", YTAG_OF(cp));
    }
}

  void
dumpFieldInfo(YT(field_info) *fi)
{
    int i;
    eprintf("flags: %04x  name: %d  descriptor: %d,  %d attributes:\n",
            fi->access_flags, fi->name_index, fi->descriptor_index,
            fi->attributes_count);
    for (i=0; i<fi->attributes_count; ++i) {
        eprintf("    [%d]:", i);
        dumpAttributeInfo(fi->attributes[i]);
    }
}

  void
dumpMethodInfo(YT(method_info) *mi)
{
    int i;
    eprintf("flags: %04x  name: %d  descriptor: %d,  %d attributes:\n",
            mi->access_flags, mi->name_index, mi->descriptor_index,
            mi->attributes_count);
    for (i=0; i<mi->attributes_count; ++i) {
        eprintf("    [%d]:", i);
        dumpAttributeInfo(mi->attributes[i]);
    }
}
