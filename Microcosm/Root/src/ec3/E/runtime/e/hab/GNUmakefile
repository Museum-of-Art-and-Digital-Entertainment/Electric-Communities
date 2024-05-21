MODNAME = hab
PACKAGE = ec.e.hab

# ECOMPFLAGS = -printe2jtree -d $(TOPDIR)

SAFETY_MODE = STEWARD

DOCDIR = $(TOP)/../../www-int/intdocs/javadoc

FILES.e = \

FILES.java = \
	BasedDesignator.java \
	Designator.java \
	DirectDesignator.java \
	HaberdasherException.java \
	Haberdashery.java \
	HaberdasheryObject.java \
	HashkeyDesignator.java \
	IndirectDesignator.java \
	NonHaberdasheryObjectHolder.java \
	NullDesignator.java \
	PathDesignator.java \
	RootDesignator.java \
	Unit.java \
	UnitPath.java \
	UnknownDesignator.java

FILES.javadoc = $(FILES.java)

CLASSES.export = \
 $(PACKAGE).Designator \
 $(PACKAGE).DirectDesignator \
 $(PACKAGE).Haberdashery \
 $(PACKAGE).HaberdasheryObject \
 $(PACKAGE).HashkeyDesignator \
 $(PACKAGE).IndirectDesignator \
 $(PACKAGE).NonHaberdasheryObjectHolder \
 $(PACKAGE).NullDesignator \
 $(PACKAGE).PathDesignator \
 $(PACKAGE).RootDesignator \
 $(PACKAGE).Unit \
 $(PACKAGE).UnitPath \
 $(PACKAGE).UnknownDesignator

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics exports

clean: basic_clean

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

exports: $(CHEADERSTATE)

include $(TOP)/E/makefiles/Rules.gmk
