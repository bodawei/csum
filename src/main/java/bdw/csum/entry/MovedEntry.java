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

package bdw.csum.entry;

import java.util.Arrays;

/**
 * Tracks a file that has been moved between the old and new reports, but otherwise
 * not changed.
 */
public class MovedEntry extends FileEntry {
	
	/**
	 * The new pathname
	 */
	private String newPathname;
	
	/**
	 * Construct an entry with an old and new entry.  They must have the same
	 * checksum.
	 * 
	 * @param oldEntry The old entry
	 * @param newEntry The new entry
	 */
	public MovedEntry(FileEntry oldEntry, FileEntry newEntry) {
		super(oldEntry.getChecksum(), oldEntry.getFileSize(),
				  oldEntry.getLastModTime(), oldEntry.getPathname());
		if (!Arrays.equals(oldEntry.getChecksum(), newEntry.getChecksum())) {
			throw new IllegalArgumentException("The two parameters must have the same checksum");
		}
		newPathname = newEntry.getPathname();
	}

	/**
	 * @return the new pathname
	 */
	public String getNewPathname() {
		return newPathname;
	}

	/**
	 * {@inheritdoc}
	 * 
	 * @param other The other object to compare with
	 * @return true if these share the same field values
	 */
	@Override
	public boolean equals(Object other) {
		if ((other != null) && (other instanceof MovedEntry)) {
			MovedEntry otherRecord = (MovedEntry) other;
			if ((super.equals(otherRecord)) &&
				(getNewPathname().equals(otherRecord.getNewPathname()))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritdoc}
	 */
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = 97 * hash + (this.newPathname != null ? this.newPathname.hashCode() : 0);
		return hash;
	}
}
