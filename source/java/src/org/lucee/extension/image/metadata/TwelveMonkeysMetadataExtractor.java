package org.lucee.extension.image.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import com.twelvemonkeys.imageio.metadata.Directory;
import com.twelvemonkeys.imageio.metadata.Entry;
import com.twelvemonkeys.imageio.metadata.iptc.IPTC;
import com.twelvemonkeys.imageio.metadata.iptc.IPTCReader;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEG;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEGSegment;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEGSegmentUtil;
import com.twelvemonkeys.imageio.metadata.psd.PSD;
import com.twelvemonkeys.imageio.metadata.psd.PSDReader;
import com.twelvemonkeys.imageio.stream.ByteArrayImageInputStream;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;

public class TwelveMonkeysMetadataExtractor implements MetadataExtractor {

	@Override
	public Struct extractIPTC(File source, String format) throws Exception {
		InputStream is = null;
		try {
			is = new FileInputStream(source);
			return extractIPTCFromStream(is);
		} finally {
			if (is != null) {
				try { is.close(); } catch (Exception e) {}
			}
		}
	}

	@Override
	public Struct extractIPTC(Resource source, String format) throws Exception {
		InputStream is = null;
		try {
			is = source.getInputStream();
			return extractIPTCFromStream(is);
		} finally {
			if (is != null) {
				try { is.close(); } catch (Exception e) {}
			}
		}
	}

	@Override
	public Struct extractIPTC(byte[] bytes, String format) throws Exception {
		ImageInputStream iis = null;
		try {
			iis = new ByteArrayImageInputStream(bytes);
			return extractIPTCFromImageInputStream(iis);
		} finally {
			if (iis != null) {
				try { iis.close(); } catch (Exception e) {}
			}
		}
	}

	private Struct extractIPTCFromStream(InputStream is) throws Exception {
		ImageInputStream iis = null;
		try {
			iis = ImageIO.createImageInputStream(is);
			return extractIPTCFromImageInputStream(iis);
		} finally {
			if (iis != null) {
				try { iis.close(); } catch (Exception e) {}
			}
		}
	}

	private Struct extractIPTCFromImageInputStream(ImageInputStream iis) throws Exception {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		Struct rtn = eng.getCreationUtil().createStruct();

		try {
			// Read APP13 "Photoshop 3.0" segments
			List<JPEGSegment> segments = JPEGSegmentUtil.readSegments(iis, JPEG.APP13, "Photoshop 3.0");

			if (segments.isEmpty()) {
				return rtn;
			}

			// Parse the first APP13 segment with PSDReader
			JPEGSegment segment = segments.get(0);
			ImageInputStream segmentStream = ImageIO.createImageInputStream(segment.data());
			Directory psd = new PSDReader().read(segmentStream);

			// Get the IPTC resource (0x0404)
			Entry iptcEntry = psd.getEntryById(PSD.RES_IPTC_NAA);
			if (iptcEntry == null) {
				return rtn;
			}

			// Parse IPTC data
			byte[] iptcData = (byte[]) iptcEntry.getValue();
			Directory iptcDir = new IPTCReader().read(new ByteArrayImageInputStream(iptcData));

			// Convert IPTC Directory to Struct
			return convertIPTCDirectoryToStruct(iptcDir);
		} catch (javax.imageio.IIOException e) {
			// Not a JPEG or no IPTC data - return empty struct
			return rtn;
		}
	}

	private Struct convertIPTCDirectoryToStruct(Directory iptcDir) throws Exception {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		Struct rtn = eng.getCreationUtil().createStruct();

		Collection.Key KEYWORDS = eng.getCreationUtil().createKey("Keywords");
		Collection.Key SUBJECT_REFERENCE = eng.getCreationUtil().createKey("Subject Reference");

		for (Entry entry : iptcDir) {
			Object tagId = entry.getIdentifier();
			Object value = entry.getValue();

			if (value == null) continue;

			// Convert String[] to String for single values, or comma-separated for multiple
			if (value instanceof String[]) {
				String[] arr = (String[]) value;
				if (arr.length == 0) continue;
				if (arr.length == 1) {
					value = arr[0];
				} else {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < arr.length; i++) {
						if (i > 0) sb.append(", ");
						sb.append(arr[i]);
					}
					value = sb.toString();
				}
			}

			// Get field name from IPTC tag
			String fieldName = getIPTCFieldName(tagId);
			if (fieldName == null) continue;

			Collection.Key key = eng.getCreationUtil().createKey(fieldName);
			Object existing = rtn.get(key, null);

			// Handle multiple values for same key
			if (existing != null) {
				if (KEYWORDS.equals(key)) {
					rtn.set(key, existing + ";" + value.toString());
				} else if (SUBJECT_REFERENCE.equals(key)) {
					rtn.set(key, existing + " " + value.toString());
				} else if (existing instanceof Array) {
					Array arr = (Array) existing;
					arr.append(value);
				} else {
					Array arr = eng.getCreationUtil().createArray();
					arr.append(existing);
					arr.append(value);
					rtn.set(key, arr);
				}
			} else {
				rtn.set(key, value);
			}
		}

