component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = getTempDirectory() & "ImageSharpen/";
		if(!directoryExists(path)){
			directoryCreate(path);
		}
		variables.srcImage = expandPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/image.jpg");
	}

	function run( testResults, testBox ) {
		describe( "test case for ImageSharpen", function() {

			it(title = "Checking with ImageSharpen", body = function( currentSpec ){
				var img = imageRead( variables.srcImage );
				ImageSharpen(img, -1);
				cfimage(action = "write", source = img, destination = path&'Sharpen.jpg', overwrite="yes");
				expect(fileExists(path&'Sharpen.jpg')).toBe(true);
			});

			it(title = "Checking with image.Sharpen() - blurFactor", body = function( currentSpec ){
				loop from=-1 to=1 step=0.5 index="local.gain" {
					var img = imageRead( variables.srcImage );
					img = img.Sharpen(gain);
					cfimage(action = "write", source = img, destination = path&'objSharpen-gain-#gain#.jpg', overwrite="yes");
					expect(fileExists(path&'objSharpen-gain-#gain#.jpg')).toBe(true);
				}
			});

		});
	}

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}