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
 * Tracks a file that has changed (its checksum has changed, but path hasn't)
 */
public class ChangedEntry extends FileEntry {
	/**
	 * The new checksum
	 */
	private byte[] newChecksum;
	
	/**
	 * Construct a ChangedEntry using an old and new entry. These should different
	 * only by their checksum.
	 * 
	 * @param oldEntry The old FileEntry
	 * @param newEntry  the new file entry
	 */
	public ChangedEntry(FileEntry oldEntry, FileEntry newEntry) {
		super(oldEntry.getChecksum(), oldEntry.getFileSize(),
				  oldEntry.getLastModTime(), oldEntry.getPathname());
		if (!oldEntry.getPathname().equals(newEntry.getPathname())) {
			throw new IllegalArgumentException("The two parameters must have the same pathname");
		}
		newChecksum = newEntry.getChecksum();
	}
	
	/**
	 * @return The checksum of the new entry
	 */
	public byte[] getNewChecksum() {
		return newChecksum;
	}

	/**
	 * {@inheritdoc}
	 * 
	 * @param other The other object to compare with
	 * @return true if these share the same field values
	 */
	@Override
	public boolean equals(Object other) {
		if ((other != null) && (other instanceof ChangedEntry)) {
			ChangedEntry otherRecord = (ChangedEntry) other;
			if ((super.equals(otherRecord)) &&
				Arrays.equals(getNewChecksum(), otherRecord.getNewChecksum())) {
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
		hash = 53 * hash + Arrays.hashCode(this.newChecksum);
		return hash;
	}

}
