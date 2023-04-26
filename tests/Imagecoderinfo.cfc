component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( "testcase for imagecoderinfo()", function() {
			it(title = "Checking with imageCoderInfo()", body = function( currentSpec ) {
				var img = "https://avatars1.githubusercontent.com/u/10973141?s=280&v=4";
				expect(imageCoderInfo(img)).toHaveKey("read");
				expect(imageCoderInfo(img)).toHaveKey("write");
				expect(imageCoderInfo(img).read[1].image).toHaveKey("colormodel");
				expect(imageCoderInfo(img).read[1].image).toHaveKey("metadata");
				expect(imageCoderInfo(img).read[1].image.height).toBe("280");
			});
		});
	}
}