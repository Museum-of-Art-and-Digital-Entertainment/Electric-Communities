/* 
  yh_symtab.c -- Symbol table management routines for YaccHelper

  Copyright 1993,1994 by Chip Morningstar
  Permission granted to use freely (including commercial use), provided this
    copyright notice remains attached.
  For further information contact chip@netcom.com

*/

#include "generic.h"
#include "yh.h"
#include <string.h>
#include <ctype.h>

#ifdef WIN32
#define strcasecmp(a,b) stricmp(a,b)
#endif /* WIN32 */

#define HASH_TABLE_SIZE 512
#define HASH_TABLE_MASK 0x1FF
static YT(symbol) *SymbolTable[HASH_TABLE_SIZE];

bool yh_CaseSensitiveSymbols = FALSE;

  void
YH_dump_symbol(YT(symbol) *symbol, int tabLevel)
{
    if (symbol)
        printf("'%s'\n", symbol->name);
    else
        printf("(NULL)\n");
}

  void
YH_free_symbol(YT(symbol) *symbol)
{
    symbol->refCount--;
    if (symbol->refCount == 0)
        yh_deleteSymbol(symbol);
}

  void
yh_bindSymbol(YT(symbol) *symbol, void *binding)
{
    if (symbol->binding)
        yh_warning("rebinding symbol %s", symbol->name);
    symbol->binding = binding;
}

  static int
yh_cmpSymbols(const void *arg1, const void *arg2)
{
    YT(symbol) **symbol1 = (YT(symbol) **) arg1;
    YT(symbol) **symbol2 = (YT(symbol) **) arg2;

    return(strcmp((*symbol1)->name, (*symbol2)->name));
}

  void
yh_deleteSymbol(YT(symbol) *symbol)
{
    longword hashValue;
    YT(symbol) *chaser;

    hashValue = yh_hashString(symbol->name);
    chaser = SymbolTable[hashValue];
    if (chaser == symbol) {
        SymbolTable[hashValue] = symbol->next;
        free(symbol->name);
        FREE(symbol);
    } else {
        while (chaser && chaser->next != symbol)
            chaser = chaser->next;
        if (chaser) {
            chaser->next = symbol->next;
            free(symbol->name);
            FREE(symbol);
        } else {
            yh_error("attempt to delete non-existant symbol '%s'",
                     symbol->name);
        }
    }
}

  bool
yh_forEachSymbol(bool (*workfunc)(YT(symbol) *symbol, void *env), void *env)
{
    int i;
    YT(symbol) *chaser;

    for (i = 0; i < HASH_TABLE_SIZE; ++i)
        for (chaser = SymbolTable[i]; chaser; chaser = chaser->next)
            if (workfunc(chaser, env))
                return(TRUE);
    return(FALSE);
}

  YT(symbol) *
yh_handleSymbol(char *name)
{
    YT(symbol) *result;

    result = yh_lookupSymbol(name);
    if (result) {
        result->refCount++;
        return(result);
    } else {
        return(yh_newSymbol(name, NULL));
    }
}

  longword
yh_hashString(char *s)
{
    longword result = 0;
    longword subResult = 0;
    int bitCount = 32;

    while (*s) {
        subResult = (subResult << 1) + tolower(*s++);
        if (!--bitCount) {
            result ^= subResult;
            subResult = 0;
            bitCount = 32;
        }
    }
    result ^= subResult;
    return(result & HASH_TABLE_MASK);
}

  void
yh_initializeSymbolTable(void)
{
    int i;

    for (i=0; i<HASH_TABLE_SIZE; ++i)
        SymbolTable[i] = NULL;
}

  void
yh_installSymbol(YT(symbol) *symbol)
{
    int test;
    longword hashValue;
    YT(symbol) *result;
    YT(symbol) *oldResult;
    
    hashValue = yh_hashString(symbol->name);
    result = SymbolTable[hashValue];
    if (result == NULL) {
        SymbolTable[hashValue] = symbol;
        symbol->next = NULL;
    } else {
        oldResult = NULL;
        while (result) {
            if (yh_CaseSensitiveSymbols)
                test = strcmp(symbol->name, result->name);
            else
                test = strcasecmp(symbol->name, result->name);
            if (test == 0) {
                yh_error("symbol %s multiply defined", symbol->name);
                return;
            } else if (test > 0) {
                symbol->next = result;
                if (oldResult == NULL)
                    SymbolTable[hashValue] = symbol;
                else
                    oldResult->next = symbol;
                return;
            } else {
                oldResult = result;
                result = result->next;
            }
        }
        if (oldResult)
            oldResult->next = symbol;
        else
            SymbolTable[hashValue] = symbol;
        symbol->next = NULL;
    }
}

  static bool
yh_countSymbol(YT(symbol) *symbol, void *env)
{
    long *countptr = env;
    ++*countptr;
    return(FALSE);
}

  static bool
yh_grabSymbol(YT(symbol) *symbol, void *env)
{
    YT(symbol) ***chaser = env;

    **chaser = symbol;
    ++*chaser;
    return(FALSE);
}

  long
yh_listOfSymbols(YT(symbol) ***symbols)
{
    YT(symbol) **chaser;

    long count;

    count = 0;
    yh_forEachSymbol(yh_countSymbol, &count);
    *symbols = TypeAllocMulti(YT(symbol) *, count);
    chaser = *symbols;
    yh_forEachSymbol(yh_grabSymbol, &chaser);
    qsort(*symbols, count, sizeof(YT(symbol) *), yh_cmpSymbols);
    return(count);
}

  YT(symbol) *
yh_lookupSymbol(char *s)
{
    YT(symbol) *result;
    int test;
    longword hashValue;
    
    hashValue = yh_hashString(s);
    result = SymbolTable[hashValue];
    while (result) {
        if (yh_CaseSensitiveSymbols)
            test = strcmp(s, result->name);
        else
            test = strcasecmp(s, result->name);
        if (test == 0)
            break;
        else if (test > 0) {
            result = NULL;
            break;
        } else {
            result = result->next;
        }
    }
    return(result);
}

  YT(symbol) *
yh_newSymbol(char *name, void *binding)
{
    YT(symbol) *symbol;
    
    symbol = TypeAlloc(YT(symbol));
    symbol->refCount = 1;
    symbol->name     = STRDUP(name);
    symbol->binding  = binding;
    yh_installSymbol(symbol);
    return(symbol);
}

  bool
yh_symbolWasNew(YT(symbol) *symbol)
{
    return(symbol->refCount == 1);
}

  int
yh_countList(YT(genericList *)list)
{
    int result = 0;

    while (list) {
        ++result;
        list = list->next;
    }
    return(result);
}
