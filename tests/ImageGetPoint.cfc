component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function run( testResults, testBox ){
		describe( "test case for ImageDrawPoint", function() {

			it(title = "Checking with ImageDrawOval", body = function( currentSpec ){
				var img = imageNew("",200,200,"rgb","red");
				imageSetDrawingColor(img, "white");
				checkPoint( img, 90, 90, "red" );

				imageDrawPoint( img, 90, 90 );
				checkPoint( img, 90, 90, "white" );
			});

		});
	}

	private function checkPoint( img, x, y, color ){
		var pixel = img.getPoint( x, y );
		var pixel2 = ImageGetPoint( img, x, y );

		structEach( pixel, function( k, v ) {
			expect( pixel[ k ] ).toBe( pixel2[ k ] );
		});

		if (color eq "white"){
			expect( pixel.red ).toBe( 255 );
			expect( pixel.blue ).toBe( 255 );
			expect( pixel.green ).toBe( 255 );
			expect( pixel.hex ).toBe( "##ffffff" );
		} else {
			expect( pixel.red ).toBe( 255 );
			expect( pixel.blue ).toBe( 0 );
			expect( pixel.green ).toBe( 0 );
			expect( pixel.hex ).toBe( "##ff0000" );
		}
	}

}

