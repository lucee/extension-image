component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.helpers= new testHelpers();
	};

	function run() {
		describe("Tests for imageGrayScale() function", function() {
			it("should correctly convert an image to grayScale - BIF", function() {
				var img = imageNew("",100,100,"rgb","blue");
				var rgb = helpers.getPixelColor( img, 30, 30);
				expect( helpers.checkColor( rgb, 0, 0, 255 ) ).toBeTrue(); //blue
				imageGrayScale( img );
				var rgb = helpers.getPixelColor( img, 30, 30 );
				expect( helpers.checkColor( rgb, 95, 95, 95 ) ).toBeTrue(); // yellow
			});

			it("should correctly convert an image to grayScale - member", function() {
				var img = imageNew("",100,100,"rgb","blue");
				var rgb = helpers.getPixelColor( img, 30, 30);
				expect( helpers.checkColor( rgb, 0, 0, 255 ) ).toBeTrue(); //blue
				img = img.GrayScale();
				var rgb = helpers.getPixelColor( img, 30, 30 );
				expect( helpers.checkColor( rgb, 95, 95, 95 ) ).toBeTrue(); // yellow
			});

			it("should throw an error if applied to a non-image variable", function() {
				var nonImageVar = "This is not an image";
				// Expect an error when trying to call imageGrayScale on a non-image
				expect( function() {
					nonImageVar.imageGrayScale();
				} ).toThrow();
			});
		});
	}

}

