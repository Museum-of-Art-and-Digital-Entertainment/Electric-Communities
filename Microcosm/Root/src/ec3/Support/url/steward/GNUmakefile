MODNAME = URLLauncher
PACKAGE = ec.url.steward

include $(TOP)/E/makefiles/Defs.gmk

DOCDIR       = $(TOP)/../../www-int/intdocs/javadoc

SAFETY_MODE = STEWARD

# ECOMPFLAGS = -printe2jtree -d $(TOPDIR)

FILES.java = \
	URLLauncherMaker.java		\
	URLLauncherSteward.java

FILES.javadoc = $(FILES.java)


.PHONY: all classes exports

all: basics

clean: basic_clean

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
