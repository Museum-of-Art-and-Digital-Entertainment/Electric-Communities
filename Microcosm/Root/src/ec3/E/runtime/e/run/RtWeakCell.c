#include <stdio.h>
#include "StubPreamble.h"
#include "ec_e_run_RtWeakCell.h"
#include <string.h>

typedef struct Hec_e_run_RtWeakCell WeakCell;

struct Hec_e_run_RtWeakling*
ec_e_run_RtWeakCell_get (WeakCell* this) {
	return (struct Hec_e_run_RtWeakling*) (unhand(this)->target);
}

void 
ec_e_run_RtWeakCell_store (WeakCell* this, struct Hec_e_run_RtWeakling* obj) {
	(unhand(this)->target) = (long)obj;
}

