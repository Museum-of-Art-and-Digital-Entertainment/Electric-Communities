package ec.ifc.app;

import netscape.application.*;

public class ECBevelBorder extends Border
{
    private static ECBevelBorder theBevelBorder = new ECBevelBorder();
    
    public static Border border () {
        return theBevelBorder;
    }
    
    private ECBevelBorder () {
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

    public int bottomMargin()   { return 2; }
    public int leftMargin()     { return 2; }
    public int rightMargin()    { return 2; }
    public int topMargin()      { return 2; }
}
   
