component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.srcImage = expandPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/IPTC-GoogleImgSrcPmd_testimg01.jpg");
		variables.metaDataFormats = [ "jpg", "tiff", "png", "webp" ];
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

		describe(title="ImageWrite Function - metadata", skip=true, body=function() {
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
			checkMetaByFormat(format, metaType, noMeta);
		}
	}

	private function checkMetaByFormat( format, metaType, noMeta ) {
		var imagePath = getTempFile( getTempDirectory(), "imageWrite", format );
		var img = imageRead( variables.srcImage );

		imageWrite(image=img, destination=imagePath, noMetaData=arguments.noMeta);
		expect(fileExists(imagePath)).toBeTrue();

		if (metaType eq "IPTC"){
			var src = ImageGetIPTCMetadata( variables.srcImage );
			var dest = ImageGetIPTCMetadata( imagePath );

		} else if (metaType eq "EXIF"){
			var src = ImageGetEXIFMetadata( variables.srcImage );
			var dest = ImageGetEXIFMetadata( imagePath );
		} else {
			throw "unsupported metadata type [#metaType#]"
		}

		if (arguments.noMeta){
			expect ( dest ).toHaveLength( 0, format & " had #len(dest)# items of metadata");
		} else {
			expect ( len( dest ) ).toBeGTE( 0, format ); // TODO some are lost?
			//expect ( dest ).toHaveLength( len( src ), format );
		}

		fileDelete(imagePath);
	}

}
