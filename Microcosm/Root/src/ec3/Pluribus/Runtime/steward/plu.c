#include <stdio.h>
#include "StubPreamble.h"
#include "ec_pl_runtime_Plu.h"
#include <string.h>

/* Native C code implementation YourClass.c */
  extern struct Hjava_lang_Object*
ec_pl_runtime_Plu_getSelfForClass (struct Hec_pl_runtime_Plu* nada,
                   struct Hjava_lang_Class* theClass)
{
    int i;
    if (theClass) {
        ClassClass* c = unhand(theClass);
        for (i = 0; i < c->fields_count; i++) {
            struct fieldblock* field = c->fields + i;
            if ((field->access & ACC_STATIC) == 0) continue;
            if (strcmp(field->name, "self") == 0) {
                return (struct Hjava_lang_Object*)field->u.static_value;
            }
        }
    }
    return NULL;
}
