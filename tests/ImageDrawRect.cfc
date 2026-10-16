component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = getTempDirectory() & "ImageDrawRect/";
		if(!directoryExists(path)){
			directoryCreate(path);
		}
	}

	function run( testResults, testBox ) {
		describe( "test case for ImageDrawRect", function() {

			it(title = "Checking with ImageDrawRect, filled true", body = function( currentSpec ){
				var img = imageNew("",150,150,"rgb","149c82");
				imageDrawRect(image=img,x=30,y=30,width=40,height=30,filled="yes");
				cfimage(action = "write", source = img, destination = path&'imgDrawRect-filled.jpg', overwrite = "yes");
				expect( fileExists(path&'imgDrawRect-filled.jpg')).toBeTrue();
			});

			it(title = "Checking with image.drawRect() filled true", body = function( currentSpec ){
				var img = imageNew("",150,150,"rgb","149c82");
				img.DrawRect(100,100,50,30,"yes");
				cfimage(action = "write", source = img, destination = path&'objDrawRect-filled.jpg', overwrite = "yes");
				expect( fileExists(path&'objDrawRect-filled.jpg')).toBeTrue();
			});

			it(title = "Checking with ImageDrawRect, filled false", body = function( currentSpec ){
				var img = imageNew("",150,150,"rgb","149c82");
				imageDrawRect(image=img,x=30,y=30,width=40,height=30,filled="false");
				cfimage(action = "write", source = img, destination = path&'imgDrawRect-outline.jpg', overwrite = "yes");
				expect( fileExists(path&'imgDrawRect-outline.jpg')).toBeTrue();
			});

			it(title = "Checking with image.drawRect() filled false", body = function( currentSpec ){
				var img = imageNew("",150,150,"rgb","149c82");
				img.DrawRect(100,100,50,30,"false");
				cfimage(action = "write", source = img, destination = path&'objDrawRect-outline.jpg', overwrite = "yes");
				expect( fileExists(path&'objDrawRect-outline.jpg')).toBeTrue();
			});

		});
	}

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}