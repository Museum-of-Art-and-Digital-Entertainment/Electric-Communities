
MODNAME	    = jwhich
PACKAGE	    = ec.jwhich
DOCDIR      = $(TOP)/../../www-int/intdocs/javadoc
SAFETY_MODE = STEWARD

FILES.javac = \
	DirFQName.java \
	jwhich.java \
	LocatedFQName.java \
	ZipFQName.java

FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
