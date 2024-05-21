PACKAGE = ec.cosm.ingredients.describe
MODNAME = describe
PLUBARCOSM = $(TOP)/Plubar/Tests/Cosm

include $(TOP)/E/makefiles/Defs.gmk

all: kinds states ingrs

kinds:
	cd $(PLUBARCOSM)/ingredients/behavior; $(MAKE) kind
	cd $(PLUBARCOSM)/ingredients/describe; $(MAKE) kind
	cd $(PLUBARCOSM)/ingredients/destination; $(MAKE) kind
	cd $(PLUBARCOSM)/ingredients/property; $(MAKE) kind
	cd $(PLUBARCOSM)/ingredients/texturize; $(MAKE) kind

states:
	cd $(PLUBARCOSM)/ingredients/behavior; $(MAKE) state
	cd $(PLUBARCOSM)/ingredients/describe; $(MAKE) state
	cd $(PLUBARCOSM)/ingredients/destination; $(MAKE) state
	cd $(PLUBARCOSM)/ingredients/property; $(MAKE) state
	cd $(PLUBARCOSM)/ingredients/texturize; $(MAKE) state

ingrs:
	cd $(PLUBARCOSM)/ingredients/behavior; $(MAKE) ingr
	cd $(PLUBARCOSM)/ingredients/describe; $(MAKE) ingr
	cd $(PLUBARCOSM)/ingredients/destination; $(MAKE) ingr
	cd $(PLUBARCOSM)/ingredients/property; $(MAKE) ingr
	cd $(PLUBARCOSM)/ingredients/texturize; $(MAKE) ingr

everything:
	cd $(PLUBARCOSM)/ingredients/behavior; $(MAKE)
	cd $(PLUBARCOSM)/ingredients/describe; $(MAKE)
	cd $(PLUBARCOSM)/ingredients/destination; $(MAKE)
	cd $(PLUBARCOSM)/ingredients/property; $(MAKE)
	cd $(PLUBARCOSM)/ingredients/texturize; $(MAKE)

karl:

clean:
	cd $(PLUBARCOSM)/ingredients/behavior; $(MAKE) clean
	cd $(PLUBARCOSM)/ingredients/describe; $(MAKE) clean
	cd $(PLUBARCOSM)/ingredients/destination; $(MAKE) clean
	cd $(PLUBARCOSM)/ingredients/property; $(MAKE) clean

again: clean all

kagain: clean karl

include $(TOP)/E/makefiles/Rules.gmk


