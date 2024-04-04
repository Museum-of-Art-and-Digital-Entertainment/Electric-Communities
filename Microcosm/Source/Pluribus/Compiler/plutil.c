/*
  plutil.c -- Utility routines for Pluribus.

  Chip Morningstar
  Electric Communities
  2-August-1996

  Copyright 1996 Electric Communities, all rights reserved.

*/

#include <ctype.h>

#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "pl.h"
#include "y.tab.h"
#include "platform.h"

static char *TagTable[BIND_LIMIT - BIND_TYPE] = {
    /* This table MUST be kept in synch with the BIND_xxxx enums defined for
       the bindingType field of YT(nameSpace) */
    "ty", /* type */
    "at", /* attribute */
    "cl", /* Java class */
    "in", /* Java interface */
    "ec", /* E class */
    "ei", /* E interface */
    "ki", /* kind */
    "ps", /* presence structure */
    "us", /* unum structure */
    "ii", /* ingredient impl */
    "pi", /* presence impl */
    "ui", /* unum impl */
    "un", /* unit */
};

char *typeSpecToString(YT(typeSpec) *typeSpec);

  int
bindingTypeFromTag(char *tag)
{
    int i;

    for (i=0; i<= BIND_UNIT; ++i)
        if (tag[0] == TagTable[i][0] && tag[1] == TagTable[i][1])
            return(i);
    return(-1);
}

  char *
bindingTypeToTag(int bindingType)
{
    if (bindingType < 0 || bindingType > BIND_UNIT)
        return("illegal binding type!!");
    else
        return(TagTable[bindingType]);
}

  bool
boolFromString(char *token)
{
    if (token) {
    if (strcasecmp("true", token) == 0 || strcasecmp("T", token) == 0)
        return TRUE;
    if (strcasecmp("false", token) == 0 || strcasecmp("F", token) == 0)
        return FALSE;
    yh_error("Invalid boolean string '%s'", token);
    return FALSE;
    }
    yh_error("Empty boolean string in boolFromString()");
    return FALSE;
}

  int
checkTypes(YT(typedValue) *lval, int op, YT(typedValue) *rval, char **msg)
{
    char *leftString, *rightString;
    int left = lval->typeCode, right = rval->typeCode;

    *msg = strdup("Spoooooon!");
    if (left != right) {
        if (((left != TV_CHAR) && (left != TV_LONG)) ||
            ((right != TV_CHAR) && (right != TV_LONG))) {
            *msg = strdup("Operand type mismatch");
            return TV_UND;
        }
    }
    switch (left) {
    case TV_TAG:
        *msg = strdup("Tags cannot be operated upon"); return TV_UND;
    case TV_BOOL:
        switch (op) {
        case  Eq: case Neq: case '&': case '^': case '|': case And: case  Or:
            return left;
        default : *msg = strdup("Operator type mismatch"); return TV_UND;
        }
    case TV_CHAR: case TV_LONG:
        switch (op) {
        case '+': case '-': case '*': case '/': case '%': case Lsl: case Lsr:
        case Asr: case '&': case '^': case '|':
            return TV_LONG;
        case '<': case '>': case Leq: case Geq: case  Eq: case Neq:
            return TV_BOOL;
        default : *msg = strdup("Operator type mismatch"); return TV_UND;
        }
    case TV_FLOAT:
        switch (op) {
        case '+': case '-': case '*': case '/':
            return TV_FLOAT;
        case '<': case '>': case Leq: case Geq: case  Eq: case Neq:
            return TV_BOOL;
        default : *msg = strdup("Operator type mismatch"); return TV_UND;
        }
    case TV_STRING:
        switch (op) {
        case '+': return TV_STRING;
        case '<': case '>': case Leq: case Geq: case  Eq: case Neq:
            return TV_BOOL;
        default : *msg = strdup("Operator type mismatch"); return TV_UND;
        }
    case TV_OTHER:
        leftString = (char *)YUVP(lval,TV_OTHER);
        rightString = (char *)YUVP(lval,TV_OTHER);
    if (!strcmp(leftString, rightString)) {
            *msg = strdup("Operand type mismatch");
            return TV_UND;
        }
        switch (op) {
        case Eq: case Neq: return TV_BOOL;
        default : *msg = strdup("Operator type mismatch"); return TV_UND;
        }
    default: *msg = strdup("*UNKNOWN OPERAND TYPE*"); return TV_UND;
    }
}

  YT(symbolDef) *
