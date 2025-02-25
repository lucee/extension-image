component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.helpers= new testHelpers();
		variables.path = getTempDirectory() & "ImageNegative/";
		if( directoryExists( path ) ){
			directoryDelete( path, true );
		}
		directoryCreate( path );
	};

	function run() {
		describe("Tests for imageNegative() function", function() {
			it("should correctly invert the colors of an image - BIF", function() {
				var img = imageNew("",100,100,"rgb","blue");
				var rgb = helpers.getPixelColor( img, 30, 30);
				expect( helpers.checkColor( rgb, 0, 0, 255 ) ).toBeTrue(); //blue
				imageNegative( img );
				var rgb = helpers.getPixelColor( img, 30, 30 );
				expect( helpers.checkColor( rgb, 255, 255, 0 ) ).toBeTrue(); // yellow
				cfimage(action="write", source=img, destination = path&"imageNegative.jpg");
				expect(fileExists(path&"imageNegative.jpg")).tobe("true");

			});

			it("should correctly invert the colors of an image - member", function() {
				var img = imageNew("",100,100,"rgb","blue");
				var rgb = helpers.getPixelColor( img, 30, 30);
				expect( helpers.checkColor( rgb, 0, 0, 255 ) ).toBeTrue(); //blue
				img = img.negative();
				var rgb = helpers.getPixelColor( img, 30, 30 );
				expect( helpers.checkColor( rgb, 255, 255, 0 ) ).toBeTrue(); // yellow
				cfimage(action="write", source=img, destination = path&"imageNegative-member.jpg");
				expect(fileExists(path&"imageNegative-member.jpg")).tobe("true");
			});

			it("should throw an error if applied to a non-image variable", function() {
				var nonImageVar = "This is not an image";
				// Expect an error when trying to call imageNegative on a non-image
				expect( function() {
					nonImageVar.imageNegative();
				} ).toThrow();
			});

			it("should correctly invert the colors of an image - BIF", function() {
				var img = imageRead(GetDirectoryFromPath(GetCurrentTemplatePath())&"images/BigBen.jpg");
				imageNegative( img );
				cfimage(action="write", source=img, destination = path&"imageNegative.jpg");
				expect(fileExists(path&"imageNegative-photo.jpg")).tobe("true");
			});
		
		});
	}

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}

}

