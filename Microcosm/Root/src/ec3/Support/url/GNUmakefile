include $(TOP)/E/makefiles/Defs.gmk

all:
	cd ./dummycrew; $(MAKE)
	cd ./steward; $(MAKE)
	cd ./crew; $(MAKE)
	cd ./testers; $(MAKE)

libs:
	cd ./crew; $(MAKE) libs

clean:
	cd ./dummycrew; $(MAKE) clean
	cd ./steward; $(MAKE) clean
	cd ./crew; $(MAKE) clean
	cd ./testers; $(MAKE) clean

test:
	cd ./testers; $(MAKE) Test
