SUBNAME = timer
MODNAME =  tests/$(SUBNAME)
PACKAGE =  ec.tests.$(SUBNAME)

include $(TOP)/E/makefiles/Defs.gmk

notbuilt = \
	EClockTest.e \
	ETimerTest.e \
	ETimerTest2.e \

FILES.e = \
	VatTimerTest.e \
	VatClockTest.e \

FILES.java = \
	TimerTest.java \

all: mk_dirs mk_java

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

EClockTest:
	$(MAKE) FILES.e=EClockTest.e FILES.java=

VatClockTest:
	$(MAKE) FILES.e=VatClockTest.e FILES.java=

VatTimerTest:
	$(MAKE) FILES.e=VatTimerTest.e FILES.java=

ETimerTest:
	$(MAKE) FILES.e=ETimerTest.e FILES.java=

ETimerTest2:
	$(MAKE) FILES.e=ETimerTest2.e FILES.java=

TimerTest:
	$(MAKE) FILES.java=TimerTest.java FILES.e=

run:
	-$(JAVAEC) ec.tests.timer.EClockTest
	-$(JAVAEC) ec.tests.timer.ETimerTest
	-$(JAVAEC) ec.tests.timer.ETimerTest2
	-$(JAVAEC) ec.tests.timer.TimerTest

include $(TOP)/E/makefiles/Rules.gmk

