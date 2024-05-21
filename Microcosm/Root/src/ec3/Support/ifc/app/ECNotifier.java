
//
// ECNotifier
//
// Copyright 1997 Electric Communities.  All rights reserved.
// By Dimitry Nasledov
//
// This class, being subclass of Observable, is supposed to invoke update
// methods of registered observers.  

package ec.ifc.app;

import java.util.Observable;


public class ECNotifier extends Observable {
    /**
     * This method is supposed to be called when you want to invoke Observer's update method
     */
    public void notify(Object data) {
        setChanged();
        notifyObservers(data);
    }
}

