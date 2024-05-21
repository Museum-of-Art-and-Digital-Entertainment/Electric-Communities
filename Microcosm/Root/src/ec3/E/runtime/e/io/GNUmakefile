include $(TOP)/E/makefiles/Defs.gmk

all:
	cd crew; $(MAKE) dummies
	$(MAKE) -f GNUmakefile.steward
	cd crew; $(MAKE)

clean:
	$(MAKE) -f GNUmakefile.steward clean
	cd crew; $(MAKE) clean

include $(TOP)/E/makefiles/Rules.gmk
