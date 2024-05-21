SUBNAME = template
MODNAME =  tests/$(SUBNAME)
PACKAGE =  ec.tests.$(SUBNAME)

include $(TOP)/E/makefiles/Defs.gmk

FILES.e = \
	ETemplateTest.e \

FILES.java = \
	TemplateTest.java \

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

TemplateTest:
	$(MAKE) FILES.java=TemplateTest.java FILES.e=

ETemplateTest:
	$(MAKE) FILES.e=ETemplateTest.e FILES.java=

run:
	-$(JAVAEC) ec.tests.template.TemplateTest
	-$(JAVAEC) ec.tests.template.ETemplateTest

include $(TOP)/E/makefiles/Rules.gmk

