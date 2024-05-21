SUBNAME = rep
MODNAME =  tests/$(SUBNAME)
PACKAGE =  ec.tests.$(SUBNAME)

SAFETY_MODE = STEWARD

include $(TOP)/E/makefiles/Defs.gmk

FILES.java = \
	Animals.java \
	rp.java \
	brep.java \
	rept.java \
	srep.java \


	#	dhtest.java \
	#	nrep.java \
	#	drep.java \
	#	prep.java \
	#	wrep.java \
	#	dhver.java \
	#	xxxtst.java \
	#	rls.java \
	#	rtst.java \
	#	ptst.java \
	#	partst.java \
	#	cryptest.java \
	#	gentst.java \
	#	protest.java \
	#	altst.java \


all:	mk_dirs mk_java

mk_java: classes

mk_dirs:
	@echo ""
	mkdir -p $(LISTPATH)
	@echo ""

clean:
	rm -rf $(CLASSDIR)$(PKGDIR)
	rm -rf $(LISTPATH)
	rm -rf $(CLASSLIST) $(TOUCHLIST)
	rm -f $(FILES.javat) $(FILES.et)

run:
	-$(JAVAEC) ec.tests.db.xxxtst
	-$(JAVAEC) ec.tests.db.rtst
	-$(JAVAEC) ec.tests.db.rls
	-$(JAVAEC) ec.tests.db.partst
	-$(JAVAEC) ec.tests.db.gentst
	-$(JAVAEC) ec.tests.db.protest

include $(TOP)/E/makefiles/Rules.gmk


