PACKAGE = ec.plubar.tests.cosm.una.swatch
MODNAME = plubarCosmUnaSwatch

include $(TOP)/E/makefiles/Defs.gmk

all:
	$(MAKE) basics \
		FILES.java=" \
			SwatchUnumDef.java \
			SwatchUnum.java \
			TestSwatchUnum.java \
			"

karl:
	$(MAKE) basics \
		FILES.java=" \
			SwatchUnumDef.java \
			"

clean: basic_clean

again: clean all

kagain: clean karl

include $(TOP)/E/makefiles/Rules.gmk


