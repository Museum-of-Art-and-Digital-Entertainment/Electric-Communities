
MODNAME	= net
PACKAGE	= ec.e.net

SAFETY_MODE = STEWARD

FILES.java = \
	ConnectionsManager.java \
	DynamicCollection.java \
	EARL.java \
	EConnection.java \
	EListener.java \
	EReceiver.java \
	ESender.java \
	ExportTable.java \
	ImportExportTables.java \
	ImportTable.java \
	PLSConnection.java \
	Msg.java \
	MsgConnection.java \
	MsgListener.java \
	MsgReceiver.java \
	MsgSender.java \
	NetIdentityMaker.java \
	ProxyInterest.java \
	Registrar.java \
	SturdyRefURLImporter.java \
	RtForwardingSturdyRef.java

FILES.e = \
	ProcessLocationServer.e \
	Intercepter.e \
	RegistrarHelper.e \
	SturdyRef.e \
	SturdyRefMaker.e \
	SturdyRefImporter.e \
	SturdyRefExporter.e \
	SturdyRefFileImporter.e \
	SturdyRefFileExporter.e \
	SturdyRefFollower.e

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: stewardDummies dummies subdirs basics

subdirs:
	(cd crew; $(MAKE) dummies)
	(cd steward; $(MAKE))
	(cd crew; $(MAKE))
	(cd pls; $(MAKE))

stewardDummies:
	(cd steward; $(MAKE) dummies)

dummies:
	$(MAKE) basics FILES.e="" FILES.java=Dummies.java

clean: basic_clean
	(cd steward; $(MAKE) clean)
	(cd crew; $(MAKE) clean)

include $(TOP)/E/makefiles/Rules.gmk
