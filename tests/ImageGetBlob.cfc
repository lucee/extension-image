component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.srcImage = expandPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/IPTC-GoogleImgSrcPmd_testimg01.jpg");
	}

	function run( testResults, testBox ){
		describe( "test case for ImageGetBlob", function() {

			it(title = "Checking with ImageGetBlob()", body = function( currentSpec ){
				var img = imageRead( srcImage );
				var blob = ImageGetBlob( img );
				expect( blob.getClass().getName() ).toBe( "[B" );
			});

			it(title = "Checking with image.getBlob()", body = function( currentSpec ){
				var img = imageRead( srcImage );
				var blob = img.getBlob();
				expect( blob.getClass().getName() ).toBe( "[B" );
			});

		});
	}

}
