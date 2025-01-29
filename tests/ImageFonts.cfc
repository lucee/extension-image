component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function run( testResults, testBox ){
		describe( "test case for ImageFonts()", function() {

			it(title = "Checking with ImageFonts()", body = function( currentSpec ){
				var fonts = imageFonts();
				expect( fonts ).toBeArray();
				expect( len( fonts ) ).toBeGT( 0 );
				expect( fonts ).toInclude( "Serif.bold" );
			});

		});
	}
}