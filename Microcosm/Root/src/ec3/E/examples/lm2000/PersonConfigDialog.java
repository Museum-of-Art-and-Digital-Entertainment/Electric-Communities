
package ec.examples.lm2000;
/*
    This class is a basic extension of the Dialog class.  It can be used
    by subclasses of Frame.  To use it, create a reference to the class,
    then instantiate an object of the class (pass 'this' in the constructor),
    and call the show() method.

    example:

    PersonConfigDialog thePersonConfigDialog;
    thePersonConfigDialog = new PersonConfigDialog(this);
    thePersonConfigDialog.show();

    You can add controls to AboutBox with Cafe Studio.
    (Menus can be added only to subclasses of Frame.)
 */

import java.awt.*;
import java.util.*;

public class PersonConfigDialog extends Dialog {
    lm2000 state;
    public void Init(lm2000 l)
    {
        // set up
        state = l;
        NameTextField.setText(state.person.name);
        BudgetTextField.setText("" + state.person.budget);
        TimeTextField.setText("" + state.person.minutes);
        Vector v = state.person.tastes;
        for(Enumeration en = v.elements(); en.hasMoreElements();) {
            Rating r = (Rating)en.nextElement();
            RestaurantList.addItem(r.restaurant.name);
        }
        if (v.size() != 0) {
            RestaurantList.select(0);
            selChangeRestaurantList(null);
        } else {
            check0.disable();
            check1.disable();
            check2.disable();
            check3.disable();
            check4.disable();
            check5.disable();
            check6.disable();
            check7.disable();
            check8.disable();
            check9.disable();
        }
    }

    private Frame parent = null; // kluge

