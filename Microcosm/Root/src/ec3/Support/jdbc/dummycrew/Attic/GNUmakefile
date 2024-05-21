MODNAME = SqlDB
PACKAGE = ec.sqldb.crew

SAFETY_MODE = CREW

DOCDIR       = $(TOP)/../../www-int/intdocs/javadoc

# ECOMPFLAGS = -printe2jtree -d $(TOPDIR)

FILES.java = 			\
	SqlDBStubs.java		\
	SqlDBRequestStubs.java

FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk

