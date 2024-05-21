PACKAGE = ec.cosm.ingredients.destination
MODNAME = destination

include $(TOP)/E/makefiles/Defs.gmk

all:
	$(MAKE) basics \
		FILES.java=" \
			Walk_state.java \
		" \
		FILES.e=" \
			Walk_kind.e \
			WalkToThis_ingr.e \
		"

state:
	$(MAKE) basics \
		FILES.java=" \
			Walk_state.java \
		"

kind:
	$(MAKE) basics \
		FILES.e=" \
			Walk_kind.e \
		"

ingr:
	$(MAKE) basics \
		FILES.e=" \
			WalkToThis_ingr.e \
		"

karl:
	$(MAKE) basics \
		FILES.java=" \
			Walk_state.java \
		" \
		FILES.e=" \
			Walk_kind.e \
		"

clean: basic_clean

again: clean all

kagain: clean karl

include $(TOP)/E/makefiles/Rules.gmk


