include $(TOP)/E/makefiles/Defs.gmk

all:
	cd ./compute;   $(MAKE)
	cd ./steward;   $(MAKE) interfaces1
	cd ./crew;      $(MAKE) 
	cd ./steward;   $(MAKE) interfaces2
	cd ./steward;   $(MAKE) otherfiles
	cd ./crew;      $(MAKE) crewlast
	cd ./testers;   $(MAKE)

clean:
	cd ./compute;      $(MAKE) clean
	cd ./steward;      $(MAKE) clean
	cd ./crew;         $(MAKE) clean
	cd ./testers;      $(MAKE) clean

test:
	cd ./testers; $(MAKE) Test
revive:
	cd ./testers; $(MAKE) Revive
