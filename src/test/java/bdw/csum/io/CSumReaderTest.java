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

import bdw.testutils.Utils;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class CSumReaderTest {
	private Utils utils;
	
	@Before
	public void setUp() {
		utils = new Utils();
	}
	
	@Test
	public void readHexString_AtEOS_ReturnsEmptyArray() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream(""));
		
		assertArrayEquals(new byte[0], r.readHexString());
	}
	
	@Test
	public void readHexString_WithHexString_ReturnsByteEquivalent() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream("3456"));
		byte[] expected = {0x34, 0x56};
		
		assertArrayEquals(expected, r.readHexString());
		assertEquals(-1, r.read());
	}

	@Test
	public void readHexString_WithShortHexString_ReturnsOnlyCompleteBytes() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream("345"));
		byte[] expected = {0x34};
		
		assertArrayEquals(expected, r.readHexString());
		assertEquals('5', r.read());
	}

	@Test
	public void readHexString_WithExtremeBytes_ReturnsUncorruptedBytes() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream("ff00"));
		byte[] expected = {-1, 0x00};
		
		assertArrayEquals(expected, r.readHexString());
	}

	@Test
	public void readHexString_WithLeadingWhitespace_Ignores() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream("		  3456"));
		byte[] expected = {0x34, 0x56};
		
		assertArrayEquals(expected, r.readHexString());
		assertEquals(-1, r.read());
	}

	@Test
	public void readHexString_WithTrailingWhitespace_SkipsOverWhitespace() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream("3456     8"));
		byte[] expected = {0x34, 0x56};
		
		assertArrayEquals(expected, r.readHexString());
		assertEquals('8', r.read());
	}

	@Test
	public void readPath_WithUnquotedPath_EndsAtEOL() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream("on/two/three\nfour five"));
		
		assertEquals("on/two/three", r.readPath());
		assertEquals('\n', r.read());
	}


	@Test
	public void readPath_WithEmptyPath_ReturnsEmptyString() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream("\nfour five"));
		
		assertEquals("", r.readPath());
	}

	@Test
	public void readPath_WithQuotedPath_TerminatesAtUnquotedQote() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream("\"foo\\\"bar\"baz\""));
		
		assertEquals("foo\"bar", r.readPath());
	}

	@Test
	public void readPath_WithBackslash_PreservesBackslash() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream("\"foo\\\\bar\""));
		
		assertEquals("foo\\bar", r.readPath());
	}

	@Test
	public void readPath_LeadingAndTrainingWhitespace_Ignored() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream("		  \"foobar\"		 "));
		
		assertEquals("foobar", r.readPath());
	}

	@Test
	public void readPath_LeadingWhitespaceWithoutQuotes_Ignored() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream("		  foo\\\\bar\"		 "));
		
		assertEquals("foo\\bar\"		 ", r.readPath());
	}

	@Test
	public void readWord_LeadingAndTrailingWhitespace_Ignores() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream("			 hi there"));
		
		assertEquals("hi", r.readWord());
		assertEquals('t', r.read());
	}

	@Test
	public void readWord_WordTerminatedByEOL_HonorsEOL() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream("			 hith\nere"));
		
		assertEquals("hith", r.readWord());
		assertEquals('\n', r.read());
	}

	@Test
	public void readDate_ValidDate_ParsedCorrectly() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream("2010.10.10.01.02.03.456"));
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(456);
		c.set(2010, 9, 10, 01, 02, 03);
		assertEquals(c.getTime(), r.readDate());
	}

	@Test
	public void readDate_WithLeadingAndTrailingSpaces_Skips() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream("   2010.10.10.01.02.03.456     5"));
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(456);
		c.set(2010, 9, 10, 01, 02, 03);
		assertEquals(c.getTime(), r.readDate());
		assertEquals('5', r.read());
	}

	@Test
	public void readDate_InvalidDateTime_ThrowsExceptionAndResets() throws IOException {
		CSumReader r = new CSumReader(utils.makeInputStream("   2010.ab.10.01.02.03.456     5"));
		try {
			Date result = r.readDate();
			fail("Got this date: " + result);
		} catch (IOException e) {
			assertEquals('5', r.read());
		}
	}
}
