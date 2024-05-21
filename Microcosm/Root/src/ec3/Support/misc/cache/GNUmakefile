MODNAME	= eccache
PACKAGE	= ec.misc.cache

SAFETY_MODE = CREW

include $(TOP)/E/makefiles/Defs.gmk

FILES.java = \
	Cache.java \
	EmptyHeapException.java \
	Heap.java \
	HeapItem.java 

INCLDIRS += -I$(JAVAINC) -I$(JAVAOSINC) -I$(CHEADERDIR)

all: basics

clean: basic_clean

include $(TOP)/E/makefiles/Rules.gmk
