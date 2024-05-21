MODNAME =  run
PACKAGE   = ec.e.run
BUILDTYPE = lib

SAFETY_MODE = STEWARD

FILES.e = \
	ETimer.e \
	Timer.e

FILES.dummies = \
	TimerDummies.java

DOCDIR       = $(TOP)/../../www-int/intdocs/javadoc

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

dummies:
	$(MAKE) basics FILES.java="$(FILES.dummies)" FILES.e=""

clean: basic_clean

include $(TOP)/E/makefiles/Rules.gmk
