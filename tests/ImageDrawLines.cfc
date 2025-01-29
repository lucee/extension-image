component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = getTempDirectory() & "ImageDrawLines/";
		if(!directoryExists(path)){
			directoryCreate(path);
		}
		variables.xcoords = [10,50,100,50];
		variables.ycoords = [100,10,100,152];
	}

	function run( testResults, testBox ) {
		describe( "test case for ImageDrawLines", function() {

			it(title = "Checking with ImageDrawLines, filled=true, isPolygon=true", body = function( currentSpec ){
				var img = imageNew("",150,150,"rgb","blue");
				imageDrawLines(image=img,xcoords=xcoords,ycoords=ycoords,filled="yes", isPolygon=true);
				cfimage( action="write", source=img, destination=path & 'imgDrawLines-filled-polygon.jpg', overwrite="yes");
				expect( fileExists(path&'imgDrawLines-filled-polygon.jpg')).toBeTrue();
			});

			it(title = "Checking with image.drawLines() filled=true, isPolygon=true", body = function( currentSpec ){
				var img = imageNew("",150,150,"rgb","blue");
				img.drawLines(xcoords,ycoords,true, true);
				cfimage( action="write", source=img, destination=path & 'objDrawLines-filled-polygon.jpg', overwrite="yes");
				expect( fileExists(path&'objDrawLines-filled-polygon.jpg')).toBeTrue();
			});

			it(title = "Checking with ImageDrawLines, filled=true, isPolygon=false", body = function( currentSpec ){
				var img = imageNew("",150,150,"rgb","blue");
				imageDrawLines(image=img,xcoords=xcoords,ycoords=ycoords,isPolygon=false, filled=true);
				cfimage( action="write", source=img, destination=path & 'imgDrawLines-filled.jpg', overwrite="yes");
				expect( fileExists(path&'imgDrawLines-filled.jpg')).toBeTrue();
			});

			it(title = "Checking with image.drawLines() filled=true, isPolygon=false", body = function( currentSpec ){
				var img = imageNew("",150,150,"rgb","blue");
				img.drawLines(xcoords,ycoords,false,true);
				cfimage( action="write", source=img, destination=path & 'objDrawLines-filled.jpg', overwrite="yes");
				expect( fileExists(path&'objDrawLines-filled.jpg')).toBeTrue();
			});

			it(title = "Checking with ImageDrawLines, filled=false", body = function( currentSpec ){
				var img = imageNew("",150,150,"rgb","blue");
				imageDrawLines(image=img,xcoords=xcoords,ycoords=ycoords,filled="false");
				cfimage( action="write", source=img, destination=path & 'imgDrawLines-outline.jpg', overwrite="yes");
				expect( fileExists(path&'imgDrawLines-outline.jpg')).toBeTrue();
			});

			it(title = "Checking with image.drawLines() filled=false", body = function( currentSpec ){
				var img = imageNew("",150,150,"rgb","blue");
				// img.drawLines(xcoords,ycoords,false); // TODO errors!
				img.drawLines(xcoords,ycoords,false,false);
				cfimage( action="write", source=img, destination=path & 'objDrawLines-outline.jpg', overwrite="yes");
				expect( fileExists(path&'objDrawLines-outline.jpg')).toBeTrue();
			});

		});
	}

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}