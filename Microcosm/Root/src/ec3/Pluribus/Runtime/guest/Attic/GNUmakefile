#
# GNUmakefile for Pluribus runtime
#

SAFETY_MODE = GUEST

INSTALL_DIR = $(BUILD)/bin
MODNAME = plrun
PACKAGE = ec.pl.runtime
BUILDTYPE = lib

FILES.c = plu.c

FILES.e = Agency.e

FILES.java = plu.java

FILES.spjava = plrunSetup.java

CLASSES.export = $(PACKAGE).Plu

include $(TOP)/E/makefiles/Defs.gmk

INCLDIRS = -I$(JAVAINC) -I$(JAVAOSINC) -I$(CHEADERDIR)

CFLAGS += $(INCLDIRS)
OTHER_LIBS = $(RUNLIB)

.PHONY: all classes exports

all: basics exports $(GENLIBS)

exports: $(FULLSTUBNAME) $(FULLSTUBONAME) $(CHEADERSTATE)

clean: basic_clean
	rm -rf $(GENLIBS)
	rm -rf $(CLASSLIST) $(TOUCHLIST)

include $(TOP)/E/makefiles/Rules.gmk

