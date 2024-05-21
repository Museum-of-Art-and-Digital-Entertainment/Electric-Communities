
MODNAME	= net
PACKAGE	= ec.e.net.crew

SAFETY_MODE = CREW

FILES.java = \
	ConnectionStatisticsCrew.java \
	Decrypt3DES.java \
	Encrypt3DES.java \
	NetAddr.java \
	ListenThread.java \
	RawConnection.java \
	RecvThread.java \
	SendThread.java


include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

dummies:
	$(MAKE) FILES.java=Dummies.java

clean: basic_clean

include $(TOP)/E/makefiles/Rules.gmk
