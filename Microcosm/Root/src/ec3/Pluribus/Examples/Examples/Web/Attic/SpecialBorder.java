package ec.plexamples.web;

import netscape_beta.application.*;

public class BevelBorder extends Border
{
	private static BevelBorder theBevelBorder = new BevelBorder();
	
	public static Border border () {
		return theBevelBorder;
	}
	
    private BevelBorder () {
    	super();
    }

	// Draws a 2 pixel border, gb top/left, bg bot/right
    public void drawInRect (Graphics g, int x, int y, int width, int height) {
		g.pushState();
    	g.setColor(Color.gray);
    	g.drawLine(x, y, x + width - 1, y);
    	g.drawLine(x + 1, y + height - 2, x + width - 2, y + height - 2);
    	g.drawLine(x, y, x, y + height - 1);
    	g.drawLine(x + width - 2, y + 1, x + width - 2, y + height - 2);
    	g.setColor(Color.black);
    	g.drawLine(x + 1, y + 1, x + width - 2, y + 1);
    	g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
    	g.drawLine(x + 1, y + 1, x + 1, y + height - 2);
    	g.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
    	g.popState();
	}    	

    public int bottomMargin()	{ return 2; }
    public int leftMargin()		{ return 2; }
    public int rightMargin()	{ return 2; }
    public int topMargin()		{ return 2; }
}
   
public class WindowBorder extends Border
{
	private static WindowBorder theWindowBorder = new WindowBorder();
	
	public static Border border () {
		return theWindowBorder;
	}
	
    private WindowBorder () {
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
   
public class ScrollBorder extends Border
{
	private static ScrollBorder theScrollBorder = new ScrollBorder();
	
	public static Border border () {
		return theScrollBorder;
	}
	
    private ScrollBorder () {
    	super();
    }

    public void drawInRect (Graphics g, int x, int y, int width, int height) {
		g.pushState();
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

    public int bottomMargin()	{ return 2; }
    public int leftMargin()		{ return 2; }
    public int rightMargin()	{ return 2; }
    public int topMargin()		{ return 2; }
}
   
public class BorderTest extends Application {   
    public static void main (String args[]) {
 		BorderTest test = new BorderTest();
 		test.run();
    }

	public void init () {
		ExternalWindow window = new ExternalWindow();
		ContainerView cv;
		Border border;
		
		Size size = window.windowSizeForContentSize(400, 400);
		window.setBounds(100, 100, size.width, size.height);
		window.rootView().setColor(Color.lightGray);

		cv = new ContainerView(0, 0, 400, 400);
		cv.setBackgroundColor(Color.lightGray);
		border = WindowBorder.border();
		cv.setBorder(border);
		window.addSubview(cv);

		cv = new ContainerView(10, 10, 380, 380);
		cv.setBackgroundColor(Color.lightGray);
		border = BevelBorder.border();
		cv.setBorder(border);
		//window.addSubview(cv);

		cv = new ContainerView(20, 20, 360, 360);
		cv.setBackgroundColor(Color.white);
		border = ScrollBorder.border();
		cv.setBorder(border);
		window.addSubview(cv);

		TextView tv = new TextView(22, 22, 356, 356);
		tv.setBackgroundColor(Color.white);
		tv.setString("Type some stuff here ...");
		window.addSubview(tv);

		window.show();
	}
}
