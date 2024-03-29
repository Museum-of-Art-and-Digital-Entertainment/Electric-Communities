/*
  plexpr.c -- Play with expression trees

  Karl Schumaker
  Electric Communities
  18-December-1996

  Copyright 1996 Electric Communities, all rights reserved.

*/

#include<stdlib.h>
#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "yh_walk.h"
#include "y.tab.h"
#include "pl.h"

//KSS Globals declared here because gdb can't find them inside of PCASEs
YT(ingredientRole) *GLBLrole = NULL;
char GLBLstring[MSGLEN];
char *GLBLstringPtr;

typedef struct {
    YT(presenceImpl) *presImpl;
    YT(scopedRef) *scopedRef;
    bool specialUnaries;
    bool reportErrors;
    char *prefix;
    exprFormEnum exprForm;
    exprTypeEnum exprType;
} t_pl_stringEnv;

  void
parseVarOrFunction(char *prefix, YT(symbolRef) *symbolRef,
                   YT(scopedRef) *scopedRef,
           YT(presenceImpl) *presImpl, exprFormEnum exprForm,
           bool reportErrors, char Result[]);

YA_FUNC_DEF(plStringExpr);
YA_FUNC_DEF(plVarsExpr);

#define PSTRING(what) \
    ((char *)YH_WALK(expr, YC(expr,arg->what), YA_FUNC(plStringExpr), env))
#define PVARS(what) \
    ((YT(scopedRefList) *)YH_WALK(expr, YC(expr,arg->what), YA_FUNC(plVarsExpr), env))

#define PCASE(type, block) YA_CASE(type, { block; YA_RETURN(arg); } )

  char *
stringFromExpr(char *prefix, exprFormEnum exprForm, exprTypeEnum exprType,
           YT(expr) *expr, YT(presenceImpl) *impl, bool specialUnaries,
           bool reportErrors)
{
    char *result;
    t_pl_stringEnv env;

    env.prefix = prefix;
    env.exprForm = exprForm;
    env.exprType = exprType;
    env.specialUnaries = specialUnaries;
    env.reportErrors = reportErrors;
    env.presImpl = impl;
    env.scopedRef = NULL;
    result = (char *)((void*) YH_WALK(expr, expr, YA_FUNC(plStringExpr), &env));
    return(result);
}

