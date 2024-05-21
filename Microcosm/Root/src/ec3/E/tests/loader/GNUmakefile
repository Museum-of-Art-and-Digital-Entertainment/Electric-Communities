SUBNAME = loader
MODNAME =  tests/$(SUBNAME)
PACKAGE =  ec.tests.$(SUBNAME)

include $(TOP)/E/makefiles/Defs.gmk

FILES.e = \
	HelloLoader.e \

FILES.java = \
	LoaderTest.java \
	ClassManagerTest.java \
	TestClassManager.java \

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

HelloLoader:
	$(MAKE) FILES.e=HelloLoader.e FILES.java=

LoaderTest:
	$(MAKE) FILES.java="LoaderTest.java TestClassManager.java" FILES.e=

ClassManagerTest:
	$(MAKE) FILES.java="ClassManagerTest.java TestClassManager.java" FILES.e=

TestClassManager:
	$(MAKE) FILES.java=TestClassManager.java FILES.e=

run:
	-$(JAVAEC) ec.tests.loader.HelloLoader

include $(TOP)/E/makefiles/Rules.gmk

