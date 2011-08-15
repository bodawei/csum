package bdw.testutils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

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
}
