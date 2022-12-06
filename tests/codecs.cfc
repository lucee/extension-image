component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function run( testResults, testBox ) localmode=true{
		
		var codecs = ["JDeli","Aspose","TwelveMonkeys","ImageIO","Lucee","ApacheImaging","JAI",""];

		loop array="#codecs#" value="local.codec" {
			describe("test image codect: [#codec#]", function(){
				it( title="test imageWriteToBrowser( #codec# ) ", 
					data={ codec=codec },
					body=function( data ) {
					// systemOutput("codec: " & data.codec, true);
					local.result = _internalRequest(
						template : "#createURI("codecs")#/encode.cfm",
						url: {
							codec: data.codec
						}
					);
					expect ( result.filecontent.trim() ).toBeEmpty( result.filecontent );
				});
				it( title="dump decoders ( #codec# )",
					data={ codec=codec },
					body=function( data ) {
					// systemOutput("codec: " & data.codec, true);
					local.result = _internalRequest(
						template : "#createURI("codecs")#/dump_decoders.cfm",
						url: {
							codec: data.codec
						}
					);
					expect ( result.filecontent.trim() ).toBeEmpty( result.filecontent );
				});
			});
		}

		describe("test write unsupported codec ", function(){
			it( title="test imageWrite unsupported [TwelveMonkeys jpeg]",
				data={ codec=codec },
				body=function( data ) {
				// systemOutput("codec: " & data.codec, true);
				local.result = _internalRequest(
					template : "#createURI("codecs")#/encode_unsupported.cfm",
					url: {
						codec: "TwelveMonkeys",
						format: "jpeg"
					}
				);
				expect ( result.filecontent.trim() ).toBeEmpty( result.filecontent );
			});

			it( title="test imageWrite unsupported [TwelveMonkeys webp]",
				data={ codec=codec },
				body=function( data ) {
				// systemOutput("codec: " & data.codec, true);
				local.result = _internalRequest(
					template : "#createURI("codecs")#/encode_unsupported.cfm",
					url: {
						codec: "TwelveMonkeys",
						format: "webp"
					}
				);
				expect ( result.filecontent.trim() ).toBeEmpty( result.filecontent );
			});

			it( title="test imageWrite unsupported [webp]",
				data={ codec=codec },
				body=function( data ) {
				// systemOutput("codec: " & data.codec, true);
				local.result = _internalRequest(
					template : "#createURI("codecs")#/encode_unsupported.cfm",
					url: {
						codec: "",
						format: "webp"
					}
				);
				expect ( result.filecontent.trim() ).toBeEmpty( result.filecontent );
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = getDirectoryFromPath( contractPath( getCurrentTemplatePath() ) );
		return baseURI&""&calledName;
	}

} 