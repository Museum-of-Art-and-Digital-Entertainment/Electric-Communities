package ec.ui;

import netscape.application.*;
import netscape.util.*;


// adapted from the ComboBox sample that shipped with IFC1.0
public class ECComboBox extends View implements Target, TextFilter, TextFieldOwner, Codable {
    InternalWindow  dropWindow;
    ScrollGroup     scrollGroup;
    ListView        listView;
    TextField       textField;
    Button          dropButton;
    boolean         listIsVisible;
    Border          border;
    int             maxShowCount;
    TextFilter      textFilter;
    TextFieldOwner  textFieldOwner;
    boolean         isDownInListView;

    private static final String CHOOSE_ITEM = "chooseItem";
    private static final String DROP_LIST = "dropList";

    final static String DROPWINDOW_KEY = "dropWindow",
                        SCROLLGROUP_KEY = "scrollGroup",
                        LISTVIEW_KEY = "listView",
                        TEXTFIELD_KEY = "textField",
                        DROPBUTTON_KEY = "dropButton",
                        BORDER_KEY = "border",
                        MAXSHOWCOUNT_KEY = "maxShowCount",
                        TEXTFILTER_KEY = "textFilter",
                        TEXTFIELDOWNER_KEY = "textFieldOwner";

	private static final String BUTTON_IMAGE_NAME = "netscape/application/ScrollDownArrow.gif";

/// creation methods
    public ECComboBox()   {
        this(0,0,0,0);
    }

    public ECComboBox(int x, int y, int width, int height)    {
        super(x,y,width,height);

        Bitmap  buttonImage;
        buttonImage = Bitmap.bitmapNamed(BUTTON_IMAGE_NAME);

        // Set up default values
        listIsVisible = false;
        maxShowCount = 6;
        border = BezelBorder.loweredBezel();
        textFilter = null;
        textFieldOwner = null;
        isDownInListView = false;

        // create components
        dropWindow  = new InternalWindow(Window.BLANK_TYPE,0,
                                            height,width,height);
        scrollGroup = new ScrollGroup(0,0,width,height);
        dropButton  = new Button(width-buttonImage.width()-border.rightMargin(),
                                    border.topMargin(),
                                    buttonImage.width(),
                                    height-border.heightMargin());
        // There is some space between dropButton and textField
        textField   = new TextField(border.leftMargin()+1,
                                    border.topMargin(),
                                    width-dropButton.width()-6-1,
                                    height-border.heightMargin());
        listView    = new ListView(0,0,textField.width(),height);

        // prepare components
        listView.setAllowsEmptySelection(true);
        listView.setTracksMouseOutsideBounds(false);
        listView.setTarget(this);
        listView.setCommand(CHOOSE_ITEM);

        scrollGroup.setContentView(listView);
        scrollGroup.setHasVertScrollBar(true);
        scrollGroup.setHorizResizeInstruction(WIDTH_CAN_CHANGE);
        scrollGroup.setVertResizeInstruction(HEIGHT_CAN_CHANGE);

        dropWindow.addSubview(scrollGroup);

        dropButton.setBordered(true);
        dropButton.setImage(buttonImage);

        textField.setBorder(null);
        textField.setFilter(this);
        textField.setOwner(this);

        this.addSubview(textField);
        this.addSubview(dropButton);
    }

/// view handling methods
    public void drawView(Graphics g) {
        border.drawInRect(g, 0, 0, width(), height());
    }

    public void performCommand(String command, Object object)   {
        if(DROP_LIST.equals(command))   {
            setListIsVisible(!listIsVisible);
        } else if (CHOOSE_ITEM.equals(command))   {
            updateTextField();
            setListIsVisible(false);
        } else  {
            throw new NoSuchMethodError("unknown command: " + command);
        }
    }

/// mouse handling method
    public View viewForMouse(int x, int y) {
        View  realView = super.viewForMouse(x, y);
        if(realView == dropButton)
            return this;
        else
            return realView;
    }

    public boolean mouseDown(MouseEvent event) {
        if (!isEnabled())
			return false;
        
        isDownInListView = false;
        if(super.viewForMouse(event.x, event.y) == dropButton) {
            setListIsVisible(!listIsVisible);
            this.setFocusedView();
            return true;
        }

        return false;
    }

    public void mouseDragged(MouseEvent event) {
        MouseEvent convEvent;

        if(!listIsVisible)   {
            return;
        }

        convEvent = convertEventToView(dropWindow, event);
        if(!isDownInListView
                && convEvent.x > 0 && convEvent.x < dropWindow.bounds.width
                && convEvent.y > 0 && convEvent.y < dropWindow.bounds.height) {
            listView.mouseDown(convertEventToView(listView, event));
            isDownInListView = true;
        }  else if(isDownInListView)    {
            listView.mouseDragged(convertEventToView(listView, event));
        }
    }

