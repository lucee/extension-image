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
import java.util.List;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.GenericImageMetadata.GenericImageMetadataItem;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;
import org.lucee.extension.image.util.CommonUtil;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;

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

}
