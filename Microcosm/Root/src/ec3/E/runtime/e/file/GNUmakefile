MODNAME = file
PACKAGE = ec.e.file

DOCDIR       = $(TOP)/../../www-int/intdocs/javadoc

SAFETY_MODE = STEWARD

# ECOMPFLAGS = -printe2jtree -d $(TOPDIR)

FILES.e = 

FILES.java = \
	EAccessibleDirectory.java \
	EAppendableFile.java \
	EDirectoryBase.java \
	EDirectoryEntry.java \
	EDirectoryRootMaker.java \
	EEditableDirectory.java \
	EEditableFile.java \
	EFileEditor.java \
	EFileInfo.java \
	EFileReader.java \
	EInputStream.java \
	EOutputStream.java \
	EReadableDirectory.java \
	EReadableFile.java \
	EStdio.java \
	URLStreamMaker.java \
	QuakeProofAppendFile.java

FILES.javadoc = $(FILES.java)

FILES.testers = \
	FileTest.e \
	QuakeFileTest.e

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

testers:
	$(MAKE) basics FILES.e="$(FILES.testers)" FILES.java=""

clean: basic_clean

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

Test:
	$(JAVAEC) ec.e.start.EBoot ec.e.file.FileTest

QuakeTest:
	rm -rf quaketest.evat
	$(JAVAEC) ec.e.start.EBoot ec.e.file.QuakeFileTest checkpoint=quaketest \
	TraceLog_quakeproofappendfile=debug

QuakeRevive:
	$(JAVAEC) ec.e.quake.Revive quaketest \
	TraceLog_quakeproofappendfile=debug

include $(TOP)/E/makefiles/Rules.gmk
