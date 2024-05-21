PACKAGE = ec.cosm.ingredients.property
MODNAME = property

include $(TOP)/E/makefiles/Defs.gmk

all:
	$(MAKE) basics \
		FILES.java=" \
			Property_state.java \
		" \
		FILES.e=" \
			Property_kind.e \
			Property_ingr.e \
		"

state:
	$(MAKE) basics \
		FILES.java=" \
			Property_state.java \
		"

kind:
	$(MAKE) basics \
		FILES.e=" \
			Property_kind.e \
		"

ingr:
	$(MAKE) basics \
		FILES.e=" \
			Property_ingr.e \
		"

karl:
	$(MAKE) basics \
		FILES.java=" \
			Property_state.java \
		" \
		FILES.e=" \
			Property_kind.e \
		"

clean: basic_clean

again: clean all

kagain: clean karl

include $(TOP)/E/makefiles/Rules.gmk


