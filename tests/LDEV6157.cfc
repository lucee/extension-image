component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function run( testResults, testBox ) {
		describe( "LDEV-6157 cfimage jakarta/javax compat", function() {

			it( title="cfimage action=writeToBrowser should not throw jakarta error on Lucee 6", body=function() {
				local.result = _internalRequest(
					template: "#createURI( 'LDEV6157' )#/LDEV6157.cfm"
				);
				expect( result.filecontent.trim() ).toInclude( "<img " );
			});

		});
	}

	private string function createURI( string calledName, boolean contract=true ) {
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast( base, '\/' )#";
		return baseURI & "/" & calledName;
	}

}
