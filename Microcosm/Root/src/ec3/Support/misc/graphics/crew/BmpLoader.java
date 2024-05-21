/**
 * BmpLoader.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * Tony Grant
 * October 6 1997
 *
 * An ImageProducer for loading BMP files from an input stream.
 * 
 * Supports:
 *   8-bit uncompressed
 *   8-bit RLE compressed
 *  24-bit uncompressed (the only kind of 24-bit BMP)
 *
 *  Currently burns some temporary memory for RLE compressed images only.
 *  It uses a full size image buffer for decoding, and then copies into the main buffer. 
 *  It should straight into data[] which has already been allocated
 */

package ec.misc.graphics;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.util.Vector;
import ec.misc.LittleEndianInputStream;
import ec.e.file.EStdio;

/**
 * Class that loads a BMP image from an input stream.
 */
public class BmpLoader extends ImageLoader
{

    static private final int TRANSPARENT_VALUE = 0x00000000;
    static private final int OPAQUE_VALUE = ImageLoader.TRANSPARENT_MASK;
    static private final int TRANSPARENT_COLOR = OPAQUE_VALUE + 0x0000ff00;

    static private final int COMPRESS_NONE = 0;
    static private final int COMPRESS_RLE8 = 1;
    
    private int MSN (int Value)  {
        return Value;
    }
    
    private int LSN (int Value)  {
        return Value;
    }   
    
    private void cleanup (boolean haveError, InputStream is) {
        error = haveError;
        if (error) data = null;
        try {
            is.close();
        } catch (Exception e) {
        }
    }
        
