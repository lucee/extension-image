component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = getTempDirectory() & "imagePaste/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}

	function run( testResults , testBox ) {
		describe( "test case for imagePaste", function() {

			it(title = "Checking with imagePaste", body = function( currentSpec ) {
				var img_First = imageNew("",200,200,"rgb","red");
				var img_Second = imageNew("",100,100,"rgb","green");
				imagePaste(img_First,img_Second,50,100);
				cfimage(action = "write", source = img_First, destination = path&"pasteImg.jpg");
			  	expect(fileExists(path&"pasteImg.jpg")).tobe("true");
			});

			it(title = "Checking with image.Paste()", body = function( currentSpec ) {
				var imgObj = imageNew("",100,100,"rgb","green");
				var newImg = imageNew("",100,100,"rgb","yellow");
				imgObj= imgObj.paste(newImg,25,50);
				cfimage(action = "write", source = imgObj , destination = path&"objPasteImage.jpg");
			  	expect(fileExists(path&"objPasteImage.jpg")).tobe("true");
			});

		});
	};

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}	
}
		