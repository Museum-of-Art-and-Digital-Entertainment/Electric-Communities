PACKAGE = ec.pl.runtime.plubar
MODNAME = plubar
PLUNITDIRS	= $(BUILD)/$(PLGEN)/objects

include $(TOP)/E/makefiles/Defs.gmk

all:
	$(MAKE) basics FILES.javac=" \
		UnumException.java \
		IngredientRoleClassEntry.java \
		UnumIngredientEntry.java \
		UnumDefinition.java \
		Ingredient.java \
		Presence.java \
		Unum.java \
		"

karl:
	$(MAKE) basics FILES.javac=" \
		UnumException.java \
		IngredientRoleClassEntry.java \
		UnumIngredientEntry.java \
		UnumDefinition.java \
		Ingredient.java \
		Presence.java \
		"

clean: basic_clean

SAFETY_MODE = STEWARD

include $(TOP)/E/makefiles/Rules.gmk


