PACKAGE = ec.plubar
MODNAME = plubar

include $(TOP)/E/makefiles/Defs.gmk

all: eclasscrap

eclasscrap:
	$(MAKE) basics \
		FILES.java=" \
			UnumException.java \
			IngredientRoleClassEntry.java \
			UnumIngredientEntry.java \
			jCapabilityGroup.java \
			jiCapabilityRecouper.java \
			jStonecastCapabilityGroup.java \
			jRecoupableCapability.java \
			jFacetRecouper.java \
			jiSetFacetRecouper.java \
			jEditableCapabilityGroup.java \
			istBase.java \
			SoulState.java \
			UnumSoul.java \
			UnumDefinition.java \
			Presence.java \
			Unum.java \
			" \
		FILES.e=" \
			Ingredient.e \
			"

ingr:
	$(MAKE) basics \
		FILES.java=" \
			Ingredient.java \
			"
real_all: first second

first:
	$(MAKE) basics \
		FILES.javac=" \
			UnumException.java \
			IngredientRoleClassEntry.java \
			UnumIngredientEntry.java \
			jCapabilityGroup.java \
			jiCapabilityRecouper.java \
			jStonecastCapabilityGroup.java \
			jRecoupableCapability.java \
			" \
		FILES.java=" \
			jFacetRecouper.java \
			"

second:
	$(MAKE) basics \
		FILES.javac=" \
			jiSetFacetRecouper.java \
			jEditableCapabilityGroup.java \
			istBase.java \
			SoulState.java \
			UnumSoul.java \
			Ingredient.java \
			UnumDefinition.java \
			Presence.java \
			Unum.java \
			"

karl:
	$(MAKE) basics \
		FILES.javac=" \
			jCapabilityGroup.java \
			jiCapabilityRecouper.java \
			jStonecastCapabilityGroup.java \
			jRecoupableCapability.java \
			" \
		FILES.java="\
			jFacetRecouper.java \
			"
	$(MAKE) basics \
		FILES.javac=" \
			UnumIngredientEntry.java \
			"

test:
	$(MAKE) basics \
		FILES.javac=" \
			Test.java \
			"

clean: basic_clean

again: clean all

kagain: clean karl

include $(TOP)/E/makefiles/Rules.gmk


