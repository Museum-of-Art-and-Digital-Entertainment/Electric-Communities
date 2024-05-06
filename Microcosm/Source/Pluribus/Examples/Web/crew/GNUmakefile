PACKAGE		= ec.pl.examples.web
MODNAME		= Web

SAFETY_MODE = CREW

FILES.java = \
	WebEvent.java \
	IFCWeb.java \
	WebApplication.java \
	ConsoleWebCrew.java \

include $(TOP)/E/makefiles/Defs.gmk

all: basics

initCrew:
	$(MAKE) basics FILES.javac=" \
		crew/WebEvent.java \
		crew/WebApplication.java \
		"

clean: basic_clean

include $(TOP)/E/makefiles/Rules.gmk
