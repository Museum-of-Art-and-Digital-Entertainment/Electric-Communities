#
# Makefile for ec.e.util Java/E utility classes.
#
#  Chip Morningstar
#  Electric Communities
#  7-March-1997
#
#  Copyright 1997 Electric Communities. All rights reserved worldwide.
#

MODNAME = util
PACKAGE = ec.e.util

DOCDIR = $(TOP)/../../www-int/intdocs/javadoc

SAFETY_MODE = COMPUTE


FILES.javac = \
	Vector.java \
	ArrayEnumeration.java \
	CompoundEnumeration.java \
	DiscreteEnumeration.java \
	EmptyEnumeration.java \
	SetCollection.java \
	ExpireCollection.java \
	ExpireCollectionEnumeratorEntry.java \
	SimpleQueue.java \
	SimpleQueueEntry.java \
	SimpleQueueReader.java \
	SimpleQueueWriter.java

FILES.javadoc = $(FILES.java)


include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes

all: basics

clean: basic_clean

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
