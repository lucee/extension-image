component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.srcImage = expandPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/IPTC-GoogleImgSrcPmd_testimg01.jpg");
	}

	function run( testResults, testBox ){
		describe( "test case for ImageGetBufferedImage", function() {

			it(title = "Checking with ImageGetBufferedImage()", body = function( currentSpec ){
				var img = imageRead( srcImage );
				var bi = ImageGetBufferedImage( img );
				expect( bi.getClass().getName() ).toBe( "java.awt.image.BufferedImage" );
			});

			it(title = "Checking with image.getBufferedImage()", body = function( currentSpec ){
				var img = imageRead( srcImage );
				var bi = img.getBufferedImage();
				expect( bi.getClass().getName() ).toBe( "java.awt.image.BufferedImage" );
			});

		});
	}

}
