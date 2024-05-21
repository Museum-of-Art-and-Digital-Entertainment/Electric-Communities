#
# Makefile for ec.e.util Java/E utility classes.
#
#  Chip Morningstar
#  Electric Communities
#  7-March-1997
#
#  Copyright 1997 Electric Communities. All rights reserved worldwide.
#

MODNAME = crew
PACKAGE = ec.e.util.crew
DOCDIR = $(TOP)/../../www-int/intdocs/prevat-javadoc
SAFETY_MODE = CREW


FILES.java = \
	ByteArrayKeyGenerator.java \
	Queue.java \
	PropUtil.java

FILES.javadoc = $(FILES.java)


include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes

all: basics

clean: basic_clean

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
