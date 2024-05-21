MODNAME = pl
YACCHELPER_DIR = ../../Tools/yacchelper

PLCFILES.c = 	\
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
 plmain.c       \
 y.tab.c

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

INCLDIRS = -I$(YACCHELPER_DIR)

# Suppress "format truncation" errors; apparently gcc thinks it should warn
# when your snprintf call could be processing endless user strings and
# trancates as a result.
CFLAGS += -Wall -Wformat-truncation=0 -Wreturn-type -I. $(INCLDIRS) $(XFLAGS)
CLFLAGS += $(XFLAGS)

vpath %.c .
vpath %.c $(YACCHELPER_DIR)

pluribus: $(PLFILES.o) $(YHFILES.o)
	$(CC) -o pluribus $(PLFILES.o) $(YHFILES.o)

y.tab.h: pluribus.y
	bison -dy pluribus.y

clean:
	rm -f pluribus $(PLFILES.o) $(YHFILES.o)

$(PLFILES.o): $(PLFILES.h) $(YHFILES.h)
$(YHFILES.o): $(YHFILES.h)

