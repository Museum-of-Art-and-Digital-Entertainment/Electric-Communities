/*
  pljnative.c -- Useful routines for getting along with the Java native code
                 environment.

  Chip Morningstar
  Electric Communities
  5-March-1997

  Copyright 1997 Electric Communities, all rights reserved.

*/

#include "generic.h"
#include <stdarg.h>
#include "plj.h"

static int countArgs(char *signature);
static char *skipArg(char *signature);

/**
* Count the arguments declared by a Java argument signature string.
*
* @param A Java method argument type signature string.
* @return The number of arguments the string describes, or -1 if it is not a
*       well-formed argument signature string.
*/
  static int
countArgs(char *signature)
{
    int result = 0;
    char *newSignature;

    if (*signature++ != '(')    /* Args signature is bracketed by parens */
        return(-1);
    while (TRUE) {
        newSignature = skipArg(signature);
        if (signature == newSignature)
            break;
        ++result;
        signature = newSignature;
    }
    if (*signature != ')')      /* Make sure that closing paren is present */
        return(-1);
    return(result);
}

/**
* Scan over a single argument type signature in a Java argument signature
*       string.
*
* @param signature A Java type signature string to be scanned.
* @return The first character in the string after what was scanned over. Note
*       that if no type signature is detected at the start of the string, the
*       result will be the (unmodified) argument string itself. 
*/
  static char *
skipArg(char *signature)
{
    char c = *signature;
    char *next = signature + 1;

    if (c == 'B' || c == 'C' || c == 'D' || c == 'F' || c == 'I' ||
            c == 'J' || c == 'S' || c == 'Z') {
        /* Primitives */
        return(next);
    } else if (c == 'L') {
        /* Class names */
        do {
            c = *next++;
        } while (c != ';' && c != '\0');
        if (c == ';')
            return(next);
        else
            return(signature);
    } else if (c == '[') {
        /* Arrays */
        char *result = skipArg(next);
        if (result == next)
            return(signature);
        else
            return(result);
    } else {
        /* Nothing there. Return arg unmodified. */
        return(signature);
    }
}

/**
* Convert a YaccHelper generic list into a Java array object. The presumption
*       is that the list is a list of structs containing references to Java
*       objects!
*
* @param list The list to convert.
* @param extractFunc A function that will return the Java object referenced by
*       a list element.
* @return Handle to a Java array of objects derived from the given list.
*/
  HArrayOfObject *
convertListToJavaArray(YT(genericList) *list,
                       HObject *(*extractFunc)(void *elem))
{
    if (list == NULL) {
        return(NULL);
    } else {
        int count = YCOUNT(list);
        HArrayOfObject *result = (HArrayOfObject *)ArrayAlloc(T_CLASS, count);
        int i;
        ClassArrayOfObject *array = unhand(result);
        
        for (i=0; i<count; ++i) {
            array->body[i] = extractFunc(list->elem);
            list = list->next;
        }
        array->body[count] = (HObject *)get_classObject();
        return(result);
    }
}

/**
* Call a Java (dynamic) method using a more friendly interface than the one
*       provided by Sun's native code interface library. Saves you from
*       having to pass in a bunch of glorpf and also takes care of any
*       exceptions that may occur inside the Java code.
*
* @param obj The object whose method we wish to call.
* @param methodName The name of the method being called.
* @param signature The method's argument signature.
* @param others 0 or more arguments to pass to the method.
* @return Whatever the Java method returned.
*/
  long
javaCall(HObject *obj, char *methodName, char *signature, ...)
{
    va_list args;
    long result;

    va_start(args, signature);
    result = do_execute_java_method_vararg(EE(), obj, methodName, signature, 
                                           0, FALSE, args, 0, FALSE);
    va_end(args);

    if (exceptionOccurred(EE()))
        wrappedExit(1);

    return(result);
}

/**
* Call a Java static method using a more friendly interface than the one
*       provided by Sun's native code interface library. Saves you from
*       having to pass in a bunch of glorpf and also takes care of any
*       exceptions that may occur inside the Java code.
*
* @param className File-path-mangled FQN of class whose method we're calling
* @param methodName The name of the method being called.
* @param signature The method's argument signature.
* @param others 0 or more arguments to pass to the method.
* @return Whatever the Java method returned.
*/
  long
javaCallStatic(char *className, char *methodName, char *signature, ...)
{
    va_list args;
    long result = 0;
    ClassClass *cla;

    cla = FindClass(EE(), className, TRUE);
    if (cla) {
        va_start(args, signature);
        result = do_execute_java_method_vararg(EE(), cla, methodName,
            signature, 0, TRUE, args, 0, FALSE);
        va_end(args);
        
        if (exceptionOccurred(EE()))
            wrappedExit(1);
    } else {
        SignalError(EE(), "ec/plcompile/CallError", STRDUP(className));
        wrappedExit(1);
    }
    return(result);
}    

/**
* Call a Java constructor method using a more friendly interface than the one
*       provided by Sun's native code interface library. Note that due to the
*       limitations of C's varargs mechanism, this function only supports calls
*       to constructors with 5 or fewer parameters (it can be trivially
*       modified to handle more should the need arise). It does, however, save
*       you from having to pass in a bunch of glorpf and also takes care of any
*       exceptions that may have occurred inside the constructor.
*
* @param className The file-path-mangled FQN of the class being constructed.
* @param signature The argument signature string of the constructor to call.
* @param others 0 to 5 arguments to pass to the constructor.
* @return The constructed Java object.
*/
  HObject *
javaConstruct(char *className, char *signature, ...)
{
    va_list args;
    HObject *result = NULL;
    long arg1 = 0, arg2 = 0, arg3 = 0, arg4 = 0, arg5 = 0;
    int argCount;

    va_start(args, signature);
    argCount = countArgs(signature);
    if (argCount < 0 || 5 < argCount) {
        /* Throw an error if the signature string is malformed or if more args
           were passed than this implementation is prepared to deal with. */
        SignalError(EE(), "ec/plcompile/CallError", STRDUP(signature));
        wrappedExit(1);
    } else {
        if (argCount > 0)
            arg1 = va_arg(args, long);
        if (argCount > 1)
            arg2 = va_arg(args, long);
        if (argCount > 2)
            arg3 = va_arg(args, long);
        if (argCount > 3)
            arg4 = va_arg(args, long);
        if (argCount > 4)
            arg5 = va_arg(args, long);
        /* The varargs parsing on the receiving end of this call will ignore
           excess arguments even though we do pass them. This is a case of
           sacrificing speed and elegance for comfort. */
        result = execute_java_constructor(EE(), className, NULL, signature,
                                          arg1, arg2, arg3, arg4, arg5);
        if (exceptionOccurred(EE()))
            wrappedExit(1);
    }
    va_end(args);
    return(result);
}
