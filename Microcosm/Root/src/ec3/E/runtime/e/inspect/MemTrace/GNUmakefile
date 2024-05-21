MODNAME		= inspect
PACKAGE		= ec.e.inspect

DOCDIR = $(TOP)/../../www-int/engineering/API_doc/javadoc 

SAFETY_MODE = STEWARD

FILES.javac = \
	Tracer.java \
	TraceInfo.java \
	RecursiveTraceInfo.java \
	TraceInfoUntraceable.java \
	TraceInfoBackref.java \


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


include $(TOP)/E/makefiles/Rules.gmk

