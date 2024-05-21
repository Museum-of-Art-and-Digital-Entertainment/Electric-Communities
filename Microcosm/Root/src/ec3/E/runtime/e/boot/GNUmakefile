
MODNAME	= boot
PACKAGE	= ec.e.boot
DOCDIR  = $(TOP)/../../www-int/intdocs/prevat-javadoc
SAFETY_MODE = STEWARD

FILES.javac = \

FILES.java = \
	EBoot.java \
	ELogin.java

FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all:	basics

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
