<cfscript>
/**
 * HEIC/HEIF/AVIF/JXL coverage for OpenizeHeic (Java 11–21) and NightMonkeys (Java 22+).
 * Fixtures skip automatically when no bundled codec can decode them on the current JVM.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	variables.imgDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "images/";
	variables.fixtures = {
		heic: { file: "sample.heic", width: 1440, height: 960, formats: [ "HEIC" ] },
		heif: { file: "sample.heif", width: 640, height: 427, formats: [ "HEIF" ] },
		avif: { file: "sample.avif", width: 0, height: 0, formats: [ "AVIF" ] },
		jxl: { file: "sample.jxl", width: 0, height: 0, formats: [ "JXL", "JPEGXL" ] }
	};

	private string function fixturePath( required string key ){
		return imgDir & fixtures[ key ].file;
	}

	private boolean function fixturePresent( required string key ){
		return fileExists( fixturePath( key ) );
	}

	private boolean function canReadFixture( required string key ){
		if ( !fixturePresent( key ) ) {
			return false;
		}
		try {
			var img = imageRead( fixturePath( key ) );
			return isImage( img ) && imageGetWidth( img ) > 0 && imageGetHeight( img ) > 0;
		} catch ( any e ) {
			return false;
		}
	}

	private numeric function getJavaFeatureVersion(){
		return createObject( "java", "java.lang.Runtime" ).getRuntime().version().feature();
	}

	private struct function getFormatsByCodec(){
		try {
			return imageFormats( true );
		} catch ( any e ) {
			return {};
		}
	}

	private array function getDecoderFormats( required string coderShortName ){
		var byCodec = getFormatsByCodec();
		if ( !structKeyExists( byCodec, "decoder" ) ) {
			return [];
		}
		var decoders = byCodec.decoder;
		for ( var key in decoders ) {
			if ( findNoCase( coderShortName, key ) ) {
				return decoders[ key ];
			}
		}
		return [];
	}

	private boolean function decoderListsAny( required string codecName, required array formatNames ){
		var formats = getDecoderFormats( codecName );
		for ( var expected in formatNames ) {
			if ( arrayFindNoCase( formats, expected ) ) {
				return true;
			}
		}
		return false;
	}

	private string function createCodecsURI(){
		return getDirectoryFromPath( getCurrentTemplatePath() ) & "codecs";
	}

	private void function expectCodecReadsFixture( required string codec, required string fixtureKey ){
		if ( !canReadFixture( fixtureKey ) ) {
			return;
		}
		var result = _internalRequest(
			template: createCodecsURI() & "/read_fixture.cfm",
			url: {
				codec: codec,
				fixture: fixtureKey
			}
		);
		expect( result.filecontent.trim() ).toBeEmpty( result.filecontent );
	}

	function run( testResults, testBox ){
		describe( "modern image fixtures", function(){
			it( title="fixture files are present on disk", body=function(){
				for ( var key in fixtures ) {
					expect( fixturePresent( key ) ).toBeTrue( "missing tests/images/#fixtures[ key ].file#" );
				}
			});

			loop collection=variables.fixtures item="fixture" key="fixtureKey" {
				it(
					title="imageRead() loads [#fixtureKey#] when a codec is available",
					data={ fixtureKey=fixtureKey, fixture=fixture },
					body=function( data ){
						if ( !canReadFixture( data.fixtureKey ) ) {
							return;
						}
						var img = imageRead( fixturePath( data.fixtureKey ) );
						expect( isImage( img ) ).toBeTrue();
						expect( imageGetWidth( img ) ).toBeGT( 0 );
						expect( imageGetHeight( img ) ).toBeGT( 0 );
						if ( data.fixture.width > 0 ) {
							expect( imageGetWidth( img ) ).toBe( data.fixture.width );
							expect( imageGetHeight( img ) ).toBe( data.fixture.height );
						}
					}
				);

				it(
					title="isImageFile() accepts [#fixtureKey#] when readable",
					data={ fixtureKey=fixtureKey },
					body=function( data ){
						if ( !canReadFixture( data.fixtureKey ) ) {
							return;
						}
						expect( isImageFile( fixturePath( data.fixtureKey ) ) ).toBeTrue();
					}
				);

				it(
					title="imageInfo() reads [#fixtureKey#] when readable",
					data={ fixtureKey=fixtureKey },
					body=function( data ){
						if ( !canReadFixture( data.fixtureKey ) ) {
							return;
						}
						var info = imageInfo( fixturePath( data.fixtureKey ) );
						expect( info ).toBeStruct();
						expect( info.width ).toBeGT( 0 );
						expect( info.height ).toBeGT( 0 );
					}
				);
			}
		});

		describe( "modern format registration", function(){
			it( title="readable HEIC/HEIF/AVIF/JXL appear in ImageFormats()", body=function(){
				var readable = getReadableImageFormats();
				if ( canReadFixture( "heic" ) ) {
					expect( listFindNoCase( readable, "heic" ) ).toBeGT( 0 );
				}
				if ( canReadFixture( "heif" ) ) {
					expect( listFindNoCase( readable, "heif" ) ).toBeGT( 0 );
				}
				if ( canReadFixture( "avif" ) ) {
					expect( listFindNoCase( readable, "avif" ) ).toBeGT( 0 );
				}
				if ( canReadFixture( "jxl" ) ) {
					expect( listFindNoCase( readable, "jxl" ) || listFindNoCase( readable, "jpegxl" ) ).toBeTrue();
				}
			});

			it( title="OpenizeHeic registers HEIC/HEIF decoders below Java 22", body=function(){
				if ( getJavaFeatureVersion() >= 22 ) {
					return;
				}
				expect( decoderListsAny( "OpenizeHeicCoder", [ "HEIC", "HEIF" ] ) ).toBeTrue();
			});

			it( title="NightMonkeys registers modern decoders on Java 22+", body=function(){
				if ( getJavaFeatureVersion() < 22 ) {
					return;
				}
				var formats = getDecoderFormats( "NightMonkeysCoder" );
				if ( arrayIsEmpty( formats ) ) {
					return; // native libs not installed in this environment
				}
				expect(
					decoderListsAny( "NightMonkeysCoder", [ "HEIC", "HEIF", "AVIF", "JXL", "JPEGXL" ] )
				).toBeTrue();
			});
		});

		describe( "codec-isolated reads", function(){
			it( title="OpenizeHeic reads HEIC on Java 11–21", body=function(){
				if ( getJavaFeatureVersion() >= 22 || !canReadFixture( "heic" ) ) {
					return;
				}
				expectCodecReadsFixture( "OpenizeHeic", "heic" );
			});

			it( title="OpenizeHeic reads HEIF on Java 11–21", body=function(){
				if ( getJavaFeatureVersion() >= 22 || !canReadFixture( "heif" ) ) {
					return;
				}
				expectCodecReadsFixture( "OpenizeHeic", "heif" );
			});

			it( title="NightMonkeys reads HEIC on Java 22+", body=function(){
				if ( getJavaFeatureVersion() < 22 || !canReadFixture( "heic" ) ) {
					return;
				}
				expectCodecReadsFixture( "NightMonkeys", "heic" );
			});

			it( title="NightMonkeys reads AVIF on Java 22+", body=function(){
				if ( getJavaFeatureVersion() < 22 || !canReadFixture( "avif" ) ) {
					return;
				}
				expectCodecReadsFixture( "NightMonkeys", "avif" );
			});

			it( title="NightMonkeys reads JXL on Java 22+", body=function(){
				if ( getJavaFeatureVersion() < 22 || !canReadFixture( "jxl" ) ) {
					return;
				}
				expectCodecReadsFixture( "NightMonkeys", "jxl" );
			});
		});
	}

}
</cfscript>
