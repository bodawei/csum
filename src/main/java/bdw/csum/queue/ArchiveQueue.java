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
import bdw.csum.io.CSumReader;
import bdw.csum.io.BuilderUtils;
import java.io.IOException;
import java.io.InputStream;

/**
 * A queue that draws its data from the an inputstream presumably written out earlier
 * by this program. The format should be a series of header comments (prefixed by #)
 * followed by a series of lines, where each line is the result of FileEntry.toString().
 * The input stream is assumed to be in UTF-8 format.
 */
public class ArchiveQueue extends EntryQueue {
	/**
	 * String to use to identify the directory comment
	 */
	public static final String DIR_COMMENT = "Directory";
	
	/**
	 * String to use to identify when this process started
	 */
	public static final String START_COMMENT = "Start";

	/**
	 * The reader used to read characters from the input stream
	 */
	private CSumReader source;
	
	/**
	 * Utility class used to help do parsing
	 */
	private BuilderUtils parseUtils;
	
	/**
	 * Constructs an archive queue.  Until it encounters comment lines
	 * specifying otherwise, this assumes that the base path is / and the
	 * start time is Dec 31, 1969
	 * @param input The stream to read from
	 * @throws bdw.csum.entry.InvalidEntryException
	 */
	public ArchiveQueue(InputStream input) throws InvalidEntryException {
		super();
		source = new CSumReader(input);
		parseUtils = new BuilderUtils();
		
		// process and skip any leading comments.
		try {
			skipAllNonData();
		} catch (Exception e) {
			throw new InvalidEntryException("Problem when trying to read the archived entry", e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @return true if there are no more entries in the queue
	 */
	@Override
	public boolean isEmpty() {
		try {
			skipAllNonData();
			if (readChar() != -1) {
				unreadChar();
				return false;
			}
		} catch (IOException e) {
		}
		
		return true;
	}

	/**
	 * {@inheritDoc}
	 * This is NOT tolerant of errors in the input stream.
	 * @return the next file entry, or null if there are no more.
	 * @throws InvalidEntryException if a parsing error is encountered while reading from the stream
	 */
	@Override
	public FileEntry dequeue() throws InvalidEntryException {
		while (true) {
			try {
				FileEntry entry = null;
				skipAllNonData();
				if (readChar() != -1) {
					unreadChar();
					entry = new FileEntry(source);
				}
				return entry;
			} catch (Exception e) {
				throw new InvalidEntryException("Problem when trying to read the next entry", e);
			}
		}
	}

	protected void skipAllNonData() throws IOException {
		while (true) {
			skipWhitespace();
			int aChar = readChar();
			switch (aChar) {
				case '#':
					skipWhitespace();
					String word = source.readWord();
					if (!word.equals("")) {
						skipWhitespace();
						if (word.equalsIgnoreCase(ArchiveQueue.DIR_COMMENT)) {
							basePath = source.readPath();
						} else if (word.equalsIgnoreCase(ArchiveQueue.START_COMMENT)) {
							startTime = source.readDate();
						}
					}
					while ((!isEol(aChar)) && (aChar != -1)) {
						aChar = readChar();
					}
					break;
				case '\n':
				case '\r':
					break;
				default:
					unreadChar();
					return;
			}
		}
	}

	
	/**
	 * Skips over whitespace.
	 * 
	 * @throws IOException If an IO exception happens.
	 */
	private void skipWhitespace() throws IOException {
		int aChar = readChar();

		while ((aChar != -1) && ((aChar == ' ') || (aChar == '\t'))) {
			aChar = readChar();
		}

		if (aChar != -1) {
			unreadChar();
		}
	}

	/**
	 * Reads a character from the input, putting a 1 character mark on
	 * the reader before doing so.
	 * 
	 * @return The character
	 */
	private int readChar() throws IOException {
		source.mark(1);
		return source.read();
	}

	/**
	 * Unreads the character that readChar() returned
	 */
	private void unreadChar() throws IOException {
		source.reset();
	}

	/**
	 * @param aChar A character value
	 * @return true if the character is a newline or carriage return
	 */
	private boolean isEol(int aChar) {
		return ((aChar == '\n') || (aChar == '\r'));
	}
}
