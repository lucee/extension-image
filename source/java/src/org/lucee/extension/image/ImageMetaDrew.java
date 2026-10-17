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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.lucee.extension.image.util.CommonUtil;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.config.Config;
import lucee.runtime.type.Struct;

public class ImageMetaDrew {

	/**
	 * adds information about a image to the given struct
	 */
	public static void addInfo(String format, Resource res, Struct info) {
		try {
			// formats supported before LDEV-6083 keep their original reader and key names (backward compatibility)
			if (ImageUtil.isJPEG(format)) legacy(res, info, false);
			else if ("tiff".equalsIgnoreCase(format) || "tif".equalsIgnoreCase(format)) legacy(res, info, true);
			else extractAll(res, info);
		}
		catch (Exception ex) {
			try {
				Log log = null;
				Config c = CFMLEngineFactory.getInstance().getThreadConfig();
				if (c != null) log = c.getLog("application");
				if (log != null) log.log(Log.LEVEL_DEBUG, "imaging", "failed to read metadata from [" + res + "], metadata is ignored", ex);
			}
			catch (Exception e) {
			}
		}
	}

	/**
	 * Read EXIF orientation via Drew, without decoding pixels. Covers every format Drew parses
	 * (JPEG/PNG/WebP/TIFF/HEIC/BMP/GIF/PSD/etc). Returns {@link Metadata#ORIENTATION_UNDEFINED}
	 * on read failure or when no orientation tag is present.
	 */
	public static int readOrientation(Resource res) {
		InputStream is = null;
		BufferedInputStream bis = null;
		try {
			is = res.getInputStream();
			bis = new BufferedInputStream(is);
			return orientationFromMetadata(ImageMetadataReader.readMetadata(bis));
		}
		catch (Exception e) {
			return org.lucee.extension.image.Metadata.ORIENTATION_UNDEFINED;
		}
		finally {
			Util.closeEL(bis);
			Util.closeEL(is);
		}
	}

	public static int readOrientation(byte[] bytes) {
		try {
			return orientationFromMetadata(ImageMetadataReader.readMetadata(new ByteArrayInputStream(bytes)));
		}
		catch (Exception e) {
			return org.lucee.extension.image.Metadata.ORIENTATION_UNDEFINED;
		}
	}

	private static int orientationFromMetadata(Metadata metadata) {
		if (metadata == null) return org.lucee.extension.image.Metadata.ORIENTATION_UNDEFINED;
		ExifIFD0Directory dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		if (dir == null) return org.lucee.extension.image.Metadata.ORIENTATION_UNDEFINED;
		if (!dir.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) return org.lucee.extension.image.Metadata.ORIENTATION_UNDEFINED;
		try {
			return dir.getInt(ExifIFD0Directory.TAG_ORIENTATION);
		}
		catch (Exception e) {
			return org.lucee.extension.image.Metadata.ORIENTATION_UNDEFINED;
		}
	}

	private static void legacy(Resource res, Struct info, boolean tiff) throws IOException, com.drew.imaging.ImageProcessingException {
		InputStream is = null;
		try {
			is = res.getInputStream();
			Metadata md = tiff ? com.drew.imaging.tiff.TiffMetadataReader.readMetadata(is) : com.drew.imaging.jpeg.JpegMetadataReader.readMetadata(is);
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			for (Directory directory: md.getDirectories()) {
				Struct sct = eng.getCreationUtil().createStruct();
				info.setEL(eng.getCreationUtil().createKey(CommonUtil.unwrap(directory.getName())), sct);
				for (Tag tag: directory.getTags()) {
					sct.setEL(eng.getCreationUtil().createKey(CommonUtil.unwrap(tag.getTagName())), CommonUtil.unwrap(tag.getDescription()));
				}
			}
		}
		finally {
			Util.closeEL(is);
		}
	}

	private static void extractAll(Resource res, Struct info) throws IOException, com.drew.imaging.ImageProcessingException {
		InputStream is = null;
		BufferedInputStream bis = null;
		try {
			is = res.getInputStream();
			bis = new BufferedInputStream(is);
			fill(info, ImageMetadataReader.readMetadata(bis));
		}
		finally {
			Util.closeEL(bis);
			Util.closeEL(is);
		}
	}

	private static void fill(Struct info, Metadata metadata) {
		Iterator<Directory> directories = metadata.getDirectories().iterator();
		CFMLEngine eng = CFMLEngineFactory.getInstance();

		while (directories.hasNext()) {
			Directory directory = directories.next();
			String dirName = CommonUtil.unwrap(directory.getName());
			Struct sct = eng.getCreationUtil().createStruct();

			// Rename GPS to lowercase gps to match Commons Imaging
			if ("GPS".equals(dirName)) {
				dirName = "gps";
			}

			info.setEL(eng.getCreationUtil().createKey(dirName), sct);

			Iterator<Tag> tags = directory.getTags().iterator();
			while (tags.hasNext()) {
				Tag tag = tags.next();
				String tagName = CommonUtil.unwrap(tag.getTagName());

				// Normalize field names by removing spaces to match Commons Imaging behavior
				String normalizedName = tagName.replace(" ", "");

				// Get raw value from directory
				Object rawValue = directory.getObject(tag.getTagType());
				Object valueToStore;

				if (rawValue != null) {
					// Store the raw value (converted to string if needed)
					if (rawValue instanceof Number) {
						valueToStore = rawValue.toString();
					} else if (rawValue instanceof String) {
						valueToStore = CommonUtil.unwrap((String) rawValue);
					} else {
						valueToStore = CommonUtil.unwrap(tag.getDescription());
					}
				} else {
					// Fallback to description if raw value is null
					valueToStore = CommonUtil.unwrap(tag.getDescription());
				}

				// Set value in directory struct
				sct.setEL(eng.getCreationUtil().createKey(normalizedName), valueToStore);

			}
		}
	}

	public static void test() {
		// to not delete, this methd is called to test if the jar exists

	}
}