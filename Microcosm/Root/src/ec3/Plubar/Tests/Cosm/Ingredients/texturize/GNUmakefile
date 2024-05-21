PACKAGE = ec.cosm.ingredients.texturize
MODNAME = texturize

include $(TOP)/E/makefiles/Defs.gmk

all:
	$(MAKE) basics \
		FILES.java=" \
			Texturize_state.java \
		" \
		FILES.e=" \
			Texturize_kind.e \
			Texturize_ingr.e \
		"

state:
	$(MAKE) basics \
		FILES.java=" \
			Texturize_state.java \
		"

kind:
	$(MAKE) basics \
		FILES.e=" \
			Texturize_kind.e \
		"

ingr:
	$(MAKE) basics \
		FILES.e=" \
			Texturize_ingr.e \
		"

karl:
	$(MAKE) basics \
		FILES.java=" \
			Texturize_state.java \
		" \
		FILES.e=" \
			Texturize_kind.e \
		"

clean: basic_clean

again: clean all

kagain: clean karl

include $(TOP)/E/makefiles/Rules.gmk


