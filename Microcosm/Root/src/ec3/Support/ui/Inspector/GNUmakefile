PACKAGE		= ec.ui
MODNAME		= inspector
DOCDIR          = $(TOP)/../../www-int/intdocs/javadoc

SAFETY_MODE = STEWARD

include $(TOP)/E/makefiles/Defs.gmk

all:
	$(MAKE) basics FILES.java=" \
				CheckPointInspector.java \
				InspectorUIPreferences.java \
				ScrollingButtonListView.java \
				FieldListView.java \
				FieldView.java \
				QueueView.java \
				RunletListView.java \
				RunletView.java \
				ScrollingRunletListView.java \
				IFCInspectorUI.java \
				InspectorRunner.java \
				InspectorButton.java \
				InspectorEvent.java \
				InspectorProportionalTextFieldView.java \
				InspectorTextField.java \
				InspectorView.java \
				InspectorViewToolBar.java \
				ButtonListView.java \
				ButtonListLayoutManager.java \
				ListButtonView.java \
				CategoryListViewToolBar.java \
				GatheredListView.java \
				EZListener.java \
				GatheredListViewToolBar.java"


FILES.javadoc = " \
				CheckPointInspector.java \
				InspectorUIPreferences.java \
				ScrollingButtonListView.java \
				FieldListView.java \
				FieldView.java \
				QueueView.java \
				IFCInspectorUI.java \
				InspectorEvent.java \
				InspectorProportionalTextFieldView.java \
				InspectorTextField.java \
				InspectorView.java \
				InspectorViewTitleBar.java \
				InspectorViewToolBar.java \
				ButtonListView.java \
				ButtonListLayoutManager.java \
				ListButtonView.java \
				CategoryListViewToolBar.java \
				GatheredListView.java \
				GatheredListViewToolBar.java"

javadoc:
	javadoc -classpath $(JAVA_CLASSPATH) -notree -noindex -d $(DOCDIR) $(FILES.javadoc)

clean: basic_clean

include $(TOP)/E/makefiles/Rules.gmk
