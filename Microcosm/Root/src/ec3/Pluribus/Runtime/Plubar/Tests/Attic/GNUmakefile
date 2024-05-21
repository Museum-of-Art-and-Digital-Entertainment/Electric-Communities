PACKAGE = ec.pl.runtime.test
MODNAME = plubartest
PLUNITDIRS	= $(BUILD)/$(PLGEN)/objects

include $(TOP)/E/makefiles/Defs.gmk

all:
	$(MAKE) basics FILES.javac=" \
		istColor.java \
		ColorIngredient.java \
		istShape.java \
		ShapeIngredient.java \
		TestInterfaceGrandParent.java \
		TestInterfaceParent.java \
		TestInterface.java \
		ExampleUnum.java \
		TestExampleUnum.java \
		"

karl:
	$(MAKE) basics FILES.javac=" \
		istColor.java \
		ColorIngredient.java \
		istShape.java \
		ShapeIngredient.java \
		TestInterface.java \
		"

clean: basic_clean

again: clean all

SAFETY_MODE = STEWARD

include $(TOP)/E/makefiles/Rules.gmk


