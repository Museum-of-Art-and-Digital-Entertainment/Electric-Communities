MODNAME = Samp
PACKAGE = ec.samp.steward

include $(TOP)/E/makefiles/Defs.gmk

SAFETY_MODE = STEWARD

DOCDIR       = $(TOP)/../../www-int/intdocs/javadoc

# ECOMPFLAGS = -printe2jtree -d $(TOPDIR)

FILES.java = \
	SampResultHandlerInt.java \
	SampMaker.java	\
	SampCrewHolderInt.java

FILES.e = \
	SampResultHandler.e \
	SampSeismoSteward.e

FILES.javadoc = $(FILES.java)

.PHONY: all classes exports

all: basics

interfaces1:
	$(MAKE) FILES.java=SampResultHandlerInt.java FILES.e=

interfaces2:
	$(MAKE) FILES.java=SampCrewHolderInt.java FILES.e=

otherfiles:
	$(MAKE) "FILES.e = SampSeismoSteward.e SampResultHandler.e"


clean: basic_clean

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
