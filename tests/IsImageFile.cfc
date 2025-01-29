component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	function run( testResults, textbox ) {
		describe("testcase for isImageFile()", function() {
			it(title="checking isImageFile() function", body=function( currentSpec ) {
				var testFile = expandPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/lucee.png");
				expect (isImageFile( testFile ) ).toBeTrue();
				expect( isImageFile ( getTempFile( getTempDirectory(), "isImagefile", "png" ) ) ).toBeFalse();
			});
		});
	}
}