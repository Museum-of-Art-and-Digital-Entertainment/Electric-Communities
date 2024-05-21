
MODNAME	        = quake
PACKAGE	        = ec.e.quake
DOCDIR          = $(TOP)/../../www-int/intdocs/prevat-javadoc
SAFETY_MODE	= STEWARD


FILES.java = \
	Checkpointable.java \
	Revive.java \
	StableStore.java \
	StateStableStore.java

FILES.e = \
	TimeMachine.e \
	StateTimeMachine.e

FILES.dummies = \
	Dummies.java

FILES.javadoc = $(FILES.java)


include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

dummies:
	$(MAKE) basics FILES.java="$(FILES.dummies)" FILES.e=""

clean: basic_clean

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
