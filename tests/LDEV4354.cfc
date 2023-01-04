component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function run( testResults , testBox ) {

		loop list="webp,jpeg,gif,png,jpg" item="local.format"{
			describe( "Testcase for LDEV-4354 [#format#]", function() {
				it( title="checking file locking issue with cfimage write [#format#] nometadata=false",
						data={ format=format },
						body=function( data ) {
					var imgText = ImageNew("",320,20,"argb");
					expect (function(){
						var dest = getTempFile(getTempDirectory(), "test", data.format);
						//systemOutput(dest, true);
						cfimage(source="#imgText#", destination="#dest#", action="write", overwrite="true");
						fileDelete(dest);
					}).notToThrow();
				});

				it( title="checking file locking issue with cfimage write [#format#] nometadata=true", 
						data={ format=format },
						body=function( data ) {
					var imgText = ImageNew("",320,20,"argb");
					expect (function(){
						var dest = getTempFile(getTempDirectory(), "test", data.format);
						//systemOutput(dest, true);
						cfimage(source="#imgText#", destination="#dest#", action="write", overwrite="true", noMetaData=true);
						fileDelete(dest);
					}).notToThrow();
				});
			});
		}
	}

}