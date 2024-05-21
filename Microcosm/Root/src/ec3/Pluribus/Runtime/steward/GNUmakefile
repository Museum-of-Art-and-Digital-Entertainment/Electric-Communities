#
# GNUmakefile for Pluribus runtime
#

SAFETY_MODE = STEWARD

MODNAME = plrun
PACKAGE = ec.pl.runtime

FILES.java = \
	ClientPresenceTracker.java \
	Ingredient.java \
	Presence.java \
	PresenceEntry.java \
	PresenceEnvironment.java \
	PresenceRouter.java \
	SoulState.java \
	UIFramework.java \
	Unum.java \
	UnumInterest.java \
	UnumPresenceState.java \
	UnumRouter.java \
	UnumSoul.java \
	Utility.java \
	istBase.java \
	j_CapabilityGroup.java \
	j_RecoupCapability.java

FILES.e = \
	Agency.e \
	EPresence.e\
	EUnum.e\

FILES.spjava = plrunSetup.java

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes

all: basics

clean: basic_clean

include $(TOP)/E/makefiles/Rules.gmk

