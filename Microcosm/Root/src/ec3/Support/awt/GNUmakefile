MODNAME	= ifcapp
PACKAGE	= ec.ifc.app

SAFETY_MODE = CREW

FILES.javac = \
	AWTFinalizeHack.java

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

DOCDIR       = $(TOP)/../../../www-int/engineering/API_doc/javadoc
FILES.javadoc = $(FILES.javac)

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk

