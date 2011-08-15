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

/**
 * Simple exception class for when problems are encountered when parsing FileEntries
 */
public class InvalidEntryException extends Exception {
	public InvalidEntryException(String message) {
		super(message);
	}
	public InvalidEntryException(String message, Exception related) {
		super(message, related);
	}
}
