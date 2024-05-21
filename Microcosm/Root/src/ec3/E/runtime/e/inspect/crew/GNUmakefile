MODNAME		= inspect
PACKAGE		= ec.e.inspect.crew

DOCDIR = $(TOP)/../../www-int/engineering/API_doc/javadoc 

SAFETY_MODE = CREW

FILES.java = \
	Inspector.java \


FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes

all: mk_dirs mk_java

dummies:
	$(MAKE) basics FILES.java="$(FILES.dummies)" FILES.e=""

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

