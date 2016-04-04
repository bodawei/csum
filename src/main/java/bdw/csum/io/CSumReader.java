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
package bdw.csum.io;

import bdw.util.ByteArrayBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A buffered input stream reader which can parse
 * several of the structures this project needs
 */
public class CSumReader extends BufferedReader {
	/**
	 * Constructs an instance using the input stream
	 * as a data source.
	 * 
	 * @param stream The data source
	 */
	public CSumReader(InputStream stream) {
		super(new InputStreamReader(stream, Charset.forName("UTF-8")));
	}

	/**
	 * Reads a series of hexadecimal characters, returning
	 * them in a byte array.
	 * This actually skips leading and trailing white space, and
	 * reads an even number of hexadecimal digits. It stops
	 * when encountering a non-hexadecimal digit, whitespace, or
	 * a final hexadecimal digit if that is the first nibble of a byte.
	 * 
	 * @return The checksum, or null if none could be found
	 * @throws IOException  If an IO error happens.
	 */
	public byte[] readHexString() throws IOException {
		ByteArrayBuilder builder = new ByteArrayBuilder();

		skipWhitespace();
		while (true) {
			mark(2);
			int hiNibbleChar = read();
			int loNibbleChar = read();

			if ((hiNibbleChar == -1) || (loNibbleChar == -1)) {
				reset();
				return builder.toByteArray();
			}
			
			int hiNibble = nibbleCharToInt(hiNibbleChar);
			int loNibble = nibbleCharToInt(loNibbleChar);

			if ((hiNibble == -1) || (loNibble == -1)) {
				reset();
				skipWhitespace();
				return builder.toByteArray();
			}
			
			builder.append((byte) ((hiNibble << 4) | loNibble));
		}
	}

	/**
	 * Reads in a pathname.  Leading and trailing whitespace is discarded.
	 * Backslash can escape any characters (to represent themselves) except
	 * \n and \r which represent new line and carriage return. Stops
	 * at end of line or end of file.
	 * 
	 * If the pathname starts with a ", then this assumes the whole thing
	 * is quoted, and will stop at an unquoted ". in this case, the quotes
	 * are not considered aprt of the path, and are not returned.
	 * 
	 * Note: A \ before eof or eoln is left in the pathname
	 * 
	 * @return The string found (or null if none read)
	 * @throws IOException 
	 */
	public String readPath() throws IOException {
		StringBuilder builder = new StringBuilder();
		int aChar;
		boolean doingAnEscape = false;
		boolean expectTrailing = false;
		boolean done = false;

		skipWhitespace();
		mark(1);
		aChar = read();
		
		if(aChar =='\"') {
			expectTrailing = true;
			mark(1);
			aChar = read();
		}

		while ((aChar != -1) && (!done)) {
			if (doingAnEscape == true) {
				switch (aChar) {
					case 'r':
						aChar = '\r';
						break;
					case 'n':
						aChar = '\n';
						break;
				}
				doingAnEscape = false;
				builder.append((char)aChar);
			} else {
				switch (aChar) {
					case '\\':
						doingAnEscape = true;
						break;
					case '\"':
						if (expectTrailing) {
							skipWhitespace();
							done = true;
						} else {
							builder.append((char)aChar);
						}
						break;
					case '\n':
					case '\r':
						reset();
						done = true;
						break;
					default:
						builder.append((char)aChar);
						break;
				}
			}
			if (done == false) {
				mark(1);
				aChar = read();
			} else {
				skipWhitespace();
			}
		}

		// If we were in the middle of what we thought was an
		// escape sequence, and we found the end of the path, then
		// put the slash back on it.
		if (doingAnEscape == true) {
			builder.append('\\');
		}

		return builder.toString();
	}
	
	/**
	 * Tries to read a date in in the form yyyy.MM.dd.HH.mm.ss.SSS.
	 * Skips leading and trailing whitespace.
	 * 
	 * @return The date
	 * @throws IOException If something goes wrong when trying to read this
	 */
	public Date readDate() throws IOException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS");
		skipWhitespace();

		StringBuilder buffer = new StringBuilder();
		mark(23);
		int aChar;

		for (int index = 0; index < 23; index++) {
			aChar = read();
			if (aChar != -1) {
				buffer.append((char)aChar);
			}
		}

		skipWhitespace();

		try {
			return formatter.parse(buffer.toString());
		} catch (ParseException e) {
			reset();
			throw new IOException("Parse problem", e);
		}
	}

	/**
	 * Reads a space-terminated (or eoln/eof terminated) string
	 * and returns it. This skips leading and trailing whitespace
	 * 
	 * @return A possibly empty space-terminated word
	 * @throws IOException 
	 */
	public String readWord() throws IOException {
		StringBuilder buffer = new StringBuilder();
		skipWhitespace();
		mark(1);
		int aChar = read();

		while ((aChar != '\n') && (aChar != '\r') && (aChar != ' ') && (aChar != '\t') && (aChar != -1)) {
			buffer.append((char)aChar);
			mark(1);
			aChar = read();
		}
		
		reset();
		skipWhitespace();
		return buffer.toString();
	}

	/**
	 * Given a character, try to interpret it as a nibble (0x0-0xf)
	 * and return the integer version of it.  Otherwise return -1
	 * 
	 * @param nibbleChar The character to interpret
	 * @return 0-15, or -1
	 */
	private int nibbleCharToInt(int nibbleChar) {
		switch (nibbleChar) {
			case '0':
				return 0;
			case '1':
				return 1;
			case '2':
				return 2;
			case '3':
				return 3;
			case '4':
				return 4;
			case '5':
				return 5;
			case '6':
				return 6;
			case '7':
				return 7;
			case '8':
				return 8;
			case '9':
				return 9;
			case 'a':
			case 'A':
				return 10;
			case 'b':
			case 'B':
				return 11;
			case 'c':
			case 'C':
				return 12;
			case 'd':
			case 'D':
				return 13;
			case 'e':
			case 'E':
				return 14;
			case 'f':
			case 'F':
				return 15;
			default:
				return -1;
		}
	}

	private void skipWhitespace() throws IOException {
		mark(1);
		int aChar = read();
		
		while ((aChar == ' ') || (aChar == '\t')) {
			mark(1);
			aChar = read();
		}
		
		reset();
	}

}
