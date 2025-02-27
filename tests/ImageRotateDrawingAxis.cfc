component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = getTempDirectory() & "imageRotateDrawingAxis/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}

	function run( testResults, testBox ) {
		describe( "test case for ImageRotate", function() {

			it(title = "Checking with imageRotateDrawingAxis()",body = function( currentSpec ){
				var img = imageNew("",150,150,"RGB","4a69bd");
				imageRotateDrawingAxis(img,135,71,71);
				imageDrawLines(img,[30,70,90,50],[90,10,100,130],"yes","yes" );
				cfimage(action = "write", source = img, destination = path&'imageRotateDrawingAxis.png', overwrite = "yes");
				expect(fileExists(path&"imageRotateDrawingAxis.png")).tobe("true");
			});

			it(title = "Checking with image.rotateDrawingAxis()",body = function( currentSpec ){
				var img = imageNew("",150,150,"RGB","red");
				img = img.rotateDrawingAxis(135,91,61);
				imageDrawLines(img,[80,70,90,50],[90,10,100,130],"yes","yes" );
				cfimage(action = "write", source = img, destination = path&'imageRotateDrawingAxisNew.png', overwrite = "yes");
				expect(fileExists(path&"imageRotateDrawingAxisNew.png")).tobe("true");
			});
		});
	}

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}