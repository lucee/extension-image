component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll() {
		variables.path = getTempDirectory() & "ImageDrawCubicCurve/";
		if(!directoryExists(path)) {
			directoryCreate(path);
		}
	}

	function run( testResults, testBox ) {
		describe( "Testcase for imageDrawCubicCurve()", function() {

			it(title="Checking with imageDrawCubicCurve() function", body=function( currentSpec ) {
				var imgDraw = imageNew("", 150, 150, "rgb", "149c82");
				imageDrawCubicCurve(imgDraw, 0, 0, 45, 15, 50, 75, 0, 100);
				cfimage(action="write", source=imgDraw, destination=path&'imgDrawCubicCurve.jpg', overwrite="yes");
				expect(fileExists(path&'imgDrawCubicCurve.jpg')).tobe("true");
			});

			it(title="Checking with image.drawCubicCurve()", body=function( currentSpec ) {
				var img = imageNew("", 400, 400);
				img.drawCubicCurve(0, 45, 45, 75, 40, 75, 0, 100);
				cfimage(action="write", source=img, destination=path&'objDrawCubicCurve.jpg', overwrite="yes");
				expect(fileExists(path&'objDrawCubicCurve.jpg')).tobe("true");
			});
		});
	}

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}