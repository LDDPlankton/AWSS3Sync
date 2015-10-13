package com.awss3sync.main;

import java.io.File;
import java.util.List;
import java.util.UUID;

import com.amazonaws.services.s3.model.Bucket;
import com.awss3.s3library.AWSToolKitS3Bucket;
import com.awss3.s3library.AWSToolKitS3BucketObjectDSO;
import com.awss3.s3library.AWSToolKitS3Utility;
import com.awss3sync.library.FileListCreator;

public class ConsoleControl
{
	private AWSToolKitS3Bucket bucket = null;
	
	public ConsoleControl()
	{
		this.bucket = new AWSToolKitS3Bucket();	
	}
	
	public void syncToRemoteLocation(String fileOrDirectory, String bucketName)
	{
		Bucket myBucket = null;
		//String mybucketName = "";
		
		//1. CHECK IF ANY BUCKETS EXIST + GET NAME
		boolean foundBucket = false;
		List<Bucket> bList = this.bucket.listBuckets();
		if(bList.size() <= 0)
		{
			System.out.println("There are no Buckets in your AWS S3 Account! Please re-run with the -nbrsync Option!");
			System.exit(-1);
		}
		for(Bucket i : bList)
		{
			if(i.getName().equals(bucketName))
			{
				foundBucket = true;
				myBucket = i;
				//bucketName = i.getName();
			}
		}
		if(!foundBucket)
		{
			System.out.println( String.format("There is no bucket associated with %s!", bucketName) );
			System.exit(-1);
		}
		
        //2. BUILD FILE LIST
		FileListCreator flc = new FileListCreator();
		flc.buildFileSyncList(fileOrDirectory);
		List<String> fileList = flc.getFileList();
		System.out.println( String.format("File list built. %d Files To Upload", fileList.size()) );
		
		//3. UPLOAD FILES
		for(String fileToUpload : fileList)
		{
			//SPLIT PATH TO GET FILENAME
			String[] parts = fileToUpload.split( File.separator + File.separator);
			String filename = parts[parts.length -1];
			
			//CONVERT FILE TO AWS COMPATIBLE
			String fileBase = AWSToolKitS3Utility.removeBaseDirFromFilePath(fileOrDirectory, fileToUpload);	//WILL STRIP BASE PATH FROM FULL FILE PATH
			//IF TRANSFERING A FILE, LENGTH==0, SO SET TO FILENAME
			if(fileBase.length() == 0)
				fileBase = filename;
			String fileAWSBase = AWSToolKitS3Utility.convertFilePathToAWSCompatible(fileBase);
			
			//UPLOAD FILE
			boolean status = this.bucket.uploadToBucket(bucketName, fileAWSBase, fileToUpload);
			if(status)
				System.out.println( String.format("File [%s] Upload OK...", fileToUpload) );
			else
				System.out.println( String.format("File [%s] Upload FAILED...", fileToUpload) );
		}
		System.out.println("All files transfered!");
	}
	
	public void syncToRemoteLocationWithNewBucket(String fileOrDirectory)
	{
        //1. CREATE NEW BUCKET
		String bucketName = "awss3remotebucket" + UUID.randomUUID();
        System.out.println("Trying to create creating bucket: " + bucketName);
        boolean status = this.bucket.createBucket(bucketName);
        if(!status)
        {
        	System.out.println(this.bucket.getErrorInformation().getErrorMessage());
        	System.exit(-1);
        }
        System.out.println("Created bucket! Now building file list...");
        
        //2. BUILD FILE LIST
		FileListCreator flc = new FileListCreator();
		flc.buildFileSyncList(fileOrDirectory);
		List<String> fileList = flc.getFileList();
		System.out.println( String.format("File list built. %d Files To Upload", fileList.size()) );
		
		//3. UPLOAD FILES
		for(String fileToUpload : fileList)
		{
			//SPLIT PATH TO GET FILENAME
			String[] parts = fileToUpload.split( File.separator + File.separator);
			String filename = parts[parts.length -1];
			
			//CONVERT FILE TO AWS COMPATIBLE [C:\\... -> FOLDER\FILE.txt]
			String fileBase = AWSToolKitS3Utility.removeBaseDirFromFilePath(fileOrDirectory, fileToUpload);	//WILL STRIP BASE PATH FROM FULL FILE PATH
			//IF TRANSFERING A FILE, LENGTH==0, SO SET TO FILENAME
			if(fileBase.length() == 0)
				fileBase = filename;
			String fileAWSBase = AWSToolKitS3Utility.convertFilePathToAWSCompatible(fileBase);
			
			//UPLOAD FILE
			status = this.bucket.uploadToBucket(bucketName, fileAWSBase, fileToUpload);
			if(status)
				System.out.println( String.format("File [%s] Upload OK...", fileToUpload) );
			else
				System.out.println( String.format("File [%s] Upload FAILED...", fileToUpload) );
		}
		System.out.println("All files transfered!");
        
	}
	
