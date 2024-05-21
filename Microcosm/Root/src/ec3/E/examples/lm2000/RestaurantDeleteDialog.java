
package ec.examples.lm2000;
/*
    This class is a basic extension of the Dialog class.  It can be used
    by subclasses of Frame.  To use it, create a reference to the class,
    then instantiate an object of the class (pass 'this' in the constructor),
    and call the show() method.

    example:

    RestaurantDeleteDialog theRestaurantDeleteDialog;
    theRestaurantDeleteDialog = new RestaurantDeleteDialog(this);
    theRestaurantDeleteDialog.show();

    You can add controls to AboutBox with Cafe Studio.
    (Menus can be added only to subclasses of Frame.)
 */

import java.awt.*;
import java.util.*;

public class RestaurantDeleteDialog extends Dialog {

    lm2000 state;
    public void Init(lm2000 l)
    {
        // set up
        state = l;
        Vector v = state.person.tastes;
        for(Enumeration en = v.elements(); en.hasMoreElements();) {
            Rating r = (Rating)en.nextElement();
            RestaurantList.addItem(r.restaurant.name);
        }
    }

    public RestaurantDeleteDialog(Frame parent) {

	    super(parent, "Delete Restaurant", true);

	    //{{INIT_CONTROLS
        setLayout(null);
        addNotify();
        resize(insets().left + insets().right + 273, insets().top + insets().bottom + 210);
        label1=new Label("Choose a restaurant to delete:");
        add(label1);
        label1.reshape(insets().left + 14,insets().top + 8,238,30);
        RestaurantList=new List();
        add(RestaurantList);
        RestaurantList.reshape(insets().left + 22,insets().top + 61,124,111);
        OKButton=new Button("OK");
        add(OKButton);
        OKButton.reshape(insets().left + 175,insets().top + 98,77,30);
        CancelButton=new Button("Cancel");
        add(CancelButton);
        CancelButton.reshape(insets().left + 175,insets().top + 143,77,30);
        //}}

    	setResizable(false);
    }

    public synchronized void show() {
    	Rectangle bounds = getParent().bounds();
    	Rectangle abounds = this.bounds();

    	move(bounds.x + (bounds.width - abounds.width)/ 2,
    	     bounds.y + (bounds.height - abounds.height)/2);

    	super.show();
    }

    public synchronized void wakeUp() {
    	notify();
    }

    public boolean handleEvent(Event event) {
    	if (event.id == Event.ACTION_EVENT && event.target == CancelButton) {
    	    	clickedCancelButton();
    	    	return true;
    	}
    	else
    	if (event.id == Event.ACTION_EVENT && event.target == OKButton) {
    	    	clickedOKButton();
    	    	return true;
    	}
    	else

    	if (event.id == Event.WINDOW_DESTROY) {
    	    hide();
    	    return true;
    	}
    	return super.handleEvent(event);
    }

    //{{DECLARE_CONTROLS
    Label label1;
    List RestaurantList;
    Button OKButton;
    Button CancelButton;
    //}}
    public void clickedOKButton() {
        handleEvent(new Event(this, Event.WINDOW_DESTROY, null));
        String name = RestaurantList.getSelectedItem();
        // send delete message to server
        if (name!= null) {
            state.server<-processDeleteRestaurant(name);
        }
    }
    public void clickedCancelButton() {
        handleEvent(new Event(this, Event.WINDOW_DESTROY, null));
    }
}


