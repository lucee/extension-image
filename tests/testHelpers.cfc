component {

	public numeric function getPixelColor( img, numeric x, numeric y ){
		var bi = ImageGetBufferedImage( arguments.img );
		return bi.getRGB( arguments.x, arguments.y );
	}

	public boolean function checkColor( numeric rgb, numeric r, numeric g, numeric b ){
		var color = createObject("java", "java.awt.Color").init( arguments.rgb );
		if ( color.getRed() eq arguments.r && color.getGreen() eq arguments.g && color.getBlue() eq arguments.b) return true;
		throw "checkColor failed, #color.getRed()# eq #arguments.r# && #color.getGreen()# eq #arguments.g# && #color.getBlue()# eq #arguments.b#";

	}

}

