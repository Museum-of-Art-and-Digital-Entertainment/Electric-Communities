MODNAME	= ifcstonelook
PACKAGE	= ec.ifc.stonelook

SAFETY_MODE = CREW

FILES.javac = \
	SLBezelBorder.java \
	SLButton.java \
	SLButtonBorder.java \
	SLChecklist.java \
	SLComboBox.java \
	SLControlGroup.java \
	SLContainerView.java \
	SLIconItem.java \
	SLListItem.java \
	SLListView.java \
	SLPageTurner.java \
	SLPopup.java \
	SLPopupItem.java \
	SLProgressBar.java \
	SLScrollBar.java \
	SLScrollGroup.java \
	SLScrollView.java \
	SLSlabBorder.java \
	SLSlider.java \
	SLTabItem.java \
	SLTabView.java \
	SLTextField.java \
	SLTextView.java \
	SLWindow.java \
	StoneLook.java

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

DOCDIR       = $(TOP)/../../../www-int/engineering/API_doc/javadoc
FILES.javadoc = $(FILES.javac)

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
