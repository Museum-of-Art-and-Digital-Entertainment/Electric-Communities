#
# Makefile for ec.security.crew
#
#  Copyright 1997 Electric Communities. All rights reserved worldwide.
#

MODNAME = crew
PACKAGE = ec.security.crew
DOCDIR = $(TOP)/../../www-int/intdocs/prevat-javadoc
SAFETY_MODE = CREW


FILES.java = \
	SecureRandomCrew.java \
	TimerJitterEntropy.java

FILES.dummies = \
	Dummies.java

FILES.javadoc = $(FILES.java)


include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes

all: basics

dummies:
	$(MAKE) basics FILES.java="$(FILES.dummies)" FILES.e=""

clean: basic_clean

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
