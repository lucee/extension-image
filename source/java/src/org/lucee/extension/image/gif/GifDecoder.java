/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package org.lucee.extension.image.gif;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class GifDecoder {

	/**
	 * File read status: No errors.
	 */
	public static final int STATUS_OK = 0;

	/**
	 * File read status: Error decoding file (may be partially decoded)
	 */
	public static final int STATUS_FORMAT_ERROR = 1;

	/**
	 * File read status: Unable to open source.
	 */
	public static final int STATUS_OPEN_ERROR = 2;

	protected BufferedInputStream in;
	protected final int status = 0;

	protected int width; // full image width
	protected int height; // full image height
	protected boolean gctFlag; // global color table used
	protected int gctSize; // size of global color table
	protected int loopCount = 1; // iterations; 0 = repeat forever

	protected int[] gct; // global color table
	protected int[] lct; // local color table
	protected int[] act; // active color table

	protected int bgIndex; // background color index
	protected int bgColor; // background color
	protected int lastBgColor; // previous bg color
	protected int pixelAspect; // pixel aspect ratio

	protected boolean lctFlag; // local color table flag
	protected boolean interlace; // interlace flag
	protected int lctSize; // local color table size

	protected int ix, iy, iw, ih; // current image rectangle
	protected Rectangle lastRect; // last image rect
	protected BufferedImage image; // current frame
	protected BufferedImage lastImage; // previous frame

	protected byte[] block = new byte[256]; // current data block
	protected int blockSize = 0; // block size

	// last graphic control extension info
	protected int dispose = 0;
	// 0=no action; 1=leave in place; 2=restore to bg; 3=restore to prev
	protected int lastDispose = 0;
	protected boolean transparency = false; // use transparent color
	protected int delay = 0; // delay in milliseconds
	protected int transIndex; // transparent color index

	protected static final int MaxStackSize = 4096;
	// max decoder pixel stack size

	// LZW decoder working arrays
	protected short[] prefix;
	protected byte[] suffix;
	protected byte[] pixelStack;
	protected byte[] pixels;

	protected ArrayList frames; // frames read from current file
	protected int frameCount;

	static class GifFrame {
		public GifFrame(BufferedImage im, int del) {
			image = im;
			delay = del;
		}

		public BufferedImage image;
		public int delay;
	}

	/**
	 * Gets display duration for specified frame.
	 *
	 * @param n int index of frame
	 * @return delay in milliseconds
	 */
	public int getDelay(int n) {
		//
		delay = -1;
		if ((n >= 0) && (n < frameCount)) {
			delay = ((GifFrame) frames.get(n)).delay;
		}
		return delay;
	}

	/**
	 * Gets the number of frames read from file.
	 * 
	 * @return frame count
	 */
	public int getFrameCount() {
		return frameCount;
	}

	/**
	 * Gets the first (or only) image read.
	 *
	 * @return BufferedImage containing first frame, or null if none.
	 */
	private BufferedImage getImage() {
		return getFrame(0);
	}

	/**
	 * Gets the "Netscape" iteration count, if any. A count of 0 means repeat indefinitiely.
	 *
	 * @return iteration count if one was specified, else 1.
	 */
	public int getLoopCount() {
		return loopCount;
	}

	/**
	 * Creates new frame image from current data (and previous frames as specified by their disposition
	 * codes).
	 */
	protected void setPixels() {
		// expose destination image's pixels as int array
		int[] dest = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		// fill in starting image contents based on last image's dispose code
		if (lastDispose > 0) {
			if (lastDispose == 3) {
				// use image before last
				int n = frameCount - 2;
				if (n > 0) {
					lastImage = getFrame(n - 1);
				}
				else {
					lastImage = null;
				}
			}

			if (lastImage != null) {
				int[] prev = ((DataBufferInt) lastImage.getRaster().getDataBuffer()).getData();
				System.arraycopy(prev, 0, dest, 0, width * height);
				// copy pixels

				if (lastDispose == 2) {
					// fill last image rect area with background color
					Graphics2D g = image.createGraphics();
					Color c = null;
					if (transparency) {
						c = new Color(0, 0, 0, 0); // assume background is transparent
					}
					else {
						c = new Color(lastBgColor); // use given background color
					}
					g.setColor(c);
					g.setComposite(AlphaComposite.Src); // replace area
					g.fill(lastRect);
					g.dispose();
				}
			}
		}

		// copy each source line to the appropriate place in the destination
		int pass = 1;
		int inc = 8;
		int iline = 0;
		for (int i = 0; i < ih; i++) {
			int line = i;
			if (interlace) {
				if (iline >= ih) {
					pass++;
					switch (pass) {
					case 2:
						iline = 4;
						break;
					case 3:
						iline = 2;
						inc = 4;
						break;
					case 4:
						iline = 1;
						inc = 2;
					}
				}
				line = iline;
				iline += inc;
			}
			line += iy;
			if (line < height) {
				int k = line * width;
				int dx = k + ix; // start of line in dest
				int dlim = dx + iw; // end of dest line
				if ((k + width) < dlim) {
					dlim = k + width; // past dest edge
				}
				int sx = i * iw; // start of line in source
				while (dx < dlim) {
					// map color and insert in destination
					int index = pixels[sx++] & 0xff;
					int c = act[index];
					if (c != 0) {
						dest[dx] = c;
					}
					dx++;
				}
			}
		}
	}

	/**
	 * Gets the image contents of frame n.
	 *
	 * @return BufferedImage representation of frame, or null if n is invalid.
	 */
	public BufferedImage getFrame(int n) {
		BufferedImage im = null;
		if ((n >= 0) && (n < frameCount)) {
			im = ((GifFrame) frames.get(n)).image;
		}
		return im;
	}

	/**
	 * Gets image size.
	 *
	 * @return GIF image dimensions
	 */
	public Dimension getFrameSize() {
		return new Dimension(width, height);
	}

	/**
	 * Reads GIF image from stream
	 *
	 * @param BufferedInputStream containing GIF file.
	 * @return
	 * @return read status code (0 = no errors)
	 * @throws IOException
	 */
	public BufferedImage read(BufferedInputStream is) throws IOException {
		init();

		in = is;
		readHeader();
		readContents();
		if (frameCount < 0) {
			throw new IOException("invalid frame count");
		}

		try {
			is.close();
		}
		catch (IOException e) {
		}
		return getImage();
	}

	/**
	 * Reads GIF image from stream
	 *
	 * @param InputStream containing GIF file.
	 * @return read status code (0 = no errors)
	 * @throws IOException
	 */
	public BufferedImage read(InputStream is) throws IOException {
		init();
		if (!(is instanceof BufferedInputStream)) is = new BufferedInputStream(is);
		in = (BufferedInputStream) is;
		readHeader();
		readContents();
		if (frameCount < 0) {
			throw new IOException("invalid frame count");
		}
		try {
			is.close();
		}
		catch (IOException e) {
		}
		return getImage();
	}

	/**
	 * Decodes LZW image data into pixel array. Adapted from John Cristy's ImageMagick.
	 * 
	 * @throws IOException
	 */
	protected void decodeImageData() throws IOException {
		int NullCode = -1;
		int npix = iw * ih;
		int available, clear, code_mask, code_size, end_of_information, in_code, old_code, bits, code, count, i, datum, data_size, first, top, bi, pi;

		if ((pixels == null) || (pixels.length < npix)) {
			pixels = new byte[npix]; // allocate new pixel array
		}
		if (prefix == null) prefix = new short[MaxStackSize];
		if (suffix == null) suffix = new byte[MaxStackSize];
		if (pixelStack == null) pixelStack = new byte[MaxStackSize + 1];

		// Initialize GIF data stream decoder.

		data_size = read();
		clear = 1 << data_size;
		end_of_information = clear + 1;
		available = clear + 2;
		old_code = NullCode;
		code_size = data_size + 1;
		code_mask = (1 << code_size) - 1;
		for (code = 0; code < clear; code++) {
			prefix[code] = 0;
			suffix[code] = (byte) code;
		}

		// Decode GIF pixel stream.

		datum = bits = count = first = top = pi = bi = 0;

		for (i = 0; i < npix;) {
			if (top == 0) {
				if (bits < code_size) {
					// Load bytes until there are enough bits for a code.
					if (count == 0) {
						// Read a new data block.
						count = readBlock();
						if (count <= 0) break;
						bi = 0;
					}
					datum += (block[bi] & 0xff) << bits;
					bits += 8;
					bi++;
					count--;
					continue;
				}

				// Get the next code.

				code = datum & code_mask;
				datum >>= code_size;
				bits -= code_size;

				// Interpret the code

				if ((code > available) || (code == end_of_information)) break;
				if (code == clear) {
					// Reset decoder.
					code_size = data_size + 1;
					code_mask = (1 << code_size) - 1;
					available = clear + 2;
					old_code = NullCode;
					continue;
				}
				if (old_code == NullCode) {
					pixelStack[top++] = suffix[code];
					old_code = code;
					first = code;
					continue;
				}
				in_code = code;
				if (code == available) {
					pixelStack[top++] = (byte) first;
					code = old_code;
				}
				while (code > clear) {
					pixelStack[top++] = suffix[code];
					code = prefix[code];
				}
				first = suffix[code] & 0xff;

				// Add a new string to the string table,

				if (available >= MaxStackSize) break;
				pixelStack[top++] = (byte) first;
				prefix[available] = (short) old_code;
				suffix[available] = (byte) first;
				available++;
				if (((available & code_mask) == 0) && (available < MaxStackSize)) {
					code_size++;
					code_mask += available;
				}
				old_code = in_code;
			}

			// Pop a pixel off the pixel stack.

			top--;
			pixels[pi++] = pixelStack[top];
			i++;
		}

		for (i = pi; i < npix; i++) {
			pixels[i] = 0; // clear missing pixels
		}

	}

	/**
	 * Initializes or re-initializes reader
	 */
	protected void init() {
		frameCount = 0;
		frames = new ArrayList();
		gct = null;
		lct = null;
	}

	/**
	 * Reads a single byte from the input stream.
	 * 
	 * @throws IOException
	 */
	protected int read() throws IOException {
		return in.read();
	}

	/**
	 * Reads next variable length block from input.
	 *
	 * @return number of bytes stored in "buffer"
	 * @throws IOException
	 */
	protected int readBlock() throws IOException {
		blockSize = read();
		int n = 0;
		if (blockSize > 0) {
			int count = 0;
			while (n < blockSize) {
				count = in.read(block, n, blockSize - n);
				if (count == -1) break;
				n += count;
			}

			if (n < blockSize) {
				throw new IOException("invalid block size");
			}
		}
		return n;
	}

	/**
	 * Reads color table as 256 RGB integer values
	 *
	 * @param ncolors int number of colors to read
	 * @return int array containing 256 colors (packed ARGB with full alpha)
	 * @throws IOException
	 */
	protected int[] readColorTable(int ncolors) throws IOException {
		int nbytes = 3 * ncolors;
		int[] tab = null;
		byte[] c = new byte[nbytes];
		int n = in.read(c);
		if (n < nbytes) {

			throw new IOException("failed to read color table");
		}
		else {
			tab = new int[256]; // max size to avoid bounds checks
			int i = 0;
			int j = 0;
			while (i < ncolors) {
				int r = c[j++] & 0xff;
				int g = c[j++] & 0xff;
				int b = c[j++] & 0xff;
				tab[i++] = 0xff000000 | (r << 16) | (g << 8) | b;
			}
		}
		return tab;
	}

	/**
	 * Main file parser. Reads GIF content blocks.
	 * 
	 * @throws IOException
	 */
	protected void readContents() throws IOException {
		// read GIF file content blocks
		boolean done = false;
		while (!done) {
			int code = read();
			switch (code) {

			case 0x2C: // image separator
				readImage();
				break;

			case 0x21: // extension
				code = read();
				switch (code) {
				case 0xf9: // graphics control extension
					readGraphicControlExt();
					break;

				case 0xff: // application extension
					readBlock();
					String app = "";
					for (int i = 0; i < 11; i++) {
						app += (char) block[i];
					}
					if (app.equals("NETSCAPE2.0")) {
						readNetscapeExt();
					}
					else skip(); // don't care
					break;

				default: // uninteresting extension
					skip();
				}
				break;

			case 0x3b: // terminator
				done = true;
				break;

			case 0x00: // bad byte, but keep going and see what happens
				break;

			default:

				throw new IOException("failed to read content");
			}
		}
	}

	/**
	 * Reads Graphics Control Extension values
	 * 
	 * @throws IOException
	 */
	protected void readGraphicControlExt() throws IOException {
		read(); // block size
		int packed = read(); // packed fields
		dispose = (packed & 0x1c) >> 2; // disposal method
		if (dispose == 0) {
			dispose = 1; // elect to keep old image if discretionary
		}
		transparency = (packed & 1) != 0;
		delay = readShort() * 10; // delay in milliseconds
		transIndex = read(); // transparent color index
		read(); // block terminator
	}

	/**
	 * Reads GIF file header information.
	 * 
	 * @throws IOException
	 */
	protected void readHeader() throws IOException {
		String id = "";
		for (int i = 0; i < 6; i++) {
			id += (char) read();
		}
		if (!id.startsWith("GIF")) {
			throw new IOException("failed to read header, status format error");
		}

		readLSD();
		if (gctFlag) {
			gct = readColorTable(gctSize);
			bgColor = gct[bgIndex];
		}
	}

	/**
	 * Reads next frame image
	 * 
	 * @throws IOException
	 */
	protected void readImage() throws IOException {
		ix = readShort(); // (sub)image position & size
		iy = readShort();
		iw = readShort();
		ih = readShort();

		int packed = read();
		lctFlag = (packed & 0x80) != 0; // 1 - local color table flag
		interlace = (packed & 0x40) != 0; // 2 - interlace flag
		// 3 - sort flag
		// 4-5 - reserved
		lctSize = 2 << (packed & 7); // 6-8 - local color table size

		if (lctFlag) {
			lct = readColorTable(lctSize); // read table
			act = lct; // make local table active
		}
		else {
			act = gct; // make global table active
			if (bgIndex == transIndex) bgColor = 0;
		}
		int save = 0;
		if (transparency) {
			save = act[transIndex];
			act[transIndex] = 0; // set transparent color if specified
		}

		if (act == null) {
			throw new IOException("no color table defined");
		}

		decodeImageData(); // decode pixel data
		skip();

		frameCount++;

		// create new image to receive frame data
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);

		setPixels(); // transfer pixel data to image

		frames.add(new GifFrame(image, delay)); // add image to frame list

		if (transparency) {
			act[transIndex] = save;
		}
		resetFrame();

	}

	/**
	 * Reads Logical Screen Descriptor
	 * 
	 * @throws IOException
	 */
	protected void readLSD() throws IOException {

		// logical screen size
		width = readShort();
		height = readShort();

		// packed fields
		int packed = read();
		gctFlag = (packed & 0x80) != 0; // 1 : global color table flag
		// 2-4 : color resolution
		// 5 : gct sort flag
		gctSize = 2 << (packed & 7); // 6-8 : gct size

		bgIndex = read(); // background color index
		pixelAspect = read(); // pixel aspect ratio
	}

	/**
	 * Reads Netscape extenstion to obtain iteration count
	 * 
	 * @throws IOException
	 */
	protected void readNetscapeExt() throws IOException {
		do {
			readBlock();
			if (block[0] == 1) {
				// loop count sub-block
				int b1 = block[1] & 0xff;
				int b2 = block[2] & 0xff;
				loopCount = (b2 << 8) | b1;
			}
		}
		while (blockSize > 0);
	}

	/**
	 * Reads next 16-bit value, LSB first
	 * 
	 * @throws IOException
	 */
	protected int readShort() throws IOException {
		// read 16-bit value, LSB first
		return read() | (read() << 8);
	}

	/**
	 * Resets frame state for reading next image.
	 */
	protected void resetFrame() {
		lastDispose = dispose;
		lastRect = new Rectangle(ix, iy, iw, ih);
		lastImage = image;
		lastBgColor = bgColor;
		// int dispose = 0;
		// boolean transparency = false;
		// int delay = 0;
		lct = null;
	}

	/**
	 * Skips variable length blocks up to and including next zero length block.
	 * 
	 * @throws IOException
	 */
	protected void skip() throws IOException {
		do {
			readBlock();
		}
		while (blockSize > 0);
	}

}