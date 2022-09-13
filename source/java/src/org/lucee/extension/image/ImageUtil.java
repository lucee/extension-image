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
package org.lucee.extension.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.codec.binary.Base64;
import org.lucee.extension.image.coder.Coder;
import org.lucee.extension.image.format.FormatExtract;
import org.lucee.extension.image.format.FormatNames;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;

public class ImageUtil {

	private static Coder _coder;

	private static final boolean useSunCodec = getSunCodec();
	private static Class JPEGCodec;
	private static Class JPEGEncodeParam;

	private static int counter = 0;

	public static final int COLOR_TYPE_RGB = 1;
	public static final int COLOR_TYPE_CMYK = 2;
	public static final int COLOR_TYPE_YCCK = 3;

	private static Coder getCoder() {
		if (_coder == null) {
			_coder = Coder.getInstance();
		}
		return _coder;
	}

	public static String[] getWriterFormatNames() throws IOException {
		Coder c = getCoder();
		if (c instanceof FormatNames) return ((FormatNames) c).getWriterFormatNames();
		return new String[] {};
	}

	public static String[] getReaderFormatNames() throws IOException {
		Coder c = getCoder();
		if (c instanceof FormatNames) return ((FormatNames) c).getReaderFormatNames();
		return new String[] {};
	}

