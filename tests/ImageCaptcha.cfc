component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = getTempDirectory() & "ImageCaptcha/";
		if(!directoryExists(path)){
			directoryCreate(path);
		}
	}

	function run( testResults, testBox ){
		describe( "test case for ImageCaptcha", function() {

			it(title = "Checking with ImageCaptcha()", body = function( currentSpec ){
				var img = imageCaptcha( "abcdec", 200, 500, "high");
				expect( isImage(img )) .toBeTrue();
				imageWrite(img, path & 'captcha.jpg', true );
				expect( fileExists( path & 'captcha.jpg') ).toBe( true );

				loop list="low,medium,high" item="local.difficulty" {
					var img = imageCaptcha( "lucee is not lucy", 100, 300, difficulty );
					expect( isImage( img ) ).toBeTrue();
					imageWrite(img, path & 'captcha-#difficulty#.jpg', true );
					expect( fileExists( path & 'captcha-#difficulty#.jpg') ).toBe( true );
				}
			});

		});
	}

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}

}
