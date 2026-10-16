package org.lucee.extension.image;

import java.awt.image.BufferedImage;

import com.twelvemonkeys.image.ResampleOp;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;

/**
 * Helper class for image resampling using TwelveMonkeys ResampleOp.
 * Provides mapping between Lucee interpolation constants and TwelveMonkeys filters,
 * and handles the decision between TwelveMonkeys (blur=1.0) and ImageResizer (blur!=1.0).
 */
public class ResampleHelper {

	// Filters that support blur (available in ImageResizer)
	private static final String BLUR_SUPPORTED_FILTERS = "[ triangle, hermite, hanning, hamming, blackman, quadratic, mitchell, lanczos, bessel ]";

	/**
	 * Resize an image using the appropriate method based on blur factor.
	 * - blur=1.0 + has TwelveMonkeys equivalent: Use TwelveMonkeys ResampleOp (faster)
	 * - blur=1.0 + Bessel: Use ImageResizer (no TwelveMonkeys equivalent)
	 * - blur!=1.0 + supports blur: Use ImageResizer
	 * - blur!=1.0 + doesn't support blur: Error
	 */
	public static BufferedImage resize( BufferedImage image, int width, int height, int interpolation, double blurFactor ) throws PageException {
		// Bessel has no TwelveMonkeys equivalent, always use ImageResizer
		if ( interpolation == Image.IP_BESSEL ) {
			return ImageResizer.resize( image, width, height, interpolation, blurFactor );
		}

		// blur=1.0 can use TwelveMonkeys for all other filters
		if ( blurFactor == 1.0 ) {
			return resizeWithTwelveMonkeys( image, width, height, interpolation );
		}

		// blur!=1.0 requires ImageResizer, but only some filters support it
		if ( !supportsBlur( interpolation ) ) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException(
				"The interpolation filter does not support blur factor. " +
				"Filters that support blur: " + BLUR_SUPPORTED_FILTERS
			);
		}

		return ImageResizer.resize( image, width, height, interpolation, blurFactor );
	}

	/**
	 * Resize using TwelveMonkeys ResampleOp.
	 */
	private static BufferedImage resizeWithTwelveMonkeys( BufferedImage image, int width, int height, int interpolation ) throws PageException {
		int tmFilter = toTwelveMonkeysFilter( interpolation );
		ResampleOp resampleOp = new ResampleOp( width, height, tmFilter );
		return resampleOp.filter( image, null );
	}

	/**
	 * Check if the interpolation filter supports blur factor.
	 * Only the original 9 ImageResizer filters (and quality presets that map to them) support blur.
	 */
	public static boolean supportsBlur( int interpolation ) {
		switch ( interpolation ) {
			// Direct filters
			case Image.IP_TRIANGLE:
			case Image.IP_HERMITE:
			case Image.IP_HANNING:
			case Image.IP_HAMMING:
			case Image.IP_BLACKMAN:
			case Image.IP_QUADRATIC:
			case Image.IP_MITCHELL:
			case Image.IP_LANCZOS:
			case Image.IP_BESSEL:
			// Quality presets (map to Hamming, Mitchell, or Lanczos)
			case Image.IP_AUTOMATIC:
			case Image.IP_HIGHESTQUALITY:
			case Image.IP_HIGHQUALITY:
			case Image.IP_MEDIUMQUALITY:
			case Image.IP_HIGHPERFORMANCE:
			case Image.IP_MEDIUMPERFORMANCE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Map Lucee IP_* or IPC_* interpolation constant to TwelveMonkeys FILTER_* constant.
	 */
	public static int toTwelveMonkeysFilter( int interpolation ) throws PageException {
		switch ( interpolation ) {
			// Basic IPC constants (Java interpolation equivalents)
			case Image.IPC_NEAREST:
			case Image.IP_POINT:
				return ResampleOp.FILTER_POINT;
			case Image.IPC_BILINEAR:
			case Image.IP_TRIANGLE:
				return ResampleOp.FILTER_TRIANGLE;
			case Image.IPC_BICUBIC:
			case Image.IP_HIGHESTPERFORMANCE:
			case Image.IP_CUBIC:
				return ResampleOp.FILTER_CUBIC;

			// Original filters (supported by both ImageResizer and TwelveMonkeys)
			case Image.IP_HERMITE:
				return ResampleOp.FILTER_HERMITE;
			case Image.IP_HANNING:
				return ResampleOp.FILTER_HANNING;
			case Image.IP_MEDIUMQUALITY:
			case Image.IP_HIGHPERFORMANCE:
			case Image.IP_HAMMING:
				return ResampleOp.FILTER_HAMMING;
			case Image.IP_BLACKMAN:
				return ResampleOp.FILTER_BLACKMAN;
			case Image.IP_QUADRATIC:
				return ResampleOp.FILTER_QUADRATIC;
			case Image.IP_AUTOMATIC:
			case Image.IP_HIGHQUALITY:
			case Image.IP_MEDIUMPERFORMANCE:
			case Image.IP_MITCHELL:
				return ResampleOp.FILTER_MITCHELL;
			case Image.IP_HIGHESTQUALITY:
			case Image.IP_LANCZOS:
				return ResampleOp.FILTER_LANCZOS;
			// Note: IP_BESSEL handled separately in resize() - always uses ImageResizer

			// TwelveMonkeys-only filters
			case Image.IP_GAUSSIAN:
				return ResampleOp.FILTER_GAUSSIAN;
			case Image.IP_CATROM:
				return ResampleOp.FILTER_CATROM;
			case Image.IP_BOX:
				return ResampleOp.FILTER_BOX;
			case Image.IP_BLACKMAN_BESSEL:
				return ResampleOp.FILTER_BLACKMAN_BESSEL;
			case Image.IP_BLACKMAN_SINC:
				return ResampleOp.FILTER_BLACKMAN_SINC;

			default:
				throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException(
					"Unknown interpolation filter: " + interpolation
				);
		}
	}
}
