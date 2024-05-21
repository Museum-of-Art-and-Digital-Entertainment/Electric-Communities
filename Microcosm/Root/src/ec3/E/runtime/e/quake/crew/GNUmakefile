MODNAME	        = crew
PACKAGE	        = ec.e.quake.crew
DOCDIR          = $(TOP)/../../www-int/intdocs/prevat-javadoc
SAFETY_MODE	= CREW


FILES.java = \
	TimeMachineMaker.java \
	StateTimeMachineMaker.java

FILES.javadoc = $(FILES.java)


include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