    /**
     * Read BMP image from a stream
     */
    public void input (InputStream is) {
        LittleEndianInputStream lis = new   LittleEndianInputStream(is);
        
        int val;
        int i, j, k;
        int type;
        int numberOfPadBytes;
        int tempByte;
        int red,green,blue;
        int decodedBuffer[] = null;
        
        // Read BMP header
        try {
            // Bitmap file header (Win32: BITMAPFILEHEADER)
            short headerType       = lis.readShort();
            int headerSize         = lis.readInt();
            short headerReserved1  = lis.readShort();
            short headerReserved2  = lis.readShort();
            int headerOffBits      = lis.readInt();

            // Bitmap info (Win32: BITMAPINFO)
            int infoSize           = lis.readInt();
            int infoWidth          = lis.readInt();
            int infoHeight         = lis.readInt();
            short infoPlanes       = lis.readShort();
            short infoBitCount     = lis.readShort();
            int infoCompression    = lis.readInt();
            int infoSizeImage      = lis.readInt();
            int infoXPelsPerMeter  = lis.readInt();
            int infoYPelsPerMeter  = lis.readInt();
            int infoClrUsed        = lis.readInt();
            int infoClrImportant   = lis.readInt();

            // Verify that this is a BMP image
            if (headerType != 19778) {    // "BM"
                System.out.println("BmpLoader input: Not a valid BMP file. headerType = " + headerType);
                cleanup(true, is);
                return;
            }           

            // debug info
            /*          
            System.out.println("\n\nHeader");
            System.out.println("======");           
            System.out.println("headerType           :" + headerType);
            System.out.println("headerSize           :" + headerSize);
            System.out.println("headerOffBits        :" + headerOffBits);
            System.out.println();
            System.out.println("Info");
            System.out.println("====");         
            System.out.println("infoSize             :" + infoSize);
            System.out.println("infoWidth            :" + infoWidth);
            System.out.println("infoHeight           :" + infoHeight);
            System.out.println("infoPlanes           :" + infoPlanes);
            System.out.println("infoBitCount         :" + infoBitCount);
            System.out.println("infoCompression      :" + infoCompression);
            System.out.println("infoSizeImage        :" + infoSizeImage);
            System.out.println("infoClrUsed          :" + infoClrUsed);
            System.out.println("infoClrImportant     :" + infoClrImportant);
            */

            
            // Set ImageLoader data
            w = infoWidth;
            h = infoHeight;
            data = new int[h][w];

            // BMP files are padded to a multiple of 4 pixels for each row
            // Find out how many pad bytes there are
            numberOfPadBytes = infoWidth %4;

            if (infoBitCount == 8) {
                // 8-bit image
                // Create an array to store the color palette
                int rgbPalette[][];

                // The number of palette entries is determined by infoClrUsed
                // If infoClrUsed is 0, then the palette uses all 256 colors 
                int numPaletteEntries;
                if (infoClrUsed == 0)  {
                    numPaletteEntries = 256;
                } else  {
                    numPaletteEntries = infoClrUsed;
                }
                
                // Set the dimensions of the palette array
                rgbPalette = new int[numPaletteEntries][3];
                
                // read the palette
                for (i=0;i<numPaletteEntries;i++) {
                    // BMP files are BGR, not RGB.
                    // Read the colors in backwards... (2,1,0)
                    rgbPalette[i][2] = lis.readUnsignedByte();
                    rgbPalette[i][1] = lis.readUnsignedByte();
                    rgbPalette[i][0] = lis.readUnsignedByte();
                    // BMPs have 4 bytes per palette entry. Ignore the last                                 
                    tempByte = lis.readUnsignedByte();
                }               

                // There may be more stuff in this header that we don't care about.
                // Jump to the offset supplied by the image header, and get the image data
                // Skip (offset - (54+palette size)                             
                
                // Is data compressed?
                if (infoCompression == COMPRESS_NONE) {
                    // Process uncompressed data
                    for (j=0;j<infoHeight;j++) {
                        for (i=0;i<infoWidth;i++) {
                            // read in a byte
                            tempByte = lis.readUnsignedByte();

                            // get the RGB components from the palette
                            red   = rgbPalette[tempByte][0];
                            green = rgbPalette[tempByte][1];
                            blue  = rgbPalette[tempByte][2];                                                
                            
                            // build a pixel                            
                            val = OPAQUE_VALUE + (red<<16) + (green<<8) + blue;

                            if (val == TRANSPARENT_COLOR) {
                                val = TRANSPARENT_VALUE;
                            }
                            data[h-j-1][i] = val;
                        }
                        for (i=0;i<numberOfPadBytes;i++ ) {
                            tempByte = lis.readUnsignedByte();
                        }
                    }
                } else if (infoCompression == COMPRESS_RLE8)  {
                    // RLE8 compression
                    
                    int bufIndex, bufferSize;
                    int runCount, runValue;
                    int Value;
                    int numScanLines = 0;


                    bufferSize = w*h;
                    bufIndex = 0;

                    //DecodedBuffer[] = new int[bufferSize];    
                    decodedBuffer = new int[w*h];   
                    
                    while (bufIndex < bufferSize) {
                        runCount = lis.readUnsignedByte();  // Number of pixels in the run
                        runValue = lis.readUnsignedByte();  // Value of pixels in the run

                        switch(runCount) {
                            case 0:                         // Literal Run or Escape Code
                                switch(runValue) {
                                    case 0:                 // End of Scan Line Escape Code
                                        //System.out.println("End of scan line Code "+ numScanLines++);
                                        break;
                                    case 1:                 // End of Bitmap Escape Code
                                        System.out.println("End of bit map Code");
                                        cleanup(true, is);
                                        return;
                                    case 2:                 // Delta Escape Code (not supported)
                                        System.out.println("Delta Escape Codes not supported!");
                                        cleanup(true, is);
                                        return;
                                    default:                // Literal Run
                                        // Check for a possible buffer overflow 
                                        if ((bufIndex + runValue) > bufferSize)  {
                                            System.out.println("Buffer overflow!");
                                            cleanup(true, is);
                                            return;
                                        }
                                        while ((runValue--) !=0) {
                                            decodedBuffer[bufIndex++] = lis.readUnsignedByte();
                                        }
                                }
                                break;
                            default:    // Encoded Run
                                // Check for a possible buffer overflow
                                if (bufIndex + runCount > bufferSize)  {
                                    System.out.println("Buffer overflow in encoded run (RLE-8).");
                                    cleanup(true, is);
                                    return;
                                }
                                while ((runCount--) !=0) {
                                    decodedBuffer[bufIndex++] = runValue;
                                }
                                break;
                            }
                    }

                } else {
                    // Unsupported compression type
                    System.out.println("BmpLoader input: Compression type not supported: " + infoCompression);
                    cleanup(true, is);
                    return;
                }

                // If this is an RLE compressed image,
                // copy from decodedBuffer to data[][] using the palette
                // XXX This should be done in place above, so we don't need
                // the decodedBuffer anymore
                
                if (infoCompression == COMPRESS_RLE8)  {
                    int paletteIndex;

                    for (j=0;j<infoHeight;j++) {
                        for (i=0;i<infoWidth;i++) {

                            if (infoCompression == 0)  {
                                paletteIndex = data[j][i];
                            } else {
                                // flip the image vertically
                                paletteIndex = decodedBuffer[(h-j-1)*w+i];
                            }
                            red   = rgbPalette[paletteIndex][0];
                            green = rgbPalette[paletteIndex][1];
                            blue  = rgbPalette[paletteIndex][2];                                                
                            
                            val = OPAQUE_VALUE + (red<<16) + (green<<8) + blue;

                            if (val == TRANSPARENT_COLOR) {
                                val = TRANSPARENT_VALUE;
                            }

                            data[j][i] = val;
                        }
                    }
                }
                // end of 8-bit image building...
                
            } else if (infoBitCount == 24) {
                // 24-bit BMP
                for (j=0;j<infoHeight;j++) {
                    for (i=0;i<infoWidth;i++) {

                        // BMPs are ordered Blue, Green, Red
                        blue =  lis.readUnsignedByte();
                        green = lis.readUnsignedByte();
                        red =   lis.readUnsignedByte();

                        val = OPAQUE_VALUE + (red<<16) + (green<<8) + blue;

                        if (val == TRANSPARENT_COLOR) {
                            val = TRANSPARENT_VALUE;
                        }
                        // BMP files are stored upside-down, so use (height-1) - j
                        data[h-j-1][i] = val;
                    }
                    // Slurp up the pad bytes
                    for (i=0;i<(numberOfPadBytes) ;i++ ) {
                        blue = lis.readUnsignedByte();
                    }
                }
            } else { 
                // Unsupported bit-depth
                System.out.println("BmpLoader input: Sorry, only 8-bit and 24-bit BMP files are supported.");
                cleanup(true, is);
                return;
            }
            is.close();
        }
        catch(IOException e) {
            System.out.println("Woof. IOException.");
            e.printStackTrace(EStdio.err());
            cleanup(true, is);
            return;
        }
    }
}