	/**
	 * translate a file resource to a buffered image
	 * 
	 * @param res
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage toBufferedImage(Resource res, String format, RefInteger jpegColorType) throws IOException {
		return getCoder().read(res, format, jpegColorType);
	}

	/**
	 * translate a binary array to a buffered image
	 * 
	 * @param binary
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage toBufferedImage(byte[] bytes, String format, RefInteger jpegColorType) throws IOException {
		return getCoder().read(bytes, format, jpegColorType);
	}

	public static void writeOut(Image img, Resource destination, String format, float quality, boolean noMeta) throws IOException {
		if (quality < 0 || quality > 1) throw new IOException("quality has an invalid value [" + quality + "], value has to be between 0 and 1");
		if (eng().getStringUtil().isEmpty(format)) format = img.getFormat();
		if (eng().getStringUtil().isEmpty(format)) throw new IOException("missing format");

		getCoder().write(img, destination, format, quality, noMeta);
	}

	public static void writeOut(Image img, OutputStream os, String format, float quality, boolean closeStream, boolean noMeta) throws IOException {
		if (quality < 0 || quality > 1) throw new IOException("quality has an invalid value [" + quality + "], value has to be between 0 and 1");
		if (eng().getStringUtil().isEmpty(format)) format = img.getFormat();
		if (eng().getStringUtil().isEmpty(format)) throw new IOException("missing format");

		Resource tmp = createTempFile(format);
		try {
			getCoder().write(img, tmp, format, quality, noMeta);
			eng().getIOUtil().copy(tmp.getInputStream(), os, true, closeStream);
		}
		finally {
			if (!tmp.delete() && tmp instanceof File) ((File) tmp).deleteOnExit();
		}
	}

	public static Resource createTempFile(String format) {
		return eng().getResourceUtil().getTempDirectory().getRealResource("tmp" + id() + "." + format);
	}

	public static byte[] readBase64(String b64str, StringBuilder mimetype) throws IOException {
		if (CFMLEngineFactory.getInstance().getStringUtil().isEmpty(b64str)) throw new IOException("base64 string is empty");

		// data:image/png;base64,
		int index = b64str.indexOf("base64,");
		if (index != -1) {
			int semiIndex = b64str.indexOf(";");
			if (mimetype != null && semiIndex < index && CFMLEngineFactory.getInstance().getStringUtil().startsWithIgnoreCase(b64str, "data:")) {
				mimetype.append(b64str.substring(5, semiIndex).trim());
			}

			b64str = b64str.substring(index + 7);
		}
		return Base64.decodeBase64(b64str.getBytes());
	}

	public static String getFormat(Resource res) throws IOException {
		long len = res.length();
		Coder c = getCoder();
		if (c instanceof FormatExtract) {
			String format = ((FormatExtract) c).getFormat(res, null);
			if (!Util.isEmpty(format, true)) return format;
		}
		// there is no need to check the mime type if the file is empty
		if (len > 0) {
			String mt = getMimeType(res, null);
			if (!Util.isEmpty(mt)) {
				String format = getImageFormatFromMimeType(mt, null);
				if (!Util.isEmpty(format)) return format;
			}
		}
		return getFormatFromExtension(res, null);
	}

	private static String getMimeType(Resource res, String defaultValue) {
		return CFMLEngineFactory.getInstance().getResourceUtil().getMimeType(res, null);
	}

	public static String getFormat(byte[] binary) throws IOException {
		Coder c = getCoder();
		if (c instanceof FormatExtract) {
			String format = ((FormatExtract) c).getFormat(binary, null);
			if (!Util.isEmpty(format, true)) return format;
		}
		return getFormatFromMimeType(CFMLEngineFactory.getInstance().getResourceUtil().getMimeType(binary, ""));
	}

	public static String getFormat(byte[] binary, String defaultValue) {
		Coder c = getCoder();
		if (c instanceof FormatExtract) {
			String format = ((FormatExtract) c).getFormat(binary, null);
			if (!Util.isEmpty(format, true)) return format;
		}
		return getImageFormatFromMimeType(CFMLEngineFactory.getInstance().getResourceUtil().getMimeType(binary, ""), defaultValue);
	}

	public static String getFormatFromExtension(Resource res, String defaultValue) {
		String ext = CFMLEngineFactory.getInstance().getResourceUtil().getExtension(res, null);
		if ("gif".equalsIgnoreCase(ext)) return "gif";
		if ("jpg".equalsIgnoreCase(ext)) return "jpg";
		if ("jpe".equalsIgnoreCase(ext)) return "jpg";
		if ("jpeg".equalsIgnoreCase(ext)) return "jpg";
		if ("icns".equalsIgnoreCase(ext)) return "icns";
		if ("ico".equalsIgnoreCase(ext)) return "ico";
		if ("png".equalsIgnoreCase(ext)) return "png";
		if ("tiff".equalsIgnoreCase(ext)) return "tiff";
		if ("tif".equalsIgnoreCase(ext)) return "tiff";
		if ("bmp".equalsIgnoreCase(ext)) return "bmp";
		if ("bmp".equalsIgnoreCase(ext)) return "bmp";
		if ("wbmp".equalsIgnoreCase(ext)) return "wbmp";
		if ("ico".equalsIgnoreCase(ext)) return "bmp";
		if ("wbmp".equalsIgnoreCase(ext)) return "wbmp";
		if ("psd".equalsIgnoreCase(ext)) return "psd";
		if ("fpx".equalsIgnoreCase(ext)) return "fpx";

		if ("pnm".equalsIgnoreCase(ext)) return "pnm";
		if ("pgm".equalsIgnoreCase(ext)) return "pgm";
		if ("pbm".equalsIgnoreCase(ext)) return "pbm";
		if ("ppm".equalsIgnoreCase(ext)) return "ppm";
		if ("heic".equalsIgnoreCase(ext)) return "heic";
		if ("heif".equalsIgnoreCase(ext)) return "heif";
		return defaultValue;
	}

	public static String getFormatFromMimeType(String mt) throws IOException {
		String format = getImageFormatFromMimeType(mt, null);
		if (format != null) return format;

		if (CFMLEngineFactory.getInstance().getStringUtil().isEmpty(mt)) throw new IOException("cannot find Format of given image");// 31
		throw new IOException("can't find Format for mime type [" + mt + "].");
	}

	public static String getImageFormatFromMimeType(String mt, String defaultValue) {
		if ("image/gif".equals(mt)) return "gif";
		if ("image/gi_".equals(mt)) return "gif";

		if ("image/jpeg".equals(mt)) return "jpg";
		if ("image/jpg".equals(mt)) return "jpg";
		if ("image/jpe".equals(mt)) return "jpg";
		if ("image/pjpeg".equals(mt)) return "jpg";
		if ("image/vnd.swiftview-jpeg".equals(mt)) return "jpg";
		if ("image/pipeg".equals(mt)) return "jpg";
		if ("application/x-jpg".equals(mt)) return "jpg";
		if ("application/jpg".equals(mt)) return "jpg";
		if ("image/jp_".equals(mt)) return "jpg";

		if ("image/png".equals(mt)) return "png";
		if ("image/x-png".equals(mt)) return "png";
		if ("application/x-png".equals(mt)) return "png";
		if ("application/png".equals(mt)) return "png";

		if ("image/tiff".equals(mt)) return "tiff";
		if ("image/tif".equals(mt)) return "tiff";
		if ("image/x-tif".equals(mt)) return "tiff";
		if ("image/x-tiff".equals(mt)) return "tiff";
		if ("application/tif".equals(mt)) return "tiff";
		if ("application/x-tif".equals(mt)) return "tiff";
		if ("application/tiff".equals(mt)) return "tiff";
		if ("application/x-tiff".equals(mt)) return "tiff";

		if ("image/bmp".equals(mt)) return "bmp";
		if ("image/vnd.wap.wbmp".equals(mt)) return "wbmp";

		if ("image/fpx".equals(mt)) return "fpx";
		if ("image/x-fpx".equals(mt)) return "fpx";
		if ("image/vnd.fpx".equals(mt)) return "fpx";
		if ("image/vnd.netfpx".equals(mt)) return "fpx";
		if ("image/vnd.fpx".equals(mt)) return "fpx";
		if ("application/vnd.netfpx".equals(mt)) return "fpx";
		if ("application/vnd.fpx".equals(mt)) return "fpx";

		if ("image/x-portable-anymap".equals(mt)) return "pnm";
		if ("image/x-portable/anymap".equals(mt)) return "pnm";
		if ("image/x-pnm".equals(mt)) return "pnm";
		if ("image/pnm".equals(mt)) return "pnm";

		if ("image/x-portable-graymap".equals(mt)) return "pgm";
		if ("image/x-portable/graymap".equals(mt)) return "pgm";
		if ("image/x-pgm".equals(mt)) return "pgm";
		if ("image/pgm".equals(mt)) return "pgm";

		if ("image/portable bitmap".equals(mt)) return "pbm";
		if ("image/x-portable-bitmap".equals(mt)) return "pbm";
		if ("image/x-portable/bitmap".equals(mt)) return "pbm";
		if ("image/x-pbm".equals(mt)) return "pbm";
		if ("image/pbm".equals(mt)) return "pbm";

		if ("image/x-portable-pixmap".equals(mt)) return "ppm";
		if ("application/ppm".equals(mt)) return "ppm";
		if ("application/x-ppm".equals(mt)) return "ppm";
		if ("image/x-p".equals(mt)) return "ppm";
		if ("image/x-ppm".equals(mt)) return "ppm";
		if ("image/ppm".equals(mt)) return "ppm";

		if ("image/ico".equals(mt)) return "ico";
		if ("image/x-icon".equals(mt)) return "ico";
		if ("image/vnd.microsoft.icon".equals(mt)) return "ico";
		if ("application/ico".equals(mt)) return "ico";
		if ("application/x-ico".equals(mt)) return "ico";
		if ("application/vnd.microsoft.icon".equals(mt)) return "ico";

		if ("image/photoshop".equals(mt)) return "psd";
		if ("image/x-photoshop".equals(mt)) return "psd";
		if ("image/psd".equals(mt)) return "psd";
		if ("application/photoshop".equals(mt)) return "psd";
		if ("application/psd".equals(mt)) return "psd";
		if ("image/vnd.adobe.photoshop".equals(mt)) return "psd";
		if ("application/vnd.adobe.photoshop".equals(mt)) return "psd";
		if ("zz-application/zz-winassoc-psd".equals(mt)) return "psd";

		if ("image/heif".equals(mt)) return "heif";
		if ("image/heic".equals(mt)) return "heic";
		if ("image/heif-sequence".equals(mt)) return "heif";
		if ("image/heic-sequence".equals(mt)) return "heic";
		if ("image/webp".equals(mt)) return "webp";

		return defaultValue;
	}

	public static String getMimeTypeFromFormat(String mt) throws IOException {
		if ("gif".equals(mt)) return "image/gif";
		if ("jpeg".equals(mt)) return "image/jpg";
		if ("jpg".equals(mt)) return "image/jpg";
		if ("jpe".equals(mt)) return "image/jpg";
		if ("png".equals(mt)) return "image/png";
		if ("tiff".equals(mt)) return "image/tiff";
		if ("tif".equals(mt)) return "image/tiff";
		if ("bmp".equals(mt)) return "image/bmp";
		if ("bmp".equals(mt)) return "image/bmp";
		if ("wbmp".equals(mt)) return "image/vnd.wap.wbmp";
		if ("fpx".equals(mt)) return "image/fpx";

		if ("pgm".equals(mt)) return "image/x-portable-graymap";
		if ("pnm".equals(mt)) return "image/x-portable-anymap";
		if ("pbm".equals(mt)) return "image/x-portable-bitmap";
		if ("ppm".equals(mt)) return "image/x-portable-pixmap";

		if ("ico".equals(mt)) return "image/ico";
		if ("psd".equals(mt)) return "image/psd";

		if (CFMLEngineFactory.getInstance().getStringUtil().isEmpty(mt)) throw new IOException("can't find Format of given image");// 31
		throw new IOException("can't find Format (" + mt + ") of given image");
	}

	public static void closeEL(ImageInputStream iis) {
		try {
			if (iis != null) iis.close();
		}
		catch (Exception e) {
		}

	}

	public static BufferedImage createBufferedImage(BufferedImage image, int columns, int rows) {
		ColorModel colormodel = image.getColorModel();
		BufferedImage newImage;
		if (colormodel instanceof IndexColorModel) {
			if (colormodel.getTransparency() != 1) newImage = new BufferedImage(columns, rows, 2);
			else newImage = new BufferedImage(columns, rows, 1);
		}
		else {
			newImage = new BufferedImage(colormodel, image.getRaster().createCompatibleWritableRaster(columns, rows), colormodel.isAlphaPremultiplied(), null);
		}
		return newImage;
	}

	public static BufferedImage createBufferedImage(BufferedImage image) {
		return createBufferedImage(image, image.getWidth(), image.getHeight());
	}

	public static boolean isJPEG(String format) {
		return "jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format);
	}

	public static boolean isBMP(String format) {
		return "bmp".equalsIgnoreCase(format);
	}

	private static CFMLEngine eng() {
		return CFMLEngineFactory.getInstance();
	}

	public static BufferedImage resize(BufferedImage input, int width, int height, boolean maintainRatio) {

		// long startTime = getStartTime();

		int outputWidth = width;
		int outputHeight = height;

		int w = input.getWidth();
		int h = input.getHeight();

		if (maintainRatio) {

			double ratio = 0;

			if (w > h) {
				ratio = (double) width / (double) w;
			}
			else {
				ratio = (double) height / (double) h;
			}

			double dw = w * ratio;
			double dh = h * ratio;

			outputWidth = (int) Math.round(dw);
			outputHeight = (int) Math.round(dh);

			if (outputWidth > w || outputHeight > h) {
				outputWidth = w;
				outputHeight = h;
			}
		}

		// Resize the image (create new buffered image)
		java.awt.Image outputImage = input.getScaledInstance(outputWidth, outputHeight, BufferedImage.SCALE_AREA_AVERAGING);
		BufferedImage bi = new BufferedImage(outputWidth, outputHeight, getImageType(input));
		Graphics2D g2d = bi.createGraphics();
		g2d.drawImage(outputImage, 0, 0, null);
		g2d.dispose();
		return bi;
	}

	private static int getImageType(BufferedImage bufferedImage) {
		int imageType = bufferedImage.getType();
		if (imageType <= 0 || imageType == 12) {
			imageType = BufferedImage.TYPE_INT_ARGB;
		}
		return imageType;
	}

	// **************************************************************************
	// ** getByteArray
	// **************************************************************************
	/** Returns the image as a byte array. */

