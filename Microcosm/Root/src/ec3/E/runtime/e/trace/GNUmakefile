MODNAME		= trace

# IMPORANT NOTE: Trace actually compiles into ec.e.run.  This is not a
# good time to make everyone change their imports, so I'm leaving the
# package alone for now.  At some point it would be nice to have trace
# actually live somewhere like ec.util.trace.

# I misleadingly use ec.e.trace here because it causes a goodly number
# of the compilation artifacts to land in a directory separate from
# ec.e.run, so that it's easier to clean up trace without messing with
# other ec.e.run classes.

PACKAGE		= ec.e.trace

DOCDIR = $(TOP)/../../www-int/engineering/API_doc/javadoc 

SAFETY_MODE = STEWARD

# NOTE FILES.class below.
FILES.java = \
	StackFrameData.java \
	Trace.java \
	TraceBuffer.java \
	TraceConstants.java \
	TraceController.java \
	TraceDate.java \
	TraceErrorWatcher.java \
	TraceLevelTranslator.java \
	TraceLog.java \
	TraceLogDescriptor.java \
	TraceMessage.java \
	TraceMessageAcceptor.java \
	TraceMessageStringifier.java \
	TraceSubsystemMediator.java \
	TraceTxtVersionNamer.java \
	TraceVersionNamer.java \

# These are commented out because IFC is compiled AFTER
# this directory.
#	TraceDisplay.java \
#	TraceEvent.java \

# The list of classes to delete.  This list could be generated via sed from 
# the contents of CLASSLIST (which contains FILES.java), but it doesn't 
# seem consistency of that directory is guaranteed.
FILES.class = \
	$(CLASSDIR)/ec/e/run/StackFrameData.class \
	$(CLASSDIR)/ec/e/run/Trace.class \
	$(CLASSDIR)/ec/e/run/TraceBuffer.class \
	$(CLASSDIR)/ec/e/run/TraceConstants.class \
	$(CLASSDIR)/ec/e/run/TraceController.class \
	$(CLASSDIR)/ec/e/run/TraceDate.class \
	$(CLASSDIR)/ec/e/run/TraceDisplay.class \
	$(CLASSDIR)/ec/e/run/TraceErrorWatcher.class \
	$(CLASSDIR)/ec/e/run/TraceEvent.class \
	$(CLASSDIR)/ec/e/run/TraceLevelTranslator.class \
	$(CLASSDIR)/ec/e/run/TraceLog.class \
	$(CLASSDIR)/ec/e/run/TraceLogDescriptor.class \
	$(CLASSDIR)/ec/e/run/TraceMessage.class \
	$(CLASSDIR)/ec/e/run/TraceMessageAcceptor.class \
	$(CLASSDIR)/ec/e/run/TraceMessageStringifier.class \
	$(CLASSDIR)/ec/e/run/TraceSubsystemMediator.class \
	$(CLASSDIR)/ec/e/run/TraceTxtVersionNamer.class \
	$(CLASSDIR)/ec/e/run/TraceVersionNamer.class \

FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes

all: mk_dirs mk_java

mk_java: classes

mk_dirs:
	@echo ""
	mkdir -p $(FULLPATH)
	mkdir -p $(LISTPATH)
	@echo ""

clean:
	rm -rf $(FILES.class)
	rm -rf $(LISTPATH)
	rm -rf $(CLASSLIST) $(TOUCHLIST)
	rm -f $(FILES.javat) $(FILES.spjavat) $(FILES.et)

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

test:
	echo $(INCLUDE)

include $(TOP)/E/makefiles/Rules.gmk

