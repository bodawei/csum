/*
 *  Copyright 2011-2016 柏大衛
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

import bdw.csum.entry.InvalidEntryException;
import bdw.csum.queue.EntryQueue;
import bdw.csum.entry.MovedEntry;
import bdw.csum.entry.ChangedEntry;
import bdw.csum.entry.FileEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This class takes to EntryQueues, and reports on differences
 * between them. Given the assumed order of the two reports (old and new)
 * this reports on files added (in the second but not the first),
 * removed (in first but not the second), changed (same path in both, but
 * different checksums), unchanged (same path and checksum in both) and
 * moved or renamed (same checksum in both, different path).  This last
 * set is the least reliable, since it isn't unreasonable to have
 * files with the same contents in different places in the system, and
 * this may think such a file has been moved or renamed even if it really is
 * removed from the old list and added in the new.  As a heuristic to reduce
 * the mistakes there, if there are two files in the new report which
 * have the same checksum, this will ignore them and consider the old one
 * to have been removed and these to be added.
 * This also can't detect a file that was
 * moved and changed (which will look like an added and removed file).
 */
public class Resolver {
	/**
	 * The set of files that haven't changed.
	 */
	private final HashSet<FileEntry> unchangedSet;
	
	/**
	 * The set of files added to the new report
	 */
	private final HashSet<FileEntry> addedSet;
	
	/**
	 * The set of files removed from the first report
	 */
	private final HashSet<FileEntry> removedSet;
	
	/**
	 * The set of files changed between the two reports
	 */
	private final HashSet<FileEntry> changedSet;
	
	/**
	 * Files that appear to be moved and renamed.
	 */
	private HashSet<FileEntry> movedOrRenamedSet;
	
	/**
	 * Constructor.  This extracts entries from the old and new reports
	 * and builds the set differences.
	 * 
	 * @param oldReport A queue of old file entries
	 * @param newReport A queue of new file entries
	 * @throws InvalidEntryException If an error occurs while retrieving the entries from the queues
	 */
	public Resolver(EntryQueue oldReport, EntryQueue newReport) throws InvalidEntryException {		
		unchangedSet = new HashSet<FileEntry>();
		addedSet = new HashSet<FileEntry>();
		removedSet = new HashSet<FileEntry>();
		changedSet = new HashSet<FileEntry>();
		movedOrRenamedSet = new HashSet<FileEntry>();

		buildSets(oldReport, newReport);
	}

	/**
	 * @return a list of FileEntries listing files that didn't
	 *		change between the two reports. (never null)
	 */
	public Set<FileEntry> getUnchangedFiles() {
		return unchangedSet;
	}
	
	/**
	 * @return a list of FileEntries listing files that had their
	 *		checksums change between the two reports. (never null)
	 */
	public Set<FileEntry> getChangedFiles() {
		return changedSet;
	}
	
	/**
	 * See the class comment for caveats about this set.
	 * 
	 * @return a list of FileEntries that seep to have been moved or
	 *		renamed between the two reports. (never null)
	 */
	public Set<FileEntry> getMovedOrRenamedFiles() {
		return movedOrRenamedSet;
	}
	
	/**
	 * @return a list of FileEntries listing files that were in the
	 *		new wreport but not the old. (never null)
	 */
	public Set<FileEntry> getAddedFiles() {
		return addedSet;
	}
	
	/**
	 * @return a list of FileEntries listing files that were in the
	 *		old report but not the new. (never null)
	 */
	public Set<FileEntry> getRemovedFiles() {
		return removedSet;
	}

	/**
	 * Compares the mapping of old entries to the new entries and builds
	 * the difference sets.
	 * 
	 * @param oldMap The mapping of old file entries
	 * @param newMap The mapping of new file entries
	 */
	private void buildSets(EntryQueue oldEntries, EntryQueue newEntries) throws InvalidEntryException {
		HashMap<String, FileEntry> oldMap = new HashMap<String, FileEntry>();
		while (!oldEntries.isEmpty()) {
			FileEntry oldEntry = oldEntries.dequeue();
			if (oldMap.containsKey(oldEntry.getPathname())) {
				throw new InvalidEntryException("One file is in the old list twice: " + oldEntry.getPathname());
			}
			oldMap.put(oldEntry.getPathname(), oldEntry);
		}
		
		// Compute the added, changed and unchanged sets
		HashMap<String, FileEntry> newMap = new HashMap<String, FileEntry>();
		while (!newEntries.isEmpty()) {
			FileEntry newEntry = newEntries.dequeue();
			
			if (newMap.containsKey(newEntry.getPathname())) {
				throw new InvalidEntryException("One file is in the new list twice: " + newEntry.getPathname());
			} else {
				newMap.put(newEntry.getPathname(), newEntry);
			}

			FileEntry oldEntry = oldMap.remove(newEntry.getPathname());
			if (oldEntry == null) {
				addedSet.add(newEntry);
			} else {
				if (Arrays.equals(oldEntry.getChecksum(), newEntry.getChecksum())) {
					unchangedSet.add(newEntry);
				} else {
					changedSet.add(new ChangedEntry(oldEntry, newEntry));
				}
			}
		}
		newMap = null; // allow quicker garbage collection.

		// The removed set is just what is left in the old mapping.
		removedSet.addAll(oldMap.values());
				
		// Look for moved and renamed files.  These will be things
		// in both the added and removed sets with the same checksum.
		// Ignore things that have multiple entries in the new set.
		// This isn't as precise a determination as the other sets,
		// but will be mostly right, which is good enough, and
		// certainly better than nothing.
		Set<FileEntry> removedEntries = new HashSet<FileEntry>();
		for (FileEntry removedEntry : removedSet) {
			int numAdded = 0;
			FileEntry theAddedEntry = null;
			
			for (FileEntry addedEntry : addedSet) {
				if (Arrays.equals(addedEntry.getChecksum(), removedEntry.getChecksum())) {
					numAdded++;
					theAddedEntry = addedEntry;
				}
			}
			if (numAdded == 1) {
				movedOrRenamedSet.add(new MovedEntry(removedEntry, theAddedEntry));
				removedEntries.add(removedEntry);
				addedSet.remove(theAddedEntry);
			}
		}
		
		removedSet.removeAll(removedEntries);
	}

}