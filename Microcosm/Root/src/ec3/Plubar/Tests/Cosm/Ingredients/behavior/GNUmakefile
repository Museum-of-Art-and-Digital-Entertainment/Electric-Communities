PACKAGE = ec.cosm.ingredients.behavior
MODNAME = behavior

include $(TOP)/E/makefiles/Defs.gmk

all:
	$(MAKE) basics \
		FILES.java=" \
			Behavior_state.java \
		" \
		FILES.e=" \
			Behavior_kind.e \
			Behavior_ingr.e \
		"

state:
	$(MAKE) basics \
		FILES.java=" \
			Behavior_state.java \
		"

kind:
	$(MAKE) basics \
		FILES.e=" \
			Behavior_kind.e \
		"

ingr:
	$(MAKE) basics \
		FILES.e=" \
			Behavior_ingr.e \
		"

karl:
	$(MAKE) basics \
		FILES.java=" \
			Behavior_state.java \
		" \
		FILES.e=" \
			Behavior_kind.e \
		"

clean: basic_clean

again: clean all

kagain: clean karl

include $(TOP)/E/makefiles/Rules.gmk


