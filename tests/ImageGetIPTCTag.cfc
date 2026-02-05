component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.srcImage = expandPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/IPTC-GoogleImgSrcPmd_testimg01.jpg");
	}

	function run( testResults, testBox ){
		describe( "test case for ImageGetIPTCTag", function() {

			it(title = "Checking with ImageGetIPTCTag() - By-line", body = function( currentSpec ){
				var img = imageRead( srcImage );
				var byline = ImageGetIPTCTag( img, "By-line" );
				expect( byline ).toBe( "Jane Photosty" );
			});

			it(title = "Checking with image.getIPTCTag() - Headline", body = function( currentSpec ){
				var img = imageRead( srcImage );
				var headline = img.getIPTCTag( "Headline" );
				expect( headline ).toBe( "The railway and the cars" );
			});

			it(title = "Checking with ImageGetIPTCTag() - non-existent tag", body = function( currentSpec ){
				var img = imageRead( srcImage );
				expect( function(){
					ImageGetIPTCTag( img, "NonExistentTag" );
				}).toThrow();
			});

			it(title = "Checking image with no IPTC metadata", body = function( currentSpec ){
				var img = imageNew( "", 100, 100, "rgb" );
				expect( function(){
					ImageGetIPTCTag( img, "By-line" );
				}).toThrow( message="This image does not contain any IPTC metadata" );
			});

		});
	}

}
