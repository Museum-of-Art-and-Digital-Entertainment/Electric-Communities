MODNAME		= trace
PACKAGE		= ec.e.run.test

DOCDIR = $(TOP)/../../www-int/engineering/API_doc/javadoc 

SAFETY_MODE = STEWARD

FILES.java = \
	Simple.java \
	Default.java \
	Fill.java \
	Many.java \
	MemLeak.java \
	MethodsCheck.java \
	Multilog1.java \
	Multilog2.java \
	Multilog3.java \
	Test.java \
	Other.java \
	Setup.java \
	Threads.java \
	Threads2.java \
	Throws.java \
	Changing.java \
	FieldChecker.java \
	UserError.java \
	BufferTests.java \
	LogTests.java \
	Dump.java \
	Reopen.java \
	Stdout.java \
	Tag.java \
	ErrorWatcher.java \
	TraceErrorWatcherImplementor.java \
	Timing.java \

FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes

all: mk_dirs mk_java

mk_java: classes

mk_dirs:
	@echo ""
	mkdir -p $(FULLPATH)
	mkdir -p $(LISTPATH)
	@echo ""

clean:
	rm -rf $(FULLPATH)
	rm -rf $(GENLIBS)
	rm -rf $(LISTPATH)
	rm -rf $(CLASSLIST) $(TOUCHLIST)
	rm -f $(FILES.javat) $(FILES.spjavat) $(FILES.et)

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

test:
	echo $(INCLUDE)

include $(TOP)/E/makefiles/Rules.gmk

