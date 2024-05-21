package ec.ui;

import netscape.application.*;

public class ECScrollBorder extends Border
{
	private static ECScrollBorder theScrollBorder = new ECScrollBorder();
	
	public static Border border () {
		return theScrollBorder;
	}
	
    private ECScrollBorder () {
    	super();
    }

    public void drawInRect (Graphics g, int x, int y, int width, int height) {
		g.pushState();

		// Note that the lines on the right side and bottom are optional depending on
		// whether there's a horizontal or vertical scroll bar. Unfortunately this poor
		// class doesn't have this information, so right now it's hardwired for vertical
		// scroll bar only (see comments below for bottomMargin and rightMargin methods).
		// We could address this by making two different classes, each with its static instance,
		// or by passing parameters to the constructor (which Gordie despises).
    	g.setColor(Color.gray);
    	g.drawLine(x, y, x + width - 1, y);
    	g.drawLine(x, y, x, y + height - 1);
    	g.setColor(Color.black);
    	g.drawLine(x + 1, y + 1, x + width - 2, y + 1);
    	g.drawLine(x + 1, y + height - 2, x + width - 2, y + height - 2);
    	g.drawLine(x + 1, y + 1, x + 1, y + height - 2);
    	g.drawLine(x + width - 2, y + 1, x + width - 2, y + height - 2);
    	g.setColor(Color.white);
    	g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
    	g.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
    	g.popState();
	}    	

	// If there's a horizontal scroll bar, bottomMargin should be 0, else it should be 2.
	// Note that setting it to 0 means there are two lines of unnecessary-because-clipped-out drawing above
    public int bottomMargin()	{ return 2; }
    public int leftMargin()		{ return 2; }
	// If there's a vertical scroll bar, rightMargin should be 0, else it should be 2.
	// Note that setting it to 0 means there are two lines of unnecessary-because-clipped-out drawing above
    public int rightMargin()	{ return 0; }
    public int topMargin()		{ return 2; }
}

