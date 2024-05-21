package ec.pl.examples.web;

import netscape.application.*;
import netscape.util.*;

class WebEvent extends Event {
	boolean state;
	Object data;
	
	public WebEvent (int type, boolean state) {
		setType(type);
		this.state = state;
	}

	public WebEvent (int type, Object data) {
		setType(type);
		this.data = data;
	}

	public int getType () {
		return type();
	}

	public void setState (boolean theState) {
		this.state = theState;
	}

	public boolean getState () {
		return this.state;
	}

    public void setData (Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }
}

