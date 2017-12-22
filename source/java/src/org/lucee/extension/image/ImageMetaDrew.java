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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.tiff.TiffMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;

public class ImageMetaDrew {

	/**
	 * adds information about a image to the given struct
	 * @param info
	 * @throws PageException 
	 * @throws IOException 
	 * @throws MetadataException 
	 * @throws JpegProcessingException 
	 */
	public static void addInfo(String format, Resource res, Struct info) {
		if("jpg".equalsIgnoreCase(format))jpg(res, info);
		else if("tiff".equalsIgnoreCase(format))tiff(res, info);
		
	}

	private static void jpg(Resource res,Struct info) {
		InputStream is=null;
		try {
			is = res.getInputStream();
			fill(info,JpegMetadataReader.readMetadata(is));
		}
		catch(Exception e) {}
		finally {
			Util.closeEL(is);
		}
	}
	
	private static void tiff(Resource res,Struct info) {
		InputStream is=null;
		try {
			is = res.getInputStream();
			fill(info,TiffMetadataReader.readMetadata(is));
		}
		catch(Exception e) {}
		finally {
			Util.closeEL(is);
		}
	}

	private static void fill(Struct info,Metadata metadata) {
		Iterator<Directory> directories = metadata.getDirectories().iterator();
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		while (directories.hasNext()) {
		    Directory directory = directories.next();
		    Struct sct=eng.getCreationUtil().createStruct();
		    info.setEL(eng.getCreationUtil().createKey(directory.getName()), sct);
		    
		    Iterator<Tag> tags = directory.getTags().iterator();
		    while (tags.hasNext()) {
		        Tag tag = tags.next();
		        sct.setEL(eng.getCreationUtil().createKey(tag.getTagName()), tag.getDescription());
		    }
		}
	}

	public static void test() {
		// to not delete, this methd is called to test if the jar exists
		
	}
}