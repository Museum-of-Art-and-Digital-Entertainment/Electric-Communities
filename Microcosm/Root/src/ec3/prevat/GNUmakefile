
SAFETY_MODE	= SAFE_JAVA
DOCDIR          = $(TOP)/../../www-int/intdocs/prevat-javadoc
SOURCEPATH      = .


PACKAGES.javadoc = \
	java.lang \
	java.util \
	java.io \
	ec.e.cap \
	ec.e.db \
	ec.e.file \
	ec.e.hab \
	ec.e.inspect \
	ec.e.io \
	ec.e.lang \
	ec.e.net \
	ec.e.net.crew \
	ec.e.net.steward \
	ec.e.openers \
	ec.e.quake \
	ec.e.rep \
	ec.e.run \
	ec.e.serial \
	ec.e.start \
	ec.e.start.crew \
	ec.e.stream \
	ec.e.util \
	ec.e.util.crew \
	ec.util \

#		ec.e.cap ec.e.db ec.e.io ec.e.lang ec.e.net \
#		ec.e.netbugs ec.e.run ec.e.stream

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

javadoc:
	javadoc -sourcepath $(SOURCEPATH) -d $(DOCDIR) $(PACKAGES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
