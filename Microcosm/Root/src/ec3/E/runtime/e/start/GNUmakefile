
MODNAME	= start
PACKAGE	= ec.e.start
DOCDIR  = $(TOP)/../../www-int/intdocs/prevat-javadoc
SAFETY_MODE = STEWARD

FILES.javac = \

FILES.java = \
	EBoot.java \
	ELogin.java \
	Tether.java \
	TimeQuake.java \
	Vat.java \
	EEnvironment.java \
	MagicPowerMaker.java \
	HabiLoader.java \
	Syncologist.java \
	InvokeMain.java \
	ZippySecurityManager.java \
	ZippyClassLoader.java 

FILES.e = \
	Seismologist.e \
	ELaunchable.e

FILES.javadoc = $(FILES.java)


include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all:	crewdummies basics
	(cd crew; $(MAKE))

crew:
	(cd crew; $(MAKE))

crewdummies:
	(cd crew; $(MAKE) dummies)

subdir_clean:
	(cd crew; $(MAKE) clean)

clean: basic_clean subdir_clean

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
