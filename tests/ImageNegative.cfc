component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.helpers= new testHelpers();
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
			});

			it("should correctly invert the colors of an image - member", function() {
				var img = imageNew("",100,100,"rgb","blue");
				var rgb = helpers.getPixelColor( img, 30, 30);
				expect( helpers.checkColor( rgb, 0, 0, 255 ) ).toBeTrue(); //blue
				img = img.negative();
				var rgb = helpers.getPixelColor( img, 30, 30 );
				expect( helpers.checkColor( rgb, 255, 255, 0 ) ).toBeTrue(); // yellow
			});

			it("should throw an error if applied to a non-image variable", function() {
				var nonImageVar = "This is not an image";
				// Expect an error when trying to call imageNegative on a non-image
				expect( function() {
					nonImageVar.imageNegative();
				} ).toThrow();
			});
		});
	}

}

