SUBNAME = db
MODNAME =  tests/$(SUBNAME)
PACKAGE =  ec.tests.$(SUBNAME)

include $(TOP)/E/makefiles/Defs.gmk

FILES.java = \
	ParimeterTest.java \
	PropTest.java \
	StreamTest.java \

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

StreamTest:
	$(MAKE) FILES.java=StreamTest.java FILES.e=

ParimeterTest:
	$(MAKE) FILES.java=ParimeterTest.java FILES.e=

PropTest:
	$(MAKE) FILES.java=PropTest.java FILES.e=

run:
	-$(JAVAEC) ec.tests.db.ParimeterTest 
	-$(JAVAEC) ec.tests.db.StreamTest 

include $(TOP)/E/makefiles/Rules.gmk


