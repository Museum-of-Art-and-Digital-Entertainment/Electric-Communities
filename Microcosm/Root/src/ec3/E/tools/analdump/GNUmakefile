#
# GNUmakefile for the Analyze Dump program, 'analdump'
#
# Chip Morningstar
# Electric Communities
# 17-December-1997
#

INSTALL_DIR = $(BUILD)/bin
MODNAME = analdump
APP_PATH = $(INSTALL_DIR)/$(MODNAME)

FILES.c = 	\
 adcommand.c	\
 adjoutput.c	\
 adlineio.c	\
 adlinkmap.c	\
 admoutput.c	\
 adoutput.c	\
 adparse.c	\
 adtables.c	\
 adwoutput.c	\
 adversion.c	\
 admain.c

FILES.h =	\
 analdump.h	\
 generic.h

# Restoration TODO recode to remove warnings suppressed below:
CFLAGS += -Wreturn-type -Wno-implicit-function-declaration -Wno-format -Wno-builtin-declaration-mismatch -I.

vpath %.c .

.PHONY: all standalone mk_standalone clean

standalone:
	$(MAKE) mk_standalone

mk_standalone: $(APP_PATH)

include $(TOP)/E/makefiles/Defs.gmk

$(FILES.o): $(FILES.h)

$(INSTALL_DIR):
	mkdir -p $@

clean:
	$(RM) -rf $(BUILD)/objs/analdump
	$(RM) -rf $(BUILD)/bin/analdump
	$(RM) -f core
	$(RM) -rf $(LISTPATH)

love:
	@echo "Not war?"

include $(TOP)/E/makefiles/Rules.gmk
