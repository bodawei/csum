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
package bdw.csum;

import bdw.csum.entry.ChangedEntry;
import bdw.csum.entry.FileEntry;
import bdw.csum.entry.InvalidEntryException;
import bdw.csum.entry.MovedEntry;
import bdw.testutils.MockQueue;
import java.io.IOException;
import java.util.Date;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Test;

public class ResolverTest {

	private Resolver resolver;

	
	private void assertSetContains(Set<FileEntry> set, FileEntry element) {
		Assert.assertTrue(set.contains(element));
	}

	private byte[] checkum1() {
		byte[] cs = {0x00, 0x01, 0x02};
		return cs;
	}

	private byte[] checkum2() {
		byte[] cs = {0x10, 0x11, 0x12};
		return cs;
	}

	private byte[] checkum3() {
		byte[] cs = {0x30, 0x31, 0x32};
		return cs;
	}

	@Test
	public void removedFileShowsUpInRemovedSet() throws IOException, InvalidEntryException {
		Date now = new Date();
		MockQueue oldQueue = new MockQueue();
		oldQueue.fakeQueue.add(new FileEntry(checkum1(), 1, now, "old.txt"));

		MockQueue newQueue = new MockQueue();
		newQueue.fakeQueue.add(new FileEntry(checkum2(), 1, new Date(), "new.txt"));

		resolver = new Resolver(oldQueue, newQueue);

		assertSetContains(resolver.getRemovedFiles(), new FileEntry(checkum1(), 1, now, "old.txt"));		
	}

	@Test(expected=InvalidEntryException.class)
	public void duplicateFileIsRejected() throws InvalidEntryException {
		MockQueue oldQueue = new MockQueue();
		oldQueue.fakeQueue.add(new FileEntry(checkum1(), 1, new Date(), "old.txt"));
		oldQueue.fakeQueue.add(new FileEntry(checkum2(), 1, new Date(), "old.txt"));

		MockQueue newQueue = new MockQueue();
		newQueue.fakeQueue.add(new FileEntry(checkum3(), 1, new Date(), "new.txt"));

		resolver = new Resolver(oldQueue, newQueue);
	}

	@Test
	public void duplicateChecksumIsNoBigDeal() throws InvalidEntryException {
		MockQueue oldQueue = new MockQueue();
		oldQueue.fakeQueue.add(new FileEntry(checkum1(), 1, new Date(), "old1.txt"));
		oldQueue.fakeQueue.add(new FileEntry(checkum1(), 1, new Date(), "old2.txt"));

		MockQueue newQueue = new MockQueue();
		newQueue.fakeQueue.add(new FileEntry(checkum3(), 1, new Date(), "new.txt"));

		resolver = new Resolver(oldQueue, newQueue);
	}

	@Test(expected=InvalidEntryException.class)
	public void duplicateOldNewFileIsRejected() throws InvalidEntryException {
		MockQueue oldQueue = new MockQueue();
		oldQueue.fakeQueue.add(new FileEntry(checkum1(), 1, new Date(), "old.txt"));

		MockQueue newQueue = new MockQueue();
		newQueue.fakeQueue.add(new FileEntry(checkum3(), 1, new Date(), "new.txt"));
		newQueue.fakeQueue.add(new FileEntry(checkum2(), 1, new Date(), "new.txt"));

		resolver = new Resolver(oldQueue, newQueue);
	}

	@Test
	public void fileAddedIsRecognizedAsAdded() throws InvalidEntryException {
		Date now = new Date();
		MockQueue oldQueue = new MockQueue();
		oldQueue.fakeQueue.add(new FileEntry(checkum2(), 1, now, "old.txt"));

		MockQueue newQueue = new MockQueue();
		newQueue.fakeQueue.add(new FileEntry(checkum1(), 1, now, "new.txt"));

		resolver = new Resolver(oldQueue, newQueue);

		assertSetContains(resolver.getAddedFiles(), new FileEntry(checkum1(), 1, now, "new.txt"));		
	}

	@Test
	public void unchangedFileIsRecognizedAsUnchanged() throws InvalidEntryException {
		Date now = new Date();
		MockQueue oldQueue = new MockQueue();
		oldQueue.fakeQueue.add(new FileEntry(checkum1(), 1, now, "old.txt"));

		MockQueue newQueue = new MockQueue();
		newQueue.fakeQueue.add(new FileEntry(checkum1(), 1, now, "old.txt"));

		resolver = new Resolver(oldQueue, newQueue);

		assertSetContains(resolver.getUnchangedFiles(), new FileEntry(checkum1(), 1, now, "old.txt"));
	}

	@Test
	public void changedFileIsRecognizedAsChanged() throws InvalidEntryException {
		Date now = new Date();
		MockQueue oldQueue = new MockQueue();
		oldQueue.fakeQueue.add(new FileEntry(checkum1(), 1, now, "old.txt"));

		MockQueue newQueue = new MockQueue();
		newQueue.fakeQueue.add(new FileEntry(checkum2(), 1, now, "old.txt"));

		resolver = new Resolver(oldQueue, newQueue);

		ChangedEntry expected = new ChangedEntry(new FileEntry(checkum1(), 1, now, "old.txt"),
				  new FileEntry(checkum2(), 1, now, "old.txt"));
		
		assertSetContains(resolver.getChangedFiles(), expected);
	}

	@Test
	public void movedFileIsRecognizedAsMoved() throws InvalidEntryException {
		Date now = new Date();
		MockQueue oldQueue = new MockQueue();
		oldQueue.fakeQueue.add(new FileEntry(checkum1(), 1, now, "old.txt"));

		MockQueue newQueue = new MockQueue();
		newQueue.fakeQueue.add(new FileEntry(checkum1(), 1, now, "other/old.txt"));

		resolver = new Resolver(oldQueue, newQueue);

		MovedEntry expected = new MovedEntry(new FileEntry(checkum1(), 1, now, "old.txt"),
				  new FileEntry(checkum1(), 1, now, "other/old.txt"));
		

		assertSetContains(resolver.getMovedOrRenamedFiles(), expected);
	}

	@Test
	public void twoNewFilesWithSameChecksumAsOldAreNotConsideredMoved() throws InvalidEntryException {
		Date now = new Date();
		MockQueue oldQueue = new MockQueue();
		oldQueue.fakeQueue.add(new FileEntry(checkum1(), 1, now, "one.txt"));

		MockQueue newQueue = new MockQueue();
		newQueue.fakeQueue.add(new FileEntry(checkum1(), 1, now, "two.txt"));
		newQueue.fakeQueue.add(new FileEntry(checkum1(), 1, now, "three.txt"));

		resolver = new Resolver(oldQueue, newQueue);

		Assert.assertEquals(0, resolver.getMovedOrRenamedFiles().size());
	}
}