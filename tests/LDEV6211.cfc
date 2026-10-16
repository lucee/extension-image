component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	variables.imgDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "images/";
	variables.testDir = getTempDirectory() & "ldev6211/";

	function beforeAll(){
		if ( directoryExists( testDir ) ) directoryDelete( testDir, true );
		directoryCreate( testDir );

		fileCopy( imgDir & "small-sample.webp", testDir & "sample.webp" );
		// file with a truly unknown extension to force the outer catch
		fileCopy( imgDir & "small-sample.webp", testDir & "mystery.xyz" );
	}

	function run( testResults, testBox ){
		describe( "LDEV-6211 unsupported formats must not throw from imageInfo", function(){

			it( title="imageInfo on a .webp file returns without throwing", body=function(){
				var info = imageInfo( testDir & "sample.webp" );
				expect( info.width ).toBeGT( 0 );
				expect( info.height ).toBeGT( 0 );
			});

			it( title="imageInfo on a file with unknown extension returns without throwing", body=function(){
				var info = imageInfo( testDir & "mystery.xyz" );
				expect( info.width ).toBeGT( 0 );
				expect( info.height ).toBeGT( 0 );
			});

		});
	}

}
