/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lucee.extension.image;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.GenericImageMetadata.GenericImageMetadataItem;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.lucee.extension.image.util.CommonUtil;

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;

public class Metadata {

	public static final int ORIENTATION_UNDEFINED = 0;
	public static final int ORIENTATION_NORMAL = 1;
	public static final int ORIENTATION_FLIP_HORIZONTAL = 2; // left right reversed mirror
	public static final int ORIENTATION_ROTATE_180 = 3;
	public static final int ORIENTATION_FLIP_VERTICAL = 4; // upside down mirror
	// flipped about top-left <--> bottom-right axis
	public static final int ORIENTATION_TRANSPOSE = 5;
	public static final int ORIENTATION_ROTATE_90 = 6; // rotate 90 cw to right it
	// flipped about top-right <--> bottom-left axis
	public static final int ORIENTATION_TRANSVERSE = 7;
	public static final int ORIENTATION_ROTATE_270 = 8; // rotate 270 to right it

	public static void addExifInfoToStruct(final Resource res, Struct info, Log log) throws ImageReadException, IOException {
		if (res == null) return;
		try {
			ImageMetadata md;
			if (res instanceof File) {
				md = Imaging.getMetadata((File) res);
			}
			else {
				// ByteArrayOutputStream baos = new ByteArrayOutputStream();
				// CFMLEngineFactory.getInstance().getIOUtil().copy(res.getInputStream(), baos, true, true);
				// md = Imaging.getMetadata(baos.toByteArray());
				md = Imaging.getMetadata(res.getInputStream(), res.getName());
			}
			if (md == null) return;

			if (md instanceof JpegImageMetadata) {
				final JpegImageMetadata jpegMetadata = (JpegImageMetadata) md;

				/*
				 * List<? extends ImageMetadataItem> dirs = jpegMetadata.getExif().getDirectories(); for
				 * (ImageMetadataItem imdi: dirs) { print.e(imdi.getClass().getName()); print.e(imdi.toString()); }
				 */

				// EXIF
				if (jpegMetadata != null) {
					Struct exif = CFMLEngineFactory.getInstance().getCreationUtil().createStruct();
					info.setEL("exif", exif);
					try {
						TiffImageMetadata _exif = jpegMetadata.getExif();
						set(_exif.getItems(), info, exif);
					}
					catch (Exception e) {
						if (log != null) log.error("imaging", e);
					}
				}
				// GPS
				try {
					gps(jpegMetadata, info);
				}
				catch (Exception e) {
					if (log != null) log.error("imaging", e);
				}
			}
		}
		catch (Exception ex) {
			if (log != null) log.error("imaging", ex);

		}
	}

	public static ImageMetadata getMetadata(Resource res) throws ImageReadException, IOException {
		if (res instanceof File) {
			return Imaging.getMetadata((File) res);
		}
		InputStream is = null;
		try {
			return Imaging.getMetadata(is = res.getInputStream(), res.getName());
		}
		finally {
			Util.closeEL(is);
		}
	}

	public static int getOrientation(ImageMetadata metadata) {
		if (metadata == null) return ORIENTATION_UNDEFINED;
		List<? extends ImageMetadataItem> items = metadata.getItems();
		GenericImageMetadataItem gimdi = null;
		for (ImageMetadataItem item: items) {
			if (!(item instanceof GenericImageMetadataItem)) continue;
			gimdi = ((GenericImageMetadataItem) item);
			if ("ORIENTATION".equalsIgnoreCase(gimdi.getKeyword())) {
				try {
					return CFMLEngineFactory.getInstance().getCastUtil().toIntValue(CommonUtil.unwrap(gimdi.getText()));
				}
				catch (Exception e) {
					return ORIENTATION_UNDEFINED;
				}
			}

		}
		return ORIENTATION_UNDEFINED;
	}

	private static void gps(JpegImageMetadata jpegMetadata, Struct info) throws ImageReadException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		Struct gps = eng.getCreationUtil().createStruct();
		info.setEL("gps", gps);
		info = gps;
		final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
		Double longitude = null;
		Double latitude = null;
		if (null != exifMetadata) {
			final TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
			if (null != gpsInfo) {
				// final String gpsDescription = gpsInfo.toString();
				longitude = gpsInfo.getLongitudeAsDegreesEast();
				latitude = gpsInfo.getLatitudeAsDegreesNorth();

			}
		}

