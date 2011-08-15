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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This wishes it was a subclass of StringBuilder, but that isn't possible.
 * So, this provides some utilities to append csum-relatd data to a
 * string builder.
 */
public class BuilderUtils {
	
	/**
	 * @param date a date value (not null)
	 * @return A string version of the date formatted in a system and locale
	 * independent fashion.
	 */
	public void appendDate(StringBuilder builder, Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS");
		
		if (date == null) {
			builder.append("null");
		} else {
			builder.append(formatter.format(date));
		}
	}
	
	/**
	 * Formats a pathname so it can be written out.
	 * The current plan is that it should be copy/paste-able into a shell
	 * without problems.  Note, however, that CR and LF in it are quoted in
	 * a way that will make them not usable in that way.
	 * 
	 * @param path a pathname to format
	 * @return A copy of the input path ready to be written out.
	 */
	public void appendPath(StringBuilder builder, String path) {		
		builder.append("\"");
		for (int index = 0; index < path.length(); index++) {
			switch (path.charAt(index)) {
				case '\"':
					builder.append("\\\"");
					break;
				case '\\':
					builder.append("\\\\");
					break;
				case '\n':
					builder.append("\\n");
					break;
				case '\r':
					builder.append("\\r");
					break;
				default:
					builder.append(path.charAt(index));
					break;
			}
		}
		builder.append("\"");
	}

	/**
	 * Given an array of bytes, write them out as a hex string
	 * @param builder The string builder to append to
	 * @param bytes The byte array to write
	 */
	public void appendHexString(StringBuilder builder, byte[] bytes) {
		
		for (int index = 0; index < bytes.length; index++) {
			builder.append(nibbleToChar((bytes[index] & 0xF0) >> 4));
			builder.append(nibbleToChar(bytes[index] & 0x0F));
		}
	}
	
	/**
	 * Given a nibble (int value 0-15), return the hex character
	 * equivalent.
	 * 
	 * @param nibble the nibble to convert
	 * @return the converted nibbld
	 */
	protected char nibbleToChar(int nibble) {
		if (nibble < 10) {
			return (char) ('0' + nibble);
		} else {
			return (char) ('a' + (nibble -10));
		}
	}
}
