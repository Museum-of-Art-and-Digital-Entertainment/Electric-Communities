
MODNAME	    = serialstate
PACKAGE	    = ec.e.serialstate
DOCDIR      = $(TOP)/../../www-int/intdocs/javadoc

# Should be guest, but repository calls this before there's a vat
# SAFETY_MODE = GUEST

SAFETY_MODE = STEWARD

FILES.java = \
	StateStreams.java \
	RtSerializableTether.java \
	StateObjectReadInterest.java \
	StateObjectWriteInterest.java \
	StateUnserializer.java \
	StateSerializer.java \

FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
