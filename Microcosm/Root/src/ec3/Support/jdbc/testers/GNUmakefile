MODNAME = SqlDBTester
PACKAGE = ec.sqldb.tester

DOCDIR       = $(TOP)/../../www-int/intdocs/javadoc

SAFETY_MODE = GUEST

# ECOMPFLAGS = -printe2jtree -d $(TOPDIR)

FILES.e = \
	SqlDBTester.e

FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

Test:
	rm -rf test.evat
	$(JAVAEC) ec.e.start.EBoot ec.sqldb.tester.SqlDBTester checkpoint=test

Revive:
	$(JAVAEC) ec.e.quake.Revive test

include $(TOP)/E/makefiles/Rules.gmk



