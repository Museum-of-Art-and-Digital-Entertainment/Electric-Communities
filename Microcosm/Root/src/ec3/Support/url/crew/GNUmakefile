# MODNAME		= URLLauncher
PACKAGE		= ec.url.crew
BUILDTYPE	= lib
DOCDIR          = $(TOP)/../../www-int/intdocs/javadoc

SAFETY_MODE = CREW

FILES.java = \
	URLLauncher.java \
	URLLauncherDemo.java \
	URLLauncherMagicPowerMaker.java

FILES.javadoc = $(FILES.java)

# CLASSES.export = $(PACKAGE).URLLauncher
CLASSES.export = 

# FILES.c = URLLauncher.c
FILES.c = 

include $(TOP)/E/makefiles/Defs.gmk

INCLDIRS = -I$(JAVAINC) -I$(JAVAOSINC) -I$(CHEADERDIR) 

CFLAGS += $(INCLDIRS)

.PHONY: all classes exports

ifeq ($(PLATFORM_OS), solaris)
# nominal: basics native
nominal: basics
else
nominal: basics
endif

libs: native

native: exports $(GENLIBS)

all: basics exports $(GENLIBS)

all: basics 

exports: $(FULLSTUBNAME) $(FULLSTUBONAME) $(CHEADERSTATE)

clean:
	rm -rf $(FULLPATH)
	rm -rf $(GENLIBS)
	rm -rf $(LISTPATH)
	rm -rf $(CLASSLIST) $(TOUCHLIST)
	rm -rf $(FILES.javat) $(FILES.spjavat) $(FILES.et) 

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
