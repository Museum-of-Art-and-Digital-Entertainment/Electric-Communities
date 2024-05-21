SUBNAME = util
MODNAME =  tests/$(SUBNAME)
PACKAGE =  ec.tests.$(SUBNAME)

include $(TOP)/E/makefiles/Defs.gmk

FILES.java = \
	IntKeyHashTest.java \
	ObjKeyHashTest.java \

all: mk_dirs mk_java

mk_java: classes

mk_dirs:
	@echo ""
	mkdir -p $(LISTPATH)
	@echo ""

clean:
	rm -rf $(CLASSDIR)$(PKGDIR)
	rm -rf $(LISTPATH)
	rm -rf $(CLASSLIST) $(TOUCHLIST)
	rm -f $(FILES.javat) $(FILES.et)

IntKeyHashTest:
	$(MAKE) FILES.java=IntKeyHashTest.java FILES.e=
	$(JAVAEC) ec.tests.util.IntKeyHashTest

ObjKeyHashTest:
	$(MAKE) FILES.java=ObjKeyHashTest.java FILES.e=
	$(JAVAEC) ec.tests.util.ObjKeyHashTest

run:
	-$(JAVAEC) ec.tests.util.IntKeyHashTest
	-$(JAVAEC) ec.tests.util.ObjKeyHashTest

include $(TOP)/E/makefiles/Rules.gmk

