SUBNAME = run
MODNAME =  tests/$(SUBNAME)
PACKAGE =  ec.tests.$(SUBNAME)

include $(TOP)/E/makefiles/Defs.gmk

FILES.e = \

FILES.java = \

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

include $(TOP)/E/makefiles/Rules.gmk