    public void mouseUp(MouseEvent event) {
        if(super.viewForMouse(event.x, event.y) == dropButton) {
            return;
        }
        if (listIsVisible) {
            listView.mouseUp(convertEventToView(listView, event));
        }
    }

    public void stopFocus()    {
        updateListViewSelection();
        if(listIsVisible)
            setListIsVisible(false);
    }

/// TextFilter method
    public boolean acceptsEvent(Object anObject, KeyEvent event, Vector eventVector)    {
        if(textFilter != null
            && !textFilter.acceptsEvent(anObject, event, eventVector))  {
            return false;
        }

        if (event.isUpArrowKey()) {
            selectPrevItem();
            if(selectedIndex() > -1)
                listView.scrollItemAtToVisible(selectedIndex());
            textField.setFocusedView();
            setListIsVisible(false);
            return false;
        } else if(event.isDownArrowKey())   {
            selectNextItem();
            if(selectedIndex() > -1)
                listView.scrollItemAtToVisible(selectedIndex());
            textField.setFocusedView();
            setListIsVisible(false);
            return false;
        }
        return true;

    }

/// List Item handling methods
    public void setListIsVisible(boolean value) {
        int rowHeight, itemCount, showCount;

        itemCount = listView.count();

        if(value && itemCount > 0 )   {
            dropButton.setState(true);
            listView.sizeToMinSize();
            rowHeight = listView.itemAt(0).minHeight();
            if(itemCount >= maxShowCount)
                showCount = maxShowCount+1;
            else
                showCount = itemCount;
			dropWindow.setRootView(rootView());	// ensure list appears in same window as ComboBox
            dropWindow.moveTo(bounds.x, bounds.maxY());
            dropWindow.sizeTo(bounds.width, rowHeight*showCount);
            dropWindow.show();
            listIsVisible = true;
        } else {
            dropButton.setState(false);
            dropWindow.hide();
            listIsVisible = false;
        }

    }

    private void updateTextField()    {
        if(listView.selectedItem() != null)
            textField.setStringValue(listView.selectedItem().title());
    }

    private void updateListViewSelection()    {
        int selectedRow = rowWithTitle(stringValue());
        if(selectedRow > -1)    {
            listView.selectItemAt(selectedRow);
            listView.scrollItemAtToVisible(selectedRow);
        }
    }

/// Cover Methods
    public void addItem(String value)   {
        listView.addItem().setTitle(value);
    }

    public void insertItemAt(String value, int index)   {
        listView.insertItemAt(index).setTitle(value);
    }

    public void addItems(Vector values) {
        int i, max;
        max = values.count();
        for(i = 0; i < max; i++)    {
            listView.addItem().setTitle((String)values.elementAt(i));
        }
    }

    public void removeItem(String value) {
        int targetRow = rowWithTitle(value);
        if(targetRow > -1)
            listView.removeItemAt(targetRow);
    }

    public void removeItemAt(int index) {
        listView.removeItemAt(index);
    }

    public void removeAllItems()    {
        listView.removeAllItems();
    }

    public int selectedIndex()    {
        return listView.selectedIndex();
    }

/**
 * Sets the font of the list view's prototype item and the text view
 * to <b>newFont</b>. This has no effect on list items already in
 * the list, so generally you should call this just after the combo box
 * is created, while it's still empty.
 */
	public void setFont(Font newFont) {
		listView.prototypeItem().setFont(newFont);
		textField.setFont(newFont);
	}

    public int rowWithTitle(String title)   {
        int i,max;

        max = listView.count();
        for(i = 0; i < max; i++)  {
            if(listView.itemAt(i).title().equals(title))
                return i;
        }
        return -1;
    }

    public int count()  {
        return listView.count();
    }

	public int intValue() {
		return textField.intValue();
	}

	public void setIntValue(int value) {
		textField.setIntValue(value);
	}

    public String stringValue() {
        return textField.stringValue();
    }

    public void setStringValue(String value)    {
        textField.setStringValue(value);
    }

    public void selectRowWithTitle(String title)    {
        int selectedRow = rowWithTitle(title);
        if(selectedRow > -1)    {
            listView.selectItemAt(selectedRow);
            updateTextField();
        }
    }

    public void selectRow(int index)    {
        if(index > -1 && index < (listView.count() -1)) {
            listView.selectItemAt(index);
            updateTextField();
        }
    }

    public void selectNextItem()    {
        if(listView.selectedIndex() < listView.count()-1) {
            listView.selectItemAt(listView.selectedIndex()+1);
        }
        updateTextField();
    }

    public void selectPrevItem()    {
        if(listView.selectedIndex() > 0 && listView.count() > 1)  {
            listView.selectItemAt(listView.selectedIndex()-1);
        }
        updateTextField();
    }

	public boolean isEnabled() {
		// assumes text field and button have same enabled state,
		// which they will if setEnabled was used
		return dropButton.isEnabled();
	}

