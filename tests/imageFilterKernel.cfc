component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function run( testResults, testBox ){
		describe( "test case for ImageFilterKernel()", function() {

			it(title = "Checking with ImageFilterKernel()", body = function( currentSpec ){
				var fc = imageFilterKernel(width=1, height=3, data=[ 1.1, 2.1, 3.1 ] ); // data is an array of floats
				expect( fc.getClass().getName() ).toBe( "java.awt.image.Kernel" );
			});

		});
	}

}
