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

package bdw.csum.entries;

import java.util.Set;
import org.junit.Test;
import bdw.csum.entry.FileEntry;
import java.util.Date;
import static junit.framework.Assert.*;

public class FileEntryTest {

	@Test
	public void constructor_PrependsDotSlash() {
		FileEntry entry = new FileEntry(new byte[0], 0, new Date(), "foo.bar.baz");
		
		assertEquals("./foo.bar.baz", entry.getPathname());
	}
}
