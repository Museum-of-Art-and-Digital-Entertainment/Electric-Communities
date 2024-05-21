PACKAGE		= ec.vcache
MODNAME		= vcache
SAFETY_MODE     = STEWARD

# Note: if you are changing the set of files made by this makefile,
# then you need to make sure you haven't broken the "minimal make"
# located in E/runtime. Please do a gnumake clean followed by
# "minimal-make" and then "minimal-test". Do not check in unless
# those work.

FILES.javac = \
	BooleanCache.java \
	ByteCache.java \
	CharCache.java \
	ClassCache.java \
	DoubleCache.java \
	FloatCache.java \
	IntCache.java \
	LongCache.java \
	ShortCache.java \
	VCache.java

include $(TOP)/E/makefiles/Defs.gmk

INCLDIRS = -I$(JAVAINC) -I$(JAVAOSINC) -I$(CHEADERDIR)

CFLAGS += $(INCLDIRS)

.PHONY: all classes 

all: mk_dirs mk_java 

clean:
	rm -rf $(FULLPATH)
	rm -rf $(GENLIBS)
	rm -rf $(LISTPATH)
	rm -rf $(CLASSLIST) $(TOUCHLIST)
	rm -f $(FILES.javat) $(FILES.spjavat) $(FILES.et)

mk_java: classes

mk_dirs:
	@echo ""
	mkdir -p $(FULLPATH)
	mkdir -p $(LISTPATH)
	@echo ""

include $(TOP)/E/makefiles/Rules.gmk

