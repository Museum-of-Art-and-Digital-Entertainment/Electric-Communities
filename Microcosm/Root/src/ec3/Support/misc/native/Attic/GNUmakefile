PACKAGE = ec.misc
SAFETY_MODE = CREW
MODNAME = ecutils
BUILDTYPE = lib

FILES.java = Native.java

FILES.cpp  = registry.cpp 

FILES.c = timer.c


CLASSES.export =  $(PACKAGE).Native 

include $(TOP)/E/makefiles/Defs.gmk

INCLDIRS += -I$(JAVAINC) -I$(JAVAOSINC) -I$(CHEADERDIR)

CFLAGS += $(INCLDIRS)

# XXX: For some reason CLFLAGS isn't being propogated properly through
#      Win32Rules.mak - so, as a workaround, add it here.
#
CLFLAGS += $(INCLDIRS) -I$(BUILD_DRIVE)$(CHEADERDIR) -DLITTLE_ENDIAN -DWIN32 -Zp8 -Zi

.PHONY: all classes exports

nominal: basics steward_basics

ifneq ($(JAVAVERSION), java-1.0.2)
USEJNI = yes
endif

libs: native

native: exports $(GENLIBS)

all: basics steward_basics exports $(GENLIBS)

exports: $(FULLSTUBNAME) $(FULLSTUBONAME) $(CHEADERSTATE)

clean: basic_clean lib_clean steward_clean

lib_clean: 
	-$(RM) -rf $(GENLIBS)

steward_basics:
	(cd steward; $(MAKE) basics)

steward_clean:
	(cd steward; $(MAKE) clean)



include $(TOP)/E/makefiles/Rules.gmk

