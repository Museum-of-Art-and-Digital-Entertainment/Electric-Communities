
MODNAME	    = serial
PACKAGE	    = ec.e.serial
DOCDIR      = $(TOP)/../../www-int/intdocs/javadoc

# Should be guest, but repository calls this before there's a vat
# SAFETY_MODE = GUEST

SAFETY_MODE = STEWARD

FILES.java = \
	Serializer.java \
	Unserializer.java \
	ParamSerializer.java \
	ParamUnserializer.java

FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
