
PACKAGE		= ec.util
MODNAME		= util
#BUILDTYPE	= lib

# Note: if you are changing the set of files made by this makefile,
# then you need to make sure you haven't broken the "minimal make"
# located in E/runtime. Please do a gnumake clean followed by
# "minimal-make" and then "minimal-test". Do not check in unless
# those work.

FILES.java = \
	ByteArray.java \
	Cache.java \
	CompletionNoticer.java \
	Convert.java \
	EMainThread.java \
	EThreadGroup.java \
	Equals.java \
	ExceptionNoticer.java \
	GenericIntHashtable.java \
	HexStringUtils.java \
	Humanity.java \
	IdentityIntHashtable.java \
	IntKeyTable.java \
	List.java \
	ListNode.java \
	NestedError.java \
	NestedThrowable.java \
	NestedThrowableVector.java \
	ObjKeyTable.java \
	PEHashtable.java \
	ReadOnlyHashtable.java \
	SortUtilities.java \
	ThreadWatcher.java \
	Zip.java \
	ZipMerge.java

#	Sniffer.java

# CLASSES.export = $(PACKAGE).Sniffer

#FILES.c = 	\
#	Sniffer.c

include $(TOP)/E/makefiles/Defs.gmk

INCLDIRS = -I$(JAVAINC) -I$(JAVAOSINC) -I$(CHEADERDIR)

CFLAGS += $(INCLDIRS)

.PHONY: all classes 

all: mk_dirs mk_java 
	cd assertion; $(MAKE)

#clean: basic_clean

clean:
	rm -rf $(FULLPATH)
	rm -rf $(GENLIBS)
	rm -rf $(LISTPATH)
	rm -rf $(CLASSLIST) $(TOUCHLIST)
	rm -f $(FILES.javat) $(FILES.spjavat) $(FILES.et)
	cd assertion; $(MAKE) clean

# exports: $(FULLSTUBNAME) $(FULLSTUBONAME) $(CHEADERSTATE)

mk_java: classes

mk_dirs:
	@echo ""
	mkdir -p $(FULLPATH)
	mkdir -p $(LISTPATH)
	@echo ""

include $(TOP)/E/makefiles/Rules.gmk

