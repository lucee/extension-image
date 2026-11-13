component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	// LDEV-6592: on Java 22+ without the native libheif, the NightMonkeys HEIF reader fails its first
	// ImageIO registration quietly, but every later registration throws
	// NoClassDefFoundError: Could not initialize class com.github.gotson.nightmonkeys.heif.Heif.
	// ImageIO.scanForPlugins() in the static initializer of Image let that error escape, so every image
	// function failed. The extension now scans with ImageIOPlugins, which skips NightMonkeys when the
	// natives are missing and isolates every other plugin.
	//
	// The full .lex strips the NightMonkeys services entries, so this test adds a class loader that
	// exposes them again, like the original jars from Maven Central do.

	function beforeAll() {
		variables.dir = getTempDirectory() & "LDEV6592-" & createUUID() & "/";
		directoryCreate( dir & "META-INF/services/" );
		fileWrite( dir & "META-INF/services/javax.imageio.spi.ImageReaderSpi",
			"com.github.gotson.nightmonkeys.heif.imageio.plugins.HeifImageReaderSpi" & chr( 10 )
			& "com.github.gotson.nightmonkeys.jxl.imageio.plugins.JxlImageReaderSpi" & chr( 10 ) );
		variables.srcImage = expandPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "images/IPTC-GoogleImgSrcPmd_testimg01.jpg" );
	}

	function afterAll() {
		if ( directoryExists( dir ) ) directoryDelete( dir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6592 ImageIO plugin scan with the NightMonkeys HEIF/JXL readers visible", function() {

			it( title="the test class loader exposes the NightMonkeys services entry", body=function( currentSpec ) {
				var loader = nightMonkeysLoader();
				var urls = createObject( "java", "java.util.Collections" ).list( loader.getResources( "META-INF/services/javax.imageio.spi.ImageReaderSpi" ) );
				expect( urls.toString() ).toInclude( "LDEV6592-" );
				expect( loader.loadClass( "com.github.gotson.nightmonkeys.heif.imageio.plugins.HeifImageReaderSpi" ).getName() )
					.toBe( "com.github.gotson.nightmonkeys.heif.imageio.plugins.HeifImageReaderSpi" );
			});

			it( title="scanning repeatedly never throws", body=function( currentSpec ) {
				var loader = nightMonkeysLoader();
				// the second and third scan are the ones that threw NoClassDefFoundError before
				loop times=3 {
					var registered = scanForPlugins( loader );
					expect( registered ).toBeGTE( 0 );
				}
			});

			it( title="scanning through the context class loader (what Image does) never throws", body=function( currentSpec ) {
				var thread = createObject( "java", "java.lang.Thread" ).currentThread();
				var before = thread.getContextClassLoader();
				try {
					thread.setContextClassLoader( nightMonkeysLoader() );
					loop times=3 {
						scanner().getMethod( "scanForPlugins", javaArray( "java.lang.Class", [] ) ).invoke( javaCast( "null", "" ), javaArray( "java.lang.Object", [] ) );
					}
				}
				finally {
					thread.setContextClassLoader( before );
				}
			});

			it( title="unrelated image functions keep working after the scans", body=function( currentSpec ) {
				var path = dir & "out/";
				directoryCreate( path );
				scanForPlugins( nightMonkeysLoader() );

				var img = imageNew( "", 40, 30, "rgb", "red" );
				imageWrite( img, path & "new.png", true );
				imageWrite( img, path & "new.jpg", true );
				expect( imageInfo( imageRead( path & "new.png" ) ).width ).toBe( 40 );
				expect( imageInfo( imageRead( path & "new.jpg" ) ).height ).toBe( 30 );

				var photo = imageRead( srcImage );
				imageResize( photo, 100, "" );
				expect( imageGetWidth( photo ) ).toBe( 100 );
				expect( len( imageGetBlob( photo ) ) ).toBeGT( 0 );
				expect( arrayLen( getReadableImageFormats().listToArray() ) ).toBeGT( 0 );
			});

			it( title="a NightMonkeys reader left registered without its natives is removed, ImageIO.read works again", body=function( currentSpec ) {
				if ( nativesAvailable() ) return; // libheif/libjxl installed, the reader is fine
				var registry = createObject( "java", "javax.imageio.spi.IIORegistry" ).getDefaultInstance();
				var spiClass = extensionLoader().loadClass( "com.github.gotson.nightmonkeys.heif.imageio.plugins.HeifImageReaderSpi" );
				// what ImageIO.scanForPlugins() does when the services entry is visible: the first registration
				// fails quietly, the next ones throw from onRegistration (the JDK catches and prints it), and on
				// Java 22+ the reader then stays registered and breaks ImageIO.read for every format
				loop times=2 {
					try {
						registry.registerServiceProvider( spiClass.newInstance() );
					}
					catch ( any e ) {
						// older JDKs let the error through, the state is the same
					}
				}
				if ( javaFeature() >= 22 ) {
					expect( nightMonkeysRegistered() ).toBeTrue( "the broken reader should be registered now (repro)" );
				}

				scanForPlugins( nightMonkeysLoader() );

				expect( nightMonkeysRegistered() ).toBeFalse();
				var bi = createObject( "java", "javax.imageio.ImageIO" ).read( createObject( "java", "java.io.File" ).init( srcImage ) );
				expect( bi.getWidth() ).toBeGT( 0 );
				expect( imageGetWidth( imageRead( srcImage ) ) ).toBeGT( 0 );
			});

			it( title="HEIF/JXL are only registered globally when the native libraries load", body=function( currentSpec ) {
				scanForPlugins( nightMonkeysLoader() );
				if ( nativesAvailable() ) return;
				expect( nightMonkeysRegistered() ).toBeFalse();
			});
		});
	}

	private function extensionLoader() {
		return imageNew( "", 1, 1 ).getClass().getClassLoader();
	}

	private function nightMonkeysLoader() {
		var dirUrl = createObject( "java", "java.io.File" ).init( dir ).toURI().toURL();
		return createObject( "java", "java.net.URLClassLoader" ).init( javaArray( "java.net.URL", [ dirUrl ] ), extensionLoader() );
	}

	private function scanner() {
		return extensionLoader().loadClass( "org.lucee.extension.image.util.ImageIOPlugins" );
	}

	private numeric function scanForPlugins( loader ) {
		var clClass = extensionLoader().loadClass( "java.lang.ClassLoader" );
		return scanner().getMethod( "scanForPlugins", javaArray( "java.lang.Class", [ clClass ] ) ).invoke( javaCast( "null", "" ), javaArray( "java.lang.Object", [ arguments.loader ] ) );
	}

	private boolean function nativesAvailable() {
		return extensionLoader().loadClass( "org.lucee.extension.image.coder.NightMonkeysNative" )
			.getMethod( "isAvailable", javaArray( "java.lang.Class", [] ) ).invoke( javaCast( "null", "" ), javaArray( "java.lang.Object", [] ) );
	}

	// a typed Java array (URL[], Class[], Object[]) for constructors and reflection calls
	private function javaArray( required string className, required array items ) {
		return javaCast( arguments.className & "[]", arguments.items );
	}

	private boolean function nightMonkeysRegistered() {
		var registry = createObject( "java", "javax.imageio.spi.IIORegistry" ).getDefaultInstance();
		var spis = registry.getServiceProviders( createObject( "java", "java.lang.Class" ).forName( "javax.imageio.spi.ImageReaderSpi" ), false );
		while ( spis.hasNext() ) {
			if ( spis.next().getClass().getName() contains "nightmonkeys" ) return true;
		}
		return false;
	}

	private numeric function javaFeature() {
		return createObject( "java", "java.lang.Runtime" ).version().feature();
	}
}
