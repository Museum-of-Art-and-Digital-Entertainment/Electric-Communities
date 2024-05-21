SUBNAME = nu
MODNAME =  tests/$(SUBNAME)
PACKAGE =  ec.tests.$(SUBNAME)

SAFETY_MODE = STEWARD

include $(TOP)/E/makefiles/Defs.gmk

FILES.e = \
	NUTest.e \
	RockRegion.e \
	Multicaster.e \
	ECapabilities.e \


all: mk_dirs mk_java

mk_java: classes

mk_dirs:
	@echo ""
	mkdir -p $(LISTPATH)
	@echo ""

clean:
	rm -rf $(CLASSDIR)/$(PKGDIR)
	rm -rf $(LISTPATH)
	rm -rf $(CLASSLIST) $(TOUCHLIST) $(JCLASSLIST) $(JTOUCHLIST)
	rm -f $(FILES.javat) $(FILES.et) $(FILES.javact)
	rm -rf *.evat

RR:
	$(MAKE) FILES.e=NUTest.e FILES.java= 

rev:
	$(JAVAEC) ec.e.quake.Revive foo

rev1:
	$(JAVAEC) ec.e.quake.Revive foo1

run:
	-$(JAVAEC) ec.e.start.EBoot ec.tests.nu.NUTest reg=scruffy

run1:
	-$(JAVAEC) ec.e.start.EBoot ec.tests.nu.NUTest lookup=scruffy

runc:
	-$(JAVAEC) ec.e.start.EBoot ec.tests.nu.NUTest reg=scruffy checkpoint=foo

runc1:
	-$(JAVAEC) ec.e.start.EBoot ec.tests.nu.NUTest lookup=scruffy checkpoint=foo1

include $(TOP)/E/makefiles/Rules.gmk

