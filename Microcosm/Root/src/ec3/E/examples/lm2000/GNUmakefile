
MODNAME	= examples
PACKAGE	= ec.examples.lm2000

include $(TOP)/E/makefiles/Defs.gmk

FILES.e = \
	EClient.e \
	EServer.e \
	ServerFinder.e

FILES.java = \
	Common.java \
	Person.java \
	PersonConfigDialog.java \
	Rating.java \
	RatingSorter.java \
	Restaurant.java \
	RestaurantAddDialog.java \
	RestaurantDeleteDialog.java \
	RestaurantObserveDialog.java \
	ClientInfo.java \
    StartServer.java \
    StartClient.java \
	lm2000.java

all: basics

clean: basic_clean

lm2000:
	$(MAKE)
	$(JAVAEC) ec.examples.lm2000.lm2000

runit:
	-$(JAVAEC) ec.examples.lm2000.lm2000

include $(TOP)/E/makefiles/Rules.gmk

