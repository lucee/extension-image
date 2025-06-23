/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package org.lucee.extension.image;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.IndexColorModel;
import java.awt.image.Kernel;
import java.awt.image.PackedColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.swing.ImageIcon;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.GenericImageMetadata.GenericImageMetadataItem;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegPhotoshopMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.imgscalr.Scalr;
import org.lucee.extension.image.font.FontUtil;
import org.lucee.extension.image.functions.ImageGetEXIFMetadata;
import org.lucee.extension.image.util.ArrayUtil;
import org.lucee.extension.image.util.CommonUtil;
import org.lucee.extension.image.util.CommonUtil.Coll;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTime;

public class Image extends StructSupport implements Cloneable, Struct {
	private static final long serialVersionUID = -2370381932689749657L;
	public static final short TYPE_IMAGE = 25; // copy from core

	public static final int INTERPOLATION_NONE = 0;
	public static final int INTERPOLATION_NEAREST = 1;
	public static final int INTERPOLATION_BILINEAR = 2;
	public static final int INTERPOLATION_BICUBIC = 3;

	public static final int IP_NONE = 0;

	public static final int IPC_NEAREST = 1;
	public static final int IPC_BILINEAR = 2;
	public static final int IPC_BICUBIC = 3;
	public static final int IPC_MAX = 3;

	public static final int IP_AUTOMATIC = 1000;
	public static final int IP_HIGHESTQUALITY = 100;
	public static final int IP_HIGHQUALITY = 101;
	public static final int IP_MEDIUMQUALITY = 102;
	public static final int IP_HIGHESTPERFORMANCE = 103;
	public static final int IP_HIGHPERFORMANCE = 104;
	public static final int IP_MEDIUMPERFORMANCE = 105;

	public static final int IP_BESSEL = 109;
	public static final int IP_BLACKMAN = 110;
	public static final int IP_HAMMING = 111;
	public static final int IP_HANNING = 112;
	public static final int IP_HERMITE = 113;
	public static final int IP_LANCZOS = 114;
	public static final int IP_MITCHELL = 115;
	public static final int IP_QUADRATIC = 116;
	public static final int IP_TRIANGLE = 117;

	private static final int ANTI_ALIAS_NONE = 0;
	private static final int ANTI_ALIAS_ON = 1;
	private static final int ANTI_ALIAS_OFF = 2;

	public static final int BORDER_ZERO = 1;
	public static final int BORDER_CONSTANT = 2;
	public static final int BORDER_COPY = 4;
	public static final int BORDER_REFLECT = 8;
	public static final int BORDER_WRAP = 16;

	public static final int TRANSPOSE_VERTICAL = 1;
	public static final int TRANSPOSE_HORIZONTAL = 2;
	public static final int TRANSPOSE_DIAGONAL = 3;
	public static final int TRANSPOSE_ANTIDIAGONAL = 4;
	public static final int TRANSPOSE_ROTATE_90 = 5;
	public static final int TRANSPOSE_ROTATE_180 = 6;
	public static final int TRANSPOSE_ROTATE_270 = 7;

	private static final String FORMAT = "javax_imageio_1.0";
	public static final int SHEAR_HORIZONTAL = 1;
	public static final int SHEAR_VERTICAL = 2;

	private BufferedImage _image;
	private Resource source = null;
	private String format;

	private Graphics2D graphics;

	private Color bgColor;
	private Color fgColor;
	private Color xmColor;

	private float tranparency = -1;
	private int antiAlias = ANTI_ALIAS_NONE;

	private Stroke stroke;

	private float alpha = 1;

	private Composite composite;
	private int orientation = Metadata.ORIENTATION_UNDEFINED;

	private static CFMLEngine _eng;
	private static Object sync = new Object();
	private final boolean fromNew;
	private Struct sctInfo;

	static {
		ImageIO.scanForPlugins();
	}

	public Image(byte[] binary) throws IOException, ImageReadException, PageException {
		this(binary, null);
	}

	public Image(byte[] binary, String format) throws IOException, ImageReadException, PageException {
		if (eng().getStringUtil().isEmpty(format))
			format = ImageUtil.getFormat(binary, null);
		this.format = format;
		_image = ImageUtil.toBufferedImage(binary, format);
		if (_image == null)
			throw new IOException("Unable to read binary image file");

		checkOrientation(binary);
		fromNew = false;
	}

	public Image(Resource res) throws IOException, ImageReadException, PageException {
		this(res, null);
	}

	public Image(Resource res, String format) throws IOException, ImageReadException, PageException {
		if (eng().getStringUtil().isEmpty(format))
			format = ImageUtil.getFormat(res);
		this.format = format;
		_image = ImageUtil.toBufferedImage(res, format);
		this.source = res;
		if (_image == null)
			throw new IOException("Unable to read image file [" + res + "]");

		checkOrientation(res);
		fromNew = false;

	}

	public Image(BufferedImage image) {
		this._image = image;
		this.format = null;
		fromNew = false;
	}

	public static Image getInstance(PageContext pc, String str, String format)
			throws IOException, ImageReadException, PageException {

		if (str.length() < 4000) {
			if (pc == null)
				pc = CFMLEngineFactory.getInstance().getThreadPageContext();
			Resource res = eng().getResourceUtil().toResourceNotExisting(pc, str);
			if (res.isFile()) {
				Config c = (pc == null) ? CFMLEngineFactory.getInstance().getThreadConfig() : pc.getConfig();
				c.getSecurityManager().checkFileLocation(res);
				return new Image(res, format);
			}
		}
		return new Image(str, format);

	}

	private Image(String b64str, String format) throws IOException, ImageReadException, PageException {

		// load binary from base64 string and get format
		StringBuilder mimetype = new StringBuilder();
		byte[] binary = ImageUtil.readBase64(b64str, mimetype);
		if (eng().getStringUtil().isEmpty(format) && !eng().getStringUtil().isEmpty(mimetype.toString())) {
			format = ImageUtil.getFormatFromMimeType(mimetype.toString());
		}
		if (eng().getStringUtil().isEmpty(format))
			format = ImageUtil.getFormat(binary, null);
		this.format = format;
		_image = ImageUtil.toBufferedImage(binary, format);
		if (_image == null)
			throw new IOException("Unable to decode image from base64 string");

		checkOrientation(binary);
		fromNew = false;
	}

	public Image(int width, int height, int imageType, Color canvasColor) throws PageException {
		_image = new BufferedImage(width, height, imageType);
		if (canvasColor != null) {

			setBackground(canvasColor);
			clearRect(0, 0, width, height);
		}
		this.format = null;
		fromNew = true;
	}

	public Image() {
		this.format = null;
		fromNew = true;

	}

