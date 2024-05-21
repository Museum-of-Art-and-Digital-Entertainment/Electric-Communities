MODNAME		= ecerep
PACKAGE		= ec.e.rep
BUILDTYPE	= lib

SAFETY_MODE = GUEST

FILES.pass1.java = \
	Dummies.java \

FILES.java = \
	ParimeterizedRepository.java \
	PublishRepository.java \
	RepositoryPublisher.java \
	StandardRepository.java \


FILES.javadoc = \
	ParimeterizedRepository.java \
	StandardRepository.java \
	steward/CacheRepository.java \
	steward/CertifiedCryptoHashBundle.java \
	steward/ParimeterizedRepositoryMaker.java \
	steward/ReadOnlyRepository.java \
	steward/Repository.java \
	steward/RepositoryAccessException.java \
	steward/RepositoryFile.java \
	steward/RepositoryKeyNotFoundException.java \
	steward/RepositoryNeedParameterException.java \
	steward/RtCodingParameters.java \
	steward/SimpleRepository.java \
	steward/StandardRepositoryMaker.java \
	steward/SuperRepository.java \
	steward/SuperRepositoryMaker.java \

include $(TOP)/E/makefiles/Defs.gmk

DOCDIR       = $(TOP)/../../www-int/engineering/API_doc/javadoc 

.PHONY: all classes 

all: subdirs basics 

subdirs:
	(cd steward; $(MAKE) pass1)
	$(MAKE) pass1
	(cd steward; $(MAKE))

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)


clean: basic_clean
	(cd steward; $(MAKE) clean)

include $(TOP)/E/makefiles/Rules.gmk