defSymbol(YT(symbol) *symbol)
{
    return(YC(symbolDef,refSymbol(symbol)));
}

  bool
equal(int typeCode, int attrType)
{
    switch (typeCode) {
    case TV_UND:
    case TV_DEP:
    case TV_OTHER:  return FALSE;
    case TV_TAG:    return (attrType == TagType);
    case TV_BOOL:   return (attrType == BOOLEAN);
    //KSSHack We really need TV_<all_Java_Prims>
    case TV_CHAR:   return ((attrType == BYTE) || (attrType == CHAR) ||
                (attrType == INT) || (attrType == LONG) ||
                (attrType == SHORT));
    case TV_LONG:   return ((attrType == BYTE) || (attrType == CHAR) ||
                (attrType == INT) || (attrType == LONG) ||
                (attrType == SHORT));
    case TV_FLOAT:  return ((attrType == DOUBLE) || (attrType == FLOAT));
    case TV_STRING: return (attrType == YTAG(stringType));
    default:
    yh_error("unknown type code %d in plutil:equal()", typeCode);
        return FALSE;
    }
}

  void
generateReadFuncCall(YT(type) *type)
{
    YT(symbolRef) *sym = NULL;
    YT(pluribusType) *plType = NULL;
    YT(undefinedType) *undef = NULL;

    switch (YTAG_OF(type)) {
        case YTAG(defType):
            PP("(%s)decoder.decodeObject()", PNAME(type));
            break;
        case YTAG(enumType):
            yh_error("<enum not allowed as typeSpec>");
            break;
        case YTAG(pluribusType):
            plType = YC(pluribusType,type);
        PP("(%s)decoder.decodeObject()",
           mangleName(SNAME(plType->type), plType->mangle));
            break;
        case YTAG(primType):
            switch (YC(primType,type)->type) {
                case BOOLEAN:        PP("decoder.readBoolean()"); break;
                case CHAR:           PP("decoder.readChar()"); break;
                case DOUBLE:         PP("decoder.readDouble()"); break;
                case FLOAT:          PP("decoder.readFloat()"); break;
                case INT:            PP("decoder.readInt()"); break;
                case LONG:           PP("decoder.readLong()"); break;
                case BYTE:           PP("decoder.readByte()"); break;
                case SHORT:          PP("decoder.readShort()"); break;
                default:
                    yh_error("invalid primType code %d in generateReadFuncCall()",
                             YC(primType,type)->type);
            }
            break;
        case YTAG(sequenceType):
            PP("(");
            generateTypeSpec(YC(sequenceType,type)->type);
            PP(")decoder.decodeObject()");
            break;
        case YTAG(stringType):
            PP("decoder.readUTF()");
            break;
        case YTAG(structType):
            yh_error("<struct not allowed as typeSpec>");
            break;
        case YTAG(undefinedType):
            undef = (YT(undefinedType) *)type;
            generateReadFuncCall(YC(type,undef->type));
            break;
        case YTAG(unionType):
            yh_error("<union not allowed as typeSpec>");
            break;
        case YTAG(symbolRef): /* XXX hack */
            sym = (YT(symbolRef) *)type;
            if (strcmp("Object", SNAME(sym)) == 0)
                PP("decoder.decodeObject()");
            else
                PP("(%s)decoder.decodeObject()",
                   pSymbolRef(YC(symbolRef,type)));
            break;
        default:
           yh_error("invalid type code %d in generateReadFuncCall()",
                    YTAG_OF(type));
    }
}

  YT(info) *
genInfo()
{
    return(YBUILD(info)(MOD_INTERNAL, 0, 0));
}

  YT(symbol) *
