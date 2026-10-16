<cfscript>
	testFile = getTempFile( getTempDirectory(), "test-text-file" );
	fileWrite( testFile , "This is test file" );
	isImageFile( testFile ); // checking isImageFile() doesn't leave files locked
	try {
		fileDelete( testFile );
	} catch( any e)  {
		echo( e.message ) ;
	}
</cfscript>