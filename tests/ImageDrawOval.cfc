component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = getTempDirectory() & "ImageDrawOval/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}

	function run( testResults, testBox ){
		describe( "test case for ImageDrawOval", function() {

			it(title = "Checking with ImageDrawOval", body = function( currentSpec ){
				var img = imageNew("",200,200,"rgb","red");
				imageSetDrawingColor(img, "white");
				ImageDrawOval(img, 50,50,70,30,"no");
				cfimage(action = "write", source = img, destination = path&'imgDrawOval.jpg', overwrite = "yes");
				expect(fileexists(path&'imgDrawOval.jpg')).tobe("true");
			});

			it(title = "Checking with image.drawOval()", body = function( currentSpec ){
				var img = imageNew("",200,200,"rgb","green");
				img.setDrawingColor("blue");
				img.drawOval(80,100,70,20,"yes");
				cfimage(action = "write", source = img, destination = path&'objDrawOval.jpg', overwrite = "yes");
				expect(fileexists(path&'objDrawOval.jpg')).tobe("true");
			});

		});
	}
	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}