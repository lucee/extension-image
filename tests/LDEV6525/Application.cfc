component {
	this.name = "LDEV6525";
	this.applicationTimeout = createTimespan( 0, 0, 0, 30 );
	// only the native WebP coder (com.github.usefulness:webp-imageio), so nothing can fall back to another coder
	this.image.coders = [ "Gotson" ];
}
