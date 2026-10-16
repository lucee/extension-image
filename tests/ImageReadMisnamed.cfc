component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	variables.imgDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "images/";
	variables.testDir = getTempDirectory( "misnamed" );

	function beforeAll(){
		// cleanup from previous runs
		if ( directoryExists( testDir ) ) directoryDelete( testDir, true );
		directoryCreate( testDir );

		var jpgSrc = imgDir & "image.jpg";
		var pngSrc = imgDir & "lucee-logo.png";
		var webpSrc = imgDir & "small-sample.webp";

		// JPEG saved with .png extension
		fileCopy( jpgSrc, testDir & "actually-jpeg.png" );
		// PNG saved with .jpg extension
		fileCopy( pngSrc, testDir & "actually-png.jpg" );
		// JPEG saved with no extension
		fileCopy( jpgSrc, testDir & "no-extension" );
		// PNG saved with .bmp extension
		fileCopy( pngSrc, testDir & "actually-png.bmp" );
		// WebP saved with .jpg extension
		fileCopy( webpSrc, testDir & "actually-webp.jpg" );
		// WebP saved with .png extension
		fileCopy( webpSrc, testDir & "actually-webp.png" );
	}

	function run( testResults, testBox ){
		describe( "imageRead with misnamed files", function(){

			it( title="reads a JPEG file saved with .png extension", body=function(){
				var img = imageRead( testDir & "actually-jpeg.png" );
				expect( isImage( img ) ).toBeTrue();
				expect( imageGetWidth( img ) ).toBeGT( 0 );
			});

			it( title="reads a PNG file saved with .jpg extension", body=function(){
				var img = imageRead( testDir & "actually-png.jpg" );
				expect( isImage( img ) ).toBeTrue();
				expect( imageGetWidth( img ) ).toBeGT( 0 );
			});

			it( title="reads a JPEG file with no extension", body=function(){
				var img = imageRead( testDir & "no-extension" );
				expect( isImage( img ) ).toBeTrue();
				expect( imageGetWidth( img ) ).toBeGT( 0 );
			});

			it( title="reads a PNG file saved with .bmp extension", body=function(){
				var img = imageRead( testDir & "actually-png.bmp" );
				expect( isImage( img ) ).toBeTrue();
				expect( imageGetWidth( img ) ).toBeGT( 0 );
			});

			it( title="imageInfo works on misnamed JPEG (.png ext)", body=function(){
				var img = imageRead( testDir & "actually-jpeg.png" );
				var info = imageInfo( img );
				expect( info.width ).toBeGT( 0 );
				expect( info.height ).toBeGT( 0 );
			});

			it( title="imageResize works on misnamed PNG (.jpg ext)", body=function(){
				var img = imageRead( testDir & "actually-png.jpg" );
				imageResize( img, 50, 50 );
				expect( imageGetWidth( img ) ).toBe( 50 );
			});

			it( title="reads a WebP file saved with .jpg extension", body=function(){
				var img = imageRead( testDir & "actually-webp.jpg" );
				expect( isImage( img ) ).toBeTrue();
				expect( imageGetWidth( img ) ).toBeGT( 0 );
			});

			it( title="reads a WebP file saved with .png extension", body=function(){
				var img = imageRead( testDir & "actually-webp.png" );
				expect( isImage( img ) ).toBeTrue();
				expect( imageGetWidth( img ) ).toBeGT( 0 );
			});

		});
	}

}
