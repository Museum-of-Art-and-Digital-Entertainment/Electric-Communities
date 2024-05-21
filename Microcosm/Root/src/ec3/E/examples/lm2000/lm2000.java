
/*
 * The LunchMaster 2000
 *
 * LM2000 users (each with their own tastes in restaurants) notify the server 
 * when they are ready to go to lunch.  The server determines, based on the
 * list of users who want to go to lunch and their tastes, which are the best 
 * restaruants to choose from, and distributes this list to all the clients.
 *
 * Felix Baumgardner
 *
 */

package ec.examples.lm2000;

/*
    This class is an extension of the Frame class for use as the
    main window of an application.

    You can add controls or menus to lm2000 with Cafe Studio.

 */

import java.awt.*;
import java.util.*;
import ec.e.comm.*;

public class lm2000 extends Frame {

    EClient client;
    EServer server;
    Person person;

    public lm2000(EServer es) {

        super("lm2000 window");

        //{{INIT_MENUS
        MenuBar mb = new MenuBar();
        fileMenu = new Menu("&File");
        fileMenu.add(new MenuItem("E&xit"));
        mb.add(fileMenu);
        editMenu = new Menu("&Edit");
        editMenu.add(new MenuItem("&Profile"));
        editMenu.add(new MenuItem("Add &Restaurant"));
        editMenu.add(new MenuItem("&Delete Restaurant"));
        mb.add(editMenu);
        helpMenu = new Menu("&Help");
        helpMenu.add(new MenuItem("&About..."));
        mb.add(helpMenu);
        setMenuBar(mb);
        //}}

        //{{INIT_CONTROLS
        setLayout(null);
        addNotify();
        resize(insets().left + insets().right + 564, insets().top + 
          insets().bottom + 352);
        LunchCheckBox1=new Checkbox("I Wanna Go To Lunch!");
        add(LunchCheckBox1);
        LunchCheckBox1.reshape(insets().left + 8,insets().top + 0,208,24);
        BestRestaurantList=new List();
        add(BestRestaurantList);
        BestRestaurantList.reshape(insets().left + 209,insets().top + 
          65,142,278);
        LunchGoerList=new List();
        add(LunchGoerList);
        LunchGoerList.reshape(insets().left + 33,insets().top + 65,142,278);
        AllRestaurantList=new List();
        add(AllRestaurantList);
        AllRestaurantList.reshape(insets().left + 385,insets().top + 
          65,142,238);
        label1=new Label("Hungry folks");
        add(label1);
        label1.reshape(insets().left + 32,insets().top + 32,160,24);
        label2=new Label("Best-Fit Restaurants");
        add(label2);
        label2.reshape(insets().left + 208,insets().top + 32,160,24);
        label3=new Label("All Restaurants");
        add(label3);
        label3.reshape(insets().left + 384,insets().top + 32,160,24);
        //}}

        server = es;
        person = Common.loadPerson();
        client = new EClient(this);

        // make sure the user has chosen a name
        while (person.name.equals("Your Name")) {
          // Non-modal dialog hell. 
          PersonConfigDialog pcd;
          pcd = new PersonConfigDialog(this);
          pcd.Init(this);
          pcd.show();
          while (pcd.isVisible()) { 
            try { Thread.sleep(100); } 
            catch (InterruptedException e) {}
          }
        }
        server<-processNewClient(client, person);

        // Wait a moment to get the list of restaurants from the server.  
        try { Thread.sleep(1000); } 
        catch (InterruptedException e) {}

        show();

    }

    public synchronized void show() {
    	move(50, 50);
    	super.show();
    }

    public boolean handleEvent(Event event) {
    	if (event.id == Event.ACTION_EVENT && event.target == BestRestaurantList) {
    	    	doubleClickBestRestaurantList();
    	    	return true;
    	}
    	else 
    	if (event.id == Event.ACTION_EVENT && event.target == AllRestaurantList) {
    	    	doubleClickAllRestaurantList();
    	    	return true;
    	}
    	else 
    	if (event.id == Event.ACTION_EVENT && event.target == LunchCheckBox1) {
    	    	clickedLunchCheckBox1();
    	    	return true;
    	}
    	else

    	if (event.id == Event.WINDOW_DESTROY) {
            hide();         // hide the Frame
            dispose();      // tell windowing system to free resources
            System.exit(0); // exit
            return true;
    	}
    	return super.handleEvent(event);
    }

    public boolean action(Event event, Object arg) {
        if (event.target instanceof MenuItem) {
            String label = (String) arg;
            if (label.equalsIgnoreCase("&Delete Restaurant")) {
                            selectedDeleteRestaurant();
                            return true;
                        } else if (label.equalsIgnoreCase("Add &Restaurant")) {
                        selectedAddRestaurant();
                        return true;
                    } else if (label.equalsIgnoreCase("&Profile")) {
                    selectedProfile();
                    return true;
                } else if (label.equalsIgnoreCase("&About...")) {
                selectedAbout();
                return true;
            } else if (label.equalsIgnoreCase("E&xit")) {
                selectedExit();
                return true;
            } else if (label.equalsIgnoreCase("&Open...")) {
                selectedOpen();
                return true;
            }
        }
        return super.action(event, arg);
    }

    //{{DECLARE_MENUS
    Menu fileMenu;
    Menu editMenu;
    Menu helpMenu;
    //}}

