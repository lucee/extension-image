component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function run( testResults, testBox ){
		describe( "test case for ImageRead()", function() {

			it(title = "Checking with ImageRead(path) ", body = function( currentSpec ){
				var testImagePath = expandPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/lucee-logo.png");
				var img = imageRead(testImagePath);
				expect( isImage( img ) ).toBeTrue();
			});

			it(title = "Checking with ImageRead(image) ", body = function( currentSpec ){
				var obj = imageNew("", 200, 200, "rgb", "white");
				var img = imageRead( obj );
				expect( isImage( img ) ).toBeTrue();
			});

			it(title = "Checking with ImageRead(url) ", body = function( currentSpec ){
				var img = imageRead( "https://www.lucee.org/assets/img/logo.png" );
				expect( isImage( img ) ).toBeTrue();
			});

			it(title = "should throw an error for non-existent images", body=function() {
				expect(function() {
					ImageRead("non-existent-image.jpg");
				}).toThrow();
			});

		});
	}

}
