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

import bdw.csum.io.CSumReader;
import bdw.csum.io.BuilderUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/**
 * Gathers several pieces of information about a file together.
 */
public class FileEntry {
	/**
	 * The checksum
	 */
	protected byte[] checksum;

	/**
	 * The pathname
	 */
	protected String pathname;
	
	/**
	 * Byte-length of the file
	 */
	protected long fileLength;
	
	/**
	 * Last mod time.
	 */
	protected Date lastModDate;

	/**
	 * Sets the entry to managing the provided values.
	 * 
	 * @param digest The checksum for the file
	 * @param length The number of bytes in the file
	 * @param lastMod The last mod time for the file.
	 * @param partialPathname The pathname relative to the root of the scanning.
	 */
	public FileEntry(byte[] digest, long length, Date lastMod, String partialPathname) {
		checksum = digest;
		fileLength = length;
		lastModDate = lastMod;
		if (partialPathname.startsWith("./")) {
			pathname = partialPathname;
		} else {
			pathname = "./" + partialPathname;
		}			
	}

	/**
	 * Reads information about a file from an archive (output from toString()).
	 */
	public FileEntry(CSumReader source) throws IOException {
		BuilderUtils f = new BuilderUtils();
		
		checksum = source.readHexString();
		fileLength = Long.parseLong(source.readWord());
		lastModDate = source.readDate();
		pathname = source.readPath();
		if (!pathname.startsWith("./")) {
			pathname = "./" + pathname;
		}
	}

	/**
	 * @return The checksum value.
	 */
	public byte[] getChecksum() {
		return checksum;
	}

	/**
	 * @return The old pathname
	 */
	public String getPathname() {
		return pathname;
	}
	
	/**
	 * @return the length of the file
	 */
	public long getFileSize() {
		return fileLength;
	}
	
	/**
	 * @return The last modification time
	 */
	public Date getLastModTime() {
		return lastModDate;
	}

	/**
	 * {@inheritdoc}
	 * 
	 * @param other The other object to compare with
	 * @return true if these share the same field values
	 */
	@Override
	public boolean equals(Object other) {
		if ((other != null) && (other instanceof FileEntry)) {
			FileEntry otherEntry = (FileEntry) other;
			if (Arrays.equals(getChecksum(), otherEntry.getChecksum()) && 
					getPathname().equals(otherEntry.getPathname()) &&
					(getFileSize() == otherEntry.getFileSize()) &&
					getLastModTime().equals(otherEntry.getLastModTime())){
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
		int hash = 7;
		hash = 83 * hash + Arrays.hashCode(this.checksum);
		hash = 83 * hash + (this.pathname != null ? this.pathname.hashCode() : 0);
		hash = 83 * hash + (int) (this.fileLength ^ (this.fileLength >>> 32));
		hash = 83 * hash + (this.lastModDate != null ? this.lastModDate.hashCode() : 0);
		return hash;
	}


	@Override
	public String toString() {
		BuilderUtils builderUtils = new BuilderUtils();
		StringBuilder builder = new StringBuilder();
		
		builderUtils.appendHexString(builder, checksum);
		builder.append("\t");
		builder.append(fileLength);		
		builder.append("\t");
		builderUtils.appendDate(builder, lastModDate);
		builder.append("\t");
		builderUtils.appendPath(builder, pathname);
	
		return builder.toString();
	}	
	
}
