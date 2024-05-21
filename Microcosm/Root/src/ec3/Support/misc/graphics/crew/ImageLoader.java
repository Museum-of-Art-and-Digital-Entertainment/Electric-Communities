package ec.misc.graphics;

// ImageLoader
// This is an abstract class that implements some of the basic methods
// needed to create an Image object from image data in some format.
// Subclasses should override one (or both) of the input() methods
// to read image format data and store it into the data[][] array in
// 32-bit ARGB format. Other classes can then create an Image object
// from the data by doing something like..
//  ImageLoader l = new XXXloader();
//  l.input(somestream);
//  Image i = createImage(l);
import java.awt.image.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Vector;

public abstract class ImageLoader implements ImageProducer
{
    static public final int TRANSPARENT_MASK    = 0xff000000;
    static public final int RED_MASK            = 0x00ff0000;
    static public final int GREEN_MASK          = 0x0000ff00;
    static public final int BLUE_MASK           = 0x000000ff;
    
    int data[][];           // where the raw data should go.
                            // This array must be [height][width]
    int w, h;               // image size
    boolean error = false;  // did something go wrong reading?

    private Vector cons = new Vector(); // Consumers
    
    // Using a direct color model with 32 bits
    // (big endian) 8 bits alpha/red/green/blue per byte
    static private ColorModel cm = new DirectColorModel(32, RED_MASK, GREEN_MASK, BLUE_MASK, TRANSPARENT_MASK);

    // input
    // Read image data from the given stream
    abstract public void input (InputStream is);

    // produce
    // Does the actual work of sending data to the current list of
    // consumers.
    synchronized void produce ()
    {
        int i, j;
        int size = cons.size();
        Vector currentConsumers = new Vector(size);
                
        ////System.out.println("ImageLoader Produce: Asked to produce");
            
        if ((data == null) && (error == false)) {
            System.out.println("ImageLoader Produce: Asked to produce without any data");
            return;
        }
        
        //
        // We need to make our own copy of the Vector because the
        // calls inside the loop might remove elements from the 
        // consumer Vector which are expecting us to tell them
        // about this productions (obviously something is hosed!)
        for (i = 0; i < size; i++) {
            currentConsumers.addElement(cons.elementAt(i));
        }

        ////System.out.println("ImageLoader produce: has " + size + " consumers");
        ////System.out.println("ImageLoader produce: consumers Vector is " + currentConsumers);
        
        for (i = 0; i < size; i++) {
            ImageConsumer c = (ImageConsumer)currentConsumers.elementAt(i);
            if (error) {
                ////System.out.println("ImageLoader Produce: telling consumer " + c + " ImageComplete IMAGEERROR");
                c.imageComplete(ImageConsumer.IMAGEERROR);
                continue;
            }
            ////System.out.println("ImageLoader Produce: telling consumer dimensions " + w + ", " + h);
            c.setDimensions(w, h);
            c.setColorModel(cm);
            c.setHints(ImageConsumer.TOPDOWNLEFTRIGHT);
            for (j = 0; j < h; j++)
                c.setPixels(0, j, w, 1, cm, data[j], 0, w);
            ////System.out.println("ImageLoader Produce: telling consumer " + c + " ImageComplete STATICIMAGEDONE");
            c.imageComplete(ImageConsumer.STATICIMAGEDONE);
        }
        ////System.out.println("ImageLoader produce returning ");
    }

    public synchronized void startProduction(ImageConsumer ic)
    {
        ////System.out.println("ImageLoader startProduction: consumer " + ic);
        addConsumer(ic);
        produce();
    }

    public synchronized void addConsumer(ImageConsumer ic)
    {
        ////System.out.println("ImageLoader " + this + " addConsumer: consumer " + ic);
        cons.addElement(ic);
    }

    public synchronized boolean isConsumer(ImageConsumer ic)
    {
        return cons.contains(ic);
    }

    public synchronized void removeConsumer(ImageConsumer ic)
    {
        ////System.out.println("ImageLoader removeConsumer: consumer " + ic);
        cons.removeElement(ic);
    }

    public synchronized void requestTopDownLeftRightResend(ImageConsumer ic)
    {
        ////System.out.println("ImageLoader requestTopDownLeftRightResend: consumer " + ic);
        cons.addElement(ic);
        produce();
    }
}

