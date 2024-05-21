package ec.ui;

import netscape.application.*;

public class ECWindowBorder extends Border
{
	private static ECWindowBorder theWindowBorder = new ECWindowBorder();
	
	public static Border border () {
		return theWindowBorder;
	}
	
    private ECWindowBorder () {
    	super();
    }

	// Draws a 3 pixel border, gbw top/left, wbg bot/right
    public void drawInRect (Graphics g, int x, int y, int width, int height) {
		g.pushState();
    	g.setColor(Color.gray);
    	g.drawLine(x, y, x + width - 1, y);
    	g.drawLine(x + 2, y + height - 3, x + width - 3, y + height - 3);
    	g.drawLine(x, y, x, y + height - 1);
    	g.drawLine(x + width - 3, y + 2, x + width - 3, y + height - 3);
    	g.setColor(Color.black);
    	g.drawLine(x + 1, y + 1, x + width - 2, y + 1);
    	g.drawLine(x + 1, y + height - 2, x + width - 2, y + height - 2);
    	g.drawLine(x + 1, y + 1, x + 1, y + height - 2);
    	g.drawLine(x + width - 2, y + 1, x + width - 2, y + height - 2);
    	g.setColor(Color.white);
    	g.drawLine(x + 2, y + 2, x + width - 3, y + 2);
    	g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
    	g.drawLine(x + 2, y + 2, x + 2, y + height - 3);
    	g.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
    	g.popState();
	}    	

    public int bottomMargin()	{ return 3; }
    public int leftMargin()		{ return 3; }
    public int rightMargin()	{ return 3; }
    public int topMargin()		{ return 3; }
}