    public PersonConfigDialog(Frame p) {

	    super(p, "Edit Your Profile", true);
        parent = p;

	    //{{INIT_CONTROLS
        setLayout(null);
        addNotify();
        resize(insets().left + insets().right + 399, insets().top + insets().bottom + 315);
        group1= new CheckboxGroup();
        label1=new Label("Name");
        add(label1);
        label1.reshape(insets().left + 14,insets().top + 15,56,23);
        label2=new Label("Budget");
        add(label2);
        label2.reshape(insets().left + 14,insets().top + 52,70,23);
        label3=new Label("Time (minutes)");
        add(label3);
        label3.reshape(insets().left + 14,insets().top + 105,91,45);
        NameTextField=new TextField(29);
        add(NameTextField);
        NameTextField.reshape(insets().left + 84,insets().top + 8,301,30);
        BudgetTextField=new TextField(7);
        add(BudgetTextField);
        BudgetTextField.reshape(insets().left + 84,insets().top + 52,77,30);
        TimeTextField=new TextField(5);
        add(TimeTextField);
        TimeTextField.reshape(insets().left + 105,insets().top + 105,56,30);
        RestaurantList=new List();
        add(RestaurantList);
        RestaurantList.reshape(insets().left + 49,insets().top + 187,112,90);
        check0=new Checkbox("Fantastic!!!",group1, false);
        add(check0);
        check0.reshape(insets().left + 182,insets().top + 43,147,25);
        OkayButton=new Button("OK");
        add(OkayButton);
        OkayButton.reshape(insets().left + 350,insets().top + 173,35,30);
        check1=new Checkbox("Excellent",group1, false);
        add(check1);
        check1.reshape(insets().left + 182,insets().top + 68,147,24);
        check2=new Checkbox("Wonderful",group1, false);
        add(check2);
        check2.reshape(insets().left + 182,insets().top + 92,147,22);
        check3=new Checkbox("Delicious",group1, false);
        add(check3);
        check3.reshape(insets().left + 182,insets().top + 113,147,22);
        check4=new Checkbox("Yummy",group1, false);
        add(check4);
        check4.reshape(insets().left + 182,insets().top + 137,147,21);
        check5=new Checkbox("Savory",group1, false);
        add(check5);
        check5.reshape(insets().left + 182,insets().top + 159,145,23);
        check6=new Checkbox("Palatable",group1, false);
        add(check6);
        check6.reshape(insets().left + 182,insets().top + 180,145,23);
        check7=new Checkbox("So-so",group1, false);
        add(check7);
        check7.reshape(insets().left + 184,insets().top + 203,147,22);
        check8=new Checkbox("Putrid",group1, false);
        add(check8);
        check8.reshape(insets().left + 184,insets().top + 223,154,23);
        check9=new Checkbox("Vomitous",group1, false);
        add(check9);
        check9.reshape(insets().left + 184,insets().top + 246,154,22);
        label4=new Label("Restaurants:");
        add(label4);
        label4.reshape(insets().left + 47,insets().top + 155,124,25);
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
    	if (event.id == Event.ACTION_EVENT && event.target == RestaurantList) {
    	    	doubleClickRestaurantList();
    	    	return true;
    	}
    	if (event.id == Event.ACTION_EVENT && event.target == check9) {
    	    	clickedCheck9();
    	    	return true;
    	}
    	else
    	if (event.id == Event.ACTION_EVENT && event.target == check8) {
    	    	clickedCheck8();
    	    	return true;
    	}
    	else
    	if (event.id == Event.ACTION_EVENT && event.target == check7) {
    	    	clickedCheck7();
    	    	return true;
    	}
    	else
    	if (event.id == Event.ACTION_EVENT && event.target == check6) {
    	    	clickedCheck6();
    	    	return true;
    	}
    	else
    	if (event.id == Event.ACTION_EVENT && event.target == check4) {
    	    	clickedCheck4();
    	    	return true;
    	}
    	else
    	if (event.id == Event.ACTION_EVENT && event.target == check3) {
    	    	clickedCheck3();
    	    	return true;
    	}
    	else
    	if (event.id == Event.ACTION_EVENT && event.target == check2) {
    	    	clickedCheck2();
    	    	return true;
    	}
    	else
    	if (event.id == Event.ACTION_EVENT && event.target == check1) {
    	    	clickedCheck1();
    	    	return true;
    	}
    	else
    	if (event.id == Event.LOST_FOCUS && event.target == check0) {
    	    	lostFocusCheck0(event);
    	    	return true;
    	}
    	else
    	if (event.id == Event.GOT_FOCUS && event.target == check0) {
    	    	gotFocusCheck0(event);
    	    	return true;
    	}
    	else
    	if (event.id == Event.LIST_SELECT && event.target == RestaurantList) {
    	    	selChangeRestaurantList(event);
    	    	return true;
    	}
    	else
    	if (event.id == Event.ACTION_EVENT && event.target == check5) {
    	    	clickedCheck5();
    	    	return true;
    	}
    	else
    	if (event.id == Event.ACTION_EVENT && event.target == check0) {
    	    	clickedCheck0();
    	    	return true;
    	}
    	else
    	if (event.id == Event.ACTION_EVENT && event.target == OkayButton) {
    	    	clickedOkayButton();
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
    CheckboxGroup group1;
    Label label1;
    Label label2;
    Label label3;
    TextField NameTextField;
    TextField BudgetTextField;
    TextField TimeTextField;
    List RestaurantList;
    Checkbox check0;
    Button OkayButton;
    Checkbox check1;
    Checkbox check2;
    Checkbox check3;
    Checkbox check4;
    Checkbox check5;
    Checkbox check6;
    Checkbox check7;
    Checkbox check8;
    Checkbox check9;
    Label label4;
    //}}
    public void clickedOkayButton() {
        // test to see if Name, Budget, Time are all legal
        state.person.name = NameTextField.getText();
        Double d = Double.valueOf(BudgetTextField.getText());
        state.person.budget = d.doubleValue();
        Integer i = Integer.valueOf(TimeTextField.getText());
        state.person.minutes = i.intValue();
        handleEvent(new Event(this, Event.WINDOW_DESTROY, null));
        Common.savePerson(state.person);
        if (state.server != null) {
          state.server<-updateClientInfo(state.person.name, state.person);
        }
    }
    public void clickedCheck0() {
        String s = RestaurantList.getSelectedItem();
        if (s == null) return;
        // search through restaurants and find the match
        for (Enumeration e = state.person.tastes.elements(); e.hasMoreElements(); ) {
            Rating r = (Rating) e.nextElement();
            if (r.restaurant.name.equals(s)) {
                r.score = 0;
            }
        }
    check0.setState(true);
    }
    public void clickedCheck5() {
        String s = RestaurantList.getSelectedItem();
        if (s == null) return;
        // search through restaurants and find the match
        for (Enumeration e = state.person.tastes.elements(); e.hasMoreElements(); ) {
            Rating r = (Rating) e.nextElement();
            if (r.restaurant.name.equals(s)) {
                r.score = 5;
            }
        }
    check5.setState(true);
    }
    public void selChangeRestaurantList(Event ev) {
        // update appropriate check box based on value
        // of highlighted item in RestaurantList
        String s = RestaurantList.getSelectedItem();
        // search through restaurants and find the match
        for (Enumeration e = state.person.tastes.elements(); e.hasMoreElements(); ) {
            Rating r = (Rating) e.nextElement();
            if (r.restaurant.name.equals(s)) {
                int score = r.score;
                // update check box here
                switch(score) {
                    case 0: check0.setState(true); break;
                    case 1: check1.setState(true); break;
                    case 2: check2.setState(true); break;
                    case 3: check3.setState(true); break;
                    case 4: check4.setState(true); break;
                    case 5: check5.setState(true); break;
                    case 6: check6.setState(true); break;
                    case 7: check7.setState(true); break;
                    case 8: check8.setState(true); break;
                    case 9: check9.setState(true); break;
                }
            }
        }
    }
    public void gotFocusCheck0(Event ev) {
    }
    public void lostFocusCheck0(Event ev) {
    }
    public void clickedCheck1() {
        String s = RestaurantList.getSelectedItem();
        if (s == null) return;
        // search through restaurants and find the match
        for (Enumeration e = state.person.tastes.elements(); e.hasMoreElements(); ) {
            Rating r = (Rating) e.nextElement();
            if (r.restaurant.name.equals(s)) {
                r.score = 1;
            }
        }
    check1.setState(true);
    }
    public void clickedCheck2() {
        String s = RestaurantList.getSelectedItem();
        if (s == null) return;
        // search through restaurants and find the match
        for (Enumeration e = state.person.tastes.elements(); e.hasMoreElements(); ) {
            Rating r = (Rating) e.nextElement();
            if (r.restaurant.name.equals(s)) {
                r.score = 2;
            }
        }
    check2.setState(true);
    }
    public void clickedCheck3() {
        String s = RestaurantList.getSelectedItem();
        if (s == null) return;
        // search through restaurants and find the match
        for (Enumeration e = state.person.tastes.elements(); e.hasMoreElements(); ) {
            Rating r = (Rating) e.nextElement();
            if (r.restaurant.name.equals(s)) {
                r.score = 3;
            }
        }
    check3.setState(true);
    }
    public void clickedCheck4() {
        String s = RestaurantList.getSelectedItem();
        if (s == null) return;
        // search through restaurants and find the match
        for (Enumeration e = state.person.tastes.elements(); e.hasMoreElements(); ) {
            Rating r = (Rating) e.nextElement();
            if (r.restaurant.name.equals(s)) {
                r.score = 4;
            }
        }
    check4.setState(true);
    }
    public void clickedCheck6() {
        String s = RestaurantList.getSelectedItem();
        if (s == null) return;
        // search through restaurants and find the match
        for (Enumeration e = state.person.tastes.elements(); e.hasMoreElements(); ) {
            Rating r = (Rating) e.nextElement();
            if (r.restaurant.name.equals(s)) {
                r.score = 6;
            }
        }
    check6.setState(true);
    }
    public void clickedCheck7() {
        String s = RestaurantList.getSelectedItem();
        if (s == null) return;
        // search through restaurants and find the match
        for (Enumeration e = state.person.tastes.elements(); e.hasMoreElements(); ) {
            Rating r = (Rating) e.nextElement();
            if (r.restaurant.name.equals(s)) {
                r.score = 7;
            }
        }
    check7.setState(true);
    }
    public void clickedCheck8() {
        String s = RestaurantList.getSelectedItem();
        if (s == null) return;
        // search through restaurants and find the match
        for (Enumeration e = state.person.tastes.elements(); e.hasMoreElements(); ) {
            Rating r = (Rating) e.nextElement();
            if (r.restaurant.name.equals(s)) {
                r.score = 8;
            }
        }
    check8.setState(true);
    }
    public void clickedCheck9() {
        String s = RestaurantList.getSelectedItem();
        if (s == null) return;
        // search through restaurants and find the match
        for (Enumeration e = state.person.tastes.elements(); e.hasMoreElements(); ) {
            Rating r = (Rating) e.nextElement();
            if (r.restaurant.name.equals(s)) {
                r.score = 9;
            }
        }
    check9.setState(true);
    }

    public void doubleClickRestaurantList() {

      String s = RestaurantList.getSelectedItem();
      for (Enumeration e = state.person.tastes.elements(); 
        e.hasMoreElements(); ) {
        Rating r = (Rating) e.nextElement();
        if (r.restaurant.name.equals(s)) {
          RestaurantObserveDialog rod = new RestaurantObserveDialog(parent);
          rod.Init(r.restaurant);
          rod.show();
        }
      }
    }

}


