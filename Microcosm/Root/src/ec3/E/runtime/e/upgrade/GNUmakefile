
MODNAME	    = upgrade
PACKAGE	    = ec.e.upgrade
DOCDIR      = $(TOP)/../../www-int/intdocs/javadoc
SAFETY_MODE = STEWARD

FILES.java = \
	InterfaceConversionException.java \
	InterfaceConverter.java \
	InterfaceWrapper.java \
	MessageConversionException.java \
	MessageWrapper.java \
	ObjectWrapper.java \
	ProxyWrapper.java \
	StateConversionException.java \
	StateConverter.java \
	UpgradeConverter.java \
	UpgradeTable.java \

FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
