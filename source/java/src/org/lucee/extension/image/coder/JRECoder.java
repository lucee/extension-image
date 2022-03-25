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
package org.lucee.extension.image.coder;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.Imaging;
import org.lucee.extension.image.Image;
import org.lucee.extension.image.ImageUtil;
import org.lucee.extension.image.Img;
import org.lucee.extension.image.JAIUtil;
import org.lucee.extension.image.PSDReader;
import org.lucee.extension.image.jpg.JpegReader;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

class JRECoder extends Coder {

	private CFMLEngine enc;
	private ImageIOCoder imageIOCoder;

	protected JRECoder() {
		super();
		imageIOCoder = new ImageIOCoder();
	}

	/**
	 * translate a file resource to a buffered image
	 * 
	 * @param res
	 * @return
	 * @throws IOException
	 */
	@Override
	public final BufferedImage read(Resource res, String format, RefInteger jpegColorType) throws IOException {
		// System.out.println("JRE.read");
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if (eng.getStringUtil().isEmpty(format)) format = ImageUtil.getFormat(res);
		if ("psd".equalsIgnoreCase(format)) {
			PSDReader reader = new PSDReader();
			InputStream is = null;
			try {
				reader.read(is = res.getInputStream());
				BufferedImage bi = reader.getImage();
				if (bi != null) return bi;
			}
			finally {
				Util.closeEL(is);
			}
		}
		else if ("jpg".equalsIgnoreCase(format)) {
			JpegReader reader = new JpegReader();
			try {
				if (res instanceof File) {
					BufferedImage bi = reader.readImage((File) res, jpegColorType);
					if (bi != null) return bi;
				}
				else {
					Resource tmp = eng.getSystemUtil().getTempFile("jpg", false);
					eng.getIOUtil().copy(res, tmp);
					try {
						BufferedImage bi = reader.readImage((File) tmp, jpegColorType);
						if (bi != null) return bi;
					}
					finally {
						if (!tmp.delete()) ((File) tmp).deleteOnExit();
					}
				}

			}
			catch (Exception e) {
			}
		}

		try {
			BufferedImage bi = imageIOCoder.read(res, format, jpegColorType);
			if (bi != null) return bi;
		}
		catch (Exception e) {
		}

		try {
			BufferedImage bi = JAIUtil.read(res);
			if (bi != null) return bi;
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
		return null;
	}

	/**
	 * translate a binary array to a buffered image
	 * 
	 * @param binary
	 * @return
	 * @throws IOException
	 */
	@Override
	public final BufferedImage read(byte[] bytes, String format, RefInteger jpegColorType) throws IOException {
		// System.out.println("JRE.read");
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if (eng.getStringUtil().isEmpty(format)) format = ImageUtil.getFormat(bytes, null);
		if ("psd".equalsIgnoreCase(format)) {
			PSDReader reader = new PSDReader();
			reader.read(new ByteArrayInputStream(bytes));
			BufferedImage bi = reader.getImage();
			if (bi != null) return bi;
		}
		else if ("jpg".equalsIgnoreCase(format)) {
			JpegReader reader = new JpegReader();
			try {
				BufferedImage bi = reader.readImage(bytes, jpegColorType);
				if (bi != null) return bi;
			}
			catch (Exception e) {
				throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
			}
		}

		try {
			BufferedImage bi = imageIOCoder.read(bytes, format, jpegColorType);
			if (bi != null) return bi;
		}
		catch (Exception e) {
		}

		try {
			BufferedImage bi = JAIUtil.read(new ByteArrayInputStream(bytes), format.equalsIgnoreCase("jpg") ? "JPEG" : format);
			if (bi != null) return bi;
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
		return null;
	}

	@Override
	public void write(Image img, Resource destination, String format, float quality, boolean noMeta) throws IOException {
		// System.out.println("JRE.write");
		try {
			writeOut(img, destination, format, noMeta, quality, noMeta);
		}
		catch (Exception e) {

			try {
				imageIOCoder.write(img, destination, format, quality, noMeta);
			}
			catch (Exception ee) {
				throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
			}

		}
	}

	@Override
	public void write(Image img, OutputStream os, String format, float quality, boolean closeStream, boolean noMeta) throws IOException {
		// System.out.println("JRE.write");
		try {
			writeOut(img, os, format, quality, closeStream, noMeta);
		}
		catch (Exception e) {
			try {
				imageIOCoder.write(img, os, format, quality, closeStream, noMeta);
			}
			catch (Exception ee) {
				throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
			}
		}
	}

	@Override
	public final String[] getWriterFormatNames() {
		return imageIOCoder.getWriterFormatNames();
	}

	@Override
	public final String[] getReaderFormatNames() {
		return imageIOCoder.getReaderFormatNames();
	}

	public static final String[] mixTogetherOrderedx(String[] names1, String[] names2) {
		Set<String> set = new HashSet<String>();

		if (names1 != null) for (int i = 0; i < names1.length; i++) {
			set.add(names1[i].toLowerCase());
		}
		if (names2 != null) for (int i = 0; i < names2.length; i++) {
			set.add(names2[i].toLowerCase());
		}

		names1 = set.toArray(new String[set.size()]);
		Arrays.sort(names1);
		return names1;
	}

	@Override
	public boolean supported() {
		return true;
	}

	@Override
	public String getFormat(Resource res) throws IOException {
		return imageIOCoder.getFormat(res);
	}

	@Override
	public String getFormat(byte[] bytes) throws IOException {
		return imageIOCoder.getFormat(bytes);
	}

	@Override
	public String getFormat(Resource res, String defaultValue) {
		return imageIOCoder.getFormat(res, defaultValue);
	}

	@Override
	public String getFormat(byte[] bytes, String defaultValue) {
		return imageIOCoder.getFormat(bytes, defaultValue);
	}

	public void writeOut(Image img, OutputStream os, String format, float quality, boolean closeStream, boolean noMeta) throws IOException, PageException {

		ImageOutputStream ios = ImageIO.createImageOutputStream(os);
		try {
			_writeOut(img, ios, format, quality, noMeta);
		}
		finally {
			eng().getIOUtil().closeSilent(ios);
		}
	}

	public void writeOut(Image img2, Resource destination, final String format, boolean overwrite, float quality, boolean noMeta) throws IOException, PageException {
		PageException pe = null;
		// try to write with ImageIO
		OutputStream os = null;
		ImageOutputStream ios = null;
		try {
			os = destination.getOutputStream();
			ios = ImageIO.createImageOutputStream(os);
			_writeOut(img2, ios, format, quality, noMeta);
			return;
		}
		catch (IIOException iioe) {
			// TODO correct the bands in case a CMYK image is read in when creating the BufferedImage
			if (img2.jpegColorType != null && img2.jpegColorType.toInt() > 0 && (iioe.getMessage() + "").indexOf("Metadata components != number of destination bands") != -1) {
				ImageUtil.closeEL(ios);
				eng().getIOUtil().closeSilent(os);

				// as a workaround we convert first to a png and then we convert that png to jpeg
				File tmp = new File(Util.getTempDirectory(), "tmp-" + System.currentTimeMillis() + ".png");
				FileOutputStream fos = new FileOutputStream(tmp);
				try {
					writeOut(img2, fos, "png", 1f, true, noMeta);
					os = destination.getOutputStream();
					ios = ImageIO.createImageOutputStream(os);
					PageContext pc = CFMLEngineFactory.getInstance().getThreadPageContext();
					Image img = Image.createImage(pc, tmp, false, false, false, format);
					_writeOut(img, ios, format, quality, false);
					return;
				}
				catch (Exception e) {
					pe = CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
				}
				finally {
					if (!tmp.delete()) tmp.deleteOnExit();
				}
			}
		}
		catch (Exception e) {
			pe = CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
		finally {
			ImageUtil.closeEL(ios);
			eng().getIOUtil().closeSilent(os);
		}

		// try it with JAI
		try {
			BufferedImage bi = img2.getBufferedImage();
			if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) {
				bi = ensureOpaque(bi);
			}

			Img img = new Img(bi);
			byte[] barr = img.getByteArray(format.equalsIgnoreCase("jpg") ? "JPEG" : format);
			if (barr != null) {
				eng().getIOUtil().copy(new ByteArrayInputStream(barr), destination, true);
				return;
			}
			else {
				JAIUtil.write(bi, destination, format.equalsIgnoreCase("jpg") ? "JPEG" : format);
				return;
			}
		}
		catch (Exception e) {
			pe = CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}

		// let's give it a last try by converting first to a different format
		if (!ImageUtil.isBMP(format)) {
			try {
				final byte[] bytes = Imaging.writeImageToBytes(img2.getBufferedImage(), ImageFormat.IMAGE_FORMAT_BMP, new HashMap<>());
				Image _img = new Image(bytes, "bmp");
				os = destination.getOutputStream();
				ios = ImageIO.createImageOutputStream(os);
				_writeOut(_img, ios, format, quality, false);
				return;
			}
			catch (Exception e) {
			}
			finally {
				ImageUtil.closeEL(ios);
				eng().getIOUtil().closeSilent(os);
			}
		}
		if (pe != null) throw pe;
	}

	private void _writeOut(Image img, ImageOutputStream ios, final String format, float quality, boolean noMeta) throws IOException, PageException {
		boolean isJpeg = format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg");
		BufferedImage bi = img.getBufferedImage();

		// IIOMetadata meta = noMeta?null:metadata(format);
		IIOMetadata meta = noMeta ? null : img.getMetaData(null, format);

		ImageWriter writer = null;
		ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(bi);
		Iterator<ImageWriter> iter = ImageIO.getImageWriters(type, format);

		if (iter.hasNext()) {
			writer = iter.next();
		}
		if (writer == null) throw new IOException(
				"no writer for format [" + format + "] available, available writer formats are [" + eng().getListUtil().toList(ImageUtil.getWriterFormatNames(), ",") + "]");

		ImageWriteParam iwp = getImageWriteParam(bi, writer, quality, img.getFormat(), format);

		writer.setOutput(ios);

		if (isJpeg) {
			BufferedImage nbi = ensureOpaque(bi);
			if (nbi != bi) {
				bi = nbi;
				meta = null;
			}
		}

		try {
			writer.write(meta, new IIOImage(bi, null, meta), iwp);
		}
		finally {
			writer.dispose();
			ios.flush();
			img.setFormat(format);
		}
	}

	private static BufferedImage ensureOpaque(BufferedImage bi) {
		if (bi.getTransparency() == BufferedImage.OPAQUE) return bi;

		int w = bi.getWidth();
		int h = bi.getHeight();
		int[] pixels = new int[w * h];
		bi.getRGB(0, 0, w, h, pixels, 0, w);
		BufferedImage bi2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		bi2.setRGB(0, 0, w, h, pixels, 0, w);
		return bi2;
	}

	private static ImageWriteParam getImageWriteParam(BufferedImage im, ImageWriter writer, float quality, String srcFormat, String trgFormat) {
		ImageWriteParam iwp;
		if ("jpg".equalsIgnoreCase(srcFormat)) {
			ColorModel cm = im.getColorModel();
			if (cm.hasAlpha()) im = jpgImage(im);
			JPEGImageWriteParam jiwp = new JPEGImageWriteParam(Locale.getDefault());
			jiwp.setOptimizeHuffmanTables(true);
			iwp = jiwp;
		}
		else iwp = writer.getDefaultWriteParam();

		setCompressionModeEL(iwp, ImageWriteParam.MODE_EXPLICIT);
		setCompressionQualityEL(iwp, quality);

		return iwp;
	}

	private static void setCompressionModeEL(ImageWriteParam iwp, int mode) {
		try {
			iwp.setCompressionMode(mode);
		}
		catch (Exception e) {
		}
	}

	private static void setCompressionQualityEL(ImageWriteParam iwp, float quality) {
		try {
			iwp.setCompressionQuality(quality);
		}
		catch (Exception e) {
		}
	}

	private static BufferedImage jpgImage(BufferedImage src) {
		int w = src.getWidth();
		int h = src.getHeight();
		SampleModel srcSM = src.getSampleModel();
		WritableRaster srcWR = src.getRaster();
		java.awt.image.DataBuffer srcDB = srcWR.getDataBuffer();

		ColorModel rgb = new DirectColorModel(32, 0xff0000, 65280, 255);
		int[] bitMasks = new int[] { 0xff0000, 65280, 255 };

		SampleModel csm = new SinglePixelPackedSampleModel(3, w, h, bitMasks);
		int data[] = new int[w * h];
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				int pix[] = null;
				int sample[] = srcSM.getPixel(j, i, pix, srcDB);
				if (sample[3] == 0 && sample[2] == 0 && sample[1] == 0 && sample[0] == 0) data[i * w + j] = 0xffffff;
				else data[i * w + j] = sample[0] << 16 | sample[1] << 8 | sample[2];
			}

		}

		java.awt.image.DataBuffer db = new DataBufferInt(data, w * h * 3);
		WritableRaster wr = Raster.createWritableRaster(csm, db, new Point(0, 0));
		return new BufferedImage(rgb, wr, false, null);
	}

	private CFMLEngine eng() {
		if (enc == null) enc = CFMLEngineFactory.getInstance();
		return enc;
	}

}