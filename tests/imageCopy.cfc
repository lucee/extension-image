component extends="org.lucee.cfml.test.LuceeTestCase" labels="image"{

	function beforeAll(){
		variables.path = getTempDirectory() & "imageCopy/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}

	function run( testResults , testBox ) {
		describe( "test case for imageCopy", function() {

			it(title = "Checking with imageCopy()", body = function( currentSpec ) {
				newImg = imageNew("",200,200,"rgb","red");
			  	copiedImg = imageCopy(newImg,50,50,50,50);
			  	cfimage(action = "write", source = copiedImg, destination = path&"funcopyImg.jpg", overwrite="yes");
			  	expect(fileExists(path&"funcopyImg.jpg")).tobe("true");
			});

			it(title = "Checking with image.Copy()", body = function( currentSpec ) {
				imgnew = imageNew("",100,100,"rgb","blue");
 				copyImg = imgnew.copy(50,50,25,50);
			  	cfimage(action = "write", source = copyImg, destination = path&"objcopyImg.jpg", overwrite="yes");
			  	expect(fileExists(path&"objcopyImg.jpg")).tobe("true");
			});

		});
	}

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}
		