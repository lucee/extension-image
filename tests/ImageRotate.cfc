component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = getTempDirectory() & "ImageRotate/";
		if( directoryExists( path ) ){
			directoryDelete( path, true );
		}
		directoryCreate( path );
	}

	function run( testResults , testBox ) {
		describe( "test case for ImageRotate", function() {

			it(title = "Checking with imageRotate() Function", body = function( currentSpec ) {
				loop list="50,90,125,180,270" item="local.angle" {
					var img = imageRead(GetDirectoryFromPath(GetCurrentTemplatePath())&"images/BigBen.jpg");
					imageRotate(img,10,10,angle);
					cfimage(action = "write", source = img, destination = path&"imageRotate-#angle#.jpg");
					expect(fileExists(path&"imageRotate-#angle#.jpg")).tobe("true");
				}
			});

			it(title = "Checking with imageRotate() with interpolation", body = function( currentSpec ) {
				var imgOne = imageNew("",150,150,"RGB","FFD700");
				imageRotate(imgOne,80,'bilinear');
				cfimage(action="write", source=imgOne , destination=path&"rotateImgone.jpg");
			  	expect(fileExists(path&"rotateImgone.jpg")).tobe("true");
			});

			it(title = "Checking with image.Rotate() with all attributes", body = function( currentSpec ) {
				var imgTwo = imageNew("",150,150,"RGB","red");
				imgTwo = imgTwo.rotate(5,5,60,'bicubic');
				cfimage(action="write", source=imgTwo, destination=path&"rotateImgtwo.jpg");
			  	expect(fileExists(path&"rotateImgtwo.jpg")).tobe("true");
			});

			it(title = "Checking with image.Rotate() with angle only", body = function( currentSpec ) {
				var imgThree = imageNew("",150,150,"RGB","A0522D");
				imgThree = imgThree.rotate(80);
				cfimage(action="write", source=imgThree, destination=path&"rotateImgthree.jpg");
			  	expect(fileExists(path&"rotateImgthree.jpg")).tobe("true");
			});

		});
	};

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}