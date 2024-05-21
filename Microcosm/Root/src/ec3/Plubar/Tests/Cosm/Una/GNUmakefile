PACKAGE = ec.plubar.tests.cosm.una
MODNAME = plubarCosmUna
PLUBARCOSM = $(TOP)/Plubar/Tests/Cosm

include $(TOP)/E/makefiles/Defs.gmk

all:
	cd $(PLUBARCOSM)/una/Prop; $(MAKE)
	cd $(PLUBARCOSM)/una/Swatch; $(MAKE)

karl:

clean:
	cd $(PLUBARCOSM)/una/Prop; $(MAKE) clean
	cd $(PLUBARCOSM)/una/Swatch; $(MAKE) clean

again: clean all

kagain: clean karl

include $(TOP)/E/makefiles/Rules.gmk


