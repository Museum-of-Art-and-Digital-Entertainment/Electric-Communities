MODNAME		= ecehold
PACKAGE		= ec.e.hold
BUILDTYPE	= lib

SAFETY_MODE = STEWARD

FILES.java = \
	DataHolderInterfaces.java \

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes 

all: basics

interfaces:
	(cd interfaces; $(MAKE))

clean: basic_clean

include $(TOP)/E/makefiles/Rules.gmk

