
MODNAME	= net
PACKAGE	= ec.e.net.steward

SAFETY_MODE = STEWARD

FILES.java = \
	ByteListener.java \
	ByteConnection.java \
	ByteReceiver.java \
	ByteSender.java \
	ConnectionStatistics.java \
	ConnectionStatisticsSteward.java \
	EObjectExport.java \
	MakeDSAPublicKey.java \
	NetworkConnection.java \
	NetworkListener.java \
	NetworkSender.java \
	Proxy.java \
	RegistrarIDGenerator.java

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

dummies:
	$(MAKE) FILES.java=Dummies.java

include $(TOP)/E/makefiles/Rules.gmk
