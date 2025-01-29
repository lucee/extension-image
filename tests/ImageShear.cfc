component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = getTempDirectory() & "ImageShear/";
		if(!directoryExists(path)){
			directoryCreate(path);
		}
	}

	function run( testResults, testBox ) {
		describe( "test case for ImageShear", function() {

			it(title = "Checking with ImageShear", body = function( currentSpec ) {
				var newimg = imageNew("",152,152,"rgb","A52A2A");
				ImageShear(newimg,1.1,"vertical");
				cfimage( action = "write", source = newimg, destination = path&'sheardrawImg.jpg', overwrite = "yes");
			  	expect(fileExists(path&"sheardrawImg.jpg")).tobe("true");
			});

			it(title = "Checking with Image.Shear()", body = function( currentSpec ) {
				var imgObj = imageNew("",200,200,"rgb","149c82");
				imgObj.shear(0.4,"horizontal");
				cfimage( action = "write", source = imgObj, destination = path&'sheardrawObj.png', overwrite = "yes" );
			  	expect( fileExists(path&"sheardrawObj.png") ).tobe("true");
			});

		});
	};

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}