PACKAGE = ec.plubar.tests.cosm.una.prop
MODNAME = plubarCosmUnaProp

include $(TOP)/E/makefiles/Defs.gmk

all:
	$(MAKE) basics \
		FILES.java=" \
			PropUnumDef.java \
			PropUnum.java \
			TestPropUnum.java \
			"

karl:
	$(MAKE) basics \
		FILES.java=" \
			PropUnumDef.java \
			"

clean: basic_clean

again: clean all

kagain: clean karl

include $(TOP)/E/makefiles/Rules.gmk


