package ec.ifc.stonelook;

import netscape.application.*;

/**
 * A tan bezel that's part of the stone-texture look. This is used for
 * several of the other SL classes, and can be used by itself.
 */
public class SLTanBezelBorder extends Border {

	private static final int BORDER_WIDTH = 3;

	private int type = BezelBorder.LOWERED;

/**
 * Creates an SLTanBezelBorder of the specified type. The type must be either
 * BezelBorder.LOWERED or BezelBorder.RAISED.
 */
	public SLTanBezelBorder (int type) {
		if (type != BezelBorder.LOWERED && type != BezelBorder.RAISED)
			throw new IllegalArgumentException("invalid border type " + type);
		this.type = type;
	}

/**
 * Overridden to draw tan bezel.
 */
    public void drawInRect(Graphics g, int x, int y, int width, int height) {

		Color topLeftColor, bottomRightColor;

		switch (type) {
			case BezelBorder.LOWERED:
				topLeftColor = StoneLook.tanShadowColor;
				bottomRightColor = StoneLook.tanHighlightColor;
				break;
			default:
				topLeftColor = StoneLook.tanHighlightColor;
				bottomRightColor = StoneLook.tanShadowColor;
				break;
		}
		
		g.setColor(topLeftColor);
		for (int index = 0; index < BORDER_WIDTH; index += 1) {
			g.drawLine(x + index, y + index, x + width - index, y + index);
			g.drawLine(x + index, y + index, x + index, y + height - index);
		}

		g.setColor(bottomRightColor);
		for (int index = 0; index < BORDER_WIDTH; index += 1) {
			g.drawLine(x + width - index - 1, y + index, x + width - index - 1, y + height - index);
			g.drawLine(x + index, y + height - index - 1, x + width - index, y + height - index - 1);
		}
    }


    public int bottomMargin() {
    	return BORDER_WIDTH;
    }

    public int leftMargin() {
    	return BORDER_WIDTH;
    }

    public int rightMargin() {
    	return BORDER_WIDTH;
    }

    public int topMargin() {
    	return BORDER_WIDTH;
    }
}
