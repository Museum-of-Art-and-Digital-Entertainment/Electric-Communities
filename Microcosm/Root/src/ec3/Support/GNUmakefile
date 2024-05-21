include $(TOP)/E/makefiles/Defs.gmk

all:
	cd $(TOP)/Support/misc; $(MAKE)
	cd $(TOP)/Support/misc/graphics; $(MAKE)
	cd $(TOP)/Support/misc/cache; $(MAKE)
#	cd $(TOP)/Support/misc/native; $(MAKE)
	cd $(TOP)/Support/ifc; $(MAKE)
	cd $(TOP)/Support/ui/util; $(MAKE)
	cd $(TOP)/Support/ui/Inspector; $(MAKE)
	cd $(TOP)/Support/url; $(MAKE)
	cd $(TOP)/Support/awt; $(MAKE)

clean:
	cd $(TOP)/Support/misc; $(MAKE) clean
	cd $(TOP)/Support/misc/graphics; $(MAKE) clean
	cd $(TOP)/Support/misc/cache; $(MAKE) clean
#	cd $(TOP)/Support/misc/native; $(MAKE) clean
	cd $(TOP)/Support/ifc; $(MAKE) clean
	cd $(TOP)/Support/ui/util; $(MAKE) clean
	cd $(TOP)/Support/ui/Inspector; $(MAKE) clean
	cd $(TOP)/Support/url; $(MAKE) clean
	cd $(TOP)/Support/awt; $(MAKE) clean

include $(TOP)/E/makefiles/Rules.gmk
