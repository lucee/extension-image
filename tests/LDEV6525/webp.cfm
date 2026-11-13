<cfscript>
	// LDEV-6525 WebP write/read through the "Gotson" coder only (see Application.cfc), returns JSON
	setting showdebugoutput=false;
	result = {};
	dir = getTempDirectory() & "LDEV6525-" & createUUID() & "/";
	directoryCreate( dir );
	images = expandPath( "../images/" );

	function webpHeader( path ) {
		var bytes = fileReadBinary( arguments.path );
		if ( arrayLen( bytes ) < 12 ) return "";
		// "RIFF" at 0..3 and "WEBP" at 8..11, the 4 bytes in between are the size
		var s = "";
		for ( var i in [ 1, 2, 3, 4, 9, 10, 11, 12 ] ) {
			s &= chr( bytes[ i ] < 0 ? bytes[ i ] + 256 : bytes[ i ] );
		}
		return s;
	}

	try {
		switch ( url.action ) {
			case "lib":
				img = imageNew( "", 1, 1 );
				cls = img.getClass().getClassLoader().loadClass( "com.luciad.imageio.webp.WebPImageWriterSpi" );
				result.location = cls.getProtectionDomain().getCodeSource().getLocation().toString();
				result.writeable = getWriteableImageFormats();
				result.readable = getReadableImageFormats();
				break;
			case "imageWrite":
				img = imageNew( "", 64, 48, url.type ?: "rgb", "red" );
				imageDrawRect( img, 8, 8, 20, 20, true );
				path = dir & "out.webp";
				imageWrite( img, path, true, 0.85 );
				result.size = getFileInfo( path ).size;
				result.header = webpHeader( path );
				back = imageRead( path );
				result.width = imageGetWidth( back );
				result.height = imageGetHeight( back );
				break;
			case "cfimageWrite":
				img = imageNew( "", 40, 30, "rgb", "blue" );
				path = dir & "tag.webp";
				cfimage( action="write", source=img, destination=path, overwrite=true );
				result.size = getFileInfo( path ).size;
				result.header = webpHeader( path );
				result.width = imageGetWidth( imageRead( path ) );
				break;
			case "read":
				img = imageRead( images & url.file );
				result.width = imageGetWidth( img );
				result.height = imageGetHeight( img );
				// write it again, read it back
				path = dir & "again.webp";
				imageWrite( img, path, true );
				result.againWidth = imageGetWidth( imageRead( path ) );
				break;
		}
		result.ok = true;
	}
	catch ( any e ) {
		result.ok = false;
		result.error = e.message;
		result.detail = e.detail ?: "";
	}
	finally {
		if ( directoryExists( dir ) ) directoryDelete( dir, true );
	}
	echo( serializeJSON( result ) );
</cfscript>
