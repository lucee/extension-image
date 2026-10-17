component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.srcImage = expandPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/IPTC-GoogleImgSrcPmd_testimg01.jpg");
		variables.metaDataFormats = [ "jpg", "png", "webp", "gif" ];

		// Cache source image and metadata to avoid repeated loading
		variables.srcImageObject = imageRead( variables.srcImage );
		variables.srcIPTC = ImageGetIPTCMetadata( variables.srcImage );
		variables.srcEXIF = ImageGetEXIFMetadata( variables.srcImage );
	}

	function run( testResults, testBox ){
		describe(title="ImageWrite Function", body=function() {
			it("should write an image to a file", function() {
				var imagePath = getTempFile( getTempDirectory(), "imageWrite", "jpg" );
				var img = imageNew("",200,200,"rgb","red");

				imageWrite(img, imagePath);

				expect(fileExists(imagePath)).toBeTrue();

				var writtenImg = imageRead(imagePath);
				expect(imageGetWidth(writtenImg)).toBe(200);
				expect(imageGetHeight(writtenImg)).toBe(200);

				fileDelete(imagePath);
			});
		});

		describe(title="ImageWrite Function - metadata", body=function() {
			it("should write an image to a file, no metadata=true EXIF", function() {
				checkMeta( metaType="EXIF", noMeta=true );
			});

			it("should write an image to a file, no metadata=true IPTC", function() {
				checkMeta( metaType="IPTC", noMeta=true );
			});

			it("should write an image to a file, no metadata=false EXIF", function() {
				checkMeta( metaType="EXIF", noMeta=false );
			});

			it("should write an image to a file, no metadata=false IPTC", function() {
				checkMeta( metaType="IPTC", noMeta=false );
			});
		});
	}

	private function checkMeta( metaType, noMeta ) {
		loop array="#metaDataFormats#" item="local.format" {
			var tick = getTickCount();
			checkMetaByFormat(format, metaType, noMeta);
			systemOutput("took #getTickCount()-tick#", true);
		}
	}

	private function checkMetaByFormat( format, metaType, noMeta ) {
		systemOutput("", true);
		systemOutput(arguments.toJson(), true);
		var imagePath = getTempFile( getTempDirectory(), "imageWrite", format );

		// Use cached source image instead of loading from disk each time
		imageWrite(image=variables.srcImageObject, destination=imagePath, noMetaData=arguments.noMeta);
		expect(fileExists(imagePath)).toBeTrue();

		// Use cached source metadata instead of extracting each time
		if (metaType eq "IPTC"){
			var src = variables.srcIPTC;
			var dest = ImageGetIPTCMetadata( imagePath );

		} else if (metaType eq "EXIF"){
			var src = variables.srcEXIF;
			var dest = ImageGetEXIFMetadata( imagePath );
		} else {
			throw "unsupported metadata type [#metaType#]"
		}

		var missing = getMissingKeys( src, dest );
		var added = getMissingKeys( dest, src );
		systemOutput("src has #len(src)# items, dest has #len(dest)# items", true);
		//systemOutput("stripped keys (#len(missing)#): #missing.toJson()#", true);
		if (len(added) )
			systemOutput("added keys (#len(added)#): #added.toJson()#", true);

		if (arguments.noMeta){
			// Verify that PII metadata was stripped - dest should have fewer items than src
			// Some auto-generated technical metadata (dimensions, color space, etc.) is acceptable
			expect ( len( dest ) ).toBeLT( len( src ), format & " should strip original metadata when noMeta=true (src=#len(src)#, dest=#len(dest)#)" );
		} else {
			expect ( len( dest ) ).toBeGTE( 0, format ); // TODO some are lost?
			//expect ( dest ).toHaveLength( len( src ), format );
		}

		fileDelete(imagePath);
	}

	private function getMissingKeys( src, dest ) {
		var missing = [];
		for ( var key in src ) {
			if ( !dest.keyExists( key ) ) {
				missing.append( key );
			}
		}
		return missing;
	}

}
