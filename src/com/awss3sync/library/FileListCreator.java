package com.awss3sync.library;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileListCreator
{
	private List<String> fileList;
	
	public FileListCreator()
	{
		this.fileList = new ArrayList<String>();
	}
	
	/*
	 * This function will build a list of files to sync.
	 * 
	 * @param	syncLocation	The location to scan, to build a list of all files we wish to transfer.
	 */
	public void buildFileSyncList(String syncLocation)
	{
		//CONVERT STRING TO FILE
		File fileChecker = new File(syncLocation);
		
		//IF FILE
		if(fileChecker.isFile())
			this.fileList.add(syncLocation);
		//IF DIRECTORY
		else if(fileChecker.isDirectory())
		{
			File[] fileCheckerFileList = fileChecker.listFiles();
			for(File i : fileCheckerFileList)
			{
				String fileOrFolderPath = i.getAbsolutePath();		//FULL PATH TO FILE/FOLDER
				if(i.isDirectory())
					buildFileSyncList(fileOrFolderPath);			//RECURSIVE SYNC TO BUILD LIST OF FILES IN SUBFOLDER
				else if(i.isFile())
				{
					this.fileList.add(fileOrFolderPath);					//ADD FILE TO FILE LIST
				}
			}
		}
	}
	
	public List<String> getFileList()
	{
		return this.fileList;
	}
}