YA_FUNC_START(plStringExpr, t_pl_stringEnv)
{
    PCASE(binop,{
        char *left;
        char *op = opToString(arg->op);
        char *right;
        char total[MSGLEN];

        left = PSTRING(left);
        right = PSTRING(right);
    switch (env->exprForm) {
    case PREFIX:
        sprintf(total,"%s %s %s", op, left, right); break;
    case INFIX:
        sprintf(total,"%s %s %s", left, op, right); break;
    case POSTFIX:
        sprintf(total,"%s %s %s", left, right, op); break;
    }
    
        YA_RETURN(total);
    });
    PCASE(condop,{
        if (PSTRING(cond))
            YA_RETURN(PSTRING(thenPart))
        else
            YA_RETURN(PSTRING(elsePart));
    });
    PCASE(unop,{
    char *oper;
        char *symbol;
        char total[MSGLEN];

        switch (arg->op) {
            case '+':
        if (env->specialUnaries)
            symbol = STRDUP("#\0");
        else
            symbol = STRDUP("+\0");
            break;
            case '-':
        if (env->specialUnaries)
            symbol = STRDUP("_\0");
        else
            symbol = STRDUP("-\0");
        break;
            case '!': symbol = STRDUP("!\0"); break;
            case '~': symbol = STRDUP("~\0"); break;
        }
        oper = PSTRING(operand);
    switch (env->exprForm) {
    case PREFIX:
    case INFIX:
        sprintf(total,"%s %s", symbol, oper); break;
    case POSTFIX:
        sprintf(total,"%s %s", oper, symbol); break;
    }
        YA_RETURN(total);
    });
    PCASE(tagLit,{
        YT(typedValue) *val = YBUILD(typedValue)(TV_TAG,YUC(value,TRUE));
        YA_RETURN("**Tag**");
    });
    PCASE(numLit,{
        char *value = malloc(20);
        sprintf(value, "%d", arg->value);
        YA_RETURN(value);
    });
    PCASE(charLit,{
        char *value = malloc(5);
        sprintf(value, "'%c'", (char)(arg->value));
        YA_RETURN(value);
    });
    PCASE(stringLit,{
        char *value = malloc(strlen((char *)arg->value)+2);
        value[0] = '\0';
        strcat(value, "\"");
        strcat(value, (char *)arg->value);
        strcat(value, "\"");
        YA_RETURN(value);
    });
    PCASE(boolLit,{
        char *value = STRDUP((arg->value ? "true":"false"));
        YA_RETURN(value);
    });
    PCASE(refTerm,{
        YA_RETURN(PSTRING(value));
    });
    PCASE(symbolRef,{
        YT(parameterDecl) *parm = NULL;
        char *result = NULL;
        char Result[MSGLEN];
        
        if (env->exprType == INIT_EXP) {
            result = STRDUP(SNAME(arg));
            parm = findParameterInMethods(arg->name,
                      env->presImpl->primeInitBlocks);
            if (parm) {
                YA_RETURN(result);
            } else {
                parm = findParameterInMethods(arg->name,
                                              env->presImpl->initBlocks);
                if (parm) {
                    YA_RETURN(result);
                }
                if (env->reportErrors)
                    yh_error("%s%s is not an init() parameter", env->prefix,
                             SNAME(arg));
                YA_RETURN("NOT AN INIT PARAM");
            }
        } else if (env->exprType == MAKE_EXP) {
            if (env->scopedRef) {
                parseVarOrFunction(env->prefix, arg, env->scopedRef,
                                   env->presImpl, env->exprForm,
                                   env->reportErrors, Result);
                YA_RETURN(Result);
            } else {
                sprintf(Result,
                        "%s%s: vars or functions in make must be <ingredient>.<name>",
                        env->prefix, SNAME(arg));
                if (env->reportErrors)
                    yh_error(Result);
                YA_RETURN(Result);
            }
        } else if (env->exprType == COND_EXP) {
            result = STRDUP(SNAME(arg));
            YA_RETURN(result); /*KSSHack until we figure out COND_EXPs */
        }
    });
    PCASE(scopedRef,{
        char *result = NULL;
    if (env->exprType == MAKE_EXP) {
        if (arg->scope && arg->scope->scope == NULL) {
        env->scopedRef = arg;
        result = PSTRING(ref);
        env->scopedRef = NULL;
        YA_RETURN(result);
        } else {
        sprintf(GLBLstring, "%s%s cannot be in nested scope",
            env->prefix, SNAME(arg->ref));
        if (env->reportErrors)
            yh_error(GLBLstring);
        result = STRDUP(GLBLstring);
        YA_RETURN(result);
        }
    } else {
        sprintf(GLBLstring, "%s%s scopedRef used in constant expression",
            env->prefix, SNAME(arg->ref));
        if (env->reportErrors)
        yh_error(GLBLstring);
        /* Don't need to parse these
           PSTRING(scope);
           PSTRING(ref);
         */
        result = STRDUP(GLBLstring);
        YA_RETURN(result);
    }
    });
    PCASE(outerRef,{
    if (env->reportErrors)
        yh_error("outerRef used in constant expression");
        PSTRING(ref);
        YA_RETURN("outerRef used in constant expression");
    });
}
YA_FUNC_END(plStringExpr)

  char *
scanToken(char **line)
{
    char *token = NULL;
    int index = 0;

    if (*line) {
    token = STRDUP(*line);
    while ((*line)[0] == ' ') // Skip blanks...
        (*line)++;
    // Grab chars until space, newline or null character...
    while ((*line)[0] != ' ' && (*line)[0] != '\n' && (*line)[0] != '\0') {
        token[index++] = (*line)[0];
        (*line)++;
    }
    token[index] = '\0';
    if (strlen(token) == 0) // If the line (and thus token) is just a null
        token = NULL;
    }
    return(token);
}

  YT(expr) *
exprFromOperand(char *token)
{
    YT(scopedRef) *scoped = NULL;
    char *charPtr;

    if (isBoolean(token))
    return (YC(expr,YBUILD(boolLit)(boolFromString(token))));
    else if (isChar(token)) {
    /* YBUILD doesn't alloc, so ensure char doesn't get overwritten */
    charPtr = malloc(1);
    charPtr[0] = token[0];
    return (YC(expr,YBUILD(charLit)(charPtr[0])));
    }
    else if (isNumber(token))
    return (YC(expr,YBUILD(numLit)(atoi(token))));
    else if (isString(token)) {
    /* YBUILD doesn't alloc, so ensure string doesn't get overwritten */
    charPtr = STRDUP(token);
    return (YC(expr,YBUILD(stringLit)(charPtr)));
    }
    else if (isVariable(token))
    if (isScopedVariable(token)) {
        scoped = scopedRefFromString(token);
        return (YC(expr,scoped));
    } else
        return (YC(expr,YBUILD(symbolRef)(yh_handleSymbol(token),NULL)));
    else {
    yh_error("Unknown operand type for '%s' in exprFromOperand()", token);
    return NULL;
    }
}

  YT(expr) *
