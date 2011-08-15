/*
 *  Copyright 2011 柏大衛
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bdw.csum.io;

import java.util.Calendar;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;


public class BuilderUtilsTest {
	protected BuilderUtils sut;
	protected StringBuilder builder;
	
	@Before
	public void setUp() {
		sut = new BuilderUtils();
		builder = new StringBuilder();
	}
	
	@Test
	public void formatDate_NullDate_WritesNull() {
		sut.appendDate(builder, null);
		Assert.assertEquals("null", builder.toString());
	}
	
	@Test
	public void formatDate_OrdinaryDate_WrittenInStandardFormat() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(0);
		c.set(1010, 9, 10, 10, 10, 10);

		sut.appendDate(builder, c.getTime());
		Assert.assertEquals("1010.10.10.10.10.10.000", builder.toString());
	}

	@Test
	public void formatPath_OrdinaryPath_WrittenInStandardFormat() {
		sut.appendPath(builder, "/Users/foo");
		Assert.assertEquals("\"/Users/foo\"", builder.toString());
	}

	@Test
	public void formatPath_PathWithManySpecialCharacters_QuotedAppropriately() {
		sut.appendPath(builder, "/Users/foo/ bad\"path\\name\nwith\rnewlines\t  ");
		Assert.assertEquals("\"/Users/foo/ bad\\\"path\\\\name\\nwith\\rnewlines	  \"", builder.toString());
	}

	@Test
	public void appendHexString_OrdinaryByteArray_AppendedWithoutProblems() {
		byte[] byteArray = {0x00, 0x01, 0x79, (byte)0x80, (byte)0x81, (byte)0xff };
		sut.appendHexString(builder, byteArray);
		Assert.assertEquals("0001798081ff", builder.toString());
	}

	@Test
	public void appendHexString_EmptyByteArray_AppendsNothing() {
		byte[] byteArray = new byte[0];
		sut.appendHexString(builder, byteArray);
		Assert.assertEquals("", builder.toString());
	}
}
