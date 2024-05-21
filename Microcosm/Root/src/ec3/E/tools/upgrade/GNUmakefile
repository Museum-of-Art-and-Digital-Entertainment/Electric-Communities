
MODNAME	    = upgrade
PACKAGE	    = ec.tools.upgrade
DOCDIR      = $(TOP)/../../www-int/intdocs/javadoc
SAFETY_MODE = STEWARD

FILES.javac = \
	DuplicateClasses.java \
	DuplicateClassesCmd.java \
	FindFile.java \
	Inheriters.java \
	InheritersCmd.java \
	Path.java \
	PathElement.java \
	PathTest.java \
	Sort.java \
	SortCompare.java \
	Xref.java \
	XrefCmd.java

FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: submake basics

submake:
	cd $(SRC)/evm1/ec/transform/classparser; $(MAKE)

clean: basic_clean

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
