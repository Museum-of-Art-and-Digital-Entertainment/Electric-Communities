
MODNAME		= guest
PACKAGE		= ec.e.openers.guest
DOCDIR          = $(TOP)/../../www-int/intdocs/javadoc
SAFETY_MODE	= STEWARD

FILES.java = \
	AllowingClassRecipe.java \
	ParamRecipe.java \
	SerializableMarker.java \
	SharpeningClassRecipe.java \
	SingletonRecipe.java

FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
