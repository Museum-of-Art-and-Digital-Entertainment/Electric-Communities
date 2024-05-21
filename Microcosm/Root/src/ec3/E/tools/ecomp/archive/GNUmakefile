MODNAME	= ecomp
PACKAGE	= ec.ecomp
SAFETY_MODE = ECOMP

FILES.eext = \
	ast.eext \
	attr.eext \
	basic.eext \
	bits.eext \
	captana.eext \
	classfile.eext \
	constants.eext \
	constfold.eext \
	defs.eext \
	dummies.eext \
	e2j.eext \
	easts.eext \
	eattr.eext \
	eparser.eext \
	epredef.eext \
	exprs.eext \
	gen.eext \
	items.eext \
	main.eext \
	names.eext \
	obj.eext \
	parser.eext \
	predef.eext \
	report.eext \
	scanner.eext \
	simplify.eext \
	stats.eext \
	zipfile.eext \
	lang/closures.eext

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports filtereext

real-all: ecomp-prep basics ecomp-done

from-scratch:
	@if [ -f $(BUILD)/objs/.ecomp-done ] ; then echo "(ecomp already made or copied)" ; else echo "(making ecomp from scratch)" ; $(MAKE) unchecked-from-scratch; fi

unchecked-from-scratch: filtereext ecomp-scratch-prep filter-and-compile ecomp-done

filter-and-compile:
	mkdir -p $(BUILD)/gencode/ecomp
	java ec.tools.filtereext.FilterEext $(BUILD_DRIVE)$(BUILD)/gencode/ecomp $(FILES.eext)
	(cd $(BUILD_DRIVE)$(BUILD)/gencode/ecomp/ec/ecomp; javac -deprecation -d $(BUILD_DRIVE)$(BUILD)/classes *.java lang$(FILE_SEPARATOR)*.java)

filtereext:
	mkdir -p $(BUILD)/classes
	(cd filter; $(MAKE))

e-none: real-all e-none-done

ecomp-done:
	rm -rf $(BUILD)/classes/ec/e
	mkdir -p $(BUILD)/objs
	touch $(BUILD)/objs/.ecomp-done

clean: basic_clean

include $(TOP)/E/makefiles/Rules.gmk
