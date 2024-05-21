#
#  Copyright 1997 Electric Communities. All rights reserved worldwide.
#

MODNAME = syntax
PACKAGE = ec.ez.syntax

DOCDIR = $(TOP)/../../www-int/intdocs/javadoc

SAFETY_MODE = STEWARD

FILES.javac = \
	Assoc.java \
	EZIdentifier.java \
	EZLexer.java \
	EZLiteral.java \
	EZParser.java \
	EZQuasiString.java \
	EZToken.java \
	MsgPatt.java \
	SyntaxException.java


FILES.javadoc = $(FILES.java)


include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes

#
# XXX Sick implicit order dependency.  Doesn't work if you put "basics" first.
#
# XXX add EZParser.java when byacc is running on Solaris
all: basics

#grammar: EZParser.java

yaccpar.java: ez.y
	byacc -jvf ez.y

# EZParser.java: byaccfixer.awk yaccpar.java
grammar: byaccfixer.awk yaccpar.java

	awk -f byaccfixer.awk yaccpar.java > EZParser.java

clean: basic_clean

#	rm -f yaccpar.java
#	rm -f EZParser.java

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