	public void syncBucketToLocalLocation(String bucketName, String destination)
	{
		List<AWSToolKitS3BucketObjectDSO> remoteFileList = this.bucket.listObjectsFromBucket(bucketName);
		for(AWSToolKitS3BucketObjectDSO i : remoteFileList)
		{
			//ATTEMPT TO DETERMINE IF NEED TO CREATE A FOLDER!
			String[] dirParts = i.name.split("/");
			if(dirParts.length > 1)
			{
				//BUILD DIR PATH
				String dirPath = "";
				for(int p = 0; p < dirParts.length-1; p++)
					dirPath += dirParts[p] + File.separator;
				dirPath = dirPath.substring(0, dirPath.length()-1);

				//MAKE DIR IF NEEDED
				File localDir = new File(destination + File.separator + dirPath);
				if(!localDir.exists())
					localDir.mkdirs();
			}
			
			//DOWNLOAD
			boolean status = this.bucket.downloadFromBucket(bucketName, i.name, destination + File.separator + i.name);
			if(!status)
			{
				System.out.println(this.bucket.getErrorInformation().getErrorMessage());
			}
			else
			{
				System.out.println(String.format("Saved %s to %s", i.name, destination + File.separator + i.name));
			}
		}
		
	}
	
	public boolean syncFileToLocalLocation(String bucketName, String key, String destination)
	{
		boolean found = false;
		key = AWSToolKitS3Utility.convertFilePathToAWSCompatible(key);	//CONVERT PATH TO FILE TO AWS COMPATIBLE
		
		List<AWSToolKitS3BucketObjectDSO> remoteFileList = this.bucket.listObjectsFromBucket(bucketName);
		for(AWSToolKitS3BucketObjectDSO i : remoteFileList)
		{
			if(i.name.equals(key))
			{
				found = true;
				break;
			}
		}
		
		//IF NOT FOUND EXIT
		if(!found)
		{
			System.out.println("Unable to locate key: " + key );
			return false;
		}
		
		//GET LAST PART OF FILENAME [REMOVE FOLDERS FROM PATH] + CALC NEW FULL FILE PATH FOR FILE
		String[] fileParts = key.split("/");
		String newFileDestination = destination + File.separator + fileParts[fileParts.length-1];

		//DOWNLOAD
		boolean status = this.bucket.downloadFromBucket(bucketName, key, newFileDestination);
		if(!status)
		{
			System.out.println(this.bucket.getErrorInformation().getErrorMessage());
		}
		else
		{
			System.out.println(String.format("Saved %s to %s", key, newFileDestination ));
		}
		return true;
	}
	
	public void listBuckets()
	{
		System.out.println("Listing Buckets...");
        List<Bucket> bList = this.bucket.listBuckets();
        for(Bucket i : bList)
        {
        	System.out.println("Bucket List Item =" + i.getName() + "|" + i.getOwner() );
        }
        System.out.println("Listing Complete!");
	}
	
	public void bucketViewer(String bucketName, String folder)
	{
		System.out.println("Listing Objects in Bucket...");
		List<AWSToolKitS3BucketObjectDSO> objectList;
		if(folder.equals("/"))
			objectList = this.bucket.listObjectsFromBucket(bucketName);
		else
			objectList = this.bucket.listObjectsFromBucketInFolder(bucketName, AWSToolKitS3Utility.convertFolderPathToAWSCompatible(folder) );
		if(objectList == null)
		{
			System.out.println(this.bucket.getErrorInformation().getErrorMessage());
			System.exit(-1);
		}
		for(AWSToolKitS3BucketObjectDSO i : objectList)
		{
			String line = String.format("- %s (size=%d) (folder=%s)", i.name, i.size, i.isFolder);
            System.out.println(line);
		}
		System.out.println(objectList.size());
		System.out.println("Listing Complete!");
	}
}
