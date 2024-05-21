#
#  Copyright 1997 Electric Communities. All rights reserved worldwide.
#

MODNAME = ezvm
PACKAGE = ec.ez.ezvm

DOCDIR = $(TOP)/../../www-int/intdocs/javadoc

SAFETY_MODE = STEWARD


FILES.java = \
	AssignExpr.java \
	CallExpr.java \
	CondAndExpr.java \
	DefineExpr.java \
	DispatchExpr.java \
	EnterPovExpr.java \
	EqualsExpr.java \
	EscapeExpr.java \
	Expr.java \
	EZImpl.java \
	EZMethodNode.java \
	IfExpr.java \
	LetExpr.java \
	LiteralExpr.java \
	LoopExpr.java \
	MatchBindExpr.java \
	MatchNode.java \
	NounExpr.java \
	PathExpr.java \
	Pattern.java \
	PovExpr.java \
	RequestNode.java \
	SendExpr.java \
	SequenceExpr.java \
	ThrowExpr.java \
	TryExpr.java

FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

# ECFLAGS="-e2jdone"

.PHONY: all classes

all: basics

clean: basic_clean
	rm -f $(BUILD)/steward/classes/ec/e/run/EZServiceEnvelope.class
	rm -f $(BUILD)/steward/classes/ec/e/run/EZEnvelope.class

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
