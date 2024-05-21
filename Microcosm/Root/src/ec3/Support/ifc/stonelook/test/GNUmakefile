MODNAME	= ifcstonelooktest
PACKAGE	= ec.ifc.stonelook.test

#SAFETY_MODE = CREW
SAFETY_MODE = STEWARD

FILES.java = \
	StoneLookTester1.java \
	StoneLookTester2.java \
	StoneLookTester3.java \

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

DOCDIR       = $(TOP)/../../../www-int/engineering/API_doc/javadoc
FILES.javadoc = $(FILES.javac)

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
