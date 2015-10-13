package com.awss3.s3library;

import java.util.List;
import java.util.UUID;

import com.amazonaws.services.s3.model.Bucket;

public class AWSToolKitS3UsageExample 
{
	private AWSToolKitS3Bucket bucket = null;
	
	public AWSToolKitS3UsageExample()
	{
		this.bucket = new AWSToolKitS3Bucket();
	}
	
	public void createBucket()
	{
        String bucketName = "buckettest1" + UUID.randomUUID();			//BUCKET NAMES MUST BE GLOBALLY UNIQUE
        boolean status;
        
        System.out.println("Creating bucket " + bucketName);
        status = this.bucket.createBucket(bucketName);
        if(!status)
        {
        	System.out.println(this.bucket.getErrorInformation().getErrorMessage());
        	System.exit(-1);
        }
	}
	
	public void createFolder()
	{
		String bucketName = "buckettest";
		String folder = "somefolder/";
		
		this.bucket.createFolder(bucketName, folder);
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
	
	public void listObjectsInBucket()
	{
		System.out.println("Listing Objects in Bucket...");
		String bucketName = "buckettest1";
		List<AWSToolKitS3BucketObjectDSO> objectList = this.bucket.listObjectsFromBucket(bucketName);
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
		System.out.println("Listing Complete!");
	}
	
	public void listObjectsInBucketInFolder()
	{
		System.out.println("Listing Objects in Bucket...");
		String bucketName = "buckettest1";
		String folder = "somefolder";
		List<AWSToolKitS3BucketObjectDSO> objectList = this.bucket.listObjectsFromBucketInFolder(bucketName, folder);
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
		System.out.println("Listing Complete!");
	}
	
	public void isFolderExist()
	{
		String bucketName = "buckettest1";
		String filenameOrPath = "somefolder/";
		boolean status = this.bucket.isFolderExist(bucketName, filenameOrPath);
		System.out.println(String.format("The file/folder (%s) : exist status = %s", filenameOrPath, status));
	}
	
	public void isFileExist()
	{
		String bucketName = "buckettest1";
		String filenameOrPath = "somefile.txt";
		boolean status = this.bucket.isObjectExist(bucketName, filenameOrPath);
		System.out.println(String.format("The file/folder (%s) : exist status = %s", filenameOrPath, status));
	}
	
	public void upload()
	{
		String bucketName = "buckettest1";
		String fileToUpload = "C:\\...\\TESTUPLOADAWS.txt";
		String[] parts = fileToUpload.split("\\\\");
		String filename = parts[parts.length -1];
		String bucketPath = AWSToolKitS3Utility.convertFolderPathToAWSCompatible("somefolder");
		
		boolean status = this.bucket.uploadToBucket(bucketName, bucketPath + filename, fileToUpload);
	}
	
	public void deleteFolder()
	{
		String bucketName = "buckettest1";
		String filenameOrPath = AWSToolKitS3Utility.convertFolderPathToAWSCompatible("somefolder");
		boolean delete = this.bucket.deleteFolder(bucketName, "somefolder/");
		if(delete)
			System.out.println("The folder was deleted!" );
		else
			System.out.println("The folder was not deleted! E=" + this.bucket.getErrorInformation().getErrorMessage());
	}
	
	
	
}
