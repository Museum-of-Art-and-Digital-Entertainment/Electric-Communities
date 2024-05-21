MODNAME		= eccosmtools
PACKAGE		= ec.cosm.tools
BUILDTYPE	= lib

DOCDIR       = $(TOP)/../../www-int/engineering/API_doc/javadoc 

SAFETY_MODE = STEWARD

FILES.java = \
    TimeRep.java \

FILES.javac = \
	TimeFileRAF.java \
	TimeFile.java \
	
# Parser.java cannot be processed by javadoc since it has closures.

FILES.javadoc = \
	Curator.java \

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes 

all: basics 

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

clean: basic_clean

include $(TOP)/E/makefiles/Rules.gmk

