PACKAGE = ec.plubar.test.ColorShapeUnum
MODNAME = plubartest

include $(TOP)/E/makefiles/Defs.gmk

#KSSHackEClass All this FILES.e should go away when we can make Ingredients
#KSSHackEClass back into plain Java Classes.

all: first second

first:
	$(MAKE) basics \
		FILES.java=" \
			istColor.java \
			istShape.java \
			TestInterfaceGrandParent.java \
			TestInterfaceParent.java \
			TestInterface.java \
			" \
		FILES.e=" \
			ColorIngredient.e \
			ShapeIngredient.e \
			"

second:
	$(MAKE) basics \
		FILES.java=" \
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

include $(TOP)/E/makefiles/Rules.gmk


