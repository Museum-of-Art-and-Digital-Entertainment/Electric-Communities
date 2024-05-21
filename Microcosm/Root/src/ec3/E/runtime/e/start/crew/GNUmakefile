MODNAME	= startcrew
PACKAGE	= ec.e.start.crew
DOCDIR  = $(TOP)/../../www-int/intdocs/prevat-javadoc
SAFETY_MODE = CREW


FILES.java = \
	CrewCapabilities.java \

FILES.e = \


FILES.javadoc = $(FILES.java)


include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

dummies:
	$(MAKE) FILES.java="Dummies.java SecurityDummies.java"

clean: basic_clean

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