	public void setEnabled(boolean newValue) {
		if (newValue == isEnabled())
			return;

		if (newValue) {
			dropButton.setEnabled(true);
			dropButton.setImage(Bitmap.bitmapNamed(BUTTON_IMAGE_NAME));
			textField.setEditable(true);
			textField.setTextColor(Color.black);
			textField.setBackgroundColor(Color.white);
		}
		else {
			dropButton.setEnabled(false);
			dropButton.setImage(null);
			textField.setEditable(false);
			textField.setTextColor(Color.gray);
			textField.setBackgroundColor(Color.lightGray);
		}

		setDirty(true);
	}

/// Ivar accessors
    public int maxShowCount()   {
        return maxShowCount;
    }

    public void setMaxShowCount(int value)  {
        if(value >= 0)
            maxShowCount = value;
        else
            throw new InconsistencyException(this
                + " setMaxShowCount must be greater than zero.");
    }

    public TextFilter filter()  {
        return textFilter;
    }

    public void setFilter(TextFilter object)    {
        textFilter = object;
    }

    public TextFieldOwner owner()  {
        return textFieldOwner;
    }

    public void setOwner(TextFieldOwner object)    {
        textFieldOwner = object;
    }

/**
 * The combo box's text field. An object that has made itself the owner of the
 * combo box might need this so it can check which text field is sending them messages.
 */
	public TextField textField() {
		return textField;
	}

/// TextFieldOwner messages
    public void textEditingDidBegin(TextField textfield)    {
        if(textFieldOwner != null)   {
            textFieldOwner.textEditingDidBegin(textfield);
        }
    }

    public void textEditingDidEnd(TextField textfield, int endCondition, boolean contentsChanged)   {
        updateListViewSelection();
        if(textFieldOwner != null)   {
            textFieldOwner.textEditingDidEnd(textfield, endCondition, contentsChanged);
        }
    }

    public boolean textEditingWillEnd(TextField textfield, int endCondition, boolean contentsChanged)   {
        if(textFieldOwner != null)   {
            return textFieldOwner.textEditingWillEnd(textfield, endCondition, contentsChanged);
        }
        return true;
    }

    public void textWasModified(TextField textfield)    {
        if(textFieldOwner != null)   {
            textFieldOwner.textWasModified(textfield);
        }
    }

/// Codable Interface
    public void describeClassInfo(ClassInfo info)   {
        super.describeClassInfo(info);

        info.addClass("ECComboBox", 1);
        info.addField(DROPWINDOW_KEY, OBJECT_TYPE);
        info.addField(SCROLLGROUP_KEY, OBJECT_TYPE);
        info.addField(LISTVIEW_KEY, OBJECT_TYPE);
        info.addField(TEXTFIELD_KEY, OBJECT_TYPE);
        info.addField(DROPBUTTON_KEY, OBJECT_TYPE);
        info.addField(BORDER_KEY, OBJECT_TYPE);
        info.addField(MAXSHOWCOUNT_KEY, INT_TYPE);
        info.addField(TEXTFILTER_KEY, OBJECT_TYPE);
        info.addField(TEXTFIELDOWNER_KEY, OBJECT_TYPE);
    }

    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);

        dropWindow = (InternalWindow)decoder.decodeObject(DROPWINDOW_KEY);
        scrollGroup = (ScrollGroup)decoder.decodeObject(SCROLLGROUP_KEY);
        listView = (ListView)decoder.decodeObject(LISTVIEW_KEY);
        textField = (TextField)decoder.decodeObject(TEXTFIELD_KEY);
        dropButton = (Button)decoder.decodeObject(DROPBUTTON_KEY);
        border = (Border)decoder.decodeObject(BORDER_KEY);
        maxShowCount = decoder.decodeInt(MAXSHOWCOUNT_KEY);
        textFilter = (TextFilter)decoder.decodeObject(TEXTFILTER_KEY);
        textFieldOwner = (TextFieldOwner)decoder.decodeObject(TEXTFIELDOWNER_KEY);

    }

    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeObject(DROPWINDOW_KEY, dropWindow);
        encoder.encodeObject(SCROLLGROUP_KEY, scrollGroup);
        encoder.encodeObject(LISTVIEW_KEY, listView);
        encoder.encodeObject(TEXTFIELD_KEY, textField);
        encoder.encodeObject(DROPBUTTON_KEY, dropButton);
        encoder.encodeObject(BORDER_KEY, border);
        encoder.encodeInt(MAXSHOWCOUNT_KEY, maxShowCount);
        encoder.encodeObject(TEXTFILTER_KEY, textFilter);
        encoder.encodeObject(TEXTFIELDOWNER_KEY, textFieldOwner);
    }

    public void finishDecoding() throws CodingException {
        super.finishDecoding();
    }
}
