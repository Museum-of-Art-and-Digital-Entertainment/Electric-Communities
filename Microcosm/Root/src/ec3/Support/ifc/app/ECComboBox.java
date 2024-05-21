/**
 * ECComboBox class - implements windows style combo box.
 * adapted from the ComboBox sample that shipped with IFC1.0
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * John Sullivan
 *
 *  970816  agm     Make updatefield notify the field owner.
 *  970801  agm     Made Combobox resize correctly.
 *  970725  agm     Modified so combobox can be in subview.
 *
 */


package ec.ifc.app;

import netscape.application.*;
import netscape.util.*;

import ec.e.run.Trace;

public class ECComboBox extends View implements Target, TextFilter, TextFieldOwner {
    private InternalWindow  dropWindow;
    private ScrollGroup     scrollGroup;
    protected ListView      myListView;
    protected Button        myDropButton;
    private boolean         myListIsVisible;
    private Border          border;
    private int             maxShowCount;
    private TextFilter      textFilter;
    private TextFieldOwner  textFieldOwner;
    private boolean         isDownInListView;
    private boolean         myAutoComplete = true;
    protected TextField     myTextField;
    
    private static final String CHOOSE_ITEM = "chooseItem";
    private static final String DROP_LIST = "dropList";

    private static final String BUTTON_IMAGE_NAME = "netscape/application/ScrollDownArrow.gif";

/// creation methods
    public ECComboBox()   {
        this(0,0,0,0);
    }

    public ECComboBox(int x, int y, int width, int height)    {
        super(x,y,width,height);

        // Set up default values
        myListIsVisible = false;
        maxShowCount = 6;
        border = createBorder();
        textFilter = null;
        textFieldOwner = null;
        isDownInListView = false;

        // create components
        dropWindow  = new InternalWindow(Window.BLANK_TYPE,0,
                                            height,width,height);

        scrollGroup = createScrollGroup();
        scrollGroup.setBounds(0,0,width,height);
        myDropButton = createDropButton();
        // button is right-aligned, vertically centered
        myDropButton.moveTo(width - myDropButton.width() - border.rightMargin(),
                   border.topMargin() + 
                   (height - border.heightMargin() - myDropButton.height())/2);
        myTextField = createTextField();
        // There is some space between dropButton and textField
        myTextField.setBounds(border.leftMargin()+1,
                            border.topMargin(),
                            width-myDropButton.width()-6-1,
                            height-border.heightMargin());  

        myDropButton.setHorizResizeInstruction(LEFT_MARGIN_CAN_CHANGE);
        myTextField.setHorizResizeInstruction(WIDTH_CAN_CHANGE);
 
        myListView = createListView();
        myListView.setBounds(0,0,myTextField.width(),height);

        // prepare components
        myListView.setAllowsEmptySelection(true);
        myListView.setTracksMouseOutsideBounds(false);
        myListView.setTarget(this);
        myListView.setCommand(CHOOSE_ITEM);

        scrollGroup.setContentView(myListView);
        scrollGroup.setHasVertScrollBar(true);
        scrollGroup.setVertScrollBarDisplay(ScrollGroup.AS_NEEDED_DISPLAY);
        scrollGroup.setHorizResizeInstruction(WIDTH_CAN_CHANGE);
        scrollGroup.setVertResizeInstruction(HEIGHT_CAN_CHANGE);

        dropWindow.addSubview(scrollGroup);

        myTextField.setFilter(this);
        myTextField.setOwner(this);

        this.addSubview(myTextField);
        this.addSubview(myDropButton);
    }

    /**
     * Returns the border drawn around the quiescent combo box.
     * You never call this method, but you might override it to
     * use a different border in a subclass. This default
     * implementation returns BezelBorder.loweredBezel().
     */ 
    protected Border createBorder() {
        return BezelBorder.loweredBezel();      
    }   

    /**
     * Returns the button that drops down the combo box's menu.
     * The button is sized correctly but not yet placed.
     * You never call this method, but you might override it to
     * customize the button's appearance in a subclass.
     * This default implementation returns a new bordered Button
     * with the scroll-down image.
     */ 
    protected Button createDropButton() {
        Button button;
        Bitmap buttonImage = Bitmap.bitmapNamed(BUTTON_IMAGE_NAME);

        button  = new Button();
        button.sizeTo(buttonImage.width(), height() - border.heightMargin());       
        button.setBordered(true);
        button.setImage(buttonImage);
        return button;
    }   
    
    /**
     * Returns the list view in the active combo box.
     * The list view is not yet sized or placed.
     * You never call this method, but you might override it to
     * customize the list view's appearance in a subclass.
     * This default implementation returns a new ListView.
     */ 
    protected ListView createListView() {
        return new ListView();      
    }   

