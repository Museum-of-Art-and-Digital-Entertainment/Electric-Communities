PACKAGE = ec.util.assertion
MODNAME = util

FILES.javac = \
	Assertion.java \
	AssertionFailed.java

include $(TOP)/E/makefiles/Defs.gmk

all: mk_dirs classes

mk_dirs:
	mkdir -p $(FULLPATH)
	mkdir -p $(LISTPATH)

clean: basic_clean

include $(TOP)/E/makefiles/Rules.gmk

