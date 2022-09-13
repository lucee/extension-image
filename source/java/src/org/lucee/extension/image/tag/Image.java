package org.lucee.extension.image.tag;

import java.awt.Color;
import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.lucee.extension.image.ImageUtil;
import org.lucee.extension.image.MarpleCaptcha;
import org.lucee.extension.image.functions.ImageCaptcha;
import org.lucee.extension.image.util.CommonUtil;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

/**
 * Lets you resize and add labels to GIF and JPEG format images.
 *
 *
 *
 **/
public final class Image extends TagImpl {

	private static int ACTION_BORDER = 0;
	private static int ACTION_CAPTCHA = 1;
	private static int ACTION_CONVERT = 2;
	private static int ACTION_INFO = 3;
	private static int ACTION_READ = 4;
	private static int ACTION_RESIZE = 5;
	private static int ACTION_ROTATE = 6;
	private static int ACTION_WRITE = 7;
	private static int ACTION_WRITE_TO_BROWSER = 8;

	private int action = ACTION_READ;
	private String strAction = "read";
	private int angle = -1;
	private Color color = Color.BLACK;
	private Resource destination;
	private int difficulty = MarpleCaptcha.DIFFICULTY_LOW;
	private String[] fonts = new String[] { "arial" };
	private int fontsize = 24;
	private String format = "png";
	private String height;
	private String width;
	private boolean isbase64;
	private String name;
	private boolean overwrite;
	private float quality = .75F;
	private Object oSource;
	private org.lucee.extension.image.Image source;
	private String structName;
	private String text;
	private int thickness = 1;
	private String passthrough;
	private boolean base64;
	private boolean nometadata;
	private CFMLEngine eng;

	public Image() {
		this.eng = CFMLEngineFactory.getInstance();
	}

