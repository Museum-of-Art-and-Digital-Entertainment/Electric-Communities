MODNAME		= state
PACKAGE		= ec.state

DOCDIR          = $(TOP)/../../www-int/intdocs/javadoc

SAFETY_MODE = STEWARD

FILES.java = \
	ReadableStateDictionary.java \
	StateAwakener.java \
	StateDictionary.java \
	StateDictionaryParsingException.java \
	Stateful.java \
	WriteableStateDictionary.java \

FILES.javadoc = \
	ReadableStateDictionary.java \
	StateAwakener.java \
	StateDictionary.java \
	StateDictionaryParsingException.java \
	Stateful.java \
	WriteableStateDictionary.java \

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes 

all: basics

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

clean: basic_clean

include $(TOP)/E/makefiles/Rules.gmk

