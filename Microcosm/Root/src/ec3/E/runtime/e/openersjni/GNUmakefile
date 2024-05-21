
MODNAME		= openers
PACKAGE		= ec.e.openers
BUILDTYPE	= lib
DOCDIR          = $(TOP)/../../www-int/intdocs/javadoc
SAFETY_MODE	= STEWARD

FILES.java = \
	AwakeAfterRevival.java \
	ClassRecipe.java \
	HashtableRecipe.java \
	JavaUtil.java \
	ObjOpener.java \
	OpenerID.java \
	OpenerRecipe.java \
	Recipe.java \
	RefOpener.java \
	RtSealerRecipe.java \
	RtWeakCellRecipe.java \
	Surgeon.java \
	TableRecipe.java \
	VarOpener.java \

FILES.javadoc = $(FILES.java)

CLASSES.export = \
	$(PACKAGE).BooleanOpener \
	$(PACKAGE).ByteOpener \
	$(PACKAGE).CharOpener \
	$(PACKAGE).ShortOpener \
	$(PACKAGE).IntOpener \
	$(PACKAGE).LongOpener \
	$(PACKAGE).FloatOpener \
	$(PACKAGE).DoubleOpener \
	$(PACKAGE).VarOpener \
	$(PACKAGE).RefOpener \
	$(PACKAGE).Surgeon \

FILES.c = openers.c

include $(TOP)/E/makefiles/Defs.gmk

INCLDIRS = -I$(JAVAINC) -I$(JAVAOSINC) -I$(CHEADERDIR)

CFLAGS += $(INCLDIRS)

.PHONY: all classes exports

ifneq ($(JAVAVERSION), java-1.0.2)
USEJNI = yes
endif

all: basics exports $(GENLIBS)
	cd guest; $(MAKE)

exports: $(FULLSTUBNAME) $(FULLSTUBONAME) $(CHEADERSTATE)

clean:
	rm -rf $(FULLPATH)
	rm -rf $(GENLIBS)
	rm -rf $(LISTPATH)
	rm -rf $(CLASSLIST) $(TOUCHLIST)
	rm -f $(FILES.javat) $(FILES.spjavat) $(FILES.et)
	rm -f $(FULLSTUBNAME)
	rm -f $(CHEADERSTATE)
	cd guest; $(MAKE) clean

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
