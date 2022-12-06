<cfscript>
	dir = getTempDirectory();
	img=imageNew("",300,300,"RGB","45aaf2");
	style={size="26",style="Italic"};
	ImageDrawText(img, "codec:[#url.codec#]" ,10,20,style);
	ImageDrawText(img, "format:[#format#]" ,10,50,style);
	temp = getTempFile(dir, "test-codec-#url.codec#-", format);
	fileDelete(temp);
	hasError="false";
	try {
		ImageWrite(img, temp); // should throw if unsupported
	} catch( e ) {
		hasError="true";
	}

	if ( !hasError )
		echo("should have thrown an error");
	//if ( !fileExists(temp) )
	//	echo("no image file produced, should have thrown an error");
	
</cfscript>