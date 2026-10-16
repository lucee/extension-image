<cfset img = imageNew( "", 100, 100, "rgb", "red" )>
<cfimage action="writeToBrowser" source="#img#" format="png">
