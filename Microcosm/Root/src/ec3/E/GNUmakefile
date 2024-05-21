include $(TOP)/E/makefiles/Defs.gmk

all: ecomp-prep dobuild

dobuild:
	cd $(TOP)/E/runtime/vcache; $(MAKE)
	cd $(TOP)/E/runtime; $(MAKE) -f GNUmakefile.first # <- myE.java
	cd $(TOP)/E/runtime/e/run; $(MAKE)
	cd $(TOP)/E/runtime/e/lang; $(MAKE)
	# do not add anything above this line without consulting with
	# either Danfuzz or Gordie
	cd $(TOP)/E/runtime/util/native; $(MAKE)
	cd $(TOP)/E/runtime/util/native; $(MAKE) libs
	cd $(TOP)/E/runtime/tables; $(MAKE)
	cd $(TOP)/E/runtime/util; $(MAKE)
	cd $(TOP)/E/runtime/state; $(MAKE)
	cd $(TOP)/E/runtime/e/trace; $(MAKE)
	cd $(TOP)/E/runtime/e/util; $(MAKE)
	cd $(TOP)/E/runtime/e/util/crew; $(MAKE)
	cd $(TOP)/E/runtime/e/inspect; $(MAKE) dummies
	cd $(TOP)/E/runtime/e/timer; $(MAKE) dummies
	cd $(TOP)/E/runtime/security/crew; $(MAKE) dummies
	cd $(TOP)/E/runtime/e/quake; $(MAKE) dummies
	cd $(TOP)/E/runtime/e/start; $(MAKE)	# needs timer.dummies, quake.dummies, inspect.dummies securty/crew.dummies
	cd $(TOP)/E/runtime/e/timer; $(MAKE)	# needs start
	cd $(TOP)/E/runtime/security; $(MAKE)	# needs start, timer
	cd $(TOP)/E/runtime/e/file; $(MAKE)
	cd $(TOP)/E/runtime/e/openers; $(MAKE)
	cd $(TOP)/E/runtime/e/stream; $(MAKE)	# needs openers
	cd $(TOP)/E/runtime/e/serial; $(MAKE)	# needs openers
	cd $(TOP)/E/runtime/e/serialstate; $(MAKE)   # needs start
	cd $(TOP)/E/runtime/e/file/crew; $(MAKE)
	cd $(TOP)/E/runtime/e/quake; $(MAKE)	# needs openers,serial,start,file
	cd $(TOP)/E/runtime/e/quake/crew; $(MAKE)
	cd $(TOP)/E/runtime/e/upgrade; $(MAKE)
	cd $(TOP)/E/runtime/e/db; $(MAKE)	# needs stream,upgrade,myE.java
	cd $(TOP)/E/runtime/e/io; $(MAKE)	# needs run, start
	cd $(TOP)/E/runtime/e/inspect; $(MAKE)	# needs openers
	cd $(TOP)/E/runtime/e/cert; $(MAKE) early 
	cd $(TOP)/E/runtime/e/net; $(MAKE)	# needs stream,run,netbugs,db,io,file
	cd $(TOP)/E/runtime/e/cert; $(MAKE) 	# needs net
	cd $(TOP)/E/runtime/e/rep/steward; $(MAKE) pass1
	cd $(TOP)/E/runtime/e/hold; $(MAKE) pass1	# needs rep/steward
	cd $(TOP)/E/runtime/e/rep; $(MAKE) pass1 # needs hold
	cd $(TOP)/E/runtime/e/hold; $(MAKE)
	cd $(TOP)/E/runtime/e/rep; $(MAKE)	# needs openers,file,util,cert, hold
	cd $(TOP)/E/runtime/e/plsping; $(MAKE)	# needs net
	cd $(TOP)/E/runtime/e/hab; $(MAKE)	# needs rep
	cd $(TOP)/E/runtime/e/boot; $(MAKE)
	cd $(TOP)/E/runtime/e/file; $(MAKE) testers # needs quake
	cd $(TOP)/E/examples; $(MAKE)
	cd $(TOP)/E/tools/jwhich; $(MAKE)

clean:
	cd $(TOP)/E/runtime; $(MAKE) -f GNUmakefile.first clean
	cd $(TOP)/E/runtime/e/trace; $(MAKE) clean
	cd $(TOP)/E/runtime/e/run; $(MAKE) clean
	cd $(TOP)/E/runtime/e/lang; $(MAKE) clean
	cd $(TOP)/E/runtime/tables; $(MAKE) clean
	cd $(TOP)/E/runtime/state; $(MAKE) clean
	cd $(TOP)/E/runtime/util; $(MAKE) clean
	cd $(TOP)/E/runtime/e/util; $(MAKE) clean
	cd $(TOP)/E/runtime/e/util/crew; $(MAKE) clean
	cd $(TOP)/E/runtime/security; $(MAKE) clean
	cd $(TOP)/E/runtime/e/start; $(MAKE) clean
	cd $(TOP)/E/runtime/e/boot; $(MAKE) clean
	cd $(TOP)/E/runtime/e/openers; $(MAKE) clean
	cd $(TOP)/E/runtime/e/stream; $(MAKE) clean
	cd $(TOP)/E/runtime/e/serial; $(MAKE) clean
	cd $(TOP)/E/runtime/e/quake; $(MAKE) clean	
	cd $(TOP)/E/runtime/e/quake/crew; $(MAKE) clean
	cd $(TOP)/E/runtime/e/timer; $(MAKE) clean
	cd $(TOP)/E/runtime/e/file; $(MAKE) clean
	cd $(TOP)/E/runtime/e/file/crew; $(MAKE) clean
	cd $(TOP)/E/examples; $(MAKE) clean
	cd $(TOP)/E/runtime/e/db; $(MAKE) clean	
	cd $(TOP)/E/runtime/e/net; $(MAKE) clean
	cd $(TOP)/E/runtime/e/rep; $(MAKE) clean
	cd $(TOP)/E/runtime/e/plsping; $(MAKE) clean
	cd $(TOP)/E/runtime/e/hab; $(MAKE) clean
	cd $(TOP)/E/runtime/e/io; $(MAKE) clean	

ecomp-prep:
	cd tools/ecomp; $(MAKE) copy-from-dist
	cd $(TOP)/E/runtime; $(MAKE) -f GNUmakefile.first bootstrap
