#ifndef PLROUTE_H_
#define PLROUTE_H_

#include "yh_struct.h"

YT(scopedRefList) *buildScopedVarList(char *prefix, YT(exprList) *inits);
YT(presence) *findPresence(YT(unumStructure) *struc, YT(symbol) *roleName);
YT(template) *findTemplate(YT(presenceImpl) *presImpl, YT(symbol) *roleName);
bool inScopedRefList(YT(scopedRef) *ref, YT(scopedRefList) *refs);
bool matchMethodInKind(YT(kind) *kind, YT(symbol) *message);
bool matchMethodOrKindName(char *prefix, YT(symbol) *target,
			   YT(symbol) *message, YT(scope) *contextScope);
char *pPrName(YT(unumImpl) *impl, YT(symbol) *roleName);
char *pUrName(YT(unumImpl) *impl, YT(symbol) *roleName);

#endif