    //{{DECLARE_CONTROLS
    Checkbox LunchCheckBox1;
    List BestRestaurantList;
    List LunchGoerList;
    List AllRestaurantList;
    Label label1;
    Label label2;
    Label label3;
    //}}

    public void selectedOpen() {
        (new FileDialog(this, "Open...")).show();
    }
    public void selectedExit() {
        QuitBox theQuitBox;
        theQuitBox = new QuitBox(this);
        theQuitBox.show();
    }
    public void selectedAbout() {
        AboutBox theAboutBox;
        theAboutBox = new AboutBox(this);
        theAboutBox.show();
    }
    public void selectedProfile() {
        PersonConfigDialog pcd;
        pcd = new PersonConfigDialog(this);
        pcd.Init(this);
        pcd.show();
    }
    public void selectedAddRestaurant() {
        RestaurantAddDialog ard;
        ard = new RestaurantAddDialog(this);
        ard.Init(this);
        ard.show();
    }
    public void selectedDeleteRestaurant() {
        RestaurantDeleteDialog drd;
        drd = new RestaurantDeleteDialog(this);
        drd.Init(this);
        drd.show();
    }
    public void clickedLunchCheckBox1() {
        if (LunchCheckBox1.getState()) {
            server<-processReadyForLunch(person.name);
        } else {
            server<-processNotReadyForLunch(person.name);
        }
    }

    public void doubleClickAllRestaurantList() {
      String s = AllRestaurantList.getSelectedItem();
      for (Enumeration e = person.tastes.elements();
        e.hasMoreElements(); ) {
        Rating r = (Rating) e.nextElement();
        if (r.restaurant.name.equals(s)) {
          RestaurantObserveDialog rod = new RestaurantObserveDialog(this);
          rod.Init(r.restaurant);
          rod.show();
        }
      }
    }

    public void doubleClickBestRestaurantList() {
      String s = BestRestaurantList.getSelectedItem();
      for (Enumeration e = person.tastes.elements();
        e.hasMoreElements(); ) {
        Rating r = (Rating) e.nextElement();
        if (r.restaurant.name.equals(s)) {
          RestaurantObserveDialog rod = new RestaurantObserveDialog(this);
          rod.Init(r.restaurant);
          rod.show();
        }
      }
    }

    public void shutdown(String reason) {
      System.err.println("LunchMaster 2000 shut down by server: " + reason);
      System.exit(0);
    }
}


/*
    This class is a basic extension of the Dialog class.  It can be used
    by subclasses of Frame.  To use it, create a reference to the class,
    then instantiate an object of the class (pass 'this' in the constructor),
    and call the show() method.

    example:

    AboutBox theAboutBox;
    theAboutBox = new AboutBox(this);
    theAboutBox.show();

    You can add controls to AboutBox with Cafe Studio.
    (Menus can be added only to subclasses of Frame.)
 */

class AboutBox extends Dialog {

    public AboutBox(Frame parent) {

	    super(parent, "About", true);
    	setResizable(false);

    	//{{INIT_CONTROLS
        setLayout(null);
        addNotify();
        resize(insets().left + insets().right + 382, insets().top + insets().bottom + 100);
        label1=new Label("The LunchMaster 2000");
        add(label1);
        label1.reshape(insets().left + 48,insets().top + 0,224,24);
        OKButton=new Button("OK");
        add(OKButton);
        OKButton.reshape(insets().left + 272,insets().top + 16,96,32);
        label2=new Label("Copyright 1996, Electric Communities");
        add(label2);
        label2.reshape(insets().left + 0,insets().top + 48,272,24);
        label3=new Label("by Felix Baumgardner");
        add(label3);
        label3.reshape(insets().left + 32,insets().top + 24,240,24);
        //}}
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
    Button OKButton;
    Label label2;
    Label label3;
    //}}

    public void clickedOKButton() {
        handleEvent(new Event(this, Event.WINDOW_DESTROY, null));
    }
}


/*
    This class is a basic extension of the Dialog class.  It can be used
    by subclasses of Frame.  To use it, create a reference to the class,
    then instantiate an object of the class (pass 'this' in the constructor),
    and call the show() method.

    example:

    QuitBox theQuitBox;
    theQuitBox = new QuitBox(this);
    theQuitBox.show();

    You can add controls, but not menus, to QuitBox with Cafe Studio.
    (Menus can be added only to subclasses of Frame.)
 */

class QuitBox extends Dialog {

    public QuitBox(Frame parent) {

	    super(parent, "Quit Application?", true);
    	setResizable(false);

    	//{{INIT_CONTROLS
        setLayout(null);
        addNotify();
        resize(insets().left + insets().right + 340, insets().top + insets().bottom + 78);
        yesButton=new Button("Yes");
        add(yesButton);
        yesButton.reshape(insets().left + 90,insets().top + 12,62,28);
        noButton=new Button("No");
        add(noButton);
        noButton.reshape(insets().left + 180,insets().top + 12,62,28);
        //}}
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
    	if (event.id == Event.ACTION_EVENT && event.target == noButton) {
    	    	clickedNoButton();
    	    	return true;
    	}
    	else
    	if (event.id == Event.ACTION_EVENT && event.target == yesButton) {
    	    	clickedYesButton();
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
    Button yesButton;
    Button noButton;
    //}}

    public void clickedYesButton() {
        System.exit(0);
    }
    public void clickedNoButton() {
        handleEvent(new Event(this, Event.WINDOW_DESTROY, null));
    }
}
