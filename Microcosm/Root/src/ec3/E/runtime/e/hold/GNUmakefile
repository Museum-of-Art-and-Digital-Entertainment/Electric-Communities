MODNAME		= ecehold
PACKAGE		= ec.e.hold
BUILDTYPE	= lib

DOCDIR       = $(TOP)/../../www-int/engineering/API_doc/javadoc 

SAFETY_MODE = STEWARD

FILES.pass1.java = \
	DataHolderInterfaces.java \


FILES.java = \
	DataHolderSteward.java \
	DataHolderRecipe.java \
	Fulfiller.java \
	NullFulfillerMaker.java \
	PublishFulfillerMaker.java \
	ReposableMarker.java


FILES.javadoc = \
	DataHolderInterfaces.java \
	DataHolderSteward.java \
	Fulfiller.java \


include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes 

all: pass1basics basics

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

clean: basic_clean

include $(TOP)/E/makefiles/Rules.gmk

