MODNAME	= ifcapp
PACKAGE	= netscape.application

SAFETY_MODE = CREW

FILES.javac = \
		AWTCompatibility.java \
		AWTComponentView.java \
		Alert.java \
		AlertContentView.java \
		AppletResources.java \
		Application.java \
		ApplicationEvent.java \
		ApplicationEventFilter.java \
		ApplicationObserver.java \
		Autoscroller.java \
		BezelBorder.java \
		Bitmap.java \
		BitmapObserver.java \
		Border.java \
		BrokenImageAttachment.java \
		Button.java \
		CheckButtonImage.java \
		Clipboard.java \
		Color.java \
		ColorChooser.java \
		ColorWell.java \
		CommandEvent.java \
		CommandFilter.java \
		ContainerView.java \
		DebugGraphics.java \
		DebugGraphicsColorFilter.java \
		DebugGraphicsInfo.java \
		DragDestination.java \
		DragSession.java \
		DragSource.java \
		DragView.java \
		DragWell.java \
		DrawingSequence.java \
		DrawingSequenceOwner.java \
		EmptyBorder.java \
		Event.java \
		EventFilter.java \
		EventLoop.java \
		EventProcessor.java \
		ExtendedTarget.java \
		ExternalWindow.java \
		FastStringBuffer.java \
		FileChooser.java \
		Font.java \
		FontChooser.java \
		FontItem.java \
		FontMetrics.java \
		FormElement.java \
		FoundationApplet.java \
		FoundationAppletStub.java \
		FoundationCheckMenuItem.java \
		FoundationDialog.java \
		FoundationFrame.java \
		FoundationMenuItem.java \
		FoundationPanel.java \
		FoundationWindow.java \
		Graphics.java \
		GraphicsState.java \
		GridLayout.java \
		HRTextAttachment.java \
		HTMLElement.java \
		HTMLParser.java \
		HTMLParsingException.java \
		HTMLParsingRules.java \
		HTMLTokenGenerator.java \
		Image.java \
		ImageAttachment.java \
		ImageSequence.java \
		InternalAlertBorder.java \
		InternalWindow.java \
		InternalWindowBorder.java \
		JDK11AirLock.java \
		KeyEvent.java \
		KeyStroke.java \
		KeyboardArrow.java \
		Label.java \
		LayoutManager.java \
		LineBorder.java \
		ListItem.java \
		ListView.java \
		Menu.java \
		MenuBorder.java \
		MenuItem.java \
		MenuShortcut.java \
		MenuView.java \
		ModalDialogManager.java \
		MouseEvent.java \
		MouseFilter.java \
		ObjectPool.java \
		PackConstraints.java \
		PackLayout.java \
		Point.java \
		Polygon.java \
		Popup.java \
		PopupItem.java \
		Range.java \
		Rect.java \
		ResizeFilter.java \
		RootView.java \
		ScrollBar.java \
		ScrollBarOwner.java \
		ScrollGroup.java \
		ScrollView.java \
		ScrollViewLineBorder.java \
		Scrollable.java \
		Size.java \
		Slider.java \
		Sound.java \
		SystemImages.java \
		Target.java \
		TargetChain.java \
		TextAttachment.java \
		TextBag.java \
		TextField.java \
		TextFieldOwner.java \
		TextFilter.java \
		TextParagraph.java \
		TextParagraphFormat.java \
		TextPositionInfo.java \
		TextSelection.java \
		TextStyleRun.java \
		TextView.java \
		TextViewHTMLContainer.java \
		TextViewHTMLContainerImp.java \
		TextViewHTMLElement.java \
		TextViewHTMLMarker.java \
		TextViewHTMLMarkerImp.java \
		TextViewHTMLString.java \
		TextViewOwner.java \
		Timer.java \
		TimerQueue.java \
		UpdateFilter.java \
		VectorCache.java \
		View.java \
		Window.java \
		WindowContentView.java \
		WindowInvalidationAgent.java \
		WindowOwner.java \

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes exports

all: basics

clean: basic_clean

DOCDIR       = $(TOP)/../../../www-int/engineering/API_doc/javadoc
FILES.javadoc = $(FILES.javac)

javadoc:
	javadoc -d $(DOCDIR) $(FILES.javadoc)

include $(TOP)/E/makefiles/Rules.gmk