gensym()
{
    static int genCount = 1;
    char buf[BUFLEN];
    
    sprintf(buf, "gen_$_%d_", genCount++);
    return(yh_handleSymbol(buf));
}

  bool
goodTypeCode(int typeCode)
{
    switch (typeCode) {
    case TV_TAG:    return TRUE;
    case TV_BOOL:   return TRUE;
    case TV_CHAR:   return TRUE;
    case TV_LONG:   return TRUE;
    case TV_FLOAT:  return TRUE;
    case TV_STRING: return TRUE;
    case TV_OTHER:  return TRUE;
    default:        return FALSE;
    }
}
 
  YT(info) *
info()
{
    return(YBUILD(info)(0, 0, 0));
}

  bool
isBoolean(char *token)
{
    if (token) {
    if (strcasecmp("true", token) == 0 || strcasecmp("false", token) == 0)
        return TRUE;
    return FALSE;
    }
    return FALSE;
}

  bool
isChar(char *token)
{
    int len = 0;

    if (token) {
    len = strlen(token);
    if (len == 3) {
        return (token[0] == '\'' && token[len-1] == '\'');
    } else
        return FALSE;
    } else
    return FALSE;
}
  bool
isExport(YT(genericDef) *def)
{
    return((def->info->modifiers & MOD_EXPORT) != 0);
}

  bool
isGenerated(char *name)
{
    return (bool)(strstr(name,"gen_$_"));
}

  bool
isInternal(YT(genericDef) *def)
{
    return((def->info->modifiers & MOD_INTERNAL) != 0);
}

  bool
isNumber(char *token)
{
    int len = 0, i;

    if (token) {
    len = strlen(token);
    for (i = 0; i < len; i++) {
        if (token[i] != '.' && !isdigit(token[i]))
        return FALSE;
    }
    return TRUE;
    } else
    return FALSE;
}

  bool
isOperator(char *token)
{
    switch (token[0]) {
    case '#': case '_': // Special versions of unary '+' and '-'
    case '+': case '-': case '*': case '/': case '%': case '<': case '>':
    case '=': case '!': case '&': case '^': case '|': return TRUE;
    default: return FALSE;
    }
}

  bool
isScopedVariable(char *token) //KSSHack could be better
{
    int i;

    if (token) {
    if (isalpha(token[0])) {
        if (strcasecmp("true", token) == 0 ||
        strcasecmp("false", token) == 0)
        return FALSE;
        else {
        for (i = 0; i < strlen(token); i++) {
            if (token[i] == '.')
            return TRUE;
        }
        return FALSE;
        }
    }
    return FALSE;
    }
    return FALSE;
}

  bool
isString(char *token)
{
    int len = 0;

    if (token) {
    len = strlen(token);
    if (len > 1) {
        return (token[0] == '"' && token[len-1] == '"');
    } else
        return FALSE;
    } else
    return FALSE;
}

  bool
isUnaryOperator(char *token)
{
    if (strlen(token) == 1) {
    switch (token[0]) {
    //  We use '#' for '+' and '_' for '-' when storing unary expressions
    case '#': case '_':
    case '!': case '~': return TRUE;
    default:  return FALSE;
    }
    } else
    return FALSE;
}

  bool
isVariable(char *token) //KSSHack could be better
{
    if (token) {
    if (isalpha(token[0])) {
        if (strcasecmp("true", token) == 0 ||
        strcasecmp("false", token) == 0)
        return FALSE;
        return TRUE;
    }
    return FALSE;
    }
    return FALSE;
}

  YT(symbol) *
makeSym(char *name)
{
    return(yh_handleSymbol(name));
}

  char *
mangleName(char *name, long mangleType)
{
    if (name) {
        if (mangleType == KIND) {
            return kindClassName(name);
        } else
            return name;
    } else {
        yh_error("null name in plutil:mangleName()");
        return "";
    }
}

  int
numCharIn(char c, char *string)
{
    char *ptr = string;
    int count = 0, len = 0;

    if (ptr) {
    len = strlen(ptr);
    while (len > 0) {
        if (*ptr == c)
        count++;
        ptr++;
        len--;
    }
    }
    return count;
}

  int