	@Override
	public void release() {
		super.release();
		action = ACTION_READ;
		strAction = "read";
		angle = -1;
		color = Color.BLACK;
		destination = null;
		difficulty = MarpleCaptcha.DIFFICULTY_LOW;
		fonts = new String[] { "arial" };
		fontsize = 24;
		format = "png";
		height = null;
		width = null;
		isbase64 = false;
		name = null;
		overwrite = false;
		quality = 0.75F;
		source = null;
		oSource = null;
		structName = null;
		text = null;
		thickness = 1;
		passthrough = null;
		base64 = false;
		nometadata = false;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(String strAction) throws PageException {
		this.strAction = strAction;
		strAction = strAction.trim().toLowerCase();
		if (eng.getStringUtil().isEmpty(strAction)) action = ACTION_READ;
		else if ("border".equals(strAction)) action = ACTION_BORDER;
		else if ("captcha".equals(strAction)) action = ACTION_CAPTCHA;
		else if ("convert".equals(strAction)) action = ACTION_CONVERT;
		else if ("info".equals(strAction)) action = ACTION_INFO;
		else if ("read".equals(strAction)) action = ACTION_READ;
		else if ("resize".equals(strAction)) action = ACTION_RESIZE;
		else if ("rotate".equals(strAction)) action = ACTION_ROTATE;
		else if ("write".equals(strAction)) action = ACTION_WRITE;
		else if ("writetobrowser".equals(strAction)) action = ACTION_WRITE_TO_BROWSER;
		else if ("write-to-browser".equals(strAction)) action = ACTION_WRITE_TO_BROWSER;
		else if ("write_to_browser".equals(strAction)) action = ACTION_WRITE_TO_BROWSER;
		else throw eng.getExceptionUtil().createApplicationException(
				"invalid action [" + this.strAction + "], " + "valid actions are [border,captcha,convert,info,read,resize,rotate,write,writeToBrowser]");
	}

	/**
	 * @param base64 the base64 to set
	 */
	public void setBase64(boolean base64) {
		this.base64 = base64;
	}

	public void setNometadata(boolean nometadata) {
		this.nometadata = nometadata;
	}

	/**
	 * @param angle the angle to set
	 */
	public void setAngle(double angle) {
		this.angle = (int) angle;
	}

	/**
	 * @param color the color to set
	 * @throws ExpressionException
	 */
	public void setColor(String strColor) throws PageException {
		this.color = eng.getCastUtil().toColor(strColor);
	}

	/**
	 * @param destination the destination to set
	 */
	public void setDestination(String destination) {
		this.destination = eng.getResourceUtil().toResourceNotExisting(pageContext, destination);
	}

	/**
	 * @param difficulty the difficulty to set
	 */
	public void setDifficulty(String strDifficulty) throws PageException {
		difficulty = ImageCaptcha.toDifficulty(strDifficulty);
	}

	/**
	 * @param fonts the fonts to set
	 * @throws PageException
	 */
	public void setFonts(Object oFonts) throws PageException {
		fonts = ImageCaptcha.toFonts(oFonts);
	}

	/**
	 * @param fontsize the fontsize to set
	 */
	public void setFontsize(double fontsize) {
		this.fontsize = ImageCaptcha.toFontSize(fontsize);
	}

	/**
	 * @param passthrough the passthrough to set
	 */
	public void setPassthrough(String passthrough) {
		this.passthrough = passthrough;
	}

	/**
	 * @param format the format to set
	 * @throws PageException
	 */
	public void setFormat(String format) throws PageException {
		format = format.trim().toLowerCase();
		if ("gif".equalsIgnoreCase(format)) this.format = "gif";
		else if ("jpg".equalsIgnoreCase(format)) this.format = "jpg";
		else if ("jpe".equalsIgnoreCase(format)) this.format = "jpg";
		else if ("jpeg".equalsIgnoreCase(format)) this.format = "jpg";
		else if ("png".equalsIgnoreCase(format)) this.format = "png";
		else if ("tiff".equalsIgnoreCase(format)) this.format = "tiff";
		else if ("bmp".equalsIgnoreCase(format)) this.format = "bmp";
		else throw eng.getExceptionUtil().createApplicationException("invalid format [" + format + "], " + "valid formats are [gif,jpg,png,tiff,bmp]");
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(String height) {
		this.height = height;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(String width) {
		this.width = width;
	}

	/**
	 * @param isbase64 the isbase64 to set
	 */
	public void setIsbase64(boolean isbase64) {
		this.isbase64 = isbase64;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param overwrite the overwrite to set
	 */
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	/**
	 * @param quality the quality to set
	 * @throws PageException
	 */
	public void setQuality(double quality) throws PageException {
		this.quality = (float) quality;
		if (quality < 0 || quality > 1)
			throw eng.getExceptionUtil().createApplicationException("quality (" + eng.getCastUtil().toString(quality) + ") has to be a value between 0 and 1");
	}

	/**
	 * @param source the source to set
	 * @throws PageException
	 */
	public void setSource(Object source) {
		this.oSource = source;
		// this.source=lucee.runtime.img.Image.createImage(pageContext, source, false, false);
	}

	/**
	 * @param structName the structName to set
	 */
	public void setStructname(String structName) {
		this.structName = structName;
	}

	/**
	 * @param structName the structName to set
	 */
	public void setResult(String structName) {
		this.structName = structName;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @param thickness the thickness to set
	 */
	public void setThickness(double thickness) {
		this.thickness = (int) thickness;
	}

	@Override
	public int doStartTag() throws JspException {
		try {
			if (this.oSource != null) {
				if (isbase64) this.source = new org.lucee.extension.image.Image(eng.getCastUtil().toString(oSource));
				else this.source = org.lucee.extension.image.Image.createImage(pageContext, oSource, false, false, true, null);
			}

			if (action == ACTION_BORDER) doActionBorder();
			else if (action == ACTION_CAPTCHA) doActionCaptcha();
			else if (action == ACTION_CONVERT) doActionConvert();
			else if (action == ACTION_INFO) doActionInfo();
			else if (action == ACTION_READ) doActionRead();
			else if (action == ACTION_RESIZE) doActionResize();
			else if (action == ACTION_ROTATE) doActionRotate();
			else if (action == ACTION_WRITE) doActionWrite();
			else if (action == ACTION_WRITE_TO_BROWSER) doActionWriteToBrowser();
		}
		catch (Throwable t) {
			throw eng.getCastUtil().toPageException(t);
		}
		return SKIP_BODY;
	}

	// Add a border to an image
	private void doActionBorder() throws PageException, IOException {
		required("source", source);

		source.addBorder(thickness, color, org.lucee.extension.image.Image.BORDER_TYPE_CONSTANT);
		write();

	}

	// Create a CAPTCHA image
	private void doActionCaptcha() throws PageException, IOException {
		required("height", height);
		required("width", width);
		required("text", text);

		boolean doRenderHtmlTag = (destination == null);

		String path = null;

		// create destination
		if (eng.getStringUtil().isEmpty(name)) path = touchDestination();

		MarpleCaptcha c = new MarpleCaptcha();
		source = new org.lucee.extension.image.Image(
				c.generate(text, eng.getCastUtil().toIntValue(width), eng.getCastUtil().toIntValue(height), fonts, true, Color.BLACK, fontsize, difficulty));

		// link destination
		if (doRenderHtmlTag) writeLink(path);

		// write out
		write();
	}

	private void writeLink(String path) throws IOException, PageException {
		StringBuilder add = new StringBuilder();
		if (passthrough != null) {
			add = add.append(' ').append(passthrough);
		}

		if (base64) {
			String b64 = source.getBase64String(format);
			pageContext.write("<img src=\"data:" + ImageUtil.getMimeTypeFromFormat(format) + ";base64," + b64 + "\" width=\"" + source.getWidth() + "\" height=\""
					+ source.getHeight() + "\"" + add + " />");
			return;
		}

		int w = eng.getCastUtil().toIntValue(width, -1);
		int h = eng.getCastUtil().toIntValue(height, -1);
		// we have a custom size
		if (w + h > 0) {
			if (w > 0) add.append(" width=\"").append(w).append('"');
			if (h > 0) add.append(" height=\"").append(h).append('"');
		}
		else {
			add.append(" width=\"").append(source.getWidth()).append('"').append(" height=\"").append(source.getHeight()).append('"');
		}

		pageContext.write("<img src=\"" + path + "\"" + add + " />");

	}

	private String touchDestination() throws IOException, PageException {
		if (destination == null) {
			String name = eng.getCreationUtil().createUUID() + "." + format;
			Resource folder = pageContext.getConfig().getTempDirectory().getRealResource("graph");
			if (!folder.exists()) folder.mkdirs();
			destination = folder.getRealResource(name);
			cleanOld(folder);

			// create path
			String cp = pageContext.getHttpServletRequest().getContextPath();
			if (eng.getStringUtil().isEmpty(cp)) cp = "";
			return cp + "/lucee/graph.cfm?img=" + name + "&type=" + (eng.getListUtil().last(ImageUtil.getMimeTypeFromFormat(format), "/", true).trim());
		}
		return CommonUtil.ContractPath(pageContext, destination.getAbsolutePath());
	}

	private static void cleanOld(Resource folder) {
		if (!folder.exists()) folder.mkdirs();
		else if (folder.isDirectory() && CommonUtil.getRealSize(folder, null) > (1024 * 1024)) {

			Resource[] children = folder.listResources();
			long maxAge = System.currentTimeMillis() - (1000 * 60);
			for (int i = 0; i < children.length; i++) {
				if (children[i].lastModified() < maxAge) children[i].delete();
			}
		}
	}

	// Convert an image file format
	private void doActionConvert() throws PageException, IOException {
		required("source", source);
		required("destination", destination);

		// source.convert(ImageUtil.getFormat(destination));
		write();
	}

	// Retrieve information about an image
	private void doActionInfo() throws PageException {
		required("source", source);
		required("structname", structName);

		pageContext.setVariable(structName, source.info());
	}

	private Struct doActionInfo(org.lucee.extension.image.Image source) throws PageException {
		return source.info();
	}

	// Read an image into memory
	private void doActionRead() throws PageException {
		required("source", source);
		required("name", name);

		pageContext.setVariable(name, source);
	}

	// Resize an image
	private void doActionResize() throws PageException, IOException {
		required("source", source);
		source.resize(width, height, "automatic", 1D);
		write();
	}

	// Rotate an image
	private void doActionRotate() throws PageException, IOException {
		required("source", source);
		required("angle", angle, -1);

		source.rotate(-1, -1, angle, org.lucee.extension.image.Image.INTERPOLATION_NONE);
		write();

	}

	// Write an image to a file
	private void doActionWrite() throws PageException, IOException {
		required("source", source);
		required("destination", destination);

		source.writeOut(destination, null, overwrite, quality, nometadata);
	}

	// Write an image to the browser
	private void doActionWriteToBrowser() throws IOException, PageException {
		required("source", source);

		String path = null;

		// create destination
		if (!base64 || !eng.getStringUtil().isEmpty(name)) {
			path = touchDestination();
			write();
		}
		// link destination
		if (eng.getStringUtil().isEmpty(name)) writeLink(path);
	}

	private void required(String label, Object value) throws PageException {
		if (value == null)
			throw eng.getExceptionUtil().createApplicationException("Missing attribute [" + label + "]. The action [" + strAction + "] requires the attribute [" + label + "].");
		// throw new ApplicationException("missing attribute ["+label+"], for the action ["+strAction+"]
		// this attribute is required but was not passed in");
	}

	private void required(String label, int value, int nullValue) throws PageException {
		if (value == nullValue)
			throw eng.getExceptionUtil().createApplicationException("Missing attribute [" + label + "]. The action [" + strAction + "] requires the attribute [" + label + "].");
		// throw new ApplicationException("missing attribute ["+label+"], for the action ["+strAction+"]
		// this attribute is required but was not passed in");
	}

	private void write() throws IOException, PageException {
		if (destination != null) {
			doActionWrite();
		}
		if (!eng.getStringUtil().isEmpty(name)) {
			required("source", source);
			pageContext.setVariable(name, source);// TODO ist das so gut
		}
		// if(writeToResponseWhenNoOtherTarget) doActionWriteToBrowser();
	}

	class Info {
		Struct struct;
		private org.lucee.extension.image.Image img;

		public Info(org.lucee.extension.image.Image img) {
			this.img = img;
		}

		/**
		 * @return the sct
		 * @throws PageException
		 */
		public Struct getStruct() throws PageException {
			if (struct == null) struct = doActionInfo(img);
			return struct;
		}
	}

	private int toDimension(String label, String dimension, Info info) throws PageException {
		if (eng.getStringUtil().isEmpty(dimension)) return -1;
		dimension = dimension.trim();
		// int value
		int i = eng.getCastUtil().toIntValue(dimension, -1);
		if (i > -1) return i;

		// percent value
		if (dimension.endsWith("%")) {
			float pro = eng.getCastUtil().toIntValue(dimension.substring(0, dimension.length() - 1).trim(), -1);
			if (pro < 0 || pro > 100)
				throw eng.getExceptionUtil().createExpressionException("attribute [" + label + "] value has an invalid percent definition [" + dimension + "]");
			pro /= 100F;
			return (int) (eng.getCastUtil().toFloatValue(info.getStruct().get(label)) * pro);
		}
		throw eng.getExceptionUtil().createExpressionException("attribute [" + label + "] value has an invalid definition [" + dimension + "]");

	}

}