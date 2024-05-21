MODNAME		= cert
PACKAGE		= ec.cert

DOCDIR       = $(TOP)/../../www-int/engineering/API_doc/javadoc 

SAFETY_MODE = STEWARD

FILES.e = \
	EVerifierServer.e \
    ECertAgencyServer.e \

FILES.java = \
	VerifierDescription.java \
	Certifier.java \
	Verifier.java \
	Certificate.java \
	CertRequesterData.java \
	CertSubmissionBundle.java \

FILES.early = \
	CryptoHash.java \

FILES.javadoc = \
	Certificate.java \
	Verifier.java \
	Certifier.java \
	CryptoHash.java \

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes 

all: basics 

early:
	$(MAKE) basics FILES.java="$(FILES.early)" FILES.e=""

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

clean: basic_clean

include $(TOP)/E/makefiles/Rules.gmk

