MODNAME = SampTester
PACKAGE = ec.samp.tester

DOCDIR       = $(TOP)/../../www-int/intdocs/javadoc

SAFETY_MODE = GUEST

# ECOMPFLAGS = -printe2jtree -d $(TOPDIR)

FILES.e = \
	SampTester.e

FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

Test:
	rm -rf test.evat
	$(JAVAEC) ec.e.start.EBoot ec.samp.tester.SampTester checkpoint=test

Revive:
	$(JAVAEC) ec.e.quake.Revive test

include $(TOP)/E/makefiles/Rules.gmk



