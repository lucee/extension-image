component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = getTempDirectory() & "ImageDrawText/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}

	function run( testResults, testBox ){
		describe( "test case for ImageDrawText()", function() {

			it(title = "Checking with ImageDrawText()", body = function( currentSpec ){
				var img = imageNew("",150,150,"RGB","149c82");
				ImageDrawText(img, "I Love Lucee",40,70);
				cfimage(action = "write", source = img, destination = path&'imgDrawtext.jpg', overwrite = "yes");
				expect(fileexists(path&'imgDrawtext.jpg')).tobe("true");
			});

			it(title = "Checking with Image.DrawText()", body = function( currentSpec ){
				var img = imageNew("",200,200,"RGB","0000BB");
				var aCollection = { style="BOLD", underline = "true", size = "23", font="Arial Black" };
				img = img.DrawText("Save Tree!!!",40,30,aCollection);
				cfimage(action = "write", source = img, destination = path&'objDrawText.jpg', overwrite="yes");
				expect(fileExists(path&'objDrawText.jpg')).tobe("true");
			});

		});
	}

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}