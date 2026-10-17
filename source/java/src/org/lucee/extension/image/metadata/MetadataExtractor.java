package org.lucee.extension.image.metadata;

import java.io.File;

import lucee.commons.io.res.Resource;
import lucee.runtime.type.Struct;

/**
 * Interface for extracting metadata (IPTC, EXIF, XMP) from image files.
 * Implementations can use different libraries (TwelveMonkeys, Commons Imaging, etc.)
 * following the MultiCoder pattern for fallback support.
 */
public interface MetadataExtractor {

	/**
	 * Extract IPTC metadata from an image file
	 * @param source File source
	 * @param format Image format (jpg, png, etc.)
	 * @return Struct containing IPTC metadata, empty if none found
	 * @throws Exception if extraction fails
	 */
	Struct extractIPTC(File source, String format) throws Exception;

	/**
	 * Extract IPTC metadata from an image resource
	 * @param source Resource source
	 * @param format Image format (jpg, png, etc.)
	 * @return Struct containing IPTC metadata, empty if none found
	 * @throws Exception if extraction fails
	 */
	Struct extractIPTC(Resource source, String format) throws Exception;

	/**
	 * Extract IPTC metadata from image bytes
	 * @param bytes Image bytes
	 * @param format Image format (jpg, png, etc.)
	 * @return Struct containing IPTC metadata, empty if none found
	 * @throws Exception if extraction fails
	 */
	Struct extractIPTC(byte[] bytes, String format) throws Exception;

	/**
	 * Extract EXIF metadata and add to provided struct
	 * @param source File source
	 * @param info Struct to populate with EXIF data
	 * @param format Image format (jpg, png, etc.)
	 * @throws Exception if extraction fails
	 */
	void extractEXIF(File source, Struct info, String format) throws Exception;

	/**
	 * Extract EXIF metadata and add to provided struct
	 * @param source Resource source
	 * @param info Struct to populate with EXIF data
	 * @param format Image format (jpg, png, etc.)
	 * @throws Exception if extraction fails
	 */
	void extractEXIF(Resource source, Struct info, String format) throws Exception;

	/**
	 * Extract EXIF metadata and add to provided struct
	 * @param bytes Image bytes
	 * @param info Struct to populate with EXIF data
	 * @param format Image format (jpg, png, etc.)
	 * @throws Exception if extraction fails
	 */
	void extractEXIF(byte[] bytes, Struct info, String format) throws Exception;

	/**
	 * Get the name of this extractor (for logging/debugging)
	 * @return Extractor name
	 */
	String getName();
}
