<cfscript>
    encoders = imageFormats( true ).encoder; 

    systemOutput("", true);
    loop collection=#encoders# key="codec" item="formats" {
        systemOutput(codec, true);
        if ( len(formats) eq 0 )
            systemOutput(chr(9) &  "No encode formats supported", true);
        else
            systemOutput(chr(9) & "Encode Formats supported: " & formats.toJson(), true);
    }

    systemOutput(chr(9) & "getWriteableImageFormats: " & listToArray(getWriteableImageFormats()).toJson(), true);
</cfscript>