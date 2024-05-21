#
#  Copyright 1997 Electric Communities. All rights reserved worldwide.
#

MODNAME = collect
PACKAGE = ec.ez.collect

DOCDIR = $(TOP)/../../www-int/intdocs/javadoc

SAFETY_MODE = GUEST

FILES.javac = \
	ArityMismatchException.java \
	ArrayIterator.java \
	AssociationEnumeration.java \
	DoubleInterval.java \
	DoubleIterator.java \
	IntegerIterator.java \
	Interval.java \
	Mapping.java \
	MappingImpl.java \
	NameTable.java \
	NameTableEditor.java \
	NameTableEditorImpl.java \
	NotFoundException.java \
	NullMarker.java \
	Tuple.java \
	TupleImpl.java

FILES.javadoc = $(FILES.java)


include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes

all: basics

clean: basic_clean

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
