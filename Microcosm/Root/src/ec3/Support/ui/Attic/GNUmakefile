MODNAME	= ec_ui
PACKAGE	= ec.ui

FILES.java = \
	InspectorView.java \

FILES.javac = \
	ECApplication.java \
	ECBevelBorder.java \
	ECChecklist.java \
	ECChecklistOwner.java \
	ECComboBox.java \
	ECEvent.java \
	ECExternalWindow.java \
	ECRootView.java \
	ECScaledImageListItem.java \
	ECScrollBorder.java \
	ECTipViewOwner.java \
	ECTextField.java \
	ECTextView.java \
	ECWindowBorder.java \

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

DOCDIR       = $(TOP)/../../../www-int/intdocs/javadoc
FILES.javadoc = \
	ECApplication.java \
	ECBevelBorder.java \
	ECChecklist.java \
	ECChecklistOwner.java \
	ECEvent.java \
	ECExternalWindow.java \
	ECRootView.java \
	ECScaledImageListItem.java \
	ECScrollBorder.java \
	ECTipViewOwner.java \
	ECWindowBorder.java \

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
