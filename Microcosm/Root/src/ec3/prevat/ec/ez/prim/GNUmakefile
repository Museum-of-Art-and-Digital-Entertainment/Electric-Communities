#
#  Copyright 1997 Electric Communities. All rights reserved worldwide.
#

MODNAME = prim
PACKAGE = ec.ez.prim

DOCDIR = $(TOP)/../../www-int/intdocs/javadoc

SAFETY_MODE = STEWARD


FILES.java = \
	ConstructorNode.java \
	EZBoolean.java \
	EZDouble.java \
	EZEObjectValueInterface.java \
	EZEObjectWrapper.java \
	EZEnumeration.java \
	EZInteger.java \
	EZPromiseBreaker.java \
	EZSealer.java \
	EZStaticWrapper.java \
	EZString.java \
	EZUniversal.java \
	InstanceMethodNode.java \
	JavaMemberNode.java \
	PackagePath.java \
	PrintFunc.java \
	PrintlnFunc.java \
	RespondActivator.java \
	RenamedInstanceMethodNode.java \
	ScriptMaker.java \
	SimpleScript.java \
	StaticMethodNode.java \
	SugarMethodNode.java \
	TupleMaker.java \


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
