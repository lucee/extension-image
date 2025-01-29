component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function testImageInfoMetaDataImageNew(){
		loop list="rgb,argb,grayscale,empty" item="local.color"{
			if (color eq "empty")
				local.img = ImageNew("",320,20);
			else
				local.img = ImageNew("",320,20, color == "empty" ? "" : color, "yellow");
			local.info= imageInfo(img);

			expect( info ).toHaveKey( "height" );
			expect( info ).toHaveKey( "width" );
			expect( info ).toHaveKey( "colormodel" );
			expect( info ).toHaveKey( "source" );
			expect( info ).notToHaveKey( "metadata" ); // image new has no meta data

			expect( info.height ).toBeNumeric();
			expect( info.width ).toBeNumeric();
			expect( info.colormodel ).toBeStruct();
			expect( info.source ).toBeEmpty();
		}
	}

	function testImageInfoMetaData(){
		local.info = imageInfo(imageRead("/test/artifacts/image.jpg"));

		expect( info ).toHaveKey( "height" );
		expect( info ).toHaveKey( "width" );
		expect( info ).toHaveKey( "metadata" );
		expect( info ).toHaveKey( "colormodel" );
		expect( info ).toHaveKey( "source" );

		expect( info.height ).toBeNumeric();
		expect( info.width ).toBeNumeric();
		expect( info.metadata ).toBeStruct();
		expect( info.colormodel ).toBeStruct();
		expect( info.source ).notToBeEmpty();
	}
}