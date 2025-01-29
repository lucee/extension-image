component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function run( testResults, testBox ){
		describe( "test case for ImageFilterWarpGrid()", function() {

			it(title = "Checking with ImageFilterWarpGrid()", body = function( currentSpec ){
				var wg = ImageFilterWarpGrid(rows=5, cols=5, width=20, height=20);
				expect( wg.getClass().getName() ).toBe( "org.lucee.extension.image.filter.WarpGrid" );
			});

		});
	}

}
