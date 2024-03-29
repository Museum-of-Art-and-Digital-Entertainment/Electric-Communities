/*
  plcode.c -- Code block output for Pluribus.
  
  Chip Morningstar
  Electric Communities
  18-July-1996
  
  Copyright 1996 Electric Communities, all rights reserved.
  
  */

#include "generic.h"
#include "yh.h"
#include "pl.h"
#include "y.tab.h"
#include "platform.h"

enum {
    MODE_NORMAL, MODE_NEW, MODE_UNUM, MODE_KIND, MODE_ING, MODE_ING_SYM,
    MODE_ING_CHKINIT, MODE_ING_SYM_PAREN, MODE_ADD_RTEXENV,
    MODE_ADD_COMMA_RTEXENV, MODE_A
};

static char* ModeSymbols[] = {
    "MODE_NORMAL", "MODE_NEW", "MODE_UNUM", "MODE_KIND", "MODE_ING",
    "MODE_ING_SYM", "MODE_ING_SYM_PAREN", "MODE_ING_CHKINIT",
    "MODE_ADD_RTEXENV", "MODE_ADD_COMMA_RTEXENV"
};

static bool UseMyKeeper = TRUE;
static char savedSymbol[BUFLEN];
static int SymbolScanMode = MODE_NORMAL;
static int NestedParenLevel = 0;

static bool permissiveMode();
static void printRtExceptionEnvArg(bool useMyKeeper, bool withComma);

/*  Scan in a possible comment; print a '/' and look for more to print; if
 * this was just a one '/', keep on going.
 */
  static char *
genScanComment(char *source)
{
    char prev = 'x';
    char c;
    
    PC('/');
    c = *source++;
    if (c == '*') {
        PC(c);
        do {
            prev = c;
            c = *source++;
            if (c)
                PC(c);
            else
                --source;
        } while ((prev != '*' || c != '/') && c != '\0');
        return(source);
    } else if (c == '/') {
        PC(c);
        do {
            c = *source++;
            if (c)
                PC(c);
            else
                --source;
        } while (c != '\n' && c != '\0');
        return(source);
    } else {
        return(--source);
    }
}

static char *
genScanQuote(char *source, char quote)
{
    bool backSlash = FALSE;
    PC(quote);
    do {
        char c = *source++;
        if (c == '\0') {
            return(--source);
        } else if (c == quote && !backSlash) {
            PC(quote);
            return(source);
        } else if (c == '\\') {
            PC(c);
            backSlash = TRUE;
        } else {
            PC(c);
            backSlash = FALSE;
        }
    } while (1);
}

  static char *
genScanSymbol(char *source, char c)
{
    char buf[BUFLEN];
    char *bufptr = buf;
    do {
        *bufptr++ = c;
        c = *source++;
    } while (yh_isSymbolChar(c));
    *bufptr = '\0';
    --source;
    switch (SymbolScanMode) {
    case MODE_NORMAL:
        if (strcasecmp(buf, "new") == 0) {
            SymbolScanMode = MODE_NEW;
        } else if (strcasecmp(buf, "kind") == 0) {
            SymbolScanMode = MODE_KIND;
        } else if (strcasecmp(buf, "ingredient") == 0) {
            SymbolScanMode = MODE_ING;
        } else {
            PS(buf);
        }
        break;
    case MODE_NEW:
        if (strcasecmp(buf, "unum") == 0) {
            SymbolScanMode = MODE_UNUM;
        } else if (strcasecmp(buf, "kind") == 0) {
            PP("new ", buf);
            SymbolScanMode = MODE_KIND;
        } else {
            PP("new %s", buf);
            SymbolScanMode = MODE_NORMAL;
        }
        break;
    case MODE_UNUM:
        PP("%s.createUnum", unumImplName(buf));
        SymbolScanMode = MODE_ADD_RTEXENV;
        UseMyKeeper = TRUE;
        break;
    case MODE_ADD_RTEXENV:
    case MODE_ADD_COMMA_RTEXENV:
        SymbolScanMode = MODE_ADD_COMMA_RTEXENV;
        PS(buf);
        break;
    case MODE_KIND:
        PP("%s", mangleName(buf, KIND));
        SymbolScanMode = MODE_NORMAL;
        break;
    case MODE_ING:
        SymbolScanMode = MODE_ING_SYM;
        savedSymbol[0] = '\0';
        strcat(savedSymbol, buf);
        break;
    case MODE_ING_CHKINIT:
        if (strcasecmp(buf, "init") == 0) {
            SymbolScanMode = MODE_ADD_RTEXENV;
            UseMyKeeper = FALSE;
        }
        PS(buf);
        break;
    default: break;
    }
    return(source);
}

/*  This is called when we're parsing something NOT in one of the MODE chains
 * so we need to clear out the saved stuff and set the mode back to
 * MODE_NORMAL.  So far this happens on a '"', '\'', ')', or '.'  This is
 * not done if we're in a permissive mode (see permissiveMode().
 */
  static void
checkModes()
{
    switch (SymbolScanMode) {
    case MODE_NORMAL:  break;
    case MODE_NEW:     PP("new"); break;
    case MODE_UNUM:    PP("new unum"); break;
    case MODE_KIND:    PP("kind"); break;
    case MODE_ING:     PP("ingredient"); break;
    case MODE_ING_SYM:
        PP("ingredient %s", savedSymbol);
        savedSymbol[0] = '\0';
        break;
    case MODE_ING_SYM_PAREN:
        PP("ingredient %s)", savedSymbol);
        savedSymbol[0] = '\0';
        break;
    case MODE_ADD_RTEXENV: break;
    case MODE_ADD_COMMA_RTEXENV: break;
    default:
        yh_error("Invalid SymbolScanMode '%d' in checkModes()",
                 SymbolScanMode);
        break;
    }

    /*  The RtExceptionEnv modes do not need to end with the advent of one
     * of the characters so far causing a call to checkModes(), so we do
     * NOT set the mode back to MODE_NORMAL. */
    if (!permissiveMode()) {
        SymbolScanMode = MODE_NORMAL;
    }
}

  void
