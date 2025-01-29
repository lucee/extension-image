component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.srcImage = expandPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/IPTC-GoogleImgSrcPmd_testimg01.jpg");
	}

	function run( testResults, testBox ){
		describe( "test case for ImageGetIptcMetadata", function() {

			it(title = "Checking with ImageGetIptcMetadata()", body = function( currentSpec ){
				var img = imageRead( srcImage );
				var meta = ImageGetIptcMetadata( img );
				expect( meta ).toBeStruct();
				expect( meta ).toHaveLength( 8 );
				expect( meta ).toHaveKey( "By-line" );
				expect( meta["By-line"] ).toBe( "Jane Photosty" );
			});

			it(title = "Checking with image.getIptcMetadata()", body = function( currentSpec ){
				var img = imageRead( srcImage );
				var meta = img.getIptcMetadata();
				expect( meta ).toBeStruct();
				expect( meta ).toHaveLength( 8 );
				expect( meta ).toHaveKey( "Headline" );
				expect( meta[ "Headline" ] ).toBe( "The railway and the cars" );
			});

		});
	}

}
