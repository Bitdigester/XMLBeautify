XMLBeautify
===========
Re-formats an XML file to make it human readable so it can be edited with notepad or any dumb text editor.
This program modifies raw XML files with CR LF and tab characters to graphically show nested XML elements and their 
encapsulated data.

It is implemented as a stream process in case it can be used for on-the-fly reformatting of serial 
instances of XML files like in web downloads.

To run: start a command shell. 
c:\>java -jar "xmlbeautify.jar" Input_file Output_file

@author H. Taylor phaecops@sbcglobal.net
