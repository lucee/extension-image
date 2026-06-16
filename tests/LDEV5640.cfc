component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function run( testResults, testBox ){
		describe( "Test ImagePaste", function(){

			it( "test further operations on image possible after imagePaste", function(){
				var black = imageNew( "", 2,  2,  "rgb", "000000" );
				var white = imageNew( "", 10, 10, "rgb", "ffffff" );
				var src = duplicate( white );
				imagePaste(white, black, 3, 1 ); // works
				var white2=duplicate(white);

				imagePaste(white, black, 6, 1 ); // doesn't work
				imagePaste(white2, black, 6, 1 ); // works after a duplicate

				expect(white.getBase64String('png')).toBe( white2.getBase64String('png') );
			});

		} );
	}

}
