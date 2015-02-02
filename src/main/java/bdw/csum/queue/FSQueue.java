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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Stack;

/**
 * A queue that draws its data from the file system.  Base behavior is to
 * return all files under the path provided to the constructor in a depth-first
 * order through the dequeue routine
 */
public class FSQueue extends EntryQueue {
	/**
	 * The stack of files and directors we still need to scan
	 */
	protected Stack<File> todo = new Stack<File>();
	
	/**
	 * The next file to process
	 */
	protected File nextFile;
	
	/**
	 * flag indicating whether special mac files should be ignored
	 */
	protected boolean ignoreSpecialMac;
	
	/**
	 * Construct an instance. Ignores special mac files by default.
	 * @param startPath The pathname to start queuing from
	 * 
	 * @throws bdw.csum.entry.InvalidEntryException
	 */
	public FSQueue(String startPath) throws InvalidEntryException {		
		super();
		ignoreSpecialMac = true;
		startTime = new Date();
		File startFile = new File(startPath);
		basePath = startFile.getAbsolutePath() + File.pathSeparator;
		if (startFile.exists() && startFile.isDirectory()) {
			todo.push(new File(startPath));
		}
	}
	
	/**
	 * Sets flag whether "special" mac files (.DS_Store, and ._*)
	 * should be ignored
	 * @param value true if these are to be ignored.
	 */
	public void setIgnoreSpecialMacFiles(boolean value) {
		ignoreSpecialMac = value;
	}
	
	/**
	 * @return true if funky mack files are to be ignored
	 */
	public boolean getIgnoreSpecialMacFiles() {
		return ignoreSpecialMac;
	}
	
	/**
	 * {@inheritDoc}
	 * @return true if there are no more entries in the queue
	 * @throws bdw.csum.entry.InvalidEntryException
	 */
	@Override
	public boolean isEmpty() throws InvalidEntryException {
		findNext();
		return ((todo.size() == 0) && (nextFile == null));
	}

	/**
	 * {@inheritDoc}
	 * @return the next file entry, or null if there are no more.
	 * @throws InvalidEntryException if something goes amis while traversing the file system.
	 */
	@Override
	public FileEntry dequeue() throws InvalidEntryException {
		findNext();
		if (nextFile == null) {
			return null;
		}

		FileEntry entry = null;
		MessageDigest digest;
		byte[] readBuffer = new byte[1024];
		InputStream stream = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			stream = new FileInputStream(nextFile);
			
			int bytesRead = stream.read(readBuffer);
			while (bytesRead != -1) {
				digest.update(readBuffer, 0, bytesRead);
				bytesRead = stream.read(readBuffer);
			}

			entry = new FileEntry(digest.digest(), nextFile.length(),
										new Date(nextFile.lastModified()),
										nextFile.getAbsolutePath().substring(basePath.length()));
			nextFile = null;
		} catch (NoSuchAlgorithmException e) {
			throw new InvalidEntryException("NoSuchAlgorithmException while building entry for " + nextFile.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new InvalidEntryException("IOException while building entry for " + nextFile.getAbsolutePath(), e);
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
				throw new InvalidEntryException("Could not close stream for " + nextFile.getAbsolutePath(), e);
			}
		}
				
		return entry;
	}
	
	
	/**
	 * Walk through the stack of things still todo, and find the
	 * next file that we'll want to handle
	 * @throws bdw.csum.entry.InvalidEntryException
	 */
	protected void findNext() throws InvalidEntryException {
		if ((nextFile != null) || (todo.size() == 0)) {
			return;
		}

		File next = todo.pop();
		
		boolean fileIsOK = false;
		while (!fileIsOK) {
			while ((next != null) && next.isDirectory()) {
				File[] files = next.listFiles();
				// push them in reverse order, so they come out in a more expected order
				for (int index = files.length-1; index >= 0; index--) {
					todo.push(files[index]);
				}
				next = (todo.size() == 0) ? null : todo.pop();
			}

			fileIsOK = true;
			if (next != null) {
				if (ignoreSpecialMac) {
					if ((next.getName().equals(".DS_Store")) ||
						 (next.getName().startsWith("._"))) {
						fileIsOK = false;
						next = (todo.size() == 0) ? null : todo.pop();
					}
				}
			}
			if (next != null) {
				if (!next.exists()) {
					System.err.println("Skipping over Nonexistent file: " + next.getAbsoluteFile());
					fileIsOK = false;
					next = (todo.size() == 0) ? null : todo.pop();
				}
			}
		}

		
		nextFile = next;
	}
}