	public static byte[] getByteArray(BufferedImage bi, String format, float quality) {
		byte[] rgb = null;

		format = format.toLowerCase();
		if (format.startsWith("image/")) {
			format = format.substring(format.indexOf("/") + 1);
		}

		try {
			if (isJPEG(format)) {
				rgb = getJPEGByteArray(bi, quality);
			}
			else {
				ByteArrayOutputStream bas = new ByteArrayOutputStream();
				ImageIO.write(bi, format.toLowerCase(), bas);
				rgb = bas.toByteArray();
			}
		}
		catch (Exception e) {
		}
		return rgb;
	}

	// **************************************************************************
	// ** getJPEGByteArray
	// **************************************************************************
	/** Returns a JPEG compressed byte array. */

	private static byte[] getJPEGByteArray(BufferedImage bufferedImage, float outputQuality) throws IOException {
		if (outputQuality >= 0f && outputQuality <= 1.2f) {
			ByteArrayOutputStream bas = new ByteArrayOutputStream();
			BufferedImage bi = bufferedImage;
			int t = bufferedImage.getTransparency();

			if (t == BufferedImage.TRANSLUCENT) {
				bi = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D biContext = bi.createGraphics();
				biContext.drawImage(bufferedImage, 0, 0, null);
			}

			// First we will try to compress the image using the com.sun.image.codec.jpeg
			// package. These classes are marked as deprecated in JDK 1.7 and several
			// users have reported problems with this method. Instead, we are
			// supposed to use the JPEGImageWriteParam class. However, I have not
			// been able to adequatly test the compression quality or find an
			// anology to the setHorizontalSubsampling and setVerticalSubsampling
			// methods. Therefore, we will attempt to compress the image using the
			// com.sun.image.codec.jpeg package. If the compression fails, we will
			// use the JPEGImageWriteParam.
			if (useSunCodec) {

				try {

					// For Java 1.7 users, we will try to invoke the Sun JPEG Codec using reflection
					Object encoder = JPEGCodec.getMethod("createJPEGEncoder", java.io.OutputStream.class).invoke(JPEGCodec, bas);
					Object params = JPEGCodec.getMethod("getDefaultJPEGEncodeParam", BufferedImage.class).invoke(JPEGCodec, bi);
					params.getClass().getMethod("setQuality", float.class, boolean.class).invoke(params, outputQuality, true);
					params.getClass().getMethod("setHorizontalSubsampling", int.class, int.class).invoke(params, 0, 2);
					params.getClass().getMethod("setVerticalSubsampling", int.class, int.class).invoke(params, 0, 2);

					// Here's the original compression code without reflection
					/*
					 * JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bas); JPEGEncodeParam params =
					 * JPEGCodec.getDefaultJPEGEncodeParam(bi); params.setQuality(outputQuality, true); //true
					 * params.setHorizontalSubsampling(0,2); params.setVerticalSubsampling(0,2);
					 * params.setMarkerData(...); encoder.encode(bi, params);
					 */

					encoder.getClass().getMethod("encode", BufferedImage.class, JPEGEncodeParam).invoke(encoder, bi, params);
				}
				catch (Exception e) {
					bas.reset();
				}
			}

			// If the com.sun.image.codec.jpeg package is not found or if the
			// compression failed, we will use the JPEGImageWriteParam class.
			if (bas.size() == 0) {

				if (outputQuality > 1f) outputQuality = 1f;

				ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
				JPEGImageWriteParam params = (JPEGImageWriteParam) writer.getDefaultWriteParam();
				params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				params.setCompressionQuality(outputQuality);
				writer.setOutput(ImageIO.createImageOutputStream(bas));
				writer.write(null, new IIOImage(bi, null, null), params);

			}

			bas.flush();
			return bas.toByteArray();
		}
		else {
			return getByteArray(bufferedImage, "jpeg", 1f);
		}
	}