    /**
     * Returns the scroll group in the active combo box.
     * The scroll group is not yet sized or placed.
     * You never call this method, but you might override it to
     * customize the scroll group's appearance in a subclass.
     * This default implementation returns a new ScrollGroup.
     */ 
    protected ScrollGroup createScrollGroup() {
        return new ScrollGroup();       
    }   

    /**
     * Returns the text field of the combo box.
     * The text field is not yet sized or placed.
     * You never call this method, but you might override it to
     * customize the text field's appearance in a subclass.
     * This default implementation returns a new borderless TextField.
     */ 
    protected TextField createTextField() {
        TextField textField = new TextField();      
        textField.setBorder(null);
        return textField;
    }
    
/// view handling methods
    public void drawView(Graphics g) {
        border.drawInRect(g, 0, 0, width(), height());
    }

    public void performCommand(String command, Object object)   {
        if(DROP_LIST.equals(command))   {
            setListIsVisible(!myListIsVisible);
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
        if(realView == myDropButton)
            return this;
        else
            return realView;
    }

    public boolean mouseDown(MouseEvent event) {
        if (!isEnabled())
            return false;
        
        isDownInListView = false;
        if(super.viewForMouse(event.x, event.y) == myDropButton) {
            setListIsVisible(!myListIsVisible);
            this.setFocusedView();
            return true;
        }

        return false;
    }

    public void mouseDragged(MouseEvent event) {
        MouseEvent convEvent;

        if(!myListIsVisible)   {
            return;
        }

        convEvent = convertEventToView(dropWindow, event);
        if(!isDownInListView
                && convEvent.x > 0 && convEvent.x < dropWindow.bounds.width
                && convEvent.y > 0 && convEvent.y < dropWindow.bounds.height) {
            myListView.mouseDown(convertEventToView(myListView, event));
            isDownInListView = true;
            if (Trace.gui.debug && Trace.ON) {
                Trace.gui.debugm("dragged over combo box list view first time");
            }
        }  else if(isDownInListView)    {
            if (Trace.gui.debug && Trace.ON) {
                Trace.gui.debugm("dragged over combo box list view");
            }
            myListView.mouseDragged(convertEventToView(myListView, event));
        }
    }

    public void mouseUp(MouseEvent event) {
        if(super.viewForMouse(event.x, event.y) == myDropButton) {
            return;
        }
        if (myListIsVisible) {
            myListView.mouseUp(convertEventToView(myListView, event));
        }
    }

    public void stopFocus()    {
        updateListViewSelection();
        if(myListIsVisible)
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
                myListView.scrollItemAtToVisible(selectedIndex());
            myTextField.setFocusedView();
            setListIsVisible(false);
            return false;
        } else if(event.isDownArrowKey())   {
            selectNextItem();
            if(selectedIndex() > -1)
                myListView.scrollItemAtToVisible(selectedIndex());
            myTextField.setFocusedView();
            setListIsVisible(false);
            return false;
        }
        return true;

    }

/// List Item handling methods
    
    /**
     * Called when an item is added or removed from list.
     * Subclasses can override to change appearance, etc.
     */
    protected void listChanged() {};

    public void setListIsVisible(boolean value) {
        int rowHeight, itemCount, showCount;

        itemCount = myListView.count();

        if (value && itemCount > 0 )   {
            // measure border for later
            Border border = scrollGroup.border();
            int heightMargin, widthMargin;
            if (border != null)  {
                heightMargin = border.heightMargin();
                widthMargin = border.widthMargin();
            }
            else {
                heightMargin = 0;
                widthMargin = 0;
            }

            myDropButton.setState(true);
            // stretch list view all the way across so when the scroll bar
            // doesn't appear there isn't a useless vertical strip where the
            // scroll bar used to be
            myListView.sizeTo(scrollGroup.width() - widthMargin, 1);
            myListView.sizeToMinSize();
            rowHeight = myListView.rowHeight();
            if (itemCount >= maxShowCount)
                showCount = maxShowCount+1;
            else
                showCount = itemCount;
            dropWindow.setRootView(rootView()); // ensure list appears in same window as ComboBox
            Point destPoint = new Point();
            convertToView(rootView(), 0, myTextField.height() + heightMargin, destPoint);
            dropWindow.moveTo(destPoint.x, destPoint.y);
            dropWindow.sizeTo(bounds.width, rowHeight*showCount + heightMargin);
            dropWindow.show();

            myListIsVisible = true;
        } else {
            myDropButton.setState(false);
            dropWindow.hide();
            myListIsVisible = false;
        }

    }

    private void updateTextField()    {
        if(myListView.selectedItem() != null) {
            myTextField.setStringValue(myListView.selectedItem().title());
            myTextField.owner().textWasModified(myTextField);
            myTextField.setDirty(true);
        }
    }

