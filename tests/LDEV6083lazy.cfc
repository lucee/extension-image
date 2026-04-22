component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	variables.imgDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "images/";

	function run( testResults, testBox ){
		describe( "LDEV-6083 lazy BufferedImage decode — metadata paths must not force decode", function(){

			it( title="imageRead alone does not decode the BufferedImage", body=function(){
				var img = imageRead( imgDir & "BigBen.jpg" );
				expect( img.isDecoded() ).toBeFalse( "imageRead should defer decode until pixel data is needed" );
			});

			it( title="imageInfo does not force decode", body=function(){
				var img = imageRead( imgDir & "BigBen.jpg" );
				var info = imageInfo( img );
				expect( info ).toHaveKey( "width" );
				expect( info ).toHaveKey( "colormodel" );
				expect( img.isDecoded() ).toBeFalse( "imageInfo should read from headers without decoding pixels" );
			});

			it( title="imageGetEXIFMetadata does not force decode", body=function(){
				var img = imageRead( imgDir & "BigBen.jpg" );
				imageGetEXIFMetadata( img );
				expect( img.isDecoded() ).toBeFalse( "EXIF comes from Drew header parse, no pixels needed" );
			});

			it( title="imageGetWidth / imageGetHeight do not force decode", body=function(){
				var img = imageRead( imgDir & "BigBen.jpg" );
				var w = imageGetWidth( img );
				var h = imageGetHeight( img );
				expect( w ).toBeGT( 0 );
				expect( h ).toBeGT( 0 );
				expect( img.isDecoded() ).toBeFalse( "dimensions come from ImageReader header, no pixels needed" );
			});

			it( title="imageResize forces decode (control)", body=function(){
				var img = imageRead( imgDir & "BigBen.jpg" );
				expect( img.isDecoded() ).toBeFalse();
				imageResize( img, 100, 100 );
				expect( img.isDecoded() ).toBeTrue( "pixel ops must decode" );
			});

			it( title="metadata then transform produces correct dimensions", body=function(){
				var img = imageRead( imgDir & "BigBen.jpg" );
				var before = imageInfo( img );
				imageResize( img, 100, 100 );
				var after = imageInfo( img );
				expect( after.width ).toBe( 100 );
				expect( after.height ).toBe( 100 );
				expect( after.width ).notToBe( before.width );
			});

			it( title="isDecoded works on WebP too", body=function(){
				var img = imageRead( imgDir & "nikon-d5300-exif.webp" );
				var meta = imageGetEXIFMetadata( img );
				expect( meta.Make ).toBe( "NIKON CORPORATION" );
				expect( img.isDecoded() ).toBeFalse();
			});

		});
	}
}
