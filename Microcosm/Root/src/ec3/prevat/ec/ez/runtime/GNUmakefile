#
#  Copyright 1997 Electric Communities. All rights reserved worldwide.
#

MODNAME = runtime
PACKAGE = ec.ez.runtime

DOCDIR = $(TOP)/../../www-int/intdocs/javadoc

SAFETY_MODE = GUEST


FILES.javac = \
	AlreadyDefinedException.java \
	EZObject.java \
	Ejection.java \
	Ejector.java \
	MethodNode.java \
	ParseNode.java \
	Script.java \
	SourceSpan.java


FILES.javadoc = $(FILES.java)


include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes

all: basics

clean: basic_clean

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