		// more specific example of how to manually access GPS values
		final TiffField gpsLatitudeRefField = jpegMetadata.findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
		final TiffField gpsLatitudeField = jpegMetadata.findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE);
		final TiffField gpsLongitudeRefField = jpegMetadata.findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
		final TiffField gpsLongitudeField = jpegMetadata.findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE);
		if (gpsLatitudeRefField != null && gpsLatitudeField != null && gpsLongitudeRefField != null && gpsLongitudeField != null) {
			// all of these values are strings.
			final String gpsLatitudeRef = (String) gpsLatitudeRefField.getValue();
			final RationalNumber gpsLatitude[] = (RationalNumber[]) (gpsLatitudeField.getValue());
			final String gpsLongitudeRef = (String) gpsLongitudeRefField.getValue();
			final RationalNumber gpsLongitude[] = (RationalNumber[]) gpsLongitudeField.getValue();

			info.setEL("GPS Latitude", gpsLatitude[0].toDisplayString() + "\"" + gpsLatitude[1].toDisplayString() + "'" + gpsLatitude[2].toDisplayString());

			info.setEL("GPS Latitude Ref", gpsLatitudeRef);
			Struct sct = eng.getCreationUtil().createStruct();
			gps.setEL("latitude", sct);
			sct.setEL("degrees", gpsLatitude[0].doubleValue());
			sct.setEL("minutes", gpsLatitude[1].doubleValue());
			sct.setEL("seconds", gpsLatitude[2].doubleValue());
			sct.setEL("ref", gpsLatitudeRef);
			sct.setEL("decimal", latitude);

			info.setEL("GPS Longitude", gpsLongitude[0].toDisplayString() + "\"" + gpsLongitude[1].toDisplayString() + "'" + gpsLongitude[2].toDisplayString());
			info.setEL("GPS Longitude Ref", gpsLongitudeRef);
			sct = eng.getCreationUtil().createStruct();
			gps.setEL("longitude", sct);
			sct.setEL("degrees", gpsLongitude[0].doubleValue());
			sct.setEL("minutes", gpsLongitude[1].doubleValue());
			sct.setEL("seconds", gpsLongitude[2].doubleValue());
			sct.setEL("ref", gpsLongitudeRef);
			sct.setEL("decimal", longitude);
		}
	}

	private static void set(Struct sct1, Struct sct2, String name1, String name2, Object value) {
		if (value instanceof CharSequence) value = CommonUtil.unwrap(value.toString());
		sct1.setEL(name1, value);
		sct2.setEL(name2, value);
	}

	private static Object val(Object value) {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if (value == null) return null;
		if (value instanceof CharSequence) return value.toString();
		if (value instanceof Number) return ((Number) value).doubleValue();
		if (eng.getDecisionUtil().isNativeArray(value) && !(value instanceof Object[])) return value;
		if (value instanceof Object[]) {
			Array trg = eng.getCreationUtil().createArray();
			Object[] arr = (Object[]) value;
			for (Object obj: arr) {
				trg.appendEL(val(obj));
			}
			return trg;
		}
		if (value instanceof RationalNumber) {
			RationalNumber rn = (RationalNumber) value;
			return rn.toDisplayString();
		}
		return value;
	}

	private static void set(List<? extends ImageMetadataItem> items, Struct data1, Struct data2) {
		Iterator<? extends ImageMetadataItem> it = items.iterator();
		GenericImageMetadataItem item;
		while (it.hasNext()) {
			item = (GenericImageMetadataItem) it.next();
			data1.setEL(item.getKeyword(), CommonUtil.unwrap(item.getText()));
			if (data2 != null) {
				data2.setEL(item.getKeyword(), CommonUtil.unwrap(item.getText()));
			}
		}
	}

	private static void set(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo, Struct info) throws ImageReadException {
		final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
		if (field != null) {
			if (!info.containsKey(tagInfo.name)) {
				Object val = val(field.getValue());
				if (val != null) info.setEL(tagInfo.name, val);
			}
		}
	}

}
