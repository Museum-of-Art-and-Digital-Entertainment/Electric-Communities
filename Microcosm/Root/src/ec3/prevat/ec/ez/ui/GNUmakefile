#
#  Copyright 1997 Electric Communities. All rights reserved worldwide.
#

MODNAME = ui
PACKAGE = ec.ez.ui

DOCDIR = $(TOP)/../../www-int/intdocs/javadoc

#
# XXX Should be split into STEWARD and CREW
#
SAFETY_MODE = STEWARD

#
# XXX Note that EZStart is actually in another package
#
FILES.java = \
	Listener.java \
	ListenerFrame.java \
	EZStart.java \
	EZMain.java


FILES.javadoc = $(FILES.java)


include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes

all: basics

clean: basic_clean
	rm -f $(BUILD)/steward/classes/ec/e/start/ezdummy.class
	rm -f $(BUILD)/steward/classes/ec/e/start/EZstart.class

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
