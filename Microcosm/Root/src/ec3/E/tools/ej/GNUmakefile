#
# GNUmakefile for the 'ej' E-to-Java converter
#
# Chip Morningstar
# Electric Communities
# 25-July-1997
#

INSTALL_DIR = $(BUILD)/bin
MODNAME = ej
APP_PATH = $(INSTALL_DIR)/$(MODNAME)
YACCHELPER_DIR = ../../../Tools/yacchelper

EJFILESC.c = 	\
 ejclassfile.c  \
 ejclasstable.c	\
 ejfileio.c	\
 ejimport.c	\
 ejparse.c	\
 ejwindirs.c	\
 ejversion.c	\
 ejmain.c

EJFILESY.c = 	\
 ejdefine.c	\
 ejlex.c	\
 ejout.c

EJFILESC.h =	\
 ej.h		\
 generic.h

EJFILESY.h =	\
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

FILES.c = $(EJFILESC.c) $(EJFILESY.c) $(YHFILES.c)

EJFILESC.o = $(EJFILESC.c:%.c=$(BUILD)/objs/$(MODNAME)/%.o)
EJFILESY.o = $(EJFILESY.c:%.c=$(BUILD)/objs/$(MODNAME)/%.o)
YHFILES.o = $(YHFILES.c:%.c=$(BUILD)/objs/$(MODNAME)/%.o)

INCLDIRS = -I$(YACCHELPER_DIR)

CFLAGS += -Wreturn-type -I. $(INCLDIRS)

vpath %.c .
vpath %.c $(YACCHELPER_DIR)

.PHONY: all standalone mk_standalone clean

standalone:
	$(MAKE) mk_standalone

mk_standalone: $(APP_PATH)

include $(TOP)/E/makefiles/Defs.gmk

$(EJFILESY.o): $(EJFILESC.h) $(EJFILESY.h) $(YHFILES.h)
$(EJFILESC.o): $(EJFILESC.h) $(YHFILES.h)
$(YHFILES.o): $(YHFILES.h)

$(INSTALL_DIR):
	mkdir -p $@

clean:
	$(RM) -rf $(BUILD)/objs/ej
	$(RM) -rf $(BUILD)/bin/ej
	$(RM) -f y.* ejparse.c core
	$(RM) -rf $(LISTPATH)

love:
	@echo "Not war?"

include $(TOP)/E/makefiles/Rules.gmk
