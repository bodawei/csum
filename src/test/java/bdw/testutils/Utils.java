package bdw.testutils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;

/**
 *
 */
public class Utils {

	
	public byte[] makeByteArrayFromString(String rawInput) throws IOException {
		StringReader inputReader = new StringReader(rawInput);
		byte[] rawInputBytes = new byte[rawInput.length()];
		int aChar = inputReader.read();
		int index = 0;
		while (aChar != -1) {
			rawInputBytes[index] = (byte) aChar;
			aChar = inputReader.read();
			index++;
		}

		return rawInputBytes;
	}
	
	public InputStream makeInputStream(String rawInput) throws IOException {
		return new ByteArrayInputStream(makeByteArrayFromString(rawInput));
	}

	public byte[] makeByteArrayFromHexString(String rawInput) throws IOException {
		byte[] bytes = new byte[rawInput.length()/2];
		int byteIndex = 0;
		
		for (int index = 0; index < rawInput.length(); index+=2) {
			char hiNibbleChar = rawInput.charAt(index);
			char loNibbleChar = rawInput.charAt(index+1);

			byte hiNibble = nibbleCharToInt(hiNibbleChar);
			byte loNibble = nibbleCharToInt(loNibbleChar);

			bytes[byteIndex] = (byte)((hiNibble << 4) | loNibble);
			byteIndex++;
		}
		return bytes;
	}

	private byte nibbleCharToInt(char nibbleChar) {
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
		
	public Date makeDate(int year, int month, int day, int hour, int minute, int second, int ms) throws IOException {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(0);
		c.set(year, month-1, day, hour, minute, second);
		c.setTimeInMillis(c.getTime().getTime() + ms);
		return c.getTime();
	}
}
