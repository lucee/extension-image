component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = getTempDirectory() & "imageAddBorder/";
		if(!directoryExists(path)){
			directoryCreate(path);
		}
	}

	function run( testResults , testBox ) {
		describe( "test case for ImageAddBorder", function() {

			it(title = "Checking with BIF", body = function( currentSpec ) {
				var img = imageNew("",200,200,"rgb","red");
				imageAddBorder( img );
				imageWrite(img, getTempFile(path, "bif","png"));
			});

			it(title = "Checking with member function", body = function( currentSpec ) {
				var img = imageNew("",200,200,"rgb","yellow");
				img.addBorder();
				imageWrite(img, getTempFile(path, "member","png"));
			});

			it(title = "Checking border", body = function( currentSpec ) {
				var borders = {
					red: 10,
					yellow: 20,
					green: 30
				}
				loop collection=#borders# item="local.thickness" key="local.color" {
					var img = imageNew( "", 200, 200, "rgb", "silver" );
					imageAddBorder( image=img, thickness=thickness, color=color );
					imageWrite(img, getTempFile(path, "border-#thickness#-#color#","png"));
				}
			});

			it(title = "Checking borderType", body = function( currentSpec ) {
				var borderTypes= [ "zero", "constant", "copy", "reflect", "wrap" ];
				loop array=#borderTypes# item="local.type" {
					var img = imageNew( "", 200, 200, "rgb", "white" );
					imageAddBorder( image=img, thickness=5, color="red", type=type );
					imageWrite(img, getTempFile(path, "borderType-#type#","png"));
				}
			});

		});
	}
	function afterAll(){
		if (server.system.environment.TEST_CLEANUP ?: true && directoryExists(path)){
			directoryDelete(path,true);
		}
	}

}