generateCode(char *source, YT(ingredientRoleList) *roles)
{
    YT(ingredientRole) *role = NULL;
    char c;

    SymbolScanMode = MODE_NORMAL;
    
    do {
        c = *source++;
        if (yh_isSymbolStartChar(c)) {
            /*  If we this is a symbol, scan it in */
            source = genScanSymbol(source, c);
        } else if (c == '/') {
            /*  Scan a possible comment or lone '/' */
            source = genScanComment(source);
        } else if (c == '"') {
            /*  Scan in until the next '"' */
            checkModes();
            source = genScanQuote(source, '"');
        } else if (c == '\'') {
            /*  Scan in until the next ''' */
            checkModes();
            source = genScanQuote(source, '\'');
        } else if (c == '(') {
            PC(c);
            NestedParenLevel++;
        } else if (c == ')') {
            if (SymbolScanMode == MODE_ING_SYM) {
                /*  If we've read in "ingredient foo(..." */
                SymbolScanMode = MODE_ING_SYM_PAREN;
            } else if ((SymbolScanMode == MODE_ADD_RTEXENV) ||
                       (SymbolScanMode == MODE_ADD_COMMA_RTEXENV)) {
                if (NestedParenLevel == 1) {
                    /*  If we've read in a function requiring us to add the arg
                     * for the local RtExceptionEnv, do so.
                     */
                    printRtExceptionEnvArg(UseMyKeeper,
                        MODE_ADD_COMMA_RTEXENV == SymbolScanMode);
                    SymbolScanMode = MODE_NORMAL;
                }
                PC(c);
            } else {
                checkModes();
                PC(c);
            }
            NestedParenLevel--;
        } else if (c == '.') {
            if (SymbolScanMode == MODE_ING_SYM ||
                SymbolScanMode == MODE_ING_SYM_PAREN) {
                role = findIngredientRole(yh_handleSymbol(savedSymbol),
                                          roles);
                if (role) {
                    if (role->template && role->template->ingredientImpl) {
                        PP("%c(%s)%s).",
                           SymbolScanMode == MODE_ING_SYM ? '(' : ' ',
                           iiJavaName(SDNAME(role->template->ingredientImpl)), savedSymbol);
                    } else {
                        PP("Can't generate code: no role->template or role->template->ingredientImpl");
                    }
                    savedSymbol[0] = '\0';
                    SymbolScanMode = MODE_ING_CHKINIT;
                } else {
                    checkModes();
                    PC(c);
                }
            } else {
                checkModes();
                PC(c);
            }
        } else {
            if (!yh_isWhitespace(c)) {
                checkModes();
                PC(c);
            } else if (permissiveMode()) {
                PC(c);
            }
        }
    } while (*source);
}

  void
generateJavaCode(YT(codeAtt) *codeAtt)
{
    YT(codeInherit) *inherit = NULL;
    YT(codeInheritList) *inherits = codeAtt->inherits;
    YT(codeModifierList) *modifiers = codeAtt->modifiers;
    YT(pluribusType) *parent = NULL;
    YT(pluribusTypeList) *parents = NULL;
    
    PP("    ");
    while (modifiers) {
        switch(modifiers->codeModifier->type) {
        case 0:     break;
        case ABSTRACT: PP("abstract "); break;
        case FINAL:    PP("final "); break;
        case PUBLIC:   PP("public "); break;
        default: yh_error("Uknown Java code modifier %d",
                          modifiers->codeModifier->type);
        }
        modifiers = modifiers->next;
    }
    
    switch(codeAtt->type) {
    case CLASS:      PP("class "); break;
    case INTERFACE:  PP("interface "); break;
    case ECLASS:     PP("eclass "); break;
    case EINTERFACE: PP("einterface "); break;
    default: yh_error("Uknown Java code type %d", codeAtt->type);
    }
    
    PP("%s", SDNAME(codeAtt));
    while (inherits) {
        P("");
        inherit = inherits->codeInherit;
        parents = inherit->parents;
        PP("    %s ", inherit->type == EXTENDS ? "extends" : "implements");
        while (parents) {
            parent = parents->pluribusType;
            PP("%s%s", mangleName(SNAME(parent->type), parent->mangle),
               parents->next ? ", " : "");
            parents = parents->next;
        }
        inherits = inherits->next;
    }
    PP(" ");
    generateCode(codeAtt->methodCode, NULL);
    P("");
}

/*  permissiveMode() returns TRUE if the current mode allows just about any
 * token, FALSE if otherwise.
 */
  static bool
permissiveMode() {
    switch (SymbolScanMode) {
    case MODE_NORMAL:
    case MODE_ADD_RTEXENV:
    case MODE_ADD_COMMA_RTEXENV:
        return TRUE;
    default:
        return FALSE;
    }
}

  static void
printRtExceptionEnvArg(bool useMyKeeper, bool withComma) {
#ifdef PLRTEXENV
    PP("%s%s", withComma ? ", " : "", useMyKeeper ? "myKeeper()" : "ee_$_");
#endif
}
