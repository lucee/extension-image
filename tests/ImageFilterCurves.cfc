component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function run( testResults, testBox ){
		describe( "test case for ImageFilterCurves()", function() {

			it(title = "Checking with ImageFilterCurves()", body = function( currentSpec ){
				var curves = ImageFilterCurves();
				expect( curves.getClass().getName() ).toBe( "org.lucee.extension.image.filter.CurvesFilter$Curve" );
			});

		});
	}

}
