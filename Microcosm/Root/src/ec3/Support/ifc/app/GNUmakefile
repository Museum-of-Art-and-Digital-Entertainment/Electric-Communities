MODNAME	= ifcapp
PACKAGE	= ec.ifc.app

SAFETY_MODE = CREW

FILES.javac = \
	ECApplication.java \
	ECBevelBorder.java \
	ECBitmap.java \
	ECButton.java \
	ECChecklist.java \
	ECChecklistOwner.java \
	ECComboBox.java \
	ECControlGroup.java \
	ECControlGroupItem.java \
	ECEvent.java \
	ECExternalWindow.java \
	ECGridView.java \
	ECIconItem.java \
	ECImageView.java \
	ECKeyFilter.java \
	ECListItem.java \
	ECListView.java \
	ECNotifier.java \
	ECPageTurner.java \
	ECPopup.java \
	ECProgressBar.java \
	ECRectView.java \
	ECRootView.java \
	ECScaledImageListItem.java \
	ECScrollBorder.java \
	ECSimpleCGItem.java \
	ECStringUtilities.java \
	ECTabItem.java \
	ECTabKeyDispenser.java \
	ECTabKeyHandler.java \
	ECTabView.java \
	ECTipViewOwner.java \
	ECSlider.java \
	ECStaticTextCGItem.java \
	ECTextField.java \
	ECTextView.java \
	ECTwoControlCGItem.java \
	ECWindowBorder.java \
	ECWindowController.java \
	IFCButtonAccess.java \
	IFCBoot.java \
	IFCListViewAccess.java \
	MainWindowKeeper.java \
	TraceUtils.java \
	WindowController.java \
	WindowPositioner.java \

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

DOCDIR       = $(TOP)/../../../www-int/engineering/API_doc/javadoc
FILES.javadoc = $(FILES.javac)

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
