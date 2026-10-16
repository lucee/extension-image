component extends="org.lucee.cfml.test.LuceeTestCase" labels="image2" {

	function beforeAll(){
		variables.path = getTempDirectory() & "imageTranslateDrawingAxis/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
		variables.srcImage = expandPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/image.jpg");
	}

	function run( testResults, testBox ){
		describe( "test case for imageTranslateDrawingAxis", function() {

			it(title = "Checking with imageTranslateDrawingAxis()", body = function( currentSpec ){
				var img = imageRead( srcImage );
				img.drawRect(50,50,70,50,"yes");
				imageTranslateDrawingAxis( img, 400,400 );
				img.drawRect(50,50,70,50,"yes");
				cfimage(action = "write", source = img, destination = path&'imgTranslateDrawingAxis.jpg',overwrite = "yes");
				expect(fileExists(path&'imgTranslateDrawingAxis.jpg')).toBe(true);
			});

			it(title = "Checking with image.translate()", body = function( currentSpec ){
				var img = imageNew("",200,200,"rgb","blue");
				img.drawRect(50,50,70,50,"yes");
				img.TranslateDrawingAxis(100,100);
				img.drawRect(50,50,70,50,"yes");
				cfimage(action = "write", source = img, destination = path&'objTranslateDrawingAxis.jpg',overwrite = "yes");
				expect(fileExists(path&'objTranslateDrawingAxis.jpg')).toBe(true);
			});

		});
	}

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}
