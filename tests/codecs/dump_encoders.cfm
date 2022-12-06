<cfscript>
    encoders = imageFormats( true ).encoder;

    systemOutput("", true);
    loop collection=#encoders# key="codec" item="formats" {
        systemOutput(codec, true);
        if ( len(formats) eq 0 )
            systemOutput("no encode formats supported", true);
        else
            systemOutput(chr(9) & "encode Formats supported: " & formats.toJson(), true);
    }

    systemOutput("GetWritableImageFormats(): " & GetWritableImageFormats().toJson(), true);
    
</cfscript>