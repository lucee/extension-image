component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.srcImage = expandPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/IPTC-GoogleImgSrcPmd_testimg01.jpg");
	}

	function run( testResults, testBox ){
		describe( "test case for ImageFilterColorMap()", function() {

			it(title = "Checking with ImageFilterColorMap(spectrum)", body = function( currentSpec ){
				var fcm = ImageFilterColorMap(type="spectrum");
				expect( fcm.getClass().getName() ).toBe( "org.lucee.extension.image.filter.SpectrumColormap" );
			});

			it(title = "Checking with ImageFilterColorMap(linear)", body = function( currentSpec ){
				var fcm = ImageFilterColorMap(type="linear", lineColor1="blue", lineColor2="yellow");
				expect( fcm.getClass().getName() ).toBe( "org.lucee.extension.image.filter.LinearColormap" );
			});

			it(title = "Checking with ImageFilterColorMap(grayscale)", body = function( currentSpec ){
				var fcm = ImageFilterColorMap(type="grayscale");
				expect( fcm.getClass().getName() ).toBe( "org.lucee.extension.image.filter.GrayscaleColormap" );
			});

			// TODO this doesn't work
			xit(title = "Checking with image.filterColorMap()", body = function( currentSpec ){
				var img = imageRead( srcImage );
				var fcm = img.filterColorMap("spectrum");
				expect( fcm.getClass().getName() ).toBe( "org.lucee.extension.image.filter.SpectrumColormap" );
			});

		});
	}

}
