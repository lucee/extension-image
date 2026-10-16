component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = getTempDirectory() & "ImageScaleToFit/";
		if(!directoryExists(path)){
			directoryCreate(path);
		}
		variables.srcImage = expandPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/image.jpg");
		variables.interpolations = ["highestQuality","highQuality","mediumQuality","highestPerformance","highPerformance",
			"mediumPerformance","nearest","bilinear","bicubic","bessel",
			"blackman","hamming","hanning","hermite","lanczos","mitchell","quadratic"
		]; 

	}

	function run( testResults, testBox ) {
		describe( "test case for ImageScaleToFit", function() {

			it(title = "Checking with ImageScaleToFit", body = function( currentSpec ){
				var newImg = imageRead( variables.srcImage );
				ImageScaleToFit(newImg, 300, 300, "highestQuality", 1);
				cfimage(action = "write", source = newImg, destination = path&'ScaleToFit.jpg', overwrite="yes");
				expect(fileExists(path&'ScaleToFit.jpg')).toBe(true);
			});

			it(title = "Checking with image.ScaleToFit()", body = function( currentSpec ){
				var newImg = imageRead( variables.srcImage );
				newImg = newImg.ScaleToFit(300, 300, "highestQuality", 1);
				cfimage(action = "write", source = newImg, destination = path&'objScaleToFit.jpg', overwrite="yes");
				expect(fileExists(path&'objScaleToFit.jpg')).toBe(true);
			});

			it(title = "Checking with ImageScaleToFit - fitWidth", body = function( currentSpec ){
				var newImg = imageRead( variables.srcImage );
				ImageScaleToFit(newImg, 450, "", "highestQuality", 1);
				cfimage(action = "write", source = newImg, destination = path&'ScaleToFit-fitWidth.jpg', overwrite="yes");
				expect(fileExists(path&'ScaleToFit-fitWidth.jpg')).toBe(true);
			});

			it(title = "Checking with image.ScaleToFit() - fitWidth", body = function( currentSpec ){
				var newImg = imageRead( variables.srcImage );
				newImg = newImg.ScaleToFit(250, "", "highestQuality", 1);
				cfimage(action = "write", source = newImg, destination = path&'objScaleToFit-fitWidth.jpg', overwrite="yes");
				expect(fileExists(path&'objScaleToFit-fitWidth.jpg')).toBe(true);
			});

			it(title = "Checking with ImageScaleToFit - fitHeight", body = function( currentSpec ){
				var newImg = imageRead( variables.srcImage );
				ImageScaleToFit(newImg, "", 450, "highestQuality", 1);
				cfimage(action = "write", source = newImg, destination = path&'ScaleToFit-fitHeight.jpg', overwrite="yes");
				expect(fileExists(path&'ScaleToFit-fitHeight.jpg')).toBe(true);
			});

			it(title = "Checking with image.ScaleToFit() - fitHeight", body = function( currentSpec ){
				var newImg = imageRead( variables.srcImage );
				newImg = newImg.ScaleToFit("", 250, "highestQuality", 1);
				cfimage(action = "write", source = newImg, destination = path&'objScaleToFit-fitHeight.jpg', overwrite="yes");
				expect(fileExists(path&'objScaleToFit-fitHeight.jpg')).toBe(true);
			});

			it(title = "Checking with ImageScaleToFit - interpolation", body = function( currentSpec ){
				loop array=#interpolations# item="local.interpolation" {
					var newImg = imageRead( variables.srcImage );
					ImageScaleToFit(newImg, 300, 300, interpolation, 1);
					cfimage(action = "write", source = newImg, destination = path&'ScaleToFit-#interpolation#.jpg', overwrite="yes");
					expect(fileExists(path&'ScaleToFit-#interpolation#.jpg')).toBe(true);
				}
			});

			it(title = "Checking with image.ScaleToFit() - blurFactor", body = function( currentSpec ){
				loop from=1  to=10 index="local.blurFactor" {
					var newImg = imageRead( variables.srcImage );
					newImg = newImg.ScaleToFit("", 100, "mediumPerformance", blurFactor);
					cfimage(action = "write", source = newImg, destination = path&'objScaleToFit-blurFactor-#blurFactor#.jpg', overwrite="yes");
					expect(fileExists(path&'objScaleToFit-blurFactor-#blurFactor#.jpg')).toBe(true);
				}
			});

		});
	}

	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}