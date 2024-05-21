#include <stdio.h>
#include "StubPreamble.h"
#include "ec_e_run_WeakCell.h"
#include <string.h>

typedef struct Hec_e_run_WeakCell WeakCell;

JHandle *weakCellGetTarget (WeakCell *obj);
void weakCellSetTarget (WeakCell *obj, JHandle *target);
void weakCellSetWeakness (WeakCell *obj, long isLocal);

struct Hjava_lang_Object*
ec_e_run_WeakCell_get (WeakCell* this) {
  return (weakCellGetTarget (this));
}

void 
ec_e_run_WeakCell_set (WeakCell* this, struct Hjava_lang_Object* obj) 
{
  weakCellSetTarget (this, obj);
}


void
ec_e_run_WeakCell_setLocal (WeakCell* this, long isLocal) {
  weakCellSetWeakness (this, isLocal);
}








