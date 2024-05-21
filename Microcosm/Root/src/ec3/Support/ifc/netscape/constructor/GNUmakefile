MODNAME	= ifcapp
PACKAGE	= netscape.application

SAFETY_MODE = CREW

FILES.javac = \
		Constructor.java \
		Plan.java \
		PlanLoader.java \
		TargetProxy.java \
		TargetProxyManager.java \
		ViewProxy.java \

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

DOCDIR       = $(TOP)/../../../www-int/engineering/API_doc/javadoc
FILES.javadoc = $(FILES.javac)

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
