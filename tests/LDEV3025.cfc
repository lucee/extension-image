component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function run( testResults, testBox ){
		describe( "LDEV-3025: ImageAddBorder inside and transparent border", function(){

			it( title="default: border is added outside and the image grows", body=function( currentSpec ){
				var img = imageNew( "", 100, 80, "rgb", "red" );
				imageAddBorder( image=img, thickness=10, color="blue" );
				expect( img.getWidth() ).toBe( 120 );
				expect( img.getHeight() ).toBe( 100 );
				expect( pixel( img, 0, 0 ).getBlue() ).toBe( 255 );
				expect( pixel( img, 10, 10 ).getRed() ).toBe( 255 );
			});

			it( title="inside=false behaves like the default", body=function( currentSpec ){
				var img = imageNew( "", 100, 80, "rgb", "red" );
				imageAddBorder( image=img, thickness=10, color="blue", borderType="constant", inside=false );
				expect( img.getWidth() ).toBe( 120 );
				expect( img.getHeight() ).toBe( 100 );
			});

			it( title="inside=true keeps the image size and draws the border over the edge", body=function( currentSpec ){
				var img = imageNew( "", 100, 80, "rgb", "red" );
				imageAddBorder( image=img, thickness=10, color="blue", inside=true );
				expect( img.getWidth() ).toBe( 100 );
				expect( img.getHeight() ).toBe( 80 );

				// outermost and innermost pixel of the border on each side
				loop array=[ [ 0, 0 ], [ 99, 79 ], [ 9, 40 ], [ 90, 40 ], [ 50, 0 ], [ 50, 9 ], [ 50, 70 ], [ 50, 79 ] ] item="local.p" {
					expect( pixel( img, p[ 1 ], p[ 2 ] ).getBlue() ).toBe( 255, "blue at " & p[ 1 ] & "/" & p[ 2 ] );
					expect( pixel( img, p[ 1 ], p[ 2 ] ).getRed() ).toBe( 0, "no red at " & p[ 1 ] & "/" & p[ 2 ] );
				}
				// first pixels of the image area
				loop array=[ [ 10, 10 ], [ 89, 69 ], [ 50, 40 ], [ 10, 40 ], [ 50, 10 ] ] item="local.p" {
					expect( pixel( img, p[ 1 ], p[ 2 ] ).getRed() ).toBe( 255, "red at " & p[ 1 ] & "/" & p[ 2 ] );
					expect( pixel( img, p[ 1 ], p[ 2 ] ).getBlue() ).toBe( 0, "no blue at " & p[ 1 ] & "/" & p[ 2 ] );
				}
			});

			it( title="inside=true works as member function", body=function( currentSpec ){
				var img = imageNew( "", 100, 80, "rgb", "red" );
				img.addBorder( thickness=5, color="blue", inside=true );
				expect( img.getWidth() ).toBe( 100 );
				expect( img.getHeight() ).toBe( 80 );
				expect( pixel( img, 0, 0 ).getBlue() ).toBe( 255 );
				expect( pixel( img, 5, 5 ).getRed() ).toBe( 255 );
			});

			it( title="inside=true keeps the image size for every borderType", body=function( currentSpec ){
				loop array=[ "zero", "constant", "copy", "reflect", "wrap" ] item="local.type" {
					var img = imageNew( "", 100, 80, "rgb", "silver" );
					imageAddBorder( image=img, thickness=8, color="red", borderType=type, inside=true );
					expect( img.getWidth() ).toBe( 100, type );
					expect( img.getHeight() ).toBe( 80, type );
				}
			});

			it( title="inside=true with borderType zero draws a black border", body=function( currentSpec ){
				var img = imageNew( "", 100, 80, "rgb", "red" );
				imageAddBorder( image=img, thickness=4, color="blue", borderType="zero", inside=true );
				expect( pixel( img, 0, 0 ).getRed() ).toBe( 0 );
				expect( pixel( img, 0, 0 ).getBlue() ).toBe( 0 );
				expect( pixel( img, 4, 4 ).getRed() ).toBe( 255 );
			});

			it( title="inside=true with borderType copy extends the edge of the remaining image", body=function( currentSpec ){
				var img = imageNew( "", 100, 80, "rgb", "red" );
				imageAddBorder( image=img, thickness=4, borderType="copy", inside=true );
				expect( img.getWidth() ).toBe( 100 );
				expect( pixel( img, 0, 0 ).getRed() ).toBe( 255 );
				expect( pixel( img, 50, 1 ).getRed() ).toBe( 255 );
			});

			it( title="transparent border outside makes the border area transparent", body=function( currentSpec ){
				var img = imageNew( "", 100, 80, "rgb", "red" );
				imageAddBorder( image=img, thickness=10, color="transparent" );
				expect( img.getWidth() ).toBe( 120 );
				expect( img.getHeight() ).toBe( 100 );
				expect( pixel( img, 0, 0 ).getAlpha() ).toBe( 0 );
				expect( pixel( img, 119, 99 ).getAlpha() ).toBe( 0 );
				expect( pixel( img, 9, 50 ).getAlpha() ).toBe( 0 );
				expect( pixel( img, 10, 10 ).getAlpha() ).toBe( 255 );
				expect( pixel( img, 10, 10 ).getRed() ).toBe( 255 );
			});

			it( title="transparent border inside keeps the image size", body=function( currentSpec ){
				var img = imageNew( "", 100, 80, "rgb", "red" );
				imageAddBorder( image=img, thickness=10, color="transparent", inside=true );
				expect( img.getWidth() ).toBe( 100 );
				expect( img.getHeight() ).toBe( 80 );
				expect( pixel( img, 0, 0 ).getAlpha() ).toBe( 0 );
				expect( pixel( img, 99, 79 ).getAlpha() ).toBe( 0 );
				expect( pixel( img, 9, 40 ).getAlpha() ).toBe( 0 );
				expect( pixel( img, 10, 10 ).getAlpha() ).toBe( 255 );
				expect( pixel( img, 89, 69 ).getAlpha() ).toBe( 255 );
			});

			it( title="transparent border survives writing to png", body=function( currentSpec ){
				var img = imageNew( "", 40, 40, "rgb", "red" );
				imageAddBorder( image=img, thickness=5, color="transparent", inside=true );
				var file = getTempFile( getTempDirectory(), "ldev3025", "png" );
				try {
					imageWrite( img, file );
					var read = imageRead( file );
					// the image is read lazily, so check it before the file is deleted
					expect( read.getWidth() ).toBe( 40 );
					expect( pixel( read, 0, 0 ).getAlpha() ).toBe( 0 );
					expect( pixel( read, 5, 5 ).getAlpha() ).toBe( 255 );
				}
				finally {
					if ( fileExists( file ) ) fileDelete( file );
				}
			});

			it( title="inside=true with a thickness of half the image or more throws", body=function( currentSpec ){
				var img = imageNew( "", 100, 80, "rgb", "red" );
				expect( function(){
					imageAddBorder( image=img, thickness=40, inside=true );
				}).toThrow( "application" );
				expect( img.getWidth() ).toBe( 100 );
				expect( img.getHeight() ).toBe( 80 );
			});

		});
	}

	private function pixel( required img, required numeric x, required numeric y ) {
		return createObject( "java", "java.awt.Color" ).init( javacast( "int", imageGetBufferedImage( arguments.img ).getRGB( javacast( "int", arguments.x ), javacast( "int", arguments.y ) ) ), true );
	}

}
