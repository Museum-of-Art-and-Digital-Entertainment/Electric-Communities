MODNAME		= ecerepsteward
PACKAGE		= ec.e.rep.steward
BUILDTYPE	= lib

DOCDIR       = $(TOP)/../../www-int/engineering/API_doc/javadoc 

SAFETY_MODE = STEWARD

FILES.pass1.java = \
	Dummies.java \
	EByteArray.java \
	RepositoryAccessException.java \
	RepositoryKeyNotFoundException.java \
	RepositoryNeedParameterException.java \
	NotRepositoryFileException.java \
	SimpleRepository.java \

FILES.java = \
	CacheRepository.java \
	CertifiedCryptoHashBundle.java \
	EDataRequestor.java \
	ERepositoryPublisher.java \
	ParimeterizedRepositoryMaker.java \
	PublishRepositoryMaker.java \
	ReadOnlyRepository.java \
	Repository.java \
	RepositoryHandle.java \
	RepositoryHandleRecipe.java \
	RepositoryFile.java \
	RepositoryPublisherMaker.java \
	RepositoryTether.java \
	RtCodingParameters.java \
	RuntimeExtensibleRepository.java \
	StandardRepositoryMaker.java \
	RemoteDownloaderMaker.java \
	SuperRepository.java \
	SuperRepositoryMaker.java \
	RemoteDownloader.java \

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes 

all:	pass1basics basics 

clean: basic_clean

include $(TOP)/E/makefiles/Rules.gmk

