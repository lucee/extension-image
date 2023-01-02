<cfscript>
    param name="url.action";
    encoders = imageFormats( true ).encoder;

    dir = getTempDirectory();
    /*
    dir = "c:\temp\images";

    files = directoryList(dir);
    for (f in files)
        fileDelete( f );
    */

    // skipped due to extra requirements for image size / params
    skipFormats = {
        "icns" :true,
        "bmp" : true,
        "wbmp" : true,
        "ico": true
    }

    // systemOutput("", true);
    loop collection=#encoders# key="testCodec" item="formats" {
        //systemOutput("encode.cfm #codec# #testCodec#", true);
        if ( len(formats) eq 0 )
            systemOutput(chr(9) & "No encoding formats supported", true);
        else
            systemOutput(chr(9) & "Encoding Formats supported: " & formats.toJson(), true);
        loop array=#formats# item="format" {
            if (structKeyExists(skipFormats, format))
                continue;
            //systemOutput(format & " " & testCodec & "[" & codec & "]", true);

           try {
                img=imageNew("",256,256,"RGB","45aaf2");
                style={size="26",style="Italic"};
                ImageDrawText(img, "testCodec:[#testCodec#]" ,10,20,style);
                ImageDrawText(img, "format:[#format#]" ,10,50,style);
                temp = getTempFile(dir, "test-testCodec--#codec#--#testCodec#-", format);
                // systemOutput(temp, true);
                fileDelete(temp);
                switch (url.action){
                    case "imageWrite":
                        ImageWrite(name=img, destination=temp, noMetaData=true);
                        break;
                    case "cfimageWrite":
                        cfimage(source="#img#", destination="#temp#", action="write", overwrite="true", nometadata="true");
                        break;
                    /*case 
                        disabled as some codecs only support encoding not decoding
                    "imageWriteToBrowser":
                        ImageWrite(name=img, destination=temp, noMetaData=true);
                        silent {
                            ImageWriteToBrowser(img);
                            ImageWriteToBrowser(temp);
                        }
                        break;
                    */
                    throw "unknow action [#url.action#]";
                }
                
                if (!fileExists(temp))
                    echo("error with testCodec [#testCodec#] with [#format#], "
                        & "no image file produced, should have thrown an error#chr(10)#");
                else
                    fileDelete(temp);
            } catch(e){
                echo("error with testCodec [#testCodec#] with [#format#] - #e.message##chr(10)#");
            }
        }
    }
</cfscript>