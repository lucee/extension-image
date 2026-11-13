component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	// LDEV-6525: the WebP writer ("Gotson" coder) is com.github.usefulness:webp-imageio, the maintained fork of the
	// archived com.github.gotson:webp-imageio 0.2.2, which had no macOS arm64 native and libwebp 1.0.0.
	// All requests run with this.image.coders = [ "Gotson" ], so no other coder can hide a failure.

	function run( testResults, testBox ) {
		describe( "LDEV-6525 WebP through the native webp-imageio coder", function() {

			it( title="the WebP coder comes from com.github.usefulness:webp-imageio and webp is read- and writeable", body=function( currentSpec ) {
				var res = call( "lib" );
				expect( res.location ).toInclude( "usefulness" );
				expect( res.location ).notToInclude( "gotson" );
				expect( listFindNoCase( res.writeable, "webp" ) ).toBeGT( 0, res.writeable );
				expect( listFindNoCase( res.readable, "webp" ) ).toBeGT( 0, res.readable );
			});

			it( title="imageWrite webp (rgb) writes a non-empty RIFF/WEBP file that reads back", body=function( currentSpec ) {
				var res = call( "imageWrite", { type: "rgb" } );
				expect( res.size ).toBeGT( 0 );
				expect( res.header ).toBe( "RIFFWEBP" );
				expect( res.width ).toBe( 64 );
				expect( res.height ).toBe( 48 );
			});

			it( title="imageWrite webp (argb) writes a non-empty RIFF/WEBP file that reads back", body=function( currentSpec ) {
				var res = call( "imageWrite", { type: "argb" } );
				expect( res.size ).toBeGT( 0 );
				expect( res.header ).toBe( "RIFFWEBP" );
				expect( res.width ).toBe( 64 );
				expect( res.height ).toBe( 48 );
			});

			it( title="cfimage action=write webp", body=function( currentSpec ) {
				var res = call( "cfimageWrite" );
				expect( res.size ).toBeGT( 0 );
				expect( res.header ).toBe( "RIFFWEBP" );
				expect( res.width ).toBe( 40 );
			});

			it( title="existing webp fixtures read and write again", body=function( currentSpec ) {
				for ( var file in [ "sample-alpha.webp", "sample-alpha-2.webp", "small-sample.webp" ] ) {
					var res = call( "read", { file: file } );
					expect( res.width ).toBeGT( 0, file );
					expect( res.height ).toBeGT( 0, file );
					expect( res.againWidth ).toBe( res.width, file );
				}
			});
		});
	}

	private struct function call( required string action, struct args = {} ) {
		var u = duplicate( arguments.args );
		u.action = arguments.action;
		var result = _internalRequest( template: "#createURI( 'LDEV6525' )#/webp.cfm", url: u );
		var body = trim( result.filecontent );
		expect( isJSON( body ) ).toBeTrue( body );
		var res = deserializeJSON( body );
		expect( res.ok ).toBeTrue( ( res.error ?: "" ) & " " & ( res.detail ?: "" ) );
		return res;
	}

	private string function createURI( string calledName, boolean contract=true ) {
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast( base, '\/' )#";
		return baseURI & "/" & calledName;
	}
}
