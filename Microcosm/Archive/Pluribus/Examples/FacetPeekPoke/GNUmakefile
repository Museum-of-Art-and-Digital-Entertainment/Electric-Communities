PACKAGE		= ec.pl.examples.PeekPoke
MODNAME		= PeekPoke

FILES.plu = \

FILES.javac = \

include $(TOP)/E/makefiles/Defs.gmk

all: una

clean: basic_clean

compile: basics

kind:
	$(MAKE) compile FILES.plu="PeekPokeShip_kind.plu \
		PeekPokeShip_state.plu" \
		FILES.e="" \
		FILES.java="j_RuntimeFacets.java"

facet: kind
	$(MAKE) compile FILES.plu="Pokeship_facet.plu" \
		FILES.e="" \
		FILES.java=""

ingr: facet
	$(MAKE) compile FILES.plu="Peekship_ingr.plu \
		Pokeship_ingr.plu" \
		FILES.e="" \
		FILES.java=""

una: ingr
	$(MAKE) compile FILES.plu="PeekPoker_unum.plu \
		PeekPokeable_unum.plu \
		Main.plu" \
		FILES.e="" \
		FILES.java=""

include $(TOP)/E/makefiles/Rules.gmk
