MODNAME		= ecedb
PACKAGE		= ec.e.db

DOCDIR       = $(TOP)/../../www-int/intdocs/javadoc

SAFETY_MODE = STEWARD

FILES.java = \
	RtSpecialObjectCoder.java \
	RtEncoder.java \

FILES.javadoc = $(FILES.java)


include $(TOP)/E/makefiles/Defs.gmk


.PHONY: all classes exports

all: basics


clean: basic_clean

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk

