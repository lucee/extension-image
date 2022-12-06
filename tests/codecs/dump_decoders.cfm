<cfscript>
    decoders = imageFormats( true ).decoder;

    systemOutput("", true);
    loop collection=#decoders# key="codec" item="formats" {
        systemOutput(codec, true);
        if ( len(formats) eq 0 )
            systemOutput("no decode formats supported", true);
        else
            systemOutput(chr(9) & "Decode Formats supported: " & formats.toJson(), true);
    }

    systemOutput("GetReadableImageFormats(): " & GetReadableImageFormats().toJson(), true);
    
</cfscript>