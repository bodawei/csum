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
package bdw.csum.queue;

import bdw.testutils.Utils;
import bdw.csum.entry.InvalidEntryException;
import bdw.csum.entry.FileEntry;
import bdw.csum.io.BuilderUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import static junit.framework.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class ArchiveQueueTest {
	private Utils utils;
	
	@Before
	public void setUp() {
		utils = new Utils();
	}
	
	private FileEntry standardEntry() {
		Date expectedDate = new Date(0);
		byte[] expectedChecksum = {0x00};
		return new FileEntry(expectedChecksum, 1, expectedDate, "foo");
	}

	@Test
	public void construct_givesDefaultValues() throws InvalidEntryException {
		ArchiveQueue q = new ArchiveQueue(new ByteArrayInputStream(new byte[0]));
		BuilderUtils f = new BuilderUtils();
		StringBuilder b = new StringBuilder();
		f.appendDate(b, q.getStartTime());
		
		assertEquals("/", q.getBasePath());
		assertEquals("1969.12.31.16.00.00.000", b.toString());
	}

	@Test
	public void dequeue_GivenLeadingBlankLines_ReturnsAnEntry() throws InvalidEntryException, IOException {
		ArchiveQueue q = new ArchiveQueue(utils.makeInputStream("\n\n00	1	1969.12.31.16.00.00.000	foo"));
		assertEquals(standardEntry(), q.dequeue());
	}

	@Test
	public void dequeue_GivenLeadingBlankDOSLines_ReturnsAnEntry() throws InvalidEntryException, IOException {
		ArchiveQueue q = new ArchiveQueue(utils.makeInputStream("\r\n\r\n00	1	1969.12.31.16.00.00.000	foo"));
		assertEquals(standardEntry(), q.dequeue());
	}

	@Test
	public void dequeue_GivenComment_ReturnsAnEntry() throws InvalidEntryException, IOException {
		ArchiveQueue q = new ArchiveQueue(utils.makeInputStream("\n# Blah blah \n00	1	1969.12.31.16.00.00.000	foo"));
		assertEquals(standardEntry(), q.dequeue());
	}

	@Test
	public void dequeue_GivenSpacesBeforeComment_ReturnsAnEntry() throws InvalidEntryException, IOException {
		ArchiveQueue q = new ArchiveQueue(utils.makeInputStream("\n     # Blah blah \n00	1	1969.12.31.16.00.00.000	foo"));
		assertEquals(standardEntry(), q.dequeue());
	}

	@Test
	public void dequeue_OneGoodEntry_ReturnsAnEntry() throws InvalidEntryException, IOException {
		ArchiveQueue q = new ArchiveQueue(utils.makeInputStream("\n\n# Version 1\n\r00	1	1969.12.31.16.00.00.000	foo"));
		assertEquals(standardEntry(), q.dequeue());
	}

	@Test
	public void dequeue_GivenDirectoryComment_ReturnsTheDirectory() throws InvalidEntryException, IOException {
		ArchiveQueue q = new ArchiveQueue(utils.makeInputStream("\n# Directory \"/whee/fun\" \n00	1	1969.12.31.16.00.00.000	foo"));
		assertEquals("/whee/fun", q.getBasePath());
	}


	@Test
	public void dequeue_GivenDateComment_ReturnsTheDate() throws InvalidEntryException, IOException {
		ArchiveQueue q = new ArchiveQueue(utils.makeInputStream("\n# Start 2011.08.13.09.14.57.123 \n00	1	1969.12.31.16.00.00.000	foo"));
		BuilderUtils builerUtils = new BuilderUtils();
		StringBuilder b = new StringBuilder();
		builerUtils.appendDate(b, q.getStartTime());

		assertEquals("2011.08.13.09.14.57.123", b.toString());
	}

	@Test
	public void dequeue_InterlineBlankLinesAndComments_ReturnsEntriesOK() throws InvalidEntryException, IOException {
		ArchiveQueue q = new ArchiveQueue(utils.makeInputStream("00	1	1969.12.31.16.00.00.000	foo\n\n\n# This is a comment\n00	1	1969.12.31.16.00.00.000	foo\n      #another comment \n"));
		assertEquals(standardEntry(), q.dequeue());
		assertEquals(standardEntry(), q.dequeue());
		assertNull(q.dequeue());
	}

}
