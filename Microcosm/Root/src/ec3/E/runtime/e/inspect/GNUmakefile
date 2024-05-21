MODNAME		= inspect
PACKAGE		= ec.e.inspect

DOCDIR = $(TOP)/../../www-int/engineering/API_doc/javadoc 

SAFETY_MODE = STEWARD

FILES.java = \
	InspectorInterfaces.java \
	CheckpointDump.java \
	Inspector.java \
	ObjectInspector.java \
	ArrayInspector.java \
	ReferencersInspector.java \
	HashtableInspector.java \
	VectorInspector.java \
	Runlet.java \
	RunQueueInspector.java \
	ReviveTest.java \
	CheckpointReviveTester.java \
	Gatherer.java \
	GathererMaker.java \


FILES.dummies = \
	InspectorInterfaces.java \
	InspectorDummies.java \


FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes guest crew

all: mk_dirs mk_java

dummies:
	$(MAKE) basics FILES.java="$(FILES.dummies)" FILES.e=""

mk_java: subdirs classes guest crew

subdirs:
	(cd MemTrace ; $(MAKE))

crew:
	(cd crew ; $(MAKE))

guest:
	(cd guest ; $(MAKE))


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
	(cd MemTrace ; $(MAKE) clean)
	(cd guest ; $(MAKE) clean)
	(cd crew ; $(MAKE) clean)

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

test:
	echo $(INCLUDE)

include $(TOP)/E/makefiles/Rules.gmk

