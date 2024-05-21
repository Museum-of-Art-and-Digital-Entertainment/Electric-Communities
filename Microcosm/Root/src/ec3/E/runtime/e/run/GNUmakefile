MODNAME =  run
PACKAGE   = ec.e.run
BUILDTYPE = lib

SAFETY_MODE = STEWARD

# IMPORTANT! READ THIS COMMENT BEFORE MODIFYING THIS FILE!
#
# If you are changing the set of files made by this makefile, then you
# need to make sure you haven't broken the "minimal make" located in
# E/runtime. Please do a gnumake clean followed by "minimal-make" and
# then "minimal-test". Do not check in unless those work. If they do
# not work, then you need to fix them. Also be aware of the minimal-dist
# which may need to be tweaked if the set of files compiled here
# changes.
#
# IMPORTANT! READ THIS COMMENT BEFORE MODIFYING THIS FILE!

FILES.generated = \
	$(BUILD_DRIVE)$(BUILD)/gencode/erun/RtEnqueue.java \
	$(BUILD_DRIVE)$(BUILD)/gencode/erun/RtQObj.java

# The bootstrap1 files are compiled with standard ecomp, but the
# bootstrap2 files are compiled with e2jdone. (See the "bootstrap"
# rules, below.)
FILES.bootstrapJava = \
	../../util/EMainThread.java \
	../../util/EThreadGroup.java \
	../../util/Humanity.java \
	../../util/NestedError.java \
	../../util/NestedThrowable.java \
	../../util/NestedThrowableVector.java \
	../../util/ExceptionNoticer.java \
	../../util/ReadOnlyHashtable.java \
	ClassDemangler.java \
	EChannel.java \
	EDelegator.java \
	EInterface.java \
	ENullTether.java \
	eParty.java \
	EStone.java \
	EUniChannel.java \
	Exportable.java \
	InternalClosures.java \
	NotificationManager.java \
	OnceOnlyException.java \
	RtAssignedTether.java \
	RtCausality.java \
	RtDeflector.java \
	RtEnvelope.java \
	RtErrorException.java \
	RtExceptionEnv.java \
	RtFinalizer.java \
	RtInterfaces.java \
	RtInvocation.java \
	RtInvocationException.java \
	RtQ.java \
	RtRun.java \
	RtRuntimeException.java \
	RtSealer.java \
	RtTether.java \
	RtWeakling.java \
	RtWeakCell.java \
	UnknownSealer.java

FILES.bootstrapE = \
	EBoolean.e \
	ECatchClosure.e \
	EDistributor.e \
	EProxy.e \
	EResult.e \
	EUniDistributor.e \
	EWhenClosure.e

FILES.bootstrap2 = \
	EObject.e

FILES.e = \
	../lang/EInteger.e \
	../lang/EDouble.e \
	ENull.e \
	ETrace.e \

FILES.javadoc = $(FILES.java)

CLASSES.export =  $(PACKAGE).RtWeakCell

FILES.c =     RtWeakCell.c

DOCDIR       = $(TOP)/../../www-int/intdocs/javadoc

include $(TOP)/E/makefiles/Defs.gmk

INCLDIRS = -I$(JAVAINC) -I$(JAVAOSINC) -I$(CHEADERDIR) 

CFLAGS += $(INCLDIRS)
OTHER_LIBS = $(RUNLIB)

.PHONY: all classes exports

#all: basics exports $(GENLIBS)
all: bootstrap1 bootstrap2 basics exports $(GENLIBS)

bootstrap1: generateFiles
	$(MAKE) basics FILES.java="$(FILES.bootstrapJava)" \
	    FILES.genjava="$(FILES.generated)" \
	    FILES.e="$(FILES.bootstrapE)"

bootstrap2:
	$(MAKE) basics FILES.java="" FILES.e="$(FILES.bootstrap2)"

generateFiles:
	bash make-sources.sh

exports: $(FULLSTUBNAME) $(FULLSTUBONAME) $(CHEADERSTATE)

clean: basic_clean
	rm -rf $(GENLIBS)
	rm -rf $(CLASSLIST) $(TOUCHLIST)
	echo "Note: because Trace is in ec.e.run, you have also cleaned up Trace classes."
	echo "The trace source is in ../trace, so it will not automatically be remade."
	echo "To add to the fun, you have to do a make clean before doing a make"
	echo "in the trace directory, else it won't get made"

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
