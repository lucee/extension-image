component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	variables.colors={
        "YELLOW":-256,
        "BLUE":-16776961,
        
    };

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

			it(title = "Checking the colors created", body = function( currentSpec ) {
				
				var blue = imageNew( "", 10,  10,  "rgb", "blue" );
				var yellow = imageNew( "", 10, 20, "rgb", "yellow" );
				
				// first we check the starting point is correct
				
				
				// should be all yellow
				var bufferedImg = imageGetBufferedImage(yellow);
				loop from=0 to=9 item="local.x" {
					loop from=0 to=19 item="local.y" {
						expect( bufferedImg.getRGB(x, y) ).toBe(variables.colors.YELLOW);
					}
				}
				
				imagePaste(image1: yellow, image2: blue, x:0, y:0 ); // top left
				// should be part blue
				var bufferedImg = imageGetBufferedImage(yellow);
				loop from=0 to=9 item="local.x" {
					loop from=0 to=9 item="local.y" {
						expect( bufferedImg.getRGB(x, y) ).toBe(variables.colors.BLUE);
					}
				}
				// should be part yellow
				loop from=0 to=9 item="local.x" {
					loop from=10 to=19 item="local.y" {
						expect( bufferedImg.getRGB(x, y) ).toBe(variables.colors.YELLOW);
					}
				}
				
				
				imagePaste(image1: yellow, image2: blue, x:0, y:10 ); // top right

				// should be all blue
				var bufferedImg = imageGetBufferedImage(yellow);
				loop from=0 to=9 item="local.x" {
					loop from=0 to=19 item="local.y" {
						expect( bufferedImg.getRGB(x, y) ).toBe(variables.colors.BLUE);
					}
				}

			});



		});
	};

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}	
}
		