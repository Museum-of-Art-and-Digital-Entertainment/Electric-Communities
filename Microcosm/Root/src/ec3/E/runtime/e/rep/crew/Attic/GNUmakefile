MODNAME		= ecerepcrew
PACKAGE		= ec.e.rep.crew
BUILDTYPE	= lib

DOCDIR       = $(TOP)/../../www-int/engineering/API_doc/javadoc 

SAFETY_MODE = CREW

FILES.pass1.java = \
	Dummies.java

FILES.java = \
	CrewRepository.java \

FILES.javadoc = \
	CrewRepository.java \

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes 

all: basics 

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

clean: basic_clean

include $(TOP)/E/makefiles/Rules.gmk

