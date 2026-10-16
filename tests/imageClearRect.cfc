component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll() {
		variables.path = getTempDirectory() & "imageClearRect/";
		if(!directoryExists(path)){
			directoryCreate(path);
		}
	}

	function run( testResults , testBox ) {
		describe( title = "Test suite for imageClearRect", body = function() {

			it( title = 'Checking with imageClearRect()',body = function( currentSpec ) {
				var img = imageNew("", 400, 400, "rgb", "white");
				imageSetBackGroundColor(img,"red");
				ImageClearRect(img,100,100,100,100);
				cfimage(action = "write", source = img, destination = path&".\rect.png", overwrite = "yes");
				assertEquals(fileExists(path&".\rect.png"),"true");
			});

			it( title = 'Checking with image.ClearRect()', body = function( currentSpec ) {
				var img = imageNew("", 400, 400, "rgb", "white");
				img.setBackGroundColor("red");
				img.ClearRect(100,100,100,100);
				cfimage(action = "write", source = img, destination = path&".\member-rect.png", overwrite = "yes");
				assertEquals(fileExists(path&".\member-rect.png"),"true");
			});

		});
	}

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}