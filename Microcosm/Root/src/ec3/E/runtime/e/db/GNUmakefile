MODNAME		= ecedb
PACKAGE		= ec.e.db

DOCDIR       = $(TOP)/../../www-int/intdocs/javadoc

SAFETY_MODE = STEWARD

FILES.java = \
	RtCodingSystem.java \
	RtCodingParameters.java \
	RtSpecialObjectCoder.java \
	RtAwakeAfterDecoding.java \
	TypeTable.java \
	RtEncoder.java \
	RtEncoderDataOutputStream.java \
	RtEncodingManager.java \
	RtDecoder.java \
	RtDecoderDataInputStream.java \
	RtDecodingManager.java \
	RtDecodingException.java \
	RtByteArrayInputStream.java \
	RtByteArrayOutputStream.java \
	RtSeekException.java  \

FILES.javadoc = $(FILES.java)


include $(TOP)/E/makefiles/Defs.gmk


.PHONY: all classes exports

all: basics


clean: basic_clean

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk

