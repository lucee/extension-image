<cfscript>
    decoders = imageFormats( true ).decoder;

    systemOutput("", true);
    loop collection=#decoders# key="codec" item="formats" {
        systemOutput(codec, true);
        if ( len(formats) eq 0 )
            systemOutput(chr(9) & "No decode formats supported", true);
        else
            systemOutput(chr(9) & "Decode Formats supported: " & formats.toJson(), true);
    }

    systemOutput(chr(9) & "getReadableImageFormats: " & listToArray(getReadableImageFormats()).toJson(), true);
</cfscript>