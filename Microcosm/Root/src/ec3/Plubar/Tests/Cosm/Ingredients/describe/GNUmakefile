PACKAGE = ec.cosm.ingredients.describe
MODNAME = describe

include $(TOP)/E/makefiles/Defs.gmk

all:
	$(MAKE) basics \
		FILES.java=" \
			Describe_state.java \
		" \
		FILES.e=" \
			Describe_kind.e \
			Describe_ingr.e \
		"

state:
	$(MAKE) basics \
		FILES.java=" \
			Describe_state.java \
		"

kind:
	$(MAKE) basics \
		FILES.e=" \
			Describe_kind.e \
		"

ingr:
	$(MAKE) basics \
		FILES.e=" \
			Describe_ingr.e \
		"

karl:
	$(MAKE) basics \
		FILES.java=" \
			Describe_state.java \
		" \
		FILES.e=" \
			Describe_kind.e \
		"

clean: basic_clean

again: clean all

kagain: clean karl

include $(TOP)/E/makefiles/Rules.gmk