	public void addBorder(int thickness, Color color, int borderType) throws PageException {
		BufferedImage src = image();
		int width = src.getWidth();
		int height = src.getHeight();
		int newWidth = width + 2 * thickness;
		int newHeight = height + 2 * thickness;

		// Create a new image with extra space for the border
		BufferedImage borderedImage = new BufferedImage(newWidth, newHeight, src.getType());
		Graphics2D g2d = borderedImage.createGraphics();

		// Sets the border to the specified color (default).
		if (borderType == BORDER_CONSTANT) {
			g2d.setColor(color);
			g2d.fillRect(0, 0, newWidth, newHeight);
		}
		// Sets sample values to copies of the nearest valid pixel. For example, pixels
		// to the left of the
		// valid rectangle assume the value of the valid edge pixel in the same row.
		// Pixels both above and
		// to the left of the valid rectangle assume the value of the upper-left pixel.
		else if (borderType == BORDER_COPY) {
			// Draw main image first
			g2d.drawImage(src, thickness, thickness, null);

			// Top edge - repeat top row pixels
			g2d.drawImage(src, thickness, 0, width + thickness, thickness, // dest
					0, 0, width, 1, // source - just first row
					null);

			// Bottom edge - repeat bottom row pixels
			g2d.drawImage(src, thickness, height + thickness, width + thickness, newHeight, // dest
					0, height - 1, width, height, // source - just last row
					null);

			// Left edge - repeat leftmost column pixels
			g2d.drawImage(src, 0, thickness, thickness, height + thickness, // dest
					0, 0, 1, height, // source - just first column
					null);

			// Right edge - repeat rightmost column pixels
			g2d.drawImage(src, width + thickness, thickness, newWidth, height + thickness, // dest
					width - 1, 0, width, height, // source - just last column
					null);

			// Corners - fill with corner pixel color from source image
			// Top-left corner
			Color topLeft = new Color(src.getRGB(0, 0));
			g2d.setColor(topLeft);
			g2d.fillRect(0, 0, thickness, thickness);

			// Top-right corner
			Color topRight = new Color(src.getRGB(width - 1, 0));
			g2d.setColor(topRight);
			g2d.fillRect(width + thickness, 0, thickness, thickness);

			// Bottom-left corner
			Color bottomLeft = new Color(src.getRGB(0, height - 1));
			g2d.setColor(bottomLeft);
			g2d.fillRect(0, height + thickness, thickness, thickness);

			// Bottom-right corner
			Color bottomRight = new Color(src.getRGB(width - 1, height - 1));
			g2d.setColor(bottomRight);
			g2d.fillRect(width + thickness, height + thickness, thickness, thickness);
		}
		// Mirrors the edges of the source image. For example, if the left edge of the
		// valid rectangle is
		// located at x = 10, pixel (9, y) is a copy of pixel (10, y) and pixel (6, y)
		// is a copy of pixel
		// (13, y).
		else if (borderType == BORDER_REFLECT) {
			// Top and bottom edges first (excluding corners)
			g2d.drawImage(src, thickness, 0, width + thickness, thickness, // dest
					0, thickness, width, 0, // source - flip top vertically
					null);
			g2d.drawImage(src, thickness, height + thickness, width + thickness, newHeight, // dest
					0, height, width, height - thickness, // source - flip bottom vertically
					null);

			// Left and right edges (excluding corners)
			g2d.drawImage(src, 0, thickness, thickness, height + thickness, // dest
					thickness, 0, 0, height, // source - flip left horizontally
					null);
			g2d.drawImage(src, width + thickness, thickness, newWidth, height + thickness, // dest
					width, 0, width - thickness, height, // source - flip right horizontally
					null);

			// Draw the main image
			g2d.drawImage(src, thickness, thickness, null);

			// Top corners - flip horizontally only
			g2d.drawImage(borderedImage, 0, 0, thickness, thickness, // dest
					thickness * 2, 0, thickness, thickness, // source - flip horizontally from top edge
					null);
			g2d.drawImage(borderedImage, width + thickness, 0, newWidth, thickness, // dest
					width + thickness, 0, width + thickness - thickness, thickness, // source - flip horizontally from
																					// top edge
					null);

			// Bottom corners - mirror from bottom edge and flip horizontally
			g2d.drawImage(borderedImage, 0, height + thickness, thickness, newHeight, // dest
					thickness * 2, height + thickness, thickness, height + thickness * 2, // source - flip from bottom
																							// horizontally
					null);
			g2d.drawImage(borderedImage, width + thickness, height + thickness, newWidth, newHeight, // dest
					width + thickness, height + thickness, width + thickness - thickness, height + thickness * 2, // source
					null);
		}
		// Tiles the source image in the plane.
		else if (borderType == BORDER_WRAP) {
			// Draw corners first
			// Top-left corner
			g2d.drawImage(src, 0, 0, thickness, thickness, // dest
					width - thickness, height - thickness, width, height, // source
					null);
			// Top-right corner
			g2d.drawImage(src, width + thickness, 0, newWidth, thickness, // dest
					0, height - thickness, thickness, height, // source
					null);
			// Bottom-left corner
			g2d.drawImage(src, 0, height + thickness, thickness, newHeight, // dest
					width - thickness, 0, width, thickness, // source
					null);
			// Bottom-right corner
			g2d.drawImage(src, width + thickness, height + thickness, newWidth, newHeight, // dest
					0, 0, thickness, thickness, // source
					null);

			// Top and bottom edges
			g2d.drawImage(src, thickness, 0, width + thickness, thickness, // dest
					0, height - thickness, width, height, // source
					null);
			g2d.drawImage(src, thickness, height + thickness, width + thickness, newHeight, // dest
					0, 0, width, thickness, // source
					null);

			// Left and right edges
			g2d.drawImage(src, 0, thickness, thickness, height + thickness, // dest
					width - thickness, 0, width, height, // source
					null);
			g2d.drawImage(src, width + thickness, thickness, newWidth, height + thickness, // dest
					0, 0, thickness, height, // source
					null);
		}
		// Sets the border color to black.
		else if (borderType == BORDER_ZERO) {
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, newWidth, newHeight);
		} else {
			throw eng().getExceptionUtil().createApplicationException(
					"invalid border type definition, valid types are [copy,reflect,wrap,zero,constant]");
		}

		// Draw the original image onto the new image, centered
		g2d.drawImage(src, thickness, thickness, null);
		g2d.dispose();

