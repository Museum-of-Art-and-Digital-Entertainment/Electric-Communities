package ec.ifc.app;

import netscape.application.*;

/**
 * A subclass of ListItem that draws an image proportionally scaled
 * to fit in the item. The scaled image is drawn on the right side
 * of the list item; it does not interfere with the image or
 * selected image that ListItem handles.
 *
 * @author         John Sullivan
 */
public class ECScaledImageListItem extends ListItem {
    private Image scaledImage;

    public Image getScaledImage()  {
        return (scaledImage);
    }

    public void setScaledImage(Image newImage)  {
        scaledImage = newImage;
    }

    //
    // drawing
    //
/**
 * Draws the scaled image on the right edge of the item, scaled to fit.
 */
  public void drawInRect(Graphics g, Rect boundsRect)  {
        super.drawInRect(g, boundsRect);

        if (scaledImage == null)
            return;

        int imageWidth = scaledImage.width();
        int imageHeight = scaledImage.height();

        // center the image in a square as tall as the given rectangle, at its right side, with at least
        // a one-pixel margin in both dimensions
        Rect imageRect = new Rect(boundsRect);
        imageRect.x += imageRect.width - imageRect.height;
        imageRect.width = imageRect.height;
        imageRect.growBy(-1, -1);
        Rect imageSquare = new Rect(imageRect);

        if (imageHeight > imageWidth) {
            imageRect.width = (imageRect.height * imageWidth)/imageHeight;
            imageRect.x = imageSquare.x + ((imageSquare.width - imageRect.width)/2);
        }
        else {
            imageRect.height = (imageRect.width * imageHeight)/imageWidth;
            imageRect.y = imageSquare.y + ((imageSquare.height - imageRect.height)/2);
        }

        scaledImage.drawScaled(g, imageRect.x, imageRect.y, imageRect.width, imageRect.height);
    }
                                                  
}