scanExpr(char **line)
{
    //KSSHack fix this!
    YT(expr) *left = NULL, *right = NULL;
    char *token;
    int num = 1;

    token = scanToken(line);
    if (token) {
    if (isOperator(token)) {
        left = scanExpr(line);
        if (left) {
        if (isUnaryOperator(token)) {
            if (token[0] == '#')
            token[0] = '+';
            else if (token[0] == '_')
            token[0] = '-';
            left = YC(expr,
                  YBUILD(unop)(opFromString(token), left));
        } else {
            right = scanExpr(line);
            if (right) {
            left = YC(expr,
                  YBUILD(binop)(left, opFromString(token),
                        right));
            } else {
            yh_error("Invalid unary operator '%s' in scanExpr()",
                 token);
            return NULL;
            }
        }
        } else {
        yh_error("Invalid expression '%s%s' in scanExpr()", token,
             *line);
        return NULL;
        }
    } else {
        return exprFromOperand(token);
    }
    }

    /* KSSHack what about the remainder of the line if there is one? */

    return left;
}

  YT(scopedRefList) *
varsFromExpr(char *prefix, YT(expr) *expr, bool reportErrors)
{
    YT(scopedRefList) *result;
    t_pl_stringEnv env;

    env.prefix = prefix;
    env.scopedRef = NULL;
    result = (YT(scopedRefList) *)((void*) YH_WALK(expr, expr,
                                                   YA_FUNC(plVarsExpr), &env));
    return(result);
}

YA_FUNC_START(plVarsExpr, t_pl_stringEnv)
{
    PCASE(binop,{
        YT(scopedRefList) *left = PVARS(left);
        YT(scopedRefList) *right= PVARS(right);

    while (right) {
        YBUILD(scopedRefList)(right->scopedRef, left);
        right = right->next;
    }
    
        YA_RETURN(left);
    });
    PCASE(condop,{
        if (PVARS(cond))
            YA_RETURN(PVARS(thenPart))
        else
            YA_RETURN(PVARS(elsePart));
    });
    PCASE(unop,{
        YT(scopedRefList) *total= PVARS(operand);
        YA_RETURN(total);
    });
    PCASE(tagLit,{
        YA_RETURN(NULL);
    });
    PCASE(numLit,{
        YA_RETURN(NULL);
    });
    PCASE(charLit,{
        YA_RETURN(NULL);
    });
    PCASE(stringLit,{
        YA_RETURN(NULL);
    });
    PCASE(boolLit,{
        YA_RETURN(NULL);
    });
    PCASE(refTerm,{
        YA_RETURN(PVARS(value));
    });
    PCASE(symbolRef,{
        YA_RETURN(NULL);
    });
    PCASE(scopedRef,{
    if (arg->scope && arg->scope->scope == NULL) {
        YA_RETURN(YBUILD(scopedRefList)(arg, NULL));
    } else {
        YA_RETURN(NULL);
    }
    });
    PCASE(outerRef,{
        YA_RETURN(NULL);
    });
}
YA_FUNC_END(plVarsExpr)

  void
parseVarOrFunction(char *prefix, YT(symbolRef) *symbolRef,
           YT(scopedRef) *scopedRef, YT(presenceImpl) *presImpl,
           exprFormEnum exprForm, bool reportErrors, char Result[])
{
    YT(exprList) *exprs = NULL;
    YT(function) *function = NULL;
    YT(ingredientRole) *role = NULL;
    YT(ingredientRoleList) *roles = presImpl->roles;

    roles = presImpl->roles;
    while (roles && !function) {
        role = roles->ingredientRole;
        if (role && role->template &&
            role->template->ingredientImpl)
            function = findPublicFunction(symbolRef->name,
                                          role->template->ingredientImpl->functions);
        roles = roles->next;
    }
    if (function) {
        /* KSS We do this in variables now; just print the variable name
           sprintf(Result, "(%s)%s).%s(",
           iiJavaName(SDNAME(role->template->ingredientImpl)),
           SNAME(scopedRef->scope->ref),
           SNAME(symbolRef));
           exprs = symbolRef->params;
           while (exprs) {
           strcat(Result,
           stringFromExpr(prefix, exprForm, MAKE_EXP, exprs->expr,
           presImpl, FALSE, reportErrors));
           exprs = exprs->next;
           }
           strcat(Result, ")");
           */
        sprintf(Result, "%s_%s",SNAME(scopedRef->scope->ref),
                SNAME(symbolRef));
    } else {
        sprintf(Result, "%s\n  no function %s in ingredient %s",
                prefix, SNAME(symbolRef), SNAME(scopedRef->scope->ref));
        if (reportErrors)
            yh_error(Result);
    }
}
