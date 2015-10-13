package com.awss3.s3library;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.ld.sharedlibrary.lib.ErrorManager;

public class AWSToolKitS3Bucket
{
	private AmazonS3 s3 = null;
	private ErrorManager errorManager = null;
	
	public AWSToolKitS3Bucket()
	{
		this.s3 = AmazonS3ClientWrapper.getInstance();
		this.errorManager = new ErrorManager();
	}
	
	public ErrorManager getErrorInformation()
	{
		return this.errorManager;
	}
	
	/*
	 * This function will create a new Bucket to store data, name must be Globally Unique.
	 * 
	 * @param	bucketName		The name of the bucket to create.
	 */
	public boolean createBucket(String bucketName)
	{
		try
		{
			Bucket r = this.s3.createBucket(bucketName);
			return true;
		}
		catch(Exception e)
		{
			this.errorManager.setErrorMessage("Create Bucket Exception: " + e.getMessage() );
			return false;
		}
	}
	
	/*
	 * This function will delete a bucket.
	 * 
	 * @param	bucketName		The bucket name to delete.
	 */
	public boolean deleteBucket(String bucketName)
	{
		try
		{
			this.s3.deleteBucket(bucketName);
			return true;
		}
		catch(Exception e)
		{
			this.errorManager.setErrorMessage("Delete Bucket Exception: " + e.getMessage());
			return false;
		}
	}
	
	public void createFolder(String bucketName, String key)
	{
		//SET FOLDER METADATA
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(0);

		//GENERATE EMPTY CONTENT FOR FOLDER
		InputStream emptyFolderStream = new ByteArrayInputStream(new byte[0]);
		
		this.s3.putObject(new PutObjectRequest(bucketName, key, emptyFolderStream, metadata));
	}
	
	public boolean deleteFolder(String bucketName, String key)
	{
		if(!this.isFolderExist(bucketName, key) )
		{
			this.errorManager.setErrorMessage(String.format("The folder %s does not exist!", key) );
			return false;
		}
		
		List<AWSToolKitS3BucketObjectDSO> objectList = this.listObjectsFromBucketInFolder(bucketName, key);
		if(objectList == null)
			return false;

		//IF THERE ARE OBJECTS TO DELETE
		if(objectList.size() > 0)
		{
			for(AWSToolKitS3BucketObjectDSO i : objectList)
			{	
				//IF WE FOUND A FOLDER THAT IS THE SAME NAME AS OUR SPECIFIED PARAM ... IGNORE AS WE CANNOT YET DELETE THE ROOT PATH [.]
				if(i.isFolder && (i.name.equals(key)) )
					continue;
				else if(i.isFolder)
					this.deleteFolder(bucketName, i.name);	//DELETE SUBFOLDER
				
				//System.out.println("Deleting file: " + i.name);
				this.s3.deleteObject(bucketName, i.name);
			}
			//System.out.println("Deleting folder: " + key);
			this.s3.deleteObject(bucketName, key);
			return true;
		}
		else
		{
			this.errorManager.setErrorMessage("There are no objects to delete!");
			return true;
		}
	}
	
	/*
	 * This function will list all Buckets in the Account.
	 * 
	 * @return	List<Bucket>
	 */
	public List<Bucket> listBuckets()
	{
        List<Bucket> bucketList = this.s3.listBuckets();
        return bucketList;
	}
	
	public boolean uploadToBucket(String bucketName, String key, String file)
	{
		File myFile = new File(file);
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(0);
/*		
		//SPLIT KEY PATH TO DETERMINE IF FOLDERS EXIST
		String folders[] = key.split("/");
		String folderName = "";
		for(String tmpFolder : folders)
		{
			folderName += tmpFolder;
			
			//IF FOLDER NOT EXIST ... CREATE IT
			if(!this.isFolderExist(bucketName, AWSToolKitS3Utility.convertFolderPathToAWSCompatible(folderName)) )
				this.createFolder(bucketName, folderName);
		}
*/
/*
TransferManager tx = new TransferManager(credentials);
Upload upload = tx.upload(bucketName, myFile.getName(), myFile);
*/

		try
		{
			//OPEN FILE + DECLARE VARIABLES
			FileReader fr = new FileReader(myFile);
			char buf[] = new char[1024];
			int offset = 0;
			int length = 1024;
			int n;
			String fileContent = "";
			//LOOP TO OPEN + READ DATA INTO BUFFER
			while( (n = fr.read(buf, offset, length)) != -1)
			{
				fileContent += new String(buf);
			}
			fileContent = fileContent.trim();					//TRIM OR BUFFER WILL BE FULL OF EMPTY SPACE
			//SET CONTENT LENGTH
			metadata.setContentLength(fileContent.length());
			
			//DECLARE INPUT STREAM WITH FILE BYTE DATA
			InputStream input = new ByteArrayInputStream(fileContent.getBytes());

			this.s3.putObject(new PutObjectRequest(bucketName, key, input, metadata));
			
			return true;
		}
		catch (Exception e)
		{
			this.errorManager.setErrorMessage("Bucket Upload Exception: " + e.getMessage());
			return false;
		}
	}
	
