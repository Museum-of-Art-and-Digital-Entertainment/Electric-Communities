include $(TOP)/E/makefiles/Defs.gmk

all:
	cd $(TOP)/Pluribus/Compiler; $(MAKE)
	cd $(TOP)/Pluribus/Runtime; $(MAKE)
	cd $(TOP)/Pluribus/Examples; $(MAKE)

clean:
	cd $(TOP)/Pluribus/Compiler; $(MAKE) clean
#	cd $(TOP)/Pluribus/Runtime; $(MAKE) clean
	cd $(TOP)/Pluribus/Examples; $(MAKE) clean

include $(TOP)/E/makefiles/Rules.gmk
