component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	variables.imgDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "images/";

	function run( testResults, testBox ) {
		describe( "LDEV-6083 Drew auto-detect extends EXIF coverage beyond JPEG/TIFF", function() {

			it( title="imageGetEXIFMetadata on WebP without EXIF returns Drew-detected format fields", body=function() {
				var img = imageRead( imgDir & "small-sample.webp" );
				var meta = imageGetEXIFMetadata( img );
				expect( meta ).toBeStruct();
				expect( meta.DetectedMIMEType ).toBe( "image/webp" );
				expect( meta.DetectedFileTypeName ).toBe( "WebP" );
				expect( meta.ImageWidth ).notToBeEmpty();
				expect( meta.ImageHeight ).notToBeEmpty();
			});

			it( title="imageGetEXIFMetadata on WebP with EXIF returns camera fields", body=function() {
				var img = imageRead( imgDir & "nikon-d5300-exif.webp" );
				var meta = imageGetEXIFMetadata( img );
				expect( meta ).toBeStruct();
				expect( meta.DetectedMIMEType ).toBe( "image/webp" );
				expect( meta.Make ).toBe( "NIKON CORPORATION" );
				expect( meta.Model ).toBe( "NIKON D5300" );
				expect( meta.Software ).toBe( "GIMP 2.9.5" );
				expect( meta.Orientation ).toBe( 1 );
				expect( meta ).toHaveKey( "exif" );
				expect( meta.exif ).toBeStruct();
				expect( structCount( meta.exif ) ).toBeGT( 100 );
			});

			it( title="imageGetEXIFMetadata on PNG without EXIF returns Drew-detected format fields", body=function() {
				var img = imageRead( imgDir & "lucee-logo.png" );
				var meta = imageGetEXIFMetadata( img );
				expect( meta ).toBeStruct();
				expect( meta.DetectedMIMEType ).toBe( "image/png" );
				expect( meta.DetectedFileTypeName ).toBe( "PNG" );
				expect( meta ).toHaveKey( "ColorType" );
				expect( meta ).toHaveKey( "BitsPerSample" );
				expect( meta ).toHaveKey( "CompressionType" );
			});

			it( title="imageGetEXIFMetadata on PNG with EXIF returns exif struct", body=function() {
				var img = imageRead( imgDir & "with-exif.png" );
				var meta = imageGetEXIFMetadata( img );
				expect( meta ).toBeStruct();
				expect( meta.DetectedMIMEType ).toBe( "image/png" );
				expect( meta ).toHaveKey( "exif" );
				expect( meta.exif ).toBeStruct();
			});

		});
	}
}
