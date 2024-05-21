MODNAME = Samp
PACKAGE = ec.samp.compute

include $(TOP)/E/makefiles/Defs.gmk

SAFETY_MODE = COMPUTE

DOCDIR       = $(TOP)/../../www-int/intdocs/javadoc

# ECOMPFLAGS = -printe2jtree -d $(TOPDIR)

FILES.java = \
	TestResults.java 

FILES.javadoc = $(FILES.java)

.PHONY: all classes exports

all: basics

clean: basic_clean

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
