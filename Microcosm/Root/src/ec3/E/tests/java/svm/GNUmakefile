SUBNAME = svm
MODNAME =  tests/java/$(SUBNAME)
PACKAGE =  ec.tests.java.$(SUBNAME)
TESTER = Shaker

JOPTIONS = -Dseismology=quake -Djava.svm.trace=quaking
# JOPTIONS = -tquake
# JOPTIONS = -t -tm -v -verbosegc

include $(TOP)/E/makefiles/Defs.gmk

FILES.java = \
	$(TESTER).java

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

$(TESTER):
	$(MAKE) FILES.java=$(TESTER).java

run:
	java_g $(JOPTIONS) $(PACKAGE).$(TESTER) foo bar
#	-$(JAVAEC) $(JOPTIONS) $(PACKAGE).$(TESTER) foo bar

include $(TOP)/E/makefiles/Rules.gmk


