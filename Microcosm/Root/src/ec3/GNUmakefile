include $(TOP)/E/makefiles/Defs.gmk

all: space_out jwhich analdump
	cd $(TOP)/E; $(MAKE)
	#cd $(TOP)/Support; $(MAKE)
	#cd $(TOP)/Pluribus/Runtime; $(MAKE)

init:
	cd $(TOP)/E/makefiles; $(MAKE)

total: init all

e-none:
	cd E/tools/ecomp; $(MAKE) e-none

ecomp:
	cd E/tools/ecomp; $(MAKE) from-scratch

jwhich:
	cd E/tools/jwhich; $(MAKE)

analdump:
	cd E/tools/analdump; $(MAKE)

trace-on:
	@echo "Setting Trace.ON = true in E/runtime/e/trace/Trace.java"
	@cat E/runtime/e/trace/Trace.java | sed 's/ON = false/ON = true/g' > .tempfile
	@mv .tempfile E/runtime/e/trace/Trace.java
	@echo "Setting Trace.ON = true in E/runtime/e/run/TraceDummies.java"
	@cat E/runtime/e/run/TraceDummies.java | sed 's/ON = false/ON = true/g' > .tempfile
	@mv .tempfile E/runtime/e/run/TraceDummies.java

trace-off:
	@echo "Setting Trace.ON = false in E/runtime/e/trace/Trace.java"
	@cat E/runtime/e/trace/Trace.java | sed 's/ON = true/ON = false/g' > .tempfile
	@mv .tempfile E/runtime/e/trace/Trace.java
	@echo "Setting Trace.ON = false in E/runtime/e/run/TraceDummies.java"
	@cat E/runtime/e/run/TraceDummies.java | sed 's/ON = true/ON = false/g' > .tempfile
	@mv .tempfile E/runtime/e/run/TraceDummies.java

clean:
	cd $(TOP)/E; $(MAKE) clean
	#cd $(TOP)/Support; $(MAKE) clean
	cd $(TOP); $(RM) -rf $(BUILD)/steward
	cd $(TOP); $(RM) -rf $(BUILD)/guest
	cd $(TOP); $(RM) -rf $(BUILD)/crew
	cd $(TOP); $(RM) -rf $(BUILD)/compute
	#cd $(TOP)/Pluribus/Runtime; $(MAKE) clean

space_out:
	@echo ""
	@echo ""
	@echo "              Total compile for $(PLATFORM_OS) platform."
	@echo ""

help:
	@echo ""
	@echo "  'all' or no args : Make the Tools, Libs, E, Java (virtual machine API)"
	@echo "  'init'           : Copy ecomp classes to the build tree"
	@echo "  'total           : Do an 'init' and an 'all'"
	@echo "  'clean           : Describe how to clean stuff up"
	@echo "  'cleanup'        : Clean up the Tools, E, Java"
	@echo "  'cleanall'       : Clean up the Libs, Tools, E, Java"
	@echo ""
	@echo "  You can make *just* the Tools, the Libs, E, Java"
	@echo "  by 'cd'ing to $$ TOP/Libs (or Tools, etc.) and doing '$(MAKE)'"
	@echo ""

include $(TOP)/E/makefiles/Rules.gmk