	public boolean downloadFromBucket(String bucketName, String key, String destination)
	{
        S3Object object = this.s3.getObject(new GetObjectRequest(bucketName, key));
        try 
        {
			FileOutputStream output = new FileOutputStream(destination);
			byte buff[] = new byte[4096];
			int readBytes;
			while( (readBytes = object.getObjectContent().read(buff)) != -1)
			{
				output.write(buff, 0, readBytes);
			}
			output.close();
			return true;
		}
        catch (Exception e)
        {
        	this.errorManager.setErrorMessage("Download from Bucket Exception: " + e.getMessage());
        	return false;
		}        
	}
	
	public void deleteObjectFromBucket(String bucketName, String filename)
	{
		this.s3.deleteObject(bucketName, filename);
	}
	
	public List<AWSToolKitS3BucketObjectDSO> listObjectsFromBucket(String bucketName)
	{
		List<AWSToolKitS3BucketObjectDSO> objectList = new ArrayList<AWSToolKitS3BucketObjectDSO>();
		try
		{
        ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName));
        
        //IF LISTING IN TRUNCATED ... MUST FETCH BY BATCH
        if(objectListing.isTruncated())
        {
        	//LOOP AS LONG AS LISTINGS ARE BEING TRUNCATED
        	while (objectListing.isTruncated())
        	{
        		objectListing = s3.listNextBatchOfObjects (objectListing);				//GET NEXT BATCH
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries())
                {
                	AWSToolKitS3BucketObjectDSO obj = new AWSToolKitS3BucketObjectDSO();
                	obj.name = objectSummary.getKey();
                	obj.size = objectSummary.getSize();
                	obj.isFolder = AWSToolKitS3Utility.isFolderETag(objectSummary.getETag());
                	objectList.add(obj);
                }
        	}
        }
        else
        {
	        //LOOP THROUGH OBJECT LISTING ... NOT BEING TRUNCATED
	        
	        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries())
	        {
	        	AWSToolKitS3BucketObjectDSO obj = new AWSToolKitS3BucketObjectDSO();
	        	obj.name = objectSummary.getKey();
	        	obj.size = objectSummary.getSize();
	        	obj.isFolder = AWSToolKitS3Utility.isFolderETag(objectSummary.getETag());
	        	objectList.add(obj);
	        }
        }
		}
		catch(Exception e)
		{
			this.errorManager.setErrorMessage("List Objects From Bucket Exception: " + e.getMessage());
			return null;
		}
        return objectList;
	}
	
	public List<AWSToolKitS3BucketObjectDSO> listObjectsFromBucketInFolder(String bucketName, String folderName)
	{
		List<AWSToolKitS3BucketObjectDSO> objectList = new ArrayList<AWSToolKitS3BucketObjectDSO>();
		try
		{
        ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName).withPrefix(folderName));
        
        //LOOP THROUGH OBJECT LISTING
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries())
        {
        	AWSToolKitS3BucketObjectDSO obj = new AWSToolKitS3BucketObjectDSO();
        	obj.name = objectSummary.getKey();
        	obj.size = objectSummary.getSize();
        	obj.isFolder = AWSToolKitS3Utility.isFolderETag(objectSummary.getETag());
        	objectList.add(obj);
        }
		}
		catch(Exception e)
		{
			this.errorManager.setErrorMessage("List Objects From Bucket In Folder Exception: " + e.getMessage());
			return null;
		}
        return objectList;
	}
	
	/*
	 * This function will determine if an Object's Key Exists. It will then check if the ETag matches a folder.
	 * 
	 * @param	bucketName		The bucketname we wish to check against.
	 * @param	key				The name of the Key we want to scan/compare against to check it's existence.
	 * @return	Boolean
	 */
	public boolean isFolderExist(String bucketName, String key)
	{
		try
		{
			ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName).withPrefix(key));
			if( objectListing.getObjectSummaries().size() > 0 )
				return true;
			else 
				return false;
		}
		catch(Exception e)
		{
			this.errorManager.setErrorMessage("Is Folder Exist Exception: " + e.getMessage() );
			return false;
		}		
	}
	
	/*
	 * This function will determine if an Object's Key Exists.
	 * 
	 * @param	bucketName		The bucketname we wish to check against.
	 * @param	key				The name of the Key we want to scan/compare against to check it's existence.
	 * @return	Boolean
	 */
	public boolean isObjectExist(String bucketName, String key)
	{
		try
		{
			S3Object object = this.s3.getObject(bucketName, key);
			return true;
		}
		catch(Exception e)
		{
			this.errorManager.setErrorMessage("Is Object Exist Exception: " + e.getMessage() );
			return false;
		}
	}
	
}
