
MODNAME	    = tables
PACKAGE	    = ec.tables
DOCDIR      = $(TOP)/../../www-int/intdocs/javadoc
SAFETY_MODE = STEWARD

FILES.java = \
	ArgsHolder.java \
	Column.java \
	ColumnEnumeration.java \
	EqualityKeyColumn.java \
	IdentityKeyColumn.java \
	IntColumn.java \
	IntTable.java \
	KeyColumn.java \
	RefColumn.java \
	ScalarColumn.java \
	ShareCount.java \
	SimTable.java \
	Table.java \
	VoidColumn.java

FILES.e = \
	NextOfKin.e

FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
