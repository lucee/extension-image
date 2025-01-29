component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = getTempDirectory() & "ImageTranslate/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
		variables.srcImage = expandPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/image.jpg");
	}

	function run( testResults, testBox ){
		describe( "test case for ImageTranslate", function() {

			it(title = "Checking with ImageTranslate()", body = function( currentSpec ){
				var img = imageRead( srcImage );
				imageTranslate( img,400,400 );
				cfimage(action = "write", source = img, destination = path&'imgTranslate.jpg',overwrite = "yes");
				expect(fileExists(path&'imgTranslate.jpg')).toBe("true");

				loop list="nearest,bilinear,bicubic" item="local.interpolation" {
					var img = imageRead( srcImage );
					imageTranslate(img,200,300, interpolation);
					cfimage(action = "write", source = img, destination = path&'imgTranslate-#interpolation#.jpg',overwrite = "yes");
					expect(fileExists(path&'imgTranslate-#interpolation#.jpg')).toBe("true");
				}
			});

			it(title = "Checking with image.translate()", body = function( currentSpec ){
				var img = imageNew("",200,200,"rgb","blue");
				img.Translate(100,100);
				cfimage(action = "write", source = img, destination = path&'objTranslate.jpg',overwrite = "yes");
				expect(fileExists(path&'objTranslate.jpg')).toBe("true");
			});

		});
	}

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}