numProtosInKind(YT(kind) *kind)
{
    int num = 0;
    YT(protoDefList) *protos = kind->protos;
    YT(kindList) *extends = kind->kinds;
    
    while (protos) {
        num++;
        protos = protos->next;
    }
    while (extends) {
        num += numProtosInKind(extends->kind);
        extends = extends->next;
    }
    return num;
}

  long
opFromString(char *token)
{
    if (strlen(token) == 1) {
    switch (token[0]) {
    case '+':
    case '-':
        case '*':
        case '/':
        case '%':
        case '<':
        case '>':
        case '&':
        case '^':
        case '|':
    case '!':
    case '~': return token[0];
    }
    } else if (strlen(token) == 2) {
    switch (token[0]) {
    case '<':
        switch (token[1]) {
        case '<': return Lsl;
        case '=': return Leq;
        }
    case '>':
        switch (token[1]) {
        case '>': return Lsr;
        case '=': return Geq;
        }
    case '=':
        if (token[1] == '=') return Eq;
    case '!':
        if (token[1] == '=') return Neq;
    case '&':
        if (token[1] == '&') return And;
    case '|':
        if (token[1] == '!') return Or;
    }
    }
    yh_error("unknown operator '%s'", token);
    return -1;
}

  char *
opToString(int op)
{
    char *unk;
    switch (op) {
    case '+': return "+";  case '-': return "-";  case '*': return "*";
    case '/': return "/";  case '%': return "%";  case Lsl: return "<<";
    case Lsr: return ">>"; case Asr: return ">>"; case '<': return "<";
    case '>': return ">";  case Leq: return "<="; case Geq: return ">=";
    case  Eq: return "=="; case Neq: return "!="; case '&': return "&";
    case '^': return "^";  case '|': return "|";  case And: return "&&";
    case  Or: return "||"; case '!': return "!"; case '~': return"~";
    default:  unk = malloc(80);
              sprintf(unk, "*UNKNOWN OPERATOR* (%d)", op);
              return unk;
    }
}

  char *
pSymbolRef(YT(symbolRef) *ref)
{
    static char buf[BUFLEN];

    buf[0] = '\0';
    symbolRefString(buf, ref);
    return(buf);
}

  YT(symbolRef) *
refSymbol(YT(symbol) *symbol)
{
    return(YBUILD(symbolRef)(symbol, NULL));
}

  YT(scopedRef) *
scopedRefFromString(char *token)
{
    YT(scopedRef) *ref = NULL;
    YT(symbolRef) *symbolRef = NULL;
    char *bufPtr = token, symbol[MSGLEN];
    int len = 0, i = 0, counter = 0;

    if (token) {
    len = strlen(token);
    counter = 0;
    for (i = 0; i < len; i++) {
        if (token[i] != '.')
        symbol[counter++] = token[i];
        else {
        symbol[counter] = '\0';
        bufPtr = symbol;
        symbolRef = YBUILD(symbolRef)(yh_handleSymbol(bufPtr),NULL);
        ref = YBUILD(scopedRef)(ref,symbolRef);
        counter = 0;
        }
    }
    symbol[counter] = '\0';
    bufPtr = symbol;
    symbolRef = YBUILD(symbolRef)(yh_handleSymbol(bufPtr),NULL);
    ref = YBUILD(scopedRef)(ref,symbolRef);
    }
    return ref;
}

  void
setExport(YT(genericDef) *def)
{
    def->info->modifiers |= MOD_EXPORT;
}

  void
setInternal(YT(genericDef) *def)
{
    def->info->modifiers |= MOD_INTERNAL;
}

  char *
