<cfscript>
	param name="url.codec" default="";
	param name="url.fixture" default="heic";

	fixtures = {
		heic: expandPath( "../images/sample.heic" ),
		heif: expandPath( "../images/sample.heif" ),
		avif: expandPath( "../images/sample.avif" ),
		jxl: expandPath( "../images/sample.jxl" )
	};

	if ( !structKeyExists( fixtures, url.fixture ) ) {
		echo( "unknown fixture [#url.fixture#]" );
		abort;
	}

	path = fixtures[ url.fixture ];
	if ( !fileExists( path ) ) {
		echo( "missing fixture file [#path#]" );
		abort;
	}

	try {
		img = imageRead( path );
		if ( !isImage( img ) || imageGetWidth( img ) <= 0 || imageGetHeight( img ) <= 0 ) {
			echo( "invalid image from fixture [#url.fixture#]" );
		}
	} catch ( any e ) {
		echo( e.message );
	}
</cfscript>
