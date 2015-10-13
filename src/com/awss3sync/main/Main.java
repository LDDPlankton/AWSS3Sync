package com.awss3sync.main;

import java.io.IOException;

import com.amazonaws.auth.BasicSessionCredentials;
import com.awss3.s3library.AWSAuthentication;
import com.awss3.s3library.AmazonS3ClientWrapper;

public class Main
{
	private static final String VERSION = "1.0.0";
	private static ConsoleControl console = new ConsoleControl();
	
	static void printMenu()
	{
		String[] options = new String[]
				{
						"trsync <File/Dir To Sync> <AWS_BUCKET>",
						"nbrsync <File or Directory>",
						"lsyncbucket <AWS_BUCKET> <Local Directory>",
						"lsyncfile <AWS_BUCKET> <AWS S3 Key> <Local Directory>",
						"listbuckets",
						"bucketviewer <AWS_BUCKET> <FOLDER>"
				};
		String[] descriptions = new String[]
				{
					"This option will Sync a File/Dir to an Remote AWS S3 Bucket.",
					"This option is similar to rsync, but will store the files in a new bucket.",
					"This option will Sync an AWS Bucket to A Local Location.",
					"This option will Sync an AWS S3 Key to A Local Location.",
					"This option will list all buckets.",
					"This option will list the content of a bucket.\n\t\t\tFOLDER should be /, /foldername, etc."
				};
		
		System.out.println("AWS S3 Sync " + VERSION + "\n"
				+ "Usage: java -jar <program> <option> <args...>\n");
		for(int i = 0; i < options.length; i++)
			System.out.println(String.format("%-55s: %s", options[i], descriptions[i]));
	}
	
	public static void init()
	{
		AWSAuthentication auth = new AWSAuthentication();
		boolean authenticationStatus = auth.processAuthentication(null, null);
		if(!authenticationStatus)
		{
			System.out.println( auth.getErrorInformation().getErrorMessage() );
			System.exit(1);
		}
		BasicSessionCredentials credentials = auth.getAuthCredentials();
		
		//SET CREDENTIALS IN WRAPPERS
		AmazonS3ClientWrapper.setInstance(credentials);
	}
	
	public static void requireNumberOfArguments(String args[], int required)
	{
		if(args.length != required)
		{
			System.out.println("Invalid Number of Arguments!");
			System.exit(1);;
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		init();
		ConsoleControl ctrl = new ConsoleControl();
		
		//ENSURE ARGUMENTS PASSED
		if(args.length < 1)
		{
			printMenu();
			System.exit(-1);
		}
		
		switch(args[0])
		{
			case "trsync":
				//HAVE REQUIRED ARGS OR DIE
				requireNumberOfArguments(args, 3);
				
				String fileOrFolder1 = args[1];
				String bucketName1 = args[2];
				ctrl.syncToRemoteLocation(fileOrFolder1, bucketName1);
				
			break;
			
			case "tnbrsync":
				//HAVE REQUIRED ARGS OR DIE
				requireNumberOfArguments(args, 2);
				
				String fileOrFolder2 = args[1];
				ctrl.syncToRemoteLocationWithNewBucket(fileOrFolder2);
			break;
			
			case "lsyncbucket":
				//HAVE REQUIRED ARGS OR DIE
				requireNumberOfArguments(args, 3);
				
				String bucketName3 = args[1];
				String localDir3 = args[2];
				ctrl.syncBucketToLocalLocation(bucketName3, localDir3);
				
			break;
			
			case "lsyncfile":
				//HAVE REQUIRED ARGS OR DIE
				requireNumberOfArguments(args, 4);
				
				String bucketName4 = args[1];
				String fileKey4 = args[2];
				String localDir4 = args[3];
				ctrl.syncFileToLocalLocation(bucketName4, fileKey4, localDir4);
				
			break;
			
			case "listbuckets":
				console.listBuckets();
			break;
			
			case "bucketviewer":
				//HAVE REQUIRED ARGS OR DIE
				requireNumberOfArguments(args, 3);
				
				String bucketName6 = args[1];
				String bucketFolder6 = args[2];

				ctrl.bucketViewer(bucketName6, bucketFolder6);
			break;
			
			default:
				printMenu();
		}
		

		
	}
}