    private void updateListViewSelection()    {
        int selectedRow = rowWithTitle(stringValue());
        if(selectedRow > -1)    {
            myListView.selectItemAt(selectedRow);
            myListView.scrollItemAtToVisible(selectedRow);
        }
    }

/// Cover Methods
    public void addItem(String value)   {
        myListView.addItem().setTitle(value);
        listChanged();
    }

    public void insertItemAt(String value, int index)   {
        myListView.insertItemAt(index).setTitle(value);
        listChanged();
    }

    public void addItems(Vector values) {
        int i, max;
        max = values.count();
        for(i = 0; i < max; i++)    {
            myListView.addItem().setTitle((String)values.elementAt(i));
        }
        if (i > 0) {
            listChanged();
        }
    }

    public void removeItem(String value) {
        int targetRow = rowWithTitle(value);
        if(targetRow > -1) {
            myListView.removeItemAt(targetRow);
            listChanged();
        }
    }

    public void removeItemAt(int index) {
        myListView.removeItemAt(index);
        listChanged();
    }

    public void removeAllItems()    {
        if (myListView.count() == 0) {
            return;
        }
        myListView.removeAllItems();
        listChanged();
    }

    public int selectedIndex()    {
        return myListView.selectedIndex();
    }

/**
 * Sets the font of the list view's prototype item and the text view
 * to <b>newFont</b>. This has no effect on list items already in
 * the list, so generally you should call this just after the combo box
 * is created, while it's still empty.
 */
    public void setFont(Font newFont) {
        myListView.prototypeItem().setFont(newFont);
        myTextField.setFont(newFont);
    }

    public int rowWithTitle(String title)   {
        int i,max;

        max = myListView.count();
        for(i = 0; i < max; i++)  {
            if(myListView.itemAt(i).title().equals(title))
                return i;
        }
        return -1;
    }

    public int count()  {
        return myListView.count();
    }

    public int intValue() {
        return myTextField.intValue();
    }

    public void setIntValue(int value) {
        myTextField.setIntValue(value);
    }

    public String stringValue() {
        return myTextField.stringValue();
    }

    public void setStringValue(String value)    {
        myTextField.setStringValue(value);
    }

    public void selectRowWithTitle(String title)    {
        int selectedRow = rowWithTitle(title);
        if(selectedRow > -1)    {
            myListView.selectItemAt(selectedRow);
            updateTextField();
        }
    }

    public void selectRow(int index)    {
        if(index > -1 && index < (myListView.count() -1)) {
            myListView.selectItemAt(index);
            updateTextField();
        }
    }

    public void selectNextItem()    {
        if(myListView.selectedIndex() < myListView.count()-1) {
            myListView.selectItemAt(myListView.selectedIndex()+1);
        }
        updateTextField();
    }

    public void selectPrevItem()    {
        if(myListView.selectedIndex() > 0 && myListView.count() > 1)  {
            myListView.selectItemAt(myListView.selectedIndex()-1);
        }
        updateTextField();
    }

    public boolean isEnabled() {
        // assumes text field and button have same enabled state,
        // which they will if setEnabled was used
        return myTextField.isEditable();
    }

    public void setEnabled(boolean newValue) {
        if (newValue == isEnabled())
            return;

        if (newValue) {
            myDropButton.setEnabled(true);
            myDropButton.setImage(Bitmap.bitmapNamed(BUTTON_IMAGE_NAME));
            myTextField.setEditable(true);
            myTextField.setTextColor(Color.black);
            myTextField.setBackgroundColor(Color.white);
        }
        else {
            myDropButton.setEnabled(false);
            myDropButton.setImage(null);
            myTextField.setEditable(false);
            myTextField.setTextColor(Color.gray);
            myTextField.setBackgroundColor(Color.lightGray);
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
     * The border surrounding the entire combo box
     * @see #createBorder
     */
    public Border border() {
        return border;
    }

    /**
     * The button that pops up the combo box's menu.
     * @see #createDropButton
     */
    public Button dropButton() {
        return myDropButton;
    }

    /**
     * The scroll group displayed when the combo box is active.
     * @see #createScrollGroup
     */
    public ScrollGroup scrollGroup() {
        return scrollGroup;
    }

/**
 * The combo box's text field. An object that has made itself the owner of the
 * combo box might need this so it can check which text field is sending them messages.
 * @see #createTextField
 */
    public TextField textField() {
        return myTextField;
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

    public void textWasModified(TextField textField)    {
        // autocompletion

        if (myAutoComplete) {
            int i,  match = 0;
            int numberOfMatches = 0;
            int itemCount = myListView.count();
            for (i = 0; i < itemCount; i++) {
                if (myListView.itemAt(i).title().startsWith(textField.stringValue())) {
                    numberOfMatches++;
                    match = i;
                }
            }
            if (numberOfMatches == 1)
                textField.setStringValue(myListView.itemAt(match).title());   
        }
        if(textFieldOwner != null)   {
            textFieldOwner.textWasModified(textField);
        }
    }

}
