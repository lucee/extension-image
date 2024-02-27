<!--- 
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	
	variables.imageDir=GetDirectoryFromPath(GetCurrentTemplatePath())&"image/";
	variables.tmpDir=GetDirectoryFromPath(GetCurrentTemplatePath())&"temp/";
	
	public void function test() {
		var src=variables.imageDir&"BigBen.jpg";
		var trg=variables.tmpDir&"BigBen.jpg";
		
		try {
			// create directory
			if(!directoryExists(variables.tmpDir)) directoryCreate(variables.tmpDir);
			
			// create file
			if(fileExists(trg)) fileDelete(trg);
			fileCopy(src,trg);

			var img=imageRead(trg);
			assertEquals(3264,ImageGetHeight(img));
			assertEquals(2448,ImageGetWidth(img));

			imageScaleTofit(img,"",50);
			imageWrite(img);

			var img2=imageRead(trg);
			assertEquals(50,ImageGetHeight(img2));
		}
		finally {
			DirectoryDelete(variables.tmpDir,true);
		}
	}
} 
</cfscript>