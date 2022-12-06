<cfscript>
    encoders = imageFormats( true ).encoder;

    dir = getTempDirectory();
    /*
    dir = "c:\temp\images";

    files = directoryList(dir);
    for (f in files)
        fileDelete( f );
    */

    skipFormats = {
        "icns" :true,
        "bmp" : true,
        "wbmp" : true,
        "ico": true
    }

    systemOutput("", true);
    loop collection=#encoders# key="codec" item="formats" {
        systemOutput(codec, true);
        if ( len(formats) eq 0 )
            systemOutput("no encoding formats supported", true);
        else
            systemOutput(chr(9) & "Encoding Formats supported: " & formats.toJson(), true);
        loop array=#formats# item="format" {
            systemOutput(format, true);
            if (structKeyExists(skipFormats, format))
                continue;

            try {
                img=imageNew("",256,256,"RGB","45aaf2");
                style={size="26",style="Italic"};
                ImageDrawText(img, "codec:[#url.codec#]" ,10,20,style);
                ImageDrawText(img, "format:[#format#]" ,10,50,style);
                temp = getTempFile(dir, "test-codec-#url.codec#-", format);
                systemOutput(temp, true);
                fileDelete(temp);
                ImageWrite(name=img, destination=temp, noMetaData=true);
                if (!fileExists(temp))
                    echo("error with codec [#codec#] with [#format#], "
                        & "no image file produced, should have thrown an error#chr(10)#");
                //ImageRead(temp);
                //ImageWriteToBrowser(temp);
            } catch(e){
                echo("error with codec [#codec#] with [#format#] -#e.message##chr(10)#");
            }
        }
    }
</cfscript>