	private static boolean getSunCodec() {
		try {
			JPEGCodec = Class.forName("com.sun.image.codec.jpeg.JPEGCodec");
			JPEGEncodeParam = Class.forName("com.sun.image.codec.jpeg.JPEGEncodeParam");
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	public static Object toColorType(Integer colorType, String defaultValue) {
		if (COLOR_TYPE_CMYK == colorType) return "CMYK";
		if (COLOR_TYPE_RGB == colorType) return "RGB";
		if (COLOR_TYPE_YCCK == colorType) return "YCCK";
		return defaultValue;
	}

	public static BufferedImage toABGR(BufferedImage src) {
		if (src.getType() == BufferedImage.TYPE_3BYTE_BGR) {
			BufferedImage bff = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			int h = src.getHeight();
			int w = src.getWidth();
			for (int y = 0; y < h; ++y) {
				for (int x = 0; x < w; ++x) {
					int argb = src.getRGB(x, y);
					bff.setRGB(x, y, argb & 0xFF000000); // same color alpha 100%
				}
			}
			return bff;
		}
		return src;
	}

	public static BufferedImage fromABGR2BGR(BufferedImage src) {
		// bufferedImage is your image.
		if (src.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
			int h = src.getHeight();
			int w = src.getWidth();
			for (int y = 0; y < h; ++y) {
				for (int x = 0; x < w; ++x) {
					int argb = src.getRGB(x, y);
					if ((argb & 0x00FFFFFF) == 0x00FFFFFF) { // if the pixel is transparent
						src.setRGB(x, y, 0xFFFFFFFF); // white color.
					}
				}
			}
		}
		return src;
	}

	final public static BufferedImage convertColorspace(BufferedImage image, int newType) {

		BufferedImage raw_image = image;
		image = new BufferedImage(raw_image.getWidth(), raw_image.getHeight(), newType);
		ColorConvertOp xformOp = new ColorConvertOp(null);
		xformOp.filter(raw_image, image);

		return image;
	}

	public static synchronized String id() {
		return (++counter) + "x" + System.currentTimeMillis();
	}
}