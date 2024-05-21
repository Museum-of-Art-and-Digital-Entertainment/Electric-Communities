
PACKAGE		= ec.security
MODNAME		= security

# needs to see jce crypto stuff
SAFETY_MODE	= STEWARD

FILES.java = \
	Cipher.java \
	DESKey.java \
	PassphraseKeyGenerator.java \
	CipherInputStream.java \
	CipherOutputStream.java \
	ECSecureRandom.java \
	ECSecureRandomMaker.java

FILES.interfaces = \
	EntropyHolder.java

FILES.javadoc=$(FILES.javac)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes

all: interfaces subdirs basics

interfaces:
	$(MAKE) "FILES.java=$(FILES.interfaces)" basics

subdirs:
	(cd crew; $(MAKE))

clean: basic_clean
	(cd crew; $(MAKE) clean)

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
