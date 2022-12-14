<cfscript>
	testFile = getTempFile( getTempDirectory(), "test-text-file" );
	fileWrite( testFile , "This is test file" );
    try {
	    x = imageRead( testFile ); // checking imageRead() doesn't leave files locked
        echo("didn't throw on reading invalid image?");
    } catch( e ){
        // expected to throw
    }
	try {
		fileDelete( testFile );
	} catch(any e) {
		echo( e.message)  ;
	}
</cfscript>