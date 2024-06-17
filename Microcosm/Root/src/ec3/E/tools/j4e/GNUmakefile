#
# GNUmakefile for the 'j4e' E-to-Java converter
#
# Chip Morningstar
# Electric Communities
# 9-January-1998
#

INSTALL_DIR = $(BUILD)/bin
MODNAME = j4e
APP_PATH = $(INSTALL_DIR)/$(MODNAME)
YACCHELPER_DIR = ../../../Tools/yacchelper

JFEFILESC.c = 	\
 jfefileio.c	\
 jfeparse.c	\
 jfeversion.c	\
 jfemain.c

JFEFILESY.c = 	\
 jfelex.c	\
 jfeout.c

JFEFILESC.h =	\
 jfe.h		\
 generic.h

JFEFILESY.h =	\
 y.tab.h

YHFILES.c =	\
 yh_build.c	\
 yh_dump.c	\
 yh_error.c	\
 yh_free.c	\
 yh_lex.c	\
 yh_symtab.c	\
 yh_walk.c

YHFILES.h =	\
 master_yh.h

FILES.c = $(JFEFILESC.c) $(JFEFILESY.c) $(YHFILES.c)

JFEFILESC.o = $(JFEFILESC.c:%.c=$(BUILD)/objs/$(MODNAME)/%.o)
JFEFILESY.o = $(JFEFILESY.c:%.c=$(BUILD)/objs/$(MODNAME)/%.o)
YHFILES.o = $(YHFILES.c:%.c=$(BUILD)/objs/$(MODNAME)/%.o)

INCLDIRS = -I$(YACCHELPER_DIR)

CFLAGS += -Wall -Wreturn-type -I. $(INCLDIRS)

vpath %.c .
vpath %.c $(YACCHELPER_DIR)

.PHONY: all standalone mk_standalone clean

standalone:
	$(MAKE) mk_standalone

mk_standalone: $(APP_PATH)

include $(TOP)/E/makefiles/Defs.gmk

$(JFEFILESY.o): $(JFEFILESC.h) $(JFEFILESY.h) $(YHFILES.h)
$(JFEFILESC.o): $(JFEFILESC.h) $(YHFILES.h)
$(YHFILES.o): $(YHFILES.h)

$(INSTALL_DIR):
	mkdir -p $@

clean:
	$(RM) -rf $(BUILD)/objs/j4e
	$(RM) -rf $(BUILD)/bin/j4e
	$(RM) -f y.* jfeparse.c core
	$(RM) -rf $(LISTPATH)

love:
	@echo "Not war?"

include $(TOP)/E/makefiles/Rules.gmk
