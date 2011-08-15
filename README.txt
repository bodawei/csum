CSum

This is a simple program used to compute listings of files with associated checksums, and to compare two such listings in a structured way, reporting what has been added, removed, changed or moved.  This is solving the problem I have of managing sets of archived files, allowing me to validate that backups or restores didn't corrupt anything, allowing me to reorganize the files and be sure I didn't accidentally delete anything etc.

To use:
- build with maven or netbeans
- put the script csum in the same directory as the jar file (csum-1.0.jar)
- run the script like this:
	csum directory1 > archive-listing.txt
or
	csum -a directory1 > archive-listing.txt
(the latter includes annoying macos files like .DS_Store)

- You can also run it like this any of these
	csum directory1 directory2
	csum directory1 archive-listing.txt
	csum archive-listing.txt directory2
	csum archive-listing1.txt archive-listing2.txt
With each of these, you can also specify:
	-c : show the changed files
	-a : show the added files
	-r : show the removed files
	-m : show the moved or renamed files
	-s : show the "same" files (the ones that are the same in both)