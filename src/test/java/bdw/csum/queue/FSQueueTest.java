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

import bdw.csum.entry.InvalidEntryException;
import bdw.csum.entry.FileEntry;
import java.util.ArrayList;
import java.util.List;
import static junit.framework.Assert.*;
import org.junit.Test;

/**
 *
 */
public class FSQueueTest {
	@Test
	public void dequeue_withOneFile_ReturnsRightFile() throws InvalidEntryException {
		FSQueue q = new FSQueue("./src/test/resources/dir1");
		
		assertEquals("./foo.txt", q.dequeue().getPathname());
	}

	@Test
	public void dequeue_WithNonFile_ReturnsNull() throws InvalidEntryException {
		FSQueue q = new FSQueue("./IDontExist");
		
		assertNull(q.dequeue());
	}

	@Test
	public void dequeue_intermediateDirectories_Ignored() throws InvalidEntryException {
		FSQueue q = new FSQueue("./src/test/resources/dir2");
		
		assertEquals("./第一个孩子/   b    \" \\ !@#$/foo.txt", q.dequeue().getPathname());
		assertNull(q.dequeue());
	}

	@Test
	public void dequeue_SpecialMacFilesFlagSetToTrue_FilesIgnored() throws InvalidEntryException {
		FSQueue q = new FSQueue("./src/test/resources/weirdFiles");
		q.setIgnoreSpecialMacFiles(true);
		
		assertEquals("./Another file", q.dequeue().getPathname());
		assertNull(q.dequeue());
	}

	@Test
	public void dequeue_WhenSpecialFileIsLast_DoesntThrowException() throws InvalidEntryException {
		FSQueue q = new FSQueue("./src/test/resources/weirdAtEnd");
		q.setIgnoreSpecialMacFiles(true);
		
		assertNull(q.dequeue());
	}

	@Test
	public void dequeue_SpecialMacFilesFlagSetToFalse_FilesListed() throws InvalidEntryException {
		FSQueue q = new FSQueue("./src/test/resources/weirdFiles");
		q.setIgnoreSpecialMacFiles(false);
		List<String> paths = new ArrayList<String>();
		
		paths.add(q.dequeue().getPathname());
		paths.add(q.dequeue().getPathname());
		paths.add(q.dequeue().getPathname());
		assertTrue(paths.contains("./Another file"));
		assertTrue(paths.contains("./._Another file"));
		assertTrue(paths.contains("./.DS_Store"));
		assertNull(q.dequeue());
	}

	@Test
	public void dequeue_FilesOnEdgeOfBufferReadingSize_ReadWithoutErrors() throws InvalidEntryException {
		FSQueue q = new FSQueue("./src/test/resources/dir3");
		int seen1023 = 0;
		int seen1024 = 0;
		int seen1025 = 0;
		int other = 0;
		
		FileEntry entry = q.dequeue();
		
		while (entry != null) {
			long size = entry.getFileSize();
			switch ((int)size) {
				case 1023:
					seen1023 ++;
					break;
				case 1024:
					seen1024 ++;
					break;
				case 1025:
					seen1025 ++;
					break;
				default:
					other ++;
					break;
			}
			entry = q.dequeue();
		}
		
		assertEquals(1, seen1023);
		assertEquals(1, seen1024);
		assertEquals(1, seen1025);
		assertEquals(0, other);
	}

	// this is a bugfix.  not sure why this was failing
//	@Test
//	public void dequeue_strangeFilename_ParsedOK() throws InvalidEntry {
//		FSQueue q = new FSQueue("./src/test/resources/nonexistent");
//		
//		assertEquals("Cimages/≠I≠t¶°VHF∏ı¿WµLΩuπqæ˜πœ.JPG", q.dequeue().getPathname());
//		assertNull(q.dequeue());
//	}

}
