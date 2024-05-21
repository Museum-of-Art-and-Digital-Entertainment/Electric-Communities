MODNAME		= sqldb
PACKAGE		= ec.sqldb.crew

SAFETY_MODE = CREW

DOCDIR          = $(TOP)/../../www-int/intdocs/javadoc

FILES.java =                           \
	SqlDB.java                     \
        SqlDBDate.java                 \
	SqlDBException.java	       \
	CrewThread.java      	       \
	RequestWorkersInt.java 	       \
	RequestWorkers.java            \
        AccountInfoTblMgr.java         \
        AddressInfoTblMgr.java         \
        BillingCreditCardTblMgr.java   \
	SubscriptionProgramTblMgr.java \
        IdentityInfoTblMgr.java

FILES.javadoc = $(FILES.java)

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

crewlast:
	$(MAKE) FILES.java=SqlDBMagicPowerMaker.java

clean: basic_clean

javadoc:
	javadoc -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk

