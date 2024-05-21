#
# GNUmakefile for Pluribus compiler
#
# Chip Morningstar
# Electric Communities
# 10-January-1997
#

INSTALL_DIR = $(BUILD)/bin
MODNAME = pl
PACKAGE = ec.plcompile
APP_PATH = $(INSTALL_DIR)/$(MODNAME)
YACCHELPER_DIR = ../../Tools/yacchelper

PLCFILES.c = 	\
 plparse.c	\
 plbackack.c	\
 plcheck.c	\
 plcode.c	\
 plexpr.c	\
 plfileio.c	\
 plfind.c	\
 plgrind.c	\
 plimport.c	\
 plingredient.c	\
 pllex.c	\
 plmangle.c	\
 plout.c	\
 plpresence.c	\
 plroute.c	\
 plscope.c	\
 plunit.c	\
 plunum.c	\
 plutil.c	\
 plversion.c	\
 plmain.c

PLFILES.h =	\
 pl.h		\
 generic.h	\
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

PLFILES.c = $(PLCFILES.c) $(PLJFILES.c)
FILES.c = $(PLFILES.c) $(YHFILES.c)

PLFILES.o = $(PLFILES.c:%.c=$(BUILD)/objs/$(MODNAME)/%.o)
YHFILES.o = $(YHFILES.c:%.c=$(BUILD)/objs/$(MODNAME)/%.o)

FILES.java = pljcompile.java

INCLDIRS = -I$(YACCHELPER_DIR) -I$(JAVAINC) -I$(JAVAOSINC) -I$(CHEADERDIR)

# Suppress "format truncation" errors; apparently gcc thinks it should warn
# when your snprintf call could be processing endless user strings and
# trancates as a result. - 2024 restoration effort.
CFLAGS += -Wall -Wformat-truncation=0 -Wreturn-type -I. $(INCLDIRS) $(XFLAGS)
CLFLAGS += $(XFLAGS)

CLASSES.export = $(PACKAGE).PluribusCompiler


vpath %.c .
vpath %.c $(YACCHELPER_DIR)

.PHONY: all standalone mk_standalone javaversion mk_javaversion mk_dirs mk_java exports clean

standalone:
	$(MAKE) mk_standalone PLJFILES.c="" XFLAGS=""

mk_standalone: $(APP_PATH)

javaversion:
	$(MAKE) mk_javaversion PLJFILES.c=pljcompile.c BUILDTYPE=biglib \
	XFLAGS="-Dmain=wrappedMain -Dexit=wrappedExit"

include $(TOP)/E/makefiles/Defs.gmk

mk_javaversion: mk_dirs mk_java exports $(FILES.o) $(GENLIBS)
	$(RM) -f $(BUILD)/objs/$(MODNAME)/plversion.o
	$(RM) -f $(BUILD)/objs/$(MODNAME)/plmain.o

$(PLFILES.o): $(PLFILES.h) $(YHFILES.h)

$(YHFILES.o): $(YHFILES.h)

$(INSTALL_DIR):
	mkdir -p $@

mk_dirs:
	mkdir -p $(FULLPATH)
	mkdir -p $(LISTPATH)

mk_java: classes

exports: $(FULLSTUBNAME) $(FULLSTUBONAME) $(CHEADERSTATE)

clean:
	$(RM) -rf $(BUILD)/objs/pl
	$(RM) -rf $(BUILD)/bin/pl
	$(RM) -f y.* plparse.c values.h core
	$(RM) -rf $(FULLPATH)
	$(RM) -rf $(GENLIBS)
	$(RM) -rf $(LISTPATH)
	$(RM) -rf $(CHEADERDIR)/pl
	$(RM) -rf $(BUILD_DRIVE)$(CLASSSTUBDIR)/pl*
	$(RM) -rf $(CLASSLIST) $(TOUCHLIST)
	$(RM) -f $(FILES.javat) $(FILES.et)

love:
	@echo "Not war?"

include $(TOP)/E/makefiles/Rules.gmk
