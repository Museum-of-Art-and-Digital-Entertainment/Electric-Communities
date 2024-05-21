package ec.misc.graphics;

// PPMLoader
// A quick and dirty ImageProducer for loading raw format PPM data from
// an input stream.
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.util.Vector;
import ec.misc.LineInputStream;

public class PPMLoader extends ImageLoader
{
	static private final int TRANSPARENT_VALUE = 0x00000000;
	static private final int OPAQUE_VALUE = ImageLoader.TRANSPARENT_MASK;
	static private final int TRANSPARENT_COLOR = OPAQUE_VALUE + 0x0000ff00;
	
	private void cleanup (boolean haveError, InputStream is) {
		error = haveError;
		if (error) data = null;
		try {
			is.close();
		} catch (Exception e) {
		}
	}
		
	// input
	// Read from a stream
	public void input (InputStream is) {
		LineInputStream lis = new LineInputStream(is);
		int val;
		int i, j, k;
		int tc = 0;
		int pc = 0;
		
		////System.out.println("PPMLoader input: reading from InputStream");
		
		// read header
		boolean raw;
		String magic = ppmgets(lis);
		if (magic.equals("P6"))
			raw = true;
		else if (magic.equals("P3"))
			raw = false;
		else {
			System.out.println("PPMLoader input: invalid magic string: " + magic);
			cleanup(true, is);
			return;
		}
		
		String size = ppmgets(lis);
		w = Integer.parseInt(size.substring(0,size.indexOf(' ')));
		h = Integer.parseInt(size.substring(size.indexOf(' ')+1));
		data = new int[h][w];
		String max = ppmgets(lis);
		if (Integer.parseInt(max) != 255) {
			System.out.println("PPMLoader input: Max value greater than 255");	
			cleanup(true, is);
			return;
		}
	
		////System.out.println("Width " + w + ", height " + h);
		
		// XXX - Check for transparant (and pick transparent) colors
		// and replace with 0xff000000
		try {
			if (raw) {
				////System.out.println("PPMLoader input: reading raw data");
				// read raw data
				byte brow[] = new byte[w*3];
				for(i = 0; i < h; i++) {
					if (lis.readdata(brow) != w*3) {
						cleanup(true, is);
						throw new IOException("PPMLoader input: error on read");
					}
					for (j = 0, k = 0; k < w*3; j++, k+=3) {
						int r=brow[k], g=brow[k+1], b=brow[k+2];
						r = r<0 ? r+256 : r;
						g = g<0 ? g+256 : g;
						b = b<0 ? b+256 : b;
						val = OPAQUE_VALUE + (r<<16) + (g<<8) + b;
						if (val == TRANSPARENT_COLOR) {
							tc++;
							val = TRANSPARENT_VALUE;
						}
						pc++;
						data[i][j] = val;
					}
				}
			}
			else {
				////System.out.println("PPMLoader input: reading ascii data");
				// read ascii data
				for (i = 0; i < h; i++) {
					for (j = 0; j < w; j++) {
						int r = Integer.parseInt(lis.getw());
						int g = Integer.parseInt(lis.getw());
						int b = Integer.parseInt(lis.getw());
						val = OPAQUE_VALUE + (r<<16) + (g<<8) + b;
						if (val == TRANSPARENT_COLOR) {
							tc++;
							val = TRANSPARENT_VALUE;
						}
						pc++;
						data[i][j] = val;
					}
				}
			}
			////System.out.println("PPMLoader input: converted " + tc + " transparent pixels out of total count " + pc);
			////System.out.println("PPMLoader input: read finished, closing input stream");
			is.close();
		}
		catch(IOException e) {
			cleanup(true, is);
			return;
		}
	}

	// ppmgets
	// Read one line from PPM format image data, ignoring comments
	String ppmgets(LineInputStream lis)
	{
		String line;
		do {
			try {
				line = lis.gets();
			} catch(IOException e) {
				return "";
			}
		} while(line.charAt(0) == '#');
		return line;
	}
}

