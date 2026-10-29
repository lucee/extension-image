component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	// LDEV-3327: the JVM wide ImageIO disk cache is off by default and can be re-enabled with
	// the system property lucee.image.use.cache or the environment variable LUCEE_IMAGE_USE_CACHE

	function beforeAll() {
		variables.srcImage = expandPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/IPTC-GoogleImgSrcPmd_testimg01.jpg" );
		variables.path = getTempDirectory() & "LDEV3327/";
		if ( !directoryExists( path ) ) directoryCreate( path );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3327 ImageIO temp file cache", function() {

			it( title="the ImageIO cache follows the opt-out setting after imageRead()", body=function( currentSpec ) {
				var img = imageRead( srcImage );
				expect( isImage( img ) ).toBeTrue();
				expect( getImageIOUseCache() ).toBe( expectedUseCache() );
			});

			it( title="the ImageIO cache follows the opt-out setting after imageWrite()", body=function( currentSpec ) {
				var img = imageRead( srcImage );
				imageWrite( img, path & "write.png", true );
				expect( fileExists( path & "write.png" ) ).toBeTrue();
				expect( getImageIOUseCache() ).toBe( expectedUseCache() );
			});

			it( title="the ImageIO cache follows the opt-out setting after imageCaptcha()", body=function( currentSpec ) {
				var img = imageCaptcha( "abcdec", 200, 500, "high" );
				expect( isImage( img ) ).toBeTrue();
				imageWrite( img, path & "captcha.jpg", true );
				expect( fileExists( path & "captcha.jpg" ) ).toBeTrue();
				expect( getImageIOUseCache() ).toBe( expectedUseCache() );
			});

			it( title="the ImageIO cache follows the opt-out setting after imageGetBlob() and imageGetEXIFMetadata()", body=function( currentSpec ) {
				var img = imageRead( srcImage );
				expect( len( imageGetBlob( img ) ) ).toBeGT( 0 );
				imageGetEXIFMetadata( img );
				expect( getImageIOUseCache() ).toBe( expectedUseCache() );
			});

			it( title="the ImageIO cache is off by default", skip=optedIn(), body=function( currentSpec ) {
				imageRead( srcImage );
				expect( getImageIOUseCache() ).toBeFalse();
			});

			it( title="the ImageIO cache is on when LUCEE_IMAGE_USE_CACHE or lucee.image.use.cache is true", skip=!optedIn(), body=function( currentSpec ) {
				imageRead( srcImage );
				expect( getImageIOUseCache() ).toBeTrue();
			});

		});
	}

	function afterAll() {
		if ( ( server.system.environment.TEST_CLEANUP ?: true ) && directoryExists( path ) ) {
			directoryDelete( path, true );
		}
	}

	private boolean function getImageIOUseCache() {
		return createObject( "java", "javax.imageio.ImageIO" ).getUseCache();
	}

	// the same rule as the extension: the system property wins over the environment variable, only "true" turns it on
	private boolean function optedIn() {
		var value = trim( server.system.properties[ "lucee.image.use.cache" ] ?: "" );
		if ( !len( value ) ) value = trim( server.system.environment[ "LUCEE_IMAGE_USE_CACHE" ] ?: "" );
		return value == "true";
	}

	private boolean function expectedUseCache() {
		return optedIn();
	}

}
