
package ec.ifc.stonelook;

import ec.ifc.app.ECPageTurner;

import netscape.application.Bitmap;
import netscape.application.Graphics;


/**
 * SLPageTurner is a subclass of ECPageTurner that customizes the appearance.
 * It uses three different images to represent first page, last page, and
 * any other page.
 */
public class SLPageTurner extends ECPageTurner {
    //
    // instance variables
    //
    /** image to use when on first page */
    protected Bitmap myFirstPageImage;
    /** image to use when on last page */
    protected Bitmap myLastPageImage;
    /** image to use on any page other than first or last */
    protected Bitmap myMiddlePageImage;

    //
    // constructors
    //
        
    /** returns a new SLPageTurner that's sized to fit its images.
     * Normal use is to create an SLPageTurner with this constructor,
     * then call moveTo to move it to the upper right corner of the "page"
     * it's on.
     */
    public SLPageTurner() {
        super();
        
        myFirstPageImage = StoneLook.pageCornerFirstPageImage();
        myLastPageImage = StoneLook.pageCornerLastPageImage();
        myMiddlePageImage = StoneLook.pageCornerMiddlePageImage();
        
        // assume all image are same dimensions
        sizeTo(myFirstPageImage.width(), myFirstPageImage.height());
    }
    
    /** Overridden to draw one of three images based on pageState */
    public void drawView(Graphics g) {
        Bitmap bitmap;
        switch (myPageState) {
            case ON_FIRST_PAGE: bitmap = myFirstPageImage; break;
            case ON_LAST_PAGE: bitmap = myLastPageImage; break;
            case ON_MIDDLE_PAGE: bitmap = myMiddlePageImage; break;
            
            default:
                throw new IllegalArgumentException(
                    "unknown page state " + myPageState);
        }
        g.drawBitmapAt(bitmap, 0, 0);
    }   

    /** Overridden to hit-test differently, based on images */  
    public boolean inPageForwardArea(int x, int y) {
        return x > bounds.width/2;
    }
}

