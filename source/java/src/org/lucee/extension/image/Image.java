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

import java.awt.AWTException;
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
import java.awt.Point;
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
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.PackedColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.media.jai.BorderExtender;
import javax.media.jai.BorderExtenderConstant;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.operator.ShearDir;
import javax.media.jai.operator.TransposeType;
import javax.swing.ImageIcon;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.common.IImageMetadata.IImageMetadataItem;
import org.apache.commons.imaging.common.ImageMetadata.Item;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegPhotoshopMetadata;
import org.imgscalr.Scalr;
import org.lucee.extension.image.filter.QuantizeFilter;
import org.lucee.extension.image.font.FontUtil;
import org.lucee.extension.image.functions.ImageGetEXIFMetadata;
import org.lucee.extension.image.gif.GifEncoder;
import org.lucee.extension.image.jpg.JpegReader;
import org.lucee.extension.image.util.ArrayUtil;
import org.lucee.extension.image.util.CommonUtil;
import org.lucee.extension.image.util.CommonUtil.Coll;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
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

	public static final int BORDER_TYPE_CONSTANT = -1;

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

	private static final String FORMAT = "javax_imageio_1.0";

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

	private Struct sctInfo;

	private float alpha = 1;

	private Composite composite;
	private RefInteger jpegColorType;

	private static CFMLEngine _eng;
	private static Object sync = new Object();

	static {
		ImageIO.scanForPlugins();
	}

	public Image(byte[] binary) throws IOException {
		this(binary, null);
	}

	public Image(byte[] binary, String format) throws IOException {
		if (eng().getStringUtil().isEmpty(format)) format = ImageUtil.getFormat(binary, null);
		this.format = format;
		jpegColorType = CFMLEngineFactory.getInstance().getCreationUtil().createRefInteger(0);
		_image = ImageUtil.toBufferedImage(binary, format, jpegColorType);
		if (_image == null) throw new IOException("can not read in image");
	}

	public Image(Resource res) throws IOException {
		this(res, null);
	}

	public Image(Resource res, String format) throws IOException {
		if (eng().getStringUtil().isEmpty(format)) format = ImageUtil.getFormat(res);
		this.format = format;
		jpegColorType = CFMLEngineFactory.getInstance().getCreationUtil().createRefInteger(0);
		_image = ImageUtil.toBufferedImage(res, format, jpegColorType);
		this.source = res;
		if (_image == null) throw new IOException("can not read in file " + res);
	}

	public Image(BufferedImage image) {
		this._image = image;
		this.format = null;
		// TODO find out jpeg type
	}

	public Image(String b64str) throws IOException {
		this(b64str, null);
	}

	public Image(String b64str, String format) throws IOException {

		// load binary from base64 string and get format
		StringBuilder mimetype = new StringBuilder();
		byte[] binary = ImageUtil.readBase64(b64str, mimetype);
		if (eng().getStringUtil().isEmpty(format) && !eng().getStringUtil().isEmpty(mimetype.toString())) {
			format = ImageUtil.getFormatFromMimeType(mimetype.toString());
		}
		if (eng().getStringUtil().isEmpty(format)) format = ImageUtil.getFormat(binary, null);
		this.format = format;
		jpegColorType = CFMLEngineFactory.getInstance().getCreationUtil().createRefInteger(0);
		_image = ImageUtil.toBufferedImage(binary, format, jpegColorType);
		if (_image == null) throw new IOException("can not read in image");
	}

	public Image(int width, int height, int imageType, Color canvasColor) throws PageException {
		_image = new BufferedImage(width, height, imageType);
		if (canvasColor != null) {

			setBackground(canvasColor);
			clearRect(0, 0, width, height);
		}
		this.format = null;
	}

	public Image() {
		this.format = null;
	}

	/**
	 * add a border to image
	 * 
	 * @param thickness
	 * @param color
	 * @param borderType
	 */
	public void addBorder(int thickness, Color color, int borderType) throws PageException {
		ColorModel cm = image().getColorModel();
		if (((cm instanceof IndexColorModel)) && (cm.hasAlpha()) && (!cm.isAlphaPremultiplied())) {
			image(paletteToARGB(image()));
			cm = image().getColorModel();
		}

		BufferedImage alpha = null;
		if ((cm.getNumComponents() > 3) && (cm.hasAlpha())) {
			alpha = getAlpha(image());
			image(removeAlpha(image()));
		}
		if (alpha != null) {
			ParameterBlock params1 = new ParameterBlock();
			params1.addSource(alpha);

			params1.add(thickness); // left
			params1.add(thickness); // right
			params1.add(thickness); // top
			params1.add(thickness); // bottom
			params1.add(new BorderExtenderConstant(new double[] { 255D }));

			RenderingHints hints = new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			hints.add(new RenderingHints(JAI.KEY_REPLACE_INDEX_COLOR_MODEL, Boolean.TRUE));
			alpha = JAI.create("border", params1, hints).getAsBufferedImage();
		}

		ParameterBlock params = new ParameterBlock();
		params.addSource(image());
		params.add(thickness); // left
		params.add(thickness); // right
		params.add(thickness); // top
		params.add(thickness); // bottom
		params.add(toBorderExtender(borderType, color));

		image(JAI.create("border", params).getAsBufferedImage());

		if (alpha != null) {
			image(addAlpha(image(), alpha, thickness, thickness));
		}
	}

	private Object toBorderExtender(int borderType, Color color) {
		if (borderType == Image.BORDER_TYPE_CONSTANT) {
			double[] colorArray = { color.getRed(), color.getGreen(), color.getBlue() };
			return new BorderExtenderConstant(colorArray);
		}
		return BorderExtender.createInstance(borderType);
	}

	public void blur(int blurFactor) throws PageException {
		ParameterBlock params = new ParameterBlock();
		params.addSource(image());
		params.add(blurFactor);
		RenderingHints hint = new RenderingHints(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(1));
		image(JAI.create("boxfilter", params, hint).getAsBufferedImage());
	}

	public void clearRect(int x, int y, int width, int height) throws PageException {
		getGraphics().clearRect(x, y, width, height);
	}

	public Struct info() throws PageException {
		if (sctInfo != null) return sctInfo;

		Struct sctInfo = eng().getCreationUtil().createStruct(), sct;
		ImageMetaDrew.addInfo(format, source, sctInfo);
		sctInfo = ImageGetEXIFMetadata.flatten(sctInfo);

		sctInfo.setEL("height", Double.valueOf(getHeight()));
		sctInfo.setEL("width", Double.valueOf(getWidth()));
		sctInfo.setEL("source", source == null ? "" : source.getAbsolutePath());
		if (jpegColorType != null && jpegColorType.toInteger() > 0) {
			sctInfo.setEL("jpeg_color_type", JpegReader.toColorType(jpegColorType.toInteger(), ""));
		}
		// sct.setEL("mime_type",getMimeType());

		ColorModel cm = image().getColorModel();
		sct = eng().getCreationUtil().createStruct();
		sctInfo.setEL("colormodel", sct);

		sct.setEL("alpha_channel_support", eng().getCastUtil().toBoolean(cm.hasAlpha()));
		sct.setEL("alpha_premultiplied", eng().getCastUtil().toBoolean(cm.isAlphaPremultiplied()));
		sct.setEL("transparency", toStringTransparency(cm.getTransparency()));
		sct.setEL("pixel_size", eng().getCastUtil().toDouble(cm.getPixelSize()));
		sct.setEL("num_components", eng().getCastUtil().toDouble(cm.getNumComponents()));
		sct.setEL("num_color_components", eng().getCastUtil().toDouble(cm.getNumColorComponents()));
		sct.setEL("colorspace", toStringColorSpace(cm.getColorSpace()));

		// bits_component
		int[] bitspercomponent = cm.getComponentSize();
		Array arr = eng().getCreationUtil().createArray();
		Double value;
		for (int i = 0; i < bitspercomponent.length; i++) {
			sct.setEL("bits_component_" + (i + 1), value = new Double(bitspercomponent[i]));
			arr.appendEL(value);
		}
		sct.setEL("bits_component", arr);

		// colormodel_type
		if (cm instanceof ComponentColorModel) sct.setEL("colormodel_type", "ComponentColorModel");
		else if (cm instanceof IndexColorModel) sct.setEL("colormodel_type", "IndexColorModel");
		else if (cm instanceof PackedColorModel) sct.setEL("colormodel_type", "PackedColorModel");
		else sct.setEL("colormodel_type", eng().getListUtil().last(cm.getClass().getName(), ".", true));

		getMetaData(sctInfo);
		// Metadata.addInfo(format,source,sctInfo);
		Metadata.addExifInfo(format, source, sctInfo);
		this.sctInfo = sctInfo;
		return sctInfo;
	}

	public IIOMetadata getMetaData(Struct parent) {
		InputStream is = null;
		javax.imageio.stream.ImageInputStreamImpl iis = null;
		try {

			if (source instanceof File) {
				iis = new FileImageInputStream((File) source);
			}
			else if (source == null) iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(getImageBytes(format, true)));
			else iis = new MemoryCacheImageInputStream(is = source.getInputStream());

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
		}
		catch (Exception e) {}
		finally {
			ImageUtil.closeEL(iis);
			eng().getIOUtil().closeSilent(is);
		}
		return null;
	}

	public Struct getIPTCMetadata() throws PageException {
		IImageMetadata md;
		Struct rtn = eng().getCreationUtil().createStruct();
		try {
			if (source instanceof File) md = Imaging.getMetadata((File) source);
			else md = Imaging.getMetadata(getImageBytes(format, true));

			// not jpeg
			if (!(md instanceof JpegImageMetadata)) return rtn;

			// fill to struct
			Key KEYWORDS = eng().getCreationUtil().createKey("Keywords");
			Key SUBJECT_REFERENCE = eng().getCreationUtil().createKey("Subject Reference");

			JpegImageMetadata jmd = (JpegImageMetadata) md;
			JpegPhotoshopMetadata jpmd = jmd.getPhotoshop(); // selects IPTC metadata
			if (jpmd == null) return rtn;
			Iterator<? extends IImageMetadataItem> it = jpmd.getItems().iterator();
			IImageMetadataItem item;
			Item i = null;
			Collection.Key k;
			Object v;
			Array arr;
			while (it.hasNext()) {
				item = it.next();
				if (item instanceof Item) {
					i = (Item) item;
					k = eng().getCreationUtil().createKey(i.getKeyword());
					v = rtn.get(k, null);
					if (v != null) {
						if (KEYWORDS.equals(k)) {
							rtn.set(k, v + ";" + i.getText());
						}
						else if (SUBJECT_REFERENCE.equals(k)) {
							rtn.set(k, v + " " + i.getText());
						}
						else if (v instanceof Array) {
							arr = (Array) v;
							arr.append(i.getText());
						}
						else {
							arr = eng().getCreationUtil().createArray();
							arr.append(v);
							arr.append(i.getText());
							rtn.set(k, arr);
						}
					}
					else rtn.set(k, i.getText());
				}
			}
		}
		catch (Exception e) {
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
		}
		else {
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
		if (child instanceof Struct) return (Struct) child;
		Struct sct = eng().getCreationUtil().createStruct();
		parent.setEL(name, sct);
		return sct;
	}

	public void sharpen(float gain) throws PageException {
		ParameterBlock params = new ParameterBlock();
		params.addSource(image());
		params.add((Object) null);
		params.add(new Float(gain));
		image(JAI.create("unsharpmask", params).getAsBufferedImage());
	}

	public void setTranparency(float percent) throws PageException {
		if (percent == -1) return;
		tranparency = percent;
		AlphaComposite rule = AlphaComposite.getInstance(3, 1.0F - (percent / 100.0F));
		getGraphics().setComposite(rule);
	}

	public void invert() throws PageException {
		ParameterBlock params = new ParameterBlock();
		params.addSource(image());
		image(JAI.create("invert", params).getAsBufferedImage());
	}

	public Image copy(float x, float y, float width, float height) throws PageException {
		ParameterBlock params = new ParameterBlock();
		params.addSource(image());
		params.add(x);
		params.add(y);
		params.add(width);
		params.add(height);
		// image(JAI.create("crop", params).getAsBufferedImage());
		return new Image(JAI.create("crop", params).getAsBufferedImage());
	}

	public Image copy(float x, float y, float width, float height, float dx, float dy) throws PageException {
		Image img = copy(x, y, width, height);
		img.getGraphics().copyArea((int) x, (int) y, (int) width, (int) height, (int) (dx - x), (int) (dy - y));
		return img;
	}

	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle, boolean filled) throws PageException {
		if (filled) getGraphics().fillArc(x, y, width, height, startAngle, arcAngle);
		else getGraphics().drawArc(x, y, width, height, startAngle, arcAngle);
	}

	public void draw3DRect(int x, int y, int width, int height, boolean raised, boolean filled) throws PageException {
		if (filled) getGraphics().fill3DRect(x, y, width + 1, height + 1, raised);
		else getGraphics().draw3DRect(x, y, width, height, raised);
	}

	public void drawCubicCurve(double ctrlx1, double ctrly1, double ctrlx2, double ctrly2, double x1, double y1, double x2, double y2) throws PageException {
		CubicCurve2D curve = new CubicCurve2D.Double(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2);
		getGraphics().draw(curve);
	}

	public void drawPoint(int x, int y) throws PageException {
		drawLine(x, y, x + 1, y);
	}

	public void drawQuadraticCurve(double x1, double y1, double ctrlx, double ctrly, double x2, double y2) throws PageException {
		QuadCurve2D curve = new QuadCurve2D.Double(x1, y1, ctrlx, ctrly, x2, y2);
		getGraphics().draw(curve);
	}

	public void drawRect(int x, int y, int width, int height, boolean filled) throws PageException {
		if (filled) getGraphics().fillRect(x, y, width + 1, height + 1);
		else getGraphics().drawRect(x, y, width, height);
	}

	public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight, boolean filled) throws PageException {
		if (filled) getGraphics().fillRoundRect(x, y, width + 1, height + 1, arcWidth, arcHeight);
		else getGraphics().drawRoundRect(x, y, width, height, arcWidth, arcHeight);
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
			if (filled) getGraphics().fillPolygon(xcoords, ycoords, xcoords.length);
			else getGraphics().drawPolygon(xcoords, ycoords, xcoords.length);
		}
		else {
			getGraphics().drawPolyline(xcoords, ycoords, xcoords.length);
		}
	}

	public void drawOval(int x, int y, int width, int height, boolean filled) throws PageException {
		if (filled) getGraphics().fillOval(x, y, width, height);
		else getGraphics().drawOval(x, y, width, height);
	}

	public void drawString(String text, int x, int y, Struct attr) throws PageException {

		if (attr != null && attr.size() > 0) {

			// font
			String font = eng().getCastUtil().toString(attr.get("font", "")).toLowerCase().trim();
			if (!eng().getStringUtil().isEmpty(font)) {
				font = FontUtil.getFont(font).getFontName();
			}
			else font = "Serif";

			// alpha
			// float alpha=eng().getCastUtil().toFloatValue(attr.get("alpha",null),1F);

			// size
			int size = eng().getCastUtil().toIntValue(attr.get("size", Integer.valueOf(10)));

			// style
			int style = Font.PLAIN;
			String strStyle = eng().getCastUtil().toString(attr.get("style", "")).toLowerCase();
			strStyle = CommonUtil.removeWhiteSpace(strStyle);
			if (!eng().getStringUtil().isEmpty(strStyle)) {
				if ("plain".equals(strStyle)) style = Font.PLAIN;
				else if ("bold".equals(strStyle)) style = Font.BOLD;
				else if ("italic".equals(strStyle)) style = Font.ITALIC;
				else if ("bolditalic".equals(strStyle)) style = Font.BOLD + Font.ITALIC;
				else if ("bold,italic".equals(strStyle)) style = Font.BOLD + Font.ITALIC;
				else if ("italicbold".equals(strStyle)) style = Font.BOLD + Font.ITALIC;
				else if ("italic,bold".equals(strStyle)) style = Font.BOLD + Font.ITALIC;
				else throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException(
						"key style of argument attributeCollection has an invalid value [" + strStyle + "], valid values are [plain,bold,italic,bolditalic]");
			}

			// strikethrough
			boolean strikethrough = eng().getCastUtil().toBooleanValue(attr.get("strikethrough", Boolean.FALSE));

			// underline
			boolean underline = eng().getCastUtil().toBooleanValue(attr.get("underline", Boolean.FALSE));

			AttributedString as = new AttributedString(text);
			as.addAttribute(TextAttribute.FONT, new Font(font, style, size));
			if (strikethrough) as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
			if (underline) as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			Graphics2D g = getGraphics();
			// if(alpha!=1D) setAlpha(g,alpha);

			g.drawString(as.getIterator(), x, y);
		}
		else getGraphics().drawString(text, x, y);

	}

	/*
	 * private void setAlpha(Graphics2D graphics,float alpha) { //Composite originalComposite =
	 * graphics.getComposite();
	 * 
	 * AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	 * 
	 * graphics.setComposite(alphaComposite); //graphics.setComposite(originalComposite); }
	 */

	public void setDrawingStroke(Struct attr) throws PageException {

		// empty
		if (attr == null || attr.size() == 0) {
			setDrawingStroke(new BasicStroke());
			return;
		}

		// width
		float width = eng().getCastUtil().toFloatValue(attr.get("width", new Float(1F)));
		if (width < 0) throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("key [width] should be a none negativ number");

		// endcaps
		String strEndcaps = eng().getCastUtil().toString(attr.get("endcaps", "square"));
		strEndcaps = strEndcaps.trim().toLowerCase();
		int endcaps;
		if ("square".equals(strEndcaps)) endcaps = BasicStroke.CAP_SQUARE;
		else if ("butt".equals(strEndcaps)) endcaps = BasicStroke.CAP_BUTT;
		else if ("round".equals(strEndcaps)) endcaps = BasicStroke.CAP_ROUND;
		else throw CFMLEngineFactory.getInstance().getExceptionUtil()
				.createExpressionException("key [endcaps] has an invalid value [" + strEndcaps + "], valid values are [square,round,butt]");

		// linejoins
		String strLinejoins = eng().getCastUtil().toString(attr.get("linejoins", "miter"));
		strLinejoins = strLinejoins.trim().toLowerCase();
		int linejoins;
		if ("bevel".equals(strLinejoins)) linejoins = BasicStroke.JOIN_BEVEL;
		else if ("miter".equals(strLinejoins)) linejoins = BasicStroke.JOIN_MITER;
		else if ("round".equals(strLinejoins)) linejoins = BasicStroke.JOIN_ROUND;
		else throw CFMLEngineFactory.getInstance().getExceptionUtil()
				.createExpressionException("key [linejoins] has an invalid value [" + strLinejoins + "], valid values are [bevel,miter,round]");

		// miterlimit
		float miterlimit = 10.0F;
		if (linejoins == BasicStroke.JOIN_MITER) {
			miterlimit = eng().getCastUtil().toFloatValue(attr.get("miterlimit", new Float(10F)));
			if (miterlimit < 1F) throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("key [miterlimit] should be greater or equal to 1");
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

	public void setDrawingStroke(float width, int endcaps, int linejoins, float miterlimit, float[] dash, float dash_phase) throws PageException {
		setDrawingStroke(new BasicStroke(width, endcaps, linejoins, miterlimit, dash, dash_phase));
	}

	public void setDrawingStroke(Stroke stroke) throws PageException {
		if (stroke == null) return;
		this.stroke = stroke;
		getGraphics().setStroke(stroke);
	}

	public void flip(TransposeType transpose) throws PageException {
		ParameterBlock params = new ParameterBlock();
		params.addSource(image());
		params.add(transpose);
		image(JAI.create("transpose", params).getAsBufferedImage());
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
		ParameterBlock params = new ParameterBlock();
		params.addSource(image());
		params.addSource(topImage.image());
		image(JAI.create("overlay", params).getAsBufferedImage());
	}

	public void paste(Image topImage, int x, int y) throws PageException {
		RenderingHints interp = new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		BorderExtender extender = BorderExtender.createInstance(1);
		Graphics2D g = getGraphics();
		g.addRenderingHints(new RenderingHints(JAI.KEY_BORDER_EXTENDER, extender));
		g.drawImage(topImage.image(), (new AffineTransformOp(AffineTransform.getTranslateInstance(x, y), interp)), 0, 0);

	}

	public void setXorMode(Color color) throws PageException {
		if (color == null) return;
		xmColor = color;
		getGraphics().setXORMode(color);
	}

	public void translate(int xtrans, int ytrans, Object interpolation) throws PageException {

		RenderingHints hints = new RenderingHints(RenderingHints.KEY_INTERPOLATION, interpolation);
		if (interpolation != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR) {
			hints.add(new RenderingHints(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(1)));
		}

		ParameterBlock pb = new ParameterBlock();
		pb.addSource(image());
		BufferedImage img = JAI.create("translate", pb).getAsBufferedImage();
		Graphics2D graphics = img.createGraphics();
		graphics.clearRect(0, 0, img.getWidth(), img.getHeight());
		AffineTransform at = new AffineTransform();
		at.setToIdentity();
		graphics.drawImage(image(), new AffineTransformOp(at, hints), xtrans, ytrans);
		graphics.dispose();
		image(img);
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

	public void shear(float shear, ShearDir direction, Object interpolation) throws PageException {
		ParameterBlock params = new ParameterBlock();
		params.addSource(image());
		params.add(shear);
		params.add(direction);
		params.add(0.0F);
		params.add(0.0F);
		RenderingHints hints = null;

		if (interpolation == RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR) params.add(Interpolation.getInstance(0));
		else if (interpolation == RenderingHints.VALUE_INTERPOLATION_BILINEAR) {
			params.add(Interpolation.getInstance(1));
			BorderExtender extender = BorderExtender.createInstance(1);
			hints = new RenderingHints(JAI.KEY_BORDER_EXTENDER, extender);
		}
		else if (interpolation == RenderingHints.VALUE_INTERPOLATION_BICUBIC) {
			params.add(Interpolation.getInstance(2));
			BorderExtender extender = BorderExtender.createInstance(1);
			hints = new RenderingHints(JAI.KEY_BORDER_EXTENDER, extender);
		}
		// TODO
		Color bg = getGraphics().getBackground();
		params.add(new double[] { bg.getRed(), bg.getGreen(), bg.getBlue() });
		image(JAI.create("shear", params, hints).getAsBufferedImage());
	}

	public BufferedImage getBufferedImage() throws PageException {
		return image();
	}

	public BufferedImage image() throws PageException {
		if (_image == null) throw (CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("image is not initialized"));
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
			if (antiAlias != ANTI_ALIAS_NONE) setAntiAliasing(antiAlias == ANTI_ALIAS_ON);
			if (bgColor != null) setBackground(bgColor);
			if (fgColor != null) setColor(fgColor);
			if (alpha != 1) setAlpha(alpha);
			if (tranparency != -1) setTranparency(tranparency);
			if (xmColor != null) setXorMode(xmColor);
			if (stroke != null) setDrawingStroke(stroke);
		}
		return graphics;
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
		if (Transparency.OPAQUE == transparency) return "OPAQUE";
		if (Transparency.BITMASK == transparency) return "BITMASK";
		if (Transparency.TRANSLUCENT == transparency) return "TRANSLUCENT";
		return "Unknown type of transparency";
	}

	public String writeBase64(Resource destination, String format, boolean inHTMLFormat) throws PageException, IOException {
		// destination
		if (destination == null) {
			if (source != null) destination = source;
			else throw new IOException("missing destination file");
		}

		String content = getBase64String(format);
		if (inHTMLFormat) content = "data:image/" + format + ";base64," + content;
		eng().getIOUtil().write(destination, content, false, (Charset) null);
		return content;
	}

	public String getBase64String(String format) throws PageException {
		byte[] imageBytes = getImageBytes(format);
		return new String(eng().getCastUtil().toBase64(imageBytes));
	}

	public void writeOut(Resource destination, boolean overwrite, float quality) throws IOException, PageException {
		String format = ImageUtil.getFormatFromExtension(destination, null);
		if (format == null && destination != null) {
			format = ImageUtil.getFormat(destination);
		}
		if (format == null) throw new IOException("cannot detect Format for given image");
		writeOut(destination, format, overwrite, quality);
	}

	public void writeOut(Resource destination, final String format, boolean overwrite, float quality) throws IOException, PageException {
		if (destination == null) {
			if (source != null) destination = source;
			else throw new IOException("missing destination file");
		}

		if (destination.exists()) {
			if (!overwrite) throw new IOException("can't overwrite existing image");
		}

		// try to write with ImageIO
		OutputStream os = null;
		ImageOutputStream ios = null;
		try {
			os = destination.getOutputStream();
			ios = ImageIO.createImageOutputStream(os);
			_writeOut(ios, format, quality, false);
			return;
		}
		catch (IIOException iioe) {
			// TODO correct the bands in case a CMYK image is read in when creating the BufferedImage
			if (jpegColorType != null && jpegColorType.toInt() > 0 && (iioe.getMessage() + "").indexOf("Metadata components != number of destination bands") != -1) {
				ImageUtil.closeEL(ios);
				eng().getIOUtil().closeSilent(os);

				// as a workaround we convert first to a png and then we convert that png to jpeg
				File tmp = new File(Util.getTempDirectory(), "tmp-" + System.currentTimeMillis() + ".png");
				FileOutputStream fos = new FileOutputStream(tmp);
				try {
					writeOut(fos, "png", 1f, true);
					os = destination.getOutputStream();
					ios = ImageIO.createImageOutputStream(os);
					PageContext pc = CFMLEngineFactory.getInstance().getThreadPageContext();
					Image img = Image.createImage(pc, tmp, false, false, false, format);
					img._writeOut(ios, format, quality, false);
					return;
				}
				catch (Exception e) {}
				finally {
					if (!tmp.delete()) tmp.deleteOnExit();
				}
			}
		}
		catch (Exception e) {}
		finally {
			ImageUtil.closeEL(ios);
			eng().getIOUtil().closeSilent(os);
		}

		// try it with JAI
		BufferedImage bi = getBufferedImage();
		if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) {
			bi = ensureOpaque(bi);
		}
		JAIUtil.write(bi, destination, format.equalsIgnoreCase("jpg") ? "JPEG" : format);
	}

	public static void writeOutGif(BufferedImage src, OutputStream os) throws IOException {
		BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		QuantizeFilter filter = new QuantizeFilter();
		filter.setSerpentine(true);
		filter.setDither(true);
		// filter.setNumColors(8);
		filter.filter(src, dst);

		// image(Quantizer.quantize(image(), 8));
		try {
			GifEncoder enc = new GifEncoder(dst);
			enc.Write(os);
			os.flush();
		}
		catch (AWTException e) {
			throw new IOException(e.getMessage());
		}
	}

	public void writeOut(OutputStream os, String format, float quality, boolean closeStream) throws IOException, PageException {
		ImageOutputStream ios = ImageIO.createImageOutputStream(os);
		try {
			_writeOut(ios, format, quality, false);
		}
		finally {
			eng().getIOUtil().closeSilent(ios);
		}
	}

	private void _writeOut(ImageOutputStream ios, String format, float quality, boolean noMeta) throws IOException, PageException {
		if (quality < 0 || quality > 1) throw new IOException("quality has an invalid value [" + quality + "], value has to be between 0 and 1");
		if (eng().getStringUtil().isEmpty(format)) format = this.format;
		if (eng().getStringUtil().isEmpty(format)) throw new IOException("missing format");

		BufferedImage bi = image();

		// IIOMetadata meta = noMeta?null:metadata(format);
		IIOMetadata meta = noMeta ? null : getMetaData(null);

		ImageWriter writer = null;
		ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(bi);
		Iterator<ImageWriter> iter = ImageIO.getImageWriters(type, format);

		if (iter.hasNext()) {
			writer = iter.next();
		}
		if (writer == null) throw new IOException(
				"no writer for format [" + format + "] available, available writer formats are [" + eng().getListUtil().toList(ImageUtil.getWriterFormatNames(), ",") + "]");

		ImageWriteParam iwp = getImageWriteParam(bi, writer, quality, this.format, format);

		writer.setOutput(ios);

		if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) {
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
			this.format = format;
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

	private static void setCompressionModeEL(ImageWriteParam iwp, int mode) {
		try {
			iwp.setCompressionMode(mode);
		}
		catch (Exception e) {}
	}

	private static void setCompressionQualityEL(ImageWriteParam iwp, float quality) {
		try {
			iwp.setCompressionQuality(quality);
		}
		catch (Exception e) {}
	}

	/*
	 * public void convertX(String format) { this.format=format; }
	 */

	public void scaleToFit(String fitWidth, String fitHeight, String interpolation, double blurFactor) throws PageException {
		if (eng().getStringUtil().isEmpty(fitWidth) || eng().getStringUtil().isEmpty(fitHeight)) resize(fitWidth, fitHeight, interpolation, blurFactor);
		else {
			float width = eng().getCastUtil().toFloatValue(fitWidth) / getWidth();
			float height = eng().getCastUtil().toFloatValue(fitHeight) / getHeight();
			if (width < height) resize(fitWidth, "", interpolation, blurFactor);
			else resize("", fitHeight, interpolation, blurFactor);
		}
	}

	/**
	 * Convenience method that returns a scaled instance of the provided {@code BufferedImage}.
	 *
	 * @param img the original image to be scaled
	 * @param targetWidth the desired width of the scaled instance, in pixels
	 * @param targetHeight the desired height of the scaled instance, in pixels
	 * @param hint one of the rendering hints that corresponds to
	 *            {@code RenderingHints.KEY_INTERPOLATION} (e.g.
	 *            {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
	 *            {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
	 *            {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
	 * @param higherQuality if true, this method will use a multi-step scaling technique that provides
	 *            higher quality than the usual one-step technique (only useful in downscaling cases,
	 *            where {@code targetWidth} or {@code targetHeight} is smaller than the original
	 *            dimensions, and generally only when the {@code BILINEAR} hint is specified)
	 * @return a scaled version of the original {@code BufferedImage}
	 */
	private BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint, boolean higherQuality) {
		// functionality not supported in java 1.4
		int transparency = Transparency.OPAQUE;
		try {
			transparency = img.getTransparency();
		}
		catch (Exception e) {}
		int type = (transparency == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

		BufferedImage ret = img;
		int w, h;
		if (higherQuality) {
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = img.getWidth();
			h = img.getHeight();
		}
		else {
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
		}
		while (w != targetWidth || h != targetHeight);

		return ret;
	}

	public void resize(int scale, String interpolation, double blurFactor) throws PageException {
		if (blurFactor <= 0.0 || blurFactor > 10.0) throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("blurFactor must be between 0 and 10");

		BufferedImage bi = image();
		float width = bi.getWidth() / 100F * scale;
		// float height = getHeight() / 100F * scale;

		resize(bi, (int) width, -1, toInterpolation(interpolation), blurFactor);
	}

	public void resize(String strWidth, String strHeight, String interpolation, double blurFactor) throws PageException {
		if (eng().getStringUtil().isEmpty(strWidth, true) && eng().getStringUtil().isEmpty(strHeight, true))
			throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("you have to define width or height");
		if (blurFactor <= 0.0 || blurFactor > 10.0) throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("blurFactor must be between 0 and 10");
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
		if (interpolation == IPC_NEAREST) ip = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
		else if (interpolation == IPC_BICUBIC) ip = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
		else if (interpolation == IPC_BILINEAR) ip = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
		else throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("invalid interpoltion definition");

		BufferedImage dst = new BufferedImage(width, height, image().getType());
		Graphics2D graphics = dst.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, ip);
		graphics.drawImage(image(), 0, 0, width, height, null);
		graphics.dispose();
		image(dst);

	}

	private float resizeDimesion(String label, String strDimension, float originalDimension) throws PageException {
		if (eng().getStringUtil().isEmpty(strDimension, true)) return -1;
		strDimension = strDimension.trim();

		if (strDimension != null && strDimension.endsWith("%")) {
			float p = eng().getCastUtil().toFloatValue(strDimension.substring(0, (strDimension.length() - 1))) / 100.0F;
			return originalDimension * p;
		}
		float dimension = eng().getCastUtil().toFloatValue(strDimension);
		if (dimension <= 0F) throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException(label + " has to be a none negative number");
		return dimension;
	}

	public void resize(int width, int height, int interpolation, double blurFactor) throws PageException {
		resize(image(), width, height, interpolation, blurFactor);
	}

	private void resize(BufferedImage bi, int width, int height, int interpolation, double blurFactor) throws PageException {

		if (interpolation == IP_AUTOMATIC && blurFactor == 1) {
			try {
				Img img = new Img(bi);
				img.resize(width, height);
				image(img.getBufferedImage());
				return;
			}
			catch (Exception e) {}
		}

		ColorModel cm = bi.getColorModel();

		if (interpolation == IP_HIGHESTPERFORMANCE) {
			interpolation = IPC_BICUBIC;
		}

		if (cm.getColorSpace().getType() == ColorSpace.TYPE_GRAY && cm.getComponentSize()[0] == 8) {
			if (interpolation == IP_AUTOMATIC || interpolation == IP_HIGHESTQUALITY || interpolation == IP_HIGHPERFORMANCE || interpolation == IP_HIGHQUALITY
					|| interpolation == IP_MEDIUMPERFORMANCE || interpolation == IP_MEDIUMQUALITY) {
				interpolation = IPC_BICUBIC;
			}
			if (interpolation != IPC_BICUBIC && interpolation != IPC_BILINEAR && interpolation != IPC_NEAREST) {
				throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("invalid grayscale interpolation");
			}
		}

		Scalr.Method method = toMethod(interpolation);
		if (blurFactor == 1D && method != null) {
			if (width == -1) image(Scalr.resize(image(), method, Scalr.Mode.FIT_TO_HEIGHT, 1, height, new BufferedImageOp[0]));
			else if (height == -1) image(Scalr.resize(image(), method, Scalr.Mode.FIT_TO_WIDTH, width, 1, new BufferedImageOp[0]));
			else image(Scalr.resize(image(), method, Scalr.Mode.FIT_EXACT, width, height, new BufferedImageOp[0]));
		}
		else {
			int h = bi.getHeight();
			int w = bi.getWidth();
			if (height == -1) height = (int) Math.round(h * (1D / w * width));
			if (width == -1) width = (int) Math.round(w * (1D / h * height));

			if (interpolation <= IPC_MAX) {
				resizeImage(width, height, interpolation);
			}
			else {
				image(ImageResizer.resize(image(), width, height, interpolation, blurFactor));

			}
		}
	}

	/*
	 * private double toScale(int src, int dst) { double tmp = Math.round((int)
	 * ((eng().getCastUtil().toDoubleValue(dst) / eng().getCastUtil().toDoubleValue(src)) * 100D));
	 * return tmp / 100D; }
	 */

	public void rotate(float x, float y, float angle, int interpolation) throws PageException {
		if (x == -1) {
			x = Math.round(getWidth() / 2);
		}
		if (y == -1) {
			y = Math.round(getHeight() / 2);
		}

		angle = (float) Math.toRadians(angle);
		ColorModel cmSource = image().getColorModel();

		if (cmSource instanceof IndexColorModel && cmSource.hasAlpha() && !cmSource.isAlphaPremultiplied()) {
			image(paletteToARGB(image()));
			cmSource = image().getColorModel();
		}

		BufferedImage alpha = null;
		if (cmSource.hasAlpha() && !cmSource.isAlphaPremultiplied()) {
			alpha = getAlpha(image());
			image(removeAlpha(image()));
		}

		Interpolation interp = Interpolation.getInstance(0);
		if (INTERPOLATION_BICUBIC == interpolation) interp = Interpolation.getInstance(1);
		else if (INTERPOLATION_BILINEAR == interpolation) interp = Interpolation.getInstance(2);

		if (alpha != null) {
			ParameterBlock params = new ParameterBlock();
			params.addSource(alpha);
			params.add(x);
			params.add(y);
			params.add(angle);
			params.add(interp);
			params.add(new double[] { 0.0 });
			RenderingHints hints = new RenderingHints(RenderingHints.KEY_INTERPOLATION, (RenderingHints.VALUE_INTERPOLATION_BICUBIC));
			hints.add(new RenderingHints(JAI.KEY_BORDER_EXTENDER, new BorderExtenderConstant(new double[] { 255.0 })));
			hints.add(new RenderingHints(JAI.KEY_REPLACE_INDEX_COLOR_MODEL, Boolean.TRUE));
			alpha = JAI.create("rotate", params, hints).getAsBufferedImage();
		}

		ParameterBlock params = new ParameterBlock();
		params.addSource(image());
		params.add(x);
		params.add(y);
		params.add(angle);
		params.add(interp);
		params.add(new double[] { 0.0 });

		BorderExtender extender = new BorderExtenderConstant(new double[] { 0.0 });
		RenderingHints hints = new RenderingHints(JAI.KEY_BORDER_EXTENDER, extender);
		hints.add(new RenderingHints(JAI.KEY_REPLACE_INDEX_COLOR_MODEL, Boolean.TRUE));
		image(JAI.create("rotate", params, hints).getAsBufferedImage());
		if (alpha != null) image(addAlpha(image(), alpha, 0, 0));
	}

	private static BufferedImage paletteToARGB(BufferedImage src) {
		IndexColorModel icm = (IndexColorModel) src.getColorModel();
		int bands = icm.hasAlpha() ? 4 : 3;

		byte[][] data = new byte[bands][icm.getMapSize()];
		if (icm.hasAlpha()) icm.getAlphas(data[3]);
		icm.getReds(data[0]);
		icm.getGreens(data[1]);
		icm.getBlues(data[2]);
		LookupTableJAI rtable = new LookupTableJAI(data);
		return JAI.create("lookup", src, rtable).getAsBufferedImage();
	}

	private static BufferedImage getAlpha(BufferedImage src) {
		return JAI.create("bandselect", src, new int[] { 3 }).getAsBufferedImage();
	}

	private static BufferedImage removeAlpha(BufferedImage src) {
		return JAI.create("bandselect", src, new int[] { 0, 1, 2 }).getAsBufferedImage();
	}

	private static BufferedImage addAlpha(BufferedImage src, BufferedImage alpha, int x, int y) {
		int w = src.getWidth();
		int h = src.getHeight();
		BufferedImage bi = new BufferedImage(w, h, 2);
		WritableRaster wr = bi.getWritableTile(0, 0);
		WritableRaster wr3 = wr.createWritableChild(0, 0, w, h, 0, 0, new int[] { 0, 1, 2 });
		WritableRaster wr1 = wr.createWritableChild(0, 0, w, h, 0, 0, new int[] { 3 });
		wr3.setRect(src.getData());
		wr1.setRect(alpha.getData());
		bi.releaseWritableTile(0, 0);
		return bi;
	}

	public void _rotate(float x, float y, float angle, String interpolation) throws PageException {

		float radiansAngle = (float) Math.toRadians(angle);

		// rotation center
		float centerX = (float) getWidth() / 2;
		float centerY = (float) getHeight() / 2;

		ParameterBlock pb = new ParameterBlock();
		pb.addSource(image());
		pb.add(centerX);
		pb.add(centerY);
		pb.add(radiansAngle);
		pb.add(new javax.media.jai.InterpolationBicubic(10));

		// create a new, rotated image
		image(JAI.create("rotate", pb).getAsBufferedImage());

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
		if (obj instanceof Image) return (Image) obj;
		if (obj instanceof ObjectWrap) return toImage(pc, ((ObjectWrap) obj).getEmbededObject(), checkForVariables);

		if (obj instanceof BufferedImage) return new Image((BufferedImage) obj);

		// try to load from binary
		if (eng().getDecisionUtil().isBinary(obj)) {
			try {
				return new Image(eng().getCastUtil().toBinary(obj), null);
			}
			catch (IOException e) {
				throw eng().getCastUtil().toPageException(e);
			}
		}
		// try to load from String (base64)
		if (obj instanceof CharSequence) {
			String str = eng().getCastUtil().toString(obj);
			if (checkForVariables && pc != null) {
				Object o = CommonUtil.getVariableEL(pc, str, null);
				if (o != null) return toImage(pc, o, false);
			}
			try {
				return new Image(str);
			}
			catch (IOException e) {

				throw eng().getCastUtil().toPageException(e);
			}
		}

		throw eng().getExceptionUtil().createCasterException(obj, "Image");
	}

	public static Image toImage(PageContext pc, Object obj, boolean checkForVariables, Image defaultValue) {
		try {
			return toImage(pc, obj, checkForVariables);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public static boolean isImage(Object obj) {
		if (obj instanceof Image) return true;
		if (obj instanceof ObjectWrap) return isImage(((ObjectWrap) obj).getEmbededObject(""));
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
			if (!coll.memberChaining) return coll.bif.invoke(pc, pre(args));
			else coll.bif.invoke(pc, pre(args));
			return this;
		}

		// TODO handle this dyn
		/*
		 * if(methodName.equals("blur")) { new ImageBlur().invoke(pc, pre(args)); return this; }
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
		if (isImage(obj)) return true;
		return toImage(pc, obj, true, null) != null;
	}

	public static Image createImage(PageContext pc, Object obj, boolean check4Var, boolean clone, boolean checkAccess, String format) throws PageException {
		try {
			if ((obj instanceof Resource || obj instanceof File)) {
				Resource res = eng().getCastUtil().toResource(obj);
				if (checkAccess) pc.getConfig().getSecurityManager().checkFileLocation(res);
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
					}
					catch (Exception ee) {
						e = ee;
					}
				}

				// Base64
				try {
					return new Image(str, format);
				}
				catch (Exception ee) {
					if (e == null) e = ee;
				}

				// variable
				if (check4Var && eng().getDecisionUtil().isVariableName(str)) {
					try {
						return createImage(pc, pc.getVariable(str), false, clone, checkAccess, format);
					}
					catch (Exception ee) {
						throw e;
					}
				}

				throw e;
			}
			if (obj instanceof Image) {
				if (clone) return (Image) ((Image) obj).clone();
				return (Image) obj;
			}
			if (eng().getDecisionUtil().isBinary(obj)) return new Image(eng().getCastUtil().toBinary(obj), format);
			if (obj instanceof BufferedImage) return new Image(((BufferedImage) obj));
			if (obj instanceof java.awt.Image) return new Image(toBufferedImage((java.awt.Image) obj));

		}
		catch (Exception e) {
			throw eng().getCastUtil().toPageException(e);
		}
		throw eng().getExceptionUtil().createCasterException(obj, "Image");
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		try {
			return new Image(getImageBytes(null));
		}
		catch (Exception e) {
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

	public void crop(float x, float y, float width, float height) throws PageException {
		ParameterBlock params = new ParameterBlock();
		params.addSource(image());
		params.add(x);
		params.add(y);

		float w = getWidth();
		float h = getHeight();

		if (w < x + width) params.add(w - x);
		else params.add(width);

		if (h < y + height) params.add(h - y);
		else params.add(height);

		image(JAI.create("crop", params).getAsBufferedImage());
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

		ImageOutputStream ios = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ios = ImageIO.createImageOutputStream(baos);
			_writeOut(ios, format, 1, noMeta);
			return baos.toByteArray();
		}
		catch (Exception e) {
			// throw eng().getCastUtil().toPageException(e);
		}
		finally {
			CFMLEngineFactory.getInstance().getIOUtil().closeSilent(ios);
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			JAIUtil.write(getBufferedImage(), baos, format.equalsIgnoreCase("jpg") ? "JPEG" : format);
		}
		catch (IOException e) {
			throw eng().getCastUtil().toPageException(e);
		}
		return baos.toByteArray();
	}

	public void setColor(Color color) throws PageException {
		if (color == null) return;
		fgColor = color;
		getGraphics().setColor(color);
	}

	public void setAlpha(float alpha) throws PageException {
		this.alpha = alpha;
		Graphics2D g = getGraphics();

		Composite alphaComposite;
		if (composite == null) {
			if (alpha == 1) return;
			composite = g.getComposite();
		}
		if (alpha == 1) alphaComposite = composite;
		else alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);

		g.setComposite(alphaComposite);
		// graphics.setComposite(originalComposite);
	}

	public void setBackground(Color color) throws PageException {
		if (color == null) return;
		bgColor = color;
		getGraphics().setBackground(color);
	}

	public void setAntiAliasing(boolean antiAlias) throws PageException {
		this.antiAlias = antiAlias ? ANTI_ALIAS_ON : ANTI_ALIAS_OFF;
		Graphics2D graphics = getGraphics();
		if (antiAlias) {
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		else {
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}

	private Struct _info() {
		try {
			return info();
		}
		catch (PageException e) {
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
		throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("can't remove key [" + key.getString() + "] from struct, struct is readonly");
	}

	@Override
	public Object removeEL(Key key) {
		throw eng().getExceptionUtil()
				.createPageRuntimeException(eng().getExceptionUtil().createApplicationException("can't remove key [" + key.getString() + "] from struct, struct is readonly"));
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("can't set key [" + key.getString() + "] to struct, struct is readonly");
	}

	@Override
	public Object setEL(Key key, Object value) {
		throw eng().getExceptionUtil()
				.createPageRuntimeException(eng().getExceptionUtil().createApplicationException("can't set key [" + key.getString() + "] to struct, struct is readonly"));
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
				dt.setComment("<img style=\"margin:5px\" src=\"data:image/png;base64," + getBase64String("png") + "\">");
			}
			catch (PageException e) {}

		}

		return dd;
	}

	@Override
	public String castToString() throws PageException {
		return "<img src=\"data:image/png;base64," + getBase64String("png") + "\">";
	}

	@Override
	public String castToString(String defaultValue) {
		try {
			return castToString();
		}
		catch (PageException e) {
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
		}
		catch (PageException e) {
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
		}
		catch (PageException e) {
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
		}
		catch (PageException e) {
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
			throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("interpolation definition is empty");
		strInterpolation = strInterpolation.trim().toLowerCase();

		if ("automatic".equals(strInterpolation)) return IP_AUTOMATIC;
		else if ("ultraQuality".equals(strInterpolation)) return IP_HIGHESTQUALITY;
		else if ("quality".equals(strInterpolation)) return IP_MEDIUMQUALITY;
		else if ("balanced".equals(strInterpolation)) return IPC_BILINEAR;
		else if ("speed".equals(strInterpolation)) return IP_HIGHESTPERFORMANCE;

		else if ("highestquality".equals(strInterpolation)) return IP_HIGHESTQUALITY;
		else if ("highquality".equals(strInterpolation)) return IP_HIGHQUALITY;
		else if ("mediumquality".equals(strInterpolation)) return IP_MEDIUMQUALITY;
		else if ("highestperformance".equals(strInterpolation)) return IP_HIGHESTPERFORMANCE;
		else if ("highperformance".equals(strInterpolation)) return IP_HIGHPERFORMANCE;
		else if ("mediumperformance".equals(strInterpolation)) return IP_MEDIUMPERFORMANCE;
		else if ("nearest".equals(strInterpolation)) return IPC_NEAREST;
		else if ("bilinear".equals(strInterpolation)) return IPC_BILINEAR;
		else if ("bicubic".equals(strInterpolation)) return IPC_BICUBIC;
		else if ("bessel".equals(strInterpolation)) return IP_BESSEL;
		else if ("blackman".equals(strInterpolation)) return IP_BLACKMAN;
		else if ("hamming".equals(strInterpolation)) return IP_HAMMING;
		else if ("hanning".equals(strInterpolation)) return IP_HANNING;
		else if ("hermite".equals(strInterpolation)) return IP_HERMITE;
		else if ("lanczos".equals(strInterpolation)) return IP_LANCZOS;
		else if ("mitchell".equals(strInterpolation)) return IP_MITCHELL;
		else if ("quadratic".equals(strInterpolation)) return IP_QUADRATIC;

		throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("interpolation definition [" + strInterpolation + "] is invalid");
	}

	private Scalr.Method toMethod(int ip) {
		if (IP_AUTOMATIC == ip) return Scalr.Method.AUTOMATIC;

		else if (IP_HIGHESTQUALITY == ip) return Scalr.Method.ULTRA_QUALITY;
		else if (IP_HIGHQUALITY == ip) return Scalr.Method.ULTRA_QUALITY;
		else if (IP_MEDIUMQUALITY == ip) return Scalr.Method.QUALITY;
		else if (IP_MEDIUMPERFORMANCE == ip) return Scalr.Method.QUALITY;
		else if (IP_HIGHPERFORMANCE == ip) return Scalr.Method.SPEED;
		else if (IP_HIGHESTPERFORMANCE == ip) return Scalr.Method.SPEED;

		else if (IPC_NEAREST == ip) return Scalr.Method.SPEED;
		else if (IPC_BILINEAR == ip) return Scalr.Method.BALANCED;
		else if (IPC_BICUBIC == ip) return Scalr.Method.QUALITY;

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
		}
		catch (PageException e) {
			return false;
		}
	}

	@Override
	public java.util.Collection values() {
		try {
			return info().values();
		}
		catch (PageException e) {
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
		}
		catch (InterruptedException e) {}

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
		}
		catch (HeadlessException e) {
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
		if (_info() instanceof StructSupport) return ((StructSupport) _info()).getType();
		return Struct.TYPE_REGULAR;
	}

	private static CFMLEngine eng() {
		if (_eng == null) _eng = CFMLEngineFactory.getInstance();
		return _eng;
	}

}