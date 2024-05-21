SUBNAME = misc
MODNAME =  tests/$(SUBNAME)
PACKAGE =  ec.tests.$(SUBNAME)

include $(TOP)/E/makefiles/Defs.gmk

FILES.e = \
	Hackery.e \
	Hackery2.e \
	HackeryInterface.e

FILES.java = 

all: mk_dirs mk_java

mk_java: classes

mk_dirs:
	@echo ""
	mkdir -p $(LISTPATH)
	@echo ""

clean:
	rm -rf $(CLASSDIR)/$(PKGDIR)
	rm -rf $(LISTPATH)
	rm -rf $(CLASSLIST) $(TOUCHLIST)
	rm -f $(FILES.javat) $(FILES.et)

Hackery:
	$(MAKE) FILES.e=Hackery.e FILES.java= 

Hackery2:
	$(MAKE) FILES.e=Hackery2.e FILES.java= 

Fib:
	$(MAKE) FILES.e=Fib.e FILES.java= 

LoaderTest:
	$(MAKE) FILES.e= FILES.java=LoaderTest.java

EnDecoding:
	$(MAKE) FILES.e= FILES.java=EnDecoding.java

run:
	-$(JAVA) ec.tests.misc.Hackery
	-$(JAVA) ec.tests.misc.Hackery2
	-$(JAVA) ec.tests.misc.LoaderTest

include $(TOP)/E/makefiles/Rules.gmk

