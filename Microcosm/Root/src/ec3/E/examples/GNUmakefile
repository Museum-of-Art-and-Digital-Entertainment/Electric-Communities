
MODNAME	= examples
PACKAGE	= ec.examples

SAFETY_MODE = STEWARD

include $(TOP)/E/makefiles/Defs.gmk

FILES.e = \
	Counter1.e \
	CounterLong.e \
	Counter2.e \
	Counter3.e \
	Counter4.e \
	HelloComm.e \
	HelloSturdyRef.e \
	HastaComm.e \
	AlohaComm.e

#	HelloE.e \
#	HelloEWhen.e \
#	HelloEWhenNest.e \
#	DieRollComm.e \
#	DieRolling.e \
#	HelloEWhenInv.e \

#	Can.e

#FILES.java = \
#	DBexample.java

all: basics

clean: basic_clean

Count1:
	rm -f foo.evat
	$(JAVAEC) ec.e.start.EBoot ec.examples.cou.Counter1 checkpoint=foo

Count4:
	rm -f foo.evat
	$(JAVAEC) ec.e.start.EBoot ec.examples.cou.Counter4 checkpoint=foo

CountLong:
	rm -f foo.evat
	$(JAVAEC) ec.e.start.EBoot ec.examples.cou.CounterLong checkpoint=foo

Count2:
	rm -f foo.evat
	$(JAVAEC) ec.e.start.EBoot ec.examples.cou.Counter2 checkpoint=foo

Count3:
	rm -f foo.evat
	$(JAVAEC) ec.e.start.EBoot ec.examples.cou.Counter3 checkpoint=foo

Revived:
	$(JAVAEC) ec.e.quake.Revive foo

HelloE:
	$(MAKE) FILES.e=HelloE.e FILES.java=""
	$(JAVAEC) ec.e.start.EBoot ec.examples.he.HelloE

HelloEWhen:
	$(MAKE) FILES.e=HelloEWhen.e FILES.java=""
	$(JAVAEC) ec.e.start.EBoot ec.examples.heew.HelloEWhen

HelloEWhenInv:
	$(MAKE) FILES.e=HelloEWhenInv.e FILES.java=""
	$(JAVAEC) ec.e.start.EBoot ec.examples.heewi.HelloEWhenInv

HelloEWhenNest:
	$(MAKE) FILES.e=HelloEWhenNest.e FILES.java=""
	$(JAVAEC) ec.e.start.EBoot ec.examples.heewn.HelloEWhenNest

DieRolling:
	$(MAKE) FILES.e=DieRolling.e FILES.java=""
	$(JAVAEC) ec.examples.drg.DieRolling

DieRollComm:
	$(MAKE) FILES.e=DieRollComm.e FILES.java=""
	$(JAVAEC) ec.e.start.EBoot ec.examples.drc.DieRollComm

HelloComm:
	$(MAKE) FILES.e=HelloComm.e FILES.java=""
#	$(JAVAEC) ec.e.start.EBoot ec.examples.hc.HelloComm

HelloSturdyRef:
	$(MAKE) FILES.e=HelloSturdyRef.e FILES.java=""

AlohaComm:
	$(MAKE) FILES.e=AlohaComm.e FILES.java=""

HastaComm:
	$(MAKE) FILES.e=HastaComm.e FILES.java=""

DBexample:
	$(MAKE) FILES.java=DBexample.java FILES.e=""
	$(JAVAEC) ec.examples.dbe.DBexample

Can:
	$(MAKE) FILES.e=Can.e FILES.java=""
	$(JAVAEC) ec.e.start.EBoot ec.examples.canophone.CanophoneDemo

runthem:
	-$(JAVAEC) ec.examples.dbe.DBexample
	-$(JAVAEC) ec.e.start.EBoot ec.examples.cou.Counter1
	-$(JAVAEC) ec.e.start.EBoot ec.examples.cou.CounterLong
	-$(JAVAEC) ec.e.start.EBoot ec.examples.cou.Counter2
	-$(JAVAEC) ec.e.start.EBoot ec.examples.cou.Counter3
	-$(JAVAEC) ec.e.start.EBoot ec.examples.cou.Counter4
	-$(JAVAEC) ec.e.start.EBoot ec.examples.he.HelloE
	-$(JAVAEC) ec.e.start.EBoot ec.examples.heew.HelloEWhen
	-$(JAVAEC) ec.e.start.EBoot ec.examples.heewi.HelloEWhenInv
	-$(JAVAEC) ec.e.start.EBoot ec.examples.heewn.HelloEWhenNest
	-$(JAVAEC) ec.examples.drg.DieRolling
	-$(JAVAEC) ec.e.start.EBoot ec.examples.drc.DieRollComm
	-$(JAVAEC) ec.e.start.EBoot ec.examples.hc.HelloComm
	-$(JAVAEC) ec.e.start.EBoot ec.examples.canophone.CanophoneDemo

include $(TOP)/E/makefiles/Rules.gmk