		return rtn;
	}

	private String getIPTCFieldName(Object tagId) {
		// Map IPTC tag IDs to field names
		if (!(tagId instanceof Integer)) return null;

		int tag = (Integer) tagId;

		// Application Record (2:xxx) tags
		switch (tag) {
			case IPTC.TAG_RECORD_VERSION: return "RecordVersion";
			case IPTC.TAG_OBJECT_TYPE_REFERENCE: return "Object Type Reference";
			case IPTC.TAG_OBJECT_ATTRIBUTE_REFERENCE: return "Object Attribute Reference";
			case IPTC.TAG_OBJECT_NAME: return "Object Name";
			case IPTC.TAG_EDIT_STATUS: return "Edit Status";
			case IPTC.TAG_EDITORIAL_UPDATE: return "Editorial Update";
			case IPTC.TAG_URGENCY: return "Urgency";
			case IPTC.TAG_SUBJECT_REFERENCE: return "Subject Reference";
			case IPTC.TAG_CATEGORY: return "Category";
			case IPTC.TAG_SUPPLEMENTAL_CATEGORIES: return "Supplemental Category";
			case IPTC.TAG_FIXTURE_IDENTIFIER: return "Fixture Identifier";
			case IPTC.TAG_KEYWORDS: return "Keywords";
			case IPTC.TAG_CONTENT_LOCATION_CODE: return "Content Location Code";
			case IPTC.TAG_CONTENT_LOCATION_NAME: return "Content Location Name";
			case IPTC.TAG_RELEASE_DATE: return "Release Date";
			case IPTC.TAG_RELEASE_TIME: return "Release Time";
			case IPTC.TAG_EXPIRATION_DATE: return "Expiration Date";
			case IPTC.TAG_EXPIRATION_TIME: return "Expiration Time";
			case IPTC.TAG_SPECIAL_INSTRUCTIONS: return "Special Instructions";
			case IPTC.TAG_ACTION_ADVICED: return "Action Advised";
			case IPTC.TAG_REFERENCE_SERVICE: return "Reference Service";
			case IPTC.TAG_REFERENCE_DATE: return "Reference Date";
			case IPTC.TAG_REFERENCE_NUMBER: return "Reference Number";
			case IPTC.TAG_DATE_CREATED: return "Date Created";
			case IPTC.TAG_TIME_CREATED: return "Time Created";
			case IPTC.TAG_DIGITAL_CREATION_DATE: return "Digital Creation Date";
			case IPTC.TAG_DIGITAL_CREATION_TIME: return "Digital Creation Time";
			case IPTC.TAG_ORIGINATING_PROGRAM: return "Originating Program";
			case IPTC.TAG_PROGRAM_VERSION: return "Program Version";
			case IPTC.TAG_OBJECT_CYCLE: return "Object Cycle";
			case IPTC.TAG_BY_LINE: return "By-line";
			case IPTC.TAG_BY_LINE_TITLE: return "By-line Title";
			case IPTC.TAG_CITY: return "City";
			case IPTC.TAG_SUB_LOCATION: return "Sub-location";
			case IPTC.TAG_PROVINCE_OR_STATE: return "Province/State";
			case IPTC.TAG_COUNTRY_OR_PRIMARY_LOCATION_CODE: return "Country Code";
			case IPTC.TAG_COUNTRY_OR_PRIMARY_LOCATION: return "Country/Primary Location Name";
			case IPTC.TAG_ORIGINAL_TRANSMISSION_REFERENCE: return "Original Transmission Reference";
			case IPTC.TAG_HEADLINE: return "Headline";
			case IPTC.TAG_CREDIT: return "Credit";
			case IPTC.TAG_SOURCE: return "Source";
			case IPTC.TAG_COPYRIGHT_NOTICE: return "Copyright Notice";
			case IPTC.TAG_CONTACT: return "Contact";
			case IPTC.TAG_CAPTION: return "Caption/Abstract";
			case IPTC.TAG_WRITER: return "Writer/Editor";
			case IPTC.TAG_IMAGE_TYPE: return "Image Type";
			case IPTC.TAG_IMAGE_ORIENTATION: return "Image Orientation";
			case IPTC.TAG_LANGUAGE_IDENTIFIER: return "Language Identifier";
			default: return "Tag-" + tag;
		}
	}

	@Override
	public void extractEXIF(File source, Struct info, String format) throws Exception {
		InputStream is = null;
		try {
			is = new FileInputStream(source);
			extractEXIFFromStream(is, info);
		} finally {
			if (is != null) {
				try { is.close(); } catch (Exception e) {}
			}
		}
	}

	@Override
	public void extractEXIF(Resource source, Struct info, String format) throws Exception {
		InputStream is = null;
		try {
			is = source.getInputStream();
			extractEXIFFromStream(is, info);
		} finally {
			if (is != null) {
				try { is.close(); } catch (Exception e) {}
			}
		}
	}

	@Override
	public void extractEXIF(byte[] bytes, Struct info, String format) throws Exception {
		ImageInputStream iis = null;
		try {
			iis = new ByteArrayImageInputStream(bytes);
			extractEXIFFromImageInputStream(iis, info);
		} finally {
			if (iis != null) {
				try { iis.close(); } catch (Exception e) {}
			}
		}
	}

	private void extractEXIFFromStream(InputStream is, Struct info) throws Exception {
		ImageInputStream iis = null;
		try {
			iis = ImageIO.createImageInputStream(is);
			extractEXIFFromImageInputStream(iis, info);
		} finally {
			if (iis != null) {
				try { iis.close(); } catch (Exception e) {}
			}
		}
	}

	private void extractEXIFFromImageInputStream(ImageInputStream iis, Struct info) throws Exception {
		// TwelveMonkeys EXIF extraction via TIFF reader from APP1 EXIF segments
		// For now, return without adding EXIF data - this is a placeholder
		// Full implementation would read APP1 "Exif" segments and parse TIFF IFDs
	}

	@Override
	public String getName() {
		return "TwelveMonkeys";
	}
}
