package ec.pl.examples.dieroll;

import netscape.application.*;
import netscape.util.*;
import ec.ifc.app.*;

class DieRollEvent extends ECEvent {
	int value;
	Object data;
	
	public DieRollEvent (int type, int value, Object data) {
		super(type);
		this.value = value;
		this.data = data;
	}

	public void setValue (int value) {
		this.value = value;
	}

	public int getValue () {
		return this.value;
	}
	
	public Object getData () {
		return this.data;
	}
}

