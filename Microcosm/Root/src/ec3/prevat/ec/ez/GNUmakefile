include $(TOP)/E/makefiles/Defs.gmk

all:
	cd runtime; $(MAKE)
	cd collect; $(MAKE)
	cd prim; $(MAKE)
	cd ezvm; $(MAKE)
	cd syntax; $(MAKE)
	cd ui; $(MAKE)

clean:
	cd runtime; $(MAKE) clean
	cd collect; $(MAKE) clean
	cd prim; $(MAKE) clean
	cd ezvm; $(MAKE) clean
	cd syntax; $(MAKE) clean
	cd ui; $(MAKE) clean

include $(TOP)/E/makefiles/Rules.gmk