stringFromExprList(char *prefix, YT(exprList) *exprs,
           YT(ingredientRoleList) *roles)
{
    YT(typedValue) val;
    YT(typeSpec) *typeSpec = NULL;
    bool anyAttributes, anyOperators;
    char *result = NULL, *addOn = NULL;

    result = malloc(MSGLEN);
    result[0] = 0;
    while (exprs) {
    val = evalExpr(prefix, exprs->expr, NULL, NULL, roles, MAKE_EXP, FALSE,
               &anyAttributes, &anyOperators);
    typeSpec = typeSpecFromTypeCode(&val);
    addOn = typeSpecToString(typeSpec);
        if (addOn)
            result = strcat(result, addOn);
    result = strcat(result, ";");
    exprs = exprs->next;
    }
    return result;
}

  char *
stringFromParameterList(YT(parameterDeclList) *params)
{
    YT(typeSpec) *typeSpec = NULL;
    char *result = NULL, *addOn = NULL;
    
    result = malloc(MSGLEN);
    result[0] = 0;
    while (params) {
        typeSpec = params->parameterDecl->type;
        addOn = typeSpecToString(typeSpec);
        if (addOn)
            result = strcat(result, addOn);
        result = strcat(result, ";");
        params = params->next;
    }
    return result;
}

  void
symbolRefString(char *buf, YT(symbolRef) *ref)
{
    YT(pluribusType) *plType = NULL;
    YT(scopedRef) *scopedRef = NULL;
    char myBuf[MSGLEN];

    if (ref) {
    if (YTAG_TEST(ref,symbolRef)) {
        strcat(buf, ref->name->name);
    } else if (YTAG_TEST(ref,scopedRef)) {
        scopedRef = YC(scopedRef,ref);
        symbolRefString(buf, YC(symbolRef,scopedRef->scope));
        if (scopedRef->scope) {
            strcat(buf, ".");
        }
        symbolRefString(buf, scopedRef->ref);
    } else if (YTAG_TEST(ref,outerRef)) {
        YT(outerRef) *outerRef = YC(outerRef,ref);
        int i;
        for (i=0; i<outerRef->level; ++i) {
            strcat(buf, "^");
        }
        strcat(buf, ".");
        symbolRefString(buf, outerRef->ref);
    } else if (YTAG_TEST(ref,pluribusType)) {
        plType = YC(pluribusType,ref);
        sprintf(myBuf, "%s",
            mangleName(plType->type->name->name, plType->mangle));
        strcat(buf, myBuf);
    } else {
        sprintf(myBuf, "invalid symbol ref %d in plutil:symbolRefString",
            YTAG_OF(ref));
        strcat(buf, myBuf);
    }
    }
}
    
  char *
typeCodeToString(YT(typedValue) val)
{
    int typeCode = val.typeCode;

    char *unk;
    switch (typeCode) {
    case TV_TAG:    return "Tag";
    case TV_BOOL:   return "boolean";
    case TV_CHAR:   return "char";
    case TV_LONG:   return "long";
    case TV_FLOAT:  return "float";
    case TV_STRING: return "string";
    case TV_OTHER:  return (char *)YUV(val,TV_OTHER);
    default:      unk = malloc(40);
                  sprintf(unk, "*UNKNOWN* (%d)", typeCode);
                  return unk;
    }
}

  YT(typeSpec) *
typeSpecFromTypeCode(YT(typedValue) *val)
{
    int typeCode = 0;

    if (val) {
    typeCode = val->typeCode;
    switch (typeCode) {
    case TV_UND:
    case TV_DEP:
    case TV_TAG: return YC(typeSpec,YBUILD(undefinedType)(NULL));
    case TV_OTHER:
        return YC(typeSpec,YBUILD(undefinedType)
              (refSymbol(makeSym((char *)YUVP(val,TV_OTHER)))));
    case TV_BOOL:   return YC(typeSpec,YBUILD(primType)(BOOLEAN));
    case TV_CHAR:   return YC(typeSpec,YBUILD(primType)(CHAR));
    case TV_LONG:   return YC(typeSpec,YBUILD(primType)(LONG));
    case TV_FLOAT:  return YC(typeSpec,YBUILD(primType)(FLOAT));
    case TV_STRING: return YC(typeSpec,YBUILD(stringType)(-1));
    default:
        yh_error("unknown type code %d in plutil:typeSpecFromTypeCode()",
             typeCode);
        return FALSE;
    }
    } else {
    yh_error("null pointer in plutil:typeSpecFromTypeCode()");
    return FALSE;
    }
}

  long