		image(borderedImage);
	}

	public void blur(int blurFactor) throws PageException {
		// Validate and adjust blur factor
		int safeFactor = Math.min(Math.max(blurFactor, 3), 10);
		if (safeFactor != blurFactor) {
			// Optionally warn about adjustment
			System.out.println("Blur factor adjusted to " + safeFactor + " (valid range: 3-10)");
		}

		BufferedImage source = image();
		BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());

		// Create kernel
		float[] kernel = new float[safeFactor * safeFactor];
		float value = 1.0f / (safeFactor * safeFactor);
		Arrays.fill(kernel, value);

		ConvolveOp op = new ConvolveOp(new Kernel(safeFactor, safeFactor, kernel), ConvolveOp.EDGE_NO_OP, null);

		op.filter(source, result);
		image(result);
	}

	public void clearRect(int x, int y, int width, int height) throws PageException {
		getGraphics().clearRect(x, y, width, height);
	}

	public Struct info() throws PageException {
		// if (sctInfo != null) return sctInfo;

		Struct sctInfo = eng().getCreationUtil().createStruct(), sct;
		ImageMetaDrew.addInfo(format, source, sctInfo);
		sctInfo = ImageGetEXIFMetadata.flatten(sctInfo);
		sctInfo.setEL("height", Double.valueOf(getHeight()));
		sctInfo.setEL("width", Double.valueOf(getWidth()));
		sctInfo.setEL("source", source == null ? "" : source.getAbsolutePath());

		if (ImageUtil.isJPEG(getFormat())) {

		}

		// if (jpegColorType != null && jpegColorType.toInteger() > 0) {
		// sctInfo.setEL("jpeg_color_type",
		// ImageUtil.toColorType(jpegColorType.toInteger(), ""));
		// }
		// sct.setEL("mime_type",getMimeType());

		ColorModel cm = image().getColorModel();
		sct = eng().getCreationUtil().createStruct();
		sctInfo.setEL("colormodel", sct);
		int numComponents = cm.getNumComponents();
		boolean hasAlpha = cm.hasAlpha();
		sct.setEL("alpha_channel_support", eng().getCastUtil().toBoolean(hasAlpha));
		sct.setEL("alpha_premultiplied", eng().getCastUtil().toBoolean(cm.isAlphaPremultiplied()));
		sct.setEL("transparency", toStringTransparency(cm.getTransparency()));
		sct.setEL("pixel_size", eng().getCastUtil().toDouble(cm.getPixelSize()));
		sct.setEL("num_components", eng().getCastUtil().toDouble(numComponents));
		sct.setEL("num_color_components", eng().getCastUtil().toDouble(cm.getNumColorComponents()));
		sct.setEL("colorspace", toStringColorSpace(cm.getColorSpace()));

		// bits_component
		int[] bitspercomponent = cm.getComponentSize();
		Array arr = eng().getCreationUtil().createArray();
		Double value;
		for (int i = 0; i < bitspercomponent.length; i++) {
			sct.setEL("bits_component_" + (i + 1), value = Double.valueOf(bitspercomponent[i]));
			arr.appendEL(value);
		}
		sct.setEL("bits_component", arr);

		// colormodel_type
		if (cm instanceof ComponentColorModel)
			sct.setEL("colormodel_type", "ComponentColorModel");
		else if (cm instanceof IndexColorModel)
			sct.setEL("colormodel_type", "IndexColorModel");
		else if (cm instanceof PackedColorModel)
			sct.setEL("colormodel_type", "PackedColorModel");
		else
			sct.setEL("colormodel_type", eng().getListUtil().last(cm.getClass().getName(), ".", true));

		IIOMetadata metadata = getMetaData(sctInfo, null);
		if (ImageUtil.isJPEG(getFormat())) {
			String ct = ImageUtil.getColorType(image(), metadata, "");
			if (ct != null) {
				sctInfo.setEL("jpeg_color_type", ct);
			}
		}
		try {
			Log log = null;
			Config c = CFMLEngineFactory.getInstance().getThreadConfig();
			if (c != null)
				log = c.getLog("application");
			Metadata.addExifInfoToStruct(source, sctInfo, log);
		} catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
		return this.sctInfo = sctInfo;
	}

	public IIOMetadata getMetaData(Struct parent, String format) {
		if (fromNew)
			return null;

		InputStream is = null;
		javax.imageio.stream.ImageInputStreamImpl iis = null;
		try {
			if (Util.isEmpty(format))
				format = Util.isEmpty(this.format) ? ImageUtil.getOneWriterFormatName("png", "jpg", "jpeg")
						: this.format;

			if (source instanceof File) {
				iis = new FileImageInputStream((File) source);
			} else if (source == null) {
				iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(getImageBytes(format, true)));
			} else {
				iis = new MemoryCacheImageInputStream(is = source.getInputStream());
			}

			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			if (readers.hasNext()) {
				// pick the first available ImageReader
				ImageReader reader = readers.next();
				IIOMetadata meta = null;
				synchronized (sync) {
					// attach source to the reader
					reader.setInput(iis, true);

					// read metadata of first image
					meta = reader.getImageMetadata(0);
					meta.setFromTree(FORMAT, meta.getAsTree(FORMAT));
					reader.reset();
				}
				// generating dump
				if (parent != null) {
					String[] formatNames = meta.getMetadataFormatNames();
					for (int i = 0; i < formatNames.length; i++) {
						Node root = meta.getAsTree(formatNames[i]);
						// print.out(XMLeng().getCastUtil().toString(root));
						addMetaddata(parent, "metadata", root);
					}
				}
				return meta;
			}
		} catch (Exception e) {
		} finally {
			ImageUtil.closeEL(iis);
			eng().getIOUtil().closeSilent(is);
		}
		return null;
	}

	public Struct getIPTCMetadata() throws PageException {
		ImageMetadata md;
		Struct rtn = eng().getCreationUtil().createStruct();
		try {
			if (source instanceof File)
				md = Imaging.getMetadata((File) source);
			else
				md = Imaging.getMetadata(getImageBytes(format, true));

			// not jpeg
			if (!(md instanceof JpegImageMetadata))
				return rtn;

			// fill to struct
			Key KEYWORDS = eng().getCreationUtil().createKey("Keywords");
			Key SUBJECT_REFERENCE = eng().getCreationUtil().createKey("Subject Reference");

			JpegImageMetadata jmd = (JpegImageMetadata) md;
			JpegPhotoshopMetadata jpmd = jmd.getPhotoshop(); // selects IPTC metadata
			if (jpmd == null)
				return rtn;
			Iterator<? extends ImageMetadataItem> it = jpmd.getItems().iterator();
			ImageMetadataItem item;
			GenericImageMetadataItem i = null;
			Collection.Key k;
			Object v;
			Array arr;
			while (it.hasNext()) {
				item = it.next();
				if (item instanceof GenericImageMetadataItem) {
					i = (GenericImageMetadataItem) item;
					k = eng().getCreationUtil().createKey(i.getKeyword());
					v = rtn.get(k, null);
					if (v != null) {
						if (KEYWORDS.equals(k)) {
							rtn.set(k, v + ";" + i.getText());
						} else if (SUBJECT_REFERENCE.equals(k)) {
							rtn.set(k, v + " " + i.getText());
						} else if (v instanceof Array) {
							arr = (Array) v;
							arr.append(i.getText());
						} else {
							arr = eng().getCreationUtil().createArray();
							arr.append(v);
							arr.append(i.getText());
							rtn.set(k, arr);
						}
					} else
						rtn.set(k, i.getText());
				}
			}
		} catch (Exception e) {
			throw eng().getCastUtil().toPageException(e);
		}
		return rtn;
	}

	private void addMetaddata(Struct parent, String name, Node node) {

		// attributes
		NamedNodeMap attrs = node.getAttributes();
		Attr attr;
		int len = attrs.getLength();
		if (len == 1 && "value".equals(attrs.item(0).getNodeName())) {
			parent.setEL(name, attrs.item(0).getNodeValue());
		} else {
			Struct sct = metaGetChild(parent, name);
			for (int i = attrs.getLength() - 1; i >= 0; i--) {
				attr = (Attr) attrs.item(i);
				sct.setEL(attr.getName(), attr.getValue());
			}
		}

		// child nodes
		ArrayList<Node> children = CommonUtil.getChildNodes(node, Node.ELEMENT_NODE, null);
		Element el;
		for (int i = children.size() - 1; i >= 0; i--) {
			el = (Element) children.get(i);
			Struct sct = metaGetChild(parent, name);
			addMetaddata(sct, el.getNodeName(), children.get(i));
		}
	}

	private Struct metaGetChild(Struct parent, String name) {
		Object child = parent.get(name, null);
		if (child instanceof Struct)
			return (Struct) child;
		Struct sct = eng().getCreationUtil().createStruct();
		parent.setEL(name, sct);
		return sct;
	}

	public void sharpen(float gain) throws PageException {
		try {
			BufferedImage source = image();

			// Create a copy of the source image
			BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(),
					source.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : source.getType());

			// Define the unsharp mask kernel
			float amount = Math.abs(gain) * 0.5f;
			float[][] kernel;

			if (gain >= 0) {
				// Sharpening kernel
				kernel = new float[][] { { -amount / 4, -amount / 4, -amount / 4 },
						{ -amount / 4, 2 + amount, -amount / 4 }, { -amount / 4, -amount / 4, -amount / 4 } };
			} else {
				// Blurring kernel (Gaussian approximation)
				float blurAmount = amount / 4;
				kernel = new float[][] { { blurAmount, blurAmount, blurAmount },
						{ blurAmount, 1 - (blurAmount * 8), blurAmount }, { blurAmount, blurAmount, blurAmount } };
			}

			// Create the convolve operation
			Kernel convKernel = new Kernel(3, 3, flattenKernel(kernel));
			ConvolveOp convOp = new ConvolveOp(convKernel, ConvolveOp.EDGE_NO_OP,
					new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));

			// Apply the filter
			convOp.filter(source, result);

			// Update the image reference
			image(result);

		} catch (Exception e) {
			throw eng().getCastUtil().toPageException(e);
		}
	}

	// Helper method to flatten 2D kernel array
	private float[] flattenKernel(float[][] kernel) {
		float[] flatKernel = new float[kernel.length * kernel[0].length];
		int index = 0;
		for (int i = 0; i < kernel.length; i++) {
			for (int j = 0; j < kernel[0].length; j++) {
				flatKernel[index++] = kernel[i][j];
			}
		}
		return flatKernel;
	}

	public void setTranparency(float percent) throws PageException {
		if (percent == -1)
			return;
		tranparency = percent;
		AlphaComposite rule = AlphaComposite.getInstance(3, 1.0F - (percent / 100.0F));
		getGraphics().setComposite(rule);
	}

	public void invert() throws PageException {
		BufferedImage source = image();
		BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());

		// Get the raster and process all pixels
		for (int x = 0; x < source.getWidth(); x++) {
			for (int y = 0; y < source.getHeight(); y++) {
				int rgb = source.getRGB(x, y);

				// Extract and invert each channel
				int alpha = (rgb >> 24) & 0xff;
				int red = 255 - (rgb >> 16) & 0xff;
				int green = 255 - (rgb >> 8) & 0xff;
				int blue = 255 - rgb & 0xff;

				// Recombine the channels
				rgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
				result.setRGB(x, y, rgb);
			}
		}

		image(result);
	}

	public Image copy(float x, float y, float width, float height) throws PageException {
		BufferedImage source = image();

		// Convert float parameters to int and ensure they're within bounds
		int ix = Math.max(0, (int) x);
		int iy = Math.max(0, (int) y);
		int iw = Math.min(source.getWidth() - ix, (int) width);
		int ih = Math.min(source.getHeight() - iy, (int) height);

		// Create a new BufferedImage for the cropped area
		BufferedImage result = source.getSubimage(ix, iy, iw, ih);

		// Create a copy of the subimage since getSubimage returns a shared data buffer
		BufferedImage copy = new BufferedImage(iw, ih, source.getType());
		Graphics2D g = copy.createGraphics();
		g.drawImage(result, 0, 0, null);
		g.dispose();

		return new Image(copy);
	}

	public Image copy(float x, float y, float width, float height, float dx, float dy) throws PageException {
		// First create the cropped copy
		Image result = copy(x, y, width, height);
		BufferedImage img = result.getBufferedImage();

		// If dx/dy are specified (not -999), draw the copied area at the new location
		if (dx != -999 && dy != -999) {
			Graphics2D g = img.createGraphics();
			g.drawImage(img, (int) (dx - x), (int) (dy - y), null);
			g.dispose();
		}

		return result;
	}

	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle, boolean filled)
			throws PageException {
		if (filled)
			getGraphics().fillArc(x, y, width, height, startAngle, arcAngle);
		else
			getGraphics().drawArc(x, y, width, height, startAngle, arcAngle);
	}

	public void draw3DRect(int x, int y, int width, int height, boolean raised, boolean filled) throws PageException {
		if (filled)
			getGraphics().fill3DRect(x, y, width + 1, height + 1, raised);
		else
			getGraphics().draw3DRect(x, y, width, height, raised);
	}

	public void drawCubicCurve(double ctrlx1, double ctrly1, double ctrlx2, double ctrly2, double x1, double y1,
			double x2, double y2) throws PageException {
		CubicCurve2D curve = new CubicCurve2D.Double(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2);
		getGraphics().draw(curve);
	}

	public void drawPoint(int x, int y) throws PageException {
		drawLine(x, y, x + 1, y);
	}

	public void drawQuadraticCurve(double x1, double y1, double ctrlx, double ctrly, double x2, double y2)
			throws PageException {
		QuadCurve2D curve = new QuadCurve2D.Double(x1, y1, ctrlx, ctrly, x2, y2);
		getGraphics().draw(curve);
	}

	public void drawRect(int x, int y, int width, int height, boolean filled) throws PageException {
		if (filled)
			getGraphics().fillRect(x, y, width + 1, height + 1);
		else
			getGraphics().drawRect(x, y, width, height);
	}

	public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight, boolean filled)
			throws PageException {
		if (filled)
			getGraphics().fillRoundRect(x, y, width + 1, height + 1, arcWidth, arcHeight);
		else
			getGraphics().drawRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	public void drawLine(int x1, int y1, int x2, int y2) throws PageException {
		getGraphics().drawLine(x1, y1, x2, y2);
	}

	public void drawImage(Image img, int x, int y) throws PageException {
		getGraphics().drawImage(img.image(), x, y, null);
	}

	public void drawImage(Image img, int x, int y, int width, int height) throws PageException {
		getGraphics().drawImage(img.image(), x, y, width, height, null);
	}

	public void drawLines(int[] xcoords, int[] ycoords, boolean isPolygon, boolean filled) throws PageException {
		if (isPolygon) {
			if (filled)
				getGraphics().fillPolygon(xcoords, ycoords, xcoords.length);
			else
				getGraphics().drawPolygon(xcoords, ycoords, xcoords.length);
		} else {
			getGraphics().drawPolyline(xcoords, ycoords, xcoords.length);
		}
	}

	public void drawOval(int x, int y, int width, int height, boolean filled) throws PageException {
		if (filled)
			getGraphics().fillOval(x, y, width, height);
		else
			getGraphics().drawOval(x, y, width, height);
	}

	public void drawString(String text, int x, int y, Struct attr) throws PageException {

		if (attr != null && attr.size() > 0) {

			// font
			String font = eng().getCastUtil().toString(attr.get("font", "")).toLowerCase().trim();
			if (!eng().getStringUtil().isEmpty(font)) {
				font = FontUtil.getFont(font).getFontName();
			} else
				font = "Serif";

			// alpha
			// float alpha=eng().getCastUtil().toFloatValue(attr.get("alpha",null),1F);

			// size
			int size = eng().getCastUtil().toIntValue(attr.get("size", Integer.valueOf(10)));

			// style
			int style = Font.PLAIN;
			String strStyle = eng().getCastUtil().toString(attr.get("style", "")).toLowerCase();
			strStyle = CommonUtil.removeWhiteSpace(strStyle);
			if (!eng().getStringUtil().isEmpty(strStyle)) {
				if ("plain".equals(strStyle))
					style = Font.PLAIN;
				else if ("bold".equals(strStyle))
					style = Font.BOLD;
				else if ("italic".equals(strStyle))
					style = Font.ITALIC;
				else if ("bolditalic".equals(strStyle))
					style = Font.BOLD + Font.ITALIC;
				else if ("bold,italic".equals(strStyle))
					style = Font.BOLD + Font.ITALIC;
				else if ("italicbold".equals(strStyle))
					style = Font.BOLD + Font.ITALIC;
				else if ("italic,bold".equals(strStyle))
					style = Font.BOLD + Font.ITALIC;
				else
					throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException(
							"key style of argument attributeCollection has an invalid value [" + strStyle
									+ "], valid values are [plain,bold,italic,bolditalic]");
			}

			// strikethrough
			boolean strikethrough = eng().getCastUtil().toBooleanValue(attr.get("strikethrough", Boolean.FALSE));

			// underline
			boolean underline = eng().getCastUtil().toBooleanValue(attr.get("underline", Boolean.FALSE));

			AttributedString as = new AttributedString(text);
			as.addAttribute(TextAttribute.FONT, new Font(font, style, size));
			if (strikethrough)
				as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
			if (underline)
				as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			Graphics2D g = getGraphics();
			// if(alpha!=1D) setAlpha(g,alpha);

			g.drawString(as.getIterator(), x, y);
		} else
			getGraphics().drawString(text, x, y);

	}

	public void setDrawingStroke(Struct attr) throws PageException {

		// empty
		if (attr == null || attr.size() == 0) {
			setDrawingStroke(new BasicStroke());
			return;
		}

		// width
		float width = eng().getCastUtil().toFloatValue(attr.get("width", new Float(1F)));
		if (width < 0)
			throw CFMLEngineFactory.getInstance().getExceptionUtil()
					.createExpressionException("key [width] should be a none negativ number");

		// endcaps
		String strEndcaps = eng().getCastUtil().toString(attr.get("endcaps", "square"));
		strEndcaps = strEndcaps.trim().toLowerCase();
		int endcaps;
		if ("square".equals(strEndcaps))
			endcaps = BasicStroke.CAP_SQUARE;
		else if ("butt".equals(strEndcaps))
			endcaps = BasicStroke.CAP_BUTT;
		else if ("round".equals(strEndcaps))
			endcaps = BasicStroke.CAP_ROUND;
		else
			throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException(
					"key [endcaps] has an invalid value [" + strEndcaps + "], valid values are [square,round,butt]");

		// linejoins
		String strLinejoins = eng().getCastUtil().toString(attr.get("linejoins", "miter"));
		strLinejoins = strLinejoins.trim().toLowerCase();
		int linejoins;
		if ("bevel".equals(strLinejoins))
			linejoins = BasicStroke.JOIN_BEVEL;
		else if ("miter".equals(strLinejoins))
			linejoins = BasicStroke.JOIN_MITER;
		else if ("round".equals(strLinejoins))
			linejoins = BasicStroke.JOIN_ROUND;
		else
			throw CFMLEngineFactory.getInstance().getExceptionUtil()
					.createExpressionException("key [linejoins] has an invalid value [" + strLinejoins
							+ "], valid values are [bevel,miter,round]");

		// miterlimit
		float miterlimit = 10.0F;
		if (linejoins == BasicStroke.JOIN_MITER) {
			miterlimit = eng().getCastUtil().toFloatValue(attr.get("miterlimit", new Float(10F)));
			if (miterlimit < 1F)
				throw CFMLEngineFactory.getInstance().getExceptionUtil()
						.createExpressionException("key [miterlimit] should be greater or equal to 1");
		}

		// dashArray
		Object oDashArray = attr.get("dashArray", null);
		float[] dashArray = null;
		if (oDashArray != null) {
			dashArray = ArrayUtil.toFloatArray(oDashArray);
		}

		// dash_phase
		float dash_phase = eng().getCastUtil().toFloatValue(attr.get("dash_phase", new Float(0F)));

		setDrawingStroke(width, endcaps, linejoins, miterlimit, dashArray, dash_phase);
	}

	public void setDrawingStroke(float width, int endcaps, int linejoins, float miterlimit, float[] dash,
			float dash_phase) throws PageException {
		setDrawingStroke(new BasicStroke(width, endcaps, linejoins, miterlimit, dash, dash_phase));
	}

	public void setDrawingStroke(Stroke stroke) throws PageException {
		if (stroke == null)
			return;
		this.stroke = stroke;
		getGraphics().setStroke(stroke);
	}

	public void flip(int transpose) throws PageException {
		BufferedImage source = image();
		BufferedImage result;
		AffineTransform transform = new AffineTransform();

		int width = source.getWidth();
		int height = source.getHeight();

		switch (transpose) {
		case TRANSPOSE_VERTICAL:
			transform.scale(1, -1);
			transform.translate(0, -height);
			result = new BufferedImage(width, height, source.getType());
			break;

		case TRANSPOSE_HORIZONTAL:
			transform.scale(-1, 1);
			transform.translate(-width, 0);
			result = new BufferedImage(width, height, source.getType());
			break;

		case TRANSPOSE_ROTATE_90:
			transform.rotate(Math.PI / 2, width / 2, height / 2);
			transform.translate((height - width) / 2, (height - width) / 2);
			result = new BufferedImage(height, width, source.getType());
			break;

		case TRANSPOSE_ROTATE_180:
			transform.rotate(Math.PI, width / 2, height / 2);
			result = new BufferedImage(width, height, source.getType());
			break;

		case TRANSPOSE_ROTATE_270:
			transform.rotate(-Math.PI / 2, width / 2, height / 2);
			transform.translate((width - height) / 2, (width - height) / 2);
			result = new BufferedImage(height, width, source.getType());
			break;

		case TRANSPOSE_DIAGONAL:
			transform.rotate(Math.PI / 2, width / 2, height / 2);
			transform.scale(-1, 1);
			transform.translate(-height, 0);
			result = new BufferedImage(height, width, source.getType());
			break;

		case TRANSPOSE_ANTIDIAGONAL:
			transform.rotate(-Math.PI / 2, width / 2, height / 2);
			transform.scale(-1, 1);
			transform.translate(-height, 0);
			result = new BufferedImage(height, width, source.getType());
			break;

		default:
			throw eng().getExceptionUtil().createApplicationException("Invalid transpose type");
		}

		Graphics2D g = result.createGraphics();
		g.setTransform(transform);
		g.drawImage(source, 0, 0, null);
		g.dispose();

		image(result);
	}

	public void grayscale() throws PageException {
		BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D graphics = img.createGraphics();
		graphics.drawImage(image(), new AffineTransformOp(AffineTransform.getTranslateInstance(0.0, 0.0), 1), 0, 0);
		graphics.dispose();
		image(img);
	}

	public void rgb() throws PageException {
		BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
		Graphics2D graphics = img.createGraphics();
		graphics.drawImage(image(), new AffineTransformOp(AffineTransform.getTranslateInstance(0.0, 0.0), 1), 0, 0);
		graphics.dispose();
		image(img);

	}

	public void threeBBger() throws PageException {
		BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics = img.createGraphics();
		graphics.drawImage(image(), new AffineTransformOp(AffineTransform.getTranslateInstance(0.0, 0.0), 1), 0, 0);
		graphics.dispose();
		image(img);
	}

	public void overlay(Image topImage) throws PageException {
		BufferedImage source = image();
		BufferedImage overlay = topImage.image();

		// Convert both images to compatible types if needed
		BufferedImage compatibleSource = ensureCompatible(source);
		BufferedImage compatibleOverlay = ensureCompatible(overlay);

		// Create graphics context with high-quality settings
		Graphics2D g = compatibleSource.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Calculate center position
		int x = (compatibleSource.getWidth() - compatibleOverlay.getWidth()) / 2;
		int y = (compatibleSource.getHeight() - compatibleOverlay.getHeight()) / 2;

		// Draw the overlay image
		g.drawImage(compatibleOverlay, x, y, null);
		g.dispose();

		// Update the source image
		image(compatibleSource);
	}

	private BufferedImage ensureCompatible(BufferedImage img) {
		// If image is already in ARGB, return it
		if (img.getType() == BufferedImage.TYPE_INT_ARGB) {
			return img;
		}

		// Create new image with ARGB color model
		BufferedImage compatible = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

		// Copy and convert the original image
		Graphics2D g = compatible.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();

		return compatible;
	}

	public void paste(Image topImage, int x, int y) throws PageException {
		Graphics2D g = createTempGraphics();

		// Set high quality rendering hints
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Draw the top image at the specified coordinates
		g.drawImage(topImage.image(), x, y, null);

		g.dispose();
	}

	public void setXorMode(Color color) throws PageException {
		if (color == null)
			return;
		xmColor = color;
		getGraphics().setXORMode(color);
	}

	public void translate(int xtrans, int ytrans, Object interpolation) throws PageException {
		BufferedImage source = image();
		BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());

		// Set interpolation hint based on parameter
		Object interpValue = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR; // default
		if ("bilinear".equalsIgnoreCase(String.valueOf(interpolation))) {
			interpValue = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
		} else if ("bicubic".equalsIgnoreCase(String.valueOf(interpolation))) {
			interpValue = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
		}

		// Create graphics context with specified interpolation
		Graphics2D g = result.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpValue);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		// Clear the background
		g.clearRect(0, 0, result.getWidth(), result.getHeight());

		// Draw the translated image
		g.drawImage(source, xtrans, ytrans, null);
		g.dispose();

		image(result);
	}

	public void translateAxis(int x, int y) throws PageException {
		getGraphics().translate(x, y);
	}

	public void rotateAxis(double angle) throws PageException {
		getGraphics().rotate(Math.toRadians(angle));
	}

	public void rotateAxis(double angle, double x, double y) throws PageException {
		getGraphics().rotate(Math.toRadians(angle), x, y);
	}

	public void shearAxis(double shx, double shy) throws PageException {
		getGraphics().shear(shx, shy);
	}

	public void shear(float shear, int direction, Object interpolation) throws PageException {
		BufferedImage sourceImage = image();
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();

		// Determine the shear transformation
		AffineTransform transform = new AffineTransform();
		if (direction == SHEAR_HORIZONTAL) {
			transform.shear(shear, 0.0);
		} else {
			transform.shear(0.0, shear);
		}

		// Create a new image with the same dimensions and transparency settings
		BufferedImage newImage = new BufferedImage(width, height, sourceImage.getType());
		Graphics2D g2d = newImage.createGraphics();

		// Set interpolation based on the provided parameter
		if (interpolation == RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR) {
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		} else if (interpolation == RenderingHints.VALUE_INTERPOLATION_BILINEAR) {
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		} else if (interpolation == RenderingHints.VALUE_INTERPOLATION_BICUBIC) {
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		}

		// Set background color
		Color bg = getGraphics().getBackground();
		g2d.setColor(bg);
		g2d.fillRect(0, 0, width, height);

		// Apply the transformation and draw the sheared image
		g2d.setTransform(transform);
		g2d.drawImage(sourceImage, 0, 0, null);

		g2d.dispose();

		// Update the image with the transformed version
		image(newImage);
	}

	public BufferedImage getBufferedImage() throws PageException {
		return image();
	}

	public BufferedImage image() throws PageException {
		if (_image == null)
			throw (CFMLEngineFactory.getInstance().getExceptionUtil()
					.createExpressionException("image is not initialized"));
		return _image;
	}

	public void image(BufferedImage image) {
		this._image = image;
		graphics = null;

		sctInfo = null;
	}

	private Graphics2D getGraphics() throws PageException {
		if (graphics == null) {
			graphics = image().createGraphics();
			// reset all properties
			if (antiAlias != ANTI_ALIAS_NONE)
				setAntiAliasing(antiAlias == ANTI_ALIAS_ON);
			if (bgColor != null)
				setBackground(bgColor);
			if (fgColor != null)
				setColor(fgColor);
			if (alpha != 1)
				setAlpha(alpha);
			if (tranparency != -1)
				setTranparency(tranparency);
			if (xmColor != null)
				setXorMode(xmColor);
			if (stroke != null)
				setDrawingStroke(stroke);
		}
		return graphics;
	}

	private Graphics2D createTempGraphics() throws PageException {
		Graphics2D tempGraphics = image().createGraphics();

		// Apply current settings to temp graphics
		if (antiAlias != ANTI_ALIAS_NONE) {
			if (antiAlias == ANTI_ALIAS_ON) {
				tempGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
						RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				tempGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			} else {
				tempGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
						RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
				tempGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		}
		if (bgColor != null)
			tempGraphics.setBackground(bgColor);
		if (fgColor != null)
			tempGraphics.setColor(fgColor);
		if (alpha != 1) {
			Composite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
			tempGraphics.setComposite(alphaComposite);
		}
		if (stroke != null)
			tempGraphics.setStroke(stroke);

		return tempGraphics;
	}

	private String toStringColorSpace(ColorSpace colorSpace) {
		switch (colorSpace.getType()) {
		case 0:
			return "Any of the family of XYZ color spaces";
		case 1:
			return "Any of the family of Lab color spaces";
		case 2:
			return "Any of the family of Luv color spaces";
		case 3:
			return "Any of the family of YCbCr color spaces";
		case 4:
			return "Any of the family of Yxy color spaces";
		case 5:
			return "Any of the family of RGB color spaces";
		case 6:
			return "Any of the family of GRAY color spaces";
		case 7:
			return "Any of the family of HSV color spaces";
		case 8:
			return "Any of the family of HLS color spaces";
		case 9:
			return "Any of the family of CMYK color spaces";
		case 11:
			return "Any of the family of CMY color spaces";
		case 12:
			return "Generic 2 component color space.";
		case 13:
			return "Generic 3 component color space.";
		case 14:
			return "Generic 4 component color space.";
		case 15:
			return "Generic 5 component color space.";
		case 16:
			return "Generic 6 component color space.";
		case 17:
			return "Generic 7 component color space.";
		case 18:
			return "Generic 8 component color space.";
		case 19:
			return "Generic 9 component color space.";
		case 20:
			return "Generic 10 component color space.";
		case 21:
			return "Generic 11 component color space.";
		case 22:
			return "Generic 12 component color space.";
		case 23:
			return "Generic 13 component color space.";
		case 24:
			return "Generic 14 component color space.";
		case 25:
			return "Generic 15 component color space.";
		case 1001:
			return "CIEXYZ";
		case 1003:
			return "GRAY";
		case 1004:
			return "LINEAR_RGB";
		case 1002:
			return "PYCC";
		case 1000:
			return "sRGB";
		}

		return "Unknown ColorSpace" + colorSpace;
	}

	private Object toStringTransparency(int transparency) {
		if (Transparency.OPAQUE == transparency)
			return "OPAQUE";
		if (Transparency.BITMASK == transparency)
			return "BITMASK";
		if (Transparency.TRANSLUCENT == transparency)
			return "TRANSLUCENT";
		return "Unknown type of transparency";
	}

	public String writeBase64(Resource destination, String format, boolean inHTMLFormat)
			throws PageException, IOException {
		// destination
		if (destination == null) {
			if (source != null)
				destination = source;
			else
				throw new IOException("missing destination file");
		}
		format = ImageUtil.toFormat(format);
		String content = getBase64String(format);
		if (inHTMLFormat)
			content = "data:image/" + format + ";base64," + content;
		eng().getIOUtil().write(destination, content, false, (Charset) null);
		return content;
	}

	public String getBase64String(String format) throws PageException {
		return new String(eng().getCastUtil().toBase64(getImageBytes(ImageUtil.toFormat(format))));
	}

	public void writeOut(Resource destination, String format, boolean overwrite, float quality, boolean noMeta)
			throws IOException, PageException {

		if (format == null && destination != null)
			format = ImageUtil.getFormat(destination);

		if (destination == null) {
			if (source != null)
				destination = source;
			else
				throw new IOException("missing destination file");
		}
		if (!destination.getParentResource().exists()) {
			throw new IOException("destination folder [ " + destination.getParentResource() + " ] doesn't exist");
		}

		if (destination.exists()) {
			if (!overwrite)
				throw new IOException("can't overwrite existing image");
		}

		ImageUtil.writeOut(this, destination, format, quality, noMeta || orientation != Metadata.ORIENTATION_UNDEFINED);
	}

	public void writeOut(OutputStream os, String format, float quality, boolean closeStream, boolean noMeta)
			throws IOException, PageException {
		ImageUtil.writeOut(this, os, format, quality, closeStream,
				noMeta || orientation != Metadata.ORIENTATION_UNDEFINED);
	}

	/*
	 * public void convertX(String format) { this.format=format; }
	 */

	public void scaleToFit(String fitWidth, String fitHeight, String interpolation, double blurFactor)
			throws PageException {
		if (eng().getStringUtil().isEmpty(fitWidth) || eng().getStringUtil().isEmpty(fitHeight))
			resize(fitWidth, fitHeight, interpolation, blurFactor);
		else {
			float width = eng().getCastUtil().toFloatValue(fitWidth) / getWidth();
			float height = eng().getCastUtil().toFloatValue(fitHeight) / getHeight();
			if (width < height)
				resize(fitWidth, "", interpolation, blurFactor);
			else
				resize("", fitHeight, interpolation, blurFactor);
		}
	}

	/**
	 * Convenience method that returns a scaled instance of the provided
	 * {@code BufferedImage}.
	 *
	 * @param img           the original image to be scaled
	 * @param targetWidth   the desired width of the scaled instance, in pixels
	 * @param targetHeight  the desired height of the scaled instance, in pixels
	 * @param hint          one of the rendering hints that corresponds to
	 *                      {@code RenderingHints.KEY_INTERPOLATION} (e.g.
	 *                      {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
	 *                      {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
	 *                      {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
	 * @param higherQuality if true, this method will use a multi-step scaling
	 *                      technique that provides higher quality than the usual
	 *                      one-step technique (only useful in downscaling cases,
	 *                      where {@code targetWidth} or {@code targetHeight} is
	 *                      smaller than the original dimensions, and generally only
	 *                      when the {@code BILINEAR} hint is specified)
	 * @return a scaled version of the original {@code BufferedImage}
	 */
	private BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint,
			boolean higherQuality) {
		// functionality not supported in java 1.4
		int transparency = Transparency.OPAQUE;
		try {
			transparency = img.getTransparency();
		} catch (Exception e) {
		}
		int type = (transparency == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

		BufferedImage ret = img;
		int w, h;
		if (higherQuality) {
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = img.getWidth();
			h = img.getHeight();
		} else {
			// Use one-step technique: scale directly from original
			// size to target size with a single drawImage() call
			w = targetWidth;
			h = targetHeight;
		}

		do {
			if (higherQuality && w > targetWidth) {
				w /= 2;
				if (w < targetWidth) {
					w = targetWidth;
				}
			}

			if (higherQuality && h > targetHeight) {
				h /= 2;
				if (h < targetHeight) {
					h = targetHeight;
				}
			}

			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;
		} while (w != targetWidth || h != targetHeight);

		return ret;
	}

	public void resize(int scale, String interpolation, double blurFactor) throws PageException {
		if (blurFactor < 0D || blurFactor > 10D)
			throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException(
					"argument [blurFactor] must be between 0 and 10, was [" + blurFactor + "]");

		BufferedImage bi = image();
		float width = bi.getWidth() / 100F * scale;
		// float height = getHeight() / 100F * scale;

		resize(bi, (int) width, -1, toInterpolation(interpolation), blurFactor);
	}

	public void resize(String strWidth, String strHeight, String interpolation, double blurFactor)
			throws PageException {
		if (eng().getStringUtil().isEmpty(strWidth, true) && eng().getStringUtil().isEmpty(strHeight, true))
			throw CFMLEngineFactory.getInstance().getExceptionUtil()
					.createExpressionException("you have to define width or height");
		if (blurFactor < 0D || blurFactor > 10D)
			throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException(
					"argument [blurFactor] must be between 0 and 10, was [" + blurFactor + "]");
		BufferedImage bi = image();
		float height = resizeDimesion("height", strHeight, bi.getHeight());
		float width = resizeDimesion("width", strWidth, bi.getWidth());
		resize(bi, (int) width, (int) height, toInterpolation(interpolation), blurFactor);
	}

	public void resizeImage2(int width, int height) throws PageException {
		image(getScaledInstance(image(), width, height, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false));
	}

	public void resizeImage(int width, int height, int interpolation) throws PageException {
		Object ip;
		if (interpolation == IPC_NEAREST)
			ip = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
		else if (interpolation == IPC_BICUBIC)
			ip = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
		else if (interpolation == IPC_BILINEAR)
			ip = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
		else
			throw CFMLEngineFactory.getInstance().getExceptionUtil()
					.createExpressionException("invalid interpoltion definition");

		BufferedImage dst = new BufferedImage(width, height, image().getType());
		Graphics2D graphics = dst.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, ip);
		graphics.drawImage(image(), 0, 0, width, height, null);
		graphics.dispose();
		image(dst);

	}

	private float resizeDimesion(String label, String strDimension, float originalDimension) throws PageException {
		if (eng().getStringUtil().isEmpty(strDimension, true))
			return -1;
		strDimension = strDimension.trim();

		if (strDimension != null && strDimension.endsWith("%")) {
			float p = eng().getCastUtil().toFloatValue(strDimension.substring(0, (strDimension.length() - 1))) / 100.0F;
			return originalDimension * p;
		}
		float dimension = eng().getCastUtil().toFloatValue(strDimension);
		if (dimension <= 0F)
			throw CFMLEngineFactory.getInstance().getExceptionUtil()
					.createExpressionException(label + " has to be a none negative number");
		return dimension;
	}

	public void resize(int width, int height, int interpolation, double blurFactor) throws PageException {
		resize(image(), width, height, interpolation, blurFactor);
	}

	private void resize(BufferedImage bi, int width, int height, int interpolation, double blurFactor)
			throws PageException {

		if (interpolation == IP_AUTOMATIC && blurFactor == 1) {
			try {
				image(ImageUtil.resize(bi, width, height, false));
				return;
			} catch (Exception e) {
			}
		}

		ColorModel cm = bi.getColorModel();

		if (interpolation == IP_HIGHESTPERFORMANCE) {
			interpolation = IPC_BICUBIC;
		}

		if (cm.getColorSpace().getType() == ColorSpace.TYPE_GRAY && cm.getComponentSize()[0] == 8) {
			if (interpolation == IP_AUTOMATIC || interpolation == IP_HIGHESTQUALITY
					|| interpolation == IP_HIGHPERFORMANCE || interpolation == IP_HIGHQUALITY
					|| interpolation == IP_MEDIUMPERFORMANCE || interpolation == IP_MEDIUMQUALITY) {
				interpolation = IPC_BICUBIC;
			}
			if (interpolation != IPC_BICUBIC && interpolation != IPC_BILINEAR && interpolation != IPC_NEAREST) {
				throw CFMLEngineFactory.getInstance().getExceptionUtil()
						.createExpressionException("invalid grayscale interpolation");
			}
		}

		Scalr.Method method = toMethod(interpolation);
		if (blurFactor == 1D && method != null) {
			if (width == -1)
				image(Scalr.resize(image(), method, Scalr.Mode.FIT_TO_HEIGHT, 1, height, new BufferedImageOp[0]));
			else if (height == -1)
				image(Scalr.resize(image(), method, Scalr.Mode.FIT_TO_WIDTH, width, 1, new BufferedImageOp[0]));
			else
				image(Scalr.resize(image(), method, Scalr.Mode.FIT_EXACT, width, height, new BufferedImageOp[0]));
		} else {
			int h = bi.getHeight();
			int w = bi.getWidth();
			if (height == -1)
				height = (int) Math.round(h * (1D / w * width));
			if (width == -1)
				width = (int) Math.round(w * (1D / h * height));

			if (interpolation <= IPC_MAX) {
				resizeImage(width, height, interpolation);
			} else {
				image(ImageResizer.resize(image(), width, height, interpolation, blurFactor));

			}
		}
	}

	private void checkOrientation(Object input) throws PageException, ImageReadException, IOException {
		try {
			ImageMetadata metadata;
			if (input instanceof Resource)
				metadata = Metadata.getMetadata((Resource) input);
			else
				metadata = Imaging.getMetadata((byte[]) input);

			int ori = Metadata.getOrientation(metadata);
			if (ori > 0) {
				changeOrientation(metadata, ori);
				orientation = Metadata.ORIENTATION_NORMAL;
				// if (input instanceof Resource) changeExifMetadata(metadata, (Resource)
				// input);

				// IImageMetadata metadata
			}
		} catch (Exception e) {

		}
	}

	private void changeOrientation(ImageMetadata metadata, int orientation) throws PageException {
		if (orientation == Metadata.ORIENTATION_ROTATE_90) {
			rotateClockwise90();
			return;
		}
		if (orientation == Metadata.ORIENTATION_ROTATE_180) {
			rotateClockwise180();
			return;
		}
		if (orientation == Metadata.ORIENTATION_ROTATE_270) {
			rotateClockwise270();
			return;
		}
		if (orientation == Metadata.ORIENTATION_FLIP_HORIZONTAL) {
			flipHorizontally();
			return;
		}
		if (orientation == Metadata.ORIENTATION_FLIP_VERTICAL) {
			flipVertically();
			return;
		}

	}

	public void rotateClockwise90() throws PageException {
		BufferedImage src = image();
		int width = src.getWidth();
		int height = src.getHeight();

		BufferedImage dest = new BufferedImage(height, width, src.getType());

		Graphics2D graphics2D = dest.createGraphics();
		graphics2D.translate((height - width) / 2, (height - width) / 2);
		graphics2D.rotate(Math.PI / 2, height / 2, width / 2);
		graphics2D.drawRenderedImage(src, null);
		image(dest);
	}

	public void rotateClockwise180() throws PageException {
		rotateClockwise90();
		rotateClockwise90();
	}

	public void rotateClockwise270() throws PageException {
		rotateClockwise90();
		rotateClockwise90();
		rotateClockwise90();
	}

	public void flipVertically() throws PageException {
		BufferedImage src = image();
		int width = src.getWidth();
		int height = src.getHeight();

		BufferedImage dest = new BufferedImage(width, height, src.getType());

		Graphics2D g2 = dest.createGraphics();
		g2.drawImage(src, 0, height, width, -height, null);
		image(dest);
	}

	public void flipHorizontally() throws PageException {
		BufferedImage src = image();
		int width = src.getWidth();
		int height = src.getHeight();

		BufferedImage dest = new BufferedImage(width, height, src.getType());

		Graphics2D g2 = dest.createGraphics();

		g2.drawImage(src, width, 0, -width, height, null);
		image(dest);
	}

	public void changeExifMetadata(ImageMetadata metadata, final Resource dst)
			throws IOException, ImageReadException, ImageWriteException {
		OutputStream os = null;
		boolean canThrow = false;
		try {
			TiffOutputSet outputSet = null;

			// note that metadata might be null if no metadata is found.
			final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			if (null != jpegMetadata) {
				// note that exif might be null if no Exif metadata is found.
				final TiffImageMetadata exif = jpegMetadata.getExif();

				if (null != exif) {
					// TiffImageMetadata class is immutable (read-only).
					// TiffOutputSet class represents the Exif data to write.
					//
					// Usually, we want to update existing Exif metadata by
					// changing
					// the values of a few fields, or adding a field.
					// In these cases, it is easiest to use getOutputSet() to
					// start with a "copy" of the fields read from the image.
					outputSet = exif.getOutputSet();
				}
			}

			// if file does not contain any exif metadata, we create an empty
			// set of exif metadata. Otherwise, we keep all of the other
			// existing tags.
			if (null == outputSet) {
				outputSet = new TiffOutputSet();
			}

			{
				// Example of how to add a field/tag to the output set.
				//
				// Note that you should first remove the field/tag if it already
				// exists in this directory, or you may end up with duplicate
				// tags. See above.
				//
				// Certain fields/tags are expected in certain Exif directories;
				// Others can occur in more than one directory (and often have a
				// different meaning in different directories).
				//
				// TagInfo constants often contain a description of what
				// directories are associated with a given tag.
				//
				final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
				// make sure to remove old value if present (this method will
				// not fail if the tag does not exist).
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
				exifDirectory.add(ExifTagConstants.EXIF_TAG_APERTURE_VALUE, new RationalNumber(3, 10));
			}

			{
				// Example of how to add/update GPS info to output set.

				// New York City
				final double longitude = -74.0; // 74 degrees W (in Degrees East)
				final double latitude = 40 + 43 / 60.0; // 40 degrees N (in Degrees
				// North)

				outputSet.setGPSInDegrees(longitude, latitude);
			}

			final TiffOutputDirectory exifDirectory = outputSet.getOrCreateRootDirectory();
			exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SOFTWARE);
			exifDirectory.add(ExifTagConstants.EXIF_TAG_SOFTWARE, "SomeKind");

			os = dst.getOutputStream();
			os = new BufferedOutputStream(os);

			// new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);

			canThrow = true;
		} finally {
			Util.closeEL(os);
		}
	}

	public void rotate(float x, float y, float angle, int interpolation) throws PageException {
		if (x == -1 && y == -1) {
			if (angle == 90) {
				rotateClockwise90();
				return;
			}
			if (angle == 180) {
				rotateClockwise180();
				return;
			}
			if (angle == 270) {
				rotateClockwise270();
				return;
			}
		}

		if (x == -1) {
			x = getWidth() / 2.0f;
		}
		if (y == -1) {
			y = getHeight() / 2.0f;
		}

		double radians = Math.toRadians(angle);
		BufferedImage src = image();
		int width = src.getWidth();
		int height = src.getHeight();

		// Calculate the new dimensions to fit the rotated image
		int newWidth = (int) Math.round(Math.abs(width * Math.cos(radians)) + Math.abs(height * Math.sin(radians)));
		int newHeight = (int) Math.round(Math.abs(height * Math.cos(radians)) + Math.abs(width * Math.sin(radians)));

		// Create an output image that accounts for rotation without cutting parts
		BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, src.getType());
		Graphics2D g2d = rotatedImage.createGraphics();

		// Apply interpolation for smooth rotation
		Object interpolationHint = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
		if (interpolation == INTERPOLATION_NEAREST) {
			interpolationHint = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
		} else if (interpolation == INTERPOLATION_BILINEAR) {
			interpolationHint = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
		}
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationHint);

		// Perform rotation and translate to center
		AffineTransform transform = new AffineTransform();
		transform.translate((newWidth - width) / 2.0, (newHeight - height) / 2.0);
		transform.rotate(radians, width / 2.0, height / 2.0);
		g2d.setTransform(transform);
		g2d.drawImage(src, 0, 0, null);
		g2d.dispose();

		image(rotatedImage);
	}

	public static Image toImage(Object obj) throws PageException {
		return toImage(eng().getThreadPageContext(), obj, true);
	}

	// used in bytecode
	public static Image toImage(Object obj, PageContext pc) throws PageException {
		return toImage(pc, obj, true);
	}

	public static Image toImage(PageContext pc, Object obj) throws PageException {
		return toImage(pc, obj, true);
	}

	public static Image toImage(PageContext pc, Object obj, boolean checkForVariables) throws PageException {
		if (obj instanceof Image)
			return (Image) obj;
		if (obj instanceof ObjectWrap)
			return toImage(pc, ((ObjectWrap) obj).getEmbededObject(), checkForVariables);

		if (obj instanceof BufferedImage)
			return new Image((BufferedImage) obj);

		// try to load from binary
		if (eng().getDecisionUtil().isBinary(obj)) {
			try {
				return new Image(eng().getCastUtil().toBinary(obj), null);
			} catch (Exception e) {
				throw eng().getCastUtil().toPageException(e);
			}
		}
		// try to load from String (base64)
		if (obj instanceof CharSequence) {
			String str = eng().getCastUtil().toString(obj);
			if (checkForVariables && pc != null) {
				Object o = CommonUtil.getVariableEL(pc, str, null);
				if (o != null)
					return toImage(pc, o, false);
			}
			try {
				return Image.getInstance(pc, str, null);
			} catch (Exception e) {

				throw eng().getCastUtil().toPageException(e);
			}
		}

		throw eng().getExceptionUtil().createCasterException(obj, "Image");
	}

	public static Image toImage(PageContext pc, Object obj, boolean checkForVariables, Image defaultValue) {
		try {
			return toImage(pc, obj, checkForVariables);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static boolean isImage(Object obj) {
		if (obj instanceof Image)
			return true;
		if (obj instanceof ObjectWrap)
			return isImage(((ObjectWrap) obj).getEmbededObject(""));
		return false;
	}

	@Override
	public Object call(PageContext pc, Key methodName, Object[] args) throws PageException {
		Object obj = get(methodName, null);
		if (obj instanceof UDF) {
			return ((UDF) obj).call(pc, methodName, args, false);
		}

		Coll coll = CommonUtil.getMembers(pc).get(methodName);
		if (coll != null) {
			if (!coll.memberChaining)
				return coll.bif.invoke(pc, pre(args));
			else
				coll.bif.invoke(pc, pre(args));
			return this;
		}

		// TODO handle this dyn
		/*
		 * if(methodName.equals("blur")) { new ImageBlur().invoke(pc, pre(args)); return
		 * this; }
		 */

		return CommonUtil.call(pc, this, methodName, args, new short[] { TYPE_IMAGE }, new String[] { "image" });
	}

	private Object[] pre(Object[] args) {
		Object[] tmp = new Object[args.length + 1];
		tmp[0] = this;
		for (int i = 0; i < args.length; i++) {
			tmp[i + 1] = args[i];
		}
		return tmp;
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws PageException {
		Object obj = get(methodName, null);
		if (obj instanceof UDF) {
			return ((UDF) obj).callWithNamedValues(pc, methodName, args, false);
		}
		return CommonUtil.callWithNamedValues(pc, this, methodName, args, TYPE_IMAGE, "image");
	}

	public static boolean isCastableToImage(PageContext pc, Object obj) {
		if (isImage(obj))
			return true;
		return toImage(pc, obj, true, null) != null;
	}

	public static Image createImage(PageContext pc, Object obj, boolean check4Var, boolean clone, boolean checkAccess,
			String format) throws PageException {
		try {
			if ((obj instanceof Resource || obj instanceof File)) {
				Resource res = eng().getCastUtil().toResource(obj);
				if (checkAccess)
					pc.getConfig().getSecurityManager().checkFileLocation(res);
				return new Image(res, format);
			}
			if (obj instanceof CharSequence) {
				String str = obj.toString();
				CFMLEngineFactory.getInstance().getDecisionUtil().isCastableToBinary(str, true);

				Exception e = null;
				// file
				if (str.length() < 4000) {
					try {
						Resource res = eng().getResourceUtil().toResourceExisting(pc, str);
						pc.getConfig().getSecurityManager().checkFileLocation(res);

						return new Image(res, format);
					} catch (Exception ee) {
						e = ee;
					}
				}

				// Base64
				try {
					return new Image(str, format);
				} catch (Exception ee) {
					if (e == null)
						e = ee;
				}

				// variable
				if (check4Var && eng().getDecisionUtil().isVariableName(str)) {
					try {
						return createImage(pc, pc.getVariable(str), false, clone, checkAccess, format);
					} catch (Exception ee) {
						throw e;
					}
				}

				throw e;
			}
			if (obj instanceof Image) {
				if (clone)
					return (Image) ((Image) obj).clone();
				return (Image) obj;
			}
			if (eng().getDecisionUtil().isBinary(obj))
				return new Image(eng().getCastUtil().toBinary(obj), format);
			if (obj instanceof BufferedImage)
				return new Image(((BufferedImage) obj));
			if (obj instanceof java.awt.Image)
				return new Image(toBufferedImage((java.awt.Image) obj));

		} catch (Exception e) {
			throw eng().getCastUtil().toPageException(e);
		}
		throw eng().getExceptionUtil().createCasterException(obj, "Image");
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		try {
			return new Image(getImageBytes(null));
		} catch (Exception e) {
			if (Util.isEmpty(getFormat()) && _image != null) {
				ColorModel cm = _image.getColorModel();
				boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
				WritableRaster raster = _image.copyData(null);
				BufferedImage bi = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
				return new Image(bi);
			}
			throw eng().getExceptionUtil().createPageRuntimeException(eng().getCastUtil().toPageException(e));
		}
	}

	public ColorModel getColorModel() throws PageException {
		return image().getColorModel();
	}

	public void crop(int x, int y, int width, int height) throws PageException {
		try {
			BufferedImage source = image();

			// Convert float parameters to int and ensure they're within bounds
			x = Math.max(0, x);
			y = Math.max(0, y);

			// Calculate actual width and height considering image boundaries
			width = Math.min(width, source.getWidth() - x);
			height = Math.min(height, source.getHeight() - y);

			// Create cropped image maintaining original color model
			BufferedImage cropped = new BufferedImage(width, height,
					source.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : source.getType());

			// Perform the crop operation
			Graphics2D g2d = cropped.createGraphics();
			try {
				// Enable high-quality rendering
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

				// Draw the cropped portion
				g2d.drawImage(source, 0, 0, width, height, // Destination coordinates
						x, y, x + width, y + height, // Source coordinates
						null);
			} finally {
				g2d.dispose(); // Clean up graphics resources
			}

			// Update the image reference
			image(cropped);

		} catch (Exception e) {
			throw eng().getCastUtil().toPageException(e);
		}
	}

	public int getWidth() throws PageException {
		return image().getWidth();
	}

	public int getHeight() throws PageException {
		return image().getHeight();
	}

	public String getFormat() {
		return format;
	}

	public byte[] getImageBytes(String format) throws PageException {
		return getImageBytes(format, false);
	}

	public byte[] getImageBytes(String format, boolean noMeta) throws PageException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			writeOut(baos, format, 1f, true, noMeta);
		} catch (IOException e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
		return baos.toByteArray();
	}

	public void setColor(Color color) throws PageException {
		if (color == null)
			return;
		fgColor = color;
		getGraphics().setColor(color);
	}

	public void setAlpha(float alpha) throws PageException {
		this.alpha = alpha;
		Graphics2D g = getGraphics();

		Composite alphaComposite;
		if (composite == null) {
			if (alpha == 1)
				return;
			composite = g.getComposite();
		}
		if (alpha == 1)
			alphaComposite = composite;
		else
			alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);

		g.setComposite(alphaComposite);
		// graphics.setComposite(originalComposite);
	}

	public void setBackground(Color color) throws PageException {
		if (color == null)
			return;
		bgColor = color;
		getGraphics().setBackground(color);
	}

	public void setAntiAliasing(boolean antiAlias) throws PageException {
		this.antiAlias = antiAlias ? ANTI_ALIAS_ON : ANTI_ALIAS_OFF;
		Graphics2D graphics = getGraphics();
		if (antiAlias) {
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		} else {
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}

	private Struct _info() {
		try {
			return info();
		} catch (PageException e) {
			throw eng().getExceptionUtil().createPageRuntimeException(eng().getCastUtil().toPageException(e));
		}
	}

	@Override
	public void clear() {
		throw new RuntimeException("can't clear struct, struct is readonly");
	}

	@Override
	public boolean containsKey(Key key) {
		return _info().containsKey(key);
	}

	public boolean containsKey(PageContext pc, Key key) {
		return _info().containsKey(key);
	}

	@Override
	public Object get(Key key) throws PageException {
		return info().get(key);
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		return _info().get(key, defaultValue);
	}

	@Override
	public Key[] keys() {
		return _info().keys();
	}

	@Override
	public Object remove(Key key) throws PageException {
		throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException(
				"can't remove key [" + key.getString() + "] from struct, struct is readonly");
	}

	@Override
	public Object removeEL(Key key) {
		throw eng().getExceptionUtil().createPageRuntimeException(eng().getExceptionUtil().createApplicationException(
				"can't remove key [" + key.getString() + "] from struct, struct is readonly"));
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		throw CFMLEngineFactory.getInstance().getExceptionUtil()
				.createExpressionException("can't set key [" + key.getString() + "] to struct, struct is readonly");
	}

	@Override
	public Object setEL(Key key, Object value) {
		throw eng().getExceptionUtil().createPageRuntimeException(eng().getExceptionUtil()
				.createApplicationException("can't set key [" + key.getString() + "] to struct, struct is readonly"));
	}

	@Override
	public int size() {
		return _info().size();
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpData dd = _info().toDumpData(pageContext, maxlevel, dp);
		if (dd instanceof DumpTable) {
			DumpTable dt = ((DumpTable) dd);
			dt.setTitle("Struct (Image)");
			try {
				String format = ImageUtil.getOneWriterFormatName("png", "jpeg");
				dt.setComment("<img style=\"margin:5px\" src=\"data:" + ImageUtil.getMimeTypeFromFormat(format)
						+ ";base64," + getBase64String(format) + "\">");
			} catch (Exception e) {
			}

		}

		return dd;
	}

	@Override
	public String castToString() throws PageException {
		try {
			String format = ImageUtil.getOneWriterFormatName("png", "jpeg");
			return "<img src=\"data:" + ImageUtil.getMimeTypeFromFormat(format) + ";base64," + getBase64String(format)
					+ "\">";
		}

		catch (IOException e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	@Override
	public String castToString(String defaultValue) {
		try {
			return castToString();
		} catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return _info().keyIterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return _info().keysAsStringIterator();
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return _info().entryIterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		return _info().valueIterator();
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return info().castToBooleanValue();
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		try {
			return info().castToBoolean(defaultValue);
		} catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return info().castToDateTime();
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		try {
			return info().castToDateTime(defaultValue);
		} catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return info().castToDoubleValue();
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		try {
			return info().castToDoubleValue(defaultValue);
		} catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public int compareTo(String str) throws PageException {
		return info().compareTo(str);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return info().compareTo(b);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return info().compareTo(d);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return info().compareTo(dt);
	}

	public static int toInterpolation(String strInterpolation) throws PageException {
		if (eng().getStringUtil().isEmpty(strInterpolation))
			throw CFMLEngineFactory.getInstance().getExceptionUtil()
					.createExpressionException("interpolation definition is empty");
		strInterpolation = strInterpolation.trim().toLowerCase();

		if ("automatic".equals(strInterpolation))
			return IP_AUTOMATIC;
		else if ("ultraQuality".equals(strInterpolation))
			return IP_HIGHESTQUALITY;
		else if ("quality".equals(strInterpolation))
			return IP_MEDIUMQUALITY;
		else if ("balanced".equals(strInterpolation))
			return IPC_BILINEAR;
		else if ("speed".equals(strInterpolation))
			return IP_HIGHESTPERFORMANCE;

		else if ("highestquality".equals(strInterpolation))
			return IP_HIGHESTQUALITY;
		else if ("highquality".equals(strInterpolation))
			return IP_HIGHQUALITY;
		else if ("mediumquality".equals(strInterpolation))
			return IP_MEDIUMQUALITY;
		else if ("highestperformance".equals(strInterpolation))
			return IP_HIGHESTPERFORMANCE;
		else if ("highperformance".equals(strInterpolation))
			return IP_HIGHPERFORMANCE;
		else if ("mediumperformance".equals(strInterpolation))
			return IP_MEDIUMPERFORMANCE;
		else if ("nearest".equals(strInterpolation))
			return IPC_NEAREST;
		else if ("bilinear".equals(strInterpolation))
			return IPC_BILINEAR;
		else if ("bicubic".equals(strInterpolation))
			return IPC_BICUBIC;
		else if ("bessel".equals(strInterpolation))
			return IP_BESSEL;
		else if ("blackman".equals(strInterpolation))
			return IP_BLACKMAN;
		else if ("hamming".equals(strInterpolation))
			return IP_HAMMING;
		else if ("hanning".equals(strInterpolation))
			return IP_HANNING;
		else if ("hermite".equals(strInterpolation))
			return IP_HERMITE;
		else if ("lanczos".equals(strInterpolation))
			return IP_LANCZOS;
		else if ("mitchell".equals(strInterpolation))
			return IP_MITCHELL;
		else if ("quadratic".equals(strInterpolation))
			return IP_QUADRATIC;

		throw CFMLEngineFactory.getInstance().getExceptionUtil()
				.createExpressionException("interpolation definition [" + strInterpolation + "] is invalid");
	}

	private Scalr.Method toMethod(int ip) {
		if (IP_AUTOMATIC == ip)
			return Scalr.Method.AUTOMATIC;

		else if (IP_HIGHESTQUALITY == ip)
			return Scalr.Method.ULTRA_QUALITY;
		else if (IP_HIGHQUALITY == ip)
			return Scalr.Method.ULTRA_QUALITY;
		else if (IP_MEDIUMQUALITY == ip)
			return Scalr.Method.QUALITY;
		else if (IP_MEDIUMPERFORMANCE == ip)
			return Scalr.Method.QUALITY;
		else if (IP_HIGHPERFORMANCE == ip)
			return Scalr.Method.SPEED;
		else if (IP_HIGHESTPERFORMANCE == ip)
			return Scalr.Method.SPEED;

		else if (IPC_NEAREST == ip)
			return Scalr.Method.SPEED;
		else if (IPC_BILINEAR == ip)
			return Scalr.Method.BALANCED;
		else if (IPC_BICUBIC == ip)
			return Scalr.Method.QUALITY;

		return null;
	}

	/**
	 * @return the source
	 */
	public Resource getSource() {
		return source;
	}

	@Override
	public boolean containsValue(Object value) {
		try {
			return info().containsValue(value);
		} catch (PageException e) {
			return false;
		}
	}

	@Override
	public java.util.Collection values() {
		try {
			return info().values();
		} catch (PageException e) {
			throw eng().getExceptionUtil().createPageRuntimeException(e);
		}
	}

	/**
	 * This method returns true if the specified image has transparent pixels
	 * 
	 * @param image
	 * @return
	 */
	public static boolean hasAlpha(java.awt.Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			BufferedImage bimage = (BufferedImage) image;
			return bimage.getColorModel().hasAlpha();
		}

		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}

		// Get the image's color model
		ColorModel cm = pg.getColorModel();
		return cm.hasAlpha();
	}

	// This method returns a buffered image with the contents of an image
	public static BufferedImage toBufferedImage(java.awt.Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels; for this method's
		boolean hasAlpha = hasAlpha(image);

		// Create a buffered image with a format that's compatible with the screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha) {
				transparency = Transparency.BITMASK;
			}

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}

		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}

	@Override
	public int getType() {
		if (_info() instanceof StructSupport)
			return ((StructSupport) _info()).getType();
		return Struct.TYPE_REGULAR;
	}

	private static CFMLEngine eng() {
		if (_eng == null)
			_eng = CFMLEngineFactory.getInstance();
		return _eng;
	}

	public void setFormat(String format) {
		this.format = format;
	}

}
