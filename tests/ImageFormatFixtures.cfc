<cfscript>
/**
 * Read/write tests using real image files under tests/images/.
 * Files are copied from Lucee core artifacts and supplemented with small public test fixtures.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	variables.imgDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "images/";
	variables.fixtures = {};

	function beforeAll(){
		registerFileFixture( "jpeg-small", "sample-small.jpg" );
		registerFileFixture( "png-small", "sample-small.png" );
		registerFileFixture( "png-smiley", "sample-smiley.png" );
		registerFileFixture( "png-transparent", "sample-transparent.png" );
		registerFileFixture( "png-green-256", "sample-green-256.png" );
		registerFileFixture( "jpeg-exif-orientation", "sample-exif-orientation.jpg" );
		registerFileFixture( "gif-animated", "sample-animated.gif" );
		registerFileFixture( "webp-alpha", "sample-alpha.webp" );
		registerFileFixture( "webp-alpha-2", "sample-alpha-2.webp" );
		registerFileFixture( "webp-large", "small-sample.webp" );
		registerFileFixture( "bmp", "sample.bmp", 64, 48 );
		registerFileFixture( "tiff", "sample.tif", 64, 48 );
		registerFileFixture( "tiff-alt", "sample.tiff", 64, 48 );
		registerFileFixture( "ico", "sample.ico", 32, 32 );
		registerFileFixture( "psd", "sample.psd", 5, 5 );
		registerFileFixture( "heic", "sample.heic", 1440, 960, true );
		registerFileFixture( "heif", "sample.heif", 640, 427, true );
		registerFileFixture( "avif", "sample.avif", 0, 0, true );
		registerFileFixture( "jxl", "sample.jxl", 0, 0, true );

		// Legacy fixtures used elsewhere in the suite
		registerFileFixture( "jpeg-bigben", "BigBen.jpg" );
		registerFileFixture( "jpeg-iptc", "IPTC-GoogleImgSrcPmd_testimg01.jpg" );
		registerFileFixture( "png-lucee-logo", "lucee-logo.png" );
	}

	private void function registerFileFixture(
		required string key,
		required string file,
		numeric expectedWidth=0,
		numeric expectedHeight=0,
		boolean optional=false
	){
		var path = imgDir & file;
		if ( !fileExists( path ) ) {
			return;
		}
		fixtures[ key ] = {
			path: path,
			file: file,
			expectedWidth: expectedWidth,
			expectedHeight: expectedHeight,
			optional: optional
		};
	}

	private boolean function canReadFixture( required struct fixture ){
		try {
			var img = imageRead( fixture.path );
			return isImage( img ) && imageGetWidth( img ) > 0 && imageGetHeight( img ) > 0;
		} catch ( any e ) {
			return false;
		}
	}

	function run( testResults, testBox ){
		describe( "file-based image fixtures", function(){
			it( title="core JPEG and PNG fixtures are present", body=function(){
				expect( structKeyExists( fixtures, "jpeg-small" ) ).toBeTrue();
				expect( structKeyExists( fixtures, "png-small" ) ).toBeTrue();
				expect( structKeyExists( fixtures, "jpeg-bigben" ) ).toBeTrue();
			});

			loop collection=variables.fixtures item="fixture" key="formatKey" {
				describe( "fixture [#formatKey#] (#fixture.file#)", function(){
					it(
						title="imageRead() loads [#formatKey#]",
						data={ fixture=fixture, formatKey=formatKey },
						body=function( data ){
							if ( data.fixture.optional && !canReadFixture( data.fixture ) ) {
								return; // e.g. HEIC without optional codec
							}
							var img = imageRead( data.fixture.path );
							expect( isImage( img ) ).toBeTrue();
							expect( imageGetWidth( img ) ).toBeGT( 0 );
							expect( imageGetHeight( img ) ).toBeGT( 0 );
							if ( data.fixture.expectedWidth ) {
								expect( imageGetWidth( img ) ).toBe( data.fixture.expectedWidth );
								expect( imageGetHeight( img ) ).toBe( data.fixture.expectedHeight );
							}
						}
					);

					it(
						title="isImageFile() accepts [#formatKey#]",
						data={ fixture=fixture },
						body=function( data ){
							if ( data.fixture.optional && !canReadFixture( data.fixture ) ) {
								return;
							}
							expect( isImageFile( data.fixture.path ) ).toBeTrue();
						}
					);

					it(
						title="imageInfo() reads [#formatKey#]",
						data={ fixture=fixture },
						body=function( data ){
							if ( data.fixture.optional && !canReadFixture( data.fixture ) ) {
								return;
							}
							var info = imageInfo( data.fixture.path );
							expect( info ).toBeStruct();
							expect( info.width ).toBeGT( 0 );
							expect( info.height ).toBeGT( 0 );
							expect( info ).toHaveKey( "colormodel" );
						}
					);

					it(
						title="imageResize() works on [#formatKey#]",
						data={ fixture=fixture },
						body=function( data ){
							if ( data.fixture.optional && !canReadFixture( data.fixture ) ) {
								return;
							}
							var img = imageRead( data.fixture.path );
							var targetW = min( 16, imageGetWidth( img ) );
							var targetH = min( 12, imageGetHeight( img ) );
							imageResize( img, targetW, targetH );
							expect( imageGetWidth( img ) ).toBe( targetW );
							expect( imageGetHeight( img ) ).toBe( targetH );
						}
					);

					it(
						title="imageWrite() roundtrip via PNG for [#formatKey#]",
						data={ fixture=fixture, formatKey=formatKey },
						body=function( data ){
							if ( data.fixture.optional && !canReadFixture( data.fixture ) ) {
								return;
							}
							var pngPath = getTempFile( getTempDirectory(), "roundtrip-#data.formatKey#", "png" );
							try {
								var img = imageRead( data.fixture.path );
								imageWrite( img, pngPath );
								expect( fileExists( pngPath ) ).toBeTrue();
								var roundtrip = imageRead( pngPath );
								expect( imageGetWidth( roundtrip ) ).toBe( imageGetWidth( img ) );
								expect( imageGetHeight( roundtrip ) ).toBe( imageGetHeight( img ) );
							} finally {
								if ( fileExists( pngPath ) ) fileDelete( pngPath );
							}
						}
					);
				} );
			}
		});

		describe( "format-specific checks", function(){
			it( title="EXIF metadata available on BigBen JPEG", body=function(){
				var img = imageRead( fixtures[ "jpeg-bigben" ].path );
				var info = imageInfo( img );
				expect( info ).toHaveKey( "exif" );
				expect( info.exif ).toBeStruct();
				expect( structCount( info.exif ) ).toBeGT( 0 );
			});

			it( title="IPTC metadata available on IPTC sample JPEG", body=function(){
				var meta = imageGetIptcMetadata( fixtures[ "jpeg-iptc" ].path );
				expect( meta ).toBeStruct();
				expect( structCount( meta ) ).toBeGT( 0 );
			});

			it( title="WebP large sample reads with expected dimensions", body=function(){
				var img = imageRead( fixtures[ "webp-large" ].path );
				expect( imageGetWidth( img ) ).toBe( 2560 );
				expect( imageGetHeight( img ) ).toBe( 1920 );
			});

			it( title="HEIC sample reads with expected dimensions when a codec is available", body=function(){
				if ( !structKeyExists( fixtures, "heic" ) || !canReadFixture( fixtures.heic ) ) {
					return;
				}
				var img = imageRead( fixtures.heic.path );
				expect( imageGetWidth( img ) ).toBe( 1440 );
				expect( imageGetHeight( img ) ).toBe( 960 );
			});

			it( title="HEIF sample reads with expected dimensions when a codec is available", body=function(){
				if ( !structKeyExists( fixtures, "heif" ) || !canReadFixture( fixtures.heif ) ) {
					return;
				}
				var img = imageRead( fixtures.heif.path );
				expect( imageGetWidth( img ) ).toBe( 640 );
				expect( imageGetHeight( img ) ).toBe( 427 );
			});

			it( title="AVIF sample reads when NightMonkeys is available", body=function(){
				if ( !structKeyExists( fixtures, "avif" ) || !canReadFixture( fixtures.avif ) ) {
					return;
				}
				var img = imageRead( fixtures.avif.path );
				expect( imageGetWidth( img ) ).toBeGT( 0 );
				expect( imageGetHeight( img ) ).toBeGT( 0 );
			});

			it( title="JPEG XL sample reads when NightMonkeys is available", body=function(){
				if ( !structKeyExists( fixtures, "jxl" ) || !canReadFixture( fixtures.jxl ) ) {
					return;
				}
				var img = imageRead( fixtures.jxl.path );
				expect( imageGetWidth( img ) ).toBeGT( 0 );
				expect( imageGetHeight( img ) ).toBeGT( 0 );
			});

			it( title="cross-format write PNG to JPEG and TIFF", body=function(){
				var src = imageRead( fixtures[ "png-small" ].path );
				var jpgPath = getTempFile( getTempDirectory(), "cross-format", "jpg" );
				var tifPath = getTempFile( getTempDirectory(), "cross-format", "tif" );
				try {
					imageWrite( src, jpgPath );
					imageWrite( src, tifPath );
					expect( isImage( imageRead( jpgPath ) ) ).toBeTrue();
					expect( isImage( imageRead( tifPath ) ) ).toBeTrue();
				} finally {
					if ( fileExists( jpgPath ) ) fileDelete( jpgPath );
					if ( fileExists( tifPath ) ) fileDelete( tifPath );
				}
			});
		});
	}

}
</cfscript>
