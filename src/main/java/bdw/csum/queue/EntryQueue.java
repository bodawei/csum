/*
 *  Copyright 2011-2015 柏大衛
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
import java.util.Date;

/**
 * A queue of Entries, where individual entries can only be dequeued.
 * A queue has some information about the set of entries it holds (e.g. the
 * base pathname shared by all entries) as well as the entries that may be
 * removed. Queues usually do some kind of work to discover the next entry when
 * asked to dequeue, and will throw an exception readily when there is a problem
 * with that source.
 */
public abstract class EntryQueue  {
	/**
	 * The base pathname for all entries (e.g. /Users/foo where all entries 
	 * are children of it.  Always ends with "/"
	 */
	protected String basePath;
	
	/**
	 * The time that this set of entries was started to be assembled.
	 */
	protected Date startTime;

	/**
	 * Construct, with a default start time of 0 milliseconds, and base
	 * path of "/"
	 */
	public EntryQueue() {
		startTime = new Date(0);
		basePath = "/";
	}
	/**
	 * @return The next entry in the queue, or null if no more.
	 * @throws bdw.csum.entry.InvalidEntryException
	 * @throws InvalidEntryException if a problem is found when trying to retrieve the next
	 * entry.
	 */
	public abstract FileEntry dequeue() throws InvalidEntryException;

	/**
	 * @return true if there are no more entries in the queue (and the next
	 * dequeue() will return null)
	 * @throws bdw.csum.entry.InvalidEntryException
	 */
	public abstract boolean isEmpty() throws InvalidEntryException;
	
	/**
	 * @return The time that this set of entries started being created. Always
	 * in conventional format.
	 */
	public Date getStartTime() {
		return startTime;
	}
	
	/**
	 * @return The pathname 
	 */
	public String getBasePath() {
		return basePath;
	}
}
