MODNAME	= ifcapp
PACKAGE	= netscape.application

SAFETY_MODE = CREW

FILES.javac = \
		ASCIIArchiveLoader.java \
		Archive.java \
		Archiver.java \
		ArchivingStack.java \
		BariumArchiveLoader.java \
		ClassInfo.java \
		ClassTable.java \
		Codable.java \
		CodingException.java \
		CompactInputStream.java \
		CompactOutputStream.java \
		Comparable.java \
		Decoder.java \
		DeserializationException.java \
		Deserializer.java \
		Encoder.java \
		Enumeration.java \
		ExternalCoder.java \
		FormattingSerializer.java \
		Hashtable.java \
		HashtableEnumerator.java \
		IdHashtable.java \
		InconsistencyException.java \
		NoSuchElementException.java \
		PrimitiveCoder.java \
		Serializer.java \
		Sort.java \
		TokenGenerator.java \
		Unarchiver.java \
		Vector.java \
		VectorEnumerator.java \

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

DOCDIR       = $(TOP)/../../../www-int/engineering/API_doc/javadoc
FILES.javadoc = $(FILES.javac)

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
