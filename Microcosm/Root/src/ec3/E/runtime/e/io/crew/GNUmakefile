
MODNAME	= iocrew
PACKAGE	= ec.e.io.crew

SAFETY_MODE = CREW

FILES.e = \
	RtConsole.e

FILES.dummies = \
	RtConsoleDummy.e

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

dummies:
	$(MAKE) basics FILES.e="$(FILES.dummies)"

clean: basic_clean

include $(TOP)/E/makefiles/Rules.gmk
