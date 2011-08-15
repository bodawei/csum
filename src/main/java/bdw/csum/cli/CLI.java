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

package bdw.csum.cli;

import bdw.csum.entry.InvalidEntryException;
import bdw.csum.queue.ArchiveQueue;
import bdw.csum.Resolver;
import bdw.csum.entry.FileEntry;
import bdw.csum.entry.MovedEntry;
import bdw.csum.queue.FSQueue;
import bdw.csum.io.BuilderUtils;
import bdw.csum.queue.EntryQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Provides a main class that can be invoked from elsewhere.  Expects
 * two arguments, paths to files to compare.  Prints out a summary of changes.
 */
public class CLI {

	/**
	 * Main entry point.
	 * Arguments:
	 *		[-a] path  : list all files (even mac special ones) in the specified path
	 *    path1 path2:  Report on all changed, added, removed, and moved files between path1 and path 2.
	 *    [-c] [-a] [-r] [-m] [-s] path1 path2:  if at least one flag is specified,
	 *			the list the Changed, Added, Removed, Moved, or Same files
	 * @param args Command line arguments
	 */
	public static void main(String[] args) throws InvalidEntryException {
		if (args.length <= 0) {
			System.err.println("# Specify one path to get a listing of it (-a to get all files, including special mac ones)");
			System.err.println("#  or");
			System.err.println("# Specify two path to get the differences between them (one or both paths may be to an archive which was written out from the above");
			System.err.println("#   If specify any of -c (changed), -a (added), -r (removed), -m (moved or renamed), -s (same. that is entirely unchanged) then only those will be shown. If none specified, equivalent to -c -a -r -m");
			System.exit(0);
		}
		
		CLI cli = new CLI();
		
		// This is really gross.
		try {
			if (args.length == 1) {
				Writer writer = new OutputStreamWriter(System.out, Charset.forName("UTF-8"));
				cli.listDirectory(args[0], false, writer);
				writer.flush();
				System.exit(0);
			} else if (args.length == 2) {
				if (args[0].equals("-a")) {
					Writer writer = new OutputStreamWriter(System.out, Charset.forName("UTF-8"));
					cli.listDirectory(args[0], true, writer);
					writer.flush();
					System.exit(0);
				}
			}
			cli.prepForCompare(args);
		} catch (Exception e) {
			System.err.println("Got an exception: ");
			e.printStackTrace(System.err);
		}
	}

	
	private boolean showAdded;
	private boolean showChanged;
	private boolean showMoved;
	private boolean showSame;
	private boolean showRemoved;

	/**
	 * Write out an Archive file starting from the specified path
	 * @param path
	 * @param all
	 * @param writer
	 * @throws InvalidEntryException 
	 */
	public void listDirectory(String path, boolean all, Writer writer) throws InvalidEntryException, IOException {
		FSQueue queue = new FSQueue(path);	
		BuilderUtils utils = new BuilderUtils();
		StringBuilder builder = new StringBuilder();

		queue.setIgnoreSpecialMacFiles(!all);
		
		writer.write("# CSum\n");
		writer.write("# Version 1\n");

		builder.append("# " + ArchiveQueue.DIR_COMMENT + " ");
		utils.appendPath(builder, queue.getBasePath());
		writer.write(builder.toString() +  "\n");

		builder.append("# " + ArchiveQueue.START_COMMENT + " ");
		utils.appendDate(builder, queue.getStartTime());
		writer.write(builder.toString() +  "\n");
		while (!queue.isEmpty()) {
			FileEntry entry = queue.dequeue();
			writer.write(entry.toString() +  "\n");
		}
	}

	
	/**
	 * Processes command line arguments in preparation for comparing the
	 * entries in two queues.
	 * 
	 * @param args The command line arguments
	 */
	protected void prepForCompare(String[] args) throws IOException, InvalidEntryException {
		boolean setOneOption = false;
		List<String> paths = new ArrayList<String>();
		
		for (String arg : args) {
			if (arg.equals("-a")) {
				showAdded = true;
				setOneOption = true;
			} else if (arg.equals("-c")) {
				showChanged = true;
				setOneOption = true;
			} else if (arg.equals("-m")) {
				showMoved = true;
				setOneOption = true;
			} else if (arg.equals("-r")) {
				showRemoved = true;
				setOneOption = true;
			} else if (arg.equals("-s")) {
				showSame = true;
				setOneOption = true;
			} else {
				paths.add(arg);
			}
		}
		
		if (setOneOption == false) {
			showAdded = true;
			showChanged = true;
			showMoved = true;
			showRemoved = true;
			showSame = false;
		}
		
		if (paths.size() != 2) {
			System.err.println("Must specify two paths to get a difference");
			System.exit(1);
		}
		
		compare(paths.get(0), paths.get(1), new OutputStreamWriter(System.out, Charset.forName("UTF-8")));
	}

	/**
	 * Compare the entries in the two paths
	 * 
	 * @param path1 path to an archive or a directory
	 * @param path2 path to an archive or a directory
	 * @throws IOException 
	 */
	public void compare(String path1, String path2, Writer writer) throws IOException, InvalidEntryException {
		File f1 = new File(path1);
		EntryQueue oldQueue;
		if (f1.isDirectory()) {
			oldQueue = new FSQueue(path1);
		} else {
			oldQueue = new ArchiveQueue(new FileInputStream(path1));
		}
		
		File f2 = new File(path2);
		EntryQueue newQueue;
		if (f2.isDirectory()) {
			newQueue = new FSQueue(path2);
		} else {
			newQueue = new ArchiveQueue(new FileInputStream(path2));
		}
		
		Resolver resolver = new Resolver(oldQueue, newQueue);
		ArrayList<FileEntry> entries;
		
		if (showChanged) {
			writeEntries("Changed Files", resolver.getChangedFiles(), writer);
		}
				
		if (showAdded) {
			writeEntries("Added Files", resolver.getAddedFiles(), writer);
		}

		if (showRemoved) {
			writeEntries("Removed Files", resolver.getRemovedFiles(), writer);
		}

		if (showMoved) {
			writer.write("\nMoved or Renamed Files\n");
			writer.write("--------------------\n");
			entries = new ArrayList<FileEntry>(resolver.getMovedOrRenamedFiles());
			Collections.sort(entries, new EntryComparator());
			for (FileEntry entry : entries) {
				MovedEntry mEntry = (MovedEntry) entry;
				writer.write(mEntry.getPathname() + "\n\t\tmoved to: " +
						mEntry.getNewPathname() + "\n");
			}
		}

		if (showSame) {
			writeEntries("Unchanged Files", resolver.getUnchangedFiles(), writer);
		}
		writer.flush();
	}
	
	/**
	 * Convenience routine to write out a bunch of entries
	 */
	protected void writeEntries(String name, Set<FileEntry> set, Writer writer) throws IOException {
		writer.write("\n" + name + "\n");
		writer.write("--------------------\n");
		ArrayList<FileEntry> entries = new ArrayList<FileEntry>(set);
		Collections.sort(entries, new EntryComparator());
		for (FileEntry entry : entries) {
			writer.write(entry.getPathname()+"\n");
		}
	}

	/**
	 * Simple class used to compare two FileEntries so we can sort them.
	 */
	protected class EntryComparator implements Comparator<FileEntry> {
		@Override
		public int compare(FileEntry t, FileEntry t1) {
			return (t.getPathname().compareTo(t1.getPathname()));
		}
		
	}
}
