package com.awss3.s3library;

import java.io.File;

public class AWSToolKitS3Utility
{
	public AWSToolKitS3Utility()
	{
		
	}
	
	/*
	 * This function will check the folders ETag to determine if it's a Folder or not.
	 * 
	 * @param	etag	The etag from the folder to compare against known values.
	 * @return	Boolean
	 */
	public static boolean isFolderETag(String etag)
	{
		if(etag.equals("d41d8cd98f00b204e9800998ecf8427e"))
			return true;
		return false;
	}
	
	/*
	 * This function will convert a FolderPath to an AWS Compatible System.
	 * 
	 * On Linux, we can specify /home/user for example, but with AWS using buckets, we must remove leading slashes.
	 * 
	 * @return	String
	 */
	public static String convertFolderPathToAWSCompatible(String folder)
	{
		//IF WE START WITH '/' WE MUST REMOVE
		//File.separator.toCharArray()[0]
		while( folder.charAt(0) == ('/') || folder.charAt(0) == ('\\') )
			folder = folder.substring(1, folder.length());
		//FOLDER MUST END WITH '/'
		while( folder.charAt(folder.length()-1) != ('/') )
			folder = folder + "/";
		return folder;
	}
	
	/*
	 * This function will convert a File Path to an AWS Compatible System.
	 * 
	 * On Linux, we can specify /home/user/file1.txt for example, but with AWS using buckets, we must remove leading slashes.
	 * 
	 * @return	String
	 */	
	public static String convertFilePathToAWSCompatible(String file)
	{		
		//IF WE START WITH '/' WE MUST REMOVE
		while( file.charAt(0) == ('/') || file.charAt(0) == ('\\') )
			file = file.substring(1, file.length());
		
		//IF WINDOWS ... REPLACE \ WITH /
		String OS = System.getProperty("os.name");
		if(OS.startsWith("Windows"))
			file = file.replace("\\", "/");
		
		return file;		
	}
	
	/*
	 * This function will take a full file path, and remove the base location, so we can maintain consistency when transfering to AWS S3.
	 * 
	 * Ex: (/home/user/folderBase, /home/user/folderBase/file1.txt) -> /file1.txt
	 * 
	 * @param	fileOrDirectoryBase			The base location to look for files / dirs in.
	 * @param	fileOrDirToStripBaseDir		The file to transfer that needs the basedir stripped.
	 */
	public static String removeBaseDirFromFilePath(String fileOrDirectoryBase, String fileOrDirToStripBaseDir)
	{
		String tmp = fileOrDirToStripBaseDir;
		tmp = tmp.replace(fileOrDirectoryBase, "");
		return tmp;
	}
	
}
