component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function run( testResults, testBox ) localmode=true{

		var codecs = ["JDeli","Aspose","TwelveMonkeys","ImageIO","Lucee","ApacheImaging","JAI",""];

		loop array="#codecs#" value="local.codec" {
			describe("test image codect: [#codec#]", function(){
				it( title="test imageWrite( #codec# ) ",
					data={ codec=codec },
					body=function( data ) {
					// systemOutput("codec: " & data.codec, true);
					local.result = _internalRequest(
						template : "#createURI("codecs")#/encode.cfm",
						url: {
							codec: data.codec,
							action: "imageWrite"
						}
					);
					expect ( result.filecontent.trim() ).toBeEmpty( result.filecontent );
				});

				it( title="test cfimage action=write( #codec# ) ",
					data={ codec=codec },
					body=function( data ) {
					// systemOutput("codec: " & data.codec, true);
					local.result = _internalRequest(
						template : "#createURI("codecs")#/encode.cfm",
						url: {
							codec: data.codec,
							action: "cfimageWrite"
						}
					);
					expect ( result.filecontent.trim() ).toBeEmpty( result.filecontent );
				});

				/* disabled as some codecs support encoding but not decoding
				it( title="test imageWriteToBrowser ( #codec# ) ",
					data={ codec=codec },
					body=function( data ) {
					// systemOutput("codec: " & data.codec, true);
					local.result = _internalRequest(
						template : "#createURI("codecs")#/encode.cfm",
						url: {
							codec: data.codec,
							action: "imageWriteToBrowser"
						}
					);
					expect ( result.filecontent.trim() ).toBeEmpty( result.filecontent );
				});
				*/

				it( title="test isImageFile locking with invalid image ( #codec# ) ",
					data={ codec=codec },
					body=function( data ) {
					// systemOutput("codec: " & data.codec, true);
					local.result = _internalRequest(
						template : "#createURI("codecs")#/test_locking_isImageFile.cfm",
						url: {
							codec: data.codec
						}
					);
					expect ( result.filecontent.trim() ).toBeEmpty( result.filecontent, codec );
				});

				it( title="test ImageRead locking with invalid image ( #codec# ) ",
					data={ codec=codec },
					body=function( data ) {
					// systemOutput("codec: " & data.codec, true);
					local.result = _internalRequest(
						template : "#createURI("codecs")#/test_locking_imageRead.cfm",
						url: {
							codec: data.codec
						}
					);
					expect ( result.filecontent.trim() ).toBeEmpty( result.filecontent, codec );
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

				it( title="dump encoders ( #codec# )",
					data={ codec=codec },
					body=function( data ) {
					// systemOutput("codec: " & data.codec, true);
					local.result = _internalRequest(
						template : "#createURI("codecs")#/dump_encoders.cfm",
						url: {
							codec: data.codec
						}
					);
					expect ( result.filecontent.trim() ).toBeEmpty( result.filecontent );
				});
			});
		}

		describe("test write check per codec ", function(){
			it( title="test imageWrite supported codec [TwelveMonkeys jpeg]",
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
				// we now support jpeg
				expect ( result.filecontent.trim() ).notToBeEmpty( result.filecontent );
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

			it( title="test imageWrite unsupported [TwelveMonkeys heic]",
				data={ codec=codec },
				body=function( data ) {
				// systemOutput("codec: " & data.codec, true);
				local.result = _internalRequest(
					template : "#createURI("codecs")#/encode_unsupported.cfm",
					url: {
						codec: "TwelveMonkeys",
						format: "heic"
					}
				);
				expect ( result.filecontent.trim() ).toBeEmpty( result.filecontent );
			});

			it( title="test imageWrite supported [webp]",
				data={ codec=codec },
				body=function( data ) {

				if ( listfind( getWriteableImageFormats(), "webp") gt 0) {
					return; // codec is installed
				}
				// systemOutput("codec: " & data.codec, true);
				local.result = _internalRequest(
					template : "#createURI("codecs")#/encode_unsupported.cfm",
					url: {
						codec: "",
						format: "webp"
					}
				);
				expect ( result.filecontent.trim() ).notToBeEmpty( result.filecontent );
			});

			it( title="test imageWrite unsupported [heic]",
				data={ codec=codec },
				body=function( data ) {

				if ( listfind( getWriteableImageFormats(), "webp") gt 0) {
					return; // codec is installed
				}
				// systemOutput("codec: " & data.codec, true);
				local.result = _internalRequest(
					template : "#createURI("codecs")#/encode_unsupported.cfm",
					url: {
						codec: "",
						format: "heic"
					}
				);
				if ( listFindNoCase( getReadableImageFormats(), "heic" ) ){
					expect ( result.filecontent.trim() ).notToBeEmpty( result.filecontent );
				} else {
					expect ( result.filecontent.trim() ).toBeEmpty( result.filecontent );
				}
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = getDirectoryFromPath( contractPath( getCurrentTemplatePath() ) );
		return baseURI&""&calledName;
	}

}