/*
  plingredient.c -- Ingredient output for Pluribus.

  Chip Morningstar
  Electric Communities
  18-August-1996

  Copyright 1996 Electric Communities, all rights reserved.

*/

#include "generic.h"
#include "yh.h"
#include "yh_build.h"
#include "y.tab.h"
#include "pl.h"

  static void
generateModifiers(long modifiers)
{
    if (modifiers & MOD_PUBLIC) {
        PP("public ");
    }
    if (modifiers & MOD_PROTECTED) {
        PP("protected ");
    }
    if (modifiers & MOD_PRIVATE) {
        PP("private ");
    }
    if (modifiers & MOD_STATIC) {
        PP("static ");
    }
    if (modifiers & MOD_FINAL) {
        PP("final ");
    }
}

  static void
generateFunction(YT(function) *function)
{
    PP("    ");
    generateModifiers(function->modifiers);
    if (function->resultType) {
        generateTypeSpec(function->resultType);
    } else {
        PP("*");
    }
    PP(" %s(", SNAME(function));
    generateParameterList(function->params, NULL);
    PP(") ");
    generateThrowsList(function->throws);
    generateCode(function->methodCode, NULL);
    P("");
}

  static void
generateInit(YT(method) *method)
{
    PP("    local void init(");
    generateParameterList(method->params, rtExceptionEnvParam(TRUE));
    PP(") ");
    generateThrowsList(method->throws);
    ekeepOpen();
    generateCode(method->methodCode, NULL);
    ekeepClose();
    P("");
}

  static void
generateMethod(YT(method) *method)
{
    PP("    emethod %s(", SNAME(method));
    generateParameterList(method->params, NULL);
    PP(") ");
    generateThrowsList(method->throws);
    generateCode(method->methodCode, NULL);
    P("");
}

  static void
generateStateBundle(YT(stateBundle) *stateBundle)
{
    if (stateBundle) {
        P("    /* State bundle '%s' */", SNAME(stateBundle));
        P("    private %s.%s %s%s%s;",
          stateBundle->packagename->name,
          stateBundle->typename->name,
          SNAME(stateBundle),
          stateBundle->init ? " = " : "",
          stateBundle->init ? stateBundle->init : ""
          );
    }
}

  static void
generateVariables(YT(variableList) *vars)
{
    int i = 0;
    YT(variable) *var = NULL;
    while (vars) {
        var = vars->variable;
        PP("    ");
        generateModifiers(var->modifiers);
        generateTypeSpec(var->type);
        PP(" %s", SNAME(var));
        for (i = 0; i < var->dimensions; i++) {
            PP("[]");
        }
        if (var->init) {
            PP(" = %s", var->init);
        }
        P(";");
        vars = vars->next;
    }
    P("");
}

  void
generateIngredientImpl(YT(ingredientImpl) *impl)
{
    YT(function) *function = NULL;
    YT(functionList) *functions = impl->functions;
    YT(implementsAttList) *implements = impl->implements;
    YT(method) *init = NULL;
    YT(methodList) *initBlocks = impl->initBlocks;
    YT(methodList) *methods = impl->methods;
    YT(neighborList) *neighbors = impl->neighbors;
    YT(stateBundle) *stateBundle = impl->stateBundle;

    P("public interface %s", iiJavaName(SDNAME(impl)));
    P("{");

    /* Init methods  */
    while (initBlocks) {
    init = initBlocks->method;
    PP("    void init(");

    generateParameterList(init->params, rtExceptionEnvParam(TRUE));
    P(");");
    initBlocks = initBlocks->next;
    }
    initBlocks = impl->initBlocks;

    /* Public functions */
    while (functions) {
    function = functions->function;
    if (function->modifiers & MOD_PUBLIC) {
        PP("    ");
        if (function->resultType)
        generateTypeSpec(function->resultType);
        else
        PP("*");
        PP(" %s(", SNAME(function));
        generateParameterList(function->params, NULL);
        PP(")");
        generateThrowsList(function->throws);
        P(";");
    }
    functions = functions->next;
    }
    functions = impl->functions;

    if (neighbors) {
        PP("    void setNeighbors(");
        while (neighbors) {
            YT(neighbor) *neighbor = neighbors->neighbor;
            neighbors = neighbors->next;
            PP("%s in_%s%s", kindClassName(SDNAME(neighbor->kind)),
               SNAME(neighbor), neighbors ? ", " : "");
        }
        P(");");
    }
    P("}");
    P("");

    P("public eclass %s", iiCodeName(SDNAME(impl)));
    P("extends Ingredient");
    PP("implements IngredientJif, %s", kindClassName(SDNAME(impl->kind)));
    PP(", %s", iiJavaName(SDNAME(impl)));
    while (implements) {
        PP(", %s", pSymbolRef(implements->implementsAtt->name));
        implements = implements->next;
    }
    P("");
    P("{");

    /* Instance variables */
    generateVariables(impl->vars);

    /* StateBundle */
    generateStateBundle(stateBundle);

    /* Neighbors */
    neighbors = impl->neighbors;
    if (neighbors) {
        P("    /* Neighbors */");
        while (neighbors) {
            YT(neighbor) *neighbor = neighbors->neighbor;
            P("    %s %s;",
              kindClassName(SDNAME(neighbor->kind)), SNAME(neighbor));
            neighbors = neighbors->next;
        }
        P("");
    }

    /* Constructor */
    P("    public %s(PresenceEnvironment environment) {",
      iiCodeName(SDNAME(impl)));
    P("        super(environment);");
    P("    }");
    P("");
    
    P("    public void initGeneric(Object state) {");
    if (impl->stateBundle) {
        P("        this.init((%s)state);",
          impl->stateBundle->typename->name);
    } else {
        P("        throw new RtRuntimeException(\"%s: no state bundle for initGeneric()\");",
          SDNAME(impl));
    }
    P("    }");
    P("");

    /* Neighbors */
    neighbors = impl->neighbors;
    if (neighbors) {
        PP("    local void setNeighbors(");
        while (neighbors) {
            YT(neighbor) *neighbor = neighbors->neighbor;
            neighbors = neighbors->next;
            PP("%s in_%s%s", kindClassName(SDNAME(neighbor->kind)),
               SNAME(neighbor), neighbors ? ", " : "");
        }
        P(") {");
        neighbors = impl->neighbors;
        while (neighbors) {
            YT(neighbor) *neighbor = neighbors->neighbor;
            P("        %s = in_%s;", SNAME(neighbor), neighbor->name->name);
            neighbors = neighbors->next;
        }
        P("    }");
        P("");
    }

    /* Functions */
    while (functions) {
        generateFunction(functions->function);
        functions = functions->next;
    }

    /* Methods */
    while (initBlocks) {
        generateInit(initBlocks->method);
    initBlocks = initBlocks->next;
    }
    initBlocks = impl->initBlocks;

    while (methods) {
        generateMethod(methods->method);
        methods = methods->next;
    }

    /* Data */
    /* TODO */

    P("}");
    P("");

//KSS removing class bloat    generateIngredientImplDescriptor(impl);
}

