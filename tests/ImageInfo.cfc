component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	function run( testResults , testBox ) {
		describe( "testcase for imageInfo()", function() {
			it(title = "Checking with imageInfo()", body = function( currentSpec ) {
				var img = imageNew("", 200, 200, "rgb", "red");
				expect(imageInfo(img)).toHaveKey("colormodel");
				expect(imageInfo(img).colormodel.colorspace).toBe("Any of the family of RGB color spaces");
				expect(imageInfo(img).width).toBe("200");
				expect(img.info().width).toBe("200");
			});

			it(title = "imageInfo() should not log NPE on JPEG without EXIF", body = function( currentSpec ) {
				var img = imageRead( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/BigBen-no-exif.jpg" );
				var info = imageInfo( img );
				expect( info ).toBeStruct();
				expect( info ).toHaveKey( "colormodel" );
				expect( info ).toHaveKey( "width" );
				expect( info ).toHaveKey( "height" );
				expect( info ).notToHaveKey( "exif" );
			});

			it(title = "imageInfo() should not throw NPE on JPEG with corrupt EXIF", body = function( currentSpec ) {
				var img = imageRead( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/BigBen-null-exif.jpg" );
				var info = imageInfo( img );
				expect( info ).toBeStruct();
				expect( info ).toHaveKey( "colormodel" );
				expect( info ).toHaveKey( "width" );
				expect( info ).toHaveKey( "height" );
				// Corrupt EXIF may not be recognized as JpegImageMetadata, so exif key may not exist
			});

			it(title = "imageInfo() should handle JPEG with EXIF data", body = function( currentSpec ) {
				var img = imageRead( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/BigBen.jpg" );
				var info = imageInfo( img );
				expect( info ).toBeStruct();
				expect( info ).toHaveKey( "colormodel" );
				expect( info ).toHaveKey( "width" );
				expect( info ).toHaveKey( "height" );
				expect( info ).toHaveKey( "exif" );
				expect( info.exif ).toBeStruct();
				expect( structCount( info.exif ) ).toBeGT( 0, "EXIF struct should have data" );
			});

			it(title = "imageInfo() with member function should not throw NPE on PNG", body = function( currentSpec ) {
				var img = imageRead( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/lucee-logo.png" );
				var info = img.info();
				expect( info ).toBeStruct();
				expect( info ).toHaveKey( "colormodel" );
			});
		});
	}
}