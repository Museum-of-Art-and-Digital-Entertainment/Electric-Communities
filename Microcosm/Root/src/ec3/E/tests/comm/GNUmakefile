SUBNAME = comm
MODNAME =  tests/$(SUBNAME)
PACKAGE =  ec.tests.$(SUBNAME)

SAFETY_MODE = GUEST

include $(TOP)/E/makefiles/Defs.gmk

FILES.e = \
	AlohaException.e \
	ByteTest.e \
	PktTest.e \
	EncryptedCheckpoint.e \
	NetCapability.e \
	NameSpaceTest.e \
	HelloComm.e \
	MultiComm.e \
	HelloDisc.e \
	HelloGordie.e \
	HelloHandoff.e \
	HelloUnique.e \
	RemoteLoad.e \

FILES.java = \
	 RemotePerson.java \

all: mk_dirs mk_java

mk_java: classes

mk_dirs:
	@echo ""
	mkdir -p $(LISTPATH)
	@echo ""

clean:
	rm -rf $(CLASSDIR)/$(PKGDIR)
	rm -rf $(LISTPATH)
	rm -rf $(CLASSLIST) $(TOUCHLIST)
	rm -f $(FILES.javat) $(FILES.et)

AlohaException:
	$(MAKE) FILES.e=AlohaException.e FILES.java= 

ByteTest:
	$(MAKE) FILES.e=ByteTest.e FILES.java= 

PktTest:
	$(MAKE) FILES.e=PktTest.e FILES.java= 

EncryptedCheckpoint:
	$(MAKE) FILES.e=EncryptedCheckpoint.e FILES.java= 

NetCapability:
	$(MAKE) FILES.e=NetCapability.e FILES.java= 

NameSpaceTest:
	$(MAKE) FILES.e=NameSpaceTest.e FILES.java= 

HelloComm:
	$(MAKE) FILES.e=HelloComm.e FILES.java= 

MultiComm:
	$(MAKE) FILES.e=MultiComm.e FILES.java= 

HelloDisc:
	$(MAKE) FILES.e=HelloDisc.e FILES.java= 

HelloGordie:
	$(MAKE) FILES.e=HelloGordie.e FILES.java=

HelloExample:
	$(MAKE) FILES.e=HelloExample.e FILES.java= 

HelloHandoff:
	$(MAKE) FILES.e=HelloHandoff.e FILES.java= 

HelloCoding:
	$(MAKE) FILES.e=HelloCoding.e FILES.java= 

HelloUnique:
	$(MAKE) FILES.e=HelloUnique.e FILES.java= 

RemoteLoad: RemotePerson
	$(MAKE) FILES.e=RemoteLoad.e FILES.java=RemotePerson.java

RemotePerson:
	$(MAKE) FILES.java=RemotePerson.java FILES.e= 

run:
	-$(JAVAEC) ec.tests.comm.HelloComm
	-$(JAVAEC) ec.tests.comm.HelloGordie
	-$(JAVAEC) ec.tests.comm.HelloHandoff
	-$(JAVAEC) ec.tests.comm.HelloUnique
	-$(JAVAEC) ec.tests.comm.RemoteLoad

include $(TOP)/E/makefiles/Rules.gmk

