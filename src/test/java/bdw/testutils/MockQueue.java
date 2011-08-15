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
package bdw.testutils;

import bdw.csum.entry.FileEntry;
import bdw.csum.entry.InvalidEntryException;
import bdw.csum.queue.EntryQueue;
import java.util.ArrayList;
import java.util.List;

/**
 * A simplistic queue which is used for testing purposes
 */
public class MockQueue extends EntryQueue {

	public List<FileEntry> fakeQueue = new ArrayList<FileEntry>();
	
	@Override
	public FileEntry dequeue() throws InvalidEntryException {
		return fakeQueue.remove(0);
	}

	@Override
	public boolean isEmpty() throws InvalidEntryException {
		return fakeQueue.isEmpty();
	}
	
}
