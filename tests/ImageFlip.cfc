component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path=getTempDirectory() & "ImageFlip/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
		variables.operations = [ "vertical", "horizontal", "diagonal", "antidiagonal", 90, 180, 270 ];
	}

	function run( testResults, testBox ){
		describe( "test case for ImageFlip", function() {

			it(title = "Checking with ImageFlip", body = function( currentSpec ){
				var img = imageNew("",200,200,"rgb","black");
				imageSetDrawingColor(img, "white");
				ImageDrawOval(img, 50,50,70,30,"no");
				cfimage(action="write", source=img, destination=path&'imgDrawOval.jpg', overwrite="yes");
				expect(fileexists(path&'imgDrawOval.jpg')).tobe("true");

				loop array="#operations#" item="local.transpose" {
					var src = duplicate( img );
					imageFlip(src, transpose);
					cfimage(action="write", source=src, destination=path&'imgFlip-#transpose#.jpg', overwrite="yes");
					expect(fileExists(path&'imgFlip-#transpose#.jpg')).tobe("true");
				}

			});

			it(title="Checking with image.flip()", body=function( currentSpec ){
				var img = imageNew("",200,200,"rgb","green");
				img.setDrawingColor("blue");
				img = img.drawOval(80,100,70,20,"yes");
				cfimage(action="write", source=img, destination=path&'objDrawOval.jpg', overwrite="yes");
				expect(fileexists(path&'objDrawOval.jpg')).tobe("true");

				loop array="#operations#" item="local.transpose" {
					var src = duplicate( img );
					src = src.flip(transpose);
					cfimage(action="write", source=src, destination=path&'objFlip-#transpose#.jpg', overwrite="yes");
					expect(fileExists(path&'objFlip-#transpose#.jpg')).tobe("true");
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