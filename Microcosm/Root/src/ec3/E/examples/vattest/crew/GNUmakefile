MODNAME		= Samp
PACKAGE		= ec.samp.crew

include $(TOP)/E/makefiles/Defs.gmk

SAFETY_MODE = CREW

DOCDIR          = $(TOP)/../../www-int/intdocs/javadoc

FILES.java = \
	Samp.java \
	SampCrewThread.java

FILES.javadoc = $(FILES.java)

.PHONY: all classes exports

all: basics

crewlast:
	$(MAKE) FILES.java=SampMagicPowerMaker.java

clean: basic_clean

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk

