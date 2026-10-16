component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = getTempDirectory() & "ImageDrawQuadraticCurve/";
		if(!directoryExists(path)){
			directoryCreate(path);
		}
	}

	function run( testResults, testBox ) {
		describe( "test case for ImageDrawQuadraticCurve", function() {

			it(title = "Checking with ImageDrawQuadraticCurve", body = function( currentSpec ){
				var newImg = imageNew("",150,150,"rgb","black");
				ImageDrawQuadraticCurve(newImg, 10, 10, 25, 45, 130, 80);
				cfimage(action = "write", source = newImg, destination = path&'DrawQuadraticCurve.jpg', overwrite="yes");
				expect(fileExists(path&'DrawQuadraticCurve.jpg')).toBe("true");
			});

			it(title = "Checking with image.drawQuadraticCurve()", body = function( currentSpec ){
				var newImg = imageNew("",150,150,"rgb","black");
				newImg.drawQuadraticCurve(10, 10, 25, 45, 130, 80);
				cfimage(action = "write", source = newImg, destination = path&'objDrawQuadraticCurve.jpg', overwrite="yes");
				expect(fileExists(path&'DrawQuadraticCurve.jpg')).toBe("true");
			});

		});
	}

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}