component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = getTempDirectory() & "ImageDrawRoundRect/";
		if(!directoryExists(path)){
			directoryCreate(path);
		}
	}

	function run( testResults, testBox ) {
		describe( "test case for ImageDrawRoundRect", function() {

			it(title = "Checking with ImageDrawRoundRect, filled true", body = function( currentSpec ){
				var img = imageNew("",150,150,"rgb","149c82");
				imageDrawRoundRect(image=img,x=30,y=30,width=40,height=30,arcWidth=25, arcHeight=25, filled="yes");
				cfimage(action = "write", source = img, destination = path&'imgDrawRoundRect-filled.jpg', overwrite = "yes");
				expect( fileExists(path&'imgDrawRoundRect-filled.jpg')).toBeTrue();
			});

			it(title = "Checking with image.drawRoundRect() filled true", body = function( currentSpec ){
				var img = imageNew("",150,150,"rgb","149c82");
				img = img.DrawRoundRect(100,100,50,30,50,50, "yes");
				cfimage(action = "write", source = img, destination = path&'objDrawRoundRect-filled.jpg', overwrite = "yes");
				expect( fileExists(path&'objDrawRoundRect-filled.jpg')).toBeTrue();
			});

			it(title = "Checking with ImageDrawRoundRect, filled false", body = function( currentSpec ){
				var img = imageNew("",150,150,"rgb","149c82");
				imageDrawRoundRect(image=img,x=30,y=30,width=40,height=30,arcWidth=25, arcHeight=25, filled="false");
				cfimage(action = "write", source = img, destination = path&'imgDrawRoundRect-outline.jpg', overwrite = "yes");
				expect( fileExists(path&'imgDrawRoundRect-outline.jpg')).toBeTrue();
			});

			it(title = "Checking with image.drawRoundRect() filled false", body = function( currentSpec ){
				var img = imageNew("",150,150,"rgb","149c82");
				img = img.DrawRoundRect(100,100,50,30,50,50,"false");
				cfimage(action = "write", source = img, destination = path&'objDrawRoundRect-outline.jpg', overwrite = "yes");
				expect( fileExists(path&'objDrawRoundRect-outline.jpg')).toBeTrue();
			});

		});
	}

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}