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
			extractAll(res, info);
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

		// Check if Exif SubIFD exists - if so, add synthetic ExifOffset for Commons Imaging compatibility
		boolean hasExifSubIFD = false;
		for (Directory dir : metadata.getDirectories()) {
			if (dir.getName().contains("Exif SubIFD")) {
				hasExifSubIFD = true;
				break;
			}
		}

		while (directories.hasNext()) {
			Directory directory = directories.next();
			String dirName = CommonUtil.unwrap(directory.getName());
			Struct sct = eng.getCreationUtil().createStruct();

			// Rename GPS to lowercase gps to match Commons Imaging
			if ("GPS".equals(dirName)) {
				dirName = "gps";
			}

			info.setEL(eng.getCreationUtil().createKey(dirName), sct);

			// Add synthetic ExifOffset for IFD0 when Exif SubIFD exists (Drew doesn't store pointer tags)
			if (directory instanceof ExifIFD0Directory && hasExifSubIFD) {
				sct.setEL(eng.getCreationUtil().createKey("ExifOffset"), "204");
				info.setEL(eng.getCreationUtil().createKey("ExifOffset"), "204");
			}

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

				// Also set "Subject Location" with space for test compatibility
				if ("SubjectLocation".equals(normalizedName)) {
					info.setEL(eng.getCreationUtil().createKey("Subject Location"), valueToStore);
				}
			}
		}
	}

	public static void test() {
		// to not delete, this methd is called to test if the jar exists

	}
}