typeOf(YT(type) *type)
{
    switch (YTAG_OF(type)) {
        case YTAG(defType):
        case YTAG(enumType):
        case YTAG(sequenceType):
        case YTAG(stringType):
        case YTAG(structType):
        case YTAG(symbolRef):
        case YTAG(undefinedType):
        case YTAG(unionType):
            return YTAG_OF(type);
        case YTAG(pluribusType):
            return YC(pluribusType,type)->mangle;
        case YTAG(primType):
            return YC(primType,type)->type;
        default:
           yh_error("unknown type code %d in plutil:typeOf()",
                    YTAG_OF(type));
    }
    return -1;
}

  char * 
typedValueToString (YT(typedValue) val)
{
    char *msg = "COMPLETELY UNKNOWN typeCode (%d) in typedValueToString";
    char * str;
    switch (val.typeCode) {
    case TV_TAG:
        return "TAG";
    case TV_BOOL:
        return (YUV(val,TV_BOOL)? "true":"false");
    case TV_CHAR:
        str = malloc(4);
        str[0] = '\'';
        str[1] = (char)YUV(val,TV_CHAR);
        str[2] = '\'';
        str[3] = '\0';
        return str;
    case TV_LONG:
        str = malloc(8);
        sprintf(str, "%d", YUV(val,TV_LONG));
        return str;
    case TV_STRING:
        str = malloc(strlen((char *)YUV(val,TV_STRING)) + 2);
        sprintf(str,"\"%s\"", YUV(val,TV_STRING));
        return str;
    case TV_FLOAT:
        str = malloc(20);
        sprintf(str, "%f", YUV(val,TV_FLOAT));
        return str;
    case TV_OTHER:
        str = malloc(strlen((char *)YUV(val,TV_OTHER)));
        sprintf(str,"%s", YUV(val,TV_OTHER));
        return str;
    case TV_UND:
        return ("UNDEFINED");
    case TV_DEP:
        return ("DEPENDENT ATTRIBUTE");
    default:
        str = malloc(strlen(msg) + 20);
        sprintf(str, msg, val.typeCode);
        return str;
    }
}

  bool
typeEqual(YT(typedValue) val, YT(parameterDecl) *param)
{
    YT(defType) *defType = NULL;
    YT(symbolRef) *symbol = NULL;
    char *otherName;

    //KSSHack What about checking for both being arrays?
    switch (val.typeCode) {
        case TV_UND: case TV_DEP: case TV_TAG:
        return FALSE;
        case TV_BOOL: case TV_CHAR: case TV_LONG: case TV_FLOAT:
        return(equal(val.typeCode, typeOf(YC(type,param->type))));
        case TV_STRING:
        return(YTAG_OF(param->type) == YTAG(stringType));
        case TV_OTHER:
        otherName = (char *)YUV(val,TV_OTHER);
        switch (YTAG_OF(param->type)) {
        //KSSHack  These are really hacky--Should actually compare
        //KSSHack the types, if they can actually happen...
            case YTAG(defType):
            defType = YC(defType,param->type);
            return(!strcmp(SDNAME(defType),otherName));
            case YTAG(enumType):
            return(!strcmp("enum",otherName));
            case YTAG(sequenceType):
            return(!strcmp("sequence",otherName));
            case YTAG(structType):
            return(!strcmp("struct",otherName));
            case YTAG(unionType):
            return(!strcmp("union",otherName));
            case YTAG(stringType):
            case YTAG(primType):
            return FALSE;
            case YTAG(symbolRef):
            symbol = YC(symbolRef,param->type);
            return (!strcmp(SNAME(symbol),otherName));
            case YTAG(undefinedType):
            symbol = YC(undefinedType,param->type)->type;
            return (!strcmp(SNAME(symbol),otherName));
            case YTAG(pluribusType):
            symbol = YC(pluribusType,param->type)->type;
            return (!strcmp(SNAME(symbol),otherName));
            default:
            yh_error("unknown type code %d in plutil:typeEqual()",
                 YTAG_OF(param->type));
        }
        default:
        yh_error("unknown typedValue code %d in plutil:typeEqual()",
             val.typeCode);
    }
    return FALSE;
}

  char *
