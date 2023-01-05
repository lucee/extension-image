component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function testImageInfoMetaData(){
		loop list="rgb,argb,grayscale,empty" item="local.color"{
			if (color eq "empty")
				local.img = ImageNew("",320,20);
			else
				local.img = ImageNew("",320,20, color == "empty" ? "" : color, "yellow");
			local.info= imageInfo(img);

			expect( info ).toHaveKey( "height" );
			expect( info ).toHaveKey( "width" );
			expect( info ).toHaveKey( "metadata" );
			expect( info ).toHaveKey( "colormodel" );

			expect( info.height ).toBeNumeric();
			expect( info.width ).toBeNumeric();
			expect( info.metadata ).toBeStruct();
			expect( info.colormodel ).toBeStruct();
		}
	}

}