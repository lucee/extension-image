component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = getTempDirectory() & "ImageDrawPoint/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}

	function run( testResults, testBox ){
		describe( "test case for ImageDrawPoint", function() {

			it(title = "Checking with ImageDrawOval", body = function( currentSpec ){
				var img = imageNew("",200,200,"rgb","red");
				imageSetDrawingColor(img, "white");
				imageDrawPoint(img,90,90);
				cfimage(action = "write", source = img, destination = path&'imgDrawPoint.jpg',overwrite = "yes");
				expect(fileexists(path&'imgDrawPoint.jpg')).toBe("true");
			});

			it(title = "Checking with image.drawOval()", body = function( currentSpec ){
				var img = imageNew("",200,200,"rgb","blue");
				img.setDrawingColor("white");
				img.DrawPoint(100,130);
				cfimage(action = "write", source = img, destination = path&'objDrawPoint.jpg',overwrite = "yes");
				expect(fileexists(path&'objDrawPoint.jpg')).toBe("true");
			});

		});
	}

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}