typeSpecToString(YT(typeSpec) *typeSpec)
{
    YT(pluribusType) *plType = NULL;

    if (!typeSpec) {
    yh_error("null pointer for typeSpec in typeSpecToString");
    return(NULL);
    }
    switch (YTAG_OF(typeSpec)) {
    case YTAG(defType): return(PNAME(typeSpec));
    case YTAG(enumType):
    yh_error("enum not allowed in type signature in typeSpecToString()");
    return(NULL);
    case YTAG(pluribusType):
    plType = YC(pluribusType,typeSpec);
    return(mangleName(SNAME(plType->type), plType->mangle));
    case YTAG(primType):
    switch (YC(primType,typeSpec)->type) {
    case BOOLEAN: return("Z"); break;
    case CHAR:    return("C"); break;
    case DOUBLE:  return("D"); break;
    case FLOAT:   return("D"); break; // Treat as doubles for exprs
    case INT:     return("J"); break; // Treat as doubles for exprs
    case LONG:    return("J"); break;
    case BYTE:    return("B"); break;
    case SHORT:   return("S"); break;
    default:
        yh_error("invalid prim type code %d in typeSpecToString()",
             YC(primType,typeSpec)->type);
        return(NULL);
    }
    return(NULL);
    case YTAG(sequenceType): return("[]");
    case YTAG(stringType): return("String");
    case YTAG(structType):
    yh_error("struct not allowed in type signature in typeSpecToString()");
    return(NULL);
    case YTAG(undefinedType):
    return(pSymbolRef(YC(undefinedType,typeSpec)->type));
    case YTAG(unionType):
    yh_error("union not allowed in type signature in typeSpecToString()");
    return(NULL);
    case YTAG(symbolRef): return(pSymbolRef(YC(symbolRef,typeSpec)));
    default:
    yh_error("invalid type code %d in typeSpecToString()",
         YTAG_OF(typeSpec));
    return(NULL);
    }
}

  char *
typeToString(int attrType)
{
    char *unk;
    switch (attrType) {
    case TagType: return "Tag";
    case BOOLEAN: return "boolean";
    case CHAR:    return "char";
    case LONG:    return "long";
    case YTAG(stringType): return "string";
    default:      unk = malloc(40);
                  sprintf(unk, "*UNKNOWN* (%d)", attrType);
                  return unk;
    }
}

char *whatKind(int what)
{
    char *unk;

    switch (what) {
    case INGREDIENT:
    case PRESENCE  :
    case UNUM      : return "kind";
    default:
        unk = malloc(40);
        sprintf(unk, "*UNKNOWN KIND* (%d)", what);
        return unk;
    }
}

  char *
writeFuncName(YT(type) *type)
{
    switch (YTAG_OF(type)) {
        case YTAG(defType):
        case YTAG(pluribusType):
        case YTAG(sequenceType):
        case YTAG(symbolRef):
        case YTAG(undefinedType):
            return("encodeObject");
        case YTAG(primType):
            switch (YC(primType,type)->type) {
                case BOOLEAN:        return("writeBoolean");
                case CHAR:           return("writeChar");
                case DOUBLE:         return("writeDouble");
                case FLOAT:          return("writeFloat");
                case INT:            return("writeInt");
                case LONG:           return("writeLong");
                case BYTE:           return("writeByte");
                case SHORT:          return("writeShort");
                default:
                    yh_error("invalid primType code %d in writeFuncName",
                             YC(primType,type)->type);
                    return(NULL);
            }
        case YTAG(stringType):
            return("writeUTF");
        default:
            yh_error("invalid type code %d in writeFuncName", YTAG_OF(type));
            return(NULL);
    }
}
